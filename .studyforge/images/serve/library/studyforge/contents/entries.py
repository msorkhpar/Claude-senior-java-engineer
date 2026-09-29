"""What a table of contents is made of: groups, entries, and the whole.

**What it does.** Holds the three value types the stable contents document is
written from — a `Contents`, the `Group` at each container level, and the
`Entry` for each unit — and says how each one is written down.

**How you use it.** `tree.build` returns a `Contents`; `document.render` turns
one into bytes. ⛔ Nothing here reads or writes a file, and nothing here
decides an order: this is the *value*, exactly as `discovery.site` is the
value of a scan.

**Depends on.** `studyforge.address` for the identity a key is minted from,
and this package's `errors`. ⛔ Not on `render` — the direction is
renderer-depends-on-contents, never the reverse — and not on the filesystem.

## ⛔ A group holds subgroups **or** units, never both and never a mixture

⭐ **Enumerate the legal.** A corpus of depth N has exactly N levels of group;
the deepest holds units and every shallower one holds groups. A type that
allowed both would admit a half-built tree — a container's units hanging one
level too high — and nothing would raise: the index would render, short, with
the units silently somewhere a reader does not look.

⚠️ **An empty deepest group is legal and is not the same thing.** A container
that declares no units yet is a container with nothing under it, and the
contents say so rather than dropping it.

## ⛔ Every key is asked for, never composed

`Address.unit_key` is the one composer of `<address>/unit-NN`, for the reason
that module states: these keys are joined by **string equality** across
surfaces that never see each other. ⭐ So an entry stores its address and its
ordinal and derives the key, and reading a document back parses the key with
that module's own inverse rather than splitting it here.

## What is written down, and what is not

⚠️ **`page` is relative to the source root, never absolute** (R7, R8). It is
recorded rather than recomputed because a consumer that re-derived it would
need the corpus's placement profile to render a link, and the whole point of
the two documents is that a consumer needs nothing but them.

⛔ **No href is stored.** An href is relative to the page that carries it, so
there is no single correct answer to store — `order.links` computes one per
asking page from the two `page` values.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import PurePosixPath

from studyforge.address import Address
from studyforge.contents.errors import ContentsError

#: The keys of one unit's entry, in the order they are written. ⛔ Fixed rather
#: than sorted: an unchanged corpus must re-render to identical bytes (R10).
ENTRY_KEYS = ("key", "unit", "title", "numbering", "page", "practices")

#: The keys every group carries, before the one that says what is under it.
GROUP_KEYS = ("level", "segment", "key", "title")

#: What a group's children are called, by whether they are groups or units.
#: ⛔ Exactly one of these appears in any group, which is what makes a
#: half-built tree unrepresentable rather than merely unlikely.
GROUP_CHILD_KEY = "groups"
UNIT_CHILD_KEY = "units"


@dataclass(frozen=True, slots=True)
class Entry:
    """One unit in the contents: what it is called, and where its page goes."""

    address: Address
    ordinal: int
    title: str
    numbering: str
    page: PurePosixPath
    practices: int

    @property
    def key(self) -> str:
        """`<address>/unit-NN` — the string every other surface joins on."""
        return self.address.unit_key(self.ordinal)

    @property
    def document(self) -> dict:
        """The entry as the contents record it, in `ENTRY_KEYS` order."""
        written = {
            "key": self.key,
            "unit": self.ordinal,
            "title": self.title,
            "numbering": self.numbering,
            "page": self.page.as_posix(),
            "practices": self.practices,
        }
        return {key: written[key] for key in ENTRY_KEYS}


@dataclass(frozen=True, slots=True)
class Group:
    """One container level: a segment of an address, its title, and what is under it.

    ⛔ `level` is the **display label** the corpus declared for this depth —
    `manifest.levels[depth]` — and not a number. A number would be this
    framework's own vocabulary arriving in a corpus's contents (R1); the word
    is the corpus's own.
    """

    level: str
    segment: str
    key: str
    title: str
    groups: tuple[Group, ...] = ()
    entries: tuple[Entry, ...] = ()

    def __post_init__(self) -> None:
        """Refuse a group that holds both kinds of child, or a key that is not one.

        ⛔ **Checked in the type rather than in the builder**, so a second
        producer — a test, a migration, a consumer assembling a partial tree —
        cannot make the shape the builder refuses to make.
        """
        if self.groups and self.entries:
            raise ContentsError(
                f"group {self.key} holds both subgroups and units; a group holds one "
                f"or the other, because a mixture is a container's units hanging one "
                f"level too high and an index that renders short with nothing raised"
            )

    @property
    def document(self) -> dict:
        """The group as the contents record it, children last.

        ⭐ The child key names which kind is there, so a reader never has to
        test two keys and guess what an empty one meant.
        """
        written: dict = {
            "level": self.level,
            "segment": self.segment,
            "key": self.key,
            "title": self.title,
        }
        written = {key: written[key] for key in GROUP_KEYS}
        if self.groups:
            written[GROUP_CHILD_KEY] = [group.document for group in self.groups]
        else:
            written[UNIT_CHILD_KEY] = [entry.document for entry in self.entries]
        return written


@dataclass(frozen=True, slots=True)
class Contents:
    """A corpus's hierarchy — and nothing at all about this machine.

    ⛔ **A pure function of committed inputs.** Check the repository out
    somewhere else and these bytes are the same bytes. Anything that would not
    survive that — which pages exist, what the reader has ticked — is the
    *local* document's, and `status.py` holds it.
    """

    corpus: str
    title: str
    levels: tuple[str, ...]
    groups: tuple[Group, ...] = ()

    @property
    def depth(self) -> int:
        """How many container levels this corpus has — `len(levels)`, always."""
        return len(self.levels)

    @property
    def document(self) -> list:
        """The hierarchy itself, in the order the groups are held in."""
        return [group.document for group in self.groups]
