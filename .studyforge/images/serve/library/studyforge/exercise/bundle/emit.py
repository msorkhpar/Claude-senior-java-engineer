r"""Turning a bundle into `practice-M.json`, and into the reader's own workspace.

**What it does.** The emission an adapter calls: reads one bundle off disk and
answers with the archive document it becomes plus the files the reader works
in. ⛔ It writes nothing until it is asked to, and what it writes is new files
only (R3).

**How you use it.**

    emission = emit(root, bundle, source="demo", ingested="2026-01-05")
    emission.document                  # what an adapter renders to practice-M.json
    write(root, emission, where)       # the reader's workspace, created not overwritten

    emit_page(root, bundles, source=…, ingested=…)   # every exercise on one page

**Depends on.** `archive.document` for `build`, `archive.blocks` for the
practice layout's three headings, `archive.markdown` for the statement,
`exercise.record` for the record and R5, `exercise.cases`, `bundle.layout`,
`bundle.document`, `exercise.errors`. Standard library otherwise.

## ⛔ THE FRAMEWORK NEVER AUTHORS AND NEVER KNOWS A SOURCE (R1)

⭐ **Every source-specific fact arrives as data** — the bundle document, the
files beside it, and the four values an adapter already holds about the page.
⛔ There is no import of any corpus, no branch on one and no name of one: the
same function emits a Java exercise and a Python one, and the difference is
entirely in what the bundle says.

## ⛔ THE PRACTICE LAYOUT IS THE ARCHIVE'S, NOT A SECOND ONE

⚠️ `archive.blocks.read_layout` refuses a practice whose blocks do not carry
the three level-2 headings with exactly one fence under the last. ⭐ So the
emission composes those three headings **from `archive.blocks`' own
constants** and never spells one, which is why a practice this module emits is
one that module can read.

## ⭐ THE REFERENCE SOLUTION SHIPS IN THE PAGE, WITHHELD BUT PRESENT

⛔ **It is always available**, and the practice panel offers it at any time
without gating on a pass. ⚠️ The page is offline and `file://`-addressable
(R8), so a reference the page did not carry would be one the reader could not
be offered — and the bundle is already on their disk, so withholding it would
be the pretence R5 exists to prevent. ⭐ It is emitted as a **`disclosure`**
block, which is the archive's own *present but withheld* state: nothing is
revealed automatically, and asking is not a run and cannot be recorded as a
failure. ⚠️ **The practice panel owns how it is drawn and what its summary reads**; this
module owns only that it is in the document.

## ⭐ THE BUILD ROLE IS LAID INTO THE WORKSPACE, AND NEVER READ

⭐ Each file `bundle.build` names is copied from the bundle's `build/` into the
workspace at the same relative path, beside the starter and the tests, so the
command a record carries finds its build declaration where the reader's own
files are. ⛔ Its bytes are not interpreted here: which tool reads it, and
what it declares, is the corpus's data (R1).

## ⛔ WHAT THE EMISSION DOES NOT WRITE

⛔ **The archive document's own path is `skills.adapter.Layout`'s**, and this
module does not compute one: a second spelling of the archive arithmetic is
the drift the one spelled layout exists to stop. ⭐ `emit` answers with the
document; where it lands is placement's decision (R2).
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

from studyforge.archive.blocks import (
    LESSON_HEADING,
    STARTING_CODE_HEADING,
    STATEMENT_HEADING,
)
from studyforge.archive.document import build
from studyforge.archive.markdown import MarkdownError
from studyforge.archive.markdown import parse as parse_markdown
from studyforge.exercise.bundle.document import Bundle
from studyforge.exercise.bundle.layout import (
    BUILD,
    STATEMENT,
    TESTS,
    edges_of,
    require_inside,
    require_no_gap,
)
from studyforge.exercise.cases import Report, cases_document, origin_document, report_document
from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.gates import REFERENCE, STARTER

#: ⛔ The summary a reader sees on the withheld reference. A framework constant
#: and never a corpus's string (R1), worded for a learner. ⚠️ The practice panel owns the
#: panel's wording and may replace this from its own vocabulary; what it may
#: not do is make the reference conditional on a pass.
REFERENCE_SUMMARY = "Show a worked solution"

#: The roles the emission digests and ships, in the order a gate record writes
#: them. ⭐ Derived from the bundle rather than listed by a caller, so there is
#: no argument by which one is left out.
SHIPPED_ROLES = (STATEMENT, STARTER, REFERENCE, TESTS, BUILD)


@dataclass(frozen=True, slots=True)
class Emission:
    """What one bundle becomes: an archive document, and the reader's files.

    ⛔ Frozen and inert. Nothing has been written when one of these exists —
    `write` is a separate act, so a caller can emit every exercise on a page
    and refuse the whole page before a single file is created.
    """

    document: dict
    files: tuple[tuple[str, bytes], ...]

    @property
    def paths(self) -> tuple[str, ...]:
        """Every path this emission would create, relative to the corpus root."""
        return tuple(path for path, _ in self.files)


def emit(
    root: Path | str,
    bundle: Bundle,
    *,
    source: str,
    ingested: str,
    lesson: tuple[dict, ...] = (),
    lesson_title: str | None = None,
) -> Emission:
    """Read one bundle and answer with what it becomes. ⛔ Writes nothing.

    `lesson` is the teaching material the page shows beside the exercise, in
    the adapter's own blocks; a page with none emits the heading and no body,
    which is what `read_layout` reads as an empty lesson section.
    """
    base = Path(root)
    where = bundle.places.bundle
    statement = _text_at(base, bundle.places.statement, where)
    starter = _text_at(base, bundle.places.in_bundle(_role_file(bundle, STARTER)), where)
    reference = _text_at(base, bundle.places.in_bundle(_role_file(bundle, REFERENCE)), where)
    tests = _text_at(base, bundle.places.in_bundle(_role_file(bundle, TESTS)), where)
    _require_plants(base, bundle, where)
    built = tuple(
        (path, _bytes_at(base, bundle.places.in_bundle(bundle.places.build_path(path)), where))
        for path in bundle.build
    )
    document = build(
        source=source,
        address=bundle.address,
        variant=bundle.variant,
        unit=bundle.unit,
        kind="practice",
        ordinal=bundle.ordinal,
        ingested=ingested,
        title=bundle.title,
        blocks=_blocks(bundle, statement, reference, starter, lesson, lesson_title, where),
        starting_code=starter,
        exercise=_record(bundle, where),
    )
    files = (
        (bundle.places.in_workspace(bundle.main_file), starter.encode("utf-8")),
        (bundle.places.in_workspace(bundle.test_file), tests.encode("utf-8")),
        *((bundle.places.in_workspace(path), data) for path, data in built),
    )
    return Emission(document=document, files=files)


def emit_page(
    root: Path | str,
    bundles: tuple[Bundle, ...],
    *,
    source: str,
    ingested: str,
    lesson: tuple[dict, ...] = (),
    lesson_title: str | None = None,
) -> tuple[Emission, ...]:
    """Emit every exercise on one page, refusing a set of ordinals with a gap.

    ⛔ **`1..n`, checked before anything is emitted.** A page whose exercises
    are numbered 1 and 3 has lost one, and emitting the two that remain would
    publish the loss as if it were the plan (spec §7: nothing is lost).
    """
    where = f"{source} unit {bundles[0].unit}" if bundles else source
    require_no_gap(tuple(one.ordinal for one in bundles), where)
    _require_one_page(bundles, where)
    return tuple(
        emit(
            root,
            one,
            source=source,
            ingested=ingested,
            lesson=lesson,
            lesson_title=lesson_title,
        )
        for one in sorted(bundles, key=lambda one: one.ordinal)
    )


def write(root: Path | str, emission: Emission, where: str) -> tuple[str, ...]:
    """Create every file the emission carries, refusing one that is already there.

    ⛔ **R3, and it is a refusal rather than a skip.** Generation is
    non-destructive: no existing file in a source repository is moved, renamed
    or rewritten, so an emission that would land on one stops and names it
    rather than deciding on the author's behalf which copy is the real one.
    """
    base = Path(root)
    existing = [path for path, _ in emission.files if (base / path).exists()]
    if existing:
        raise ExerciseError(
            f"{where}: the emission would write over {len(existing)} file(s) that "
            f"already exist, the first at '{existing[0]}'. Generation is "
            f"non-destructive: nothing in a source repository is rewritten, so "
            f"an emission that lands on an existing file is refused rather than "
            f"deciding which copy is the real one."
        )
    for path, data in emission.files:
        target = base / path
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
    return emission.paths


def _record(bundle: Bundle, where: str) -> dict:
    """Return the `exercise` record, with every path spelled as the corpus root sees it.

    ⛔ **R5 is applied by `archive.document.build`**, which reads this object
    through `exercise.record.from_document` — so a bundle claiming
    `generated`/`authoritative` is refused by the module that owns that rule
    and not by a second spelling of it here.
    """
    places = bundle.places
    record = {
        "main_path": places.in_workspace(bundle.main_file),
        "test_path": places.in_workspace(bundle.test_file),
        "run_command": list(_arguments(bundle, bundle.run_command, "run_command", where)),
        "test_command": list(_arguments(bundle, bundle.test_command, "test_command", where)),
        "provenance": bundle.provenance,
    }
    if bundle.trust is not None:
        record["trust"] = bundle.trust
    record["cases"] = cases_document(bundle.cases)
    record["report"] = report_document(
        Report(format=bundle.report.format, path=places.in_workspace(bundle.report.path))
    )
    record["origin"] = origin_document(bundle.origin)
    return record


def _arguments(bundle: Bundle, command: tuple[str, ...], field: str, where: str) -> tuple[str, ...]:
    """Refuse a command argument naming a path outside this exercise's workspace.

    ⚠️ **An argument with no separator is left alone**: `mvn`, `test` and `-pl`
    are not paths, and a check that treated them as one would refuse every real
    command. ⛔ An argument that IS a path is required to be inside the
    exercise's own workspace, so one exercise's command cannot reach another's
    files or the corpus's own. ⭐ This is the property the workspace-relative
    values get for free, bought here for the one value that cannot be one: a
    command runs from the corpus root, so it is spelled as that root sees it.
    """
    for argument in command:
        if "/" in argument:
            require_inside(argument, bundle.places.workspace, f"{where}: '{field}'")
    return command


def _blocks(
    bundle: Bundle,
    statement: str,
    reference: str,
    starter: str,
    lesson: tuple[dict, ...],
    lesson_title: str | None,
    where: str,
) -> list[dict]:
    """Return the practice's blocks, laid out the way `archive.blocks` reads one."""
    heading = LESSON_HEADING if lesson_title is None else f"{LESSON_HEADING}: {lesson_title}"
    return [
        {"type": "heading", "level": 2, "text": STATEMENT_HEADING},
        *_statement_blocks(bundle, statement, where),
        {"type": "heading", "level": 2, "text": heading},
        *lesson,
        {
            "type": "disclosure",
            "summary": REFERENCE_SUMMARY,
            "open": False,
            "blocks": [{"type": "code", "lang": bundle.lang, "text": reference}],
        },
        {"type": "heading", "level": 2, "text": STARTING_CODE_HEADING},
        {"type": "code", "lang": bundle.lang, "text": starter},
    ]


