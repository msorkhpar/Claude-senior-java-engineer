"""The served unit document: archive content, the authored overlay, and section keys.

**What it does.** Builds what a reader is actually served for one unit, by
composing the archive's blocks with whatever the corpus authored on top of them
and assigning the section keys that navigation, narration and progress all
address.

**How you use it.** Give the builder an archive document and the corpus's
overlay for that address; take back a versioned unit document. That document's
`api` field is checked, not migrated at read time — an unknown version is
refused (R9).

**Depends on.** `address` and `archive`. ⛔ Not on `render`: this package
decides *what* a unit is, and the renderer decides what it looks like. That
split is what lets the same unit document serve a page, a table of contents
entry and a narration script without three ideas of the same content.

⚠️ **Section keys are load-bearing.** The player highlights by them, progress
records by them, and in-page navigation links by them, so a key that changes
when the prose is edited silently detaches a reader's saved position from the
thing it marked.

⭐ **A unit with an overlay and a unit with none are the same shape.** One with
none is complete, not deficient — the fixtures carry both from wave 0 so the
no-overlay path is never the one discovered late.

## What it holds

| Module | Owns |
|---|---|
| `content` | `content.json` — the authored overlay, and the only file a person edits |
| `sections` | the section-key vocabulary: `shared`, `<variant>`, `practice-<variant>` |
| `trust` | where a grader came from and what it may claim (R5) |
| `outline` | the source's own outline number, left off every title and heading a reader is served |
| `headings` | a unit's headings by source anchor and outline number, with their page ids |
| `mentions` | a mention of another unit, a file or a heading, served as what it names |
| `prose` | every run of words a unit shows, in its blocks and its quiz, walked as a copy |
| `errors` | `ContentError`, the only exception any of it raises |

⛔ **`content_api` is minted here** (R21): the overlay lives at
`units/unit-NN/content.json`, is versioned by `content_api`, and is written by
**a person** and nothing else. R9's enumeration was written from the generating
side and did not list the one document this framework only ever reads.

⭐ **Where that file sits in a real archive is `skills.adapter.Layout.content`
and nowhere else** (R21) — the contract owns the filename, placement owns
the directory, and a consumer that joins its own is the second authority this
rule exists to remove. ⚠️ **Located is not applied: v1's build serves a unit
from its archive documents alone**, so an overlay that exists is read by
nothing a build runs, and that verb is still unowned.

`builder` assembles the unit document from the material and the overlay.
"""

from __future__ import annotations

from studyforge.unit.content import (
    CONTENT_API,
    CONTENT_FILENAME,
    DERIVED_FIELDS,
    KNOWN_CONTENT_API,
    OVERLAY_KEYS,
    SECTION_FIELDS,
    Overlay,
    Section,
    from_document,
    load,
    parse,
)
from studyforge.unit.errors import ContentError
from studyforge.unit.headings import Heading, headings
from studyforge.unit.mentions import Mentions
from studyforge.unit.outline import listed_numbering, without_outline_number
from studyforge.unit.practice import bare_lesson
from studyforge.unit.sections import (
    KIND_OF,
    KINDS_WITH_A_LANG,
    PRACTICE_PREFIX,
    SECTION_KINDS,
    SHARED_KEY,
    derived_section_key,
    heading_anchor,
    section_key,
)
from studyforge.unit.trust import (
    DEFAULT_TRUST,
    MAY_BE_AUTHORITATIVE,
    PROVENANCE,
    TRUST,
    check_test_record,
)

#: ⛔ The package's whole public surface.
__all__ = [
    "CONTENT_API",
    "CONTENT_FILENAME",
    "DEFAULT_TRUST",
    "DERIVED_FIELDS",
    "MAY_BE_AUTHORITATIVE",
    "KINDS_WITH_A_LANG",
    "KIND_OF",
    "KNOWN_CONTENT_API",
    "OVERLAY_KEYS",
    "PRACTICE_PREFIX",
    "PROVENANCE",
    "SECTION_FIELDS",
    "SECTION_KINDS",
    "SHARED_KEY",
    "TRUST",
    "ContentError",
    "Heading",
    "Mentions",
    "Overlay",
    "Section",
    "bare_lesson",
    "check_test_record",
    "derived_section_key",
    "from_document",
    "heading_anchor",
    "headings",
    "listed_numbering",
    "load",
    "parse",
    "section_key",
    "without_outline_number",
]
