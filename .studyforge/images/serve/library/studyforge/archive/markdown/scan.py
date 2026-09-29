"""Which construct opens at a line, and the line arithmetic every reader shares.

**What it does.** Classifies a line as the opening of exactly one construct,
and provides the indent, dedent, fence and table-cell helpers the readers use.

**How you use it.** `block_kind(lines, index)` returns a construct name or
`None`. ⛔ It is the **single source of truth** for "does a fresh block start
here", shared by the dispatcher and by the paragraph reader. Duplicating the
judgement is how a paragraph comes to disagree with the dispatcher and quietly
absorbs a fence, a heading or a table row into prose.

**Depends on.** `patterns`. Nothing else in this package, so nothing here can
recurse.

⛔ **Fence awareness lives here, and it is the constraint that actually
bites.** `FENCE_OPEN` is tested **first**, before any tag rule, so a
`<details>` or a `<div>` written inside a fenced block is never classified as
markup — the code reader consumes to the closing fence and the tag is never
offered to anything. ⚠️ A reader that scanned the document for `<` instead
would pass every other case and fail here, silently, on material that quotes
XML or HTML in a fence: in real material, tag-shaped text is mostly inside
fences.
"""

from __future__ import annotations

from studyforge.archive.markdown import patterns


def indent_of(line: str) -> int:
    """Return how many spaces `line` is indented by."""
    return len(line) - len(line.lstrip(" "))


def dedent(line: str, indent: int) -> str:
    """Remove up to `indent` leading spaces, never a character of content."""
    keep = 0
    while keep < indent and keep < len(line) and line[keep] == " ":
        keep += 1
    return line[keep:]


def fence_parts(line: str) -> tuple[str, str]:
    """Return `(ticks, info)` for a fence at any indent — what a list opens with."""
    stripped = line.lstrip(" ")
    ticks = stripped[: len(stripped) - len(stripped.lstrip("`"))]
    return ticks, stripped[len(ticks) :]


def cells(line: str) -> list[str]:
    r"""Return the cells of one pipe-table row: strip the outer `|`, split, unescape.

    Split on *unescaped* pipes only, and `\|` comes back as `|`: a cell is
    allowed to contain a pipe and says so with a backslash, which is what GFM
    means by it.
    """
    inner = line.strip()
    if inner.startswith("|"):
        inner = inner[1:]
    if inner.endswith("|") and not inner.endswith("\\|"):
        inner = inner[:-1]
    return [cell.replace("\\|", "|").strip() for cell in patterns.UNESCAPED_PIPE.split(inner)]


def is_separator_row(line: str) -> bool:
    """Report whether `line` is a table's `--- | ---` rule.

    ⛔ **The outer pipes are optional and the inner one is not.** GFM makes a
    leading and trailing `|` optional and authors omit both, so requiring the
    leading pipe turns every borderless comparison table into a paragraph —
    silently, because prose is the catch-all.

    Requiring an **unescaped pipe in the separator row itself** is what keeps
    the leading-pipe rule's one benefit: without it, any paragraph line holding
    a `|` followed by a setext underline (`---`) would open a table.
    """
    if not patterns.UNESCAPED_PIPE.search(line):
        return False
    row = cells(line)
    return bool(row) and all(patterns.SEPARATOR_CELL.match(cell) for cell in row)


def image_span(lines: list[str], start: int) -> tuple[str, int] | None:
    """Return `(text, next_index)` for an image whose alt text wraps, else None.

    Bounded and blank-line terminated: an `![` that never closes stays prose
    rather than absorbing the rest of the document.
    """
    if not patterns.MD_IMAGE_OPEN.match(lines[start]) or patterns.MD_IMAGE.match(lines[start]):
        return None
    stop = min(len(lines), start + patterns.IMAGE_SPAN_LIMIT)
    for end in range(start + 1, stop):
        if not lines[end].strip():
            return None
        joined = "\n".join(lines[start : end + 1])
        if patterns.MD_IMAGE_SPAN.match(joined):
            return joined, end + 1
    return None


def block_kind(lines: list[str], index: int) -> str | None:
    """Return which construct opens at `lines[index]`, or None if it opens nothing.

    ⛔ The order of these tests is the contract, not an implementation detail:

    1. **A fence comes first**, so nothing inside one is ever classified.
    2. A thematic break before a list, because `- - -` is both and CommonMark
       gives the break priority.
    3. The named tags — image, media embed, disclosure, summary — before the
       general raw-HTML rule, so each reaches the reader that understands it.
    4. Raw HTML last among the tag rules, and prose last of all.

    `None` means "nothing classifiable", and the paragraph reader takes it.
    ⭐ That catch-all is deliberate: only a *classifiable* line must never be
    swallowed.
    """
    line = lines[index]
    if patterns.FENCE_OPEN.match(line):
        return "code"
    if patterns.HEADING.match(line):
        return "heading"
    if patterns.MD_IMAGE.match(line) or patterns.IMG_TAG.match(line):
        return "image"
    if image_span(lines, index) is not None:
        return "image"
    if patterns.QUOTE.match(line):
        return "quote"
    stripped = line.strip()
    if patterns.DISCLOSURE_OPEN.match(stripped):
        return "disclosure"
    if patterns.MEDIA_EMBED.match(stripped):
        return "video" if patterns.EMBED_SRC.match(stripped) else "media-embed"
    if patterns.THEMATIC.match(line):
        return "rule"
    if patterns.UNORDERED.match(line) or patterns.ORDERED.match(line):
        return "list"
    # A header row is any line carrying an unescaped pipe whose next line is
    # the separator. The outer pipes are optional — see `is_separator_row`.
    if (
        patterns.UNESCAPED_PIPE.search(line)
        and index + 1 < len(lines)
        and is_separator_row(lines[index + 1])
    ):
        return "table"
    if patterns.HTML_OPEN.match(stripped):
        return "html"
    return None
