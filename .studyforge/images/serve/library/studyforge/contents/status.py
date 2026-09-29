"""`status.json` — the volatile half: what is true about this corpus, here, now.

**What it does.** Annotates the stable contents by unit key with the two facts
that do not survive a clone — which units this machine actually has a page
for, and which ones the reader has marked read — plus what to read next. It
carries **no structure of its own**, and joining a stale pair is refused.

**How you use it.**

    from studyforge import contents as toc

    local = toc.status(built, toc.found(site, built.corpus), read=marks)
    toc.render_status(local)          # the bytes
    toc.join(built, local)            # raises unless the pair belongs together
    toc.missing(built, local)         # declared units this machine has no page for

**Depends on.** `studyforge.corpus.discovery` for what a scan found,
`studyforge.archive.scrub` for R7's gate, `studyforge.version` for R9's, and
this package's `document`, `entries`, `errors`, `order` and `writing`.

## ⛔ Two lifetimes, two documents, and the join is CHECKED

⭐ **The stable document changes when the corpus is rebuilt; this one changes
every time the reader finishes a section.** Kept in one blob, a consumer could
neither cache the stable half nor diff the volatile one — every regeneration
would rewrite everything and nobody could tell what had actually changed.

⚠️ **The cost of splitting them is that a consumer can hold two documents that
do not belong together**, and the ids would join perfectly while meaning
different units. ⛔ So this document names the schema and the exact stable
document it annotates — `toc_api` and `toc_sha256` — and `join` refuses a pair
that disagrees rather than producing a plausible, wrong answer.

## ⛔ A unit missing from this machine is REPORTED, not dropped (R6)

⚠️ This is the same failure `discovery.Unidentified` exists to prevent, one
layer up. ⭐ A unit the corpus declares and this machine has no page for stays
in the contents, with `present` false, and `missing` lists it — because a
table of contents that is short by one is a reader who has nothing at all to
look at and no way to find out why.

## ⭐ `read` arrives as data, and this module stores no reader's record

⛔ The reader's own record is `studyforge.progress`'s, and over `file://` it is
the browser's. This document is a **snapshot** of it taken when the site was
generated, so a page can show what is next without a server to ask (R8) — it
is never the authority for it, and nothing here writes one.
"""

from __future__ import annotations

import json
from collections.abc import Iterable
from dataclasses import dataclass
from pathlib import Path

from studyforge.archive.scrub import assert_clean
from studyforge.contents.document import (
    KNOWN_TOC_API,
    TOC_API,
    digest,
)
from studyforge.contents.entries import Contents, Entry
from studyforge.contents.errors import ContentsError
from studyforge.contents.order import order
from studyforge.contents.writing import write as write_text
from studyforge.corpus.discovery import Site
from studyforge.describe import describe, describe_keys
from studyforge.version import check as check_version

#: What the local document is called wherever it is written.
STATUS_FILENAME = "status.json"

#: The keys of the document, in the order they are written (R10).
STATUS_KEYS = ("toc_api", "corpus", "toc_sha256", "next", "units")

#: The keys of one unit's annotation. ⛔ `key` and nothing structural: this
#: document annotates the stable one by id and carries no hierarchy at all.
UNIT_STATUS_KEYS = ("key", "present", "read")


@dataclass(frozen=True, slots=True)
class UnitStatus:
    """What is true about one declared unit on this machine."""

    key: str
    present: bool
    read: bool

    @property
    def document(self) -> dict:
        """The annotation as the local document records it, in a stated order."""
        written = {"key": self.key, "present": self.present, "read": self.read}
        return {key: written[key] for key in UNIT_STATUS_KEYS}


@dataclass(frozen=True, slots=True)
class LocalStatus:
    """The local half: which stable document it annotates, and what it says about it."""

    corpus: str
    toc_sha256: str
    units: tuple[UnitStatus, ...] = ()
    next_key: str | None = None

    @property
    def by_key(self) -> dict[str, UnitStatus]:
        """The annotations by unit key, for a consumer joining them to entries."""
        return {unit.key: unit for unit in self.units}


