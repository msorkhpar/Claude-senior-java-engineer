r"""What a container page is rendered from: the container, and the units it lists.

**What it does.** Holds the two records the container renderer takes — one unit
as its container lists it, and the container itself — and refuses the shapes
that would render as a page saying nothing.

**How you use it.**

    from studyforge.render.container import Document, Item

    Item(numbering="1.1", title="Your first class", href="unit-01-….unit.html")
    Item(numbering="1.2", title="Fields and constructors")   # ⭐ no page yet
    Document(address=address, title="Getting Started", variant="java",
             items=(…,), level="module", note="What this module is.")

**Depends on.** `address` for what an address is and `render.page` for
`PageError`. ⛔ Nothing that reads a file: what a corpus declares is
`corpus.container`'s, what exists on this machine is `contents.status`'s, and
this record is what a caller assembles out of the two.

## ⛔ `href=None` is *declared absence*; a refused href is a *fault*

⚠️ **The two look alike and must not be handled alike.** A unit whose page has
not been generated is a real, expected state — §7's three states — and it is
listed **without** a link, so a reader sees the material's full shape and which
of it is readable today. ⛔ An href that is present and unusable is something
else entirely, and `listing` **raises** on it rather than dropping the anchor.

⭐ **That is the one place this page deliberately differs from the between-units
bar.** `page.navigation._link` drops a refused slot, because chrome that cannot
be followed is worse than chrome that is not there — and when the refusal is
wrong, slots are gone with nothing raised.
⛔ **Here the links are not chrome, they are the page**, so a silent drop would
turn a module's contents into an unclickable list with no error anywhere.

## ⛔ No href is composed here, and none is stored either

⚠️ `Item.href` arrives already answered, because only the study order knows the
path arithmetic — the study order's *"no href is stored, because there is no single
correct one"*, one layer out. ⭐ The caller asks `relative_href(container page,
unit page)`; a renderer that spelled the shape would be correct under `tree` and
silently wrong under `sibling`.
"""

from __future__ import annotations

from dataclasses import dataclass

from studyforge.address import Address
from studyforge.describe import describe
from studyforge.render.page import PageError


@dataclass(frozen=True, slots=True)
class Item:
    """One unit as its container's page lists it: what it is called, and whether it reads.

    `numbering` is the unit's numbering **as a reader sees it** —
    `container.Unit.numbering`, `4.4.1` or `7` — and may be empty for material
    that numbers nothing. `href` is how *this container's page* addresses that
    unit's page, or `None` when no page has been generated for it.
    """

    numbering: str
    title: str
    href: str | None = None

    def __post_init__(self) -> None:
        """Refuse an item that would render as a blank line in the list.

        ⛔ **Refused in the type rather than in the renderer**, so a second
        producer — a test, a migration, a caller assembling a partial list —
        cannot make the shape the renderer refuses to make. `contents.Group`
        makes the same argument for the same reason.
        """
        if not isinstance(self.title, str) or not self.title.strip():
            # ⛔ The value is DESCRIBED, never echoed (R7): a title is a
            # corpus's own text, this runs over every unit in a corpus, and
            # the branch that fires is the one where the value is not a title.
            raise PageError(
                f"a unit listed on a container page is titled, and this one is "
                f"{describe(self.title)}; a row with no name is a row a reader "
                f"cannot tell from the next one"
            )
        if not isinstance(self.numbering, str):
            raise PageError(
                f"a unit's numbering is text as a reader sees it, and this one is "
                f"{describe(self.numbering)}; nothing may read it back as an ordinal"
            )
        if self.href is not None and (not isinstance(self.href, str) or not self.href.strip()):
            raise PageError(
                f"a unit's link is a relative reference or is absent, and this one is "
                f"{describe(self.href)}; absent is spelled None, because an empty "
                f"string is an anchor that goes nowhere"
            )

    @property
    def readable(self) -> bool:
        """Whether this unit has a page for this container's page to point at."""
        return self.href is not None


@dataclass(frozen=True, slots=True)
class Document:
    """One container, as its own page renders it.

    ⛔ **A pure value.** The same document renders to the same bytes on every
    machine, every run (R10) — so `items` is a tuple in the order the container
    declared them, and nothing here is a set, a dict view or a directory walk.
    """

    address: Address
    title: str
    variant: str
    items: tuple[Item, ...]
    level: str = ""
    note: str | None = None

    def __post_init__(self) -> None:
        """Refuse a container page that would render as a heading and nothing else."""
        if not isinstance(self.title, str) or not self.title.strip():
            raise PageError(
                f"a container page is titled, and this record's title is {describe(self.title)}"
            )
        if not isinstance(self.items, tuple) or not self.items:
            raise PageError(
                f"a container page lists at least one unit, and this record lists "
                f"{describe(self.items)}; a container page with no units is "
                f"indistinguishable from one whose units failed to load"
            )
        for item in self.items:
            if not isinstance(item, Item):
                raise PageError(
                    f"a container page lists Items, and one of these is {describe(item)}"
                )

    @property
    def readable(self) -> int:
        """How many of the listed units have a page on this machine."""
        return sum(1 for item in self.items if item.readable)
