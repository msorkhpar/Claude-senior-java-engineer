"""One practice-shaped session in an editor image: a container, a folder and a browser at its workbench.

**What it does.** `session(image, kind)` starts a container from `image` with
the image's own command line, mounts a folder shaped like a practice (the file
the URL opens, and a second file to be tempted by) as a named volume seeded
through the Docker CLI -- never a host temporary directory, so it runs on any
engine, Windows included -- waits for the server's
health, and opens a headless browser with its debugger at the workbench URL a
study page builds. It yields `(container, port, debugger port)` and removes the
container afterwards, whatever happened. `wait_for_editor(session)` answers
where the editor's own text area is, once the workbench has opened the file.

**Why it is a module of its own.** The confinement gate (`confinement.py`)
and its seed generator both open THIS session, and must open the same shape of
session or what the one measures is not what the other writes down. ⚠️ It was
split out of `confinement.py` when that file reached the 400-line bound,
with no change to what either does: the names below are re-exported there.

**Depends on.** The standard library, `activation.py` (container, browser and
the workbench URL, through `engine.py`) and `cdp.py`. ⛔ A probe that fails
quotes the browser's own last lines, which are never discarded. ⛔ A missing browser REFUSES rather than skips,
through `activation.browser()`; no Docker socket is mounted: it runs `docker`
from outside, like everything else here.
"""

from __future__ import annotations

import contextlib
import socket
import sys
import time
import uuid
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import activation  # noqa: E402
import cdp  # noqa: E402

Refused = activation.Refused

#: The probe workspace -- the file the URL opens and a second one to be
#: tempted by.
MAIN, OTHER = "practice.txt", "judge.txt"
MAIN_TEXT = "The confinement probe edits this file. It belongs to no corpus.\n"
OTHER_TEXT = "The confinement probe never opens this one.\n"

READY_TIMEOUT = 180.0


def free_port() -> int:
    """A port nothing is listening on, for the browser's debugger."""
    with socket.socket() as held:
        held.bind(("127.0.0.1", 0))
        return held.getsockname()[1]


@contextlib.contextmanager
def session(image: str, kind: str):
    """One container of `image`, one practice-shaped folder and one browser at its workbench.

    ⭐ The setup both readings need, so neither can drift from the other: the
    behavioural proof and the generator must open the SAME shape of session or
    what the one measures is not what the other writes down.
    """
    found = activation.browser()
    name = f"{kind}-probe-{uuid.uuid4().hex[:12]}"
    try:
        activation.seed(image, name, {MAIN: MAIN_TEXT, OTHER: OTHER_TEXT})
        activation.start(image, name, name)
        port = activation.published_port(name)
        activation.wait_for_health(port)
        with _browser(found, port) as debug:
            yield name, port, debug
    finally:
        activation.remove(name, name)


class _browser:
    """The headless browser, with its debugger open, for as long as the probe needs it.

    ⛔ A refusal raised while it is open carries the browser's last lines.
    """

    def __init__(self, found: str, port: int):
        self.debug = free_port()
        url = activation.workbench_url(port, name=MAIN)
        self.session = activation.Browser(found, f"--remote-debugging-port={self.debug}", url)

    def __enter__(self) -> int:
        return self.debug

    def __exit__(self, kind, error, _trace) -> None:
        said = self.session.tail()
        self.session.close()
        if isinstance(error, (Refused, cdp.CdpError)) and said and not getattr(error, "browser", None):
            error.browser = said
            error.args = (f"{error.args[0] if error.args else error}\nthe browser's last lines:\n{said}",)


_EDITOR_BOX = """(() => {
  const lines = document.querySelector('.monaco-editor .view-lines');
  if (!lines) { return null; }
  const box = lines.getBoundingClientRect();
  if (box.width < 10 || box.height < 5) { return null; }
  return {x: Math.round(box.x + 20), y: Math.round(box.y + 6)};
})()"""


def wait_for_editor(session) -> dict:
    """The editor's own text area, once the workbench has opened the URL's file."""
    deadline = time.monotonic() + READY_TIMEOUT
    while time.monotonic() < deadline:
        box = cdp.evaluate(session, _EDITOR_BOX)
        if box:
            time.sleep(4.0)  # let the lockdown's own closing passes settle first
            return box
        time.sleep(2.0)
    raise Refused(f"the workbench never showed an editor for {MAIN} within {READY_TIMEOUT:.0f}s")
