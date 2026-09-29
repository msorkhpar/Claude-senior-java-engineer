r"""A study server published with one compose: where its editor and its runner are, handed in.

**What it does.** Reads the four values a published compose hands the study
server's container — where the browser reaches the editor, what the editor
binds, where the editor's health answers on the compose network, and where the
runner's run service is — and answers them as the host-process serve answers
its own probes: an `Editor` (through `DeclaredEditorProbe`) and a `Service` for
`Runner`. `write_allowed` writes the one list of argv the run service runs.

**How you use it.**

    config = from_environment(os.environ)       # None when nothing is declared
    config.editor_probe(), config.service
    write_allowed(root, entries)                 # every (cwd, argv) the records name

**Depends on.** `editor` for the `Editor` record, `remote` for `Service`, and
`urllib.request` for the editor's health, asked on the compose network.
⛔ No `docker`, no socket and no process: published, the study server asks the
editor over HTTP and the runner over its run service.

## ⭐ THE PORTS ARE SET IN ONE PLACE, AND THE PAGE LEARNS THEM FROM THE API

⭐ **The compose file interpolates every value below from the instance file**
(`instance.env`), so a reader changes a port there and nowhere else. The
browser-facing editor origin reaches a page only through the run index
(`serve.routes.run.index`), exactly as the host-process probe's does, so no
built file ever names a port (R8).

## ⛔ THE ORIGIN IS A LOOPBACK ONE

⭐ The editor is published on `127.0.0.1` by the compose file, and the frame
policy admits only loopback origins (`serve.security.frame_origin`). ⚠️ An
origin that is not one is refused here rather than framed nowhere: a reader who
widened the bind must restore the editor's authentication first, and the
documents say so.

## ⛔ THE ALLOWLIST IS THE RECORDS' ARGV, AND ONLY THE STUDY SERVER WRITES IT

⭐ Every entry is a working directory and an argv, each field ended by `NUL`,
each entry ended by one more `NUL` — the run service's own request framing, so
it compares bytes and parses nothing. ⚠️ It is rewritten whole from the
corpus's records (`serve.published`), never appended to from a request.
"""

from __future__ import annotations

import threading
import time
import urllib.request
from collections.abc import Iterable, Mapping, Sequence
from dataclasses import dataclass
from pathlib import Path
from urllib.parse import urlsplit

from studyforge.execute.editor import EDITOR_TTL, Editor
from studyforge.execute.errors import RunRefused
from studyforge.execute.remote import SERVICE_PORT, Service

#: Where the browser reaches the editor: `http://127.0.0.1:<port>`.
EDITOR_ORIGIN = "STUDYFORGE_EDITOR_ORIGIN"
#: What the editor binds: `<corpus-relative directory>=<folder in the editor>`, `;` between.
EDITOR_BINDS = "STUDYFORGE_EDITOR_BINDS"
#: Where the editor's health answers on the compose network.
EDITOR_HEALTH = "STUDYFORGE_EDITOR_HEALTH"
#: Where the runner's run service answers on the internal network: `<host>` or `<host>:<port>`.
RUN_SERVICE = "STUDYFORGE_RUN_SERVICE"

#: The four, in the order the compose file writes them.
VARIABLES = (EDITOR_ORIGIN, EDITOR_BINDS, EDITOR_HEALTH, RUN_SERVICE)

#: The run service's allowlist, relative to the corpus root. ⭐ The runner reads it
#: at `/work/` + this; it is ignored but for its own ignore file.
ALLOWED_DIR = ".studyforge/execution/allowed"
ALLOWED_FILE = f"{ALLOWED_DIR}/runs"

#: The ignore file that keeps the allowlist out of the repository.
ALLOWED_IGNORE = "*\n!/.gitignore\n"

#: The run service's script in this package, which the runner runs under `perl`.
SCRIPT = "assets/runservice.pl"

#: The hosts a browser-facing editor origin may name.
LOOPBACK_HOSTS = frozenset({"127.0.0.1", "localhost", "::1"})

#: How long the editor's health may take to answer.
HEALTH_TIMEOUT = 3.0


@dataclass(frozen=True, slots=True)
class Published:
    """What a published compose declared: the editor, and the run service."""

    origin: str | None
    binds: tuple[tuple[str, str], ...]
    health: str | None
    service: Service | None

    def editor_probe(self) -> DeclaredEditorProbe | None:
        """Return the editor as declared, or `None` when nothing declares one."""
        if self.origin is None or not self.binds or self.health is None:
            return None
        return DeclaredEditorProbe(self.origin, self.binds, self.health)


