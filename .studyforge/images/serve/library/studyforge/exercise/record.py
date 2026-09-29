"""The `exercise` record: a workspace, where it came from, and what it may claim.

**What it does.** Reads one `exercise` object out of a practice archive
document, refuses every way it can be wrong, and writes it back byte-stably.

**How you use it.** `of(document, where)` returns an `Exercise` or `None`;
`from_document(value, where)` reads the object itself; `to_document(exercise)`
is the round trip.

**Depends on.** `safety` for the four workspace values, **`cases` for the
vocabulary the four authored keys are written in**, `keys` for the record's
key order and the refusals of its shape, `concepts` for what an exercise
practises, and **`unit.trust` for R5's rule**.

## ⛔ R5's rule is imported, never re-spelled

⚠️ **`unit.trust` owns the provenance and trust vocabulary and the pairs R5
forbids**, so this module calls `check_test_record` and adds nothing to it. ⭐ A
second spelling of one rule is the defect `placement.names.label_of` records:
two guards, one missing character, and a failure that showed as the character.

## Where it lives, and what its presence means

⛔ An `exercise` belongs to a **practice** document (spec §7). On a lesson it is
refused, not ignored: its presence *is* the graded state, and a misplaced one is a
grader never offered, in a corpus that validates green.

⛔ **A record is one of two shapes, and nothing in between**:

| Shape | Carries | State |
|---|---|---|
| a file and how it runs | `REQUIRED_KEYS` — `main_path`, `run_command` | **ungraded** |
| that, plus a grader | every key in `EXERCISE_KEYS` | **graded** |

⭐ **A file with no test is a record and not a second declaration** because the
unit document's `workspace` is this record: the reader's file reaches the one
place every consumer already reads, with no new key anywhere.

⛔ **The grader half is written whole or not at all.** A record carrying
`test_path` and no `test_command` is not a lesser exercise; it is a grader
somebody half wrote, and it is refused, naming what is missing. ⚠️ `trust` is
the one key a grader may omit, defaulted from `provenance` by `unit.trust`,
which ruled that a field an author must fill in to say the obvious is a field
an author fills in wrongly. ⛔ **With no grader, `provenance` and `trust` are
refused too**: both are facts about a grader, and a claim of trust in a grader
that does not exist is the claim R5 exists to stop.

## ⛔ The four authored keys are `cases`'s vocabulary, not a second spelling

⭐ **The case vocabulary adds `kind`, `cases`, `report` and `origin`; every rule about
what they may *say* lives in `cases`** — the kinds, the case shape, the report
formats, and what a region of the source is. ⛔ This module keeps the
**document**: which keys are written, in what order, and which shape each one
may appear on. ⚠️ The seam is the same one `safety` and `unit.trust` sit on,
and it is why `record` did not grow four vocabularies.

⭐ **Appended, never inserted** (the archive's `OPTIONAL_KEYS` made the same
choice): the six original keys are in their old order and old position, so a
record that gains a `kind` does not reorder what was already on disk (R10).

⛔ **`cases` and `report` are a GRADER fact and are refused on a record with no
grader, naming the key.** `origin` is a fact about the material and may appear
on either shape. ⚠️ And none of the four is written where the record does not
carry it — `kind` included, because every record ever written is `code` and
writing that token would re-render every archive document in existence. The
argument is `cases`'s docstring, which owns it.

## ⛔ A quiz carries `questions` in place of a workspace

⭐ **The quiz adds the key and `exercise.quiz` owns every rule about it**, the
same seam `cases` sits on. ⚠️ A quiz names no `test_path`, so no RUN completes
it — spec §7 §7's requirement, which `quiz`'s own contract argues.

## ⭐ `concepts` says what an exercise practises, on either shape

⭐ The plan's own sentences, written where the plan is known and read by the
page's card (`exercise.concepts` argues it). ⛔ Appended and optional like the
authored keys, so a record written before it round-trips unchanged.

## ⛔ Unknown keys are refused

⭐ **Without this, an archive document carrying an unknown top-level
`exercise` object would validate green — 0 findings, 0 unchecked claims** —
because `content_sha256` is taken over `blocks` and an unknown sibling key is
not a block. ⚠️ Tolerating an unknown key means tolerating a **typo** in it, and
a typo'd `exercise` is a grader that is invisible while the corpus passes. The
same argument applies one level down, which is why this record refuses a key it
does not define.
"""

from __future__ import annotations

from dataclasses import dataclass

