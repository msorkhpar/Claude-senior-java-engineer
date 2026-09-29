"""The case vocabulary: what a grader reports, in what kind of exercise, built from what.

**What it does.** Reads and writes the four values the case vocabulary adds to the §7
record — the exercise's own `kind`, its `cases`, the `report` its grader
writes, and the `origin` it was built from — and refuses every way each of
them can be wrong.

**How you use it.** `kind_of(value, where)`; `cases_of` / `cases_document`;
`report_of` / `report_document`; `origin_of` / `origin_document`.

**Depends on.** `errors` for the one exception this package raises, `safety`
for the rule a path inside the workspace obeys, **`corpus.container.fields` for
what an `origin` is**, and `studyforge.describe` for naming a value without
reproducing it (R7).

## ⛔ `origin`'s rule is imported, never re-spelled

⚠️ **`origin` has ONE reader in this tree** — `fields.optional_origin`, which
owns both origin shapes and the heading bound on a region — and that is a
standing property, asserted by `tests/studyforge/corpus/container/
test_origin_sites.py` over `src/` and `tests/` rather than at one ref. ⭐ So
this module hands the value to that reader and re-raises its sentence under
this package's type, exactly as `record` does with R5 (`unit.trust`). ⛔ A
second spelling of the region shape was written here first, and the instrument
refused it — which is the instrument working.

⚠️ **The direction of the dependency is stated rather than left to be
inferred:** `exercise` imports `corpus.container`, and nothing in `corpus`
imports `exercise`. ⭐ The alternative — a leaf module both packages ask, the
shape `sourcepath` and `describe` already have — would be the better home for it.

## ⛔ Why this is a module and not four more functions in `record`

⭐ **A split at a named seam** (R11): `record` owns the **document** — which
keys exist, which of them are written, and in what order — and this module owns
the **vocabulary** those keys are written in. ⚠️ The alternative was a wider
`record`, which is the longest module in this package, and the remedy for
that is a seam rather than a trim.

## ⛔ Three kinds of key, and the shape each one belongs to

| the key | it is a fact about | so it may appear on |
|---|---|---|
| `kind` | the exercise | every record |
| `cases`, `report` | **the grader** | a graded record only |
| `origin` | the material it was built from | every record |

⛔ **`cases` and `report` are refused on a record with no grader, naming the
key** — the argument `record` already makes for `provenance` and `trust`,
applied to the two keys that arrived after it. A case map is a claim about what
a grader *reports*; on a record with nothing that runs it is a breakdown no
reader is ever shown, while the corpus validates green.

⭐ **`origin` is NOT a grader fact, and that difference is deliberate.** An
exercise authored from a page was authored from that page whether or not
anything checks the reader's answer, and the source ledger (spec §7 §3) must
resolve its entry either way. ⚠️ So an ungraded record may carry an `origin`
and may carry nothing else new.

⛔ **The breakdown is written whole or not at all.** `cases` with no `report`
names tests nothing can be read from, and a `report` with no `cases` is a file
nothing folds through. ⭐ The precedent is the grader half itself:
half a claim is not a lesser exercise, it is a claim somebody half wrote.

## ⛔ `code` is the default and it is NOT written out

⚠️ **The one place this module departs from `trust`'s ruling**, which writes a
defaulted field anyway so that it cannot be told from one nobody wrote. ⭐ **The
difference is the installed base.** `trust` is defaulted inside a half that is
already being written whole, so writing it costs one key on records that were
being written anyway. Every `exercise` record ever written is `code`, so writing
that token would re-render every archive document in existence — which is a
version bump's cost under R10. ⭐ **An absent `kind` is unambiguous precisely
because every record written without it is `code`**, which is the claim a
defaulted `trust` could not make.

## ⛔ Every set here is closed, and each one is enumerable

The kinds, the case kinds, the report formats, the keys of a case, a report and
a region: each is a list of what is **permitted**, so the unforeseen entry is
refused at the boundary by somebody who then has to decide. The one value that
is not enumerable is `says`, which is a sentence for a reader; it is required
to be text and is never reproduced in a refusal.
"""

from __future__ import annotations

import re
from dataclasses import dataclass

from studyforge.corpus.container import ORIGIN_KEYS as CONTAINER_ORIGIN_KEYS
from studyforge.corpus.container import ContainerError, fields
from studyforge.describe import describe, describe_keys
from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.safety import require_path

#: The exercise with no quiz and no cases: a workspace the reader writes code
#: in.
CODE = "code"

#: A checkable practice for material that admits no coding task. ⚠️ The shape
#: itself — questions, the key, and grading with no compiler — is `exercise.quiz`'s;
#: what lands here is the token, so a record can carry it and round-trip.
QUIZ = "quiz"

#: ⛔ Closed. A kind this build does not define is refused rather than carried.
EXERCISE_KINDS = (CODE, QUIZ)

