r"""The `check` verb: run the test for a unit's file the reader edited, or else its program.

**What it does.** Takes the path of a file the reader edited in their own editor.
Finds the one practice whose generated unit document names that file as its
`main_path` (the section's `workspace`, spec §7). Hands the command that document
names to `studyforge.execute` and streams the output. A graded file runs its
`test_command`, and the exit code is the test's verdict. A file with no test runs
its `run_command`, says no test checks it, and exits `0`.

**How you use it.**

    studyforge check practice/passes/greet.py              # the corpus is found above the file
    studyforge check practice/passes/greet.py --corpus .   # or named

`main(argv) -> int` is the callable the dispatcher registers. `runner_for=`
builds the `Runner` from the source root and the container name, and a test
passes its own there.

**Depends on.** `generate.declarations` for the corpus, `unit.builder` and
`unit.served` for the generated unit document (the one the content route
serves), `exercise` for the record and the completion rule, `execute` to run
anything, `progress` to record a grader run, `archive.scrub` for R7, and
`argparse`. ⛔ Nothing here knows any source (R1).

## The two decisions, stated before code

- ⭐ **(a) A file with no test runs its program** (`run_command`, from the
  document), then says no test checks it, and exits `0` whatever the program
  did (C5). The program's own status is in the stream's exit line.
- ⭐ **(b) A graded file's test run is recorded in the progress store** under
  `progress.practice_key`, in `test` mode. So a passing run is a practice pass,
  the same fact the page's Submit records (spec §8.5). A run of an ungraded
  program is never recorded.

## ⛔ Nothing the reader types becomes a command (spec §8.3, rule 3)

⭐ **The argument SELECTS a practice and is never passed on.** It is compared,
as a path relative to the source root, with each practice's `main_path`. A
match picks that practice's record, and the argv handed to the runner is the
record's own, read from disk. A path no record names is refused by name and
nothing runs. The runner checks the argv again (`execute.commands`). ⛔ There is
no option that takes a command, an argument for the program, or an environment
variable.

## ⭐ Graded is the grader's presence

Read from `Exercise.graded`, never from `workspace` being non-null. An ungraded
unit's `workspace` is `{main_path, run_command}`, which is not null.

## ⛔ Every line is gated (R7)

The runner's lines pass its `LineGate`: relative to the source root, then
scrubbed. This module's own lines, including the reader's argument echoed in a
refusal, pass `scrub`. ⭐ **Unfiltered**: the run output's filter is for the page. A
reader in a terminal sees what the build printed.

⛔ **Exit codes:** `0` the test passed, or the file has no test; `1` the test
did not pass (a failure, a timeout, a stop); `2` nothing ran — no corpus, a
corpus that cannot be read, or a file no practice names.
"""

from __future__ import annotations

import argparse
import os
import shlex
from collections.abc import Callable, Iterator
from dataclasses import dataclass
from datetime import UTC, datetime
from pathlib import Path, PurePosixPath

from studyforge.archive.scrub import PersonalDataLeak, scrub
from studyforge.execute import EXIT_STOPPED, EXIT_TIMEOUT, Runner, RunRefused, recorded
from studyforge.execute import exit_line as spell_exit
from studyforge.exercise import Exercise, ExerciseError, from_document
from studyforge.exitcodes import UNUSABLE
from studyforge.generate import RAISES as UNREADABLE
from studyforge.generate.declarations import Corpus, UnitSource, read_corpus
from studyforge.progress import MODE_TEST, Progress
from studyforge.progress import RAISES as UNRECORDED
from studyforge.unit import served
from studyforge.unit.builder import NoMaterial, build_unit
from studyforge.unit.builder import render as render_unit
from studyforge.unit.errors import ContentError
from studyforge.validate.report import INVALID, OK

#: The test passed, or the file has no test.
PASSED = OK

#: The test ran and did not pass: a failure, a timeout or a stop.
FAILED = INVALID

#: The file that marks a corpus root.
MANIFEST = "corpus.json"

