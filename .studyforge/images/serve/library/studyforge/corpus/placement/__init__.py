"""Placement: the map from a logical address to physical locations, per corpus.

**What it does.** Turns an address into paths — where a unit's page goes, its
audio, its images, its practice, where the shared assets and the archive live —
under whichever profile the corpus declared. It also defines the **identity
block** every generated artifact embeds, which is what makes any of it safe.

**How you use it.**

    from studyforge.corpus.placement import identity, profile_for

    where = profile_for(manifest.placement).unit(address, 7, "Streams", origin=origin)
    where.page          # 16-streams-api/study/basics.16-streams-api.unit-07-streams.unit.html
    where.href("audio", "07.mp3")   # relative to the page (R8)

**Depends on.** `studyforge.address` and `studyforge.version`. ⛔ **No
filesystem, no I/O.** This package answers "where would this go"; whether
anything is there is `corpus.discovery`'s question, and `studyforge plan` exists precisely
because the answer is computable before a single file is written.

## Location is data; identity is embedded

⛔ **R4 is the ruling this package implements.** The framework never infers
what a file *is* from where it sits: every generated artifact carries its
identity inside it, and discovery assembles a site from what each file says it
is. That is what makes "artifacts go wherever suits the material" a real
option rather than a slogan.

⚠️ **Identity survives a move; presentation need not.** A moved page is still
correctly identified, still appears in the contents, still resolves as the unit
it is — and renders unstyled and silent, because its stylesheet and its sibling
audio resolve **relative to the page** (R8). ⛔ Both are true and **neither is
a defect**: conflating them would force either absolute asset paths, breaking
the `file://` floor, or a fixed tree, breaking placement.

## The two profiles

| Profile | Pages go |
|---|---|
| `tree` | under one generated root, in directories spelling the address |
| `sibling` | in a `study/` directory beside the source file they were generated from |

⚠️ `tree` is for material with no layout worth preserving; `sibling` is for a
repository whose layout the reader already knows, which is what R3 requires.

⭐ **A third is added by registering it**, and no consumer changes: nothing
downstream names a profile, and `test_profile` asserts that of all of `src/`.

## Every generated page carries a real name

⛔ Never `index.html` — a scan reads names, and the one `index.html` a build
writes is the root index. ⭐ **The name is derived from identity, never from
the source filename**; `names.py` records why, and the `label=` argument is the
seam through which a corpus's own numbering becomes part of it the day an
adapter records one.

⛔ **Deterministic** (R10): no clock, no content hash, no dependence on the
order a filesystem enumerates.

## What is in the package

| Module | Owns |
|---|---|
| `profile` | the `Profile` contract, the registry, `origin_directory`, the ignore file |
| `tree` | the `tree` profile |
| `sibling` | the `sibling` profile |
| `names` | what every artifact is called, and why |
| `identity` | the block an artifact embeds, and reading it back |
| `locations` | the path sets and the ignore file a profile returns, and relative hrefs |
| `errors` | `PlacementError`, the only exception any of it raises |
"""

from __future__ import annotations

from studyforge.corpus.placement import identity
from studyforge.corpus.placement.errors import PlacementError
from studyforge.corpus.placement.identity import (
    IDENTITY_API,
    IDENTITY_ELEMENT_ID,
    IDENTITY_KEYS,
    KINDS,
    KNOWN_IDENTITY_API,
    Identity,
)
from studyforge.corpus.placement.locations import (
    ContainerLocations,
    CorpusLocations,
    IgnoreFile,
    UnitLocations,
    relative_href,
)
from studyforge.corpus.placement.names import (
    ARCHIVE_DIRNAME,
    ASSETS_DIRNAME,
    ATTACHMENTS_DIRNAME,
    AUDIO_DIRNAME,
    CONTAINER_SUFFIX,
    IGNORE_FILENAME,
    IMAGES_DIRNAME,
    PRACTICE_DIRNAME,
    RAW_DIRNAME,
    ROOT_INDEX_FILENAME,
    SITE_CACHE_FILENAME,
    STAGING_SUFFIX,
    STUDY_DIRNAME,
    UNIT_MEDIA_DIRNAMES,
    UNIT_SUFFIX,
    UNITS_DIRNAME,
    VIDEO_DIRNAME,
    container_page_name,
    is_container_page,
    is_unit_page,
    label_of,
    unit_page_name,
    unit_stem,
)
from studyforge.corpus.placement.profile import (
    CACHE_IGNORE_LINES,
    GENERATED_IGNORE_HOME,
    GENERATED_ROOT,
    SELF_IGNORE_LINE,
    Profile,
    cache_ignore_lines,
    origin_directory,
    profile_for,
    register,
    registered,
)
from studyforge.corpus.placement.sibling import SIBLING, SiblingProfile
from studyforge.corpus.placement.tree import TREE, TreeProfile

#: ⛔ The package's whole public surface.
__all__ = [
    "ARCHIVE_DIRNAME",
    "ASSETS_DIRNAME",
    "CACHE_IGNORE_LINES",
    "ATTACHMENTS_DIRNAME",
    "AUDIO_DIRNAME",
    "CONTAINER_SUFFIX",
    "GENERATED_IGNORE_HOME",
    "GENERATED_ROOT",
    "IDENTITY_API",
    "IDENTITY_ELEMENT_ID",
    "IDENTITY_KEYS",
    "IGNORE_FILENAME",
    "IMAGES_DIRNAME",
    "KINDS",
    "KNOWN_IDENTITY_API",
    "PRACTICE_DIRNAME",
    "RAW_DIRNAME",
    "ROOT_INDEX_FILENAME",
    "SELF_IGNORE_LINE",
    "SIBLING",
    "SITE_CACHE_FILENAME",
    "STAGING_SUFFIX",
    "STUDY_DIRNAME",
    "TREE",
    "UNITS_DIRNAME",
    "UNIT_MEDIA_DIRNAMES",
    "UNIT_SUFFIX",
    "VIDEO_DIRNAME",
    "ContainerLocations",
    "CorpusLocations",
    "Identity",
    "IgnoreFile",
    "PlacementError",
    "Profile",
    "SiblingProfile",
    "TreeProfile",
    "UnitLocations",
    "cache_ignore_lines",
    "container_page_name",
    "identity",
    "is_container_page",
    "is_unit_page",
    "label_of",
    "origin_directory",
    "profile_for",
    "register",
    "registered",
    "relative_href",
    "unit_page_name",
    "unit_stem",
]
