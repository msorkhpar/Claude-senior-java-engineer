"""What a practice is: the workspace it runs in, and how much its verdict is trusted.

**What it does.** Defines the three states an exercise can be in, the workspace
a reader works in, and the trust attached to whatever checks their answer.

**How you use it.** Read an exercise from a practice archive document; ask what
state it is in before rendering anything that implies a grade.

    from studyforge.exercise import of, state_of, completes_practice

    state_of(None)                       # 'none' — the unit sets no work
    state_of(practice_document)          # 'ungraded' or 'graded'
    exercise = of(practice_document, where)   # an Exercise, or None
    exercise.graded                      # False: a file, and nothing checks it
    completes_practice(state, "test", passed=True)

**Depends on.** `unit.trust`, which owns R5's rule and whose own contract says
that this package consumes it. ⛔ Not on `execute` — running a grader is a separate
concern from saying what a grader's word is worth. ⛔ Not on `render`: this
says what may be claimed, never how it is drawn.

⛔ **Three states, not two** (C5). *No exercise*; an **ungraded** exercise — a
prompt the reader works, with nothing to check it; and a **graded** exercise,
whose verdict is then `authoritative` or `advisory` (R5). Only the third can
complete a practice. Collapsing the middle state into "no exercise" discards
real teaching content: one surveyed corpus has an exercise in every one of its
19 lessons and a test for none of them.

⭐ **The three states are read off the structure and there is no `state` field**
— none to set, none to forget. **none** is no practice document at all;
**ungraded** is a practice document with no grader in it; **graded** is a
record that names one. ⭐ **An ungraded unit may still name its file**:
its record carries `main_path` and `run_command` and no grader half, so a
reader's file resolves to its unit whether or not anything checks it.
⛔ A corpus that must declare its own emptiness is a contract fitted to the one
source that ships 168 graders (§11.0), and the common case here writes nothing.

⛔ **Nothing generated is presented as more authoritative than it is** (R5). A
grader written by us against a hidden upstream grader is `advisory`. A grader
that shipped with the material and passes the gates is `authoritative`. The
framework refuses to render the first as the second — and it refuses in code,
at the point the record is read, so no consumer has to remember.

⛔ **Run and Submit are different acts, and the distinction is in the data.**
The record carries two commands, and only a passing `test` run completes a
practice. A unit document with one command could not stop a program that merely
printed from completing one.

⭐ **A corpus with no graders is complete, not short.** It finishes at the
reading floor, which is a whole product for prose material.

## What is in the package

| Module | Owns |
|---|---|
| `states` | the three states, the two acts, and what may complete a practice |
| `record` | `Exercise`, and reading one out of a practice document |
| `keys` | the record's key order, and the refusals of its shape |
| `concepts` | what an exercise practises, as the reader is told it on its card |
| `cases` | the case vocabulary: `kind`, `cases`, `report` and `origin` |
| `report` | folding a run's JUnit report through those cases into a breakdown |
| `quiz` | the quiz: its questions, its key, and the rule that grades them |
| `gates` | the authoring gates and the gate record they write |
| `bundle` | what an authored exercise is on disk, and what it emits |
| `safety` | what a path and a command may be, checked before either reaches a file |
| `errors` | `ExerciseError`, the only exception any of it raises |

⭐ **A graded exercise may say what it is checked IN** (spec §7 §10):
its `cases` — each one an `id` as the test report spells it, a `kind` of `main`
or `edge`, and `says`, the sentence a reader is shown — plus the `report` those
ids are read out of and the `origin` the exercise was built from. ⛔ The
breakdown is a **report and never a second definition of a pass**: a practice
completes when every case passes, exactly as before.

⭐ **And what a run then REPORTED is folded back through them**:
`breakdown_of` reads the JUnit XML the run wrote, refuses it if it is stale,
malformed or names a test the map does not, and answers *main ask* plus *edge
cases n/m*. ⛔ The breakdown is a **report and never a second definition of a
pass**, which is the sentence above said from the other end.

⭐ **And what an authored exercise CLEARED before it shipped is
`gates`** (spec §7 §6): `G1`–`G5` read over the runs a caller takes in
the pinned runner image, and one `GateRecord` carrying the digest of every
input beside each gate's verdict. ⛔ **No gate can be disabled, skipped or
weakened by configuration**, and a second family of gates writes into
that same record without the package being edited. ⚠️ The gates are not part of
reading a record: nothing in this module calls them, and a reader's machine
never runs one.

⭐ **And where an authored exercise LIVES is `bundle`** (spec §7):
the directory a corpus repository commits for one — its statement, its
starter, its reference solution, its tests, its plants, its `cases` and the
gate record beside them — plus the emission an adapter calls to turn it into a
practice document and the reader's own workspace. ⛔ **The framework never
authors and never reaches into a corpus**: every source-specific fact arrives
as the bundle's own data (R1), and `studyforge validate` re-reads the gate
record through `validate.exercises`.

⛔ **Structural, with no flag and no version bump.** A record written before
these keys reads unchanged and round-trips to the same bytes; a document carrying
these keys is refused by a build that predates them, because the record's key
set is closed.

⭐ **An exercise whose `kind` is `quiz` carries `questions` in place of a
workspace** (spec §7 §7) — a stem, an ordered set of options, exactly
one keyed correct, and one sentence per option. ⛔ **It is graded with no
compiler, no container, no network and no model**: the key ships in the
practice document and the rule is `studyforge.exercise.quiz`'s, so the reading
is identical over `file://` and over a served origin (R8). ⛔ **A quiz produces
no run**, so it names no `test_path`, `completes_practice` answers `False` for
it in every act, and its own completion rule is `quiz.completes` — every
question answered correctly, recorded through the reader's state.
"""