#: What a file with no test says, after its program has run.
NO_TEST = "no test checks this file: it ran, and nothing grades it, which is not a failure"

#: What a file no practice names says. ⭐ It names the files that ARE checked.
NOT_A_UNIT_FILE = "no practice in this corpus names this file"

#: What is said when two practices name one file. ⛔ Neither is guessed at.
AMBIGUOUS = "more than one practice names this file, so none is run"

#: A document the verb could not read.
UNREADABLE_UNIT = (*UNREADABLE, ContentError, ExerciseError, PersonalDataLeak)


@dataclass(frozen=True)
class Practice:
    """One practice that names a file: where it is, and its record."""

    unit: UnitSource
    section: str
    exercise: Exercise

    @property
    def key(self) -> str:
        """`<unit key>/<section>`, as the reader is told it."""
        return f"{self.unit.key}/{self.section}"


def build_parser() -> argparse.ArgumentParser:
    """Return the argument parser, so a test can read the interface."""
    parser = argparse.ArgumentParser(
        prog="studyforge check",
        description=(
            "Run the test for a unit's file you edited, through the runner container when it "
            "is up and on this machine otherwise. A file with no test runs its program and "
            "is not a failure. The command run is the one the unit's generated document names; "
            "nothing you type becomes a command."
        ),
    )
    parser.add_argument("file", help="the file you edited, as a path from where you are")
    parser.add_argument(
        "--corpus",
        default=None,
        metavar="DIR",
        help=f"the corpus root, the directory holding {MANIFEST} "
        "(default: the nearest one above the file)",
    )
    return parser


def main(
    argv: list[str] | None = None,
    out=None,
    *,
    runner_for: Callable[[Path, str], Runner] = Runner,
) -> int:
    """Check one file and return its exit code."""
    import sys

    stream = sys.stdout if out is None else out

    def say(line: str) -> None:
        # ⛔ Every line this module writes is scrubbed (R7); the runner's are gated already.
        print(scrub(line), file=stream, flush=True)

    arguments = build_parser().parse_args(argv)
    given = arguments.file
    root = _corpus_root(given, arguments.corpus)
    if root is None:
        where = arguments.corpus or given
        say(f"{where}: no {MANIFEST} found; name the corpus root with --corpus")
        return UNUSABLE
    try:
        corpus = read_corpus(root)
        practices = list(_practices(corpus))
    except UNREADABLE_UNIT as refusal:
        say(str(refusal))
        return UNUSABLE
    chosen = [practice for practice in practices if _names(practice, root, given)]
    if not chosen:
        say(f"{given}: {NOT_A_UNIT_FILE}")
        for path in sorted({practice.exercise.main_path for practice in practices}):
            say(f"  checked: {path}")
        return UNUSABLE
    if len(chosen) > 1:
        say(f"{given}: {AMBIGUOUS}: {', '.join(practice.key for practice in chosen)}")
        return UNUSABLE
    return _check(chosen[0], corpus, say, stream, runner_for)


def _check(
    practice: Practice,
    corpus: Corpus,
    say: Callable[[str], None],
    stream,
    runner_for: Callable[[Path, str], Runner],
) -> int:
    """Run the one command the practice's record names, and report it."""
    exercise = practice.exercise
    try:
        # ⭐ THIS checkout's runner, by the name it recorded, never `source` alone.
        runner = runner_for(corpus.root, recorded(corpus.root, corpus.manifest.source).runner)
        command = exercise.test_command if exercise.graded else exercise.run_command
        state = f"graded ({exercise.trust})" if exercise.graded else "ungraded"
        say(f"check {exercise.main_path}  practice {practice.key}  {state}  mode {runner.mode()}")
        handle = runner.start([command])
    except RunRefused as refusal:
        say(str(refusal))
        return UNUSABLE
    last = ""
    lines: Iterator[str] = handle.lines()
    # ⛔ Unfiltered: every line the runner yields, as it yields it (already gated, R7).
    try:
        for last in lines:
            print(last, file=stream, flush=True)
    except KeyboardInterrupt:
        # ⭐ The reader's Ctrl-C ends the run's whole tree (the runner's stop), and
        # the stream still ends in its one exit line.
        handle.stop()
        last = spell_exit(EXIT_STOPPED)
        print(last, file=stream, flush=True)
    if not exercise.graded:
        say(NO_TEST)
        return PASSED
    verdict = _verdict(last, handle.returncode)
    _record(practice, corpus, command, verdict, say)
    passed = verdict == 0
    say(f"{'passed' if passed else 'not passed'}: practice {practice.key}")
    return PASSED if passed else FAILED


