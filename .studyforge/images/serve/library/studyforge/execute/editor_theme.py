r"""The practice editor wears the page's colours, derived from the page's own stylesheets.

**What it does.** `editor_colours()` returns the workspace settings that paint
a practice's code-server windows in the colour set of the page's code blocks:
the base themes, the workbench surfaces, and the syntax colours of the same
token families. ⭐ **Every colour is READ, at the moment it is asked for, out of
the three stylesheets that paint the page** — `palette.css` for each custom
property in each theme, `code-highlight.css` for which property each Prism token
class is painted with (and its weight and slant), and `reading.css` for the
code block's own ground and ink. ⛔ **There is no second copy**: this module
holds no colour literal, so a palette change reaches the page and the editor at
once, and `test_editor_theme` fails when the two drift.

**How you use it.** `workbench.settings` merges `editor_colours()` into every
practice's settings; `TOKENS`, `PLAIN` and `SURFACES` are the mapping.

**Depends on.** `page_colours`, which reads the three stylesheets. Raises its
`EditorColoursUnread`.

## ⛔ The mapping is the design, and it lives in `TOKENS`

⚠️ Prism names a token by what it IS in the page's grammar (`class-name`,
`annotation`); TextMate names it by where it sits in the editor's grammar
(`entity.name.type`, `storage.type.annotation`); and a language server's
semantic tokens (`class`, `annotation`) repaint many of them once it answers.
⭐ **Each row of `TOKENS` is one Prism class, and its colour, weight and slant
are that class's rule in `code-highlight.css`**, cascade order included — so
the row says only WHERE in the editor that class's colour belongs. ⭐ TextMate
picks the most specific scope, which is how `keyword.operator.new` stays a
keyword while `keyword.operator` is punctuation, as Prism has it. ⚠️ Java's
TextMate grammar scopes a type REFERENCE as `storage.type`, which every other
grammar uses for a type KEYWORD (`const`, `def`); the keyword reading is kept,
and Java's language server repaints the reference as `class` once it answers.

⭐ **An identifier Prism leaves plain is plain here too** (`PLAIN`): a variable,
a parameter or a field takes the block's ink, where the base themes paint it
light blue.

## ⛔ Light and dark follow the page, and the page's own frame carries it

⭐ `window.autoDetectColorScheme` makes the workbench follow
`prefers-color-scheme` **as its own document sees it**, and in an iframe that is
the colour scheme of the iframe ELEMENT in the page — which the page's `:root`
sets from the reader's choice (`color-scheme: light dark` when they chose
nothing, `dark` or `light` when they did). ⭐ So the editor matches the page for
all three states without the page telling it anything. In Chrome an iframe under
`color-scheme: dark` reads dark, and one given `color-scheme: light` reads
light, whatever the system says. ⚠️ Each theme's colours are scoped to its base
(`[Visual Studio Light]`, `[Visual Studio Dark]`), so one settings file carries
both.

⭐ **The bases are the workbench's plainest pair, `Visual Studio Light` and
`Visual Studio Dark`** (`BASES`), and the reason is MEASURED. ⛔ A customization
does not beat a theme's own rule of a MORE SPECIFIC scope, and the newer pairs
(`Dark Modern`, `Dark 2026`) inherit `Dark+`'s per-language rules: under
`Dark 2026` Java's `int` and `void` (`storage.type.primitive.java`) stayed the
base's teal beside the page's keyword violet. ⭐ The Visual Studio pair is the
root the others include and carries the fewest token rules of the four default
pairs, so the page's mapping decides the tokens a practice file carries; the
three Java rules it does keep (an
import's and a package's path, and `*`) are repainted by the language server's
`namespace` tokens. ⚠️ Its workbench greys are older, and every one a practice
frame shows is overridden in `SURFACES`. ⛔ Solarized, Quiet Light and Kimbie
are warm, and Tomorrow Night Blue and Abyss are saturated navy; the palette is
none of those.

⭐ **Every rule states its weight and slant, even when the page declares
none**: a style left unset is inherited from a less specific rule, and a type
reference (`storage.type` in Java's grammar) came out bold beside the page's
regular one.
"""

from __future__ import annotations

from dataclasses import dataclass

from studyforge.execute.page_colours import (
    GROUND,
    HIGHLIGHT,
    INK,
    PALETTE,
    EditorColoursUnread,
    code_block,
    colour,
    highlight,
    palette,
    property_of,
)

#: The built-in base theme for each, by the id the workbench's settings use.
BASES = {"light": "Visual Studio Light", "dark": "Visual Studio Dark"}


@dataclass(frozen=True, slots=True)
class Token:
    """One Prism token class, and where its colour belongs in the editor."""

    prism: str
    scopes: tuple[str, ...]
    semantic: tuple[str, ...] = ()


