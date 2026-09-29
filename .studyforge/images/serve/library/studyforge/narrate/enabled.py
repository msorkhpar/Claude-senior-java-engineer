r"""Whether narration is ON for one corpus and one run — ⛔ the one predicate that says so.

**What it does.** Answers one question every stage that reads narration asks
before it reads it: *is narration part of this corpus, for this run?*

**How you use it.**

    from studyforge.narrate import narration_on

    if narration_on(root, asked=arguments.narration):   # None: nobody asked this run
        ...judge, build or serve the clips...

**Depends on.** `corpus.manifest` for the corpus's recorded answer, and nothing
else. ⛔ It opens no record and no clip: whether a corpus HAS narration is the
record's question (`synth.read_state`, whose `present=False` is the reading
floor, C5); whether narration is WANTED is this one's.

## ⛔ ONE PREDICATE, SO "NARRATION OFF" HAS ONE MEANING EVERYWHERE

⭐ **Narration is optional, per corpus and per run**, and the skills ask about
it at capture and at serve. Four stages
read clips: `validate` judges whether they are current, `build` links them,
`serve` answers them and the build-and-serve skill reports them. ⚠️ Private
answers to *is it off?* would drift the way readers of one record always have,
and the symptom would be a `validate` that reports stale clips for a corpus whose
site carries no player. ⭐ So each stage asks here, and here reads the
corpus's answer.

## ⭐ THE ORDER: THIS RUN'S ANSWER, THEN THE CORPUS'S, THEN ON

1. `asked` — the run's own `--narration` / `--no-narration` — wins whenever it
   was given, either way.
2. ⭐ **The corpus's recorded choice**: `corpus.json`'s `narration`
   (`corpus_api: 5`), which the onboarding skill asks the author for.
3. Otherwise ON — ⭐ the default: a manifest
   that says nothing, or that cannot be read, is voiced. ⛔ An unreadable manifest
   is not this predicate's to report: `validate` and `build` refuse it in its own
   words, and a second reason here would name the same defect twice.
"""

from __future__ import annotations

from pathlib import Path

from studyforge.archive.scrub import PersonalDataLeak
from studyforge.corpus.manifest import MANIFEST_FILENAME, RAISES, load


def narration_on(root: Path | str, *, asked: bool | None = None) -> bool:
    """Return whether narration is on for the corpus at `root` in this run.

    ⛔ `asked` is `None` when the run was given no answer, never `False`: a run
    that said nothing has not turned narration off.
    """
    if asked is not None:
        return bool(asked)
    return declared(root)


def declared(root: Path | str) -> bool:
    """Return the corpus's recorded answer: `corpus.json`'s `narration`, or on."""
    try:
        return load(Path(root) / MANIFEST_FILENAME).narration
    except PersonalDataLeak:
        raise  # ⛔ R7's refusal is never swallowed.
    except RAISES:
        return True
