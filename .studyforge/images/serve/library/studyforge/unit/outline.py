r"""The source's own outline number, left off every title and heading a reader is served.

**What it does.** Removes a leading outline number — `5.1.1.1 `, `3.2.1. `,
`10.7.2 — `, `1. ` — from a title or a heading's text, and from every heading
block in a run of blocks; takes an unmistakable one off the front of a list
item; serves a paragraph that is only an outline as a list of its entries; and
leaves every other word alone.

**How you use it.** `without_outline_number(text)` for one string;
`outline_number(text)` for the number it takes off, which `unit.headings`
indexes a heading by; `headings_without_outline_numbers(blocks)` for a
section's blocks, and
`listed_numbering(numbering, ordinal)` for the chip a listing shows. The served
unit builder applies both to what it serves, and the contents tree to every
title it lists, so the page, its chrome and its narration all read the same
words.

**Depends on.** `re` and `archive.blocks` for the one recursion over the
vocabulary.

## ⛔ Why the number goes, and where it stays

⭐ **The site lists and orders everything itself.** The contents, the rail and
the index already put each unit in its place, so a title that repeats the
source's `5.1.1.1` is a second numbering beside the site's own, and read aloud
it is four numbers before every heading. ⛔ **The archive keeps it**: the
archive records what the source says, `origin.section` is matched against the
heading as the file writes it, and a container map's titles are records. Only
what a reader is SERVED leaves it off, which is a rule this framework applies
to every corpus rather than a setting a corpus chooses.

⚠️ **A page's file name keeps it.** The file name is built from the recorded
label or ordinal, and it is an address rather than something a reader reads:
renaming every page of every corpus would move its links for no reader's gain.

## ⛔ A number that is part of the words is never touched

⚠️ **The shape is narrow on purpose.** An outline number is at the very start,
is either dotted (`3.2`, `3.2.1`, `3.2.1.`) or one number closed by a full stop
(`3.`), has at most three digits per part, and is followed by a space and then
the words, with an optional dash or colon between. ⭐ So `Java 21 features`,
`ISO 8583 messages`, `Top 10 pitfalls`, `10 tips`, `2024 in review` and a
bare `3.2.1` with nothing after it keep every character. ⚠️ A two-part number
with no closing stop, followed by a word that counts or measures, reads as a
quantity (`1.5 million requests`, `3.5 seconds`) and is kept too.

⛔ **What the next word LOOKS like decides nothing.** `8.1 iOS builds`,
`4.2 var`, `2.3 java.util.function`, `8.1 "quoted"` and `8.1 (optional)` all
lose their number: a heading's first word is often an identifier, and an
identifier starts however its language spells it. Only the closed list of
counting and measuring words below (`QUANTITY_WORDS`) keeps a two-part number,
because that is what a decimal quantity is followed by.
"""

from __future__ import annotations

import re

from studyforge.archive.blocks import CONTAINER_TYPES

#: A leading outline number and what separates it from the words.
#: ⛔ Dotted, or a single number closed by a stop; each part at most three digits.
OUTLINE_NUMBER = re.compile(
    r"^(?P<number>\d{1,3}(?:\.\d{1,3})+(?P<stop>\.)?|\d{1,3}\.(?P<single>))"
    r"(?:[ \t]*[—–:]|[ \t]+-)?[ \t]+(?=\S)"
)

#: How many dotted parts make a number that could be a quantity when unstopped.
QUANTITY_PARTS = 2

#: ⛔ The words after which an unstopped two-part number is a quantity and is kept.
#: A closed list, matched exactly: any other next word, whatever its case, loses the number.
QUANTITY_WORDS = frozenset(
    {
        "thousand",
        "million",
        "billion",
        "trillion",
        "percent",
        "times",
        "x",
        "ms",
        "milliseconds",
        "seconds",
        "second",
        "minutes",
        "minute",
        "hours",
        "hour",
        "days",
        "day",
        "weeks",
        "months",
        "years",
        "bytes",
        "bits",
        "kb",
        "mb",
        "gb",
        "tb",
    }
)

#: The next word, as `QUANTITY_WORDS` spells it: its leading run of letters.
_WORD = re.compile(r"[A-Za-z]+")


def without_outline_number(text: str) -> str:
    """Return `text` with its leading outline number removed, or `text` itself.

    ⛔ A value that is not a string is returned as it came: refusing a malformed
    title is the reader's job, never this one's.
    """
    if not isinstance(text, str):
        return text
    match = OUTLINE_NUMBER.match(text)
    if match is None:
        return text
    number = match.group("number")
    unstopped = match.group("single") is None and match.group("stop") is None
    if unstopped and number.count(".") + 1 == QUANTITY_PARTS and _counts(text[match.end() :]):
        return text
    return text[match.end() :]


def outline_number(text: object) -> str | None:
    """Return the outline number `without_outline_number` takes off `text`, stop and all dropped.

    ⭐ `2.2. Bitmaps` gives `2.2`; a heading it leaves whole (`Java 21`,
    `1.5 million`) gives `None`, so a number is only ever the one a reader lost.
    """
    if not isinstance(text, str) or without_outline_number(text) == text:
        return None
    match = OUTLINE_NUMBER.match(text)
    return None if match is None else match.group("number").rstrip(".")


