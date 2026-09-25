#!/usr/bin/perl
# The runner's run service: runs the argv a corpus's records allow, for the study server,
# on the compose network's internal side.
#
# Written by studyforge's execution skill and mounted read-only into the runner by the
# corpus's compose file. It publishes no port. It listens only on the internal network,
# which the study server and the runner are the only members of. The editor, which
# hands a person a shell, is not on it.
#
# No credential. What reaches this service is the study server, and the one thing it
# can ask for is an argv the corpus's records already name
# (.studyforge/execution/allowed/runs, written by the study server from those records).
# That argv would run in this container anyway, on a Run, a Submit or an example's test.
#
# The wire, as studyforge.execute.remote speaks it:
#   request  fields each ended by NUL, then one more NUL
#            run  <STUDYFORGE_RUN=token> <cwd> <argv...>
#            stop <STUDYFORGE_RUN=token> <TERM|KILL>
#            ping
#   answer   run:  "O<n>\n" + n bytes of output, repeated; then "X<status>\n"
#            stop: "stopped\n"   ping: "pong\n"   refused: "X126\n" after one O frame saying why
#
# Perl, and only modules perl-base ships, because every Debian-based runner image
# carries it and nothing else is certain.
use strict;
use warnings;
use IO::Socket::INET;
use POSIX qw(_exit dup2 setsid);

my $PORT    = $ENV{STUDYFORGE_RUN_PORT} // 7123;
my $BIND    = $ENV{STUDYFORGE_RUN_BIND} || '0.0.0.0';
my $WORK    = $ENV{STUDYFORGE_RUN_WORK} || '/work';
my $ALLOWED = "$WORK/.studyforge/execution/allowed/runs";
my %SIGNALS = (TERM => 'TERM', KILL => 'KILL');
my $TOKEN   = qr/\ASTUDYFORGE_RUN=[0-9a-f]{32}\z/;
my $MAX     = 1 << 20;

$SIG{CHLD} = 'IGNORE';
$SIG{PIPE} = 'IGNORE';

my $server = IO::Socket::INET->new(
    LocalAddr => $BIND, LocalPort => $PORT, Listen => 16, ReuseAddr => 1, Proto => 'tcp',
) or die "run service: cannot listen on $PORT: $!\n";
print STDERR 'run service: listening on ' . $server->sockport . "\n";

while (1) {
    my $client = $server->accept or next;
    my $pid = fork;
    if (!defined $pid) { close $client; next }
    if ($pid == 0) { close $server; serve($client); _exit(0) }
    close $client;
}

sub serve {
    my ($client) = @_;
    my $data = '';
    while ($data !~ /\x00\x00/ && length($data) < $MAX) {
        my $read = sysread($client, my $chunk, 65536);
        last if !$read;
        $data .= $chunk;
    }
    return if $data !~ /\A(.*?\x00)\x00/s;
    my @fields = split /\x00/, $1, -1;
    pop @fields;
    my $kind = shift @fields // '';
    if ($kind eq 'ping') { send_all($client, "pong\n"); return }
    if ($kind eq 'stop') { stop(@fields); send_all($client, "stopped\n"); return }
    if ($kind eq 'run')  { run($client, @fields); return }
}

sub send_all {
    my ($client, $bytes) = @_;
    while (length $bytes) {
        my $sent = syswrite $client, $bytes;
        return 0 if !defined $sent;
        substr($bytes, 0, $sent) = '';
    }
    return 1;
}

sub refuse {
    my ($client, $why) = @_;
    my $line = "run service: $why\n";
    send_all($client, 'O' . length($line) . "\n" . $line . "X126\n");
}

sub allowed {
    my ($entry) = @_;
    open my $fh, '<:raw', $ALLOWED or return 0;
    local $/;
    my $list = <$fh>;
    close $fh;
    return 0 if !defined $list;
    for my $one (split /(?<=\x00\x00)/, $list) {
        return 1 if $one eq $entry;
    }
    return 0;
}

sub run {
    my ($client, $token, $cwd, @argv) = @_;
    if (!defined $token || $token !~ $TOKEN || !defined $cwd || !@argv) {
        return refuse($client, 'a run names a token, a directory and an argv');
    }
    if ($cwd ne '.' && ($cwd =~ m{\A/} || grep { $_ eq '' || $_ eq '.' || $_ eq '..' } split m{/}, $cwd, -1)) {
        return refuse($client, 'a run\'s directory is relative to the corpus and never climbs out');
    }
    my $entry = join('', map { "$_\x00" } $cwd, @argv) . "\x00";
    return refuse($client, 'this argv is not one the corpus\'s records name') if !allowed($entry);
    pipe(my $out, my $in) or return refuse($client, 'no pipe');
    local $SIG{CHLD} = 'DEFAULT';
    my $pid = fork;
    return refuse($client, 'no process') if !defined $pid;
    if ($pid == 0) {
        close $out;
        setsid();
        open STDIN, '<', '/dev/null';
        dup2(fileno($in), 1);
        dup2(fileno($in), 2);
        close $in;
        chdir($cwd eq '.' ? $WORK : "$WORK/$cwd") or _exit(127);
        $ENV{PYTHONDONTWRITEBYTECODE} = '1';
        $ENV{PYTHONUNBUFFERED} = '1';
        my ($name, $value) = split /=/, $token, 2;
        $ENV{$name} = $value;
        { no warnings 'exec'; exec { $argv[0] } @argv; }
        print STDERR "$argv[0]: the program could not be started (is it on PATH?)\n";
        _exit(127);
    }
    close $in;
    my $gone = 0;
    while (1) {
        my $read = sysread($out, my $chunk, 65536);
        last if !$read;
        next if $gone;
        if (!send_all($client, 'O' . length($chunk) . "\n" . $chunk)) { $gone = 1; stop($token, 'KILL') }
    }
    waitpid($pid, 0);
    my $status = $? & 127 ? 128 + ($? & 127) : $? >> 8;
    send_all($client, "X$status\n") if !$gone;
}

sub stop {
    my ($token, $name) = @_;
    return if !defined $token || $token !~ $TOKEN || !defined $name || !$SIGNALS{$name};
    opendir my $proc, '/proc' or return;
    for my $pid (grep { /\A\d+\z/ } readdir $proc) {
        next if $pid == $$;
        open my $fh, '<:raw', "/proc/$pid/environ" or next;
        local $/;
        my $environ = <$fh> // '';
        close $fh;
        kill $SIGNALS{$name}, $pid if grep { $_ eq $token } split /\x00/, $environ;
    }
    closedir $proc;
}
