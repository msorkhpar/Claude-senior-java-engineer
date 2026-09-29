"""Folding a test run's report through the record's cases: the main ask, and the edges.

**What it does.** Reads the machine-readable report a graded run wrote, folds
it through the record's `cases`, and answers *main ask* plus *edge cases n/m*
— naming each edge that did not pass by the one sentence the corpus wrote for
it. ⛔ It refuses a report it cannot honestly fold rather than returning an
emptier one.

**How you use it.**

    from studyforge.exercise import breakdown_of

    started = time.time()          # before the run is started
    ...                            # the run
    breakdown = breakdown_of(exercise, source_root, where, started=started)
    if breakdown is not None:
        breakdown.ask              # did the main ask pass
        breakdown.edges_passed     # n
        breakdown.edges_total      # m
        breakdown.failed_edges     # each failed edge's `says`, in declared order

**Depends on.** `cases` for the vocabulary — `Case`, and `JUNIT`, which is the
one format this build reads — `record` for `Exercise`, `errors` for the one
exception, and the standard library's `xml.etree.ElementTree` and `pathlib`.
⛔ Not on `execute`: this reads a file a run left behind and never starts one,
so a breakdown can be taken from a run somebody else's process performed.
⛔ Not on `progress` or `serve`: recording the breakdown beside a verdict is
`progress`'s, and the direction is those-depend-on-this.

## ⛔ The channel is JUnit XML, and console output is not a channel

⭐ Maven's surefire writes it with no configuration, pytest writes it on one
flag, and the standard library reads it. ⛔ The reader's stream is **rewritten
by design** — `execute.quiet` drops the declared build tool's own lines — so
folding a breakdown out of it would make what a reader is told depend on a
display decision. The format token is `cases.REPORT_FORMATS`, closed at one,
and this module refuses any other rather than guessing.

## ⛔ Five refusals, and each is a different mistake

| what arrived | the answer |
|---|---|
| a report naming a test the case map does not | ⛔ **refused**, never counted |
| a report older than the run that should have written it | ⛔ **refused, and never read** |
| a report that is not readable JUnit XML | ⛔ **a named failure** |
| no report at all | ⭐ **no breakdown and no error** |
| a record with no `cases` and no `report` | ⭐ **no breakdown and no error** |

⚠️ **The two quiet answers are quiet for different reasons and both are
`None`.** A record that declares no breakdown is an exercise with no `cases`;
a declared report that is not on disk is a run that did not get far
enough to write one — a compile failure writes no surefire file. ⛔ Neither is
a defect, and raising for either would make a breakdown a precondition of
running rather than a report about a run.

## ⛔ A test the map does not name is refused rather than counted

⭐ **The alternative fails silently.** Counting only the named tests turns a
report of four results into *edge cases 1/1* — green, and a lie about what ran.
Ignoring the unnamed test is the same lie with an extra step. ⚠️ **The cost is
stated:** the declared path must hold the exercise's report and nothing else,
so a report directory shared with an unrelated test class is refused. That is
the authoring defect gate G4 exists to catch, caught here too because
a gate that runs at authoring time does not run on the reader's machine.

## ⛔ Staleness is a claim about TIME, so it is measured against the run

⚠️ **A report is a file, and a file outlives the run that wrote it.** A run
that fails before its tests execute leaves the previous run's report exactly
where this module looks, so folding whatever is on disk reports a result
nobody produced — and it reports it as *the reader's last Submit*. ⭐ So the
caller hands over `started`, the wall clock read before the run was started,
and every file is refused unless it was written at or after it. ⛔ **The
mtimes of every file are read BEFORE any of them is parsed**, so a stale
report is never read at all — which also means a report a corpus *committed*,
rather than ran, is refused before this module parses corpus-authored XML.

⚠️ **`CLOCK_SLACK` is a measurement tolerance and not a grace period.** A
filesystem whose timestamps are coarse can stamp a file written during the run
with a time before it, and a false *stale* would refuse a run that really did
write its report. ⛔ It is seconds wide, and a stale report is a previous
run's — minutes or hours old — so nothing this clause exists to catch survives
it.

## ⭐ A case passes only if the report says so

⛔ **Enumerated the way this project enumerates**: the closed set is
the children a **passing** testcase may carry, never the children that mean it
failed. A report element this build has not seen — `rerunFailure`, a provider's
own extension — therefore reads as *did not pass* rather than slipping through
a list of failure tags nobody finished. ⚠️ A case the report never names did
not pass either: a run that stops early names fewer tests, and a case with no
result is not a case that succeeded.

⭐ **The breakdown is a REPORT and never a second definition of a pass** (spec
§7). `is_pass` stays what it is — a test-mode run that exited zero —
and a reader shown *edge cases 2/3* is looking at an incomplete practice.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from xml.etree import ElementTree

from studyforge.exercise.cases import EDGE, JUNIT, Case
from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.record import Exercise

#: What a report directory's members are named. ⚠️ Surefire writes a DIRECTORY
#: of `TEST-*.xml` and pytest writes ONE file; the record declares either, and
#: which one it is is read off the disk rather than off the format token.
REPORT_SUFFIX = ".xml"

#: ⛔ Closed: what a JUnit document's root element may be. Surefire writes
#: `testsuite` per class, pytest wraps its suites in `testsuites`. Anything
#: else is a well-formed XML document that is not a report, which is a named
#: failure rather than a breakdown of nothing.
REPORT_ROOTS = ("testsuite", "testsuites")

#: ⛔ Closed, and closed on the PASSING side on purpose: the children a
#: testcase that passed may still carry. `failure`, `error` and `skipped` are
#: not here, and neither is the unforeseen one.
PASSING_CHILDREN = ("system-out", "system-err", "properties")

#: Seconds of tolerance between the run's start and a report's timestamp.
#: ⚠️ It exists for coarse filesystem timestamps, not for old reports: two
#: seconds is the widest granularity a real filesystem has, and a stale report
#: is a previous run's.
CLOCK_SLACK = 2.0


@dataclass(frozen=True, slots=True)
class Breakdown:
    """What a run reported, folded through the cases the record declared.

    ⛔ Frozen and derived: it carries the cases in the order the corpus wrote
    them and the ids that passed, and every number a reader is shown is read
    off those two. ⚠️ Constructing one by hand asserts nothing — `breakdown_of`
    is what guarantees the ids came out of a report of that run.
    """

    cases: tuple[Case, ...]
    passed_ids: frozenset[str]

    def passed(self, case: Case) -> bool:
        """Did this case pass? ⛔ A case the report did not name did not pass."""
        return case.id in self.passed_ids

    @property
    def ask(self) -> bool:
        """Did the main ask pass — the half of the breakdown that is not counted."""
        return all(self.passed(case) for case in self.cases if case.ask)

    @property
    def edges(self) -> tuple[Case, ...]:
        """The declared edge cases, in the order the corpus wrote them."""
        return tuple(case for case in self.cases if case.kind == EDGE)

    @property
    def edges_passed(self) -> int:
        """`n` of *edge cases n/m*."""
        return sum(1 for case in self.edges if self.passed(case))

    @property
    def edges_total(self) -> int:
        """`m` of *edge cases n/m*."""
        return len(self.edges)

    @property
    def failed_edges(self) -> tuple[str, ...]:
        """Each failed edge's `says`, in declared order — what the reader is shown.

        ⚠️ Edges only. The main ask's own verdict is `ask`, because a reader is
        shown *main ask* and *edge cases n/m* as two different things.
        """
        return tuple(case.says for case in self.edges if not self.passed(case))

    @property
    def complete(self) -> bool:
        """Did every declared case pass? ⛔ A report about a run, never a pass rule."""
        return self.ask and self.edges_passed == self.edges_total


def breakdown_of(exercise: Exercise, root: Path, where: str, *, started: float) -> Breakdown | None:
    """Fold the report this exercise's run should have written, or return `None`.

    `root` is the source root the record's paths are relative to, and `started`
    is the wall clock — `time.time()` — read before the run was started.

    ⭐ `None` is the answer twice and neither is a failure: the record declares
    no breakdown, or the run wrote no report. Everything else raises.
    """
    cases, report = exercise.cases, exercise.report
    if not exercise.breaks_down or cases is None or report is None:
        return None
    if report.format != JUNIT:
        raise ExerciseError(
            f"{where}: this build reads {JUNIT!r} reports and the record declares "
            f"another format. Widening that set is a line in 'cases.REPORT_FORMATS' "
            f"and a reader for it here."
        )
    files = _files(root / report.path)
    if not files:
        return None
    _require_fresh(files, report.path, where, started=started)
    return Breakdown(cases=cases, passed_ids=_fold(files, cases, report.path, where))


def _files(target: Path) -> tuple[Path, ...]:
    """Every report file at the declared path — one file, a directory's, or none.

    ⚠️ Not recursive: surefire writes its XML flat into the directory it is
    given, and descending would sweep in a nested module's report, which is
    the unrelated test this module refuses to count.
    """
    if target.is_dir():
        return tuple(sorted(path for path in target.glob(f"*{REPORT_SUFFIX}") if path.is_file()))
    return (target,) if target.is_file() else ()


def _require_fresh(files: tuple[Path, ...], declared: str, where: str, *, started: float) -> None:
    """Refuse a report older than its run — ⛔ before any of them is parsed."""
    for path in files:
        try:
            written = path.stat().st_mtime
        except OSError:
            raise ExerciseError(_unreadable(declared, path, where)) from None
        if written < started - CLOCK_SLACK:
            raise ExerciseError(
                f"{where}: the report at '{_named(declared, path)}' is older than the "
                f"run that should have written it, so it is refused and not read. A "
                f"report a run did not write is the previous run's, and folding it "
                f"would report a result nobody produced."
            )


def _fold(
    files: tuple[Path, ...], cases: tuple[Case, ...], declared: str, where: str
) -> frozenset[str]:
    """Return the ids the report says passed, refusing a test the cases do not name."""
    ids = tuple(case.id for case in cases)
    reported: set[str] = set()
    failed: set[str] = set()
    for path in files:
        for element in _parse(path, declared, where).iter("testcase"):
            case_id = _identify(element, ids, declared, path, where)
            reported.add(case_id)
            if not _passed(element):
                failed.add(case_id)
    return frozenset(reported - failed)


def _parse(path: Path, declared: str, where: str) -> ElementTree.Element:
    """Read one report file, or name the failure — ⛔ never an empty breakdown."""
    try:
        root = ElementTree.parse(path).getroot()
    except ElementTree.ParseError as error:
        line, column = error.position
        raise ExerciseError(
            f"{where}: the report at '{_named(declared, path)}' is not well-formed "
            f"XML, at line {line}, column {column}. A report that cannot be read is "
            f"a named failure and never a breakdown of nothing."
        ) from None
    except OSError:
        raise ExerciseError(_unreadable(declared, path, where)) from None
    if root.tag not in REPORT_ROOTS:
        raise ExerciseError(
            f"{where}: the report at '{_named(declared, path)}' is XML, but its root "
            f"element is not one of {list(REPORT_ROOTS)}, so it is not a {JUNIT!r} "
            f"report. The element's name is not reproduced here, since a refusal never quotes a "
            f"value that may be personal."
        )
    return root


def _identify(
    element: ElementTree.Element, ids: tuple[str, ...], declared: str, path: Path, where: str
) -> str:
    """Return the declared case this testcase reports on, refusing one nothing names."""
    if not element.get("name", ""):
        raise ExerciseError(
            f"{where}: the report at '{_named(declared, path)}' carries a testcase "
            f"with no 'name'. A result that names no test cannot be folded through "
            f"any case map."
        )
    named = [case_id for case_id in ids if _spells(element, case_id)]
    if not named:
        raise ExerciseError(
            f"{where}: the report at '{_named(declared, path)}' names a test the "
            f"record's 'cases' does not, so it is refused rather than counted — a "
            f"breakdown that silently drops a result is a green lie about what ran. "
            f"The ids are not reproduced here, since a refusal never quotes a value that may be "
            f"personal."
        )
    if len(named) > 1:
        raise ExerciseError(
            f"{where}: the report at '{_named(declared, path)}' names one test that "
            f"{len(named)} of the record's 'cases' each claim, so no result can "
            f"be attributed. The ids are not reproduced here, since a refusal never quotes a "
            f"value that may be personal."
        )
    return named[0]


def _spells(element: ElementTree.Element, case_id: str) -> bool:
    """Return whether this testcase is the one the corpus wrote `case_id` down for.

    ⭐ Three spellings, because two test runners spell an id differently and the
    record carries what its own runner writes: a pytest node id
    (`file::Class::name`), a JUnit one (the class, a number sign, the method),
    and the bare name a runner that reports neither a file nor a class leaves.
    ⛔ Every comparison is byte for byte and nothing is repaired — a report's
    own spelling is what the corpus author is told to write down.

    ⚠️ **The declared id is taken APART rather than a spelling composed from the
    report**, and that is not only style: `render.markup` owns the one composer
    of a number sign in this tree, asserted over `src/` by a sweep that
    cannot tell a URL fragment from a method separator. ⭐ Reading the declared
    value is the honest direction anyway — the case map is the authority, and
    nothing here invents a string to test it against.
    """
    name = element.get("name", "")
    classname = element.get("classname", "")
    file = element.get("file", "")
    if case_id == name:
        return True
    owner, separator, method = case_id.rpartition("#")
    if separator and owner == classname and method == name:
        return True
    return bool(file) and case_id == _node_id(file, classname, name)


def _node_id(file: str, classname: str, name: str) -> str:
    """`file::Class::name`, rebuilt from a testcase the way pytest spelled it.

    ⚠️ pytest's JUnit `classname` is the module's dotted path with any class
    appended, so the class is what is left once the module is taken off the
    front. ⭐ The rebuild matches pytest's own node ids, checked against
    this repository's own reports; it is spelled here because
    `src/` imports no test or developer code.
    """
    module = file.removesuffix(".py").replace("/", ".")
    inner = classname[len(module) + 1 :].split(".") if classname.startswith(f"{module}.") else []
    return "::".join([file, *[part for part in inner if part], name])


def _passed(element: ElementTree.Element) -> bool:
    """Did this testcase pass? ⛔ Only if every child it carries is a passing one."""
    return all(child.tag in PASSING_CHILDREN for child in element)


def _named(declared: str, path: Path) -> str:
    """Name a report file without ever writing an absolute path (R7).

    ⛔ The declared path is a **workspace** path — `safety.require_path` refused
    an absolute one, a `..` and a backslash before the record was accepted — so
    it cannot carry a home directory, and a file inside it is named by the
    declared path plus its own one segment.
    """
    return declared if path.name == Path(declared).name else f"{declared}/{path.name}"


def _unreadable(declared: str, path: Path, where: str) -> str:
    """One sentence for a report that is there and cannot be read."""
    return (
        f"{where}: the report at '{_named(declared, path)}' could not be read. A file "
        f"that is present and unreadable is a named failure; a run that wrote no "
        f"report at all is not."
    )
