r"""The practice panel: the reader-facing surface of one graded or ungraded exercise.

**What it does.** Renders the controls a practice is worked with — where the
reader's file is, the editor slot and its two tabs, Run and Submit, the streamed
result, and the label saying what a pass here is worth.

**How you use it.** `practice.render(section, document, placement)` returns the
panel's markup, or `''` for a section that sets no work; `page.document` joins
it after the section it belongs to.

**Depends on.** `studyforge.exercise` for what a workspace *is* — ⛔ the record's
own predicates, never a second reading of `provenance` and `trust`;
`studyforge.progress` and `studyforge.address` for the key a run is named by;
`page.mark` for the unit half of that key; `render.templates` and
`render.markup`. ⛔ **Not on `serve`**: a page that needed a server to render is
a page that fails the `file://` floor (R8), and this panel renders there — it
simply renders as the honest statement that Run and Submit are not available.

## ⛔ The statement is the SECTION; this is only the controls

⚠️ A practice's prose, its hint and its starting code are ordinary blocks and
are rendered by `page.section` like any other material (R1: every word a reader
sees that is not this framework's own structure comes out of the document). ⭐ So
this module never touches `blocks`, and a corpus that words its problem
statement differently needs no change here.

## ⛔ Three honesty requirements, and none of them is polish

1. ⛔ **The editor container is deliberately not auto-started.** It is an IDE
   with a shell, and starting one because somebody opened a reading page is not
   a decision a page gets to make. So the editor slot ships with the sentence
   that says the editor is not running and how to start it — ⭐ **a panel that
   says so is not a panel that renders blank and looks broken**, and blank is
   what an `<iframe>` pointed at a container that is down gives you.
2. ⛔ **An advisory grader is labelled** (R5). A reader must be able to tell
   *"the tests that ship with this material passed"* from *"something generated
   locally passed"*, and the two sentences are two template files.
3. ⛔ **A QUIZ renders its questions and NO run affordance** (§7).
   It carries questions in place of a workspace: no file, no editor
   window, no Run and no Submit — ⚠️ **and not disabled ones**, which is the
   same rule a reading-only unit gets below. ⭐ Its surface is
   `page.quiz`'s; this module chooses between the two and renders the label.
4. ⛔ **A reading-only unit shows NO practice control — not a disabled one.**
   A dead button is a promise the page cannot keep. A section with no
   `workspace` gets no panel at all, and Submit is emitted only where the
   record names a test command.

## ⭐ The panel is shown in the page's one workspace

⭐ A lesson's practices are listed as cards (`page.practices`), and a card opens
its practice in the one workspace the page carries: the statement on the left,
this panel on the right. ⛔ **The panel is not moved there**, and nothing in it
is re-rendered: `practice-workspace.js` shows it in the workspace's geometry,
so the editor frame it builds on opening never changes parent — a frame moved
to another parent reloads.

## ⛔ R5's keys never reach the page

⚠️ `provenance` and `trust` are the framework's vocabulary for how much a
verdict is worth; neither is a word a reader was ever told the meaning of. ⭐ So
this module asks the record's own predicates — `Exercise.graded` and
`Exercise.authoritative`, `Exercise.ships_with_material` — and chooses the
sentence. **No `data-*`
attribute carries either key**, which is what stops the words leaking back onto
the page through a stylesheet hook.

## ⛔ The practice key is ASKED FOR, never composed

⭐ `progress.practice_key` is the one composer and `page.mark.key` is the one
spelling of the unit half — the same string the read mark is filed under and the
run route parses back. This module joins them through
`address.parse_unit_key`, the address package's own inverse, so there is no
string arithmetic here and no second format anywhere on the page. ⚠️ A key
spelled twice and differing by one character simply never matches anything, with
nothing failing anywhere; that is the defect `progress.keys` records paying for.

## ⛔ The page names no API, no origin and no client file (R8)

⭐ The panel reads `window.studyforge.run` and nothing else. The execution
client is added to a served page by the **serving process**
(`serve.routes.assets`), because only a server knows it is a server — and a
built text that named the client would be a defect R8's floor reads
(`tests/studyforge/cli/serving.py`). ⚠️ Over `file://` the object is absent, the
controls stay hidden, and the panel says why.

## ⭐ THE BREAKDOWN IS DECLARED HERE AND READ BACK BY THE SCRIPT

⛔ **The counts are DERIVED and never recorded**: what a run reports
is `{case id: did it pass}`, and *main ask*, *edge cases n/m* and each failed
edge's sentence are all that map joined with the `cases` the record declares.
⭐ So this module emits every declared case once — its id, its kind and its
`says`, which is the corpus's own text (R1) — and `practice.js` marks them with
what the run said. ⚠️ **The panel never fetches anything**: a built page may
name no API and no origin (R8), so the verdicts arrive on the run's own
stream, said by `serve.routes.breakdown`.

⛔ **A reader shown *edge cases 2/3* is looking at an INCOMPLETE practice, not
at a failed one.** `progress.is_pass` is untouched and nothing here is a second
definition of a pass.

## ⭐ THE REFERENCE SOLUTION IS ALREADY IN THE DOCUMENT, AND THAT IS THE DESIGN

⚠️ `exercise.bundle.emit` ships it as a **`disclosure` block** under the
lesson, so `page.blocks.prose` draws it as a real `<details>`: closed until the
reader asks, openable with scripting off entirely, and reachable before a first
Submit because nothing gates it (spec §7 §8). ⛔ **There is
nothing for this module to add and adding one would be the second copy** — a
region here would be a second place the reference could be withheld from.
"""

