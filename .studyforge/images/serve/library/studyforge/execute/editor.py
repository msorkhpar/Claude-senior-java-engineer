"""Where a running editor is, if one is up: an origin to reach it at and a folder to open.

**What it does.** Asks the Docker CLI whether the corpus's editor container is
running, what host port it publishes, and which directory of THIS source root
it binds — and caches the answer for `EDITOR_TTL` seconds. An editor that is
not up, not this corpus's, or not reachable is `None`.

**How you use it.** `probe = EditorProbe(source_root, container)`;
`probe.editor()` returns an `Editor` — `origin` and `folder` — or `None`.
`container=None` is no editor, always, with no probe at all.

**Depends on.** `subprocess` — ⭐ a probe starts a process, and this package is
where the framework's execution starts them — `time` and `pathlib`.

## ⛔ Why this is asked at SERVE time and can never be built into a page

The editor's host port is **per-project**: the contract declares
one and says two corpora on one host would collide on it, so the port a reader
actually has is a fact about their machine. ⛔ **R8 forbids a built page naming
an origin or a port**, and a built site opens over `file://` where there is no
server to ask. ⭐ So the answer is published by the run namespace's index, which
exists exactly where an origin can answer it, and the page reads it there.

## ⛔ The runner never starts, stops or builds a container, and neither does this

⭐ The reader-starts-it seam and spec §8.3 hold here unchanged: a probe is
`docker inspect`, which READS. ⛔ **The Docker socket is never mounted into the
serving process** — not behind a flag, not "only locally". The host's CLI is
the only thing that talks to the daemon, and it runs on the host. ⛔ **Nothing
here starts an editor**: it is a full development environment with a shell, and
opening a reading page is not consent to run one.

## Up is not enough: it must bind THIS corpus, and publish exactly one port

⭐ **The folder is DISCOVERED, never composed.** A container by the right name
that binds some other checkout would open a reader's page onto files they are
not being taught from, so "up" means running **and** binding a directory that
is this source root or inside it — and the `folder` published is that mount's
own destination, read back out of the container rather than written here. ⛔ No
path inside anybody's image is spelled in this framework (R1): every value
below comes from the daemon's answer.

## ⛔ Addressing a FILE needs the mount's own base, and that is why it is kept

⭐ **A practice's `main_path` is relative to the SOURCE ROOT and the editor
mounts a directory INSIDE it**, so the two do not compose without knowing which
directory that is. `Editor.base` is that mount's source **relative to the root**
— ⛔ never the host directory, which is a home (R7) and is published nowhere.
⭐ `Editor.file(path)` is then the absolute path inside the container, and
`None` for a path the editor does not hold. ⚠️ **`None` matters more than it
looks**: a code-server URL naming a path that is not mounted opens an empty,
dirty buffer titled with the file's own name, which looks exactly like a
corrupted file and is not one.

⚠️ **The editor binds directories INSIDE the source root, where the runner
mounts the root itself** — §8.1 mounts only the sources, never the
repository — which is why this probe accepts a descendant and `mode.ModeProbe`
demands equality. No mount inside the root is no editor.

⭐ **Several binds inside the root are several answers, one per path**:
the generated editor binds the sources AND the practice workspaces, which are
siblings, so a practice file is held by exactly one of them. `Editor.holding`
answers the editor as seen through the bind that holds a path — its own folder,
its own base — and `None` for a path no bind holds. ⚠️ The index publishes the
first bind by base, which a page only asks whether it has.

⭐ **A compose-started editor is read by its labels** (`labels`), never by a
mount's `Source`, which is the ENGINE's spelling (on Docker Desktop for Windows,
a path inside its VM); an unlabelled container is still read by its source.

⭐ **One published host port, or no editor.** The container publishes the UI and
nothing else, so a single host port IS the answer; several is a container this
framework cannot tell the UI's port from, and guessing would embed a frame
pointing at something else. ⚠️ A binding on an unspecified address is reported
at loopback, which is the address the reader's own browser reaches it on and
the only one §8.1 permits it to publish.

## Every failure to ask is NO EDITOR

No `docker` binary, no such container, a daemon that hangs past
`INSPECT_TIMEOUT`, output that does not parse — each answers `None`, and the
panel then shows the sentence it already ships: the editor is not running, and
here is how to start one. ⛔ Nothing on a page fails because the probe did.

## Cached briefly

An index fetched once per page must not fork `docker` per corpus per tick, so
an answer — including a negative — is believed for `EDITOR_TTL` seconds; after
that the next ask is a real one, so an editor the reader starts or stops is
noticed within that window.
"""

