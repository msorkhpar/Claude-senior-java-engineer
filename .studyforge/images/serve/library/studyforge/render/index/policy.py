r"""Which disclosures start open, decided from the corpus's own shape and nothing else.

**What it does.** Answers one question — *how many levels of this index open
before the reader touches anything?* — as a pure function of how many rows each
level holds.

**How you use it.**

    from studyforge.render.index import open_to, row_counts

    row_counts(document)   # (10, 45, 166) — rows at each level, shallowest first
    open_to(document)      # 2 — levels 1 and 2 render `open`, level 3 does not

**Depends on.** `entries`. ⛔ No markup, no template, no filesystem: this module
decides, `disclosure` renders.

## ⛔ Open what was already visible; make what would multiply the page opt-in

⚠️ **A one-level corpus and a four-level one want opposite defaults, and neither
of them wants a flag.** ⭐ So the default is computed: a level starts **open**
exactly when opening it — and every level above it — leaves the number of rows
the page shows at once inside `VISIBLE_ROW_BUDGET`.

⛔ **The top level is visible before anything can expand**, because its summaries
are the rows of the outermost list and there is no disclosure above them to
close. ⚠️ That is why this function opens *levels* rather than deciding whether
to render one: a corpus with more top-level containers than the budget still
renders all of them, and the budget only stops the next level unfolding on top.

⭐ **The consequence, stated rather than discovered:** a small corpus renders
entirely open and reads as a plain nested list, and a large one renders as the
shape of the material with the long tail one click away. ⛔ Neither is a branch
on which corpus it is (R1) — both fall out of the same arithmetic.

## ⛔ Why a constant here is not a corpus reading (R1)

⚠️ **A bare integer in an *Acceptance* is a corpus reading wearing a number.**
⭐ This one is not a reading of any corpus: it is a claim about a **reader** —
roughly two screenfuls of the reading column — and it is the same number for
every corpus this framework will ever build. ⛔ It is published so a test can
sit exactly on it, and `row_counts` is published beside it so the population is
readable before it is reduced to a verdict.
"""

from __future__ import annotations

from studyforge.render.index.entries import Document, Section

#: How many rows the index may show at once before the next level down becomes
#: opt-in. ⚠️ A claim about a reader, not about any corpus — see the docstring.
VISIBLE_ROW_BUDGET = 60


def row_counts(document: Document) -> tuple[int, ...]:
    """Return how many rows sit at each level, shallowest first.

    ⭐ Level 1 is the number of top-level sections; level *n+1* is the total
    number of children of everything at level *n*. ⛔ The tuple is as long as
    the levels that actually hold something, so a corpus whose deepest
    containers declare no units yet reports the levels that exist rather than a
    padded shape.
    """
    tally: dict[int, int] = {}
    _tally(document.sections, 1, tally)
    return tuple(tally[level] for level in sorted(tally))


def _tally(sections: tuple[Section, ...], level: int, tally: dict[int, int]) -> None:
    """Add one level's rows to `tally`, then the rows each of them reveals.

    ⛔ Counted by **level**, never by kind: siblings that disagree about whether
    they hold sections or units are refused one record along, and a tally that
    walked only one of the two branches would report a level that is short with
    nothing raised — which is the shape this whole page exists to avoid.
    """
    if not sections:
        return
    tally[level] = tally.get(level, 0) + len(sections)
    for section in sections:
        if section.items:
            tally[level + 1] = tally.get(level + 1, 0) + len(section.items)
        _tally(section.sections, level + 1, tally)


def open_to(document: Document) -> int:
    """Return the deepest level whose disclosures render already open.

    `0` means every disclosure starts closed and the reader sees only the top
    level's summaries. ⛔ Monotone by construction: the visible count never
    falls as a level opens, so the deepest affordable level is also the last one
    that fits, and there is no choice to make between them.
    """
    counts = row_counts(document)
    visible = counts[0] if counts else 0
    opened = 0
    for level, rows in enumerate(counts[1:], start=1):
        if visible + rows > VISIBLE_ROW_BUDGET:
            break
        visible += rows
        opened = level
    return opened


def visible_rows(document: Document, opened: int) -> int:
    """How many rows the page shows when levels 1 through `opened` are open.

    ⭐ Published because it is the arithmetic `open_to` is a search over, and a
    test that could only see the verdict could not tell a policy that counted
    correctly from one that returned the same answer for another reason.
    """
    counts = row_counts(document)
    return sum(counts[: opened + 1])
