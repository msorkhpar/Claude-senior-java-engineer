r"""The blocks a reader reads — heading, para, list, table, rule, quote, disclosure.

**What it does.** Renders the seven block types that are prose or hold prose.
⛔ **Every one of them escapes its text**, and there is no path through this
module on which a block's own characters reach the page as markup.

**How you use it.** `RENDERS` names the types this module answers for and
`render(block, position, …)` renders one; both are read by the dispatcher, and
nothing else calls this module directly.

**Depends on.** `render.markup` for escaping and the inline markers, `page.anchors`
for a heading's anchor, and `render.pageassets` for the class names. ⛔ Not on
`page.assets`: nothing here addresses a file.

## ⛔ Not one class name is typed in this module

⚠️ Every class comes from `pageassets.class_for` or `SURFACE_HOOKS`. ⭐ **That
makes the two-sided markup contract structural rather than checked:** this
module *cannot* invent a name the stylesheet does not target, because it does
not spell one. The failure that would otherwise be silent — a page that renders,
carries every word, and is unstyled — is unrepresentable here.

## ⛔ A container's children are rendered by the dispatcher, not here

⚠️ `quote` and `disclosure` receive their inner markup as `children`. ⭐ The one
recursion over the vocabulary lives in the dispatcher, which walks
`CONTAINER_TYPES` — the same argument `archive.blocks.walk` makes: *a consumer
that names `quote` itself is the next `disclosure` waiting to be forgotten.*

## ⚠️ `disclosure` is present-but-withheld, and `open` is the author's

⛔ Nothing here may show a disclosure by default. Every surveyed use of
`<details>` in real material hides an **exercise answer**, so the archive's own
`open` is carried through and never defaulted to true.
"""

from __future__ import annotations

from studyforge.archive.blocks import BLOCK_TYPES, item_parts, list_start
from studyforge.render.markup import escape, escape_attribute, inline
from studyforge.render.page.anchors import block_anchor, heading_level
from studyforge.render.page.blocks import figure
from studyforge.render.page.narration import SILENT, Narration
from studyforge.render.pageassets import SURFACE_HOOKS, class_for


def render(
    block: dict,
    position: int,
    *,
    placement: object = None,
    section: str = "",
    children: str = "",
    path: tuple[int, ...] = (),
    narration: Narration = SILENT,
) -> str:
    """Render one prose block. ⛔ `placement` is unused and is part of the shape.

    ⚠️ Every renderer in this package takes the same arguments, so the
    dispatcher holds one call and no branch on which renderer wants what. A
    renderer that quietly took fewer would be the one that has to be special-
    cased the day it needs one more.
    """
    del placement
    return _RENDERERS[block["type"]](block, position, section, children, path, narration)


def _heading(block, position, section, children, path, narration) -> str:
    """One heading, carrying the anchor the outline and any inbound link use."""
    del children
    level = heading_level(block)
    anchor = escape_attribute(block_anchor(section, position))
    audio = narration.attribute(section, path)
    return f'<h{level} id="{anchor}"{audio}>{inline(block.get("text"))}</h{level}>'


def _para(block, position, section, children, path, narration) -> str:
    """Return one paragraph, escaped.

    ⛔ See this package's `verbatim` module for the one block type that is not,
    and for why the difference cannot be read off the text.
    """
    del position, children
    return f"<p{narration.attribute(section, path)}>{inline(block.get('text'))}</p>"


def _rule(block, position, section, children, path, narration) -> str:
    """Return a thematic break: the element, and nothing else.

    ⛔ Never narrated — `SPEECH_OF` calls a rule `silent`, and a horizontal line
    has no words to say.
    """
    del block, position, section, children, path, narration
    return "<hr>"


def _listing(block, position, section, children, path, narration) -> str:
    """One list, ordered or not, its items rendered as inline prose.

    ⚠️ **Narrated per ITEM, not per list.** `SPEECH_OF` calls a list `items`, so
    each `<li>` is its own speech unit with its own clip — which is what lets the
    highlight sit on the line being read rather than over the whole list.
    """
    del position, children
    items = "".join(
        f"<li{narration.attribute(section, path, index)}>{_item(item)}</li>"
        for index, item in enumerate(block.get("items") or ())
    )
    return _list_element(block, items)


def _list_element(block: dict, items: str) -> str:
    """Wrap rendered items in the list's own tag and the published class."""
    tag = "ol" if block.get("ordered") else "ul"
    klass = escape_attribute(class_for("list"))
    # ⛔ The number the author started at, and nothing when it is one.
    start = list_start(block) if tag == "ol" else 1
    first = f' start="{start}"' if start != 1 else ""
    return f'<{tag} class="{klass}"{first}>{items}</{tag}>'


