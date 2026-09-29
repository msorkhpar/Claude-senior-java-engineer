r"""Checks about the archive alone: where things are, and whether they agree.

**What it does.** Every check that needs only what the adapter wrote — the
container map against the directory holding it, containers against each other,
documents against their container and their own filename, and each document
against its own digest and counts.

**How you use it.** Each function takes the `Walk` and yields `Finding`s.
`checks()` is the list, in the order a report reads best.

**Depends on.** `archive.blocks`, `archive.document`, `validate.blocks`, `corpus.container`,
`corpus.placement`, `validate.corpus`, `validate.report`.

⛔ **Every check yields; none raises.** One run reports every problem (R6).

⚠️ **A digest check cannot answer the question that matters most.** It compares
the blocks against a number taken from the blocks, so a construct the parser
never recognised is absent from both and nothing raises. That question belongs
to `validate.source`, which counts something the parser did not produce.
"""

from __future__ import annotations

import re
from collections.abc import Iterator

from studyforge.archive.blocks import counts_of
from studyforge.archive.document import content_sha256
from studyforge.corpus.placement import ARCHIVE_DIRNAME
from studyforge.describe import describe
from studyforge.validate.blocks import check_block_shapes
from studyforge.validate.corpus import Unit, Walk
from studyforge.validate.report import Finding, Unchecked

RULE_ADDRESS_DIRECTORY = "address-directory"
RULE_DUPLICATE_ADDRESS = "duplicate-address"
RULE_IDENTITY = "identity"
RULE_DIGEST = "digest"
RULE_COUNTS = "counts"
RULE_EMPTY_UNIT = "empty-unit"
RULE_UNIT_MISSING = "unit-missing"
RULE_PRACTICE_COUNT = "practice-count"

UNIT_DIR = re.compile(r"^unit-(\d{2,})$")
ARCHIVE_FILE = re.compile(r"^(lesson|practice)-(\d+)\.json$")


def check_address_matches_directory(walk: Walk) -> Iterator[Finding]:
    """Every container map's address is the directory path holding it (§6).

    ⛔ Not resolved by preferring one over the other: an address is **recorded,
    never derived** (§6), and a disagreement means one of the two was written
    by something that did not know about the other.
    """
    for held in walk.containers:
        on_disk = held.directory.relative_to(walk.root / ARCHIVE_DIRNAME).as_posix()
        if on_disk != held.container.address.key:
            yield Finding(
                RULE_ADDRESS_DIRECTORY,
                held.where,
                f"declares the address {held.container.address.key!r} and is held by "
                f"the directory {on_disk!r}. An address is recorded, never derived, "
                f"so neither is preferred over the other.",
            )


def check_no_duplicate_addresses(walk: Walk) -> Iterator[Finding]:
    """No two containers claim the same address.

    ⭐ **This is `slugify`'s collision caught from the other end.** `slugify` is
    ASCII-lossy — `café` and `cafe` produce one slug — so two distinct titles
    can claim one address. ⛔ The framework cannot check the *cause*: by the
    time it sees an archive the title is gone, and §6 rules an address recorded
    rather than derived precisely so it never tries. ⚠️ But the *effect* needs
    no title at all.

    ⭐ Defence in depth, and the reason it is here rather than only in the
    adapter: **a collision only the adapter can see is a collision one careless
    adapter author disables.**
    """
    seen: dict[str, str] = {}
    for held in walk.containers:
        key = held.container.address.key
        first = seen.get(key)
        if first is None:
            seen[key] = held.where
        else:
            yield Finding(
                RULE_DUPLICATE_ADDRESS,
                held.where,
                f"claims the address {key!r}, which {first} already claims. "
                f"Two containers at one address means one silently overwrites the "
                f"other, and slugifying two distinct titles is how that happens.",
            )


def check_document_identity(walk: Walk) -> Iterator[Finding]:
    """Each document agrees with its container and with its own filename (R4).

    ⭐ Identity is embedded and location is data, so the two are independently
    written and a disagreement is real information rather than a tautology.
    """
    for unit in walk.units:
        yield from _identity(unit)


def _identity(unit: Unit) -> Iterator[Finding]:
    document, container = unit.document, unit.container
    if list(container.address.segments) != document.get("address"):
        yield Finding(
            RULE_IDENTITY,
            unit.where,
            f"declares an address its container map does not: the container is "
            f"{container.address.key!r}",
        )
    if document.get("variant") != container.variant:
        yield Finding(
            RULE_IDENTITY,
            unit.where,
            f"declares a variant its container map does not: the container is "
            f"{container.variant!r}",
        )
    directory = UNIT_DIR.match(unit.path.parent.name)
    if directory is None or int(directory.group(1)) != document.get("unit"):
        yield Finding(
            RULE_IDENTITY,
            unit.where,
            f"declares unit {describe(document.get('unit'))} and sits in {unit.path.parent.name!r}",
        )
    name = ARCHIVE_FILE.match(unit.path.name)
    if name is None:
        yield Finding(RULE_IDENTITY, unit.where, "is not named '<lesson|practice>-<n>.json'")
    elif (name.group(1), int(name.group(2))) != (document.get("kind"), document.get("ordinal")):
        yield Finding(
            RULE_IDENTITY,
            unit.where,
            f"declares kind {describe(document.get('kind'))} ordinal "
            f"{describe(document.get('ordinal'))}, which its filename does not",
        )


