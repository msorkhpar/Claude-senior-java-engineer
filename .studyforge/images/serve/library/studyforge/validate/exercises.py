r"""Checks about an authored exercise: its gate record, and the files it was taken over.

**What it does.** The arm authored exercises owe `studyforge validate`. For every
practice document whose grader is `generated`, it requires the gate record
the authoring gates write, requires that record to have cleared, re-digests every input it
names against the bundle beside it, and refuses a bundle holding a file its
shape does not permit.

**How you use it.** `CHECKS`, which `validate.run` drains like every other
check's tuple. Each function takes the `Walk` and yields `Finding`s.

**Depends on.** `exercise.bundle` for where a bundle is and what may be in it,
`exercise.gates` for the record and the drift reading, `exercise` for the
record and `ExerciseError`, `unit.trust` for the one provenance this arm is
about, `validate.corpus`, `validate.report`.

⛔ **Every check yields; none raises.** One run reports every problem (R6), and
an authored corpus is exactly the case where a validator that stopped at the
first bad bundle would be run once and abandoned.

## ⛔ `generated` IS THE TRIGGER, AND IT IS THE WHOLE OF R5's HONESTY AS A GATE

⭐ **R5, as gates**: an authored grader is `generated`, therefore
`advisory`, and ships **only** with a gate record `validate` re-reads. ⛔ So a
`generated` exercise with no record is refused rather than trusted — otherwise
the cheapest way to ship an ungated exercise would be to delete the record that
refused it, which is the shape the gate record already refuses inside itself
(`all(())` is `True`).

⚠️ **A `bundled` exercise is untouched.** Its grader shipped with the material
and its own derivation gates answer for it; nothing here widens to it, so a
corpus that predates this milestone validates exactly as it did.

## ⛔ CLEARED AND INTACT ARE TWO QUESTIONS

⭐ `GateRecord.clears` reads the **verdicts** — did the bundle earn its place —
and `drifted` reads the **files** — is the reading still about them. ⛔ A
record that cleared is not a bundle that is intact, so this module owes both
and asks them separately, with a rule id each.

⚠️ **`origins` are NOT re-digested here.** They name material in the corpus's
source, which `G5` resolved against the ledger at authoring time; re-resolving
one would need the ledger, and `validate` may be run where it is not.
"""

from __future__ import annotations

import json
from collections.abc import Iterator

from studyforge.archive.scrub import PersonalDataLeak, assert_clean
from studyforge.exercise import ExerciseError
from studyforge.exercise import of as exercise_of
from studyforge.exercise.bundle import BUNDLE_DIRNAMES, RUN_OUTPUT_DIRNAME, Places, unpermitted
from studyforge.exercise.gates import drifted, record_of
from studyforge.validate.corpus import Unit, Walk
from studyforge.validate.report import Finding

#: A `generated` exercise that ships no gate record at all.
RULE_GATE_RECORD = "gate-record"

#: A gate record that is there but does not say every gate held.
RULE_GATE_SHORTFALL = "gate-shortfall"

#: An input whose file no longer digests to what the record says.
RULE_BUNDLE_DIGEST = "bundle-digest"

#: A bundle holding a file its shape does not permit.
RULE_BUNDLE_CONTENTS = "bundle-contents"

#: A page whose exercises are not numbered `1..n`.
RULE_PRACTICE_ORDINALS = "practice-ordinals"

#: ⛔ A gate record carrying personal data. ⚠️ `validate.corpus`' own spelling,
#: because it is the same rule and two ids for one fact is two audits.
RULE_PERSONAL_DATA = "personal-data"

#: ⛔ The one provenance this arm is about. `unit.trust` owns the vocabulary and
#: this is a reference to its member, never a second spelling of the rule.
GENERATED = "generated"


def check_gate_records(walk: Walk) -> Iterator[Finding]:
    """Every `generated` exercise ships a gate record, and it cleared.

    ⛔ **Absent, unreadable and did-not-clear are three findings, not one.** An
    integrator fixing an authored corpus needs to know which of the three they
    have: the first is a bundle that never ran the gates, the second is one
    whose record nothing can read, and the third is one the gates refused.
    """
    for unit in walk.units:
        authored = _authored(unit)
        if authored is None:
            continue
        places, where = authored
        path = walk.root / places.gates
        if not path.is_file():
            yield Finding(
                RULE_GATE_RECORD,
                where,
                f"declares a 'generated' grader and ships no gate record at "
                f"'{places.gates}'. An authored grader is advisory and ships only "
                f"with the record of the gates it cleared (spec §7), so one without "
                f"is refused rather than trusted.",
            )
            continue
        record = None
        try:
            record = read_record(path, places.gates)
        except PersonalDataLeak as error:
            # ⛔ FIRST, and its own arm: `validate.corpus` sets the shape, and
            # two finding rules must not collapse into one.
            yield Finding(RULE_PERSONAL_DATA, where, str(error))
        except (ExerciseError, ValueError) as error:
            yield Finding(
                RULE_GATE_RECORD, where, f"ships a gate record that will not read: {error}"
            )
        if record is not None and not record.clears:
            yield Finding(
                RULE_GATE_SHORTFALL,
                where,
                "ships a gate record in which not every gate held. A shortfall is "
                "reported rather than engineered away: re-author the exercise, "
                "never loosen the gate.",
            )


