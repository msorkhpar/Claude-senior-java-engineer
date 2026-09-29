r"""The build's narration pass: it READS a record and a disk, and synthesises nothing.

**What it does.** Opens a corpus's narration record once and answers, for each
unit page, which of the three narration states that page is in, as
the `render.page.Narration` the renderer takes. `write_narration(root, into)`
re-runs the unit-page pass after `studyforge narrate`, rewriting only the pages
whose bytes moved.

**How you use it.**

    from studyforge.generate import write_narration

    moved = write_narration(corpus_root, output_root)
    moved.written.pages    # pages whose narration changed, rewritten
    moved.unchanged        # this build's own pages whose bytes did not move

`narrated(corpus)` and `narration_for(...)` are the two halves `generate.units`
calls while it renders; a page pass never reads the record itself.
`voiced(corpus, choice)` is a run's override of the corpus's own choice.
`heard(...)` is the join both the page and `generate.clips` ask, so the clip a
build copies is the clip the page addresses. `clips_on_disk(corpus, state)` says
whether any recorded clip is on disk.

**Depends on.** `narrate.playable` for the join, `narrate.recorded` for where the
record lives, how it is read and where a unit's clips were placed,
`render.page` for `Narration`, and `generate.units` for the page pass (deferred:
`units` imports this module). Not `narrate.client` and not `synthesise`
(a build never synthesises), asserted by `tests/studyforge/generate/test_no_synthesis.py`.

## The three states: recorded, unkept and not recorded

No record: `SILENT`, so the page is byte-for-byte the pre-narration page. A
record: `playable_of` is asked WITH `audio=`, because without the directory a
recorded clip is taken at its word and a broken corpus renders as a working one.
The gaps are `UNKEPT` only:
`NOT_RECORDED` is a unit nobody promised anything, never a gap.

## ⛔ Narration off is a fourth input, and it is the FIRST state exactly

⭐ **Narration is optional**: a corpus may be read without voices, even when
clips were generated. ⭐ So `narrated`
answers an absent record for a corpus that is not voiced (`Corpus.narration`,
from `corpus.json`'s `narration` or a run's `--no-narration`), **without
opening the record**: every page is `SILENT` and no clip is copied, which is
the reading floor byte for byte, with no player and no gap notice. ⛔ **Nothing
is deleted and nothing is rewritten** (R3): the record and every clip stay
where `narrate` put them, so voicing the corpus again plays them with no
re-synthesis. ⛔ **`narrated` is the ONE gate**: a pass that called `recorded`
itself would voice a corpus its author turned off.

## ⛔ No clip on disk at all is a download not taken, and it is not a gap

⭐ **A corpus's clips may be a download** (a release the reader restores), so a
site with none of them on disk is the ordinary state of a fresh checkout, and a
page that called every passage *missing* would be complaining about a choice.
⭐ **So when no clip the record names is on disk anywhere in the corpus**
(`clips_on_disk`), each page links every clip the record names for it, the
disk unconsulted: `NOT_ON_DISK` is not a gap, and `NOT_PLACED` and `MISFILED`,
which are faults in the record, still are. ⭐ The page then learns whether the
clips arrived by asking its first clip in the browser (`narration-probe.js`),
so a restore after the build is heard without a rebuild, and nothing the build
writes can go stale when the clips move. ⛔ **With any clip on disk, the three
states are exactly as above**: a clip missing beside others is a gap the page
names.

## Where the disk is probed: the page's own directory

The disk is probed at `recorded.audio_dir(root, at)`, where `at` is this page's own
`unit_location`, the one derivation `narrate` places clips through as well. So a
labelled unit's clips are where its page looks, and every href a
page emits is answerable. A clip that is not there is a gap the page names;
nothing is guessed.

## What "does not rewrite" means here, against the rebuild policy (R3)

A full build (`write_site`) replaces its whole footprint, so every page is
rewritten, byte-identical where nothing moved (R10). `write_narration` is the
pass invoked alone: a page this build owns whose rendered bytes equal what is on
disk is not opened for writing at all, and every other target goes through
`writing.place`, so refusals are unchanged. The two agree; neither weakens R3.
"""

from __future__ import annotations

from dataclasses import dataclass, replace
from pathlib import Path, PurePosixPath
from types import MappingProxyType

from studyforge.corpus.placement import UnitLocations
from studyforge.generate.declarations import (
    BuildError,
    Corpus,
    UnitSource,
    read_corpus,
    unit_location,
)
from studyforge.generate.writing import Written, place
from studyforge.narrate.playable import MISFILED, NOT_ON_DISK, NOT_PLACED, Playable, playable_of
from studyforge.narrate.recorded import State, StateError, audio_dir, read_state, state_file
from studyforge.render.page import SILENT, Narration, Placement

#: The silent states that are a promise the disk did not keep. `NOT_RECORDED`
#: is deliberately absent: see this module's contract.
UNKEPT = frozenset({NOT_PLACED, MISFILED, NOT_ON_DISK})

