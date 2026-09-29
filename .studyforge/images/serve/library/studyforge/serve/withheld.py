r"""Withheld: a quiz's key and its sentences leave the serving process only in its own page.

**What it does.** Says what of a quiz the SITE may never serve — which option is
keyed (`correct`) and each option's sentence (`says`) — and applies that on the
two ways a document leaves this process:

- `redacted(document)` — a unit document with every quiz option cut down to its
  id and its words, which is what the content namespace answers;
- `carries(body, marks)` — whether a file's bytes hold a served quiz's key or one
  of its sentences, which is what the static mount refuses;
- `OutputGate(marks)` — a run's output, line by line, with a line that carries
  either replaced by `WITHHELD_LINE`.

**How you use it.**

    document = redacted(served.parse(text, served.UNIT_FILENAME))   # content namespace
    found = marks_in(document)                                      # before redacting
    if carries(body, found):                                        # a file's bytes
        return not_found
    withheld = refused_by(source)       # what `serve.app` hands the static mount

**Depends on.** The standard library only. ⛔ It imports no route and no reader,
so `routes.content` and `routes.assets` can both use it and neither imports the
other.

## ⭐ THE KEY LIVES IN THE PAGE IT GRADES, AND NOWHERE ELSE THE SITE SERVES

⭐ **The register ruling (2026-09-25), reversing the key kept on the server**: a
quiz's answers and sentences live in a script local to the page that grades
it, and the browser grades. ⛔ **So the one served file that may carry a key
is a page** — `routes.assets` never asks this module about one — and **this
module closes every other URL**: the unit document on the content namespace,
the archive's `practice-M.json`, the bundle's `tests/quiz.json`, any other
file under the served root that repeats them, and a run's output.

## ⭐ Why redact one surface and refuse the other

⭐ **The content namespace ANSWERS a document**, whose contract is the unit's shape,
so it answers that shape with the two withheld fields gone: the questions and each
option's words are what the page already shows. ⛔ **The static mount answers FILES,
byte for byte** — a file is not edited on the way out, so a file that carries a
key or a sentence is refused, `404`, exactly as every other refusal there is.
⭐ **Both hold for both forms of `serve`**, because `serve.app` wires them from the
content source every form already hands it.

## ⛔ What `carries` reads, and what it cannot

1. ⭐ **The key, structurally — and ONLY beside a quiz this instance serves**: a
   JSON `"correct": true|false` pair, or a `data-…-correct` attribute, in a
   file that ALSO names one of the served
   quizzes' question ids, quoted (`"q-1"`). ⛔ **Either half alone withholds
   nothing**: a question id is on every quiz page
   and is no secret, and a `"correct"` field with no quiz id is some other
   data's own field — a code practice's test cases must not answer a
   silent `404`. ⭐ Both together are a quiz record carrying its
   key, which is exactly what the archive, the bundle and a quoting document
   hold. ⚠️ So a key for a quiz this instance does NOT serve is served: it is
   the key to nothing the site grades. ⛔ A key with no structure — prose
   saying *"the answer is b"* — is not detectable either.
2. ⭐ **Every sentence of every quiz the instance serves**, found in the text
   raw, JSON-escaped or HTML-escaped, with every run of whitespace read as one
   space — so a hard-wrapped quotation is still found. ⚠️ **A sentence shorter
   than `MIN_WORDS` words is not searched for**: a sentence as short as
   *"Right."* would withhold every page that happens to say it.

⚠️ **A page is not read here at all**, so a page that carried ANOTHER page's
key would be served: that is a build's property, asserted where pages are
rendered (`tests/studyforge/render/page/test_quiz.py`), never a served one.

⛔ **The files on disk are never touched** (R3): a reader who holds the corpus
checkout can open the bundle, and the rule is about what the SITE serves.
"""

from __future__ import annotations

import copy
import html
import json
import re
from collections.abc import Callable, Iterator
from dataclasses import dataclass

#: Where a quiz record keeps its questions, and a question its options.
QUESTIONS = "questions"
OPTIONS = "options"

#: The two fields of an option the site never serves: the key and the sentence.
KEY = "correct"
SAYS = "says"
WITHHELD = (KEY, SAYS)

#: A sentence shorter than this, in words, is not searched for in a file.
MIN_WORDS = 4

#: The key as structure: a JSON pair, or an older page's option attribute.
KEY_PATTERN = re.compile(r'"correct"\s*:\s*(?:true|false)\b|\bdata-[a-z-]*-correct\b')


@dataclass(frozen=True)
class Marks:
    """What identifies the quizzes an instance serves: every sentence and every question id."""

    sentences: frozenset[str] = frozenset()
    questions: frozenset[str] = frozenset()

    def __or__(self, other: Marks) -> Marks:
        """Return both sets of marks, as one instance serving both corpora holds them."""
        return Marks(self.sentences | other.sentences, self.questions | other.questions)


