"""`content.json` — the authored overlay, and the only file in this contract a person edits.

**What it does.** Reads and validates a unit's optional human-judgement layer:
its address, its title, and its `sections` array, which the builder uses
**verbatim**.

**How you use it.** `parse(text, depth)` for the document, `load(path, depth)`
for the file.

**Depends on.** `studyforge.address`, `studyforge.archive` for the block
vocabulary and the personal-data gate, `studyforge.version` for R9, and this
package's `sections`, `trust` and `errors`.

## R21: this contract is located, versioned and produced by exactly one party

| | |
|---|---|
| **File** | `units/unit-NN/content.json`, beside the unit it overlays |
| **Address** | ⭐ **`skills.adapter.Layout.content`** — ⛔ the one computed answer (R21) |
| **Version key** | `content_api` — ⭐ **minted here**, see below |
| **Producer** | **a person**, and nothing else ever writes it |

⛔ **The `File` row is the CONTRACT's shape; the `Address` row is the only
thing that resolves it against a corpus, and no caller may compose its own**
(R21). ⭐ This module keeps `CONTENT_FILENAME` — the filename is the
contract's, minted beside `content_api` above — and `Layout` joins it to the
directory `UNITS_DIR` names, so the address has one spelling made of two
pieces, each owned where it belongs. ⚠️ **The arrow points that way and not
back:** `Layout` imports this name, this module imports no layout, because a
contract that resolved itself against an archive would be a second authority
on the archive's shape.

⚠️ **v1 LOCATES this overlay and APPLIES none, and that is a statement rather
than an omission**. ⛔ A build reads a unit from its
archive documents alone; a unit that ships an overlay is served without it,
and the verb is unowned. ⭐ It is said here because this is where a reader
asks — finding the address and inferring the feature is exactly the wrong
inference to leave available.

⭐ **`content_api` is new, and R9's list did not have it.** R9 enumerates
`corpus_api`, `container_api`, `raw_api`, `unit.json`'s `api` and the TOC
schema version — every one of them a document this framework *generates*. The
overlay is the one document it only ever *reads*, which is why it fell out of
an enumeration written from the generating side, and it is also the one
likeliest to drift, because a person edits it.

⛔ **Silence was not an option (R21), so this is a decision and not an
omission.** Three things decided it:

1. **There is nothing to migrate**: no overlay predates the version key, so
   versioning costs nothing.
2. **R9's asymmetry, which the manifest reader already lives by.** An unknown version is
   *refused, never migrated*. Adding the key later means every overlay written
   before it is refused until somebody edits it — and for a generated map that
   is a re-run, while for this file it is **a person redoing judgement work by
   hand**. The cost of adding it late is strictly worse here than anywhere else
   R9 already applies.
3. The `sections` array is used verbatim, so this file's shape *is* the page.
   A contract with that reach and no version is one that cannot be changed at
   all.

## What the overlay may say, and what it may not

⛔ **The address is recorded, never derived** (§6). An overlay that spelled
its address as *titles*, slugified to compare, would be filed under the wrong
unit about one time in eight, because a real catalogue serves that share of
its units at a slug their title does not produce. Here `address` is a list
of slugs and is compared as one.

⛔ **`workspace` and `video` are refused**, in every section. They are derived
by the builder from the archive and the placement profile — an author writing
one is asserting something they are not the author of, and it would be silently
overwritten or, worse, silently believed.

⛔ **Two sections may not share a key**, because two sections on one key mint
the same audio filenames and one unit's narration then overwrites another's.
"""

from __future__ import annotations

import json
from dataclasses import dataclass, field
from pathlib import Path

from studyforge.address import Address, require_ordinal
from studyforge.archive.blocks import BLOCK_FIELDS
from studyforge.archive.scrub import assert_clean
from studyforge.describe import describe_keys
from studyforge.unit.errors import ContentError, describe
from studyforge.unit.sections import KINDS_WITH_A_LANG, SECTION_KINDS, section_key
from studyforge.version import check as check_version

#: R9's key for this contract. ⭐ Registered in
#: `version.CONTRACT_FIELDS` in the same commit, or the shared guard cannot see
#: it.
CONTENT_API = 1
KNOWN_CONTENT_API = frozenset({CONTENT_API})

