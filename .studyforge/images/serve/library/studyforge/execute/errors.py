"""`RunRefused` — the one exception the command runner raises.

**What it does.** Names a request to run something that the runner will not
start: commands that are not a list of argv lists, an argument outside the
permitted set, a working directory that is not a relative path inside the
source root, or a container name that is not one word.

**How you use it.** `except RunRefused` around `Runner.start`. It is a
`ValueError`, so a caller that only knows "bad input" still catches it.

**Depends on.** Nothing.

⛔ **The message never reproduces the refused value** (R7), for the reason
`studyforge.exercise.ExerciseError` gives: the one shape a refusal exists to
stop is the shape that carries a home directory, so quoting it would copy
personal data into a log from the check that keeps it out of one.

⭐ **A command that RAN and failed is not an exception.** It is a line of
output and an exit line, because a failing test is the most ordinary thing a
learner's run does — raising for it would make the common case the exceptional
one.
"""

from __future__ import annotations


class RunRefused(ValueError):
    """A run the runner will not start; the message names what is permitted."""
