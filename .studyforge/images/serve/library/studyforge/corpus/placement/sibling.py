"""`sibling` — artifacts land in a declared subdirectory beside their source file.

**What it does.** Places a unit's page in `<source directory>/study/`, and every
kind of its own files — audio, images, video, practice and attachments — in
`<source directory>/study/<kind>/<stem>/`, named from its container's address
and the unit's own stem (`names.contained_stem`).

**How you use it.** `profile_for("sibling")`.

**Depends on.** `profile`, `names`, `locations`.

## Why this profile exists

⭐ **It is what lets the framework enhance a repository instead of restructuring
it** (R3). A consuming corpus's reader already knows their way around
`16-streams-api/`; a generated site that moved their material somewhere else
would be a site they had to learn twice. So the page appears beside the
`README` it was made from, and the `README` is untouched.

## ⛔ Every name carries the container

Two containers whose series mirror each other in one source directory would
otherwise place two units at one path: every call correct and the pair wrong,
and a build replacing pages it had written in the same run. ⭐ The address makes
two containers' names distinct by construction, whatever their ordinals, titles
or labels. ⚠️ The cost: every page and media name of a `sibling` corpus carries
that prefix.

## ⛔ Beside the source file means beside it in `study/`, not loose in it

⚠️ **Read as *in the same directory, unqualified*, "beside the source file"
buries the material**: one source directory holds its sources, one page per
source **and** one media directory per source, interleaved in one listing, and
a source file at the repository root puts its page and its media at the
repository root. ⛔ Enhancing a repository and burying its material are not
the same thing.

⭐ **So one declared segment, `names.STUDY_DIRNAME`, holds everything this
profile writes into a source directory**, and that directory's own listing is
its own files plus one entry. ⛔ **The segment is declared once
and composed nowhere**: every consumer asks this profile, and the page,
the media and the container page all come back already carrying it.

## The media is under one directory per KIND, not one per unit

⛔ **Many units share one `study/`**, so a unit's clips cannot simply be
`audio/` — twenty units would collide in one folder and no clip could be told
from another. ⭐ The stem discriminates one level lower instead:
`study/audio/<stem>/`, one directory per kind and one per unit inside it. So a
source directory's `study/` lists its pages and at most five directories,
whatever the unit count.

⚠️ **The cost, stated rather than hidden: a unit's artifacts do not sort as
one contiguous run.** ⭐ The kinds are a listing somebody reads. ⛔ Deleting one
unit is one page plus one directory
per kind, and `UnitLocations.directories` is what names them — never a glob a
caller composes.

⚠️ **`origin` is required, and its absence is refused rather than guessed
around** (R6). "Beside the source file" has no answer for a unit with no source
file, and inventing a directory would put generated output somewhere the corpus
owner never agreed to (R3).
"""

from __future__ import annotations

from pathlib import PurePosixPath

from studyforge.corpus.placement.errors import PlacementError
from studyforge.corpus.placement.locations import ContainerLocations, IgnoreFile, UnitLocations
from studyforge.corpus.placement.names import (
    ATTACHMENTS_DIRNAME,
    AUDIO_DIRNAME,
    IGNORE_FILENAME,
    IMAGES_DIRNAME,
    PRACTICE_DIRNAME,
    STUDY_DIRNAME,
    UNCOMMITTED_DIRNAMES,
    UNIT_SUFFIX,
    VIDEO_DIRNAME,
    contained_stem,
    container_page_name,
)
from studyforge.corpus.placement.profile import Profile, origin_directory, register


