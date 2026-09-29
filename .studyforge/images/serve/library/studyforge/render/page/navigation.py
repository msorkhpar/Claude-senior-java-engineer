r"""Where this page points — back, forward and up, and where the reader is.

**What it does.** Renders the trail that says where in the material the reader
is, and the bar that points at the previous unit, the next one and the index.

**How you use it.**

    from studyforge.render.page import navigation

    navigation.breadcrumb((navigation.Crumb("section", "Basics", "../i.html"),
                           navigation.Crumb("", "Your first class")))
    navigation.between_units(navigation.Links(previous=…, next=…, index=…))

**Depends on.** `render.templates` for the region wrappers and the link rows,
`render.pageassets` for the two hooks the trail's level word carries, and
`render.markup` for escaping and for the fragment a fallback slot hangs on the
index. ⛔ Not on `contents`: a trail and a bar arrive as **plain values a caller
built**, so this module imports nothing from the package that computes a reading
order and nothing from a peer renderer. ⛔ Not on `render.index` either, which
imports this package — hence `render.markup.anchor` rather than `index.anchor`
(R17).

## ⭐ Split from `page.anchors` at a seam that already existed

⚠️ One module answered four questions — anchors, the outline, the trail, the bar
— at eight lines under its ceiling. ⭐ *Where this page points* is this module;
*what may be linked to* is `page.anchors`.

## ⛔ Every region is CHROME, so a refused href DROPS

⭐ **The bar drops the whole slot** — a bar with a dead `next` is worse than one
with no `next`, and the reader still has the trail, the outline and the page.

⛔ **The trail drops the LINK and keeps the CRUMB**, and that is the same rule
rather than an exception to it. Its question is *"has the reader lost a WAY TO
GET SOMEWHERE, or has the page lost THE THING IT EXISTS TO SHOW?"* — and a crumb
is both at once: the **anchor** is the way somewhere, the **trail of labels** is
what the region exists to show. ⚠️ Dropping a crumb renumbers the hierarchy on
the page — *"Basics › Your first class"* where the material says *"Basics ›
Getting Started › Your first class"* is a true statement about a different
corpus, which is worse than an unlinked word.

## ⛔ A neighbour with NO PAGE is a declared type, not a guessed href

⚠️ **`Link(href=None)` is §7's third state**, spelled as
`render.index.Item(href=None)` spells it: *no page on this machine*. ⛔ A
renderer that cannot tell that from a **bad** href always picks the wrong one of
drop-or-raise, so the type is what makes drop-in-chrome applicable rather than a coin
toss. ⭐ And it degrades instead of dangling: that unit still has a row on the
root index anchored by its own key, so the slot points at `<the
index>#<the key>`, where the reader can see it is not built yet. ⛔ With no key,
or no index to hang the fragment on, nothing useful is left and the slot drops.
"""

from __future__ import annotations

from collections.abc import Sequence
from dataclasses import dataclass

from studyforge.render import templates
from studyforge.render.markup import anchor, escape, escape_attribute, inline, safe_href
from studyforge.render.pageassets import SURFACE_HOOKS

#: The markup of the two chrome regions this module renders. ⛔ **Files, not
#: f-strings** (R13): each wrapper carries a product string — the
#: `aria-label`s — and a product string typed in Python
#: is a sentence every corpus has to live with, in the one language nobody
#: thinks to look in when the page's wording is wrong. ⭐ The row bodies stay in
#: code, which is the line `render/page/__init__.py` draws: *"loop bodies and
#: inline wrappers stay in code, because a file for a closing tag removes no
#: duplication and adds a hop."*
BETWEEN_UNITS_TEMPLATE = "between-units.html"
BREADCRUMB_TEMPLATE = "breadcrumb.html"

#: What stands between two crumbs. ⛔ **A file for one glyph, which is R13 read
#: exactly as written**: `›` is a character a reader sees, chosen by this
#: framework, and a glyph in a loop body is a product string typed in Python.
#: ⚠️ Markup rather than a `::before` rule because the `file://` floor is the
#: baseline (R8) — a page opened with no stylesheet still reads as a trail.
SEPARATOR_TEMPLATE = "crumb-separator.html"

#: The three slots of the between-units bar, in emitted order: the `Links` field,
#: and the template its row is authored in. ⛔ A tuple, so the order is stated
#: rather than depending on iteration (R10).
#:
#: ⛔ **The `rel` and the arrows left Python here**
#: (R13). They were `("previous", "prev", "← ")` and a `" →"`
#: decided by an `== "next"` test inside `_link`. ⭐ Three files rather than one
#: with a `${lead}` slot: a template whose glyph arrives as a substitution has
#: not moved the glyph out of Python, only the markup around it.
LINK_SLOTS = (
    ("previous", "link-previous.html"),
    ("index", "link-index.html"),
    ("next", "link-next.html"),
)

#: The attribute the trail's level word is reached by, and the value saying it is
#: one. ⛔ Taken from the published surface, never typed: the surface made these the
#: one spelling after two renderers had each invented their own.
KIND_ATTRIBUTE = SURFACE_HOOKS["kind"]
LEVEL_KIND = SURFACE_HOOKS["level"]

#: What the trail says about the crumb the reader is already on. ⛔ Structure,
#: never wording: it names no language, and is not a word on the page.
CURRENT = ' aria-current="page"'

#: How many crumbs earn a trail. ⛔ One crumb is a list of one, which is the
#: same argument `outline` makes: chrome that says nothing at all.
BREADCRUMB_MINIMUM = 2


