r"""What the root index is written as, in what order, out of which templates.

**What it does.** Composes the one page a reader lands on first: the same
skeleton every other page of the site has, filled with the corpus's own title
and its whole tree.

**How you use it.** `compose(document, placement)` returns the page's text;
`index.render` is the public entry point and turns it into bytes.

**Depends on.** `render.templates` for the markup, `disclosure` for the body,
`render.markup` for the escaping, and `render.page` for `PageError`.
⛔ Not on `corpus.placement.identity` — see below — and not on `contents`:
`assemble` has already turned the two documents into a record by the time
anything here runs.

## ⛔ The skeleton is `page.html`, the unit page's own, and that is the design

⭐ **The three documents are one product.** The design asks this page to share the unit
page's palette and type stack *by importing them rather than restating them*;
sharing the **skeleton** is that rule taken as far as it goes, and it is why
this package adds no template file and no asset. ⚠️ A second skeleton would be a
second `<head>`, a second masthead and a second place a `<meta viewport>` has to
be remembered — and the day one gains a region the other silently would not.

⛔ **Seven of the skeleton's slots are empty here, and they are empty
explicitly.** `templates.fill` refuses a placeholder with no value **and** a
value with no placeholder, so each one is passed `""` by name rather than
omitted — which means the day the skeleton drops a slot this module fails by
name instead of quietly losing a region. ⭐ **And the day it GAINS one this
module fails too, which is how `breadcrumb` arrived**.

⚠️ **Each absence is a fact rather than an oversight:**

| slot | why this page has nothing to put in it |
|---|---|
| `breadcrumb` | ⛔ the trail says where in the hierarchy the reader is, and this
page is where every trail ends: a crumb for it would be a link to itself |
| `identity` | ⛔ see below — the index is the artifact identity does not describe |
| `meta` | a second masthead line would be this framework's own sentence about
a corpus, and there is no corpus datum for it that the tree does not already
say (R1) |
| `nav` | the between-pages bar points at neighbours in reading order, and the
index has none: it is where that order begins |
| `mark` | ⛔ a read mark is a UNIT's, and the control belongs on the page
whose reading it records. ⭐ The marks themselves DO reach this page —
as a state on the rows this tree already keys by unit key — but that is the
shared script's work at read time, not a region this module fills |
| `pending` | practices belong to a unit |
| `player` | narration belongs to a unit |

## ⭐ Two slots it fills that a first reading would leave empty

⛔ **`rail`: the first page keeps the rail.** The body already lists every
container, so a rail repeats the tree. ⚠️ It is kept for the reader: the rail is
the site's one constant place, and a first page
without it is the one page where the reader has to learn a second way round.
⭐ It arrives from the caller as plain values, exactly as a unit page's does, and
below `rail.RAIL_MINIMUM` containers it is still nothing.

⭐ **`outline`: the slot for the page's secondary block, and this page's is its
explanation** — how to use the site and how it is ordered. It sits in the
skeleton's aside position, so a wide window puts it beside the list rather than
above it, and a narrow one keeps it above, where it always was.

## ⛔ The root index carries NO identity block, and that is not an omission

⚠️ **R4's block answers *which unit or container is this*, and the index is
neither.** `placement.identity.KINDS` holds exactly two members and an
`Identity` requires a non-empty address; there is no address this page could
give, because it is the page *about* every address.

⭐ **Nothing looks for one, and that is checked rather than assumed:** a scan
globs the two page suffixes this framework mints, and `is_unit_page` and
`is_container_page` both refuse `ROOT_INDEX_FILENAME` — so the index is outside
every scan's population, by name, before any file is opened. ⛔ Minting a third
kind to say *"I am the index"* would be an `identity_api` change made to satisfy
a rule that does not reach this page.

## ⛔ This module is the format (R10)

⚠️ The order of the slots below **is** the page, and an unchanged document must
re-render to identical bytes. ⛔ So: no clock, no filesystem enumeration, no set
iteration, and every optional region is exactly empty or exactly its markup plus
one newline — never a conditional newline somewhere else.
"""

from __future__ import annotations

from collections.abc import Sequence

