r"""Narration off, as the `build` and `serve` verbs honour it.

**What it does.** Answers the two questions a verb asks once a run has left
narration out: which built pages still carry narration (`voiced_pages`), and
which files under a served root are narration clips that must not be served
(`unvoiced_clips`). ⛔ **Whether a run leaves it out is not asked here**: that is
`narrate.narration_on`, the one predicate every stage asks.

**How you use it.**

    if not narration_on(root, asked=arguments.narration):
        loud = voiced_pages(corpus, site)        # non-empty: refuse, name each
        private = unvoiced_clips([site])         # the static mount's `private=`

**Depends on.** `corpus.placement` for the audio directory's one spelling,
`narrate.speakable` for the clip name's one parser, and `render.page` for the
attribute a narrated element carries. ⛔ Read at startup, never per request:
`serve` renders nothing at request time and this adds nothing to it.

## ⛔ Narration is optional

A reader may cover a course without voices: the skills ask about narration when
the material is captured and when it is served, and clips that were generated
may still go unserved.

## ⭐ Why serve REFUSES a narrated build rather than editing it on the way out

⛔ **Narration is in a page's bytes**: the player and every `data-audio` are
written by `build`, and R8 makes the built page the product — it opens over
`file://` with no server. ⚠️ A serve that stripped the player per request would
be a second author of the page and would answer differently from the file on
disk; one that only withheld the clips would serve a player whose every passage
fails, which is the broken-narration state and the opposite of the
reading floor. ⭐ **So `serve --no-narration` over a site built with narration
refuses, naming each page and the command that fixes it**, exactly as it
refuses a page nobody built. ⭐ And it withholds every clip under the root it
serves, so a clip a reader types the address of is not served either: a build
into the corpus root leaves the clips where `narrate` put them (R3), inside
what `serve` serves.
"""

from __future__ import annotations

from collections.abc import Callable, Iterable
from pathlib import Path, PurePath

from studyforge.corpus.placement import AUDIO_DIRNAME
from studyforge.narrate.speakable.naming import SpeakableError, parse_clip_name
from studyforge.render.page import AUDIO_ATTRIBUTE

#: What a page built with narration carries: the attribute, opened. ⭐ The
#: player's own gate (`render.page.document.player`) derives from it, so a page
#: without it has no player; an escaped quote in prose never matches.
NARRATED_MARK = f' {AUDIO_ATTRIBUTE}="'

#: Why a narrated page refuses a serve with narration off. ⭐ It names the fix.
BUILT_VOICED = (
    "narration is off for this serve and this page was built with it; "
    "build again with --no-narration"
)

#: What a serve says for each corpus it serves without narration.
SERVED_SILENT = "no player and no clip is served; every clip stays on disk"


def voiced_pages(corpus: object, site: Path) -> list[str]:
    """Every page of `corpus` under `site` that carries narration, sorted.

    ⭐ The population is `serve`'s own: the footprint's pages and the root index.
    A page that is not there is `serve`'s *unbuilt* refusal, not this one.
    """
    pages = {corpus.shared.root_index} | {
        path for path in corpus.footprint.files if path.suffix == ".html"
    }
    return sorted(str(page) for page in pages if _narrated(Path(site) / page))


def unvoiced_clips(roots: Iterable[Path]) -> Callable[[Path], bool]:
    """Return a `private=` predicate: whether a file is a narration clip under a root.

    ⭐ **A clip is a file inside an audio directory whose name is a clip name**
    (spec §8.2, parsed by `narrate.speakable`'s one parser), so material a
    source keeps in a folder that happens to be called `audio` is still served.
    """
    bases = tuple(Path(root).resolve() for root in roots)

    def private(path: Path) -> bool:
        resolved = Path(path).resolve()
        return any(
            resolved.is_relative_to(base) and is_clip(resolved.relative_to(base)) for base in bases
        )

    return private


def is_clip(relative: PurePath) -> bool:
    """Whether a path below a served root names a narration clip."""
    if AUDIO_DIRNAME not in relative.parts[:-1]:
        return False
    try:
        parse_clip_name(PurePath(relative.name).stem)
    except SpeakableError:
        return False
    return True


def _narrated(page: Path) -> bool:
    """Whether one built page carries narration. ⚠️ An unreadable page is `serve`'s to report."""
    try:
        return NARRATED_MARK in page.read_text(encoding="utf-8")
    except OSError, UnicodeDecodeError:
        return False
