"""What in a source directory is material, what is deliberately not, and why.

**What it does.** Models `corpus.json`'s `content` key — plain `include`
globs, `exclude` entries that each name one file and carry a `why`, and
`not_material` globs that each carry one too — and answers one question about
one path: **included**, **excluded**, **not material**, **contested** or
**unclassified**?

**How you use it.** `parse_content(document["content"])`, then
`policy.classify("src/whole-series.md")`. ⛔ Classifying takes a path and does
no I/O; enumerating a source root is `studyforge validate`'s job, and
refusing the unclassified ones is its verdict.

**Depends on.** `errors`, `studyforge.describe`, and `pathlib.PurePosixPath`
for glob semantics — a pure path object that never touches a disk.

## What is in the package

| Module | Owns |
|---|---|
| `policy` | the three states, the objects, and `classify` — what a declaration **says** |
| `parse` | reading that declaration out of a decoded object, and every refusal |

⭐ **The seam is one-way and that is the whole of it**: `parse` builds what
`policy` defines, and `policy` never reads `parse`. A reader who wants to know
what the manifest *means* opens one module; a reader who wants to know what it
*refuses* opens the other. ⛔ The direction is asserted in `test_init.py`
rather than promised here.

## Why this is a schema field and not a convention

⛔ **C2 is a schema problem, so the countermeasure is a schema field.** One of
the four designed shapes ships per-unit files *and* three whole-series
aggregates that are digest-identical ordered concatenations of them, so
`src/*.md` ingests all 38 units twice and **nothing complains**. Nothing in a
manifest without this field could say otherwise, and no heuristic should:
"these two files overlap" is a finding for reconnaissance to report, not a
guess for an ingest to make.
"""

from __future__ import annotations

from studyforge.corpus.manifest.content.parse import (
    EACH_DIRECTORY_API,
    MIN_WHY_CHARS,
    WILDCARDS,
    each_directory,
    parse_content,
)
from studyforge.corpus.manifest.content.policy import (
    Classification,
    ContentPolicy,
    Exclusion,
    NotMaterial,
)

#: ⛔ The package's whole public surface. A consumer that has to import
#: `studyforge.corpus.manifest.content.parse` directly is a consumer this
#: contract failed.
__all__ = [
    "EACH_DIRECTORY_API",
    "MIN_WHY_CHARS",
    "WILDCARDS",
    "Classification",
    "ContentPolicy",
    "Exclusion",
    "NotMaterial",
    "each_directory",
    "parse_content",
]
