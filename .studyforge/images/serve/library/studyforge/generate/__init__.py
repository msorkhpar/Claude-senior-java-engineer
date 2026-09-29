r"""Turning what an adapter wrote into the pages a reader opens.

**What it does.** Holds the framework's build — the walk from a corpus root to
files on disk. Today that is the **reading floor**: every unit page, one
container page per declared container, the root index, the shared bundle those
three link, and every file a page shows, copied out of the archive into the
directory the page addresses. Narration is READ here and never made: a page
plays the clips `studyforge narrate` recorded, names the ones it promised and
cannot find, and a corpus with no record renders exactly as before (R3, R8).
`write_narration(root, into)` re-runs that pass alone. Under
any output root but the corpus root the clips a page addresses are copied there
(R8); `write_clips(root, into)` is that copy alone.

**How you use it.**

    from studyforge.generate import write_site

    written = write_site(corpus_root, output_root)

`sources(root)` answers *which units have material and where*;
`read_corpus(root)` hands back everything a corpus declares, read once;
`write_pages(root, into)` runs the unit-page pass alone and
`write_media(root, into)` the media pass alone. ⛔ **A caller catches
`RAISES`**: `BuildError`, plus `PersonalDataLeak`, which travels through
untranslated — so a command over this package refuses a leak
instead of printing a traceback.

**Depends on.** the declaration, contents, placement, builder and renderer
packages. ⛔ Nothing here knows any source (R1), and nothing here is a command:
this is the library a command would call, not the command.

## ⛔ This package is a FLOOR, not the build pipeline

⚠️ **The build pipeline proper (`studyforge build` and its progress writer) is
larger than this.** What is here is the part that is genuinely true end
to end — a corpus goes in, a navigable site comes out, an unnarrated corpus stays
quiet, this build's own previous answer is replaced and everything else in the
output directory is refused by name (R3, and `footprint`'s contract for the line
between the two). ⛔ **It is deliberately
not a subcommand and not a flag**: registering an entry point is the command
line's alone, and adding one here would put a second declaration of the
build's surface in the tree.

⭐ **It sits beside `cli/`** rather than inside it so that the command line can adopt,
move or absorb it without a consumer having imported a command.

## What is in the package

| Module | Owns |
|---|---|
| `declarations` | `Corpus` — the manifest, the container maps, the tree, the walk |
| `footprint` | which paths under an output root are the build's own to replace |
| `writing` | `Written`, and the one call that makes R3 a refusal |
| `navigation` | the contents document joined to a page's bar and its trail |
| `units` | the unit-page pass |
| `narration` | which of the three narration states a page is in, read from the record |
| `containers` | the `*.section.html` pass, and where each one went |
| `media` | the media directories, and the copy that fills them |
| `clips` | narration clips copied under any output root but the corpus root |
| `site` | the whole floor: the passes, the index, the bundle and the media |

## ⚠️ Why this package is not called `build`

⛔ **The root ignore file carries a bare `build/` rule** — the ordinary Python
packaging artifact — **and a bare rule matches at every depth**, so a package of
that name under `src/` is untracked from the moment it is written and every
check that walks the tracked tree reads it as absent. ⭐ Named here because
`build` is the word every task document uses, so the next author reaches for it
first, and the failure is silent in the worst direction: a green floor over code
git never saw.
"""

from __future__ import annotations

from studyforge.archive.scrub import PersonalDataLeak
from studyforge.generate.clips import for_output, unit_clips, write_clips
from studyforge.generate.containers import container_pages, page_paths
from studyforge.generate.declarations import (
    BuildError,
    Corpus,
    UnitSource,
    containers,
    declared_location,
    declared_practices,
    read_corpus,
    read_manifest,
    sources,
    unit_location,
)
from studyforge.generate.footprint import Footprint, footprint_for
from studyforge.generate.media import Reference, references, unit_media, write_media
from studyforge.generate.narration import Renarrated, heard, write_narration
from studyforge.generate.navigation import ancestors, bar, deepest, index_href, rail, trail
from studyforge.generate.site import assets, root_index, write_site
from studyforge.generate.units import unit_pages, write_pages
from studyforge.generate.writing import Written

#: ⛔ **What a build lets out, as the tuple a command catches**.
#: ⭐ Every member is reached from `write_site` in
#: `tests/studyforge/generate/test_init.py`, so an unreachable one fails.
RAISES: tuple[type[Exception], ...] = (BuildError, PersonalDataLeak)

#: ⛔ The package's whole public surface. A consumer that has to import
#: `studyforge.generate.declarations` directly is a consumer this contract
#: failed — R17 makes `__init__.py` the
#: contract.
__all__ = [
    "BuildError",
    "Corpus",
    "Footprint",
    "RAISES",
    "Reference",
    "Renarrated",
    "UnitSource",
    "Written",
    "ancestors",
    "assets",
    "bar",
    "container_pages",
    "containers",
    "declared_location",
    "declared_practices",
    "deepest",
    "footprint_for",
    "for_output",
    "heard",
    "index_href",
    "page_paths",
    "rail",
    "read_corpus",
    "read_manifest",
    "references",
    "root_index",
    "sources",
    "trail",
    "unit_clips",
    "unit_location",
    "unit_media",
    "unit_pages",
    "write_clips",
    "write_media",
    "write_narration",
    "write_pages",
    "write_site",
]
