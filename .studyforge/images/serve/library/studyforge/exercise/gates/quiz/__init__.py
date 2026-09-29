"""The quiz authoring gates: `Q1`–`Q5`, and the half of the gate record they write.

**What it does.** Answers spec §7's five honesty gates for a quiz — the
practice a corpus whose subject is not code ships instead of a workspace — and
returns the verdicts that go into the one gate record `studyforge.exercise.gates`
owns. ⛔ It opens no second record, and no module of that package is edited to
make room for these five.

**How you use it.**

    from studyforge.exercise.gates import GateRecord, record_document
    from studyforge.exercise.gates.quiz import check_quiz, cited_for, question_digest

    origins = cited_for(exercise.questions, ledger, where)
    record = GateRecord(
        origins=origins,
        verdicts=check_quiz(exercise, judgements, origins, ledger, where),
    )
    record.clears                        # did this quiz earn its place
    record_document(record)              # and what ships beside the bundle

**Depends on.** `studyforge.exercise.gates`' own modules — `families` for the
registry, `record` for `Verdict`, `digests` for `Cited` and the digest shape —
plus `studyforge.exercise.quiz` for what a question is and
`studyforge.exercise.record` for `Exercise`. Standard library only.
⛔ **Not on `studyforge.execute`**, for the reason the parent package states:
nothing here runs anything, and a quiz has nothing to run.

## What is in the sub-package

| Module | Owns |
|---|---|
| `family` | `Q1`–`Q5`, the registration, and how a verdict and a cited role are spelt |
| `judged` | `Q1`–`Q3`: what a shipped model judgement carries, and when it goes stale |
| `giveaway` | `Q2`'s one reading: a page-free reader's pick, turned into its judgement |
| `mechanical` | `Q4` and `Q5`: the two gates `validate` re-runs from the document |
| `checks` | the suite — all five, in order, over an exercise that cannot be authoritative |

## ⛔ THE FIVE, AND WHAT EACH ONE PROVES (spec §7 §7)

| gate | what must hold |
|---|---|
| **Q1** answerable | a pass given the page and the question picks the key, twice alike |
| **Q2** not free | a reader of the wording alone says *none* or picks a wrong option |
| **Q3** discriminating | every wrong option is refuted by a named passage of the page |
| **Q4** key total, single | one option keyed, the options distinct, each with its sentence |
| **Q5** origin | each `origin` still digests to what the source ledger has |

⭐ **What each one proves, in order:** the page really contains the answer; the
key is not given away by the question's own wording (`Q2_RULE`: an option longer
or more specific than the rest, words repeated from the stem, absolute terms,
grammar, or an option restating the stem), and ⚠️ NOT that the subject is
unknown to a reader who has studied it; no
distractor is one nobody could rule out; the reader is told why whichever
option they chose; and the question is built from the page it is attached to.

## ⛔ THREE OF THE FIVE CANNOT BE RE-RUN, AND THAT IS WHY A QUIZ IS NEVER `authoritative`

⭐ **`Q1`–`Q3` are MODEL JUDGEMENTS taken once at authoring and shipped as a
record; `Q4` and `Q5` are mechanical and `studyforge validate` re-runs them.**
⚠️ **That is the whole difference between this family and the `code` one**,
whose five are all re-runnable by anybody holding the bundle — and R5 turns on
exactly that distinction: *a proof you can re-take* against *a reading somebody
took for you*.

⛔ **So a quiz grader is `generated`/`advisory` ALWAYS**, and every path to
`authoritative` is closed:

| the height | what closes it |
|---|---|
| the practice document | `quiz.shape.require_quiz_shape` — the pair, stated positively |
| a hand-built `Exercise` | `checks.require_advisory`: the dataclass is frozen and NOT validated |
| the gate record | no module here spells either word, so no verdict can widen one |

## ⭐ WHAT MAKES A SHIPPED JUDGEMENT CHECKABLE AFTERWARDS

⚠️ **A verdict nobody can re-take is worth exactly what the digest beside it
says.** ⛔ So every judgement carries `over` — the digest of the question
exactly as it was judged — and `judged_gate` re-takes that digest from the
question in hand. ⭐ **Re-author the question and keep the record, and the gate
is refused**, which is the hole a shipped judgement would otherwise open.

⚠️ **The page's own drift is `Q5`'s and not `Q1`–`Q3`'s.** A page and a
question move for different reasons, and the record says which moved.

## ⛔ NO GATE IS CONFIGURABLE OFF

⭐ **`check_quiz` takes no options and returns all five, always.** A gate with
nothing to read answers *did not hold* and says why. ⛔ Nothing here reads an
environment variable, a configuration file or any other thing an installation
could set, and the record's key sets are closed by `gates.record`, so a bundle
inventing `"skip"` is refused rather than ignored.

## ⭐ THE SEAM, USED RATHER THAN WIDENED

⛔ **`families.py`, `record.py`, `digests.py` and `runs.py` take NO edit.** This
sub-package declares a `Family`, registers it at `family`'s own import, writes
`Verdict`s and `Cited` passages, and the one line it adds outside itself is the
import in `studyforge/exercise/gates/__init__.py` that R17 obliges a
sub-package to have there. ⚠️ **Do not make that automatic**: there is no legal
discovery mechanism in `src/` — `tests/harness/test_isolation.py`'s
run-time-import arm refuses one for the whole framework.

**Scope.** The bundle on disk and `validate`'s arm over this record are
`exercise.bundle`'s; the ledger and wiring an authoring pass to take the
`Q1`–`Q3` judgements are the exercises skill's.
"""

from __future__ import annotations

from studyforge.exercise.gates.quiz.checks import check_quiz, require_advisory, require_quiz
from studyforge.exercise.gates.quiz.family import (
    JUDGED,
    MECHANICAL,
    Q1,
    Q2,
    Q2_PROMPT,
    Q2_RULE,
    Q3,
    Q4,
    Q5,
    QUESTION_ROLE,
    QUIZ,
    cited_role,
    verdict,
)
from studyforge.exercise.gates.quiz.giveaway import PICKED_NONE, page_free
from studyforge.exercise.gates.quiz.judged import (
    JUDGED_FIELDS,
    WHOLE_QUESTION,
    Judgement,
    judged_gate,
    question_digest,
    require_judgements,
)
from studyforge.exercise.gates.quiz.mechanical import (
    cited_for,
    key_total_and_single,
    origin_still_resolves,
)

#: ⛔ The sub-package's whole public surface. A consumer that has to import
#: `studyforge.exercise.gates.quiz.judged` directly is a consumer this contract
#: failed — R17 makes the package's `__init__.py` its
#: contract, and this is what it says.
__all__ = [
    "JUDGED",
    "JUDGED_FIELDS",
    "MECHANICAL",
    "PICKED_NONE",
    "QUESTION_ROLE",
    "QUIZ",
    "Q1",
    "Q2",
    "Q2_PROMPT",
    "Q2_RULE",
    "Q3",
    "Q4",
    "Q5",
    "WHOLE_QUESTION",
    "Judgement",
    "check_quiz",
    "cited_for",
    "cited_role",
    "judged_gate",
    "key_total_and_single",
    "origin_still_resolves",
    "page_free",
    "question_digest",
    "require_advisory",
    "require_judgements",
    "require_quiz",
    "verdict",
]