def quiz_questions(document: object) -> Iterator[dict]:
    """Yield every question dict of every quiz a unit document's sections carry.

    ⭐ Read RAW, never validated: what is withheld must not depend on the record
    being valid, or an invalid quiz would be served whole.
    """
    sections = document.get("sections") if isinstance(document, dict) else None
    for section in sections if isinstance(sections, list) else ():
        workspace = section.get("workspace") if isinstance(section, dict) else None
        questions = workspace.get(QUESTIONS) if isinstance(workspace, dict) else None
        for question in questions if isinstance(questions, list) else ():
            if isinstance(question, dict):
                yield question


def quiz_options(document: object) -> Iterator[dict]:
    """Yield every option dict of every quiz a unit document's sections carry."""
    for question in quiz_questions(document):
        options = question.get(OPTIONS)
        for option in options if isinstance(options, list) else ():
            if isinstance(option, dict):
                yield option


def marks_in(document: object) -> Marks:
    """Return every option sentence and every question id of every quiz in a unit document."""
    return Marks(
        frozenset(o[SAYS] for o in quiz_options(document) if isinstance(o.get(SAYS), str)),
        frozenset(q["id"] for q in quiz_questions(document) if isinstance(q.get("id"), str)),
    )


def redacted(document: object) -> object:
    """Return `document` with every quiz option's key and sentence removed.

    ⛔ **A copy**: the document handed in is not changed, so a caller that reads
    the same document for another purpose still has its whole record.
    """
    answered = copy.deepcopy(document)
    for option in quiz_options(answered):
        for field in WITHHELD:
            option.pop(field, None)
    return answered


def spellings(sentence: str) -> set[str]:
    """Return how `sentence` can appear in a served text, whitespace collapsed."""
    forms = {
        sentence,
        json.dumps(sentence)[1:-1],
        json.dumps(sentence, ensure_ascii=False)[1:-1],
        html.escape(sentence, quote=True),
        html.escape(sentence, quote=False),
    }
    return {collapsed(form) for form in forms}


def collapsed(text: str) -> str:
    """Return `text` with every run of whitespace read as one space."""
    return " ".join(text.split())


def searched(sentences: frozenset[str]) -> frozenset[str]:
    """Return every spelling of every sentence long enough to be searched for."""
    return frozenset(
        form
        for sentence in sentences
        if len(sentence.split()) >= MIN_WORDS
        for form in spellings(sentence)
    )


def marks_of(source: object) -> Marks:
    """Return what a content source says marks its quizzes; none if it cannot say.

    ⚠️ Duck-typed, because a test's stand-in source serves no quiz: both real
    sources (`routes.content.CorpusContent`, `addressing.CorporaContent`) answer.
    """
    found = getattr(source, "withheld", None)
    return Marks() if found is None else found()


def carries(body: bytes, marks: Marks) -> bool:
    """Say whether `body` holds a served quiz's key, or any of its sentences in any spelling."""
    text = body.decode("utf-8", errors="replace")
    if KEY_PATTERN.search(text) and any(f'"{one}"' in text for one in marks.questions):
        return True
    forms = searched(marks.sentences)
    if not forms:
        return False
    flat = collapsed(text)
    return any(form in flat for form in forms)


def refused_by(source: object) -> Callable[[bytes], bool]:
    """Return the static mount's `withheld` for an instance serving `source`'s corpora.

    ⭐ **The marks are asked for per file, never captured once**, so a quiz
    added or edited while the instance serves is withheld from then on.
    """
    return lambda body: carries(body, marks_of(source))


#: What a run's output says in place of a line that carries a key or a sentence.
WITHHELD_LINE = "--- a line is withheld here: it carries a quiz's key ---"


class OutputGate:
    """A run's output, one line at a time, never carrying a served quiz's key.

    ⛔ **A run's output is whatever a corpus program prints**, and the
    runner binds the whole corpus root — so a program that prints a bundle
    would hand the reader its key through the run route. ⭐ Each line is
    read as `carries` reads a file, and ⛔ **a line is judged with the run's
    earlier lines behind it**: a pretty-printed bundle puts the question id and
    the key on different lines, so once a served question id has been printed,
    every later line spelling a key is withheld too. ⚠️ A key printed BEFORE any
    question id of its quiz is not detected — the order the bundle's own shape
    never takes — and neither is anything `carries` cannot read.
    """

    def __init__(self, marks: Marks) -> None:
        """Hold the marks of the quizzes this instance serves; no id has been printed yet."""
        self.marks = marks
        self.named = False

    def __call__(self, line: str) -> str:
        """Return `line`, or `WITHHELD_LINE` when it carries a key or a sentence."""
        keyed = KEY_PATTERN.search(line) is not None
        named = any(f'"{one}"' in line for one in self.marks.questions)
        self.named = self.named or named
        if (keyed and self.named) or carries(line.encode("utf-8"), self.marks):
            return WITHHELD_LINE
        return line
