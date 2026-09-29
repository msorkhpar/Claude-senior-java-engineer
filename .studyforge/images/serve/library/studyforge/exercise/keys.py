r"""Which keys an `exercise` record carries, in what order, and the refusals of its shape.

**What it does.** Holds the record's key order — which is its format (R10) —
the keys every record, a grader and the authored material carry, and the
refusals of a record that names a key it does not define, no file, or a grader
or a breakdown written in part.

**How you use it.** `exercise.record` reads a record through it:

    require_known_keys(value, where)   # refuses a key the record does not define
    require_present(value, where)      # refuses no file, and half a grader

**Depends on.** `cases` for the breakdown's keys, `quiz` for a quiz's,
`concepts` for what an exercise practises, `states` for the key whose presence
is the graded state, and `describe` to name keys without values (R7).

⭐ **Split from `record` at this seam** (R11): what the record's keys ARE sits
here, and what each key MEANS stays with its reader. The arguments for each
key's place are `record`'s docstring's.
"""

from __future__ import annotations

from studyforge.describe import describe_keys
from studyforge.exercise import quiz
from studyforge.exercise.cases import BREAKDOWN_KEYS
from studyforge.exercise.concepts import CONCEPTS
from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.states import GRADER_KEY

#: The record's key order, which is the order it is written in. ⛔ Serialised
#: `sort_keys=False` by the document that holds it, so this tuple is the format
#: (R10).
EXERCISE_KEYS = (
    "main_path",
    "test_path",
    "run_command",
    "test_command",
    "provenance",
    "trust",
    "kind",
    "cases",
    "report",
    "origin",
    quiz.QUESTIONS,
    CONCEPTS,
)

#: The keys every record carries: the reader's file, and how it runs. ⭐ Also
#: the whole of an ungraded record, in `EXERCISE_KEYS` order.
REQUIRED_KEYS = ("main_path", "run_command")

#: The grader half: written whole, or not at all. ⛔ `GRADER_KEY` first, because
#: its presence is the graded state (`states`).
GRADER_KEYS = (GRADER_KEY, "test_command", "provenance", "trust")

#: The keys a grader need not carry. ⚠️ Exactly one, and `unit.trust` owns the
#: reason.
DEFAULTED_KEYS = ("trust",)

#: ⭐ The keys the cases and the quiz add, in `EXERCISE_KEYS` order. ⛔ **Written
#: only where the record carries them**, which is what keeps every document
#: written before them byte-identical through a round trip (R10).
#: `BREAKDOWN_KEYS` is `cases`'s and `QUESTIONS` is `quiz`'s, with their reasons.
AUTHORED_KEYS = ("kind", *BREAKDOWN_KEYS, "origin", quiz.QUESTIONS, CONCEPTS)


def require_known_keys(value: dict, where: str) -> None:
    """Refuse a key the record does not define — because a typo is one."""
    unknown = [key for key in value if key not in EXERCISE_KEYS]
    if unknown:
        raise ExerciseError(
            f"{where}: 'exercise' carries {len(unknown)} key(s) the record does "
            f"not define, {describe_keys(unknown)}. The record is {list(EXERCISE_KEYS)}. "
            f"A key nothing reads is how a misspelled field becomes a grader "
            f"nobody is offered while the corpus validates green."
        )


def require_present(value: dict, where: str) -> None:
    """Refuse a record with no file, and a grader written in part."""
    missing = [key for key in REQUIRED_KEYS if key not in value]
    if missing:
        raise ExerciseError(
            f"{where}: 'exercise' is missing {missing}. Every record names the "
            f"reader's file and how it runs; a practice that names no file "
            f"writes no 'exercise' key at all."
        )
    if any(key in value for key in GRADER_KEYS):
        missing = [key for key in GRADER_KEYS if key not in DEFAULTED_KEYS and key not in value]
        if missing:
            raise ExerciseError(
                f"{where}: 'exercise' names part of a grader and is missing {missing}. "
                f"A grader is {list(GRADER_KEYS)}, written whole with only 'trust' "
                f"defaulted — or none of them, for a file with no test, which is "
                f"§7's ungraded state."
            )
    _require_whole_breakdown(value, where)


def _require_whole_breakdown(value: dict, where: str) -> None:
    """Refuse a breakdown beside no grader, and one written in part — naming the key."""
    named = [key for key in BREAKDOWN_KEYS if key in value]
    if not named:
        return
    if GRADER_KEY not in value:
        raise ExerciseError(
            f"{where}: 'exercise' names {named} on a record with no grader. Both "
            f"are facts about what a grader reports, and a breakdown of a run "
            f"that cannot happen is one no reader is ever shown. An ungraded "
            f"record carries 'origin' and neither of these."
        )
    missing = [key for key in BREAKDOWN_KEYS if key not in value]
    if missing:
        raise ExerciseError(
            f"{where}: 'exercise' names part of a breakdown and is missing "
            f"{missing}. A breakdown is {list(BREAKDOWN_KEYS)}, written whole: "
            f"'cases' with no 'report' names tests nothing can be read from, and "
            f"a 'report' with no 'cases' is a file nothing folds through."
        )