from __future__ import annotations

import os
import subprocess
import threading
import time
from collections.abc import Callable
from dataclasses import dataclass
from pathlib import Path

from studyforge.execute import labels

#: The CLI the probe invokes, found on `PATH`.
DOCKER = "docker"

#: How long an answer is believed, in seconds.
EDITOR_TTL = 10.0

#: How long `docker inspect` may take before the answer is `None`.
INSPECT_TIMEOUT = 3.0

#: The scheme a code editor served over loopback is reached on. ⛔ Not a choice:
#: the contract publishes plain HTTP on loopback and says why TLS is not there.
SCHEME = "http"

#: Where an unspecified binding is reported — the address the reader's browser
#: has, and the only one §8.1 lets the editor publish on.
LOOPBACK = "127.0.0.1"

#: The host addresses that mean "every interface", which is not an address a
#: browser can be sent to.
UNSPECIFIED = ("", "0.0.0.0", "::", "[::]")

#: Segments that name somewhere other than themselves. ⚠️ Spelled here rather
#: than imported from `exercise.safety`: `execute` starts processes and knows
#: nothing about a record, and one import for one tuple would tie the two
#: packages together for no other reason.
TRAVERSAL = (".", "..")

#: The two record kinds the inspect format emits, and the field separator. ⚠️ A
#: path carrying a tab or a newline does not parse and answers no editor, which
#: is the same discipline every other failure to ask gets.
PORT = "port"
MOUNT = "mount"
LABEL = "label"
FIELD = "\t"

#: One record per line: whether it runs, then every published binding, then
#: every mount as its source and its destination.
_INSPECT_FORMAT = (
    "{{.State.Running}}\n"
    "{{range .NetworkSettings.Ports}}{{range .}}"
    + PORT
    + FIELD
    + "{{.HostIp}}"
    + FIELD
    + "{{.HostPort}}\n"
    "{{end}}{{end}}"
    "{{range .Mounts}}"
    + MOUNT
    + FIELD
    + "{{.Source}}"
    + FIELD
    + "{{.Destination}}\n{{end}}"
    + "".join(
        f'{LABEL}{FIELD}{name}{FIELD}{{{{index .Config.Labels "{name}"}}}}\n'
        for name in (labels.WORKING_DIR, labels.BINDS)
    )
)


