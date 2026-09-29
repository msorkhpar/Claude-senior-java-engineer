r"""What on this page may be linked to — its anchors, and the outline that uses them.

**What it does.** Mints the anchors a unit page addresses itself by, and builds
the in-page outline from the document being rendered.

**How you use it.**

    from studyforge.render.page import anchors

    anchors.section_anchor("practice-java")     # 's-practice-java'
    anchors.block_anchor("practice-java", 3)    # 'practice-java-b3'
    anchors.outline(document)                   # markup, or ''

**Depends on.** `address` for the slug rule, `unit.sections` for the one
spelling of a heading's anchor, `render.templates` for the outline's
wrapper, `render.markup` for escaping and for the one fragment composer, and
`page.errors`. ⛔ Not on `contents`: the outline is derived from the one document
being rendered, never from `toc.json`. ⛔ Not on `render.index` either, which
imports this package — which is why an href here is `render.markup.anchor` of an
id rather than a `"#"` spelled locally.

## ⭐ Split from `page.navigation` at a seam that already existed

⚠️ One module answered four questions — anchors, the outline, the trail, the bar
— at eight lines under its ceiling. ⭐ *What may be linked to* is this module;
*where this page points* is `page.navigation`.

## ⛔ An anchor is derived from structure, never from a heading

⚠️ **A retitled section must not move every anchor beneath it.** `unit.sections`
makes exactly this argument for section keys — *"a content-derived key would
make a copy-edit silently orphan a unit's narration"* — and an anchor is the
same fact one layer out: a link into a page from a contents document, or from a
reader's own bookmark, survives an edit to the prose it points at.

⭐ So an anchor is `(section key, block position)`, both structural, and the
section key is already required to be a slug by the module that mints it.

## ⛔ These are anchors. The page renderer mints no speech id (R21)

⚠️ **Narration owns speech ids** and the extraction source's own docstring
says why the two must not be one scheme: *"two numbering schemes that agree
today are exactly the coupling that breaks silently tomorrow."* ⭐ So this
module answers the question a page has — *what may be linked to?* — and
answers it for **headings and sections only**, which is what an outline and a
cross-page link actually address.

⛔ **Deliberately not one id per block.** Minting an anchor for every paragraph
would be the speech-id scheme under another name, and its agreement with narration's would be a
coincidence
nothing checks.

## ⛔ The page is headed by the material's own title when the material states one

⚠️ **A source document states what it is in its first line**, and a unit page
states what it is in its `<h1>` — so a page that emits both prints the title
twice, which is what a reader sees first. ⭐ **The resolution is that the two are ONE statement**:
when
the page's opening section begins with a heading that is that material's own
title, that block **is** the page's heading, and the body does not print it a
second time.

⛔ **Promoted, never dropped.** *"Drop the first `<h1>`"* is the wrong rule: a unit whose
page title is one word opens with a numbered sentence that contains that word
and says more — it **restates the title and is not the same words** — so a rule that
only compared text would miss it, and a rule that deleted the block would take the material's own
numbering off the page. ⭐ Moving it keeps
every word, keeps its anchor, and leaves the `<h2>`s beneath it at the level the
document wrote them, so the page's outline becomes the document's own outline
instead of a flattened copy.

## ⛔ A TITLE DOMINATES ITS MATERIAL, AND THAT — NOT ITS LEVEL — IS THE TEST

⛔ **A level test is wrong.** A corpus may write some document titles at level 1
and others at level 2, with each document's sub-sections one level below its own
opener. ⚠️ A rule keyed on level 1 leaves the second kind reading their title
twice.

⭐ **What every such opener has in common is not a level: it is that
NOTHING ELSE IN THE MATERIAL STANDS AT ITS LEVEL.** A title dominates the
document under it; a section heading has siblings. ⛔ So the test is: the first
block is a heading, and no other heading in that section's material is at the
same level.

⚠️ **Read on the declared level and never on the rendered one.** The rendered
level is clamped to `h2..h6`, so a level-1 opener and its level-2 sections all
render as `<h2>` and would look like peers to a test that asked the clamp — and
the rule would refuse exactly the documents it exists for.

⭐ **The second clause is a heading that says the unit's title in the unit's own
words**, whether or not it has peers — because a page cannot print the same
sentence twice and call the second one content.

⚠️ **The textual clause is safe here in a way it would not be under deletion,**
and that is the whole reason `page.section` once refused one: promoting a
heading whose words are already the title changes nothing a reader can see,
while deleting it removes a line. ⛔ And an author who edits one character of a
heading that has peers gets it back in the body, which is correct — it now says
something the page's title does not.

⛔ **Positional, always.** A heading further down is content, whatever its level
and whatever its words; a section's material that opens with no heading loses
nothing, because there is nothing to move; and only the section that OPENS the
page is asked, because a later section's first heading is that section's own
name.

## ⛔ A lesson's practices are ONE list in the outline, each by its title

⚠️ **A practice's layout is three headings** — the statement, the lesson and
the starting code — and each is the same word on every practice. ⭐ Listed,
a page with seven practices gave the outline twenty-eight lines, twenty-one of
them one of three words. ⛔ So the practices are listed once, as *Practice
(n)*, with each practice under it by its title — pointing at its CARD, where a
reader chooses it (`page.practices`) — and none of their headings. ⭐ The
title is its recorded heading, or, where that only repeats the unit's title,
the heading its material opens with (`practice_title`).

## The outline stops at level 3

⚠️ Level 4 and below are sub-points within a topic. Listing them turns a rail
that can be scanned in one glance into a second document — and a second document
that disagrees with the first is worse than no rail.

"""

