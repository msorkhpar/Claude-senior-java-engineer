"""The record of practice passes: what this machine's grader runs have established.

**What it does.** Keeps one corpus's practice passes — facts a grader run
established — in one git-ignored JSON file under the corpus's generated root,
and reads them back. Local, the reader's own, never a database and never inside
served content (spec §8.5, R16).

**How you use it.** Build a store from the corpus root and the manifest's
depth, record each finished run, and catch `RAISES` around any call:

    store = Progress(root, depth=len(levels))
    store.record_run(address, 7, "practice-java", mode="test", exit_code=0,
                     commands=["./gradlew test"], when="2026-09-12T10:00:00+00:00")
    store.entry(address, 7, "practice-java")["first_passed_at"]

**Depends on.** `address`, `archive.scrub`, `corpus.placement` and `version`.
⛔ Not on `serve` — the store is what the server serves, not the other way
round, and the personal-archive skill and the build write it from outside any
server.

⭐ **A Submit's breakdown rides beside its verdict** as `last.cases`, one
verdict per case the practice's record declared. ⛔ **It is a report
and never a second rule for a pass**: `is_pass` is untouched, and `document`'s
own docstring carries the whole ground.

⚠️ **Two records, not one, and this package is one of them.** A pass is earned
from a grader, so it is written where the run happened; a **read mark** is the
reader's own assertion and lives in the browser (§8.5,
`render/assets/study-progress.js`). ⛔ **Nothing here can hold a read mark**:
the modes are `run` and `test`, and only a test run that exits 0 passes.

⭐ **This is the durable artifact the reader keeps** (R16) — the thing that
makes a generated site a personal record rather than a rendering.

## What is in the package

| Module | Owns |
|---|---|
| `store` | `Progress`, `store_dir` — location, the ignore file, the atomic update |
| `document` | the shape, `validate`, `render`, `is_pass` and the first-pass rule |
| `keys` | `practice_key`, `parse_practice_key` — the address package's key plus a section |
| `lock` | `exclusive` — the inter-process lock and what it guarantees on which platforms |
| `errors` | `ProgressError`, `ProgressFormatError` |

## ⭐ The seam the server calls (the state route and the run route)

A route holding a key string parses it with `parse_practice_key(key, depth)`
or the address package, then calls `Progress.record_run` or `Progress.entry`
with the `Address`. ⛔ **It never composes a key itself**: the store, the page
and the run route join on that string by equality.
"""

from __future__ import annotations

from studyforge.archive.scrub import PersonalDataLeak
from studyforge.progress.document import (
    CASES_KEY,
    EXIT_STOPPED,
    EXIT_TIMEOUT,
    MODE_RUN,
    MODE_TEST,
    MODES,
    PROGRESS_API,
    PROGRESS_FILENAME,
    is_pass,
)
from studyforge.progress.errors import ProgressError, ProgressFormatError
from studyforge.progress.keys import parse_practice_key, practice_key
from studyforge.progress.lock import LOCK_FILENAME
from studyforge.progress.store import IGNORE_FILENAME, PROGRESS_DIRNAME, Progress, store_dir

#: ⛔ What any call into this package lets out, as a tuple. `ProgressFormatError`
#: is a `ProgressError` and needs no entry of its own; `PersonalDataLeak` is
#: R7's gate on a record somebody edited by hand, and is let through unchanged.
RAISES = (ProgressError, PersonalDataLeak)

__all__ = [
    "CASES_KEY",
    "EXIT_STOPPED",
    "EXIT_TIMEOUT",
    "IGNORE_FILENAME",
    "LOCK_FILENAME",
    "MODES",
    "MODE_RUN",
    "MODE_TEST",
    "PROGRESS_API",
    "PROGRESS_DIRNAME",
    "PROGRESS_FILENAME",
    "RAISES",
    "Progress",
    "ProgressError",
    "ProgressFormatError",
    "is_pass",
    "parse_practice_key",
    "practice_key",
    "store_dir",
]
