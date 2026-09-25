"""The claude-senior-java-engineer-interview-preparation adapter: read this source, write an archive, prove it.

**What it does.** Reads this repository's own material and writes the archive `studyforge validate` accepts. 2 container level(s), variant(s) prose, document kind(s) lesson, practice — every one of those from `corpus.json`.

**How you use it.** `python3 -m ingest <corpus-root>` writes the archive and audits it. The exit code is the answer; there is no other agreement with the framework.

**Depends on.** the installed `studyforge` library, at the version and commit onboarding pins, and nothing else. ⛔ The framework never imports this package: the seam is on disk.
"""

from __future__ import annotations


from ingest.audit import audit
from ingest.emit import emit
from ingest.read import containers, documents

__all__ = ["audit", "containers", "documents", "emit"]