from __future__ import annotations

from studyforge.address import AddressError, parse_unit_key
from studyforge.archive.scrub import PersonalDataLeak
from studyforge.exercise import RUN, TEST, Exercise, ExerciseError, from_document
from studyforge.progress import RAISES as PROGRESS_RAISES
from studyforge.progress import practice_key
from studyforge.render import templates
from studyforge.render.markup import escape, escape_attribute
from studyforge.render.page import mark, quiz
from studyforge.render.page.assets import Placement
from studyforge.render.page.errors import PageError

#: The section kind that can carry a workspace. ⛔ The archive's word, and the
#: same one `serve.routes.run.PRACTICE` selects on.
PRACTICE = "practice"

#: The panel itself: the editor slot, the controls, the status line and the
#: output region — a whole element with attributes, so a file (R13).
PANEL_TEMPLATE = "practice-panel.html"

#: The two windows, and the tab that reaches each. ⭐ The Tests tab is emitted
#: only where the record NAMES a test — the same honesty Submit already gets
#:: a tab over a file the material does not have is a dead control.
#: ⛔ The tablist ships hidden; it is shown only where a running editor answered.
TABS_TEMPLATE = "practice-tabs.html"
TESTS_TAB_TEMPLATE = "practice-tab-tests.html"

#: The two acts, one template each. ⭐ The LABEL is markup and the MODE is
#: `exercise`'s own word, substituted in — so the vocabulary the client posts
#: under is spelled once, in the package that owns it.
ACT_TEMPLATES = {RUN: "practice-run.html", TEST: "practice-submit.html"}

#: `what this record IS -> the sentence a reader is shown` (spec §7 §9). ⛔ **One file per
#: case** (R13), keyed on
#: the record's own PREDICATES and never on `provenance` or `trust`, so neither
#: R5 key can reach the page through this mapping — and no `data-*` attribute
#: carries one either, which is what stops the words leaking back through a
#: stylesheet hook.
GRADER_TEMPLATES = {
    "shipped": "practice-grader-shipped.html",
    "bundled": "practice-grader-bundled.html",
    "user": "practice-grader-user.html",
    "generated": "practice-grader-generated.html",
    "quiz": "practice-grader-quiz.html",
    "none": "practice-grader-none.html",
}

