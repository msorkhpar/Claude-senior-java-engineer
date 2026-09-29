"""The quiz gate suite: all five, in order, over an exercise no path can call authoritative.

**What it does.** Answers `Q1`–`Q5` over one quiz and returns the five verdicts
that go into the gate record `gates.record` owns. ⛔ It takes no options, returns all
five always, and refuses an exercise that is not a quiz or that claims anything
but `generated`/`advisory`.

**How you use it.**

    from studyforge.exercise.gates import GateRecord, record_document
    from studyforge.exercise.gates.quiz import check_quiz, cited_for

    origins = cited_for(exercise.questions, ledger, where)
    record = GateRecord(
        origins=origins,
        verdicts=check_quiz(exercise, judgements, origins, ledger, where),
    )
    record.clears                      # did this quiz earn its place
    record_document(record)            # and what ships beside it

**Depends on.** `judged` for `Q1`–`Q3`, `mechanical` for `Q4` and `Q5`,
`family` for the gate ids, `studyforge.exercise.quiz` for the one legal
`(provenance, trust)` pair, `studyforge.exercise.record` for `Exercise`, and
`studyforge.exercise.errors` for the one exception. Standard library only.

## ⛔ ASSERTING `authoritative` FAILS HERE TOO, AND THAT IS DELIBERATE DUPLICATION

⚠️ **`quiz.shape.require_quiz_shape` already refuses a document claiming
anything but `generated`/`advisory`** — so why refuse it again? ⭐ Because
`Exercise` is frozen and **not validated**: its own contract says a caller that
constructs one asks `from_document(to_document(...))` before believing it, and
a gate suite handed a hand-built `Exercise` would otherwise gate a quiz that
had never been through that reading. ⛔ **The quiz gates' promise is
*asserting `authoritative` anywhere in the chain fails*, and a chain is only
closed where every link is.**

⭐ **Stated positively, as the pair that is legal**, and imported
from `quiz.shape` rather than re-spelt — so a fourth provenance added to
`unit.trust` is refused here on the day it is added, without anybody deciding.

## ⛔ NO OPTION ASKS FOR FEWER GATES

⭐ **`check_quiz` has five parameters, none of them defaulted, none of them
variadic, and no branch leaves a verdict out.** A gate with nothing to read
answers *did not hold* and says why — never *held vacuously*, because a gate
that passes for want of evidence is R5's gates failing open.

⚠️ **And nothing in this sub-package reads an environment variable, a file or
any other thing an installation could set.** That is asserted, not promised:
`tests/studyforge/exercise/gates/test_configuration.py` sweeps every module
under `gates/` — this one included — for a reader of one.

## ⛔ THE EMPTY QUIZ IS REFUSED AT THE BOUNDARY, NOT LEFT TO `all(())`

⚠️ **`all(())` is `True`**, and the code gates paid a red gate for it: the cheapest way
to make a failing bundle read green is to delete what was failing. ⭐ A quiz
whose questions were all dropped is refused here, and each gate refuses it
again on its own, and `record_of` refuses a record naming no gate — three
heights, none of them relying on the others.
"""

from __future__ import annotations

from collections.abc import Mapping

from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.gates.digests import Cited
from studyforge.exercise.gates.quiz.family import Q1, Q2, Q3
from studyforge.exercise.gates.quiz.judged import Judgement, judged_gate, require_judgements
from studyforge.exercise.gates.quiz.mechanical import key_total_and_single, origin_still_resolves
from studyforge.exercise.gates.record import Verdict
from studyforge.exercise.quiz import QUIZ_PROVENANCE, QUIZ_TRUST, Question
from studyforge.exercise.record import Exercise


def check_quiz(
    exercise: Exercise,
    judgements: tuple[Judgement, ...],
    origins: tuple[Cited, ...],
    ledger: Mapping[str, str],
    where: str,
) -> tuple[Verdict, ...]:
    """Answer all five quiz gates — ⛔ always five, in order, never fewer."""
    questions = require_quiz(exercise, where)
    taken = require_judgements(judgements, where)
    return (
        judged_gate(Q1, questions, taken),
        judged_gate(Q2, questions, taken),
        judged_gate(Q3, questions, taken),
        key_total_and_single(questions),
        origin_still_resolves(questions, origins, ledger),
    )


def require_quiz(exercise: Exercise, where: str) -> tuple[Question, ...]:
    """Return the questions to gate, refusing anything this suite has no business reading.

    ⛔ **Three refusals and each closes a different door:** an exercise that is
    not a quiz, a quiz that asks nothing, and a quiz claiming a
    `(provenance, trust)` pair spec §7 §7 does not permit it.
    """
    if not isinstance(exercise, Exercise):
        raise ExerciseError(
            f"{where}: the quiz gates read an Exercise. A value that merely has the "
            f"right attributes is one nothing has ever validated."
        )
    if not exercise.is_quiz:
        raise ExerciseError(
            f"{where}: the quiz gates read a 'quiz' exercise and this one is "
            f"{exercise.kind!r}. An exercise that names a file is gated by running "
            f"it — that is the 'code' family's five, not these."
        )
    require_advisory(exercise, where)
    questions = exercise.questions
    if not questions:
        raise ExerciseError(
            f"{where}: this quiz asks nothing, so there is nothing to gate. A record "
            f"whose questions were all dropped would otherwise clear every gate it "
            f"carried, because every question it owed a reading for held — and it "
            f"owed none."
        )
    return questions


def require_advisory(exercise: Exercise, where: str) -> None:
    """⛔ Refuse a quiz claiming anything but `generated`/`advisory` (R5, spec §7 §7).

    ⚠️ **The pair is compared whole**, so `generated` beside a `trust` somebody
    widened is refused as readily as `authoritative` itself — a forbidden-pair
    list fails open on a pair nobody listed.
    """
    pair = (exercise.provenance, exercise.trust)
    if pair != (QUIZ_PROVENANCE, QUIZ_TRUST):
        raise ExerciseError(
            f"{where}: a quiz is {QUIZ_PROVENANCE!r} and {QUIZ_TRUST!r}, and this one "
            f"declares {exercise.provenance!r} and {exercise.trust!r}. Three of a "
            f"quiz's five gates are model judgements taken ONCE at authoring and "
            f"cannot be re-run by whoever holds the bundle, so no quiz may claim to "
            f"be the source's own grader (spec §7)."
        )
