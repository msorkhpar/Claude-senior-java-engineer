"""The readers that produce one block: heading, code, image, video, rule, html, para.

**What it does.** Turns a run of lines into a single block of the archive's
vocabulary. Each function takes `(lines, start)` and returns
`(block, next_index)`, or takes one line where one line is all it needs.

**How you use it.** Through `document.parse`. These are the handlers the
dispatcher picks between; calling one directly means having already decided
what the line is, which is `scan.block_kind`'s job.

**Depends on.** `patterns`, `scan` and `errors`. ⛔ Not on `document`: nothing
here recurses, which is what keeps the container readers the only place that
does.

⚠️ **`rule` and `html` are studyforge's, and the extraction source has
neither.** Its parser emits no block at all for a thematic break, and lets an
unknown tag fall through to prose — both to agree with its DOM reader, for
which `<hr>` is not a block tag and raw markup is markup. ⭐ studyforge has no
DOM reader to agree with, so a thematic break is a `rule` block and raw
block-level HTML is an `html` block. The source will look like it is doing the
opposite; it is, deliberately.
"""

from __future__ import annotations

from html import unescape

from studyforge.archive.markdown import fences, patterns, scan
from studyforge.archive.markdown.errors import MarkdownError


def read_heading(line: str) -> dict:
    """Return the `heading` block for an ATX heading line."""
    match = patterns.HEADING.match(line)
    return {"type": "heading", "level": len(match.group(1)), "text": match.group(2)}


def read_rule() -> dict:
    """Return the `rule` block for a thematic break.

    ⚠️ A block, where the extraction source emits nothing. A thematic break is
    a real division of the material — 10 of one surveyed corpus's 166 lessons
    use one — and a reader that dropped it would be losing structure the author
    wrote, which is the loss this parser exists to refuse.
    """
    return {"type": "rule"}


def read_code(lines: list[str], start: int, lang_default: str, indent: int | None = None):
    """Return `(block, next_index)` for the fenced code block opening at `start`.

    `indent` is how far the opening fence is indented, and every content line
    has up to that many leading spaces removed — CommonMark's rule, and the
    only way a fence written inside a list item yields the code the author
    wrote rather than the code plus the list's scaffolding. It is passed in
    rather than re-measured because a list opens a fence at the item's column,
    which `FENCE_OPEN` alone would not accept.
    """
    match = patterns.FENCE_OPEN.match(lines[start])
    if indent is None:
        indent = len(match.group(1)) if match else 0
    ticks, info = (match.group(2), match.group(3)) if match else scan.fence_parts(lines[start])
    lang = info.strip().lower() or lang_default
    # ⛔ Where the fence closes is `fences.closes`'s, the one grammar the
    # exercise ledger reads fences by too: at most three spaces of indent MORE
    # than the opener, relative to it, so a fence inside a list item closes at
    # its own level and an indented ``` inside the code never closes it early.
    opened = fences.Opened(indent, len(ticks), info)
    for index in range(start + 1, len(lines)):
        if fences.closes(lines[index], opened):
            body = [scan.dedent(line, indent) for line in lines[start + 1 : index]]
            return {"type": "code", "lang": lang, "text": "\n".join(body)}, index + 1
    raise MarkdownError(
        f"a code fence opened at line {start + 1} is never closed — absorbing the "
        f"rest of the document as code would be exactly the silent loss this "
        f"reader must not commit"
    )


def read_image(line: str, lineno: int) -> dict | None:
    """Return the `image` block for a line that is exactly `<img …>` or `![alt](src)`.

    Attribute values may be single- or double-quoted; real material uses both.
    ⛔ An `<img>` tag with no `src` raises rather than producing an image block
    with an empty URL: a figure that silently renders as a broken icon is
    exactly the kind of loss this reader exists to refuse.
    """
    match = patterns.MD_IMAGE_SPAN.match(line)
    if match:
        alt, src = match.groups()
        return {"type": "image", "src": src, "alt": alt, "width": None}
    match = patterns.IMG_TAG.match(line)
    if match:
        attrs = {}
        for attr in patterns.ATTR.finditer(match.group(1)):
            value = attr.group("dq") if attr.group("dq") is not None else attr.group("sq")
            attrs[attr.group("name")] = value
        src = attrs.get("src")
        if not src:
            # ⛔ The line is the source's own text and is never reproduced
            # (R7): a markdown line can carry anything the corpus author
            # wrote, including a path. The line **number** is what a reader
            # needs, and they have the file.
            raise MarkdownError(
                f"line {lineno} is an <img> tag with no src attribute; "
                f"an image block cannot be written without one"
            )
        # ⛔ A PIXEL COUNT OR NOTHING. The block contract is `width: int | None`,
        # and authors also write `width="90%"` — `int("90%")` raises a bare
        # ValueError out of here and costs the whole document to an unhandled
        # crash. ⭐ Dropped rather than coerced: a page constrains every image
        # to the reading column anyway, so a percentage is close to a no-op
        # there, and losing the document over it is not.
        width = attrs.get("width") or ""
        return {
            "type": "image",
            "src": src,
            "alt": attrs.get("alt", ""),
            "width": int(width) if width.isdigit() else None,
        }
    return None


def read_video(line: str) -> dict:
    """Return the `video` block for an inline media embed carrying a `src`.

    ⛔ UNESCAPED. The attribute arrives as the page wrote it, so a query string
    comes through as `a&amp;b`; left alone it is escaped again into the href
    and the reader clicks through to the wrong URL.
    """
    stripped = line.strip()
    title = patterns.EMBED_TITLE.search(stripped)
    return {
        "type": "video",
        "src": unescape(patterns.EMBED_SRC.match(stripped).group(1)),
        "title": unescape(title.group(1).strip() if title else ""),
    }


def read_html(lines: list[str], start: int):
    """Return `(block, next_index)` for a run of raw block-level HTML.

    ⭐ **`html` survives for genuinely unstructured markup**, stored verbatim,
    rendered as-is, never parsed — because a lesson teaching HTML must keep its
    HTML. It is gated like any other string.

    The run ends at a blank line, which is CommonMark's own rule for an HTML
    block, and is bounded by nothing else: an unclosed `<div>` costs the
    paragraph after it, never the rest of the document.

    ⚠️ This is the LAST tag rule. `<img>`, a media embed, `<details>` and
    `<summary>` all reach their own readers first, and a fence reaches the code
    reader before any of them.
    """
    index = start
    while index < len(lines) and lines[index].strip():
        index += 1
    return {"type": "html", "text": "\n".join(lines[start:index])}, index


def read_paragraph(lines: list[str], start: int):
    """Return `(block, next_index)` for the paragraph opening at `start`.

    Everything that is not itself the start of another construct accumulates
    here, joined with single spaces. It stops at a blank line *or* at the first
    following line `scan.block_kind` recognises — ⛔ a paragraph must never
    absorb a line the dispatcher would have handled, which is exactly how a
    code fence with no blank line before it ends up as prose with literal
    backticks in it.

    A line that matches nothing still falls in here unchanged. ⭐ That
    catch-all is deliberate: only a *classifiable* line must not be swallowed.
    """
    index = start
    total = len(lines)
    while index < total and lines[index].strip():
        if index > start and scan.block_kind(lines, index) is not None:
            break
        index += 1
    text = " ".join(line.strip() for line in lines[start:index])
    return {"type": "para", "text": text}, index
