r"""A run in the runner, reached over the compose network: no `docker`, no socket.

**What it does.** Speaks the runner's run service (`assets/runservice.pl`) from
the serving process: `RemoteLauncher` starts one argv there and streams its
output as a process would, `stop` signals the run's tree by its token, and
`ServiceProbe` says whether the service answers at all.

**How you use it.** `Runner(root, service=Service("runner", 7123))` — the runner
then launches through this module instead of `docker exec`, and
`execute.published` is what builds that `Service` from the environment a
published compose hands the study server.

**Depends on.** `socket`, and `handle` for the launcher's shape. ⛔ Nothing here
starts a process or reaches a daemon: a run is a request on a socket, and the
answer is the run's bytes.

## ⛔ WHY A SERVICE AND NOT `docker exec`

⭐ **Published with one compose, the study server is a container too**, and
`docker exec` from inside a container needs the Docker socket, which is never
mounted into a serving process (spec §8.3, standing). ⭐ So the runner offers a
minimal run service on the compose file's INTERNAL network, which only the
study server and the runner join, and it publishes no port. The host-process
`serve` keeps `docker exec`: nothing here replaces it.

## ⭐ THE WIRE — FIXED FIELDS, NUL-SEPARATED, NEVER A SHELL LINE

A request is its fields, each ended by `NUL`, and the request is ended by one
more `NUL`:

- `run`: the run's token (`STUDYFORGE_RUN=<32 hex>`), the working directory
  relative to `/work`, then the argv, verbatim;
- `stop`: the token, then a signal name (`TERM`, `KILL`);
- `ping`: nothing.

⭐ The answer to `run` is frames: `O<n>\n` and then `n` bytes of the run's
output (stdout and stderr joined where the program runs, as `docker exec`
joins them), and last `X<status>\n`. ⛔ The argv is never joined into a
command line on either side: the service `exec`s it as the list it arrived as.

## ⛔ THE SERVICE RUNS ONLY WHAT THE CORPUS'S RECORDS ALLOW

⭐ The study server writes every argv the corpus's records name into the
runner's allowlist (`published.write_allowed`) before a run, and the service
refuses anything else by name. ⚠️ So a request from anything that reaches the
internal network is one of the commands a Run, a Submit or an example's test
would have started anyway.
"""

from __future__ import annotations

import socket
import subprocess
import threading
import time
import uuid
from collections.abc import Sequence
from dataclasses import dataclass

from studyforge.execute.errors import RunRefused
from studyforge.execute.handle import KILL

#: The variable that marks a run's processes, as `runner.RUN_TOKEN` spells it.
RUN_TOKEN = "STUDYFORGE_RUN"

#: The port the run service listens on, inside the internal network only.
SERVICE_PORT = 7123

#: The field and request terminator.
NUL = b"\x00"

#: How long connecting to the service may take before it is not up.
CONNECT_TIMEOUT = 3.0

#: How long a `ping` answer is believed, in seconds.
SERVICE_TTL = 10.0

#: The status a run gets when the service hung up without saying one.
LOST = 255


@dataclass(frozen=True, slots=True)
class Service:
    """Where the runner's run service answers: a compose service name and a port."""

    host: str
    port: int = SERVICE_PORT


def request(kind: str, *fields: str) -> bytes:
    """Return one request's bytes. ⛔ A field carrying a `NUL` cannot be framed and is refused."""
    parts = [kind, *fields]
    if any("\x00" in part for part in parts):
        raise RunRefused("a run's argument cannot carry a NUL byte")
    return b"".join(part.encode("utf-8") + NUL for part in parts) + NUL


def fresh_token() -> str:
    """Return a new run's token, `STUDYFORGE_RUN=<32 hex>`."""
    return f"{RUN_TOKEN}={uuid.uuid4().hex}"


