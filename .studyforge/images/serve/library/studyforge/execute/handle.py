"""One run: its commands in sequence, their merged output, and exactly one exit line.

**What it does.** Starts each command as the one before it succeeds, yields
every output line as the process writes it, and ends the stream with one exit
line — the last status, `timeout`, or `stopped`.

**How you use it.** `Runner.start` returns a `RunHandle`; iterate
`handle.lines()`; call `handle.stop()` from another thread to end it early.
`exit_line(code)` spells the last line.

**Depends on.** `signal`, `subprocess`, `threading`, and a `Launcher` — the
mode's way to start a command and to signal what it started (`runner`).

## ⭐ The first failure ends the run

Commands run in order and a non-zero status stops the sequence, so a file that
fails to compile never has its tests "run" against nothing: ⛔ **a green result
from an empty run is the worst outcome a learner can be shown.**

## ⭐ Exactly one exit line, whatever happens

    --- exit <code> ---     the last command's status: 0 is every command passed
    --- exit timeout ---    the run hit its ceiling and was killed
    --- exit stopped ---    `stop()` was called

A command that could not be started at all (its program is not on `PATH`) is a
line saying so and the shell's conventional status, `127` — never an exception
out of the stream, because the stream's one promise is that it ends.

## Killing is the whole tree, TERM then KILL

The launcher starts each command in its own session, so a stop or a timeout
signals the process GROUP — the program and every child it started — rather
than orphaning children. `SIGTERM` first; then, once the command's own process
has gone or `grace` seconds have passed, `SIGKILL` to whatever of the group is
left — a child that ignores `TERM` outlives the parent that obeyed it.
⭐ On Windows the launcher makes the same two steps a console break to the
command's process group and a kill of its whole tree (`runner`).
Abandoning `lines()` (a client hung up) kills whatever is still alive, so a
disconnected page cannot leave a build running for ten minutes.

Size: this module is the handle only; the modes live in `runner`.
"""

from __future__ import annotations

import signal
import subprocess
import threading
from collections.abc import Callable, Iterator, Sequence
from typing import Protocol

#: ⭐ The two signals a stop sends, as numbers every host has. `SIGKILL` is a
#: POSIX name that Windows lacks, so it is its number there; each launcher turns
#: both into its own host's way of ending a tree (`runner._signal_group`).
STOP = int(signal.SIGTERM)
KILL = int(getattr(signal, "SIGKILL", 9))

EXIT_TIMEOUT = "timeout"
EXIT_STOPPED = "stopped"

#: The status of a command whose program could not be started.
NOT_STARTED = 127


def exit_line(code: int | str) -> str:
    """Spell the one line that ends every stream; `code` is a status or a word."""
    return f"--- exit {code} ---"


class Launcher(Protocol):
    """How one mode starts a command and signals what it started."""

    def spawn(self, argv: Sequence[str]) -> subprocess.Popen[str]:
        """Start `argv` in its own session, output merged into one text pipe."""
        ...

    def signal(self, process: subprocess.Popen[str], signum: int) -> None:
        """Send `signum` to everything `process` started."""
        ...


class RunHandle:
    """A sequence of commands, streamed; the first is started on construction.

    ⭐ Starting the first command here makes "a run is live" true from the moment
    `Runner.start` returns, so a server can refuse a second run without racing a
    lazy generator.
    """

    def __init__(
        self,
        commands: Sequence[Sequence[str]],
        launcher: Launcher,
        gate: Callable[[str], str],
        *,
        mode: str,
        timeout: float,
        grace: float,
    ) -> None:
        """Check nothing — `Runner.start` has — and start the first command."""
        self.commands = tuple(tuple(command) for command in commands)
        self.mode = mode
        self._launcher = launcher
        self._gate = gate
        self._grace = grace
        self._lock = threading.Lock()
        self._process: subprocess.Popen[str] | None = None
        self._codes: list[int] = []
        self._stopped = False
        self._timed_out = False
        self._done = False
        self._timer = threading.Timer(timeout, self._on_timeout)
        self._timer.daemon = True
        self._first = self._start(self.commands[0])
        self._timer.start()

    @property
    def returncode(self) -> int | None:
        """The last finished command's status, or `None` before any finished."""
        return self._codes[-1] if self._codes else None

    @property
    def running(self) -> bool:
        """Whether the run has neither ended, been stopped, nor timed out."""
        return not (self._done or self._stopped or self._timed_out)

    def lines(self) -> Iterator[str]:
        """Yield each gated output line as it arrives, then the exit line."""
        try:
            for index, command in enumerate(self.commands):
                started = self._first if index == 0 else self._start_if_live(command)
                if started is None:
                    break
                if isinstance(started, str):
                    yield self._gate(started)
                    self._codes.append(NOT_STARTED)
                    break
                assert started.stdout is not None
                for raw in iter(started.stdout.readline, ""):
                    yield self._gate(raw)
                code = started.wait()
                with self._lock:
                    self._process = None
                self._codes.append(code)
                if self._stopped or self._timed_out or code != 0:
                    break
            yield exit_line(self._verdict())
        finally:
            self._timer.cancel()
            self._close()

    def stop(self) -> bool:
        """Kill the live command's tree. `True` if there was a run to stop."""
        with self._lock:
            if self._done or self._stopped or self._timed_out:
                return False
            self._stopped = True
        self._terminate()
        return True

    def _verdict(self) -> int | str:
        if self._timed_out:
            return EXIT_TIMEOUT
        if self._stopped:
            return EXIT_STOPPED
        return self.returncode if self.returncode is not None else 0

    def _start(self, command: tuple[str, ...]) -> subprocess.Popen[str] | str:
        """Start `command`; a program that cannot be started is a line, not a raise."""
        try:
            process = self._launcher.spawn(command)
        except OSError:
            return f"{command[0]}: the program could not be started (is it on PATH?)"
        with self._lock:
            self._process = process
        return process

    def _start_if_live(self, command: tuple[str, ...]) -> subprocess.Popen[str] | str | None:
        with self._lock:
            if self._stopped or self._timed_out:
                return None
        return self._start(command)

    def _terminate(self) -> None:
        with self._lock:
            process = self._process
        if process is None:
            return
        self._launcher.signal(process, STOP)
        try:
            process.wait(timeout=self._grace)
        except subprocess.TimeoutExpired:
            pass
        # ⛔ KILL whatever is left even when the leader obeyed TERM: a child that
        # ignores TERM outlives its parent, and the group outlives both.
        self._launcher.signal(process, KILL)

    def _close(self) -> None:
        with self._lock:
            already = self._done
            self._done = True
        if not already:
            self._terminate()

    def _on_timeout(self) -> None:
        with self._lock:
            if self._done or self._stopped:
                return
            self._timed_out = True
        self._terminate()
