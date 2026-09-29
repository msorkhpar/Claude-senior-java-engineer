r"""How a string becomes safe page text — escaping, hrefs, and inline markers.

**What it does.** Turns the archive's text into markup: escapes it, checks a
link's scheme, and renders the four inline markers the Markdown reader
deliberately left in place.

**How you use it.**

    from studyforge.render import markup

    markup.escape("a < b")            # 'a &lt; b'
    markup.escape_attribute(value)    # additionally neutralises "'"
    markup.inline("see `x` and **y**")

**Depends on.** `re`, and nothing else — not this framework's error type and not
any renderer. ⛔ Nothing that knows what a block is: every block renderer
depends on this module, and it depends on none of them. ⚠️ **The line this
replaced said *"`re` and `page.errors`"* and had said so from the start; the
import list has never carried `page.errors`, and a "depends on" that
names a dependency the module does not have is the sentence that makes the move
this module has just made look impossible.

## ⛔ Escaping is not optional, and it is the only thing between the archive and
## the browser

⚠️ Teaching material quotes `<`, `>`, `&` and `"` constantly — inside code
blocks on almost every page, and inside prose whenever the subject is markup.
Getting this wrong is a rendering bug and an injection bug at the same time, so
**every** text path goes through `escape`, every attribute through
`escape_attribute`, and every href is additionally scheme-checked.

⭐ **`&` is replaced first**, or every entity the later replacements write is
escaped a second time and the page shows `&amp;lt;`.

## ⭐ The inline markers are rendered here, and this is their one definition

⚠️ **`archive.markdown` states that emphasis, inline code and links are left
untouched inside `text`** — *"because stripping them is exactly the loss it
exists to prevent"*. ⛔ **So something has to render them, and if nothing does,
the reading floor shows a reader literal backticks and `[label](href)`.** That
is this module.

⛔ **`SEGMENT_KINDS` and `segments()` are published for narration's
speech units**, which needs the same split for the *spoken* half and must take it from here
rather than write a second one. ⚠️ The extraction source made the same argument
in the opposite direction — its renderer imported the split from its speech
module — and the argument is the ordering, not the direction: **display and
speech must never disagree about where a marker begins.** ⭐ Only the
*rendering* differs, which is the whole point: speech drops the href, display
keeps it clickable.

⚠️ **A marker is rendered, never shown literally.** A parser that gains a marker
and a renderer that does not is exactly how markup reaches the page as text, so
the branch list below is checked against `SEGMENT_KINDS` by this module's test.

## ⛔ A refused href keeps its words and loses its link

⭐ `javascript:`, `data:` and `vbscript:` render as plain text rather than as a
live anchor. The corpus is trusted-ish; a page the reader opens in their own
browser is not the place to find out that it was not.

## ⛔ TWO permitted forms

⛔ **A permitted href is an *absolute* one whose scheme is in `SAFE_SCHEMES`, or
a *relative reference* that stays inside the site. Nothing else.**

⚠️ **Both halves are closed sets, and that is the point.** The scheme list is
closed because the unforeseen scheme must be refused; the relative form is
closed because it is a *grammar* — no scheme, not rooted — rather than a list of
prefixes somebody extends when a page breaks.

⛔ **`#`, `./` and `../` are not schemes.** ⚠️ Crowded into a set called
`SAFE_SCHEMES` they would make it look complete while the commonest relative
reference of all — a bare `page.html`, naming a file in the same directory —
had no entry. ⭐ **Under `sibling` placement that is every same-container
*next* and *previous* link.**

⭐ **The consequence, stated rather than hidden: `example.com/x` is a
relative link to a local file that does not exist.** ⚠️ It is formally indistinguishable from
`unit-02-fields.unit.html` —
both are one path segment containing dots — so separating them needs a *"looks
like a hostname"* heuristic, which is the open set this project has banned five
times. ⛔ **A dead relative link costs one reader one click; the alternative costs
every same-container link on every `sibling` corpus.**

## ⛔ What a relative reference may NOT be, and why each one is named

⚠️ **A rooted href — `/x` — is refused (R8).** Opened over `file://` it resolves
to the *filesystem* root, not the site root, so it is broken at the floor this
project ships on; `placement.relative_href` already refuses to *emit* one for
exactly that reason. ⛔ **And `/home/<someone>/notes.html` is a rooted href, so
admitting the form admits a personal path straight into a page (R7).**

⚠️ **A protocol-relative href — `//host/x` — is refused.** It carries no scheme
of its own and inherits the page's, which is how a relative-looking string
reaches a *remote host* and breaks R8's no-network floor.

⛔ **And every character is checked against `_HREF_CHARACTERS`, because a browser
normalises before it parses.** Tab, newline and carriage return are *stripped*
from a URL and `\` is *rewritten* to `/`, so `java<TAB>script:x` has no scheme to
a naive reader and `javascript:` to the browser — and `/\host/x` is `//host/x`.
⭐ Enumerating the legal spelling closes both; enumerating the illegal one is the
list that would have forgotten the second.

## ⛔ What this function does NOT guarantee

⭐ **The neighbour this does not assert, named.** `safe_href` sees a
string and no page, so it cannot know how many `../` steps leave the site root:
`../../../../../etc/passwd` is a well-formed relative reference and is
**admitted**. ⚠️ Bounding that needs the asking page's depth, which lives in
`corpus.placement`, not here.
"""

