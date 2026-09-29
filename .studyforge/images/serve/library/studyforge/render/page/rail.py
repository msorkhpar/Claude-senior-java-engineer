r"""Where else the reader could go: every container of the corpus, on every page.

**What it does.** Renders the region that reaches the material a reader is *not*
in — every container the corpus declares, each holding its own units — and says
which container and which unit the reader is on.

**How you use it.**

    from studyforge.render.page import RailContainer, RailUnit, rail

    rail((RailContainer("Getting started", level="section", href="a.section.html",
                        current=True,
                        units=(RailUnit("Your first class", href="u1.unit.html"),)),
          RailContainer("Going further", href="b.section.html")))

**Depends on.** `render.templates` for the region wrapper, `render.markup` for
escaping and the href gate, and `render.pageassets` for the two hooks a row
carries. ⛔ Not on `contents`, and not on any peer renderer: a rail arrives as
**plain values a caller built**, exactly as a trail and a bar do.

## ⛔ Why this region exists — without it the route between two courses is the root index

⚠️ **A unit page's other three `<nav>` regions never reach a page in another
container.** The bar
points at the previous and next units *inside one container*; the trail points
up at that container and at the root index; the outline points into the page
itself. ⭐ So without the rail the only way from one course to another is back
through the root index — two clicks and a lost place, on every crossing.

## ⛔ It degrades to NOTHING below two containers, and that is asserted

⭐ **A rail listing one course is a list with no choice in it** — the reader is
already in the only container there is, and the trail and the bar already say
so. ⚠️ `RAIL_MINIMUM` is the same argument `BREADCRUMB_MINIMUM` makes one module
along: chrome that cannot say anything is chrome that is not emitted.

## ⛔ Real disclosure elements, because the floor is a double-clicked file (R8)

⭐ **`<details>` opens, closes and takes keyboard focus with scripting off
entirely**, so every unit of every container is reachable over `file://` with no
script anywhere. ⚠️ A rail that rendered only the current container's units, or
that revealed the rest with a click handler, would be a rail whose whole subject
— *getting to the other course* — depended on the one thing R8's floor does not
have. ⛔ `render.index.disclosure` makes this argument first and this module
follows it rather than restating it.

## ⛔ This region is CHROME, so a refused href drops the LINK

⚠️ **The row survives and the anchor does not**, which is the trail's reading
rather than the two lists' — and for the trail's reason. ⭐ A container or a
unit dropped out of this list is a **true statement about a different corpus**:
the reader counts the courses, finds one fewer than the material has, and
nothing anywhere says so. ⛔ A row with no anchor is visibly not openable and
costs the reader nothing but a trip through the root index, which is where they
were before this region existed.

## ⛔ Not one `id` is emitted here — a unit row carries its key as `data-unit`

⚠️ **A container page already carries `id="<unit key>"` on every row of its own
listing** (`render.container.listing`), and `read-mark.js` resolves a mark with
`document.getElementById(key)`. ⭐ A rail that keyed its rows the same way would
put two elements under one id on that page, and the mark would light whichever
one the document happened to hold first. ⛔ So the rail is not deep-linkable.

⭐ **It still shows what the reader marked**: a unit row carries its key
in `data-unit` — the attribute the read-mark control already carries, which is a
**script** hook and so is spelled here rather than published
(`render.page.mark` says why) — and `progress-view.js` sets the published
`data-marked` on the rows the store holds. ⛔ The key arrives from the caller,
never derived from an href or a position here: a row handed none carries none.
⚠️ The mark itself is set at read time and emitted by nothing, so a built page
is byte-identical whoever opens it (R10).

⭐ **And a screen reader is told**: a keyed row carries, inside what it
links, the words `templates/read-state.html` holds — the same file both lists
fill — emitted `hidden` and shown by `progress-view.js` only on a row the store
holds. ⛔ The tick stays decorative CSS and the words stay off the screen, so
the visual mark is exactly what it was.

## ⛔ Not one class name is typed here either

⚠️ Every hook is an element, an `aria-label` or a `data-*` attribute — taken
from `pageassets.SURFACE_HOOKS`, never spelled — so this region costs no entry
in the published class vocabulary, exactly as the two lists it sits beside.
"""

from __future__ import annotations

from collections.abc import Sequence
from dataclasses import dataclass

from studyforge.render import templates
from studyforge.render.markup import escape, escape_attribute, inline, safe_href
from studyforge.render.pageassets import SURFACE_HOOKS

#: The region's wrapper. ⛔ A file, not an f-string (R13): it carries
#: the `aria-label`, which is a product string every corpus has to live with.
RAIL_TEMPLATE = "rail.html"

