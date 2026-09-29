r"""The content namespace: what a unit *is*, as JSON, with strong validators.

**What it does.** Answers `/api/v1/content/toc` with the corpus's contents document
and `/api/v1/content/units/<key>` with one unit's served document, each inside the
API envelope, each with a strong ETag and `Cache-Control: no-cache`, and each
answered `304` when `If-None-Match` names the current tag.

**How you use it.** `route(source, request, rest)`, where `source` is any
`ContentSource`. `CorpusContent(corpus)` is the one this module ships: it builds each
document from a corpus's declarations on request.

**Depends on.** `contents.document`, `unit.builder`, `unit.served`, `archive.scrub`,
`serve.caching`, `serve.response`, and `serve.withheld` for what of a quiz is never
served. ⛔ Not on `render`: HTML is one renderer over
this data, and changing a template must not be able to break it.

## ⛔ `ContentSource` is the seam, and addressing is not this module's

⭐ **A unit is looked up by its whole key, exactly** — the string the contents
document joins on — so no part of the URL is ever split, decoded or used as a path,
and there is no traversal surface here at all. ⚠️ The N-segment routing a reader
types, and discovering the corpus from a root, are `serve/addressing.py`'s;
it plugs in by supplying a `ContentSource` or by mapping its addresses onto keys.

## ⭐ Why a document is built on request and not held

Content is reproducible, so a cache would be correct only until the archive
changed underneath it — and the strong tag is then the thing that lies. Built on
request, the tag is always over the bytes being served, which is the whole
guarantee a `304` makes.

## ⛔ A quiz's key and sentences are WITHHELD

⭐ **A quiz's key lives only in the page it grades** (register ruling,
2026-09-25), so a unit document is answered with every quiz option cut down to
its id and its words (`serve.withheld.redacted`). ⛔ `ContentSource.unit` is NOT
redacted — the build renders the page's key from it — and the redaction is this
route's, on the way out. ⭐ `CorpusContent.withheld` names every sentence and
question id its quizzes carry, both as served and as the archive wrote them,
which is what the static mount refuses a file other than a page for.

## ⛔ Every document is gated on the way out

A unit passes `unit.served.parse` (version, shape and the personal-data gate — the
trust boundary that module names the server as a consumer of); the contents
document passes `assert_clean`. A failure answers with a fixed message: `422` for a
shape this build does not recognise, `500` for personal data.
"""

from __future__ import annotations

import json
from pathlib import Path
from typing import Protocol

from studyforge.archive.scrub import PersonalDataLeak, assert_clean
from studyforge.contents import document as contents_document
from studyforge.serve.caching import not_modified, strong_etag
from studyforge.serve.response import API_PREFIX, JSON_TYPE, Request, Response, envelope, error
from studyforge.serve.withheld import Marks, marks_in, redacted
from studyforge.unit import served
from studyforge.unit.builder import NoMaterial, build_unit
from studyforge.unit.builder import read as read_material
from studyforge.unit.builder import render as render_unit
from studyforge.unit.errors import ContentError

#: Content revalidates every time and is re-sent only when its bytes changed.
CONTENT_CACHE = "no-cache"

TOC = "toc"
UNITS = "units/"

NO_SUCH_CONTENT = "no such content"
NO_SUCH_UNIT = "no such unit"
NOT_MATERIAL = "this unit is declared but has no material"
UNRECOGNISED = "content failed validation"
GATED = "content failed the personal-data gate"


class ContentSource(Protocol):
    """Where the content namespace's documents come from."""

    def toc(self) -> str:
        """Return the contents document's text."""
        ...

    def unit(self, key: str) -> str | None:
        """Return the unit document's text, or `None` when `key` has no material."""
        ...

    def declares(self, key: str) -> bool:
        """Say whether the corpus declares a unit at `key`, with material or without."""
        ...


