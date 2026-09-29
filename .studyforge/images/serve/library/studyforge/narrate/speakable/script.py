r"""What each block type becomes as speech, and the walk that puts them in order.

**What it does.** Decides, per block type, what the narrator says — prose read as
written, a fence not at all, a list read item by item (a list of code examples
not at all), a table read row
by row, a disclosure's summary spoken and its body withheld — and walks one
section's blocks into ordered `SpeechUnit`s.

**How you use it.** `units_of(unit, section_key, blocks)` returns
`(units, withheld)` for one section. `ordinal_word` is published because it is a
separately testable phrasing decision.

**Depends on.** `studyforge.archive.blocks` for the one block vocabulary and the
one recursion over it, `studyforge.archive.scrub` for the gate,
and this package's `naming`, `panel`, `records` and `voice`.

## ⛔ The disposition table is CLOSED, and a twelfth block type fails the build

⭐ **`SPEECH_OF` names every block type and what happens to it.** ⛔ The extraction
source's rule was *"a block type with no branch here is spoken as a paragraph"* —
an open set that fails toward **acceptance**, so a new container type would have
had its children silently read aloud out of a collapsed section. ⚠️ Here an
unknown type **raises**, and `test_script.py` asserts `SPEECH_OF` covers
`BLOCK_TYPES` exactly, so the twelfth row of the vocabulary cannot land without
somebody deciding what it sounds like.

## ⛔ A fence is never spoken — not its body, and not a caption for it

⭐ **Register ruling (2026-09-26): narration covers a lesson's prose only.** A code
example is shown and never narrated, so `SPEECH_OF` calls `code` `silent`, exactly
as it calls an image silent, and a code part inside a list item adds nothing to
that item's clip. ⛔ No sentence stands in for the fence either: a caption
announcing the code below is words the author did not write, spoken over
something the reader has to look at anyway.

⚠️ **A silent fence still spends its position**, because a position is where a
block sits and not how many units came before it — so a prose unit's id, and its
clip, never depend on whether a fence beside it is spoken. ⭐ The escape hatch is the
archive and it costs no field: a corpus that wants its steps narrated emits them
as prose blocks rather than as fences, decided once at extraction (R1).

## ⛔ A list of code examples is never spoken, and neither is the heading over it

⭐ **Register ruling (2026-09-26): a lesson's code-example panel is a code
example, and a code example is never narrated.** A list whose every item is one
link to a code file and a short label (`Source: …`, `Test: …`) is the list the
page draws as that panel (`render.page.code`), so it yields no unit — no item
of it, whatever its words. `panel.py` says what such a list is.

⭐ **A heading over a panel and nothing spoken is silent**, and keeps its
position; `panel.py` says why a heading over a lone fence still speaks.

## ⛔ Narration speaks a disclosure's summary and stops. It never walks the body

A `disclosure` is content the author decided the reader should **choose** to see.
Reading it aloud overrides that decision silently, on a surface the reader cannot
see — the page still shows the section collapsed while the audio gives away what is
inside it. ⭐ **The decisive argument is §8.5's, about read marks: a record the
reader cannot trust is worse than none.** Narration that *sometimes* reads out an
answer is narration nobody can leave playing, and that loses the feature for a
whole corpus rather than for one lesson.

⚠️ **Withheld is not dropped, and R6 applies.** The summary gets a speech id; the
body gets **none**, so a clip for it cannot be minted or addressed. The count comes
back so a coverage report can name the unit. ⛔ **No spoken sentence announcing the
hidden section is invented here** — that would be narration writing prose the author
did not.

## ⛔ An empty block spends its number anyway

⚠️ A block whose transform leaves nothing to say yields no unit and **never shifts
the numbering of anything after it**, because the position comes from where the
block sits and not from how many units have been emitted. ⭐ A lesson that gains a
figure renumbers only the blocks after it.

## ⛔ The gate runs here, on every string, regardless of what ran upstream (R7)

⛔ **It runs at BOTH ends of the transform.** A local hostname — one of the four
shapes the gate's own vocabulary recognises — in a list item would otherwise be
**admitted**: `split_identifier` respaces the machine-name shape — a host
followed by the `.local` suffix — into two words, destroying the dot the gate's
pattern anchors on.
⭐ So `_spoken` gates the **source** string and `_emit` gates the **derived**
one, and `test_script.py` asserts every refusing row of
the personal-data shape table through every carrier this module has — an
emitter and its gate read as a pair (R7), both halves this module's own.

⭐ **It refuses; it does not scrub.** `scrub` is for words this framework wrote —
a log line, a path in a report. A spoken string is derived from **the source's**
words, and rewriting one into a placeholder would mean an mp3 that quietly says
something the material does not, with nobody knowing personal data had ever been
there. ⚠️ `archive.scrub`'s own contract rules it: *scrub our words, refuse the
source's*. ⛔ So `PersonalDataLeak` travels out of here as itself and is never
translated into `SpeakableError`.
"""

