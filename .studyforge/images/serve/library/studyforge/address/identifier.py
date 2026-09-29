"""A slug as a code identifier — a package segment, a class name's prefix, a module.

**What it does.** Turns one address segment into a form that is legal where a
hyphen is not and a leading digit is not: `16-streams-api` → `_16_streams_api`.

**How you use it.** `identifier(slug)` for one segment. ⛔ It returns a
*segment*, never a dotted package and never a path — joining them is the
caller's, because how they join is a placement decision (§5) and this
package knows nothing about where anything lives (R1, R4).

**Depends on.** `slug` and `errors`. Nothing else, and deliberately nothing
that knows what language the identifier is for.

## Why a leading digit is prefixed and never dropped

A leading digit is legal in a directory name and illegal in a Java package
segment, and one of the four designed shapes numbers every module —
`09-records`, `10-sealed` (spec §4's table names it).
⛔ **Dropping the digit makes `01-basics` and `basics` the same identifier**,
which is one practice module silently overwriting another.

⭐ **The prefix is `_`, and that is a correction to the inherited rule rather
than a copy of it.** The extraction source prefixes `c`, which fixes the
dropping collision and quietly introduces another: `c01-a` and `01-a` both
become `c01_a`. `_` cannot collide with anything, because **no slug contains an
underscore** — `slugify` turns `_` into a separator — so no slug can produce an
identifier that starts with one. The inherited docstring says the point is that
two slugs must not collide; this is that intent, held to.

⭐ **The function is therefore injective over slugs, and that is a testable
claim rather than a hopeful one.** `tests/studyforge/address/test_identifier.py`
asserts it over the adversarial pairs directly.

⚠️ `_16_streams_api` is a legal identifier in Java, Kotlin and Python. A single
`_` is reserved in Java 9+, but `identifier` never returns one: a slug is
non-empty, so the result always has a character after the prefix.
"""

from __future__ import annotations

from studyforge.address.slug import require_slug

#: Prefixed when a slug starts with a digit. ⛔ Chosen because no slug can
#: begin with it, which is what makes `identifier` injective; any letter would
#: be a character some other slug can also start with.
DIGIT_PREFIX = "_"


def identifier(slug: str) -> str:
    """Return `slug` as a code identifier: hyphens become underscores, a leading digit is prefixed.

    Raises `AddressError` if `slug` is not already a slug — the same refusal
    `require_slug` makes, for the same reason.
    """
    require_slug(slug, "identifier source")
    ident = slug.replace("-", "_")
    return DIGIT_PREFIX + ident if ident[0].isdigit() else ident
