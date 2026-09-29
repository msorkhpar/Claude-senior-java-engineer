"""The strict Markdown reader: authored text in, archive blocks out.

**What it does.** Turns the Markdown an author actually wrote into the
archive's block vocabulary — `heading`, `para`, `code`, `list`, `table`,
`image`, `video`, `rule`, `quote`, `html` and `disclosure` — and refuses,
loudly, anything it cannot represent without loss.

**How you use it.**

    from studyforge.archive.markdown import parse, MarkdownError
    blocks = parse(text)

A bare fence keeps no language unless the adapter passes `lang_default`: a
default is a declaration the adapter owns, never a guess (spec §8.4, `Q7`).

`BLOCK_TYPES` is the vocabulary and `CONTAINER_TYPES` names the two block types
that hold other blocks, so a walker can recurse on them without knowing which
they are.

**Depends on.** The standard library. ⛔ Not on `render` or `serve`: the
archive is the input to a page, and a vocabulary that knew how it would be
displayed would have stopped being a vocabulary.

⛔ **The one rule that overrides every other judgement here: never silently
drop a line.** A construct this reader does not recognise raises rather than
being swallowed; an unclosed fence raises rather than absorbing the rest of the
document as code. It is a line-oriented reader over exactly the constructs real
teaching material emits — **not a general Markdown engine** — and emphasis,
inline code and links are left untouched inside `text`, because stripping them
is exactly the loss it exists to prevent.

⚠️ **Fence awareness is the constraint that actually bites**, and it is the one
a reader passes every other test without having. Material quotes XML and HTML
inside fences constantly; a reader that scanned for `<` would read a quoted
`<details>` as a disclosure and a quoted `<div>` as raw markup. `scan` tests
for a fence **first**, and the disclosure reader skips fences using the code
reader itself, so "where does this fence end" has one definition.

⚠️ **Three things here are deliberately the opposite of the extraction
source**, and all three are intended rather than drift. It emits **no block**
for a thematic break, returns a quote's inner blocks **transparently**, and
**flattens** a disclosure to its summary — each to agree with its own DOM
reader, for which `<hr>` and `<blockquote>` are not block tags and a
`<details>` is markup. ⭐ A repository-shaped source has no DOM reader to agree
with, so studyforge makes all three block types. The last is not merely
different but would be wrong here: every surveyed use of `<details>` hides an
**exercise answer**, and flattening shows it outright.

**Layout.** A package rather than a module because the port surface is 678
lines against R11's 400, and because these are genuinely separate jobs:

- `patterns` — every expression, and the defect each one fixes.
- `scan` — what opens at a line, and the line arithmetic. Fence-first.
- `errors` — the refusal, and why refusing is the rule.
- `leaf` — the readers producing one block.
- `table`, `listing` — the two constructs with rules of their own.
- `container` — `quote` and `disclosure`, the blocks that hold blocks.
- `document` — the dispatcher.
"""

from __future__ import annotations

from studyforge.archive.blocks import BLOCK_TYPES, CONTAINER_TYPES
from studyforge.archive.markdown.document import parse
from studyforge.archive.markdown.errors import MarkdownError

__all__ = ["BLOCK_TYPES", "CONTAINER_TYPES", "MarkdownError", "parse"]