class SiblingProfile(Profile):
    """Output in a declared subdirectory beside the source file it was generated from."""

    name = "sibling"
    describes = "each artifact in a study/ directory beside the source file it was generated from"

    def study_dir(self, origin, address, what: str = "artifact") -> PurePosixPath:
        """Return `<source directory>/study` — the one directory this profile writes into.

        ⛔ **Every path this profile answers with goes through here**,
        so the declared segment is joined in one place and no caller, no build
        and no test composes it. ⚠️ `origin` is refused before it is joined:
        `origin_directory` is what keeps a generated directory from being
        created outside the source root.
        """
        return origin_directory(origin, address, what) / STUDY_DIRNAME

    def unit(self, address, ordinal, title, *, origin=None, label=None) -> UnitLocations:
        """Where one unit's artifacts go, in the `study/` directory beside its own source file."""
        directory = self.study_dir(origin, address, "unit")
        stem = contained_stem(address, ordinal, title, label)
        return UnitLocations(
            page=directory / f"{stem}{UNIT_SUFFIX}",
            audio=directory / AUDIO_DIRNAME / stem,
            images=directory / IMAGES_DIRNAME / stem,
            video=directory / VIDEO_DIRNAME / stem,
            practice=directory / PRACTICE_DIRNAME / stem,
            attachments=directory / ATTACHMENTS_DIRNAME / stem,
        )

    def container(self, address, titles, *, origin=None) -> ContainerLocations:
        """Return the container's page, in the `study/` directory beside its own source file."""
        return ContainerLocations(
            page=self.study_dir(origin, address, "container") / container_page_name(titles)
        )

    def media_ignore_lines(self) -> tuple[str, ...]:
        """`study/audio/`, the clips' directory, unanchored because the material is.

        ⛔ **The clips and nothing else**, as under `tree`: `UNCOMMITTED_DIRNAMES`
        says why each other kind is committed.

        ⭐ **This is the cheapest half of a profile's ignore answer (R3).** Under this profile the
        generated names are the unit's own stem, so a corpus cannot enumerate
        them — one measured `sibling` build wrote 79 artifacts, 67 of them
        content-hash-named, and `content.exclude` refuses globs by design.
        `.gitignore` takes them, which is why the ignore declaration is where
        this belongs and `content` never was.

        ⚠️ **Unanchored, because the material is**: a corpus has one `study/`
        per source directory and no way to say in advance which directories
        those are. ⭐ **The rule is narrowed to limit that cost.** A bare stem
        suffix such as `*.audio/` would make git ignore a repository's own
        `lecture.audio/`, so the rule carries this profile's own declared
        segment in front of it and matches only inside a directory this
        framework writes.

        ⛔ **These lines have no single home** (`ignore_home`), so `ignore_file`
        refuses them; `media_ignore_files` is how they are written, one file in
        each `study/` directory that holds clips.
        """
        return tuple(f"{STUDY_DIRNAME}/{kind}/" for kind in UNCOMMITTED_DIRNAMES)

    def media_ignore_files(self, audio_directories) -> tuple[IgnoreFile, ...]:
        """One ignore file in each `study/` directory that holds clips: `audio/`.

        ⭐ **The home a single-file answer lacks.** A `study/` directory is
        generated, so a file inside it is this framework's to write, and git
        reads its rules relative to it: `audio/` there covers exactly
        `study/audio/<stem>/`, and nothing of the corpus's own. ⛔ The
        directories are asked of the caller, who holds them (a plan from its
        placed units, a pack from its narration record): this profile composes
        no path of its own. ⛔ A directory that is not `<source>/study/audio/…`
        is refused rather than guessed at.
        """
        homes: dict[PurePosixPath, None] = {}
        for directory in audio_directories:
            homes.setdefault(self._study_of(PurePosixPath(directory)), None)
        rules = tuple(f"{kind}/" for kind in UNCOMMITTED_DIRNAMES)
        return tuple(
            IgnoreFile(home=study / IGNORE_FILENAME, lines=rules) for study in sorted(homes)
        )

    @staticmethod
    def _study_of(directory: PurePosixPath) -> PurePosixPath:
        """Return the `study/` directory an audio directory lies in, or refuse the directory."""
        parts = directory.parts
        for at in range(1, len(parts)):
            if parts[at] == AUDIO_DIRNAME and parts[at - 1] == STUDY_DIRNAME:
                return PurePosixPath(*parts[:at])
        raise PlacementError(
            f"an audio directory of placement 'sibling' lies in "
            f"<source directory>/{STUDY_DIRNAME}/{AUDIO_DIRNAME}/, and this one does not"
        )

    def ignore_home(self) -> None:
        """None: this profile's media is enclosed by many generated directories, not one.

        ⚠️ A `study/` directory *is* generated and *does* enclose the media
        beneath it. ⛔ But there is one such directory **per source
        directory**, and an `IgnoreFile` has one home. ⭐ The answer for a
        policy that keeps clips out of git is `media_ignore_files`, one file in
        each `study/` directory that holds them. The root ignore file
        stays the one thing never edited (R3).
        """
        return None


SIBLING = register(SiblingProfile())
