"""One grammar for a fenced code block: the line that opens one, and the line that closes it.

**What it does.** Answers the two questions every reader of fences asks:
`opening(line, in_list=…)` — does this line open a fence, and with what — and
`closes(line, opened)` — does this line close the fence `opened` began.

**How you use it.** `leaf.read_code` closes the archive reader's fences with
`closes`, and the exercise ledger (`skills.exercises.scan`) opens and closes
its fences with both, so the two cannot disagree about where an example is.

**Depends on.** `patterns` and `scan` in this package. Standard library only.

## ⛔ ONE GRAMMAR, BECAUSE TWO READ ONE FILE

⚠️ **Measured on a real course:** the ledger read fences with its own pattern
(at most three spaces of indent, either marker character) while the archive
reader opened a fence indented inside a list item and closed it by CommonMark's
rule. The ledger missed five examples indented under list items, and refused a
page whose fence opened at four spaces and closed at three, which the archive
reader accepted. ⭐ So both read this module: a fence is backticks — the only
fence the archive reader keeps — at up to three spaces, or at any indent
inside a list item; and it closes on a line of backticks at least as long, no
more than three spaces deeper than the fence that opened it.
"""

from __future__ import annotations

from dataclasses import dataclass

from studyforge.archive.markdown import patterns, scan


@dataclass(frozen=True, slots=True)
class Opened:
    """A fence as its opening line has it: how far in, how many backticks, and its info."""

    indent: int
    ticks: int
    info: str


def opening(line: str, *, in_list: bool = False) -> Opened | None:
    """Return what `line` opens, or `None` when it opens no fence here.

    ⭐ Up to three spaces anywhere; any indent only where the line continues a
    list item, which is the one place four spaces is not an indented code block.
    """
    match = patterns.FENCE_OPEN.match(line)
    if match is not None:
        return Opened(len(match.group(1)), len(match.group(2)), match.group(3))
    if in_list and patterns.INDENTED_FENCE.match(line):
        ticks, info = scan.fence_parts(line)
        return Opened(scan.indent_of(line), len(ticks), info)
    return None


def closes(line: str, opened: Opened) -> bool:
    """Whether `line` closes the fence `opened` began.

    ⛔ CommonMark: a closing fence is only backticks, at least as many as the
    opener's, and carries at most three spaces of indent MORE than the fence
    that opened it — relative to the opener, so a fence inside a list item
    closes at its own level, and an indented ``` inside the code never closes
    the block early.
    """
    stripped = line.strip()
    return (
        bool(stripped)
        and all(char == "`" for char in stripped)
        and len(stripped) >= opened.ticks
        and scan.indent_of(line) <= opened.indent + 3
    )
