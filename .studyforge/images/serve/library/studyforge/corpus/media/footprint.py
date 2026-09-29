"""What a corpus's generated media actually weighs, read off the disk.

**What it does.** Walks the per-unit media directories a placement profile
minted, and every clip the narration record locates under the corpus root, and
records every file it finds, with its size, keyed by its path relative to the
corpus root. A recorded clip it cannot locate is named in `unweighed`.

**How you use it.** `measure(root, units)` where `units` are the
`UnitLocations` placement answered with; `measure_directories(root, dirs)` for
a caller that already holds the directories.

    footprint = measure(root, [profile.unit(address, 1, "Intro", origin=...)])
    footprint.total_bytes      # 3_214_998
    footprint.count            # 41
    footprint.largest.path     # PurePosixPath('.../audio/u-1-s3-9ab1c2de.mp3')

**Depends on.** `pathlib`, `studyforge.corpus.placement` for the kinds and the
locations type, `studyforge.describe`, and this package's `errors` and
`recorded` (which reads the record through `narrate.synth`).

## ⛔ A measurement, never a projection

⭐ **This module opens no manifest, multiplies nothing by a rate, and predicts
nothing.** A projection at a rate is `cli/plan/report.py`'s, in its own
wording; this is the reading of the disk, which is the one a commit decision may
rest on. ⭐ **`studyforge plan` prints this reading too, by calling `measure`**
— it takes no measurement of its own, so there is one number and not
two that could disagree. ⛔ The extraction source's push became impossible
because the only number anybody had was an estimate that nobody had ever
compared against a disk.

## ⛔ The population is placement's tuple, never a list retyped here

⚠️ **A fifth media kind must not leave this walk quietly weighing four.** The
directories come from `UnitLocations.directories`, which is derived from
`UNIT_MEDIA_DIRNAMES`, every kind `auto` commits. ⭐ **That the measured
population and the committed population are the same population is the property
that makes the verdict mean anything**: a footprint that weighed less than a
commit carries would clear a limit by not looking. ⚠️ `never` is not weighed,
and it ignores only the clips (`placement.names.UNCOMMITTED_DIRNAMES`).

## ⛔ The declared directories are not the whole population

⚠️ **A clip the narration record locates outside every declared directory** —
a removed or relabelled unit's old directory, a superseded clip — is on disk
and committed, and a walk over the declared directories never reaches it. ⭐ So
the reading adds every clip `recorded.recorded_clips` locates and something is
at, keyed exactly as the walk keys a file, so a clip both reach is counted once.
⛔ **The record locates; placement is never re-derived here.** A recorded clip
the record cannot locate, and that no walked directory holds under its name, is
named in `MediaFootprint.unweighed` rather than dropped, and an unreadable
record refuses the reading.

## ⚠️ An absent directory weighs nothing, and that is not a refusal

A corpus that has generated no media yet has a footprint of zero, and every
limit is under it. ⛔ Refusing there would make the first build of every corpus
fail on the honest absence of the thing it was about to create. **A root that
is not a directory at all is a different mistake** and is refused — the caller
has been handed something that is not a corpus.

## ⛔ Enumeration order is stated (R10)

`iterdir` and `rglob` answer in whatever order the filesystem chooses, and two
machines choose differently. Every file is keyed by its **posix path relative
to the root** and the records are sorted on that string — not on `Path`, which
compares by parts, and not on the absolute path, which begins with somebody's
home directory (R7).
"""

from __future__ import annotations

from collections.abc import Iterable
from dataclasses import dataclass
from pathlib import Path, PurePosixPath

from studyforge.corpus.media.errors import MediaError
from studyforge.corpus.media.recorded import recorded_clips
from studyforge.corpus.placement import UnitLocations
from studyforge.describe import describe


@dataclass(frozen=True, slots=True)
class MediaFile:
    """One generated media file, as the measurement found it."""

    #: Relative to the corpus root, as a posix path. ⛔ Never absolute: this
    #: value is printed in a refusal and carried into a report (R7).
    path: PurePosixPath
    size: int