def _statement_blocks(bundle: Bundle, statement: str, where: str) -> list[dict]:
    """Return the statement's Markdown as blocks, refusing one the archive cannot carry."""
    try:
        return parse_markdown(statement, lang_default=bundle.lang)
    except MarkdownError as error:
        raise ExerciseError(
            f"{where}: the statement is not Markdown this archive can carry: {error}"
        ) from None


def _role_file(bundle: Bundle, role: str) -> str:
    """Return the bundle-relative path one role's copy of a workspace file sits at."""
    name = bundle.test_file if role == TESTS else bundle.main_file
    return bundle.places.role_path(role, name)


def _require_plants(base: Path, bundle: Bundle, where: str) -> None:
    """Every edge case has the planted solution `G3` was run over.

    ⛔ **Refused rather than shipped short.** A gate is as fine as the claim it
    backs (one gate per case, as per method), so an edge case whose plant is absent
    is an edge case nothing was ever proved against.
    """
    for position, _case in enumerate(edges_of(bundle.cases), start=1):
        path = bundle.places.in_bundle(bundle.places.plant_path(position, bundle.main_file))
        if not (base / path).is_file():
            raise ExerciseError(
                f"{where}: the solution planted to fail edge case {position} is not "
                f"at '{path}'. Every edge case ships with the plant 'G3' was run "
                f"over, and an edge case with none is one nothing was ever proved "
                f"against. ⚠️ The gate record names it by its case id, which is why "
                f"its directory is numbered instead."
            )


