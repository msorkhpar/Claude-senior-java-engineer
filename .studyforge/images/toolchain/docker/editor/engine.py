"""What a probe asks of the engine and of the host's browser: a seeded volume, a container, a session.

**What it does.** `seed(image, volume, files)` makes a NAMED VOLUME holding a
probe's files; `start(image, name, volume)` runs the image's own command line
with that volume at `SOURCES`; `remove(name, volume)` takes both away.
`Browser(found, url)` is a headless browser in a throwaway profile whose output
is kept, so a refusal can quote it (`tail()`).

**Why it exists.** ⛔ **No probe hands the engine a host temporary directory.**
Docker Desktop shares no host `/tmp` and refuses such a bind, and Windows has
no `/tmp` at all: the register's direction is that every docker step runs on
any engine, Windows included. ⭐ So the files cross as a tar stream on the
Docker CLI's stdin, into a volume the engine owns, and nothing on the host is
mounted. ⛔ **And no browser output is discarded**: with its `TMPDIR` too long,
headless Chrome dies with "Socket path too long" before it opens a page, and a
probe that sent its output to `/dev/null` reported only that the lockdown
"activated in no session". ⭐ The browser's profile and `TMPDIR` are one short
directory (`short_base()`), and a refusal carries its last lines.

**Depends on.** The standard library and a Docker CLI. ⭐ `docker` is always
the plain CLI with the caller's environment, so `DOCKER_CONTEXT` (or the
current context) chooses the engine; nothing here names or switches a context.
It was split out of `activation.py` at the 400-line bound; `activation`
re-exports every name, so callers are unchanged.
"""

from __future__ import annotations

import io
import json
import os
import shutil
import subprocess
import tarfile
import tempfile
from pathlib import Path
from typing import Mapping


class Refused(Exception):
    """A probe that could not be run, or an image that did not pass it."""


#: Headless, its own throwaway profile, and nothing of the host's: this must
#: never touch a person's browser while it runs on their machine.
BROWSER_FLAGS = ("--headless=new", "--disable-gpu", "--disable-dev-shm-usage", "--no-first-run",
                 "--no-default-browser-check", "--disable-extensions", "--mute-audio")
#: Where the consuming contract binds a corpus's sources. The folder is a MOUNT
#: the workbench has never been told to trust, which is exactly the condition
#: the lockdown has to survive. ⭐ The probe's mount is a named volume, not a
#: host bind: the workbench cannot tell the two apart, and the engine needs no
#: host path for it.
SOURCES = "/home/coder/repo/sources"
#: The uid and gid a probe container runs as when the host has none to lend
#: (Windows): the image's own `coder`.
DEFAULT_USER = (1000, 1000)
#: How many of the browser's own last lines a refusal quotes.
BROWSER_TAIL = 20
#: The longest temporary base a POSIX browser's singleton socket fits under.
#: ⚠️ Chromium makes `$TMPDIR/.<vendor>.XXXXXX/SingletonSocket` (about 42
#: characters) under a per-session directory, and a Unix socket path holds 107:
#: a longer base kills the browser with "Socket path too long" before it opens
#: a page.
SHORT_BASE = 50


def command_of(image: str) -> list[str]:
    """The image's OWN command line, with authentication turned off for the probe.

    ⛔ The probe runs what the image SHIPS -- the defect this gate exists for was a flag the contract
    carried and the image's `CMD` did not, so a probe that invented its own
    command line would have proved nothing. ⭐ Only `--auth` is added: a
    password would have to be generated, held and then kept out of a log, and
    the probe has no business owning a secret.
    """
    inspected = docker_cli("image", "inspect", "--format", "{{json .Config.Cmd}}", image)
    cmd = json.loads(inspected.stdout.strip() or "null")
    if not cmd:
        raise Refused(f"{image} declares no CMD, so there is no shipped command line to prove")
    return list(cmd) + ["--auth=none"]


def host_user() -> str:
    """`uid:gid` for a probe container: the host's where it has one, else the image's `coder`."""
    getuid, getgid = getattr(os, "getuid", None), getattr(os, "getgid", None)
    uid, gid = (getuid(), getgid()) if getuid and getgid else DEFAULT_USER
    return f"{uid}:{gid}"