def _item(item: object) -> str:
    """One item's parts in reading order: text as inline prose, a block as itself.

    ⛔ **A nested list is rendered as a nested list, and a code block as the
    same figure a top-level fence gets**, never as its parent's text. ⚠️ Neither
    carries an **audio attribute**: a part is spoken inside its parent item's
    clip (`narrate.speakable.script`), so the highlight sits on the parent
    `<li>`, which holds it.
    """
    return "".join(_part(part) for part in item_parts(item))


def _part(part: object) -> str:
    """One part of an item: text, a nested list, or a code figure."""
    if not isinstance(part, dict):
        return inline(part)
    if part.get("type") == "code":
        return figure.render(part, 0)
    return _list_element(part, "".join(f"<li>{_item(sub)}</li>" for sub in part.get("items") or ()))


def _table(block, position, section, children, path, narration) -> str:
    """One table, inside its own scrolling box.

    ⛔ The wrapper is not decoration: a table wider than the reading column must
    scroll inside its own box, or the page scrolls sideways and every paragraph
    goes with it.

    ⚠️ **Narrated per body ROW** (`SPEECH_OF` says `rows`), and the header row is
    not one: a `<tr>` inside `<thead>` gets no speech id from the walker, so it
    must carry no attribute here — a header that linked a clip would light up
    under audio that is not reading it.
    """
    del position, children
    klass = escape_attribute(SURFACE_HOOKS["table_scroll"])
    parts = [f'<div class="{klass}"><table>']
    headers = block.get("headers") or ()
    if headers:
        cells = "".join(f"<th>{inline(header)}</th>" for header in headers)
        parts.append(f"<thead><tr>{cells}</tr></thead>")
    parts.append("<tbody>")
    for index, row in enumerate(block.get("rows") or ()):
        cells = "".join(f"<td>{inline(cell)}</td>" for cell in row)
        parts.append(f"<tr{narration.attribute(section, path, index)}>{cells}</tr>")
    parts.append("</tbody></table></div>")
    return "".join(parts)


def _quote(block, position, section, children, path, narration) -> str:
    """Return a quotation, holding whatever blocks it holds.

    ⚠️ A container, not a paragraph in italics: a list or a code block inside a
    quote must still read as itself.

    ⛔ **The quote itself is never narrated** — `SPEECH_OF` calls it `recurse`,
    so the clips belong to the blocks inside it, which the dispatcher has already
    rendered against this block's own path.
    """
    del block, position, section, path, narration
    return f"<blockquote>{children}</blockquote>"


def _disclosure(block, position, section, children, path, narration) -> str:
    """Content the archive records as shown on demand — a real `<details>`.

    ⭐ It opens, closes and takes keyboard focus with scripting off entirely,
    which is the third state between shown and absent. ⛔ `open` is the
    author's, delivered from the archive, and is never defaulted here.

    ⛔ **The clip belongs to the `<summary>`, never to the `<details>`.**
    Narration speaks a disclosure's summary and stops; the body gets
    no speech id at all, so a `<details>` carrying the attribute would put the
    highlight over content the audio deliberately withheld — on a surface the
    reader chose not to open.
    """
    del position
    klass = escape_attribute(class_for("disclosure"))
    opened = " open" if block.get("open") else ""
    summary = escape(block.get("summary") or "")
    audio = narration.attribute(section, path)
    return (
        f'<details class="{klass}"{opened}><summary{audio}>{summary}</summary>{children}</details>'
    )


#: `block type -> the function that renders it`. ⛔ Named functions rather than
#: a chain of `if`s, so what this module answers for is a mapping a diff can
#: read — and `RENDERS` below is derived from it, so the two cannot drift.
_RENDERERS = {
    "heading": _heading,
    "para": _para,
    "list": _listing,
    "table": _table,
    "rule": _rule,
    "quote": _quote,
    "disclosure": _disclosure,
}

#: The block types this module answers for, in the vocabulary's own order.
#: ⛔ **Derived from `BLOCK_TYPES`, never typed a second time.** Seven names
#: written twice in one module is the fifth copy of the block vocabulary this
#: project has now removed four of, and `test_blocks` fails a module that spells
#: four of them without saying where it got them.
RENDERS = tuple(name for name in BLOCK_TYPES if name in _RENDERERS)
