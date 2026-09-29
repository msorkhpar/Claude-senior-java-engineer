r"""Whose container this is, told by its labels: never by the spelling of a bind's host side.

**What it does.** Names the two labels a container the corpus's compose file
starts carries, and answers the two questions `mode` and `editor` ask of them:
is this container THIS checkout's (`this_checkout`), and which directory of the
corpus is mounted where (`binds_of`).

**How you use it.** The execution skill writes `BINDS` onto the editor and the
runner with `binds_text(pairs)`; compose writes `WORKING_DIR` itself. A probe
reads both from `docker inspect` and hands them here.

**Depends on.** `instance` for the compose file's one spelling, and `os.path`.
⛔ No process and no I/O beyond resolving a path on this host.

## ⛔ WHY NOT THE MOUNT'S SOURCE

⚠️ `docker inspect` reports a bind's `Source` as the ENGINE sees it. On Linux
that is the host path; on Docker Desktop for Windows it is the path inside the
engine's VM (`/run/desktop/mnt/host/c/...`), and on macOS it can be another
spelling again, so comparing it with the source root `serve` holds matches on
one host and silently answers *no container* on the rest. ⭐ **So a container
the compose file started is matched by labels**:

- ⭐ `org.studyforge.binds` — written by the skill: `<corpus-relative
  directory>=<folder inside>` pairs, `;` between, the root itself spelled as an
  empty directory. It says what each mount IS; the mount's destination, which
  every engine reports as written, says it is there.
- ⭐ `com.docker.compose.project.working_dir` — written by compose's CLIENT, on
  the host, in the host's own spelling: the compose file's directory, exactly
  as `serve`, another process on the same host, spells
  `<root>/.studyforge/execution`. ⛔ Two host-side spellings, compared on the
  host (`realpath`, then `normcase`, so `C:\` and `c:\` are one directory); the
  engine's spelling is never involved.

⚠️ A container with no `BINDS` label — one a reader started by hand from the
component's documented run line, or from a compose file generated before this
label — is matched by its mount's source, as before, which holds wherever the
engine reports host paths (Linux).
"""

from __future__ import annotations

import os
from collections.abc import Iterable
from pathlib import PurePosixPath

from studyforge.execute.instance import COMPOSE_FILE

#: The label the execution skill writes: what each mount of the container is.
BINDS = "org.studyforge.binds"

#: The label compose's client writes: the compose file's directory, on the host.
WORKING_DIR = "com.docker.compose.project.working_dir"

#: Where the compose file sits, relative to the corpus root.
EXECUTION_DIR = PurePosixPath(COMPOSE_FILE).parent

#: Between two pairs, and between a pair's two halves.
PAIRS = ";"
HALVES = "="


def binds_text(pairs: Iterable[tuple[str, str]]) -> str:
    """Return `BINDS`'s value: `<corpus-relative directory>=<folder inside>` pairs."""
    return PAIRS.join(f"{base}{HALVES}{inside}" for base, inside in pairs)


def binds_of(text: str) -> tuple[tuple[str, str], ...] | None:
    """Return `BINDS`'s pairs, or `None` when there are none or one does not parse.

    ⛔ **Refuses rather than repairs**: a directory that is absolute, climbs, or
    holds a backslash, and a folder that is not absolute, answers `None`, which
    a probe reads as *not labelled*.
    """
    found = []
    for entry in (one for one in text.split(PAIRS) if one.strip()):
        base, equals, inside = entry.partition(HALVES)
        parts = [part for part in base.split("/") if part]
        if (
            not equals
            or not inside.startswith("/")
            or base.startswith("/")
            or "\\" in base
            or any(part in (".", "..") for part in parts)
        ):
            return None
        found.append(("/".join(parts), inside))
    return tuple(found) or None


def this_checkout(working_dir: str, root: str | os.PathLike[str], *, path=os.path) -> bool:
    """Whether compose's `working_dir` is `root`'s own compose directory, on this host.

    ⭐ `path` is the host's path module; a test hands in `ntpath` to read a
    Windows host's answer on any host.
    """
    if not working_dir:
        return False
    expected = path.join(os.fspath(root), *EXECUTION_DIR.parts)
    try:
        return path.normcase(path.realpath(working_dir)) == path.normcase(path.realpath(expected))
    except OSError, ValueError:
        return False


def held(
    mounts: list[tuple[str, str]], labelled: dict[str, str], root: str | os.PathLike[str], *, path
) -> list[tuple[str, str]] | None:
    """Return `(base, folder)` for each labelled bind really mounted, or `None` if unlabelled.

    ⭐ `mounts` are `(source, destination)` as the engine reports them, and only
    the destination is read. `[]` when compose's working directory is another
    checkout's: that container is not this corpus's.
    """
    binds = binds_of(labelled.get(BINDS, ""))
    if binds is None:
        return None
    if not this_checkout(labelled.get(WORKING_DIR, ""), root, path=path):
        return []
    destinations = {destination for _, destination in mounts}
    return [(base, folder) for base, folder in binds if folder in destinations]
