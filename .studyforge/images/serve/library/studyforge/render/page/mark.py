r"""The mark-as-read control: where it sits on a unit page, and what it names.

**What it does.** Renders the one region the reader acts in — an explicit
mark-as-read control — carrying the unit key the browser files the mark under.

**How you use it.** `mark.render(document)` returns the region's markup;
`page.document` puts it in the page's slot.

**Depends on.** `studyforge.address` for the one key composer,
`render.templates` for the markup, `render.markup` for the attribute escape,
and `page.errors`. ⛔ Nothing that knows where a file is, and nothing that
reads one.

## ⛔ Why the control exists at all, and why it is not the server's

⚠️ **A reader who never starts the server had no record of anything** (spec
§8.5). The progress store is the served half: a **pass** is a fact established by a grader
run, so it is written where it was established. ⛔ A **read mark** is the
reader's own assertion, needs no server and no origin (R8), and lives in the
browser. ⭐ **And it binds hardest on exactly the sources this framework exists
to serve:** §7 admits units with no exercise or an ungraded one, so an arbitrary
repository's every unit is `none` or `ungraded` and a server-side-only store
would record nothing whatever for the whole corpus.

## ⛔ The region ships HIDDEN, and the reader-facing words are markup

⭐ **`templates/read-mark.html` carries the button, its two labels and the
sentence saying where the mark lives** — so no word a reader sees is typed in
Python (R13), and `render/assets/read-mark.js` unhides the region only once it
has a working store behind it. ⚠️ With scripting off the reader is shown
nothing, which is the `copy-code.js` bargain: a control that does nothing is
worse than no control.

## ⛔ The key is ASKED FOR, never composed

⚠️ `Address.unit_key` is the one composer, and its own docstring says why: *"the
page writes a read mark under it, the index reads the mark back … a second
spelling that differed by one character would simply never match anything, with
nothing failing anywhere."* ⭐ So this module joins the page's recorded address
to its recorded ordinal through that function and nothing else — no string
arithmetic here, and no second format anywhere on the page.

## ⛔ `data-unit` is NOT in `pageassets.SURFACE_HOOKS`, and that is deliberate

⚠️ **`SURFACE_HOOKS` is what the STYLESHEET targets**, and
`test_chrome.py::test_every_non_class_hook_is_reached_by_a_rule_in_this_part`
obliges every non-class entry to be reached by a rule in `chrome.css`. ⛔ A
stylesheet rule keyed on a unit's address would be meaningless — and writing one
only so a published hook could be called painted is a false paint, committed
on purpose. ⭐ This attribute is a **script** hook: one emitter (this module) and
one reader (`read-mark.js`), which is the same two-sided spelling every hook on
the page has, because markup and script cannot import Python.

⚠️ **The state hook is the other way round.** `data-marked` *is* reached by
`chrome.css` — it is how a marked row and a marked control are drawn — so it is
published in `SURFACE_HOOKS` and taken from there rather than typed here.
"""

from __future__ import annotations

from studyforge.address import Address, AddressError
from studyforge.describe import describe
from studyforge.render import templates
from studyforge.render.markup import escape_attribute
from studyforge.render.page.errors import PageError

#: The region's markup. ⛔ A file, not a string: it holds a button, two labels
#: and a sentence, which is a whole element with attributes and never a
#: fragment (R13).
CONTROL_TEMPLATE = "read-mark.html"


def key(document: dict) -> str:
    """Return the unit key this page's mark is filed under.

    ⛔ Built by `Address.unit_key` from the document's own recorded address and
    ordinal — the same function the root index's rows are keyed by, so the mark
    the page writes and the mark the index reads back are one string.

    ⚠️ **The list is checked for being a list, and that is not defensiveness.** `tuple("basics")`
    is `("b","a","s","i","c","s")` — six
    single-character segments, every one of them a valid slug — so an address
    recorded as a *string* rather than a list becomes a six-level address that
    refuses nothing and keys every mark under a name nothing else will ever
    produce. ⛔ Refused by name instead (R6). ⚠️ `page.document.identity` has
    the same door, and closing it is that module's.
    """
    segments = document.get("address")
    if segments is not None and not isinstance(segments, list):
        raise PageError(
            f"a unit's address is recorded as a list of slugs, and this document "
            f"records {describe(segments)}; it is refused rather than taken apart, "
            f"because a string would become one segment per character and key every "
            f"mark under a name nothing else will ever mint"
        )
    try:
        return Address(tuple(segments or ())).unit_key(document.get("unit"))
    except AddressError as error:
        # ⛔ Re-typed, not re-worded: `address` owns what a key is, and a caller
        # rendering a site catches one family per page. ⚠️ The message carries
        # the address package's own sentence, which describes rather than echoes
        # what arrived (R7) — a unit ordinal is a number and a segment is a slug,
        # but this runs over every unit in a corpus and into a build log.
        raise PageError(
            f"this unit cannot say what its read mark would be filed under: {error}"
        ) from None


def render(document: dict) -> str:
    """Return the mark-as-read region for this unit page.

    ⚠️ Never empty, and that is the difference between this region and every
    other optional one: a unit page always has an address and an ordinal, and a
    document that has not is refused by `key` above rather than rendered without
    the control — a page that declined to offer it is a page whose reading the
    reader cannot record.
    """
    return templates.fill(CONTROL_TEMPLATE, unit=escape_attribute(key(document)))
