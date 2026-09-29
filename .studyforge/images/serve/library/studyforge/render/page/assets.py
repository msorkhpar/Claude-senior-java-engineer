r"""Where this page reaches, relative to itself — the whole of R8, in one module.

**What it does.** Holds the placement decision one page is rendered against, and
turns it into the hrefs the page actually writes: its stylesheet, its script,
and every media file it shows.

**How you use it.**

    from studyforge.corpus import placement
    from studyforge.render.page.assets import Placement

    profile = placement.profile_for(manifest.placement)
    where = Placement(
        corpus=manifest.source,
        unit=profile.unit(address, ordinal, title),
        shared=profile.corpus(),
    )
    where.stylesheet()          # '../../../.studyforge/assets/page.css'
    where.media("images", "diagram.svg")

**Depends on.** `corpus.placement` for what a location is, `render.pageassets`
for the two shared filenames, `sourcepath` for what a path inside a corpus may
be, and `page.errors`.

## ⛔ Every href is relative to the page, and nothing here is ever absolute

⚠️ **The floor is a page opened by double-clicking it** (R8): no server, no
build step, no `fetch`. A rooted href works under a server and breaks the moment
the file is opened from disk, and an absolute one carries a home directory (R7).
⭐ So both are unrepresentable here — `relative_href` refuses an absolute path,
and this module never joins one.

## ⛔ Ask the profile; never compose the shape

⚠️ `audio/<clip>.mp3` is the `tree` profile's answer. Under `sibling` the same
clip is `audio/<stem>/<clip>.mp3`, because many units share one `study/`
directory there. ⛔ **The invariant that survives every profile is *relative to the page*,
not the literal string** — a renderer that spelled `audio/` would be correct
under one profile and silently wrong under the other, and the page would render
either way.

## ⭐ `AUDIO_ATTRIBUTE` is named here, one milestone before its writer

⛔ **The page renderer mints no speech id** — `html.py`'s own docstring is explicit that
*"two numbering schemes that agree today are exactly the coupling that breaks
silently tomorrow"*, and narration owns the derivation. ⚠️ But *the name of
the attribute* is a contract with two sides and only one of them exists yet,
and *a definition arriving after its first writer is a definition two writers
each guess at differently.*

⭐ **So the attribute is named, and nothing here writes one.** The player
and `page.document`'s player region both take the spelling from this constant,
which is what lets the region be *derived* — a page carries a player when its
body carries audio — instead of gated on a document field this milestone would
have had to invent.
"""

from __future__ import annotations

from collections.abc import Callable
from dataclasses import dataclass
from pathlib import PurePosixPath

from studyforge.corpus.placement import (
    UNIT_MEDIA_DIRNAMES,
    CorpusLocations,
    UnitLocations,
    relative_href,
)
from studyforge.describe import describe
from studyforge.render.page.errors import PageError
from studyforge.render.pageassets import SCRIPT_NAME, STYLESHEET_NAME
from studyforge.sourcepath import SOURCE_PATH_DESCRIBED, source_path_fault

#: The attribute a narrated element carries. ⛔ Written by narration and by
#: nothing else — named here so the two sides cannot spell it differently.
AUDIO_ATTRIBUTE = "data-audio"

#: How a media reference is recognised as pointing off this machine. ⚠️ The test
#: is for a scheme, not for a host: a lesson's own `media/x.mp4` and a remote
#: `https://…/x.mp4` are the two cases, and everything with `://` in it is the
#: second whatever it names.
REMOTE_MARK = "://"


@dataclass(frozen=True, slots=True)
class Placement:
    """Where one page sits, and where everything it links sits.

    ⭐ Locations and no profile: the profile has already answered by the time
    a page is rendered, so the renderer holds locations rather than the engine
    that computed them — which is what keeps `page/` free of any branch on a
    profile name.

    ⭐ `code` is the source suffixes the corpus's declared runtimes write, for a
    page that sits beside the corpus's files — `()` everywhere else — so a link
    to one of its code files can open in the editor (`page.code`). ⭐ `pairing`
    answers, for one such file, the source and the test it stands between, so a
    list of the page's code is drawn one example per pair; `None` pairs nothing.
    """

    corpus: str
    unit: UnitLocations
    shared: CorpusLocations
    code: tuple[str, ...] = ()
    pairing: Callable[[str], tuple[str | None, str | None]] | None = None

    def stylesheet(self) -> str:
        """How this page addresses the shared stylesheet."""
        return self._shared_asset(STYLESHEET_NAME)

    def script(self) -> str:
        """How this page addresses the shared script."""
        return self._shared_asset(SCRIPT_NAME)

    def media(self, kind: str, source: object) -> str:
        """How this page addresses one of its own media files.

        `source` is the archive's own relative path — `media/diagram.svg` — and
        what reaches the page is the *placed* copy, whose directory the profile
        chose. ⛔ Only the filename survives: the archive's directory is a fact
        about the archive, and a page that echoed it would address a file that
        placement never put there.
        """
        return self.unit.href(kind, filename(source))

    def _shared_asset(self, name: str) -> str:
        """One file from the corpus's shared asset directory, relative to this page."""
        return relative_href(self.unit.page, self.shared.assets / name)


def is_remote(source: object) -> bool:
    """Return whether a media reference points off this machine.

    ⭐ Consulted before `filename`, because a remote reference has no file on
    disk to address and must be offered as a link instead (R8: a page fetches
    nothing, so an embed is a link the reader chooses to follow).
    """
    return isinstance(source, str) and REMOTE_MARK in source


def filename(source: object) -> str:
    """Return the one filename a media reference names, or refuse describing it.

    ⛔ **The refusal never quotes the value** (R7). Every shape refused here is,
    by construction, a candidate home directory, and this runs over every media
    reference in a corpus — into a log.
    """
    if not isinstance(source, str) or not source.strip():
        raise PageError(
            "a media block names the file it shows, and this one names nothing; "
            "a figure with no source would render as an empty box"
        )
    fault = source_path_fault(source)
    if fault is not None:
        raise PageError(f"a media reference is {SOURCE_PATH_DESCRIBED} and this one is {fault}")
    return PurePosixPath(source).name


def media_kind(block_type: str) -> str:
    """Return the unit media directory one block type's file was placed in.

    ⛔ Derived from the profile's own vocabulary rather than spelled again: a
    directory named here and named differently in `placement.names` is a page
    whose figures all point at nothing, with no error anywhere.
    """
    kind = "images" if block_type == "image" else block_type
    if kind not in UNIT_MEDIA_DIRNAMES:
        # ⛔ The offending name is DESCRIBED, never echoed (R7, R6).
        # This branch fires precisely because the value is not one of a closed
        # set — which is the branch an absolute path arrives at — and this
        # function runs over every media block in a corpus, into a log.
        raise PageError(
            f"no unit media directory holds {describe(block_type)}; a unit's media are "
            f"{list(UNIT_MEDIA_DIRNAMES)}"
        )
    return kind
