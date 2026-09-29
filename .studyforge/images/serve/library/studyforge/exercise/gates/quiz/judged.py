"""`Q1`–`Q3`: the three model judgements, what a shipped one carries, and when it goes stale.

**What it does.** Reads the judgements an authoring pass took over a quiz —
`Q1` *answerable*, `Q2` *not free*, `Q3` *discriminating* — and answers each
gate from them. ⛔ It takes no judgement itself and never could: these are
readings a model produced at authoring, and this module is what makes a shipped
one checkable afterwards.

**How you use it.**

    from studyforge.exercise.gates.quiz.judged import Judgement, judged_gate, question_digest

    taken = Judgement(
        gate=Q1, question="q-1", option="",
        prompt="given this page and this question alone, which option is keyed?",
        taken_by="the authoring pass, twice, independently",
        outcome="picked 'a' on both readings",
        held=True, over=question_digest(question),
    )
    judged_gate(Q1, questions, require_judgements((taken,), where))

**Depends on.** `family` for the gate ids and the verdict spelling, `digests`
for the digest shape, `studyforge.exercise.quiz` for what a question is,
`studyforge.describe` for naming a value without reproducing it (R7),
`studyforge.exercise.errors` for the one exception, and `json` for the one
canonical spelling a digest is taken over. Standard library only.

## ⛔ A MODEL JUDGEMENT CANNOT BE RE-RUN. ITS INPUTS CAN BE RE-DIGESTED.

⚠️ **That sentence is the whole design of this module.** `studyforge validate`
runs on a reader's machine with no model, no network and no page-by-page
authoring context, so it cannot ask again whether a question is answerable from
its page. ⛔ **What it can do is refuse a judgement that is no longer about the
question it was taken over** — and that closes the hole a shipped judgement
would otherwise open: re-author the question, keep the record, and the gate
still reads as held.

⭐ **So every judgement carries `over`, the digest of the question exactly as
it was judged**, and `judged_gate` re-takes that digest from the question in
hand. ⚠️ **The page's drift is NOT this module's** — that is `Q5`, against the
ledger — and the two are separate because a page and a question move for
different reasons.

## ⛔ `Q3` IS PER WRONG OPTION, FOR THE REASON `G3` IS PER EDGE CASE

⚠️ **A gate coarser than the claim it backs is theatre**, and this project paid
for learning that once (R5, and the `code` family's own note). *Every wrong
option is refuted by a named passage of the page* is a claim about **each
distractor**, so a question with three wrong options owes three judgements and
is answered for by each of them. ⛔ One judgement saying *the distractors are
fine* would let the one nobody could rule out ride along with two that were
obviously wrong — which is the trick question this gate exists to catch.

⭐ `Q1` and `Q2` are per QUESTION and say so by carrying an empty `option`.

## ⛔ A `Q2` READING IS TAKEN UNDER `Q2_PROMPT`, AND SAYS WHY

⚠️ **`Q2` guards against a giveaway** (`family.Q2_RULE`): its reader answers
from the question's wording alone. ⛔ So a `Q2` judgement whose prompt is not
`Q2_PROMPT` is refused, and so is one with no `because`, the cue the reader
named or its word that none decided. ⭐ A refused `Q2` quotes that reason in
its verdict, which is what a retry's brief carries: the author is told which
cue gave the key away, never handed the same brief again. ⭐ Every `Q2` verdict
records `rule` first, so the record names the rule's version.

## ⛔ WHAT A JUDGEMENT RECORDS, AND WHY NOTHING HERE READS IT TO DECIDE

⭐ **The prompt, the pass and the outcome** (what a judged gate must keep),
written into `Verdict.recorded` so a reader of the record can see what was
asked, who answered and what they said. ⛔ **`held` is the verdict**, exactly as
`record.py` says: nothing in this module parses an outcome sentence to decide
whether a gate held, because a gate decided by reading prose is a gate whose
rule nobody can state.

⚠️ **A judgement with nothing recorded is refused.** A shipped judgement whose
prompt was blank is one nobody can audit, and an unauditable judgement is the
thing R5's gates are written against.
"""

from __future__ import annotations

import json
from dataclasses import dataclass

from studyforge.describe import describe
from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.gates.digests import digest_of_bytes, require_digest, require_role
from studyforge.exercise.gates.quiz.family import JUDGED, Q1, Q2, Q2_PROMPT, Q2_RULE, Q3, verdict
from studyforge.exercise.gates.record import Verdict
from studyforge.exercise.quiz import Option, Question, questions_document

#: The three things a shipped judgement records, in the order they are written
#: into `Verdict.recorded` (R10). ⭐ `over` is the fourth and is what makes the
#: other three checkable: it says which question they were about.
JUDGED_FIELDS = ("prompt", "pass", "outcome", "over")

#: ⛔ What `option` is for a judgement about a whole question. `Q1` and `Q2`
#: are per question; only `Q3` names an option.
WHOLE_QUESTION = ""