from studyforge.render import templates
from studyforge.render.container import progress
from studyforge.render.index import disclosure
from studyforge.render.index.entries import Document
from studyforge.render.index.placement import Placement
from studyforge.render.markup import escape, escape_attribute
from studyforge.render.page import PageError, RailContainer
from studyforge.render.page import rail as render_rail

#: The skeleton every page of this site is filled from — the unit page's own.
SKELETON = "page.html"

#: Every page ends in exactly one newline. ⚠️ Appended here rather than left as
#: a trailing blank line in the template, because the loader strips one trailing
#: newline so an editor cannot silently lengthen it.
TRAILING_NEWLINE = "\n"

#: The skeleton slots the root index has nothing to put in. ⛔ Named and passed
#: rather than omitted — see this module's docstring for what each absence is.
#:
#: ⚠️ `headingattributes` is one of them: the unit page promotes its material's own
#: opening heading into the `<h1>` and anchors it there, and a page
#: whose heading is a name rather than a block has no anchor to carry.
EMPTY_SLOTS = (
    "breadcrumb",
    "headingattributes",
    "identity",
    "mark",
    "meta",
    "nav",
    "pending",
    "player",
)


def compose(
    document: Document,
    placement: Placement,
    rail: Sequence[RailContainer] | None = None,
) -> str:
    """Return the root index's exact text.

    ⛔ Pure: the same document, placement and rail give byte-identical output,
    every run, on every machine (R10). `rail=None` is the page with no rail.
    """
    try:
        return (
            templates.fill(
                SKELETON,
                title=escape(document.title),
                heading=escape(document.title),
                stylesheet=escape_attribute(placement.stylesheet()),
                script=escape_attribute(placement.script()),
                rail=render_rail(rail),
                outline=f"{ABOUT}\n",
                body=head(document) + disclosure.render(document),
                **dict.fromkeys(EMPTY_SLOTS, ""),
            )
            + TRAILING_NEWLINE
        )
    except templates.TemplateError as error:
        # ⛔ Re-typed, not re-worded: `templates` owns what a fillable skeleton
        # is, and a caller rendering a whole site catches one family for every
        # kind of page. ⛔ **Narrow on purpose** — `PersonalDataLeak` is
        # deliberately outside this branch and travels through as itself
        # (R7), so a leak is never logged as one more page that did not
        # render.
        raise PageError(f"the root index cannot be composed: {error}") from None


#: What the index says about the site, before any of the material
#: (R1). ⛔ **About the SITE, never the material** (R1): how to use
#: it and how it is ordered are this framework's facts. ⚠️ Where the material
#: comes from is the corpus's, so that column waits for manifest data
#: rather than being guessed.
LEDE = (
    "<p>A study site made from the material below. Read each unit in order, "
    "and tick it off when you finish it.</p>"
)

ABOUT = (
    f'<section aria-label="About this site">{LEDE}'
    "<section><h2>How to use it</h2><ol>"
    "<li>Open the unit named under Up next.</li>"
    "<li>At the foot of the unit, press Mark as read. The next unit takes its "
    "place here.</li>"
    "<li>Your marks are kept in this browser on this machine, not in the "
    "repository. Clearing its site data clears them.</li>"
    "</ol></section>"
    "<section><h2>How it is ordered</h2>"
    "<p>In the order the author arranged the material: each group from top to "
    "bottom, and the groups in the order they are listed.</p>"
    "</section></section>"
)


def head(document: Document) -> str:
    """Return what comes above the tree: progress, Up next and the filter.

    ⭐ The explanation is not here: it is the page's aside, in the
    skeleton's `outline` slot — see this module's docstring.
    """
    everything = [
        item for section in document.sections for item in disclosure.readable_items(section)
    ]
    # ⛔ One segment per TOP-LEVEL group, sized by every readable unit under it.
    # ⚠️ A segment per deepest group would put every module of a large course
    # in one row. The outermost groups are the course's own sections, so the
    # strip says what the tree below it says first, and a module is one click
    # further, in its section.
    segments = [
        (group.key, group.title, len(disclosure.readable_items(group)))
        for group in document.sections
    ]
    segments = [segment for segment in segments if segment[2]]
    first = everything[0] if everything else None
    parts = (
        progress.line(len(everything), progress.strip(segments)),
        progress.up_next(first.title, first.href) if first else "",
        progress.finder(),
    )
    return "".join(f"{part}\n" for part in parts if part)