#: One spelling, because "does this unit have an overlay?" is asked by the
#: builder, by `studyforge plan` and by validation.
CONTENT_FILENAME = "content.json"

#: Every key an overlay may carry, in the order §6 writes them.
OVERLAY_KEYS = ("content_api", "address", "unit", "title", "sections")

#: Every key a section may carry. ⚠️ `key` is the escape hatch and is optional;
#: everything else here is required for the kinds that use it.
SECTION_FIELDS = ("kind", "lang", "key", "heading", "blocks")

#: ⛔ Derived by the builder, never written by an author. ⚠️ Each is already
#: refused as an unknown key; naming it here is what makes the refusal say WHY,
#: and `attachments` joined the day a page began to link them (§6): an
#: authored list would name files no ingest fetched and no build copies.
DERIVED_FIELDS = ("workspace", "video", "attachments")


@dataclass(frozen=True, slots=True)
class Section:
    """One authored section, with the key every consumer addresses it by."""

    kind: str
    key: str
    heading: str
    blocks: tuple[dict, ...] = field(default=())
    variant: str | None = None


@dataclass(frozen=True, slots=True)
class Overlay:
    """One unit's authored judgement layer. Immutable once validated."""

    address: Address
    unit: int
    title: str
    sections: tuple[Section, ...]
    content_api: int = CONTENT_API

    @property
    def keys(self) -> tuple[str, ...]:
        """Every section key, in the author's order. ⛔ Order is never re-derived."""
        return tuple(section.key for section in self.sections)

    @property
    def block_count(self) -> int:
        """How many blocks the whole overlay carries."""
        return sum(len(section.blocks) for section in self.sections)


def parse(text: str, depth: int, where: str = CONTENT_FILENAME) -> Overlay:
    """Build an `Overlay` from the text of a `content.json`."""
    try:
        document = json.loads(text)
    except TypeError, json.JSONDecodeError:
        # ⛔ The parser's own message is not formatted in: it quotes the
        # offending line, and this file is one a person edits — the line could
        # be anything (R7).
        raise ContentError(f"{where} is not valid JSON") from None
    return from_document(document, depth, where)


def load(path: Path | str, depth: int) -> Overlay:
    """Read, gate and validate one `content.json`."""
    path = Path(path)
    try:
        text = path.read_text(encoding="utf-8")
    except OSError as error:
        # ⛔ `strerror`, never the exception: an OSError renders with the
        # absolute path it was given (R7).
        raise ContentError(f"cannot read {path.name}: {error.strerror}") from None
    return parse(text, depth, path.name)


def from_document(document: object, depth: int, where: str = CONTENT_FILENAME) -> Overlay:
    """Build an `Overlay` from an already-parsed object.

    ⛔ **The personal-data gate runs first, over the whole decoded document**
    (R7). It runs here rather than field by field because the fields
    this module validates are not the fields a leak turns up in: an author's
    `heading`, a paragraph's text and a code sample are all strings nothing
    else here reads.
    """
    if not isinstance(document, dict):
        raise ContentError(f"{where} must be a JSON object, got {type(document).__name__}")
    _gate(document, where)
    check_version(
        "content_api",
        document.get("content_api"),
        KNOWN_CONTENT_API,
        where=where,
        error=ContentError,
    )
    unknown = sorted(set(document) - set(OVERLAY_KEYS))
    if unknown:
        raise ContentError(
            f"{where} has unknown key(s), {describe_keys(unknown)}; "
            f"this build reads {list(OVERLAY_KEYS)}"
        )
    missing = [key for key in OVERLAY_KEYS if key not in document]
    if missing:
        raise ContentError(f"{where} is missing required key(s) {missing}")
    return Overlay(
        address=_address_of(document["address"], depth, where),
        unit=require_ordinal(document["unit"], f"{where} 'unit'"),
        title=_title_of(document["title"], where),
        sections=_sections_of(document["sections"], where),
    )


def _gate(document: dict, where: str) -> None:
    """Refuse an overlay carrying personal data, naming the shape and not the value.

    ⛔ **`PersonalDataLeak` is raised as itself, not translated** (R7).
    `ContentError` exists so a caller walking a corpus catches one type per
    overlay, reports it and continues — so an R7 refusal inside that family
    would be logged as *"that unit did not build"*, the walk would finish, and
    the report would be green about the one thing R7 exists to make loud. ⭐ It
    is deliberately not a `ValueError` and deliberately not in this package's
    family, so it stops the run; `unit/errors.py` states it in the package's own
    contract, exactly as `unit/served.py` does one file over.
    """
    assert_clean(document, where)


