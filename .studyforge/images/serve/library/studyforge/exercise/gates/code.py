"""`G1`–`G5`: the five gates a code exercise clears before it ships, and the `code` family.

**What it does.** Reads spec §7 §6's five gates off the evidence a suite of
runs produced and the digests a bundle recorded, and answers each one with a
verdict — held or not, and the one sentence saying what it found. ⛔ It runs
nothing itself: the runs arrive through `runs.Attempt`, which is what lets the
suite be taken in the pinned runner image without this package knowing a
container exists.

**How you use it.**

    from studyforge.exercise.gates.code import CODE, check

    verdicts = check(exercise, evidence, origins, ledger, where)
    GateRecord(inputs=inputs, origins=origins, verdicts=verdicts).clears

**Depends on.** `evidence` for the runs, `runs` for what one is, `digests` for
a cited passage, `record` for `Verdict`, `studyforge.exercise.cases` for `Case`,
and `errors`. Standard library only.

## ⛔ THE FIVE, AND WHAT EACH ONE PROVES (spec §7 §6)

| gate | what must hold | what it proves |
|---|---|---|
| **G1** | every case passes on the reference, twice, alike | it is solvable, and not flaky |
| **G2** | **no** case passes on the starter | no test is vacuous |
| **G3** | per edge: its plant passes the ask, fails it | each edge test catches its omission |
| **G4** | every reported test maps to one case; every case is reported | the breakdown is total |
| **G5** | each cited passage still digests to the ledger's value | it is built from the source |

⚠️ **`G3` is per-case for the same reason a derivation gate is
per-method.** A gate coarser than the claim it backs is theatre, and it was
expensive to learn that once (R5). ⛔ So a suite with three edge cases reads
three plants and answers for each of them by name.

## ⛔ `check` TAKES NO OPTIONS, AND RETURNS ALL FIVE, ALWAYS

⭐ **There is no argument by which a caller asks for fewer gates**, no gate
that returns nothing, and no branch that leaves a verdict out. A gate with
nothing to read answers **did not hold** and says why — ⚠️ never *held
vacuously*, because a gate that passes for want of evidence is the exact defect
R5's gates exist to stop.

## ⛔ A FOLD THAT REFUSED IS `G4`'s FINDING, AND IT COSTS THE OTHERS THEIR EVIDENCE

⚠️ **Stated because it is visible in the record and would otherwise read as a
cascade of unrelated failures.** *A test the report names that the case map
does not* is refused by `report.breakdown_of` on every run of that exercise —
so `G4` names it, and `G1`, `G2` and `G3` answer **did not hold, no reading
could be folded**. ⭐ That is the honest answer: they were not read, and saying
they held would be inventing a reading.

## ⛔ A CASE IS NAMED BY ITS SENTENCE, NEVER BY ITS ID

⭐ `says` is the one sentence the corpus wrote for a reader and is already in
the practice document, so a record echoing it exposes nothing new. ⚠️ A case
`id` is a wider permitted set than a workspace path — it carries `/` and `.` —
so printing one into a record is a shape nobody has bounded (R7). ⛔ The one
sentence this module quotes from elsewhere is `report`'s own refusal, which
reproduces no id either.
"""

from __future__ import annotations

from collections.abc import Mapping

from studyforge.exercise.cases import MAIN, Case
from studyforge.exercise.gates.digests import ORIGIN_ROLE, Cited
from studyforge.exercise.gates.evidence import Evidence, declared_cases, edges
from studyforge.exercise.gates.families import Family, register
from studyforge.exercise.gates.record import Verdict
from studyforge.exercise.gates.runs import FIRST, REFERENCE, SECOND, STARTER, Run, plant_role
from studyforge.exercise.record import Exercise

#: The gates a code exercise clears, in the order spec §7 §6 states them.
G1, G2, G3, G4, G5 = "G1", "G2", "G3", "G4", "G5"

