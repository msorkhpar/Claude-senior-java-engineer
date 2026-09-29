#!/bin/sh
# The MAVEN warmer: run a consumer's prime project's `test` phase once,
# so a local repository holds everything a first `mvn -o test` needs.
#
#   warm-maven.sh warm  PROJECT REPO   run `mvn test` with the network into REPO
#   warm-maven.sh prove PROJECT REPO   run a fresh copy OFFLINE against a copy of REPO
#   warm-maven.sh check LOG            only the source check, on a `mvn -B` log
#
# ⭐ Maven resolves every dependency a POM DECLARES for every module, used or
# not, so running the phase warms what the POM declares, not what the tests
# reference. ⭐ Every file is checked against Maven Central's own checksum as
# it arrives (`-C`, strict checksums), and the request carries a placeholder
# User-Agent and no identity.
#
# ⛔ THE SOURCES MUST BE REAL: a build that compiles no source, compiles no
# test, or runs no test, never resolves what those steps need, so the build
# fails naming what it lacked. ⭐ That is asked of the BUILD, not of each
# module: a module of a multi-module build that carries no sources is primed
# through its POM, because Maven resolves every dependency a POM declares
# before a step runs, whether or not that step then finds a source — so it is
# named, and never refused. ⭐ A test that FAILS in the consumer's own build is
# the consumer's finding and not the prime's: the run it failed in resolved
# what it needed, so it is named and the warm goes on.
# There is no Maven wrapper to honour: the image's pinned Maven IS the version
# (the version guards refuse a prime whose wrapper names another).
set -eu

AGENT="Example/0.1 (+https://example.invalid)"

fail() { printf 'prime: %s\n' "$*" >&2; exit 1; }

check() {
    awk '
    /^\[INFO\] --- .* @ [^ ]+ ---$/ { module = $(NF - 1); goal = $3; sub(/.*:/, "", goal) }
    /^\[INFO\] No sources to compile$/ {
        printf "prime: maven module %s compiles no sources in %s, so it is primed through its POM alone\n", module, goal > "/dev/stderr"
    }
    /^\[INFO\] No tests to run\.$/ || /^\[INFO\] Tests are skipped\.$/ {
        printf "prime: maven module %s runs no tests, so it is primed through its POM alone\n", module > "/dev/stderr"
    }
    /^\[INFO\] Compiling [1-9][0-9]* source files?( |$)/ { if (goal == "compile") compiled = 1; if (goal == "testCompile") testcompiled = 1 }
    /^\[(INFO|WARNING|ERROR)\] Tests run: [1-9]/ { tested = 1 }
    /^\[ERROR\] .* -- Time elapsed: .* <<< (FAILURE|ERROR)!$/ {
        name = $0; sub(/^\[ERROR\] /, "", name); sub(/ -- Time elapsed: .*$/, "", name)
        printf "prime: the consumer'"'"'s own test %s fails in maven module %s; that is the consumer'"'"'s finding, and the prime is warmed regardless\n", name, module > "/dev/stderr"
    }
    END {
        if (!compiled) {
            print "prime: the maven prime compiled no source: a step with no sources never resolves what it needs, so the prime would prime nothing; give one module one real source and one real test" > "/dev/stderr"
            bad = 1
        }
        if (!testcompiled || !tested) {
            print "prime: the maven prime ran no test: the test step never resolves its provider, so the prime would prime nothing; give one module one real test" > "/dev/stderr"
            bad = 1
        }
        exit bad
    }' "$1"
}

build() {  # PROJECT REPO [-o]
    work="$(mktemp -d)"
    cp -R "$1"/. "$work"/
    log="$(mktemp)"
    repo="$2"
    shift 2
    if ! (cd "$work" && mvn -B -C -ntp "$@" -Dmaven.repo.local="$repo" \
            "-Daether.connector.userAgent=$AGENT" -Dmaven.test.failure.ignore=true test) > "$log" 2>&1; then
        tail -n 60 "$log" >&2
        fail "the maven prime did not build"
    fi
    check "$log"
    rm -rf "$work" "$log"
}

mode="${1:-}"
case "$mode" in
    check)
        [ $# -eq 2 ] || fail "usage: warm-maven.sh check LOG"
        check "$2" ;;
    warm)
        [ $# -eq 3 ] || fail "usage: warm-maven.sh warm PROJECT REPO"
        mkdir -p "$3"
        build "$2" "$3"
        # Download bookkeeping names the host a file came from and when; an
        # offline build needs none of it, and it would differ build to build.
        find "$3" \( -name '_remote.repositories' -o -name '*.lastUpdated' \
            -o -name 'resolver-status.properties' \) -delete
        chmod -R a+rX "$3"
        echo "prime: maven repository at $3" ;;
    prove)
        [ $# -eq 3 ] || fail "usage: warm-maven.sh prove PROJECT REPO"
        repo="$(mktemp -d)"
        cp -R "$3"/. "$repo"/
        build "$2" "$repo" -o
        rm -rf "$repo"
        echo "prime: a fresh copy of the maven prime runs its tests offline from the repository" ;;
    *)
        fail "usage: warm-maven.sh warm|prove PROJECT REPO | check LOG" ;;
esac
