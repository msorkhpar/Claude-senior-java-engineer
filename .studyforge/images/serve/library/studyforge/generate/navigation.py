r"""The contents document joined to a page's bar, its trail and its rail.

**What it does.** Turns the corpus's tree into the chrome records
`render.page.render` takes — `Links` for the between-units bar, a tuple of
`Crumb` for the trail, and a tuple of `RailContainer` for the rail that reaches
the other containers — for one page at a time.

**How you use it.**

    bar(contents, key, absent=corpus.absent)
    trail(contents, key, index_href, above)
    rail(contents, from_page, above, container=…, unit=…, absent=corpus.absent)

`above` maps a container's address key to where that container's page went, so a
crumb links where a page exists and lists where one does not, and the rail
addresses every container from whichever page is asking.

**Depends on.** `contents` for the order and the labels, `render.page` for the
records, and `corpus.placement.relative_href` for *how does the page at A
address B*. ⛔ Nothing here touches a filesystem and nothing here **composes** a
path: every path shape is answered by `corpus.placement`, and the arithmetic is
asked of it rather than counted here.

## ⛔ Why the rail is built here and not beside the renderer

⭐ **It is the same join, one region wider.** The renderer takes plain values and
knows no order; the contents document knows the order and no markup. ⚠️ A rail
assembled inside `render.page` would be that package reaching for `contents`,
which the direction of this project forbids — and a rail assembled at each call
site would be the dropped-value defect again, computed on one side and dropped on the
other.

## ⛔ Why this join is a module rather than three lines in the writer

⚠️ **It did not exist in `src/` until this one.** `contents` computes a slot and
`render.page` renders one, and nothing joined them — and the lesson is exact: a
value computed on one side and dropped on the other satisfies both sides' tests.
⭐ The join lived in `tests/studyforge/render/page/sites.py`, whose own docstring
says it stands in for the caller the build would write.

## ⛔ A neighbour with no page is a DECLARED absence, never a guessed href

⚠️ The contents document computes an href for **every** declared unit, because
it is a function of the address and knows nothing about what this machine has.
⭐ Handing that href to the bar for a unit nobody generated is the one failure
neither href policy (chrome drops, content raises) catches: the anchor is present, the shape
is legal, and the file is not there. ⛔ So an absent neighbour is
`Link(href=None, key=…)`, and the bar degrades to that unit's index row.

## ⚠️ A container crumb links only where a container page exists

⭐ Every label comes out of the contents document — `Contents.title`,
`Group.level` (the corpus's own word for the depth, R1) and `Group.title` — and
not one of them is spelled here. ⛔ **An intermediate level has no container
page**, because a container map is written at the address a corpus declares
units under and nowhere above it; that crumb is therefore listed rather than
linked, which is the unlinked intermediate crumb made visible on the page instead of invisible.

## ⛔ The trail names each level once

⚠️ **A corpus may call its outermost group what it calls itself** — a measured
corpus's first group carries the corpus's own title — and the trail then read
the same long title twice in a row. ⭐ A group crumb whose title is the crumb
before it is not printed a second time: the first one stays, because it is the
root every trail starts from, and the rail still reaches that group's page.
"""

from __future__ import annotations

from collections.abc import Mapping
from pathlib import PurePosixPath

from studyforge.contents import Contents, Entry, Group, links, order
from studyforge.corpus.placement import relative_href
from studyforge.describe import describe
from studyforge.generate.declarations import BuildError
from studyforge.render.page import Crumb, Link, Links, RailContainer, RailGroup, RailUnit

#: The two bar slots whose target is a neighbouring unit. ⛔ `index` is not one
#: of them: it addresses the root index, which every corpus has.
NEIGHBOUR_SLOTS = ("previous", "next")


def bar(contents: Contents, key: str, absent: frozenset[str] = frozenset()) -> Links:
    """Return the between-units bar for one unit, with declared absences declared."""
    computed = links(contents, key)
    walked = [entry.key for entry in order(contents)]
    if key not in walked:
        # ⛔ `walked.index(key)` would raise a ValueError carrying the value (R7).
        raise BuildError(f"no unit of this corpus is keyed {describe(key)}")
    at = walked.index(key)
    slots: dict[str, Link] = {}
    for field in NEIGHBOUR_SLOTS:
        if field not in computed:
            continue
        target = walked[at - 1] if field == "previous" else walked[at + 1]
        href = None if target in absent else computed[field]["href"]
        slots[field] = Link(href, computed[field]["label"], target)
    slots["index"] = Link(computed["index"]["href"], computed["index"]["label"])
    return Links(**slots)


def index_href(contents: Contents, key: str) -> str:
    """How the page for `key` addresses the root index."""
    return links(contents, key)["index"]["href"]


