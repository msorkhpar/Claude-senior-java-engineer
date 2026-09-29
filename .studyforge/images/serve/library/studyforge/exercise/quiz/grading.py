"""The grading rule: what a reader answered, what was right, and when a quiz is complete.

**What it does.** Folds a reader's answers through a quiz's questions into a
verdict — one row per question, the sentence for whatever was chosen, and the
one predicate that says the quiz is complete.

**How you use it.** `grade(questions, answers)` returns a `Verdict`;
`completes(questions, answers)` is the predicate on its own. ⭐ **The rule's
reference**: a reader's quiz is graded in the browser, from the key its page
carries (`render/assets/practice-quiz.js`, the register ruling of 2026-09-25),
and `tests/visual/test_practice_quiz.py` reads the page's verdicts against
this module's.

**Depends on.** `questions` for what a question is, and **nothing else**. ⛔ Not
the filesystem, not `subprocess`, not the network, not `serve`, not a model,
and not the clock or a random source — see below.

## ⛔ NO COMPILER, NO CONTAINER, NO NETWORK, NO MODEL

⭐ **Everything this rule reads arrives in its arguments**: the questions come
out of the practice document, which carries the key and every sentence, and
the answers come out of the reader's own state. ⛔ **So the reading is
identical over `file://` and over a served origin** (R8), because there is
nothing for an origin to change — no fetch, no path, no process, no ambient
value of any kind.

⚠️ **That is a property of this module's imports, and it is asserted rather
than promised**: `tests/studyforge/exercise/quiz/test_grading.py` reads what
this package imports and refuses any name that could reach a file, a process or
a socket, and it takes the same reading twice with the filesystem and the
network made unusable.

## ⛔ A QUIZ PRODUCES NO RUN, AND COMPLETES THROUGH NO RUN VERDICT

⛔ **`is_pass`'s rule for a run is untouched by this module and so is
`states.completes_practice`** (spec §7 §7). A quiz record names no
`test_path`, so `states.state_of` reads a quiz as `UNGRADED` and
`completes_practice` answers `False` for it in every act — which is the
correct answer to the question that function asks: *may this RUN complete the
practice*. ⭐ **The quiz's own completion is `completes` here**, recorded
through the reader's state by the page, and the two rules never meet.

⚠️ **Asserted, not argued**: `test_grading.py` puts a quiz to
`completes_practice` in every combination of act and outcome and requires
`False` from all of them.

## ⛔ EVERY QUESTION, NOT MOST OF THEM

⭐ **A quiz completes only when every question is answered correctly.** There
is no partial credit and no pass mark: a pass mark is a number somebody
chooses, and the moment it exists a corpus wants to configure it. ⚠️ The
reader is not punished for it either — a quiz has no attempt limit here, and
what is recorded is the state, not a history.

## ⛔ NOTHING HERE RAISES

⭐ **`states.completes_practice`'s rule, applied to this one**: grading is
asked on the completion path, with whatever a reader's stored state happens to
hold, and a caller that had to catch an exception to learn "no" is a caller
that will eventually not catch it and complete a practice on an error. ⛔ So an
answer naming an option the question does not offer, an answer naming a
question this quiz does not ask, a mapping that is not a mapping, and no
answers at all are each **not a correct answer** rather than a failure.

⚠️ **The refusals live at the boundary, in `questions`** — a document with no
key, two keys or a duplicate option never reaches this module, because
`exercise.of` refused it. ⭐ That is the seam: the record is validated once,
when it is read, and the completion path stays total.
"""

from __future__ import annotations

from collections.abc import Mapping
from dataclasses import dataclass

from studyforge.exercise.quiz.questions import Option, Question

#: What a reader who has answered nothing is graded against. ⚠️ Named so a
#: caller with no state yet passes a value rather than a special case.
NO_ANSWERS: Mapping[str, str] = {}


@dataclass(frozen=True, slots=True)
class Answered:
    """One question's row of the verdict: what was chosen, whether it was right, and why."""

    question: Question
    chosen: Option | None
    correct: bool

    @property
    def answered(self) -> bool:
        """Whether the reader chose an option this question offers."""
        return self.chosen is not None

    @property
    def says(self) -> str:
        """The sentence the reader is shown for what they chose, or `''` for nothing chosen.

        ⭐ **The option's own sentence, right or wrong** — every option carries
        one (`questions`), so a reader is told why whichever way they went.
        ⛔ Empty for an unanswered question, because there is nothing to say
        about a choice nobody made; the page says *unanswered*, which is a
        different statement and not this module's to word.
        """
        return "" if self.chosen is None else self.chosen.says


@dataclass(frozen=True, slots=True)
class Verdict:
    """A whole quiz's reading: one row per question, and whether the practice is complete."""

    answered: tuple[Answered, ...]

    @property
    def asked(self) -> int:
        """How many questions the quiz asks."""
        return len(self.answered)

    @property
    def right(self) -> int:
        """How many of them are answered correctly."""
        return sum(1 for row in self.answered if row.correct)

    @property
    def complete(self) -> bool:
        """⛔ Every question answered correctly, and nothing less (spec §7 §7)."""
        return self.asked > 0 and self.right == self.asked


def grade(questions: tuple[Question, ...], answers: object) -> Verdict:
    """Return the reading of `answers` against `questions`, in the order they are asked.

    ⛔ **Total**: `answers` is whatever the reader's state holds, and anything
    this rule does not recognise is not a correct answer. ⭐ The verdict has one
    row per QUESTION and never one per answer, so an answer to a question this
    quiz does not ask cannot appear in it.
    """
    chosen = answers if isinstance(answers, Mapping) else NO_ANSWERS
    return Verdict(tuple(_row(question, chosen.get(question.id)) for question in questions))


def completes(questions: tuple[Question, ...], answers: object) -> bool:
    """⛔ May these answers complete the practice? Only every question, answered correctly.

    ⚠️ **This is the quiz's completion rule and it is not `states.completes_practice`**,
    which answers for a RUN. A quiz produces no run, so the two never answer the same
    question about the same practice.
    """
    return grade(questions, answers).complete


def _row(question: Question, answer: object) -> Answered:
    """Read one question against one answer, whatever the answer turns out to be."""
    chosen = question.option(answer)
    return Answered(question=question, chosen=chosen, correct=chosen is not None and chosen.correct)
