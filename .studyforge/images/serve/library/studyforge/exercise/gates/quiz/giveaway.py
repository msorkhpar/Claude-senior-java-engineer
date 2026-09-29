"""`Q2`'s one reading: what the page-free reader picked, turned into the judgement it ships as.

**What it does.** Takes what a page-free reader answered for one question
(an option, or *none*) and the cue it named, and returns the `Q2` judgement
that reading is. ⭐ It decides `held` by the rule (`family.Q2_RULE`): the reader
said *none*, or picked a wrong option. ⛔ It refuses an answer that is not one
of the question's options, and a reading that does not say why.

**How you use it.** A judge asks its reader `Q2_PROMPT` over the question's
stem and options, never the page, then writes what came back:

    from studyforge.exercise.gates.quiz import Q2_PROMPT, page_free

    page_free(question, picked=None, because="no cue decides it",
              taken_by="an independent reader, never the author")
    page_free(question, picked="c", because="the longest option, restating the stem",
              taken_by="an independent reader, never the author")   # held is False

**Depends on.** `family` for `Q2` and its prompt, `judged` for the judgement
and the question's digest, `studyforge.exercise.quiz` for what a question is,
and `studyforge.exercise.errors` for the one exception. Standard library only.

## ⭐ THE READER'S WORD "none" IS `PICKED_NONE`

⚠️ **Measured on a Java course:** a judge that passed the reader's answer
through as written handed over `"none"`, and it was refused as none of the
question's options. ⭐ So the word `Q2_PROMPT` asks the reader for is read as
`PICKED_NONE`, in any case and with surrounding space. ⛔ An option whose id
is `"none"` is still that option.

## ⛔ THE READER DOES NOT DECIDE `held`; THE RULE DOES

⚠️ **A judge that wrote `held` itself could ship the retired rule under the new
name**, or read *"none"* as a failure. ⭐ So the judge hands over what its
reader picked, and this module compares it with the key. ⭐ The outcome it
writes names the pick and the key, and `because` carries the reader's cue:
the one sentence a retry after a refused `Q2` is briefed with.
"""

from __future__ import annotations

from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.gates.quiz.family import Q2, Q2_PROMPT
from studyforge.exercise.gates.quiz.judged import WHOLE_QUESTION, Judgement, question_digest
from studyforge.exercise.quiz import Question

#: ⭐ What a reader answers when the question's wording alone decides nothing.
PICKED_NONE = None

#: ⭐ The reader's own word for `PICKED_NONE`, which `Q2_PROMPT` asks for: a
#: judge may hand it over as the reader wrote it, in any case.
READER_NONE = "none"


def page_free(question: Question, picked: str | None, because: str, taken_by: str) -> Judgement:
    """Return the `Q2` judgement for one page-free reading of `question`.

    ⭐ `picked` is an option's id, or `None` for *none*; the reader's own word
    `"none"`, in any case, is read as `None` unless an option has that id.
    ⛔ `held` is read off the rule: the reading holds unless it picked the key.
    """
    offered = [option.id for option in question.options]
    if isinstance(picked, str) and picked not in offered and picked.strip().lower() == READER_NONE:
        picked = PICKED_NONE
    if picked is not PICKED_NONE and picked not in offered:
        raise ExerciseError(
            f"a page-free reading of {question.stem!r} picked an answer that is none of "
            f"its {len(offered)} options. The reader answers with one option, or with "
            f"'none' when the wording alone does not decide."
        )
    if not isinstance(because, str) or not because.strip():
        raise ExerciseError(
            f"a page-free reading of {question.stem!r} gives no reason. It names the cue "
            f"in the wording that decided it, or says that none did."
        )
    keys = [option.id for option in question.options if option.correct]
    said = "none" if picked is PICKED_NONE else f"'{picked}'"
    return Judgement(
        gate=Q2,
        question=question.id,
        option=WHOLE_QUESTION,
        prompt=Q2_PROMPT,
        taken_by=taken_by,
        outcome=f"from the question's text alone, the reader picked {said}; the key is "
        f"{', '.join(repr(key) for key in keys) or 'no option'}",
        held=picked is PICKED_NONE or picked not in keys,
        over=question_digest(question),
        because=because,
    )