@dataclass(frozen=True, slots=True)
class MediaFootprint:
    """Every generated media file under one corpus root, and what it weighs."""

    #: ⛔ Ordered by relative posix path, so two machines report the same
    #: footprint in the same order (R10).
    files: tuple[MediaFile, ...] = ()
    #: ⛔ One sentence per recorded clip this reading could not weigh,
    #: naming its speech id and why. Empty when every located clip was weighed.
    unweighed: tuple[str, ...] = ()

    @property
    def total_bytes(self) -> int:
        """What the whole generated media weighs."""
        return sum(found.size for found in self.files)

    @property
    def count(self) -> int:
        """How many generated media files there are.

        ⭐ **`max_files` reads it** — the limit a corpus of many small
        clips crosses while both byte ceilings are still under. ⚠️ A file
        count is the third thing a host bounds.
        """
        return len(self.files)

    @property
    def largest(self) -> MediaFile | None:
        """The heaviest file, or `None` when nothing was generated.

        ⛔ **Ties break on the path**, so the file a refusal names is the same
        file on two machines (R10).
        """
        if not self.files:
            return None
        return max(self.files, key=lambda found: (found.size, _key(found.path)))

    def over(self, limit: int) -> tuple[MediaFile, ...]:
        """Every file heavier than `limit`, in the footprint's own order.

        ⚠️ **All of them, not the first.** A corpus one file over its per-file
        limit and a corpus two hundred files over it are different situations,
        and a refusal naming only the heaviest would let the second be
        discovered one file at a time.
        """
        return tuple(found for found in self.files if found.size > limit)


def measure(root: Path | str, units: Iterable[UnitLocations]) -> MediaFootprint:
    """Weigh the generated media of every unit in `units`, under `root`.

    ⭐ Takes the locations placement already answered with, so this module has
    no opinion about where a profile puts media — under `tree` it is
    `.studyforge/units/<address>/audio/` and under `sibling` it is
    `study/audio/<stem>/` beside the source, and the walk is the same walk.
    """
    return measure_directories(root, _directories(units))


def measure_directories(
    root: Path | str, directories: Iterable[PurePosixPath | str]
) -> MediaFootprint:
    """Weigh every file under each of `directories`, and every recorded clip, under `root`.

    ⛔ **The narration record's clips are weighed wherever it locates
    them**, not only where `directories` reach, and a clip it cannot locate is
    named in `unweighed`. Raises `MediaError` when the record cannot be read.

    ⛔ **A directory named twice is walked once**, and a file reached through
    two of them is counted once: the records are keyed by relative path, so a
    caller that passed overlapping directories cannot inflate a total and
    cross a limit that was never crossed.
    """
    base = _root(root)
    found: dict[str, MediaFile] = {}
    for directory in directories:
        for path in _files_under(base / _relative(directory)):
            key = path.relative_to(base).as_posix()
            found[key] = MediaFile(PurePosixPath(key), path.stat().st_size)
    walked = {PurePosixPath(key).name for key in found}
    located, unlocated = recorded_clips(base)
    for relative in located:
        path = base / relative
        if path.is_file():
            found[relative.as_posix()] = MediaFile(relative, path.stat().st_size)
    unweighed = tuple(
        clip.sentence()
        for clip in unlocated
        if clip.filename is None or clip.filename not in walked
    )
    return MediaFootprint(tuple(found[key] for key in sorted(found)), unweighed)


def _root(root: object) -> Path:
    """Return the corpus root as a directory, or refuse without echoing it.

    ⛔ **DESCRIBED, never echoed** (R7). The root is an absolute
    path on somebody's machine, and this refusal is the one a misconfigured
    build prints into a log.
    """
    try:
        base = Path(root)  # type: ignore[arg-type]
    except TypeError:
        base = None
    if base is None or not base.is_dir():
        raise MediaError(
            "the corpus root is not a directory, so there is no generated media to "
            "weigh; a footprint is measured against the corpus root that holds the "
            f"generated output, and this was {describe(root)}"
        )
    return base


def _directories(units: Iterable[UnitLocations]) -> tuple[PurePosixPath, ...]:
    """Every media directory of every unit, in the order placement states."""
    return tuple(directory for unit in units for directory in unit.directories)


def _relative(directory: PurePosixPath | str) -> PurePosixPath:
    """Refuse a media directory that is not relative to the corpus root."""
    relative = PurePosixPath(directory)
    if relative.is_absolute():
        raise MediaError(
            "a media directory is named relative to the corpus root, and this one is "
            "absolute; weighing it would measure something outside the corpus and "
            "report a path from this machine"
        )
    return relative


def _files_under(directory: Path) -> Iterable[Path]:
    """Every file below `directory`, or nothing at all when it does not exist.

    ⚠️ **Symlinks are followed for their size and not resolved for their
    path.** `stat` answers for the target, which is what a clone will carry;
    the key stays the name inside the corpus, which is what the page addresses.
    """
    if not directory.is_dir():
        return ()
    return (path for path in directory.rglob("*") if path.is_file())


def _key(path: PurePosixPath) -> str:
    """Sort key for a recorded path. ⛔ The posix string, never the `PurePath`."""
    return path.as_posix()
