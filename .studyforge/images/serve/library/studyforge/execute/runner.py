"""`Runner` — start a corpus's commands in the runner container when it is up, else on the host.

**What it does.** Checks the commands, asks which mode this run takes, and
returns a `RunHandle` streaming the run — the same argv, in the same directory
relative to the source root, in either mode.

**How you use it.**

    runner = Runner(source_root, container_for(source))
    handle = runner.start([exercise.run_command, exercise.test_command])
    for line in handle.lines(): ...

`container=None` runs on the host, always. ⭐ `service=Service(...)` runs in
the runner through its run service instead (`remote`), which is how a study
server published with one compose reaches it without the Docker socket; it
never falls back to the host, since the host there is the study server's own
container.

**Depends on.** `subprocess` and `os` — ⭐ `execute` is the only package that
runs a corpus's commands — plus `commands`, `mode`, `output` and `handle`
beside it.

## The two modes, one contract

| | container | host |
|---|---|---|
| how | `docker exec -w /work/<cwd> <c> sh -c MERGE_STDERR sh <argv…>` | `<argv…>` in `<cwd>` |
| the argv | ⭐ **verbatim**, never parsed | ⭐ **verbatim**, never parsed |
| environment | `RUN_ENVIRONMENT`, with `-e` | the host's, plus `RUN_ENVIRONMENT` |
| output | `/work` made relative, then scrubbed | the root made relative, then scrubbed |
| stop, timeout | the run's tree by its token, then the client | the command's process group |

⭐ **On Windows the host mode keeps the same contract another way**: a command
leads a new process GROUP (there are no sessions), a stop is a console break
to that group, and a kill ends the whole tree with `taskkill /T /F` — there is
no `killpg` and no `SIGKILL`, so both signals are the `handle` module's own
numbers and each launcher turns them into its host's own way of ending a tree.

⛔ **The runner never starts, stops or builds a container** (spec §8.3's
seam): the reader starts it, and a stop here ends the
RUN's processes inside it, never the container. ⛔ **No socket is mounted
anywhere** (spec §8.3): the host's `docker` CLI reaches in from outside.

## ⭐ `RUN_ENVIRONMENT` — decided here, the same in both modes

- `PYTHONDONTWRITEBYTECODE=1`: a grader imports the file under test,
  and Python would write `__pycache__/` beside it — into the reader's tree,
  which a run must leave as it found it.
- `PYTHONUNBUFFERED=1`: a Python program writing to a pipe buffers its output
  in blocks, so "line by line as the process writes it" would arrive in lumps,
  and stdout and stderr would interleave differently from a terminal.

⚠️ **Both reach a Python program only.** A JVM or a Node build that writes a
cache into the tree is not stopped by them, and that class of write is a
corpus's ignore rules, not this module's.

## Why stderr is joined INSIDE the container

⚠️ `docker exec` without a terminal carries stdout and stderr as two streams,
and the client writes each to its own descriptor — so a program's
`out, err, out` arrives here as `out, out, err`, where the host's single pipe
keeps the order. ⭐ So the join happens where the program runs: `sh -c
MERGE_STDERR sh <argv…>` points the program's stderr at its stdout and then
`exec`s it — the argv as positional arguments, verbatim, and the same process
(the run token and the kill reach it unchanged). ⛔ A terminal (`-t`) would
also join them, and would change line endings, colour and width — a different
program's output, not the same one.

## Why a run token inside the container

⚠️ **`docker exec` forwards no signal**: killing the local client leaves the
command running inside the container, where a bare `pytest` finishes on its
own. ⭐ So each run carries
`STUDYFORGE_RUN=<a fresh random token>` in its environment, every child inherits
it, and a stop or timeout runs `KILL_BY_TOKEN` inside the container — a fixed
`sh` program of this module's, taking the token and a signal name as arguments,
never text a client sent — which signals every process carrying that token.
That is the container's process group: the same set, found the only way a
process outside the container can find it.
"""

from __future__ import annotations

import os
import signal
import subprocess
import uuid
from collections.abc import Iterable, Sequence
from pathlib import Path

from studyforge.execute.commands import (
    ROOT_DIR,
    require_commands,
    require_container,
    require_workdir,
)
from studyforge.execute.errors import RunRefused
from studyforge.execute.handle import KILL, RunHandle
from studyforge.execute.mode import CONTAINER, DOCKER, HOST, WORKDIR_IN_CONTAINER, ModeProbe
from studyforge.execute.output import LineGate
from studyforge.execute.remote import RemoteLauncher, Service, ServiceProbe

#: The mode a run takes through the runner's run service.
SERVICE = "service"

#: Why a corpus that declares its runner runs nothing while it is down.
RUNNER_DOWN = (
    "this course's runner is not running, and its code runs nowhere else; start it with "
    'the one command under "Bring it up" in its EXECUTION.md'
)

