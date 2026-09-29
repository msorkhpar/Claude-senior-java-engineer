r"""What a corpus declares, read once and handed to every pass of the build.

**What it does.** Reads `corpus.json` and every container map beneath the
archive, works out which declared units have material on disk, and builds the
contents document the whole site is navigated by — all of it in memory.

**How you use it.** `read_corpus(root)` for the lot; `sources(root)` when only
the unit walk is wanted; `unit_location(corpus, source)` and
`declared_location(corpus, container, unit)` for where one unit's artifacts go
— ⛔ each takes the unit **whole**, so no caller spells its label.
`BuildError` is the only exception any of it raises,
**except** `PersonalDataLeak`, which travels through untranslated —
the package's `RAISES` is the pair.

**Depends on.** `corpus.manifest`, `corpus.container` and `corpus.placement`
for the declarations, `archive.layout` for the archive's layout, and `contents`
for the tree. ⛔ It names no source (R1) and it reads nothing outside the
archive.

## ⛔ The contents document is built and NOT written

⚠️ **Nothing in `corpus.placement` says where `toc.json` and `status.json` go**,
and `studyforge plan`'s committed goldens do not enumerate them — so a build
that wrote them would create two paths the plan never declared and fail
R3's path-for-path clause on both fixtures. ⭐ The tree is therefore a
**value** here, consumed by the index and the navigation join in the same
process. ⛔ Where the two documents finally sit is a placement decision, and
this module does not take it.

## ⚠️ A defect this package reports is raised, not drained

`studyforge validate` drains every check and reports all of them (R6). A
**build** is downstream of that: it stops on the first declaration it cannot
read, and the report an integrator wants already exists one command earlier.
"""

from __future__ import annotations

from dataclasses import dataclass, replace
from pathlib import Path, PurePosixPath

from studyforge.address import Address
from studyforge.archive.layout import Layout
from studyforge.archive.scrub import PersonalDataLeak
from studyforge.contents import Contents, ContentsError, build, order
from studyforge.corpus.container import CONTAINER_FILENAME, Container, Unit
from studyforge.corpus.container import RAISES as CONTAINER_RAISES
from studyforge.corpus.container import parse as parse_container
from studyforge.corpus.manifest import MANIFEST_FILENAME, Manifest
from studyforge.corpus.manifest import RAISES as MANIFEST_RAISES
from studyforge.corpus.manifest import parse as parse_manifest
from studyforge.corpus.placement import (
    CorpusLocations,
    PlacementError,
    Profile,
    UnitLocations,
    profile_for,
)
from studyforge.generate.footprint import Footprint, footprint_for
from studyforge.unit import ContentError, Heading, Mentions
from studyforge.unit.builder import unit_headings


class BuildError(ValueError):
    """A corpus this package will not build.

    ⛔ The message names the record and the address, **never a path and never
    the offending value** (R7): a corpus root is somebody's home directory with
    a few segments on the end, and a build's messages go into logs.
    """


@dataclass(frozen=True, slots=True)
class UnitSource:
    """One declared unit and the archive directory holding its material.

    ⭐ Every field is a **declaration**, read from the container map — the
    ordinal, the title, the origin and the label are what placement is asked
    with, and none of them is recovered from a filename (§6: recorded, never
    derived).
    """

    container: Container
    ordinal: int
    title: str
    origin: str | None
    label: str | None
    declared_practices: int | None
    directory: Path
    #: ⭐ The corpus's other units as this one names them (`unit.mentions`),
    #: for `build_unit(mentions=...)`. The default names nothing.
    mentions: Mentions = Mentions()

    @property
    def key(self) -> str:
        """`<address>/unit-NN` — the string the contents document joins on."""
        return self.container.address.unit_key(self.ordinal)


@dataclass(frozen=True, slots=True)
class Corpus:
    """One corpus's declarations, read once.

    ⛔ **Read once and passed down, rather than re-read per pass.** Three passes
    each re-parsing `corpus.json` and every container map would be three chances
    to disagree about what the corpus declares, and the disagreement would show
    up as a page linking a unit another page does not list.
    """

    root: Path
    manifest: Manifest
    profile: Profile
    maps: tuple[tuple[str, Container], ...]
    contents: Contents
    units: tuple[UnitSource, ...]
    #: Which paths under an output root are this build's own to replace.
    #:
    #: ⭐ **Read once with everything else**: a footprint is a declaration
    #: about the corpus, and five passes each deriving one would be five
    #: chances to disagree about which files a rebuild may touch. ⛔ **The
    #: default owns nothing**, so a `Corpus` assembled by hand gets R3's floor
    #: rather than a licence; `read_corpus` is what fills it.
    footprint: Footprint = Footprint()
    #: Whether this build voices the corpus: `corpus.json`'s
    #: `narration`, or a run's override of it (`generate.narration.voiced`).
    #: ⭐ **`True` by default**, so a `Corpus` assembled by hand builds as
    #: every corpus did before the key: a record's clips play.
    narration: bool = True

    @property
    def shared(self) -> CorpusLocations:
        """Where the corpus's own artifacts sit, under its declared profile."""
        return self.profile.corpus()

    @property
    def present(self) -> frozenset[str]:
        """The keys of every declared unit this build has material for."""
        return frozenset(source.key for source in self.units)

    @property
    def absent(self) -> frozenset[str]:
        """The keys of every declared unit this build has NO material for.

        ⭐ §7's third state, and it is the corpus's shape rather than a fault:
        the index lists those units unlinked and the bar declares the absence
        instead of guessing an href at a page nobody wrote.
        """
        return frozenset(entry.key for entry in order(self.contents)) - self.present