@dataclass(frozen=True, slots=True)
class Link:
    """One destination outside this page: a relative href and what to call it.

    ⛔ `href` is relative and is emitted verbatim after a scheme check, because
    only the study order knows the path arithmetic and this
    module must not invent it.

    ⭐ **`href=None` is a DECLARED absence** — *no page on this machine* — and
    `key` is the unit key its root-index row is anchored by, so the slot degrades
    to that row instead of dangling. ⛔ `None` rather than `""` for the reason
    `render.index.Item` gives: an empty string is an anchor that goes nowhere.
    """

    href: str | None
    label: str
    key: str = ""


@dataclass(frozen=True, slots=True)
class Links:
    """Where a unit page points when the reader has finished it.

    ⭐ Every field optional, and all three absent is a normal state:
    nothing computes a reading order before the study order does, and a page with no bar is a
    page that renders exactly as it will once one does — minus the bar.
    """

    previous: Link | None = None
    next: Link | None = None
    index: Link | None = None


@dataclass(frozen=True, slots=True)
class Crumb:
    """One step of the trail: what the corpus calls this depth, and what sits there.

    ⛔ `level` is the **corpus's own word** for the depth — `manifest.levels[d]`,
    carried through `contents.Group.level` — and never a number, which would be
    this framework's vocabulary in a corpus's own chrome (R1). ⭐ Empty for a step
    the corpus names with nothing, and for the unit itself, which is the page.

    ⚠️ `href` is how **this page** addresses that step, or `None` where there is
    no page for it — the ordinary case for a container, which is then listed
    rather than linked.
    """

    level: str
    title: str
    href: str | None = None


def breadcrumb(crumbs: Sequence[Crumb] | None) -> str:
    """Return where the reader is, or `''` when the trail would say nothing.

    ⛔ The **last** crumb is the page itself: it carries `aria-current="page"` and
    is never a link, whatever href it was handed, because a page that links to
    itself is a way to get nowhere. ⚠️ A trail of one is no trail — see
    `BREADCRUMB_MINIMUM`.
    """
    if crumbs is None or len(crumbs) < BREADCRUMB_MINIMUM:
        return ""
    last = len(crumbs) - 1
    rows = "".join(
        _crumb(
            crumb,
            lead="" if position == 0 else templates.fill(SEPARATOR_TEMPLATE),
            current=position == last,
        )
        for position, crumb in enumerate(crumbs)
    )
    return templates.fill(BREADCRUMB_TEMPLATE, crumbs=rows)


def _crumb(crumb: Crumb, *, lead: str, current: bool) -> str:
    """Return one step of the trail, linked where there is something to link to.

    ⛔ The crumb survives a refused or absent href and the **anchor** is what
    drops — see this module's reading of drop-in-chrome. ⚠️ Both branches emit the
    same words, so a trail is never short by a step and never silently renumbers
    the hierarchy.
    """
    body = f"{_level(crumb)}{inline(crumb.title)}"
    target = None if current or crumb.href is None else safe_href(crumb.href)
    row = body if target is None else f'<a href="{escape_attribute(target)}">{body}</a>'
    return f"<li{CURRENT if current else ''}>{lead}{row}</li>"


def _level(crumb: Crumb) -> str:
    """Return the corpus's own word for this depth and its trailing space, or `''`.

    ⚠️ Empty for a step the corpus names with nothing, and the space goes with it
    — a conditional separator left in the caller is a page that differs from its
    golden by one character on every such corpus. ⭐ Spelled exactly as
    `render.index.disclosure` spells it, from the same two published hooks, so
    the trail and the index say *"section"* the same way.
    """
    if not crumb.level:
        return ""
    return f'<span {KIND_ATTRIBUTE}="{LEVEL_KIND}">{escape(crumb.level)}</span> '


def between_units(links: Links | None) -> str:
    """Return the previous/index/next bar, or `''` when nothing is pointed at."""
    if links is None:
        return ""
    parts = [
        rendered
        for field, row in LINK_SLOTS
        if (rendered := _link(getattr(links, field), row, links.index))
    ]
    if not parts:
        return ""
    return templates.fill(BETWEEN_UNITS_TEMPLATE, links="".join(parts))


def _link(link: Link | None, row: str, index: Link | None) -> str:
    """Return one slot of the bar, or `''` when there is nothing useful to point at.

    ⚠️ A refused scheme drops the slot rather than rendering dead text: this is
    chrome, and chrome that cannot be followed is worse than chrome that is not
    there. ⭐ A **declared** absence is not a refusal — it falls back to the
    unit's own row on the root index, and drops only when there is no key or no
    index to address that row from.
    """
    if link is None:
        return ""
    target = _destination(link, index)
    if target is None:
        return ""
    return templates.fill(row, href=escape_attribute(target), label=escape(link.label))


def _destination(link: Link, index: Link | None) -> str | None:
    """Return where this slot actually points, or `None` when nowhere useful does.

    ⛔ **The fallback is gated as ONE string, after composing.** A key is a
    corpus's own text; gating only the index's half would let a key no href can
    be spelled with reach the page, which is the half of the dropped-href lesson that was
    about the *set* rather than about the prefixes in it.
    """
    if link.href is not None:
        return safe_href(link.href)
    if not link.key or index is None or index.href is None:
        return None
    return safe_href(f"{index.href}{anchor(link.key)}")
