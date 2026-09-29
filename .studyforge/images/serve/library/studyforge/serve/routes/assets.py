r"""The assets namespace and the static mount: a built site's bytes, unchanged.

**What it does.** Maps a URL path onto one regular file under the served root —
refusing every traversal — and answers it with a weak ETag, `304` on a matching
`If-None-Match`, and `206` / `416` for a `Range`. Text passes the personal-data
gate before it leaves; binary media is streamed.

**How you use it.** `route(root, private, request, rest)` for `/api/v1/assets/`;
`serve(root, request, url_path, private)` for the static mount, which is the same
function — ⛔ **one resolver, never two**, because two are two traversal surfaces.

**Depends on.** `archive.scrub`, `corpus.placement.profile` for the generated
directory's name, `serve.caching`, `serve.response`, and `serve.withheld` for a
quiz's key.

⭐ **A missing `/favicon.ico` is `204`, not `404`**: a browser asks every origin for
it unprompted, a page names no icon, and a `404` is an error in the reader's
console on every served page.

## ⛔ `resolve` is the whole traversal control, and its order is the control

1. split on `/` **before** decoding, so `%2f` cannot manufacture a separator;
2. decode each segment, then refuse `.`, `..`, a separator, a backslash or a NUL;
3. refuse a dot-prefixed segment — ⭐ **except the build's own generated directory,
   as the first segment or beside a `corpus.json`**, because every page a build
   writes links into it and a blanket dotfile rule would serve a site with no
   stylesheet. ⛔ **Beside a manifest, not "at any depth"**: a
   root holding several corpora puts each one's generated directory one level
   down, and the manifest on disk is what says a corpus is there — no mount list is
   configured, and any other nested dot-directory is still refused;
4. `resolve()` (following symlinks) and require the result inside the root and a
   regular file.

## ⚠️ Text ignores `Range`

A range over text would bypass the gate by construction, so gated text is always
answered whole, with `Accept-Ranges: none` — RFC 9110 §14.2 lets a server ignore
`Range`. The extraction source answered `416` there instead, which refuses a range
that was satisfiable. Text too large to gate is refused, never served ungated.

⭐ **`private` is the seam for a file that sits under the root and is not content**
— the reader's own record is the first. It answers `404`, as does every refusal,
so a prober never learns which guess was interesting.

## ⛔ A file carrying a quiz's key or sentence is REFUSED — unless it is a page

⭐ **A quiz's key lives only in the page it grades** (the register ruling of
2026-09-25), so a page is served whole. ⛔ **Every other file carrying one is
refused**: a corpus built into its own root puts the archive's `practice-M.json`
and the bundle's `tests/quiz.json` under the served root, so `withheld` is asked
of every other text — and every file of an unknown type, where an editor's
backup lands — before anything is answered, a `304` included: `404`.
⭐ **The default withholds nothing**: what a file may not carry is decided by the
quizzes an instance SERVES, and `serve.app` hands both mounts that predicate.
⚠️ Media is not read, and an unknown-type file over `GATE_MAX_BYTES` is served
unread: a compressed archive holding a bundle cannot be read here at all.

## ⛔ The run client reaches a page ONE way, and this is it

⭐ **The serving process adds the client to the page it answers; a built page
never loads it.** When an instance registers the run namespace it hands this
route the client's path, and an HTML page leaves here with exactly one
`<script src="…" defer></script>` inserted before its first `</head>`. ⛔ **The
file on disk is untouched** — nothing is written, and the same bytes are served
again the next time with no client when the namespace is not registered.

⚠️ **Why this way and no other** (how a served page loads the run
client): every alternative puts the client's address into the BUILD. A
`<script src="/api/…">` is a rooted reference that names the API; a relative
`api/v1/…` resolved against `location.origin` names no origin and still loads
it; a copy of the client in the site names the API on every line. ⭐ Only the
server knows it is a server, so only the server says so — and R8's floor
(`tests/studyforge/cli/serving.py`) reads a built text that names the client as
a defect.

⛔ **A served page carries a DIFFERENT validator from the same file on disk.**
The weak ETag is taken from the file's size and mtime, which do not move when
the client is inserted — so a page cached from a served origin and one cached
from a plain file mount would collide under one ETag. `client_etag` marks the
served form, and the mark is part of the opaque tag rather than a second header.
"""

from __future__ import annotations

import re
from collections.abc import Callable
from pathlib import Path
from urllib.parse import unquote

