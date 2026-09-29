#!/bin/sh
# The GRADLE warmer: build a consumer's prime project once, so its
# Gradle user home holds everything a first offline build needs.
#
#   warm-gradle.sh warm  PROJECT SEED   build PROJECT with the network into SEED
#   warm-gradle.sh prove PROJECT SEED   build a fresh copy OFFLINE from a copy of SEED
#   warm-gradle.sh check LOG            only the source check, on a `--console=plain` log
#
# ⛔ THE SOURCES MUST BE REAL. A NO-SOURCE compile task never resolves its
# classpath, so a prime with no sources primes nothing while it appears to
# succeed. So every project that compiles at all must ACTUALLY RUN a main
# compile task, a test compile task and `test`, or the build fails naming the
# project. (A pure-Kotlin project's compileJava is NO-SOURCE and that is fine:
# its compileKotlin ran and resolved the same classpath.)
#
# PROJECT is only read: it is copied first, because the build context is a
# read-only bind mount and a build writes into its project. A wrapper in
# PROJECT is used when present (the version guards have already required it
# to name the pinned Gradle and its sha256); otherwise the image's pinned
# `gradle` runs. Every file Gradle fetches is checked against the project's
# gradle/verification-metadata.xml, which the contract requires.
set -eu

fail() { printf 'prime: %s\n' "$*" >&2; exit 1; }

check() {
    awk '
    /^> Task :/ {
        path = $3; status = $4
        n = split(path, seg, ":"); name = seg[n]
        project = substr(path, 1, length(path) - length(name) - 1)
        if (project == "") project = ":"
        if (name ~ /^compileTest[A-Z]/) kind = "test compile task"
        else if (name ~ /^compile[A-Z]/) kind = "main compile task"
        else if (name == "test") kind = "test task"
        else next
        if (!(project in seen)) { seen[project] = 1; projects++ }
        if (status == "" || status == "FROM-CACHE" || status == "UP-TO-DATE") ran[project, kind] = 1
    }
    END {
        if (projects == 0) {
            print "prime: the gradle prime compiled nothing: a prime with no sources primes nothing" > "/dev/stderr"
            exit 1
        }
        bad = 0
        for (project in seen) {
            split("main compile task|test compile task|test task", kinds, "|")
            for (i = 1; i <= 3; i++) if (!((project, kinds[i]) in ran)) {
                printf "prime: gradle project %s ran no %s (NO-SOURCE): a task with no sources never resolves its classpath, so the prime would prime nothing; give it one real source and one real test\n", project, kinds[i] > "/dev/stderr"
                bad = 1
            }
        }
        exit bad
    }' "$1"
}

build() {  # PROJECT HOME [--offline]
    work="$(mktemp -d)"
    cp -R "$1"/. "$work"/
    log="$(mktemp)"
    tool=gradle
    [ -f "$work/gradlew" ] && tool="sh ./gradlew"
    shift
    home="$1"
    shift
    if ! (cd "$work" && GRADLE_USER_HOME="$home" $tool build "$@" --no-daemon --console=plain \
            -Porg.gradle.java.installations.auto-download=false) > "$log" 2>&1; then
        tail -n 60 "$log" >&2
        fail "the gradle prime did not build"
    fi
    check "$log"
    rm -rf "$work" "$log"
}

mode="${1:-}"
case "$mode" in
    check)
        [ $# -eq 2 ] || fail "usage: warm-gradle.sh check LOG"
        check "$2" ;;
    warm)
        [ $# -eq 3 ] || fail "usage: warm-gradle.sh warm PROJECT SEED"
        mkdir -p "$3"
        build "$2" "$3"
        # A daemon's registry, temporary files and lock files belong to the
        # build that made them, never to the reader's first build.
        rm -rf "$3/daemon" "$3/.tmp" "$3/kotlin-profile"
        find "$3" -name '*.lock' -delete
        chmod -R a+rX "$3"
        echo "prime: gradle seed at $3" ;;
    prove)
        [ $# -eq 3 ] || fail "usage: warm-gradle.sh prove PROJECT SEED"
        home="$(mktemp -d)"
        cp -R "$3"/. "$home"/
        build "$2" "$home" --offline
        rm -rf "$home"
        echo "prime: a fresh copy of the gradle prime builds offline from the seed" ;;
    *)
        fail "usage: warm-gradle.sh warm|prove PROJECT SEED | check LOG" ;;
esac