from studyforge.exercise import quiz
from studyforge.exercise.cases import (
    BREAKDOWN_KEYS,
    DEFAULT_KIND,
    QUIZ,
    Case,
    Origin,
    Report,
    cases_document,
    cases_of,
    kind_of,
    origin_document,
    origin_in,
    report_document,
    report_of,
)
from studyforge.exercise.concepts import CONCEPTS, concepts_in
from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.keys import (
    AUTHORED_KEYS,
    EXERCISE_KEYS,
    REQUIRED_KEYS,
    require_known_keys,
    require_present,
)
from studyforge.exercise.safety import require_command, require_path
from studyforge.exercise.states import EXERCISE_KEY, GRADER_KEY
from studyforge.unit.errors import ContentError
from studyforge.unit.trust import check_test_record


@dataclass(frozen=True, slots=True)
class Exercise:
    """One exercise: where the work lives, how it runs, and — if graded — what checks it.

    ⚠️ **The four grader fields are `None` together, or set together**,
    which is what `from_document` guarantees. ⛔ The dataclass itself does not:
    it is frozen, not validated, so a caller that constructs one asks
    `from_document(to_document(...))` before believing it.

    ⭐ **The authored fields carry their defaults**, so every caller
    written before them constructs today's exercise by saying nothing: `kind`
    is `code`, and a record built from no authored material carries none.

    ⛔ **`main_path` and `run_command` are `None` for a QUIZ and nothing else**,
    which carries `questions` in place of a workspace. ⚠️ A consumer reaching
    for either asks `is_quiz`, as it asks `graded` before the grader four.
    """

    main_path: str | None
    test_path: str | None
    run_command: tuple[str, ...] | None
    test_command: tuple[str, ...] | None
    provenance: str | None
    trust: str | None
    kind: str = DEFAULT_KIND
    cases: tuple[Case, ...] | None = None
    report: Report | None = None
    origin: Origin | None = None
    questions: tuple[quiz.Question, ...] | None = None
    #: ⭐ What it practises, in the plan's order (`exercise.concepts`).
    concepts: tuple[str, ...] | None = None

    @property
    def graded(self) -> bool:
        """Does anything check this file? ⭐ The same question `states.state_of` asks."""
        return self.test_path is not None

    @property
    def breaks_down(self) -> bool:
        """Is a Submit reported as *main ask* plus *edge cases n/m* for this exercise.

        ⛔ The two keys are one claim, so this answers for both (`cases`), and
        it answers `False` for a record with no grader however the two are set
        — there is no run whose report could be folded.
        """
        return self.graded and self.cases is not None and self.report is not None

    @property
    def is_quiz(self) -> bool:
        """Is this graded by its own key rather than by running anything? ⭐ Spec §7 §7."""
        return self.kind == QUIZ

    @property
    def authoritative(self) -> bool:
        """Is this the source's own grader? ⛔ Never true for a generated one (R5), or none."""
        return self.trust == "authoritative"

    @property
    def ships_with_material(self) -> bool:
        """Did this grader come with the material, whatever it may claim? ⭐ `bundled`."""
        return self.provenance == "bundled"

    @property
    def written_by_hand(self) -> bool:
        """Did somebody write this grader by hand, so that no gate proved it? ⭐ `user`."""
        return self.provenance == "user"


def of(document: object, where: str) -> Exercise | None:
    """Return the exercise a practice document carries, or `None` if it carries none.

    ⚠️ `None` is not a failure: it is a unit that names no file, which is the
    common case. ⛔ **An `Exercise` is not the graded answer either** — one whose
    `graded` is false names a file nothing checks. A caller wanting
    the three states asks `states.state_of`, which also answers for a document
    that does not exist.
    """
    if not isinstance(document, dict) or EXERCISE_KEY not in document:
        return None
    if document.get("kind") != "practice":
        raise ExerciseError(
            f"{where}: an 'exercise' belongs to a practice document and this one "
            f"declares kind {_kind(document)}. The key's presence is what makes a "
            f"practice graded, so one on a lesson is a grader no reader is ever "
            f"offered."
        )
    return from_document(document[EXERCISE_KEY], where)


