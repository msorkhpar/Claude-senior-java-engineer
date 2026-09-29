"""Every line the runner yields: relative to the source root, then scrubbed.

**What it does.** Rewrites the absolute spelling of the source root in a line of
output to a path relative to it, then passes the line through R7's gate.

**How you use it.** `gate = LineGate(roots)` once per run, with every absolute
spelling the root has in the run's mode; `gate(raw)` per line.

**Depends on.** `re`, and `studyforge.archive.scrub` — ⛔ the one personal-data
gate, never a second pattern set.

## ⭐ Why relative, in BOTH modes

A build prints absolute paths. The same run prints `/work/practice/x.py` in the
runner container and the host's own path on the host — so *"both modes produce
identical observable behaviour"* failed on paths alone, and the host's spelling
is a home directory (R7). ⭐ **Relative to the source root is the one spelling
both modes share**, so each mode rewrites ITS root: `/work/practice/x.py` and
`<host root>/practice/x.py` both become `practice/x.py`, and the root itself
becomes `.`.

⚠️ **A root is rewritten only where it is a whole path.** `/work` inside
`/workspace`, `/work.d` or `/srv/work` is somebody else's path and is left
alone: the root must neither continue a segment nor sit under another one, and
a segment's characters are spelled once, as `_SEGMENT_CHAR`.

## ⛔ The order is the point

Relative first, THEN scrub. Scrubbing first would rewrite a host root under
`/home/<name>` to the placeholder before it could be recognised as the root,
and the line would carry `/path/to/project/practice/x.py` on the host and
`practice/x.py` in the container. ⭐ **The scrub still runs on every line**:
a path OUTSIDE the root — a tool's own install, a cache — is not relative to
anything and is exactly what the gate is for.
"""

from __future__ import annotations

import re
from collections.abc import Iterable

from studyforge.archive.scrub import scrub

#: A character that continues a path segment. A root followed by one of these is
#: a different, longer name, not the root.
_SEGMENT_CHAR = r"[A-Za-z0-9._~+-]"

#: A character that, before a root, makes it the tail of a longer path.
_PRECEDING_CHAR = r"[A-Za-z0-9._~+/-]"

#: What the root itself becomes when it is printed bare.
ROOT_SPELLING = "."


class LineGate:
    """Rewrite each root to a relative spelling, then scrub (R7)."""

    def __init__(self, roots: Iterable[str]) -> None:
        """Compile one pattern over every spelling of the root, longest first."""
        spellings = sorted(
            {root.rstrip("/") for root in roots if root.strip("/")}, key=len, reverse=True
        )
        alternatives = "|".join(re.escape(root) for root in spellings)
        self._pattern = (
            re.compile(f"(?<!{_PRECEDING_CHAR})(?:{alternatives})(?:(/)|(?!{_SEGMENT_CHAR}))")
            if spellings
            else None
        )

    def __call__(self, raw: str) -> str:
        """Return the line as the runner yields it."""
        line = raw.rstrip("\r\n")
        if self._pattern is not None:
            line = self._pattern.sub(_relative, line)
        return scrub(line)


def _relative(match: re.Match[str]) -> str:
    """`<root>/rest` loses `<root>/`; a bare `<root>` becomes `.`."""
    return "" if match.group(1) else ROOT_SPELLING