from __future__ import annotations

import re

#: Ordered, and `&` is first. ⛔ Any other order double-escapes.
_ESCAPES = (("&", "&amp;"), ("<", "&lt;"), (">", "&gt;"), ('"', "&quot;"))

#: The schemes an ABSOLUTE href may carry — lower-cased, and without the `:`.
#: ⛔ A closed set of what is permitted, never a list of what is refused: the
#: forbidden list is the one that is silently incomplete, and `vbscript:` is the
#: entry every version of it forgets.
#:
#: ⚠️ **These are schemes and only schemes.** `#`, `/`, `./` and `../` are not
#: schemes; a relative reference is the second, separate form above.
SAFE_SCHEMES = ("http", "https", "mailto")

#: Every character an href may be spelled with: RFC 3986's whole repertoire —
#: unreserved, gen-delims, sub-delims and `%` — and nothing else.
#:
#: ⛔ This is what keeps a refused scheme refused. A browser strips tab, newline
#: and carriage return out of a URL and rewrites `\` to `/` **before** deciding
#: what the scheme is, so a reader that does not check the spelling sees no
#: scheme in `java<TAB>script:x` where the browser sees `javascript:`.
_HREF_CHARACTERS = frozenset(
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    "-._~"  # unreserved
    ":/?#[]@"  # gen-delims
    "!$&'()*+,;="  # sub-delims
    "%"  # percent-encoding
)

#: RFC 3986's `scheme ":"`, anchored. ⚠️ `/` is deliberately not a scheme
#: character, so `notes/todo:1` has no scheme — which is what a browser
#: concludes too, and why the check must be a grammar rather than a `":" in`.
_SCHEME = re.compile(r"([A-Za-z][A-Za-z0-9+.\-]*):")

#: What a relative reference may not begin with: `/x` is rooted and breaks the
#: `file://` floor (R8) and can carry a home path (R7); `//x` is protocol-
#: relative and reaches a remote host. ⭐ One prefix refuses both.
_OUTSIDE_THE_SITE = "/"

#: What one inline segment can be. ⛔ Ordered as `_INLINE` alternates, so a
#: position where two markers could both start resolves the same way here as it
#: does in the expression.
SEGMENT_KINDS = ("text", "code", "link", "strong", "em")

