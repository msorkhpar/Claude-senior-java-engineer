#!/bin/sh
# The editor's entrypoint: seed the reader's settings and the prime's caches
# once, then hand over to the base image's own entrypoint with the arguments
# untouched and the two call-home switches after them (see the end).
#
# The prime's caches are built into the image under
# /opt/code-server/prime and copied to where the tools look — the Gradle user
# home and the Maven local repository — only when that directory is EMPTY, so
# a volume that already holds a reader's cache is never touched. The copy goes
# through a .seed-partial sub-directory, so a container killed mid-copy leaves
# the directory "still empty" and the next start finishes the job. The copy
# is owned by the running uid; the seed in the image is root's.
#
# The user data directory lives on a named volume, so anything placed there at
# image-build time is invisible once the volume is mounted, and a volume from an
# earlier image may already hold the reader's edits. So the seed is written
# ONLY when no settings file exists, and never overwrites: an overwrite would
# lose the reader's edits on every start.
#
# ⛔ THE KEYBINDINGS SEED IS THE OPPOSITE, AND THE DIFFERENCE IS DELIBERATE.
# settings.json is the READER's file — the paragraph above is about
# their edits. keybindings.json is not theirs and never was: it is the
# workbench lockdown, it is generated from lockdown/allowed.js, and a reader
# confined by it has no command with which to write one. A volume carrying an
# older image's copy — or none, which is every volume that predates this — is
# a keybinding the allow-list no longer permits still firing, so it is written
# on EVERY start. ⚠️ A consumer who wants a different set changes the
# allow-list and rebuilds; an edit to this file in a volume does not survive.
#
# ⛔ The final `exec` chains to /usr/bin/entrypoint.sh, never to code-server
# directly: bypassing it loses the base's fixuid, DOCKER_USER handling and
# dumb-init. ⛔ There is no mode that runs some other command instead of
# code-server: the reader's code runs in the runner image.
#
# ⛔ fixuid RUNS FIRST, and that ordering belongs to the compose contract.
# The base runs it too, in /usr/bin/entrypoint.sh — which is AFTER
# everything below. A consumer runs this container as the uid:gid that owns its
# sources, and any uid but the image's own has no passwd entry until fixuid
# writes one: HOME is then `/`, the seed below tries `//.local` and fails, and
# the container exits before code-server starts. So it runs here as well. It is
# the same command, it is idempotent, and both halves are measured in
# tests/test_consuming_image.py.
set -eu

eval "$(fixuid -q)"

SEED_SETTINGS=/opt/code-server/seed/settings.json
SEED_KEYBINDINGS=/opt/code-server/seed/keybindings.json
SEED_GRADLE=/opt/code-server/prime/gradle-home
SEED_MAVEN=/opt/code-server/prime/maven-repo

log() { printf '[editor-entrypoint] %s\n' "$*"; }

# seed_tree SEED TARGET LABEL
seed_tree() {
    if [ ! -d "$1" ]; then
        log "no $3 seed in this image; skipping"
        return
    fi
    mkdir -p "$2"
    # Anything other than a leftover partial copy counts as "not empty".
    if [ -n "$(ls -A "$2" | grep -vx '.seed-partial' || true)" ]; then
        log "$3 at $2 is not empty; leaving it alone"
        return
    fi
    log "seeding $3 at $2"
    rm -rf "$2/.seed-partial"
    cp -R --preserve=mode,timestamps "$1" "$2/.seed-partial"
    find "$2/.seed-partial" -mindepth 1 -maxdepth 1 -exec mv -t "$2" -- {} +
    rmdir "$2/.seed-partial"
    log "$3 seeded"
}

seed_tree "$SEED_GRADLE" "${GRADLE_USER_HOME:-${HOME:-/home/coder}/.gradle}" "gradle home"
seed_tree "$SEED_MAVEN" "${HOME:-/home/coder}/.m2/repository" "maven repository"

# code-server's default, unless --user-data-dir was passed on the command line.
user_data_dir="${HOME:-/home/coder}/.local/share/code-server"
prev=""
for arg in "$@"; do
    case "$arg" in
        --user-data-dir=*) user_data_dir="${arg#--user-data-dir=}" ;;
        *) [ "$prev" = "--user-data-dir" ] && user_data_dir="$arg" ;;
    esac
    prev="$arg"
done

target="$user_data_dir/User/settings.json"
if [ -e "$target" ]; then
    log "settings exist at $target; leaving them alone"
else
    log "writing default settings to $target"
    mkdir -p "$user_data_dir/User"
    cp "$SEED_SETTINGS" "$target"
fi

# The workbench lockdown's keybindings, written every start — see the note at
# the top of this file for why this one is not the reader's to keep.
keys="$user_data_dir/User/keybindings.json"
if [ -e "$SEED_KEYBINDINGS" ]; then
    log "writing the lockdown keybindings to $keys"
    mkdir -p "$user_data_dir/User"
    cp "$SEED_KEYBINDINGS" "$keys.seed-partial"
    mv "$keys.seed-partial" "$keys"
else
    log "no keybindings seed in this image; the workbench keeps its defaults"
fi

# ⛔ BUILDSHIP'S GRADLE VERSION LIST IS SEEDED, SO THE LANGUAGE SERVER NEVER
# FETCHES IT. Measured: every session's Java language server connected to
# services.gradle.org, whatever the Gradle settings said -- Buildship, inside
# it, downloads the published Gradle versions whenever its cache file is
# missing or older than a day, and honours no proxy or offline setting. Its
# cache is `$XDG_CACHE_HOME/tooling/gradle/versions.json` when that variable is
# set (the image sets it to the XDG default, `~/.cache`), so an empty list is
# written there on every start with a modification time that never ages. The
# list feeds Buildship's version pickers, which this editor has no surface for.
versions="${XDG_CACHE_HOME:-${HOME:-/home/coder}/.cache}/tooling/gradle/versions.json"
mkdir -p "$(dirname "$versions")"
printf '[]\n' > "$versions"
touch -d '2100-01-01 00:00:00' "$versions"

# ⛔ THE EDITOR MAKES NO OUTBOUND REQUEST OF ITS OWN, WHATEVER THE COMMAND.
# Measured on an idle session before this: the server looked up and connected
# to api.github.com (code-server's update check) and v1.telemetry.coder.com
# (its telemetry) within seconds of starting. Both switches are flags only, and
# a consumer's compose `command:` replaces CMD, so they are appended HERE, to
# whatever command arrived: the entrypoint is the one part a command cannot
# drop. A repeated boolean flag is harmless. The rest of the call-homes are the
# image's ENV (the extension gallery) and the lockdown manifest's defaults (the
# extensions' own); `tests/test_editor_egress.py` reads the whole of it live.
exec /usr/bin/entrypoint.sh "$@" --disable-update-check --disable-telemetry