def check_bundle_digests(walk: Walk) -> Iterator[Finding]:
    """Every input a gate record names still digests to what the record says.

    ⛔ **Naming the file is the whole point** — `gates.drifted` answers with one
    sentence per input rather than a boolean for exactly that reason.
    """
    for unit in walk.units:
        authored = _authored(unit)
        if authored is None:
            continue
        places, where = authored
        record = _record(walk, places)
        if record is None:
            continue
        try:
            sentences = drifted(walk.root / places.bundle, record.inputs, places.bundle)
        except ExerciseError as error:
            yield Finding(RULE_BUNDLE_DIGEST, where, str(error))
            continue
        for sentence in sentences:
            yield Finding(RULE_BUNDLE_DIGEST, where, sentence)


def check_bundle_contents(walk: Walk) -> Iterator[Finding]:
    """Refuse a bundle holding anything its shape does not permit.

    ⛔ **This is where a committed run report is refused.** A JUnit report carries the
    machine's hostname — `pytest --junit-xml` and surefire both write
    `hostname="…"` — and a corpus repository is where this repository's own
    personal-data gate never looks (R7). A run's report is a run artifact; a
    bundle that holds one is named here, by file.
    """
    for unit in walk.units:
        authored = _authored(unit)
        if authored is None:
            continue
        places, where = authored
        for found in unpermitted(walk.root, places):
            yield Finding(
                RULE_BUNDLE_CONTENTS,
                where,
                f"its bundle holds '{found}', which a bundle's shape does not permit. "
                f"A bundle carries its document, its statement, its gate record and "
                f"the files under {', '.join(repr(one) for one in BUNDLE_DIRNAMES)}, "
                f"none of them in a '{RUN_OUTPUT_DIRNAME}' directory. A run's report "
                f"in particular is a run artifact and never a bundle input: it "
                f"carries the machine's hostname.",
            )


def check_practice_ordinals(walk: Walk) -> Iterator[Finding]:
    """Every page's practices take the ordinals `1..n` with no gap.

    ⚠️ **Counted per unit, and it is not `structure`'s practice count said
    twice.** That check compares how many practices a container *declares*
    against how many the archive holds; this one asks whether the ones that are
    there are numbered without a hole — a corpus can declare two, hold two, and
    number them 1 and 3.
    """
    seen: dict[tuple[str, str, int], list[int]] = {}
    for unit in walk.units:
        document = unit.document
        if document.get("kind") != "practice":
            continue
        key = (unit.container.address.key, unit.container.variant, document.get("unit"))
        seen.setdefault(key, []).append(document.get("ordinal"))
    for (address, variant, number), found in sorted(seen.items(), key=lambda item: str(item[0])):
        ordered = sorted(value for value in found if isinstance(value, int))
        if ordered != list(range(1, len(found) + 1)):
            yield Finding(
                RULE_PRACTICE_ORDINALS,
                f"{address}/{variant}/unit-{number}",
                f"holds {len(found)} practice(s) and does not number them "
                f"{list(range(1, len(found) + 1))}. A gap is a missing exercise, and "
                f"renumbering the rest would move every reader's recorded progress.",
            )


def _authored(unit: Unit) -> tuple[Places, str] | None:
    """Return the bundle of this document's authored exercise, or `None` if it has none."""
    document = unit.document
    if document.get("kind") != "practice":
        return None
    try:
        exercise = exercise_of(document, unit.where)
    except ExerciseError:  # pragma: no cover - `validate.corpus` refuses it first
        return None
    if exercise is None or exercise.provenance != GENERATED:
        return None
    number, ordinal = document.get("unit"), document.get("ordinal")
    if not isinstance(number, int) or not isinstance(ordinal, int):
        # ⛔ `structure` already refuses a document whose identity is not a
        # number, by name. Re-refusing it here would report one defect twice.
        return None
    return Places(unit.container.address, unit.container.variant, number, ordinal), unit.where


def read_record(path, where: str):
    """Decode one gate record and gate every string in it (R7).

    ⛔ **The gate runs before the record is read**, because a gate record is a
    document a corpus wrote and every string in it reaches a report line. ⚠️ It
    RAISES rather than scrubs: a scrubber that quietly rewrote would leave
    nobody knowing personal data had been there. ⛔ A file that cannot be read
    is refused naming `where`, never the path it was handed (R7).
    """
    try:
        text = path.read_text(encoding="utf-8")
    except OSError:
        raise ExerciseError(f"{where}: the gate record could not be read.") from None
    document = json.loads(text)
    assert_clean(document, where)
    return record_of(document, where)


def _record(walk: Walk, places: Places):
    """Return the gate record, or `None` — ⛔ absent and unreadable are `check_gate_records`'."""
    try:
        return read_record(walk.root / places.gates, places.gates)
    except ExerciseError, PersonalDataLeak, ValueError, OSError:
        return None


#: In the order a report reads best: is there a record, is the bundle intact,
#: is it holding anything it should not, and is the page numbered.
CHECKS = (
    check_gate_records,
    check_bundle_digests,
    check_bundle_contents,
    check_practice_ordinals,
)
