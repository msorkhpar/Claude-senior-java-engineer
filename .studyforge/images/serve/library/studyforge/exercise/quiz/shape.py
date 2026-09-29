"""What a quiz record carries instead of a workspace, and what it may claim about itself.

**What it does.** Owns the two rules that belong to the RECORD rather than to a
question: which keys a `kind: "quiz"` record may carry, and the narrowing R5
takes for a grader nobody can re-run.

**How you use it.** `require_quiz_shape(record, where)` returns the resolved
`(provenance, trust)` pair or refuses; `questions_in(record, where)` reads the
key; `require_no_questions(record, where)` is the other direction, asked of a
record that is not a quiz.

**Depends on.** `exercise.errors`, and **`unit.trust` for R5's rule**, which is
imported and never re-spelled — the shape `record._trust` and
`cases.origin_in` already use.

## ⛔ QUESTIONS IN PLACE OF A WORKSPACE, AND THE KEY SET SAYS SO

⭐ **A quiz has no `main_path`, no `run_command`, no `test_path` and no
`test_command`** (spec §7 §7): there is no file for the reader to edit and
nothing to execute. ⛔ So `QUIZ_KEYS` **enumerates what a quiz may carry** —
stated positively, the legal set and never the illegal one — and a key outside
it is refused naming it. ⚠️ A key outside the record altogether was already
refused by `record._require_known_keys`; this is the second, narrower question.

⛔ **And the other direction is checked too**: `questions` on a record that is
not a quiz is refused, because a `code` record carrying questions is a quiz
whose key nothing grades against while the corpus validates green — the same
failure `record` refuses an unknown key for.

## ⛔ A QUIZ GRADER IS `generated` AND `advisory`, ALWAYS

⚠️ **This is stronger than R5's general rule and the difference is the
point.** R5 lets `bundled` material claim `authoritative`, because a test that
shipped with the source can be re-run by anyone holding it. ⛔ **A quiz's
honesty gates cannot be re-run**: Q1–Q3 are model judgements taken once at
authoring and shipped as a record, and only Q4 and Q5 are mechanical (spec §7
§7). ⭐ **So there is no path by which a quiz becomes `authoritative`** — not
through `bundled`, which is the path `unit.trust` leaves open and which this
module closes, and not through `user`.

⭐ **Stated positively, as the pair that is legal**, so a fourth provenance
added to `unit.trust.PROVENANCE` is refused on a quiz the day it is added,
without anybody deciding. ⚠️ That is the shape `MAY_BE_AUTHORITATIVE` was
rewritten into after a forbidden-pair list failed open, and the
reason is the same one.

## ⛔ NEITHER KEY IS REQUIRED, AND BOTH ARE WRITTEN BACK

⚠️ **`provenance` and `trust` default here rather than being required**, for
`unit.trust`'s own reason: a field an author must fill in to say the only
thing it may say is a field an author fills in wrongly. ⭐ **And `record`
writes both back**, following `trust`'s ruling that a defaulted field which
disappears when it is obvious cannot be told from one nobody wrote.

⚠️ **The cost, stated:** a quiz document that omits them round-trips to one
that carries them. ⛔ That is affordable here and not for `kind`: a quiz
record always carries them, so no document is re-rendered by this choice (R10).
"""

from __future__ import annotations

from studyforge.describe import describe_keys
from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.quiz.questions import Question, questions_of
from studyforge.unit.errors import ContentError
from studyforge.unit.trust import check_test_record

#: The key a quiz carries in place of a workspace.
QUESTIONS = "questions"

#: ⛔ Every key a quiz record may carry, in `record.EXERCISE_KEYS` order. The
#: legal set, never the illegal one. ⚠️ `cases` and `report` are
#: absent because they are facts about what a test RUN reports, and a quiz
#: produces no run — `record` already refuses them beside no grader, and that
#: refusal is the case vocabulary's own rule rather than something to relax here.
QUIZ_KEYS = ("provenance", "trust", "kind", "origin", QUESTIONS, "concepts")