#: ⭐ THE MAPPING. The page's families, weakest to strongest as
#: `code-highlight.css` orders them; ⛔ no colour here, only where it goes.
TOKENS: tuple[Token, ...] = (
    Token(
        "punctuation",
        ("punctuation", "meta.brace", "punctuation.separator", "punctuation.terminator"),
    ),
    Token("operator", ("keyword.operator",), ("operator",)),
    Token(
        "class-name",
        (
            "entity.name.type",
            "entity.name.class",
            "entity.other.inherited-class",
            "support.class",
            "support.type",
        ),
        ("class", "interface", "enum", "struct", "type", "typeParameter", "record"),
    ),
    Token(
        "annotation",
        ("storage.type.annotation", "punctuation.definition.annotation"),
        ("annotation", "decorator"),
    ),
    Token(
        "namespace",
        (
            "entity.name.namespace",
            "entity.name.package",
            "storage.modifier.import",
            "storage.modifier.package",
        ),
        ("namespace",),
    ),
    Token("tag", ("entity.name.tag",)),
    Token("attr-name", ("entity.other.attribute-name",)),
    Token("property", ("support.type.property-name",)),
    Token("function", ("entity.name.function", "support.function"), ("function", "method")),
    Token("number", ("constant.numeric",), ("number",)),
    Token(
        "string",
        ("string", "punctuation.definition.string", "constant.character", "string.regexp"),
        ("string", "regexp"),
    ),
    Token(
        "keyword",
        (
            "keyword",
            "keyword.control",
            "keyword.other",
            "keyword.operator.new",
            "keyword.operator.instanceof",
            "storage.type",
            "storage.modifier",
            "variable.language",
        ),
        ("keyword", "modifier"),
    ),
    Token("boolean", ("constant.language",)),
    Token(
        "comment",
        ("comment", "punctuation.definition.comment"),
        ("comment", "keyword.documentation"),
    ),
)

#: ⭐ What Prism leaves plain takes the block's ink.
PLAIN = Token(
    "",
    ("variable", "variable.other", "variable.parameter", "entity.name.variable"),
    ("variable", "parameter", "property", "enumMember", "annotationMember", "recordComponent"),
)

#: ⭐ The workbench surfaces: `(key, what paints it, alpha)`. What paints it is a
#: palette property, or `GROUND`/`INK` for the code block's own; an alpha is
#: two hex digits laid over a colour the workbench requires to be translucent.
SURFACES: tuple[tuple[str, str | tuple[str, str], str], ...] = (
    ("editor.background", GROUND, ""),
    ("editor.foreground", INK, ""),
    ("editorGutter.background", GROUND, ""),
    ("editorLineNumber.foreground", "--rule-strong", ""),
    ("editorLineNumber.activeForeground", "--accent", ""),
    ("editorCursor.foreground", "--accent", ""),
    ("editor.selectionBackground", "--focus", "40"),
    ("editor.inactiveSelectionBackground", "--focus", "26"),
    ("editor.selectionHighlightBackground", "--focus", "1f"),
    ("editor.wordHighlightBackground", "--focus", "1f"),
    ("editor.wordHighlightStrongBackground", "--focus", "2e"),
    # ⭐ The page's highlighted-line tint — the accent, faint (`--hl-code` is
    # the accent at about this alpha, written as an rgba colour, which `colour`
    # refuses) — and its border the same, so the base's grey outline never shows.
    ("editor.lineHighlightBackground", "--accent", "1f"),
    ("editor.lineHighlightBorder", "--accent", "1f"),
    ("editorBracketMatch.background", "--accent", "1f"),
    ("editorBracketMatch.border", "--accent", ""),
    ("editor.findMatchBackground", "--accent", "59"),
    ("editor.findMatchHighlightBackground", "--accent", "26"),
    ("editorIndentGuide.background1", "--rule", ""),
    ("editorIndentGuide.activeBackground1", "--rule-strong", ""),
    ("editorWhitespace.foreground", "--rule", ""),
    ("editorRuler.foreground", "--rule", ""),
    ("editorOverviewRuler.border", GROUND, ""),
    ("editorStickyScroll.background", GROUND, ""),
    ("editorStickyScrollHover.background", "--surface-2", ""),
    ("editorStickyScroll.shadow", "--rule", ""),
    ("scrollbar.shadow", GROUND, ""),
    ("scrollbarSlider.background", "--rule-strong", "40"),
    ("scrollbarSlider.hoverBackground", "--rule-strong", "80"),
    ("scrollbarSlider.activeBackground", "--rule-strong", "b3"),
    ("editorWidget.background", "--surface", ""),
    ("editorWidget.foreground", "--fg", ""),
    ("editorWidget.border", "--rule", ""),
    ("widget.border", "--rule", ""),
    ("editorSuggestWidget.background", "--surface", ""),
    ("editorSuggestWidget.border", "--rule", ""),
    ("editorSuggestWidget.foreground", "--fg", ""),
    ("editorSuggestWidget.selectedBackground", "--surface-2", ""),
    ("editorSuggestWidget.selectedForeground", "--fg", ""),
    ("editorSuggestWidget.highlightForeground", "--focus", ""),
    ("editorSuggestWidget.focusHighlightForeground", "--focus", ""),
    ("editorHoverWidget.background", "--surface", ""),
    ("editorHoverWidget.foreground", "--fg", ""),
    ("editorHoverWidget.border", "--rule", ""),
    ("editorHoverWidget.statusBarBackground", "--surface-2", ""),
    ("editorInlayHint.background", "--surface-2", ""),
    ("editorInlayHint.foreground", "--muted", ""),
    ("editorCodeLens.foreground", "--muted", ""),
    ("editorLink.activeForeground", "--focus", ""),
    ("textLink.foreground", "--focus", ""),
    ("focusBorder", "--focus", ""),
    ("foreground", "--fg", ""),
    ("descriptionForeground", "--muted", ""),
    ("input.background", "--bg", ""),
    ("input.foreground", "--fg", ""),
    ("input.border", "--rule", ""),
    ("input.placeholderForeground", "--muted", ""),
    ("quickInput.background", "--surface", ""),
    ("quickInput.foreground", "--fg", ""),
    ("list.hoverBackground", "--surface-2", ""),
    ("list.activeSelectionBackground", "--surface-2", ""),
    ("list.activeSelectionForeground", "--fg", ""),
    ("list.highlightForeground", "--focus", ""),
    ("list.focusHighlightForeground", "--focus", ""),
    ("list.focusOutline", "--focus", ""),
    ("editorGroup.border", "--rule", ""),
    ("editorGroup.emptyBackground", GROUND, ""),
    ("editorGroupHeader.tabsBackground", GROUND, ""),
    ("editorGroupHeader.noTabsBackground", GROUND, ""),
    ("titleBar.activeBackground", GROUND, ""),
    ("titleBar.inactiveBackground", GROUND, ""),
    ("titleBar.activeForeground", "--fg-soft", ""),
    ("titleBar.inactiveForeground", "--muted", ""),
    ("titleBar.border", GROUND, ""),
    ("sideBar.background", "--surface", ""),
    ("sideBar.foreground", "--fg", ""),
    ("panel.background", GROUND, ""),
    ("panel.border", "--rule", ""),
)

