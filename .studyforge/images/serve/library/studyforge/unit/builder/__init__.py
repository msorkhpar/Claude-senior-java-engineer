r"""Build the one document every consumer reads, for one unit.

**What it does.** Turns a unit's ingested archive documents — and the overlay a
person may have written beside them — into the served `unit.json`.

**How you use it.** `build_unit(unit_directory, overlay=None,
declared_practices=None)` returns the document; `render(document)` is its bytes.
`NoMaterial` is raised when nothing has been ingested for that unit.

**Depends on.** `archive`, `exercise`, and the rest of `unit`.

## ⛔ Two shapes, and they are two modules

| shape | when | who owns the order |
|---|---|---|
| **derived** | no overlay | ⭐ **this build computes it** — lessons before practices |
| **authored** | an overlay beside it | ⛔ **the author**, verbatim, never re-derived |

⚠️ **The split is the most important line in this package.** Two consumers
ordering one unit differently mint different speech ids for the same section, so
the page asks for audio that belongs to another sentence: every page renders,
every clip exists, and they no longer correspond. ⭐ Split, the authored path
cannot reach the ordering code by accident — there is no ordering code in that
module at all.

## ⛔ No material is an answer, not a failure

⚠️ A unit nobody has ingested yet raises `NoMaterial`, which a caller walking a
corpus skips while reporting the rest. ⛔ **Never an empty document**: a unit
that renders as "lesson, then the end" is a page that lies by omission, and it
is indistinguishable from a unit that has nothing more to say.

## What sits where

| module | the question it answers |
|---|---|
| `material` | what did ingestion leave for this unit, and is every file still clean? |
| `derived` | what does a unit nobody has curated look like? |
| `authored` | what does the author's order, plus the two fields that are not theirs, look like? |
| `parts` | what is one served section, and where do its derived fields come from? |
| `document` | what is written, in what order, and out of which files? |

⭐ **`unit.served` is a sibling of this package rather than a member**, because
reading a built document back is a different question with different consumers
— the page generator, the server and the run route, none of which builds.
"""

from __future__ import annotations

from pathlib import Path

from studyforge.unit.builder.derived import sections as derived_sections
from studyforge.unit.builder.document import (
    API,
    BUILT_FROM_KEYS,
    KNOWN_API,
    PRACTICES_KEYS,
    UNIT_KEYS,
    build,
    render,
)
from studyforge.unit.builder.material import KIND_ORDER, Material, NoMaterial, read
from studyforge.unit.builder.parts import SECTION_KEYS
from studyforge.unit.content import Overlay
from studyforge.unit.headings import Heading, headings
from studyforge.unit.mentions import Mentions

__all__ = [
    "API",
    "BUILT_FROM_KEYS",
    "KIND_ORDER",
    "KNOWN_API",
    "PRACTICES_KEYS",
    "SECTION_KEYS",
    "UNIT_KEYS",
    "Material",
    "NoMaterial",
    "build",
    "build_unit",
    "read",
    "render",
    "unit_headings",
    "unit_sections",
]


def unit_headings(unit_directory: Path | str) -> tuple[Heading, ...]:
    """Read one unit's material off disk and index its headings, numbers and all.

    ⭐ The sections `build` serves, before their headings lose their numbers,
    so another unit's `section 2.2` names the heading this unit's page shows.
    """
    return headings(unit_sections(unit_directory))


def unit_sections(unit_directory: Path | str) -> tuple[dict, ...]:
    """Read one unit's material off disk and return the sections `build` serves, unserved.

    ⭐ What `Mentions.unreached` counts in: the prose as the archive wrote it.
    """
    return derived_sections(read(unit_directory))


def build_unit(
    unit_directory: Path | str,
    *,
    overlay: Overlay | None = None,
    declared_practices: int | None = None,
    title: str | None = None,
    mentions: Mentions | None = None,
) -> dict:
    """Read one unit's material off disk and build its served document.

    ⭐ The whole task in one call, for the common case. A caller that already
    holds the documents — a builder walking a corpus, or a test — uses
    `Material` and `build` directly and never touches the disk twice.
    """
    return build(
        read(unit_directory),
        overlay=overlay,
        declared_practices=declared_practices,
        title=title,
        mentions=mentions,
    )