from __future__ import annotations

from studyforge.address import is_slug
from studyforge.render import templates
from studyforge.render.markup import anchor, escape_attribute, inline
from studyforge.render.page.errors import PageError
from studyforge.unit import heading_anchor

#: The section kind whose headings earn no line of their own (see above). ⛔ The
#: archive's word, the one `page.practice.PRACTICE` selects its panel on.
PRACTICE = "practice"

#: The practice list's own anchor, and what a practice's card's anchor is prefixed
#: with. ⛔ Neither can collide: a section's anchor is `s-…` and a block's ends
#: `-b<n>`.
PRACTICES_ANCHOR = "practices"
CARD_PREFIX = "card-"

#: The practice list's heading, *Practice (n)* (R13).
PRACTICES_TITLE_TEMPLATE = "practices-title.html"

#: Deepest heading level that earns a line in the outline.
OUTLINE_MAX_LEVEL = 3

#: Where in its section the block a page is headed by sits. ⛔ Always the first,
#: by `title_heading`'s own rule — spelled here, where that rule lives, so the
#: body that withholds the block, the anchor minted from it and the clip filed
#: under it cannot be addressed by three different numbers.
TITLE_POSITION = 0

#: The markup of the outline's region. ⛔ **A file, not an f-string** (R13):
#: the wrapper carries a product string — the word `Contents` and an
#: `aria-label` — and a product string typed in Python is a sentence every corpus
#: has to live with. ⭐ The row bodies stay in code, which is the line
#: `render/page/__init__.py` draws.
OUTLINE_TEMPLATE = "outline.html"

#: What a section's own anchor is prefixed with. ⛔ So a section can never
#: collide with a block anchor, which always reads `<key>-b<n>`.
SECTION_PREFIX = "s-"


def card_anchor(key: object) -> str:
    """Return the DOM id of a practice's card: `practice-java` -> `card-practice-java`."""
    return CARD_PREFIX + _slug(key, "a section key")


def practices_title(count: int) -> str:
    """Return the practice list's heading: *Practice (n)*, in the template's words (R13)."""
    return templates.fill(PRACTICES_TITLE_TEMPLATE, count=str(count))


