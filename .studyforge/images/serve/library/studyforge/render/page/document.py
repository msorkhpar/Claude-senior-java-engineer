r"""What is written, in what order, out of which templates — the page's format.

**What it does.** Composes one unit page: the skeleton every page has, and the
regions a particular page may or may not carry.

**How you use it.** `compose(document, placement, links)` returns the page's
text; `page.render` is the public entry point and turns it into bytes.

**Depends on.** `render.templates` for the markup, `page.section`, `page.anchors`,
`page.navigation`, `page.assets`, `page.code`, `corpus.placement.identity` for
R4's block, and `page.errors`.

## ⛔ This module *is* the format (R10)

⚠️ **The order of the slots below is the page**, and an unchanged document must
re-render to identical bytes. ⛔ So: no clock, no filesystem enumeration, no set
iteration, and every optional region is a value that is either exactly empty or
exactly its markup plus one newline — never a conditional newline somewhere else.

## ⛔ The identity block is on every page, and it is not decoration (R4)

⚠️ **Discovery reads this block and never the path.** A page moved to another
directory, or renamed, is still exactly the unit it says it is. ⭐ It is
rendered by `corpus.placement.identity`, which owns both halves and is tested
round-trip — this module chooses **where** it sits and nothing about what it
says.

## ⭐ The seam this module divides on, named before anybody needs it

⚠️ **This is the module the composer grows into**: every new page region — the
player, the read control, a container page — adds a slot. ⛔ Its next split
is **not** at a convenient line number: it is between the **skeleton** (which
regions exist, in what order, and the one `page.html` substitution that fills
them) and the **regions themselves**, each of which is *optional and gated on
something*. When this file crosses R11's 400 it divides into `document.py` and
`regions.py` along that line, and not elsewhere.

## ⛔ The page's heading is the MATERIAL's when the material has one

⚠️ **A source document states what it is in its first heading**, and this page
states what it is in its `<h1>` — and printing both is the title read twice.
⭐ So the two are one statement: `page.anchors.title_heading` names the block,
this module prints it as the page's heading, and `page.section` withholds it.

⛔ **The unit's own title still names the unit everywhere else** — the `<title>`,
R4's identity block, the trail, the contents and the bar between units: those
name the *unit*, and the `<h1>` names the *page*. ⭐ A unit whose material
states no title is headed by the unit's title, the common authored case.

⭐ **A lesson's code links are marked, and a list of them is drawn as examples
that open in place** (`page.code`); a page that links no code is unchanged.

## ⛔ The player is *derived*, not declared — and that is the answer to a real tension

⚠️ **The page renderer must not invent speech ids.** *"Ids come from
`speakable.py`, never from here … two numbering schemes that agree today are
exactly the coupling that breaks silently tomorrow."* ⛔ So this module mints
no speech id and writes no audio attribute.

⭐ **So the player's gate reads what the page actually emitted**: a page carries
a player when its body carries `assets.AUDIO_ATTRIBUTE`, and nothing else.
⚠️ **No document field holds a gate.**

⚠️ **So a page's bytes change when a corpus gains narration**: a product
change, not a regression — R10 pins that *a rerun is identical*.
"""

from __future__ import annotations

from collections.abc import Sequence

from studyforge.address import Address, AddressError
from studyforge.corpus.placement import PlacementError
from studyforge.corpus.placement import identity as identity_block
from studyforge.render import templates
from studyforge.render.markup import escape, escape_attribute, inline
from studyforge.render.page import anchors, navigation
from studyforge.render.page import code as code_region
from studyforge.render.page import mark as mark_region
from studyforge.render.page import practice as practice_region
from studyforge.render.page import practices as practices_region
from studyforge.render.page import rail as rail_region
from studyforge.render.page import section as section_module
from studyforge.render.page.anchors import TITLE_POSITION
from studyforge.render.page.assets import AUDIO_ATTRIBUTE, Placement
from studyforge.render.page.errors import PageError
from studyforge.render.page.narration import SILENT, Narration
from studyforge.render.page.navigation import Crumb, Links
from studyforge.render.page.rail import RailContainer

