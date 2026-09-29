"""Container maps in, one hierarchy out — the only place a reading order is decided.

**What it does.** Assembles a corpus's `Contents` from its manifest and its
container maps: one group per address segment at every declared level, one
entry per **declared** unit, and a sibling order that is a function of the
addresses rather than of anything about this machine.

**How you use it.** `build(manifest, containers)`. ⛔ It reads no files: a
caller hands over the container maps it has already read, which is what keeps
this package out of the still-open question of where a corpus's archive root
is (spec §6).

**Depends on.** `studyforge.address`, `studyforge.corpus.manifest`,
`studyforge.corpus.container` and `studyforge.corpus.placement` for the page
each unit's artifacts occupy, and `studyforge.unit.outline`: every title is
listed without the source's own outline number, which the contents replace.
⚠️ A page's place is still named from the title as recorded, so no address or
filename moves. ⛔ Not on `corpus.discovery`: what a *scan* found
is the local document's business, and mixing the two here is precisely how a
table of contents ends up short by the units nobody has generated yet.

## ⛔ The contents count what the corpus DECLARES, never what a scan FOUND

⚠️ **This is the short-parse defect in this package's own terms.** A contents
document sourced from discovery would list the pages that exist; a unit
declared in a container map and not yet generated would simply not appear, the
document would be internally consistent, and **nothing would raise** — the
reader sees a list short by one and has no way to know. ⭐ So the stable half
is built from the declaration, and `status.py` says which of those declared
units this machine actually has.

## ⛔ Sibling order is a function of the ADDRESS, not of the caller

⭐ **R10, and the trap is that three orders are available and two of them are
wrong.** The order a caller happened to enumerate the maps in is not a
committed input — a `rglob` gives one, a test literal gives another. The order
they sit in on disk is not one either (R4: nothing is inferred from where a
file sits). ⛔ **What is committed is the address**, so siblings are ordered by
their segment.

- Compared as **text, by code point** — plain `sorted`, never a locale
  collation and never case-folded. A build whose order depended on `LANG`
  would be reproducible only on the machine that made it.
- ⚠️ **Which means a corpus gets alphabetical order unless it puts the order
  in its segments**, and real material does exactly that: `01-getting-started`
  before `02-going-further`. ⛔ A framework that inferred an order from
  anything else would be a framework that knew something about a source (R1).

**Units are the exception, and they are not a second orderer**: the container
reader has already refused any map whose ordinals are not contiguous from 1,
so the declared order *is* the ordinal order and this module uses it as given.

## ⛔ One title per address prefix, and a disagreement is refused

⚠️ Two containers under `basics` each declare what `basics` is called. They
agree in every corpus anybody meant to write; when they do not, one of them
loses silently and a reader sees a section named after whichever map was read
first. ⭐ So the disagreement is raised, naming the prefix and both titles.
"""

from __future__ import annotations

from collections.abc import Iterable, Iterator

from studyforge.address import Address, AddressError
from studyforge.contents.entries import Contents, Entry, Group
from studyforge.contents.errors import ContentsError
from studyforge.corpus.container import Container
from studyforge.corpus.manifest import Manifest
from studyforge.corpus.placement import PlacementError, Profile, profile_for
from studyforge.unit import listed_numbering, without_outline_number


def build(manifest: Manifest, containers: Iterable[Container]) -> Contents:
    """Return the corpus's stable contents — the same bytes on any machine (R10)."""
    profile = profile_for(manifest.placement)
    held = _by_address(manifest, containers)
    titles = _titles(held)
    return Contents(
        corpus=manifest.source,
        title=manifest.title,
        levels=manifest.levels,
        groups=_groups(manifest, profile, held, titles, ()),
    )


def _by_address(
    manifest: Manifest, containers: Iterable[Container]
) -> dict[tuple[str, ...], Container]:
    """Every container by its address segments, refusing a depth or a duplicate.

    ⛔ The depth is re-checked here even though `container.parse` checks it
    against the same manifest: this function also accepts containers a caller
    built in memory, and a guarantee that holds for one producer is not a
    property of the value.
    """
    held: dict[tuple[str, ...], Container] = {}
    for container in containers:
        segments = container.address.segments
        if len(segments) != manifest.depth:
            raise ContentsError(
                f"container {container.address.key} has {len(segments)} address "
                f"segment(s) and this corpus declares {manifest.depth} level(s), "
                f"{list(manifest.levels)}; a contents tree of two depths has no order"
            )
        if len(container.titles) != manifest.depth:
            raise ContentsError(
                f"container {container.address.key} declares {len(container.titles)} "
                f"title(s) for {manifest.depth} level(s); every level a corpus declares "
                f"is a level the contents have to name"
            )
        if segments in held:
            raise ContentsError(
                f"two container maps declare the address {container.address.key}; "
                f"one of them would silently replace the other, and the contents "
                f"would be short by everything the loser declared"
            )
        held[segments] = container
    return held