#: What a record that writes no `kind` is.
DEFAULT_KIND = CODE

#: The ask itself — what the exercise is for.
MAIN = "main"

#: One named edge of that ask, backed by its own test (spec §7, gate G3).
EDGE = "edge"

#: ⛔ Closed, and two rather than a scale: the breakdown a reader is shown is
#: *main ask* plus *edge cases n/m*, and a third kind would have nowhere to go.
CASE_KINDS = (MAIN, EDGE)

#: One case, in the order it is written. ⛔ All three required and nothing
#: else: an `id` as the test report spells it, a `kind`, and the one sentence
#: the reader is shown.
CASE_KEYS = ("id", "kind", "says")

#: JUnit XML, as Maven's surefire writes it with no configuration and pytest
#: writes it on one flag. ⭐ Named here and read by `report`; console output is
#: not a format, because the quiet run modes rewrite that stream by design.
JUNIT = "junit"

#: ⛔ Closed. A format nothing can read is a breakdown nobody gets.
REPORT_FORMATS = (JUNIT,)

#: The grader's machine-readable output: what it is, and where it lands.
REPORT_KEYS = ("format", "path")

#: A region of the source — ⛔ **the origin tuple itself, imported and never
#: re-typed.** A region is bounded by a **heading**, because a line
#: range couples a declaration to a file's byte layout and an anchor couples it
#: to a renderer's slug rules. ⭐ Re-exported here so a consumer of `exercise`
#: reads one contract, and it is the same object: two spellings of one shape is
#: the defect `describe` was extracted over.
ORIGIN_KEYS = CONTAINER_ORIGIN_KEYS

#: ⛔ The two keys that are one claim, written whole or not at all.
BREAKDOWN_KEYS = ("cases", "report")

#: What a case `id` may be. ⛔ A permitted set, and wider than a
#: path or an argv token on purpose: this value is compared byte for byte with
#: what a test report spells, so it carries a Java `Class#method`, a pytest
#: `file::test[param]` and a parameterised `name(int, int)`. ⚠️ It carries no
#: whitespace, so it stays one token wherever it is printed or matched.
CASE_ID = re.compile(r"\A[A-Za-z0-9._:#$()\[\],+=@/-]+\Z")

#: Said in a refusal instead of the value (R7), for the reason `safety` states:
#: the message tells an author what to write, and the value is corpus data.
CASE_ID_PERMITTED = (
    "the test's own id as the report spells it, carrying only ASCII letters, "
    "digits and '. _ - : # $ / @ + = , ( ) [ ]', with no whitespace"
)


@dataclass(frozen=True, slots=True)
class Case:
    """One thing a grader reports on: the test that reports it, and what it says.

    ⛔ Frozen, not validated: `cases_of` is what guarantees the kind is one of
    `CASE_KINDS` and the id is one a report can name.
    """

    id: str
    kind: str
    says: str

    @property
    def ask(self) -> bool:
        """Is this the main ask? ⭐ The half of the breakdown that is not counted."""
        return self.kind == MAIN


@dataclass(frozen=True, slots=True)
class Report:
    """Where a grader's machine-readable output lands, and what format it is in."""

    format: str
    path: str


@dataclass(frozen=True, slots=True)
class Origin:
    """The material an exercise was built from: a file, or a region of one.

    ⚠️ `section` is the second half of one declared field and never a second
    key, so every reader that wants a *path* gets a plain string out of `path`
    whichever shape was written.
    """

    path: str
    section: str | None


def kind_of(value: object, where: str) -> str:
    """Return the exercise's own kind, refusing one this build does not define."""
    if value not in EXERCISE_KINDS:
        raise ExerciseError(
            f"{where}: 'exercise' declares a 'kind' this build does not define. "
            f"The kinds are {list(EXERCISE_KINDS)}, and a record that writes no "
            f"'kind' is {DEFAULT_KIND!r}. The value is {describe(value)}."
        )
    return value


def cases_of(value: object, where: str) -> tuple[Case, ...]:
    """Read the `cases` a grader's report is folded through, in the order written."""
    if isinstance(value, str) or not isinstance(value, (list, tuple)):
        raise ExerciseError(
            f"{where}: 'cases' must be an array of case objects, each "
            f"{list(CASE_KEYS)}. The value is {describe(value)}."
        )
    if not value:
        raise ExerciseError(
            f"{where}: 'cases' is empty. A grader that names nothing writes no "
            f"'cases' key at all, and one that writes the key names at least the "
            f"main ask."
        )
    cases = tuple(_case(entry, where) for entry in value)
    _require_the_main_ask(cases, where)
    _require_distinct_ids(cases, where)
    return cases


def cases_document(cases: tuple[Case, ...]) -> list[dict]:
    """Return the cases as decoded objects, each in `CASE_KEYS` order (R10)."""
    return [{"id": case.id, "kind": case.kind, "says": case.says} for case in cases]


