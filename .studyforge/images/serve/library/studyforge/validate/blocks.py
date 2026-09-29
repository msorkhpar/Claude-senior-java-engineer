r"""Every block's shape, read once for `validate` and for the fixture check.

**What it does.** Walks every block, into container blocks and into list items, and names
where one breaks spec §6: a block that is not an object, a type not in `BLOCK_TYPES`, keys
that are not its fields then only its `BLOCK_OPTIONAL` keys, and for a `list` block an item
that is neither a string nor an array of strings and whole blocks of an `ITEM_BLOCKS` type
(a nested list or a code block), or a `start` that is not an integer.

**How you use it.** `block_problems(blocks)` yields `(where, what)` pairs, and
`check_block_shapes(walk)` yields them as `document` findings. ⛔ `tests/fixture_checks` calls
`block_problems` through this module too, so the two agree through ONE reader.

**Depends on.** `archive.blocks` for the vocabulary, `describe`, `validate.corpus`,
`validate.report`. ⛔ Not `archive.markdown`: this reads what was written, never how.

⚠️ **A block of an unknown type is named and read no further**: there is no row to read its
keys or what it holds against.
"""

from __future__ import annotations

from collections.abc import Iterator

from studyforge.archive.blocks import (
    BLOCK_FIELDS,
    BLOCK_OPTIONAL,
    BLOCK_TYPES,
    CONTAINER_TYPES,
    ITEM_BLOCKS,
)
from studyforge.describe import describe
from studyforge.validate.corpus import RULE_DOCUMENT, Walk
from studyforge.validate.report import Finding

LIST = "list"


def block_problems(blocks: object, where: str = "blocks") -> Iterator[tuple[str, str]]:
    """Every place a block in `blocks` breaks spec §6, at any depth."""
    if not isinstance(blocks, list):
        return
    for index, block in enumerate(blocks):
        yield from _block(block, f"{where}[{index}]")


def _block(block: object, at: str) -> Iterator[tuple[str, str]]:
    if not isinstance(block, dict):
        yield at, f"is {describe(block)}; a block is an object with a type"
        return
    kind = block.get("type")
    if kind not in BLOCK_TYPES:
        yield at, f"has type {describe(kind)}; the block types are {list(BLOCK_TYPES)}"
        return
    fields, optional = BLOCK_FIELDS[kind], BLOCK_OPTIONAL[kind]
    keys = tuple(block)
    extra = keys[len(fields) :]
    if keys[: len(fields)] != fields or extra != tuple(key for key in optional if key in extra):
        then = f", then only {list(optional)}" if optional else ""
        yield at, f"has keys {list(keys)}; a {kind} carries {list(fields)}{then}"
    if kind == LIST:
        yield from _list(block, at)
    elif kind in CONTAINER_TYPES:
        yield from block_problems(block.get("blocks"), f"{at}.blocks")


def _list(block: dict, at: str) -> Iterator[tuple[str, str]]:
    if "start" in block and (
        not isinstance(block["start"], int) or isinstance(block["start"], bool)
    ):
        yield f"{at}.start", f"is {describe(block['start'])}; start is an integer"
    items = block.get("items")
    if not isinstance(items, list):
        yield f"{at}.items", f"is {describe(items)}; items is an array"
        return
    for number, item in enumerate(items):
        yield from _item(item, f"{at}.items[{number}]")


def _item(item: object, at: str) -> Iterator[tuple[str, str]]:
    if isinstance(item, str):
        return
    if not isinstance(item, list):
        yield at, f"is {describe(item)}; an item is a string, or an array of strings and blocks"
        return
    for number, part in enumerate(item):
        here = f"{at}[{number}]"
        if isinstance(part, dict) and part.get("type") in ITEM_BLOCKS:
            yield from _block(part, here)
        elif not isinstance(part, str):
            yield (
                here,
                f"is {describe(part)}; a part of an item is a string or a whole block "
                f"of a type in {list(ITEM_BLOCKS)}",
            )


def check_block_shapes(walk: Walk) -> Iterator[Finding]:
    """Refuse, by where it sits, every block spec §6 does not admit."""
    for unit in walk.units:
        for at, what in block_problems(unit.document.get("blocks")):
            yield Finding(RULE_DOCUMENT, unit.where, f"{at} {what} (spec §6)")
