r"""A practice section's layout, as a served unit document carries it.

**What it does.** Names the one heading of a practice's layout that heads
nothing: the lesson heading of an exercise authored with no lesson, which has
only its worked solution under it.

**How you use it.**

    bare_lesson(section["blocks"])     # 4, or None

**Depends on.** `archive.blocks` for the lesson heading's word. Standard
library otherwise.

## ⭐ ONE RULE, READ BY THE PAGE AND BY THE NARRATION

⚠️ **The page withholds the heading and the narration must not speak it**, or
a clip is made that nothing on the page plays. ⭐ So the rule is here, in the
package both read a served unit document through, and each asks it rather
than keeping a copy.
"""

from __future__ import annotations

from studyforge.archive.blocks import LESSON_HEADING


def bare_lesson(blocks: list) -> int | None:
    """Return the position of a practice's lesson heading that heads no lesson, or `None`.

    ⭐ The heading is the archive's (`LESSON_HEADING`, or it followed by `: `
    and a title) at level 2, and what it heads runs to the next level-2
    heading. ⛔ It is bare when that run holds nothing but disclosures — the
    worked solution is the exercise's, not a lesson — and absent otherwise.
    """
    for position, block in enumerate(blocks):
        if not _is_h2(block) or not _says_lesson(block.get("text")):
            continue
        run = []
        for held in blocks[position + 1 :]:
            if _is_h2(held):
                break
            run.append(held)
        bare = all(isinstance(one, dict) and one.get("type") == "disclosure" for one in run)
        return position if bare else None
    return None


def _is_h2(block: object) -> bool:
    """Answer whether a block is a level-2 heading."""
    return isinstance(block, dict) and block.get("type") == "heading" and block.get("level") == 2


def _says_lesson(text: object) -> bool:
    """Answer whether a heading's text is the practice layout's lesson heading."""
    return isinstance(text, str) and (
        text == LESSON_HEADING or text.startswith(f"{LESSON_HEADING}: ")
    )