#: The skeleton every unit page is filled from.
SKELETON = "page.html"

#: The panel that says a unit is not finished.
PENDING_TEMPLATE = "pending-practices.html"

#: The narration transport. ⛔ Its region is gated on the body carrying a clip —
#: see this module's docstring.
PLAYER_TEMPLATE = "player.html"

#: The panel that says this unit was narrated and some of its audio is not on
#: disk. ⛔ Inside the player's own region rather than a slot of its own, so the
#: notice and the transport it explains cannot arrive separately.
NARRATION_GAP_TEMPLATE = "narration-gap.html"

#: What separates two rendered sections, and what closes an optional region.
JOIN = "\n"

#: Every page ends in exactly one newline. ⚠️ Appended here rather than left as
#: a trailing blank line in `page.html`, because the loader strips one trailing
#: newline so an editor cannot silently lengthen an inline template — and a file
#: ending in a blank line is what an editor tidies away.
TRAILING_NEWLINE = "\n"


def compose(
    document: dict,
    placement: Placement,
    links: Links | None = None,
    trail: Sequence[Crumb] | None = None,
    narration: Narration = SILENT,
    rail: Sequence[RailContainer] | None = None,
) -> str:
    """Return one unit page's exact text.

    ⛔ Pure: the same document and the same placement give byte-identical
    output, every run, on every machine (R10).

    ⚠️ `trail` is optional for the reason `links` is: only something that has
    walked the corpus's hierarchy can build one, so a page renders without it
    exactly as it will once a build does — minus the region.

    ⚠️ `rail` is optional for exactly that reason too, and it is the region that
    reaches the OTHER containers: only a caller holding the whole
    contents document can name them, and a corpus with one container renders
    without it by design rather than by omission.

    ⭐ `narration` is optional for a third reason, and it is the one the player's
    gate below was designed around: a corpus whose clips have not been
    synthesised renders exactly as a corpus that will never have any (R6), and
    the transport is absent in both cases rather than present and dead.
    """
    title = _title(document)
    sections = _sections(document)
    heads = anchors.title_heading(document) is not None
    attributes = heading_attributes(document, narration)
    parts = [
        _part(section, placement, narration, document, heads_page=heads and index == 0)
        for index, section in enumerate(sections)
    ]
    joined = practices_region.joined(parts, sections, document, placement)
    body = code_region.examples(*code_region.mark(joined, placement), placement)
    return (
        templates.fill(
            SKELETON,
            title=escape(title),
            heading=heading(document, title),
            headingattributes=attributes,
            identity=_region(identity(document, placement)),
            stylesheet=escape_attribute(placement.stylesheet()),
            script=escape_attribute(placement.script()),
            meta=_region(meta(document)),
            breadcrumb=_region(navigation.breadcrumb(trail)),
            rail=_region(rail_region.render(rail)),
            outline=_region(anchors.outline(document)),
            body=body,
            pending=_region(pending(document)),
            mark=_region(mark_region.render(document)),
            # ⛔ The heading is part of what the gate reads, because the page's
            # own `<h1>` is a narrated passage when the material supplied it —
            # and a page whose only spoken line is its title must still be able
            # to play it.
            player=_region(player(attributes + body, narration)),
            nav=_region(navigation.between_units(links)),
        )
        + TRAILING_NEWLINE
    )


def _part(
    section: dict, placement: Placement, narration: Narration, document: dict, *, heads_page: bool
) -> str:
    """Return one section and, where it sets work, the panel the reader acts in.

    ⛔ **The panel sits AFTER the section rather than inside it**, which is the
    shape `section`'s own attachments region already has: the statement, the
    hint and the starting code are the material's blocks and belong to the
    material; the editor slot, Run, Submit and the result are this framework's
    controls and belong beside it. ⭐ Keeping it outside `<section>` also keeps
    it out of the outline, exactly as the narrated deck above the section is.

    ⚠️ **Joined here rather than given a slot of its own**, because a unit may
    carry SEVERAL practices and a slot is one region per page: a panel has to
    follow the practice it is about, or a reader reads two statements and then
    two sets of controls with nothing saying which is which.

    ⚠️ `heads_page` is `section.render`'s own word, passed STRAIGHT through
    and REQUIRED: `compose` is its only caller and the only holder of the page.
    """
    rendered = section_module.render(section, placement, narration, heads_page=heads_page)
    panel = practice_region.render(section, document, placement)
    return f"{rendered}{JOIN}{panel}" if panel else rendered