def check_digests(walk: Walk) -> Iterator[Finding]:
    """Each document's `content_sha256` matches its blocks.

    ⚠️ The re-ingest signal, and **only** that: a digest that disagrees means
    the material changed since it was read. ⛔ It cannot mean the adapter read
    everything — see `validate.source`.
    """
    for unit in walk.units:
        blocks = unit.document.get("blocks") or []
        if unit.document.get("content_sha256") != content_sha256(blocks):
            yield Finding(
                RULE_DIGEST,
                unit.where,
                "records a content_sha256 its blocks do not produce. The material "
                "changed since it was read: re-ingest it rather than editing the "
                "digest.",
            )


def check_counts(walk: Walk) -> Iterator[Finding]:
    """Each document's `counts` matches the blocks it carries.

    ⛔ A block that is not an object counts as no type: `check_block_shapes` names
    it, and reading a type off it here raised instead of yielding (R6).
    """
    for unit in walk.units:
        blocks = [block for block in unit.document.get("blocks") or [] if isinstance(block, dict)]
        recorded = unit.document.get("counts")
        if recorded != counts_of(blocks):
            yield Finding(
                RULE_COUNTS,
                unit.where,
                "records counts its blocks do not produce",
            )


def check_units_have_content(walk: Walk) -> Iterator[Finding]:
    """No document carries an empty block list (R6).

    ⛔ A unit with no content is the shape of a short read that produced a
    well-formed file, and it is reported rather than accepted as "a unit that
    happens to be empty".
    """
    for unit in walk.units:
        if not (unit.document.get("blocks") or []):
            yield Finding(RULE_EMPTY_UNIT, unit.where, "carries no blocks at all")


def check_declared_units_are_present(walk: Walk) -> Iterator[Finding | Unchecked]:
    """Every unit the container declares has at least one archive document.

    ⛔ **A document that was *refused* is not a missing unit.** Reporting it as
    one turns a corpus that breaks a single rule into two findings and sends
    the reader after the wrong defect: the personal-data fixture breaks
    exactly one rule and reads as one finding.
    """
    for held in walk.containers:
        present = {
            unit.document.get("unit") for unit in walk.units if unit.container is held.container
        }
        refused = {item.unit for item in walk.refused if item.container is held.container}
        for declared in held.container.units:
            if declared.n in present:
                continue
            if declared.n in refused:
                yield Unchecked(
                    RULE_UNIT_MISSING,
                    held.where,
                    f"unit {declared.n} has a document that was refused above, so "
                    f"whether the unit is complete could not be judged",
                )
            else:
                yield Finding(
                    RULE_UNIT_MISSING,
                    held.where,
                    f"declares unit {declared.n} and the archive holds no document for it",
                )


def check_practice_counts(walk: Walk) -> Iterator[Finding | Unchecked]:
    """Each unit's declared practice count matches the practices on disk.

    ⛔ The declaration is preserved verbatim by the reader precisely so this
    comparison is possible; a reader that corrected it would have deleted the
    disagreement this exists to find.
    """
    for held in walk.containers:
        refused = {item.unit for item in walk.refused if item.container is held.container}
        for declared in held.container.units:
            if declared.n in refused:
                # ⛔ Same rule as above: a refused document is not an absent
                # practice, and counting it as one would be a second finding
                # for one defect.
                yield Unchecked(
                    RULE_PRACTICE_COUNT,
                    held.where,
                    f"unit {declared.n} has a document that was refused above, so "
                    f"its practice count could not be compared",
                )
                continue
            found = sum(
                1
                for unit in walk.units
                if unit.container is held.container
                and unit.document.get("unit") == declared.n
                and unit.document.get("kind") == "practice"
            )
            if found != declared.practices:
                yield Finding(
                    RULE_PRACTICE_COUNT,
                    held.where,
                    f"declares {declared.practices} practice(s) for unit "
                    f"{declared.n}; the archive holds {found}",
                )


#: In the order a report reads best: the corpus, then each container, then
#: each document.
CHECKS = (
    check_address_matches_directory,
    check_no_duplicate_addresses,
    check_declared_units_are_present,
    check_practice_counts,
    check_document_identity,
    check_digests,
    check_counts,
    check_block_shapes,
    check_units_have_content,
)