def section_anchor(key: object) -> str:
    """Return the DOM id of a whole section: `practice-java` -> `s-practice-java`."""
    return SECTION_PREFIX + _slug(key, "a section key")


def block_anchor(section_key: object, position: int) -> str:
    """Return the DOM id of one addressable block: `java`, 3 -> `java-b3`."""
    if not isinstance(position, int) or isinstance(position, bool) or position < 0:
        raise PageError("a block's position on its page is 0 or more")
    return heading_anchor(_slug(section_key, "a section key"), position)


def entries(document: dict) -> tuple[tuple[int, str, str], ...]:
    """`(level, label, href)` for the outline, in reading order.

    A section contributes one level-1 entry — ⚠️ **only on a page that has more
    than one**, because a lone section's name is already the page's title and
    listing it says nothing — and one entry per heading shallow enough to earn a
    line. ⛔ A lesson's practices are one level-1 entry, *Practice (n)*, and
    one level-2 entry per practice, its title, pointing at its card. ⭐ Every entry
    points at an anchor this page actually emitted, because both come from the
    same walk.
    """
    sections = list(document.get("sections") or ())
    out: list[tuple[int, str, str]] = []
    promoted = title_heading(document)
    practices = [s for s in sections if isinstance(s, dict) and s.get("kind") == PRACTICE]
    for index, section in enumerate(sections):
        key = section.get("key")
        if section.get("kind") == PRACTICE:
            # ⭐ The list once, then each practice by its title, pointing at its card.
            if section is practices[0]:
                out.append((1, practices_title(len(practices)), anchor(PRACTICES_ANCHOR)))
            label = practice_title(section, document.get("title"))
            out.append((2, label, anchor(card_anchor(key))))
            continue
        if len(sections) > 1:
            out.append((1, str(section.get("heading") or key or ""), anchor(section_anchor(key))))
        for position, block in enumerate(section.get("blocks") or ()):
            # ⛔ The block the page is HEADED by is not a line in the page's own
            # contents: a list whose first entry is the title above it says
            # nothing, and the entry would point at the heading a reader is
            # already looking at.
            if index == 0 and position == 0 and block is promoted:
                continue
            if not isinstance(block, dict) or block.get("type") != "heading":
                continue
            level = heading_level(block)
            if level > OUTLINE_MAX_LEVEL:
                continue
            out.append((level, str(block.get("text") or ""), anchor(block_anchor(key, position))))
    return tuple(out)


def practice_title(section: dict, unit_title: object) -> str:
    """Return the one outline line a practice section is listed by.

    ⭐ **Its recorded heading**, the practice's title — ⚠️ unless that heading
    only repeats the unit's title, as a source's own practice may record it,
    while its material opens with a heading of its own (*"Practice: Building the
    primary bitmap"*): then that heading names the practice. ⛔ Textual only in
    the way `_says` is, stripped and nothing cleverer.
    """
    recorded = str(section.get("heading") or section.get("key") or "")
    opening = _leading_heading(section)
    if opening is not None and _says(recorded, unit_title):
        return str(opening.get("text"))
    return recorded


def title_heading(document: dict) -> dict | None:
    """Return the block this page is headed by, or `None` when the material states none.

    ⭐ **The opening section's first block, when that block is the material's own
    title** — because it dominates the material, or because it says the unit's
    title in the unit's own words. See this module's docstring for the built site
    that put both clauses there and threw out a third.

    ⚠️ The **block itself** is returned rather than its text, because two
    consumers act on it — the heading the page prints and the body that must not
    print it again — and an identity is the one thing they cannot disagree about.
    """
    if not isinstance(document, dict):
        return None
    sections = document.get("sections")
    if not isinstance(sections, list) or not sections:
        return None
    opening = _leading_heading(sections[0])
    if opening is None:
        return None
    if _dominates(opening, sections[0]):
        return opening
    return opening if _says(opening.get("text"), document.get("title")) else None


