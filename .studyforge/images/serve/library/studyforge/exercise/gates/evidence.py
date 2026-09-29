"""Every run a code gate suite needs, taken before any gate is read — and never fewer.

**What it does.** Works out, from the exercise's own case map, exactly which
runs the gates require — the reference twice, the starter once, one planted
solution per edge case — asks the caller for each of them, and hands the gates
a reading they cannot add to or subtract from.

**How you use it.**

    from studyforge.exercise.gates.evidence import Evidence, required_roles

    evidence = Evidence.taken(exercise, attempt, where)
    evidence.of(REFERENCE, FIRST)

**Depends on.** `runs` for what one run is, `studyforge.exercise.cases` for
`Case`, `record` for `Exercise`, and `errors`. Standard library only.

## ⛔ THE RUN LIST IS DERIVED FROM THE RECORD, NOT PASSED IN

⭐ **This is where *no gate can be skipped* is structural rather than
promised.** `taken` has no argument that names a role, a gate or a subset:
`required_roles` reads the case map and returns the whole list, and every one
of them is asked for. ⚠️ A signature that accepted *which runs to take* would
make a gate suite that skipped `G3` a legal call rather than a defect — and
nobody reading the record afterwards could tell.

⛔ **An exercise with no case map is refused here**, not silently given an
empty suite. A gate reading with nothing to fold is not a green reading.

## ⛔ THE ORDER MATTERS, AND IT IS THE ORDER A CACHE WOULD BREAK

⭐ The reference is run first and twice, then the starter, then the plants in
the case map's own order. ⚠️ A caller that memoised its runs by role would
hand `G1` one reading twice and its flakiness arm would be unfalsifiable — so
each run carries the attempt it was, `require_run` checks it, and `G1` compares
two readings it knows are two.
"""

from __future__ import annotations

from dataclasses import dataclass

from studyforge.exercise.cases import EDGE, Case
from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.gates.runs import (
    FIRST,
    REFERENCE,
    SECOND,
    STARTER,
    Attempt,
    Run,
    plant_role,
    require_run,
)
from studyforge.exercise.record import Exercise


@dataclass(frozen=True, slots=True)
class Evidence:
    """Every run the code gates read, in the order it was taken."""

    runs: tuple[Run, ...]

    @classmethod
    def taken(cls, exercise: Exercise, attempt: Attempt, where: str) -> Evidence:
        """Take every run the gates require, asking `attempt(role, n)` for each.

        ⛔ There is no argument selecting which: the list comes from the case
        map, so a caller cannot ask for a suite with a gate's evidence missing.
        """
        wanted = required_roles(exercise, where)
        return cls(
            runs=tuple(
                require_run(attempt(role, number), role, number, where) for role, number in wanted
            )
        )

    def of(self, role: str, attempt: int) -> Run | None:
        """Return the run taken for this role and attempt, or `None` if none was."""
        return next(
            (run for run in self.runs if run.role == role and run.attempt == attempt),
            None,
        )


def required_roles(exercise: Exercise, where: str) -> tuple[tuple[str, int], ...]:
    """Every `(role, attempt)` the code gates need, derived from the exercise's own cases."""
    cases = declared_cases(exercise, where)
    return (
        (REFERENCE, FIRST),
        (REFERENCE, SECOND),
        (STARTER, FIRST),
        *((plant_role(case), FIRST) for case in cases if case.kind == EDGE),
    )


def edges(exercise: Exercise, where: str) -> tuple[Case, ...]:
    """Return the edge cases in the order the corpus wrote them — one plant each (`G3`)."""
    return tuple(case for case in declared_cases(exercise, where) if case.kind == EDGE)


def declared_cases(exercise: Exercise, where: str) -> tuple[Case, ...]:
    """Return the case map, refusing an exercise the gates have nothing to read."""
    if not exercise.breaks_down or exercise.cases is None:
        raise ExerciseError(
            f"{where}: the gates are read over an exercise's 'cases' and the "
            f"'report' they are folded out of, and this record declares neither. A "
            f"gate suite with nothing to fold answers nothing, and answering nothing "
            f"green is the theatre these gates exist to prevent."
        )
    return exercise.cases
