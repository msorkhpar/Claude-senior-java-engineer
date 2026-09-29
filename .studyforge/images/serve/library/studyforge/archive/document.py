r"""The archive document: what an ingested unit *is* on disk (spec §6).

**What it does.** Owns the format and nothing else — builds one document,
renders it to the exact bytes that reach disk, and reads one back. ⛔ Where
the file goes is placement's decision (§5) and stays there.

**How you use it.** `build(...)` for a document, `render(document)` for its
bytes, `parse(text, where)` and `load(path)` to read one. Every entry point
runs the personal-data gate; there is no unguarded way in.

**Depends on.** `archive.blocks` for the vocabulary, `archive.errors`,
`archive.scrub` for R7's gate, `studyforge.version` for R9's, and
`studyforge.address`. ⛔ Not on `render` or `serve`: the archive is the input
to a page.

## The four properties that make it trustworthy

- **Fixed key order, serialised unsorted** (R10). `DOCUMENT_KEYS` is what
  reaches disk, `sort_keys=False`, so an unchanged document re-renders to
  identical bytes. Sorting would make that true by accident until somebody
  added a key.
- **`content_sha256` covers `blocks` and nothing else.** It answers exactly
  one question — did the source edit this since we read it? Folding in
  `ingested` would make every re-ingest differ regardless of content; folding
  in media would make re-downloading a 6 MB video look like a source edit.
  Either would make the one signal it exists for worthless.
- **An unknown `raw_api` is refused, never migrated in place** (R9). A
  migration that runs because something merely wanted to render a page
  rewrites the record of what was ingested. ⛔ The test is `version`'s; this
  module owns only `KNOWN_RAW_API`, because which versions the archive speaks
  is this contract's business and the check is nobody's twice.
- **Every string is gated, and the gate refuses** (R7). Rewriting an archive
  would corrupt the record of what the source said *and* break its own digest.

## Two gates, one walker, decoded strings only

⭐ `build` gates the title and the blocks, assembles, and then gates the
**whole document** — so anything smuggled in through the metadata, a `source`
or an asset's remote address, is caught before it reaches a filesystem. The
inner gate is not redundancy: a match there means an upstream stage failed and
that is worth surfacing (R6).

⛔ **Never `render(document)`.** In JSON a newline is the two characters `\`
and `n`, so a decorator on its own line serialises as `...\n@router.get(` and
`n@router.get` is address-shaped; the extraction source gated its rendered
bytes and refused three clean lessons. `archive.scrub` reads decoded strings
through one walker, and both gates here ride it.

⚠️ **`parse` does not gate the file's raw text when it is not JSON**, and that
is a deliberate divergence from the extraction source. Text that failed to
parse has no decoded strings, so a text-level gate would be reading escaping
again — the exact shape of the defect above — and it would report a leak where
the honest answer is *"this file is malformed"*. Nothing reaches the archive
either way, and the refusal never echoes the text.

## Optional keys are appended, never slotted in

`OPTIONAL_KEYS` are written only when they have something to say and always
**after** `content_sha256`, so adding one cannot disturb the digest and every
document written before a key existed still re-renders byte for byte.
⛔ `media_skipped` exists because its absence was a way to look finished while
being short: an ingest that named media and deliberately did not fetch it was
otherwise indistinguishable from a unit that simply has none.
"""

from __future__ import annotations

import hashlib
import json
import re
from pathlib import Path

from studyforge.address import Address
from studyforge.archive.blocks import counts_of
from studyforge.archive.errors import ArchiveError
from studyforge.archive.scrub import assert_clean
from studyforge.describe import describe, describe_keys
from studyforge.exercise import Exercise, ExerciseError
from studyforge.exercise import of as exercise_of
from studyforge.exercise import to_document as exercise_document
from studyforge.version import check as check_version

#: The document format version. ⚠️ Bumped when a reader of the old shape would
#: be *wrong* rather than merely incomplete.
RAW_API = 1

#: The versions this build reads. ⛔ The membership test is `studyforge.
#: version`'s, not this module's; what lives here is the set.
KNOWN_RAW_API = frozenset({RAW_API})

