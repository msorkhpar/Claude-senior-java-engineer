"""The two container blocks: `quote` and `disclosure`.

**What it does.** Reads a blockquote and a `<details>` disclosure into blocks
that **hold blocks**, and nothing else.

**How you use it.** `read_quote(lines, start, lang_default, parse)` and
`read_disclosure(...)`. ⭐ `parse` arrives as an argument rather than an
import: these are the only readers that recurse, and passing the dispatcher in
keeps the dependency one-way — nothing else in this package can loop back into
the document reader, and these two are testable against a stub.

**Depends on.** `patterns`, `errors`, and `leaf.read_code` for the one thing it
must agree with exactly: where a fence ends.

⛔ **Both hold blocks, not text, and this is ruled rather than chosen.** A
withheld section or a quotation can carry a list, a fence or a table, and a
`text`-only container loses exactly what *never silently drop* exists to keep.
⭐ Two container blocks, one shape, no new concept.

⚠️ **The extraction source does the opposite for `quote`, deliberately.** Its
reader returns a quote's *inner* blocks transparently so that its output agrees
with its DOM reader, for which `<blockquote>` is not a block tag. studyforge
has no DOM reader to agree with, so the reason does not carry over and a
wrapper block is correct here. Its `disclosure` handling — tags dropped, the
summary kept as a paragraph — is the same reasoning reaching an answer that is
actively wrong for us: every surveyed use of `<details>` hides an **exercise
answer**, and flattening shows it outright.

⛔ **Present but withheld is a third state.** The archive records the semantics
and the label; that it renders as `<details><summary>` is the renderer's
decision (R13). `open` is the author's default and is honoured, not overridden.
"""

from __future__ import annotations

from collections.abc import Callable

from studyforge.archive.markdown import patterns
from studyforge.archive.markdown.errors import MarkdownError
from studyforge.archive.markdown.leaf import read_code

Parse = Callable[..., list[dict]]


def read_quote(lines: list[str], start: int, lang_default: str, parse: Parse):
    """Return `(block, next_index)` for the block quote opening at `start`.

    The markers come off and the content goes back through `parse`, so a quote
    holds whatever any other place holds — a list, a fence, a table.
    """
    index = start
    inner: list[str] = []
    while index < len(lines):
        match = patterns.QUOTE.match(lines[index])
        if match is None:
            break
        inner.append(match.group(1) or "")
        index += 1
    blocks = parse("\n".join(inner), lang_default=lang_default)
    return {"type": "quote", "blocks": blocks}, index


def find_close(lines: list[str], start: int) -> int | None:
    """Return the index of the `</details>` closing the one at `start`, or None.

    ⛔ **Fence-aware, and this is the constraint that actually bites.** A
    disclosure's body may quote a `<details>` inside a fenced block — material
    that teaches HTML does exactly that — and a scan that looked for the
    closing tag line by line would stop inside the fence, ending the withheld
    section early and spilling the rest of it into the page. The fence is
    skipped using the **same reader that parses it**, so "where does this fence
    end" has one definition and cannot drift.

    ⛔ Nesting-aware too: a disclosure inside a disclosure closes its own tag
    first.
    """
    depth = 0
    index = start + 1
    total = len(lines)
    while index < total:
        if patterns.FENCE_OPEN.match(lines[index]):
            # Raises if the fence never closes, which is the right failure:
            # an unclosed fence inside a withheld section is still an unclosed
            # fence, and reporting it names the real defect.
            _block, index = read_code(lines, index, "")
            continue
        stripped = lines[index].strip()
        if patterns.DISCLOSURE_OPEN.match(stripped):
            depth += 1
        elif patterns.DISCLOSURE_CLOSE.match(stripped):
            if depth == 0:
                return index
            depth -= 1
        index += 1
    return None


def read_disclosure(lines: list[str], start: int, lang_default: str, parse: Parse):
    """Return `(block, next_index)` for the disclosure opening at `start`.

    The body is parsed like any other content, so a withheld answer keeps its
    fences, lists and tables as blocks. The `<summary>` line is lifted out as
    the block's label — it is content, and is gated, counted and spoken like
    any other text.

    ⚠️ A disclosure with no `<summary>` is legal markup and is **not** a
    refusal: nothing is lost, the label is simply empty. Only material that
    cannot be represented without loss raises.

    ⛔ A disclosure that never closes raises. Absorbing the rest of the
    document into a withheld section is worse than absorbing it as code — the
    reader cannot even see what went missing.
    """
    attrs = patterns.DISCLOSURE_OPEN.match(lines[start].strip()).group(1)
    close = find_close(lines, start)
    if close is None:
        raise MarkdownError(
            f"a <details> opened at line {start + 1} is never closed — the rest of "
            f"the document would become a withheld section, which is a loss the "
            f"reader cannot even show"
        )

    summary = ""
    body: list[str] = []
    for line in lines[start + 1 : close]:
        match = patterns.SUMMARY.match(line.strip())
        if match is not None and not summary:
            summary = match.group(1).strip()
            continue
        body.append(line)

    block = {
        "type": "disclosure",
        "summary": summary,
        "open": bool(patterns.DISCLOSURE_ATTR_OPEN.search(attrs)),
        "blocks": parse("\n".join(body), lang_default=lang_default),
    }
    return block, close + 1
