"""The address itself: N segments joined into one string that cannot be re-split wrong.

**What it does.** Models where a unit's container sits in a corpus's hierarchy
— exactly `len(levels)` slug segments — and the two strings derived from it:
the container `key`, and the `unit_key` that addresses one unit inside it.

**How you use it.** `Address.of("basics", "01-getting-started")` when you have
segments; `Address(document["address"])` when you have the JSON list an archive
document stores; `parse_key(key, depth)` when you have a key and the corpus's
declared depth. Ask an address for `.key`, `.depth` and `.identifiers`.

**Depends on.** `slug`, `identifier`, `ordinal`, `errors`,
`studyforge.describe`. ⛔ Not `pathlib`,
not `os`, not the manifest — an address is an identity, and where it lives is
`corpus/placement/`'s answer (R1, R4). `tests/studyforge/address/test_init.py`
asserts the whole package imports nothing that touches a filesystem.

## Why the key is one string

The extraction source passed `(path, course)` as a pair for a while, and the
two are both lowercase hyphenated slugs — so swapping them is invisible until a
directory is missing. `CourseRef` existed to make the swap impossible, and its
`key` was one joined string for the same reason. v1 widens *exactly two* to
*exactly `len(levels)`* and keeps the property.

⭐ **The separator can never appear inside a segment**, which is what makes the
join lossless: `slugify` turns `/` into a hyphen like every other
non-alphanumeric, so no slug contains one. The key therefore has exactly one
segmentation, and `parse_key` is a true inverse of `.key`.

## Where arity is checked, and why here rather than in the manifest

⚠️ **This is the address/manifest boundary, decided once.** `corpus.json`
*declares* the depth, by declaring `levels`; `corpus.manifest` owns that field and
validates it as a declaration. **This package owns the comparison** — given a depth, is this
address the right shape for it? — because the address is the thing being
checked and it is the only thing both halves have in common.

⛔ **So `parse_key` takes a `depth` and it is not optional.** The extraction
source's `parse_key` always checked arity; it could hardcode `2`. The v1
generalisation is that the number comes from data, and making it optional would
turn a check that always ran into one that usually does not. A caller with no
declared depth is not parsing a key it can validate, and should say so by
building an `Address` from segments instead.
"""

from __future__ import annotations

from dataclasses import dataclass

from studyforge.address.errors import AddressError
from studyforge.address.identifier import identifier
from studyforge.address.ordinal import require_ordinal, unit_name
from studyforge.address.slug import require_slug
from studyforge.describe import describe

#: What joins segments into a key. ⛔ Never appears inside a segment, because
#: `slugify` turns it into a hyphen — that is what makes the join reversible.
SEPARATOR = "/"