#: ⛔ The one a quiz record must carry. `kind` is what selected this shape, and
#: everything else is defaulted or optional.
QUIZ_REQUIRED_KEYS = (QUESTIONS,)

#: ⛔ Where a quiz's key came from, and what its word is worth. Both are fixed
#: by spec §7 §7 and neither is a corpus's to choose.
QUIZ_PROVENANCE = "generated"
QUIZ_TRUST = "advisory"


def require_quiz_shape(record: dict, where: str) -> tuple[str, str]:
    """Refuse every way a quiz record can be wrong, and return its `(provenance, trust)`."""
    _require_only_quiz_keys(record, where)
    _require_the_questions(record, where)
    return _quiz_trust(record, where)


def questions_in(record: dict, where: str) -> tuple[Question, ...] | None:
    """Return the questions a record carries, or `None` where it carries none.

    ⛔ **Takes the record, not the value** — `cases.origin_in`'s shape, and for
    its reason: the key is read where it is handed straight to its one reader,
    so no second site in the tree reads `questions` and decides something.
    """
    return questions_of(record[QUESTIONS], where) if QUESTIONS in record else None


def require_no_questions(record: dict, where: str) -> None:
    """⛔ Refuse questions on a record that is not a quiz, naming the key."""
    if QUESTIONS in record:
        raise ExerciseError(
            f"{where}: 'exercise' names {[QUESTIONS]} on a record that is not a "
            f"quiz. Questions are what a quiz carries in place of a workspace, "
            f"and a record that names a file is graded by running it — so "
            f"questions here are a key nothing ever grades the reader against."
        )


def _require_only_quiz_keys(record: dict, where: str) -> None:
    """⛔ Refuse a workspace key on a quiz: there is no file and nothing to run."""
    unknown = [key for key in record if key not in QUIZ_KEYS]
    if unknown:
        raise ExerciseError(
            f"{where}: a quiz carries {list(QUIZ_KEYS)} and this one also carries "
            f"{describe_keys(unknown)}. A quiz asks questions in place of a "
            f"workspace: there is no file for the reader to edit, nothing to "
            f"execute, and no run whose verdict could complete it."
        )


def _require_the_questions(record: dict, where: str) -> None:
    """⛔ Refuse a quiz that asks nothing."""
    missing = [key for key in QUIZ_REQUIRED_KEYS if key not in record]
    if missing:
        raise ExerciseError(
            f"{where}: a quiz is missing {missing}. The questions are the whole "
            f"of a quiz — a record declaring the kind and asking nothing is a "
            f"practice a reader cannot begin."
        )


def _quiz_trust(record: dict, where: str) -> tuple[str, str]:
    """Apply R5's rule, then the narrowing spec §7 §7 takes for a quiz.

    ⚠️ Re-raised, not re-worded: `unit.trust`'s message is the one that states
    R5, and re-spelling it here is the duplication this package refuses. The
    type changes so a caller of `exercise` catches one family; the sentence
    does not. ⭐ `record._trust` is the precedent, character for character.
    """
    try:
        pair = check_test_record(record.get("provenance", QUIZ_PROVENANCE), record.get("trust"))
    except ContentError as error:
        raise ExerciseError(f"{where}: {error}") from None
    if pair != (QUIZ_PROVENANCE, QUIZ_TRUST):
        raise ExerciseError(
            f"{where}: a quiz is {QUIZ_PROVENANCE!r} and {QUIZ_TRUST!r}, and this "
            f"one declares {pair[0]!r} and {pair[1]!r}. A quiz's honesty gates "
            f"are taken ONCE at authoring and cannot be re-run by whoever holds "
            f"the bundle, so no quiz may claim to be the source's own grader "
            f"(spec §7)."
        )
    return pair
