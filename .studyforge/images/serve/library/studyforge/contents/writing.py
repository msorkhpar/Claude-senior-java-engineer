"""How both contents documents reach the disk: staged beside the target, then moved.

**What it does.** Writes one document's text to one path, creating the
directory, via a `*.writing` file that is renamed into place.

**How you use it.** `write(path, text)`. ⛔ It knows nothing about what the
text is — the two documents differ in every other respect and share only this.

**Depends on.** `studyforge.archive.scrub` for R7's gate, plus `os` and
`pathlib`. ⛔ Nothing else of this package's, so neither document's module is
the other's dependency.

⚠️ **A torn document must read as absent, never as present and wrong.** A file
half-overwritten in place is a table of contents that parses, is short, and
raises nothing; a torn `*.writing` file is a rebuild. ⭐ Same discipline §6
states for a generator and `discovery.cache.write` already follows — recorded
as a finding rather than reached across a package boundary for.

## ⛔ The gate runs on the WRITE path too, and it runs before the `mkdir`

⭐ **Both readers already gate what they decode; this is the other direction,
asserted independently.** R7's rule for a defence that cannot
enumerate its legal set is *every layer asserted, because no layer is
sufficient* — and the read gate is not sufficient here: a `Contents` can be
**built in memory** from a container map, a test, or a caller's own value and
written out without ever having been decoded.

⚠️ **Before the `mkdir`, deliberately.** A refusal that had already created a
directory would leave the tree changed by the check that refused it.
"""

from __future__ import annotations

import os
from pathlib import Path

from studyforge.archive.scrub import assert_clean

#: What is appended to a path while it is being written.
WRITING_SUFFIX = ".writing"


def write(path: Path | str, text: str) -> None:
    """Write `text` to `path`, atomically as far as the filesystem allows.

    ⛔ Raises `PersonalDataLeak` — as itself, never re-typed (R7) —
    before touching the filesystem, if what is about to be written carries
    personal data (R7).
    """
    path = Path(path)
    # ⛔ `path.name`, never `path`: `where` is formatted into the refusal and
    # the path a caller has is absolute.
    assert_clean(text, path.name)
    path.parent.mkdir(parents=True, exist_ok=True)
    staged = path.with_name(path.name + WRITING_SUFFIX)
    try:
        staged.write_text(text, encoding="utf-8", newline="\n")
        os.replace(staged, path)
    finally:
        staged.unlink(missing_ok=True)
