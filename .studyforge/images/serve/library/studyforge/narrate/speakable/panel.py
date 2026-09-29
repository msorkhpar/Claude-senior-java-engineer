r"""What a lesson's code-example panel is, read from the served document.

**What it does.** Answers whether one `list` block is the list a page draws as
its code-example panel: every item one link to a code file and a short label
(`Source: …`, `Test: …`), and nothing else.

**How you use it.** `code_examples(block)` is `True` for such a list, and
`unheard_headings(blocks, heard)` names the headings over one with nothing
spoken beside it. The script (`script.py`) gives neither a speech unit.

**Depends on.** `archive.blocks.item_parts` for what an item is,
`render.markup.segments` for the one inline-marker parser (as `voice.py` takes
it), and `corpus.manifest.SOURCE_SUFFIXES` for what a code file is called. ⛔ No
corpus is read: the table is the framework's constant.

## ⛔ A code-example panel is a code example, and it is never narrated

⭐ **Register ruling (2026-09-26).** The panel's lines name files, a source and
its test; spoken, they are a list of filenames read over something the reader
has to open anyway. ⭐ **The rule is the page's, read from the document**:
`render.page.code` draws a list as the panel when each item is one code link
and a label of at most `MAX_LABEL` characters, and this reads the same line
before it is drawn.

⚠️ **It reads a little wider than the page draws.** A code file here is one
written in a source suffix of ANY runtime this framework knows, never only the
ones the corpus declares, and its place is not asked. So the script of a
document depends on the document alone, and a list of code links is silent
whether or not its page sits beside the corpus's files. ⭐ The page's own
reading is the narrower one, so every panel it draws is a list this module
silences.

## ⭐ The heading over a panel and nothing spoken is silent too

A heading's blocks are those up to the next heading of its level or above.
When they hold a panel and nothing that speaks, the heading (*Code Examples*)
names only the panel, so it says nothing either. ⚠️ A heading over nothing but
a fence keeps its words: they name what the reader is looking at, and they are
often the one spoken trace of a worked example. ⛔ A silent heading keeps its
position, so nothing after it is renumbered.
"""

from __future__ import annotations

import re
from urllib.parse import unquote

from studyforge.archive.blocks import item_parts
from studyforge.corpus.manifest import SOURCE_SUFFIXES
from studyforge.render.markup import segments

#: The most characters a code-example line carries beside its one link: a label
#: (`Source:`, `Test:`), not prose. ⚠️ `render.page.code.MAX_LABEL` is the page's
#: reading of the same line, and a test holds the two equal.
MAX_LABEL = 40

#: Every suffix a source file of a runtime this framework knows is written in.
#: ⭐ The framework's table, never one corpus's declaration: the script of a
#: document depends on the document alone.
CODE_SUFFIXES = tuple(sorted({one for suffixes in SOURCE_SUFFIXES.values() for one in suffixes}))

#: An href that carries a scheme, and so names no file beside the page.
_SCHEME = re.compile(r"^[A-Za-z][A-Za-z0-9+.-]*:")


def code_examples(block: dict) -> bool:
    """Whether a list is a lesson's code examples: every item one code link and a label.

    ⭐ The list a page draws as its code-example panel, read from the document
    and a little wider than the page reads it (see the module contract).
    """
    items = block.get("items")
    return isinstance(items, list) and bool(items) and all(_code_line(item) for item in items)


def _code_line(item: object) -> bool:
    """Whether one list item is one link to a code file and a short label, and nothing else."""
    parts = item_parts(item)
    if len(parts) != 1 or not isinstance(parts[0], str):
        return False
    found = segments(parts[0])
    links = [href for kind, _body, href in found if kind == "link"]
    label = "".join(body for kind, body, _href in found if kind != "link")
    return len(links) == 1 and len(label.strip()) <= MAX_LABEL and _code_file(links[0])


def _code_file(href: str) -> bool:
    """Whether a relative `href` names a file written in a known runtime's source suffix."""
    path = re.split(r"[?#]", href, maxsplit=1)[0]
    if not path or _SCHEME.match(href) or href.startswith("/") or href.startswith("#"):
        return False
    named = [part for part in unquote(path).split("/") if part not in ("", ".")]
    return bool(named) and named[-1] != ".." and named[-1].endswith(CODE_SUFFIXES)


def unheard_headings(blocks: list, heard: list[bool]) -> list[int]:
    """Return the positions of the headings over a code-examples panel and nothing spoken.

    `heard` says, per block, whether it yields any speech unit.

    ⭐ A heading's blocks are those after it up to the next heading of its level
    or above. ⚠️ Read from the last heading back, so a subheading that fell
    silent leaves nothing spoken under the heading above it. ⛔ Blocks with no
    panel among them keep their heading, however silent they are.
    """
    heard = list(heard)
    silent: list[int] = []
    for index in range(len(blocks) - 1, -1, -1):
        level = _heading_level(blocks[index])
        if level is None or not heard[index]:
            continue
        end = index + 1
        while end < len(blocks) and not _closes(blocks[end], level):
            end += 1
        under = blocks[index + 1 : end]
        if any(_panel(one) for one in under) and not any(heard[index + 1 : end]):
            heard[index] = False
            silent.append(index)
    return silent


def _panel(block: object) -> bool:
    """Whether `block` is a code-examples list, or a quote holding one."""
    if not isinstance(block, dict):
        return False
    if block.get("type") == "quote":
        inner = block.get("blocks")
        return any(_panel(one) for one in (inner if isinstance(inner, list) else []))
    return block.get("type") == "list" and code_examples(block)


def _heading_level(block: object) -> int | None:
    """Return a heading block's level, or `None` for any other block."""
    if not isinstance(block, dict) or block.get("type") != "heading":
        return None
    level = block.get("level")
    return level if isinstance(level, int) and not isinstance(level, bool) else 1


def _closes(block: object, level: int) -> bool:
    """Whether `block` is a heading at `level` or above, which ends a heading's blocks."""
    found = _heading_level(block)
    return found is not None and found <= level
