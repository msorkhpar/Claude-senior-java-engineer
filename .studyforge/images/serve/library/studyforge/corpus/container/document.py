"""The container-map format: what a `container.json` is, read and rendered.

**What it does.** Owns the key order that reaches disk, the two records a map
decodes to, and the four entry points that read and re-render one.

**How you use it.** `parse(text, where, manifest)`, `load(path, manifest)`,
`render(container)`, `to_document(container)`. ⛔ The manifest is required.

**Depends on.** `studyforge.address`, `studyforge.corpus.manifest`,
`archive.scrub` for R7's gate, `studyforge.version` for R9's, and this
package's `fields` and `errors`. The package contract is in `__init__.py`.
"""

from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path

from studyforge.address import Address, AddressError, require_ordinal
from studyforge.archive.scrub import assert_clean
from studyforge.corpus.container import fields
from studyforge.corpus.container.errors import ContainerError
from studyforge.corpus.manifest import Manifest
from studyforge.describe import describe, describe_keys
from studyforge.version import check as check_version

#: The document format version, and what a generator writes today. ⚠️ Bumped
#: when a reader of the old shape would be *wrong* rather than merely
#: incomplete — ⭐ **2 is the version at which a unit's `origin`
#: may name a region of a file rather than a whole one**; ⭐ **3 is the one
#: at which a unit's practice may be read from a file of its own**.
CONTAINER_API = 3

#: The versions this build reads. ⛔ The membership test is
#: `studyforge.version`'s; what lives here is the set. ⭐ **Spelled as
#: literals rather than derived from `CONTAINER_API`**: a set built out of the
#: constant it is meant to accompany moves whenever that constant does, and an
#: assertion about it can only prove self-consistency.
#:
#: ⚠️ **1 is still read, and that is not a migration.** A v1 map declares no
#: region, so every claim it makes is one this build understands unchanged;
#: R9 refuses the *unknown*, and 1 is known. ⛔ What R9 does require is the
#: other direction, and `Container.__post_init__` enforces it: a map that uses
#: the v2 shape may not call itself v1, and a v3 shape may not call itself v2.
KNOWN_CONTAINER_API = frozenset({1, 2, 3})

#: ⛔ The version at which a unit's `origin` may be an object naming a region.
#: A build that does not speak this version must be **unable** to
#: read a map that uses the shape — otherwise the version is decorative and
#: an old reader silently takes seventeen regions for seventeen whole files.
REGION_ORIGIN_API = 2

#: ⛔ The version at which a unit may declare `practice_origin` — a **second**
#: source file holding its `practice` documents. ⚠️ Guarded for the
#: reason a region is: a build that cannot see the key counts both files'
#: headings against one of them and reports a short read on a complete corpus.
PRACTICE_ORIGIN_API = 3

CONTAINER_FILENAME = "container.json"

#: The document's key order, which is what reaches disk (R10). ⚠️ `origin` and
#: `note` are omitted when they have nothing to say; the rest keep this order.
CONTAINER_KEYS = (
    "container_api",
    "address",
    "titles",
    "variant",
    "ingested",
    "origin",
    "note",
    "units",
)

#: A unit entry's key order. `origin`, `url_slug`, `label` and `note` are
#: omitted when absent.
#:
#: ⭐ **`origin` carries two shapes and remains one key.** A string is a whole
#: file; an object — `fields.ORIGIN_KEYS`, `{"path": …, "section": …}` — is the
#: region of a file that opens at that heading. ⛔ `section` is
#: therefore **not** a unit key and does not appear here.
#:
#: ⭐ **`origin` and `url_slug` are both provenance and neither is redundant.**
#: `origin` is a path *inside* the source, which is what a repository corpus
#: has; `url_slug` is the source's own addressing for the unit, which is what a
#: web source has instead — it has no file. A corpus normally carries one or
#: the other. ⚠️ `depth2`'s second container carries both because it is the
#: fixture that exercises both.
#: ⭐ **`practice_origin` is `origin`'s twin and carries the same two shapes.**
#: It names where this unit's `practice` documents were read from when that is
#: **not** the file its prose came from — the one way a practice joins a topic
#: page whose source file may not be rewritten (R3). ⛔ It places nothing.
UNIT_KEYS = ("n", "title", "practices", "origin", "practice_origin", "url_slug", "label", "note")

