"""The dispatcher: authored Markdown in, blocks out.

**What it does.** Walks the lines once, asks `scan.block_kind` what opens at
each, and hands it to the reader that understands it.

**How you use it.** `parse(text, lang_default="")`. Re-exported from the
package, which is what consumers import.

**Depends on.** Every other module here. ⛔ Nothing depends on it, which is
what keeps the recursion in one place: the container readers receive `parse`
as an argument rather than importing it.
"""

from __future__ import annotations

from studyforge.archive.markdown import container, leaf, listing, scan, table


def parse(text: str, *, lang_default: str = "") -> list[dict]:
    r"""Return the authored Markdown `text` as a list of blocks, in document order.

    `lang_default` is the language a bare code fence falls back to — typically
    the corpus variant, since material often fences a snippet without naming
    the language.

    Raises `MarkdownError` rather than returning a partial result when a fenced
    code block never closes, when a `<details>` never closes, or when an `<img>`
    tag has no `src` — the constructs that cannot fall back to an ordinary
    paragraph without losing the thing they exist to carry.

    Line endings are normalised (`\r\n` and lone `\r` both become `\n`)
    before splitting, so CRLF input classifies identically to LF input: every
    pattern anchors on `$`, and a stray trailing `\r` would otherwise make
    every one of them silently miss.
    """
    text = text.replace("\r\n", "\n").replace("\r", "\n")
    lines = text.split("\n")
    blocks: list[dict] = []
    index = 0
    total = len(lines)
    while index < total:
        line = lines[index]
        if not line.strip():
            index += 1
            continue

        kind = scan.block_kind(lines, index)
        if kind == "code":
            block, index = leaf.read_code(lines, index, lang_default)
        elif kind == "heading":
            block = leaf.read_heading(line)
            index += 1
        elif kind == "image":
            span = scan.image_span(lines, index)
            if span is None:
                block = leaf.read_image(line, index + 1)
                index += 1
            else:
                joined, index = span
                block = leaf.read_image(joined, index)
                # ⚠️ The alt reaches an HTML attribute and a narrator reads it
                # aloud; a renderer collapses its newlines, so this does.
                block["alt"] = " ".join(block["alt"].split())
        elif kind == "quote":
            block, index = container.read_quote(lines, index, lang_default, parse)
        elif kind == "disclosure":
            block, index = container.read_disclosure(lines, index, lang_default, parse)
        elif kind == "video":
            block = leaf.read_video(line)
            index += 1
        elif kind == "rule":
            block = leaf.read_rule()
            index += 1
        elif kind == "media-embed":
            # ⚠️ The closing wrapper of an embed whose opening tag already
            # produced the `video` block. It carries no content of its own —
            # the `src` is the content, and it has been kept.
            index += 1
            continue
        elif kind == "html":
            block, index = leaf.read_html(lines, index)
        elif kind == "table":
            block, index = table.read_table(lines, index)
        elif kind == "list":
            # ⭐ An item's fenced code is a part of that item (spec §6), so
            # the list reader returns the one list block it read.
            made, index = listing.read_list(lines, index)
            blocks.extend(made)
            continue
        else:
            block, index = leaf.read_paragraph(lines, index)
        blocks.append(block)
    return blocks


# ⛔ `BLOCK_TYPES` and `CONTAINER_TYPES` live in `studyforge.archive.blocks`,
# not here, and this package's `__init__` re-exports them from there: the
# vocabulary is one row per type, and the reader is a consumer of it.
#
# ⚠️ One thing changed in the move: the order. This tuple had `list` before
# `table`, and a document's `counts` object — which reaches disk — has
# `tables` before `lists`. The serialised order wins (R10); nothing here ever
# depended on the order, which is why the wrong one was the easy one to keep.
