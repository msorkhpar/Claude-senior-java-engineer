r"""The bundle's own document: everything an adapter needs that it cannot derive.

**What it does.** Reads and writes `bundle.json` — the one file an authoring
skill writes that says what this exercise **is**, as against what its files
contain. ⛔ It is not a second archive document: it carries no blocks, no
counts and no digest, because those are `emit`'s arithmetic over the material
beside it.

**How you use it.**

    bundle = bundle_of(json.loads(text), where)
    bundle.places.bundle            # where it sits, derived from its own identity
    bundle_document(bundle)         # the same bytes back (R10)

**Depends on.** `address`, `exercise.cases` for the case vocabulary,
`exercise.safety` for what a path and a command may be, `bundle.layout` for the
two roots, `exercise.errors`. ⛔ Not on `archive`: a bundle is corpus material
and a document is what `emit` makes of it.

## ⛔ R5 IS NOT RE-SPELLED HERE

⚠️ `record.from_document` already refuses R5's forbidden pair, through
`unit.trust`, and `record`'s own contract records what a second spelling of one
rule cost this tree. ⭐ So this module reads `provenance` and `trust` as text
and **`emit` is where they become a record** — which means a bundle claiming
`generated`/`authoritative` is refused by the code that owns that rule, at the
moment it would otherwise reach a document.

## ⛔ EVERY PATH HERE IS WORKSPACE-RELATIVE, AND THAT IS THE WHOLE SEAM

⭐ `main_file`, `test_file` and the report's path are named **as the reader's
own directory sees them** — `Bitmap.java`, not `practice/…/Bitmap.java`. ⛔ The
corpus-root spelling every consumer reads is `emit`'s, joined onto the derived
workspace root, so a bundle cannot name a file in another exercise's workspace
and an author never retypes a path arithmetic (R19).

⚠️ **The commands are the exception, and it is a decision rather than an
oversight**: a command runs from the corpus root, so its arguments are spelled
as that root sees them. ⛔ `emit` refuses any argument naming a path outside
this exercise's own workspace, which is the property the workspace-relative
values get for free.

## ⭐ `build` NAMES THE BUILD ROLE'S FILES, AND IS OPTIONAL

⭐ **An exercise whose tests need nothing but the language declares no
`build`**, and its document is exactly what it was before. One whose tests
import a library lists the build files it ships under `build/`, each
workspace-relative, and `emit` lays each into the workspace at that path.
⛔ A build file may not share a path with the main file or the test file, and
may not sit in the run-output directory, which a run overwrites.

## ⛔ THE REPORT'S PATH IS IN THE WORKSPACE, NEVER IN THE BUNDLE

⚠️ **No run report is committed, and that is answered in two places.** A JUnit report
carries the machine's hostname, and a bundle that could name one as its own
file is a bundle somebody commits one into. ⭐ Here the path is workspace-
relative, so it cannot address the bundle at all; in `layout.unpermitted` the
bundle's file set is closed, so one that arrived by hand is named by
`studyforge validate`.

⭐ **And it is inside `layout.RUN_OUTPUT_DIRNAME`**: one
convention for every exercise, so the corpus's ignore rule is one line
written once rather than one per exercise.
"""

from __future__ import annotations

from dataclasses import dataclass

from studyforge.address import Address, require_ordinal
from studyforge.describe import describe, describe_keys
from studyforge.exercise.bundle.layout import (
    RUN_OUTPUT_DIRNAME,
    Places,
    is_run_output,
    plant_positions,
)
from studyforge.exercise.cases import (
    Case,
    Origin,
    Report,
    cases_document,
    cases_of,
    origin_document,
    origin_in,
    report_document,
    report_of,
)
from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.safety import require_command, require_path
from studyforge.version import check as check_version

#: The bundle format's version. ⚠️ Bumped when a reader of the old shape would
#: be *wrong* rather than merely incomplete, following the archive document's
#: own rule.
BUNDLE_API = 1

#: ⛔ The document's key order, which is what reaches disk. Serialised
#: `sort_keys=False`, so this tuple is the format (R10).
BUNDLE_KEYS = (
    "bundle_api",
    "address",
    "variant",
    "unit",
    "ordinal",
    "title",
    "lang",
    "main_file",
    "test_file",
    "build",
    "run_command",
    "test_command",
    "provenance",
    "trust",
    "cases",
    "report",
    "origin",
)

