r"""Checks about an exercise claiming the source's own grader: its derivation's two gates.

**What it does.** For every practice document whose grader is `authoritative`,
it requires the record of the derivation's two gates beside the exercise,
requires that record to support the claim, requires both gates to have held,
and re-digests every file the record names against the corpus.

**How you use it.** `CHECKS`, which `validate.run` drains like every other
check's tuple. Each function takes the `Walk` and yields `Finding`s.

**Depends on.** `exercise` for the record, `exercise.bundle` for where the
record sits, `exercise.gates` for the derivation family and the drift reading,
`validate.exercises` for reading a gate record, `validate.corpus`,
`validate.report`.

⛔ **Every check yields; none raises** (R6), for `validate.exercises`' reason.

## ⛔ `authoritative` IS THE TRIGGER

⭐ **Only an exercise derived from a shipped grader earns `bundled` ·
`authoritative`** (R5, spec §7), and the derivation proves itself: Gate 1
blanks each taught method alone and the shipped test must fail on it, Gate 2
runs the shipped test against the original and it must pass. ⛔ So an
`authoritative` exercise with no record of those gates is refused rather than
believed, and one whose record does not support the claim is refused naming
why. ⭐ An exercise whose grader is not the derivation's declares `advisory`,
which is the honest claim and asks nothing of this module.

⚠️ **The record sits where an authored exercise's does**, at the practice's
`gates.json` under the exercises root, and it is read by the same reader,
version and all. Its inputs are spelled from the corpus root, because a derived
exercise's files are the reader's workspace and the source's original, not a
bundle's copies.
"""

from __future__ import annotations

from collections.abc import Iterator

from studyforge.archive.scrub import PersonalDataLeak
from studyforge.exercise import ExerciseError
from studyforge.exercise import of as exercise_of
from studyforge.exercise.bundle import Places
from studyforge.exercise.gates import drifted, faults
from studyforge.validate.corpus import Unit, Walk
from studyforge.validate.exercises import RULE_PERSONAL_DATA, read_record
from studyforge.validate.report import Finding

#: An `authoritative` exercise with no record of its derivation, or one that
#: does not support the claim.
RULE_DERIVATION_RECORD = "derivation-record"

#: A derivation record in which not both gates held.
RULE_DERIVATION_SHORTFALL = "derivation-shortfall"

#: A file the derivation record names that no longer digests to what it says.
RULE_DERIVATION_DIGEST = "derivation-digest"


def check_derivations(walk: Walk) -> Iterator[Finding]:
    """Every `authoritative` exercise ships the record of its derivation, and it holds."""
    for unit in walk.units:
        claimed = _claimed(unit)
        if claimed is None:
            continue
        places, exercise = claimed
        where = unit.where
        path = walk.root / places.gates
        if not path.is_file():
            yield Finding(
                RULE_DERIVATION_RECORD,
                where,
                f"declares an authoritative grader and ships no record of its derivation "
                f"at '{places.gates}'. Only an exercise derived from the source's own "
                f"grader through the two gates is authoritative, so the claim is refused "
                f"rather than believed; a grader that was not derived is advisory.",
            )
            continue
        try:
            record = read_record(path, places.gates)
        except PersonalDataLeak as error:
            yield Finding(RULE_PERSONAL_DATA, where, str(error))
            continue
        except (ExerciseError, ValueError) as error:
            message = f"ships a derivation record that will not read: {error}"
            yield Finding(RULE_DERIVATION_RECORD, where, message)
            continue
        found = faults(record, exercise.main_path, exercise.test_path)
        for sentence in found:
            yield Finding(
                RULE_DERIVATION_RECORD,
                where,
                f"declares an authoritative grader and {sentence}.",
            )
        if found:
            continue
        if not record.clears:
            yield Finding(
                RULE_DERIVATION_SHORTFALL,
                where,
                f"declares an authoritative grader and its derivation's "
                f"{list(record.refused_by)} did not hold. A derivation that fails a gate "
                f"ships no exercise for that hole (spec §7); a gate is never loosened.",
            )
        yield from _drift(walk, record, places, where)


def _drift(walk: Walk, record, places: Places, where: str) -> Iterator[Finding]:
    """One finding per file the record names that has changed since the gates ran."""
    try:
        sentences = drifted(walk.root, record.inputs, places.gates)
    except ExerciseError as error:
        yield Finding(RULE_DERIVATION_DIGEST, where, str(error))
        return
    for sentence in sentences:
        yield Finding(RULE_DERIVATION_DIGEST, where, sentence)


def _claimed(unit: Unit):
    """Return `(places, exercise)` for a practice claiming an authoritative grader, or `None`."""
    document = unit.document
    if document.get("kind") != "practice":
        return None
    try:
        exercise = exercise_of(document, unit.where)
    except ExerciseError:  # pragma: no cover - `validate.corpus` refuses it first
        return None
    if exercise is None or not exercise.authoritative:
        return None
    number, ordinal = document.get("unit"), document.get("ordinal")
    if not isinstance(number, int) or not isinstance(ordinal, int):
        # ⛔ `structure` already refuses a document whose identity is not a number.
        return None
    places = Places(unit.container.address, unit.container.variant, number, ordinal)
    return places, exercise


CHECKS = (check_derivations,)
