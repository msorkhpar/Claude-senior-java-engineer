r"""A narrated page's clips, copied under any output root but the corpus root.

**What it does.** For every unit with material, copies each clip its page
addresses from where `studyforge narrate` placed it beside the material to the
path that page's href resolves to under the output root, and names each clip
the record promised and the disk does not hold. At `--out` = the corpus root it
copies nothing: the clips already sit where the pages look.

**How you use it.** `write_clips(root, into)` for a corpus root;
`unit_clips(corpus, into)` when the declarations have been read;
`for_output(corpus, into)` for the corpus whose footprint a build into `into`
uses — at the corpus root it owns no clip, anywhere else it links no corpus
file — and `files_unreached(corpus)` for the corpus-file links that leaves.

**Depends on.** `generate.narration` for what a page plays and where its clips
were probed, `generate.declarations`, `generate.writing` for R3,
`corpus.placement` for the destination, `narrate.playable` for the state a
clip absent from disk is in, and `unit.builder`. ⛔ Not `narrate.client` and
not `synthesise`: a build copies what narrate wrote.

## ⛔ Why a build copies (R8)

⚠️ A page's audio href is relative to the page, so under an output root other
than the corpus root it resolves under that root, where narrate wrote nothing:
the player would play nothing and name no gap. ⭐ The record and the clips are
inputs exactly like the archive, so the build copies them as `generate.media`
copies the archive's.

## ⛔ The destination is ASKED, never composed

⭐ Every copy lands at `UnitLocations.media_dir(AUDIO_DIRNAME) / <filename>` —
the two calls `render.page.Narration.of`'s href is made of — so the copy and the
href agree by construction. ⛔ The source is `narration.heard`'s probed
directory, the one the page's three states were decided against, so a clip is
copied exactly when its page plays it.

## ⛔ Only what a page addresses, never a synthesis, and nothing is deleted

⭐ The population is `Playable.filenames`: a clip file beside the material that
no page addresses is not copied. ⛔ A clip the record promised and the disk
lacks (`NOT_ON_DISK`) is not copied and goes into `Written.missing`; the page
already names the gap. `NOT_RECORDED` is never missing. ⚠️ A copy a later
record no longer names is left where it is, and not silently: a build with
narration off into another root names each clip an earlier narrated build copied
there (`Written.unlinked`), and a voiced build names each clip it plays that says
words its paragraph no longer says (`Written.stale`). Neither is acted on.

## ⚠️ What `studyforge plan` names and this does not copy

⛔ The plan reads the record without opening a unit document, so an entry whose
speech id no page produces any more is named there and copied by nothing here.
`narrate` discloses those entries and `--prune` removes them.
"""

from __future__ import annotations

from dataclasses import replace
from pathlib import Path, PurePosixPath

from studyforge.corpus.placement import AUDIO_DIRNAME
from studyforge.generate.declarations import BuildError, Corpus, read_corpus, unit_location
from studyforge.generate.narration import heard, narrated
from studyforge.generate.writing import Written, copy, same_root
from studyforge.narrate.playable import NOT_ON_DISK
from studyforge.narrate.speakable.naming import SpeakableError, parse_clip_name
from studyforge.unit import ContentError
from studyforge.unit.builder import build_unit, unit_sections


def write_clips(root: Path | str, into: Path | str) -> Written:
    """Read one corpus and copy the clips its pages address into `into`."""
    corpus = read_corpus(root)
    return unit_clips(for_output(corpus, into), into)


def for_output(corpus: Corpus, into: Path | str) -> Corpus:
    """Return `corpus` with the footprint a build into `into` may use.

    ⛔ **At the corpus root no clip is the build's own**: every clip there is
    narrate's, beside the material, and a rebuild must leave its bytes alone.
    ⚠️ **Anywhere else no page links a corpus file** (`files_unlinked`): the
    file stays where its author put it, and only a site written beside it
    reaches it.
    """
    if same_root(Path(into), corpus.root):
        return replace(corpus, footprint=corpus.footprint.without_clips())
    return files_unlinked(corpus)