from studyforge.archive.scrub import PersonalDataLeak, assert_clean
from studyforge.corpus.manifest import SOURCE_SUFFIXES
from studyforge.corpus.manifest.document import MANIFEST_FILENAME
from studyforge.corpus.placement.profile import GENERATED_ROOT
from studyforge.progress import store_dir
from studyforge.serve.caching import UNSATISFIABLE, WHOLE, not_modified, parse_range, weak_etag
from studyforge.serve.response import TEXT_TYPE, Request, Response

#: Assets revalidate every time; a `304` costs one `stat`, and one read of a text
#: or unknown-type file, which `withheld` is asked of first.
ASSET_CACHE = "no-cache"

#: What a browser asks every served origin for, unprompted. ⭐ Answered `204` when
#: the site has none: a `404` is an error in the reader's console on every page,
#: and a page names no icon, so there is nothing a build could fix.
FAVICON = "/favicon.ico"

#: Longest URL path accepted, before decoding.
MAX_PATH = 1024

#: The file a directory request resolves to.
INDEX_FILENAME = "index.html"

#: The one tag a served page gains, and where it goes. ⛔ Before the FIRST
#: `</head>`, and exactly once: a deferred script in the head runs before the
#: deferred page script at the end of the body, so the panel finds
#: `window.studyforge.run` already published when it looks.
CLIENT_TAG = '<script src="{path}" defer></script>'
HEAD_CLOSE = b"</head>"

#: What marks the validator of a page the client was added to. ⚠️ Inside the
#: opaque tag, so `If-None-Match`'s weak comparison still matches it against
#: itself and never against the plain file's.
CLIENT_ETAG_MARK = "+client"

#: What a client path may be: one rooted URL path, and nothing that could close
#: the attribute it is written into. ⛔ Checked rather than escaped, because the
#: only caller passes `serve.routes.run.CLIENT_PATH` — a value that needed
#: escaping here would be a value this route should not have been given.
CLIENT_PATH_FORBIDDEN = "\"'<>& \t\r\n"

#: The one dot-prefixed name served: first, or where a corpus manifest sits beside it.
EXPOSED_DOT_DIRECTORY = GENERATED_ROOT

#: Largest text the gate reads. ⛔ Above it a file is refused, not served ungated.
GATE_MAX_BYTES = 4 * 1024 * 1024

#: Content types that are text, and so gated and never ranged.
GATED_TYPES = ("text/", "application/json", "image/svg+xml")

#: By extension, from a fixed table: `mimetypes` reads system files and varies by
#: machine. An unknown extension is opaque bytes, which `nosniff` makes inert.
#: ⭐ A code file of any declarable runtime is TEXT, so a lesson's link to one is
#: a plain view the browser shows rather than a download (a code example's fallback);
#: every entry spelled below wins over that, so `.js` stays a script.
CONTENT_TYPES = {
    **dict.fromkeys(sorted({one for kind in SOURCE_SUFFIXES.values() for one in kind}), TEXT_TYPE),
    ".html": "text/html; charset=utf-8",
    ".css": "text/css; charset=utf-8",
    ".js": "text/javascript; charset=utf-8",
    ".json": "application/json; charset=utf-8",
    ".md": TEXT_TYPE,
    ".txt": TEXT_TYPE,
    ".vtt": "text/vtt; charset=utf-8",
    ".svg": "image/svg+xml",
    ".png": "image/png",
    ".jpg": "image/jpeg",
    ".jpeg": "image/jpeg",
    ".gif": "image/gif",
    ".webp": "image/webp",
    ".ico": "image/x-icon",
    ".mp3": "audio/mpeg",
    ".m4a": "audio/mp4",
    ".ogg": "audio/ogg",
    ".wav": "audio/wav",
    ".mp4": "video/mp4",
    ".webm": "video/webm",
    ".woff2": "font/woff2",
}
DEFAULT_CONTENT_TYPE = "application/octet-stream"

#: ⭐ **A source file: the code a lesson links to, served VERBATIM** (plain-text suffixes only; `.js`
#: stays a script). ⛔ Never refused for a sample address or token, or its size; a home path or
#: hostname still is, and `withheld` is still asked.
SOURCE_SUFFIXES_SERVED = frozenset(
    one for kind in SOURCE_SUFFIXES.values() for one in kind if CONTENT_TYPES[one] == TEXT_TYPE
)
SAMPLES = re.compile(r"\b[\w.%+\-]+@[\w.\-]+\.[A-Za-z]{2,}\b|\bBearer\s+[\w.\-]{8,}")

Private = Callable[[Path], bool]

