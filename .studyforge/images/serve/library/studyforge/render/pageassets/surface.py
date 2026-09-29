"""The class names the reading surface styles, published so a renderer can use them.

**What it does.** States the markup vocabulary the shared stylesheet and
scripts target — one entry per archive block type that needs a hook.

**How you use it.** A renderer takes the class from here rather than typing it:
`SURFACE_CLASSES["code"]` is what a code block's `<figure>` must carry.

**Depends on.** `studyforge.archive.blocks` for the vocabulary, and nothing
else. ⛔ **The keys are the archive's, never retyped here.** A block type added
to the vocabulary and not answered for below raises at import — where today it
would silently acquire no class and render unstyled, which is the same failure
this file exists to prevent arriving by a different door.

⭐ **This exists because the failure it prevents is silent.** A stylesheet and
a template that disagree about a class name produce a page that renders,
carries every word, and is unstyled — with no error anywhere. Before the split
between them existed they were one codebase and could not disagree; the
assets and the renderer are separate here, so the agreement has to be written down and checked.

⚠️ **The page renderer may rename any of these**, and that is a change to this file plus
the stylesheet, together. ⛔ What it may not do is invent a second name for
something already here: `test_surface` asserts that every class the shared
stylesheet targets appears in this mapping, so a rename that touches only one
side fails.

⛔ **These are hooks, not semantics.** The archive says what a block *is*; a
class name says what the stylesheet may reach. Nothing downstream may read a
class name back as a block type — that is R4's argument about paths, applied
to markup.

## ⛔ A hook is not always a class

⚠️ **`SURFACE_CLASSES` is classes and `SURFACE_HOOKS` is not.** Three of
its entries are classes; the rest are the **name** of a `data-*` attribute or a
**value** of `data-kind`, because `render.container.listing` and
`render.index.disclosure` address their rows that way on purpose — ⭐ *"a
`data-*` rather than a class, so this page needs no entry in a published class
set"* — and that is the property that lets a chrome stylesheet change
independently of the markup it styles, with no page change and no re-render.

⛔ **So `_FORM_OF` says which form each hook takes, `HOOK_CLASSES` is the
class-shaped subset, and the both-directions class contract compares against
that subset and not against the whole mapping.** ⚠️ Comparing against the whole
mapping is not a harmless widening: it makes the stylesheet owe a rule for
`.data-readable` in one direction, and lets a template carry
`class="data-readable"` in the other.
"""

from __future__ import annotations

from studyforge.archive.blocks import BLOCK_TYPES

#: What each block type's element carries, or `None` where it needs none.
#: ⛔ **Every block type in the vocabulary appears**, answered either way: a
#: heading, a paragraph, a thematic break, a table and a quote are styled as
#: the plain elements they are, because a class that adds nothing is a class
#: that has to be kept in step for nothing — and saying so explicitly is what
#: makes a *new* block type a loud failure here rather than an unstyled page.
#:
#: ⚠️ The values are this file's own and the keys are the archive's. That is
#: the whole of the seam: `SURFACE_CLASSES["list"] == "items"` is deliberate
#: and unchanged, so nothing can invert the mapping and read a class name back
#: as a block type (R4 applied to markup).
_CLASS_OF = {
    "heading": None,
    "para": None,
    "code": "code",
    "table": None,
    "list": "items",
    "image": "image",
    "video": "video",
    "rule": None,
    "quote": None,
    "html": None,
    "disclosure": "disclosure",
}

#: `archive block type -> the class its element carries`, for the types that
#: have one. ⛔ Built by walking `BLOCK_TYPES`, so the keys come from the one
#: block-type list and a vocabulary change cannot pass unnoticed.
SURFACE_CLASSES = {
    block_type: _CLASS_OF[block_type]
    for block_type in BLOCK_TYPES
    if _CLASS_OF[block_type] is not None
}

#: How a hook reaches the page. ⛔ Three forms and no fourth, because a hook
#: whose form nobody decided is a hook a stylesheet reaches for as a class and a
#: renderer emits as an attribute — which renders, carries every word, and is
#: unstyled with no error anywhere.
CLASS_FORM = "class"  # the element carries it in `class="…"`
ATTRIBUTE_FORM = "attribute"  # it is the NAME of a `data-*` attribute
KIND_FORM = "kind"  # it is a VALUE of the `data-kind` attribute

