r"""One pass: read the corpus, run every check, drain them into one report.

**What it does.** Joins the walk to the checks. It is the only place that
knows the full list, so "which checks does `validate` run" has one answer.

**How you use it.** `validate(root) -> Report`. ⛔ It never raises for an
invalid corpus — an invalid corpus is a *result*, and a caller that had to
catch an exception to learn the verdict could not report ten problems at once.

**Depends on.** `validate.corpus`, `validate.structure`, `validate.paths`,
`validate.source`, `validate.exercises`, `validate.derived`, `validate.ledger`,
`validate.links`, `validate.narration`, `validate.report`, and `narrate.enabled`
for whether the last is run.

⭐ **The check list is data, so it is assertable.** `tests` asserts that every
rule id the tool can emit appears in `RULES`, which means a check added without
a rule id, or a rule id nobody can produce, is a test failure rather than a
surprise in somebody's CI log.
"""

from __future__ import annotations

from collections.abc import Iterator
from pathlib import Path

from studyforge.narrate import narration_on
from studyforge.validate import (
    derived,
    exercises,
    ledger,
    links,
    narration,
    paths,
    source,
    structure,
)
from studyforge.validate.corpus import Walk, read
from studyforge.validate.report import Finding, Report, Unchecked

#: Every check, in the order a report reads best. ⛔ One list, so the answer to
#: "what does validate check" is not spread across five modules. ⚠️ The
#: authored-exercise arm is last because it reads what the first three have
#: already judged to be a document.
CHECKS = (
    *structure.CHECKS,
    *paths.CHECKS,
    *source.CHECKS,
    *exercises.CHECKS,
    *derived.CHECKS,
    *ledger.CHECKS,
    *links.CHECKS,
    *narration.CHECKS,
)


def validate(root: Path | str, *, narration: bool | None = None) -> Report:
    """Run every check over one corpus root and return what they found.

    ⭐ `narration` is this run's answer to *is narration on?* — `None` when the
    run was not asked — and `narrate.enabled.narration_on` is what decides.
    ⛔ Off, no clip is judged: a corpus served without voices has no stale one.
    """
    walk = read(Path(root))
    return Report.of(_run(walk, narrating=narration_on(walk.root, asked=narration)))


def _run(walk: Walk, *, narrating: bool = True) -> Iterator[Finding | Unchecked]:
    yield from walk.findings
    if walk.manifest is None:
        # ⛔ Without a manifest nothing else can be judged, and saying so is
        # not the same as saying the archive is fine (R6's sibling).
        yield Unchecked("corpus", ".", "the manifest did not parse, so no other check could run")
        return
    for item in walk.refused:
        if item.container is None:
            # ⛔ A container map that would not parse takes its documents with
            # it. Saying so is not the same as saying they are fine.
            yield Unchecked(
                "document",
                item.where,
                "this container map did not parse, so no document beneath it was read",
            )
    for check in CHECKS:
        if check in narration.CHECKS and not narrating:
            continue
        yield from check(walk)