def read_corpus(root: Path | str) -> Corpus:
    """Read one corpus's declarations, or refuse naming the record (R7)."""
    root = Path(root)
    manifest = read_manifest(root)
    maps = containers(root, manifest)
    profile = profile_for(manifest.placement)
    return Corpus(
        root=root,
        manifest=manifest,
        profile=profile,
        maps=maps,
        contents=_contents(manifest, maps),
        units=_sources(root, manifest, maps),
        footprint=footprint_for(root, profile),
        narration=manifest.narration,
    )


def sources(root: Path | str) -> tuple[UnitSource, ...]:
    """Every declared unit that has material on disk, in declared order."""
    manifest = read_manifest(root)
    return _sources(Path(root), manifest, containers(root, manifest))


def _sources(
    root: Path, manifest: Manifest, maps: tuple[tuple[str, Container], ...]
) -> tuple[UnitSource, ...]:
    """Walk declarations already read, keeping every unit that has material.

    ⛔ **A unit declared with no material is skipped in silence, and that is
    deliberate.** `validate`'s `unit-missing` and `empty-unit` already report
    it against the container that declared it; a build that raised a second
    sentence about the same fact would make one defect read as two.
    """
    layout = Layout(root)
    found: list[UnitSource] = []
    profile = profile_for(manifest.placement)
    pages: list[PurePosixPath] = []
    for _, container in maps:
        for unit in container.units:
            directory = layout.unit_dir(container.address, container.variant, unit.n)
            if not any(directory.glob("*.json")):
                continue
            found.append(
                UnitSource(
                    container=container,
                    ordinal=unit.n,
                    title=unit.title,
                    origin=unit.origin,
                    label=unit.label,
                    declared_practices=declared_practices(manifest, unit.practices),
                    directory=directory,
                )
            )
            pages.append(_page(profile, container.address, unit))
    return _mentioning(root, tuple(found), tuple(pages))


def _page(profile: Profile, address: Address, unit: Unit) -> PurePosixPath:
    """One unit's page, or refuse naming it: the one placement question, asked early."""
    try:
        return profile.unit(address, unit.n, unit.title, origin=unit.origin, label=unit.label).page
    except PlacementError as error:
        raise BuildError(
            f"the unit {unit.n} declared at {address.key!r} cannot be placed: {error}"
        ) from None


def _mentioning(
    root: Path, found: tuple[UnitSource, ...], pages: tuple[PurePosixPath, ...]
) -> tuple[UnitSource, ...]:
    """Hand every unit the corpus's units, their headings and its container's, as it may name them.

    ⭐ One index for the corpus, so the page and every other consumer of a served
    unit resolve a reference the same way. ⚠️ A unit whose material will not
    read offers no heading here: building it raises, where it is reported.
    """
    read = tuple(_headings(source) for source in found)
    labels, origins = Mentions.of(
        tuple(
            (source.label, source.origin, source.title, page, own)
            for source, page, own in zip(found, pages, read, strict=True)
        )
    )
    held: dict[str, list[tuple[PurePosixPath, tuple[Heading, ...]]]] = {}
    for source, page, own in zip(found, pages, read, strict=True):
        held.setdefault(source.container.address.key, []).append((page, own))
    numbers = {key: Mentions.numbered(tuple(rows)) for key, rows in held.items()}
    return tuple(
        replace(
            source,
            mentions=Mentions(
                labels,
                origins,
                source.origin,
                page,
                numbers=numbers[source.container.address.key],
                root=root,
            ),
        )
        for source, page in zip(found, pages, strict=True)
    )


def _headings(source: UnitSource) -> tuple[Heading, ...]:
    """One unit's headings as its prose names them, or none when its material will not read."""
    try:
        return unit_headings(source.directory)
    except ContentError:
        return ()


def unit_location(corpus: Corpus, source: UnitSource) -> UnitLocations:
    """Where one unit WITH MATERIAL puts its artifacts, asked of the corpus's own profile.

    ⛔ **It takes the unit WHOLE**, so no caller spells arguments out of a
    `UnitSource` it already holds and none can drop one. ⭐ There is nothing to
    drop: the source carries its own
    address, ordinal, title, origin and label, and this is the one place they
    are read off it.
    """
    return _place(
        corpus,
        source.container.address,
        source.ordinal,
        source.title,
        source.origin,
        source.label,
    )