@dataclass(frozen=True, slots=True)
class Address:
    """A container's address: one slug per declared level, in order, immutable.

    ⚠️ **Accepts a list as well as a tuple, and stores a tuple.** An archive
    document spells its address as a JSON array, so `Address(doc["address"])`
    is the common construction and refusing a list would make every caller
    write the same conversion. What is stored is always a tuple, so an address
    is hashable and cannot be edited after it is validated.
    """

    segments: tuple[str, ...]

    def __post_init__(self) -> None:
        """Normalise to a tuple and refuse anything that is not a slug."""
        if isinstance(self.segments, str) or not isinstance(self.segments, (list, tuple)):
            raise AddressError(
                f"address segments must be a list or tuple of slugs, got {describe(self.segments)}"
            )
        segments = tuple(self.segments)
        if not segments:
            raise AddressError("address must have at least one segment, got none")
        for position, segment in enumerate(segments, start=1):
            require_slug(segment, f"address segment {position} of {len(segments)}")
        object.__setattr__(self, "segments", segments)

    @classmethod
    def of(cls, *segments: str) -> Address:
        """Return an address from segments given one per argument.

        ⛔ It does **not** slugify. There is deliberately no constructor that
        accepts titles: one that did would make "a title passed where a slug
        is required" a silent success, which is the failure `require_slug`
        exists to make loud. An adapter slugifies, records what it produced,
        and passes the recorded value here.
        """
        return cls(segments)

    @property
    def depth(self) -> int:
        """How many container levels this address names."""
        return len(self.segments)

    @property
    def key(self) -> str:
        """The address as one string — what hrefs, progress keys and APIs carry."""
        return SEPARATOR.join(self.segments)

    @property
    def identifiers(self) -> tuple[str, ...]:
        """Each segment as a code identifier, in order.

        ⛔ Returned as separate segments and never joined. How they join — a
        dotted package, a directory chain, a class-name prefix — is a
        placement decision, and this package does not make placement decisions
        (R1, R4).
        """
        return tuple(identifier(segment) for segment in self.segments)

    def require_depth(self, depth: int) -> Address:
        """Return self, or raise `AddressError` if this address is not `depth` deep.

        For a caller that already holds an address and a manifest — the check
        `parse_key` makes for you, available on its own.
        """
        if not isinstance(depth, int) or isinstance(depth, bool) or depth < 1:
            raise AddressError(f"declared depth must be a positive int, got {describe(depth)}")
        if self.depth != depth:
            raise AddressError(
                f"address {self.key!r} has {self.depth} segment(s); the corpus "
                f"declares {depth} level(s)"
            )
        return self

    def unit_key(self, n: int) -> str:
        """Return `<key>/unit-NN` — how one unit is addressed as a string.

        ⛔ **One composer, because these keys are joined by string equality
        across surfaces that never see each other**: the page writes a read
        mark under it, the index reads the mark back, the run route names a
        workspace with it. A second spelling that differed by one character
        would simply never match anything, with nothing failing anywhere —
        which the extraction source records as having already happened once.
        """
        return f"{self.key}{SEPARATOR}{unit_name(n)}"

    def __str__(self) -> str:
        """Return the key, so an address formats as its own identity."""
        return self.key


def parse_key(key: str, depth: int) -> Address:
    """Return the `Address` that `key` names, checked against a declared `depth`.

    The inverse of `Address.key`. `depth` is required and comes from the
    corpus manifest's `levels`; see this module's docstring for why it is not
    optional.
    """
    if not isinstance(key, str) or not key:
        raise AddressError(f"address key must be a non-empty str, got {describe(key)}")
    return Address(tuple(key.split(SEPARATOR))).require_depth(depth)


def parse_unit_key(key: str, depth: int) -> tuple[Address, int]:
    """Return the `(address, ordinal)` that a `<key>/unit-NN` string names.

    The inverse of `Address.unit_key`, and the reason a unit key is safe to
    store: what was written can be read back to the same two values, or
    refused.
    """
    if not isinstance(key, str) or not key:
        raise AddressError(f"unit key must be a non-empty str, got {describe(key)}")
    head, separator, tail = key.rpartition(SEPARATOR)
    if not separator:
        raise AddressError(
            f"unit key must be '<address>{SEPARATOR}unit-NN', got {describe(key)} "
            f"with no {SEPARATOR!r} in it"
        )
    address = parse_key(head, depth)
    ordinal = _ordinal_of(tail, key)
    return address, ordinal


def _ordinal_of(name: str, key: str) -> int:
    """Return the ordinal a `unit-NN` name carries, or raise naming the fault.

    ⚠️ **`key` is quoted in the second refusal and described in the first, and
    the asymmetry is the rule rather than an oversight** (R7). By
    the time the second one fires, `name` has been proved to be
    `unit-<digits>` and the address before it has been proved to be slugs — so
    the whole key is this framework's own vocabulary. Before that proof it is
    whatever a caller passed, which may be a path.
    """
    prefix, separator, digits = name.partition("-")
    if prefix != "unit" or not separator or not digits.isdigit():
        raise AddressError(
            f"unit key must end in 'unit-NN'; after the address it carries "
            f"{describe(name)} that is not one"
        )
    ordinal = require_ordinal(int(digits), "unit ordinal")
    # ⛔ Round-trip rather than accept: `unit-7` and `unit-007` both parse to 7
    # and would then be written back as `unit-07`, so two spellings of one unit
    # would exist and compare unequal on the surfaces that join by string.
    if unit_name(ordinal) != name:
        raise AddressError(f"unit name must be {unit_name(ordinal)!r}, got {name!r} in {key!r}")
    return ordinal
