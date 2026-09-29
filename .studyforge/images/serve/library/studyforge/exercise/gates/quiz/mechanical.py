"""`Q4` and `Q5`: the two quiz gates nothing has to take anybody's word for.

**What it does.** Reads `Q4` *key total and single* off the questions
themselves and `Q5` *origin* off the record's cited passages and the source
ledger — ⭐ both of them from material a reader of the bundle holds, so
`studyforge validate` re-runs them in full rather than believing a shipped
verdict.

**How you use it.**

    from studyforge.exercise.gates.quiz.mechanical import cited_for, key_total_and_single

    origins = cited_for(questions, ledger, where)   # at authoring, from the ledger
    key_total_and_single(questions)                 # and at validate, from the document
    origin_still_resolves(questions, origins, ledger)

**Depends on.** `family` for the gate ids and the verdict spelling, `digests`
for `Cited`, `studyforge.exercise.quiz` for what a question is and what its
normal form is, and `studyforge.exercise.errors` for the one exception.
Standard library only.

## ⛔ THESE TWO ARE WHY A QUIZ IS CHECKABLE AT ALL

⚠️ **`Q1`–`Q3` ship as a record and cannot be re-taken** (spec §7 §7). ⭐ These
two can, and that is the difference R5 turns on: a verdict somebody took for
you is worth exactly what the digest beside it says, while a verdict you can
re-take is a proof. ⛔ So neither of these reads a shipped verdict, a recorded
outcome or anything else a bundle claims about itself — each one reads the
questions and the ledger and answers from scratch.

## ⛔ `Q4` RE-ASKS WHAT `questions_of` ALREADY REFUSES, AND THAT IS NOT DUPLICATION

⚠️ **`quiz.questions.Question` is frozen and NOT validated** — its own contract
says so, and `questions_of` is what guarantees a document's questions are
sound. ⭐ **Authoring builds questions before any document exists**, so the
suite that runs at authoring time is given `Question` values nothing has
checked, and `Q4` is the gate that catches a two-keyed question **before** it
is written down rather than when somebody tries to read it back.

⛔ **And the two are not the same reading.** `questions_of` refuses a whole
document on the first bad question, with one sentence; `Q4` answers for **every
question of the quiz at once**, because a gate record states what the gate
found and an author fixing four questions should be told about four.

## ⛔ `Q5` IS PER QUESTION, BECAUSE THE QUESTIONS ARE WRITTEN PER PASSAGE

⚠️ **The `code` family cites one `origin` for the whole exercise.** A quiz
cannot: each question is written from one passage, so one cited passage per
question is the finest grain the claim is made at, and anything coarser would
let one drifted page hide behind the pages that had not moved.

⭐ **The ledger is asked exactly one question** — `ledger.get(path)`, the digest
that source path has now — and this module knows nothing else about
it, so the ledger may be any `Mapping[str, str]` at the call.

## ⛔ A QUESTION IS NAMED BY ITS STEM, NEVER BY ITS ID

⭐ The `code` family's rule and its reason: a stem and an option's text are
already shipped to a reader inside the practice document, so a record echoing
them publishes nothing new — while an id is a value a record has no reason to
carry into a sentence.
"""

from __future__ import annotations

from collections.abc import Mapping

from studyforge.describe import describe
from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.gates.digests import Cited
from studyforge.exercise.gates.quiz.family import Q4, Q5, QUESTION_ROLE, cited_role, verdict
from studyforge.exercise.gates.record import Verdict
from studyforge.exercise.quiz import (
    KEYED_OPTIONS,
    MINIMUM_OPTIONS,
    Question,
    normalised,
)


def key_total_and_single(questions: tuple[Question, ...]) -> Verdict:
    """`Q4`: exactly one option keyed, the options distinct, and every one with its sentence.

    ⛔ **Answers for every question, not for the first one that is wrong.** A
    gate record is what an author fixes a quiz from, and a record naming one of
    four defects sends them back four times.
    """
    if not questions:
        return verdict(
            Q4,
            held=False,
            says="this quiz asks nothing, so there is no key to be total and single "
            "and nothing for this gate to have read",
        )
    findings = [sentence for question in questions for sentence in _q4_findings(question)]
    if findings:
        return verdict(
            Q4,
            held=False,
            says=f"{len(findings)} of this quiz's {len(questions)} questions do not key "
            f"exactly one distinct option with a sentence of its own: " + "; ".join(findings),
        )
    return verdict(
        Q4,
        held=True,
        says=f"each of the {len(questions)} questions keys exactly {KEYED_OPTIONS} of its "
        f"distinct options and every option carries the sentence its reader is shown",
    )