#: ⭐ Registered at import, so a record carrying these ids is complete only
#: when it carries all five. ⛔ `families` discovers this module; nothing has
#: to name it.
CODE = register(Family("code", (G1, G2, G3, G4, G5)))


def check(
    exercise: Exercise,
    evidence: Evidence,
    origins: tuple[Cited, ...],
    ledger: Mapping[str, str],
    where: str,
) -> tuple[Verdict, ...]:
    """Answer all five gates over this evidence — ⛔ always five, in order, never fewer."""
    cases = declared_cases(exercise, where)
    return (
        _g1(cases, evidence),
        _g2(cases, evidence),
        _g3(exercise, evidence, where),
        _g4(cases, evidence),
        _g5(exercise, origins, ledger),
    )


def _g1(cases: tuple[Case, ...], evidence: Evidence) -> Verdict:
    """Every test passes on the reference, on two runs, with the same outcome each time."""
    first, second = evidence.of(REFERENCE, FIRST), evidence.of(REFERENCE, SECOND)
    missing = _unread(G1, (first, second))
    if missing is not None:
        return missing
    if first.passed_ids != second.passed_ids or first.exit_code != second.exit_code:
        return _refused(
            G1,
            "the reference solution reported a different outcome on its two runs, so "
            "at least one of these tests is flaky and a reader's Submit would be "
            "decided by luck",
        )
    failed = [case for case in cases if not first.passed(case)]
    if failed:
        return _refused(
            G1,
            f"{len(failed)} of this exercise's {len(cases)} cases did not pass on the "
            f"reference solution, so the exercise is not solvable as written: "
            + _sentences(failed),
        )
    return _held(G1, f"all {len(cases)} cases passed on the reference, on two runs alike")


def _g2(cases: tuple[Case, ...], evidence: Evidence) -> Verdict:
    """Every test fails on the starter, not merely one — ⛔ no test is vacuous."""
    starter = evidence.of(STARTER, FIRST)
    if starter is None:
        return _refused(G2, "the starter was not run, so nothing says the work is undone")
    if starter.refusal is not None:
        return _refused(G2, _UNFOLDABLE)
    if not starter.reported:
        if starter.exit_code == 0:
            return _refused(
                G2,
                "the starter run wrote no report and still succeeded, so nothing shows "
                "the reader starting with the work undone",
            )
        return _held(
            G2,
            "the starter run failed before any test reported, so no test passed on it",
        )
    passed = [case for case in cases if starter.passed(case)]
    if passed:
        return _refused(
            G2,
            f"{len(passed)} of this exercise's cases passed on the starter, so that "
            f"many of its tests are vacuous — a reader would start with the work "
            f"already done for: " + _sentences(passed),
        )
    return _held(G2, f"none of the {len(cases)} cases passed on the starter")


def _g3(exercise: Exercise, evidence: Evidence, where: str) -> Verdict:
    """For each edge case, a plant that solves the ask and ignores exactly that edge."""
    declared = edges(exercise, where)
    if not declared:
        return _refused(
            G3,
            "this exercise names no edge case, so there is nothing for a Submit to "
            "report as 'edge cases n/m' and nothing for this gate to prove",
        )
    plants = tuple(evidence.of(plant_role(case), FIRST) for case in declared)
    if any(run is not None and run.refusal is not None for run in plants):
        return _refused(G3, _UNFOLDABLE)
    unproven = [case for case in declared if _g3_fails(exercise, evidence, case)]
    if unproven:
        return _refused(
            G3,
            f"{len(unproven)} of this exercise's {len(declared)} edge cases are not "
            f"caught by a test of their own: " + _sentences(unproven),
        )
    return _held(G3, f"each of the {len(declared)} edge cases was caught by its own plant")


