"""A headless browser this machine already has, asked for a picture and the DOM of one page.

**What it does.** Finds a Chromium-family browser — the one named, or the first
of `BROWSER_NAMES` on `PATH` — and runs it headless over one page's `file://`
address twice: once for a screenshot, once for the DOM as the page's own scripts
left it. Every launch shares one throwaway profile, removed when the look ends.

**How you use it.**

    binary = find_browser(None)              # or find_browser("/path/to/chrome")
    with BrowserProfile() as profile:
        seen = capture_page(binary, profile, page_path, png, dom)
        seen.ok, seen.detail

**Depends on.** `shutil`, `subprocess`, `tempfile` and `os`: the standard
library. ⛔ No driver library and no protocol client: the browser's own
`--screenshot` and `--dump-dom` flags are the whole interface.

## ⛔ Why a browser launch lives in the runner's package

Spec §8.3 keeps every process the framework starts in `execute`, so the one
place process starts are audited stays one place. ⭐ `studyforge.look` hands the
launch here rather than starting a process itself. ⛔ It is not a run: no
corpus command reaches it, its argv is this module's fixed flags around a page
address, and it never touches a container.

## ⛔ Nothing leaves the machine, and nothing is left behind

The launch flags turn off the browser's own background traffic, and a built page
issues no request of its own (R8), so a look reaches no network. ⭐ The profile is
a fresh temporary directory, removed on the way out whether the look succeeded
or not.

## ⚠️ The browser's sandbox is on wherever this machine can provide it

A Chromium-family browser refuses to start as root with its sandbox on, and in a
container without user namespaces it dies by a signal before drawing anything.
⭐ So `--no-sandbox` is passed as root, and a launch that a signal ended is
tried once more without the sandbox; a page captured that way says so on its
line. The pages are the reader's own built site, opened with the network off.

## ⛔ The browser's words are not echoed

Its output names the `file://` address it opened, which is an absolute path on
this machine (R7). A failure is reported by its exit code, never by quoting it.
"""

from __future__ import annotations

import os
import shutil
import subprocess
import tempfile
from dataclasses import dataclass
from pathlib import Path

#: The names looked for on `PATH`, in order. ⛔ Chromium-family only: the two
#: flags this module relies on are theirs.
BROWSER_NAMES = (
    "chrome-headless-shell",
    "headless-shell",
    "chromium",
    "chromium-browser",
    "google-chrome",
    "google-chrome-stable",
    "chrome",
)

#: The window every screenshot is taken at, so two looks compare.
WINDOW = (1280, 900)

#: Milliseconds of page time the browser grants a page's scripts before it reads.
SCRIPT_BUDGET_MS = 3000

#: Seconds one launch may take before it is stopped and reported.
LAUNCH_TIMEOUT = 90

#: The first bytes of every PNG file.
PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"

#: Flags every launch carries: headless, no first-run work, no background traffic.
FLAGS = (
    "--headless",
    "--disable-gpu",
    "--hide-scrollbars",
    "--no-first-run",
    "--no-default-browser-check",
    "--disable-extensions",
    "--disable-background-networking",
    "--disable-component-update",
    "--disable-default-apps",
    "--disable-sync",
    "--force-device-scale-factor=1",
    f"--window-size={WINDOW[0]},{WINDOW[1]}",
    f"--virtual-time-budget={SCRIPT_BUDGET_MS}",
)


def find_browser(named: str | None) -> str | None:
    """Return the browser to run: `named` when it runs, else the first candidate on `PATH`."""
    if named:
        return shutil.which(named) or (named if os.access(named, os.X_OK) else None)
    return next((path for path in map(shutil.which, BROWSER_NAMES) if path), None)


class BrowserProfile:
    """A throwaway browser profile directory, removed when the `with` block ends."""

    def __enter__(self) -> Path:
        """Make the directory and return it."""
        self.path = Path(tempfile.mkdtemp(prefix="studyforge-look-"))
        return self.path

    def __exit__(self, *exc: object) -> None:
        """Remove the directory, whatever the look did."""
        shutil.rmtree(self.path, ignore_errors=True)


@dataclass(frozen=True, slots=True)
class PageSeen:
    """What one page's look produced: whether both captures worked, and why not."""

    ok: bool
    detail: str


def capture_page(binary: str, profile: Path, page: Path, png: Path, dom: Path) -> PageSeen:
    """Screenshot `page` into `png` and write its DOM into `dom`, then check both.

    ⚠️ Sandboxed first unless running as root; a launch a signal ended is taken
    once more without the sandbox, and the answer says so.
    """
    sandboxed = not _root()
    seen = _capture(binary, profile, page, png, dom, sandboxed)
    if sandboxed and not seen.ok and SIGNALLED in seen.detail:
        again = _capture(binary, profile, page, png, dom, sandboxed=False)
        return PageSeen(True, UNSANDBOXED) if again.ok else again
    return seen


#: What a failure ended by a signal says, which is how a sandbox that cannot start ends.
SIGNALLED = "was ended by signal"

#: What a page captured without the browser's sandbox says on its line.
UNSANDBOXED = "rendered, without the browser's sandbox, which this machine cannot provide"


def _capture(
    binary: str, profile: Path, page: Path, png: Path, dom: Path, sandboxed: bool
) -> PageSeen:
    """One attempt at both captures, with the sandbox on or off."""
    url = page.resolve().as_uri()
    flags = _flags(profile, sandboxed)
    _, failed = _launch([binary, *flags, f"--screenshot={png}", url])
    if failed is not None:
        return PageSeen(False, f"the screenshot launch {failed}")
    if not png.is_file() or not png.read_bytes().startswith(PNG_SIGNATURE):
        return PageSeen(False, "the browser exited 0 and wrote no PNG")
    text, failed = _launch([binary, *flags, "--dump-dom", url])
    if failed is not None:
        return PageSeen(False, f"the DOM launch {failed}")
    if "<body" not in text:
        return PageSeen(False, "the DOM the browser printed has no body")
    dom.write_text(text, encoding="utf-8", newline="\n")
    return PageSeen(True, "rendered")


def _root() -> bool:
    """Whether this process runs as root, where the browser refuses its sandbox."""
    return hasattr(os, "geteuid") and os.geteuid() == 0


def _flags(profile: Path, sandboxed: bool) -> list[str]:
    """Every launch's flags, with the profile, and `--no-sandbox` when it is off."""
    return [*FLAGS, f"--user-data-dir={profile}", *([] if sandboxed else ["--no-sandbox"])]


def _launch(argv: list[str]) -> tuple[str, str | None]:
    """Run one launch; return its standard output and `None`, or `""` and how it failed."""
    try:
        done = subprocess.run(  # noqa: S603 - the browser this module found, fixed flags
            argv,
            capture_output=True,
            text=True,
            timeout=LAUNCH_TIMEOUT,
            check=False,
            input="",
        )
    except subprocess.TimeoutExpired:
        return "", f"did not finish within {LAUNCH_TIMEOUT} s"
    except OSError:
        return "", "did not start"
    if done.returncode < 0:
        return "", f"{SIGNALLED} {-done.returncode}"
    if done.returncode != 0:
        return "", f"exited {done.returncode}"
    return done.stdout, None