def report_of(value: object, where: str) -> Report:
    """Read the `report` a grader writes: its format, and where it lands."""
    if not isinstance(value, dict) or set(value) != set(REPORT_KEYS):
        raise ExerciseError(
            f"{where}: 'report' is {list(REPORT_KEYS)}, both required and nothing "
            f"else. A format with no path names no file and a path with no format "
            f"is a file nothing can read. The value is {describe(value)}."
        )
    if value["format"] not in REPORT_FORMATS:
        raise ExerciseError(
            f"{where}: 'report' declares a format this build cannot read. The "
            f"formats are {list(REPORT_FORMATS)}. The value is "
            f"{describe(value['format'])}."
        )
    return Report(format=value["format"], path=require_path(value["path"], "report path", where))


def report_document(report: Report) -> dict:
    """Return the report as the decoded object, in `REPORT_KEYS` order (R10)."""
    return {"format": report.format, "path": report.path}


def origin_in(record: dict, where: str) -> Origin | None:
    """Read the `origin` a record declares — a file inside the source, or a region of one.

    ⛔ **Takes the record, not the value**, and that is not a convenience: the
    key is read where it is handed straight to its one reader, so there is no
    second site in the tree that reads it and decides something.

    ⚠️ Re-raised, not re-worded: `fields.optional_origin`'s message is the one
    that states the shape, and re-spelling it here is the duplication this
    module's docstring refuses. The type changes so a caller of `exercise`
    catches one family; the sentence does not. ⭐ `record._trust` is the
    precedent, character for character.
    """
    try:
        path, section = fields.optional_origin(record.get("origin"), "origin", where)
    except ContainerError as error:
        raise ExerciseError(str(error)) from None
    return None if path is None else Origin(path, section)


def origin_document(origin: Origin) -> str | dict:
    """Return the origin in the shape it was declared in — a path, or a region."""
    if origin.section is None:
        return origin.path
    return {"path": origin.path, "section": origin.section}


def _case(value: object, where: str) -> Case:
    """Read one case, refusing a key the case does not define — because a typo is one."""
    if not isinstance(value, dict):
        raise ExerciseError(
            f"{where}: a case is an object, {list(CASE_KEYS)}. This one is {describe(value)}."
        )
    if set(value) != set(CASE_KEYS):
        unknown = [key for key in value if key not in CASE_KEYS]
        missing = [key for key in CASE_KEYS if key not in value]
        raise ExerciseError(
            f"{where}: a case is {list(CASE_KEYS)}, all of them required and "
            f"nothing else. This one is missing {missing} and carries "
            f"{describe_keys(unknown)} the case does not define."
        )
    return Case(
        id=_case_id(value["id"], where),
        kind=_case_kind(value["kind"], where),
        says=_says(value["says"], where),
    )


def _case_id(value: object, where: str) -> str:
    """Refuse an id no test report could spell."""
    if not isinstance(value, str) or not CASE_ID.match(value):
        raise ExerciseError(
            f"{where}: a case's 'id' must be {CASE_ID_PERMITTED}. The value is not "
            f"reproduced here, since a refusal never quotes a value that may be personal."
        )
    return value


def _case_kind(value: object, where: str) -> str:
    """Refuse a case kind the breakdown has nowhere to put."""
    if value not in CASE_KINDS:
        raise ExerciseError(
            f"{where}: a case's 'kind' is one of {list(CASE_KINDS)} — the ask "
            f"itself, or one named edge of it. The value is {describe(value)}."
        )
    return value


def _says(value: object, where: str) -> str:
    """Refuse a case with nothing to say, because the reader is shown this sentence."""
    if not isinstance(value, str) or not value.strip():
        raise ExerciseError(
            f"{where}: a case's 'says' is the one sentence the reader is shown "
            f"when it fails, and it must be text. The value is {describe(value)}."
        )
    return value


def _require_the_main_ask(cases: tuple[Case, ...], where: str) -> None:
    """⛔ Refuse a map that reports edges and never the ask they are edges of."""
    if not any(case.ask for case in cases):
        raise ExerciseError(
            f"{where}: 'cases' names no {MAIN!r} case. A reader is shown the main "
            f"ask plus 'edge cases n/m', so a map of edges alone describes a run "
            f"whose ask nothing reports."
        )


def _require_distinct_ids(cases: tuple[Case, ...], where: str) -> None:
    """⛔ Refuse a map that is not a map: one test's result folding into two cases."""
    ids = [case.id for case in cases]
    repeated = len(ids) - len(set(ids))
    if repeated:
        raise ExerciseError(
            f"{where}: 'cases' names {repeated} id more than once. Every test the "
            f"report names maps to exactly one case, so a repeated id is a result "
            f"counted twice. The ids are not reproduced here, since a refusal never quotes a "
            f"value that may be personal."
        )
