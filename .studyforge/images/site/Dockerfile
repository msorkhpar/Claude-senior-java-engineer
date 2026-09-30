# Written by studyforge's execution skill (standalone). Regenerate it; never edit it.
# The course's study site: every file of the course on the shared server.
# Two targets: without-narration, whose pages name no clip, and with-narration,
# which adds every clip and refuses to build unless each matches the checksum
# the course committed.
ARG STUDYFORGE_NAMESPACE

FROM ${STUDYFORGE_NAMESPACE:?STUDYFORGE_NAMESPACE-is-not-set}/studyforge-serve:0.1.0-76df622858caaebd20691d2d40b5289f9bb1bda6beb90407279509c96c3ec19c@sha256:d7998f7902c5643c12fc0c6663567716965598a6b431fbe0170ec9c9ae7d1dcf AS without-narration
USER root
RUN --mount=type=bind,source=.,target=/context \
    set -eu; mkdir -p /corpus; \
    tar -C /context --exclude=./.git --exclude='./.studyforge/*/audio' --exclude='*/study/audio' -cf - . \
      | tar -C /corpus -xf -; \
    mkdir -p /corpus/.studyforge/progress; \
    find /corpus -type f -name '*.html' -exec sed -i -E 's/ data-audio="[^"]*"//g' {} +; \
    chown -R 1000:1000 /corpus
USER 1000:1000
WORKDIR /corpus
LABEL org.studyforge.course="claude-senior-java-engineer-interview-preparation" org.studyforge.narration="without"

FROM ${STUDYFORGE_NAMESPACE:?STUDYFORGE_NAMESPACE-is-not-set}/studyforge-serve:0.1.0-76df622858caaebd20691d2d40b5289f9bb1bda6beb90407279509c96c3ec19c@sha256:d7998f7902c5643c12fc0c6663567716965598a6b431fbe0170ec9c9ae7d1dcf AS with-narration
USER root
RUN --mount=type=bind,source=.,target=/context \
    set -eu; mkdir -p /corpus; \
    tar -C /context --exclude=./.git --exclude='./.studyforge/*/audio' --exclude='*/study/audio' -cf - . \
      | tar -C /corpus -xf -; \
    mkdir -p /corpus/.studyforge/progress; \
    cd /context; \
    find . \( -path './.studyforge/*' -o -path '*/study/*' \) -type d -name audio -prune | while IFS= read -r dir; do \
      mkdir -p "/corpus/$dir"; cp -R "$dir/." "/corpus/$dir/"; \
    done; \
    chown -R 1000:1000 /corpus; \
    cd /corpus; sha256sum -c --quiet .studyforge/narration-release/clips.sha256 \
      || { echo 'narration clips missing or wrong: restore them first' >&2; \
           exit 1; }
USER 1000:1000
WORKDIR /corpus
LABEL org.studyforge.course="claude-senior-java-engineer-interview-preparation" org.studyforge.narration="with"
