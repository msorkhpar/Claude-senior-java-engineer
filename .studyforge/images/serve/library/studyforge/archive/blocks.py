r"""The block vocabulary — **the** block vocabulary — and a practice's layout.

**What it does.** States what a block may be, once: its name, the key its
count is written under, the fields it carries, and whether it holds other
blocks. Also reads the layout of a practice, which is the one thing the
archive document does not say outright.

**How you use it.** `BLOCKS` is the list; `BLOCK_TYPES`, `CONTAINER_TYPES`,
`COUNT_KEYS` and `BLOCK_FIELDS` are derived from it and are what consumers
import. `counts_of(blocks)` for a document's `counts`, `walk(blocks)` to
reach nested ones, `read_layout(document)` for a practice's parts.

**Depends on.** `archive.errors`, and `describe` to name a block this
vocabulary does not admit without quoting what it says (R7). ⛔ Not on
`archive.document`, which imports *this*, and not on `archive.markdown`, which
also imports this: the vocabulary is the leaf that both the reader and the
format hang from.

## One list, and why it is a list of rows

⛔ **One definition, imported** — the Markdown reader's block and container
types and the fixture checker's count keys, container blocks and block fields
all read it. Two copies of a contract disagree with each other.

⭐ **A row per block type rather than four parallel
tuples**, because four tuples that must stay in the same order are three
chances to get the order wrong. Adding a block type is one row, and it is a
`raw_api` change — a block type is a contract, not a convenience.

⚠️ **The two copies disagreed about order, and the disagreement was data.**
`markdown/`'s tuple had `list` before `table`; the fixtures' `counts` object —
which is what reaches disk — has `tables` before `lists`. **The reader's order
moved**, because `counts` is serialised in this order and an unchanged
document must re-render to identical bytes (R10), while the reader's tuple was
only ever a membership test. Nothing in the tree asserted the reader's order,
which is exactly why the wrong one was the easy one to keep.

## `video` is a block type *and* a document record

⚠️ Both, and they are not the same thing. A `video` **block** is content — it
sits in `blocks` in reading order and carries `src` and `title`. The
document's `video` **record** is the unit's headline video and carries
`VIDEO_KEYS` (`archive.document`): how the page plays it, plus the addresses
it came from. ⛔ Do not fold one into the other while consolidating lists;
this is the fact most easily lost here.
"""

from __future__ import annotations

from collections.abc import Iterator
from dataclasses import dataclass

from studyforge.archive.errors import ArchiveError
from studyforge.describe import describe


@dataclass(frozen=True, slots=True)
class BlockType:
    """One row of the vocabulary: everything the framework knows about a type.

    `count_key` is deliberately not derived from `name`. English plurals are
    irregular where it matters — `code` counts as `code` and `html` as `html`,
    not `codes` and `htmls` — and a rule with two exceptions is a lookup table
    that has not admitted what it is.
    """

    name: str
    count_key: str
    fields: tuple[str, ...]
    holds_blocks: bool = False
    #: Keys written only when they say something, AFTER `fields`.
    #: ⭐ A block without one is byte-identical to the same block before it existed.
    optional: tuple[str, ...] = ()


#: The vocabulary, in the order `counts` is written (R10). ⛔ Eleven rows, and
#: a twelfth is a `raw_api` change.
BLOCKS = (
    BlockType("heading", "headings", ("type", "level", "text")),
    BlockType("para", "paras", ("type", "text")),
    BlockType("code", "code", ("type", "lang", "text")),
    BlockType("table", "tables", ("type", "headers", "rows")),
    BlockType("list", "lists", ("type", "ordered", "items"), optional=("start",)),
    BlockType("image", "images", ("type", "src", "alt", "width")),
    BlockType("video", "videos", ("type", "src", "title")),
    BlockType("rule", "rules", ("type",)),
    BlockType("quote", "quotes", ("type", "blocks"), holds_blocks=True),
    BlockType("html", "html", ("type", "text")),
    BlockType(
        "disclosure",
        "disclosures",
        ("type", "summary", "open", "blocks"),
        holds_blocks=True,
    ),
)

#: The names, in vocabulary order.
BLOCK_TYPES = tuple(block.name for block in BLOCKS)

#: The types that hold other blocks. ⭐ Every walker recurses on this rather
#: than naming `quote` and then forgetting the next one — which is how
#: `disclosure` was nearly missed. **Present but withheld** is a third state
#: between shown and absent: the archive records that the content is disclosed
#: on demand and what its label is, and that the markup is `<details>` is the
#: renderer's decision, not this document's (R13).
CONTAINER_TYPES = tuple(block.name for block in BLOCKS if block.holds_blocks)

#: `count key -> block type`, in the order `counts` is serialised.
COUNT_KEYS = {block.count_key: block.name for block in BLOCKS}

#: `block type -> its keys, in order`.
BLOCK_FIELDS = {block.name: block.fields for block in BLOCKS}


#: The block types a list item may hold as a PART, in vocabulary order: a
#: nested list, and a code block. ⭐ One line, so what an item may hold is
#: answered once for the reader, the validator, the page and the narration.
ITEM_BLOCKS = ("code", "list")


