r"""Walking a corpus on disk: the manifest, the container maps, the documents.

**What it does.** Turns a directory into the three things every check needs —
one `Manifest`, the container maps with the directories that hold them, and
every archive document with the file it came from — reading each exactly once
and reporting a refusal rather than raising it.

**How you use it.** `read(root)` returns a `Walk`. Its `findings` are the
refusals met while reading; its `containers` and `documents` are what parsed.
⛔ A check never opens a file itself: one reader means one answer to "what is
in this corpus", and a second walk is a second answer.

**Depends on.** `corpus.manifest`, `corpus.container`, `archive.document`,
`report`.

⚠️ **A refusal met while reading is a finding, not an exception.** `validate`
reports every problem in one run (R6), so a container map that will not parse
must not stop the twelve documents beside it from being read.

⛔ **`where` is always workspace-relative**, never the path the tool was given.
An absolute path in a report is personal data in a log (R7), and a report is
the most-pasted artifact this tool produces.

## ⛔ This module forwards refusals, and it does not scrub them

⚠️ Six of the findings here are `str(error)` from a reader that refused, and a
refusal's R7-cleanliness is **that reader's** guarantee: one `{value!r}`
upstream would reach a report line through this forwarding. ⭐ The raising
reader keeps it clean rather than this one,
because a scrub in the one report anybody reads would hide the same echo in
every traceback and every other caller — and `test_run` measures the
composition end to end so the trust is enforced somewhere.
"""

from __future__ import annotations

import re
from dataclasses import dataclass, field
from pathlib import Path

from studyforge.archive.document import parse as parse_document
from studyforge.archive.errors import ArchiveError
from studyforge.archive.scrub import PersonalDataLeak
from studyforge.corpus.container import CONTAINER_FILENAME, Container
from studyforge.corpus.container import RAISES as CONTAINER_RAISES
from studyforge.corpus.container import parse as parse_container
from studyforge.corpus.manifest import MANIFEST_FILENAME, Manifest
from studyforge.corpus.manifest import RAISES as MANIFEST_RAISES
from studyforge.corpus.manifest import parse as parse_manifest
from studyforge.corpus.placement import ARCHIVE_DIRNAME, RAW_DIRNAME
from studyforge.validate.report import Finding

#: A unit directory. ⚠️ Read from the *path*, so it still answers "which unit
#: is this" for a file whose content was refused.
UNIT_DIR = re.compile(r"^unit-(\d{2,})$")

#: The rules this module can report. ⭐ Named here so a test can assert the
#: set of rule ids the tool speaks, rather than matching on message text.
RULE_UNREADABLE = "unreadable"
RULE_MANIFEST = "manifest"
RULE_CONTAINER = "container"
RULE_DOCUMENT = "document"
RULE_PERSONAL_DATA = "personal-data"
#: ⛔ No container map beneath the archive root: absent, or present and empty.
RULE_NO_ARCHIVE = "no-archive"


@dataclass(frozen=True, slots=True)
class Unit:
    """One archive document, with where it was read from."""

    where: str
    path: Path
    container: Container
    document: dict


@dataclass(frozen=True, slots=True)
class Refused:
    """One file that would not parse, and which unit it claims by its location.

    ⭐ **Recorded rather than dropped, and that is a rule about reports, not a
    convenience.** A downstream check must never report the *absence* of
    something an upstream check already refused: a document refused by the R7
    gate is not a missing unit, and reporting it as one turns a fixture that
    breaks exactly one rule into two findings and makes the report harder to
    act on, not easier.

    ⚠️ The unit ordinal comes from the **directory name**, which is readable
    even when the content is not. Location is data (R4), so it is still
    information when identity has been refused.
    """

    where: str
    path: Path
    unit: int | None
    container: Container | None


@dataclass(frozen=True, slots=True)
class Held:
    """One container map, with the directory that holds it."""

    where: str
    directory: Path
    container: Container


@dataclass
class Walk:
    """What one pass over a corpus root read, and what it could not."""

    root: Path
    manifest: Manifest | None = None
    containers: list[Held] = field(default_factory=list)
    units: list[Unit] = field(default_factory=list)
    refused: list[Refused] = field(default_factory=list)
    findings: list[Finding] = field(default_factory=list)

    def relative(self, path: Path) -> str:
        """Return `path` as the report will name it: relative to the root (R7)."""
        try:
            return str(path.relative_to(self.root))
        except ValueError:  # pragma: no cover - a path from outside the root
            return path.name


