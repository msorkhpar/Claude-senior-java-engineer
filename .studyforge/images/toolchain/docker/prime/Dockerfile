# check=skip=InvalidDefaultArgInFrom
# ⭐ That directive is deliberate: the linter warns that an ARG used in FROM has
# no default, and having NO default is the point (see below).
#
# The COURSE LAYER: one consumer's prime, warmed on top of an UNPRIMED runner or
# editor this component already built for the same declared set.
#
# ⭐ WHY A LAYER AND NOT A PRIMED BUILD. A primed build (`build.py --prime`) is
# a whole image per consumer: the runner's stage is keyed by its tag, and the
# tag folds the prime, so two courses on one declared set share no runner layer.
# Built here instead, every course on `java,maven` shares ONE runner and ONE
# editor, and each course's image is only its own caches on top: a reader who
# pulls two courses downloads the toolchain once.
#
# ⛔ Build it from the data `consuming/builds.py runner-prime|editor-prime`
# prints: every ARG below comes from the same plans `build.py` reads and carries
# NO default, so a build that bypasses them fails on an empty argument instead
# of choosing. `BASE_IMAGE` is the unprimed tag the plan names; the prime
# arrives as the read-only named context `consumer-prime`, as it does for both
# primed builds.
#
# ⛔ The warm and the proof are the primed builds' own steps, with the same
# warmers (`prime/`) and the same seed roots (`runner.prime.root`,
# `editor.prime.root`): what a layer holds is what a primed image holds.
# ⛔ The layer adds caches and nothing else. The editor's lockdown was proved
# running on the base before that base was tagged (`build.py`); nothing here
# touches the workbench, its extensions or its settings seed.

ARG BASE_IMAGE

# ------------------------------------------------------------------ runner
FROM ${BASE_IMAGE} AS runner-prime
ARG WITH_GRADLE_PRIME
ARG WITH_MAVEN_PRIME
ARG PRIME_KEY
# ⭐ HOME is root's for the warm, as it is in the primed runner, whose HOME is
# set only after its warm; the base here already carries the reader's HOME.
RUN --mount=type=bind,from=consumer-prime,target=/tmp/prime \
    --mount=type=bind,source=prime,target=/tmp/warmers \
    set -eu; \
    echo "prime: ${PRIME_KEY}"; \
    if [ "${WITH_GRADLE_PRIME}" = yes ]; then \
      HOME=/root sh /tmp/warmers/warm-gradle.sh warm /tmp/prime/gradle /opt/prime/gradle-home; \
    fi; \
    if [ "${WITH_MAVEN_PRIME}" = yes ]; then \
      HOME=/root sh /tmp/warmers/warm-maven.sh warm /tmp/prime/maven /opt/prime/maven-repo; \
    fi; \
    [ ! -d /opt/prime ] || chmod -R a+rwX /opt/prime; \
    rm -rf /root/.m2 /root/.gradle /root/.local/share/kotlin /tmp/kotlin-daemon*

# ⭐ What the layer guarantees, checked before it is tagged: a FRESH copy of the
# prime builds with NO network from a copy of the seed.
RUN --network=none --mount=type=bind,from=consumer-prime,target=/tmp/prime \
    --mount=type=bind,source=prime,target=/tmp/warmers \
    set -eu; \
    if [ "${WITH_GRADLE_PRIME}" = yes ]; then \
      HOME=/root sh /tmp/warmers/warm-gradle.sh prove /tmp/prime/gradle /opt/prime/gradle-home; \
    fi; \
    if [ "${WITH_MAVEN_PRIME}" = yes ]; then \
      HOME=/root sh /tmp/warmers/warm-maven.sh prove /tmp/prime/maven /opt/prime/maven-repo; \
    fi; \
    rm -rf /root/.m2 /root/.gradle /root/.local/share/kotlin /tmp/kotlin-daemon*

# ⭐ Where each tool looks for its cache, so a graded run needs no flag of its
# own: the primed runner's two ENV lines, from the same plan.
ARG PRIME_GRADLE_HOME
ARG PRIME_MAVEN_ARGS
ENV GRADLE_USER_HOME=${PRIME_GRADLE_HOME}
ENV MAVEN_ARGS=${PRIME_MAVEN_ARGS}
LABEL org.studyforge.runner.prime="${PRIME_KEY}"

# ------------------------------------------------------------------ editor
FROM ${BASE_IMAGE} AS editor-prime
USER root
ARG WITH_GRADLE_PRIME
ARG WITH_MAVEN_PRIME
ARG PRIME_KEY
# ⭐ The entrypoint seeds the reader's caches from these two roots when theirs
# are empty (`editor.prime.seeds`), exactly as it does for a primed editor.
RUN --mount=type=bind,from=consumer-prime,target=/tmp/prime \
    --mount=type=bind,source=prime,target=/tmp/warmers \
    set -eu; \
    echo "prime: ${PRIME_KEY}"; \
    if [ "${WITH_GRADLE_PRIME}" = yes ]; then \
      sh /tmp/warmers/warm-gradle.sh warm /tmp/prime/gradle /opt/code-server/prime/gradle-home; \
    fi; \
    if [ "${WITH_MAVEN_PRIME}" = yes ]; then \
      sh /tmp/warmers/warm-maven.sh warm /tmp/prime/maven /opt/code-server/prime/maven-repo; \
    fi; \
    rm -rf /root/.m2 /root/.local/share/kotlin /tmp/kotlin-daemon*

RUN --network=none --mount=type=bind,from=consumer-prime,target=/tmp/prime \
    --mount=type=bind,source=prime,target=/tmp/warmers \
    set -eu; \
    if [ "${WITH_GRADLE_PRIME}" = yes ]; then \
      sh /tmp/warmers/warm-gradle.sh prove /tmp/prime/gradle /opt/code-server/prime/gradle-home; \
    fi; \
    if [ "${WITH_MAVEN_PRIME}" = yes ]; then \
      sh /tmp/warmers/warm-maven.sh prove /tmp/prime/maven /opt/code-server/prime/maven-repo; \
    fi; \
    rm -rf /root/.m2 /root/.local/share/kotlin /tmp/kotlin-daemon*

LABEL org.studyforge.editor.prime="${PRIME_KEY}"
USER 1000
