r"""What the root index is rendered from: the tree, its sections, and its units.

**What it does.** Holds the three records the index renderer takes — one unit as
the index lists it, one container level of the hierarchy, and the whole document
— and refuses the shapes that would render as a page saying nothing.

**How you use it.**

    from studyforge.render.index import Document, Item, Section

    Item(key="basics/01-intro/unit-01", numbering="1.1", title="One",
         href="basics/01-intro/one.unit.html")
    Item(key="basics/01-intro/unit-02", numbering="1.2", title="Two")  # no page yet
    Section(level="module", key="basics/01-intro", title="Intro", items=(...,))
    Document(title="A Corpus", levels=("section", "module"), sections=(...,))

**Depends on.** `describe` for R7-safe refusals and `render.page` for
`PageError`. ⛔ Nothing that reads a file and nothing that knows a placement
profile: `assemble` turns the two contents documents into these, and this module
is what they arrive as.

## ⛔ A section holds subsections **or** units, never both

⭐ **`contents.Group` makes exactly this refusal, and it is repeated here rather
than trusted.** A record this renderer can be handed by a test, a migration or a
future caller that never went through `assemble` must be unable to express a
half-built tree — a container's units hanging one level too high, an index that
renders short, and nothing raised.

⚠️ **A deepest section with no units at all is legal and is not that.** A
container that declares no units yet is listed with an empty disclosure, because
a tree that dropped it would be short by a container with no way to find out.

## ⛔ `href=None` is *declared absence*; a refused href is a *fault*

⚠️ **The two look alike and must not be handled alike** — §7's three states.
A unit whose page has not been generated on this machine is a real, expected
state, and it is listed **without** a link so the reader sees the material's
full shape and which of it is readable today. ⛔ An href that is present and
unusable is something else, and `disclosure` **raises** on it.

## ⛔ Every key is the one the rest of the framework joins on

⭐ `Entry.key` — `<address>/unit-NN`, minted by `Address.unit_key` — and
`Group.key` are carried through unchanged, because they are what the index's
own anchors are made of. ⚠️ A second identifier minted here would be a second
key that has to agree with the first, and `anchor` exists so no consumer
composes one.
"""

from __future__ import annotations

from dataclasses import dataclass

from studyforge.describe import describe
from studyforge.render.page import PageError


@dataclass(frozen=True, slots=True)
class Item:
    """One unit as the root index lists it: what it is called, and whether it reads.

    `key` is the unit key every surface joins on, and it is this row's anchor.
    `numbering` is the unit's numbering **as a reader sees it** and may be empty
    for material that numbers nothing. `href` is how *the root index* addresses
    that unit's page, or `None` when this machine has no page for it.
    """

    key: str
    numbering: str
    title: str
    href: str | None = None

    def __post_init__(self) -> None:
        """Refuse an item that would render as an unnamed or unreachable row."""
        _require_text(self.key, "a unit listed on the root index is keyed, and this key is")
        if not isinstance(self.title, str) or not self.title.strip():
            # ⛔ The value is DESCRIBED, never echoed (R7): a title is a
            # corpus's own text, this runs over every unit in a corpus, and the
            # branch that fires is the one where the value is not a title.
            raise PageError(
                f"a unit listed on the root index is titled, and this one is "
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
        """Whether this machine has a page for this unit for the index to point at."""
        return self.href is not None


@dataclass(frozen=True, slots=True)
class Section:
    """One container level of the tree: what it is called, and what is under it.

    ⛔ `level` is the **corpus's own word** for this depth — `manifest.levels[d]`
    — and never a number. A number would be this framework's vocabulary arriving
    on a corpus's own index (R1).
    """

    level: str
    key: str
    title: str
    sections: tuple[Section, ...] = ()
    items: tuple[Item, ...] = ()

    def __post_init__(self) -> None:
        """Refuse a section that holds both kinds of child, or that has no name."""
        _require_text(self.key, "a section of the root index is keyed, and this key is")
        if not isinstance(self.title, str) or not self.title.strip():
            raise PageError(
                f"a section of the root index is titled, and this one is "
                f"{describe(self.title)}; a disclosure with no summary is one a "
                f"reader cannot decide whether to open"
            )
        if not isinstance(self.level, str):
            raise PageError(
                f"a section's level is the corpus's own word for this depth, and this "
                f"one is {describe(self.level)}"
            )
        _require_members(self.sections, Section, "sections")
        _require_members(self.items, Item, "units")
        if self.sections and self.items:
            raise PageError(
                f"the section keyed at depth {len(self.key.split('/'))} holds both "
                f"subsections and units; a section holds one or the other, because a "
                f"mixture is a container's units hanging one level too high and an "
                f"index that renders short with nothing raised"
            )

    @property
    def rows(self) -> int:
        """How many rows this section reveals when it is opened."""
        return len(self.sections) + len(self.items)


@dataclass(frozen=True, slots=True)
class Document:
    """A whole corpus as its root index shows it.

    ⛔ **A pure value.** The same document renders to the same bytes on every
    machine, every run (R10) — so every child is a tuple in the order the
    contents held it, and nothing here is a set, a dict view or a directory
    walk.
    """

    title: str
    levels: tuple[str, ...]
    sections: tuple[Section, ...] = ()

    def __post_init__(self) -> None:
        """Refuse an index that would render as a heading and nothing else."""
        if not isinstance(self.title, str) or not self.title.strip():
            raise PageError(
                f"a root index is titled with the corpus's own title, and this "
                f"record's title is {describe(self.title)}"
            )
        if not isinstance(self.levels, tuple) or not self.levels:
            raise PageError(
                f"a corpus declares at least one container level and this record "
                f"declares {describe(self.levels)}; the depth is what says how deep "
                f"the disclosures go"
            )
        _require_members(self.sections, Section, "sections")
        if not self.sections:
            raise PageError(
                "a root index lists at least one section; an index with none is "
                "indistinguishable from one whose contents failed to load"
            )

    @property
    def depth(self) -> int:
        """How many container levels this corpus has — `len(levels)`, always."""
        return len(self.levels)


def _require_text(value: object, what: str) -> None:
    """Refuse a key that is not a non-empty string, describing what arrived."""
    if not isinstance(value, str) or not value.strip():
        raise PageError(
            f"{what} {describe(value)}; a key is minted by the contents and matched "
            f"by string equality, so a near miss never matches anything"
        )


def _require_members(children: object, kind: type, what: str) -> None:
    """Refuse a child collection that is not a tuple of the one type it may hold."""
    if not isinstance(children, tuple):
        raise PageError(
            f"a root index holds its {what} in a tuple, in declared order, and this "
            f"record holds {describe(children)}; an order that came from a set or a "
            f"directory walk is one that differs between machines"
        )
    for child in children:
        if not isinstance(child, kind):
            raise PageError(
                f"a root index's {what} are {kind.__name__} records, and one of "
                f"these is {describe(child)}"
            )