#: ⭐ Bracket-pair colouring paints each nesting depth its own hue; the page
#: paints every bracket the punctuation ink, so it is off.
PLAIN_BRACKETS = {"editor.bracketPairColorization.enabled": False}


def _ink(theme: dict[str, str], prop: str, alpha: str = "") -> str:
    if prop not in theme:
        raise EditorColoursUnread(f"{PALETTE} defines no {prop}")
    return colour(theme[prop], alpha)


def _style(declared: dict[str, str]) -> list[str]:
    """Return the slant and weight a token's rule declares, in the workbench's words."""
    style = []
    if declared.get("font-style") == "italic":
        style.append("italic")
    weight = declared.get("font-weight", "")
    if weight == "bold" or (weight.isdigit() and int(weight) >= 600):
        style.append("bold")
    return style


def _painted(token: Token, painted: dict[str, dict[str, str]], ink: str) -> tuple[str, list[str]]:
    """Return the property a token is painted with, and its style."""
    if not token.prism:
        return ink, []
    declared = painted.get(token.prism)
    if declared is None or "color" not in declared:
        raise EditorColoursUnread(f"{HIGHLIGHT} paints no .token.{token.prism}")
    try:
        return property_of(declared["color"]), _style(declared)
    except EditorColoursUnread:
        raise EditorColoursUnread(
            f"{HIGHLIGHT} paints .token.{token.prism} with no palette property"
        ) from None


def _theme(theme: dict[str, str], painted: dict, block: dict) -> tuple[dict, dict, dict]:
    """Return one theme's surfaces, TextMate rules and semantic rules."""
    surfaces = {
        key: _ink(theme, block[source] if isinstance(source, tuple) else source, alpha)
        for key, source, alpha in SURFACES
    }
    textmate, semantic = [], {}
    for token in (*TOKENS, PLAIN):
        prop, style = _painted(token, painted, block[INK])
        foreground = _ink(theme, prop)
        textmate.append(
            {
                "scope": list(token.scopes),
                "settings": {"foreground": foreground, "fontStyle": " ".join(style)},
            }
        )
        for selector in token.semantic:
            semantic[selector] = {
                "foreground": foreground,
                "bold": "bold" in style,
                "italic": "italic" in style,
            }
    return surfaces, {"textMateRules": textmate}, {"enabled": True, "rules": semantic}


def editor_colours() -> dict[str, object]:
    """Return the workspace settings that paint the editor in the page's colours, both themes."""
    themes, painted, block = palette(), highlight(), code_block()
    surfaces, tokens, semantic = {}, {}, {}
    for scheme, base in BASES.items():
        scope = f"[{base}]"
        surfaces[scope], tokens[scope], semantic[scope] = _theme(themes[scheme], painted, block)
    return {
        "workbench.colorTheme": BASES["dark"],
        "window.autoDetectColorScheme": True,
        "workbench.preferredLightColorTheme": BASES["light"],
        "workbench.preferredDarkColorTheme": BASES["dark"],
        "workbench.colorCustomizations": surfaces,
        "editor.tokenColorCustomizations": tokens,
        "editor.semanticTokenColorCustomizations": semantic,
        **PLAIN_BRACKETS,
    }