def found(site: Site, corpus: str) -> frozenset[str]:
    """Return the unit keys `site` has a page for, for one corpus.

    ⭐ **This is why the package depends on discovery at all.** The mapping from a
    scan's artifacts to the keys this document is written in is one line, and a
    line every consumer would otherwise retype — which R19 calls a hole in the
    thing that should have produced it.

    ⛔ Identity, never a path: `Artifact.identity` is what says which unit a
    page is, and a page moved anywhere still answers for the unit it names
    (R4).
    """
    return frozenset(
        artifact.identity.address.unit_key(artifact.identity.unit)
        for artifact in site.units
        if artifact.identity.corpus == corpus and artifact.identity.unit is not None
    )


def status(contents: Contents, present: Iterable[str], read: Iterable[str] = ()) -> LocalStatus:
    """Annotate `contents` with what this machine has and what the reader has ticked.

    ⛔ **Every declared unit gets a row**, in reading order, whether or not it
    is present and whether or not it has been read. A document that listed
    only the interesting ones would make *"absent"* and *"not mentioned"* the
    same reading, and only one of those is something to look at.
    """
    here = frozenset(present)
    ticked = frozenset(read)
    walked = order(contents)
    return LocalStatus(
        corpus=contents.corpus,
        toc_sha256=digest(contents),
        units=tuple(
            UnitStatus(key=entry.key, present=entry.key in here, read=entry.key in ticked)
            for entry in walked
        ),
        next_key=_next(walked, ticked),
    )


def _next(walked: tuple[Entry, ...], ticked: frozenset[str]) -> str | None:
    """Return the first unit in reading order the reader has not ticked, or `None`.

    ⛔ Derived from the one walk in `order`, never from a second traversal:
    *"what is next"* disagreeing with *"what order this is read in"* is the
    kind of defect a reader reports as the site being haunted.
    """
    for entry in walked:
        if entry.key not in ticked:
            return entry.key
    return None


def join(contents: Contents, local: LocalStatus) -> dict[str, UnitStatus]:
    """Return the annotations by key, refusing a pair that does not belong together.

    ⛔ **The whole reason the split is safe.** Two documents joined on ids that
    no longer mean the same unit produce a plausible answer and no symptom;
    this refuses instead, naming which half is stale.
    """
    if local.corpus != contents.corpus:
        raise ContentsError(
            f"the local status annotates corpus {local.corpus!r} and these contents "
            f"are {contents.corpus!r}; the two would join on unit keys that mean "
            f"different units in different corpora"
        )
    here = digest(contents)
    if local.toc_sha256 != here:
        raise ContentsError(
            f"the local status annotates a table of contents with digest "
            f"{local.toc_sha256} and this one has {here}; one of the pair is stale, "
            f"and joining them would annotate units by ids that have been re-minted"
        )
    return local.by_key


def missing(contents: Contents, local: LocalStatus) -> tuple[str, ...]:
    """Return the declared units this machine has no page for, in reading order (R6).

    ⭐ Reported rather than dropped: a build that generated 37 of 38 pages is a
    build somebody has to be told about, and the contents are the one place the
    full declared count is known.
    """
    annotations = join(contents, local)
    return tuple(
        entry.key
        for entry in order(contents)
        if entry.key in annotations and not annotations[entry.key].present
    )


def status_document(local: LocalStatus) -> dict:
    """Return the local status as an object, in `STATUS_KEYS` order."""
    written = {
        "toc_api": TOC_API,
        "corpus": local.corpus,
        "toc_sha256": local.toc_sha256,
        "next": local.next_key,
        "units": [unit.document for unit in local.units],
    }
    return {key: written[key] for key in STATUS_KEYS}


def render_status(local: LocalStatus) -> str:
    """Return the local document's bytes.

    ⚠️ **Reproducible for one machine's state, which is a weaker promise than
    `toc.json`'s and is the point of the split.** The same reader state gives
    the same bytes; a different machine gives different bytes and that is the
    document doing its job.
    """
    return json.dumps(status_document(local), indent=2, ensure_ascii=False, sort_keys=False) + "\n"


