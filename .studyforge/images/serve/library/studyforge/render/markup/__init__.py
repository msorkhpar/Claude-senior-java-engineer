r"""How any renderer turns a string into safe page markup — escaping, the href gate, anchors.

**What it does.** Holds the framework's one escaping routine, its one
attribute-escaping routine, its one permitted-href decision, its one inline
marker reader and its one fragment composer, so that every renderer under
`render/` asks the same function the same question and gets the same answer.

**How you use it.**

    from studyforge.render.markup import anchor, escape, escape_attribute, inline, safe_href

    escape("a < b")                 # 'a &lt; b'
    escape_attribute(value)         # additionally neutralises "'"
    inline("see `x` and **y**")     # the four markers become markup
    safe_href("javascript:x")       # None — render the words, not a link
    anchor("s-java")                # '#s-java' — ask, never compose

**Depends on.** `re`, and nothing else in this framework. ⛔ Not on `page`, not
on `container`, not on `pageassets`: this package is what they all reach for, so
an edge back into any of them would be a cycle waiting for the next renderer.

## ⛔ Why this is a sibling of the renderers rather than a name on one of them

⚠️ **`escape`, `escape_attribute`, `inline` and `safe_href` lived on
`render.page`'s private surface and were imported past it by `render.container`
anyway** — ⛔ **against `render/page/__init__.py`'s own sentence**,
which says a consumer that has to import a submodule directly is a consumer that
contract failed.

⭐ **Publishing them from `render.page` was the other candidate and it was
refused on a count:** `render.page` is *the unit page*, `render.container` is a
peer, and `render/index/` is a third peer that shares the unit page's
**palette** with it and nothing else. ⛔ Three peers reaching into one of
themselves for the escaping gate makes the unit page the base of a layer it is
not the base of. ⭐ **The repository already answers this question once —
`render.pageassets` is the sibling package `page` and `container` both take the
class names and asset filenames from** — and the escaping gate was the one
shared primitive that was not shaped that way.

## ⛔ One escaper, one gate, and that is the whole claim

⚠️ **A second escaper is the failure this package exists to make impossible.**
Two of them agree on the day they are written and disagree the day one learns
about `'`; the page still renders, still carries every word, and the difference
is an injection. ⭐ So `text` is the only module under `src/studyforge` that
defines these names, and `test_init.py` asserts that over the whole tree rather
than promising it.

## What sits where

| module | the question it answers |
|---|---|
| `text` | how does a string become safe page text — escaped, gated, inlined? |
| `fragment` | how does a renderer address a place inside a page? |

⭐ **`fragment` is here for the reason `text` is**:
`render.index` and `render.page` both compose anchors, `render.index` imports
`render.page`, and a composer on either one is a composer the other cannot reach
without a cycle. ⛔ `__init__.py` is the contract a consumer reads (R17), and a
declared `__all__` is what one exported home (R21) asks a cross-package import about.
"""

from __future__ import annotations

from studyforge.render.markup.fragment import FRAGMENT, anchor
from studyforge.render.markup.text import (
    SAFE_SCHEMES,
    SEGMENT_KINDS,
    escape,
    escape_attribute,
    inline,
    safe_href,
    segments,
)

#: ⛔ The package's whole public surface. A consumer that has to import
#: a submodule of this package directly is a consumer this contract failed.
__all__ = [
    "FRAGMENT",
    "SAFE_SCHEMES",
    "SEGMENT_KINDS",
    "anchor",
    "escape",
    "escape_attribute",
    "inline",
    "safe_href",
    "segments",
]
