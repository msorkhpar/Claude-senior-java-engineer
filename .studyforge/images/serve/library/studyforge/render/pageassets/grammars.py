"""Which fence languages the vendored highlighter covers, and the plain fallback.

**What it does.** Reads the language names the vendored `prism.js` declares in
its own header, and answers, for one fence language, which grammar the page
asks for: that language when the bundle covers it, `PLAIN` when it does not.

**How you use it.** `grammar_for(lang)` gives the name for `language-<name>`;
`falls_back(lang)` says whether the page must say so. `highlighted_languages()`
is the whole declared set.

**Depends on.** `errors`, `source` and `vendored`.

## ⛔ The set is DECLARED in the bundle and DERIVED by a test

⚠️ The declaration is the `Languages:` line of the vendoring header, the one
part of a vendored file this project may write. A list no instrument checks
would drift the day a grammar is re-vendored. ⭐ So
`test_highlight_grammars.py` runs the bundle and compares every name
`Prism.languages` carries with this line, both ways. A name declared but
absent is RED. A grammar carried but undeclared is RED too, because it would
render as a fallback that is not one.

⛔ **The framework learns no corpus's languages** (R1). The set belongs to the
asset this project ships, and a corpus manifest never states it. A corpus in a
language nobody vendored still renders.

## ⛔ An undeclared language renders as a DECLARED plain fallback

⚠️ Emitting `language-gherkin` for a bundle with no gherkin grammar gives a
page that looks as if the highlighter broke, and nothing says why. ⭐ The page
asks for `PLAIN` instead, a grammar the bundle declares and that emits no
tokens. The renderer marks the figure so the reader sees which language fell
back.
"""

from __future__ import annotations

from studyforge.render.pageassets.errors import AssetError
from studyforge.render.pageassets.vendored import header_of

#: The vendored bundle whose grammars decide what is highlighted.
HIGHLIGHTER = "prism.js"

#: What starts the declaration line in that bundle's header.
DECLARATION_MARKER = "Languages:"

#: The grammar an undeclared language falls back to. ⛔ It must be declared
#: itself, and Prism defines it as empty, so it colours nothing.
PLAIN = "plain"

#: The declaration once read. ⚠️ A module-level value rather than a cache
#: decorator, because this package imports nothing outside the standard
#: library's `pathlib` and itself, and a page of fences asks once per fence.
_declared: frozenset[str] | None = None


def declared_languages(header: str) -> frozenset[str]:
    """Return the language names a vendoring `header` declares, or raise.

    ⛔ Refuses a header with no declaration, an empty one, and one that does not
    declare `PLAIN`. With any of those, every fence would render unstyled and
    nothing would say so.
    """
    lines = [line for line in header.splitlines() if DECLARATION_MARKER in line]
    if len(lines) != 1:
        raise AssetError(
            f"the header of {HIGHLIGHTER} must carry exactly one "
            f"{DECLARATION_MARKER!r} line, and it carries {len(lines)}"
        )
    body = lines[0].split(DECLARATION_MARKER, 1)[1].strip().removesuffix(".")
    names = frozenset(body.split())
    if not names:
        raise AssetError(f"the {DECLARATION_MARKER!r} line of {HIGHLIGHTER} names no language")
    if PLAIN not in names:
        raise AssetError(
            f"the {DECLARATION_MARKER!r} line of {HIGHLIGHTER} does not declare "
            f"{PLAIN!r}, which an undeclared fence language falls back to"
        )
    return names


def highlighted_languages() -> frozenset[str]:
    """Return every fence language the vendored highlighter covers."""
    global _declared  # noqa: PLW0603 - read once per process, see `_declared`
    if _declared is None:
        _declared = declared_languages(header_of(HIGHLIGHTER))
    return _declared


def falls_back(language: str) -> bool:
    """Whether a fence in `language` renders as the plain fallback."""
    return language.strip().lower() not in highlighted_languages()


def grammar_for(language: str) -> str:
    """Return the grammar name a fence in `language` asks the highlighter for."""
    name = language.strip().lower()
    return PLAIN if falls_back(name) else name
