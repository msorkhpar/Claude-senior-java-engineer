r"""The files a page shows or hands over — put where the page looks for them.

**What it does.** Copies each file a built page references from the archive
into the media directory the page addresses, minting that directory and no
other: a kind a unit has no files of gets no directory.

**How you use it.** `write_media(root, into)` for a corpus root;
`unit_media(corpus, into)` when the declarations have already been read;
`references(document)` when only *what a page will reach for* is wanted.

**Depends on.** `generate.declarations` for the corpus, `generate.writing` for
R3, `archive.layout.Layout` for where the archive keeps a unit's own files,
`archive.blocks` for the one recursion over the vocabulary, `corpus.placement`
for where the copy goes, `sourcepath` for what a reference may be, and
`unit.builder` for the document a page is rendered from. ⛔ It names no
source (R1).

## ⛔ Why this pass exists: the renderer copies nothing

⚠️ `render.page` emits `<img src="images/diagram.svg">` — the **placed** copy,
whose directory the placement profile chose — and puts no file there. Without
this pass a figure renders as the broken-image glyph, and the renderer's own
checks cannot see it, because they resolve hrefs against a tree they write.

## ⛔ The destination is ASKED, and the population is the RENDERER's

⭐ Every copy lands at `UnitLocations.media_dir(kind) / <basename>` — the two
calls `Placement.media` itself is made of — so the copy and the href agree by
construction rather than by two modules spelling `images/` the same way.

⚠️ **The three rules this module re-derives are `render.page`'s and are not on
its `__all__`** — what a remote reference looks like, which directory a block
type's file was placed in, and that only the basename survives. One exported home
(R21): a name that is not on the owner's surface is not shared, so it is
re-derived and **pinned behaviourally** — `tests/studyforge/generate/test_site.py`
asserts that every reference a built page emits resolves to a file this pass
wrote, on both fixtures. ⛔ A re-derivation that disagreed with the renderer
would leave the file beside the href, and that test is red the moment it does.

## ⛔ A file the corpus never fetched is NAMED, never invented

⚠️ `media_skipped` is a legal state: *"an ingest that named media and
deliberately did not fetch it"* (`archive.document`). `depth2`'s third unit is
that corpus, and its page reaches for a video and a diagram that are on no disk
anywhere. ⭐ **So the reference goes into `Written.missing` and the build carries
on.** Stopping would refuse to build a corpus that is legally incomplete; and
whether a build stops or drains is not this module's decision.

## ⛔ An ATTACHMENT is copied because the page LINKS it

⚠️ Spec C4's companion files — a dataset a lesson loads, a notebook — are
declared in the archive and shown by no block. ⭐ They are copied on exactly the
same ground as every other file here: **the page emitted a reference to them**,
`render.page.section` linked them, and this pass follows what a page emits. ⛔
The order matters and is R3's: `studyforge plan` declares the directory,
the renderer emits the link, and this copies into it — a copy with no plan line
breaks the agreement, and bytes no page links are a footprint the media limit weighs and
a reader can never reach.

## ⛔ A unit gets a media directory only for a kind it has files of

⚠️ **Git cannot track an empty directory**, so a directory minted for every
declared unit would make a built checkout differ from its clone. ⭐ A directory
is minted only when a copy fills it. ⛔ **Every declared
directory is still asked about** (`writing.stand`), so a reader's file standing
where one belongs is named whether or not anything would fill it (R3), and a
directory an earlier build left on disk is never removed.
"""

from __future__ import annotations

from collections.abc import Iterator
from dataclasses import dataclass
from pathlib import Path, PurePosixPath

from studyforge.archive.blocks import BLOCK_FIELDS, walk
from studyforge.archive.layout import Layout
from studyforge.corpus.placement import (
    ATTACHMENTS_DIRNAME,
    IMAGES_DIRNAME,
    UNIT_MEDIA_DIRNAMES,
    VIDEO_DIRNAME,
    UnitLocations,
)
from studyforge.generate.declarations import (
    BuildError,
    Corpus,
    UnitSource,
    declared_location,
    read_corpus,
    unit_location,
)
from studyforge.generate.writing import Written, copy, mint, stand
from studyforge.sourcepath import SOURCE_PATH_DESCRIBED, source_path_fault
from studyforge.unit.builder import build_unit

