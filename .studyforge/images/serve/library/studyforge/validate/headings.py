r"""Counting headings in raw source, and bounding the region one unit is.

**What it does.** Reads ATX heading lines out of Markdown **without a Markdown
reader**: how many a file carries, and — where a unit is a region of a shared
file — how many its region carries.

**How you use it.** `count_headings(text)` for a whole file, `region(text,
section)` for one unit's slice of it. `headings(text)` is the scan both are
built on, for a caller that wants the depths.

**Depends on.** `re` and `dataclasses`. ⛔ **Nothing else, and never
`archive.markdown`** — this module exists to produce a number the parser did
not, and a scan that borrowed the parser's would agree with it by
construction. `tests/studyforge/validate/test_headings.py` asserts the import
is absent rather than trusting this sentence.

## ⛔ Why the region is bounded by a heading

⭐ **`check_completeness` exists to disagree with the parser**, so the bound
may not come from it. An *anchor* — `TestCases.md#card-issuance` — would: a
slug is a renderer's rule about how it names a heading, and reading one back
means agreeing with a renderer about the answer. ⚠️ A **line range** is
parser-independent too and is refused for a different reason: it is brittle
against an upstream file that grows a paragraph, and these corpora are living
repositories.

⭐ **A heading needs neither.** The regex below already knows a heading's depth,
so the region is "this heading, and every heading after it that is deeper,
stopping at the first that is not" — computed from the same scan that produces
the count, with no second reading of anything.

## ⚠️ The section is matched as written, and that is a refusal, not a guess

The text compared against `section` is the heading line with its hashes and
its **surrounding** whitespace removed, and nothing else: a closing-hash
heading (`## Card issuance ##`) carries the trailing hashes in its text.
⛔ A corpus that writes the section a different way from the file gets
`occurrences == 0` — a named finding pointing at the unit — rather than a
region that quietly starts somewhere else. ⭐ **Zero and two are both loud**;
that is the whole reason uniqueness is a rule.
"""

from __future__ import annotations

import re
from dataclasses import dataclass

#: An ATX heading, with its depth and its text. ⛔ Deliberately not the Markdown
#: reader's: this number exists to disagree with the parser, so it may not come
#: from it. ⚠️ Seven hashes is not a heading and `#no-space` is not one either,
#: which is what the trailing group is for.
HEADING_LINE = re.compile(r"^ {0,3}(#{1,6})(?:[ \t]+(.*?))?[ \t]*$")

#: A fence opening or closing. ⚠️ Fence awareness is the whole difficulty: a
#: `#` inside a code block is a comment in half the languages this framework
#: will meet, and counting it would make the check cry wolf on correct output.
FENCE = re.compile(r"^ {0,3}(`{3,}|~{3,})")


@dataclass(frozen=True, slots=True)
class Heading:
    """One ATX heading line, as the raw source carries it."""

    depth: int
    text: str


@dataclass(frozen=True, slots=True)
class Region:
    """What a `section` names in one file: whether it is unique, and how big.

    ⛔ **`occurrences` is reported rather than resolved.** A section occurring
    twice is sixteen silent short reads if the reader picks one, so the count
    travels to the caller and the caller refuses. `headings` is meaningful
    only when `occurrences` is 1, and is 0 otherwise.
    """

    section: str
    occurrences: int
    headings: int


def headings(text: str) -> list[Heading]:
    """Every ATX heading outside fenced code, in the order the file carries them.

    ⚠️ **Fence-aware, and that is not decoration.** A `#` at the start of a
    line inside a fence is a comment in Python, Ruby, shell and YAML; counting
    those would make this check fire on correct output, and a check that fires
    on correct output is a check somebody turns off.

    ⛔ **And list-aware, because a fence inside a list item is measured from
    the item.** An item's content starts at its content column, so a fence
    opener written four spaces in under `1. ` is one space into that item and
    opens a fence, where at the top level four spaces is indented code and
    opens nothing. ⚠️ Reading every fence from column zero would take that opener
    for text, pair the next two fences the wrong way round, and count a heading
    inside a fence while skipping one outside it. ⭐ So the scan keeps the
    content columns of the items it is inside (`_Items`), and a fence or a
    heading is matched relative to the innermost one — the reading a standard
    CommonMark parser takes.
    """
    found: list[Heading] = []
    fence: _Fence | None = None
    items = _Items()
    for raw in text.splitlines():
        line = raw.expandtabs(TAB)
        if fence is not None:
            if fence.holds(line):
                if fence.closed_by(line):
                    fence = None
                continue
            # ⛔ A line left of the item the fence sits in ends the item, and the fence with it.
            fence = None
        if not line.strip():
            items.blank()
            continue
        column = items.enter(line)
        rest = line[column:]
        marker = LIST_MARKER.match(rest)
        if marker is not None and not THEMATIC.match(rest):
            column = items.open(column, marker)
            rest = line[column:] if len(line) > column else ""
        opened = FENCE.match(rest)
        if opened is not None:
            fence = _Fence(opened.group(1)[0], len(opened.group(1)), column)
            items.structural()
            continue
        match = HEADING_LINE.match(rest)
        if match is not None:
            found.append(Heading(len(match.group(1)), (match.group(2) or "").strip()))
            items.structural()
            continue
        items.paragraph()
    return found


