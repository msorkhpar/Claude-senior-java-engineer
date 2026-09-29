"""The logical address of a unit: N segments, joined, so its halves cannot be swapped.

**What it does.** Models where a unit sits in a corpus's hierarchy, and the
keys, slugs and identifiers derived from it. The depth is not two and not
fixed: it is exactly `len(levels)` from the corpus manifest, because a real
source may have one container level (a course, a group) or two (section →
module), and a contract that cannot express both is wrong (spec §1, §4).

**How you use it.** Build an address from a corpus's `levels` and the segment
values; ask it for its key, its slug, and its stable identifier. ⛔ Never
assemble one of those strings by hand elsewhere — the joined key exists
precisely so that two segments cannot be passed in the wrong order without a
type saying so, and that guarantee is void the moment a caller re-derives it.

    from studyforge.address import Address, parse_key, slugify

    address = Address.of("basics", "01-getting-started")
    address.key                     # 'basics/01-getting-started'
    address.depth                   # 2
    address.identifiers             # ('basics', '_01_getting_started')
    address.unit_key(7)             # 'basics/01-getting-started/unit-07'
    parse_key(address.key, depth=2) # the same address back

**Depends on.** Nothing. This is the bottom of the framework, and every other
package depends on it rather than the reverse.

⚠️ **Identity survives a move; presentation need not** (R4). An address is
embedded in the artifact it names, so a page moved away from its assets is
still correctly identified and still resolves as the unit it is — while
rendering unstyled and silent, because assets resolve relative to the page.
Both are true and neither is a defect.

## What is in the package

| Module | Owns |
|---|---|
| `address` | `Address`, `parse_key`, `parse_unit_key` — the identity and its two inverses |
| `slug` | what a slug **is**, and `slugify` for adapters that must make one |
| `identifier` | one segment as a code identifier, injectively |
| `ordinal` | a unit's ordinal, and `unit-NN` |
| `errors` | `AddressError`, the only exception any of it raises |

⛔ **Depth 1 is the common case, not the degenerate one.** Two of the four
designed source shapes have a single container level — one declares
`["course"]` and another `["group"]`; **spec §4's table is where they are named**
— so nothing here is written for two levels and then
checked against one. Every function takes the depth as data and the tests run
each of them at depths 1 through 4.
"""

from __future__ import annotations

from studyforge.address.address import SEPARATOR, Address, parse_key, parse_unit_key
from studyforge.address.errors import AddressError
from studyforge.address.identifier import DIGIT_PREFIX, identifier
from studyforge.address.ordinal import FIRST_ORDINAL, require_ordinal, unit_name
from studyforge.address.slug import is_slug, require_slug, slugify

#: ⛔ The package's whole public surface. A consumer that has to import
#: `studyforge.address.address` directly is a consumer this contract failed —
#: a package's `__init__.py` is its contract (R17), and
#: this is what it says.
__all__ = [
    "DIGIT_PREFIX",
    "FIRST_ORDINAL",
    "SEPARATOR",
    "Address",
    "AddressError",
    "identifier",
    "is_slug",
    "parse_key",
    "parse_unit_key",
    "require_ordinal",
    "require_slug",
    "slugify",
    "unit_name",
]