#: Whether a file's bytes carry what a site never serves.
Withheld = Callable[[bytes], bool]

#: ⭐ The one type `withheld` never reads: a page, where a quiz's key lives.
PAGE_TYPE = CONTENT_TYPES[".html"]

#: ⛔ **The reader's progress record, which is never content**. It sits
#: at `<generated root>/progress/` beside the pages a `tree` profile writes, so the
#: static mount would otherwise serve it. Refused BY PATH, on the resolved file, so
#: a symlink into it is refused too; the state namespace is where the
#: record is served. ⚠️ A hard link to it elsewhere under the root is not seen.
#: ⛔ **Matched ANYWHERE in the resolved absolute path, never relative to the served
#: root**: a root that is a corpus's generated directory, or one holding
#: several corpora, puts a store at a different depth, and no caller has to pass
#: `private=` to keep it unserved.
#: ⭐ **Derived from `progress.store_dir`, the store's one spelling**, so
#: the store cannot move without this refusal moving with it.
PROGRESS_PREFIX = store_dir(".").parts


def nothing_private(path: Path) -> bool:
    """Treat no file under the root as private: the default until a store names one."""
    return False


def nothing_withheld(body: bytes) -> bool:
    """Withhold no file: the default where no instance has named the quizzes it serves."""
    return False


def client_tag(client: str) -> bytes:
    """Return the one script tag a served page gains, or raise on an unusable path."""
    if not client.startswith("/") or any(char in client for char in CLIENT_PATH_FORBIDDEN):
        raise ValueError(
            "the run client is served at one rooted URL path carrying no attribute "
            "delimiter; the value is not reproduced here, since a refusal never quotes a value "
            "that may be personal"
        )
    return CLIENT_TAG.format(path=client).encode("utf-8")


def with_client(body: bytes, client: str | None) -> bytes:
    """Return `body` with exactly one client tag before its first `</head>`.

    ⛔ **Exactly one, and only where there is a head to close.** A page already
    carrying the tag is left alone, and a text with no `</head>` — anything a
    corpus happens to ship as `.html` that is not a built page — is served
    unchanged rather than having a script pushed into the middle of it.
    """
    if not client:
        return body
    tag = client_tag(client)
    if tag in body or HEAD_CLOSE not in body:
        return body
    return body.replace(HEAD_CLOSE, tag + HEAD_CLOSE, 1)


def client_etag(etag: str) -> str:
    """Return the validator for the served form of a page, told apart from the file's."""
    return f'{etag[:-1]}{CLIENT_ETAG_MARK}"' if etag.endswith('"') else etag + CLIENT_ETAG_MARK


def resolve(root: Path, url_path: str) -> Path | None:
    """Return the regular file under `root` that `url_path` names, or `None`."""
    try:
        base = Path(root).resolve(strict=True)
    except OSError, RuntimeError:
        return None
    if not url_path.startswith("/") or len(url_path) > MAX_PATH or "\x00" in url_path:
        return None
    segments = []
    for raw in url_path.split("/")[1:]:
        if not raw:
            continue
        try:
            segment = unquote(raw, encoding="utf-8", errors="strict")
        except UnicodeDecodeError:
            return None
        if segment in (".", "..") or any(c in segment for c in "/\\\x00"):
            return None
        if segment.startswith(".") and not _exposed(base, segments, segment):
            return None
        segments.append(segment)
    target = base.joinpath(*segments)
    if target.is_dir():
        target = target / INDEX_FILENAME
    try:
        real = target.resolve(strict=True)
    except OSError, RuntimeError:
        return None
    if base not in real.parents or not real.is_file():
        return None
    if in_a_progress_store(real):
        return None
    return real


def _exposed(base: Path, above: list[str], segment: str) -> bool:
    """Say whether a dot-prefixed segment is a generated directory the mount serves.

    ⭐ The served root's own, or one whose parent holds a corpus manifest. ⛔ The
    progress store inside it is still refused, on the resolved path, by `resolve`.
    """
    if segment != EXPOSED_DOT_DIRECTORY:
        return False
    return not above or base.joinpath(*above, MANIFEST_FILENAME).is_file()


def in_a_progress_store(path: Path) -> bool:
    """Say whether `PROGRESS_PREFIX` occurs anywhere in a resolved path's parts."""
    parts, width = path.parts, len(PROGRESS_PREFIX)
    return any(parts[i : i + width] == PROGRESS_PREFIX for i in range(len(parts) - width + 1))