def identity(document: dict, placement: Placement) -> str:
    """Return R4's block, saying what this page is wherever it ends up.

    ⛔ Built from the served document's own fields and never from the page's
    path: inferring identity from a location is the thing R4 forbids, and this
    is the module a reader would expect to find it done in.
    """
    try:
        record = identity_block.Identity(
            corpus=placement.corpus,
            address=Address(tuple(document.get("address") or ())),
            variant=str(document.get("variant") or ""),
            kind="unit",
            unit=document.get("unit"),
        )
    except (PlacementError, AddressError) as error:
        # ⛔ Re-typed, not re-worded: `placement` owns what an identity may say,
        # and re-spelling its sentence here would be two descriptions of one
        # rule. The name is this package's so a caller catches one family.
        #
        # ⛔ **Narrow on purpose.** `PersonalDataLeak` is deliberately outside
        # this pair and travels through as itself: a caller
        # rendering a site catches `PageError` per unit and carries on, and an
        # R7 refusal folded into that family would be logged as one more page
        # that did not render, with the leak the thing nobody looked at.
        raise PageError(f"this unit cannot identify itself: {error}") from None
    return identity_block.render(record)


def heading(document: dict, title: str) -> str:
    """Return what this page is headed by: the material's own title, or the unit's.

    ⛔ **`inline` for one and `escape` for the other, and the difference is
    real.** A heading block's text is material and carries the archive's inline
    markers, exactly as the same block would have carried them in the body; a
    unit's title is a name a manifest or an author wrote and has never been
    inline prose anywhere else on this page.
    """
    promoted = anchors.title_heading(document)
    if promoted is None:
        return escape(title)
    return inline(promoted.get("text"))


def heading_attributes(document: dict, narration: Narration = SILENT) -> str:
    """Return the promoted heading's own anchor and clip, or `''` when none was.

    ⭐ **The block keeps the id AND the clip it would have had in the body**, so
    a bookmark into this page still lands where it always did and the narrator
    still reads the page's first line — which is what makes this a move rather
    than a deletion. ⚠️ `narration.js` therefore looks for passages in the whole
    document rather than inside `#content`, because this one is in the header.

    ⛔ **The position is `0` and is not recomputed anywhere**: it is the block's
    place in its section, and both the anchor and the clip are addressed by it
    exactly as `page.blocks` would have addressed them.
    """
    if anchors.title_heading(document) is None:
        return ""
    section = (document.get("sections") or ())[0]
    key = section.get("key")
    anchor = escape_attribute(anchors.block_anchor(key, TITLE_POSITION))
    return f' id="{anchor}"{narration.attribute(key, (TITLE_POSITION,))}'


def meta(document: dict) -> str:
    """Return the masthead's quieter second line — which a unit page does not have.

    ⛔ **No address slugs**: builder identifiers joined by middle dots, while
    the trail above the title already names every container by its title.
    ⛔ **No variant**: shown alone under the title it is a bare kind word
    (`prose`, `java`) that tells a reader nothing the page does not.
    ⭐ The variant is still the page's, in R4's identity block and the served
    document; it is just not printed as a line of its own.
    """
    del document
    return ""