def write_status(path: Path | str, local: LocalStatus) -> None:
    """Write `status.json`, staged and moved into place."""
    write_text(path, render_status(local))


def parse_status(text: str, where: str = STATUS_FILENAME) -> LocalStatus:
    """Build a `LocalStatus` from the text of a `status.json`."""
    try:
        document = json.loads(text)
    except (TypeError, json.JSONDecodeError) as exc:
        raise ContentsError(f"{where} is not valid JSON: {exc}") from None
    if not isinstance(document, dict):
        raise ContentsError(f"{where} must be a JSON object, got {describe(document)}")
    return from_status_document(document, where)


def load_status(path: Path | str) -> LocalStatus:
    """Read, parse and version-check one `status.json`.

    ⛔ `exc.strerror`, never `exc`: an `OSError` renders with the absolute path
    it was given, and a refusal carrying one carries a home directory (R7).
    """
    path = Path(path)
    try:
        text = path.read_text(encoding="utf-8")
    except OSError as exc:
        reason = exc.strerror or exc.__class__.__name__
        raise ContentsError(f"cannot read {path.name}: {reason}") from None
    except UnicodeDecodeError:
        raise ContentsError(f"{path.name} is not UTF-8 text") from None
    return parse_status(text, path.name)


def from_status_document(document: dict, where: str = STATUS_FILENAME) -> LocalStatus:
    """Build a `LocalStatus` from a decoded `status.json`."""
    check_version(
        "toc_api", document.get("toc_api"), KNOWN_TOC_API, where=where, error=ContentsError
    )
    assert_clean(document, where)
    unknown = sorted(set(document) - set(STATUS_KEYS))
    if unknown:
        raise ContentsError(
            f"{where} carries unknown key(s), {describe_keys(unknown)}; a local status "
            f"is "
            f"{list(STATUS_KEYS)}"
        )
    units = document.get("units")
    if not isinstance(units, list):
        raise ContentsError(
            f"{where} must carry a 'units' list, and carries {describe(units)}; a "
            f"missing list read as an empty one is a status that annotates nothing "
            f"and says nothing about it"
        )
    return LocalStatus(
        corpus=_text(document.get("corpus"), "corpus", where),
        toc_sha256=_text(document.get("toc_sha256"), "toc_sha256", where),
        units=tuple(_unit(unit, where) for unit in units),
        next_key=_optional(document.get("next"), "next", where),
    )


def _unit(value: object, where: str) -> UnitStatus:
    """One unit's annotation, with both flags required to be real booleans."""
    if not isinstance(value, dict):
        raise ContentsError(f"{where} has an annotation that is {describe(value)}, not an object")
    unknown = sorted(set(value) - set(UNIT_STATUS_KEYS))
    if unknown:
        raise ContentsError(
            f"{where} has an annotation carrying unknown key(s), {describe_keys(unknown)}"
        )
    return UnitStatus(
        key=_text(value.get("key"), "key", where),
        present=_flag(value.get("present"), "present", where),
        read=_flag(value.get("read"), "read", where),
    )


def _flag(value: object, what: str, where: str) -> bool:
    """Return a real boolean, refusing anything else.

    ⛔ `1` is not `true`: a JSON number here means somebody's generator wrote a
    count where a flag belongs, and reading it as true would hide that.
    """
    if not isinstance(value, bool):
        raise ContentsError(f"{where} needs {what} as true or false, got {describe(value)}")
    return value


def _text(value: object, what: str, where: str) -> str:
    """Return a required non-empty string. ⛔ Described, never echoed (R7)."""
    if not isinstance(value, str) or not value:
        raise ContentsError(f"{where} needs a non-empty {what}, and has {describe(value)}")
    return value


def _optional(value: object, what: str, where: str) -> str | None:
    """Return a string or `None`. ⛔ `null` means *nothing is next* — finished."""
    if value is None:
        return None
    return _text(value, what, where)
