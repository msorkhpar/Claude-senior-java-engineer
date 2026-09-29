r"""Which clip each narrated element plays — ⛔ joined on the SPEECH ID, never the position.

**What it does.** Joins `speakable`'s speech units to `recorded`'s regeneration record
and returns the one mapping a page needs: `SpeechUnit.position -> filename`,
plus a **named state** for every unit that has no clip to play.

**How you use it.** ⭐ Read the record ONCE for the corpus, then ask per document:

    from studyforge.narrate.playable import playable_of
    from studyforge.narrate.recorded.record import read_state, state_file

    recorded = read_state(state_file(root))          # once per corpus
    playing = playable_of(document, recorded)        # once per page
    narration = Narration.of(playing.filenames, placement)   # the renderer's side

⚠️ **`read_state` on a corpus that has never been narrated answers `present=False`
rather than raising**, and this module keeps that quiet all the way through —
`playing.narrated` is `False`, `playing.silent` is empty, and the page renders
exactly as it did before narration existed. ⛔ That is the ordinary case, not an
error path.

**Depends on.** `narrate.speakable` for the unit record and the clip-name
parser, `narrate.recorded.record` for the recorded state, and `describe`. ⛔ **Not
on `render`** — this module hands out filenames, and turning one into an href is
`render.page.narration`'s through the placement policy (R4).

## ⛔ WHY THIS MODULE EXISTS AT ALL, AND WHY IT IS NEITHER NEIGHBOUR'S

⚠️ **Synthesis is keyed by speech id and the player by position, and until
now nothing held both.** ⛔ It is not `narrate/synth/`'s: that package answers
*which clips must be MADE now* and is the write side, whose seam runs
`incremental → record` and stops. ⛔ It is not `render/page/`'s: that package's
own contract says *"`narrate.speakable` is where a speech unit is minted, and
this package must not reach for it"*, so a join living there would be the second
numbering scheme the whole design is built to prevent. ⭐ **So it is a third
module, a sibling of both, inside `narrate/` because both halves of the join are
`narrate`'s records** — and it adds no dependency edge that did not exist.

⭐ **It is `speakable`'s mirror, and named for it.** `speakable_of(document)`
says what can be *said*; `playable_of(units, state)` says what can be *played*.

## ⛔ THE JOIN KEY IS `SpeechUnit.id`. A POSITIONAL JOIN IS THE DEFECT.

⚠️ **A positional join — zip the units against the record's entries in order —
is correct exactly until one unit gains or loses a speakable element, and then
every clip after it addresses the WRONG audio, with every file present and every
filename valid.** ⛔ Nothing renders wrong and nothing raises; the reader simply
hears another paragraph. ⭐ **Two facts make that failure ordinary rather than
exotic, and either alone is enough:**

1. ⛔ **The record is written in `sorted(clips)` order, which is lexicographic
   over ids and NOT reading order.** `…b10` sorts before `…b2`, so the record's
   own sequence diverges from the page's the moment a section has ten speech
   units.
2. ⛔ **The record is never pruned.** `synth.incremental.synthesise` merges into
   what it read, so a unit whose blocks moved leaves its old entries behind and
   the record carries MORE clips than the document has units, interleaved.

⭐ **So the id is the only key, and the recorded filename is made to corroborate
it rather than merely be trusted**: `parse_clip_name` reads the stem back to
`(speech id, digest)`, the id must be the key the entry is filed under, and the
digest is compared against what the unit says *now*.

## ⭐ A CLIP THAT CANNOT BE PLAYED IS A NAMED STATE (R6), NEVER A `KeyError`

⛔ **The live run is long and a failure discovered at the end of it costs the
whole run**, so nothing here raises on an absent clip and nothing returns silence
without saying why. Every unit that yields no href appears in `Playable.silent`
with a sentence a person reads.

⚠️ **`WORDS_MOVED` is the one state that still plays.** The recorded clip is a
real file made from older words; withholding it would give the reader silence
with no reason, and the next synthesis pass replaces it. ⭐ Same answer, and the
same argument, as `synth`'s `Synthesis.unsettled`. ⛔ **It plays, and it is not
silent**: `validate.narration` reports every one as a RED finding naming what
to re-run, because a stale clip nobody is told about is the defect.
"""

