r"""The list of units a container page exists to show, and the one gate on it.

**What it does.** Renders a container's units, in the order the container
declared them, as an ordered list — one row per unit, linked when the unit has a
page and plainly listed when it has not.

**How you use it.** `listing.render(document.address, document.items)` returns
the markup; `container.document` puts it in the page's body.

**Depends on.** `studyforge.address` for the one key composer, `render.markup`
for escaping and the href gate, `entries`, and `render.page` for `PageError`.
⛔ Nothing that knows where a file is: an `Item` arrives with its href already
answered.

## ⛔ A row carries the unit key, and that is what a read mark joins on

⭐ **`Address.unit_key` is the one composer**, and its own docstring says why:
*"the page writes a read mark under it, the index reads the mark back … a second
spelling that differed by one character would simply never match anything, with
nothing failing anywhere."* ⚠️ So the key is asked for rather than spelled, and
the container's declared order **is** the ordinal order — the container reader
has already refused any map whose ordinals are not contiguous from 1, which is
the same invariant `render` relies on to list the units at all.

⛔ **It is the row's `id`, not a second attribute, because `render.index.
disclosure` already keys its rows that way** and the two pages must be joinable
by one question. ⭐ A row that can be addressed is also a row that can be
deep-linked, which this page could not offer before and the root index always
could.

⚠️ **The address arrives as an argument rather than being read off an `Item`.**
An `Item` carries what a reader sees; a key is the corpus's own address, which
belongs to the container and not to each row — and a per-row copy is a second
place for it to disagree with the ordinal the row was listed under.

## ⛔ A refused href RAISES here, and that is deliberate

⚠️ **`page.navigation._link` drops a slot whose scheme is refused**, on the
argument that chrome which cannot be followed is worse than chrome that is not
there. ⭐ **That argument is right for the bar and wrong for this list**: the
bar is chrome and this list is the page. ⛔ A silently dropped anchor here turns
a module's contents into unclickable text — the reader sees every title, every
row is present, `validate` passes, nothing logs, and not one unit can be opened.

⚠️ **Spelled out rather than assumed.** A bar can lose slots to a wrong refusal
while its acceptance clause still passes. ⭐ So **the emitter and the gate must
agree about what a refusal means**, and a page whose whole content is links
cannot answer *"drop it"*.

## ⛔ Markup on one line, in code, and not in a template (R13)

⭐ `render/page/__init__.py` draws the line and this is on its far side: *"loop
bodies and inline wrappers stay in code, because a file for a closing tag
removes no duplication and adds a hop."* ⚠️ `anchors.outline` renders exactly
this shape — a `<nav>`, an `<ol>`, a row per entry — the same way.

## ⭐ Not one class name is typed here either

⚠️ Every hook is an element, an `aria-label` or a `data-*` attribute, which is
what lets the stylesheet change independently of the markup it styles. ⛔ A class
would cost an entry in `SURFACE_HOOKS` and one in `chrome.css`, in two packages
this one does not own — and a class name with no rule is not styling.

⭐ **And the hooks themselves are now TAKEN from `pageassets.SURFACE_HOOKS`
rather than spelled here** (R13). ⚠️ The spelling
was identical in this module and in `render.index.disclosure` and meant the same
thing in both — which is agreement by coincidence, and the day one page gains a
third state the two part company with nothing to notice.
"""

from __future__ import annotations

from studyforge.address import Address
from studyforge.render import templates
from studyforge.render.container.entries import Item
from studyforge.render.markup import escape, escape_attribute, inline, safe_href
from studyforge.render.page import PageError
from studyforge.render.pageassets import SURFACE_HOOKS

#: What the list is labelled for a reader who cannot see it. ⛔ This framework's
#: own structural word, never a corpus's: every string on the page that names
#: the *material* comes out of the document (R1).
LIST_LABEL = "Units"

#: The attribute that says whether a row could be linked. ⚠️ `data-*` rather
#: than a class, so this page needs no entry in a published class set — see the
#: module docstring. ⛔ **Taken from the contract, never typed**:
#: `render.index.disclosure` says the same thing about the same rows, and
#: the stylesheet writes one rule for both.
READABLE_ATTRIBUTE = SURFACE_HOOKS["readable"]

#: The attribute a reader-facing label's kind is carried in. ⚠️ Overloaded on
#: purpose — `templates/section.html` carries a section's own kind in it — which
#: is why a stylesheet rule for a kind names the element as well.
KIND_ATTRIBUTE = SURFACE_HOOKS["kind"]

#: What wraps a unit's numbering, so a stylesheet can reach it without the
#: numbering being glued to the title in one string.
NUMBERING_KIND = SURFACE_HOOKS["numbering"]

#: The words a read row says to assistive technology, and the kind that wraps
#: them. ⛔ One file for the rail and both lists (R13): emitted hidden,
#: shown by `progress-view.js` on a row the store holds, never drawn on screen.
READ_STATE_TEMPLATE = "read-state.html"
READ_STATE_KIND = SURFACE_HOOKS["read_state"]


def render(address: Address, items: tuple[Item, ...]) -> str:
    """Return the container's units as one ordered list, in declared order.

    ⛔ **The declared order is used, never re-derived.** The container reader
    has already refused any map whose ordinals are not contiguous from 1, so the
    declared order *is* the ordinal order; a renderer that sorted would be the
    second orderer `contents` was written to prevent.

    ⭐ **Which is also what makes each row's key derivable here**: the position a
    unit is listed at is its ordinal, and `address.unit_key` turns the pair into
    the one string every surface joins on.
    """
    rows = "".join(_row(address, position, item) for position, item in enumerate(items, start=1))
    return f'<nav aria-label="{LIST_LABEL}"><ol>{rows}</ol></nav>'


def _row(address: Address, position: int, item: Item) -> str:
    """Return one unit's row: linked when it has a page, plain when it has not."""
    body = f"{_numbering(item)}{inline(item.title)}{_read_state()}"
    where = f'id="{escape_attribute(address.unit_key(position))}"'
    if item.href is None:
        return f'<li {where} {READABLE_ATTRIBUTE}="false">{body}</li>'
    target = safe_href(item.href)
    if target is None:
        # ⛔ The href is DESCRIBED by its position and never reproduced (R7).
        # This branch fires precisely because the value is not a permitted
        # relative reference — which is the branch an absolute path and a
        # rooted href both arrive at — and it runs over every unit in a corpus,
        # into a build log. The position is what tells the author where to look.
        raise PageError(
            f"the unit at position {position} of this container carries a link that is "
            f"neither a permitted scheme nor a relative reference inside the site; it "
            f"is refused rather than dropped, because on this page the links are the "
            f"content and a dropped one is a row nobody can open"
        )
    return (
        f'<li {where} {READABLE_ATTRIBUTE}="true">'
        f'<a href="{escape_attribute(target)}">{body}</a></li>'
    )


def _numbering(item: Item) -> str:
    """Return the reader-facing numbering and its trailing space, or `''`.

    ⚠️ Empty for material that numbers nothing, and the space goes with it —
    a conditional separator left in the caller is a page that differs from its
    golden by one character on every unnumbered corpus.
    """
    if not item.numbering:
        return ""
    return f'<span {KIND_ATTRIBUTE}="{NUMBERING_KIND}">{escape(item.numbering)}</span> '


def _read_state() -> str:
    """Return the hidden words a read row speaks, from the one template all three regions fill."""
    return templates.fill(READ_STATE_TEMPLATE, kind=READ_STATE_KIND)
