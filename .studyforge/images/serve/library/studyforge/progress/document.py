"""The progress document: its shape, its bytes, and the one rule for a pass.

**What it does.** Validates a decoded progress document, renders one to the
exact bytes the store writes, and folds one finished run into a practice's
entry under the first-pass rule.

**How you use it.** `validate(decoded, depth)` on everything read;
`render(document)` on everything written; `next_entry(previous, ...)` for the
entry a run leaves behind. The store is the caller.

**Depends on.** `studyforge.version` for R9, `studyforge.describe`, and this
package's `keys` and `errors`.

## What is recorded

    {
      "practices": {
        "<address>/unit-NN/<section>": {
          "first_passed_at": "<iso>" | null,
          "last": {"at": "<iso>", "commands": [...], "exit": 0, "mode": "test",
                   "passed": true,
                   "cases": {"<case id>": true, ...}},
          "runs": 3
        }
      },
      "progress_api": 1
    }

⛔ **`passed` is exactly `mode == "test" and exit == 0`, and `is_pass` is the
only place that says so** — the writer and the validator reading the file back
share it, so they cannot drift apart about what a pass is. A program that ran
and printed successfully has demonstrated nothing about its tests; only a test
run completes a practice. ⛔ **`first_passed_at` is set by the first
pass and never moves**: a later failure does not un-pass, and a later pass does
not make it newer.

⛔ **There is no `read` mode and there will not be one.** A read mark is the
reader's own assertion and lives in the browser (§8.5); a record that
could hold one could be made to treat it as a pass.

## ⛔ `last.cases` is a REPORT about a run, never a second rule for a pass

⭐ **`cases` is the one OPTIONAL key in `last`**: a Submit whose
record declared a breakdown carries one verdict per declared case, and every
other run carries none. ⛔ **A document written before it reads unchanged** —
that is what *optional* is for here, and it is checked rather than assumed.

⛔ **Nothing in it is allowed to decide a pass.** `is_pass` stays `mode ==
"test" and exit == 0`, so a practice still completes only when the grader
itself exited zero; a reader shown *edge cases 2/3* is being shown an
incomplete practice. ⚠️ **So `passed` is NOT cross-checked against `cases`**,
deliberately: a rule that made a false verdict here un-pass a run would be the
second definition of a pass this record exists to avoid.

⭐ **The per-case verdicts are recorded and every count is derived** — *main
ask*, *edge cases n/m* and each failed edge's sentence are all read off this
map joined with the practice's own declared cases. ⛔ Recording the counts too
would be two spellings of one claim, and the id is the stable key: a `says` is
reader-facing corpus text, already in the practice document, and it goes stale
the moment the corpus is regenerated.

⛔ **The shape is checked here and the ids are NOT.** This file is read back
with no corpus in hand — `studyforge archive` reads one on a machine that has
neither the source nor the case map — so a key is refused for being blank or
not a string and never for failing `exercise.cases.CASE_ID`. ⚠️ A case map
that has since changed makes an entry's breakdown unreadable to the panel, not
the record invalid.

⭐ **Every key sorts** (`sort_keys=True`), so a valid file written by anyone
re-renders to one byte sequence and a diff of it says what changed.
"""

from __future__ import annotations

import json
from datetime import datetime

from studyforge.describe import describe
from studyforge.progress.errors import ProgressError, ProgressFormatError
from studyforge.progress.keys import parse_practice_key
from studyforge.version import check

#: R9's key for this contract, registered in `version.CONTRACT_FIELDS` in the
#: commit that minted it. ⛔ Read through `check`: this record is re-earned only
#: by re-running every grader, so an unknown version stops rather than resets.
PROGRESS_API = 1
KNOWN_PROGRESS_API = frozenset({PROGRESS_API})

#: The file's own name, used as `where` in every refusal (R7: never a path).
PROGRESS_FILENAME = "progress.json"

#: Which of a practice's two commands ran — the run route's mode segment.
MODE_RUN = "run"
MODE_TEST = "test"
MODES = (MODE_RUN, MODE_TEST)

#: The runner's two non-status verdicts.
EXIT_TIMEOUT = "timeout"
EXIT_STOPPED = "stopped"
EXIT_WORDS = (EXIT_TIMEOUT, EXIT_STOPPED)

