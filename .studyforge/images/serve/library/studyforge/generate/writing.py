r"""The one place a build puts bytes on disk, and the one place it refuses to.

**What it does.** Holds `Written` — what a pass put on disk and what it left
alone — and the three calls every pass writes through: `place` for bytes a pass
rendered, `copy` for a file it brings in from the archive, `mint` for a directory the
plan declares and a copy fills, and `stand` for one it declares and nothing fills.

**How you use it.** `place(out, at, body, written, refused, replaced,
footprint=…)`; `copy(out, at, source, written, refused, replaced,
footprint=…)`; `mint(out, at, refused)`; add the records two passes return
with `+`.

**Depends on.** `generate.footprint` for the line between the build's own
output and everybody else's, and the standard library.

## ⛔ R3 BY REFUSING, NOT BY REMEMBERING

**No path that already exists is written unless the plan declares it as this
build's own.** Every other target on disk is named in `Written.refused` and left
byte-for-byte alone; nothing is moved, renamed or deleted, and the only
directories minted are the ones a target needs.

## ⛔ THE REBUILD POLICY, AND WHY IT IS A FOOTPRINT RATHER THAN A MEMORY

⭐ **R3 distinguishes the build's own prior output from the user's material.** A
file the build wrote last time is not somebody's material; it is the build's own
previous answer, and replacing it is what *rebuilding after editing a lesson*
means. ⛔ Everything else stays protected absolutely and is refused **by name**.

⛔ **The line between the two is `generate.footprint.Footprint`, derived from
`studyforge plan`** — the enumeration that already exists, is goldened and is
asserted. ⚠️ **It is not a memory of what ran**: no digest is recorded, no
receipt is written into the output tree, and nothing here reads state an earlier
run left behind. A build asks *"does the plan declare this path as mine?"* and
nothing else.

## ⛔ THE DISCRIMINATION IS BY PATH, AND NEVER BY CONTENT

    a path the plan's enumeration NAMES    →  the build replaces it
    a path the enumeration does NOT name   →  refused, by name

⛔ **Nothing here opens an existing file to decide.** No digest, no comparison
against what the build would have written, no marker read back out of an
artifact. The only two questions asked of a target that exists are *what is its
path* and *is it a directory* — so the rule above is the whole rule, and a
reader can predict a build's conduct from `studyforge plan` alone.

⚠️ **The consequence, stated plainly rather than left to be discovered.** A
build has no prior content to compare against, so *"somebody edited this page by
hand"* is not a question it can ask. A hand-edited copy of a page the build
wrote is INSIDE the footprint and IS overwritten. ⭐ That is the decision and
not an oversight, and R19 already ruled it the right one: a hand-edit to a
generated artifact is *a finding, not a fix* — customisation enters as manifest
data, and the page is regenerated and committed again, so the diff of
the next build is where the lost edit shows.

⛔ **It follows that a file somebody put at a named path before any build ever
ran is replaced too**, on the FIRST build, with no prior output in existence.
⚠️ A hand-written `index.html` in an empty output directory does not
survive. ⭐ It is named in the report — a `replace` line,
never a silent `wrote` — and it is the price of a rule that needs no memory.

⛔ **A directory where a file belongs is still a refusal, and a file where a
directory belongs still is too.** Replacing this build's own file is a write;
removing a tree, or a file the build never wrote, is a deletion, and nothing
here deletes.

⭐ **One module, so the refusal cannot be forgotten by a pass added later.** A
second pass that opened a file itself would be a second R3 policy, and the one
that skipped the check would be the one nobody noticed. ⚠️ `copy` exists rather
than `place(out, at, source.read_bytes(), …)` for one reason and it is not
style: a lesson video is megabytes, and a build that read every one of them
into memory to hand them straight back out would be sized by the corpus.

## ⚠️ `missing` is a RECORD, not a policy

⭐ A pass may find that a corpus declares a file it never fetched —
`media_skipped` is exactly that state, and it is legal. ⛔ **So `Written.missing`
names it and the build carries on.** Whether a build should *stop* on one, or
drain them into a report, is a decision this module does not take: it is the
same shape as `refused`, which has named rather than raised since the first
pass existed.

## ⛔ A build mints its own pages and never its own output ROOT

⚠️ A writer that minted its own root would create a directory wherever a
relative path points — `tests/emission` calls every public callable with
filler arguments such as `alpha`, which would land **in the repository**. ⭐ A
relative output root resolves against whatever the process's working directory
happens to be, which is the one thing a build must never let decide where its
output lands. ⛔ So the root is the caller's to create and this refuses without
it.
"""

