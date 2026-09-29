r"""Every link a unit's page carries leads somewhere, or the unit is named.

**What it does.** Builds each unit's served document the way a build does and
reads every relative link in its prose. A link that climbs out of the corpus,
names a file that is not there, or names an anchor that is none of the page's
headings is a `link-unresolved` finding against the unit.

**How you use it.** `check_links_resolve(walk)`, yielding `Finding`s and
`Unchecked`s like every other check.

**Depends on.** `generate` for the corpus as a build reads it, `unit.builder`
for the served document, `unit.headings` for the ids its headings are given,
`validate.corpus` and `validate.report`. ⚠️ Imported inside the check, as
`validate.narration` imports them, so reading a report never loads a build.

## ⛔ It reads what is SERVED, never what the source wrote

⭐ `unit.mentions` serves a link to another unit as that unit's page, a link to
a corpus file from the page and a source anchor as the page's own id. So what is
left is what a reader would click and find nothing: that is the finding, and a
link the build resolves is never one. ⚠️ A unit's page is read as sitting
where its placement profile puts it under the corpus root, which is where
`studyforge build <root> --out <root>` writes it.

## ⛔ The finding names the unit and the link's words, never the href

⚠️ An href is a path, and the shape refused here (`/home/…`, `../../…`) is
exactly the shape that carries a home directory (R7). The finding names the
unit by its key and title and the link by its label, which is prose.
"""

from __future__ import annotations

import re
from collections.abc import Iterator
from pathlib import Path, PurePosixPath
from urllib.parse import unquote

from studyforge.archive.blocks import CONTAINER_TYPES
from studyforge.archive.errors import ArchiveError
from studyforge.validate.corpus import Walk
from studyforge.validate.report import Finding, Unchecked

#: A relative link a reader would follow to nothing.
RULE_LINK_UNRESOLVED = "link-unresolved"

#: A link as the archive writes one, and the code span nothing inside is a link.
_LINKS = re.compile(r"`[^`]+`|\[(?P<label>[^\]\n]+)\]\((?P<href>[^)\s]*)\)")

#: An href that carries a scheme, and so names no file of the corpus.
_SCHEME = re.compile(r"^[A-Za-z][A-Za-z0-9+.-]*:")


def check_links_resolve(walk: Walk) -> Iterator[Finding | Unchecked]:
    """Every relative link on every unit's page leads to a file, a page or a heading.

    ⭐ **One finding per link**, filed against the unit's archive directory and
    naming the unit, so a corpus with ten dead links reads as ten.
    """
    from studyforge.generate import RAISES, read_corpus, unit_location
    from studyforge.unit import headings
    from studyforge.unit.builder import build_unit
    from studyforge.unit.errors import ContentError

    try:
        corpus = read_corpus(walk.root)
    except RAISES:
        yield Unchecked(
            RULE_LINK_UNRESOLVED,
            ".",
            "the corpus did not read the way a build reads it, so no link was followed",
        )
        return
    pages = {unit_location(corpus, source).page for source in corpus.units}
    for source in corpus.units:
        at = _where(source.directory, walk.root, source.key)
        try:
            document = build_unit(
                source.directory,
                declared_practices=source.declared_practices,
                mentions=source.mentions,
            )
        except (*RAISES, ContentError, ArchiveError):
            yield Unchecked(
                RULE_LINK_UNRESOLVED, at, "the unit did not build; no link was followed"
            )
            continue
        page = unit_location(corpus, source).page
        ids = {heading.anchor for heading in headings(document.get("sections"))}
        origin = None if source.origin is None else PurePosixPath(source.origin)
        for label, href in links(document.get("sections")):
            wrong = _unresolved(href, page, ids, pages, walk.root)
            if wrong == NOT_THERE and origin is not None:
                wrong = _as_written(href, origin)
            if wrong is not None:
                yield Finding(
                    RULE_LINK_UNRESOLVED,
                    at,
                    f"unit {source.key} ({source.title}) links '{label}' to {wrong}; the "
                    f"page would lead a reader nowhere. Link a file the corpus holds, "
                    f"relative to this unit's origin, or a heading of this unit.",
                )


