"""`python3 -m studyforge.cli` — the installed command, run without installing.

⚠️ One implementation: this module is four lines on top of `dispatch.main`, the
same callable `pyproject.toml` registers as `studyforge`, so a reader who has
not installed the package gets the command the installed one gives.
"""

from __future__ import annotations

import sys

from studyforge.cli.dispatch import main

if __name__ == "__main__":  # pragma: no cover - exercised as a subprocess
    sys.exit(main(sys.argv[1:]))