#: The keys a bundle may leave out. ⚠️ `trust` is exactly the record's own
#: exception, for the reason `unit.trust` gives: a field an author fills in to
#: say the obvious is a field an author fills in wrongly. ⭐ `build` is absent
#: for an exercise whose tests need nothing but the language.
OPTIONAL_KEYS = ("trust", "build")


@dataclass(frozen=True, slots=True)
class Bundle:
    """One authored exercise, as its own directory declares it.

    ⛔ Frozen, not validated: `bundle_of` is what guarantees every field, and a
    caller that builds one by hand asks `bundle_of(bundle_document(...))`
    before believing it.
    """

    address: Address
    variant: str
    unit: int
    ordinal: int
    title: str
    lang: str
    main_file: str
    test_file: str
    run_command: tuple[str, ...]
    test_command: tuple[str, ...]
    provenance: str
    trust: str | None
    cases: tuple[Case, ...]
    report: Report
    origin: Origin
    build: tuple[str, ...] = ()

    @property
    def places(self) -> Places:
        """The two roots this exercise occupies, derived from its own identity."""
        return Places(self.address, self.variant, self.unit, self.ordinal)

    @property
    def plants(self) -> dict[str, int]:
        """Each edge case's id mapped to the position its planted solution is filed under."""
        return plant_positions(self.cases)


def bundle_of(value: object, where: str) -> Bundle:
    """Read one bundle document, refusing every way it can be wrong.

    ⛔ **An unknown key is refused rather than ignored**, for the archive
    document's own reason: tolerating an unknown key is tolerating a
    typo in a known one, and a typo'd `test_file` is a grader nothing runs
    while the corpus validates green.
    """
    document = _require_keys(value, where)
    address = _address(document["address"], where)
    unit = _ordinal(document["unit"], "unit", where)
    ordinal = _ordinal(document["ordinal"], "ordinal", where)
    bundle = Bundle(
        address=address,
        variant=_text(document["variant"], "variant", where),
        unit=unit,
        ordinal=ordinal,
        title=_text(document["title"], "title", where),
        lang=_text(document["lang"], "lang", where),
        main_file=require_path(document["main_file"], "main_file", where),
        test_file=require_path(document["test_file"], "test_file", where),
        run_command=require_command(document["run_command"], "run_command", where),
        test_command=require_command(document["test_command"], "test_command", where),
        provenance=_text(document["provenance"], "provenance", where),
        trust=_optional_text(document.get("trust"), "trust", where),
        cases=cases_of(document["cases"], where),
        report=report_of(document["report"], where),
        origin=_origin(document, where),
        build=_build(document.get("build"), where),
    )
    _require_derivable(bundle, where)
    _require_report_in_workspace(bundle, where)
    _require_build_apart(bundle, where)
    return bundle


def bundle_document(bundle: Bundle) -> dict:
    """Return the bundle as the decoded object it ships as, in `BUNDLE_KEYS` order (R10)."""
    document = {
        "bundle_api": BUNDLE_API,
        "address": list(bundle.address.segments),
        "variant": bundle.variant,
        "unit": bundle.unit,
        "ordinal": bundle.ordinal,
        "title": bundle.title,
        "lang": bundle.lang,
        "main_file": bundle.main_file,
        "test_file": bundle.test_file,
    }
    if bundle.build:
        document["build"] = list(bundle.build)
    document |= {
        "run_command": list(bundle.run_command),
        "test_command": list(bundle.test_command),
        "provenance": bundle.provenance,
    }
    if bundle.trust is not None:
        document["trust"] = bundle.trust
    document["cases"] = cases_document(bundle.cases)
    document["report"] = report_document(bundle.report)
    document["origin"] = origin_document(bundle.origin)
    return document


def _require_keys(value: object, where: str) -> dict:
    """Refuse a document that is not an object, or whose key set is not the format's."""
    if not isinstance(value, dict):
        raise ExerciseError(
            f"{where}: a bundle document is an object, {list(BUNDLE_KEYS)}. "
            f"The value is {describe(value)}."
        )
    unknown = [key for key in value if key not in BUNDLE_KEYS]
    missing = [key for key in BUNDLE_KEYS if key not in OPTIONAL_KEYS and key not in value]
    if unknown or missing:
        raise ExerciseError(
            f"{where}: a bundle document is {list(BUNDLE_KEYS)}, every one of them "
            f"required but {list(OPTIONAL_KEYS)}. It is missing "
            f"{describe_keys(missing)} and carries {describe_keys(unknown)} this build "
            f"does not define. An unknown key is refused rather than ignored, because "
            f"ignoring one is how a typo becomes a grader nothing runs."
        )
    check_version(
        "bundle_api", value["bundle_api"], (BUNDLE_API,), where=where, error=ExerciseError
    )
    return value


