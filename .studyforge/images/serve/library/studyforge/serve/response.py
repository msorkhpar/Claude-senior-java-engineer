r"""A route's answer as a value, so routes are tested without a socket.

**What it does.** `Response` is what every route returns — a status, its headers,
and either bytes, a span of one file to stream, or a `stream` of chunks written
as they are produced. `Request` is what a route is given. The helpers build the
JSON envelope every API answer carries.

**How you use it.** A route returns `json_response(200, {...})`,
`error(404, "no such unit")`, `Response(206, headers, file=path, span=(a, b))`,
or `Response(200, headers, stream=chunks)`. `app` writes it; nothing else touches
the wire.

⭐ **A `stream` is written chunk by chunk and delimited by closing the connection**
(a run's output reaches the page line by line as the program writes it).
⛔ `app` always closes a stream it was handed — finished, failed or abandoned by
the client — so whatever the stream holds open (a run) ends with the response.
⭐ A stream that also has `cancel()` has it called, from another thread, the moment
the client hangs up: a stream waiting on its next chunk writes nothing, so no write
would ever discover it.

⛔ **`Request` carries no body and no query string, and that is structural**: a
route cannot read what a client sent beyond its method, path and headers, so no
client value has a field to arrive through (spec §8.3, rule 3).

**Depends on.** `json`, `dataclasses` and `pathlib`.

⛔ **Only messages this package wrote reach `error`.** An exception's text is the
easiest way for an absolute path — personal data (R7) — to reach a response
body, so a caller logs the exception's *type* and answers with a fixed string.
"""

from __future__ import annotations

import json
from collections.abc import Iterator, Mapping
from dataclasses import dataclass
from pathlib import Path

#: Every JSON answer says which API version it speaks.
API_VERSION = 1
API_ROOT = "/api"
API_PREFIX = f"{API_ROOT}/v{API_VERSION}"

JSON_TYPE = "application/json; charset=utf-8"
TEXT_TYPE = "text/plain; charset=utf-8"

#: What every non-content JSON answer carries: never stored, always re-asked.
NO_STORE = "no-store"

#: Statuses that carry no body and, so, no `Content-Length` (RFC 9110 §8.6).
BODILESS = frozenset({204, 304})


@dataclass(frozen=True, slots=True)
class Request:
    """The parts of a request a route may read: its method, its path, its headers."""

    method: str
    path: str
    headers: Mapping[str, str]


@dataclass(frozen=True, slots=True)
class Response:
    """One answer: bytes in `body`, the inclusive `span` of `file`, or a `stream` of chunks."""

    status: int
    headers: tuple[tuple[str, str], ...] = ()
    body: bytes = b""
    file: Path | None = None
    span: tuple[int, int] | None = None
    stream: Iterator[bytes] | None = None

    @property
    def length(self) -> int:
        """Return the number of body bytes this response sends; a stream's is not known."""
        if self.stream is not None:
            return 0
        if self.file is None:
            return len(self.body)
        if self.span is None:
            return 0
        return self.span[1] - self.span[0] + 1

    def header(self, name: str) -> str | None:
        """Return the first value of header `name`, compared case-insensitively."""
        wanted = name.lower()
        return next((value for key, value in self.headers if key.lower() == wanted), None)


def envelope(payload: Mapping[str, object]) -> bytes:
    """Return a JSON answer's bytes, `api` first, one serialisation for every route."""
    text = json.dumps({"api": API_VERSION, **payload}, indent=2, ensure_ascii=False)
    return (text + "\n").encode("utf-8")


def json_response(
    status: int, payload: Mapping[str, object], headers: tuple[tuple[str, str], ...] = ()
) -> Response:
    """Return a JSON answer that is never cached, unless `headers` says otherwise."""
    cached = any(key.lower() == "cache-control" for key, _ in headers)
    cache = () if cached else (("Cache-Control", NO_STORE),)
    return Response(status, (("Content-Type", JSON_TYPE), *cache, *headers), envelope(payload))


def error(status: int, message: str) -> Response:
    """Return a JSON error carrying a message this package wrote."""
    return json_response(status, {"error": message})
