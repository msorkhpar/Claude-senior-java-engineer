r"""⛔ Which block types bypass escaping — `html`, and nothing else.

**What it does.** Renders the one block type in the whole archive vocabulary
whose text reaches the page **unescaped**.

**How you use it.** `RENDERS` is the answer to *which types are raw?* — and it is
answered by `ls`, not by reading branches. `render(block, position, …)` renders
one, and the dispatcher is its only caller.

**Depends on.** Nothing. ⭐ Not even `render.markup`: this module's whole content is
that it does *not* escape, and importing the escaper would put the thing it
refuses to do one keystroke away.

# ⛔ This module is small on purpose, and the reason is that its failure is silent

⚠️ **Two block types are byte-identical in shape and have opposite rules about
the same field.** In `archive/markdown/leaf.py`:

- `read_paragraph` returns `{"type": "para", "text": …}`
- `read_html` returns `{"type": "html", "text": …}`

⛔ **Same field name, same Python type, same possible content.** The text of a
tag-shaped paragraph — `<blink>hello</blink>` — is byte-identical to the text of
an `html` block carrying the same tag. ⭐ **Nothing but the declared `type`
separates them**, and `pageassets.surface` gives both the same answer (`None` —
no class), so a class name cannot tell them apart either.

⛔ **The failure is silent in both directions, and neither raises anything:**

- **Escape too little.** A `para` emitted raw is consumed by the browser as
  markup. The page renders, is well-formed, links correctly, carries every other
  word, and **the sentence is gone**. The archive is unchanged. `validate`
  passes. Nothing logs.
- **Escape too much.** An `html` block escaped shows a lesson's own markup as
  literal text — visible, wrong, and equally silent.

⚠️ **The specific way this arrives** is by deciding escaping from the *text*
rather than from the declared *type* — `if text.lstrip().startswith("<")` inside
a generic block renderer, written by somebody reasonably trying to be helpful.
⛔ **That is exactly the promise the archive parser declined CommonMark's type-7 raw-HTML
rule to keep** — *an unknown tag on its own line stays a paragraph, because a
lesson teaching HTML must keep it* — and the page renderer is its only enforcer.

⭐ **Split out, "which block types bypass escaping" is a one-line answer that a
diff can review.** Adding a second raw type becomes a new name in `RENDERS`, in
a module called `verbatim`, in a package whose test derives the raw set from
`BLOCK_TYPES` and fails when it changes. Adding a second raw *branch* inside a
two-hundred-line dispatcher is not a reviewable event.

⚠️ **It is not free.** This is the smallest module in the package and will look
like over-engineering to a reviewer who has not read this docstring. ⭐ That is
the argument for writing the docstring, not for merging the module.

## ⛔ Raw is not the same as ungated

⚠️ The archive stores this text verbatim and **gates it like every other
string** — `archive.scrub` on the way in, `unit.served` on the way out — so what
reaches here has already been refused if it carries personal data (R7). ⭐ What
this module declines is *escaping*, not *checking*, and the two are different
promises made by different modules.
"""

from __future__ import annotations

#: ⛔ The whole of it. One block type, and the test that guards this module
#: derives the expected set from `archive.blocks.BLOCK_TYPES` rather than
#: repeating this tuple — so a twelfth block type that quietly acquired a raw
#: path fails loudly instead of passing two fixture-shaped tests.
RENDERS = ("html",)


def render(
    block: dict,
    position: int,
    *,
    placement: object = None,
    section: str = "",
    children: str = "",
    path: tuple[int, ...] = (),
    narration: object = None,
) -> str:
    """Emit the block's text exactly as the archive stored it.

    ⛔ **The decision is the declared `type`, never the text.** This function is
    reached only because the dispatcher looked `block["type"]` up in a mapping
    whose raw half is this module's `RENDERS`; nothing here inspects a character
    of the payload, and there is no other way in.

    ⛔ **And it is never narrated.** `SPEECH_OF` calls `html` `silent`, which is
    the only answer available here: this module emits a payload whose elements,
    if it has any, are the archive's own — so there is nothing for an attribute
    to be written onto that would not be an edit to a source's markup.
    """
    del position, placement, section, children, path, narration
    return str(block.get("text") or "")
