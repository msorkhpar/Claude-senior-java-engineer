r"""The quiz panel: the questions, what may be answered, and the key this page grades with.

**What it does.** Renders the reader-facing surface of an exercise whose `kind`
is `quiz` — the stem, the options, the control that grades the reader's choices,
and the page's own key: which option is right and each option's sentence, as a
data script inside the quiz's section. ⛔ **No editor, no Run and no Submit, and
not disabled ones either**: a quiz has no file to open, no command to run and no
grader to submit to (the page never shows a control it cannot honour).

**How you use it.** `quiz.render(exercise, key=…, corpus=…, grader=…)` returns
the section's markup; `page.practice.render` calls it for a quiz and emits its
own panel for everything else.

**Depends on.** `studyforge.exercise.quiz` for what a question IS — the record's
own `Question` and `Option`, never a second reading of the shape — plus
`render.templates` and `render.markup`. ⛔ **Not on `serve`**: nothing about a
quiz is a server function.

## ⭐ THE KEY LIVES IN THE PAGE IT GRADES (register ruling, 2026-09-25)

⭐ A quiz's answers stay in the page's own markup, read by a script local to
that page, and grading is never a server function. This reverses
the ruling that kept the key on the local study server. ⭐ So each quiz carries
its key in ONE `<script type="application/json">` inside its own section:
`{question id: {"key": option id, "says": {option id: sentence}}}`, and
`practice-quiz.js` — the shared script, which holds no key — grades from it in
the browser, the same over `file://` and served, with no request.

⛔ **Only in this page**: never in a shared asset (`page.js` holds the rule and
no answer), never in another unit's page, and never in the unit document the
content namespace answers (`serve.withheld.redacted`).

⚠️ **Data, not code.** The JSON block is inert: a served page's script policy
admits no inline code, and a data block needs no admitting. Its text is JSON
with `<`, `>` and `&` written as `\u` escapes, so no sentence can close the
element it sits in.

## ⛔ R5's VOCABULARY IS NOT HERE EITHER

⚠️ `provenance` and `trust` are the framework's internal words and a reader is
never shown one (spec §7 §9). ⭐ The label this section carries arrives already
rendered, from `page.practice`'s one mapping over the record's own predicates.

## ⭐ The words a reader reads are the CORPUS's; the words about them are OURS

⛔ A stem, an option and a sentence come out of the document (R1). ⭐ A stem
and an option go through the prose's own inline renderer (`markup.inline`), so
`` `int` `` reads as code exactly as it does in the lesson, and every other
character is escaped. ⚠️ *Right.*,
*Not this one.*, the counting sentence and the no-script sentence are this
framework's own words about its own control, and they live in the
**templates**.
"""

from __future__ import annotations

import json

from studyforge.exercise import Exercise
from studyforge.exercise.quiz import Option, Question
from studyforge.render import templates
from studyforge.render.markup import escape_attribute, inline

#: The whole section: the questions, the control that grades them and the line
#: that says what it made of them — one element with attributes, so a file (R13).
QUIZ_TEMPLATE = "practice-quiz.html"

#: One question, and one answer it offers.
QUESTION_TEMPLATE = "practice-question.html"
OPTION_TEMPLATE = "practice-option.html"

#: What separates two rendered rows. ⚠️ The same shape `page.practice._region`
#: uses: a row is exactly its markup plus one newline, never a conditional
#: newline somewhere else (R10).
JOIN = "\n"


def render(exercise: Exercise, *, key: str, corpus: str, grader: str) -> str:
    """Return one quiz's section, or `''` for an exercise that is not one.

    ⛔ **`''` is the honest answer for a code exercise**, the same way
    `page.practice.render` answers `''` for a unit that sets no work: the two
    shapes share one surface and each renders the other as an absence rather
    than as controls a reader may not use.
    """
    if not exercise.is_quiz or not exercise.questions:
        return ""
    return templates.fill(
        QUIZ_TEMPLATE,
        key=escape_attribute(key),
        corpus=escape_attribute(corpus),
        grader=grader,
        questions=questions(exercise.questions, key),
        answers=answers(exercise.questions),
    )


def questions(asked: tuple[Question, ...], key: str) -> str:
    """Return every question, in the order the corpus wrote them.

    ⛔ **The order is the record's and is never sorted here.** A quiz reads as a
    sequence built from one passage, and re-ordering it would be this framework
    editing the material (R1).
    """
    return "".join(question(one, key) + JOIN for one in asked)


def question(asked: Question, key: str) -> str:
    """Return one question: its stem, its options, and the empty line its answer lands in.

    ⭐ **The radio group's name joins the PRACTICE KEY to the question's id**, so
    two quizzes on one page — or one quiz rendered twice — cannot share a group
    and silently unselect each other. ⚠️ Nothing is composed out of thin air:
    both halves arrive already minted, and this module neither parses nor
    re-spells either.
    """
    return templates.fill(
        QUESTION_TEMPLATE,
        id=escape_attribute(asked.id),
        stem=inline(asked.stem),
        options="".join(option(one, f"{key}:{asked.id}") + JOIN for one in asked.options),
    )


def option(offered: Option, name: str) -> str:
    """Return one answer: its id and its words.

    ⭐ Whether it is right, and its sentence, are in the quiz's one key block
    (`answers`), read by the script that grades — never on the option itself,
    where a stylesheet or a screen reader could say it before the reader chose.
    """
    return templates.fill(
        OPTION_TEMPLATE,
        id=escape_attribute(offered.id),
        name=escape_attribute(name),
        text=inline(offered.text),
    )


#: What `<`, `>` and `&` become inside the key block, so no sentence can close
#: the `<script>` element or open another. ⭐ JSON reads each back as itself.
SCRIPT_SAFE = {"<": "\\u003c", ">": "\\u003e", "&": "\\u0026"}


def answers(asked: tuple[Question, ...]) -> str:
    """Return this quiz's key as the text of its data block, in the record's order.

    ⭐ `{question id: {"key": the right option's id, "says": {option id: sentence}}}`,
    each sentence already rendered by `markup.inline` — escaped, with its inline
    code as code — so the script inserts markup this build made and nothing else.
    ⛔ Written with a fixed separator and in the record's own order, so the same
    quiz gives the same bytes every build (R10).
    """
    document = {
        one.id: {
            "key": next(option.id for option in one.options if option.correct),
            "says": {option.id: inline(option.says) for option in one.options},
        }
        for one in asked
    }
    text = json.dumps(document, ensure_ascii=False, separators=(",", ":"))
    return "".join(SCRIPT_SAFE.get(char, char) for char in text)