def _g3_fails(exercise: Exercise, evidence: Evidence, case: Case) -> bool:
    """Whether this edge case's plant fails to prove the claim the edge makes."""
    run = evidence.of(plant_role(case), FIRST)
    if run is None or not run.reported:
        return True
    if run.passed(case):
        return True  # the solution that ignores this edge still passed its test.
    ask = [other for other in exercise.cases or () if other.kind == MAIN]
    return any(not run.passed(other) for other in ask)


def _g4(cases: tuple[Case, ...], evidence: Evidence) -> Verdict:
    """Every test the report names maps to one case, and every case is backed by a test."""
    reference = evidence.of(REFERENCE, FIRST)
    if reference is None:
        return _refused(G4, "the reference was not run, so no report was there to read")
    if reference.refusal is not None:
        return _refused(G4, reference.refusal)
    if not reference.reported:
        return _refused(
            G4,
            "the reference run wrote no report, so nothing says the Submit breakdown "
            "covers what actually ran",
        )
    unbacked = [case for case in cases if not reference.passed(case)]
    if unbacked:
        return _refused(
            G4,
            f"{len(unbacked)} of this exercise's {len(cases)} cases were not named as "
            f"passing in the reference run's report, so nothing backs them and the "
            f"breakdown a reader is shown is not total: " + _sentences(unbacked),
        )
    return _held(
        G4,
        f"every test the report named mapped to one of the {len(cases)} cases, and "
        f"every case was named",
    )


def _g5(exercise: Exercise, origins: tuple[Cited, ...], ledger: Mapping[str, str]) -> Verdict:
    """Each cited passage resolves to a ledger entry whose digest still matches."""
    origin = exercise.origin
    if origin is None:
        return _held(G5, "this exercise cites no source material, so nothing has drifted")
    cited = next((entry for entry in origins if entry.role == ORIGIN_ROLE), None)
    if cited is None:
        return _refused(
            G5,
            f"this exercise declares an origin and the record carries no "
            f"{ORIGIN_ROLE!r} digest for it, so nothing says which version of the "
            f"source it was built from",
        )
    if cited.path != origin.path or cited.section != origin.section:
        return _refused(
            G5,
            "the record's cited passage is not the one this exercise's origin names, "
            "so the digest below belongs to different material",
        )
    current = ledger.get(origin.path)
    if current is None:
        return _refused(
            G5,
            f"the source ledger has no entry for '{origin.path}', so this exercise "
            f"cites material the ledger does not account for",
        )
    if current != cited.digest:
        return _refused(
            G5,
            f"'{origin.path}' has changed in the source since this exercise was built "
            f"from it, so the exercise no longer reflects what the source has",
        )
    return _held(G5, f"the cited passage of '{origin.path}' still matches the ledger")


def _unread(gate: str, runs: tuple[Run | None, ...]) -> Verdict | None:
    """Answer for a gate whose runs are missing or unfoldable, or `None` if they are not."""
    if any(run is None for run in runs):
        return _refused(gate, "the runs this gate reads were not taken")
    if any(run.refusal is not None for run in runs):
        return _refused(gate, _UNFOLDABLE)
    if any(not run.reported for run in runs):
        return _refused(gate, "a run this gate reads left no report, so nothing was read")
    return None


def _sentences(cases: list[Case]) -> str:
    """Name each case by the one sentence a reader is shown — ⛔ never by its id."""
    return "; ".join(case.says for case in cases)


def _held(gate: str, says: str) -> Verdict:
    """Return a gate that held, with the sentence saying what was read."""
    return Verdict(id=gate, family=CODE.name, held=True, says=says)


def _refused(gate: str, says: str) -> Verdict:
    """Return a gate that did not hold, with the sentence saying what was found."""
    return Verdict(id=gate, family=CODE.name, held=False, says=says)


#: What every gate but `G4` says when the report could not be folded. ⚠️ It
#: names `G4` rather than repeating its finding, so one defect is described
#: once in a record that carries five verdicts.
_UNFOLDABLE = (
    "no reading could be folded out of this exercise's report, so this gate was not "
    "read at all — G4 names what is wrong with the report"
)