#: How a media reference is recognised as pointing off this machine. ⚠️ A scheme
#: and not a host: a lesson's own `media/x.mp4` and a remote `https://…/x.mp4`
#: are the two cases, and everything with `://` in it is the second.
REMOTE_MARK = "://"

#: The block types that carry a file, derived from the vocabulary rather than
#: listed. ⛔ A twelfth block type with a `src` is picked up here the day
#: `archive.blocks` grows one; a list of two names is not.
MEDIA_BLOCKS = tuple(name for name, fields in BLOCK_FIELDS.items() if "src" in fields)

#: The keys of a section's own deck that name a file on disk. ⚠️ `remote` and
#: `poster_remote` are provenance and are deliberately not among them — they are
#: the addresses the source served, and nothing on the page reaches for one.
DECK_KEYS = ("src", "poster")


@dataclass(frozen=True, slots=True)
class Reference:
    """One file a built page will reach for, and the directory it was placed in."""

    kind: str
    source: object


def write_media(root: Path | str, into: Path | str) -> Written:
    """Read one corpus and put its media where its pages look for it."""
    return unit_media(read_corpus(root), into)


def unit_media(corpus: Corpus, into: Path | str) -> Written:
    """Run the media pass over declarations that have already been read.

    ⚠️ **The unit documents are built again here rather than shared with the
    page pass**, and that is what makes this stage independently invocable — a
    reader replacing one lesson's image should not have to re-render the site.
    ⭐ It cannot drift: `build_unit` is a function of the bytes on disk, so the
    document this pass reads the references out of is the document the page was
    rendered from.
    """
    out = Path(into)
    layout = Layout(corpus.root)
    written: list[PurePosixPath] = []
    refused: list[PurePosixPath] = []
    replaced: list[PurePosixPath] = []
    missing: list[PurePosixPath] = []
    planned: list[tuple[PurePosixPath, Path]] = []
    for source in corpus.units:
        home = layout.unit_files(source.container.address, source.ordinal)
        document = build_unit(
            source.directory,
            declared_practices=source.declared_practices,
            mentions=source.mentions,
        )
        for target, origin in _copies(source, unit_location(corpus, source), home, document):
            if not origin.is_file():
                missing.append(target)
                continue
            planned.append((target, origin))
    # ⛔ A media directory is minted only where a copy lands; every declared one is still asked.
    filled = {target.parent for target, _ in planned}
    for at in _declared(corpus):
        for directory in at.directories:
            (mint if directory in filled else stand)(out, directory, refused)
    for target, origin in planned:
        if target.parent in refused:
            # ⭐ Already named: a reader's file stands where this directory belongs.
            continue
        copy(out, target, origin, written, refused, replaced, footprint=corpus.footprint)
    return Written(
        media=tuple(written),
        refused=tuple(refused),
        missing=tuple(missing),
        replaced=tuple(replaced),
    )


def references(document: dict) -> Iterator[Reference]:
    """Every file the page rendered from `document` will reach for, in reading order.

    ⛔ **Recursed with `archive.blocks.walk`**, which is the one recursion over
    the vocabulary — *"a consumer that names `quote` itself is the next
    `disclosure` waiting to be forgotten"*. An image inside a disclosure is a
    file the page shows.
    """
    for section in document.get("sections") or ():
        if not isinstance(section, dict):
            continue
        for block in walk(list(section.get("blocks") or ())):
            if not isinstance(block, dict) or block.get("type") not in MEDIA_BLOCKS:
                continue
            if _is_remote(block.get("src")):
                # ⭐ A remote file is offered as a link the reader chooses to
                # follow (R8), so there is nothing on disk to place.
                continue
            yield Reference(_kind(str(block.get("type"))), block.get("src"))
        yield from _deck(section.get("video"))
        yield from _attachments(section.get("attachments"))