from __future__ import annotations

from collections.abc import Mapping, Sequence
from dataclasses import dataclass
from pathlib import Path
from types import MappingProxyType

from studyforge.describe import describe
from studyforge.narrate.recorded.record import State
from studyforge.narrate.speakable import speakable_of
from studyforge.narrate.speakable.naming import digest_of, parse_clip_name
from studyforge.narrate.speakable.records import SpeakableError, SpeechUnit

#: Where a narrated element sits: the served section's key, the block indices
#: from that section down, and the item or row inside the block — or `None` for
#: the block as a whole. ⛔ It is `SpeechUnit.position`'s tuple and this module
#: never spells it a second way. ⚠️ `render.page.narration` declares the same
#: shape in its own package **on purpose**: the renderer must not import
#: `narrate`, so the two agree by contract rather than by an import edge.
Position = tuple[str, tuple[int, ...], "int | None"]

#: Why a unit has no clip to play. ⛔ Sentences, because a person reads them (R6)
#: — the same form `synth.incremental` uses for why a unit is stale.
NOT_RECORDED = "no clip is recorded for this unit"
NOT_PLACED = "the record names no file for this unit"
MISFILED = "the recorded clip is not named for this unit"
NOT_ON_DISK = "the recorded clip is not on disk"

#: ⚠️ Not a reason to go silent — reported beside the href, not instead of it.
WORDS_MOVED = "the clip was made from other words than this unit now says"


@dataclass(frozen=True, slots=True)
class Unmatched:
    """One unit the record does not match, and why — ⛔ a state, not an omission."""

    position: Position
    speech_id: str
    reason: str
    #: What the record named, or `''` when it named nothing. ⚠️ Carried so a
    #: `MISFILED` finding can be read without opening the record by hand.
    filename: str = ""


@dataclass(frozen=True, slots=True)
class Playable:
    """What this page can play, what it cannot, and what it plays under protest."""

    #: `position -> filename`. ⛔ A filename, never a path: where a unit's audio
    #: was placed is the placement profile's answer and nothing here knows it (R4).
    filenames: Mapping[Position, str]
    #: Every unit with no clip, in reading order. ⛔ None of these is in `filenames`.
    silent: tuple[Unmatched, ...]
    #: Every unit whose clip is real but was made from other words. ⚠️ These ARE
    #: in `filenames` and do play; see the module contract.
    stale: tuple[Unmatched, ...]
    #: Whether the corpus has a narration record at all. ⛔ **`False` is the
    #: ORDINARY case** — a corpus that has never been narrated — and it is QUIET:
    #: `silent` is empty, because a unit with no clip in a corpus with no clips
    #: is not a fault anyone can act on. ⭐ Spelled the way `record.State.present`
    #: is, and for the same reason: absent is a state, not a failure.
    narrated: bool = True

    def __bool__(self) -> bool:
        """Whether this page plays anything at all."""
        return bool(self.filenames)

    def reasons(self) -> Mapping[str, str]:
        """Return `speech id -> sentence` for every unit the record does not match.

        ⭐ One place to print at the end of a long run, in `synth.incremental`'s
        own spelling — ⛔ and it covers `stale` as well as `silent`, because a
        clip that plays the wrong words is not a clean unit.
        """
        return MappingProxyType(
            {entry.speech_id: entry.reason for entry in (*self.silent, *self.stale)}
        )