def from_environment(environ: Mapping[str, str]) -> Published | None:
    """Return what `environ` declares, `None` when it declares nothing, or raise `RunRefused`."""
    if not any(environ.get(one) for one in VARIABLES):
        return None
    origin = environ.get(EDITOR_ORIGIN) or None
    if origin is not None and not _loopback(origin):
        raise RunRefused(
            f"{EDITOR_ORIGIN} must be an http origin on 127.0.0.1 or localhost; a page frames "
            "only a loopback editor, and a wider bind needs the editor's authentication back"
        )
    return Published(
        origin=None if origin is None else origin.rstrip("/"),
        binds=_binds(environ.get(EDITOR_BINDS, "")),
        health=environ.get(EDITOR_HEALTH) or None,
        service=_service(environ.get(RUN_SERVICE, "")),
    )


class DeclaredEditorProbe:
    """The editor a published compose declared, up when its health answers.

    ⭐ The same two readers as `editor.EditorProbe`: `editor()` asks (over HTTP on
    the compose network, never `docker`), `known()` reads what the last ask left.
    """

    def __init__(
        self, origin: str, binds: Sequence[tuple[str, str]], health: str, *, ttl: float = EDITOR_TTL
    ) -> None:
        """Hold the declaration; ask nothing until `editor()`."""
        (base, folder), *others = sorted(binds)
        self.declared = Editor(origin=origin, folder=folder, base=base, others=tuple(others))
        self.health = health
        self._ttl = ttl
        self._lock = threading.Lock()
        self._cached: tuple[Editor | None, float] | None = None

    def editor(self) -> Editor | None:
        """Return the declared editor when its health answers, else `None`."""
        now = time.monotonic()
        with self._lock:
            if self._cached is not None and 0 <= now - self._cached[1] < self._ttl:
                return self._cached[0]
            found = self.declared if _healthy(self.health) else None
            self._cached = (found, now)
            return found

    def known(self) -> Editor | None:
        """Return the last answer while it is fresh; a cold or expired reading is `None`."""
        with self._lock:
            if self._cached is None:
                return None
            found, asked_at = self._cached
            return found if 0 <= time.monotonic() - asked_at < self._ttl else None


def run_service_script() -> str:
    """Return the run service's script, as the execution skill writes it beside the compose file."""
    return (Path(__file__).parent / SCRIPT).read_text(encoding="utf-8")


def allowed_bytes(entries: Iterable[tuple[str, Sequence[str]]]) -> bytes:
    """Return the allowlist's bytes: each `(cwd, argv)`, fields `NUL`-ended, then one `NUL`.

    ⭐ Sorted and without repeats, so the same records write the same bytes (R10).
    """
    framed = set()
    for cwd, argv in entries:
        fields = [cwd, *argv]
        if any("\x00" in field for field in fields):
            continue
        framed.add(b"".join(field.encode("utf-8") + b"\x00" for field in fields) + b"\x00")
    return b"".join(sorted(framed))


def write_allowed(root: Path, entries: Iterable[tuple[str, Sequence[str]]]) -> Path:
    """Write the run service's allowlist under `root`, whole; return its path."""
    directory = Path(root) / ALLOWED_DIR
    directory.mkdir(parents=True, exist_ok=True)
    target = directory / "runs"
    staged = directory / ".runs.next"
    staged.write_bytes(allowed_bytes(entries))
    staged.replace(target)
    return target


def _loopback(origin: str) -> bool:
    """Whether `origin` is an http origin on a loopback host and nothing more."""
    parts = urlsplit(origin)
    try:
        port = parts.port
    except ValueError:
        return False
    return (
        parts.scheme == "http"
        and parts.hostname in LOOPBACK_HOSTS
        and port is not None
        and parts.path in ("", "/")
        and not parts.query
        and not parts.fragment
        and "@" not in parts.netloc
    )


def _binds(text: str) -> tuple[tuple[str, str], ...]:
    """Parse `base=folder;base=folder`, refusing a base that is not corpus-relative."""
    found = []
    for entry in (one for one in text.split(";") if one.strip()):
        base, equals, folder = entry.partition("=")
        base, folder = base.strip(), folder.strip()
        parts = base.split("/")
        climbs = any(part in ("", ".", "..") for part in parts)
        if not equals or not folder.startswith("/") or climbs:
            raise RunRefused(
                f"{EDITOR_BINDS} is '<corpus-relative directory>=<absolute folder>' pairs "
                "separated by ';'"
            )
        found.append((base, folder))
    return tuple(found)


def _service(text: str) -> Service | None:
    """Parse `<host>` or `<host>:<port>`; empty is no service."""
    if not text.strip():
        return None
    host, colon, port = text.strip().partition(":")
    if not host or (colon and not port.isdecimal()):
        raise RunRefused(f"{RUN_SERVICE} is '<host>' or '<host>:<port>'")
    return Service(host, int(port) if colon else SERVICE_PORT)


def _healthy(url: str) -> bool:
    """Whether the editor's health answers `200` on the compose network."""
    try:
        with urllib.request.urlopen(url, timeout=HEALTH_TIMEOUT) as answer:
            return answer.status == 200
    except OSError, ValueError:
        return False