def _verdict(last: str, returncode: int | None) -> int | str:
    """Return the run's verdict as the progress store spells it, read off its one exit line."""
    for word in (EXIT_TIMEOUT, EXIT_STOPPED):
        if last == spell_exit(word):
            return word
    return returncode if returncode is not None else EXIT_STOPPED


def _record(
    practice: Practice,
    corpus: Corpus,
    command: tuple[str, ...],
    verdict: int | str,
    say: Callable[[str], None],
) -> None:
    """Record one finished grader run in the corpus's progress store (decision (b)).

    ⚠️ A store that cannot be written is said, and the verdict stands.
    """
    when = datetime.now(UTC).isoformat(timespec="seconds")
    try:
        Progress(corpus.root, depth=len(corpus.manifest.levels)).record_run(
            practice.unit.container.address,
            practice.unit.ordinal,
            practice.section,
            mode=MODE_TEST,
            exit_code=verdict,
            # ⭐ `shlex.join`, the spelling a run records: it reads back to the argv.
            commands=[shlex.join(command)],
            when=when,
        )
    except UNRECORDED as refusal:
        say(f"the run was not recorded: {refusal}")


def _practices(corpus: Corpus) -> Iterator[Practice]:
    """Every practice section that names a file, read from its generated unit document.

    ⭐ The document is built and read back exactly as the content route serves it
    (`serve.routes.content`): `build_unit`, rendered, then `unit.served.parse`,
    which is the trust boundary.
    """
    for unit in corpus.units:
        try:
            text = render_unit(
                build_unit(
                    unit.directory,
                    declared_practices=unit.declared_practices,
                    mentions=unit.mentions,
                )
            )
        except NoMaterial:
            continue
        document = served.parse(text, f"{unit.key}/{served.UNIT_FILENAME}")
        for section in document["sections"]:
            workspace = section["workspace"]
            if workspace is not None:
                where = f"{unit.key}/{section['key']}"
                yield Practice(unit, section["key"], from_document(workspace, where))


def _names(practice: Practice, root: Path, given: str) -> bool:
    """Whether the path the reader gave is this practice's `main_path`.

    ⭐ Compared as a path relative to the source root, normalised first and then
    with links resolved, so `./x`, `a/../x` and an absolute spelling all select
    the same file. ⛔ The comparison is the argument's only use.
    """
    main = PurePosixPath(practice.exercise.main_path)
    return any(relative == main for relative in _relative_spellings(root, given))


def _relative_spellings(root: Path, given: str) -> Iterator[PurePosixPath]:
    """Return the argument as a path under `root`, spelled as written and with links resolved."""
    pairs = (
        (Path(os.path.abspath(root)), Path(os.path.abspath(given))),
        (root.resolve(), Path(given).resolve()),
    )
    for base, path in pairs:
        if path.is_relative_to(base):
            yield PurePosixPath(path.relative_to(base).as_posix())


def _corpus_root(given: str, named: str | None) -> Path | None:
    """Return the named corpus root, or the nearest directory above the file holding a manifest."""
    if named is not None:
        root = Path(named)
        return root if (root / MANIFEST).is_file() else None
    start = Path(os.path.abspath(given)).parent
    for directory in (start, *start.parents):
        if (directory / MANIFEST).is_file():
            return directory
    return None
