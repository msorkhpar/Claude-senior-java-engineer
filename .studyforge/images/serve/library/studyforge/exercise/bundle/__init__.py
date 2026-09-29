r"""What an authored exercise IS on disk, and how it becomes `practice-M.json`.

**What it does.** Defines the **bundle** — the directory a corpus repository
commits for one authored exercise, carrying its statement, its starter, its
reference solution, its tests, its plants, its `cases` and the gate record
the authoring gates wrote — and the emission an adapter calls to turn one into an archive
practice document and the reader's own workspace.

**How you use it.**

    bundle = bundle_of(json.loads(text), where)
    emission = emit(root, bundle, source="demo", ingested="2026-01-05")
    emission.document                       # render this to practice-M.json
    write(root, emission, where)            # the reader's files, created not overwritten

⛔ **`studyforge validate`'s half is not called from here.** It is
`validate.exercises`, which reads a bundle through `Places` and `unpermitted`
and re-reads the gate record through `exercise.gates`.

**Depends on.** `address`, `archive.document`, `archive.blocks`,
`archive.markdown`, `corpus.placement` for the workspace segment,
`exercise.cases`, `exercise.gates`, `exercise.record` (through `build`),
`exercise.safety`, `exercise.errors`. Standard library only.

## What is in the package

| Module | Owns |
|---|---|
| `layout` | the two roots, the closed file set, where each role's copy sits, where a run writes |
| `document` | `bundle.json`: what an exercise declares about itself |
| `emit` | the archive document a bundle becomes, and the workspace it creates |

## ⛔ THE FRAMEWORK NEVER AUTHORS, AND NEVER REACHES INTO A CORPUS (R1, R2)

⭐ **Nothing here writes a statement, a test or a solution.** Authoring happens
once, at ingestion, by the exercises skill (spec §7, part 2); this
package is the **shape** that skill writes into and the **arithmetic** an
adapter reads it back out with. ⛔ No corpus is imported, named or branched on:
every source-specific fact arrives as the bundle's own data.

## ⛔ TWO ROOTS, BECAUSE THE READER'S FILE IS NOT THE STARTER

⭐ **The bundle holds the pristine material and the workspace holds the reader's
copy**, both derived from the identity the archive document already carries. ⚠️
**With one root**, the gate record's digest of the starter would drift the
moment anybody did the exercise, and `studyforge validate` would report every
worked corpus as broken. ⛔ So every file a gate record digests is one no reader
touches, which is what keeps *"every shipped exercise's gate record verifies
against the files beside it"* true after the corpus has been used.

## ⛔ A BUNDLE'S FILE SET IS CLOSED, SO NO RUN REPORT IS COMMITTED

⚠️ **A JUnit report carries the machine's HOSTNAME** — `pytest --junit-xml`
and surefire both write `hostname="…"` — and a corpus
repository is where **this** repository's personal-data gate never looks (R7).
⛔ **The remedy is mechanical in two places rather than a sentence in a
guide:** a bundle may hold `bundle.json`, `statement.md`, `gates.json` and the
files under `starter/`, `reference/`, `tests/` and `plants/`, so a committed
report is named by `validate.exercises`; and the bundle document's report path
is **workspace-relative**, so it cannot address the bundle at all. ⭐ **The
report is a run artifact, never a bundle input**, and that is now a refusal
instead of a promise.

## ⭐ AN EXERCISE'S DEPENDENCIES ARRIVE THROUGH ITS BUILD ROLE

⛔ **No file a bundle held could carry a third-party dependency**, so an
exercise whose tests import a library could not be graded. ⭐ `build/` is the
one directory the closed set gained: the build files `bundle.json` names under
`build`, digested by the gate record and laid into the workspace by `emit`.
⛔ The framework never reads one — it is corpus data a build tool reads (R1) —
and the dependencies are never corpus files: the runner image's prime carries
them, warmed from the same declaration, so a graded run resolves them
with no network. ⭐ **Every run's output, the report included, lands in
`RUN_OUTPUT_DIRNAME`** inside the workspace, so the corpus ignores every run
artifact with the one line `RUN_OUTPUT_IGNORE`, and a bundle file
under that directory is refused wherever it sits — the hostname reason survives
the wider set.

## ⭐ THE REFERENCE SOLUTION SHIPS, WITHHELD BUT PRESENT

⛔ **It is always available** and the practice panel offers it at any time,
never gated on a pass. ⭐ `emit` writes it into the practice document as a
`disclosure` block — the archive's own *present but withheld* state — so an
offline page can offer it with no run, no request and nothing recorded.
⚠️ Withholding it would be a pretence anyway: the bundle is already on the
reader's disk.

## ⚠️ A PLANT IS FILED BY POSITION, NEVER BY CASE ID

⛔ **A case id is not a path**: a valid case id permits `/` and `:`, so one spelled as a path
segment could put a plant outside its own bundle. ⭐ The directory is
`plants/edge-N`, N being the edge case's position in the record's `cases`, and
the case id reaches the gate record's **role**, where `gates.require_role`
already bounds it.

**Scope.** The exercises skill writes bundles into a corpus;
the quiz has no workspace and is a shape of its own.
"""

from __future__ import annotations

from studyforge.exercise.bundle.document import (
    BUNDLE_API,
    BUNDLE_KEYS,
    OPTIONAL_KEYS,
    Bundle,
    bundle_document,
    bundle_of,
)
from studyforge.exercise.bundle.emit import (
    REFERENCE_SUMMARY,
    SHIPPED_ROLES,
    Emission,
    emit,
    emit_page,
    write,
)
from studyforge.exercise.bundle.layout import (
    BUILD,
    BUNDLE_DIRNAMES,
    BUNDLE_FILENAME,
    BUNDLE_FILENAMES,
    BUNDLES_DIRNAME,
    GATES_FILENAME,
    PLANT_DIRNAME,
    PLANTS_DIRNAME,
    ROLE_DIRNAMES,
    RUN_OUTPUT_DIRNAME,
    RUN_OUTPUT_IGNORE,
    STATEMENT,
    STATEMENT_FILENAME,
    TESTS,
    Places,
    edges_of,
    is_run_output,
    ordinals,
    plant_dirname,
    plant_positions,
    require_inside,
    require_no_gap,
    unpermitted,
)

#: ⛔ The package's whole public surface. A consumer that has to import
#: `studyforge.exercise.bundle.layout` directly is a consumer this contract
#: failed — R17 makes the package's `__init__.py` its
#: contract, and this is what it says.
__all__ = [
    "BUILD",
    "BUNDLES_DIRNAME",
    "BUNDLE_API",
    "BUNDLE_DIRNAMES",
    "BUNDLE_FILENAME",
    "BUNDLE_FILENAMES",
    "BUNDLE_KEYS",
    "Bundle",
    "Emission",
    "GATES_FILENAME",
    "OPTIONAL_KEYS",
    "PLANTS_DIRNAME",
    "PLANT_DIRNAME",
    "Places",
    "REFERENCE_SUMMARY",
    "ROLE_DIRNAMES",
    "RUN_OUTPUT_DIRNAME",
    "RUN_OUTPUT_IGNORE",
    "SHIPPED_ROLES",
    "STATEMENT",
    "STATEMENT_FILENAME",
    "TESTS",
    "bundle_document",
    "bundle_of",
    "edges_of",
    "emit",
    "is_run_output",
    "emit_page",
    "ordinals",
    "plant_dirname",
    "plant_positions",
    "require_inside",
    "require_no_gap",
    "unpermitted",
    "write",
]
