r"""`curriculum` — where a corpus records its curriculum, and how its names cross-check it.

**What it does.** Validates the optional top-level `curriculum` block: the
document that records the corpus's reading order and grouping, the address each
of that document's groups is filed at, and — optionally, per group — the
filename prefix its files carry, declared as a **cross-check**.

**How you use it.** `document.from_document` calls `parse_curriculum`; a caller
reads `Manifest.curriculum`, which is `None` when the corpus declares nothing.

    manifest.curriculum.record                  # 'README.md'
    manifest.curriculum.containers[1].address   # Address(('deeper',))
    manifest.curriculum.containers[1].prefix    # 'deep_'
    prefix_of("lessons/deep_10.md")             # 'deep_' — the name's own claim

**Depends on.** `studyforge.address` for what an address is, and `errors`.
⛔ It reads no file: whether the tree agrees with the declaration is the
adapter skill's `curriculum.filed` to say, because a manifest parses with no
filesystem (`document.parse`).

## ⛔ The record files a unit; a prefix never does

⭐ **§6: an address is recorded, never derived.** The document named by
`record` is the record, and `containers` says which address each of its groups
is filed at — the map a corpus's adapter otherwise carries as a constant a
second corpus would retype (R19).

⚠️ **A prefix is a derivation, so it may only ever agree.** The reconnaissance
skill's step 2 keeps the filename-prefix rule *"as a cross-check that must
agree — never as the source"*. So `prefix` decides nothing: it is a
second partition of the same files, and the adapter refuses when the two
partitions differ, in either direction.

## ⭐ A linked level: the record's own entries open the last level's containers

⚠️ **Some records write a level as a linked entry, not a label.** A section is
a bare line, and under it each module is a list entry linking the module's own
contents page, with the module's units indented beneath it. Declaring one
group per module is refused, because the record's labels are the sections, and
declaring the sections alone files every module's units into one container.

⭐ **`linked` names the last of `levels`, and says the record opens each of its
containers with a linked entry.** The declared `containers` are then the
record's labels at the level above it, and each linked entry beneath a label
is one container of the last level, filed at the label's address followed by
the name of the directory holding the file it links. ⛔ That name is written in
the record, so the address is still recorded and never derived (§6). The
adapter skill's `curriculum.filed` reads it and refuses a record that does not
have this shape.

## ⭐ What a prefix is, and why it may carry no digit

A file's name carries prefix `p` when its stem is exactly `p` followed by a
number: `s10.md` carries `s`, `10.md` carries the empty prefix, and
`Server.md`, `1a.md` and `s10-notes.md` carry none. ⛔ **A declared prefix may
contain no digit and no `/`**, and that is what makes the partition a
partition: with no digit in `p`, a stem's prefix is the stem with its trailing
number taken off, so no name can carry two declared prefixes.
"""

from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import PurePosixPath

from studyforge.address import Address, AddressError, parse_key
from studyforge.corpus.manifest.errors import ManifestError
from studyforge.corpus.manifest.fields import GLOB_CHARACTERS
from studyforge.describe import describe

#: The keys a `curriculum` block may carry, and the ones each group may carry.
#: ⛔ Closed sets: an unknown key is refused, never ignored (R9).
CURRICULUM_KEYS = ("record", "containers", "linked")
CONTAINER_KEYS = ("label", "address", "prefix")

#: A stem that is a prefix and a number, and nothing else.
PREFIXED = re.compile(r"(?P<prefix>\D*)(?P<number>\d+)")


@dataclass(frozen=True, slots=True)
class DeclaredContainer:
    """One group of the record: the label it is written under, its address, its prefix."""

    #: The group's label exactly as the record writes it, emphasis removed.
    label: str
    address: Address
    #: The prefix its files' names carry, or `None` for no cross-check.
    prefix: str | None = None


@dataclass(frozen=True, slots=True)
class Curriculum:
    """Where the curriculum is recorded, and what its groups are filed at."""

    #: The recording document, root-relative.
    record: str
    #: Its groups, in the order the record writes them; empty when undeclared.
    containers: tuple[DeclaredContainer, ...] = ()
    #: The last level's name when the record opens each of its containers with
    #: a linked entry beneath a group, else `None`. ⭐ The groups are then
    #: declared one level up, and each linked entry adds the last segment.
    linked: str | None = None

    @property
    def prefixes(self) -> dict[str, DeclaredContainer]:
        """Every declared prefix, and the group whose files carry it."""
        return {each.prefix: each for each in self.containers if each.prefix is not None}


def prefix_of(path: str) -> str | None:
    """Return the prefix `path`'s name carries, or `None` when it carries none."""
    match = PREFIXED.fullmatch(PurePosixPath(path).stem)
    return match.group("prefix") if match is not None else None


