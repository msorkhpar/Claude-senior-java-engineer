"""Every pattern the reader recognises, and the defect each one exists to fix.

**What it does.** Holds the compiled expressions for the constructs real
teaching material emits, and nothing else. No parsing happens here.

**How you use it.** Import the constant. ⛔ Never re-declare one locally: each
of these was widened or narrowed against a document that broke, and a second
copy is a second answer to "what is a fence".

**Depends on.** `re`. Nothing in this package, so it can be read first.

⚠️ **The comments are the deliverable as much as the expressions are.** Each
pattern's comment says what real material it must read and what a simpler
pattern would misread, because a bare pattern invites somebody to "simplify"
it into one that misreads that material.
"""

from __future__ import annotations

import re

HEADING = re.compile(r"^(#{1,6}) (.*)$")

#: ⛔ **Up to three leading spaces, per CommonMark.** Anchored at column 0, a
#: one-space opener is invisible — which makes the *closing* fence an opener
#: and refuses the document with "never closed" pointing at the wrong line.
#: Four spaces or more is an indented code block at the top level and is
#: deliberately still not a fence here; inside a list item it is, and
#: `listing` handles that.
FENCE_OPEN = re.compile(r"^( {0,3})(`{3,})\s*(.*)$")

#: A fence indented past what `FENCE_OPEN` accepts — only meaningful as the
#: continuation of a list item, which is the one place four or more spaces
#: does not mean "indented code block".
INDENTED_FENCE = re.compile(r"^ +`{3,}")

#: ⛔ Up to three leading spaces here too. Material that indents a whole list
#: two spaces under the paragraph introducing it had its items read as lazy
#: continuation of that paragraph, and the lists vanished into prose.
#: ⚠️ Four or more at the top level is an indented code block, not a list —
#: which is why the bound is 3 and not "any".
UNORDERED = re.compile(r"^ {0,3}[-*] (.*)$")

#: `)` as well as `.`: CommonMark allows both and authors use both.
ORDERED = re.compile(r"^ {0,3}\d+[.)] (.*)$")

#: ⛔ The same two markers at ANY indent, and only ever asked INSIDE a list.
#: Bounded at three, a nested item written four spaces under its
#: parent matched nothing and folded into the parent as literal `- …` text.
NESTED_UNORDERED = re.compile(r"^ *[-*] (.*)$")
NESTED_ORDERED = re.compile(r"^ *\d+[.)] (.*)$")

#: ⭐ The number an ordered marker carries, at any indent. Asked only of
#: a line one of the ordered patterns above already matched.
ORDERED_NUMBER = re.compile(r"^ *(\d+)[.)] ")

#: ⚠️ Checked BEFORE the list, because `- - -` is a valid thematic break AND
#: looks like a list item whose text is `- -`; CommonMark gives the break
#: priority. A table's separator row carries pipes, so it can never match.
THEMATIC = re.compile(r"^ {0,3}([-*_])(?:[ \t]*\1){2,}[ \t]*$")

QUOTE = re.compile(r"^ {0,3}>(?: (.*)|)$")

#: ⛔ An inline raw-HTML **media embed**. A page renders a real element for
#: these, so the wrapper carries no prose: `video`/`source`/`iframe` with a
#: `src` becomes a `video` block, and the bare wrapper tags around it carry
#: nothing. ⭐ NAMED tags only — `<img>` is deliberately absent, because it has
#: a block type of its own and its file is archived beside the page.
MEDIA_EMBED = re.compile(
    r"^</?(?:video|source|iframe|audio|track|embed|object)\b[^>]*>"
    r"(?:\s*</(?:iframe|video|audio|object)\s*>)?$",
    re.IGNORECASE,
)

#: ⛔ The SOURCE inside a media embed is content, not markup. Dropping the
#: wrapper whole leaves a reader looking at "watch the video below" above
#: nothing.
EMBED_SRC = re.compile(
    r"^<(?:source|iframe|embed)\b[^>]*\bsrc=[\"\']([^\"\']+)[\"\']",
    re.IGNORECASE,
)
EMBED_TITLE = re.compile(r"\btitle=[\"\']([^\"\']*)[\"\']", re.IGNORECASE)

