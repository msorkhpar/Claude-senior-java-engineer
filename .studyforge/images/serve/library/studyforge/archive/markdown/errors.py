"""The governing rule, made concrete: never silently drop a line.

**What it does.** Defines `MarkdownError`, the refusal this reader raises
instead of returning a shorter, well-formed result than the material it was
given.

**How you use it.** Catch it to report; ⛔ never to continue. A caller that
swallows it has reintroduced exactly the loss the reader exists to remove.

**Depends on.** Nothing.

⭐ **Why a whole module for one class.** Because it is the contract, not a
detail. R6 says fail loud, never silently short: a unit with no content, a
broken link, an unmatched class is reported *by name* and exits non-zero. This
is that ruling reaching the parser, and the reason the parser refuses material
it does not understand rather than shrugging. ⚠️ The answer to a refusal is a
**vocabulary that covers what real material contains** — never a parser that
guesses.
"""

from __future__ import annotations


class MarkdownError(Exception):
    """The text is not Markdown this reader can turn into blocks without loss.

    Raised when a fenced code block never closes, when a disclosure never
    closes, or when an `<img>` tag carries no `src`. Every other line is
    classified as one of the block types or, failing all of them, `para` — so
    a paragraph is the deliberate catch-all, and these are the constructs that
    cannot fall back to it without losing the very thing they exist to carry:
    the rest of the document as code, a withheld answer as prose, a figure's
    URL.
    """
