r"""The copy of a corpus's code that its editor opens and its runner tests, kept current.

**What it does.** Mirrors every file of a corpus's code into `CODE_COPY`, a
directory of the corpus's own bookkeeping that the editor binds and the runner
reaches, so a lesson's link to a code file can open that file in the editor and
run its test without the author's tree being touched.

**How you use it.** `sync(root)` brings the copy up to date and returns its
directory; `code_files(root)` is every file the copy mirrors, as corpus-relative
paths; `in_copy(path)` is where a corpus-relative path sits in the copy.
`IGNORE_TEXT` is the one file the copy commits, which execution onboarding
writes so the directory exists before the editor starts.

**Depends on.** `os`, `shutil` and `pathlib`; `conventions` for which
directories hold no code; the archive's, the bundles' and the practice
workspaces' own directory names.

## ⛔ The author's tree is never written, so a run happens in a COPY

⚠️ **A test run writes**: Maven compiles into `target/`, pytest caches, a JVM
drops crash logs. ⭐ So the run happens in a copy, and the reader is told so on
the page. The copy lives under `.studyforge/execution/`, beside the compose
file that binds it, and ⛔ **everything in it but its own ignore file is
ignored** by that one committed file (`*`, then `!/.gitignore`) — so the author's
`git status` is clean however many runs a reader makes, and the directory exists
in every checkout, as a bind source must before `compose up` (spec §8.1).

## ⭐ Newer wins, so a reader's own change survives until the author's changes

⭐ **A file is copied when the copy lacks it or the author's is NEWER**, and
the copy it writes is stamped with the time of the sync, never the author's.
So a file a reader edited in the editor is newer than the author's and is
kept, and a file the author changed since is newer than the copy and replaces
it.

## ⛔ A written file is newer than anything built from what it replaced

⚠️ **Measured on a Maven build:** a reader edited a test, ran it (the edit
compiled), then brought the author's file back by deleting theirs. A copy
carrying the author's OLDER modification time was older than the class the
edit had compiled to, so Maven never recompiled, and every later run reported
the edit's result for code no longer there. ⭐ **So a file the sync writes is
stamped now**: every incremental build — Maven, Gradle, make — decides by
comparing a source's time with its output's, so a source newer than anything
built before it is recompiled by every one of them. ⛔ That is chosen over
deleting the affected build output, which would mean knowing where each
tool puts what (R1), and would miss a tool nobody taught it. ⛔ A file the copy
holds and the author's tree no longer does is removed, so a deleted test never
keeps compiling. ⚠️ Output directories and dot-directories inside the copy —
`target/`, the editor's `.vscode/` — are the copy's own and are never removed.

## ⛔ What is never copied

- ⛔ **No quiz key.** The archive and the bundles hold a quiz's key, the key
  lives only in the page it grades, and the editor is a process a reader opens
  any file in — so neither directory is walked (`binds.unkeyed` holds the same
  line for a bind).
- ⛔ **No dot-file and no dot-directory**: `.git`, an `.env` holding a
  secret, an IDE's settings, and this framework's own `.studyforge/`, which is
  where the copy itself lives.
- ⛔ **No build output and no dependency tree** (`conventions.SKIPPED`): a
  `target/` taken from the author's machine is a copy of a copy.
- ⛔ **No symlink**, followed or not: it can name anything on the host.
- The practice workspaces, which are the practices' and are bound by the
  editor already.
- ⛔ **No manifest** (`corpus.json`), at any depth. ⚠️ **Measured:** a corpus
  whose material sits at its root had its own `corpus.json` mirrored into the
  copy, and `serve`'s next start found two corpora with one source and refused
  to start (exit 2). ⭐ The copy is the corpus's CODE, never the corpus: no
  manifest and no `.studyforge` bookkeeping (a dot-directory, never walked).
  ⭐ A manifest an earlier sync left in the copy is removed by the next one, as
  any file the copy holds and the sync does not want.

⭐ **A copy larger than `MAX_BYTES` or `MAX_FILES` is refused** rather than
made: a corpus whose "code" is a media library is not one a reader's click
should duplicate.
"""

from __future__ import annotations

import os
import shutil
from pathlib import Path

from studyforge.corpus.manifest import MANIFEST_FILENAME
from studyforge.corpus.placement.names import ARCHIVE_DIRNAME, PRACTICE_DIRNAME
from studyforge.execute.conventions import SKIPPED
from studyforge.exercise.bundle.layout import BUNDLES_DIRNAME