class CorpusContent:
    """A `ContentSource` over a corpus's declarations, as `generate.read_corpus` returns them.

    ⚠️ Duck-typed on `contents`, `units` (each with `key`, `directory` and
    `declared_practices`) and `absent`, so this package does not reach into the
    build pipeline for a type.
    """

    def __init__(self, corpus: object) -> None:
        """Index the corpus's units by key; nothing is built until a request asks."""
        self._corpus = corpus
        self._units = {source.key: source for source in corpus.units}
        self._absent = frozenset(corpus.absent)
        self._withheld: tuple[tuple, Marks] = ((), Marks())

    def toc(self) -> str:
        """Return the contents document, rendered by its one serialiser."""
        return contents_document.render(self._corpus.contents)

    def unit(self, key: str) -> str | None:
        """Build and render one unit's document, or `None` when it has no material."""
        source = self._units.get(key)
        if source is None:
            return None
        try:
            document = build_unit(
                source.directory,
                declared_practices=source.declared_practices,
                mentions=source.mentions,
            )
        except NoMaterial:
            return None
        return render_unit(document)

    def declares(self, key: str) -> bool:
        """Say whether `key` is a declared unit of this corpus."""
        return key in self._units or key in self._absent

    def withheld(self) -> Marks:
        """Return every option sentence and question id of every quiz in this corpus's units.

        ⭐ **Read through `unit` — the same reader the build renders a quiz's key
        from** — so what is withheld is what a page grades with. ⚠️ Cached against
        the size and mtime of every file in every unit's directory, re-read when one moves: a quiz
        edited while served is withheld from its next request on.
        """
        stamp = tuple(_stamp(source.directory) for source in self._units.values())
        held, found = self._withheld
        if held == stamp and held:
            return found
        found = Marks()
        for key in self._units:
            found |= self._marks(key)
        self._withheld = (stamp, found)
        return found

    def _marks(self, key: str) -> Marks:
        """One unit's quiz marks; none for a unit that has no document or fails to build.

        ⚠️ A unit that cannot be built is skipped rather than failing every file
        the static mount serves; `serve.withheld`'s structural reading of the key
        still refuses its quiz's files.
        """
        try:
            text = self.unit(key)
            if text is None:
                return Marks()
            return marks_in(json.loads(text)) | _as_written(self._units[key].directory)
        except ContentError, OSError, ValueError:
            return Marks()


def _as_written(directory: Path) -> Marks:
    """One unit's quiz marks as its archive writes them, before a mention is served.

    ⭐ The served document says `section 1.2.3` in a heading's words
    (`unit.mentions`), so a file quoting the sentence as the archive wrote it
    would carry neither served spelling. Both spellings are withheld.
    """
    documents = read_material(directory).documents
    return marks_in({"sections": [{"workspace": one.get("exercise")} for one in documents]})


def _stamp(directory: Path) -> tuple:
    """Every file under `directory` with its size and mtime, sorted: what a rebuild reads."""
    try:
        found = sorted(path for path in Path(directory).rglob("*") if path.is_file())
        return tuple((path.as_posix(), *_size_and_time(path)) for path in found)
    except OSError:
        return ()


def _size_and_time(path: Path) -> tuple[int, int]:
    stat = path.stat()
    return stat.st_size, stat.st_mtime_ns


def version_document(namespaces: object, own: tuple[str, ...]) -> dict:
    """Return what `/api/v1` says: the namespaces served and which of them cache."""
    return {
        "resource": "api-version",
        "namespaces": sorted(namespaces),
        "cacheable": [f"{API_PREFIX}/{name}/" for name in own],
        "endpoints": {
            "toc": f"{API_PREFIX}/content/{TOC}",
            "unit": f"{API_PREFIX}/content/{UNITS}{{key}}",
            "asset": f"{API_PREFIX}/assets/{{path}}",
        },
    }


def route(source: ContentSource, request: Request, rest: str) -> Response:
    """Answer one request under `/api/v1/content/`; `rest` is the path after it."""
    if rest == TOC:
        return _document(request, "toc", {}, source.toc(), _toc)
    if rest.startswith(UNITS):
        key = rest[len(UNITS) :]
        text = source.unit(key)
        if text is None:
            return error(404, NOT_MATERIAL if source.declares(key) else NO_SUCH_UNIT)
        return _document(request, "unit", {"key": key}, text, _unit)
    return error(404, NO_SUCH_CONTENT)


def _toc(text: str) -> object:
    """Gate the contents document and return its value."""
    assert_clean(text, contents_document.TOC_FILENAME)
    return json.loads(text)


def _unit(text: str) -> object:
    """Read a unit document back through its own trust boundary, its quizzes withheld."""
    return redacted(served.parse(text, served.UNIT_FILENAME))


def _document(request: Request, resource: str, extra: dict, text: str, read) -> Response:
    """Wrap one gated document in the envelope and answer `200` or `304`."""
    try:
        document = read(text)
    except PersonalDataLeak:
        return error(500, GATED)
    except ContentError, ValueError:
        return error(422, UNRECOGNISED)
    body = envelope({"resource": resource, **extra, "document": document})
    etag = strong_etag(body)
    validators = (("ETag", etag), ("Cache-Control", CONTENT_CACHE))
    if not_modified(request.headers.get("If-None-Match"), etag):
        return Response(304, validators)
    return Response(200, (("Content-Type", JSON_TYPE), *validators), body)