def _counts(words: str) -> bool:
    """Say whether `words` opens with a word that counts or measures."""
    word = _WORD.match(words)
    return word is not None and word.group() in QUANTITY_WORDS


#: A label that is an outline number and nothing else: dotted, or closed by a stop.
OUTLINE_LABEL = re.compile(r"^\d{1,3}(?:\.\d{1,3})+\.?$|^\d{1,3}\.$")


def listed_numbering(numbering: str, ordinal: int) -> str:
    """Return what a listing shows beside a unit: its place, never the source's outline number.

    ⭐ **The label keeps the number, and the listing does not show it.** A
    container map records a unit's `label` as the source numbers it (`4.2.4`),
    and that record still orders the unit and names its page file. What a
    reader sees beside the title is the unit's place in its container
    (`ordinal`), because the listing is the site's own contents. ⛔ A label
    that is not an outline number (`7`, `A`, `Appendix`) is shown as recorded.
    """
    if isinstance(numbering, str) and OUTLINE_LABEL.match(numbering):
        return str(ordinal)
    return numbering


def headings_without_outline_numbers(blocks: object) -> object:
    """Return `blocks` with every heading's and list item's outline number removed, at any depth.

    ⭐ **A copy, never an edit**: the blocks are the archive's, and the served
    document is built beside them. A block that is not a heading and holds no
    blocks is the same object; anything that is not a list comes back as it came.
    """
    if not isinstance(blocks, list):
        return blocks
    return [_block(block) for block in blocks]


#: A list item's opening outline number: unmistakably one, so at least three
#: dotted parts or two closed by a stop, after an optional emphasis or link opener.
ITEM_NUMBER = re.compile(
    r"^(?P<opener>\*\*|__|\*|_|\[)?"
    r"(?:\d{1,3}(?:\.\d{1,3}){2,}\.?|\d{1,3}\.\d{1,3}\.)[ \t]+(?=\S)"
)


def without_item_number(text: object) -> object:
    """Return a list item's text with its opening outline number removed, or as it came.

    ⭐ **An item is prose, so the shape is narrower than a heading's.** A
    source's sub-lesson list (`**8.3.2.1. Extracting patterns** -- …`,
    `[7.3.2.1. LocalDate](…)`) opens each item with the number of a heading it
    stands for, and a reader is served neither. ⛔ Only a number no sentence
    starts with is taken: three dotted parts or more, or two closed by a stop.
    `1.5 million`, `2.0 is out` and `Java 21` keep every character.
    """
    if not isinstance(text, str):
        return text
    match = ITEM_NUMBER.match(text)
    if match is None:
        return text
    return (match.group("opener") or "") + text[match.end() :]


#: One entry of an outline written as a paragraph: a stopped dotted number, then words.
_ENTRY = re.compile(r"(?:^|[ \t\n]+)(\d{1,3}(?:\.\d{1,3})+)\.[ \t]+(?=\S)")


def outline_entries(text: object) -> list[str]:
    """Return the entries of a paragraph that is only an outline, or `[]` for any other.

    ⭐ **A source's outline, written as lines of a paragraph** (`7.2.1. Batch
    processing` / `7.2.2. Clearing files` / …) reads, once its lines are joined,
    as one run of numbers and words. It is served as a list of its entries, each
    without its number. ⛔ Only when the paragraph OPENS with an entry, holds two
    or more, and every number is a sibling of the first (`7.2.1.`, `7.2.2.`):
    a sentence that mentions `3.2.1.` somewhere is never one.
    """
    if not isinstance(text, str):
        return []
    parts = _ENTRY.split(text)
    numbers, words = parts[1::2], parts[2::2]
    if parts[0] or len(numbers) < 2 or not all(word.strip() for word in words):
        return []
    parent = numbers[0].rsplit(".", 1)[0]
    if any(number.rsplit(".", 1)[0] != parent for number in numbers):
        return []
    return [word.strip() for word in words]


def _block(block: object) -> object:
    if not isinstance(block, dict):
        return block
    if block.get("type") == "heading":
        return {**block, "text": without_outline_number(block.get("text"))}
    if block.get("type") == "list":
        return {**block, "items": _items(block.get("items"))}
    if block.get("type") == "para":
        entries = outline_entries(block.get("text"))
        if entries:
            return {"type": "list", "ordered": False, "items": entries}
    if block.get("type") in CONTAINER_TYPES:
        return {**block, "blocks": headings_without_outline_numbers(block.get("blocks"))}
    return block


def _items(items: object) -> object:
    """Every item's opening number removed: a string item, or each text part of one."""
    if not isinstance(items, list):
        return items
    return [
        [without_item_number(part) if isinstance(part, str) else _block(part) for part in item]
        if isinstance(item, list)
        else without_item_number(item)
        for item in items
    ]
