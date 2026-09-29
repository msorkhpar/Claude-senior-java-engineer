"""Prove the workbench lockdown RUNS in a real session, before the image is tagged.

**What it does.** `prove(image)` starts a container from `image` with the
image's OWN command line, opens the workbench in a headless browser against an
untrusted bind-mounted folder — which is the shape every consumer serves — and
then reads the extension host's log out of that container. It returns a `Proof`
that is true only when BOTH lines are there: the host saying it activated
`studyforge.practice-focus`, and the extension's own banner saying it ran.

**Why it exists, and why the check it replaces could not work.** The lockdown's
stated guarantee was that *"an image whose lockdown did not load is not
tagged"*, and the check that carried it read
`code-server --list-extensions`. ⛔ **The extension was installed, listed,
present in `extensions.json`, parsed under the image's own node and inside its
engine range — and the extension host activated it in no session, with no
error**, because the workbench's Restricted Mode had disabled it. ⚠️ **A check
that reads INSTALLATION cannot see ACTIVATION**, and no amount of care with the
installed list would have caught it. This module observes the running thing
instead.

**How you use it.** `docker/editor/build.py` builds the image to an image ID,
runs this, and applies the tag only if it passes — so the guarantee is the
tag's. It also runs standalone against any image reference:

    python3 docker/editor/activation.py code-server-toolchain/editor:<tag>

**Depends on.** The standard library, a Docker CLI, and a Chromium-family
browser on the host. ⭐ **Any engine, Windows included**: the probe folder is a
NAMED VOLUME seeded through the Docker CLI's stdin, never a bind of a host
temporary directory, so an engine that shares no host `/tmp` (Docker Desktop)
or a host that has none (Windows) runs it unchanged. ⭐ `docker` is the plain
CLI, so `DOCKER_CONTEXT` (or the current context) picks the engine; nothing
here switches a context. ⛔ **A missing browser REFUSES rather than skips**: a gate
that can be absent is not a gate, and this one exists because the previous one
passed while the product was broken. ⛔ It mounts no Docker socket: it runs
`docker` from outside, like everything else here, because a socket inside a
container is root on the host.
"""

from __future__ import annotations

import json
import os
import shutil
import subprocess
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid
from dataclasses import dataclass
from pathlib import Path

HERE = Path(__file__).resolve().parent
COMPONENT = HERE.parents[1]
sys.path.insert(0, str(COMPONENT / "lockdown"))
import lockdown as lockdown_extension  # noqa: E402

if str(HERE) not in sys.path:
    sys.path.append(str(HERE))  # appended: `build` must stay the runner's for `build.py`
# ⭐ What a probe asks of the engine and of the host's browser lives in
# `engine.py`, split out at the 400-line bound; the names are re-exported here.
from engine import (  # noqa: E402, F401
    BROWSER_FLAGS, BROWSER_TAIL, DEFAULT_USER, SHORT_BASE, SOURCES, Browser, Refused, command_of, docker_cli,
    host_user, remove, seed, short_base, start,
)


#: The browsers this looks for, in order. ⭐ `STUDYFORGE_BROWSER` overrides the
#: list outright, so a host with one under another name states it rather than
#: patching this file.
BROWSERS = ("google-chrome", "google-chrome-stable", "chromium", "chromium-browser", "chrome")
BROWSER_ENV = "STUDYFORGE_BROWSER"
#: Where the image installs its extensions, which is the image's own `CMD`'s
#: `--extensions-dir`. Named here so a test can read the installed list of an
#: image this module refused.
EXTENSIONS_DIR = "/opt/code-server/extensions"
#: The one file the probe workspace holds, and the file the URL opens.
PROBE_FILE = "probe.txt"
PROBE_TEXT = "The activation probe opens this file. It belongs to no corpus.\n"
#: The extension-host logs, inside the container.
LOGS = "/home/coder/.local/share/code-server/logs"
HEALTH_TIMEOUT = 120.0
ACTIVATION_TIMEOUT = 180.0
POLL = 2.0


@dataclass(frozen=True)
class Proof:
    """What a real session showed. ⭐ `ok` is the conjunction, and nothing else is."""

    image: str
    activated: str | None
    banner: str | None
    #: What the extension host logged, for a refusal to quote rather than summarise.
    log: str
    #: The browser's own last lines, quoted by a refusal. ⛔ Never discarded: a
    #: browser that died is otherwise indistinguishable from a lockdown that did
    #: not run.
    browser: str = ""

    @property
    def ok(self) -> bool:
        return bool(self.activated) and bool(self.banner)

    def complaint(self) -> str:
        missing = []
        if not self.activated:
            missing.append("the extension host activated it in no session")
        if not self.banner:
            missing.append("the extension printed no banner, so it activated and did not run")
        said = f"\nthe browser's last lines:\n{self.browser}" if self.browser.strip() else ""
        return f"{self.image}: the lockdown did not run -- {'; '.join(missing)}{said}"