#: The document's key order, which is the reading order and is also what
#: reaches disk: what it is, where it came from, what it says, what it is made
#: of. ⛔ Serialised `sort_keys=False`, so this tuple is the format (R10).
DOCUMENT_KEYS = (
    "raw_api",
    "source",
    "address",
    "variant",
    "unit",
    "kind",
    "ordinal",
    "ingested",
    "title",
    "blocks",
    "video",
    "assets",
    "attachments",
    "counts",
    "content_sha256",
)

#: Written only when they have something to say, and always after the digest.
#: ⚠️ **Appended, never inserted.** `exercise` joined later and went on the
#: end for that reason: every document written before it existed still renders
#: the bytes it always did.
OPTIONAL_KEYS = ("assets_sha256", "starting_code", "media_skipped", "exercise")

#: Every key this format defines. ⛔ A document carrying anything else is
#: **refused** — see `parse`, and the measurement in its docstring.
KNOWN_KEYS = frozenset(DOCUMENT_KEYS) | frozenset(OPTIONAL_KEYS)

#: What a `video` **record** says, in the order it is written. ⚠️ Two halves
#: that must not be confused: `src`, `poster` and `mime` are how the page plays
#: it — plain relative paths beside the page — while `remote` and
#: `poster_remote` are provenance, the addresses the source served, kept so a
#: re-fetch is possible from the document alone and deliberately never
#: rendered. ⛔ This is not the `video` **block**, which is content and carries
#: `("type", "src", "title")` — see `archive.blocks`.
VIDEO_KEYS = ("src", "poster", "mime", "remote", "poster_remote")

#: What ONE entry of `assets` or `attachments` says, in the order it is written
#: — ⭐ **one entry vocabulary and not two**. ⚠️ `local` is the
#: file's path inside the unit's own archive directory and is the only half a
#: page may address; `remote` is provenance, the address the source served, and
#: is never rendered, exactly as a `video` record's is.
#:
#: ⛔ **The two lists differ in what they are FOR, not in what they hold**
#: (spec C4): an **asset** is a file some block already shows, so the
#: page reaches it through that block's `src`; an **attachment** is a companion
#: file no block names — a dataset a lesson loads, a notebook — and the page
#: links it for download. ⭐ That is why the served section carries the
#: attachments and not the assets: a page that linked its assets as well would
#: offer the reader the diagram it is already looking at.
MEDIA_ENTRY_KEYS = ("remote", "local", "sha256", "bytes", "content_type", "kind")

#: What a unit's file may be. ⚠️ A practice is a lesson with a layout, not a
#: different document.
KINDS = ("lesson", "practice")

#: `ingested` is an ISO calendar date and nothing else. ⭐ Shape only — a real
#: calendar check would reject nothing this framework can produce and would
#: make the rule harder to state than the format it enforces.
ISO_DATE = re.compile(r"^\d{4}-\d{2}-\d{2}$")


def content_sha256(blocks: list) -> str:
    """Return the digest of `blocks`, and of nothing else.

    Compact and `sort_keys=False`: the digest must be recomputable from the
    file by anyone holding it, so it is taken over one canonical serialisation
    of the same key order the file itself carries.
    """
    payload = json.dumps(blocks, separators=(",", ":"), ensure_ascii=False)
    return hashlib.sha256(payload.encode("utf-8")).hexdigest()