@dataclass(frozen=True, slots=True)
class Judgement:
    """One reading an authoring pass took, and the question it was taken over.

    ⛔ Frozen, not validated: `require_judgements` is what guarantees the gate
    is one this family judges, the digest is one this build can read, and
    nothing a record carries is blank.
    """

    gate: str
    question: str
    option: str
    prompt: str
    taken_by: str
    outcome: str
    held: bool
    over: str
    #: ⭐ Why the reader answered as it did. Required on `Q2`: the cue that gave
    #: the key away, or that none did. A retry's brief quotes it.
    because: str = ""


def question_digest(question: Question) -> str:
    """Return the digest of this question exactly as it would be written (R10).

    ⭐ **One canonical spelling, taken over the document the question round
    trips to** — the stem, every option's text, its key and its sentence, and
    the passage it came from. ⛔ So a re-authoring pass that changes any of
    those changes this digest, and every judgement taken over the old one is
    refused rather than inherited.

    ⚠️ **Sorted keys and no spaces**, so two takings of one question are the
    same bytes on any build (R10) — never the key order a dictionary happens to
    have been built in.
    """
    written = questions_document((question,))[0]
    return digest_of_bytes(
        json.dumps(written, sort_keys=True, ensure_ascii=True, separators=(",", ":")).encode(
            "ascii"
        )
    )


def require_judgements(judgements: object, where: str) -> tuple[Judgement, ...]:
    """Refuse every way a shipped judgement can be one nobody could audit."""
    if isinstance(judgements, (str, bytes)) or not isinstance(judgements, (list, tuple)):
        raise ExerciseError(
            f"{where}: the quiz judgements are a sequence of Judgement values, one per "
            f"question for Q1 and Q2 and one per wrong option for Q3. The value is "
            f"{describe(judgements)}."
        )
    taken = tuple(_judgement(entry, where) for entry in judgements)
    seen = [(entry.gate, entry.question, entry.option) for entry in taken]
    repeated = len(seen) - len(set(seen))
    if repeated:
        raise ExerciseError(
            f"{where}: {repeated} judgement is recorded twice for one gate and one "
            f"question. A gate reads one judgement per thing it judges, so a second "
            f"one is a reading nobody can say was used. The ids are not reproduced "
            f"here, since a refusal never quotes a value that may be personal."
        )
    return taken


def judged_gate(
    gate: str, questions: tuple[Question, ...], taken: tuple[Judgement, ...]
) -> Verdict:
    """Answer one of `Q1`–`Q3` from the judgements shipped for it.

    ⛔ **A gate with nothing to read does not hold**, and that arm is not
    pedantry: a quiz whose questions were all dropped would otherwise clear
    every judged gate, because every judgement it owed held — and it owed none.
    """
    if gate not in JUDGED:
        raise ExerciseError(
            f"{list(JUDGED)} are the quiz gates taken as model judgements, and this is "
            f"not one of them. Q4 and Q5 are mechanical and are read from the document "
            f"and the ledger instead."
        )
    if not questions:
        return verdict(
            gate,
            held=False,
            says="this quiz asks nothing, so there was no judgement to read and "
            "nothing for this gate to hold over",
        )
    owed = _owed(gate, questions)
    found = {(entry.question, entry.option): entry for entry in taken if entry.gate == gate}
    findings = [sentence for pair in owed for sentence in _findings(gate, pair, found)]
    findings += _unowed(gate, owed, found)
    recorded = (() if gate != Q2 else (("rule", Q2_RULE),)) + tuple(
        written
        for key in (_key(pair) for pair in owed)
        if key in found
        for written in _recorded(found[key])
    )
    if findings:
        return verdict(
            gate,
            held=False,
            says=f"{len(findings)} of this quiz's {len(owed)} {_SUBJECT[gate]} readings "
            f"are missing, negative or no longer about what they were taken over: "
            + "; ".join(findings),
            recorded=recorded,
        )
    return verdict(
        gate,
        held=True,
        says=f"all {len(owed)} of this quiz's {_SUBJECT[gate]} readings held and each "
        f"was taken over the question as it stands",
        recorded=recorded,
    )


def _owed(gate: str, questions: tuple[Question, ...]) -> tuple[tuple[Question, Option | None], ...]:
    """Return what this gate owes: one judgement per wrong option for `Q3`, else per question."""
    if gate == Q3:
        return tuple(
            (question, option)
            for question in questions
            for option in question.options
            if not option.correct
        )
    return tuple((question, None) for question in questions)


def _key(pair: tuple[Question, Option | None]) -> tuple[str, str]:
    """Return the `(question, option)` key a judgement for this pair is filed under."""
    question, option = pair
    return (question.id, WHOLE_QUESTION if option is None else option.id)


def _findings(
    gate: str,
    pair: tuple[Question, Option | None],
    found: dict[tuple[str, str], Judgement],
) -> list[str]:
    """Return what is wrong with this pair's judgement — nothing, or exactly one sentence."""
    question, option = pair
    named = _names(question, option)
    entry = found.get(_key(pair))
    if entry is None:
        return [f"{named} carries no {gate} judgement at all"]
    if not entry.held:
        why = f", because {entry.because}" if entry.because.strip() else ""
        return [f"{named} was judged and {gate} did not hold: {entry.outcome}{why}"]
    if entry.over != question_digest(question):
        return [
            f"{named} has changed since its {gate} judgement was taken, so that "
            f"reading is about a question this quiz no longer asks"
        ]
    return []


