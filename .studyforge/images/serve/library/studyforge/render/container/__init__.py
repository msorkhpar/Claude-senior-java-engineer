r"""The container page: one container and one placement decision in, bytes out.

**What it does.** Renders the page a reader lands on when they navigate
*downward* — the `*.section.html` page spec §5 names: what this container is,
its units in order, which of them are readable, and the links up to its parent
and down to its units.

**How you use it.**

    from studyforge.corpus import placement
    from studyforge.render.container import Document, Item, Placement, render

    profile = placement.profile_for(manifest.placement)
    where = Placement(
        corpus=manifest.source,
        container=profile.container(container.address, container.titles,
                                    origin=container.origin),
        shared=profile.corpus(),
    )
    document = Document(
        address=container.address,
        title=container.titles[-1],
        variant=container.variant,
        level=manifest.levels[-1],
        note=container.note,
        items=(Item(numbering="1.1", title="Your first class",
                    href="unit-01-your-first-class.unit.html"),),
    )
    (root / where.container.page).write_bytes(render(document, where))

`Links` carries the previous/index/next bar when something knows the reading
order; left out, the page renders without it. `PageError` is the only exception
this package raises, and it is `render.page`'s — a caller rendering a whole site
catches one family for both kinds of page.

**Depends on.** `corpus.placement` for where things go and for R4's identity
block, `render.templates` for the markup, and `render.page` for the escaping,
the href gate, the between-pages bar and the error type. ⛔ Not on `serve`, and
not on `contents`: this page is handed its units already ordered and already
addressed, so it renders whether or not a contents document has ever been built.

## ⛔ Why this package exists

⚠️ **The spec names `*.section.html` and discovery scans for it**, so without
this package a page is missing per container and discovery cannot find what it
scans for. ⭐ It is the one page a reader lands on when navigating downward, so its
absence is not cosmetic.

## ⭐ One renderer at every depth

⛔ **No branch on how deep a corpus is.** A 1-level corpus has one container
page and a 2-level corpus has one per module; the same renderer serves both,
because the only thing that varies is the address and the corpus's own word for
the level — which arrives as data (`levels`, in the manifest), exactly as the breadcrumb
takes it. ⚠️ A renderer that asked *"how many levels?"* would be the source
knowledge R1 forbids, in the one module where it would look reasonable.

## The rulings this package holds

⛔ **R8 — the page works over `file://`, with no network and no server.** Its
stylesheet, its script and every unit it links are addressed **relative to the
page**. ⚠️ **And the number of `../` steps is not the unit page's** — see
`placement.py`.

⛔ **R10 — what is emitted is byte-for-byte reproducible.** No clock, no
directory enumeration, no set iteration; `items` is a tuple in declared order
and `document` states the slot order.

⛔ **R13 — markup is a source file, never a Python string.** The skeleton is
`render/templates/page.html`, shared with the unit page; the unit list is a loop
body, which is the far side of the line `render/page/__init__.py` draws.

⛔ **R4 — the page carries its own identity and nothing infers it from a path.**
`kind="container"`, rendered and parsed by `corpus.placement.identity`.

⛔ **R1 — the framework knows nothing about any source.** Not a corpus name, not
a level's word, not a sentence of one site's wording.

## ⛔ What this package does NOT guarantee

⚠️ **That a linked unit page exists.** `Item.href` is checked for *shape* — it
is a permitted relative reference — and never for *presence*, because this
package touches no filesystem. ⭐ Whether the file is there is discovery's
question, and `Item(href=None)` is how a caller says it already knows it is not.
"""

from __future__ import annotations

from collections.abc import Sequence

from studyforge.render.container.document import KIND, compose
from studyforge.render.container.entries import Document, Item
from studyforge.render.container.placement import Placement
from studyforge.render.page import ENCODING, Link, Links, PageError, RailContainer, RailUnit

#: ⛔ The package's whole public surface. A consumer that has to import
#: `studyforge.render.container.document` directly is a consumer this contract
#: failed.
__all__ = [
    "ENCODING",
    "KIND",
    "Document",
    "Item",
    "Link",
    "Links",
    "PageError",
    "Placement",
    "RailContainer",
    "RailUnit",
    "compose",
    "render",
]


def render(
    document: Document,
    placement: Placement,
    links: Links | None = None,
    rail: Sequence[RailContainer] | None = None,
) -> bytes:
    """Render one container page.

    ⭐ **Bytes, not text, and that is the contract.** What is compared against a
    golden file, written to disk and served is a byte string; handing back text
    would leave the encoding to whoever wrote the file, and R10's guarantee
    would hold everywhere except the one step that matters.
    """
    return compose(document, placement, links, rail).encode(ENCODING)