#: How many columns a tab advances to, as CommonMark reads indentation.
TAB = 4

#: A list marker and the spaces after it: a bullet, or up to nine digits and `.`
#: or `)`. ⛔ At most three spaces in, measured from the enclosing item.
LIST_MARKER = re.compile(r"^( {0,3})([-*+]|\d{1,9}[.)])( +|$)")

#: A thematic break, which `- - -` and `* * *` would otherwise read as a marker.
THEMATIC = re.compile(r"^ {0,3}([-*_])(?: *\1){2,} *$")

#: A closing fence: nothing after its run but spaces.
CLOSING = re.compile(r"^ {0,3}(`{3,}|~{3,}) *$")


@dataclass(slots=True)
class _Fence:
    """An open fence: its character, its length, and the column of the item it sits in."""

    char: str
    length: int
    column: int

    def holds(self, line: str) -> bool:
        """Whether `line` is still inside the item this fence sits in."""
        return not line.strip() or _indent(line) >= self.column

    def closed_by(self, line: str) -> bool:
        """Whether `line` closes this fence: the same character, at least as many."""
        closing = CLOSING.match(line[self.column :])
        return (
            closing is not None
            and closing.group(1)[0] == self.char
            and len(closing.group(1)) >= self.length
        )


class _Items:
    """The content columns of the list items a line is inside, outermost first.

    ⭐ Only what decides where a fence or a heading starts is kept: an item
    stays open while its lines are indented to its content, a blank line does
    not close it, and an unindented line straight after its paragraph is that
    paragraph's lazy continuation rather than the item's end.
    """

    def __init__(self) -> None:
        self.columns: list[int] = []
        self.in_paragraph = False

    def enter(self, line: str) -> int:
        """Leave every item `line` sits left of, and return the column it is read from."""
        indent = _indent(line)
        lazy = self.in_paragraph and not _starts_block(line)
        while self.columns and indent < self.columns[-1] and not lazy:
            self.columns.pop()
        return min(indent, self.columns[-1]) if self.columns else 0

    def open(self, column: int, marker: re.Match) -> int:
        """Open an item at `marker`, found at `column`, and return its content column."""
        spaces = len(marker.group(3))
        width = len(marker.group(1)) + len(marker.group(2))
        # ⚠️ Five or more spaces after a marker is one space and indented code.
        inner = column + width + (spaces if 1 <= spaces <= 4 else 1)
        while self.columns and self.columns[-1] >= inner:
            self.columns.pop()
        self.columns.append(inner)
        self.in_paragraph = False
        return inner

    def blank(self) -> None:
        self.in_paragraph = False

    def structural(self) -> None:
        self.in_paragraph = False

    def paragraph(self) -> None:
        self.in_paragraph = True


def _indent(line: str) -> int:
    return len(line) - len(line.lstrip(" "))


def _starts_block(line: str) -> bool:
    """Whether `line` opens a block of its own, and so cannot continue a paragraph lazily."""
    head = line.lstrip(" ")
    return bool(
        FENCE.match(head)
        or HEADING_LINE.match(head)
        or THEMATIC.match(head)
        or LIST_MARKER.match(head)
    )


def count_headings(text: str) -> int:
    """Count ATX heading lines outside fenced code."""
    return len(headings(text))


def region(text: str, section: str) -> Region:
    """Find the region `section` opens, and count the headings inside it.

    ⛔ **The bound is the next heading of the same or shallower depth**, never
    the next heading of any depth: a unit's own subsections belong to it, and
    stopping at the first of them would report a short read on every unit that
    has one. ⭐ The opening heading counts — the region *is* that heading and
    what it introduces, which is what the parser sees for the unit too.
    """
    found = headings(text)
    matched = [index for index, heading in enumerate(found) if heading.text == section]
    if len(matched) != 1:
        return Region(section, len(matched), 0)
    opening = matched[0]
    depth = found[opening].depth
    inside = 1
    for heading in found[opening + 1 :]:
        if heading.depth <= depth:
            break
        inside += 1
    return Region(section, 1, inside)
