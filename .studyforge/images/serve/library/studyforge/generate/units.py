r"""One corpus root in, its unit pages on disk — the caller the renderer never had.

**What it does.** Builds each declared unit's served document from the material
an adapter wrote, places it under the corpus's own placement profile, renders
its page with the bar and the trail the contents document computes, and writes
the bytes.

**How you use it.** `write_pages(root, into)` for a corpus root;
`unit_pages(corpus, into)` when the declarations have already been read. ⛔
`into` is **required and has no default** — where a build's output goes is a
decision this module does not take.

**Depends on.** `generate.declarations` for the corpus, `generate.navigation`
for the join — the bar, the trail and the rail across containers —
`generate.containers` for where a crumb and a rail row point, `generate.writing`
for R3, `generate.narration` for what each page plays, `unit.builder` for the
document, `render.page` for the bytes, `execute.conventions` for which
suffixes are the corpus's code and `execute.codepair` for which source a test
stands beside. ⛔ It names no source (R1).

## ⛔ Why this module exists

It is the one module outside `render/` that calls the page renderer and writes
a unit page: the renderer, the builder, the placement profiles and the
navigation join meet here.

## ⚠️ Two things this module deliberately does NOT do, each for a reason

⛔ **No synthesis.** Each page's narration is `generate.narration`'s answer
over the record `studyforge narrate` wrote; a corpus with no record renders
`SILENT`, byte for byte the pre-narration page. A build never invokes synthesis
(R8).

⛔ **No authored overlay.** A unit that has one is built without it: this
module hands `build_unit` a unit's archive documents and nothing else.

⭐ **Where an overlay sits is declared:
`archive.layout.Layout.content(address, unit)` is the address**, so a build
has somewhere to look. ⛔ **Applying one is unowned** — reading the file, refusing its version,
composing it over the
archive's blocks and deciding what a conflict means are none of them decided,
and this module will not decide them by being the first caller. ⭐ A path
invented here would still be a second authority on the archive's shape; that
is why the address is asked for rather than composed.
"""

from __future__ import annotations

from collections.abc import Callable, Iterator
from pathlib import Path, PurePosixPath

from studyforge.corpus.placement import relative_href
from studyforge.execute import is_a_test, pairing, source_suffixes
from studyforge.generate.containers import page_paths
from studyforge.generate.declarations import Corpus, read_corpus, unit_location
from studyforge.generate.narration import clips_on_disk, narrated, narration_for
from studyforge.generate.navigation import bar, index_href, rail, trail
from studyforge.generate.writing import Written, place
from studyforge.render.page import Placement, render
from studyforge.unit.builder import build_unit


def write_pages(root: Path | str, into: Path | str) -> Written:
    """Render every unit that has material and write its page under `into`.

    ⛔ `into` is separate from `root` and required. A build that defaulted it to
    the corpus root would have decided, silently, that generated output belongs
    inside the material — which is the one placement question §5 gives to the
    corpus and not to the framework.
    """
    return unit_pages(read_corpus(root), into)


def unit_pages(corpus: Corpus, into: Path | str) -> Written:
    """Run the unit-page pass over declarations that have already been read."""
    out = Path(into)
    written: list[PurePosixPath] = []
    refused: list[PurePosixPath] = []
    replaced: list[PurePosixPath] = []
    for at, body in unit_bodies(corpus):
        place(out, at, body, written, refused, replaced, footprint=corpus.footprint)
    return Written(pages=tuple(written), refused=tuple(refused), replaced=tuple(replaced))


def unit_bodies(corpus: Corpus) -> Iterator[tuple[PurePosixPath, bytes]]:
    """Yield `(page path, rendered bytes)` for every unit with material, writing nothing.

    The narration record is read once, before the first page is rendered, so an
    unreadable record stops the pass before anything reaches disk.
    """
    state = narrated(corpus)
    on_disk = clips_on_disk(corpus, state)
    shared = corpus.shared
    absent = corpus.absent
    above = page_paths(corpus)
    # ⭐ The code a lesson may link: what the declared runtimes write (`page.code`).
    code = source_suffixes(corpus.manifest.runtimes)
    # ⭐ Which source a linked test stands beside: the corpus's code walked once.
    pairs = _pairs(corpus) if code else None
    for source in corpus.units:
        at = unit_location(corpus, source)
        document = build_unit(
            source.directory,
            declared_practices=source.declared_practices,
            mentions=source.mentions,
        )
        placement = Placement(
            corpus=corpus.manifest.source,
            unit=at,
            shared=shared,
            code=code if source.mentions.beside else (),
            pairing=pairs if source.mentions.beside else None,
        )
        body = render(
            document,
            placement,
            bar(corpus.contents, source.key, absent),
            trail(
                corpus.contents,
                source.key,
                index_href(corpus.contents, source.key),
                {key: relative_href(at.page, page) for key, page in above.items()},
            ),
            narration_for(corpus, source, at, document, placement, state, on_disk=on_disk),
            rail(
                corpus.contents,
                at.page,
                above,
                container=source.container.address.key,
                unit=source.key,
                absent=absent,
            ),
        )
        yield at.page, body


def _pairs(corpus: Corpus) -> Callable[[str], tuple[str | None, str | None]]:
    """Answer, for one code file, the source and the test it stands between.

    ⭐ `execute.pair`'s own answer, so an example's entry is the pair the served
    editor opens. ⚠️ A file it cannot pair is its own entry, a test or a source
    by its name.
    """
    found = pairing(corpus.root, corpus.manifest.runtimes)

    def answer(path: str) -> tuple[str | None, str | None]:
        pair = found(path)
        if pair is None:
            return (None, path) if is_a_test(path) else (path, None)
        return pair.source, pair.test

    return answer
