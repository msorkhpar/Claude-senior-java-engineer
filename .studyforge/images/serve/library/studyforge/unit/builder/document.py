r"""The served unit document: what is written, in what order, out of which files.

**What it does.** Assembles one `unit.json` from a unit's material and its
optional overlay, and renders it to the exact bytes that reach disk.

**How you use it.** `build(material, overlay=..., declared_practices=...)`
returns the document; `render(document)` is its bytes.

**Depends on.** `builder.material`, `builder.derived`, `builder.authored`,
`unit.mentions` and `unit.outline` for what the prose names and the numbers it
loses, and `archive.document` for the one canonical serialisation.

## ⛔ The headings are read BEFORE the outline numbers leave

⭐ A sentence names a heading by the number the source wrote in front of it
(`Section 2.2 describes it`), so `unit.headings` reads the headings with their
numbers first. Then `headings_without_outline_numbers` takes the numbers off,
and only then does `Mentions.sections` serve every reference, so a mention
is served in the words a reader reads and a heading's own number is never
taken for a mention. ⚠️ Taking a number off never adds or removes a block, so
every heading keeps the position its page's id is minted from.

## ⛔ It is generated, byte-for-byte, and never hand-edited

⚠️ **The one document every consumer reads**: the page renders it, the server
serves it, the run route takes its commands out of it. R10 makes regeneration
byte-identical, and R19 makes a hand-edit to it a **finding** rather than a fix.

## ⛔ `practices` holds two counts and no verdict

⚠️ **Without this the page lies by omission.** A unit whose practices were never
ingested renders exactly like a unit that has none: lesson, then the end. ⭐ So
the document says how many the corpus **declared** and how many are **archived**,
and says nothing about whether that is complete — *complete*, *short*, *unknown*
is `studyforge validate`'s single job, and the page only needs to know there is
more to come.

⚠️ `declared` is `None` when nothing declared a count, which is not the same as
zero and must not render as it.

## ⛔ Provenance is `built_from`, and the name was ruled

⚠️ **`source` stays a corpus id (R4)**, and the served array may not be
called `sources`, `source` or `origin` — the extraction source uses `source` for
a fetched address, and one word meaning two things is how the two levels get
confused exactly where they meet.

⭐ **No path is recorded**, because a path is a fact about one disk: an
entry names the variant, kind and ordinal that identify the document within
this unit, and the unit's own `address` is on the document above it. A location
is a fact about a disk; this is a fact about the corpus.

## ⚠️ The next seam in this module, named before it is needed

⛔ **Do not pre-split it.** When this file needs a seam the division is
already known: `practices` is asked
by the **page** — *is there more to come?* — and `built_from` by **re-ingest
detection** — *did the material change under us?* Two questions, two consumers,
already two constants.

## ⚠️ What this module does **not** assert

⛔ The neighbours are named: it does not recompute
`content_sha256` (the archive owns it and `validate` recomputes it), it does not
check a block's shape, and it does not decide whether the practice counts agree.
⭐ What it *does* assert is that the overlay in hand and the material in hand
describe the same unit — because merging two units' work into one page is a
failure nothing downstream could notice.
"""

from __future__ import annotations

from studyforge.archive.document import render as render_json
from studyforge.unit.builder import authored as authored_shape
from studyforge.unit.builder import derived as derived_shape
from studyforge.unit.builder.material import Material
from studyforge.unit.content import Overlay
from studyforge.unit.errors import ContentError, describe
from studyforge.unit.headings import headings
from studyforge.unit.mentions import Mentions
from studyforge.unit.outline import headings_without_outline_numbers, without_outline_number

#: 2 — the served document's shape. ⛔ Registered in the spec's R9 table as
#: `unit.json` / `api`, and bumped rather than widened.
#:
#: ⚠️ **`2` is the version in which a section carries its `attachments`.** ⛔ Bumped rather
#: than widened because `unit.served` refuses a shape it does not know, and the
#: two answers it must not conflate are *"this document was written before
#: sections carried attachments"* and *"this unit has no companion files"* —
#: the argument `served` already makes about `video`, arriving for the second
#: time. ⭐ Nothing under `src/` writes a `unit.json`, so what a bump refuses is
#: a document some other tool wrote: it is refused, and never migrated (R9).
API = 2

