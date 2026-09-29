r"""What ingestion left for one unit, and whether every file is still clean.

**What it does.** Reads every archive document in one unit's directory, refuses
anything that does not belong with the rest, and hands back the reading order.

**How you use it.** `read(unit_directory)` returns a `Material`, or raises
`NoMaterial` when nothing has been ingested for that unit at all.

**Depends on.** `archive.document`, `unit.errors`.

## ⛔ The gate runs here, and it is not a second gate

⚠️ **Do not re-ask within one read path; do gate at every trust
boundary.** Reading a file off disk **is** a trust boundary, so every document
goes through `archive.document.load` — which version-checks it, refuses an
unknown key, and runs the personal-data gate. ⭐ **By delegation, so there is no
second spelling of R7 here**; what this module adds is the part `load` cannot
know, which is whether these documents belong to *each other*.

⛔ **Never `scrub`, always refuse.** A signed archive that needs rewriting at
build time is a defect upstream, and rewriting it here would break the very
`content_sha256` that says what was ingested.

⚠️ **And a `PersonalDataLeak` passes through untranslated**, unlike every other
refusal here. ⭐ Deliberate: `PersonalDataLeak` is not a `ValueError` and is not
in this package's family, so a caller walking a corpus and catching
`ContentError` per unit **cannot** swallow one as *"that unit did not build"*.
R7 refusals stop the run.

## ⛔ The order comes from what a document records, not from its filename

⚠️ **This is §6's rule arriving somewhere unexpected.** A file called
`lesson-2.json` records its own `kind` and `ordinal`, and the *record* is the
answer — so this module never parses a filename, and the constant that would
have had to be copied from `validate` does not need to exist here.

⭐ **The duplication is dissolved rather than moved.** Whether a filename agrees
with the document inside it is a different question with a different consumer,
and `studyforge validate` already asks it.

## ⚠️ What this module does **not** assert

⛔ **A guarantee does not extend to what sits beside it**, so the
adjacent unasserted things are named rather than left to be assumed:

- **that the address matches the directory these files were found in.** That is
  `validate`'s question and it is not re-asked here.
- **that `content_sha256` covers the blocks.** `archive.document` owns the
  digest and `validate` recomputes it; this module carries it into provenance
  without recomputing it.
- **that the declared practice count matches what is on disk.** The builder
  *records* both numbers and delivers no verdict — see `builder.document`.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

from studyforge.archive.document import load
from studyforge.archive.errors import ArchiveError
from studyforge.unit.errors import ContentError, describe

#: Reading order between the kinds. ⛔ Lessons before practices, always: a
#: reader is taught before being asked, and a unit that opened with its
#: exercise would be a different product.
KIND_ORDER = ("lesson", "practice")


class NoMaterial(ContentError):
    """Nothing has been ingested for this unit — ⭐ an answer, not a failure.

    ⚠️ **A first-class outcome, and the distinction is the point.**
    A caller walking a whole corpus skips these and reports the rest: a unit
    nobody has ingested yet is a fact about the run, not a disagreement worth
    stopping for. ⛔ And it is never an empty document — a unit that renders as
    "lesson, then the end" is a page that lies by omission.
    """


@dataclass(frozen=True, slots=True)
class Material:
    """One unit's ingested documents, in reading order."""

    documents: tuple[dict, ...]

    @property
    def variant(self) -> str:
        """The one variant these documents are of."""
        return self.documents[0]["variant"]

    @property
    def unit(self) -> int:
        """The unit number they all record."""
        return self.documents[0]["unit"]

    def of_kind(self, kind: str) -> tuple[dict, ...]:
        """Every document of one kind, in ordinal order."""
        return tuple(d for d in self.documents if d["kind"] == kind)

    @property
    def archived_practices(self) -> int:
        """How many practices were ingested. ⛔ A count, never a verdict."""
        return len(self.of_kind("practice"))


def read(unit_directory: Path | str) -> Material:
    """Read every archive document in one unit directory, in reading order."""
    directory = Path(unit_directory)
    paths = sorted(p for p in directory.glob("*.json") if p.is_file())
    documents = []
    for path in paths:
        try:
            documents.append(load(path))
        except ArchiveError as error:
            # ⛔ Re-typed, not re-worded: `archive` owns what an archive
            # document may say, and re-spelling its sentence here would be the
            # duplication this package refuses. The name is this package's so a
            # caller catches one family.
            raise ContentError(str(error)) from None
    return of(documents, directory.name)


def of(documents: list[dict], where: str) -> Material:
    """Order documents already read, refusing any that do not belong together."""
    if not documents:
        raise NoMaterial(f"{where}: nothing has been ingested for this unit")
    _require_one_unit(documents, where)
    _require_distinct_ordinals(documents, where)
    return Material(documents=tuple(sorted(documents, key=_position)))


def _position(document: dict) -> tuple[int, int]:
    """Where a document sits in the reading order."""
    return KIND_ORDER.index(document["kind"]), document["ordinal"]


def _require_one_unit(documents: list[dict], where: str) -> None:
    """Refuse documents that are not all of one unit and one variant.

    ⚠️ **A property of these inputs, not a re-ask of `validate`.** Merging two
    units' documents into one page is the failure this refuses, and nothing
    downstream could notice it: every document is individually valid.
    """
    for field in ("source", "address", "variant", "unit"):
        seen = {_hashable(document.get(field)) for document in documents}
        if len(seen) != 1:
            raise ContentError(
                f"{where}: these documents do not all record one {field}, so they "
                f"are not one unit's material ({len(seen)} distinct values)"
            )
    for document in documents:
        if document["kind"] not in KIND_ORDER:
            raise ContentError(
                f"{where}: an archive document's kind must be one of "
                f"{list(KIND_ORDER)}, got {describe(document['kind'])}"
            )


def _require_distinct_ordinals(documents: list[dict], where: str) -> None:
    """Refuse two documents of one kind claiming the same ordinal.

    ⛔ Two files claiming to be the same lesson is a silently lossy read: one
    of them would be ordered arbitrarily against the other and a reader would
    see whichever the sort happened to put first.
    """
    seen: set[tuple[str, int]] = set()
    for document in documents:
        at = (document["kind"], document["ordinal"])
        if at in seen:
            raise ContentError(f"{where}: two {at[0]} documents both record ordinal {at[1]}")
        seen.add(at)


def _hashable(value: object) -> object:
    """Return a comparable form of a recorded field. ⚠️ `address` is a list."""
    return tuple(value) if isinstance(value, list) else value