def from_document(value: object, where: str) -> Exercise:
    """Read one `exercise` object, refusing every way it can be wrong."""
    if not isinstance(value, dict):
        raise ExerciseError(
            f"{where}: 'exercise' must be a JSON object; the value is not reproduced here, since "
            f"a refusal never quotes a value that may be personal"
        )
    require_known_keys(value, where)
    kind = kind_of(value["kind"], where) if "kind" in value else DEFAULT_KIND
    if kind == QUIZ:
        # ⛔ Every rule a quiz obeys is `exercise.quiz`'s, R5's narrowing
        # included; this branch of the reader chooses the shape and nothing else.
        provenance, trust = quiz.require_quiz_shape(value, where)
        return Exercise(None, None, None, None, provenance, trust, **_authored(value, QUIZ, where))
    quiz.require_no_questions(value, where)
    require_present(value, where)
    authored = _authored(value, kind, where)
    main_path = require_path(value.get("main_path"), "main_path", where)
    run_command = require_command(value.get("run_command"), "run_command", where)
    if GRADER_KEY not in value:
        return Exercise(main_path, None, run_command, None, None, None, **authored)
    provenance, trust = _trust(value, where)
    return Exercise(
        main_path=main_path,
        test_path=require_path(value.get("test_path"), "test_path", where),
        run_command=run_command,
        test_command=require_command(value.get("test_command"), "test_command", where),
        provenance=provenance,
        trust=trust,
        **authored,
    )


def to_document(exercise: Exercise) -> dict:
    """Return the record as the decoded object, in `EXERCISE_KEYS` order.

    ⭐ A graded record writes every key, `trust` included even where it is the
    default. A defaulted field that disappears when it is obvious cannot be
    told from one nobody wrote — the same argument the archive's `counts`
    makes — and the record that reaches disk should say what the framework
    decided it means. ⛔ An ungraded one writes `REQUIRED_KEYS` and nothing
    else: the keys are chosen by `graded`, never by which values happen to be
    `None`, so an unvalidated `Exercise` cannot write half a grader.

    ⭐ **An authored key is written only where the record carries it**, and
    `kind` counts as carried only where it is not `code`. ⛔ So a record written
    before the authored keys round-trips to the same bytes it was read from, which is what
    lets this contract land with no version bump (`cases`, and the ungraded record
    before it).
    """
    values = {
        "main_path": exercise.main_path,
        "test_path": exercise.test_path,
        "run_command": list(exercise.run_command or ()),
        "test_command": list(exercise.test_command or ()),
        "provenance": exercise.provenance,
        "trust": exercise.trust,
        "kind": exercise.kind,
        "cases": cases_document(exercise.cases or ()),
        "report": report_document(exercise.report) if exercise.report else None,
        "origin": origin_document(exercise.origin) if exercise.origin else None,
        quiz.QUESTIONS: quiz.questions_document(exercise.questions or ()),
        CONCEPTS: list(exercise.concepts or ()),
    }
    return {key: values[key] for key in _written_keys(exercise)}


def _written_keys(exercise: Exercise) -> tuple[str, ...]:
    """Which keys this record writes — chosen by its shape, never by which values are `None`."""
    concepts = {CONCEPTS} if exercise.concepts is not None else set()
    if exercise.is_quiz:
        carried = set(quiz.QUIZ_KEYS) - (set() if exercise.origin else {"origin"})
        carried = (carried - {CONCEPTS}) | concepts
        return tuple(key for key in EXERCISE_KEYS if key in carried)
    carried = set(concepts)
    if exercise.kind != DEFAULT_KIND:
        carried.add("kind")
    if exercise.breaks_down:
        carried.update(BREAKDOWN_KEYS)
    if exercise.origin is not None:
        carried.add("origin")
    shape = set(EXERCISE_KEYS if exercise.graded else REQUIRED_KEYS) - set(AUTHORED_KEYS)
    return tuple(key for key in EXERCISE_KEYS if key in shape or key in carried)


def _authored(value: dict, kind: str, where: str) -> dict:
    """Read the keys appended after the original six, each only where the record writes it."""
    # ⚠️ One display, and `origin_in` takes the whole record: `origin` is read
    # only where it is handed to its one reader, so no site here reads
    # that key and decides something.
    return {
        "kind": kind,
        "cases": cases_of(value["cases"], where) if "cases" in value else None,
        "report": report_of(value["report"], where) if "report" in value else None,
        "origin": origin_in(value, where),
        quiz.QUESTIONS: quiz.questions_in(value, where),
        CONCEPTS: concepts_in(value, where),
    }


def _trust(value: dict, where: str) -> tuple[str, str]:
    """Apply R5's rule, which `unit.trust` owns, and speak this package's error."""
    try:
        return check_test_record(value.get("provenance"), value.get("trust"))
    except ContentError as error:
        # ⛔ Re-raised, not re-worded: `unit.trust`'s message is the one that
        # states R5, and re-spelling it here is the duplication this module's
        # docstring refuses. The type changes so a caller of `exercise` catches
        # one family; the sentence does not.
        raise ExerciseError(f"{where}: {error}") from None


def _kind(document: dict) -> str:
    """Name the document's `kind` only where it is one of the two the format has."""
    kind = document.get("kind")
    return repr(kind) if kind in ("lesson", "practice") else "a value that is neither"
