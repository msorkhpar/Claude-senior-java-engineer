r"""The `*.section.html` page a reader lands on navigating downward, written.

**What it does.** Renders one container page per container map the corpus
declares, listing that container's units in declared order and linking the ones
this machine has material for.

**How you use it.** `container_pages(corpus, into)`; `page_paths(corpus)` when
only *where they went* is wanted — which is what the breadcrumb join needs.

**Depends on.** `generate.declarations` for the corpus, `corpus.placement` for
where a page goes and for the relative-href arithmetic, `generate.navigation`
for the rail that reaches the other containers, and `render.container` for the
bytes. ⛔ It composes no path itself.

## ⛔ An unbuilt unit is listed WITHOUT a link, and that is a state not a fault

⚠️ `render.container` draws the line itself: `Item(href=None)` is §7's *declared
but not present*, and an href that is present and unusable **raises**, because
this page's list is the page rather than chrome. ⭐ So the caller has to know
which units have material, and it does: `Corpus.present` is the same set the
root index and the between-units bar are built from, so all three agree by
construction rather than by three walks agreeing.

## ⚠️ Only the addresses a container map is written at get a page

⛔ **An intermediate level has none.** A two-level corpus declaring
`basics/01-getting-started` has a container map at that address and none at
`basics`, so `basics` is a group in the contents document with no page — which
is why `page_paths` is a mapping a crumb may miss rather than a lookup that
raises. ⭐ `studyforge plan`'s goldens say the same thing: one `create …
.section.html` line per container map, and none above.
"""

from __future__ import annotations

from pathlib import Path, PurePosixPath

from studyforge.corpus.container import Container, Unit
from studyforge.corpus.placement import ContainerLocations, PlacementError, relative_href
from studyforge.generate.declarations import BuildError, Corpus, declared_location
from studyforge.generate.navigation import rail
from studyforge.generate.writing import Written, place
from studyforge.render.container import Document, Item, PageError
from studyforge.render.container import Placement as ContainerPlacement
from studyforge.render.container import render as render_container
from studyforge.unit import listed_numbering, without_outline_number


def container_pages(corpus: Corpus, into: Path | str) -> Written:
    """Write one page per declared container, refusing every path already there."""
    out = Path(into)
    shared = corpus.shared
    written: list[PurePosixPath] = []
    refused: list[PurePosixPath] = []
    replaced: list[PurePosixPath] = []
    above = page_paths(corpus)
    for _, container in corpus.maps:
        at = _location(corpus, container)
        placement = ContainerPlacement(corpus=corpus.manifest.source, container=at, shared=shared)
        place(
            out,
            at.page,
            render_container(
                _document(corpus, container, at),
                placement,
                rail=rail(
                    corpus.contents,
                    at.page,
                    above,
                    container=container.address.key,
                    absent=corpus.absent,
                ),
            ),
            written,
            refused,
            replaced,
            footprint=corpus.footprint,
        )
    return Written(pages=tuple(written), refused=tuple(refused), replaced=tuple(replaced))


def page_paths(corpus: Corpus) -> dict[str, PurePosixPath]:
    """`container address key -> where its page goes`, for every declared container."""
    return {
        container.address.key: _location(corpus, container).page for _, container in corpus.maps
    }


def _location(corpus: Corpus, container: Container) -> ContainerLocations:
    """Where one container's page goes, asked of the corpus's own profile."""
    try:
        return corpus.profile.container(
            container.address, container.titles, origin=container.origin
        )
    except PlacementError as error:
        raise BuildError(
            f"the container {container.address.key!r} cannot be placed: {error}"
        ) from None


def _document(corpus: Corpus, container: Container, at: ContainerLocations) -> Document:
    """One container as its own page renders it.

    ⛔ `level` is the corpus's own word for the depth a container map sits at,
    and that is always the **deepest** one: `corpus.container.parse` calls
    `Address(...).require_depth(manifest.depth)`, so a map at any other depth is
    refused before it reaches here. ⭐ `levels[-1]` and `levels[depth - 1]` are
    therefore the same expression, and the first is written because the second
    implies a variation this contract does not permit. ⚠️ The refusal is what makes it true, so it
    is asserted rather
    than assumed.
    """
    present = corpus.present
    try:
        return Document(
            address=container.address,
            title=without_outline_number(container.titles[-1]),
            variant=container.variant,
            level=corpus.manifest.levels[-1],
            note=container.note,
            items=tuple(
                Item(
                    numbering=listed_numbering(unit.numbering, unit.n),
                    title=without_outline_number(unit.title),
                    href=_href(corpus, container, unit, at)
                    if container.address.unit_key(unit.n) in present
                    else None,
                )
                for unit in container.units
            ),
        )
    except PageError as error:
        raise BuildError(
            f"the container {container.address.key!r} will not render: {error}"
        ) from None


def _href(corpus: Corpus, container: Container, unit: Unit, at: ContainerLocations) -> str:
    """How this container's page addresses one of its units' pages."""
    target = declared_location(corpus, container, unit)
    return relative_href(at.page, target.page)