def build(
    *,
    source: str,
    address: Address | list | tuple,
    variant: str,
    unit: int,
    kind: str,
    ordinal: int,
    ingested: str,
    title: str,
    blocks: list,
    video: dict | None = None,
    assets: list | None = None,
    attachments: list | None = None,
    assets_sha256: str | None = None,
    starting_code: str | None = None,
    media_skipped: bool = False,
    exercise: object = None,
) -> dict:
    """Assemble one archive document — every gate run, nothing written.

    ⚠️ `address` is the container's address as `studyforge.address` defines it and reaches
    the file as a JSON array of slugs. ⛔ It is not a path: where the document
    lands is placement's decision (R2, §5), and an address that carried a
    directory separator would have made the two the same thing.
    """
    if kind not in KINDS:
        raise ArchiveError(f"kind must be one of {list(KINDS)}, got {describe(kind)}")
    at = address if isinstance(address, Address) else Address(address)
    where = f"{at.key}/unit-{unit}/{kind}-{ordinal}"
    _require_iso_date(ingested, where)

    blocks = list(blocks or [])
    assert_clean(title or "", f"{where} title")
    assert_clean(blocks, f"{where} blocks")

    document = {
        "raw_api": RAW_API,
        "source": source,
        "address": list(at.segments),
        "variant": variant,
        "unit": unit,
        "kind": kind,
        "ordinal": ordinal,
        "ingested": ingested,
        "title": title,
        "blocks": blocks,
        "video": video,
        "assets": list(assets or []),
        "attachments": list(attachments or []),
        "counts": counts_of(blocks, f"{where} blocks"),
        "content_sha256": content_sha256(blocks),
    }
    if tuple(document) != DOCUMENT_KEYS:  # pragma: no cover - built above
        raise ArchiveError(
            f"built the keys {list(document)}; the format is {list(DOCUMENT_KEYS)}. "
            f"Raised rather than asserted: `python -O` elides an assert, and the "
            f"key ORDER is what reaches disk."
        )
    # ⚠️ Appended in `OPTIONAL_KEYS` order, after the digest, so a document
    # written before one of them existed still renders what it always did.
    if assets_sha256:
        document["assets_sha256"] = assets_sha256
    if starting_code is not None:
        document["starting_code"] = starting_code
    if media_skipped:
        document["media_skipped"] = True
    if exercise is not None:
        # ⛔ Validated on the way in, not merely carried. The four workspace
        # values reach a file a runner executes against, and R5's pair is
        # refused here so no consumer has to remember to ask (spec §7).
        document["exercise"] = _exercise_document(exercise, document, where)

    # ⛔ The second gate, over every string in the WHOLE document. This is the
    # one that reaches the metadata — a `source`, an asset's remote address —
    # and it is the reason a test can prove the gate is invoked by hiding a
    # leak where only this layer can see it.
    assert_clean(document, where)
    return document


def render(document: dict) -> str:
    """Serialise the document to the exact bytes that reach disk. One place, so builds match."""
    return json.dumps(document, indent=2, ensure_ascii=False, sort_keys=False) + "\n"


def parse(text: str, where: str) -> dict:
    """Read one archive document from its text: valid JSON, a known version, clean.

    ⛔ In that order. A document declaring a version this build cannot read is
    refused for *that* reason before anything else is said about it — otherwise
    a v2 archive is refused for a v1 reason and the integrator upgrades the
    wrong thing.

    ## ⛔ A key this format does not define is refused, not carried

    ⚠️ **Without this check** a document carrying an unknown top-level object
    validates **green — 0 findings, 0 unchecked claims**. `content_sha256`
    covers `blocks` and nothing else, so an unknown sibling key disturbs no
    digest and no count.

    ⭐ **Tolerating an unknown key means tolerating a typo in a known one**, and
    a misspelled `exercise` is a grader the reader is never offered while the
    corpus passes every check. That is the worst outcome available here: a
    corpus green with its graders invisible. ⛔ So the key set is closed, and
    adding a key is a change to `DOCUMENT_KEYS` or `OPTIONAL_KEYS` rather than
    something a writer can do by accident.

    ⚠️ The check runs **after** the gate, deliberately: `archive.scrub` walks
    dict keys as well as values, so by the time a key can be named in a refusal
    it has already been swept.

    ## ⛔ A block's SHAPE is not this function's question

    ⚠️ **`parse` asks four things and no more**: is it JSON, is it a version
    this build reads, is it clean (R7), and are its top-level keys ones this
    format defines. ⛔ **It does not read a block's shape, and it may not** —
    the full spec §6 reading is `validate.blocks`, and `validate` imports
    `archive`, so asking it here would make the archive depend on its own
    consumer.

    ⭐ **So the guarantee is made in two places that are not this one**, and
    and together they leave no gap: `blocks.counts_of` refuses a non-object
    top-level block by name on the way IN through `build`; `blocks.read_layout`
    refuses it on the way OUT, so no reader of a parsed document reaches `.get`
    on a string; and `validate.blocks.block_problems` is the full §6 reading a
    corpus meets through `studyforge validate` (R2).

    ⛔ **Said here deliberately**: a document that parses clean is the one a
    caller most easily mistakes for a document that is WELL-FORMED.
    """
    try:
        document = json.loads(text)
    except json.JSONDecodeError as exc:
        # ⛔ Names the fields, never the exception object and never the text:
        # an exception's `str()` is written by whoever raised it, and the text
        # is the thing that might be carrying the leak.
        raise ArchiveError(
            f"{where} is not valid JSON: {exc.msg} at line {exc.lineno} column {exc.colno}"
        ) from None
    if not isinstance(document, dict):
        raise ArchiveError(f"{where} must be a JSON object, got a {type(document).__name__}")
    check_version(
        "raw_api",
        document.get("raw_api"),
        KNOWN_RAW_API,
        where=where,
        error=ArchiveError,
    )
    assert_clean(document, where)
    _require_known_keys(document, where)
    _require_valid_exercise(document, where)
    return document