#: ⭐ What a corpus that is not voiced reads: the record's own absent
#: state, so every reader downstream takes the path it already takes for a
#: corpus nobody narrated, and no branch on narration grows anywhere else.
SILENCED = State(clips=MappingProxyType({}), present=False)


@dataclass(frozen=True, slots=True)
class Renarrated:
    """What `write_narration` did: the pages it wrote, and the ones it left alone."""

    written: Written
    #: Pages this build owns whose bytes did not move, so they were not rewritten.
    unchanged: tuple[PurePosixPath, ...] = ()


def recorded(root: Path | str) -> State:
    """Read the corpus's narration record once, or stop naming it.

    An absent record is `State(present=False)`, the ordinary case. An unreadable
    one STOPS the build (R6): rendering it silent would make a
    broken narration identical to none, which is the defect the three states remove.
    The message is the record's own and carries no path (R7).
    """
    try:
        return read_state(state_file(root))
    except StateError as error:
        raise BuildError(f"the narration record cannot be read: {error}") from None


def narrated(corpus: Corpus) -> State:
    """Return the record this build voices, or an absent one when narration is off.

    ⭐ **The one gate every pass reads the record through**, so the page pass and
    the clip pass cannot disagree about whether a corpus speaks. ⛔ Off reads
    nothing: not the record and not the disk.
    """
    if not corpus.narration:
        return SILENCED
    return recorded(corpus.root)


def voiced(corpus: Corpus, choice: bool | None) -> Corpus:
    """Return `corpus` voiced as a run asked, or as it declares when the run asked nothing.

    ⭐ A run's `--narration` / `--no-narration` overrides `corpus.json` for that
    run only; `None` keeps the corpus's own answer. ⛔ Nothing is written.
    """
    return corpus if choice is None else replace(corpus, narration=choice)


def gaps(playing: Playable) -> tuple:
    """Return the positions the record promised a clip for and cannot deliver, in order."""
    return tuple(entry.position for entry in playing.silent if entry.reason in UNKEPT)


def narration_for(
    corpus: Corpus,
    source: UnitSource,
    at: UnitLocations,
    document: dict,
    placement: Placement,
    state: State,
    *,
    on_disk: bool = True,
) -> Narration:
    """Return one unit page's narration, in whichever of the three states it is in.

    ⭐ `on_disk` is `clips_on_disk`'s answer for the whole corpus. `False` links
    every clip the record names for this page without probing the disk, because
    no clip is there to probe: see this module's contract.
    """
    if not state.present:
        return SILENT
    if on_disk:
        _, playing = heard(corpus, source, at, document, state)
    else:
        playing = playable_of(document, state)
    return Narration.of(playing.filenames, placement, missing=gaps(playing))


def clips_on_disk(corpus: Corpus, state: State) -> bool:
    """Whether any clip the record names is on disk where a unit's page links it.

    ⭐ Asked once per build, of every declared unit's audio directory — the one
    `narrate` placed into — and a directory is listed once rather than probed
    per clip. ⛔ An absent record is `False`: nothing was promised.
    """
    if not state.present:
        return False
    names = {
        clip.filename
        for clip in state.clips.values()
        if isinstance(clip.filename, str) and clip.filename.strip()
    }
    for source in corpus.units:
        directory = audio_dir(corpus.root, unit_location(corpus, source))
        if not directory.is_dir():
            continue
        if any(entry.name in names and entry.is_file() for entry in directory.iterdir()):
            return True
    return False


def heard(
    corpus: Corpus, source: UnitSource, at: UnitLocations, document: dict, state: State
) -> tuple[Path, Playable]:
    """Return the directory one unit's clips were probed in, and what its page plays.

    ⛔ `at` is the page's own placement, the one `narrate` wrote through.
    """
    probed = audio_dir(corpus.root, at)
    return probed, playable_of(document, state, audio=probed)


def write_narration(root: Path | str, into: Path | str) -> Renarrated:
    """Re-run the unit-page pass after narration moved, rewriting only moved pages.

    `into` is required, as for every pass in this package. The imports are
    deferred because `generate.units` and `generate.clips` import this module.
    ⭐ The clips a moved page addresses are copied too (`generate.clips`), or a
    narration re-run into any output but the corpus root would link nothing.
    """
    from studyforge.generate.clips import for_output, unit_clips
    from studyforge.generate.units import unit_bodies

    corpus = for_output(read_corpus(root), into)
    out = Path(into)
    written: list[PurePosixPath] = []
    refused: list[PurePosixPath] = []
    replaced: list[PurePosixPath] = []
    unchanged: list[PurePosixPath] = []
    for at, body in unit_bodies(corpus):
        target = out / Path(str(at))
        if corpus.footprint.owns(at) and _holds(target, body):
            unchanged.append(at)
            continue
        place(out, at, body, written, refused, replaced, footprint=corpus.footprint)
    pages = Written(pages=tuple(written), refused=tuple(refused), replaced=tuple(replaced))
    return Renarrated(pages + unit_clips(corpus, out), tuple(unchanged))


def _holds(target: Path, body: bytes) -> bool:
    """Whether `target` is a regular file already holding exactly `body`."""
    return target.is_file() and not target.is_symlink() and target.read_bytes() == body
