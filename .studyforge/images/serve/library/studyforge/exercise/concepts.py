r"""What an exercise practises: the ideas a reader is told before opening it.

**What it does.** Reads the record's optional `concepts` key — the ideas the
exercise was planned to check, one sentence each — and refuses one that is not
a list of sentences.

**How you use it.**

    concepts_in(record, where)      # ('a negative index counts back', …) or None

**Depends on.** `exercise.errors`, and `describe` to name a value without
reproducing it (R7). Standard library otherwise.

## ⭐ WRITTEN WHERE THE PLAN IS KNOWN, NEVER BY THE PAGE

⚠️ **A reader chooses a practice from a list**, and a title alone does not say
what it practises. ⭐ The ideas are the plan's own sentences — each aspect the
authoring skill planned the exercise to check — carried in the record the
archive already holds, so a build reads them like every other field and no
renderer reaches back into authoring material. ⛔ **Optional and written only
where carried**, so every record written before the key round-trips to the
bytes it was read from.
"""

from __future__ import annotations

from studyforge.describe import describe
from studyforge.exercise.errors import ExerciseError

#: The record's key for what the exercise practises.
CONCEPTS = "concepts"


def concepts_in(record: dict, where: str) -> tuple[str, ...] | None:
    """Return the concepts a record carries, or `None` where it carries none.

    ⛔ **Takes the record, not the value** — `cases.origin_in`'s shape: the key is
    read where it is handed straight to its one reader.
    """
    if CONCEPTS not in record:
        return None
    value = record[CONCEPTS]
    if (
        not isinstance(value, list)
        or not value
        or not all(isinstance(one, str) and one.strip() for one in value)
    ):
        raise ExerciseError(
            f"{where}: 'concepts' is a non-empty list of sentences, one per idea the "
            f"exercise practises. The value is {describe(value)}."
        )
    return tuple(value)
