"""A unit's ordinal, and the name derived from it.

**What it does.** Validates that an ordinal is a counting number, and turns it
into the one string every consumer spells a unit with: `7` → `unit-07`.

**How you use it.** `require_ordinal(n, what)` at a boundary; `unit_name(n)`
wherever a unit needs naming.

**Depends on.** `errors` and `studyforge.describe`. Nothing else.

⛔ **No refusal here reproduces the value** (R7). The
type branch fires on whatever a caller passed where an ordinal was wanted —
including a path — so it names the **type** and not the payload. ⭐ The range
branch does quote, and that is not an inconsistency: by then the value is an
`int`, which is the one shape that cannot carry an identifier, and a refusal
that will not say `got 0` is a refusal nobody can act on.

⚠️ **It is `unit_name`, not `unit_dirname`** — the rename is the point, not
tidying. The extraction source called it `unit_dirname` because in that project
the string was always a directory. Here it is an **identity** (R4): it appears
in a progress key, in a served address, in an editor's task file and in an
artifact's embedded identity block, and only sometimes in a directory name.
⛔ A name that says `dir` invites the next reader to assume this package knows
about the filesystem, which is exactly what R1 and R4 forbid it to know.

⭐ **Zero-padded to two digits, and one is the first.** Padding is what makes a
plain sort agree with a numeric one — without it `unit-10` sorts before
`unit-2` — and every ordering in this project is derived from a declared
ordinal rather than from whatever order a filesystem enumerates (R10). Three
digits are not used: a container with a hundred units in it is a shape no
source in scope has, and widening later is a change to one function.
"""

from __future__ import annotations

from studyforge.address.errors import AddressError
from studyforge.describe import describe

#: `unit-NN`. Zero-padded to at least two digits; a unit numbered 100 widens
#: rather than truncating, because a truncated ordinal would collide.
UNIT_NAME = "unit-{:02d}"

#: Ordinals count from one, not zero. Unit ordinals are what a container map
#: declares and what §6 requires to be contiguous from 1, so a zeroth unit is
#: an off-by-one somewhere upstream and is refused here rather than filed.
FIRST_ORDINAL = 1


def require_ordinal(value: object, what: str = "unit ordinal") -> int:
    """Return `value` unchanged, or raise `AddressError` naming `what` it was.

    ⛔ `bool` is rejected even though it is an `int` in Python. `True` would
    otherwise be accepted as unit 1 and format as `unit-01`, which is a whole
    unit's material filed under a flag somebody passed by mistake.
    """
    if not isinstance(value, int) or isinstance(value, bool):
        raise AddressError(f"{what} must be an int, got {describe(value)}")
    if value < FIRST_ORDINAL:
        raise AddressError(f"{what} must be {FIRST_ORDINAL} or greater, got {describe(value)}")
    return value


def unit_name(n: int) -> str:
    """Return the name of unit `n`: `unit-07`, `unit-12`, `unit-100`."""
    return UNIT_NAME.format(require_ordinal(n))