def _attachments(attachments: object) -> Iterator[Reference]:
    """Yield every companion file this section's page links (spec C4).

    ⛔ **`local` and never `remote`**: `remote` is the address the source served,
    kept so a re-fetch is possible from the document alone, and nothing on the
    page reaches for one — the rule `DECK_KEYS` states one function along.

    ⚠️ **A `local` that is not a location inside the source is REFUSED here
    rather than skipped**, because `render.page.section` refuses it too: an
    attachment is a file the archive says it fetched, so the two halves have to
    agree about which files exist. ⛔ A remote `src` on a BLOCK is the different
    case and is skipped above — there the page renders a link the reader chooses
    to follow, and there is nothing on disk to place.
    """
    if not isinstance(attachments, list):
        return
    for entry in attachments:
        if isinstance(entry, dict):
            yield Reference(ATTACHMENTS_DIRNAME, entry.get("local"))


def _deck(video: object) -> Iterator[Reference]:
    """Yield the unit's own narrated video and its poster, when one was filed.

    ⛔ **Gated on `src` exactly as `render.page.section` gates the region it
    renders.** A poster with no video is a file no page reaches for, and copying
    it would put a byte in the tree that nothing addresses.
    """
    if not isinstance(video, dict) or not _named(video.get("src")):
        return
    for key in DECK_KEYS:
        value = video.get(key)
        if _named(value):
            yield Reference(VIDEO_DIRNAME, value)


def _copies(
    source: UnitSource, at: UnitLocations, home: Path, document: dict
) -> tuple[tuple[PurePosixPath, Path], ...]:
    """Where each of one unit's files comes from and goes, each destination once.

    ⛔ **Two different files that would be placed at one path is a REFUSAL**, and
    it is the one media defect that is otherwise silent: only the basename
    survives placement, so `media/a/plan.png` and `media/b/plan.png` render as
    the same href, and whichever was copied second would be shown for both.
    ⭐ The same file named twice — a deck and a `video` block are usually the
    same file — is not that, and is copied once.
    """
    held: dict[PurePosixPath, Path] = {}
    for reference in references(document):
        target = at.media_dir(reference.kind) / _filename(reference.source)
        origin = home / PurePosixPath(str(reference.source))
        if held.setdefault(target, origin) != origin:
            raise BuildError(
                f"the unit {source.ordinal} declared at {source.container.address.key!r} "
                f"names two different files that would both be placed as "
                f"{target.name!r} among its {reference.kind}"
            )
    return tuple(held.items())


def _declared(corpus: Corpus) -> Iterator[UnitLocations]:
    """Where every unit the corpus DECLARES puts its artifacts — material or not."""
    for _, container in corpus.maps:
        for unit in container.units:
            yield declared_location(corpus, container, unit)


def _kind(block_type: str) -> str:
    """Return the unit media directory one block type's file was placed in.

    ⚠️ `render.page.assets.media_kind`'s rule, re-derived — see this module's
    docstring — and it is the one place the plural is spoken: a page's images
    are filed under `images` and its videos under `video`.
    """
    kind = IMAGES_DIRNAME if block_type == "image" else block_type
    if kind not in UNIT_MEDIA_DIRNAMES:
        # ⛔ The offending name is not echoed (R7): this runs over every media
        # block in a corpus, into a log, and the branch that fires is the one
        # an absolute path arrives at.
        raise BuildError(
            f"a block names a file that no unit media directory holds; a unit's "
            f"media are {list(UNIT_MEDIA_DIRNAMES)}"
        )
    return kind


def _filename(source: object) -> str:
    """Return the one filename a media reference names, or refuse describing it.

    ⛔ **The refusal never quotes the value** (R7). Every shape refused here is,
    by construction, a candidate home directory.
    """
    if not _named(source):
        raise BuildError(
            "a media block names the file it shows, and this one names nothing; "
            "a figure with no source would render as an empty box"
        )
    fault = source_path_fault(str(source))
    if fault is not None:
        raise BuildError(f"a media reference is {SOURCE_PATH_DESCRIBED} and this one is {fault}")
    return PurePosixPath(str(source)).name


def _is_remote(source: object) -> bool:
    """Return whether a reference points off this machine."""
    return isinstance(source, str) and REMOTE_MARK in source


def _named(value: object) -> bool:
    """Return whether a record's field actually names a file."""
    return isinstance(value, str) and bool(value.strip())