@dataclass(frozen=True, slots=True)
class Editor:
    """A running editor: where a browser reaches it, what it opens, and what it holds.

    ⭐ `base` is the part of THIS source root the editor actually mounts, as a
    relative POSIX path — `""` when it mounts the root itself. ⛔ It is
    relative and never the host directory: the host directory is a home (R7)
    and this record is read by the serving process, which publishes two of
    these three fields on its index.
    """

    origin: str
    folder: str
    base: str = ""
    #: Every FURTHER bind of this source root, as `(base, folder)`.
    others: tuple[tuple[str, str], ...] = ()

    def holding(self, path: str) -> Editor | None:
        """Return this editor as seen through the bind that holds `path`, or `None`.

        ⭐ The deepest bind holding it wins, so a bind nested in another is the
        one a file is opened through. The answer carries no `others`: it is one
        folder, which is what a window opens and a settings file is written into.
        """
        held = [
            (base, folder)
            for base, folder in ((self.base, self.folder), *self.others)
            if inside_base(base, path) is not None
        ]
        if not held:
            return None
        base, folder = max(held, key=lambda one: len([part for part in one[0].split("/") if part]))
        return Editor(origin=self.origin, folder=folder, base=base)

    def within(self, directory: str) -> Editor | None:
        """Return this editor opened on `directory` of its bind, or `None`.

        `directory` is relative to the SOURCE ROOT and is `base` or below it.
        ⭐ The answer is a narrower folder of the SAME container, so a practice
        can open — and carry the settings of — a folder of its own.
        """
        parts = [segment for segment in directory.split("/") if segment]
        prefix = [segment for segment in self.base.split("/") if segment]
        if directory.startswith("/") or "\\" in directory or parts[: len(prefix)] != prefix:
            return None
        if any(segment in TRAVERSAL for segment in parts):
            return None
        tail = "/".join(parts[len(prefix) :])
        folder = f"{self.folder.rstrip('/')}/{tail}" if tail else self.folder
        return Editor(origin=self.origin, folder=folder, base="/".join(parts))

    def inside(self, path: str) -> str | None:
        """Return `path`'s place in the opened folder, or `None` when it is not there.

        `path` is relative to the SOURCE ROOT, which is what a practice's
        `main_path` and `test_path` are. ⚠️ A path outside `base` is not
        mounted, and naming one would open an **empty, dirty buffer titled
        with the file's own name** — which looks exactly like a corrupted file
        and is not one. So it answers nothing at all.
        """
        return inside_base(self.base, path)

    def file(self, path: str) -> str | None:
        """Return the absolute path `path` has INSIDE the container, or `None`."""
        within = self.inside(path)
        return None if within is None else f"{self.folder.rstrip('/')}/{within}"


class EditorProbe:
    """Answer where the editor is for one source root and one container name, or `None`.

    ⭐ **Two readers, and only one of them may ask**: `editor()` asks
    `docker` when its answer has expired, and `known()` reads what the last ask
    left without ever forking. ⛔ Anything on the path of an ordinary response
    uses `known()` — the serving process does not reach the Docker socket to
    render a page (spec §8.3).
    """

    def __init__(
        self,
        source_root: Path,
        container: str | None,
        *,
        docker: str = DOCKER,
        clock: Callable[[], float] = time.monotonic,
        ttl: float = EDITOR_TTL,
        inspect_timeout: float = INSPECT_TIMEOUT,
    ) -> None:
        """Remember the root and name; ask nothing until `editor()`."""
        self.source_root = Path(source_root)
        self.container = container
        self.docker = docker
        self._clock = clock
        self._ttl = ttl
        self._inspect_timeout = inspect_timeout
        self._lock = threading.Lock()
        self._cached: tuple[Editor | None, float] | None = None

    def editor(self) -> Editor | None:
        """Where this corpus's editor is, or `None` when there is not one to say."""
        if self.container is None:
            return None
        now = self._clock()
        with self._lock:
            if self._cached is not None:
                found, asked_at = self._cached
                if 0 <= now - asked_at < self._ttl:
                    return found
            found = self._ask()
            self._cached = (found, now)
            return found

    def known(self) -> Editor | None:
        """Where this editor is if it has ALREADY been asked about — asking nothing.

        ⛔ **This never forks, and that is the whole point** (spec §8.3):
        a caller on the path of an ordinary response may read what a previous
        `editor()` left behind, and a COLD cache is `None` rather than a reason
        to reach the Docker socket while serving a page.
        ⚠️ **An expired reading is cold**, not stale-but-usable: a reading older
        than the TTL is not a reading, and a caller deciding whether to act
        against a container must not act on one.
        ⛔ **So this is the wrong reader to compose a LASTING policy from, and
        a live reading says so**: a `frame-src` read straight off
        this named the editor for `EDITOR_TTL` seconds after an ask and `'none'`
        from then on, which a reader is essentially never inside. ⭐ The remedy
        is a record kept by the policy's own caller —
        `serve.routes.runs.Runs.origins()` — and never a softer TTL here: the
        TTL's ground is *do not act on a stale answer*, and that ground does not
        transfer to NAMING an origin a page may embed.
        """
        if self.container is None:
            return None
        with self._lock:
            if self._cached is None:
                return None
            found, asked_at = self._cached
            return found if 0 <= self._clock() - asked_at < self._ttl else None

    def _ask(self) -> Editor | None:
        """Ask once. Any failure to get a clear answer is no editor."""
        try:
            answer = subprocess.run(
                [self.docker, "inspect", "--format", _INSPECT_FORMAT, "--", self.container],
                input="",
                capture_output=True,
                text=True,
                encoding="utf-8",
                errors="replace",
                timeout=self._inspect_timeout,
                check=False,
            )
        except OSError, ValueError, subprocess.SubprocessError:
            return None
        if answer.returncode != 0:
            return None
        return editor_from(answer.stdout, self.source_root)


