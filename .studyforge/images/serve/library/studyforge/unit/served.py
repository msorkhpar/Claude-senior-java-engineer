r"""Reading a built `unit.json` back, and refusing a stale one.

**What it does.** Validates a served unit document on the way *in*, so a
consumer never renders a shape this build does not recognise.

**How you use it.** `parse(text, where)` returns the document; `load(path)`
reads it from disk. Both raise `ContentError`.

**Depends on.** `unit.builder.document` for the contract, `archive.scrub` for
the gate.

## ⛔ A sibling of `builder`, not a member of it

⚠️ **Its consumers are the page generator, the server and the run route — none
of which builds anything.** In the extraction source the two live in one file,
which is the seam most likely to be ported by habit, because *"it was in that
file"* is the only argument for keeping them together.

## ⛔ Why this gates rather than trusting `validate`

⭐ **Do not re-ask within one read path; do gate at every trust
boundary.** Reading a document off disk is a trust boundary, and the decisive
argument is that **`studyforge validate` is optional** — so a serve-time reader
assuming it ran trusts a promise nobody made. ⚠️ The version check and the
personal-data gate are therefore *here*, at the boundary, and are not repeated
anywhere further in.

⛔ **An older `api` is refused rather than read.** *"This document was written
before sections carried a video"* and *"this unit has no video"* are different
answers, and a reader that conflated them would render a unit as silent because
the build that wrote it did not know about audio.

## ⚠️ What this module does **not** assert

⛔ The neighbours are named. It checks the document's **shape**:
its version, its keys and their order, and that every section carries the keys a
section carries. It does **not** re-validate blocks against the archive
vocabulary, does not recompute a digest — there is none in this document — and
does not check that `built_from` names files that still exist. ⭐ That last one
is deliberate and is somebody's decision, not an omission: re-ingest detection
compares provenance against the archive, and it is a walk of the corpus rather
than a read of one document.
"""

from __future__ import annotations

import json
from pathlib import Path

from studyforge.archive.scrub import assert_clean
from studyforge.unit.builder.document import (
    BUILT_FROM_KEYS,
    KNOWN_API,
    PRACTICES_KEYS,
    UNIT_KEYS,
)
from studyforge.unit.builder.parts import SECTION_KEYS
from studyforge.unit.errors import ContentError, describe
from studyforge.version import check as check_version

#: What a served document is called wherever one is written.
UNIT_FILENAME = "unit.json"


def parse(text: str, where: str = UNIT_FILENAME) -> dict:
    """Read one served unit document from text, refusing every way it can be stale."""
    try:
        document = json.loads(text)
    except TypeError, json.JSONDecodeError:
        # ⛔ The parser's own message quotes the offending line, and this
        # document carries a corpus's text (R7).
        raise ContentError(f"{where} is not valid JSON") from None
    if not isinstance(document, dict):
        raise ContentError(f"{where} must be a JSON object, got {describe(document)}")
    _gate(document, where)
    check_version("api", document.get("api"), KNOWN_API, where=where, error=ContentError)
    _require_keys(tuple(document), UNIT_KEYS, where, "a served unit document")
    _require_keys(tuple(document["practices"] or {}), PRACTICES_KEYS, where, "'practices'")
    _require_sections(document, where)
    _require_built_from(document, where)
    return document


def load(path: Path | str) -> dict:
    """Read, gate and validate one generated `unit.json`.

    ⛔ `where` is the file's **name**, never the path it was read from: an
    absolute path in a refusal is personal data in a log (R7).
    """
    path = Path(path)
    try:
        text = path.read_text(encoding="utf-8")
    except OSError as error:
        raise ContentError(f"cannot read {path.name}: {error.strerror}") from None
    return parse(text, path.name)


def _gate(document: dict, where: str) -> None:
    """Refuse a served document carrying personal data.

    ⭐ **The last boundary before a browser.** Everything in here was gated on
    the way into the archive; this is the gate on the way out of the build, and
    gating at every trust boundary is why both exist without either being a re-ask.

    ⛔ **`PersonalDataLeak` is raised as itself, not translated.** A caller
    serving a corpus catches `ContentError` per unit and serves the rest; if an
    R7 refusal were in that family it would be logged as one more unit that did
    not render, and the leak would be the thing nobody looked at. ⚠️ It is not
    a `ValueError` and it is not in this package's family, so it stops the run.
    """
    assert_clean(document, where)


def _require_keys(found: tuple, expected: tuple, where: str, what: str) -> None:
    """Refuse anything but exactly these keys, in this order (R10)."""
    if found != expected:
        raise ContentError(f"{where}: {what} must carry exactly {list(expected)}, in that order")


def _require_sections(document: dict, where: str) -> None:
    """Every section carries a section's keys, in order.

    ⚠️ **The order matters as much as the membership**, because these bytes are
    compared: a document whose sections rendered in a different key order would
    look changed to re-ingest detection while saying the same thing.
    """
    sections = document["sections"]
    if not isinstance(sections, list):
        raise ContentError(f"{where}: 'sections' must be a list, got {describe(sections)}")
    for index, section in enumerate(sections):
        if not isinstance(section, dict):
            raise ContentError(
                f"{where}: section {index} must be an object, got {describe(section)}"
            )
        _require_keys(tuple(section), SECTION_KEYS, where, f"section {index}")


def _require_built_from(document: dict, where: str) -> None:
    """Every provenance entry carries a provenance entry's keys, in order.

    ⛔ **A document with no provenance is refused rather than read.** Provenance
    is what a later run compares against to notice the material changed, and an
    entry that cannot be compared against anything is worse than absent: it
    reads as *checked* and is not.
    """
    entries = document["built_from"]
    if not isinstance(entries, list) or not entries:
        raise ContentError(
            f"{where}: 'built_from' must name at least one archive document; a "
            f"unit built from nothing cannot be checked against anything"
        )
    for index, entry in enumerate(entries):
        if not isinstance(entry, dict):
            raise ContentError(
                f"{where}: 'built_from' entry {index} must be an object, got {describe(entry)}"
            )
        _require_keys(tuple(entry), BUILT_FROM_KEYS, where, f"'built_from' entry {index}")