def browser(env=None) -> str:
    """The browser to drive the workbench with, or `Refused` naming what was looked for."""
    env = os.environ if env is None else env
    stated = env.get(BROWSER_ENV)
    if stated:
        found = shutil.which(stated) or (stated if Path(stated).is_file() else None)
        if not found:
            raise Refused(f"{BROWSER_ENV} is '{stated}', which is not an executable on this host")
        return found
    for name in BROWSERS:
        found = shutil.which(name)
        if found:
            return found
    raise Refused(f"no browser to prove the lockdown with: looked for {', '.join(BROWSERS)} on PATH, and "
                  f"{BROWSER_ENV} names none. The image is NOT tagged without this proof")


def workbench_url(port: int, folder: str = SOURCES, name: str = PROBE_FILE) -> str:
    """The URL a study page builds: one folder, one file, one window.

    ⭐ The `payload` is the extraction source's shape and the framework's
    (`studyforge.execute.workbench`), so the probe opens the workbench the same
    way a reader's page does rather than a way only the probe uses.
    """
    payload = json.dumps([["openFile", f"vscode-remote://127.0.0.1:{port}{folder}/{name}"]], separators=(",", ":"))
    query = urllib.parse.urlencode({"folder": folder, "payload": payload})
    return f"http://127.0.0.1:{port}/?{query}"


def prove(image: str, lock=None, keep: bool = False) -> Proof:
    """Run `image`, open a workbench in it, and read what the extension host did."""
    lock = lock or lockdown_extension.identity(COMPONENT / "lockdown")
    found = browser()
    name = f"lockdown-probe-{uuid.uuid4().hex[:12]}"
    try:
        seed(image, name, {PROBE_FILE: PROBE_TEXT})
        start(image, name, name)
        port = published_port(name)
        wait_for_health(port)
        proof = _drive(found, image, name, port, lock)
    finally:
        if not keep:
            remove(name, name)
    return proof


def published_port(name: str) -> int:
    published = docker_cli("port", name, "8080/tcp").stdout.strip().splitlines()
    if not published:
        raise Refused(f"{name} published no port for 8080/tcp")
    return int(published[0].rsplit(":", 1)[1])


def wait_for_health(port: int) -> None:
    deadline = time.monotonic() + HEALTH_TIMEOUT
    while time.monotonic() < deadline:
        try:
            with urllib.request.urlopen(f"http://127.0.0.1:{port}/healthz", timeout=5) as answer:
                if answer.status == 200:
                    return
        except (urllib.error.URLError, OSError, TimeoutError):
            pass
        time.sleep(POLL)
    raise Refused(f"the editor never answered /healthz on 127.0.0.1:{port} within {HEALTH_TIMEOUT:.0f}s")


def _drive(found: str, image: str, name: str, port: int, lock) -> Proof:
    """Open the workbench and poll the container's extension-host log until both lines are there.

    ⛔ A browser that exits ends the wait at once, and every refusal carries its
    last lines: a dead browser is otherwise "activated in no session".
    """
    activation = f"_doActivateExtension {lock.id}"
    with Browser(found, workbench_url(port)) as session:
        deadline, log = time.monotonic() + ACTIVATION_TIMEOUT, ""
        while time.monotonic() < deadline:
            log = exthost_log(name, lock)
            proof = _read(image, log, activation, lock.banner)
            if proof.ok:
                return proof
            if session.exited():
                break
            time.sleep(POLL)
        proof = _read(image, log, activation, lock.banner)
        if proof.ok:
            return proof
        said = session.tail() or f"(the browser printed nothing; it {'exited' if session.exited() else 'was still running'})"
        return Proof(proof.image, proof.activated, proof.banner, proof.log, said)


def _read(image: str, log: str, activation: str, banner: str) -> Proof:
    lines = log.splitlines()
    return Proof(image=image,
                 activated=next((line for line in lines if activation in line), None),
                 banner=next((line for line in lines if banner in line), None),
                 log=log)


def exthost_log(name: str, lock) -> str:
    """What a session wrote: the extension host's own log, and the extension's.

    ⭐ Two files and two different facts. `remoteexthost.log` is the WORKBENCH
    saying it activated the extension; `<logs>/exthost*/<id>/<record>` is the
    EXTENSION saying it ran, written by `extension.js` into the log directory
    the workbench hands it. ⚠️ An extension that activates and then throws
    produces the first and not the second, which is why the gate wants both.
    """
    read = subprocess.run(
        ["docker", "exec", name, "sh", "-c",
         f"cat {LOGS}/*/exthost*/remoteexthost.log {LOGS}/*/exthost*/{lock.id}/{lock.record} 2>/dev/null"],
        stdin=subprocess.DEVNULL, capture_output=True, text=True)
    return read.stdout


def main(argv: list[str]) -> int:
    if len(argv) != 1:
        print(f"usage: {Path(__file__).name} <image reference>", file=sys.stderr)
        return 2
    try:
        proof = prove(argv[0])
    except Refused as refusal:
        print(f"refused: {refusal}", file=sys.stderr)
        return 2
    if not proof.ok:
        print(f"refused: {proof.complaint()}", file=sys.stderr)
        return 1
    print(proof.activated.strip())
    print(proof.banner.strip())
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