#: `hook -> its form`. ⛔ Every entry of `SURFACE_HOOKS` appears, and a hook
#: added without a form raises at import — the same door `_CLASS_OF` closes one
#: mapping up, for the same reason.
_FORM_OF = {
    "table_scroll": CLASS_FORM,
    "copy_button": CLASS_FORM,
    "code_caption": CLASS_FORM,
    "code_fallback": CLASS_FORM,
    "readable": ATTRIBUTE_FORM,
    "kind": ATTRIBUTE_FORM,
    "marked": ATTRIBUTE_FORM,
    "numbering": KIND_FORM,
    "level": KIND_FORM,
    "read_state": KIND_FORM,
}

#: Wrappers, controls and states the surface styles that are not block types.
#: ⭐ `code_fallback` marks a code caption whose language the vendored
#: highlighter does not cover (`grammars`), so the reader sees why it is plain.
#: ⛔ The scroll wrapper is not decoration: a table wider than the column must
#: scroll inside its own box, or the page scrolls sideways and every paragraph
#: with it.
#:
#: ⭐ **The last four are here because MORE THAN ONE RENDERER NEEDS THEM**, and
#: R17's answer governs: what more than one renderer needs is a sibling
#: package, never a name on one of them. `render.container.listing` and
#: `render.index.disclosure` both say *this row could not be linked* and *this is
#: the unit's reader-facing numbering*, and before the stylesheet they each spelled it
#: themselves. ⚠️ Two spellings that agree today disagree the day one
#: page gains a third state — and the stylesheet writes the rules against whichever it
#: finds first, so the spelling had to stop being plural before the rules
#: existed. ⛔ **Not promoted onto `render.container.__all__`**: that would be a
#: reach past a package's exported surface (R21), which `render/page/test_init.py`'s sweep fails by
#: name.
#:
#: ⭐ **`marked` is the one hook NO renderer emits, and it is published for the
#: same reason the others are**. A read mark is the reader's own
#: assertion, kept in their browser — so it can only be set at read time, by
#: `render/assets/read-mark.js`, on the unit page's control and on the rows of
#: the two lists. ⛔ It is here rather than spelled in the script because
#: `chrome.css` is what DRAWS the marked state, and a state the stylesheet
#: reaches is exactly what this mapping is for: two spellings that agree today
#: disagree the day one of them is renamed, and the symptom is a badge that
#: never lights. ⚠️ **`data-unit`, the key the mark is FILED under, is
#: deliberately NOT here** — it is a script hook no rule could meaningfully
#: reach, and putting it here would oblige `chrome.css` to paint a unit's
#: address. `render/page/mark.py` says so at length.
#:
#: ⚠️ **`kind` is the attribute and `numbering`/`level` are two of its values,
#: and the attribute is OVERLOADED.** `templates/section.html` carries
#: `data-kind="<the section's kind>"`, whose value comes out of a corpus. So a
#: rule for `numbering` or `level` names the element too — `chrome.css` uses
#: `span[data-kind="numbering"]` — because a corpus may call a section kind
#: anything, including one of these words.
#:
#: ⭐ **`read_state` wraps the words that tell assistive technology a row is
#: read**. The rail and both lists emit it hidden on every row, from
#: one template, and `progress-view.js` shows it on the rows the store holds —
#: so a built page stays byte-identical whoever opens it (R10). `chrome.css`
#: keeps it off the screen: the tick is the visual mark, the words are not.
SURFACE_HOOKS = {
    "table_scroll": "scroll",
    "copy_button": "copy",
    "code_caption": "what",
    "code_fallback": "unhighlighted",
    "readable": "data-readable",
    "kind": "data-kind",
    "marked": "data-marked",
    "numbering": "numbering",
    "level": "level",
    "read_state": "read-state",
}

#: `hook -> the class its element carries`, for the hooks that are classes.
#: ⛔ Derived from `_FORM_OF` rather than listed, and published because the
#: markup contract *"every class the stylesheet targets is published, and every
#: published class is styled"* is about classes only: an attribute name in that
#: comparison makes the stylesheet owe a rule for `.data-readable`, and a
#: template carrying `class="data-readable"` pass.
HOOK_CLASSES = {
    hook: value for hook, value in SURFACE_HOOKS.items() if _FORM_OF[hook] == CLASS_FORM
}


def class_for(block_type: str) -> str | None:
    """Return the class an element of `block_type` carries, or None when it needs none."""
    return SURFACE_CLASSES.get(block_type)
