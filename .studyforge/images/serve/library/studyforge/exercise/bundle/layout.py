r"""Where an authored exercise lives in a corpus repository, and what may be in it.

**What it does.** Turns *(address, variant, unit, ordinal)* into the two
directories an authored exercise occupies — the **bundle**, which is authoring
material and is never edited by a reader, and the **workspace**, which is the
reader's own files — and closes the set of files a bundle may hold.

**How you use it.**

    places = Places(address, "prose", unit=2, ordinal=1)
    places.bundle                       # 'exercises/demo/prose/unit-02/practice-1'
    places.workspace                    # 'practice/demo/prose/unit-02/practice-1'
    places.role_path(STARTER, "Bitmap.java")   # '<bundle>/starter/Bitmap.java'
    places.in_workspace("Bitmap.java")  # '<workspace>/Bitmap.java'
    places.build_path("pom.xml")        # '<bundle>/build/pom.xml'
    unpermitted(root, places)           # every file in the bundle the shape forbids

**Depends on.** `address` for `Address`, `unit_name` and `require_ordinal`,
`corpus.placement` for the workspace segment, `exercise.gates` for the four
role names a `code` bundle writes, `exercise.safety` for what a path may be,
`exercise.errors`. Standard library otherwise.

## ⛔ Both directories are DERIVED, so nobody picks a slug

⚠️ R19: anything a second source would have to retype is a hole in the skills,
and a per-exercise directory name is the purest instance — two authors, two
conventions, and a corpus whose exercises cannot be found by arithmetic. ⭐ So
the two roots are computed from the identity the archive document already
carries, segment for segment the way `skills.adapter.Layout` computes an
archive path. ⛔ The bundle **records** that identity as well, and
`validate.exercises` compares the two: an address is recorded, never derived
(§6), and the same rule applies to the directory holding a bundle.

## ⛔ THE READER'S FILE IS NOT THE STARTER, AND THAT IS WHY THERE ARE TWO ROOTS

⚠️ **If the reader edited the bundle's own starter, the gate record's digest of that starter would
drift the moment anybody did the exercise**, and `studyforge validate` would
report every worked corpus as broken. ⭐ So the bundle holds the **pristine**
starter and `emit` writes a copy into the workspace; the record's `main_path`
names the copy. ⛔ Every file the gate record digests is therefore one no reader
touches, which is what makes *"every shipped exercise's gate record
verifies against the files beside it"* a reading that stays true.

## ⛔ A BUNDLE'S FILE SET IS CLOSED, SO NO RUN REPORT IS COMMITTED

⚠️ **A JUnit report carries the machine's hostname** — `pytest --junit-xml` and
surefire both write `hostname="…"` — and a corpus repository is where this
repository's own personal-data gate never looks (R7). ⛔ **The remedy
is mechanical here rather than a sentence in a guide:** a bundle may hold
`bundle.json`, `statement.md`, `gates.json` and files under `starter/`,
`reference/`, `tests/` and `plants/`, and `unpermitted` names anything else, so
a run's report committed into a bundle is refused by `studyforge validate`.
⭐ **The report is a run artifact**: `document` requires its path to be in the
workspace, which is not in the bundle at all.

## ⭐ AN EXERCISE'S DEPENDENCIES ARRIVE THROUGH ITS BUILD ROLE

⛔ **An exercise whose tests import a library needs a build declaration, and
nothing else in the bundle could carry one.** ⭐ So `build/` is the one
directory the closed set gained: the files a build tool reads — a `pom.xml`,
a `build.gradle.kts`, a `pyproject.toml` — declared by name in `bundle.json`,
digested by the gate record like every other input, and laid into the
reader's workspace beside the starter and the tests. ⛔ **The framework never
reads one**: it is corpus data a tool reads, so no line here branches on a
language or a library (R1). The dependencies themselves are never files of
the corpus — the runner image's prime carries them, warmed from the
same declaration, so a graded run resolves them with no network.

## ⛔ EVERY RUN'S OUTPUT LANDS IN ONE DIRECTORY, AND IT NEVER SITS IN A BUNDLE

⭐ **`RUN_OUTPUT_DIRNAME` is the one convention a report path follows**:
a bundle's report is under it, so the ignore rule a corpus
writes for its run artifacts is the one line `RUN_OUTPUT_IGNORE`, written once
and never per exercise. ⚠️ `target` because it is where Maven writes with no
configuration and already where `skills.execution.prime` refuses to take a
specimen from; every other tool is told the directory in its own build file or
command. ⛔ **A bundle file under a directory of that name is refused** by
`unpermitted`, whichever role's directory it sits in — so the widened set
still cannot hold a run's report, and the hostname reason survives.

## ⚠️ A PLANT IS FILED BY ORDINAL, NEVER BY CASE ID

⛔ **A case id is corpus data of an unbounded shape**: `CASE_ID`
permits `. _ - : # $ / @ + = , ( ) [ ]`, so a valid case id can spell an
absolute path. ⭐ A plant's directory is therefore `plants/edge-N`, N being the
edge case's position in the record's own `cases`, and the case id reaches the
gate record's **role** — where `gates.require_role` already bounds it — and
never a path segment.
"""