def _unowed(
    gate: str,
    owed: tuple[tuple[Question, Option | None], ...],
    found: dict[tuple[str, str], Judgement],
) -> list[str]:
    """⛔ A judgement for something this quiz no longer has is a finding, never a spare."""
    expected = {_key(pair) for pair in owed}
    extra = len([key for key in found if key not in expected])
    if not extra:
        return []
    return [
        f"{extra} {gate} judgement is recorded for a question or an option this quiz "
        f"does not offer, so the record was taken over material that has since moved "
        f"(the ids are not reproduced here, since a refusal never quotes a value that may be "
        f"personal)"
    ]


def _names(question: Question, option: Option | None) -> str:
    """Name what a finding is about by the words a reader is shown — ⛔ never by an id.

    ⭐ The `code` family's rule, for its reason: a stem and an option's text are
    already shipped to the reader inside the practice document, so a record
    echoing them exposes nothing the corpus had not already published.
    """
    if option is None:
        return f"the question {question.stem!r}"
    return f"the option {option.text!r} of {question.stem!r}"


def _recorded(entry: Judgement) -> tuple[tuple[str, str], ...]:
    """Write one judgement's prompt, pass, outcome and digest into the verdict's evidence."""
    tail = entry.question if entry.option == WHOLE_QUESTION else f"{entry.question}:{entry.option}"
    written = tuple(
        (f"{field}:{tail}", value)
        for field, value in zip(
            JUDGED_FIELDS, (entry.prompt, entry.taken_by, entry.outcome, entry.over), strict=True
        )
    )
    return written + (((f"because:{tail}", entry.because),) if entry.because.strip() else ())


def _judgement(value: object, where: str) -> Judgement:
    """Read one judgement, refusing a gate, a digest or a blank a record could not carry."""
    if not isinstance(value, Judgement):
        raise ExerciseError(
            f"{where}: a quiz judgement is a Judgement value. The value is {describe(value)}."
        )
    if value.gate not in JUDGED:
        raise ExerciseError(
            f"{where}: a judgement names a gate that is not one of {list(JUDGED)}. Q4 "
            f"and Q5 are mechanical and are re-run rather than judged, so a judgement "
            f"for one of them is a reading nothing would ever read."
        )
    if not isinstance(value.held, bool):
        raise ExerciseError(
            f"{where}: a judgement's 'held' is true or false — did the pass answer the "
            f"way the gate needs. The value is {describe(value.held)}."
        )
    require_role(value.question, f"{where}: a judgement's question")
    if value.option != WHOLE_QUESTION:
        require_role(value.option, f"{where}: a judgement's option")
    elif value.gate == Q3:
        raise ExerciseError(
            f"{where}: a Q3 judgement names no option. Every wrong option is refuted "
            f"on its own, because a judgement covering all of them at once would let "
            f"the distractor nobody can rule out ride along with the obvious ones."
        )
    require_digest(value.over, f"{where}: a judgement's 'over'")
    _require_recorded(value, where)
    if not isinstance(value.because, str):
        raise ExerciseError(
            f"{where}: a judgement's 'because' is text: why its reader answered as it "
            f"did. The value is {describe(value.because)}."
        )
    if value.gate == Q2:
        _require_page_free(value, where)
    return value


def _require_page_free(value: Judgement, where: str) -> None:
    """⛔ Refuse a `Q2` reading taken under another prompt, or one that does not say why."""
    if value.prompt != Q2_PROMPT:
        raise ExerciseError(
            f"{where}: a Q2 judgement was taken under a prompt that is not Q2_PROMPT. "
            f"Q2 ({Q2_RULE}) asks its reader to answer from the question's wording "
            f"alone, so a reading taken any other way, such as from general knowledge, "
            f"is a reading of another rule."
        )
    if not isinstance(value.because, str) or not value.because.strip():
        raise ExerciseError(
            f"{where}: a Q2 judgement's 'because' is required: the cue in the "
            f"question's wording that decided the reading, or that none did. A retry "
            f"after a refused Q2 is briefed with it."
        )


def _require_recorded(value: Judgement, where: str) -> None:
    """⛔ Refuse a judgement nobody could audit: the prompt, the pass and the outcome."""
    for field, text in zip(
        JUDGED_FIELDS[:3], (value.prompt, value.taken_by, value.outcome), strict=True
    ):
        if not isinstance(text, str) or not text.strip():
            raise ExerciseError(
                f"{where}: a judgement's {field!r} is required and must be text. A "
                f"model judgement ships instead of being re-run, so a reader of the "
                f"record has nothing but what it was asked, who answered and what "
                f"they said. The value is {describe(text)}."
            )


#: What each judged gate reads, said in the one word its verdict uses.
_SUBJECT = {Q1: "answerable", Q2: "not-free", Q3: "discriminating"}
