r"""Two artifacts must not want the same path, and only this module can see it.

**What it does.** Places **every** container and **every** declared unit of the
archive under the corpus's own placement profile, and refuses a path that two
of them claim — pages, media directories and container pages in one set. It
also asks the one question placement cannot: whether an `origin` on disk is the
file its contract says it is.

**How you use it.** `check_placement(walk)` and `check_origins_are_files(walk)`,
yielding `Finding`s and `Unchecked`s like every other check.

**Depends on.** `corpus.placement` for where things go, `validate.corpus`,
`validate.report`.

## Placement is a pure function of one unit, so it cannot see a collision

⚠️ **Every individual call is correct and the pair is wrong.** `profile.unit`
takes an address, an ordinal, a title, an origin and a label, and has no view
of the corpus. Under `sibling` the directory an artifact lands in comes from
the **origin**, not the address — so two units in *different* containers whose
origins share a directory, with the same ordinal and the same title slug,
compute the same page path, and neither call had any way to know.

⛔ **So the check is the whole path set, not the addresses.** Two distinct
titles can be one name, and that is invisible in the source and in the address.
⚠️ **`slugify` does not delete an accented character:** `slugify('café') ==
'caf'` and `slugify('cafe') == 'cafe'` — an accent is a non-alphanumeric, so it
collapses to a **separator**, and those two do not collide at all. ⭐ The real class is
wider than accents: any two titles whose non-alphanumerics collapse to the same
separator run are one name, so `'Streams: an API'` and `'Streams, an API'` both
give `streams-an-api`. ⛔ Which is the argument for checking the *set* rather
than any one cause — the set catches every cause, including one nobody has
named.

⚠️ The placement package's corpus-wide test places units only;
container pages are the half it does not cover, and two containers whose
origins share a directory and whose deepest titles slugify alike collide the
same way.

## ⛔ It names no profile

⚠️ **Comparing `manifest.placement` against the string `'sibling'` would fail
`test_nothing_downstream_branches_on_a_profile_name`.** A registry that coexists
with `if placement == "…"` has already failed, and the third profile somebody
registers would silently skip the check. So this module asks the profile where
things go and compares what comes back; a profile with no collisions produces a
set with no duplicates, and the check costs nothing on it.

## `origin` is a file, and this is the only place that is checkable

⛔ The unit contract says an `origin` names a **file**. `origin_directory` takes
its parent and does no I/O, so a container that recorded its *directory* places
its page one level up — at the repository root, for a top-level container — and
nothing raises. ⭐ This module has the filesystem in front of it.

⚠️ **Absent is not wrong.** An archive is shippable without its source beside
it (R2), so an origin that is simply not there is reported `Unchecked`, in the
output and counted (R6's sibling), never as a pass.
"""

from __future__ import annotations

from collections.abc import Iterator
from pathlib import PurePosixPath

from studyforge.corpus.container.document import Container, Unit
from studyforge.corpus.manifest import MANIFEST_FILENAME
from studyforge.corpus.placement import (
    UNIT_MEDIA_DIRNAMES,
    PlacementError,
    Profile,
    profile_for,
)
from studyforge.validate.corpus import Held, Walk
from studyforge.validate.report import Finding, Unchecked

#: The rules this module can report.
RULE_DUPLICATE_PATH = "duplicate-path"
RULE_UNPLACEABLE = "unplaceable"
RULE_ORIGIN_NOT_A_FILE = "origin-not-a-file"

#: A `(path, what claimed it)` pair on its way into the set.
Claim = tuple[str, str]


def check_placement(walk: Walk) -> Iterator[Finding | Unchecked]:
    """Place the whole archive and refuse any path two artifacts claim.

    ⛔ Every container **and** every unit, under the corpus's declared profile,
    into one set: a check narrowed to addresses cannot see a `sibling`
    collision, and a check narrowed to units cannot see a container page.
    """
    if walk.manifest is None:  # pragma: no cover - the walk stops without one
        return
    try:
        profile = profile_for(walk.manifest.placement)
    except PlacementError as error:
        # ⚠️ The manifest reader already refuses an unknown profile, so this is
        # the belt to that pair of braces rather than a live path.
        yield Finding(RULE_UNPLACEABLE, MANIFEST_FILENAME, str(error))
        return
    claimed: dict[str, str] = {}
    for held in walk.containers:
        for item in _claims(held, profile):
            if isinstance(item, Finding):
                yield item
                continue
            path, what = item
            first = claimed.get(path)
            if first is None:
                claimed[path] = f"{held.where}: {what}"
            else:
                yield _collision(held, what, path, first)


