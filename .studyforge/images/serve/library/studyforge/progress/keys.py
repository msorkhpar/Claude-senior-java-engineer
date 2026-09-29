"""A practice's key: the unit's address key with the practice's section key on the end.

**What it does.** Composes `<address>/unit-NN/<section>` from an `Address`, an
ordinal and a section key, and parses one back at a declared depth.

**How you use it.** `practice_key(address, 7, "practice-java")` to name a
practice; `parse_practice_key(key, depth)` for `(address, ordinal, section)`.

**Depends on.** `studyforge.address` — ⛔ **and composes nothing itself.** The
unit half is `Address.unit_key` and its inverse is `parse_unit_key`, because
the page's read mark, the index and the run route all join on that string by
equality. The extraction source recorded a pass under one spelling and read it
back under another, and nothing failed anywhere: the page simply never marked
the practice complete.

⚠️ **The section is a slug and nothing more.** `unit.sections` prefixes a
derived practice key with `practice-`, but an authored overlay's explicit key
wins and may be any slug, so requiring the prefix here would refuse a legal
practice.
"""

from __future__ import annotations

from studyforge.address import SEPARATOR, Address, AddressError, is_slug, parse_unit_key
from studyforge.describe import describe
from studyforge.progress.errors import ProgressError


def practice_key(address: Address, ordinal: int, section: str) -> str:
    """Return the key one practice is recorded under.

    ⛔ Takes an `Address`, never a string: a caller holding a key parses it
    with the address package first, so a malformed one is refused there.
    """
    if not isinstance(address, Address):
        raise ProgressError(f"a practice is addressed by an Address, got {describe(address)}")
    try:
        unit = address.unit_key(ordinal)
    except AddressError:
        # ⛔ Not chained into the message: `AddressError` may quote its input.
        raise ProgressError(
            f"a practice's unit ordinal must be 1 or more, got {describe(ordinal)}"
        ) from None
    return f"{unit}{SEPARATOR}{_section(section)}"


def parse_practice_key(key: str, depth: int) -> tuple[Address, int, str]:
    """Return `(address, ordinal, section)` for a key, or raise naming nothing of it."""
    if not isinstance(key, str) or not key:
        raise ProgressError(f"a practice key must be a non-empty str, got {describe(key)}")
    head, separator, section = key.rpartition(SEPARATOR)
    if not separator:
        raise ProgressError("a practice key must be '<address>/unit-NN/<section>'")
    try:
        address, ordinal = parse_unit_key(head, depth)
    except AddressError:
        raise ProgressError(
            f"a practice key must name a unit at the corpus's {describe(depth)} level(s) "
            f"before its section"
        ) from None
    return address, ordinal, _section(section)


def _section(section: object) -> str:
    """Return `section` if it is a slug; refuse by type and length, never by value."""
    if not isinstance(section, str) or not section:
        raise ProgressError(
            f"a practice's section key must be a non-empty str, got {describe(section)}"
        )
    if not is_slug(section):
        raise ProgressError(
            f"a practice's section key must be a slug, got a {len(section)}-character str "
            f"that is not one"
        )
    return section