def files_unlinked(corpus: Corpus) -> Corpus:
    """Return `corpus` with no unit linking a corpus file: a site written somewhere else.

    ⛔ A corpus file is linked where the author put it, which a page reaches
    only when the site is written into the corpus root. Anywhere else the link
    keeps the href the author wrote, and `files_unreached` names it.
    """
    return replace(
        corpus,
        units=tuple(
            replace(source, mentions=replace(source.mentions, beside=False))
            for source in corpus.units
        ),
    )


def files_unreached(corpus: Corpus) -> Written:
    """Name, by its page, every link to a corpus file this build's pages cannot reach.

    ⭐ One `unreached` entry per link, so the report counts them by unit.
    ⛔ Empty for a site written into the corpus root, which links every one,
    and nothing is copied or linked upward for any other: the report says
    where a build keeps them. A unit whose material will not read names none
    here; its page pass reports it.
    """
    found: list[PurePosixPath] = []
    for source in corpus.units:
        if source.mentions.beside:
            continue
        try:
            count = source.mentions.unreached(list(unit_sections(source.directory)))
        except ContentError:
            continue
        found += [unit_location(corpus, source).page] * count
    return Written(unreached=tuple(found))


def unit_clips(corpus: Corpus, into: Path | str) -> Written:
    """Run the clip pass over declarations that have already been read."""
    out = Path(into)
    beside = same_root(out, corpus.root)
    state = narrated(corpus)
    if not state.present:
        return Written(unlinked=() if beside or corpus.narration else _unlinked(corpus, out))
    written: list[PurePosixPath] = []
    refused: list[PurePosixPath] = []
    replaced: list[PurePosixPath] = []
    missing: list[PurePosixPath] = []
    stale: list[PurePosixPath] = []
    for source in corpus.units:
        at = unit_location(corpus, source)
        document = build_unit(
            source.directory,
            declared_practices=source.declared_practices,
            mentions=source.mentions,
        )
        probed, playing = heard(corpus, source, at, document, state)
        audio = at.media_dir(AUDIO_DIRNAME)
        for name in sorted({_one_file(name) for name in playing.filenames.values()}):
            if not beside:
                target = audio / name
                copy(
                    out,
                    target,
                    probed / name,
                    written,
                    refused,
                    replaced,
                    footprint=corpus.footprint,
                )
        missing += [
            audio / _one_file(entry.filename)
            for entry in playing.silent
            if entry.reason == NOT_ON_DISK
        ]
        stale += [audio / _one_file(entry.filename) for entry in playing.stale]
    return Written(
        media=tuple(written),
        refused=tuple(refused),
        missing=tuple(missing),
        replaced=tuple(replaced),
        stale=tuple(sorted(set(stale))),
    )


def _unlinked(corpus: Corpus, out: Path) -> tuple[PurePosixPath, ...]:
    """Every clip file under `out` in a unit's audio directory, which no page here links.

    ⭐ A build with narration off links no clip, so each one an earlier
    narrated build copied into this `--out` is reported. ⛔ Never deleted: a build
    deletes nothing, and the report says how a person removes them.
    """
    found: set[PurePosixPath] = set()
    for source in corpus.units:
        audio = unit_location(corpus, source).media_dir(AUDIO_DIRNAME)
        directory = out / Path(str(audio))
        if directory.is_dir():
            found |= {audio / path.name for path in directory.iterdir() if _is_clip(path)}
    return tuple(sorted(found))


def _is_clip(path: Path) -> bool:
    """Whether a file's name is a narration clip's (spec §8.2, the one parser)."""
    try:
        parse_clip_name(path.stem)
    except SpeakableError:
        return False
    return path.is_file()


def _one_file(name: str) -> str:
    """Return a recorded clip filename that is one file name, or refuse without echoing it.

    ⛔ A name carrying a directory would be copied outside its unit's audio
    directory, where no href points (R7 for the message: it is a record's value).
    """
    if PurePosixPath(name).name != name or name in (".", ".."):
        raise BuildError(
            "the narration record names a clip file that is not one file name, so a copy "
            "of it would land outside its unit's audio directory"
        )
    return name
