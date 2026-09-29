r"""What wraps one section of a unit, and what sits above it.

**What it does.** Renders one served section — its wrapper, its blocks, the
narrated deck the archive filed against it, and the companion files it declares
— from the section record `unit.builder.parts` writes.

**How you use it.**

    from studyforge.render.page import section

    markup = section.render(document["sections"][0], placement)

**Depends on.** `page.blocks` for the blocks, `page.assets` for where the deck's
file sits, `page.anchors` for the wrapper's anchor, `unit.bare_lesson` for a
practice's lesson heading over nothing, `render.templates` for the markup, and
`page.errors`.

## ⛔ The wrapper shows its heading only when the material carries none

⚠️ **A section's `heading` is usually the archive document's title, and the
document's own first block is usually a heading saying the same words.**
Emitting both puts the sentence on the page twice. ⚠️ **The third copy that used
to sit above them both is gone**: the page's `<h1>` is now that same
opening heading, moved, so the count this paragraph is about is two and not
three. ⛔ **But a section whose material has no heading at all —
an authored `shared` section, typically — would otherwise reach the reader as an
unlabelled run of prose they cannot locate from the outline.**

⭐ **So the test is structural and never textual:** the recorded heading is shown
when the section's blocks contain no heading of any kind, and is otherwise left
to `data-label`. ⚠️ Comparing the *words* instead — *"emit it unless it matches
the first block"* — would put the heading back the day an author changed one
character, and take it away again the day they changed it back.


## ⛔ The section that OPENS the page does not print the title the page prints

⚠️ **`heads_page` is the one thing this module cannot work out for itself**, and
it is deliberately an instruction rather than a question this module re-answers.
A document states what it is in its first heading and the page states what it is
in its `<h1>`; `page.anchors.title_heading` rules whether those are one
statement, `page.document` prints it — and this module withholds it, or the
title reads twice, as it once did on every page of a rebuilt corpus. ⛔ Two
modules deciding it independently is two chances to disagree, and the page that
results either says its title twice or does not say it at all.

⛔ **Withheld from the OUTPUT and never from the walk.** The block keeps its
position, so every anchor and every clip after it is addressed exactly as before
— see `blocks.render_all`'s `omit` for what renumbering would cost.

⭐ **Only the opening section.** A later section's first heading sits nowhere
near the page's title and is that section's own name, which is what separates it
from the section above it.

## ⛔ A practice renders no empty heading

⚠️ **An exercise authored with no lesson beside it still carries the layout's
`## Lesson`**, and under it only its worked solution, a closed disclosure — so
every such practice showed a heading over nothing. ⭐ `unit.bare_lesson` names that
heading — a practice's level-2 lesson heading with nothing but disclosures
before the next level-2 heading — and it is withheld exactly as the title is:
from the output, never from the walk. ⛔ A lesson heading over any lesson
block renders as it always did, and no other section kind is asked.

## ⛔ The section's identity is `data-section`, and it is the key, not the title

⚠️ **`unit.sections` mints the key from kind and variant**, both structural,
*because* a retitled section must not renumber the audio filed under it. ⭐ The
page carries that key verbatim, so the player, progress and an
in-page link all address the same thing, and none of them has to read a heading.

## ⛔ The attachments are LINKS and sit after the section (spec C4)

⚠️ **A dataset a lesson loads, a notebook, a sample document** — files a unit
references that are neither prose nor inline media. ⭐ Spec C4: *"the page links
them for download rather than rendering them"*, so this region is a list of
links and never a figure, and it sits **after** the material because a reader
takes a file away once they know what it is for.

⛔ **Only `attachments`, never `assets`.** An asset is a file some block already
names, and the page reaches it through that block's own `src`; linking those
here would offer the reader the diagram they are already looking at
(`archive.document.MEDIA_ENTRY_KEYS`).

⚠️ **The href is asked of placement, exactly as a figure's is**, so the link
resolves relative to the page over `file://` with no server (R8) — and the file
it names is the one `generate.media` copies, because that pass copies what a
page emits.

## ⭐ The deck sits above the section, not inside it

⚠️ The unit's own video is the whole lesson in one narrated deck, so it reads as
an *alternative* to the section rather than as its first block — and keeping it
outside `<section>` keeps it out of the outline. ⛔ Per section rather than once
per unit, because the archive files a video against the document it came from
and dropping the later ones would be a silence.

⚠️ **`remote` and `poster_remote` are provenance and never reach the page.**
They are the addresses the source served, kept so a re-fetch is possible from
the document alone; rendering one would make a page fetch from the network on
open, which is the one thing R8 forbids outright.
"""

from __future__ import annotations

from studyforge.corpus.placement import ATTACHMENTS_DIRNAME
from studyforge.render import templates
from studyforge.render.markup import escape, escape_attribute
from studyforge.render.page import blocks
from studyforge.render.page.anchors import TITLE_POSITION, section_anchor
from studyforge.render.page.assets import Placement, filename
from studyforge.render.page.errors import PageError
from studyforge.render.page.narration import SILENT, Narration
from studyforge.unit import bare_lesson

#: The section kind a practice is rendered under. ⛔ The archive's word, the one
#: `page.practice.PRACTICE` selects its panel on.
PRACTICE = "practice"

#: The unit media directory a section's own deck was placed in.
DECK_KIND = "video"

#: The region listing the files this section's material comes with.
ATTACHMENTS_TEMPLATE = "attachments.html"