def _address(value: object, where: str) -> Address:
    """Read the address, re-raising as this package's own exception."""
    if not isinstance(value, (list, tuple)) or not value:
        raise ExerciseError(
            f"{where}: 'address' is the container's address as an array of slugs, "
            f"and it is {describe(value)}."
        )
    try:
        return Address(list(value))
    except ValueError as error:
        raise ExerciseError(f"{where}: 'address' is not one this build can read: {error}") from None


def _ordinal(value: object, field: str, where: str) -> int:
    """Refuse a unit or an ordinal that is not a counting number."""
    if not isinstance(value, int) or isinstance(value, bool):
        raise ExerciseError(f"{where}: '{field}' counts from one and is {describe(value)}.")
    try:
        return require_ordinal(value)
    except ValueError:
        raise ExerciseError(f"{where}: '{field}' counts from one.") from None


def _text(value: object, field: str, where: str) -> str:
    """Refuse an empty or non-text value, naming the field and never the value."""
    if not isinstance(value, str) or not value.strip():
        raise ExerciseError(f"{where}: '{field}' is a non-empty string and is {describe(value)}.")
    return value


def _optional_text(value: object, field: str, where: str) -> str | None:
    """Read the same, for a key a bundle may leave out."""
    return None if value is None else _text(value, field, where)


def _origin(document: dict, where: str) -> Origin:
    """Read the origin, refusing a bundle that declares none.

    ⛔ **Required here where the record leaves it optional**, and that is
    spec §7's *nothing the source has is lost* rather than a preference: `G5` resolves an authored
    exercise's origin against the source ledger, so an authored exercise with
    no origin is material the ledger cannot account for.
    """
    origin = origin_in(document, where)
    if origin is None:
        raise ExerciseError(
            f"{where}: 'origin' names the material this exercise was built from, and "
            f"an authored exercise carries one. Without it the source ledger cannot "
            f"account for the material and 'G5' has nothing to resolve."
        )
    return origin


def _require_derivable(bundle: Bundle, where: str) -> None:
    """Refuse an identity whose own directories would not be paths.

    ⚠️ The two roots are computed from the identity, so a variant or a slug that
    cannot be a path segment is refused **here**, where the field is named,
    rather than at the join, where only a composed value could be quoted (R7).
    """
    require_path(bundle.places.bundle, "the bundle's own directory", where)
    require_path(bundle.places.workspace, "the exercise's workspace", where)


def _require_report_in_workspace(bundle: Bundle, where: str) -> None:
    """Refuse a report path that is not the reader's workspace's.

    ⛔ **A run's report is a run ARTIFACT and never a bundle input.** It carries
    the machine's hostname (R7), so a bundle able to name one as its own file is
    a bundle somebody commits one into — in a corpus repository, where this
    repository's personal-data gate never looks.
    """
    require_path(bundle.report.path, "the report's path", where)
    if not is_run_output(bundle.report.path):
        raise ExerciseError(
            f"{where}: the report's path is inside '{RUN_OUTPUT_DIRNAME}/', the one "
            f"directory every run artifact of an exercise lands in, and this one is "
            f"not. One convention is what lets a corpus ignore every run's report "
            f"with one line instead of one per exercise."
        )


def _build(value: object, where: str) -> tuple[str, ...]:
    """Read the build role's file list: absent is none, present is non-empty and distinct."""
    if value is None:
        return ()
    if not isinstance(value, list) or not value:
        raise ExerciseError(
            f"{where}: 'build' lists the build files this exercise ships under 'build/', "
            f"as a non-empty array of workspace-relative paths, and it is {describe(value)}. "
            f"An exercise that needs none leaves the key out."
        )
    paths = tuple(require_path(one, "a build file", where) for one in value)
    if len(set(paths)) != len(paths):
        raise ExerciseError(f"{where}: 'build' names one file more than once.")
    return paths


def _require_build_apart(bundle: Bundle, where: str) -> None:
    """Refuse a build file that is the main file, the test file, or a run's output."""
    for path in bundle.build:
        if path in (bundle.main_file, bundle.test_file):
            raise ExerciseError(
                f"{where}: a build file shares its path with the main file or the test "
                f"file, so the workspace would hold one file for two roles."
            )
        if is_run_output(path):
            raise ExerciseError(
                f"{where}: a build file sits in '{RUN_OUTPUT_DIRNAME}/', which every run "
                f"writes and a corpus ignores, so it would be neither kept nor tracked."
            )