def playable_of(
    document: dict,
    state: State,
    *,
    audio: Path | str | None = None,
) -> Playable:
    """Return the clips one served unit document can play — ⭐ `speakable_of`'s mirror.

    ⛔ **This is the whole join, from the thing a build actually holds.** A build
    reads the record once for the corpus (`read_state(state_file(root))`) and asks
    this per document; the answer goes straight to `render.page.Narration.of`.

    ⚠️ It walks the document through `speakable_of`, so the units it joins are the
    minter's own and no caller derives a second set.
    """
    return playable_of_units(speakable_of(document).units, state, audio=audio)


def playable_of_units(
    units: Sequence[SpeechUnit],
    state: State,
    *,
    audio: Path | str | None = None,
) -> Playable:
    """Return the clips `units` can play, joined to `state` **on the speech id**.

    ⭐ The lower door, for a caller that has already walked the document. Most
    callers want `playable_of` above.

    ⛔ `audio` is the directory those clips were placed in — the unit's placement
    rooted by `recorded.audio_dir`, never composed. When it
    is `None` the disk is not consulted at all and a recorded clip is taken at
    its word, which keeps the pure path pure (R10).

    ⚠️ Two units sharing one position is refused rather than resolved: the later
    one would silently replace the earlier in the mapping, which is one clip
    going missing with nothing raised — the failure shape this module exists to
    remove.

    ⛔ **A corpus with no record at all is QUIET.** Every unit is unplayable, but
    none of them is reported: `narrated` is `False` and `silent` is empty. ⚠️ The
    alternative is a build that prints one complaint per paragraph for every
    corpus that has never been narrated, which is the reading floor arriving as
    an error log (R6, and the player's `SILENT` is the same answer at the page).
    """
    if not isinstance(state, State):
        raise TypeError(f"the recorded clips arrive as a State, got {describe(state)}")
    directory = Path(audio) if audio is not None else None
    resolved: dict[Position, str] = {}
    silent: list[Unmatched] = []
    stale: list[Unmatched] = []
    seen: set[Position] = set()
    for index, unit in enumerate(units):
        if not isinstance(unit, SpeechUnit):
            raise TypeError(f"segment {index} is {describe(unit)}, not a SpeechUnit")
        if unit.position in seen:
            raise ValueError(
                "two speech units claim one position, so one of their clips would be "
                "dropped with nothing raised; a walker mints one position per unit"
            )
        seen.add(unit.position)
        filename, reason = _clip_for(unit, state, directory)
        if reason:
            if state.present:
                silent.append(Unmatched(unit.position, unit.id, reason, filename))
            continue
        resolved[unit.position] = filename
        if _words_moved(unit, filename):
            stale.append(Unmatched(unit.position, unit.id, WORDS_MOVED, filename))
    return Playable(MappingProxyType(resolved), tuple(silent), tuple(stale), narrated=state.present)


def _clip_for(unit: SpeechUnit, state: State, directory: Path | None) -> tuple[str, str]:
    """Return `(filename, '')` for a unit that plays, or `(what was named, why not)`.

    ⛔ **The corroboration is here and it is the whole point.** The record is
    keyed by id, and the filename it holds carries the id again; if the two
    disagree the entry cannot be trusted to address this unit's audio, so the
    unit goes silent rather than playing somebody else's words.
    """
    recorded = state.clips.get(unit.id)
    if recorded is None:
        return "", NOT_RECORDED
    filename = recorded.filename
    if not isinstance(filename, str) or not filename.strip():
        return "", NOT_PLACED
    try:
        filed, _ = parse_clip_name(Path(filename).stem)
    except SpeakableError:
        return filename, MISFILED
    if filed != unit.id:
        return filename, MISFILED
    if directory is not None and not (directory / filename).is_file():
        return filename, NOT_ON_DISK
    return filename, ""


def _words_moved(unit: SpeechUnit, filename: str) -> bool:
    """Whether the recorded clip was synthesised from words this unit no longer says."""
    _, digest = parse_clip_name(Path(filename).stem)
    return digest != digest_of(unit.speak)