#: What separates two links in that region. ⚠️ Real output: the region's own
#: template is page-shaped, so its list reads as a list in the source too.
ITEM_JOIN = "\n"


def render(
    section: dict,
    placement: Placement,
    narration: Narration = SILENT,
    *,
    heads_page: bool = False,
) -> str:
    """Render one section: its deck, when it has one, then the section itself.

    ⚠️ **The section's own key is what a clip is addressed under**, which is the
    reason `data-section` is minted from kind and variant rather than from a
    heading: *"the page carries that key verbatim, so the player, progress and an
    in-page link all address the same thing"*. ⛔ Narration is
    looked up under that key and never under a title.

    ⚠️ `heads_page` says this section's first block is what the page is already
    headed by, so this render withholds it. Its default is the conservative
    answer: a caller that says nothing gets every block of its material. ⭐ Only
    `page.document` knows, because only it holds the whole page.
    """
    if not isinstance(section, dict):
        raise PageError("a served section is an object, and this one is not")
    key = section.get("key")
    contents = list(section.get("blocks") or ())
    withheld = (TITLE_POSITION,) if heads_page else ()
    if section.get("kind") == PRACTICE and (bare := bare_lesson(contents)) is not None:
        # ⛔ A practice's lesson heading over nothing but its worked solution is
        # an empty heading: withheld, never renumbered (`blocks.render_all`).
        withheld = (*withheld, bare)
    # ⛔ A practice is never narrated: whatever a record holds, its parts carry
    # no audio attribute (the register ruling that narration is lesson prose only).
    heard = SILENT if section.get("kind") == PRACTICE else narration
    body = blocks.render_all(
        contents, placement=placement, section=key, narration=heard, omit=withheld
    )
    wrapper = templates.fill(
        "section.html",
        id=escape_attribute(section_anchor(key)),
        key=escape_attribute(key),
        kind=escape_attribute(section.get("kind") or ""),
        label=escape_attribute(section.get("heading") or ""),
        heading=_heading(section, contents),
        body=body,
    )
    deck = _deck(section.get("video"), placement)
    files = _attachments(section.get("attachments"), placement)
    return blocks.JOIN.join(part for part in (deck, wrapper, files) if part)


def _attachments(attachments: object, placement: Placement) -> str:
    """Return the links to this section's companion files, or `''` when it has none.

    ⛔ **No empty region, ever.** A heading over an empty list reads as *"the
    files are missing"*, where a unit with no companion files simply has none —
    the same distinction `narration_gap` and `pending` are each built around.

    ⚠️ **A malformed entry is refused rather than dropped**, through the one gate
    every file reference on this page passes: an attachment that names no usable
    file is a link to nothing, and silently emitting no link would leave a
    reader told about a dataset that never appears.
    """
    if not isinstance(attachments, list):
        return ""
    entries = [entry for entry in attachments if isinstance(entry, dict)]
    if not entries:
        return ""
    items = ITEM_JOIN.join(_link(entry, placement) for entry in entries)
    return templates.fill(ATTACHMENTS_TEMPLATE, items=items)


def _link(entry: dict, placement: Placement) -> str:
    """One companion file, addressed relative to the page and named by its own filename.

    ⭐ **The link's text is the file's name and nothing this framework wrote**
    (R1): a corpus's own `dataset.ttl` says more to its reader than any sentence
    invented here, and inventing one would be a sentence every corpus lived with.

    ⛔ **The label is taken from the REFERENCE and never read back out of the
    href** — that would be inferring what a thing is called from where it was
    put, which is R4's own argument about paths. ⭐ Both come from `local`
    through the one gate, so a reference this page refuses is refused before
    either half of the link exists.
    """
    source = entry.get("local")
    href = placement.media(ATTACHMENTS_DIRNAME, source)
    label = filename(source)
    return f'<li><a href="{escape_attribute(href)}" download>{escape(label)}</a></li>'


def _heading(section: dict, contents: list) -> str:
    """Return the section's recorded heading, or `''` when the material has one.

    ⛔ **Structural, never textual** — see this module's docstring. `heading` is
    the only test, so a section whose first block is an `h4` counts as headed
    just as much as one whose first block is an `h2`: the reader can see where
    they are either way, which is the whole question.

    ⚠️ **The WHOLE material is asked, including a block `heads_page` withheld.**
    A section whose one heading was promoted into the page's `<h1>` is headed —
    by the page — so printing the recorded heading here would put the sentence
    back under the heading that was moved out of its way.
    """
    if any(isinstance(block, dict) and block.get("type") == "heading" for block in contents):
        return ""
    heading = str(section.get("heading") or "").strip()
    return f"<h2>{escape(heading)}</h2>{blocks.JOIN}" if heading else ""


def _deck(video: object, placement: Placement) -> str:
    """Return the unit's own narrated video, or `''` when the archive filed none.

    ⛔ `None` and *"the archive had no video"* are the same answer here and a
    different one from *"this build could not see it"* — which is why
    `unit.builder.parts` always writes the key and this reads it rather than
    inferring anything from its absence.
    """
    if not isinstance(video, dict):
        return ""
    source = video.get("src")
    if not isinstance(source, str) or not source.strip():
        return ""
    poster = video.get("poster")
    return templates.fill(
        "video.html",
        src=escape_attribute(placement.media(DECK_KIND, source)),
        poster=(
            f' poster="{escape_attribute(placement.media(DECK_KIND, poster))}"'
            if isinstance(poster, str) and poster.strip()
            else ""
        ),
        caption="",
    )