def read(root: Path) -> Walk:
    """Read one corpus root, collecting refusals rather than raising them."""
    walk = Walk(root=Path(root))
    walk.manifest = _manifest(walk)
    if walk.manifest is None:
        return walk
    maps = sorted((walk.root / ARCHIVE_DIRNAME).rglob(CONTAINER_FILENAME))
    if not maps:
        walk.findings.append(_no_archive(walk))
    for path in maps:
        held = _container(walk, path)
        if held is None:
            # ⛔ Its documents are not read, and saying so is not the same as
            # saying they are fine (R6's sibling). The walk records the
            # container's own file; `run` turns that into an unchecked claim.
            walk.refused.append(Refused(walk.relative(path), path, None, None))
            continue
        walk.containers.append(held)
        walk.units.extend(_documents(walk, held))
    return walk


def _no_archive(walk: Walk) -> Finding:
    """Refuse a corpus with no container map beneath its archive root.

    ⛔ **A `Finding`, never an `Unchecked`.** `report` keeps `Unchecked` for an
    input whose absence is itself a validated fact, like the source tree an
    archive ships without (R2). The archive is not such an input: it is what
    `validate` judges, so "valid" over no archive is a verdict with no subject.
    """
    state = "holds no container map" if (walk.root / ARCHIVE_DIRNAME).is_dir() else "is absent"
    return Finding(
        RULE_NO_ARCHIVE,
        f"{ARCHIVE_DIRNAME}/",
        f"{state}, so there is no archive to judge. An adapter writes the archive here,"
        f" and a corpus is not valid until one is present.",
    )


def _manifest(walk: Walk) -> Manifest | None:
    path = walk.root / MANIFEST_FILENAME
    text = _text(walk, path, RULE_MANIFEST)
    if text is None:
        return None
    try:
        return parse_manifest(text, MANIFEST_FILENAME)
    except PersonalDataLeak as error:
        # ⛔ FIRST, and its own arm: the tuple below contains it, and two finding
        # rules must not collapse into one.
        walk.findings.append(Finding(RULE_PERSONAL_DATA, MANIFEST_FILENAME, str(error)))
    except MANIFEST_RAISES as error:
        walk.findings.append(Finding(RULE_MANIFEST, MANIFEST_FILENAME, str(error)))
    return None


def _container(walk: Walk, path: Path) -> Held | None:
    where = walk.relative(path)
    text = _text(walk, path, RULE_CONTAINER)
    if text is None:
        return None
    assert walk.manifest is not None
    try:
        return Held(where, path.parent, parse_container(text, where, walk.manifest))
    except PersonalDataLeak as error:
        # ⛔ FIRST, for the same reason as `_manifest`'s arm.
        walk.findings.append(Finding(RULE_PERSONAL_DATA, where, str(error)))
    except CONTAINER_RAISES as error:
        # ⛔ Named types, never the `ValueError` category this once caught
        # (Finding 11) — and named by the reader, never retyped here.
        walk.findings.append(Finding(RULE_CONTAINER, where, str(error)))
    return None


def _documents(walk: Walk, held: Held) -> list[Unit]:
    raw = held.directory / RAW_DIRNAME / held.container.variant
    found: list[Unit] = []
    for path in sorted(raw.rglob("*.json")):
        where = walk.relative(path)
        text = _text(walk, path, RULE_DOCUMENT)
        if text is None:
            walk.refused.append(_refused(walk, path, held))
            continue
        try:
            found.append(Unit(where, path, held.container, parse_document(text, where)))
        except ArchiveError as error:
            walk.findings.append(Finding(RULE_DOCUMENT, where, str(error)))
            walk.refused.append(_refused(walk, path, held))
        except PersonalDataLeak as error:
            walk.findings.append(Finding(RULE_PERSONAL_DATA, where, str(error)))
            walk.refused.append(_refused(walk, path, held))
    return found


def _refused(walk: Walk, path: Path, held: Held) -> Refused:
    """Record a document that would not parse, and the unit its location claims."""
    match = UNIT_DIR.match(path.parent.name)
    return Refused(
        walk.relative(path), path, int(match.group(1)) if match else None, held.container
    )


def _text(walk: Walk, path: Path, rule: str) -> str | None:
    """Read one file, or record why it could not be read.

    ⛔ `exc.strerror`, never `exc`: `OSError` formats itself with the filename
    it was given, so `{exc}` here would put an absolute path in the report.
    """
    try:
        return path.read_text(encoding="utf-8")
    except OSError as exc:
        walk.findings.append(
            Finding(RULE_UNREADABLE, walk.relative(path), f"cannot be read: {exc.strerror}")
        )
    except UnicodeDecodeError:
        walk.findings.append(Finding(RULE_UNREADABLE, walk.relative(path), "is not UTF-8 text"))
    return None
