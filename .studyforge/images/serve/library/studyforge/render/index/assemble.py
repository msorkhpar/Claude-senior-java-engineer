r"""The two contents documents in, one renderable index out — and nothing else in.

**What it does.** Turns the stable contents and this machine's status into the
record `disclosure` renders, resolving each unit's href against where the index
sits.

**How you use it.**

    from studyforge import contents as toc
    from studyforge.render.index import Placement, from_contents, render

    where = Placement(shared=profile.corpus())
    document = from_contents(toc.load(site / toc.TOC_FILENAME),
                             toc.load_status(site / toc.STATUS_FILENAME), where)
    (site / where.shared.root_index).write_bytes(render(document, where))

**Depends on.** `contents` for the two documents and for the checked join,
`entries` for what comes out, and `placement` for the href arithmetic.
⛔ **Nothing that reads a corpus.** Not `corpus.container`, not
`corpus.manifest`, not `corpus.discovery`, not the archive, not the filesystem —
and `test_assemble` asserts that over this package's source rather than
promising it.

## ⛔ This function is the acceptance clause, not a convenience

⚠️ **The rendering design: *"It reads only the two contents documents — never the filesystem,
never a catalog. That isolation is the point: if this page can be built, the
contract carries everything a renderer needs."*** ⭐ Left to the caller, the
clause would be a claim about a caller, and every consumer would retype the same
twenty lines — ⛔ which R19 calls a hole in the thing that should have produced
it. **So the mapping lives here and the isolation is a property of this package.**

⚠️ **The limit, stated rather than discovered**: the *corpus* input
is those two documents. ⭐ The renderer still loads this framework's **own**
source files — the page skeleton and the two asset names — exactly as every
other renderer does, and those carry nothing about any corpus (R1).

## ⛔ The pair is JOINED, never zipped

⚠️ Two documents joined on ids that no longer mean the same unit produce a
plausible index and no symptom. ⭐ `contents.join` refuses that pair naming which
half is stale, so this module calls it and never walks the two in step itself.

## ⛔ Present, absent, and unmentioned are three different answers

⭐ `present` false is §7's *declared absence*: the unit is listed with no link.
⛔ **A declared unit the local document does not mention at all is a fault**, not
a third way of saying absent: it means the pair joined but the annotation is
short, and an index built from it would quietly claim to know something it does
not. ⚠️ `contents.status` gives every declared unit a row precisely so this
branch never fires on a document it wrote.
"""

from __future__ import annotations

from studyforge.contents import Contents, Entry, Group, LocalStatus, UnitStatus, join
from studyforge.render.index.entries import Document, Item, Section
from studyforge.render.index.placement import Placement
from studyforge.render.page import PageError


def from_contents(contents: Contents, local: LocalStatus, placement: Placement) -> Document:
    """Return the index of `contents`, as this machine can render it today.

    ⛔ Pure: the same pair of documents and the same placement give the same
    record, every run, on every machine (R10).
    """
    known = join(contents, local)
    return Document(
        title=contents.title,
        levels=contents.levels,
        sections=tuple(
            _section(group, known, placement, (position,))
            for position, group in enumerate(contents.groups, start=1)
        ),
    )


def _section(
    group: Group,
    known: dict[str, UnitStatus],
    placement: Placement,
    at: tuple[int, ...],
) -> Section:
    """Return one container level, and everything under it, in the order it is held."""
    return Section(
        level=group.level,
        key=group.key,
        title=group.title,
        sections=tuple(
            _section(child, known, placement, (*at, position))
            for position, child in enumerate(group.groups, start=1)
        ),
        items=tuple(
            _item(entry, known, placement, (*at, position))
            for position, entry in enumerate(group.entries, start=1)
        ),
    )


def _item(
    entry: Entry,
    known: dict[str, UnitStatus],
    placement: Placement,
    at: tuple[int, ...],
) -> Item:
    """Return one unit's row, linked when this machine has a page for it."""
    state = known.get(entry.key)
    if state is None:
        # ⛔ The key is DESCRIBED by its position and never echoed (R7). This
        # branch fires precisely because the local document is short, so the
        # thing that would be quoted is whatever the contents happen to hold.
        raise PageError(
            f"the local status annotates every declared unit and says nothing about "
            f"the one at position {'.'.join(str(step) for step in at)} of this corpus; "
            f"an index built from a short annotation lists a unit whose readability "
            f"nobody actually looked up"
        )
    return Item(
        key=entry.key,
        numbering=entry.numbering,
        title=entry.title,
        href=placement.unit(entry.page) if state.present else None,
    )