DOCUMENT_KEYS = frozenset({"progress_api", "practices"})
ENTRY_KEYS = frozenset({"runs", "last", "first_passed_at"})
LAST_KEYS = frozenset({"at", "mode", "exit", "passed", "commands"})

#: ⭐ The framework's ONE spelling of the breakdown's key, read by the run
#: route that writes it rather than retyped there.
CASES_KEY = "cases"

#: ⛔ The only key in `last` that may be absent, which is how a document
#: written before the breakdown existed reads unchanged.
LAST_OPTIONAL_KEYS = frozenset({CASES_KEY})


def is_pass(mode: object, exit_code: object) -> bool:
    """Return whether a run with this mode and verdict passed. ⛔ The one rule."""
    return mode == MODE_TEST and exit_code == 0 and not isinstance(exit_code, bool)


def new_document() -> dict:
    """Return the empty document a missing file reads as."""
    return {"progress_api": PROGRESS_API, "practices": {}}


def render(document: dict) -> str:
    """Return the exact text written: sorted keys, two-space indent, one newline."""
    return json.dumps(document, indent=2, sort_keys=True, ensure_ascii=False) + "\n"


def next_entry(
    previous: dict | None,
    *,
    mode: str,
    exit_code: int | str,
    commands: list[str],
    when: str,
    cases: dict | None = None,
) -> dict:
    """Return the entry a practice has after one more finished run.

    `commands` must already be scrubbed; the store does that and then gates the
    rendered document. ⛔ Every argument is checked before anything is built,
    so a bad call leaves no half-formed entry.

    `cases` is one run's breakdown — `{case id: did it pass}` — or `None`,
    which is every run that produced none: a Run, a record that declares no
    breakdown, a run that wrote no report, and every run recorded before
    the breakdown existed. ⛔ It never touches `passed`.
    """
    if mode not in MODES:
        raise ProgressError(f"a run's mode must be one of {list(MODES)}, got {describe(mode)}")
    if not _is_exit(exit_code):
        raise ProgressError(
            f"a run's exit must be a status of 0 or more or one of {list(EXIT_WORDS)}, "
            f"got {describe(exit_code)}"
        )
    if not _is_commands(commands):
        raise ProgressError("a run's commands must be a non-empty list of non-blank str")
    if not _is_timestamp(when):
        raise ProgressError(f"a run's time must be an ISO 8601 str, got {describe(when)}")
    if cases is not None:
        if mode != MODE_TEST:
            raise ProgressError(
                f"only a {MODE_TEST!r} run carries a breakdown; a {MODE_RUN!r} run "
                f"produces no grader report to fold"
            )
        if not _is_cases(cases):
            raise ProgressError(
                f"a run's {CASES_KEY!r} must be a non-empty object of one true or "
                f"false verdict per case id, got {describe(cases)}"
            )
    passed = is_pass(mode, exit_code)
    first = previous["first_passed_at"] if previous else None
    if first is None and passed:
        first = when
    last = {
        "at": when,
        "mode": mode,
        "exit": exit_code,
        "passed": passed,
        "commands": list(commands),
    }
    if cases is not None:
        last[CASES_KEY] = dict(cases)
    return {
        "runs": (previous["runs"] if previous else 0) + 1,
        "last": last,
        "first_passed_at": first,
    }


def validate(data: object, depth: int) -> dict:
    """Return `data` if it is exactly a progress document at `depth`, else raise.

    ⛔ Every message names a place — `practice #3 'last.mode'` — and never a
    value or a key, because the file is the reader's and may hold anything.
    """
    if not isinstance(data, dict):
        raise ProgressFormatError(f"{PROGRESS_FILENAME} holds {describe(data)}, not an object")
    check(
        "progress_api",
        data.get("progress_api"),
        KNOWN_PROGRESS_API,
        where=PROGRESS_FILENAME,
        error=ProgressFormatError,
    )
    _exact(data, DOCUMENT_KEYS, PROGRESS_FILENAME)
    practices = data["practices"]
    if not isinstance(practices, dict):
        raise ProgressFormatError(
            f"{PROGRESS_FILENAME} 'practices' is {describe(practices)}, not an object"
        )
    for index, (key, entry) in enumerate(practices.items(), start=1):
        _entry(key, entry, depth, f"{PROGRESS_FILENAME} practice #{index}")
    return data