#: ⭐ **The whole content of "hand-authorable"** (Q3). These are the fields a
#: person may amend and a generator must round-trip rather than overwrite;
#: everything else is derived and hand-writing it is a finding.
EDITORIAL_KEYS = ("title", "titles", "note")


@dataclass(frozen=True, slots=True)
class Unit:
    """One unit as its container declares it.

    `practices` is the **declared** count, preserved verbatim: `validate` checks it
    against what is actually on disk, and a reader that corrected it here
    would delete the disagreement that check exists to find.
    """

    n: int
    title: str
    practices: int
    origin: str | None = None
    #: ⭐ **The second half of `origin`, not a second field of the document.**
    #: `None` means the unit is the whole file; a heading's exact text means it
    #: is the region opening at that heading and ending at the next heading of
    #: the same or shallower depth. ⛔ `origin` stays a plain path either way,
    #: because every other consumer of it wants a path — `origin_directory`
    #: takes `.parent` of one, and media placement is unaffected.
    origin_section: str | None = None
    #: ⭐ **Where this unit's `practice` documents came from, when that is a
    #: file of its own**. `None` means they came from `origin`, which
    #: is the common case and every map written before `container_api` 3.
    practice_origin: str | None = None
    #: The region half of `practice_origin`, as `origin_section` is `origin`'s.
    practice_origin_section: str | None = None
    url_slug: str | None = None
    label: str | None = None
    note: str | None = None

    @property
    def numbering(self) -> str:
        """Return this unit's numbering **as a reader sees it**: `4.4.1`, or `7`.

        ⚠️ **Not the filename component**, which is `placement.names.label_of` and
        gives `unit-07` where this gives `7`. The two are one rule with two
        fallbacks: when a label is present they are identical and it *is* the
        label; when it is absent, a filename wants a prefixed, zero-padded,
        sortable form and a page heading does not.

        ⛔ Presentation only, either way. Nothing may read one back as an
        ordinal — R4's argument about paths, applied to numbering.
        """
        return self.label if self.label is not None else str(self.n)


@dataclass(frozen=True, slots=True)
class Container:
    """One container's declaration. Immutable once validated."""

    address: Address
    titles: tuple[str, ...]
    variant: str
    ingested: str
    units: tuple[Unit, ...]
    origin: str | None = None
    note: str | None = None
    container_api: int = CONTAINER_API

    def __post_init__(self) -> None:
        """Refuse a region declared at a version that has no regions (R9).

        ⛔ **R9's other direction, and the reason a version was minted at
        all.** A build reading only `container_api: 1` must be *unable* to read
        a map that uses the v2 shape; a map that uses it and calls itself v1
        would slip past that build's version check and be read as seventeen
        whole files. ⭐ Checked here rather than in `from_document` so the
        render path cannot mint one either — the two directions are one rule
        and it has one home.
        """
        for unit in self.units:
            if unit.origin_section is not None and self.container_api < REGION_ORIGIN_API:
                raise ContainerError(self._too_old(unit.n, "origin as a region", REGION_ORIGIN_API))
            if unit.practice_origin is None:
                continue
            if self.container_api < PRACTICE_ORIGIN_API:
                raise ContainerError(
                    self._too_old(unit.n, "a practice_origin", PRACTICE_ORIGIN_API)
                )
            if unit.origin is None:
                raise ContainerError(
                    f"{self.address.key} declares unit {unit.n} practice_origin and no "
                    f"origin. ⛔ The key exists to say that the practice came from a "
                    f"file OTHER than the prose did; with no origin there is no other "
                    f"file, and the one path belongs in origin where everything that "
                    f"places a page can see it."
                )

    def _too_old(self, n: int, what: str, needs: int) -> str:
        """Say that unit `n` uses a shape the version it declares does not have.

        ⛔ **R9's other direction, spelled once for both shapes.** It is
        refused rather than read: a build that does not speak the shape must be
        unable to read the map, or the version says nothing.
        """
        return (
            f"{self.address.key} declares unit {n} {what} at container_api "
            f"{self.container_api}; it needs container_api {needs}. It is refused "
            f"rather than read: a build that does not speak the shape must be unable "
            f"to read the map, or the version says nothing."
        )

    @property
    def ordinals(self) -> tuple[int, ...]:
        """The unit ordinals, in declared order."""
        return tuple(unit.n for unit in self.units)

    def unit(self, n: int) -> Unit:
        """Return unit `n`, or raise `ContainerError` naming what is declared."""
        for unit in self.units:
            if unit.n == n:
                return unit
        raise ContainerError(
            f"{self.address.key} declares units {list(self.ordinals)}; there is no unit {n!r}"
        )