#: The attribute that says whether a row can be opened from here. ⛔ Taken from
#: the contract, never typed: `render.container.listing` and
#: `render.index.disclosure` say the same thing about the same rows.
READABLE_ATTRIBUTE = SURFACE_HOOKS["readable"]

#: The attribute a reader-facing label's kind is carried in. ⚠️ Overloaded on
#: purpose — `templates/section.html` carries a corpus's own section kind in it
#: — which is why a stylesheet rule for a kind names the element as well.
KIND_ATTRIBUTE = SURFACE_HOOKS["kind"]

#: What wraps a unit's reader-facing ordinal, and what wraps the corpus's own
#: word for a container's depth.
NUMBERING_KIND = SURFACE_HOOKS["numbering"]
LEVEL_KIND = SURFACE_HOOKS["level"]

#: The words a read row says to assistive technology, and the kind that wraps
#: them. ⛔ A file, not a string here (R13): both lists fill the same one.
READ_STATE_TEMPLATE = "read-state.html"
READ_STATE_KIND = SURFACE_HOOKS["read_state"]

#: What the region says about the container the reader is inside, and about the
#: unit they are reading. ⛔ Structure, never wording: neither names a language,
#: and neither is a word on the page. ⚠️ `page` is the unit, because that is
#: what the reader is on; the container is `true`, because the page they are on
#: is *within* it and not it.
CURRENT_CONTAINER = ' aria-current="true"'
CURRENT_UNIT = ' aria-current="page"'

#: What the current container's disclosure carries so its units are already
#: visible. ⛔ The others render closed: a reader crossing to another course
#: opens one summary, and a reader staying in this one is shown where they are.
OPEN = " open"

#: The attribute a unit row carries its key in. ⛔ A script hook, the same
#: spelling `render/templates/read-mark.html` emits and `progress-view.js`
#: reads; not in `SURFACE_HOOKS`, because no stylesheet rule targets a key.
UNIT_ATTRIBUTE = "data-unit"

#: How many containers earn a rail — see this module's docstring.
RAIL_MINIMUM = 2


@dataclass(frozen=True, slots=True)
class RailUnit:
    """One unit as the rail lists it: what it is called and how to open it.

    ⛔ `href=None` is §7's third state — *no page on this machine* — and is not
    the same as `current`, which is *this is the page you are reading*. Both
    render without an anchor and the two say opposite things about whether the
    unit can be read, so the row declares which.

    ⭐ `key` is the unit's key, which is what a read mark is stored under; `''`
    emits no key and the row can then never show a mark.
    """

    title: str
    numbering: str = ""
    href: str | None = None
    current: bool = False
    key: str = ""


@dataclass(frozen=True, slots=True)
class RailGroup:
    """A level ABOVE the containers, as the rail groups them under it.

    ⭐ `key` is what tells two groups apart — its address key — because two
    sections may share a title; `level` is the corpus's own word for its depth.
    """

    title: str
    level: str = ""
    key: str = ""


@dataclass(frozen=True, slots=True)
class RailContainer:
    """One container as the rail lists it, with the units declared under it.

    ⛔ `level` is the **corpus's own word** for this depth — `manifest.levels[-1]`,
    carried through `contents.Group.level` — and never a number, which would be
    this framework's vocabulary in a corpus's own chrome (R1).
    """

    title: str
    level: str = ""
    href: str | None = None
    current: bool = False
    units: tuple[RailUnit, ...] = ()
    #: ⭐ The groups this container sits in, outermost first; `()` at depth 1.
    within: tuple[RailGroup, ...] = ()


def render(containers: Sequence[RailContainer] | None) -> str:
    """Return the rail, or `''` when there is no crossing for it to offer.

    ⛔ Below `RAIL_MINIMUM` containers the region is not emitted at all: see
    this module's docstring for why one course is not a rail.
    """
    if containers is None or len(containers) < RAIL_MINIMUM:
        return ""
    said = templates.fill(READ_STATE_TEMPLATE, kind=READ_STATE_KIND)
    return templates.fill(RAIL_TEMPLATE, containers=_rows(tuple(containers), 0, said))


def _rows(containers: tuple[RailContainer, ...], depth: int, said: str) -> str:
    """Return the rows at one depth: each group holding its containers, or a container.

    ⛔ **Sections group their modules.** A corpus filed in sections lists its
    containers under the section they sit in, the way its index does, rather
    than as one flat run of every module. ⭐ A container with no group at this
    depth is its own row, so a depth-1 rail is one flat list of containers.
    """
    rows: list[str] = []
    index = 0
    while index < len(containers):
        container = containers[index]
        if len(container.within) <= depth:
            rows.append(_container(container, said))
            index += 1
            continue
        group = container.within[depth]
        end = index
        while (
            end < len(containers)
            and len(containers[end].within) > depth
            and containers[end].within[depth].key == group.key
        ):
            end += 1
        rows.append(_group(group, containers[index:end], depth, said))
        index = end
    return "".join(rows)


