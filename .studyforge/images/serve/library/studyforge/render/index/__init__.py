r"""The root index: two contents documents in, one self-contained page out.

**What it does.** Renders the single page a reader opens by double-clicking it —
the corpus's whole hierarchy, to whatever depth it declares, as real disclosure
elements with a linked row per unit that this machine can read.

**How you use it.**

    from studyforge import contents as toc
    from studyforge.corpus import placement
    from studyforge.render.index import Placement, from_contents, render

    where = Placement(shared=placement.profile_for(manifest.placement).corpus())
    document = from_contents(
        toc.load(site / toc.TOC_FILENAME),
        toc.load_status(site / toc.STATUS_FILENAME),
        where,
    )
    (site / where.shared.root_index).write_bytes(render(document, where))

`PageError` is the only exception this package raises, and it is
`render.page`'s — a caller rendering a whole site catches one family for all
three kinds of page.

**Depends on.** `contents` for the two documents and their checked join,
`corpus.placement` for where things go, `render.templates` for the markup,
`render.markup` for the escaping and the href gate, `render.pageassets` for the
two shared filenames, and `render.page` for the error type. ⛔ Not on `serve`, not
on `corpus.container`, not on `corpus.discovery`, and not on the filesystem.

## ⛔ Its only corpus input is the two contents documents, and that is the point

⚠️ **The rendering design: *"if this page can be built, the contract carries everything a renderer
needs; if it cannot, the contract is missing something."*** ⭐ So `from_contents`
lives **here** rather than in every caller, and the isolation is a property of
this package that `test_assemble` asserts over the source — ⛔ not a claim about
whoever happens to call it.

⚠️ **The limit, stated rather than discovered**: the *corpus* input
is those two documents. This package still loads the framework's own page
skeleton and asset names, exactly as the other two renderers do, and neither
carries anything about any corpus (R1).

## ⛔ It fetches nothing at runtime, and it cannot

⚠️ **A `fetch('toc.json')` passes every served test and then dies silently over
`file://`** — no origin to ask, an empty index, and no error anywhere. ⭐ The
contents are read at **generation** time and the tree is baked into the bytes;
the page carries no script of its own at all, so the failure is unrepresentable
rather than merely avoided (R8).

## ⛔ This page's list is CONTENT, so a refused href RAISES

⭐ **The question is not *"is it a link"*: it is *if this element vanishes, has
the reader lost a WAY TO GET SOMEWHERE, or has the page lost THE THING IT EXISTS
TO SHOW?"*** ⛔ **This page is nothing but its tree**, so a dropped anchor is a
unit nobody can open from the page a reader lands on first, with every title
present and nothing logged. ⚠️ `page.navigation` drops for the opposite reason
and is right to: a bar is chrome. ⭐ A unit with **no page on this machine** is
neither — it is `Item(href=None)`, listed and marked `data-readable="false"`
(§7's three states).

## ⛔ Real disclosure elements, because the floor is a double-clicked file

⭐ `<details>` opens, closes and takes keyboard focus with **scripting off
entirely** (R8), and a row's anchor is its unit key, so a deep link addresses a
row inside its disclosures rather than the disclosure itself. ⚠️ Which levels
start open is computed from the corpus's own shape — `policy` — so a one-level
corpus reads as a plain nested list and a deep one does not unfold its longest
level on top of the reader, with no branch on which corpus it is (R1).

## What is in the package

| Module | Owns |
|---|---|
| `entries` | `Item`, `Section`, `Document`, and the shapes each refuses |
| `assemble` | `from_contents` — the two documents in, one record out |
| `policy` | which levels start open, and the arithmetic that decides |
| `disclosure` | the tree itself, its anchors, and the one gate on them |
| `placement` | where the index sits, and every href it writes |
| `document` | what is written, in what order, out of which template |
| `__init__` | the contract, and `render` |
"""

from __future__ import annotations

import typing

from studyforge.render.index.assemble import from_contents
from studyforge.render.index.disclosure import (
    LEVEL_KIND,
    LIST_LABEL,
    NUMBERING_KIND,
    READABLE_ATTRIBUTE,
)
from studyforge.render.index.document import EMPTY_SLOTS, SKELETON, compose
from studyforge.render.index.entries import Document, Item, Section
from studyforge.render.index.placement import Placement
from studyforge.render.index.policy import VISIBLE_ROW_BUDGET, open_to, row_counts, visible_rows
from studyforge.render.page import ENCODING, PageError

if typing.TYPE_CHECKING:  # ⛔ Annotation-only: neither name joins this surface.
    from collections.abc import Sequence

    from studyforge.render.page import RailContainer

#: ⛔ The package's whole public surface. A consumer that has to import
#: `studyforge.render.index.disclosure` directly is a consumer this contract
#: failed.
__all__ = [
    "EMPTY_SLOTS",
    "ENCODING",
    "LEVEL_KIND",
    "LIST_LABEL",
    "NUMBERING_KIND",
    "READABLE_ATTRIBUTE",
    "SKELETON",
    "VISIBLE_ROW_BUDGET",
    "Document",
    "Item",
    "PageError",
    "Placement",
    "Section",
    "compose",
    "from_contents",
    "open_to",
    "render",
    "row_counts",
    "visible_rows",
]


def render(
    document: Document,
    placement: Placement,
    rail: Sequence[RailContainer] | None = None,
) -> bytes:
    """Render the root index, with the rail across containers when one is handed in.

    ⭐ **Bytes, not text, and that is the contract.** What is compared against a
    golden file, written to disk and served is a byte string; handing back text
    would leave the encoding to whoever wrote the file, and R10's guarantee
    would hold everywhere except the one step that matters.
    """
    return compose(document, placement, rail).encode(ENCODING)
