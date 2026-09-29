"""The one exception the unit page renderer raises.

**What it does.** Names every way one unit page can fail to render, so a caller
walking a corpus catches one type per page and carries on with the rest.

**How you use it.** Catch `PageError`.

**Depends on.** Nothing.

⚠️ **`Exception`, not `ValueError`, following the split this house already
made** (`AssetError`, `ContentError`, `PlacementError`): a *value* error means
*"you handed me something I cannot accept"*, a *document* error means *"this is
not something I can render"*. Every failure here is the second kind.

⛔ **`PersonalDataLeak` travels through as itself, never translated** (Ruling
58). A caller rendering a site catches `PageError` per unit and serves the rest;
an R7 refusal inside that family would be logged as one more page that did not
render, and the leak would be the thing nobody looked at.

⚠️ **`TemplateError` is deliberately not in this family either.** It says a
template file is missing or a slot went unfilled — a defect in *this build*,
true of every page it would render, not of the one document in hand. A renderer
that folded it into `PageError` would let a corpus walk report a thousand
unrenderable units where the answer is one broken template.
"""

from __future__ import annotations


class PageError(Exception):
    """One unit document cannot be rendered as a page.

    ⛔ The message names the field and what was wrong with it, and never quotes
    the document's own text: it is a corpus's material and can carry anything
    (R7).
    """
