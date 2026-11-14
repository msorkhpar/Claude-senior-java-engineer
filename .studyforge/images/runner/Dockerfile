# Written by studyforge's execution skill (standalone). Regenerate it; never edit it.
# The course's runner: the toolchain's course layer, with the run service the
# site reaches on the compose file's internal network.
ARG RUNNER_PRIMED
FROM ${RUNNER_PRIMED}
COPY .studyforge/execution/runservice.pl /opt/studyforge/runservice.pl
# A run prints text as written: under the base's POSIX locale a JVM prints ö as ?.
ENV LANG=C.UTF-8
WORKDIR /work
CMD ["perl", "/opt/studyforge/runservice.pl"]
LABEL org.studyforge.course="claude-senior-java-engineer-interview-preparation"
