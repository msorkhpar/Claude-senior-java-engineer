"""`tree` — all output under one generated root, mirroring the address.

**What it does.** Places every artifact under `.studyforge/`, in directories
that spell the address, with the extraction source's per-unit shape below that.

**How you use it.** `profile_for("tree")`.

**Depends on.** `profile`, `names`, `locations`.

## What "reproduces the extraction source's shape" does and does not mean

⭐ **The shape below the container is identical, segment for segment** —
`units/unit-NN/` with one directory per media kind inside it — so a later
migration moves a tree rather than re-deriving one, and every href a page holds
to its own media is unchanged.

⛔ **The page's filename is not `index.html`, and cannot be.** §5 rules that
every generated page carries a real name, *because a scan reads names* — and
an `index.html` is neither unique in a listing nor distinguishable from the
root index. So a unit page is `*.unit.html`, and the directories are what
this profile keeps as they are.

⚠️ **`tree` needs no `origin`.** A corpus whose container map records none can
still be placed this way, which is what makes it the profile for material that
has no source layout worth preserving.
"""

from __future__ import annotations

from pathlib import PurePosixPath

from studyforge.address import Address, unit_name
from studyforge.corpus.placement.locations import ContainerLocations, UnitLocations
from studyforge.corpus.placement.names import (
    ATTACHMENTS_DIRNAME,
    AUDIO_DIRNAME,
    IMAGES_DIRNAME,
    PRACTICE_DIRNAME,
    UNCOMMITTED_DIRNAMES,
    UNITS_DIRNAME,
    VIDEO_DIRNAME,
    container_page_name,
    unit_page_name,
)
from studyforge.corpus.placement.profile import (
    GENERATED_IGNORE_HOME,
    GENERATED_ROOT,
    Profile,
    register,
)


class TreeProfile(Profile):
    """All output under one generated root."""

    name = "tree"
    describes = "all output under one generated root, in directories that spell the address"

    def container_dir(self, address: Address) -> PurePosixPath:
        """`.studyforge/<segment>/<segment>/…` — the address as directories."""
        return PurePosixPath(GENERATED_ROOT, *address.segments)

    def unit_dir(self, address: Address, ordinal: int) -> PurePosixPath:
        """`…/units/unit-NN` — the extraction source's shape, kept."""
        return self.container_dir(address) / UNITS_DIRNAME / unit_name(ordinal)

    def unit(self, address, ordinal, title, *, origin=None, label=None) -> UnitLocations:
        """Where one unit's artifacts go. ⚠️ `origin` is accepted and unused."""
        del origin
        base = self.unit_dir(address, ordinal)
        return UnitLocations(
            page=base / unit_page_name(ordinal, title, label),
            audio=base / AUDIO_DIRNAME,
            images=base / IMAGES_DIRNAME,
            video=base / VIDEO_DIRNAME,
            practice=base / PRACTICE_DIRNAME,
            attachments=base / ATTACHMENTS_DIRNAME,
        )

    def container(self, address, titles, *, origin=None) -> ContainerLocations:
        """Return the container's page, in the directory that spells its address."""
        del origin
        return ContainerLocations(page=self.container_dir(address) / container_page_name(titles))

    def media_ignore_lines(self) -> tuple[str, ...]:
        """`**/audio/`, the clips' directory, in the generated root's own ignore file.

        ⛔ **The clips and nothing else** (`UNCOMMITTED_DIRNAMES` says why each
        other kind is committed). ⛔ **Scoped by where it lives, and that is not
        tidiness.** `audio` is a word a real repository uses for its own
        material; an unanchored `audio/` in the root ignore file would tell git
        to ignore the corpus's own recordings. The line lives in `ignore_home`,
        so git applies it below `.studyforge/` and nowhere else.
        """
        return tuple(f"**/{kind}/" for kind in UNCOMMITTED_DIRNAMES)

    def ignore_home(self) -> PurePosixPath:
        """`.studyforge/.gitignore`: every clip this profile places is below the generated root."""
        return GENERATED_IGNORE_HOME


TREE = register(TreeProfile())
