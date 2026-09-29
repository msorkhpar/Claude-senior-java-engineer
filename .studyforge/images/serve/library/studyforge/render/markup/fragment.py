r"""How any renderer addresses a place inside a page — the one fragment composer.

**What it does.** Holds `FRAGMENT`, the character a fragment is introduced by,
and `anchor(key)`, the one function that turns a DOM id into the reference that
addresses it. Every renderer that links to a place on a page asks this module.

**How you use it.**

    from studyforge.render.markup import anchor

    anchor("s-practice-java")                  # '#s-practice-java'
    f"{index_href}{anchor(unit_key)}"          # a row on another page

**Depends on.** Nothing at all. ⛔ Not on `page`, not on `index`: both of them
compose fragments, and `render.index` imports `render.page`, so a composer on
either one is a composer the other cannot reach without a cycle.

## ⛔ Ask, never compose

⚠️ **A producer that spells `"#" + key` is a second definition of every anchor
it links to**, correct until the day one of them is escaped or prefixed and
silently wrong from then on. ⭐ This was two definitions — `render.index` and
`render.page.navigation` each held a `FRAGMENT = "#"` and said the other one was
the same character for the same reason — so both moved here, the sibling
package that exists for exactly the case of a name several renderers need.

⛔ **The composer does not escape.** A key is escaped where it is put into an
attribute, by the caller, exactly as every other href is; a composer that also
escaped would escape twice.
"""

from __future__ import annotations

#: How a fragment is introduced. ⛔ Named so `anchor` is the one composer, and
#: published so a reader of a page — a link check, not a producer — can split a
#: reference at the same character a producer joined it at.
FRAGMENT = "#"


def anchor(key: str) -> str:
    """Return the reference that addresses the element whose id is `key`.

    ⛔ **Ask, never compose.** A producer that spelled `"#" + key` would be a
    second definition of this framework's anchors.
    """
    return f"{FRAGMENT}{key}"