from __future__ import annotations

from studyforge.archive.blocks import BLOCK_TYPES, CONTAINER_TYPES, item_parts, list_start, walk
from studyforge.archive.scrub import assert_clean
from studyforge.narrate.speakable.naming import speech_id
from studyforge.narrate.speakable.panel import code_examples, unheard_headings
from studyforge.narrate.speakable.records import SpeakableError, SpeechUnit
from studyforge.narrate.speakable.voice import spoken_text

#: Spelled out, never a numeral: an engine reads "1." as a decimal. Past twentieth
#: — never yet observed — an ordered item falls back to "Item 21,".
ORDINALS = (
    "First",
    "Second",
    "Third",
    "Fourth",
    "Fifth",
    "Sixth",
    "Seventh",
    "Eighth",
    "Ninth",
    "Tenth",
    "Eleventh",
    "Twelfth",
    "Thirteenth",
    "Fourteenth",
    "Fifteenth",
    "Sixteenth",
    "Seventeenth",
    "Eighteenth",
    "Nineteenth",
    "Twentieth",
)

#: ⛔ **The closed disposition table** — every block type, and what it sounds like.
#: `prose` reads its `text`; `items` and `rows` address below block level;
#: `silent` is shown and never spoken — a fence among them; `recurse`
#: walks a container's children; `summary` speaks a container's label and withholds
#: everything under it. ⚠️ Asserted total over `BLOCK_TYPES` by this module's test.
SPEECH_OF = {
    "heading": "prose",
    "para": "prose",
    "code": "silent",
    "table": "rows",
    "list": "items",
    "image": "silent",
    "video": "silent",
    "rule": "silent",
    "quote": "recurse",
    "html": "silent",
    "disclosure": "summary",
}


def ordinal_word(position: int) -> str:
    """Return "First", "Second", … "Twentieth", then "Item 21"."""
    if 1 <= position <= len(ORDINALS):
        return ORDINALS[position - 1]
    return f"Item {position}"


def units_of(
    unit: str,
    section_key: str,
    blocks: object,
    path: tuple[int, ...] = (),
) -> tuple[tuple[SpeechUnit, ...], int]:
    """Return one section's ordered speech units and the count of blocks withheld.

    `unit` is a flattened unit token, `section_key` the served section's key, and
    `path` the positions already walked into — empty for a section's own blocks.

    ⛔ A heading over a panel and nothing spoken says nothing (`panel.unheard_headings`),
    and its words are gated all the same.
    """
    listed = blocks if isinstance(blocks, list) else []
    said: list[list[SpeechUnit]] = []
    withheld = 0
    for index, block in enumerate(listed):
        here = (*path, index)
        kind = block.get("type") if isinstance(block, dict) else None
        rule = SPEECH_OF.get(kind if isinstance(kind, str) else "")
        if rule is None:
            raise SpeakableError(
                f"no speech is defined for a block of this type, and the vocabulary is "
                f"{list(BLOCK_TYPES)} — a type with no disposition would be read aloud by "
                f"accident, so it is refused here instead"
            )
        found, held = _one_block(unit, section_key, block, kind, rule, here)
        said.append(found)
        withheld += held
    for index in unheard_headings(listed, [bool(found) for found in said]):
        said[index] = []
    return tuple(unit for found in said for unit in found), withheld


def _one_block(
    unit: str,
    section_key: str,
    block: dict,
    kind: str,
    rule: str,
    path: tuple[int, ...],
) -> tuple[list[SpeechUnit], int]:
    """Return what one block says and how much of it was held back."""
    if rule == "silent":
        return [], 0
    if rule == "recurse":
        found, held = units_of(unit, section_key, block.get("blocks"), path)
        return list(found), held
    where = speech_id(unit, section_key, path)
    if rule == "summary":
        said = _emit(unit, section_key, path, _spoken(block.get("summary"), where), kind)
        return said, sum(1 for _ in walk(block.get("blocks") or []))
    if rule == "items":
        return _items(unit, section_key, block, path, where), 0
    if rule == "rows":
        return _rows(unit, section_key, block, path, where), 0
    return _emit(unit, section_key, path, _spoken(block.get("text"), where), kind), 0