#: What every run's environment carries, in both modes.
RUN_ENVIRONMENT = {"PYTHONDONTWRITEBYTECODE": "1", "PYTHONUNBUFFERED": "1"}

#: The variable that marks a run's processes inside the container.
RUN_TOKEN = "STUDYFORGE_RUN"

#: Hard ceiling on one run, in seconds: generous for a cold first build.
DEFAULT_TIMEOUT = 600.0

#: Seconds between `SIGTERM` and `SIGKILL`.
GRACE = 5.0

#: How long the in-container kill may take before it is abandoned.
KILL_TIMEOUT = 10.0

#: Run `"$@"` with its stderr joined to its stdout INSIDE the container. ⛔ Fixed
#: text: the argv arrives as positional arguments and `exec "$@"` hands it on
#: verbatim — nothing is interpolated, quoted or split.
MERGE_STDERR = 'exec "$@" 2>&1'

#: Signal every process whose environment carries `$1` (`NAME=token`) with `$2`.
#: ⛔ Fixed text: its only inputs are this module's token and a signal name.
KILL_BY_TOKEN = (
    "for p in /proc/[0-9]*; do "
    'if tr "\\000" "\\n" < "$p/environ" 2>/dev/null | grep -qxF -- "$1"; then '
    'kill -s "$2" "${p#/proc/}" 2>/dev/null; fi; done; exit 0'
)


#: ⭐ Windows' flag for a new process group, whose id is the leader's pid, which a
#: console break reaches whole. The number where `subprocess` does not name it.
NEW_PROCESS_GROUP = getattr(subprocess, "CREATE_NEW_PROCESS_GROUP", 0x00000200)

#: Windows' console break, which a process group started with `NEW_PROCESS_GROUP`
#: receives: its polite stop, as `SIGTERM` is POSIX's.
CTRL_BREAK = getattr(signal, "CTRL_BREAK_EVENT", 1)

#: Windows' tree kill: every process the leader started, then the leader.
TREE_KILL = ("taskkill", "/T", "/F", "/PID")


def _own_group() -> dict[str, object]:
    """Return what starts a command as the leader of its own group, on this host.

    ⛔ POSIX: a new session, which `os.killpg` reaches whole. ⭐ Windows has no
    sessions or `killpg`; a new process group is the unit a console break reaches.
    """
    if os.name == "nt":
        return {"creationflags": NEW_PROCESS_GROUP}
    return {"start_new_session": True}


def _pipe(argv: Sequence[str], **where: object) -> subprocess.Popen[str]:
    """Start `argv` in its own session, stdout and stderr merged, line-buffered text.

    ⭐ Its stdin is a pipe closed at once — end of input, as `/dev/null` would
    give — so starting a run opens no file at all: `/dev/null` is opened for
    WRITING by `DEVNULL`, and the emission census counts every such open.
    """
    process = subprocess.Popen(
        list(argv),
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        encoding="utf-8",
        errors="replace",
        bufsize=1,
        **_own_group(),  # type: ignore[arg-type]
        **where,  # type: ignore[arg-type]
    )
    assert process.stdin is not None
    process.stdin.close()
    return process


def _signal_group(process: subprocess.Popen[str], signum: int) -> None:
    """Signal the group `process` leads. Gone already is not an error.

    ⭐ On Windows, where there is no `killpg`: `STOP` is a console break to the
    group, and `KILL` ends the whole tree (`_end_tree`).
    """
    if os.name == "nt":
        _end_tree(process, signum)
        return
    try:
        os.killpg(process.pid, signum)
    except ProcessLookupError, PermissionError:
        pass


def _end_tree(process: subprocess.Popen[str], signum: int) -> None:
    """Windows: break the group `process` leads, or kill its tree. Gone is not an error.

    ⚠️ A break reaches only a group that shares a console; a group that does not
    ignores it, and the `KILL` a stop always sends after the grace ends it.
    """
    if process.poll() is not None and signum != KILL:
        return
    if signum != KILL:
        try:
            process.send_signal(CTRL_BREAK)
        except OSError, ValueError:
            pass
        return
    try:
        subprocess.run(
            [*TREE_KILL, str(process.pid)],
            input=b"",
            capture_output=True,
            timeout=KILL_TIMEOUT,
            check=False,
        )
    except OSError, subprocess.SubprocessError:
        pass
    try:
        process.kill()
    except OSError:
        pass


class HostLauncher:
    """Run the argv on the host, in `<source root>/<cwd>`."""

    def __init__(self, source_root: Path, cwd: str) -> None:
        """Run in `<source root>/<cwd>` with the host environment plus `RUN_ENVIRONMENT`."""
        self.directory = source_root / cwd
        self.environment = {**os.environ, **RUN_ENVIRONMENT}

    def spawn(self, argv: Sequence[str]) -> subprocess.Popen[str]:
        """Start `argv` on the host, in its own session."""
        return _pipe(argv, cwd=self.directory, env=self.environment)

    def signal(self, process: subprocess.Popen[str], signum: int) -> None:
        """Signal the command's process group."""
        _signal_group(process, signum)