from __future__ import annotations

from dataclasses import dataclass

from studyforge.address import Address, require_ordinal, unit_name
from studyforge.corpus.placement import PRACTICE_DIRNAME
from studyforge.exercise.cases import Case
from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.gates import REFERENCE, STARTER, TESTS
from studyforge.exercise.safety import require_path

#: ⛔ **The directory holding every bundle, and its ONE spelling.** At the
#: corpus root beside `corpus.json` and the archive root: a bundle is authored
#: material a corpus commits, not something a build writes, so it does not live
#: under the generated root.
BUNDLES_DIRNAME = "exercises"

#: The bundle's own document, which `document` reads.
BUNDLE_FILENAME = "bundle.json"

#: The statement, as Markdown. ⭐ Authored once and emitted into the practice
#: document's blocks, so the page and the bundle cannot disagree (R19).
STATEMENT_FILENAME = "statement.md"

#: ⛔ The gate record the authoring gates write and `studyforge validate` re-reads.
#: Beside the bundle rather than a key of the exercise record, so the two halves
#: stay apart; this is its filename.
GATES_FILENAME = "gates.json"

#: The role a bundle's statement takes in the gate record's inputs.
STATEMENT = "statement"

#: ⚠️ `STARTER`, `REFERENCE` and `TESTS` (the role the grader's own files take)
#: are the gates' spellings, taken from that surface rather than re-minted.

#: ⭐ The build role: the files a build tool reads to resolve the
#: exercise's dependencies, laid into the workspace at the same relative path.
BUILD = "build"

#: The sub-directory each role's files sit under, in the order a record writes
#: them. ⛔ `plants/` is keyed by the edge case's ordinal, never by its id.
ROLE_DIRNAMES = {STARTER: STARTER, REFERENCE: REFERENCE, TESTS: TESTS}

#: ⛔ **The one directory every run artifact of an exercise lands in**, inside
#: its workspace — the report included. Never a bundle's.
RUN_OUTPUT_DIRNAME = "target"

#: ⭐ The one ignore line that keeps every run artifact out of every commit, in
#: git's own pattern syntax: a directory of that name at any depth below the
#: ignore file that carries it.
RUN_OUTPUT_IGNORE = f"{RUN_OUTPUT_DIRNAME}/"

#: Where a plant's files sit, one directory per edge case.
PLANTS_DIRNAME = "plants"

#: What a plant's own directory is called, given an edge case's position.
PLANT_DIRNAME = "edge"

#: ⛔ The files a bundle may hold at its top level, and nothing else.
BUNDLE_FILENAMES = (BUNDLE_FILENAME, STATEMENT_FILENAME, GATES_FILENAME)

#: ⛔ The directories a bundle may hold, and nothing else.
BUNDLE_DIRNAMES = (STARTER, REFERENCE, TESTS, PLANTS_DIRNAME, BUILD)


@dataclass(frozen=True, slots=True)
class Places:
    """The two roots one authored exercise occupies, and every path inside them.

    ⛔ Frozen and computed: nothing here reads the disk, so a caller asking
    where a bundle *would* be gets the same answer as one asking where it is.
    """

    address: Address
    variant: str
    unit: int
    ordinal: int

    @property
    def tail(self) -> str:
        """The part both roots share: `<address>/<variant>/unit-NN/practice-M`."""
        return (
            f"{self.address.key}/{self.variant}/{unit_name(self.unit)}/"
            f"practice-{require_ordinal(self.ordinal)}"
        )

    @property
    def bundle(self) -> str:
        """Where the authoring material sits, relative to the corpus root."""
        return f"{BUNDLES_DIRNAME}/{self.tail}"

    @property
    def workspace(self) -> str:
        """Where the reader's own files sit, relative to the corpus root."""
        return f"{PRACTICE_DIRNAME}/{self.tail}"

    @property
    def document(self) -> str:
        """The bundle's own document."""
        return f"{self.bundle}/{BUNDLE_FILENAME}"

    @property
    def statement(self) -> str:
        """The statement, relative to the corpus root."""
        return f"{self.bundle}/{STATEMENT_FILENAME}"

    @property
    def gates(self) -> str:
        """The gate record, relative to the corpus root."""
        return f"{self.bundle}/{GATES_FILENAME}"

    def in_bundle(self, path: str) -> str:
        """Return a bundle-relative path as the corpus root sees it."""
        return f"{self.bundle}/{path}"

    def in_workspace(self, path: str) -> str:
        """Return a workspace-relative path as the corpus root sees it."""
        return f"{self.workspace}/{path}"

    def role_path(self, role: str, path: str) -> str:
        """Where one role's copy of a workspace file sits, **inside the bundle**.

        ⛔ Bundle-relative, because that is the space a gate record's `inputs`
        are digested in: `validate` re-digests them against the bundle root.
        """
        directory = ROLE_DIRNAMES.get(role)
        if directory is None:
            raise ExerciseError(
                f"a bundle files {list(ROLE_DIRNAMES)} and its plants, and nothing "
                f"else. The role named is not one of them."
            )
        return f"{directory}/{path}"

    def build_path(self, path: str) -> str:
        """Where one build file sits, **inside the bundle**: `build/<path>`."""
        return f"{BUILD}/{path}"

    def plant_path(self, position: int, path: str) -> str:
        """Where the solution planted to fail the `position`-th edge case sits."""
        return f"{PLANTS_DIRNAME}/{plant_dirname(position)}/{path}"