#: The breakdown a Submit is reported in, and the one declared case inside it.
#: ⛔ Emitted only where the record declares both halves (`Exercise.breaks_down`):
#: a region with nothing to report is the dead control this panel refuses.
BREAKDOWN_TEMPLATE = "practice-breakdown.html"
CASE_TEMPLATE = "practice-case.html"

#: What closes an optional region inside the panel. ⚠️ The same shape
#: `page.document._region` uses, and for the same reason: a region is exactly
#: empty or exactly its markup plus one newline, never a conditional newline
#: somewhere else (R10).
JOIN = "\n"


def render(section: dict, document: dict, placement: Placement) -> str:
    """Return one practice section's panel, or `''` where the unit sets no work.

    ⛔ Empty is the ordinary answer and it is the whole product decision: §7's
    three states are structural, and a unit with no exercise is COMPLETE at the
    reading floor rather than short (C5). ⭐ So a lesson, a practice with no
    workspace and a corpus that ships no graders at all each render with no
    practice affordance whatever — not a disabled one.
    """
    if not isinstance(section, dict) or section.get("kind") != PRACTICE:
        return ""
    workspace = section.get("workspace")
    if not isinstance(workspace, dict):
        return ""
    exercise = _exercise(workspace)
    key = key_of(document, section)
    if exercise.is_quiz:
        # ⛔ **A quiz is not work at a file**: it carries questions in
        # place of a workspace, so there is no file to name, nothing to open in
        # an editor, no command to Run and no grader to Submit to. ⭐ The two
        # shapes share this one surface and this one renders its questions with
        # no frame and no dead control — which is the same rule a reading-only
        # unit gets two lines above, applied to the other shape.
        return quiz.render(
            exercise, key=key, corpus=placement.corpus, grader=_region(grader(exercise))
        )
    return templates.fill(
        PANEL_TEMPLATE,
        key=escape_attribute(key),
        corpus=escape_attribute(placement.corpus),
        main=escape(exercise.main_path),
        grader=_region(grader(exercise)),
        tabs=_region(tabs(exercise)),
        controls=controls(exercise),
        breakdown=_region(breakdown(exercise)),
    )


def key_of(document: dict, section: dict) -> str:
    """Return the key this practice's runs and passes are recorded under.

    ⛔ **Composed by `progress.practice_key` and by nothing here.** The unit half
    is `page.mark.key`, which is also the string the read mark is filed under, and
    it is taken apart again by the address package's own inverse at the depth the
    document's own address states — so no separator is typed in this module.
    """
    unit = mark.key(document)
    depth = len(document.get("address") or ())
    try:
        address, ordinal = parse_unit_key(unit, depth)
        return practice_key(address, ordinal, section.get("key"))
    except (AddressError, *PROGRESS_RAISES) as error:
        # ⛔ **`progress.RAISES` is named WHOLE** (`tests/test_raises_convention.py`):
        # a handler that listed one member would stop naming the tuple the day
        # that package widens it, and the symptom is an exception nobody catches.
        # ⛔ **And `PersonalDataLeak` still travels through as itself**:
        # a caller rendering a site catches `PageError` per unit and carries on,
        # and an R7 refusal folded into that family would be logged as one more
        # page that did not render, with the leak the thing nobody looked at.
        if isinstance(error, PersonalDataLeak):
            raise
        # ⛔ Re-typed, not re-worded: `address` and `progress` own what a key is,
        # and a caller rendering a site catches one family per page. ⚠️ The
        # message carries their sentence, which describes rather than echoes
        # what arrived (R7) — this runs over every unit in a corpus and into a
        # build log.
        raise PageError(
            f"this practice cannot say what its runs would be recorded under: {error}"
        ) from None