def load(path: Path | str) -> dict:
    """Read, version-check and gate one archive document from disk.

    ⛔ `where` is the file's **name**, never the path it was read from: an
    absolute path in a refusal is personal data in a log, which is the leak
    this module's own gate exists to prevent (R7).
    """
    path = Path(path)
    try:
        text = path.read_text(encoding="utf-8")
    except OSError as exc:
        # ⛔ `exc.strerror`, never `exc`: `OSError` formats itself with the
        # filename it was given, so `{exc}` here would produce a refusal
        # carrying an absolute path.
        raise ArchiveError(f"cannot read {path.name}: {exc.strerror}") from None
    return parse(text, path.name)


def _require_iso_date(value: object, where: str) -> None:
    """Refuse an `ingested` that is not `YYYY-MM-DD`."""
    if not isinstance(value, str) or not ISO_DATE.match(value):
        raise ArchiveError(
            f"{where} has an invalid 'ingested' value; it must be an ISO date, YYYY-MM-DD"
        )


def _exercise_document(exercise: object, document: dict, where: str) -> dict:
    """Validate an exercise on its way into a document, and return what is written.

    ⛔ Round-tripped through `studyforge.exercise` even when the caller already
    holds an `Exercise`: the dataclass is frozen, not validated — anybody can
    construct one with a path outside the safe pattern — and the record that
    reaches disk must be one the reader would accept back.
    """
    raw = exercise_document(exercise) if isinstance(exercise, Exercise) else exercise
    try:
        # ⭐ Asked through `of`, so the practice-kind rule is applied by the
        # module that owns it rather than restated here.
        record = exercise_of({"kind": document["kind"], "exercise": raw}, where)
    except ExerciseError as error:
        raise ArchiveError(str(error)) from None
    assert record is not None  # noqa: S101 - the key is present by construction
    return exercise_document(record)


def _require_known_keys(document: dict, where: str) -> None:
    """Refuse a top-level key this format does not define. See `parse`."""
    unknown = [key for key in document if key not in KNOWN_KEYS]
    if unknown:
        raise ArchiveError(
            f"{where} carries {len(unknown)} key(s) the archive format does not "
            f"define, {describe_keys(unknown)}. The format is {list(DOCUMENT_KEYS)} plus "
            f"{list(OPTIONAL_KEYS)}. A key nothing reads is how a misspelled "
            f"field becomes content nobody is ever shown."
        )


def _require_valid_exercise(document: dict, where: str) -> None:
    """Apply the exercise contract to a document that carries one (spec §7).

    ⭐ Here rather than in a consumer, so R5 has one enforcement point that
    every consumer passes through. `studyforge validate` refuses the forbidden
    pair because it parses the document; so does anything that renders one.
    """
    try:
        exercise_of(document, where)
    except ExerciseError as error:
        # ⛔ `archive.errors` states the rule: reading an archive document
        # raises `ArchiveError` and nothing else, including where the rule
        # being applied is somebody else's.
        raise ArchiveError(str(error)) from None
