r"""Every page a reader navigates between, from one corpus root, in one pass.

**What it does.** Reads the declarations once and runs three writers over them —
the unit pages, the container pages, and the root index — then writes the shared
stylesheet and script the three of them link, and puts each unit's media where
its page looks for it — narration clips included, under any output root but the
corpus root (`generate.clips`).

**How you use it.**

    from studyforge.generate import write_site

    written = write_site(corpus_root, output_root)
    written.pages      # every page, relative to the output root
    written.assets     # the shared bundle
    written.media      # every file a page shows, copied out of the archive or narrate's
    written.replaced   # every path that held this build's own previous answer
    written.refused    # every other target on disk, left byte-for-byte alone
    written.missing    # every file the material names and the archive has not
    written.unreached  # a page, once per corpus-file link it cannot reach from here

**Depends on.** `generate.declarations`, `.units`, `.containers`, `.navigation`
and `.writing`, plus `contents` for the local status and `render.index` for the
index's bytes. ⛔ Nothing here knows any source (R1) and nothing here is a
command: this is the library a command would call.

## ⭐ What this writes is exactly the HTML `studyforge plan` declares

⛔ **R3's clause is that the plan and the build agree PATH FOR PATH**, and
the goldens under `tests/fixtures/golden/` are what `plan` says. Every `.html`
line in them is written here — one root index, one page per container map, one
page per unit with material — and nothing else is; every `…/<unit>/<kind>/` line
is minted by the media pass, for every unit the corpus *declares*.

⚠️ **The plan's two remaining `create` lines are NOT this pass's**, and each has
an owner:

| planned path | who writes it |
|---|---|
| `…/archive/` | the **adapter** (R2) — a build reads it and never writes it |
| `…/site.json` | `corpus.discovery`'s cache, *"never the authority"* |

## ⛔ The contents documents are BUILT and NOT WRITTEN — a seam, not an omission

⚠️ **`toc.json` and `status.json` appear in no plan golden and in no placement
profile.** `CorpusLocations` names four paths and neither of them is among them,
so a build that wrote them would create two paths the plan never declared —
failing the very clause above — at a location this package would have had to
invent. ⭐ Both documents are therefore values here, handed straight to
`render.index.from_contents`, which is the shipped assembler and takes them as
records rather than as files. ⛔ Where they finally sit is a placement decision
and it is not this module's.

## ⚠️ The index is built from what THIS build wrote, not from what is on disk

⭐ `contents.status` is asked with the units that have material, so a unit the
corpus declares and nobody has generated is listed and marked unreadable — §7's
three states — instead of being omitted or linked to nothing. ⛔ A scan of the
output directory would have said something different the moment a reader deleted
a page, which is a reading of the *reader's* disk and not of the corpus.
"""

from __future__ import annotations

from pathlib import Path, PurePosixPath

from studyforge.contents import status
from studyforge.generate.clips import files_unreached, for_output, unit_clips
from studyforge.generate.containers import container_pages, page_paths
from studyforge.generate.declarations import Corpus, read_corpus
from studyforge.generate.media import unit_media
from studyforge.generate.narration import voiced
from studyforge.generate.navigation import rail
from studyforge.generate.units import unit_pages
from studyforge.generate.writing import Written, place
from studyforge.render.index import Placement as IndexPlacement
from studyforge.render.index import from_contents
from studyforge.render.index import render as render_index
from studyforge.render.pageassets import written_files


def write_site(root: Path | str, into: Path | str, *, narration: bool | None = None) -> Written:
    """Build one corpus's whole reading floor under `into`.

    ⛔ `into` is separate from `root` and required, for the reason
    `units.write_pages` gives: where generated output goes is the corpus's
    decision and not the framework's, and no default may take it silently.
    ⭐ `narration` overrides `corpus.json`'s `narration` for this build;
    `None` keeps the corpus's own answer. Off copies no clip and
    deletes none.
    """
    corpus = for_output(voiced(read_corpus(root), narration), into)
    return (
        unit_pages(corpus, into)
        + container_pages(corpus, into)
        + root_index(corpus, into)
        + assets(corpus, into)
        + unit_media(corpus, into)
        + unit_clips(corpus, into)
        + files_unreached(corpus)
        + retired(corpus, into)
    )


#: Files an earlier build wrote into the shared asset directory that no page
#: reads any more. ⭐ `narration-clips.js` told a page whether its clips were
#: here, and went stale whenever anything but its writer moved them; a page now
#: asks its first clip itself (`narration-probe.js`).
RETIRED = ("narration-clips.js",)


def retired(corpus: Corpus, into: Path | str) -> Written:
    """Name each retired file an earlier build left in this site's shared asset directory.

    ⛔ **Named, never deleted** (R3): the report says how a person removes it,
    as it does for a clip no page links.
    """
    found = [
        corpus.shared.assets / name
        for name in RETIRED
        if (Path(into) / Path(str(corpus.shared.assets / name))).is_file()
    ]
    return Written(retired=tuple(found))


def root_index(corpus: Corpus, into: Path | str) -> Written:
    """Write the single page a reader opens by double-clicking it."""
    where = IndexPlacement(shared=corpus.shared)
    local = status(corpus.contents, corpus.present)
    written: list[PurePosixPath] = []
    refused: list[PurePosixPath] = []
    replaced: list[PurePosixPath] = []
    place(
        Path(into),
        where.shared.root_index,
        render_index(
            from_contents(corpus.contents, local, where),
            where,
            # ⭐ The first page carries the rail too.
            rail(
                corpus.contents,
                where.shared.root_index,
                page_paths(corpus),
                absent=corpus.absent,
            ),
        ),
        written,
        refused,
        replaced,
        footprint=corpus.footprint,
    )
    return Written(pages=tuple(written), refused=tuple(refused), replaced=tuple(replaced))


def assets(corpus: Corpus, into: Path | str) -> Written:
    """Write the shared stylesheet and script every page of the site links.

    ⛔ **Asked of `render.pageassets` as one call**, never assembled here: the
    two names and the two bodies come from `written_files()` together, so a
    build cannot write one under the other's name.
    """
    out = Path(into)
    directory = corpus.shared.assets
    written: list[PurePosixPath] = []
    refused: list[PurePosixPath] = []
    replaced: list[PurePosixPath] = []
    for filename, body in sorted(written_files().items()):
        place(
            out,
            directory / filename,
            body.encode("utf-8"),
            written,
            refused,
            replaced,
            footprint=corpus.footprint,
        )
    return Written(assets=tuple(written), refused=tuple(refused), replaced=tuple(replaced))