from __future__ import annotations

import os
import shutil
from dataclasses import dataclass
from pathlib import Path, PurePosixPath

from studyforge.generate.declarations import BuildError
from studyforge.generate.footprint import Footprint


@dataclass(frozen=True, slots=True)
class Written:
    """What one run put on disk, and what it refused to touch.

    ⭐ All of them are paths **relative to the output root**, exactly as
    placement named them — so a caller can diff them against `studyforge plan`
    without knowing where the run wrote.

    ⚠️ `pages`, `assets` and `media` are separate because they answer different
    questions: the plan enumerates pages one by one and both the shared bundle
    and a unit's media as *directories*, and a single list would make the second
    unaskable.

    ⭐ `missing` is the one entry that is not about this build's own conduct: a
    file the corpus's material names and its archive does not hold. It is
    reported rather than raised — see this module's docstring.

    ⛔ **`replaced` is a CROSS-CUTTING record, exactly like `refused`, and not a
    fourth category of output.** A replaced page is still one of `pages`, so
    `paths` stays the path-for-path diff against `studyforge plan` that
    R3 asks for whether the run was a first build or a rebuild. ⭐ It is
    reported separately because *"which of my files did this run overwrite"* is
    the question the rebuild policy owes an auditable answer to, and a report
    that said `wrote` for both would not be one.
    """

    pages: tuple[PurePosixPath, ...] = ()
    assets: tuple[PurePosixPath, ...] = ()
    media: tuple[PurePosixPath, ...] = ()
    refused: tuple[PurePosixPath, ...] = ()
    missing: tuple[PurePosixPath, ...] = ()
    replaced: tuple[PurePosixPath, ...] = ()
    #: ⭐ Clips a page plays that say words its paragraph no longer says, and
    #: clips an earlier narrated build copied here that no page of this one
    #: links. ⛔ Reported, never acted on: nothing is deleted.
    stale: tuple[PurePosixPath, ...] = ()
    unlinked: tuple[PurePosixPath, ...] = ()
    #: ⭐ One entry per link to a corpus file that a page of a site written
    #: outside the corpus root cannot reach, naming that page. ⛔ Reported,
    #: never acted on: no file is copied and no link climbs out of the site.
    unreached: tuple[PurePosixPath, ...] = ()
    #: ⭐ A file an earlier build wrote that no page of this framework reads any
    #: more (`generate.site.RETIRED`). ⛔ Reported, never acted on: nothing is
    #: deleted, and the report says how a person removes it.
    retired: tuple[PurePosixPath, ...] = ()

    def __add__(self, other: Written) -> Written:
        """Two passes' records, in the order the passes ran."""
        if not isinstance(other, Written):
            return NotImplemented
        return Written(
            pages=self.pages + other.pages,
            assets=self.assets + other.assets,
            media=self.media + other.media,
            refused=self.refused + other.refused,
            missing=self.missing + other.missing,
            replaced=self.replaced + other.replaced,
            stale=self.stale + other.stale,
            unlinked=self.unlinked + other.unlinked,
            unreached=self.unreached + other.unreached,
            retired=self.retired + other.retired,
        )

    @property
    def paths(self) -> tuple[PurePosixPath, ...]:
        """Every file this run actually wrote, page, bundle and media alike."""
        return self.pages + self.assets + self.media


def place(
    out: Path,
    at: PurePosixPath,
    body: bytes,
    written: list[PurePosixPath],
    refused: list[PurePosixPath],
    replaced: list[PurePosixPath],
    *,
    footprint: Footprint,
) -> None:
    """Write `body` at `at` under `out`, or name the path and leave it alone.

    ⛔ Refuses when `out` is not an existing directory — see this module's
    docstring. The refusal names neither the root nor the target (R7).

    ⛔ **`footprint` is required and keyword-only.** A pass added later cannot
    reach the write without answering *whose file is this*, and an omission is
    a `TypeError` at the call rather than a silent overwrite at a reader's.
    """
    if at in written:
        raise _written_twice(at)
    target = _under(out, at)
    if target.exists():
        if not _mine(target, at, footprint):
            # ⛔ R3: named and left alone, never opened for writing.
            refused.append(at)
            return
        replaced.append(at)
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(body)
    written.append(at)


