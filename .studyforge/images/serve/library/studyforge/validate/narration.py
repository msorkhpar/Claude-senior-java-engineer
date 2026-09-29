r"""Refuse a narrated corpus whose clips say words their paragraphs no longer say.

**What it does.** Where a corpus carries a narration record, asks the build's
own join which clip each unit page would play, and reports every speech unit
whose clip was synthesised from other words than the unit says now —
`narrate.playable`'s `WORDS_MOVED`. ⭐ Each finding names the unit's speech id
and says what to run: `studyforge narrate <corpus-root> --voice <the record's
voice>`. ⛔ It never reproduces a word of the text (R7).

**How you use it.** `CHECKS`, which `validate.run` drains like every other
check's tuple — ⚠️ **only while narration is on** (`narrate.enabled`), which
`validate.run` asks and this module does not.

**Depends on.** `validate.corpus` for the root, `validate.report`,
`archive.scrub` for the voice it prints, and — deferred, see below —
`generate` for the build's own walk and join, `unit` for the unit builder and
`narrate.recorded` for the record.

## ⛔ WHY `validate`, WHEN THE JOIN ALREADY KNEW

⚠️ **A paragraph whose prose changed keeps playing its old clip.**
`narrate.playable` marks it stale and resolves it anyway, which is right for
the page (see below), and ⛔ **nothing else a person runs prints the list** —
`plan` and the build both read clean.
⭐ `validate` is the definition of done an author already runs, so the list
lands where it is read.

## ⭐ THE BUILD'S JOIN, NOT A SECOND ONE

⛔ **`generate.narration.heard` is asked, per unit, exactly as a build asks
it**: the same served document, the same audio directory, the same
`playable_of` with the disk probed. So the units this check names are the
units a built page plays under protest, by construction — a second walk here
would be a second answer to *which clip does this paragraph play*. ⚠️ The
import is deferred: `generate` imports the renderer, and this package's
contract keeps `render` out of every other check's import.

## ⚠️ WHY THE PAGE STILL PLAYS A STALE CLIP, AND IS NOT SILENT ABOUT IT HERE

⭐ The renderer's third state is *a clip promised and not on disk*, and its
notice says so; a stale clip IS on disk, so marking it there would print a
false sentence and take the paragraph's audio away for any edit, a comma
included. ⛔ **The defect was that nobody was told, and this check is the
telling**: it goes RED, the build-and-serve skill stops on a RED `validate`
unless it is about to re-narrate, and the finding names the command.

⛔ **Every check yields; none raises** (R6). ⭐ **A corpus with no record is
quiet** — the reading floor is complete without narration (C5).
"""

from __future__ import annotations

from collections.abc import Iterator
from pathlib import Path

from studyforge.archive.errors import ArchiveError
from studyforge.archive.scrub import PersonalDataLeak, assert_clean
from studyforge.validate.corpus import RULE_PERSONAL_DATA, Walk
from studyforge.validate.report import Finding, Unchecked

#: ⛔ A narrated speech unit whose clip was made from words it no longer says.
RULE_NARRATION_STALE = "narration-stale"

#: ⛔ A narration record the build would refuse to read, so no clip is judged.
RULE_NARRATION_RECORD = "narration-record"

#: What the finding says to run when the record names no voice.
NO_VOICE = "<voice>"


def check_narration_current(walk: Walk) -> Iterator[Finding | Unchecked]:
    """Every narrated speech unit's clip was made from the words it says now.

    ⭐ **One finding per stale speech unit**, filed against its unit's archive
    directory and naming its speech id — the id carries the section and the
    block, so the paragraph is found without a word of it quoted.
    """
    from studyforge.generate import RAISES, heard, read_corpus, unit_location
    from studyforge.narrate.recorded import StateError, read_state, state_file
    from studyforge.unit.builder import build_unit
    from studyforge.unit.errors import ContentError

    where = state_file(walk.root).relative_to(walk.root).as_posix()
    try:
        state = read_state(state_file(walk.root))
    except StateError as error:
        # ⭐ The build stops on the same record (`generate.narration.recorded`).
        yield Finding(RULE_NARRATION_RECORD, where, f"{error}; the build refuses it too.")
        return
    if not state.present:
        return
    voice = yield from _voice(state.voice, where)
    try:
        corpus = read_corpus(walk.root)
    except RAISES:
        yield Unchecked(
            RULE_NARRATION_STALE,
            ".",
            "the corpus did not read the way a build reads it, so no clip was judged",
        )
        return
    for source in corpus.units:
        at = _where(source.directory, walk.root, source.key)
        try:
            document = build_unit(
                source.directory,
                declared_practices=source.declared_practices,
                mentions=source.mentions,
            )
        except (*RAISES, ContentError, ArchiveError):
            # ⛔ A unit that will not build is another check's finding, not a clean one.
            yield Unchecked(RULE_NARRATION_STALE, at, "the unit did not build; no clip was judged")
            continue
        _, playing = heard(corpus, source, unit_location(corpus, source), document, state)
        for entry in playing.stale:
            yield Finding(RULE_NARRATION_STALE, at, _stale(entry.speech_id, voice))


def _where(directory: Path, root: Path, key: str) -> str:
    """Return the unit's archive directory under the root, or its key. ⛔ Never absolute (R7)."""
    try:
        return directory.relative_to(root).as_posix()
    except ValueError:
        return key


def _voice(voice: str | None, where: str):
    """Return the voice a finding may print, yielding a finding if the record's is not clean."""
    if voice is None:
        return NO_VOICE
    try:
        assert_clean({"voice": voice}, where)
    except PersonalDataLeak as error:
        yield Finding(RULE_PERSONAL_DATA, where, str(error))
        return NO_VOICE
    return voice


def _stale(speech_id: str, voice: str) -> str:
    """One stale unit's finding: which, why it matters, and what to run. ⛔ No text (R7)."""
    return (
        f"speech unit '{speech_id}' plays a clip made from words it no longer says; "
        f"run `studyforge narrate <corpus-root> --voice {voice}`, which re-makes the "
        f"clips whose words moved."
    )


#: Is every narrated unit's clip made from what the unit says now.
CHECKS = (check_narration_current,)