def _dominates(opening: dict, section: dict) -> bool:
    """Whether nothing else in this material stands at the opening heading's level.

    ⛔ **The DECLARED level, never the rendered one** — see this module's
    docstring. ⚠️ Counted over the section's own run of blocks, which is the run
    the outline walks; a heading nested inside a quote is not a peer of the
    document's title any more than it is a line in the contents.
    """
    level = _declared_level(opening)
    peers = sum(
        1
        for block in section.get("blocks") or ()
        if isinstance(block, dict)
        and block.get("type") == "heading"
        and _declared_level(block) == level
    )
    return peers == 1


def _leading_heading(section: object) -> dict | None:
    """Return the heading a section's material opens with, or `None` when it opens otherwise."""
    if not isinstance(section, dict):
        return None
    blocks = section.get("blocks")
    if not isinstance(blocks, list) or not blocks:
        return None
    first = blocks[0]
    if not isinstance(first, dict) or first.get("type") != "heading":
        return None
    # ⛔ A heading with nothing in it is not a title, whatever it dominates:
    # promoting one would leave the page with a blank `<h1>` and the unit's own
    # name nowhere on it. It stays in the body, where an empty heading has always
    # rendered as an empty heading.
    text = first.get("text")
    return first if isinstance(text, str) and text.strip() else None


def _says(text: object, title: object) -> bool:
    """Whether a heading says the unit's title, ignoring the space around it.

    ⛔ **Nothing cleverer than stripping.** A comparison that folded case,
    punctuation or numbering would start deciding that two different sentences
    are one, and the level clause above is what catches a restatement whose words
    genuinely differ.
    """
    if not isinstance(text, str) or not isinstance(title, str):
        return False
    return text.strip() == title.strip() and bool(text.strip())


def _declared_level(block: dict) -> int | None:
    """Return the level the material wrote, or `None` when it wrote nothing usable.

    ⛔ **Not `heading_level`.** That one clamps to the range a *rendered* heading
    may take, and its floor is 2 — so a level-1 opener and the level-2 sections
    beneath it come back identical, and every document written that way would
    look like a run of peers.
    """
    level = block.get("level")
    if isinstance(level, bool) or not isinstance(level, int):
        return None
    return level


def heading_level(block: dict) -> int:
    """Clamped to h2..h6, never h1: the page's single h1 is the unit's title.

    ⚠️ Spelled here rather than in the block renderer because the outline and
    the heading itself must agree about the level, and two clamps are two
    chances to disagree by one.
    """
    try:
        level = int(block.get("level", 2))
    except TypeError, ValueError:
        level = 2
    return max(2, min(6, level))


def outline(document: dict) -> str:
    """Return the page's own contents, or `''` when nothing is worth listing.

    ⛔ One entry means a list of one, which is chrome that says nothing, so a
    document with a single section and no headings gets no outline at all.
    """
    found = entries(document)
    if len(found) < 2:
        return ""
    items = "".join(
        f'<li data-level="{level}"><a href="{escape_attribute(href)}">{inline(label)}</a></li>'
        for level, label, href in found
    )
    return templates.fill(OUTLINE_TEMPLATE, items=items)


def _slug(value: object, what: str) -> str:
    """Return an already-slug key, or refuse without quoting it.

    ⛔ **Required, never made.** `unit.sections` refuses a non-slug key when the
    section is built, for the reason that a filename is minted from it; this is
    the same rule one layer out, where a DOM id is minted from it. ⚠️ The value
    is not quoted: it comes from a file a person edits and can be a path (R7).
    """
    if not isinstance(value, str) or not is_slug(value):
        raise PageError(
            f"{what} must already be a slug, because a DOM id is minted from it and a "
            f"link into this page has to survive an edit to its prose"
        )
    return value
