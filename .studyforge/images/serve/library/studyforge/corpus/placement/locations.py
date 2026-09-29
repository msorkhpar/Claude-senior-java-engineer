"""What a profile answers with: the paths one address maps to.

**What it does.** Holds the location sets — for a unit, for a container, for
the corpus as a whole — plus the one relative-href computation every page
needs.

**How you use it.** A profile returns these; `studyforge plan` prints them,
the page renderer writes to them, and `href_from` turns two of them into a link.

**Depends on.** `names` and `errors`. ⛔ No filesystem: every path here is a
`PurePosixPath` **relative to the source root**, and whether it exists is
`corpus.discovery`'s question.

⛔ **Relative to the source root, always, and never absolute.** An absolute
path in a plan or a page carries the user's home directory (R7), and a corpus
that only builds at one location is a corpus that cannot be cloned.

## Delivery is orthogonal to placement

⛔ **An href never encodes how a file arrived.** A generated artifact is
addressed relative to the page that references it, and that address is the same
whether the file was generated locally, committed, or restored from somewhere
else. ⚠️ The extraction source proved this in reverse and expensively: when
11.7 GiB of media left git for release assets, **the layout on disk did not
move and every page still addressed a clip as plain `audio/<clip>.mp3`** —
which is the only reason that change was a script rather than a re-render of
1,290 pages.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import PurePosixPath

from studyforge.corpus.placement.errors import PlacementError
from studyforge.corpus.placement.names import UNIT_MEDIA_DIRNAMES
from studyforge.describe import describe


@dataclass(frozen=True, slots=True)
class UnitLocations:
    """Every path one unit's artifacts occupy, relative to the source root."""

    page: PurePosixPath
    audio: PurePosixPath
    images: PurePosixPath
    video: PurePosixPath
    practice: PurePosixPath
    #: ⭐ The files the page LINKS rather than shows — spec C4's
    #: attachments. ⚠️ One field per kind, each named exactly as its directory
    #: is, because `media_dir` asks for one by that name.
    attachments: PurePosixPath

    @property
    def directories(self) -> tuple[PurePosixPath, ...]:
        """The directories a build creates for this unit, in a stated order.

        ⛔ **Asked kind by kind, never listed a second time**. The
        footprint weighs exactly these and a profile's ignore rules cover
        exactly `UNIT_MEDIA_DIRNAMES`; a kind that reached one list and not the
        other would be a limit cleared by not looking, which is the property
        `corpus.media.footprint` says its verdict rests on.
        """
        return tuple(self.media_dir(kind) for kind in UNIT_MEDIA_DIRNAMES)

    def media_dir(self, kind: str) -> PurePosixPath:
        """Return the directory of one kind of this unit's media."""
        try:
            return getattr(self, kind) if kind in UNIT_MEDIA_DIRNAMES else _refuse(kind)
        except AttributeError:
            return _refuse(kind)

    def href(self, kind: str, filename: str) -> str:
        """How this unit's own page addresses one of its media files.

        ⭐ Relative to the page, so the page works over `file://` with no
        server and no rewriting (R8) — and so moving the whole unit moves the
        link with it.

        ⛔ **Ask, never compose.** `audio/<clip>.mp3` is the `tree` shape; under
        `sibling` the same clip is `audio/<stem>/<clip>.mp3`, because many
        units share one `study/` directory there. The invariant that survives every
        profile is "relative to the page", not the literal string — and a
        renderer that hardcoded the string would be correct under one profile
        and silently wrong under the other.
        """
        if not isinstance(filename, str) or not filename or "/" in filename:
            raise PlacementError(f"a media href names one file, got {describe(filename)}")
        return relative_href(self.page, self.media_dir(kind) / filename)


@dataclass(frozen=True, slots=True)
class ContainerLocations:
    """Where a container's own page sits."""

    page: PurePosixPath


@dataclass(frozen=True, slots=True)
class CorpusLocations:
    """The paths that exist once per corpus, whatever its addresses are."""

    root_index: PurePosixPath
    assets: PurePosixPath
    archive: PurePosixPath
    site_cache: PurePosixPath

    @property
    def directories(self) -> tuple[PurePosixPath, ...]:
        """The directories a build creates once per corpus, in a stated order."""
        return (self.assets, self.archive)


#: The first line of every ignore file a profile answers with. ⚠️ It does not
#: name the media policy as the file's only source: the file also
#: carries the rules for this framework's own caches, and a corpus with media
#: committed has a file that carries nothing else.
IGNORE_HEADER = "# Written by studyforge. Regenerate, never edit."


@dataclass(frozen=True, slots=True)
class IgnoreFile:
    """The ignore rules a build requires, and the file inside a generated directory holding them.

    ⛔ `home` is never the repository's root ignore file (R3). `lines` are
    relative to `home`'s own directory, which is how git reads a nested file.
    """

    home: PurePosixPath
    lines: tuple[str, ...]

    def text(self) -> str:
        """Return the file's content: who wrote it, then one rule per line."""
        return "".join(f"{line}\n" for line in (IGNORE_HEADER, *self.lines))


def relative_href(from_page: PurePosixPath, to_target: PurePosixPath) -> str:
    """Return how a page at `from_page` addresses `to_target`.

    ⛔ Both are relative to the source root and the answer is relative to the
    page — never rooted, never absolute. A rooted href would work under a
    server and break the moment the page was opened from a file.
    """
    # ⛔ The refusal DESCRIBES the offending path and never formats it in
    # (R7). This branch fires precisely because the value is
    # absolute, so echoing it would emit a home directory out of the check
    # written to prevent one — the same defect `origin_directory` argues
    # against three modules away, and it was here the whole time.
    for name, path in (("the page", from_page), ("its target", to_target)):
        if path.is_absolute():
            raise PlacementError(
                f"{name} must be a path relative to the source root, and this one is "
                f"absolute; an href built from it would break the moment the page was "
                f"opened from a file"
            )
    here = from_page.parent.parts
    there = to_target.parts
    shared = 0
    while shared < len(here) and shared < len(there) - 1 and here[shared] == there[shared]:
        shared += 1
    up = [".."] * (len(here) - shared)
    return "/".join([*up, *there[shared:]])


def _refuse(kind: object) -> PurePosixPath:
    """Refuse a media kind this unit does not have, naming the ones it does."""
    raise PlacementError(
        f"a unit's media kind must be one of {list(UNIT_MEDIA_DIRNAMES)}, got {describe(kind)}"
    )