#: Where the copy lives, relative to the corpus root: beside the compose file
#: that binds it, in the corpus's own bookkeeping.
CODE_COPY = ".studyforge/execution/code"

#: The one file the copy commits, and what it says.
IGNORE_FILE = ".gitignore"
IGNORE_TEXT = (
    "# Written by studyforge execution onboarding. This directory is a copy of the\n"
    "# corpus's code that its editor opens and its runner tests; only this file is\n"
    "# committed, so the author's own files are never what a run writes into.\n"
    "*\n"
    f"!/{IGNORE_FILE}\n"
)

#: The top-level directories of this framework's own that are never copied.
FRAMEWORK_DIRS = (ARCHIVE_DIRNAME, BUNDLES_DIRNAME, PRACTICE_DIRNAME)

#: The most a copy may hold. ⚠️ Generous for source, and far short of a media
#: library.
MAX_FILES = 20_000
MAX_BYTES = 256 * 1024 * 1024

#: What a file being copied is named until it is complete. ⭐ Dot-prefixed, so
#: a sync that walks the copy never reads a half-written one as the author's.
STAGING_PREFIX = ".studyforge-copy-"


class CodeRefused(Exception):
    """The copy could not be made or kept current, saying why."""


def in_copy(path: str) -> str:
    """Return where a corpus-relative path sits in the copy, relative to the root."""
    return f"{CODE_COPY}/{path}"


def code_files(root: Path) -> dict[str, Path]:
    """Return every file the copy mirrors, `corpus-relative path -> file`, sorted.

    Raises `CodeRefused` when the code is larger than a copy may be.
    """
    found: dict[str, Path] = {}
    total = 0
    for directory, names, files in os.walk(root):
        here = Path(directory)
        top = here == root
        names[:] = sorted(name for name in names if _walked(name, top=top))
        for name in sorted(files):
            path = here / name
            if name.startswith(".") or name == MANIFEST_FILENAME or path.is_symlink():
                continue
            if not path.is_file():
                continue
            total += path.stat().st_size
            found[path.relative_to(root).as_posix()] = path
            if len(found) > MAX_FILES or total > MAX_BYTES:
                raise CodeRefused(
                    "this corpus's code is larger than a copy is made of "
                    f"({MAX_FILES} files or {MAX_BYTES // (1024 * 1024)} MiB), so its editor "
                    "does not open it"
                )
    return found


def sync(root: Path) -> Path:
    """Bring the copy up to date with the author's tree, and return its directory.

    Raises `CodeRefused` when the copy's directory is missing — it is created by
    execution onboarding, never here — or a file cannot be copied.
    """
    root = Path(root)
    copy = root / CODE_COPY
    if not copy.is_dir():
        raise CodeRefused(
            f"{CODE_COPY} is missing; regenerate this corpus's execution files, which "
            "write it, before its editor opens the corpus's code"
        )
    wanted = code_files(root)
    try:
        for relative, source in wanted.items():
            _copy_if_newer(source, copy / relative)
        for relative, held in _held(copy).items():
            if relative not in wanted:
                held.unlink()
    except OSError as error:
        raise CodeRefused(
            f"the copy of this corpus's code could not be kept current: "
            f"{error.strerror or error.__class__.__name__}"
        ) from None
    return copy


def _walked(name: str, *, top: bool) -> bool:
    """Whether a directory is walked: no dot-directory, no output, no framework directory."""
    if name.startswith(".") or name in SKIPPED:
        return False
    return not (top and name in FRAMEWORK_DIRS)


def _held(copy: Path) -> dict[str, Path]:
    """Every file the copy holds that a sync owns: what `code_files` would name there."""
    held: dict[str, Path] = {}
    for directory, names, files in os.walk(copy):
        here = Path(directory)
        names[:] = [name for name in names if _walked(name, top=False)]
        for name in files:
            path = here / name
            if not name.startswith(".") and not path.is_symlink():
                held[path.relative_to(copy).as_posix()] = path
    return held


def _copy_if_newer(source: Path, target: Path) -> None:
    """Copy `source` over `target` when the target is missing or older; atomically."""
    try:
        if target.stat().st_mtime_ns >= source.stat().st_mtime_ns:
            return
    except FileNotFoundError:
        pass
    target.parent.mkdir(parents=True, exist_ok=True)
    staging = target.with_name(f"{STAGING_PREFIX}{target.name}")
    try:
        shutil.copy(source, staging)  # ⭐ its bytes and mode; its time is now
        os.replace(staging, target)
    finally:
        staging.unlink(missing_ok=True)