def declared_location(corpus: Corpus, container: Container, unit: Unit) -> UnitLocations:
    """Where one unit its container DECLARES puts its artifacts — material or not.

    ⛔ **The same derivation, asked of a DECLARATION rather than of material**.
    The container page links every unit it declares and the media
    pass asks about every declared directory; neither has a `UnitSource` for a
    unit this machine has no material for, because `_sources` skips those.
    ⭐ Both arguments are whole objects, so no call site spells a label here
    either — which is the property, not the signature.
    """
    return _place(corpus, container.address, unit.n, unit.title, unit.origin, unit.label)


def _place(
    corpus: Corpus,
    address: Address,
    ordinal: int,
    title: str,
    origin: str | None,
    label: str | None,
) -> UnitLocations:
    """Answer the question in ONE spelling, because the page and its container both ask.

    ⛔ Two calls composing the same arguments differently would put an anchor on
    a container page that points beside the file the build wrote, and nothing
    would raise. ⭐ **This is the only body in the framework that names these
    five**; both public spellings above read them off one object.
    """
    try:
        return corpus.profile.unit(address, ordinal, title, origin=origin, label=label)
    except PlacementError as error:
        raise BuildError(
            f"the unit {ordinal} declared at {address.key!r} cannot be placed: {error}"
        ) from None


def declared_practices(manifest: Manifest, practices: int) -> int | None:
    """How many practices a unit declares, given its corpus's own declaration.

    ⛔ **`exercises: false` is a declaration of ZERO, not an absence**, and the
    difference is a *"more to come"* panel on every page of a finished prose
    corpus — which contradicts §7's three states and §11.0's reading floor,
    where a graderless corpus is complete rather than short. ⭐ One direction
    only: `exercises: true` says nothing about any unit, so the count then comes
    from the container map that declared the unit.
    """
    return 0 if not manifest.exercises else practices


def read_manifest(root: Path | str) -> Manifest:
    """Read and parse one corpus manifest, or refuse naming the file (R7)."""
    text = _text(Path(root) / MANIFEST_FILENAME, MANIFEST_FILENAME)
    try:
        return parse_manifest(text, MANIFEST_FILENAME)
    except PersonalDataLeak:
        raise  # ⛔ R7's refusal is never translated into `BuildError`.
    except MANIFEST_RAISES as error:
        # ⭐ The reader's own tuple: a member it gains arrives here.
        raise BuildError(str(error)) from None


def containers(root: Path | str, manifest: Manifest) -> tuple[tuple[str, Container], ...]:
    """Every container map beneath the archive, in sorted path order (R10).

    ⚠️ **The archive's shape is `Layout`'s and not this module's.** A build
    that composed `archive/<address>/raw/<variant>` from string pieces would be
    a second authority on a layout the adapter already writes to one.
    """
    layout = Layout(root)
    held: list[tuple[str, Container]] = []
    for path in sorted(layout.archive.rglob(CONTAINER_FILENAME)):
        where = path.relative_to(Path(root)).as_posix()
        text = _text(path, where)
        try:
            held.append((where, parse_container(text, where, manifest)))
        except PersonalDataLeak:
            raise  # ⛔ R7's refusal is never translated into `BuildError`.
        except CONTAINER_RAISES as error:
            # ⭐ What `corpus.container` lets out is its `RAISES`, stated in its
            # package contract; every member but the leak above becomes
            # this package's refusal, so a wrong-depth map refuses.
            raise BuildError(f"{where}: {error}") from None
    return tuple(held)


def _contents(manifest: Manifest, maps: tuple[tuple[str, Container], ...]) -> Contents:
    """Build the corpus's tree, from the declarations and nothing on disk."""
    try:
        return build(manifest, [container for _, container in maps])
    except ContentsError as error:
        raise BuildError(str(error)) from None


def _text(path: Path, where: str) -> str:
    """One file's text, or a refusal that names the record and never the path.

    ⛔ `exc.strerror`, never `exc`: `OSError` formats itself with the filename
    it was given, so `{exc}` here would put a corpus root — somebody's home
    directory with a few segments on the end — into a build log (R7).

    ⚠️ **Two `except` clauses rather than one two-type clause, deliberately.**
    The house style rules the unparenthesised spelling out and `ruff format` rewrites
    the parenthesised one into it whenever there is no `as` binding, so the two
    cannot both be satisfied by a single clause. One type each
    satisfies both, and it lets each refusal say what actually went wrong.
    """
    try:
        return path.read_text(encoding="utf-8")
    except OSError as exc:
        raise BuildError(f"{where} cannot be read: {exc.strerror}") from None
    except UnicodeDecodeError:
        raise BuildError(f"{where} is not UTF-8 text") from None