from __future__ import annotations

from studyforge.exercise.cases import (
    BREAKDOWN_KEYS,
    CASE_ID_PERMITTED,
    CASE_KEYS,
    CASE_KINDS,
    CODE,
    DEFAULT_KIND,
    EDGE,
    EXERCISE_KINDS,
    JUNIT,
    MAIN,
    ORIGIN_KEYS,
    QUIZ,
    REPORT_FORMATS,
    REPORT_KEYS,
    Case,
    Origin,
    Report,
    cases_document,
    cases_of,
    kind_of,
    origin_document,
    origin_in,
    report_document,
    report_of,
)
from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.keys import (
    AUTHORED_KEYS,
    DEFAULTED_KEYS,
    EXERCISE_KEYS,
    GRADER_KEYS,
    REQUIRED_KEYS,
)
from studyforge.exercise.record import (
    Exercise,
    from_document,
    of,
    to_document,
)
from studyforge.exercise.report import (
    CLOCK_SLACK,
    PASSING_CHILDREN,
    REPORT_ROOTS,
    REPORT_SUFFIX,
    Breakdown,
    breakdown_of,
)
from studyforge.exercise.safety import (
    ARGUMENT_PERMITTED,
    PATH_PERMITTED,
    SAFE_ARGUMENT,
    SAFE_SEGMENT,
    require_command,
    require_path,
)
from studyforge.exercise.states import (
    COMMANDS,
    EXERCISE_KEY,
    GRADED,
    GRADER_KEY,
    NONE,
    RUN,
    STATES,
    TEST,
    UNGRADED,
    completes_practice,
    state_of,
)

#: ⛔ The package's whole public surface. A consumer that has to import
#: `studyforge.exercise.record` directly is a consumer this contract failed —
#: R17 makes the package's `__init__.py` its contract, and
#: this is what it says.
__all__ = [
    "ARGUMENT_PERMITTED",
    "AUTHORED_KEYS",
    "BREAKDOWN_KEYS",
    "Breakdown",
    "CASE_ID_PERMITTED",
    "CASE_KEYS",
    "CASE_KINDS",
    "CLOCK_SLACK",
    "CODE",
    "COMMANDS",
    "Case",
    "DEFAULTED_KEYS",
    "DEFAULT_KIND",
    "EDGE",
    "EXERCISE_KEY",
    "EXERCISE_KEYS",
    "EXERCISE_KINDS",
    "Exercise",
    "ExerciseError",
    "GRADED",
    "GRADER_KEY",
    "GRADER_KEYS",
    "JUNIT",
    "MAIN",
    "NONE",
    "ORIGIN_KEYS",
    "Origin",
    "PASSING_CHILDREN",
    "PATH_PERMITTED",
    "QUIZ",
    "REPORT_FORMATS",
    "REPORT_KEYS",
    "REPORT_ROOTS",
    "REPORT_SUFFIX",
    "REQUIRED_KEYS",
    "RUN",
    "Report",
    "SAFE_ARGUMENT",
    "SAFE_SEGMENT",
    "STATES",
    "TEST",
    "UNGRADED",
    "breakdown_of",
    "cases_document",
    "cases_of",
    "completes_practice",
    "from_document",
    "kind_of",
    "of",
    "origin_document",
    "origin_in",
    "report_document",
    "report_of",
    "require_command",
    "require_path",
    "state_of",
    "to_document",
]
