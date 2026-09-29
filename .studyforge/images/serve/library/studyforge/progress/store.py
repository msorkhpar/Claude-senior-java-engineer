"""`Progress` — the record of practice passes on disk, read whole and replaced whole.

**What it does.** Reads the corpus's `progress.json`, and records one finished
run of a practice as a read-validate-modify-write under one inter-process lock,
staged beside the file and renamed over it.

**How you use it.** `Progress(root, depth)` with the corpus root and the
manifest's `len(levels)`; `record_run(address, ordinal, section, mode=...,
exit_code=..., commands=..., when=..., cases=...)` after a run; `read()`,
`entry(...)` and `unit_entries(...)` to report. ⭐ `cases` is one Submit's
breakdown and is omitted by every caller that has none.

**Depends on.** `studyforge.corpus.placement` for `GENERATED_ROOT`,
`studyforge.archive.scrub` for R7, `studyforge.address`, and this package's
`document`, `keys`, `lock` and `errors`.

## ⛔ Where it lives, and how it stays out of version control without R3's forbidden edit

`<root>/.studyforge/progress/progress.json`. R3 forbids editing a repository's
root ignore file however declared, and says to write an ignore file *inside*
the generated directory instead — so the store writes
`.studyforge/progress/.gitignore` holding `*` beside the record. That ignores
the record, the lock, a staged write and the ignore file itself, and edits
nothing that existed. ⛔ **An ignore file already there that does not ignore
everything is refused, never rewritten**: it is not this store's to change,
and recording into a directory it no longer ignores would put a reader's
record in a commit. `.studyforge/` is under the corpus root, never a home
directory (R7), and the `tree` profile's pages sit below it — ⚠️ **so a route
serving `.studyforge/` as static bytes must not serve `progress/`**.

## ⭐ Discipline

A mutation takes the lock (`lock`), ensures the ignore file, reads and
validates the whole file, applies the change, validates the result, renders,
gates the exact text (R7), writes it to `progress.json.writing`, `fsync`s it,
and `os.replace`s it over the record. A crash at any point leaves the old file
or the new one. ⭐ **A read takes no lock and needs none**: a rename is atomic,
so a reader sees one whole document. A missing file reads as empty and reading
creates nothing. ⛔ A malformed file raises `ProgressFormatError` on every read
and every write, and its bytes are never touched.

⚠️ **The first mutation creates the lock file a moment before the ignore file
that covers it**, because the ignore file is written under the lock. A crash in
that window leaves one empty lock file unignored until the next mutation.
"""

from __future__ import annotations

import contextlib
import copy
import json
import os
from collections.abc import Callable
from pathlib import Path
from typing import TypeVar

from studyforge.address import Address
from studyforge.archive.scrub import assert_clean, scrub
from studyforge.corpus.placement import GENERATED_ROOT
from studyforge.describe import describe
from studyforge.progress.document import (
    PROGRESS_FILENAME,
    new_document,
    next_entry,
    render,
    validate,
)
from studyforge.progress.errors import ProgressError, ProgressFormatError
from studyforge.progress.keys import practice_key
from studyforge.progress.lock import LOCK_FILENAME, exclusive

#: The store's own directory under `GENERATED_ROOT`.
PROGRESS_DIRNAME = "progress"

#: The ignore file written inside that directory, and what it holds.
IGNORE_FILENAME = ".gitignore"
IGNORE_TEXT = (
    "# Written by studyforge's progress store. Everything in this directory is\n"
    "# this machine's own record of practice passes, and never enters a commit.\n"
    "*\n"
)

#: Appended while a file is staged. ⚠️ A torn `*.writing` is never read; a file
#: half-overwritten in place would read as present and malformed.
WRITING_SUFFIX = ".writing"

T = TypeVar("T")


def store_dir(root: Path | str) -> Path:
    """Return the store's directory under a corpus root. ⛔ One spelling."""
    return Path(root) / GENERATED_ROOT / PROGRESS_DIRNAME