def editor_from(inspected: str, source_root: Path | str, *, path=os.path) -> Editor | None:
    """Turn one `docker inspect` answer into an `Editor`, or `None`; `path` is the host's."""
    lines = inspected.splitlines()
    if not lines or lines[0].strip() != "true":
        return None
    bindings: list[tuple[str, str]] = []
    sources: list[tuple[str, str]] = []
    labelled: dict[str, str] = {}
    for line in lines[1:]:
        kind, _, rest = line.partition(FIELD)
        value, _, tail = rest.partition(FIELD)
        if kind == PORT and tail:
            bindings.append((value, tail))
        elif kind == MOUNT and tail:
            sources.append((value, tail))
        elif kind == LABEL:
            labelled[value] = tail.strip()
    mounts = labels.held(sources, labelled, source_root, path=path)
    if mounts is None:
        within = ((_within(source, Path(source_root)), inside) for source, inside in sources)
        mounts = [(base, inside) for base, inside in within if base is not None]
    published = {port for _, port in bindings}
    if len(published) != 1 or not mounts:
        return None
    port = published.pop()
    host = min(address for address, bound in bindings if bound == port)
    (base, folder), *others = sorted(mounts)
    return Editor(
        origin=f"{SCHEME}://{_reachable(host)}:{port}",
        folder=folder,
        base=base,
        others=tuple(others),
    )


def inside_base(base: str, path: str) -> str | None:
    """Return a source-root-relative `path`'s place under `base`, or `None`.

    ⛔ **Pure, and it refuses rather than repairs.** An absolute path, a path
    climbing out with `..`, and a path that is simply not under `base` all
    answer `None` — `exercise.safety` has already refused the first two on the
    way into the record, and a second reading here is belt to that brace
    because the answer addresses a file in somebody else's container.
    """
    if not path or path.startswith("/") or "\\" in path:
        return None
    parts = [segment for segment in path.split("/") if segment]
    if not parts or any(segment in TRAVERSAL for segment in parts):
        return None
    prefix = [segment for segment in base.split("/") if segment]
    if parts[: len(prefix)] != prefix or len(parts) == len(prefix):
        return None
    return "/".join(parts[len(prefix) :])


def _within(mounted: str, source_root: Path) -> str | None:
    """Return the mount's source relative to this source root, or `None` when it is outside.

    ⭐ `""` is the root itself — a real answer, and the one `Path.relative_to`
    spells `.`. ⛔ `None` is *not this corpus*, which is a different thing and
    is why this answers more than a bool.
    """
    if not mounted:
        return None
    try:
        relative = Path(mounted).resolve().relative_to(source_root.resolve())
    except OSError, ValueError:
        return None
    return "/".join(relative.parts)


def _reachable(host: str) -> str:
    """Return the address a browser is sent to for a binding on `host`."""
    if host.strip() in UNSPECIFIED:
        return LOOPBACK
    if ":" in host and not host.startswith("["):
        return f"[{host}]"
    return host
