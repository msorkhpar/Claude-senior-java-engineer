"""The `quiz` family — `Q1`–`Q5` — registered at this module's import, and how a verdict is spelt.

**What it does.** Declares the five gates a quiz clears (spec §7 § *A corpus
whose subject is not code*) and registers them, so every completeness rule,
every refusal and the whole write order in `gates.record` reaches `Q1`–`Q5`
from the moment this module is imported. ⛔ It also owns the two spellings the
rest of this sub-package shares: how a verdict names its family, and how a
cited passage names the question it belongs to.

**How you use it.**

    from studyforge.exercise.gates.quiz.family import Q4, QUIZ, cited_role, verdict

    verdict(Q4, held=True, says="every question keys exactly one option")
    cited_role("q-1")            # 'question:q-1', the role Q5 looks a passage up by

**Depends on.** `gates.families` for the registry, `gates.record` for
`Verdict`, and `studyforge.exercise.errors` for the one exception. Standard
library only.

## ⛔ THREE OF THESE ARE NOT RE-RUNNABLE, AND THE SPLIT IS DECLARED HERE

⭐ **`Q1`–`Q3` are model judgements taken ONCE at authoring and shipped as a
record; `Q4` and `Q5` are mechanical and `studyforge validate` re-runs them**
(spec §7 §7). ⛔ So `JUDGED` and `MECHANICAL` are named rather than left to be
inferred from which module a gate lives in: a reader of a gate record has to be
able to tell which verdicts they can re-take and which ones somebody took for
them, and **that distinction is exactly what R5 turns on**.

⚠️ **The consequence, stated where it is declared:** a quiz grader is
`generated` and `advisory` always, because three of its five gates cannot be
re-run by whoever holds the bundle. ⛔ `checks.check_quiz` refuses an exercise
claiming anything else, and `quiz.shape.require_quiz_shape` refuses the
document — two heights, neither relying on the other.

## ⛔ THE FAMILY IS REGISTERED HERE AND THE ONE LINE OUTSIDE THIS SUB-PACKAGE IS AN IMPORT

⭐ **The gate record's seam, used exactly as it was left:** `families.py`, `record.py`,
`digests.py` and `runs.py` take no edit at all. The only line this family adds
anywhere outside `gates/quiz/` is the import in
`studyforge/exercise/gates/__init__.py`, which R17 obliges a sub-package to
have there anyway — a parent contract is where a reader finds out a
sub-package exists. ⛔ **Do not try to make that automatic:** there is no legal
discovery mechanism in `src/`, because `tests/harness/test_isolation.py`'s
run-time-import arm refuses one for the whole framework.

## ⭐ `Q2`'S RULE IS DECLARED HERE, WITH ITS VERSION

⚠️ **`Q2` guards against a giveaway, not against knowledge.** Its reader is
told to answer from the question's wording alone (`Q2_PROMPT`), and `Q2` holds
when that reader says *none* or picks a wrong option. ⭐ `Q2_RULE` names the
version, and `judged` writes it into every `Q2` verdict's record, so a record
says which rule its reading was taken under.

## ⛔ A CITED PASSAGE IS NAMED PER QUESTION, AND THAT IS WHY `Q5` CAN BE PER QUESTION

⚠️ **A quiz's questions are written one passage at a time**, so a single
`origin` role for the whole exercise — which is what the `code` family
writes — would let one drifted page hide behind four that had not moved.
⭐ `cited_role` spells `question:<id>`, and `digests.require_role` permits the
`:` and the token after it for exactly this.
"""

from __future__ import annotations

from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.gates.families import Family, register
from studyforge.exercise.gates.record import Verdict

#: The five gates a quiz clears, in the order spec §7 §7 states them.
Q1, Q2, Q3, Q4, Q5 = "Q1", "Q2", "Q3", "Q4", "Q5"

#: ⛔ The gates that are MODEL JUDGEMENTS, taken once at authoring and shipped
#: as a record. ⚠️ `validate` cannot re-take these; what it re-checks is that
#: the judgement is still about the question it was taken over.
JUDGED = (Q1, Q2, Q3)

#: ⭐ The gates that are MECHANICAL, and that `studyforge validate` re-runs in
#: full over the shipped document and the ledger.
MECHANICAL = (Q4, Q5)

#: ⭐ Registered at import, so a record naming this family is complete only
#: when it carries all five. ⛔ `families` holds the rules; this line is the
#: whole of what makes `Q1`–`Q5` exist.
QUIZ = register(Family("quiz", (*JUDGED, *MECHANICAL)))

#: ⭐ **The version of the rule `Q2` is judged by**, written into every `Q2`
#: verdict's record under `rule`. ⚠️ Version 2 guards against a GIVEAWAY — a key
#: the question's own wording betrays — and no longer against a reader who
#: knows the subject: on a well-known subject every correct question about an
#: important idea is known, so version 1 refused them all.
Q2_RULE = "Q2 v2: a giveaway, read from the question's own text alone"

#: ⛔ **What the page-free reader is asked, word for word.** A `Q2` judgement
#: taken under any other prompt is refused, so no judge can ask the retired
#: question (*"from general knowledge"*) and ship it under this rule's name.
Q2_PROMPT = (
    "Answer ONLY from this question's own text: its stem and its options. Use no "
    "knowledge of the subject at all; treat every term as a word you have never "
    "met. Pick an option only when the wording itself singles it out: it is longer "
    "or more specific than the others, it repeats words from the stem, the others "
    "carry absolute terms such as 'always' or 'never', the stem's grammar fits it "
    "alone, or it restates the stem. A cue decides only when it clearly singles out "
    "one option over every other; a weak cue, or one another option shares, decides "
    "nothing. Otherwise answer 'none'. Say which cue decided it, or that none did."
)

#: How a cited passage names the question it was written from: `question:<id>`.
#: ⭐ Named here rather than in `mechanical` because the authoring helper and
#: the gate both spell it, and one spelling is what makes them meet.
QUESTION_ROLE = "question"


def cited_role(question: str) -> str:
    """Return the `origins` role that carries this question's passage.

    ⚠️ A quiz id is a narrow token (`quiz.questions.QUIZ_ID`), so the result is
    inside `digests.ROLE_PERMITTED` for any id a question could carry — and a
    value that is not one is refused by `require_role` where it is written,
    rather than here where it would be refused twice.
    """
    return f"{QUESTION_ROLE}:{question}"


def verdict(
    gate: str,
    *,
    held: bool,
    says: str,
    recorded: tuple[tuple[str, str], ...] = (),
) -> Verdict:
    """Return one of this family's verdicts, refusing a gate this family does not declare.

    ⛔ **The gate id is checked rather than trusted.** A verdict naming a gate
    `quiz` does not declare would be refused by `record_of` later, with a
    sentence about the registry rather than about the caller — and a mistake is
    cheapest where it is made.
    """
    if gate not in QUIZ.gates:
        raise ExerciseError(
            f"the quiz family declares {list(QUIZ.gates)} and a verdict was written "
            f"for another gate. A gate id belongs to exactly one family, so writing "
            f"somebody else's is answering for a gate this family never read."
        )
    if not isinstance(held, bool):
        raise ExerciseError(
            "a verdict's 'held' is true or false — did the gate hold. A merely truthy "
            "value would make a gate's answer depend on how a reader of the record "
            "coerces it."
        )
    return Verdict(id=gate, family=QUIZ.name, held=held, says=says, recorded=recorded)