class ContainerLauncher:
    """Run the argv inside the runner container, in `/work/<cwd>`, marked by a token."""

    def __init__(self, container: str, cwd: str, docker: str = DOCKER) -> None:
        """Run in `/work/<cwd>` inside `container`, every process marked with a fresh token."""
        self.container = container
        self.docker = docker
        self.workdir = WORKDIR_IN_CONTAINER if cwd == ROOT_DIR else f"{WORKDIR_IN_CONTAINER}/{cwd}"
        self.marker = f"{RUN_TOKEN}={uuid.uuid4().hex}"

    def spawn(self, argv: Sequence[str]) -> subprocess.Popen[str]:
        """Start `argv` inside the container through `docker exec`, verbatim."""
        environment = [
            part for key, value in RUN_ENVIRONMENT.items() for part in ("-e", f"{key}={value}")
        ]
        return _pipe(
            [
                self.docker,
                "exec",
                "-w",
                self.workdir,
                *environment,
                "-e",
                self.marker,
                self.container,
                "sh",
                "-c",
                MERGE_STDERR,
                "sh",
                *argv,
            ]
        )

    def signal(self, process: subprocess.Popen[str], signum: int) -> None:
        """Signal the run's tree inside the container first, then the local client."""
        try:
            subprocess.run(
                [
                    self.docker,
                    "exec",
                    self.container,
                    "sh",
                    "-c",
                    KILL_BY_TOKEN,
                    "sh",
                    self.marker,
                    "KILL" if signum == KILL else "TERM",
                ],
                input=b"",
                capture_output=True,
                timeout=KILL_TIMEOUT,
                check=False,
            )
        except OSError, subprocess.SubprocessError:
            pass
        _signal_group(process, signum)


class Runner:
    """Start a source root's commands; the only way the framework runs one."""

    def __init__(
        self,
        source_root: Path,
        container: str | None = None,
        *,
        docker: str = DOCKER,
        timeout: float = DEFAULT_TIMEOUT,
        grace: float = GRACE,
        probe: ModeProbe | None = None,
        service: Service | None = None,
        required: bool = False,
    ) -> None:
        """Check the container name; the mode is asked per run, through `probe`.

        ⛔ `required` is a corpus that DECLARES its runner (`instance.declares_runner`):
        its runs never fall back to the host, and a runner that is not up refuses them.
        """
        self.source_root = Path(source_root).absolute()
        self.container = None if container is None else require_container(container)
        self.docker = docker
        self.timeout = timeout
        self.grace = grace
        self.probe = probe or ModeProbe(self.source_root, self.container, docker=docker)
        self.service = service
        self.service_probe = None if service is None else ServiceProbe(service)
        self.required = required or service is not None

    def mode(self) -> str:
        """`SERVICE`, `CONTAINER` or `HOST`, as the next run would take it.

        ⛔ **A required runner never answers `HOST`** — one given a service, or
        one its corpus declares: the host is not where that corpus's code runs
        (published, it is the study server's own container, with no toolchain).
        A runner that is not up refuses the run, and the run index says so.
        """
        if self.service_probe is not None:
            if not self.service_probe.up():
                raise RunRefused("the runner's run service is not answering; is the compose up?")
            return SERVICE
        mode = self.probe.mode()
        if mode == HOST and self.required:
            raise RunRefused(RUNNER_DOWN)
        return mode

    def start(self, commands: object, cwd: object = ROOT_DIR) -> RunHandle:
        """Run `commands` in order from `cwd`; the first is started before this returns.

        ⛔ `commands` is argv lists from a generated document on disk. It is
        checked (`commands.require_commands`) and never composed, split or
        quoted here.
        """
        checked = require_commands(commands)
        directory = require_workdir(cwd)
        mode = self.mode()
        if mode == SERVICE:
            assert self.service is not None
            launcher: HostLauncher | ContainerLauncher | RemoteLauncher = RemoteLauncher(
                self.service, directory
            )
            roots: Iterable[str] = (WORKDIR_IN_CONTAINER,)
        elif mode == CONTAINER:
            assert self.container is not None
            launcher = ContainerLauncher(self.container, directory, self.docker)
            roots = (WORKDIR_IN_CONTAINER,)
        else:
            launcher = HostLauncher(self.source_root, directory)
            roots = (str(self.source_root), str(self.source_root.resolve()))
        return RunHandle(
            checked, launcher, LineGate(roots), mode=mode, timeout=self.timeout, grace=self.grace
        )
