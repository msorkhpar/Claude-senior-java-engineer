"""The clips the narration record locates, as the footprint weighs them.

**What it does.** Reads `.studyforge/narration.json` once, through
`narrate.recorded`'s own reader, and answers one question for `footprint`: *at
which paths under the corpus root does the record locate a clip* — every
entry's current clip and every superseded one — and, by name, which recorded
clips it cannot locate.

**How you use it.** `located, unlocated = recorded_clips(base)`, where `base` is
the corpus root `footprint` has already checked is a directory.

**Depends on.** `narrate.recorded` for the record's file, its reader and
`located`, this package's `errors`, and the standard library. ⛔ It opens the
record and nothing else, and it never opens a clip.

## ⛔ Why the footprint reads the record at all

⚠️ **The declared units' media directories are not every clip a corpus holds.**
A clip in a removed or relabelled unit's old directory, or a superseded clip
outside every declared directory, is still on disk and still committed under
`auto`, and a walk over the declared directories alone never weighs it — so a
corpus could read UNDER a limit its bytes had crossed. ⭐ The record locates
every clip it wrote, so the footprint asks it.

⛔ **The record locates; this module never re-derives placement.** A path here
is `recorded.located(root, where, filename)` and nothing else, and a recorded
directory that is absolute or climbs out of the root is refused by the
record's own reader before this module sees it (R7).

## ⚠️ What it cannot locate is NAMED, never dropped

An entry a version-1 record wrote carries no directory (`where` is null), and a
filename that is not one file name locates nothing honest. ⭐ Each such clip is
returned by speech id, so `footprint` can state what it did not weigh instead
of under-counting in silence. ⛔ **An unreadable record is a refusal**
(`MediaError`), not an empty answer: an empty answer is exactly the silent
under-count this module exists to prevent. A blank filename records no clip,
so there is nothing to weigh and nothing to name.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path, PurePosixPath

from studyforge.corpus.media.errors import MediaError
from studyforge.narrate.recorded import StateError, located, read_state, state_file


@dataclass(frozen=True, slots=True)
class Unlocated:
    """One clip the record names and cannot locate under the corpus root."""

    speech_id: str
    #: ⛔ `None` when the recorded name is not one file name: it is not echoed (R7).
    filename: str | None

    def sentence(self) -> str:
        """Say, by name, what was not weighed and why."""
        if self.filename is None:
            return (
                f"{self.speech_id}'s clip: the narration record gives it a filename that is "
                "not one file name, so it locates nothing to weigh"
            )
        return (
            f"{self.speech_id}'s clip {self.filename}: the narration record carries no "
            "directory for it and no weighed media directory holds that name; "
            "`studyforge narrate` records where it is"
        )


def recorded_clips(base: Path) -> tuple[tuple[PurePosixPath, ...], tuple[Unlocated, ...]]:
    """Return every path the record locates a clip at, and every clip it cannot locate.

    ⭐ An absent record locates nothing and is not a failure. ⛔ Paths are
    relative to `base`, posix and sorted (R10), and include clips that are not
    on disk: whether something is there is the footprint's question.
    """
    try:
        state = read_state(state_file(base))
    except StateError as error:
        raise MediaError(
            "the narration record cannot be read, so the clips it locates cannot be "
            f"weighed and the footprint is not taken: {error}"
        ) from None
    found: set[PurePosixPath] = set()
    unlocated: list[Unlocated] = []
    for speech_id, clip in sorted(state.clips.items()):
        written = [(clip.where, clip.filename)]
        written += [(item.where, item.filename) for item in clip.superseded]
        for where, filename in written:
            if not filename.strip():
                continue
            if not _one_file(filename):
                unlocated.append(Unlocated(speech_id, None))
                continue
            path = located(base, where, filename)
            if path is None:
                unlocated.append(Unlocated(speech_id, filename))
                continue
            found.add(PurePosixPath(path.relative_to(base).as_posix()))
    return (
        tuple(sorted(found, key=PurePosixPath.as_posix)),
        tuple(dict.fromkeys(unlocated)),
    )


def _one_file(name: str) -> bool:
    """Whether a recorded filename is one file name and nothing more."""
    return PurePosixPath(name).name == name and name not in (".", "..")
