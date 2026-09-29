r"""A unit's headings as its own prose names them: by the source's anchor, and by its number.

**What it does.** Indexes the headings of one unit's sections, each with the id
its page gives it, the words a reader is served and the two names the source
may have used for it:

- the anchor a Markdown source links it by (`#introduction`, `#key-points-1`),
  the way the source's own renderer spelled it;
- the outline number the source wrote in front of it (`2.2` for
  `2.2. Bitmaps`), which the served heading no longer shows.

**How you use it.** `headings(sections)` over a unit's sections **before**
their outline numbers leave (`unit.builder`), then `by_slug(found)` and
`by_number(found)` for the two lookups. `unit.mentions` serves a reference
through them.

**Depends on.** `re`, `unit.outline` for the number a heading loses, and
`unit.sections` for the one spelling of a heading's id and of the link to it.

## ⛔ Only a heading of a section's own run is indexed

⭐ The page gives an id to a heading of the section's own run of blocks, at its
position there, and to nothing nested in a quote or a disclosure. So a
reference is resolved only to what the page can be scrolled to.

## ⚠️ The source's anchor is the source renderer's, duplicates and all

A Markdown source links its own headings by the anchor its host renders:
lower case, every character that is not a letter, a digit, a space, a hyphen or
an underscore dropped, each space a hyphen, and a second heading of the same
words suffixed `-1`, then `-2`. ⭐ Both the heading as the source wrote it and
the heading as it is served are indexed, so `#22-bitmaps` and `#bitmaps` both
find `2.2. Bitmaps`.

## ⛔ A number two headings share names neither

⚠️ A number that could be either heading is left as the source wrote it, which
is the rule `unit.mentions` keeps for a label two units share.
"""

from __future__ import annotations

import re
from dataclasses import dataclass

from studyforge.unit.outline import outline_number, without_outline_number
from studyforge.unit.sections import heading_anchor, heading_reference


@dataclass(frozen=True, slots=True)
class Heading:
    """One heading a reference may name: its number, words, source names, id and link."""

    number: str | None
    text: str
    slug: str
    served_slug: str
    anchor: str
    reference: str


#: A link inside a heading's words, whose label is what a source renderer slugs.
_LINK = re.compile(r"\[([^\]\n]*)\]\([^)\s]*\)")

#: Every character a source renderer drops from an anchor.
_DROPPED = re.compile(r"[^\w\- ]")


def headings(sections: object) -> tuple[Heading, ...]:
    """Return the headings of every section's own run of blocks, in reading order.

    ⛔ Read **before** the outline numbers leave: a heading's number is what a
    sentence that names it says. A heading with no words is skipped.
    """
    if not isinstance(sections, list | tuple):
        return ()
    found: list[Heading] = []
    for section in sections:
        if not isinstance(section, dict):
            continue
        key, blocks = section.get("key"), section.get("blocks")
        if not isinstance(key, str) or not isinstance(blocks, list):
            continue
        for position, block in enumerate(blocks):
            if not isinstance(block, dict) or block.get("type") != "heading":
                continue
            text = block.get("text")
            if not isinstance(text, str) or not text.strip():
                continue
            served = without_outline_number(text).strip()
            found.append(
                Heading(
                    number=outline_number(text),
                    text=served,
                    slug=source_slug(text),
                    served_slug=source_slug(served),
                    anchor=heading_anchor(key, position),
                    reference=heading_reference(key, position),
                )
            )
    return tuple(found)


def source_slug(text: str) -> str:
    """Return the anchor a Markdown source's host renders for a heading's words."""
    words = _LINK.sub(r"\1", text).strip().lower()
    return _DROPPED.sub("", words).replace(" ", "-")


def by_slug(found: tuple[Heading, ...]) -> dict[str, str]:
    """Return every source anchor of `found` -> how the page links the heading it names.

    ⭐ The source's own anchors first, suffixed as its host suffixes a repeat;
    then the served words' anchor, where no source anchor already holds it.
    """
    anchors: dict[str, str] = {}
    seen: dict[str, int] = {}
    for heading in found:
        count = seen.get(heading.slug, 0)
        seen[heading.slug] = count + 1
        anchors.setdefault(
            heading.slug if count == 0 else f"{heading.slug}-{count}", heading.reference
        )
    for heading in found:
        anchors.setdefault(heading.served_slug, heading.reference)
    return anchors


def by_number(found: tuple[Heading, ...]) -> dict[str, Heading]:
    """Return every outline number `found` holds exactly once -> its heading."""
    numbered: dict[str, Heading] = {}
    shared: set[str] = set()
    for heading in found:
        if heading.number is None:
            continue
        if heading.number in numbered:
            shared.add(heading.number)
        numbered[heading.number] = heading
    return {number: heading for number, heading in numbered.items() if number not in shared}