def from_document(document: dict, where: str, manifest: Manifest) -> Container:
    """Build a `Container` from a decoded `container.json`, checked against its corpus."""
    # ⛔ The declared version is **kept**, never replaced by this build's own.
    # A v1 map read and written back must come out as a v1 map: `render`'s
    # round-trip guarantee is that every byte of everything untouched is the
    # byte that was there, and silently promoting the version would rewrite
    # the one field that says which reader the document was written for.
    container_api = check_version(
        "container_api",
        document.get("container_api"),
        KNOWN_CONTAINER_API,
        where=where,
        error=ContainerError,
    )
    assert_clean(document, where)
    unknown = sorted(set(document) - set(CONTAINER_KEYS))
    if unknown:
        raise ContainerError(
            f"{where} carries unknown key(s), {describe_keys(unknown)}; "
            f"a container map is {list(CONTAINER_KEYS)}"
        )

    address = Address(document.get("address", ())).require_depth(manifest.depth)
    titles = _titles(document.get("titles"), manifest.depth, where)
    variant = document.get("variant")
    if variant not in manifest.variants:
        raise ContainerError(
            f"{where} declares a variant this corpus does not have, {describe(variant)}; "
            f"{manifest.source} declares "
            f"{list(manifest.variants)}"
        )
    return Container(
        address=address,
        titles=titles,
        variant=variant,
        ingested=fields.required_text(document.get("ingested"), "ingested", where),
        units=_units(document.get("units"), where),
        origin=fields.optional_path(document.get("origin"), "origin", where),
        note=fields.optional_text(document.get("note"), "note", where),
        container_api=container_api,
    )


def parse(text: str, where: str, manifest: Manifest) -> Container:
    """Read one container map from its text: valid JSON, a known version, clean."""
    try:
        document = json.loads(text)
    except json.JSONDecodeError as exc:
        # ⛔ Names the fields, never the exception object and never the text.
        raise ContainerError(
            f"{where} is not valid JSON: {exc.msg} at line {exc.lineno} column {exc.colno}"
        ) from None
    if not isinstance(document, dict):
        raise ContainerError(f"{where} must be a JSON object, got a {type(document).__name__}")
    return from_document(document, where, manifest)


def load(path: Path | str, manifest: Manifest) -> Container:
    """Read, version-check and gate one container map from disk.

    ⛔ `where` is the file's **name**, never the path it was read from: an
    absolute path in a refusal is personal data in a log (R7).
    """
    path = Path(path)
    try:
        text = path.read_text(encoding="utf-8")
    except OSError as exc:
        # ⛔ `exc.strerror`, never `exc`: `OSError` formats itself with the
        # filename it was given.
        raise ContainerError(f"cannot read {path.name}: {exc.strerror}") from None
    return parse(text, path.name, manifest)


def to_document(container: Container) -> dict:
    """Return the container as the decoded document, in `CONTAINER_KEYS` order."""
    document = {
        "container_api": container.container_api,
        "address": list(container.address.segments),
        "titles": list(container.titles),
        "variant": container.variant,
        "ingested": container.ingested,
    }
    if container.origin is not None:
        document["origin"] = container.origin
    if container.note is not None:
        document["note"] = container.note
    document["units"] = [_unit_document(unit) for unit in container.units]
    return document


