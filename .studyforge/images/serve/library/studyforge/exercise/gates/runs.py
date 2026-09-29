"""What one run of an authored exercise's tests reported, and the seam a gate takes runs through.

**What it does.** Names the roles a gate suite runs the tests against —
the reference, the starter, and one planted solution per edge case — defines
what one such run reports, and folds a run's JUnit report into it.

**How you use it.**

    from studyforge.exercise.gates.runs import Run, folded, PLANT, plant_role

    def attempt(role: str, attempt: int) -> Run:
        started = time.time()
        exit_code = ...                 # run the tests with `role`'s file in place
        return folded(exercise, root, role, attempt, exit_code, where, started=started)

**Depends on.** `studyforge.exercise.cases` for `Case`, `record` for
`Exercise`, `report` for the fold, and `errors` for the one exception.
⛔ **Not on `execute`, and this is the point of the seam**: `execute` imports
`studyforge.exercise`, so a gate that reached back for it would make the two
packages circular. ⭐ **The caller runs the command and hands the result over**
— which is also what lets the gates be read in the pinned runner image without
this package knowing a container exists (spec §7 §6).

## ⛔ THREE OUTCOMES, AND TWO OF THEM ARE NOT A BREAKDOWN

| what the run left behind | `passed` | `refusal` |
|---|---|---|
| a report this build folded | ⭐ the ids that passed | `None` |
| **no report at all** | `None` | `None` |
| a report that could not be folded | `None` | ⛔ the sentence saying why |

⚠️ **They are three and not two because the gates answer differently for
each.** A starter that does not build writes no report, and *no test passed*
is exactly what `G2` needs to see; a report naming a test the case map does not
is `G4`'s defect and must never read as *nothing passed*. ⛔ Collapsing the
second and third would make an authoring defect indistinguishable from a
starter doing its job.

## ⛔ A CASE THE REPORT DID NOT NAME DID NOT PASS

⭐ `report.Breakdown` already answers that way and this carries it unchanged: a
run that stops early names fewer tests, and a case with no result is not a case
that succeeded. ⚠️ So `Run.passed(case_id)` is `False` for a case nothing
reported, and `G4` is the gate whose job is to notice that on the reference.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from typing import Protocol

from studyforge.describe import describe
from studyforge.exercise.cases import Case
from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.record import Exercise
from studyforge.exercise.report import breakdown_of

#: The reader's own answer, written by the author to prove the exercise is
#: solvable. ⭐ `G1` runs it twice.
REFERENCE = "reference"

#: What the reader starts from. ⭐ `G2` runs it once.
STARTER = "starter"

#: The prefix of a planted solution's role: `plant:<case id>`. ⭐ `G3` runs one
#: per edge case, which is what makes the gate as fine as the claim it backs.
PLANT = "plant"

#: ⛔ The two runs `G1` takes, numbered, because *the same outcome each time* is
#: a claim about two readings and a caller must be able to tell them apart.
FIRST, SECOND = 1, 2


@dataclass(frozen=True, slots=True)
class Run:
    """What one run of the tests, with one solution in place, reported.

    ⛔ Frozen and derived: `passed_ids` came out of a report of THAT run, which
    is what `folded` guarantees and what constructing one by hand does not.
    """

    role: str
    attempt: int
    exit_code: int
    passed_ids: frozenset[str] | None = None
    refusal: str | None = None

    @property
    def reported(self) -> bool:
        """Did this run leave a report this build could fold? ⛔ Neither other outcome."""
        return self.passed_ids is not None

    def passed(self, case: Case) -> bool:
        """Did this case pass? ⛔ A case nothing reported did not pass."""
        return self.passed_ids is not None and case.id in self.passed_ids


class Attempt(Protocol):
    """How a gate suite asks for one run: the role to run, and which attempt it is.

    ⭐ The caller owns everything this package deliberately does not know — the
    workspace, the container, the build tool, how a solution is put in place —
    and answers with what the run reported. ⛔ It raises nothing for a run that
    merely failed: a failing run is evidence, and several gates need one.
    """

    def __call__(self, role: str, attempt: int) -> Run:
        """Run the exercise's tests with `role`'s solution in place, and fold the report."""


def plant_role(case: Case) -> str:
    """Return the role of the solution planted to fail exactly this edge case."""
    return f"{PLANT}:{case.id}"


def folded(
    exercise: Exercise,
    root: Path,
    role: str,
    attempt: int,
    exit_code: int,
    where: str,
    *,
    started: float,
) -> Run:
    """Fold the report a run left behind into the evidence a gate reads.

    `started` is the wall clock read **before** the run was started, so a
    report an earlier run wrote is refused rather than folded.
    ⭐ A fold that refuses is carried as a `refusal` and never raised: an
    authoring defect is a gate's finding, not this function's crash.
    """
    try:
        breakdown = breakdown_of(exercise, root, where, started=started)
    except ExerciseError as error:
        return Run(role=role, attempt=attempt, exit_code=exit_code, refusal=str(error))
    if breakdown is None:
        return Run(role=role, attempt=attempt, exit_code=exit_code)
    return Run(
        role=role,
        attempt=attempt,
        exit_code=exit_code,
        passed_ids=breakdown.passed_ids,
    )


def require_run(value: object, role: str, attempt: int, where: str) -> Run:
    """Refuse anything that is not this role's run — ⛔ a gate reads the run it asked for.

    ⚠️ **The check is not ceremony.** The caller supplies the runs, so a caller
    that answered every role with one run would give `G2` the reference's
    reading and every gate would hold. ⛔ A gate suite that can be satisfied by
    a caller's bookkeeping error is one nobody should believe.

    ⛔ **Neither sentence reproduces the ROLE** (R7). A plant role carries a
    case id, whose permitted set is wider than a workspace path — it holds `/`
    and `.` — so a role is a value of a shape nobody has bounded. ⚠️
    **MEASURED** by `tests/test_emission.py::test_no_refusal_reproduces_the_value_it_refused`,
    which poisoned this parameter and read a home path straight back out.
    """
    if not isinstance(value, Run):
        raise ExerciseError(
            f"{where}: a gate reads a Run for each role it asked for, and attempt "
            f"{attempt} of one of them is {describe(value)}."
        )
    if value.role != role or value.attempt != attempt:
        raise ExerciseError(
            f"{where}: a gate asked for one role's attempt {attempt} and was handed "
            f"another run. A suite answered from the wrong run is one whose gates all "
            f"hold for a reason nobody intended. The roles are not reproduced here, since a "
            f"refusal never quotes a value that may be personal."
        )
    return value