class Progress:
    """One corpus's record of practice passes. Holds no document between calls."""

    def __init__(self, root: Path | str, depth: int) -> None:
        """Bind the store to a corpus root and the depth its practice keys have."""
        if isinstance(depth, bool) or not isinstance(depth, int) or depth < 1:
            raise ProgressError(f"a corpus's depth is an int of 1 or more, got {describe(depth)}")
        self.directory = store_dir(root)
        self.depth = depth

    @property
    def path(self) -> Path:
        """Return where the record is."""
        return self.directory / PROGRESS_FILENAME

    def read(self) -> dict:
        """Return the whole validated document, as a copy the caller may keep."""
        return self._read()

    def entry(self, address: Address, ordinal: int, section: str) -> dict | None:
        """Return one practice's entry, or `None` if it has never been run."""
        key = practice_key(self._at_depth(address), ordinal, section)
        return self._read()["practices"].get(key)

    def unit_entries(self, address: Address, ordinal: int) -> dict[str, dict]:
        """Return `{section: entry}` for every recorded practice of one unit."""
        prefix = self._at_depth(address).unit_key(_ordinal(ordinal)) + "/"
        return {
            key[len(prefix) :]: entry
            for key, entry in self._read()["practices"].items()
            if key.startswith(prefix) and "/" not in key[len(prefix) :]
        }

    def record_run(
        self,
        address: Address,
        ordinal: int,
        section: str,
        *,
        mode: str,
        exit_code: int | str,
        commands: list[str],
        when: str,
        cases: dict | None = None,
    ) -> dict:
        """Record one finished run of a practice and return its entry.

        ⛔ Only `mode="test"` with exit `0` passes. Commands are scrubbed on the
        way in and the rendered document is gated before it is written (R7).

        ⭐ `cases` is this run's breakdown — one verdict per declared case — or
        `None` for every run that produced none. ⛔ **It is a report and never
        a pass rule**: `is_pass` is untouched by it, and the
        rendered document is gated for R7 with it in, like everything else.
        """
        key = practice_key(self._at_depth(address), ordinal, section)
        strings = isinstance(commands, list) and all(isinstance(c, str) for c in commands)
        clean = [scrub(c) for c in commands] if strings else commands
        arguments = {
            "mode": mode,
            "exit_code": exit_code,
            "commands": clean,
            "when": when,
            "cases": cases,
        }
        next_entry(None, **arguments)  # refuse a bad call before the file is touched

        def change(document: dict) -> dict:
            practices = document["practices"]
            practices[key] = next_entry(practices.get(key), **arguments)
            return practices[key]

        return copy.deepcopy(self._update(change))

    def _at_depth(self, address: Address) -> Address:
        """Return `address` if it has this corpus's depth, else refuse."""
        if not isinstance(address, Address):
            raise ProgressError(f"a practice is addressed by an Address, got {describe(address)}")
        if address.depth != self.depth:
            raise ProgressError(
                f"an address of {address.depth} segment(s) is not a practice in a corpus "
                f"of {self.depth} level(s)"
            )
        return address

    def _read(self) -> dict:
        """Return the validated document; a missing file is an empty one."""
        try:
            text = self.path.read_text(encoding="utf-8")
        except FileNotFoundError:
            return new_document()
        except UnicodeDecodeError:
            raise ProgressFormatError(f"{PROGRESS_FILENAME} is not valid UTF-8") from None
        except OSError as fault:
            raise ProgressError(f"{PROGRESS_FILENAME} cannot be read: {fault.strerror}") from None
        try:
            decoded = json.loads(text)
        except ValueError:
            raise ProgressFormatError(
                f"{PROGRESS_FILENAME} is not valid JSON, and it is left exactly as it is"
            ) from None
        assert_clean(decoded, PROGRESS_FILENAME)
        return validate(decoded, self.depth)

    def _update(self, change: Callable[[dict], T]) -> T:
        """Apply `change` to the document under the lock, and replace the file atomically."""
        try:
            self.directory.mkdir(parents=True, exist_ok=True)
        except OSError as fault:
            raise ProgressError(
                f"the progress directory cannot be made: {fault.strerror}"
            ) from None
        with exclusive(self.directory / LOCK_FILENAME):
            self._ensure_ignored()
            document = self._read()
            result = change(document)
            validate(document, self.depth)
            text = render(document)
            assert_clean(text, PROGRESS_FILENAME)
            _replace(self.path, text)
        return result

    def _ensure_ignored(self) -> None:
        """Write the directory's ignore file if absent; refuse one that ignores less."""
        ignore = self.directory / IGNORE_FILENAME
        try:
            existing = ignore.read_text(encoding="utf-8")
        except FileNotFoundError:
            _replace(ignore, IGNORE_TEXT)
            return
        except OSError, UnicodeDecodeError:
            existing = ""
        lines = [line.strip() for line in existing.splitlines()]
        if "*" not in lines or any(line.startswith("!") for line in lines):
            raise ProgressError(
                f"{GENERATED_ROOT}/{PROGRESS_DIRNAME}/{IGNORE_FILENAME} does not ignore everything "
                f"beside the record; it is not this store's to rewrite, so nothing is recorded "
                f"until it holds a '*' line and no '!' line"
            )


def _ordinal(ordinal: object) -> int:
    """Return `ordinal` if it is a unit ordinal, refusing by description."""
    if isinstance(ordinal, bool) or not isinstance(ordinal, int) or ordinal < 1:
        raise ProgressError(f"a practice's unit ordinal must be 1 or more, got {describe(ordinal)}")
    return ordinal


def _replace(path: Path, text: str) -> None:
    """Write `text` beside `path`, flush it to disk, and rename it over `path`."""
    staged = path.with_name(path.name + WRITING_SUFFIX)
    try:
        with open(staged, "w", encoding="utf-8", newline="\n") as handle:
            handle.write(text)
            handle.flush()
            os.fsync(handle.fileno())
        os.replace(staged, path)
    except OSError as fault:
        with contextlib.suppress(OSError):
            staged.unlink()
        raise ProgressError(f"{path.name} could not be written: {fault.strerror}") from None
    _sync_directory(path.parent)


def _sync_directory(directory: Path) -> None:
    """Flush the rename itself, best effort: a filesystem that cannot is not an error."""
    with contextlib.suppress(OSError):
        descriptor = os.open(directory, os.O_RDONLY)
        try:
            os.fsync(descriptor)
        finally:
            os.close(descriptor)