def _address_of(value: object, depth: int, where: str) -> Address:
    """Return the recorded address. ⛔ A list of slugs, never a list of titles."""
    if not isinstance(value, list):
        raise ContentError(f"{where} 'address' must be a list of slugs, got {describe(value)}")
    address = Address(tuple(value))
    if address.depth != depth:
        raise ContentError(
            f"{where} declares an address {address.depth} segment(s) deep; the corpus "
            f"declares {depth} level(s)"
        )
    return address


def _title_of(value: object, where: str) -> str:
    """Return the unit's title as the author wants it read. ⚠️ A title, not a slug."""
    if not isinstance(value, str) or not value.strip():
        raise ContentError(f"{where} has no non-empty 'title'")
    return value


def _sections_of(value: object, where: str) -> tuple[Section, ...]:
    """Return the `sections` array, verbatim in order, with every key resolved."""
    if not isinstance(value, list) or not value:
        raise ContentError(f"{where} has no non-empty 'sections' array")
    sections: list[Section] = []
    seen: dict[str, int] = {}
    for index, entry in enumerate(value):
        section = _section_of(entry, index, where)
        if section.key in seen:
            raise ContentError(
                f"{where} sections {seen[section.key]} and {index} both key on "
                f"{section.key!r}; two sections sharing a key mint the same audio filenames"
            )
        seen[section.key] = index
        sections.append(section)
    if not sum(len(section.blocks) for section in sections):
        raise ContentError(
            f"{where} has {len(sections)} section(s) and no blocks in any of them; "
            f"refusing to write an empty page"
        )
    return tuple(sections)


def _section_of(entry: object, index: int, where: str) -> Section:
    """One section: its kind, its variant, its key and its blocks."""
    at = f"{where} section {index}"
    if not isinstance(entry, dict):
        raise ContentError(f"{at} is not an object")
    for name in DERIVED_FIELDS:
        if name in entry:
            raise ContentError(
                f"{at} writes {name!r}, which the builder derives from the archive and "
                f"the placement profile; it is not the author's to write"
            )
    unknown = sorted(set(entry) - set(SECTION_FIELDS))
    if unknown:
        raise ContentError(
            f"{at} has unknown key(s), {describe_keys(unknown)}; "
            f"a section carries {list(SECTION_FIELDS)}"
        )
    kind = entry.get("kind")
    if kind not in SECTION_KINDS:
        raise ContentError(f"{at} has kind {describe(kind)}: must be one of {list(SECTION_KINDS)}")
    variant = entry.get("lang")
    if kind in KINDS_WITH_A_LANG and not variant:
        raise ContentError(f"{at} is of kind {kind!r} and names no 'lang'")
    if kind == "shared" and variant:
        raise ContentError(f"{at} is 'shared' and also names a variant")
    return Section(
        kind=kind,
        key=section_key(kind, variant, entry.get("key")),
        heading=_heading_of(entry.get("heading"), at),
        blocks=_blocks_of(entry.get("blocks"), at),
        variant=variant,
    )


def _heading_of(value: object, at: str) -> str:
    """Return a section's heading. ⚠️ Read by the reader, by nothing that keys on it."""
    if not isinstance(value, str) or not value.strip():
        raise ContentError(f"{at} has no non-empty 'heading'")
    return value


def _blocks_of(value: object, at: str) -> tuple[dict, ...]:
    """Return the section's blocks, checked against `archive.blocks`' vocabulary and no other.

    ⛔ The type list is imported, never restated: a block vocabulary with two
    definitions is two answers to what a page may contain.
    """
    blocks = [] if value is None else value
    if not isinstance(blocks, list):
        raise ContentError(f"{at} has a non-list 'blocks'")
    for position, block in enumerate(blocks):
        if not isinstance(block, dict):
            raise ContentError(f"{at} block {position} is not an object")
        kind = block.get("type")
        if kind not in BLOCK_FIELDS:
            raise ContentError(
                f"{at} block {position} has type {describe(kind)}, which the block "
                f"vocabulary does not name"
            )
    return tuple(blocks)