def trail(
    contents: Contents,
    key: str,
    to_index: str,
    above: Mapping[str, str] | None = None,
) -> tuple[Crumb, ...]:
    """Return the crumbs for one unit: the corpus, its containers, then itself.

    ⛔ A crumb that repeats the title of the crumb before it is dropped — see
    this module's docstring on naming each level once.
    """
    linked = above or {}
    crumbs = [Crumb("", contents.title, to_index)]
    for group in ancestors(contents, key):
        if group.title != crumbs[-1].title:
            crumbs.append(Crumb(group.level, group.title, linked.get(group.key)))
    return (*crumbs, Crumb("", _entry(contents, key).title))


def ancestors(contents: Contents, key: str) -> tuple[Group, ...]:
    """Return the groups enclosing the unit `key` names, outermost first."""
    for group in contents.groups:
        found = _descend(group, key)
        if found is not None:
            return found
    return ()


def _descend(group: Group, key: str) -> tuple[Group, ...] | None:
    """`group` and what is under it, when the unit is in there."""
    if any(entry.key == key for entry in group.entries):
        return (group,)
    for child in group.groups:
        deeper = _descend(child, key)
        if deeper is not None:
            return (group, *deeper)
    return None


def _entry(contents: Contents, key: str) -> Entry:
    """Return the declared unit `key` names. ⛔ Raises rather than inventing a title.

    ⛔ The value is **described, never echoed** (R7). The branch that fires is
    the one where the argument is not a unit key, which is exactly the branch an
    absolute path arrives at, and a crumb is rendered per unit of every corpus.
    """
    for entry in order(contents):
        if entry.key == key:
            return entry
    raise BuildError(f"no unit of this corpus is keyed {describe(key)}")


def rail(
    contents: Contents,
    from_page: PurePosixPath,
    above: Mapping[str, PurePosixPath],
    *,
    container: str = "",
    unit: str = "",
    absent: frozenset[str] = frozenset(),
) -> tuple[RailContainer, ...]:
    """Return every container of this corpus as the page at `from_page` reaches it.

    `container` is the address key of the container the asking page is inside
    and `unit` the unit key of the page itself, either of which may be empty —
    a container page names the first and not the second.

    ⛔ **The containers are the deepest groups and nothing else**, because a
    container map is written at the address a corpus declares units under and
    nowhere above it: `generate.containers` writes one page per map and none
    higher, so an intermediate level has no page for a rail to point at.
    ⭐ Read off the contents document rather than walked off the
    disk — there is one model of what this corpus holds and this is not a
    second one.

    ⚠️ **A unit this build has no material for is a DECLARED absence**, exactly
    as the bar's neighbours are: it is listed with no href rather than pointing
    at a page nobody wrote.
    """
    return tuple(
        _rail_container(group, within, from_page, above, container, unit, absent)
        for group, within in _deepest_within(contents)
    )


def deepest(contents: Contents) -> tuple[Group, ...]:
    """Return the groups that hold units, in reading order, outermost walk first.

    ⛔ Depth-first in the order `contents.order` walks it, so the rail lists the
    containers in the order a reader reads them. ⚠️ A group holds subgroups
    **or** units and never both (`contents.entries` refuses the mixture), so
    *"holds no subgroups"* is the whole of the test and no count is needed.
    """
    found: list[Group] = []
    for group in contents.groups:
        _deepest(group, found)
    return tuple(found)


def _deepest(group: Group, found: list[Group]) -> None:
    """Append `group` when it is a container, or descend into the groups it holds."""
    if not group.groups:
        found.append(group)
        return
    for child in group.groups:
        _deepest(child, found)


def _deepest_within(contents: Contents) -> tuple[tuple[Group, tuple[RailGroup, ...]], ...]:
    """Each container in `deepest`'s order, with the groups above it, outermost first.

    ⭐ The rail groups its containers under these, so a corpus filed in sections
    lists its modules in their sections, as its index does.
    """
    found: list[tuple[Group, tuple[RailGroup, ...]]] = []

    def walk(group: Group, above: tuple[RailGroup, ...]) -> None:
        if not group.groups:
            found.append((group, above))
            return
        here = (*above, RailGroup(title=group.title, level=group.level, key=group.key))
        for child in group.groups:
            walk(child, here)

    for group in contents.groups:
        walk(group, ())
    return tuple(found)


def _rail_container(
    group: Group,
    within: tuple[RailGroup, ...],
    from_page: PurePosixPath,
    above: Mapping[str, PurePosixPath],
    container: str,
    unit: str,
    absent: frozenset[str],
) -> RailContainer:
    """One container of the rail, addressed from the page that is asking."""
    page = above.get(group.key)
    return RailContainer(
        title=group.title,
        level=group.level,
        href=None if page is None else relative_href(from_page, page),
        current=bool(container) and group.key == container,
        units=tuple(
            RailUnit(
                title=entry.title,
                numbering=entry.numbering,
                href=None if entry.key in absent else relative_href(from_page, entry.page),
                current=bool(unit) and entry.key == unit,
                key=entry.key,
            )
            for entry in group.entries
        ),
        within=within,
    )