def controls(exercise: Exercise) -> str:
    """Return the acts this workspace can actually perform, in a stated order.

    ⛔ **Run is always offered and Submit only where a test command is named**
    (and `serve.routes.run` answers `409` for the other case): a record
    carries `main_path` and `run_command` or it is not a record, while the
    grader half is written whole or not at all. ⚠️ Offering a Submit that can
    only fail is a dead button.
    """
    acts = [RUN] if exercise.test_command is None else [RUN, TEST]
    return "".join(templates.fill(ACT_TEMPLATES[act], mode=act) for act in acts)


def tabs(exercise: Exercise) -> str:
    """Return the tablist over this practice's two editor windows.

    ⭐ **Two windows of ONE editor, never a split pane**: the file a
    reader may type in and the file that judges it are two different acts of
    reading, and standing them side by side halves the width of both.

    ⛔ **The second tab is emitted only where a test is named.** ⚠️ This module
    names neither file and builds no URL: which file a window shows is decided
    by that window's own URL, which only a served origin can say (R8) — the
    tabs are the surface, and `practice.js` asks for the two addresses.
    """
    tests = templates.fill(TESTS_TAB_TEMPLATE) if exercise.test_path is not None else ""
    return templates.fill(TABS_TEMPLATE, tests=tests)


def grader(exercise: Exercise) -> str:
    """Return the sentence saying what a pass here is worth, in a learner's words.

    ⭐ **One sentence and one file per case in `GRADER_TEMPLATES`** (spec §7 §9,
    R13). ⛔ The
    ungraded case says *nothing here checks your answer* rather than saying
    nothing at all: the label is never omitted because it is unflattering, and
    a reader who is told is a reader who can tell this practice from the one
    above it.
    """
    return templates.fill(GRADER_TEMPLATES[label_of(exercise)])


def label_of(exercise: Exercise) -> str:
    """Return which label in `GRADER_TEMPLATES` this record is owed.

    ⛔ **Asked of the record's own predicates.** `is_quiz` comes first because a
    quiz's `graded` is `False` — `graded` means *a grader runs*, and a quiz's
    key checks it without one (*For dependents*).
    """
    if exercise.is_quiz:
        return "quiz"
    if not exercise.graded:
        return "none"
    if exercise.authoritative:
        return "shipped"
    # ⛔ Shipped with the material and not proven by the derivation's gates:
    # it is neither the proven grader nor one written for this site (R5).
    if exercise.ships_with_material:
        return "bundled"
    # ⛔ Written by hand: nothing proved it, so it may not borrow the sentence
    # a generated grader earns through its gates.
    return "user" if exercise.written_by_hand else "generated"


def breakdown(exercise: Exercise) -> str:
    """Return the region a Submit's breakdown is drawn in, or `''` where none can be.

    ⛔ **Every declared case is emitted, passed or not**, because the whole
    report is *these are the things this checks* and a region that listed only
    the failures would read as the practice's whole content on a first Submit.
    ⚠️ The verdicts are not here: they are this run's, and they arrive on this
    run's stream.
    """
    if not exercise.breaks_down or not exercise.cases:
        return ""
    cases = "".join(
        templates.fill(
            CASE_TEMPLATE,
            id=escape_attribute(case.id),
            kind=escape_attribute(case.kind),
            says=escape(case.says),
        )
        + JOIN
        for case in exercise.cases
    )
    return templates.fill(BREAKDOWN_TEMPLATE, cases=cases)


def _exercise(workspace: dict) -> Exercise:
    """Read the section's workspace as the record it is, or refuse saying so.

    ⚠️ Read through `exercise.from_document`, which owns R5's rule by way of
    `unit.trust`. ⛔ No provenance or trust logic is spelled here; a second
    spelling of that rule is the defect `exercise.record` refuses.
    """
    try:
        return from_document(workspace, "this unit's practice workspace")
    except ExerciseError as error:
        raise PageError(f"this practice's workspace cannot be rendered: {error}") from None


def _region(markup: str) -> str:
    """Return one optional region: exactly empty, or its markup and one newline."""
    return f"{markup}{JOIN}" if markup else ""