def _titles(held: dict[tuple[str, ...], Container]) -> dict[tuple[str, ...], str]:
    """Return what every address prefix is called, refusing two answers for one."""
    titles: dict[tuple[str, ...], str] = {}
    for segments, container in held.items():
        for depth, title in enumerate(container.titles, start=1):
            prefix = segments[:depth]
            already = titles.get(prefix)
            if already is not None and already != title:
                raise ContentsError(
                    f"the address prefix {_key(prefix)} is called {already!r} by one "
                    f"container map and {title!r} by another; one of them would lose "
                    f"silently and the reader would see whichever map was read first"
                )
            titles[prefix] = title
    return titles


def _groups(
    manifest: Manifest,
    profile: Profile,
    held: dict[tuple[str, ...], Container],
    titles: dict[tuple[str, ...], str],
    prefix: tuple[str, ...],
) -> tuple[Group, ...]:
    """Return the groups directly under `prefix`, ordered by segment."""
    return tuple(
        _group(manifest, profile, held, titles, prefix, segment) for segment in _under(held, prefix)
    )


def _under(held: dict[tuple[str, ...], Container], prefix: tuple[str, ...]) -> Iterator[str]:
    """Return the distinct segments one level under `prefix`, in code-point order.

    ⛔ Built through a `dict` and then sorted rather than through a `set`: R10
    forbids an answer that depends on iteration order, and this one decides
    what a reader reads first.
    """
    depth = len(prefix)
    seen = {segments[depth]: None for segments in held if segments[:depth] == prefix}
    return iter(sorted(seen))


def _group(
    manifest: Manifest,
    profile: Profile,
    held: dict[tuple[str, ...], Container],
    titles: dict[tuple[str, ...], str],
    prefix: tuple[str, ...],
    segment: str,
) -> Group:
    """One group: its subgroups if it has any level below it, else its units."""
    here = (*prefix, segment)
    level = manifest.levels[len(prefix)]
    shared = {
        "level": level,
        "segment": segment,
        "key": _key(here),
        "title": without_outline_number(titles[here]),
    }
    if len(here) < manifest.depth:
        return Group(**shared, groups=_groups(manifest, profile, held, titles, here))
    return Group(**shared, entries=_entries(profile, held[here]))


def _entries(profile: Profile, container: Container) -> tuple[Entry, ...]:
    """One entry per declared unit, in the container's own order.

    ⛔ **Not re-sorted.** The container reader refuses any map whose ordinals
    are not contiguous from 1, so the declared order already *is* the ordinal
    order; sorting here would be a second orderer with nothing to add and one
    more chance to disagree.
    """
    return tuple(_entry(profile, container, unit.n) for unit in container.units)


def _entry(profile: Profile, container: Container, ordinal: int) -> Entry:
    """One unit's entry, with the page its artifacts occupy under this profile."""
    unit = container.unit(ordinal)
    try:
        where = profile.unit(
            container.address, unit.n, unit.title, origin=unit.origin, label=unit.label
        )
    except (PlacementError, AddressError) as error:
        # ⛔ Re-typed, not re-worded: `placement` owns what a page may be
        # called and re-spelling its sentence here would be two descriptions
        # of one rule. ⚠️ `PersonalDataLeak` is deliberately not in this pair
        # and travels through as itself (R7).
        raise ContentsError(
            f"{container.address.key} unit {unit.n} has no place in this corpus: {error}"
        ) from None
    return Entry(
        address=container.address,
        ordinal=unit.n,
        title=without_outline_number(unit.title),
        numbering=listed_numbering(unit.numbering, unit.n),
        page=where.page,
        practices=unit.practices,
    )


def _key(segments: tuple[str, ...]) -> str:
    """Return the joined key for an address prefix. ⛔ Asked for, never composed.

    `Address.key` is the one composer of a joined address, for the reason that
    module gives: these strings are matched by equality across surfaces that
    never see each other, and a second spelling that differed by one character
    would simply never match, with nothing failing anywhere.
    """
    return Address(segments).key