def item_parts(item: object) -> list:
    """Return one list item's parts in reading order: text strings and whole blocks.

    ⛔ **What a list item is (spec §6).** A string when it holds no
    block — every plain list is written as it always was — and
    otherwise an ARRAY of its parts in the order the author wrote them: runs of
    text, and whole blocks of the types `ITEM_BLOCKS` names — a nested `list`,
    whose items follow this same rule, or a `code` block. ⚠️ An
    array and not `{"text", "list"}`, because material continues an item with a
    paragraph AFTER its nested list or its code and then opens another, and a
    pair could only refuse that or reorder it.

    ⭐ **A code block inside an item stays inside it.** Material writes a step,
    its snippet, and the sentence after the snippet as one item; closing the
    list around the code detaches that sentence from its step.

    ⭐ **Not a twelfth block type and not a `raw_api` change**: `list` keeps its
    three fields and its count key, and a document with no block in an item reads
    exactly as it did; an older build refuses the new shape by name rather than
    misreading it. ⚠️ A part is part of its item, not a block in
    reading order, so `counts_of` does not count it and `walk` does not yield
    it; a consumer wanting every string walks the values, as `assert_clean`
    does. Every consumer reads an item through this function, so what an item
    may be is answered once.
    """
    return list(item) if isinstance(item, list) else [item]


#: `block type -> the keys it may carry after its fields`, in order.
BLOCK_OPTIONAL = {block.name: block.optional for block in BLOCKS}


def list_start(block: dict) -> int:
    """Return the number an ordered `list` block's first item carries: its `start`, else 1.

    ⛔ **What an ordered list records about its numbering (spec §6).**
    `start` is written only when the author's first marker is not `1`, so an
    author who continues a step list after a code block with `2.` keeps `2`,
    on the page and aloud. ⭐ A list starting at one carries no `start` and is
    byte-identical to every list written before, and so is its document's
    `content_sha256`. ⚠️ Not a `raw_api` change for that reason. Every consumer
    reads the number through this function, so the default is answered once.
    """
    start = block.get("start")
    return start if isinstance(start, int) and not isinstance(start, bool) else 1


#: `block type -> its row`, for a consumer that has a type and wants the rest.
BY_NAME = {block.name: block for block in BLOCKS}


def counts_of(blocks: list, where: str = "blocks") -> dict[str, int]:
    """One entry per count key — **always all of them**, including the zeroes.

    ⛔ A count that disappears when it is zero cannot be told from a count
    nobody wrote, and noticing an ingest that came back short is the whole
    reason they are recorded (R6).

    ⚠️ **Top-level blocks only.** A paragraph inside a quote is not counted,
    and that is the inherited behaviour rather than an oversight: `counts`
    answers "how long is this document", and anything wanting the total walks
    `walk` deliberately.

    ## ⛔ A block that is not an object is REFUSED BY NAME (R6)

    ⚠️ **Reaching `.get` on one** would send `AttributeError` — a Python error
    naming a *type* — out through `archive.document.build`. ⛔ The one thing
    that message cannot say is the thing the reader needs: **which block**.
    ⭐ `validate` filters to object blocks first; the builder has no such
    filter and needs none, because a document it must refuse is refused rather
    than counted.

    ⭐ `where` is the document's own, spelled as `assert_clean`'s is, and the
    refusal names the index and **describes** the value rather than quoting
    it (R7) — the same sentence `validate.blocks` yields for the same shape.
    """
    types = _types_of(blocks, where)
    return {
        key: sum(1 for kind in types if kind == block_type)
        for key, block_type in COUNT_KEYS.items()
    }


def _objects(blocks: list, where: str) -> Iterator[dict]:
    """Each top-level block, refusing BY NAME anything that is not an object.

    ⛔ **The one place this refusal is spelled** (on the build path and on the
    read path alike). `counts_of` reads it on the way IN, through `build`;
    `read_layout` reads it on the way OUT, over a document that was parsed off
    disk and never built here. ⚠️ **Two doors, one sentence** — a second copy
    would disagree with the first.

    ⭐ **A generator rather than a check beside a loop**, so a caller cannot
    take the blocks without taking the refusal: there is no unguarded way to
    iterate them.
    """
    for index, block in enumerate(blocks):
        if not isinstance(block, dict):
            raise ArchiveError(
                f"{where}[{index}] is {describe(block)}; a block is an object with a type"
            )
        yield block


def _types_of(blocks: list, where: str) -> list:
    """Each top-level block's `type`, refusing by name anything that is not an object.

    ⭐ One pass rather than eleven: the types are read once and the eleven
    counts are taken over that, so no block is reached twice and a refusal
    cannot depend on which count key was being tallied when it was reached.
    """
    return [block.get("type") for block in _objects(blocks, where)]


