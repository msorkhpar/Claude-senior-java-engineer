r"""A lesson's practices as one list of titled cards, and the one workspace they open in.

**What it does.** Renders the **Practice (n)** section a unit page carries when
it has practices — one card per practice, each with its title, the concepts it
practises and a slot for its status — and the one workspace every card opens
in: a bar with the practice's title, **Previous**, **Next** and **Close**.

**How you use it.** `page.document` joins its rendered sections through it:

    practices.joined(parts, sections, document, placement)   # the page's body
    practices.region(document, placement)                    # '' for a unit with none

**Depends on.** `page.anchors` for each practice's section anchor and its title,
`page.practice` for the key a practice's runs are recorded under, `exercise`
for what a record says it practises and whether it can pass, and
`render.templates` and `render.markup`. ⛔ Not on `serve` (R8).

## ⭐ A LIST TO CHOOSE FROM, AND A WORKSPACE TO WORK IN (register ruling)

⚠️ **Seven practices laid out one after another** repeated the same three
headings and a panel per practice, and gave the reader no view of what the
lesson asks. ⭐ So the page lists them first, as cards, and a card opens its
practice in a full-screen workspace: the statement on the left, the editor and
the two acts on the right. ⛔ **The practice's own material is not moved or
copied**: each practice's section and panel still follow the list, and
`practice-workspace.js` shows the chosen pair in the workspace's geometry and
hides the rest. With no script they stay where they are, under the list, so
every practice is still readable over `file://` with nothing running (R8).

## ⛔ THE STATUS IS THE READER'S OWN RECORD, NEVER THE PAGE'S

⭐ A card carries its status slot hidden, with both of its words in the markup,
and says which record it is read from (`data-practice-kind`): a code
practice's from the served origin's progress record, where a run established
it; a quiz's from the reader's browser store, where the quiz page recorded it —
nothing about a quiz is a server's (register ruling). ⛔ A built page says
nothing about any reader, so it is byte-identical whoever opens it (R10).
⚠️ **An ungraded practice carries no slot**: nothing completes it, so the
record could only ever say *not started*.

## ⛔ ONE ENTRY IN THE OUTLINE FOR THE LIST, AND ONE PER CARD

⭐ The outline lists **Practice (n)** and, under it, each practice by its title,
pointing at its CARD — where a reader chooses — rather than at the material
the workspace shows (`page.anchors`).
"""

from __future__ import annotations

from studyforge.exercise import Exercise, ExerciseError, from_document
from studyforge.render import templates
from studyforge.render.markup import escape, escape_attribute, inline
from studyforge.render.page import anchors
from studyforge.render.page import practice as practice_module
from studyforge.render.page.assets import Placement
from studyforge.render.page.errors import PageError

#: The section kind a practice is. ⛔ The archive's word.
PRACTICE = practice_module.PRACTICE

#: The list's own region, its heading's words, one card, and a card's parts.
REGION_TEMPLATE = "practices.html"
CARD_TEMPLATE = "practice-card.html"
CONCEPTS_TEMPLATE = "practice-concepts.html"
STATE_TEMPLATE = "practice-state.html"

#: The one workspace every card opens in.
WORKSPACE_TEMPLATE = "practice-workspace.html"

#: What a card says its practice is, so its status is read from the right record:
#: a quiz's from the reader's browser store, a code practice's from the server.
QUIZ_KIND = "quiz"
CODE_KIND = "code"

#: What separates two cards, and two concepts.
JOIN = "\n"


def of(document: dict) -> list[dict]:
    """Return the page's practice sections, in the document's own order."""
    return [
        section
        for section in document.get("sections") or ()
        if isinstance(section, dict) and section.get("kind") == PRACTICE
    ]


def region(document: dict, placement: Placement) -> str:
    """Return the **Practice (n)** section, or `''` for a unit that sets no practice."""
    found = of(document)
    if not found:
        return ""
    unit = document.get("title")
    cards = "".join(card(section, document, placement, unit) + JOIN for section in found)
    return templates.fill(
        REGION_TEMPLATE,
        id=escape_attribute(anchors.PRACTICES_ANCHOR),
        title=escape(anchors.practices_title(len(found))),
        cards=cards,
    )


def card(section: dict, document: dict, placement: Placement, unit: object) -> str:
    """Return one practice's card: its title, what it practises, and its status slot."""
    exercise = _exercise(section)
    quiz = exercise is not None and exercise.is_quiz
    graded = quiz or (exercise is not None and exercise.test_command is not None)
    return templates.fill(
        CARD_TEMPLATE,
        id=escape_attribute(anchors.card_anchor(section.get("key"))),
        section=escape_attribute(anchors.section_anchor(section.get("key"))),
        key=escape_attribute(practice_module.key_of(document, section)),
        corpus=escape_attribute(placement.corpus),
        kind=QUIZ_KIND if quiz else CODE_KIND,
        anchor=escape_attribute(anchors.section_anchor(section.get("key"))),
        title=inline(anchors.practice_title(section, unit)),
        concepts=_region(concepts(exercise)),
        state=_region(templates.fill(STATE_TEMPLATE) if graded else ""),
    )


def concepts(exercise: Exercise | None) -> str:
    """Return the list of what a practice practises, or `''` where its record says nothing."""
    if exercise is None or not exercise.concepts:
        return ""
    items = "".join(f"<li>{escape(one)}</li>{JOIN}" for one in exercise.concepts)
    return templates.fill(CONCEPTS_TEMPLATE, items=items)


def joined(parts: list[str], sections: list, document: dict, placement: Placement) -> str:
    """Join a page's rendered sections, with the list before its first practice.

    ⭐ **The list, then the practices' own material, then the one workspace**:
    the reader chooses from the list, and the material stays in the page for a
    reader with no script (R8). ⛔ The document's order is kept: a practice is
    never moved ahead of a section the document put before it.
    """
    listed = region(document, placement)
    if not listed:
        return JOIN.join(parts)
    first = next(
        index
        for index, section in enumerate(sections)
        if isinstance(section, dict) and section.get("kind") == PRACTICE
    )
    return JOIN.join([*parts[:first], listed, *parts[first:], workspace(document)])


def workspace(document: dict) -> str:
    """Return the one workspace every card opens in, or `''` for a unit with no practice."""
    return templates.fill(WORKSPACE_TEMPLATE) if of(document) else ""


def _exercise(section: dict) -> Exercise | None:
    """Read a practice's record, or `None` for one that names no workspace."""
    workspace_record = section.get("workspace")
    if not isinstance(workspace_record, dict):
        return None
    try:
        return from_document(workspace_record, "this unit's practice workspace")
    except ExerciseError as error:
        raise PageError(f"this practice's card cannot be rendered: {error}") from None


def _region(markup: str) -> str:
    """Return one optional part: exactly empty, or its markup and one newline."""
    return f"{markup}{JOIN}" if markup else ""