#: The four markers, in precedence order. ⚠️ Code first: a backtick span may
#: contain any of the others and none of them may be read inside it.
_INLINE = re.compile(
    r"`([^`]+)`"  # 1    `code`
    r"|\[([^\]\n]+)\]\(([^)\s]*)\)"  # 2,3  [label](href)
    r"|\*\*([^\s*](?:[^*\n]*[^\s*])?)\*\*"  # 4    **strong**
    r"|\*([^\s*](?:[^*\n]*[^\s*])?)\*"  # 5    *em*
    r"|(?<!\w)_([^\s_](?:[^_\n]*[^\s_])?)_(?!\w)"  # 6    _em_
)


def escape(value: object) -> str:
    """Escape text content. ⛔ `&` first, or every later entity double-escapes."""
    out = str(value if value is not None else "")
    for char, entity in _ESCAPES:
        out = out.replace(char, entity)
    return out


def escape_attribute(value: object) -> str:
    """Escape for a double-quoted attribute value; also neutralises `'`.

    ⚠️ The apostrophe matters even though every attribute this renderer writes
    is double-quoted: a page is edited, and an attribute re-quoted by hand
    tomorrow must not become an escape hatch today.
    """
    return escape(value).replace("'", "&#39;")


def safe_href(href: object) -> str | None:
    """Return the href when it is permitted, else `None` — render it as text.

    ⛔ **Two permitted forms and nothing else**, in the order they are decided:

    * **absolute** — a scheme in `SAFE_SCHEMES` followed by `:`;
    * **relative reference** — no scheme at all, and inside the site: neither
      rooted (`/x`, R8 and R7) nor protocol-relative (`//host/x`, R8).

    ⚠️ The spelling is checked first, because a browser normalises a URL before
    it parses one and this function must not read a different string than the
    browser will.
    """
    candidate = href.strip() if isinstance(href, str) else ""
    if not candidate or not _HREF_CHARACTERS.issuperset(candidate):
        return None
    scheme = _SCHEME.match(candidate)
    if scheme is not None:
        return candidate if scheme.group(1).lower() in SAFE_SCHEMES else None
    return None if candidate.startswith(_OUTSIDE_THE_SITE) else candidate


def segments(value: object) -> tuple[tuple[str, str, str], ...]:
    """Split text into `(kind, body, href)` segments, left to right.

    `kind` is one of `SEGMENT_KINDS`; `href` is empty for everything but a link,
    and `body` is the marker's *content* with the marker characters already
    removed. ⛔ Markers are never nested and never re-escaped — the archive
    stores them verbatim and this is their only reader.
    """
    text = value if isinstance(value, str) else ""
    out: list[tuple[str, str, str]] = []
    position = 0
    for match in _INLINE.finditer(text):
        if match.start() > position:
            out.append(("text", text[position : match.start()], ""))
        code, label, href, strong, em_star, em_score = match.groups()
        if code is not None:
            out.append(("code", code, ""))
        elif label is not None:
            out.append(("link", label, href or ""))
        elif strong is not None:
            out.append(("strong", strong, ""))
        else:
            out.append(("em", em_star if em_star is not None else em_score, ""))
        position = match.end()
    if position < len(text):
        out.append(("text", text[position:], ""))
    return tuple(out)


def inline(value: object) -> str:
    """Render one run of prose: the markers become markup, everything else escapes.

    ⛔ **Every branch escapes.** The marker decides which element the body is
    wrapped in; it never decides whether the body is trusted, and there is no
    path through this function on which text reaches the page unescaped.
    """
    out: list[str] = []
    for kind, body, href in segments(value):
        if kind == "code":
            out.append(f"<code>{escape(body)}</code>")
        elif kind == "strong":
            out.append(f"<strong>{escape(body)}</strong>")
        elif kind == "em":
            out.append(f"<em>{escape(body)}</em>")
        elif kind == "link":
            out.append(_anchor(body, href))
        else:
            out.append(escape(body))
    return "".join(out)


def _anchor(body: str, href: str) -> str:
    """One inline link, or its words alone when the scheme was refused."""
    target = safe_href(href)
    if target is None:
        return escape(body)
    return f'<a href="{escape_attribute(target)}" rel="noopener noreferrer">{escape(body)}</a>'