def _group(group: RailGroup, containers: tuple[RailContainer, ...], depth: int, said: str) -> str:
    """Return one group as a disclosure holding its containers, open when it holds the reader.

    ⚠️ **No link, no readable attribute and no `aria-current`**: no page is
    written above a container, so the summary is a label and nothing else, and
    the container the reader is in stays the one row that says so.
    """
    current = any(container.current for container in containers)
    summary = _body(_level(group), group.title)
    return (
        f"<li><details{OPEN if current else ''}><summary>{summary}</summary>"
        f"<ol>{_rows(containers, depth + 1, said)}</ol></details></li>"
    )


def _container(container: RailContainer, said: str) -> str:
    """Return one container as a disclosure holding the units under it."""
    summary = _body(_level(container), container.title)
    units = "".join(_unit(unit, said) for unit in container.units)
    return (
        f"<li{CURRENT_CONTAINER if container.current else ''} "
        f"{_readable(container.href, container.current)}>"
        f"<details{OPEN if container.current else ''}>"
        f"<summary>{_link(summary, container.href, container.current)}</summary>"
        f"<ol>{units}</ol></details></li>"
    )


def _unit(unit: RailUnit, said: str) -> str:
    """Return one unit's row, linked unless it is the page the reader is on.

    ⭐ A keyed row ends with `said`, the hidden words a read row speaks; a row
    with no key can never be marked, so it carries none.
    """
    body = _body(_numbering(unit), unit.title) + (said if unit.key else "")
    return (
        f"<li{CURRENT_UNIT if unit.current else ''}{_key(unit.key)} "
        f"{_readable(unit.href, unit.current)}>"
        f"{_link(body, unit.href, unit.current)}</li>"
    )


def _link(body: str, href: str | None, current: bool) -> str:
    """Return `body` wrapped in an anchor, or `body` alone when there is nowhere to go.

    ⛔ The row survives a refused or absent href and the **anchor** is what drops
    — see this module's reading of drop-in-chrome. ⚠️ A current row is never linked
    whatever href it was handed, because a page that links to itself is a way to
    get nowhere; `page.navigation.breadcrumb` refuses the same link for the same
    reason.
    """
    target = None if current or href is None else safe_href(href)
    if target is None:
        return body
    return f'<a href="{escape_attribute(target)}">{body}</a>'


def _key(key: str) -> str:
    """Return the attribute carrying a unit's key and its leading space, or `''`."""
    if not key:
        return ""
    return f' {UNIT_ATTRIBUTE}="{escape_attribute(key)}"'


def _readable(href: str | None, current: bool) -> str:
    """Return the attribute saying whether this row can be opened from here.

    ⭐ **It reports what the row EMITTED, not what it was handed.** A declared
    absence and a refused href both leave a row nobody can click, and a reader
    who cannot see colour is told so by the same rule either way. ⛔ The current
    row is `true` and carries no anchor: it is the most readable row on the page,
    and marking it *"listed, not openable"* would be a false sentence rendered
    in italics.
    """
    openable = current or (href is not None and safe_href(href) is not None)
    return f'{READABLE_ATTRIBUTE}="{"true" if openable else "false"}"'


def _body(chip: str, title: str) -> str:
    """Return a row's visible text: its chip, if it has one, then its title."""
    return f"{chip}{inline(title)}"


def _level(container: RailContainer | RailGroup) -> str:
    """Return the corpus's own word for this depth and its trailing space, or `''`.

    ⚠️ Empty for a corpus that names its levels with nothing, and the space goes
    with it — a conditional separator left in the caller is a page that differs
    from its golden by one character on every such corpus. ⭐ Spelled from the
    same two published hooks as the trail's and the index's, so all three say
    *"section"* the same way.
    """
    if not container.level:
        return ""
    return f'<span {KIND_ATTRIBUTE}="{LEVEL_KIND}">{escape(container.level)}</span> '


def _numbering(unit: RailUnit) -> str:
    """Return the reader-facing numbering and its trailing space, or `''`."""
    if not unit.numbering:
        return ""
    return f'<span {KIND_ATTRIBUTE}="{NUMBERING_KIND}">{escape(unit.numbering)}</span> '