def content_type_for(path: Path) -> str:
    """Return the content type served for `path`, from the fixed table."""
    return CONTENT_TYPES.get(path.suffix.lower(), DEFAULT_CONTENT_TYPE)


def route(
    root: Path,
    private: Private,
    request: Request,
    rest: str,
    *,
    client: str | None = None,
    withheld: Withheld = nothing_withheld,
) -> Response:
    """Answer one request under `/api/v1/assets/`; `rest` is the path after it."""
    return serve(root, request, "/" + rest, private, client, withheld)


def serve(
    root: Path,
    request: Request,
    url_path: str,
    private: Private = nothing_private,
    client: str | None = None,
    withheld: Withheld = nothing_withheld,
) -> Response:
    """Answer one file: `200`, `206`, `304`, `404` or `416`.

    ⭐ `client` is where the run namespace serves the page's execution client,
    and `None` is an instance that registers no such namespace. ⛔ It reaches an
    HTML page's BYTES and never the file on disk — see this module's docstring.
    """
    target = resolve(root, url_path)
    if target is None and url_path == FAVICON:
        return Response(204, ())
    if target is None or private(target):
        return _not_found()
    try:
        stat = target.stat()
    except OSError:
        return _not_found()
    ctype = content_type_for(target)
    body = None
    if ctype.startswith(GATED_TYPES) or ctype == DEFAULT_CONTENT_TYPE:
        try:
            body = target.read_bytes() if stat.st_size <= GATE_MAX_BYTES else None
        except OSError:
            return _not_found()
        if body is not None and ctype != PAGE_TYPE and withheld(body):
            return _not_found()
    added = client if client and ctype == CONTENT_TYPES[".html"] else None
    etag = client_etag(weak_etag(stat)) if added else weak_etag(stat)
    validators = (("ETag", etag), ("Cache-Control", ASSET_CACHE))
    if not_modified(request.headers.get("If-None-Match"), etag):
        return Response(304, validators)
    if target.suffix.lower() in SOURCE_SUFFIXES_SERVED:
        return _source(target, body, stat.st_size, ctype, validators)
    if ctype.startswith(GATED_TYPES):
        return _text(body, ctype, validators, added)
    ranges = request.headers.get("Range") if request.headers.get("If-Range") is None else None
    span = parse_range(ranges, stat.st_size)
    if span == UNSATISFIABLE:
        headers = (("Content-Range", f"bytes */{stat.st_size}"), ("Accept-Ranges", "bytes"))
        return Response(416, (("Content-Type", TEXT_TYPE), *headers), b"range not satisfiable\n")
    headers = (("Content-Type", ctype), *validators, ("Accept-Ranges", "bytes"))
    if span == WHOLE:
        whole = (0, stat.st_size - 1) if stat.st_size else None
        return Response(200, headers, file=target, span=whole)
    first, last = span
    ranged = (*headers, ("Content-Range", f"bytes {first}-{last}/{stat.st_size}"))
    return Response(206, ranged, file=target, span=span)


def _source(target: Path, body: bytes | None, size: int, ctype: str, validators: tuple) -> Response:
    """Answer a source file whole; one too large to read is streamed unread."""
    headers = (("Content-Type", ctype), *validators, ("Accept-Ranges", "none"))
    try:
        assert_clean(SAMPLES.sub("", (body or b"").decode("utf-8", errors="replace")), "asset")
    except PersonalDataLeak:
        return Response(500, (("Content-Type", TEXT_TYPE),), b"asset failed the gate\n")
    if body is not None:
        return Response(200, headers, body)
    return Response(200, headers, file=target, span=(0, size - 1) if size else None)


def _text(body: bytes | None, ctype: str, validators: tuple, client: str | None) -> Response:
    """Take a text file's bytes whole, add the client where one is named, gate it, answer it.

    ⛔ **The gate runs over what LEAVES this process**, so the insertion happens
    before it rather than after: a page gated and then edited is a page whose
    served bytes nothing checked.
    """
    if body is None:
        return Response(500, (("Content-Type", TEXT_TYPE),), b"text too large to gate\n")
    body = with_client(body, client)
    try:
        assert_clean(body.decode("utf-8", errors="replace"), "asset")
    except PersonalDataLeak:
        return Response(500, (("Content-Type", TEXT_TYPE),), b"asset failed the gate\n")
    headers = (("Content-Type", ctype), *validators, ("Accept-Ranges", "none"))
    return Response(200, headers, body)


def _not_found() -> Response:
    """Return the one `404` every refusal shares."""
    return Response(404, (("Content-Type", TEXT_TYPE),), b"not found\n")