#: ⛔ `<details>` opens a **disclosure**, a container block — not markup to
#: drop and not one opaque string. The attribute group captures `open`, which
#: is the author's default and is honoured rather than overridden.
DISCLOSURE_OPEN = re.compile(r"^<details\b([^>]*)>$", re.IGNORECASE)
DISCLOSURE_CLOSE = re.compile(r"^</details\s*>$", re.IGNORECASE)
DISCLOSURE_ATTR_OPEN = re.compile(
    r"(?:^|\s)open(?:\s*=\s*[\"\']?(?:open|true|)[\"\']?)?(?:\s|$)", re.IGNORECASE
)

#: The label. ⚠️ Its text is content: gated, counted and spoken like any other.
SUMMARY = re.compile(r"^<summary\s*>(.*)</summary\s*>$", re.IGNORECASE | re.DOTALL)

#: ⛔ Raw block-level HTML, and the tag list is **CommonMark's own** (its
#: "HTML block type 6"). Narrow on two axes, both deliberate:
#:
#: - ⛔ **A NAMED block-level tag, not any tag.** CommonMark's type 7 would
#:   also open an HTML block for an *unknown* tag alone on a line; this
#:   reader deliberately does not, so `<marquee>hello</marquee>` and
#:   `<not-a-known-tag> stays as it was` remain **prose**. ⭐ That is what
#:   keeps an `html` block a statement about structure rather than a catch-all
#:   for anything angle-shaped — and it means a paragraph that merely opens
#:   with something tag-shaped keeps its inline emphasis, which an `html`
#:   block would render verbatim and lose.
#: - ⛔ `A < B` in prose is prose. A pattern loose enough to catch a bare `<`
#:   turns arithmetic into markup.
#:
#: ⚠️ Checked LAST among the tag rules, so `<img>`, a media embed,
#: `<details>` and `<summary>` reach their own readers first.
HTML_BLOCK_TAGS = (
    "address|article|aside|base|basefont|blockquote|body|caption|center|col|colgroup"
    "|dd|details|dialog|dir|div|dl|dt|fieldset|figcaption|figure|footer|form|frame"
    "|frameset|h[1-6]|head|header|hr|html|iframe|legend|li|link|main|menu|menuitem"
    "|nav|noframes|ol|optgroup|option|p|param|search|section|summary|table|tbody|td"
    "|tfoot|th|thead|title|tr|track|ul"
)
HTML_OPEN = re.compile(
    r"^(?:<!--|</?(?:" + HTML_BLOCK_TAGS + r")(?:\s[^<>]*)?/?>)",
    re.IGNORECASE,
)

MD_IMAGE = re.compile(r"^!\[(.*)\]\((.*)\)$")

#: ⛔ An image whose ALT TEXT WRAPS. CommonMark allows a newline inside an
#: image's link text, and authors write a paragraph of alt text that wraps
#: before the `](url)`. A single-line anchored pattern does not match it and
#: the whole thing becomes a paragraph.
MD_IMAGE_OPEN = re.compile(r"^!\[")
MD_IMAGE_SPAN = re.compile(r"^!\[(.*)\]\((.*)\)$", re.DOTALL)

#: How many lines an image's alt text may wrap over before this stops looking.
#: A bound, so an unclosed `![` cannot swallow the rest of the document.
IMAGE_SPAN_LIMIT = 12

IMG_TAG = re.compile(r"^<img\s+(.*?)\s*/?>$", re.IGNORECASE)
ATTR = re.compile(r"""(?P<name>\w+)=(?:"(?P<dq>[^"]*)"|'(?P<sq>[^']*)')""")

SEPARATOR_CELL = re.compile(r"^:?-{3,}:?$")

#: A `|` that is not preceded by a backslash: the only thing that ends a table
#: cell. GFM writes a literal pipe inside a cell as `\|`. Splitting on every
#: `|` turns one such cell into two, which is the silent loss this reader
#: exists to refuse.
UNESCAPED_PIPE = re.compile(r"(?<!\\)\|")