def render(container: Container) -> str:
    """Serialise a container to the exact bytes that reach disk.

    ⛔ `sort_keys=False`. ⭐ This is the round-trip half of "hand-authorable":
    read a map, amend an editorial field, write it back, and every byte of
    everything untouched is the byte that was there.
    """
    return json.dumps(to_document(container), indent=2, ensure_ascii=False, sort_keys=False) + "\n"


def _unit_document(unit: Unit) -> dict:
    entry: dict[str, object] = {"n": unit.n, "title": unit.title, "practices": unit.practices}
    for key in ("origin", "practice_origin", "url_slug", "label", "note"):
        value = getattr(unit, key)
        if value is not None:
            entry[key] = value
    sections = (("origin", unit.origin_section), ("practice_origin", unit.practice_origin_section))
    for key, section in sections:
        if section is not None:
            # ⭐ The object replaces the string **in place**, so a region keeps
            # its key's position in `UNIT_KEYS` and the round trip is byte-exact.
            entry[key] = {"path": entry[key], "section": section}
    return entry


def _titles(value: object, depth: int, where: str) -> tuple[str, ...]:
    if not isinstance(value, list) or len(value) != depth:
        raise ContainerError(
            f"{where} must carry one title per container level; the corpus declares {depth}"
        )
    return tuple(fields.required_text(title, "title", where) for title in value)


def _units(value: object, where: str) -> tuple[Unit, ...]:
    if not isinstance(value, list) or not value:
        raise ContainerError(f"{where} declares no units; a container map declares at least one")
    units = tuple(_unit(entry, where) for entry in value)
    expected = list(range(1, len(units) + 1))
    if [unit.n for unit in units] != expected:
        raise ContainerError(
            f"{where} declares units {[unit.n for unit in units]}; ordinals must be "
            f"contiguous from 1, so {expected} was expected. A gap is a unit that was "
            f"not ingested, and it is reported rather than closed."
        )
    return units


def _unit(entry: object, where: str) -> Unit:
    if not isinstance(entry, dict):
        raise ContainerError(f"{where} has a unit entry that is not an object")
    unknown = sorted(set(entry) - set(UNIT_KEYS))
    if unknown:
        raise ContainerError(
            f"{where} has a unit carrying unknown key(s), {describe_keys(unknown)}; "
            f"a unit is {list(UNIT_KEYS)}"
        )
    try:
        n = require_ordinal(entry.get("n"), f"{where} unit ordinal")
    except AddressError:
        # ⛔ Converted rather than allowed to escape, and not only for
        # consistency: `studyforge.address`'s message formats the value with `!r`, and this
        # one is read straight out of a file somebody else wrote (R7).
        raise ContainerError(
            f"{where} has a unit whose ordinal is {fields.said(entry.get('n'))}; "
            f"it must be a whole number of 1 or more"
        ) from None
    practices = entry.get("practices")
    if not isinstance(practices, int) or isinstance(practices, bool) or practices < 0:
        raise ContainerError(
            f"{where} unit {n} declares a practice count that is not a whole number. "
            f"It is preserved verbatim for `validate` to check against reality, so it is "
            f"never corrected here."
        )
    origin, section = fields.optional_origin(entry.get("origin"), f"unit {n} origin", where)
    practice, practice_section = fields.optional_origin(
        entry.get("practice_origin"), f"unit {n} practice_origin", where
    )
    return Unit(
        n=n,
        title=fields.required_text(entry.get("title"), f"unit {n} title", where),
        practices=practices,
        origin=origin,
        origin_section=section,
        practice_origin=practice,
        practice_origin_section=practice_section,
        url_slug=fields.optional_slug(entry.get("url_slug"), f"unit {n} url_slug", where),
        label=fields.optional_label(entry.get("label"), f"unit {n} label", where),
        note=fields.optional_text(entry.get("note"), f"unit {n} note", where),
    )
