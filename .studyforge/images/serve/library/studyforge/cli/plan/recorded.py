"""The narration record as a plan reads it: which clips a build copies, and which it never will.

**What it does.** Reads `.studyforge/narration.json` once, without opening any
clip, and answers two questions for `derive`: which clip filenames the record
locates in a given unit's audio directory (`Recorded.copies`), and which
superseded clips the record still names on disk (`Recorded.superseded`).

**How you use it.** `recorded = read_record(root)`, then
`recorded.copies(token, audio)` for each declared unit and
`recorded.superseded` for the lines no build copies.

**Depends on.** `narrate.recorded` for the record, its reader and `located`,
`narrate.speakable.naming` for the clip name's parser, and `cli.plan.report`
for the records a plan is made of. ⛔ It opens the record and nothing else.

## ⛔ The narration record is a plan input

⭐ A build copies each clip a page addresses into any `--out` but the corpus
root, so the plan names every copy: one line per clip the record files under a
declared unit. ⛔ **The unit is read off the speech id's own unit token**, which
the declarations can answer; an entry whose filename does not carry the id it
is filed under is `narrate.playable`'s MISFILED, which no page addresses, so it
is not named.

## ⛔ A copy is LOCATED FROM THE RECORD, never re-derived

⚠️ A build probes a unit's clips in that unit's own audio directory, so an
entry whose recorded `where` is some other directory — a relabelled or removed
unit's — is a clip that directory holds and no page reads, and a build copies
nothing for it. ⭐ **So a copy is named only when the record's own `where` IS the
declared unit's audio directory**, and that directory is compared, never used to
guess where a clip is. ⛔ An entry with no `where` (a version-1 entry no run has
placed) cannot be located from the record, so it is not named as a copy: a line
that declined is recoverable by `studyforge narrate`, which records the
directory, and a line that overclaimed is not.

⚠️ **What is still named and not copied, stated rather than hidden:** an entry
whose speech id no page produces any more, filed in its unit's own directory,
cannot be told apart without opening the unit documents, which a plan never
does. `narrate` discloses such entries and `--prune` removes them.

## ⛔ A superseded clip is named as SUPERSEDED, never as a copy

⭐ The record names every clip an earlier wording or directory wrote that no
prune has removed. A build never copies one, so it is never a `Creation` and
never in `Plan.paths`, which a build's footprint is taken from. ⛔ It is named
only when the record locates it and something is at that path, asked by
`os.path.lexists` and never by opening it.
"""

from __future__ import annotations

import os
from collections.abc import Mapping
from dataclasses import dataclass, field
from pathlib import Path, PurePosixPath
from types import MappingProxyType

from studyforge.cli.plan.report import Refusal, SupersededClip
from studyforge.narrate.recorded import StateError, located, read_state, state_file
from studyforge.narrate.speakable import SpeakableError
from studyforge.narrate.speakable.naming import SEGMENT, parse_clip_name

#: What a refusal says about a recorded filename that carries a directory.
NOT_ONE_FILE = "records a clip filename that is not one file name"


@dataclass(frozen=True, slots=True)
class Recorded:
    """What the record says, as far as a plan may read it."""

    #: `unit token -> (path, ...)`: where the record locates each entry whose
    #: filename carries the id it is filed under, relative to the corpus root.
    #: ⛔ An entry the record cannot locate is not here.
    entries: Mapping[str, tuple[PurePosixPath, ...]] = field(
        default_factory=lambda: MappingProxyType({})
    )
    #: Every superseded clip the record locates and the corpus root holds, by path.
    superseded: tuple[SupersededClip, ...] = ()
    #: The record, relative to the corpus root, when it was read.
    read: tuple[str, ...] = ()
    refusals: tuple[Refusal, ...] = ()

    def copies(self, token: str | None, audio: str) -> tuple[str, ...]:
        """Return the filenames the record locates in `audio` for the unit `token` names.

        ⛔ `audio` is the declared unit's audio directory, relative to the corpus
        root. An entry recorded anywhere else, or nowhere, is not a copy.
        """
        if token is None:
            return ()
        here = PurePosixPath(audio)
        found = self.entries.get(token, ())
        return tuple(sorted({path.name for path in found if path.parent == here}))


def read_record(root: Path) -> Recorded:
    """Read the corpus's narration record, or say why not. ⛔ Nothing raises.

    An absent record is the ordinary case and is not read. An unreadable one is
    a refusal, exactly as a container map is.
    """
    file = state_file(root)
    where = file.relative_to(root).as_posix()
    try:
        state = read_state(file)
    except StateError as error:
        return Recorded(read=(where,), refusals=(Refusal(where, str(error)),))
    if not state.present:
        return Recorded()
    grouped: dict[str, set[PurePosixPath]] = {}
    refusals: list[Refusal] = []
    superseded: dict[str, SupersededClip] = {}
    for speech_id, clip in sorted(state.clips.items()):
        for item in clip.superseded:
            found = _superseded(root, speech_id, item.where, item.filename)
            if found is not None:
                superseded.setdefault(found.path, found)
        name = clip.filename
        if not name.strip():
            continue
        if not _one_file(name):
            # ⛔ The value is not echoed (R7). A copy of it would leave its unit's
            # audio directory, and a page's href never would.
            refusals.append(Refusal(where, NOT_ONE_FILE))
            continue
        at = _located(root, clip.where, name)
        if _filed_as(name) == speech_id and at is not None:
            grouped.setdefault(speech_id.split(SEGMENT, 1)[0], set()).add(at)
    entries = {token: tuple(sorted(found, key=str)) for token, found in grouped.items()}
    return Recorded(
        entries=MappingProxyType(entries),
        superseded=tuple(superseded[path] for path in sorted(superseded)),
        read=(where,),
        refusals=tuple(refusals),
    )


def _superseded(
    root: Path, speech_id: str, where: str | None, filename: str
) -> SupersededClip | None:
    """Return one superseded clip as a plan names it, or None if it cannot be named honestly.

    ⛔ Not named when the record cannot locate it, when its name carries a
    directory, or when nothing is at the located path.
    """
    at = _located(root, where, filename) if _one_file(filename) else None
    if at is None or not os.path.lexists(root / at):
        return None
    return SupersededClip(at.as_posix(), speech_id)


def _located(root: Path, where: str | None, filename: str) -> PurePosixPath | None:
    """Return where the record locates a clip, relative to `root`, or None if it cannot say.

    ⛔ `recorded.located` is asked, so a recorded directory that is absolute or
    climbs out of the root locates nothing here either (R7).
    """
    path = located(root, where, filename)
    return None if path is None else PurePosixPath(path.relative_to(root).as_posix())


def _one_file(name: str) -> bool:
    """Whether a recorded filename is one file name and nothing more."""
    return bool(name.strip()) and PurePosixPath(name).name == name and name not in (".", "..")


def _filed_as(name: str) -> str | None:
    """Return the speech id a clip filename carries, or None if it carries none."""
    try:
        filed, _ = parse_clip_name(PurePosixPath(name).stem)
    except SpeakableError:
        return None
    return filed