#: The versions this build reads.
KNOWN_API = frozenset({API})

#: The document's key order, which is the reading order and is also what
#: reaches disk: what it is, where it came from, what it says, what it is made
#: of. ⛔ Serialised `sort_keys=False`, so this tuple is the format (R10).
UNIT_KEYS = (
    "api",
    "source",
    "address",
    "variant",
    "unit",
    "title",
    "practices",
    "sections",
    "built_from",
)

#: Two counts and no verdict. ⚠️ `declared` may be `None`.
PRACTICES_KEYS = ("declared", "archived")

#: One entry per archive document this unit was built from. ⛔ No path — see
#: the module docstring.
BUILT_FROM_KEYS = ("variant", "kind", "ordinal", "ingested", "content_sha256")


def build(
    material: Material,
    *,
    overlay: Overlay | None = None,
    declared_practices: int | None = None,
    title: str | None = None,
    mentions: Mentions | None = None,
) -> dict:
    """Assemble one served unit document from what was ingested and what was authored.

    ⭐ `mentions` serves every mention of another unit of the corpus as that
    unit, and every link to a file of the corpus from the page (`unit.mentions`);
    without it only the unit's references to itself are served.
    """
    first = material.documents[0]
    if overlay is not None:
        _require_same_unit(overlay, material)
    sections = (
        derived_shape.sections(material)
        if overlay is None
        else authored_shape.sections(overlay, material)
    )
    return {
        "api": API,
        "source": first["source"],
        "address": list(first["address"]),
        "variant": material.variant,
        "unit": material.unit,
        "title": without_outline_number(_title(title, overlay, first)),
        "practices": _practices(material, declared_practices),
        "sections": _served(sections, mentions),
        "built_from": _built_from(material),
    }


def _served(sections: tuple[dict, ...], mentions: Mentions | None) -> list[dict]:
    """Return the sections without their outline numbers, and with every reference served.

    ⭐ With no `mentions` the unit still serves its references to itself: an
    in-page anchor and a mention of its own heading need nothing of the corpus.
    """
    found = headings(sections)
    stripped = [
        {**section, "blocks": headings_without_outline_numbers(section.get("blocks"))}
        for section in sections
    ]
    return (mentions or Mentions()).sections(stripped, found)


def render(document: dict) -> str:
    """Render the document's exact bytes. ⭐ One serialisation, the archive's own."""
    return render_json(document)


def _title(title: str | None, overlay: Overlay | None, first: dict) -> str:
    """Return what this unit is called: the caller's, the author's, the archive's.

    ⚠️ **The archive's title is the fallback and not the first choice.** It is
    one document's heading; a unit is the whole set, and an author who named the
    unit meant that name for all of it.
    """
    if title is not None:
        return title
    if overlay is not None:
        return overlay.title
    return first["title"]


def _practices(material: Material, declared: int | None) -> dict:
    """Two counts, in key order. ⛔ No verdict — see the module docstring."""
    if declared is not None and (
        not isinstance(declared, int) or isinstance(declared, bool) or declared < 0
    ):
        raise ContentError(
            f"a declared practice count must be 0 or more, or nothing at all, "
            f"got {describe(declared)}"
        )
    return {"declared": declared, "archived": material.archived_practices}


def _built_from(material: Material) -> list[dict]:
    """One entry per document, in the reading order they were used in."""
    return [
        {name: document.get(name) for name in BUILT_FROM_KEYS} for document in material.documents
    ]


def _require_same_unit(overlay: Overlay, material: Material) -> None:
    """Refuse an overlay that describes a different unit from the material.

    ⛔ **Nothing downstream could notice this.** Both halves are individually
    valid; joined, they produce one page out of two units' work, with an
    author's headings over somebody else's material.
    """
    first = material.documents[0]
    if list(overlay.address.segments) != list(first["address"]):
        raise ContentError(
            "the overlay and the ingested material record different addresses, "
            "so they are not the same unit"
        )
    if overlay.unit != material.unit:
        raise ContentError(
            f"the overlay records unit {overlay.unit} and the material records unit {material.unit}"
        )
