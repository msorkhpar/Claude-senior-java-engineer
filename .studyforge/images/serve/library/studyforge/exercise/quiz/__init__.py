"""The quiz: a checkable practice for material that admits no coding task.

**What it does.** Defines what a quiz record carries in place of a workspace —
its questions, each with a stem, an ordered set of options, exactly one keyed
correct and one sentence per option — and the rule that grades a reader's
answers against that key.

**How you use it.** `exercise.of` reads a `kind: "quiz"` record and hands you
the questions. ⭐ The page grades in the browser, from the key its own page
carries (`render/assets/practice-quiz.js`); the two functions here are the
rule it implements, and the browser tests read the page against them.

    from studyforge.exercise import of
    from studyforge.exercise.quiz import completes, grade

    exercise = of(practice_document, where)   # exercise.kind == 'quiz'
    verdict = grade(exercise.questions, {"q-1": "b"})
    verdict.complete                          # every question, answered correctly

**Depends on.** `exercise.cases` for what a passage of the source is,
`exercise.errors` for the one exception, and `unit.trust` for R5's rule.
⛔ **Nothing else, ever** — see *no compiler, no container* below.

## ⭐ WHY THIS EXISTS

⭐ **Most teaching material is not code** (spec §7 §7) — a history book, a
standards walkthrough, a prose tutorial — and without this shape all of it
would stay at the reading floor, with no practice for a reader at all.

## ⛔ NO COMPILER, NO CONTAINER, NO NETWORK AND NO MODEL

⭐ **The key and the per-option sentences ship inside the practice document,
and the grading rule is the framework's.** So the reading is identical over
`file://` and over a served origin (R8): there is nothing for an origin to
change, because grading reads only its arguments.

⚠️ **The key is in the material and this package does not pretend otherwise.**
An offline page cannot hide the answer it is about to grade with, exactly as an
offline workspace cannot hide its test file, and ⛔ **claiming to hide either
is the theatre R5 exists to prevent.** The honest design shows the reader the
answer once they have answered.

## ⛔ A QUIZ PRODUCES NO RUN

⛔ **A quiz completes only when every question is answered correctly, recorded
through the reader's own state — never through a run verdict.**
`progress.is_pass`'s rule for a run and `states.completes_practice` are
untouched by this package, and a quiz record names no `test_path`, so
`completes_practice` answers `False` for a quiz in every act. ⭐ The quiz's own
rule is `completes`, and the two never answer the same question.

## ⛔ AND IT IS NEVER `authoritative`

⭐ **A quiz grader is `generated` and `advisory`, always** (spec §7 §7). Its
honesty gates Q1–Q3 are model judgements taken once at authoring; only Q4 and
Q5 are mechanical and re-runnable. ⛔ `shape` closes every path by which a quiz
could claim to be the source's own grader, including the `bundled` one that
R5's general rule leaves open for real shipped tests.

## What is in the package

| Module | Owns |
|---|---|
| `questions` | what a question is: the stem, the options, the key, the sentences |
| `shape` | what a quiz RECORD carries instead of a workspace, and its trust |
| `grading` | the rule: what was answered, what was right, when it is complete |

**Scope.** The authoring gates Q1–Q5 are `exercise.gates.quiz`'s and the
reader's panel is the renderer's.
"""

from __future__ import annotations

from studyforge.exercise.quiz.grading import (
    NO_ANSWERS,
    Answered,
    Verdict,
    completes,
    grade,
)
from studyforge.exercise.quiz.questions import (
    KEYED_OPTIONS,
    MINIMUM_OPTIONS,
    OPTION_KEYS,
    QUESTION_KEYS,
    QUIZ_ID,
    QUIZ_ID_PERMITTED,
    Option,
    Question,
    normalised,
    questions_document,
    questions_of,
)
from studyforge.exercise.quiz.shape import (
    QUESTIONS,
    QUIZ_KEYS,
    QUIZ_PROVENANCE,
    QUIZ_REQUIRED_KEYS,
    QUIZ_TRUST,
    questions_in,
    require_no_questions,
    require_quiz_shape,
)

#: ⛔ The sub-package's whole public surface. A consumer that has to import
#: `studyforge.exercise.quiz.questions` directly is a consumer this contract
#: failed — R17 makes the package's `__init__.py` its
#: contract, and this is what it says.
__all__ = [
    "KEYED_OPTIONS",
    "MINIMUM_OPTIONS",
    "NO_ANSWERS",
    "OPTION_KEYS",
    "QUESTIONS",
    "QUESTION_KEYS",
    "QUIZ_ID",
    "QUIZ_ID_PERMITTED",
    "QUIZ_KEYS",
    "QUIZ_PROVENANCE",
    "QUIZ_REQUIRED_KEYS",
    "QUIZ_TRUST",
    "Answered",
    "Option",
    "Question",
    "Verdict",
    "completes",
    "grade",
    "normalised",
    "questions_document",
    "questions_in",
    "questions_of",
    "require_no_questions",
    "require_quiz_shape",
]