def seed(image: str, volume: str, files: Mapping[str, str | bytes]) -> None:
    """Create the named volume `volume` holding `files`, owned by `host_user()`.

    ⭐ The bytes cross as a tar stream on the Docker CLI's stdin into a
    throwaway container of `image` itself, so no host path is ever handed to
    the engine: this is what lets Docker Desktop, which shares no host `/tmp`,
    and Windows, which has none, run the probe. ⭐ Extracted as root, so the
    archive's own numeric owner lands and the probe's user can edit the files.
    """
    uid, gid = (int(part) for part in host_user().split(":"))
    stream = io.BytesIO()
    with tarfile.open(fileobj=stream, mode="w", format=tarfile.USTAR_FORMAT) as archive:
        root = tarfile.TarInfo(".")
        root.type, root.mode, root.uid, root.gid = tarfile.DIRTYPE, 0o755, uid, gid
        archive.addfile(root)
        made = set()
        for relative in sorted(files):
            parts = relative.split("/")
            for depth in range(1, len(parts)):
                folder = "/".join(parts[:depth])
                if folder not in made:
                    made.add(folder)
                    entry = tarfile.TarInfo(folder)
                    entry.type, entry.mode, entry.uid, entry.gid = tarfile.DIRTYPE, 0o755, uid, gid
                    archive.addfile(entry)
            data = files[relative]
            data = data.encode("utf-8") if isinstance(data, str) else data
            entry = tarfile.TarInfo(relative)
            entry.size, entry.mode, entry.uid, entry.gid = len(data), 0o644, uid, gid
            archive.addfile(entry, io.BytesIO(data))
    docker_cli("volume", "create", volume)
    done = subprocess.run(
        ["docker", "run", "--rm", "-i", "--network", "none", "--user", "0:0", "--entrypoint", "tar",
         "-v", f"{volume}:/seed", image, "-x", "--numeric-owner", "-f", "-", "-C", "/seed"],
        input=stream.getvalue(), capture_output=True)
    if done.returncode != 0:
        said = (done.stderr or done.stdout).decode("utf-8", "replace").strip()
        raise Refused(f"docker run (seeding {volume}) failed: {said}")


def start(image: str, name: str, volume: str, *extra: str, port: int | None = None) -> None:
    """Start `image`'s own command line as `name`, with the named `volume` as its sources."""
    docker_cli("run", "-d", "--name", name, "--init",
               "--user", host_user(),
               "-p", f"127.0.0.1:{port or ''}:8080",
               "--tmpfs", "/home/coder/repo",
               "-v", f"{volume}:{SOURCES}",
               *extra, image, *command_of(image))


def remove(name: str | None, volume: str | None = None) -> None:
    """Remove the container `name` and the volume `volume`, whatever state either is in."""
    if name:
        subprocess.run(["docker", "rm", "-f", name], stdin=subprocess.DEVNULL, capture_output=True)
    if volume:
        subprocess.run(["docker", "volume", "rm", "-f", volume], stdin=subprocess.DEVNULL, capture_output=True)


def short_base(env=None) -> str:
    """A host directory short enough for the browser's singleton socket, or `Refused`.

    ⭐ The inherited temporary directory when it fits; otherwise the session's
    runtime directory, then `/tmp`. ⛔ Only the browser's OWN profile lives
    here -- nothing is ever mounted from it -- and on Windows, which has no Unix
    socket to fit, the inherited one is always used.
    """
    env = os.environ if env is None else env
    inherited = tempfile.gettempdir()
    if os.name == "nt":
        return inherited
    for candidate in (inherited, env.get("XDG_RUNTIME_DIR"), "/tmp", "/var/tmp"):
        if candidate and len(candidate) <= SHORT_BASE and os.path.isdir(candidate) and os.access(candidate, os.W_OK):
            return candidate
    raise Refused(f"no writable temporary directory of at most {SHORT_BASE} characters for the browser's "
                  f"singleton socket; the inherited one is {len(inherited)} long")


class Browser:
    """A headless browser in a throwaway profile, its output kept for a refusal to quote.

    ⭐ The profile AND the browser's `TMPDIR` are one short directory
    (`short_base()`), so a long inherited `TMPDIR` cannot kill it with "Socket
    path too long". ⛔ Its stdout and stderr go to a file in that directory,
    never to `/dev/null`: `tail()` is what a refusal shows.
    """

    def __init__(self, found: str, *arguments: str):
        self.home = Path(tempfile.mkdtemp(prefix="sf-", dir=short_base()))
        self.log = self.home / "browser.log"
        environment = dict(os.environ)
        if os.name != "nt":
            environment["TMPDIR"] = str(self.home)
        with open(self.log, "wb") as output:
            self.process = subprocess.Popen(
                [found, *BROWSER_FLAGS, f"--user-data-dir={self.home / 'profile'}", *arguments],
                stdin=subprocess.DEVNULL, stdout=output, stderr=subprocess.STDOUT, env=environment)

    def exited(self) -> bool:
        return self.process.poll() is not None

    def tail(self, lines: int = BROWSER_TAIL) -> str:
        try:
            said = self.log.read_text(encoding="utf-8", errors="replace").splitlines()
        except OSError:
            return ""
        return "\n".join(said[-lines:])

    def close(self) -> None:
        if self.process.poll() is None:
            self.process.terminate()
            try:
                self.process.wait(timeout=20)
            except subprocess.TimeoutExpired:
                self.process.kill()
                self.process.wait()
        shutil.rmtree(self.home, ignore_errors=True)

    def __enter__(self) -> "Browser":
        return self

    def __exit__(self, *_) -> None:
        self.close()


def docker_cli(*args: str) -> subprocess.CompletedProcess:
    done = subprocess.run(["docker", *args], stdin=subprocess.DEVNULL, capture_output=True, text=True)
    if done.returncode != 0:
        raise Refused(f"docker {' '.join(args[:2])} failed: {done.stderr.strip() or done.stdout.strip()}")
    return done