def parse_curriculum(
    value: object, where: str, depth: int, *, levels: tuple[str, ...] = ()
) -> Curriculum:
    """Return the declaration, refusing anything it does not state exactly.

    ⛔ **Each address is checked against this corpus's depth here**, where the
    levels are known, so no reader of the declaration meets an address the
    manifest could not have filed. ⭐ Under `linked` a group's address is one
    level short, because the linked entry supplies the last segment.
    """
    if not isinstance(value, dict):
        raise ManifestError(
            f"{where} 'curriculum' must be an object with 'record' and optionally "
            f"'containers' and 'linked', got {describe(value)}"
        )
    _closed(value, CURRICULUM_KEYS, "curriculum", where)
    if "record" not in value:
        raise ManifestError(f"{where} 'curriculum' must name its 'record'")
    record = _record_of(value["record"], where)
    linked = _linked_of(value, where, depth, levels)
    containers = _containers_of(value.get("containers", []), where, depth - 1 if linked else depth)
    if linked and any(each.prefix is not None for each in containers):
        # ⛔ A prefix checks the units of the group it is declared on, and under
        # `linked` those are filed one level further down, in several containers.
        raise ManifestError(
            f"{where} 'curriculum.linked' files each group's units into the containers "
            f"its linked entries open, so a group declares no 'prefix'"
        )
    return Curriculum(record=record, containers=containers, linked=linked)


def _linked_of(value: dict, where: str, depth: int, levels: tuple[str, ...]) -> str | None:
    """Return the linked level's name, refusing one that is not the last of `levels`.

    ⛔ Only the last level can be linked, a depth-1 corpus has no level above it
    to hold the groups, and a linked level with no declared groups has nothing
    to be linked beneath.
    """
    if "linked" not in value:
        return None
    linked = value["linked"]
    if depth < 2 or not levels or linked != levels[-1]:
        raise ManifestError(
            f"{where} 'curriculum.linked' must name the last of 'levels', in a corpus of "
            f"two levels or more, got {describe(linked)}"
        )
    if not value.get("containers"):
        raise ManifestError(
            f"{where} 'curriculum.linked' needs 'curriculum.containers': the record's "
            f"groups one level up, under which each linked entry opens a container"
        )
    return linked


def _record_of(value: object, where: str) -> str:
    """Return the recording document: a Markdown file inside the corpus, spelled one way.

    ⛔ **The value is never quoted** (R7): a path that is not a corpus path is
    usually somebody's home directory.
    """
    rule = (
        f"{where} 'curriculum.record' must be a path inside the corpus ending .md, "
        f"with no '.' or '..' segment and no glob character"
    )
    if not isinstance(value, str) or not value:
        raise ManifestError(f"{rule}, got {describe(value)}")
    path = PurePosixPath(value)
    clean = (
        path.as_posix() == value
        and not path.is_absolute()
        and not any(part in (".", "..") for part in path.parts)
        and not GLOB_CHARACTERS & set(value)
        and path.suffix == ".md"
        and path.stem != ""
    )
    if not clean:
        raise ManifestError(f"{rule}; the value is not reproduced, since it may be a path")
    return value


def _containers_of(value: object, where: str, depth: int) -> tuple[DeclaredContainer, ...]:
    """Each group's label, address and prefix, refusing a repeat of any of the three."""
    if not isinstance(value, list):
        raise ManifestError(
            f"{where} 'curriculum.containers' must be a list, got {describe(value)}"
        )
    found = tuple(
        _container_of(entry, f"{where} 'curriculum.containers[{index}]'", depth)
        for index, entry in enumerate(value)
    )
    for field in CONTAINER_KEYS:
        seen = [getattr(each, field) for each in found if getattr(each, field) is not None]
        if len(seen) != len(set(seen)):
            # ⛔ A repeated label is two groups the record cannot tell apart, a
            # repeated address is two groups filed in one place, and a repeated
            # prefix is a cross-check that no longer partitions anything.
            raise ManifestError(
                f"{where} 'curriculum.containers' repeats a {field}; each group of the "
                f"record has its own"
            )
    return found


def _container_of(entry: object, where: str, depth: int) -> DeclaredContainer:
    """One group, with its address checked at this corpus's depth."""
    if not isinstance(entry, dict):
        raise ManifestError(f"{where} must be an object, got {describe(entry)}")
    _closed(entry, CONTAINER_KEYS, "container", where)
    label = entry.get("label")
    if not isinstance(label, str) or not label.strip():
        raise ManifestError(f"{where} 'label' must be a non-empty str, got {describe(label)}")
    try:
        address = parse_key(entry.get("address"), depth)
    except AddressError as exc:
        # ⭐ The form is named, because `container.json` writes an address as a
        # list and this block writes it as the key a URL and a folder share.
        raise ManifestError(
            f"{where} 'address': {exc}. Write it as one key, {depth} segment(s) joined "
            f"by '/', such as {'/'.join(['a', 'b', 'c'][:depth])!r}"
        ) from None
    return DeclaredContainer(
        label=label, address=address, prefix=_prefix_of(entry.get("prefix"), where)
    )


def _prefix_of(value: object, where: str) -> str | None:
    """Return a declared prefix: no digit, `/` or whitespace. ⭐ Empty is a prefix."""
    if value is None:
        return None
    if not isinstance(value, str) or re.search(r"[\d/\s]", value) or GLOB_CHARACTERS & set(value):
        raise ManifestError(
            f"{where} 'prefix' must be a str with no digit, '/', whitespace or glob "
            f"character — a name carries it as the text before its number — "
            f"got {describe(value)}"
        )
    return value


def _closed(value: dict, keys: tuple[str, ...], what: str, where: str) -> None:
    """Refuse a key this block does not define. ⭐ Names are this module's, never quoted."""
    unknown = set(value) - set(keys)
    if unknown:
        raise ManifestError(
            f"{where} a {what} declaration has {len(unknown)} unknown key(s); "
            f"this build reads {list(keys)}"
        )
