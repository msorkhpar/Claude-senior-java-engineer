# Written by studyforge's execution skill (standalone). Regenerate it; never edit it.
# The shared study server: the part of the studyforge library that serves a
# course, found by its imports, with no ingestion, no skill and no synthesis.
FROM python:3.14-slim@sha256:cad9a2c871761c413caa6fdd6441c783451e740a48aaeba60ae62a8b53525ef6
COPY library/ /opt/studyforge/library/
ENV PYTHONPATH=/opt/studyforge/library PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1
LABEL org.studyforge.serve.version="0.1.0" \
      org.studyforge.serve.commit="2c238755fdc02f29fa0c3a552e58249b0492195e"
ENTRYPOINT ["python3", "-m", "studyforge.cli"]
