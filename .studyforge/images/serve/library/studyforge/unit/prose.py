r"""Every run of words a unit shows a reader, handed on one at a time: blocks and a quiz.

**What it does.** Walks the two places a served unit carries words, and returns
a copy with each run passed through a function the caller gives:

- `walk(blocks, text)`: every heading, paragraph, list item, table cell and
  disclosure summary, at any depth, and never a code block;
- `quiz(workspace, words)`: a quiz's stem and each option's words and
  sentence, and nothing that is not words (an id, the key, an origin).

**How you use it.** `unit.mentions` serves a unit's references through both,
and counts what it cannot reach through `walk`. A run that is not a string is
handed on as it came, and the function decides what to do with it.

**Depends on.** `archive.blocks` for which block types hold other blocks.
⛔ Not on `exercise`: the quiz's keys are spelled here as its record writes
them (`exercise.quiz.shape.QUESTIONS`, `exercise.quiz.questions.QUESTION_KEYS`
and `OPTION_KEYS`), and
`tests/studyforge/unit/test_prose.py` holds them to those.

## ⛔ A copy, never an edit

⭐ The archive's blocks and the exercise record are read by every consumer of
the unit, so the walk builds new lists and dicts and leaves the ones it was
handed alone. ⚠️ A workspace that is not a quiz comes back as it came.
"""

from __future__ import annotations

from collections.abc import Callable

from studyforge.archive.blocks import CONTAINER_TYPES

#: What a walk hands every run of words to.
Text = Callable[[object], object]

#: Where a quiz record keeps its questions and a question its options.
QUESTIONS = "questions"
OPTIONS = "options"

#: The keys of a question and of an option whose value is words a reader is
#: shown. ⛔ `id`, `correct` and `origin` are not words.
QUESTION_WORDS = ("stem",)
OPTION_WORDS = ("text", "says")


def walk(blocks: object, text: Text) -> object:
    """Return `blocks` with every run of words passed through `text`; a copy."""
    if not isinstance(blocks, list):
        return blocks
    return [_block(block, text) for block in blocks]


def quiz(workspace: object, words: Text) -> object:
    """Return a quiz `workspace` with every question's and option's words passed through `words`.

    ⭐ Anything that is not a quiz record, a workspace with no questions list
    included, comes back as it came.
    """
    if not isinstance(workspace, dict) or not isinstance(workspace.get(QUESTIONS), list):
        return workspace
    return {**workspace, QUESTIONS: [_question(one, words) for one in workspace[QUESTIONS]]}


def _question(question: object, words: Text) -> object:
    if not isinstance(question, dict):
        return question
    served = {**question, **_words(question, QUESTION_WORDS, words)}
    if isinstance(question.get(OPTIONS), list):
        served[OPTIONS] = [
            {**option, **_words(option, OPTION_WORDS, words)}
            if isinstance(option, dict)
            else option
            for option in question[OPTIONS]
        ]
    return served


def _words(record: dict, keys: tuple[str, ...], words: Text) -> dict:
    return {key: words(record[key]) for key in keys if key in record}


def _block(block: object, text: Text) -> object:
    if not isinstance(block, dict):
        return block
    kind = block.get("type")
    if kind in ("heading", "para"):
        return {**block, "text": text(block.get("text"))}
    if kind == "list":
        return {**block, "items": _items(block.get("items"), text)}
    if kind == "table":
        return {
            **block,
            "headers": _row(block.get("headers"), text),
            "rows": [_row(row, text) for row in block.get("rows") or []],
        }
    if kind in CONTAINER_TYPES:
        served = {**block, "blocks": walk(block.get("blocks"), text)}
        if "summary" in block:
            served["summary"] = text(block.get("summary"))
        return served
    return block


def _items(items: object, text: Text) -> object:
    if not isinstance(items, list):
        return items
    return [
        [_block(part, text) if isinstance(part, dict) else text(part) for part in item]
        if isinstance(item, list)
        else text(item)
        for item in items
    ]


def _row(row: object, text: Text) -> object:
    return [text(cell) for cell in row] if isinstance(row, list) else row
