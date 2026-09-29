r"""The page's colours, read out of the page's own stylesheets for the practice editor.

**What it does.** Reads the three stylesheets that paint a code block —
`palette.css` for each custom property in each theme, `code-highlight.css` for
which property each Prism token class is painted with (and its weight and
slant), `reading.css` for the block's own ground and ink — and spells a colour
the way the workbench takes one.

**How you use it.** `palette()`, `highlight()` and `code_block()` are the three
readings; `property_of(value)` names the palette property a `var()` value is,
and `colour(value, alpha)` turns a palette value into `#rrggbb` or `#rrggbbaa`.
`editor_theme` is the one caller.

**Depends on.** `re` and `studyforge.render.pageassets.text`, which reads a page
asset exactly. Raises `EditorColoursUnread`.

⛔ **Read, never copied, and read at the moment it is asked for**: the editor's
colours are these stylesheets' colours, so a palette change reaches the page
and the editor at once. ⚠️ **Split on the rule blocks, not parsed as CSS** —
`test_palette` splits the same file the same way, for the same reason: the
point is what the FILE declares under each selector, and nothing here resolves
a cascade except the one `code-highlight.css` documents, its last rule winning.
"""

from __future__ import annotations

import re

from studyforge.describe import describe
from studyforge.render.pageassets import AssetError, text

#: The three stylesheets the colours are read from.
PALETTE = "palette.css"
HIGHLIGHT = "code-highlight.css"
READING = "reading.css"

#: The selector each theme's custom properties are declared under in `PALETTE`.
#: ⭐ Dark is the SYSTEM-keyed block; `test_palette` holds the reader-chosen
#: `[data-theme="dark"]` block equal to it.
THEME_SELECTORS = {"light": ":root", "dark": ':root:not([data-theme="light"])'}

#: The code block's ground and ink, as `READING` paints them.
GROUND = ("figure.code", "background")
INK = ("figure.code code", "color")

#: How `HIGHLIGHT` names a token class.
TOKEN_SELECTOR = re.compile(r"^figure\.code \.token\.([\w-]+)$")


_COMMENT = re.compile(r"/\*.*?\*/", re.DOTALL)
_RULE = re.compile(r"([^{}]+)\{([^{}]*)\}")
_DECLARATION = re.compile(r"([\w-]+)\s*:\s*([^;]+);")
_VAR = re.compile(r"^var\(\s*(--[\w-]+)\s*\)$")
_HEX = re.compile(r"^(#)([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$")


class EditorColoursUnread(Exception):
    """The page's stylesheets could not be read as the editor's colours, saying which and why."""


def rules(name: str) -> list[tuple[str, dict[str, str]]]:
    """Return `(selector, declarations)` for every innermost rule of one stylesheet, in order."""
    try:
        body = _COMMENT.sub("", text(name))
    except AssetError as error:
        raise EditorColoursUnread(str(error)) from None
    found = []
    for selectors, block in _RULE.findall(body):
        declared = {key: value.strip() for key, value in _DECLARATION.findall(block + ";")}
        found.extend((selector.strip(), declared) for selector in selectors.split(","))
    return found


def _declared(name: str, selector: str, prop: str) -> str:
    """Return what the last rule for `selector` in `name` declares for `prop`."""
    values = [found[prop] for at, found in rules(name) if at == selector and prop in found]
    if not values:
        raise EditorColoursUnread(f"{name} declares no {prop} for {selector}")
    return values[-1]


def palette() -> dict[str, dict[str, str]]:
    """Return each theme's custom properties, `{"light": {"--bg": "#…"}, "dark": {…}}`."""
    declared = rules(PALETTE)
    themes = {}
    for scheme, selector in THEME_SELECTORS.items():
        blocks = [found for at, found in declared if at == selector]
        if not blocks:
            raise EditorColoursUnread(f"{PALETTE} has no {selector} block")
        themes[scheme] = {key: value for key, value in blocks[0].items() if key.startswith("--")}
    return themes


def highlight() -> dict[str, dict[str, str]]:
    """Return each Prism class's winning declarations, the cascade's last rule on top."""
    painted: dict[str, dict[str, str]] = {}
    for selector, declared in rules(HIGHLIGHT):
        match = TOKEN_SELECTOR.match(selector)
        if match:
            painted.setdefault(match.group(1), {}).update(declared)
    return painted


def code_block() -> dict[tuple[str, str], str]:
    """Return the property `READING` paints the code block's ground and ink with."""
    found = {}
    for role in (GROUND, INK):
        try:
            found[role] = property_of(_declared(READING, *role))
        except EditorColoursUnread:
            raise EditorColoursUnread(
                f"{READING} paints {role[0]}'s {role[1]} with no palette property"
            ) from None
    return found


def property_of(value: str) -> str:
    """Return the custom property a `var(--x)` value names.

    ⛔ A refusal never reproduces the value it refused (R7): it names its type.
    """
    match = _VAR.match(value)
    if match is None:
        raise EditorColoursUnread(
            f"a paint must be a palette property's var(), got {describe(value)}"
        )
    return match.group(1)


def colour(value: str, alpha: str = "") -> str:
    """Return a palette `#rgb` or `#rrggbb` as the workbench's `#rrggbb`, `alpha` appended.

    ⚠️ **The `#` is the stylesheet's own, kept, never typed here**: a `#`
    literal outside `render/markup` reads as a composed page anchor, and that
    sweep (`test_fragment`) is right to be blunt. ⛔ So a colour with no `#` of
    its own (an rgba colour, a name) is refused: a translucent surface is a palette
    colour and an alpha stated beside it in `editor_theme.SURFACES`.
    """
    match = _HEX.match(value)
    if match is None:
        raise EditorColoursUnread(
            f"the editor takes a palette colour as #rgb or #rrggbb, got {describe(value)}"
        )
    digits = match.group(2)
    digits = "".join(c * 2 for c in digits) if len(digits) == 3 else digits
    return f"{match.group(1)}{digits.lower()}{alpha}"
