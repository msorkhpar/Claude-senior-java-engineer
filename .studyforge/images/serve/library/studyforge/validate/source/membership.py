r"""What the archive root holds and does not account for, and what it accounts for and has not.

**What it does.** Refuses, by name, every file beneath the archive root that is
not an archive member, and every file a document declares
that the archive does not hold where the declaration says.
`classification` never scans beneath the root, so the first check is the other
half of that skip: every file there is a member or a finding, and none is
silently lost.

**How you use it.** `check_archive_members(walk)` and `check_declared_files(walk)`,
yielding `Finding`s like every other check. `archive_members(walk)` returns the
member set on its own.

**Depends on.** `validate.corpus` for what the archive reader read,
`archive.layout.Layout` for a unit's own files, `corpus.container`,
`corpus.placement`, `address` for the first ordinal, `sourcepath` for what a
`local` may be, `validate.report`. ⛔ Nothing in this package's other three
modules.

## ⛔ A member is what the readers read, never a list kept here

⭐ **Derived from two sources that already exist.** The walk records every
container map it parsed and every document beneath each map's variant, parsed
or refused. The adapter scaffold's `Layout.unit_files` names each declared unit's own
directory, which a build reads for media and the overlay. ⛔ Anything else is
a stray: a note, a map's second variant, a file outside a unit, an undeclared
unit's files.

⚠️ **A member-shaped file at the wrong depth is not always a stray.** A `.json`
beneath a map's variant is read as a document wherever it sits, so
`identity` refuses it and this check does not refuse it twice. A non-`.json`
file at the same place is never read, and so it is a stray.

⛔ **Refused, never resolved.** A stray is not read as material, and the
manifest cannot include it. Beneath a map that did not parse, nothing is
judged here: `run` already reports that no document there was read.

## ⛔ And the other direction: a file the archive DECLARES and did not write

⚠️ **Two questions.** `check_archive_members` asks whether every file beneath the root
is accounted for; `check_declared_files` asks whether everything the archive
accounts for is there. ⛔ Without the second, an adapter that wrote a unit's
media anywhere but `Layout.unit_files` shipped an archive `validate` called
valid and a reader met as a broken glyph on every media-bearing page — the
latest and most expensive place a location disagreement can surface.

⭐ **The two checks are one question from two ends**, which is why they share
this module and its one derivation of where a unit's own files live. ⚠️ A
second spelling of that directory in either half is the defect both exist to
refuse.

⛔ **`media_skipped` is a third state and is exempt**, on the terms
`archive.document` states: *an ingest that named media and deliberately did not
fetch it*. Saying so is not the same as looking finished — the marker is in the
document, and `tests/fixture_checks/media.py` has read it that way from
the start.
"""

from __future__ import annotations

from collections.abc import Iterator
from pathlib import Path, PurePosixPath

from studyforge.address import FIRST_ORDINAL
from studyforge.archive.layout import Layout
from studyforge.corpus.container import CONTAINER_FILENAME
from studyforge.corpus.placement import ARCHIVE_DIRNAME
from studyforge.sourcepath import source_path_fault
from studyforge.validate.corpus import Unit, Walk
from studyforge.validate.report import Finding

#: ⛔ A file beneath the archive root that no reader reads.
RULE_ARCHIVE_STRAY = "archive-stray"

#: ⛔ A file a document declares and the archive does not hold where it says.
#: ⚠️ One rule id for three shapes of the same defect — absent,
#: misplaced, or named by nothing — because they are one question to a reader
#: and one fix to an adapter: write the file where `Layout.unit_files` puts it.
RULE_MEDIA_MISSING = "media-missing"

#: The two document keys whose entries name a file inside the unit's own
#: directory. ⛔ `archive.document`'s pair, and one entry vocabulary rather than
#: two: they differ in what a page does with them, never in what they hold.
DECLARED_FILES = ("assets", "attachments")


def check_archive_members(walk: Walk) -> Iterator[Finding]:
    """Refuse each file beneath the archive root that is not an archive member."""
    if walk.manifest is None:  # pragma: no cover - the walk stops without one
        return
    archive = walk.root / ARCHIVE_DIRNAME
    if not archive.is_dir():
        return
    members = archive_members(walk)
    units = _unit_files(walk)
    unread = {item.path.parent for item in walk.refused if item.container is None}
    for path in sorted(archive.rglob("*")):
        if not path.is_file() or path in members:
            continue
        if any(parent in units or parent in unread for parent in path.parents):
            continue
        yield Finding(
            RULE_ARCHIVE_STRAY,
            walk.relative(path),
            f"sits beneath {ARCHIVE_DIRNAME}/ and is not an archive member, so no check and "
            f"no build reads it. The archive holds container maps, the documents under each "
            f"map's variant, and each declared unit's own files. It is refused, not skipped "
            f"and not read as material: move it out, or write it where the layout places it.",
        )