def origin_still_resolves(
    questions: tuple[Question, ...],
    origins: tuple[Cited, ...],
    ledger: Mapping[str, str],
) -> Verdict:
    """`Q5`: every question's passage is cited, and still digests to what the ledger has."""
    if not questions:
        return verdict(
            Q5,
            held=False,
            says="this quiz asks nothing, so no passage was cited and this gate read "
            "no material at all",
        )
    cited = {entry.role: entry for entry in origins}
    findings = [
        sentence for question in questions for sentence in _q5_findings(question, cited, ledger)
    ]
    findings += _uncited(questions, origins)
    if findings:
        return verdict(
            Q5,
            held=False,
            says=f"{len(findings)} of this quiz's {len(questions)} questions no longer "
            f"resolve to the passage of the source they were written from: " + "; ".join(findings),
        )
    return verdict(
        Q5,
        held=True,
        says=f"each of the {len(questions)} questions cites a passage the ledger still "
        f"digests to the value this exercise was built from",
    )


def cited_for(
    questions: tuple[Question, ...], ledger: Mapping[str, str], where: str
) -> tuple[Cited, ...]:
    """Return one cited passage per question, read from the ledger at authoring time.

    ⛔ **Refuses a question whose passage the ledger does not account for**,
    which is spec §7's *nothing the source has is lost* firing where it is cheapest: at authoring,
    before a record exists to carry the omission. ⚠️ `Q5` refuses the same
    thing later, because the ledger a bundle is validated against is not the
    one it was authored against.
    """
    built: list[Cited] = []
    for question in questions:
        digest = ledger.get(question.origin.path)
        if digest is None:
            raise ExerciseError(
                f"{where}: the source ledger has no entry for "
                f"'{question.origin.path}', so a question cites material the ledger "
                f"does not account for. Every passage an exercise is built from is "
                f"either in the ledger or the exercise is not built from the source."
            )
        if not isinstance(digest, str):
            raise ExerciseError(
                f"{where}: the source ledger answers with something that is not a "
                f"digest for '{question.origin.path}'. The value is {describe(digest)}."
            )
        built.append(
            Cited(
                role=cited_role(question.id),
                path=question.origin.path,
                section=question.origin.section,
                digest=digest,
            )
        )
    return tuple(built)


def _q4_findings(question: Question) -> list[str]:
    """Return what is wrong with this question's options — nothing, or one sentence per defect."""
    named = f"the question {question.stem!r}"
    findings: list[str] = []
    if len(question.options) < MINIMUM_OPTIONS:
        findings.append(
            f"{named} offers {len(question.options)} options and a reader who cannot "
            f"choose wrongly has not been asked anything"
        )
    keyed = sum(1 for option in question.options if option.correct is True)
    if keyed != KEYED_OPTIONS:
        findings.append(
            f"{named} keys {keyed} of its options correct and exactly {KEYED_OPTIONS} "
            f"is keyed, so the reader is never told which answer the page taught"
        )
    forms = [normalised(option.text) for option in question.options]
    repeated = len(forms) - len(set(forms))
    if repeated:
        findings.append(
            f"{named} offers {repeated} option that is identical to another after "
            f"normalisation, so one answer is offered twice"
        )
    silent = [option for option in question.options if not str(option.says).strip()]
    if silent:
        findings.append(
            f"{named} leaves {len(silent)} of its options without the one sentence "
            f"saying why it is right or wrong, so a reader who picks it is told 'no' "
            f"and nothing else"
        )
    return findings


def _q5_findings(
    question: Question, cited: Mapping[str, Cited], ledger: Mapping[str, str]
) -> list[str]:
    """Whether this question's passage is cited, is the one it names, and has not drifted."""
    named = f"the question {question.stem!r}"
    entry = cited.get(cited_role(question.id))
    if entry is None:
        return [
            f"{named} has no cited passage in the record, so nothing says which "
            f"version of the source it was written from"
        ]
    if entry.path != question.origin.path or entry.section != question.origin.section:
        return [
            f"{named} cites a passage the record does not carry the digest of, so the "
            f"digest beside it belongs to different material"
        ]
    current = ledger.get(question.origin.path)
    if current is None:
        return [
            f"{named} is built from '{question.origin.path}', which the source ledger "
            f"does not account for"
        ]
    if current != entry.digest:
        return [
            f"'{question.origin.path}' has changed in the source since {named} was "
            f"written from it, so the question no longer reflects what the source has"
        ]
    return []


def _uncited(questions: tuple[Question, ...], origins: tuple[Cited, ...]) -> list[str]:
    """⛔ A passage cited for a question this quiz does not ask is a finding, never a spare."""
    asked = {cited_role(question.id) for question in questions}
    mine = [entry for entry in origins if entry.role.startswith(f"{QUESTION_ROLE}:")]
    extra = len([entry for entry in mine if entry.role not in asked])
    if not extra:
        return []
    return [
        f"{extra} of the {len(mine)} cited passages name a question this quiz does not "
        f"ask, so the record was written over questions that have since moved"
    ]