class RemoteProcess:
    """A run in the runner, read as a process is: `stdout.readline()`, `wait()`, `returncode`.

    ⭐ It is its own `stdout`, so `handle.RunHandle` reads it exactly as it reads
    a local process, and the frames are decoded as they are read.
    """

    def __init__(self, connection: socket.socket) -> None:
        """Hold the open connection a `run` request was sent on."""
        self._connection = connection
        self._reader = connection.makefile("rb")
        self._pending = b""
        self._ended = threading.Event()
        self.returncode: int | None = None
        self.stdout = self
        self.pid = 0

    def readline(self) -> str:
        """Return the next line of the run's output, or `''` once the run has ended."""
        while b"\n" not in self._pending:
            if not self._frame():
                break
        if b"\n" in self._pending:
            line, _, self._pending = self._pending.partition(b"\n")
            return (line + b"\n").decode("utf-8", errors="replace")
        rest, self._pending = self._pending, b""
        return rest.decode("utf-8", errors="replace")

    def wait(self, timeout: float | None = None) -> int:
        """Return the run's status once it is known; raise `TimeoutExpired` past `timeout`."""
        if not self._ended.wait(timeout):
            raise subprocess.TimeoutExpired("run", timeout or 0)
        assert self.returncode is not None
        return self.returncode

    def close(self) -> None:
        """Hang up: the service ends a run whose client is gone."""
        try:
            self._connection.close()
        except OSError:
            pass

    def _frame(self) -> bool:
        """Read one frame; `False` once the run has ended or the service hung up."""
        if self._ended.is_set():
            return False
        try:
            head = self._reader.readline()
        except OSError:
            head = b""
        if head.startswith(b"O") and head[1:-1].isdigit():
            self._pending += self._reader.read(int(head[1:-1]))
            return True
        status = head[1:-1] if head.startswith(b"X") else b""
        self.returncode = int(status) if status.lstrip(b"-").isdigit() else LOST
        self._ended.set()
        self.close()
        return False


class RemoteLauncher:
    """Start the argv in the runner through its run service, in `/work/<cwd>`, marked by a token."""

    def __init__(self, service: Service, cwd: str) -> None:
        """Run in `/work/<cwd>`, every process of the run marked with a fresh token."""
        self.service = service
        self.cwd = cwd
        self.marker = fresh_token()

    def spawn(self, argv: Sequence[str]) -> RemoteProcess:
        """Send one `run` request and return the run, read as a process is."""
        payload = request("run", self.marker, self.cwd, *argv)
        connection = _connect(self.service)
        connection.sendall(payload)
        return RemoteProcess(connection)

    def signal(self, process: RemoteProcess, signum: int) -> None:
        """Signal the run's tree in the runner; a `KILL` also hangs the connection up."""
        name = "KILL" if signum == KILL else "TERM"
        try:
            with _connect(self.service) as connection:
                connection.sendall(request("stop", self.marker, name))
                connection.recv(64)
        except OSError, RunRefused:
            pass
        if name == "KILL":
            process.close()


class ServiceProbe:
    """Whether the run service answers `ping`, believed for `SERVICE_TTL` seconds."""

    def __init__(self, service: Service, *, ttl: float = SERVICE_TTL) -> None:
        """Remember where to ask; ask nothing until `up()`."""
        self.service = service
        self._ttl = ttl
        self._lock = threading.Lock()
        self._cached: tuple[bool, float] | None = None

    def up(self) -> bool:
        """Ask once per TTL. Any failure to hear `pong` is not up."""
        now = time.monotonic()
        with self._lock:
            if self._cached is not None and 0 <= now - self._cached[1] < self._ttl:
                return self._cached[0]
            answer = _ping(self.service)
            self._cached = (answer, now)
            return answer


def _ping(service: Service) -> bool:
    """Send `ping` and read the answer; anything but `pong` is no."""
    try:
        with _connect(service) as connection:
            connection.sendall(request("ping"))
            return connection.recv(16).startswith(b"pong")
    except OSError, RunRefused:
        return False


def _connect(service: Service) -> socket.socket:
    """Open a connection to the service, or raise `RunRefused` saying it is not answering."""
    try:
        connection = socket.create_connection((service.host, service.port), timeout=CONNECT_TIMEOUT)
    except OSError:
        raise RunRefused("the runner's run service is not answering; is the compose up?") from None
    connection.settimeout(None)
    return connection