def _collision(held: Held, what: str, path: str, first: str) -> Finding:
    """Name both claimants and the one path, and say why nothing else saw it."""
    return Finding(
        RULE_DUPLICATE_PATH,
        held.where,
        f"{what} is placed at {path!r}, which {first} already claims. Placement is "
        f"a pure function of one artifact, so both calls are individually correct "
        f"and only the whole set shows the collision — one of these overwrites the "
        f"other at build time.",
    )


def _claims(held: Held, profile: Profile) -> Iterator[Claim | Finding]:
    """Every path one container and its declared units occupy."""
    yield from _container_claims(held, profile)
    for declared in held.container.units:
        yield from _unit_claims(held, declared, profile)


def _container_claims(held: Held, profile: Profile) -> Iterator[Claim | Finding]:
    container = held.container
    try:
        locations = profile.container(container.address, container.titles, origin=container.origin)
    except PlacementError as error:
        yield Finding(RULE_UNPLACEABLE, held.where, str(error))
        return
    yield (locations.page.as_posix(), "its container page")


def _unit_claims(held: Held, declared: Unit, profile: Profile) -> Iterator[Claim | Finding]:
    try:
        where = profile.unit(
            held.container.address,
            declared.n,
            declared.title,
            origin=declared.origin,
            label=declared.label,
        )
    except PlacementError as error:
        yield Finding(RULE_UNPLACEABLE, held.where, f"unit {declared.n}: {error}")
        return
    yield (where.page.as_posix(), f"unit {declared.n}'s page")
    for kind in UNIT_MEDIA_DIRNAMES:
        yield (where.media_dir(kind).as_posix(), f"unit {declared.n}'s {kind} directory")


def check_origins_are_files(walk: Walk) -> Iterator[Finding | Unchecked]:
    """Refuse an `origin` that is a directory on disk where a file was recorded.

    ⛔ Placement does no I/O, so it takes the parent of whatever it is given
    and cannot tell `basics/01/README.md` from `basics/01`. A container that
    recorded the second places its page in `basics/`, one level above where its
    author meant, and every href it holds is off by a directory.
    """
    if walk.manifest is None:  # pragma: no cover - the walk stops without one
        return
    declared = list(_origins(walk))
    if not declared:
        yield Unchecked(
            RULE_ORIGIN_NOT_A_FILE,
            ".",
            "nothing in this archive declares an 'origin', so there is no path to resolve",
        )
        return
    present = [(where, what, path) for where, what, path in declared if path.exists()]
    if not present:
        yield Unchecked(
            RULE_ORIGIN_NOT_A_FILE,
            ".",
            f"none of the {len(declared)} declared origin path(s) is on disk, so "
            f"whether each names a file rather than a directory could not be checked",
        )
        return
    for where, what, path in present:
        if path.is_dir():
            yield Finding(
                RULE_ORIGIN_NOT_A_FILE,
                where,
                f"the 'origin' recorded for {what} is a directory on disk, and an "
                f"origin names the source **file** an artifact was generated from. "
                f"Placement takes its parent, so this artifact is placed one level "
                f"above the material it belongs to.",
            )


def _origins(walk: Walk) -> Iterator[tuple[str, str, object]]:
    """`(where, what carries it, resolved path)` for every declared origin.

    ⛔ Resolved against the corpus root and never echoed: an origin is corpus
    data and the shape being refused is exactly the shape that carries a home
    directory (R7), so a message names the record and not the string.
    """
    for held in walk.containers:
        yield from _container_origins(walk, held)


def _container_origins(walk: Walk, held: Held) -> Iterator[tuple[str, str, object]]:
    container: Container = held.container
    if container.origin is not None:
        yield (held.where, "this container", walk.root / _relative(container.origin))
    for declared in container.units:
        if declared.origin is not None:
            yield (
                held.where,
                f"unit {declared.n}",
                walk.root / _relative(declared.origin),
            )
        # ⭐ A unit's practice may name a file of its own, and it is a
        # file for exactly the same reason its prose origin is.
        if declared.practice_origin is not None:
            yield (
                held.where,
                f"unit {declared.n} practice",
                walk.root / _relative(declared.practice_origin),
            )


def _relative(origin: str) -> str:
    """Return `origin` as a root-relative POSIX path, refusing to escape upward.

    ⚠️ An origin that is absolute or climbs out of the root is `origin_directory`'s
    refusal to make, not this function's — it is normalised to something harmless
    here so that a filesystem probe never follows it, and the placement check
    above reports it by name.
    """
    path = PurePosixPath(origin)
    parts = [part for part in path.parts if part not in ("", "/", "..")]
    return PurePosixPath(*parts).as_posix() if parts else "."


#: The corpus first, then each container: the order a report reads best.
CHECKS = (check_placement, check_origins_are_files)
