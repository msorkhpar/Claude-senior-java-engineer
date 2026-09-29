"""The store's inter-process lock: `fcntl.flock` on a sibling file, POSIX only.

**What it does.** Holds an exclusive advisory lock on one lock file for the
length of a `with` block, so a second process — or a second thread — that
takes the same lock waits until the first has finished its read, change and
replace.

**How you use it.** `with exclusive(directory / LOCK_FILENAME): ...`. The store
is the caller; nothing else in the framework should need to be.

**Depends on.** `fcntl` from the standard library, and this package's `errors`.

## ⛔ Why not `threading.Lock`, which the extraction source used

It protects nothing against a second **process**. That was safe there because
one server owned the file; here the personal archive merges progress from outside
the server and a build may run while a site is served (§8.5).

## ⭐ What it guarantees, and where

On Linux and macOS, every writer that goes through this package excludes
every other one on the same host — across processes, and across threads in one
process too, because each `with` opens its own descriptor and `flock` locks
belong to the open file description rather than to the process. ⚠️ **Advisory
only**: a program that writes `progress.json` without taking the lock is not
stopped. ⚠️ **Not a promise over a network filesystem**, where `flock`
semantics vary by client.

⛔ **On a platform with no `fcntl` it refuses rather than running unlocked.** A
store that silently lost the lock would lose updates with no symptom, and a
lost pass is exactly what a reader cannot notice.

⭐ **The lock is a separate file, never `progress.json` itself**: the record
is replaced by rename, and a lock on the old inode would be a lock on a file
nobody reads any more.
"""

from __future__ import annotations

import os
from collections.abc import Iterator
from contextlib import contextmanager
from pathlib import Path

from studyforge.progress.errors import ProgressError

try:
    import fcntl
except ImportError:  # pragma: no cover - reached only off POSIX
    fcntl = None

#: The lock file's own name, beside the record it guards.
LOCK_FILENAME = "progress.lock"

#: The lock file holds no data; it is still this reader's, so it is theirs alone.
LOCK_MODE = 0o600


def supported() -> bool:
    """Return whether this platform gives the store an inter-process lock."""
    return fcntl is not None


@contextmanager
def exclusive(path: Path | str) -> Iterator[None]:
    """Hold the exclusive lock at `path` for the block; create the file if absent.

    ⛔ Blocks without a timeout. A writer holds the lock for one read, one
    change and one rename, so a wait that never ends is a stopped process
    holding it — and closing that process releases it.
    """
    if fcntl is None:
        raise ProgressError(
            "this platform has no fcntl.flock, so the progress store cannot exclude a "
            "second writer and records nothing rather than risk losing a pass"
        )
    try:
        descriptor = os.open(path, os.O_RDWR | os.O_CREAT, LOCK_MODE)
    except OSError as fault:
        # ⛔ `strerror`, never the exception: an `OSError` renders its path (R7).
        raise ProgressError(f"the progress lock cannot be opened: {fault.strerror}") from None
    try:
        fcntl.flock(descriptor, fcntl.LOCK_EX)
        yield
    finally:
        os.close(descriptor)