def plant_dirname(position: int) -> str:
    """Return `edge-N` for the N-th edge case, counted from one.

    ⛔ **The position, never the case id**: a case id permits `/`
    and `:`, so one spelled as a path would put a plant outside its bundle.
    """
    return f"{PLANT_DIRNAME}-{require_ordinal(position)}"


def plant_positions(cases: tuple[Case, ...]) -> dict[str, int]:
    """Return each edge case's id mapped to its position among the edges, from one."""
    return {case.id: n for n, case in enumerate(edges_of(cases), start=1)}


def edges_of(cases: tuple[Case, ...]) -> tuple[Case, ...]:
    """Return the edge cases, in the order the record declares them."""
    return tuple(case for case in cases if not case.ask)


def ordinals(count: int) -> tuple[int, ...]:
    """Return the ordinals `1..count` a page's exercises take, in order."""
    return tuple(range(1, count + 1))


def require_no_gap(values: tuple[int, ...], where: str) -> tuple[int, ...]:
    """Refuse a set of ordinals that is not `1..n`, naming what it should have been.

    ⛔ **A gap is refused rather than closed.** An exercise that vanished from
    the middle of a page leaves a hole, and renumbering the survivors would
    make the loss unreadable while every reader's recorded progress moved to a
    different exercise (`progress.keys` keys off the ordinal).
    """
    ordered = tuple(sorted(values))
    if ordered != ordinals(len(ordered)):
        raise ExerciseError(
            f"{where}: a page's exercises take the ordinals {list(ordinals(len(ordered)))} "
            f"with no gap and no repeat, and these do not. A gap is a missing exercise "
            f"and renumbering the rest would move every reader's recorded progress."
        )
    return ordered


def unpermitted(root, places: Places) -> tuple[str, ...]:
    """Every file in the bundle the shape does not permit, bundle-relative, sorted.

    ⛔ **The closed set is what refuses a committed run report**: a
    JUnit report carries the machine's hostname, so a bundle that may hold
    anything is a bundle somebody commits one into. ⛔ A file under a
    `RUN_OUTPUT_DIRNAME` directory is refused wherever it sits.
    ⭐ Empty for a bundle that is not there, because *absent* is
    `validate.exercises`' finding to make and not this function's.
    """
    directory = root / places.bundle
    if not directory.is_dir():
        return ()
    found = []
    for path in sorted(directory.rglob("*")):
        if path.is_dir():
            continue
        relative = path.relative_to(directory).as_posix()
        if not _permitted(relative):
            found.append(relative)
    return tuple(found)


def _permitted(relative: str) -> bool:
    """Answer whether this bundle-relative path is one the shape allows."""
    if RUN_OUTPUT_DIRNAME in relative.split("/")[:-1]:
        return False
    head, _, tail = relative.partition("/")
    if not tail:
        return head in BUNDLE_FILENAMES
    return head in BUNDLE_DIRNAMES


def is_run_output(path: str) -> bool:
    """Answer whether a workspace-relative path is inside the run-output directory."""
    return path.split("/", 1)[0] == RUN_OUTPUT_DIRNAME


def require_inside(path: object, root: object, where: str) -> str:
    """Refuse a path that is not inside `root`, reproducing neither of them.

    ⛔ **Both values go through `require_path` BEFORE anything is quoted, and
    then nothing is quoted at all.** ⚠️ **MEASURED**: the first spelling took a
    field label and a root and put both in its message, and
    `tests/test_emission.py::test_no_refusal_reproduces_the_value_it_refused`
    read a home path straight back out of each. ⭐ Which value was refused is
    carried by `where`, which the caller composes, exactly as
    `digests.digest_of_file` records paying for.
    """
    inside = require_path(path, "a path this exercise emits", where)
    base = require_path(root, "the exercise's own workspace", where)
    if inside != base and not inside.startswith(f"{base}/"):
        raise ExerciseError(
            f"{where}: this names a path outside the exercise's own workspace "
            f"directory, so one exercise could reach another's files or the "
            f"corpus's own. Every path an exercise emits is inside its workspace. "
            f"The value is not reproduced here, since a refusal never quotes a value that may be "
            f"personal."
        )
    return inside