def copy(
    out: Path,
    at: PurePosixPath,
    source: Path,
    written: list[PurePosixPath],
    refused: list[PurePosixPath],
    replaced: list[PurePosixPath],
    *,
    footprint: Footprint,
) -> None:
    """Copy `source` to `at` under `out`, or name the path and leave it alone.

    ⛔ **Content only: `copyfile` rather than `copy2`**, so the archive's
    *permissions* are not carried into the generated tree. A generated file gets
    the ordinary mode a new file gets, and in particular an archive file that
    arrived executable — a downloaded sample, a file restored from an archive
    that kept its bits — does not put an executable byte in a reader's
    repository. ⚠️ **The timestamp is NOT the reason**, and the sentence that
    said it was is wrong in the wrong direction: `copy2` would carry the
    archive's mtime, which is *more* stable across two builds than the wall
    clock `copyfile` leaves. R10 is about the bytes, and both spellings agree
    about those.
    """
    if at in written:
        raise _written_twice(at)
    target = _under(out, at)
    if target.exists():
        if not _mine(target, at, footprint):
            # ⛔ R3, the same refusal `place` makes and for the same reason.
            refused.append(at)
            return
        replaced.append(at)
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(source, target)
    written.append(at)


def _written_twice(at: PurePosixPath) -> BuildError:
    """Refuse a second write to one path in one run (clause 3).

    ⛔ **A file this run already wrote is never replaced by this run.** The
    footprint allows replacing the build's own output from an EARLIER run.
    Within one run, a second write at a path means two artifacts claim it, and
    whichever came second would silently erase the first. ⚠️ `studyforge build`
    refuses that from the plan before writing anything, so this line is the
    backstop for every other caller of a pass.
    """
    return BuildError(
        f"this run already wrote {at.as_posix()!r}, and a second artifact is placed at "
        f"the same path; two artifacts claim one path, which `studyforge plan` refuses "
        f"by name"
    )


def _mine(target: Path, at: PurePosixPath, footprint: Footprint) -> bool:
    """Whether an existing `target` is this build's own prior output to replace.

    ⛔ **A directory is never this build's own file**, whatever the plan says
    about the path: replacing one would mean removing a tree, and the rebuild
    policy is *overwrite what the build wrote*, not *delete what is in the way*.
    ⚠️ Checked before the footprint rather than after, so a directory standing
    where a page belongs is refused by name under both profiles.
    """
    if target.is_dir():
        return False
    return footprint.owns(at)


def mint(out: Path, at: PurePosixPath, refused: list[PurePosixPath]) -> None:
    """Create the directory `at` under `out`, or name it and leave it alone.

    ⚠️ **A directory is not a write and is not recorded as one.** `Written`
    enumerates files, and `studyforge plan` enumerates a media directory as a
    directory — so one that is already there is simply already there, and only a
    *file* sitting where a directory belongs is a refusal (R3).
    """
    if stand(out, at, refused):
        _under(out, at).mkdir(parents=True, exist_ok=True)


def stand(out: Path, at: PurePosixPath, refused: list[PurePosixPath]) -> bool:
    """Name a file standing where the directory `at` belongs, and create nothing.

    ⭐ **A directory nothing will be copied into is never minted**, since
    git cannot track an empty one and a built checkout would differ from its
    clone. ⛔ The R3 refusal is still owed there, so it is asked here. Returns
    whether the way is clear. ⚠️ A directory already on disk is never removed.
    """
    directory = _under(out, at)
    if directory.exists() and not directory.is_dir():
        refused.append(at)
        return False
    return True


def same_root(out: Path, root: Path) -> bool:
    """Whether the output root IS the corpus root, refusing one that is not a directory.

    ⭐ Asked of the filesystem, not of two spellings: `.` and an absolute path
    can name one directory, and a comparison of strings would copy a clip onto
    itself. ⛔ The refusal is `_under`'s, so it names neither root (R7).
    """
    _under(out, PurePosixPath())
    return os.path.samefile(out, root)


def _under(out: Path, at: PurePosixPath) -> Path:
    """Resolve one target under the output root, refusing a root that is not one.

    ⛔ The refusal names neither the root nor the target (R7).
    """
    if not out.is_dir():
        raise BuildError(
            "the output root is not a directory that already exists; a build mints "
            "its own pages and never its own root, because a relative one would "
            "resolve against whatever the caller's working directory happens to be"
        )
    return out / at