def pending(document: dict) -> str:
    """Return the panel that says a unit is short, or `''` when it is not.

    ⛔ **Without this the page lies by omission.** A unit whose practices were
    never ingested renders exactly like a unit that has none: lesson, then the
    end, which reads as finished. ⚠️ `declared is None` is *also* outstanding
    and not fine — nothing having said how many practices a unit has is not the
    same as it having none.
    """
    practices = document.get("practices")
    if not isinstance(practices, dict):
        return ""
    archived = practices.get("archived") or 0
    declared = practices.get("declared")
    if declared is not None and archived >= declared:
        return ""
    if declared is None:
        count = (
            f"{_practices(archived)} here. The material does not say how many this "
            f"unit has, so it cannot be called finished."
        )
    else:
        count = f"{_practices(archived)} here, {declared - archived} still to come."
    return templates.fill(PENDING_TEMPLATE, count=escape(count))


def _practices(number: int) -> str:
    """`1 practice is` or `3 practices are`, in the reader's words (P2).

    ⛔ Not `archived`: that is the builder's word for how the material reached
    this page, and a reader was never told what it meant.
    """
    return f"{number} practice is" if number == 1 else f"{number} practices are"


def player(body: str, narration: Narration = SILENT) -> str:
    """Return the narration transport, or `''` when this page has nothing to play.

    ⭐ Whether its clips are here is the page's own question, asked of its
    first clip in the browser (`narration-probe.js`), and never a file's.

    ⛔ **Derived from the body, never from a document field** — see this
    module's docstring for why the gate is here rather than in a key this module
    would have had to invent one milestone early. ⭐ **The derivation survived
    the three narration states unchanged**, and that is the payoff for writing it as one: a
    promised clip that is not on disk emits an *empty* `AUDIO_ATTRIBUTE`, so the
    body carries the attribute and the transport arrives with no new gate.

    ⚠️ The gap notice rides inside this region rather than in a slot of its own, so
    a page can never say *"some narration is missing"* with no transport to say it about.
    """
    if AUDIO_ATTRIBUTE not in body:
        return ""
    gap = _region(narration_gap(narration))
    return templates.fill(PLAYER_TEMPLATE, gap=gap)


def narration_gap(narration: Narration) -> str:
    """Return the panel naming this page's unkept narration promises, or `''`.

    ⛔ **Empty is the ordinary answer and it is the whole product decision**
    (R3, R8): a corpus that was never narrated is COMPLETE, not short (§7's
    C5, §11.0), so it carries no notice — while a corpus whose audio broke says
    so. ⚠️ Before this the two rendered identically.

    ⭐ The count is composed here and the explanation is in the template, which
    is `pending`'s split exactly: a number is not prose, and prose a reader sees
    is a file (R13).
    """
    absent = len(narration.missing)
    if not absent:
        return ""
    count = (
        f"{absent} of {narration.promised} narrated passages on this page have no "
        f"audio file on disk."
    )
    return templates.fill(NARRATION_GAP_TEMPLATE, count=escape(count))


def _region(markup: str) -> str:
    """Return one optional region: exactly empty, or its markup and one newline.

    ⭐ The conditional newline lives here and nowhere else. Spread across the
    slots it is one chance per slot to emit a page that differs from its golden
    file by one blank line, which is the least interesting diff a reviewer can be
    handed. ⛔ A count here would be a second statement of `page.html`'s slot
    list, wrong the next time the skeleton grows — which it has.
    """
    return f"{markup}{JOIN}" if markup else ""


def _title(document: dict) -> str:
    """Return what this unit is called."""
    title = document.get("title")
    if not isinstance(title, str) or not title.strip():
        raise PageError("a unit page is titled, and this document records no usable title")
    return title


def _sections(document: dict) -> list:
    """Return the sections to render, in the order the document records them.

    ⛔ **The document's order is used, never re-derived.** `unit.builder` splits
    derived ordering from authored ordering into two modules precisely so that
    nothing downstream re-computes it; a renderer that sorted would be the
    second orderer that module exists to prevent.
    """
    sections = document.get("sections")
    if not isinstance(sections, list) or not sections:
        raise PageError(
            "a unit page renders at least one section; a document with none would "
            "render as a title and nothing else, which is indistinguishable from a "
            "unit that has nothing to say"
        )
    return sections
