"""Which mode a run takes: the runner container when it is up, the host otherwise.

**What it does.** Asks the Docker CLI whether the corpus's runner container is
running AND mounts this source root at `/work`, and caches the answer for
`MODE_TTL` seconds.

**How you use it.** `probe = ModeProbe(source_root, container)`; `probe.mode()`
returns `CONTAINER` or `HOST`. `container=None` is host mode, always, with no
probe at all.

**Depends on.** `subprocess` — ⭐ a probe starts a process, and this package
is where the framework's execution starts them — and `time`.

## ⛔ The runner never starts, stops or builds a container

⭐ **Decided (spec §8.3): the READER starts the runner
container** with `code-server-toolchain`'s documented run line, and this module
only ASKS. A probe is `docker inspect`, which reads; the run itself is
`docker exec`, which reaches in from outside. ⛔ **The Docker socket is never
mounted into the serving process** (spec §8.3, rule 2) — the host's CLI is the
only thing that talks to the daemon, and it runs on the host.

## Up is not enough: it must mount THIS root

A container by the right name that mounts a different directory at `/work` —
an older checkout, another copy — would run the commands against files the
reader is not editing, and report a verdict about them. ⭐ So "up" means
running **and** its `/work` mount's source is this source root; anything else
is host mode, where the reader's files are the ones run.

⭐ **"This root" is told by labels where the compose file started it**
(`labels`): compose's host-side working directory names this checkout, and
the skill's `org.studyforge.binds` says the root is what `/work` holds. ⛔ The
mount's `Source` is the ENGINE's spelling, which on Docker Desktop for Windows
is a path inside its VM, so it is compared only for a container that carries
no such label — one started by hand — where the engine is the host's.

## Every failure to ask is HOST

No `docker` binary, no such container, a daemon that hangs past
`INSPECT_TIMEOUT`, output that does not parse — each answers `HOST`. ⛔ A run
must never fail because the probe did: host mode runs the same argv against the
same files.

## Cached briefly

A page polling for output must not fork `docker` on every tick, so an answer is
believed for `MODE_TTL` seconds; after that, the next run asks again, so a
container the reader starts or stops is noticed within that window.
"""

from __future__ import annotations

import os
import subprocess
import threading
import time
from collections.abc import Callable
from pathlib import Path

from studyforge.execute import labels

CONTAINER = "container"
HOST = "host"
MODES = (CONTAINER, HOST)

#: The CLI the probe and the run invoke, found on `PATH`.
DOCKER = "docker"

#: Where the runner container mounts the source root (`code-server-toolchain`'s README).
WORKDIR_IN_CONTAINER = "/work"

#: How long an answer is believed, in seconds.
MODE_TTL = 10.0

#: How long `docker inspect` may take before the answer is `HOST`.
INSPECT_TIMEOUT = 3.0

#: Whether it runs; the two labels (`labels`); then every mount as source and destination.
_INSPECT_FORMAT = (
    "{{.State.Running}}\n"
    '{{index .Config.Labels "' + labels.WORKING_DIR + '"}}\n'
    '{{index .Config.Labels "' + labels.BINDS + '"}}\n'
    "{{range .Mounts}}{{.Source}}\t{{.Destination}}\n{{end}}"
)


class ModeProbe:
    """Answer `CONTAINER` or `HOST` for one source root and one container name."""

    def __init__(
        self,
        source_root: Path,
        container: str | None,
        *,
        docker: str = DOCKER,
        clock: Callable[[], float] = time.monotonic,
        ttl: float = MODE_TTL,
        inspect_timeout: float = INSPECT_TIMEOUT,
    ) -> None:
        """Remember the root and name; ask nothing until `mode()`."""
        self.source_root = Path(source_root)
        self.container = container
        self.docker = docker
        self._clock = clock
        self._ttl = ttl
        self._inspect_timeout = inspect_timeout
        self._lock = threading.Lock()
        self._cached: tuple[str, float] | None = None

    def mode(self) -> str:
        """`CONTAINER` if the runner container is up over this root, else `HOST`."""
        if self.container is None:
            return HOST
        now = self._clock()
        with self._lock:
            if self._cached is not None:
                mode, asked_at = self._cached
                if 0 <= now - asked_at < self._ttl:
                    return mode
            mode = CONTAINER if self._is_up() else HOST
            self._cached = (mode, now)
            return mode

    def _is_up(self) -> bool:
        """Ask once. Any failure to get a clear yes is a no."""
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
            return False
        if answer.returncode != 0:
            return False
        return up_from(answer.stdout, self.source_root)


def up_from(inspected: str, source_root: Path | str, *, path=os.path) -> bool:
    """Whether one `docker inspect` answer is a running runner over this root at `/work`.

    ⭐ Labelled (the compose file started it): compose's working directory is this
    checkout's, the root is what `/work` holds, and `/work` is mounted. ⚠️ Not
    labelled: the mount's source is this root, which holds where the engine
    reports host paths. ⭐ `path` is the host's path module (`labels.this_checkout`).
    """
    lines = inspected.split("\n")
    if len(lines) < 3 or lines[0].strip() != "true":
        return False
    working_dir, binds = lines[1].strip(), labels.binds_of(lines[2].strip())
    mounts: dict[str, str] = {}
    for line in lines[3:]:
        source, tab, destination = line.partition("\t")
        if tab:
            mounts[destination] = source
    if binds is not None:
        return (
            labels.this_checkout(working_dir, source_root, path=path)
            and ("", WORKDIR_IN_CONTAINER) in binds
            and WORKDIR_IN_CONTAINER in mounts
        )
    mounted = mounts.get(WORKDIR_IN_CONTAINER, "")
    return bool(mounted) and _same_directory(Path(mounted), Path(source_root))


def _same_directory(mounted: Path, source_root: Path) -> bool:
    """Tell whether the container's `/work` is this source root, however spelled."""
    try:
        return mounted.resolve() == source_root.resolve()
    except OSError:
        return False