def _entry(key: object, entry: object, depth: int, where: str) -> None:
    """Refuse one practice entry that is not exactly what `next_entry` builds."""
    try:
        parse_practice_key(key, depth)
    except ProgressError:
        raise ProgressFormatError(
            f"{where} is not keyed by a practice key at this corpus's depth"
        ) from None
    if not isinstance(entry, dict):
        raise ProgressFormatError(f"{where} is {describe(entry)}, not an object")
    _exact(entry, ENTRY_KEYS, where)
    runs = entry["runs"]
    if isinstance(runs, bool) or not isinstance(runs, int) or runs < 1:
        raise ProgressFormatError(
            f"{where} 'runs' must be an int of 1 or more, got {describe(runs)}"
        )
    last = entry["last"]
    if not isinstance(last, dict):
        raise ProgressFormatError(f"{where} 'last' is {describe(last)}, not an object")
    _exact(last, LAST_KEYS, f"{where} 'last'", optional=LAST_OPTIONAL_KEYS)
    if not _is_timestamp(last["at"]):
        raise ProgressFormatError(f"{where} 'last.at' is not an ISO 8601 timestamp")
    if last["mode"] not in MODES:
        raise ProgressFormatError(f"{where} 'last.mode' is not one of {list(MODES)}")
    if not _is_exit(last["exit"]):
        raise ProgressFormatError(
            f"{where} 'last.exit' is not a status or one of {list(EXIT_WORDS)}"
        )
    if last["passed"] is not is_pass(last["mode"], last["exit"]):
        raise ProgressFormatError(
            f"{where} 'last.passed' disagrees with 'last.mode' and 'last.exit'"
        )
    if not _is_commands(last["commands"]):
        raise ProgressFormatError(f"{where} 'last.commands' is not a non-empty list of commands")
    if CASES_KEY in last:
        # ⛔ Read AFTER `last.mode`, so this refusal is only ever reached by a
        # run whose mode is one of `MODES` and can be named in the sentence.
        if last["mode"] != MODE_TEST:
            raise ProgressFormatError(
                f"{where} 'last.{CASES_KEY}' is on a run that is not a {MODE_TEST!r} run"
            )
        if not _is_cases(last[CASES_KEY]):
            raise ProgressFormatError(
                f"{where} 'last.{CASES_KEY}' is not an object of one true or false "
                f"verdict per case id"
            )
    first = entry["first_passed_at"]
    if first is not None and not _is_timestamp(first):
        raise ProgressFormatError(f"{where} 'first_passed_at' is not an ISO 8601 timestamp or null")


def _exact(
    value: dict, keys: frozenset[str], where: str, optional: frozenset[str] = frozenset()
) -> None:
    """Refuse an object whose keys are not `keys` plus any of `optional`.

    ⛔ Names the expected sets and never the keys it found, because the file is
    the reader's and a key in it may hold anything.
    """
    if set(value) - optional != keys:
        also = f", and may also have {sorted(optional)}" if optional else ""
        raise ProgressFormatError(f"{where} must have exactly the keys {sorted(keys)}{also}")


def _is_exit(value: object) -> bool:
    """Whether `value` is a runner verdict: a status of 0 or more, or a known word."""
    if isinstance(value, bool):
        return False
    if isinstance(value, int):
        return value >= 0
    return value in EXIT_WORDS


def _is_commands(value: object) -> bool:
    """Whether `value` is a non-empty list of non-blank command strings."""
    return (
        isinstance(value, list)
        and bool(value)
        and all(isinstance(c, str) and c.strip() for c in value)
    )


def _is_cases(value: object) -> bool:
    """Whether `value` is a non-empty object of one boolean verdict per case id.

    ⛔ The id's own spelling is not checked here; the module docstring says why.
    """
    return (
        isinstance(value, dict)
        and bool(value)
        and all(
            isinstance(case_id, str) and bool(case_id.strip()) and isinstance(passed, bool)
            for case_id, passed in value.items()
        )
    )


def _is_timestamp(value: object) -> bool:
    """Whether `value` is a str `datetime.fromisoformat` reads."""
    if not isinstance(value, str) or not value:
        return False
    try:
        datetime.fromisoformat(value)
    except ValueError:
        return False
    return True
