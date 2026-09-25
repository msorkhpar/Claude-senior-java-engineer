"""The ingest command: write the archive, then audit what was written.

**What it does.** One run of the whole adapter. Emits, audits, prints both, and returns the audit's exit code — so the answer to *did this work* is a number, not an opinion.

**How you use it.** `python3 -m ingest <corpus-root> [YYYY-MM-DD]`.

**Depends on.** This package's `emit` and `audit`, and `studyforge.validate` for the codes.
"""

from __future__ import annotations


import sys
from datetime import date

from studyforge.validate import UNUSABLE

from ingest.audit import audit
from ingest.emit import emit


def main(argv=None) -> int:
    """Emit and audit one corpus root; return the audit's exit code."""
    argv = list(sys.argv[1:] if argv is None else argv)
    if not 1 <= len(argv) <= 2:
        print("usage: python3 -m ingest <corpus-root> [YYYY-MM-DD]")
        return UNUSABLE
    root = argv[0]
    # ⚠️ Reproducible: re-running produces identical bytes apart from `ingested`, so the
    # date is an argument first and today's date only as a fallback.
    ingested = argv[1] if len(argv) == 2 else date.today().isoformat()
    for where in emit(root, ingested=ingested, replace=True):
        print(f"wrote {where}")
    lines, code = audit(root)
    for line in lines:
        print(line)
    return code


if __name__ == "__main__":  # pragma: no cover - the command line
    raise SystemExit(main())