def check_declared_files(walk: Walk) -> Iterator[Finding]:
    """Refuse each declared asset or attachment the archive does not hold where it says.

    ⛔ **`local` resolves against the unit's own directory and nowhere else**
    (`archive.document`), so this asks `Layout.unit_files` for the directory
    rather than composing one, so no reader of this location composes its own.
    """
    if walk.manifest is None:  # pragma: no cover - the walk stops without one
        return
    layout = Layout(walk.root)
    for unit in walk.units:
        if unit.document.get("media_skipped"):
            # ⭐ Declared and deliberately not fetched, which is a state and not
            # a shortfall — and it is said in the document rather than inferred.
            continue
        home = _home(layout, unit)
        if home is None:
            continue
        for key in DECLARED_FILES:
            for entry in unit.document.get(key) or ():
                yield from _declared(walk, unit, key, entry, home)


def _home(layout: Layout, unit: Unit) -> Path | None:
    """Return the unit's own directory, or `None` when `identity` already refuses it.

    ⛔ **Never a second finding for one defect.** A document whose `unit` is not
    an ordinal, or is not the one its directory names, is already refused by
    `structure.check_document_identity`; resolving a file against a directory
    derived from the disagreement would report the same defect a second time,
    in a rule about media.
    """
    ordinal = unit.document.get("unit")
    if isinstance(ordinal, bool) or not isinstance(ordinal, int) or ordinal < FIRST_ORDINAL:
        return None
    home = layout.unit_files(unit.container.address, ordinal)
    return home if home.name == unit.path.parent.name else None


def _declared(walk: Walk, unit: Unit, key: str, entry: object, home: Path) -> Iterator[Finding]:
    """Judge one entry of `assets` or `attachments` against the file it names."""
    singular = key[:-1]
    if not isinstance(entry, dict):
        return
    local = entry.get("local")
    if not isinstance(local, str) or not local.strip():
        yield Finding(
            RULE_MEDIA_MISSING,
            unit.where,
            f"declares an {singular} whose 'local' names no file, so the build has "
            f"nothing to place and the page would reach for an empty address. Every "
            f"entry names its file relative to {walk.relative(home)}/.",
        )
        return
    fault = source_path_fault(local)
    if fault is not None:
        # ⛔ The value is not echoed (R7): every shape refused here is, by
        # construction, a candidate home directory.
        yield Finding(
            RULE_MEDIA_MISSING,
            unit.where,
            f"declares an {singular} whose 'local' is not a plain location inside the "
            f"unit's own directory: it is {fault}. Every entry names its file "
            f"relative to {walk.relative(home)}/.",
        )
        return
    if not (home / PurePosixPath(local)).is_file():
        yield Finding(
            RULE_MEDIA_MISSING,
            unit.where,
            f"declares the {singular} {local!r} and the archive does not hold it at "
            f"{walk.relative(home / PurePosixPath(local))}. A unit's own files sit "
            f"beside raw/, not inside a variant; written anywhere else the page "
            f"renders a broken link and nothing else fails.",
        )


def archive_members(walk: Walk) -> frozenset[Path]:
    """Every container map and document file the archive reader read, parsed or refused."""
    found = {held.directory / CONTAINER_FILENAME for held in walk.containers}
    found.update(unit.path for unit in walk.units)
    found.update(item.path for item in walk.refused if item.container is not None)
    return frozenset(found)


def _unit_files(walk: Walk) -> frozenset[Path]:
    """Each declared unit's own directory, as the adapter's published layout places it.

    ⭐ `Layout` is `archive.layout`'s, which imports neither `validate` nor any
    skill, so the import is an ordinary one. ⭐ Asked of the address, as a build
    asks it (`generate.media`). A unit's files beside a map held at another
    directory are strays, beside that map's `address-directory`, because no
    build reads them there either.
    """
    layout = Layout(walk.root)
    return frozenset(
        layout.unit_files(held.container.address, unit.n)
        for held in walk.containers
        for unit in held.container.units
    )