def links(sections: object) -> Iterator[tuple[str, str]]:
    """Yield `(label, href)` for every link in the prose of a served document's sections."""
    for section in sections if isinstance(sections, list) else ():
        if isinstance(section, dict):
            for text in _texts(section.get("blocks")):
                for match in _LINKS.finditer(text):
                    if match.group("label") is not None:
                        yield match.group("label"), match.group("href")


def _texts(blocks: object) -> Iterator[str]:
    """Every run of prose in `blocks`, at any depth, as `unit.mentions` serves them."""
    for block in blocks if isinstance(blocks, list) else ():
        if not isinstance(block, dict):
            continue
        kind = block.get("type")
        if kind in ("heading", "para"):
            yield from _strings(block.get("text"))
        elif kind == "list":
            for item in block.get("items") or ():
                parts = item if isinstance(item, list) else [item]
                for part in parts:
                    yield from _texts([part]) if isinstance(part, dict) else _strings(part)
        elif kind == "table":
            for row in [block.get("headers"), *(block.get("rows") or ())]:
                for cell in row if isinstance(row, list) else ():
                    yield from _strings(cell)
        elif kind in CONTAINER_TYPES:
            yield from _strings(block.get("summary"))
            yield from _texts(block.get("blocks"))


def _strings(value: object) -> Iterator[str]:
    if isinstance(value, str):
        yield value


#: What a link that names no file of the corpus leads to.
NOT_THERE = "a file that is not in the corpus"

#: What a link that climbs out of the corpus leads to.
OUTSIDE = "a path that leads outside the corpus"


def _as_written(href: str, origin: PurePosixPath) -> str:
    """Say what a link the build kept names, read as its author wrote it: from the origin.

    ⭐ A link the build could not resolve keeps the author's href, which is
    relative to the source file and not to the page, so why it leads nowhere is
    read from there.
    """
    walked = _normal(f"{origin.parent.as_posix()}/{unquote(href.split('#', 1)[0])}")
    return OUTSIDE if not walked or walked.split("/", 1)[0] == ".." else NOT_THERE


def _unresolved(
    href: str, page: PurePosixPath, ids: set[str], pages: set[PurePosixPath], root: Path
) -> str | None:
    """Say where `href` fails to lead from `page`, or `None` when it leads somewhere."""
    if not href or _SCHEME.match(href) or href.startswith("//"):
        return None
    if href.startswith("/"):
        return "a rooted path, which leads outside the corpus"
    path, _, fragment = href.partition("#")
    path = path.split("?", 1)[0]
    if not path:
        return None if unquote(fragment) in ids else "an anchor that names no heading of this unit"
    walked = _normal(f"{page.parent.as_posix()}/{unquote(path)}")
    if not walked or walked.split("/", 1)[0] == "..":
        return OUTSIDE
    if PurePosixPath(walked) in pages:
        return None
    base = root.resolve()
    found = (base / walked).resolve()
    if not found.is_relative_to(base):
        return OUTSIDE
    return None if found.is_file() else NOT_THERE


def _normal(path: str) -> str:
    """Return a relative path with its `.` and `..` segments walked."""
    out: list[str] = []
    for part in path.split("/"):
        if part in ("", "."):
            continue
        if part == ".." and out and out[-1] != "..":
            out.pop()
        else:
            out.append(part)
    return "/".join(out)


def _where(directory: Path, root: Path, key: str) -> str:
    """Return the unit's archive directory under the root, or its key. ⛔ Never absolute (R7)."""
    try:
        return directory.relative_to(root).as_posix()
    except ValueError:
        return key


#: Does every relative link on a unit's page lead somewhere.
CHECKS = (check_links_resolve,)
