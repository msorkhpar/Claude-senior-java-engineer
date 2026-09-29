"""The derivation's two gates: what an exercise derived from a shipped grader must record.

**What it does.** Declares the `derivation` gate family — `D1`, every blanked
method's body alone made the shipped test fail, and `D2`, the shipped test
passes against the original — and answers, for one exercise claiming the
source's own grader, every way its record fails to support that claim (spec §7,
"Exercises derived from a shipped grader").

**How you use it.**

    from studyforge.exercise.gates.derivation import DERIVATION, D1, D2, hole, faults

    Verdict(id=D1, family=DERIVATION.name, held=True, says="...",
            recorded=((hole("primary"), "expected 40 but was 00"),))
    faults(record, main_path, test_path)   # () when the record supports the claim

**Depends on.** `families` to register, `record` for the record and its
verdicts, `runs` for the starter and reference roles. Standard library only.

## ⛔ THE CLAIM IS `authoritative`, AND THIS RECORD IS ITS ONLY EVIDENCE

⭐ **Deriving an exercise by blanking what a lesson teaches is the only way an
exercise earns `bundled` · `authoritative`** (R5). An adapter runs the gates;
this module is what `studyforge validate` reads back, so the claim no longer
rests on the adapter's say-so. ⛔ A gate record from another family is not
evidence for it: an authored exercise's gates prove an advisory grader, and a
gate record never promotes a claim.

## ⛔ WHAT THE RECORD MUST CARRY

- ⭐ **One verdict per gate, `D1` then `D2`, and no other family's.**
- ⭐ **`D1` names every hole it blanked** as a `hole:<method>` key of its
  `recorded` evidence, the attributed failure as its value. Gate 1 is
  per-method, so a `D1` naming no hole blanked nothing and sets no work.
- ⭐ **Its inputs are the three files, spelled from the corpus root**: the
  `starter` the reader edits and the `tests` that grade it — the exercise
  record's own `main_path` and `test_path` — and the `reference`, the original
  the holes were blanked from.
- ⛔ **The starter is not the reference.** A starter identical to the original
  has nothing blanked, so the shipped test passes before the reader starts.

⚠️ What is checked is the record, never the language: nothing here parses a
method or runs a test (R1). Running both gates is the adapter's work.
"""

from __future__ import annotations

from studyforge.exercise.gates.families import Family, register
from studyforge.exercise.gates.record import GateRecord
from studyforge.exercise.gates.runs import REFERENCE, STARTER

#: ⭐ The family, registered at import. Gate 1 is `D1`, Gate 2 is `D2`.
DERIVATION = register(Family("derivation", ("D1", "D2")))

#: Gate 1: each selected method's body, blanked alone, makes the test fail.
#: Gate 2: the test passes against the original.
D1, D2 = DERIVATION.gates

#: The role the shipped test takes. ⭐ The one spelling: `bundle.layout` takes it.
TESTS = "tests"

#: The roles a derivation record's inputs must carry.
DERIVED_ROLES = (STARTER, REFERENCE, TESTS)

#: The prefix of each key `D1` records, one per blanked method.
HOLE = "hole"


def hole(method: str) -> str:
    """Return the key `D1` records a blanked method under: `hole:<method>`."""
    return f"{HOLE}:{method}"


def holes(record: GateRecord) -> tuple[str, ...]:
    """Return the methods `D1` records as blanked, in the order written."""
    verdict = record.verdict(D1)
    if verdict is None:
        return ()
    prefix = f"{HOLE}:"
    return tuple(key[len(prefix) :] for key, _ in verdict.recorded if key.startswith(prefix))


def faults(record: GateRecord, main_path: str | None, test_path: str | None) -> tuple[str, ...]:
    """Every way `record` fails to support an exercise's `authoritative` claim, one sentence each.

    ⛔ A gate that did not hold is not a fault here: `record.clears` answers
    that, and a caller reports it as a shortfall rather than as a bad record.
    """
    others = sorted({verdict.family for verdict in record.verdicts} - {DERIVATION.name})
    if others or record.verdict(D1) is None:
        return (
            f"its gate record carries {'the ' + ', '.join(others) if others else 'no'} "
            f"family's verdicts where the derivation's {list(DERIVATION.gates)} belong. "
            f"Only those gates are evidence that an exercise was derived from the "
            f"source's own grader, and no other gate promotes a claim",
        )
    found = []
    blanked = holes(record)
    written = [key for key, _ in record.verdict(D1).recorded]
    if not blanked or len(blanked) != len(written):
        found.append(
            f"its gate record's {D1} names {len(blanked)} blanked method(s) among "
            f"{len(written)} recorded key(s). Gate 1 is per method: every key is "
            f"'{HOLE}:<method>' and there is at least one, or nothing was blanked"
        )
    inputs = {entry.role: entry for entry in record.inputs}
    missing = [role for role in DERIVED_ROLES if role not in inputs]
    if missing:
        found.append(
            f"its gate record digests no {missing}. The derivation is read over the "
            f"starter, the shipped test and the original it was blanked from"
        )
        return tuple(found)
    for role, declared in ((STARTER, main_path), (TESTS, test_path)):
        if inputs[role].path != declared:
            found.append(
                f"its gate record's {role!r} is not the file the exercise names for it, "
                f"so the gates were read over some other exercise's files"
            )
    if inputs[STARTER].digest == inputs[REFERENCE].digest:
        found.append(
            "its gate record's starter digests to the original it was derived from, so "
            "nothing was blanked and the test passes before the reader starts"
        )
    return tuple(found)