def _spoken(value: object, where: str) -> str:
    """Return `value` spoken, having gated it BEFORE the transform as well as after.

    ⛔ **The transform must not be able to launder a leak.** A local hostname — one of
    the four shapes the gate's own vocabulary recognises — in a list item would
    otherwise be **admitted**: the identifier splitter respaces the machine-name
    shape — a host followed by the `.local` suffix — into two words, destroying
    the dot the gate anchors on.
    ⭐ So the source string is gated here and the derived string again in
    `_emit` — R7's rule that an emitter and its
    gate are read as a pair, with the pair being this module's own two ends.
    """
    assert_clean(value, where)
    return spoken_text(value)


def _items(
    unit: str, section_key: str, block: dict, path: tuple[int, ...], where: str
) -> list[SpeechUnit]:
    """Return one unit per list item, each numbered aloud when the list is ordered.

    ⛔ A list of code examples says nothing at all (`code_examples`).
    """
    if code_examples(block):
        return []
    ordered = bool(block.get("ordered"))
    items = block.get("items")
    said: list[SpeechUnit] = []
    for position, item in enumerate(items if isinstance(items, list) else []):
        words = _item_words(item, where)
        if ordered and words:
            words = f"{ordinal_word(position + list_start(block))}, {words}"
        said += _emit(unit, section_key, path, words, "list", position)
    return said


#: What a sentence already ends with, so joining two parts adds no second stop.
_STOPS = (".", ":", ";", ",", "!", "?")


def _item_words(item: object, where: str) -> str:
    """Return one item's words: its parts in reading order, a nested list item by item.

    ⛔ **A nested list is spoken INSIDE its parent item's clip**, each of
    its items numbered aloud when that list is ordered, and a code part says
    nothing there, exactly as a top-level fence says nothing. ⭐ That keeps the speech-id
    grammar as it is — a list's items are the only thing addressed below a block,
    and one level of them — and the page puts the audio on the parent `<li>`,
    which holds the nested list. ⚠️ A plain string item says exactly what it said
    before, since it has one part.
    """
    said: list[str] = []
    for part in item_parts(item):
        if not isinstance(part, dict):
            said.append(_spoken(part, where))
            continue
        if part.get("type") == "code":
            # ⛔ A code part says what a top-level fence says: nothing at all.
            continue
        nested = part.get("items")
        for position, sub in enumerate(nested if isinstance(nested, list) else []):
            words = _item_words(sub, where)
            if part.get("ordered") and words:
                words = f"{ordinal_word(position + list_start(part))}, {words}"
            said.append(words)
    joined = ""
    for words in (words.strip() for words in said):
        if words:
            joined = (
                f"{joined}{' ' if joined.endswith(_STOPS) else '. '}{words}" if joined else words
            )
    return joined


def _rows(
    unit: str, section_key: str, block: dict, path: tuple[int, ...], where: str
) -> list[SpeechUnit]:
    """Return one unit per table row, each cell labelled by its own header.

    ⚠️ The header row is never spoken on its own — it is folded into every cell
    below it, and saying it twice is how a table stops being listenable.
    """
    headers = [_spoken(cell, where) for cell in _cells(block.get("headers"))]
    rows = block.get("rows")
    said: list[SpeechUnit] = []
    for position, row in enumerate(rows if isinstance(rows, list) else []):
        words = _row_speech(headers, [_spoken(cell, where) for cell in _cells(row)])
        if not headers:
            words = f"Row {position + 1}: {words}" if words else ""
        said += _emit(unit, section_key, path, words, "table", position)
    return said


def _cells(value: object) -> list:
    """Return a row's cells, or nothing at all when the archive recorded none."""
    return value if isinstance(value, list) else []


def _row_speech(headers: list[str], cells: list[str]) -> str:
    """Return one row spoken, cell by cell, labelled where there is a header to label it."""
    if not headers:
        return ", ".join(cell for cell in cells if cell)
    said = []
    for position, cell in enumerate(cells):
        label = headers[position] if position < len(headers) else ""
        said.append(f"{label}: {cell}." if label else f"{cell}.")
    return " ".join(said)


def _emit(
    unit: str,
    section_key: str,
    path: tuple[int, ...],
    words: str,
    kind: str,
    sub_index: int | None = None,
) -> list[SpeechUnit]:
    """Return one gated unit, or nothing at all when there is nothing left to say."""
    said = (words or "").strip()
    if not said:
        return []
    identifier = speech_id(
        unit, section_key, path, sub_index, kind if sub_index is not None else None
    )
    assert_clean(said, identifier)
    return [
        SpeechUnit(
            id=identifier,
            speak=said,
            section=section_key,
            block_path=path,
            sub_index=sub_index,
            kind=kind,
        )
    ]


#: ⛔ Named so the reader of `SPEECH_OF` can see the two container rules are the two
#: container types, rather than trusting that they are.
CONTAINER_RULES = {kind: SPEECH_OF[kind] for kind in CONTAINER_TYPES}