def walk(blocks: list) -> Iterator[dict]:
    """Every block, containers first, then the blocks they hold, depth-first.

    The one recursion over the vocabulary. ⛔ A consumer that names `quote`
    itself is the next `disclosure` waiting to be forgotten.
    """
    for block in blocks:
        yield block
        if isinstance(block, dict) and block.get("type") in CONTAINER_TYPES:
            yield from walk(block.get("blocks") or [])


# ---------------------------------------------------------------------------
# A practice's layout
# ---------------------------------------------------------------------------

#: The three level-2 headings a practice is laid out under, in order. ⚠️ These
#: are **content**, not format: an adapter writes them into the blocks, and
#: this module recognises them. That is why they are matched rather than
#: declared in the document's own keys — the archive stores what the source
#: said, and the layout is a reading of it.
STATEMENT_HEADING = "Problem statement"
LESSON_HEADING = "Lesson"
STARTING_CODE_HEADING = "Starting code"


@dataclass(frozen=True, slots=True)
class Layout:
    """A practice's parts, without re-parsing anything.

    `blocks` on the document stays the whole thing — the three headings and
    the fence included — so a consumer that wants the file as it reads gets
    that, and one that wants the parts gets these.
    """

    statement: tuple[dict, ...]
    lesson: tuple[dict, ...]
    lesson_title: str | None
    starting_code: str
    starting_lang: str | None


def _is_h2(block: object, text: str | None = None) -> bool:
    return (
        isinstance(block, dict)
        and block.get("type") == "heading"
        and block.get("level") == 2
        and (text is None or block.get("text") == text)
    )


def _is_lesson_heading(block: object) -> bool:
    if not _is_h2(block):
        return False
    text = block.get("text", "")  # type: ignore[union-attr]
    return text == LESSON_HEADING or text.startswith(LESSON_HEADING + ": ")


def read_layout(document: dict, where: str) -> Layout | None:
    """Return a practice's parts, or `None` for a lesson.

    ⛔ The document's own `kind` decides. A lesson is never split into
    practice sections however it is laid out, and a practice whose blocks lack
    the three sections is **refused** rather than quietly returned as a lesson
    (R6) — looking finished while being short is the failure the counts exist
    to catch, and it applies here too.

    ⚠️ Lesson-tab headings are floored to level 3, so no level-2 heading can
    occur inside the lesson section; statement headings are not floored, which
    is why the lesson heading is searched for from the front and the
    starting-code heading only after it.

    ## ⛔ A non-object block is refused BY NAME here too (R6)

    ⚠️ **The BUILD path's guard is not enough.** `_sections` reading
    `tail[0].get("type")` unguarded would let a hand-written or adapter-written
    practice raise `AttributeError` — a Python error naming a TYPE — where
    `build` names the BLOCK and the FILE.
    ⛔ **And `build` is not the door such a document arrives by**: it is parsed
    off disk, and `parse` reads the key set and the version, never a shape.

    ⭐ **Guarded BEFORE the layout is read, not at the fence.** Only `tail[0]`
    crashed — but `_is_h2` merely returns False for a non-object, so guarding
    the one read that raised would leave every other position silently unread
    and the document refused for the WRONG reason (a guarantee does not extend
    to what sits beside it).
    """
    if document.get("kind") != "practice":
        return None
    blocks = list(_objects(document.get("blocks") or [], f"{where} blocks"))
    parts = _sections(blocks)
    if parts is None:
        raise ArchiveError(
            f"{where} says kind 'practice' but its blocks do not carry "
            f"'## {STATEMENT_HEADING}', '## {LESSON_HEADING}' and "
            f"'## {STARTING_CODE_HEADING}' with exactly one fence under it"
        )
    statement, lesson_title, lesson, code = parts
    return Layout(
        statement=tuple(statement),
        lesson=tuple(lesson),
        lesson_title=lesson_title,
        starting_code=code["text"],
        starting_lang=code["lang"] or None,
    )


def _sections(blocks: list) -> tuple[list, str | None, list, dict] | None:
    """Return `(statement, lesson title, lesson, code)`, or `None` if not laid out.

    ⛔ **Every block reaching here is an object**, because `read_layout` takes
    them through `_objects`, which refuses anything else by name. ⚠️ That is a
    PRECONDITION rather than a habit: it is what makes `tail[0].get` below safe,
    and a caller reaching this function by another route reintroduces that unguarded read.
    """
    if not blocks or not _is_h2(blocks[0], STATEMENT_HEADING):
        return None
    lesson_at = next(
        (i for i, block in enumerate(blocks) if i > 0 and _is_lesson_heading(block)), None
    )
    if lesson_at is None:
        return None
    code_at = next(
        (
            i
            for i, block in enumerate(blocks)
            if i > lesson_at and _is_h2(block, STARTING_CODE_HEADING)
        ),
        None,
    )
    if code_at is None:
        return None
    tail = blocks[code_at + 1 :]
    if len(tail) != 1 or tail[0].get("type") != "code":
        return None
    heading = blocks[lesson_at]["text"]
    title = heading[len(LESSON_HEADING) + 2 :] if heading != LESSON_HEADING else None
    return blocks[1:lesson_at], title, blocks[lesson_at + 1 : code_at], tail[0]