def _require_one_page(bundles: tuple[Bundle, ...], where: str) -> None:
    """Refuse a set of bundles that are not all the same page's."""
    pages = {(one.address.key, one.variant, one.unit) for one in bundles}
    if len(pages) > 1:
        raise ExerciseError(
            f"{where}: these bundles are not all one page's, so '1..n with no gap' "
            f"would be checked across pages that each number from one. Emit one "
            f"page's exercises at a time."
        )


def _bytes_at(base: Path, path: str, where: str) -> bytes:
    """Read one build file of the bundle as bytes, naming the file rather than the machine (R7).

    ⭐ Bytes, not text: a build file is copied, never interpreted, so it is not
    required to be anything the archive could carry.
    """
    try:
        return (base / path).read_bytes()
    except OSError:
        raise ExerciseError(
            f"{where}: the bundle declares a build file at '{path}' and holds none. "
            f"A build file the gate runs never read is one no gate proved."
        ) from None


def _text_at(base: Path, path: str, where: str) -> str:
    """Read one file of the bundle, naming the file rather than the machine (R7)."""
    try:
        return (base / path).read_text(encoding="utf-8")
    except OSError:
        raise ExerciseError(
            f"{where}: the bundle has no file at '{path}'. A bundle ships the "
            f"statement, the starter, the reference solution and the tests, and one "
            f"that is missing is material no gate was ever read over."
        ) from None
    except UnicodeDecodeError:
        raise ExerciseError(
            f"{where}: the file at '{path}' is not UTF-8 text, and an archive "
            f"document carries text."
        ) from None
