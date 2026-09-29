"""The one exception this package raises.

**What it does.** Names every way an address, a slug, an identifier or a unit
ordinal can be wrong, so a caller catches one type rather than four.

**How you use it.** Catch `AddressError`. Every public function in this package
raises it and nothing else; none of them returns a sentinel, returns `None` for
bad input, or repairs a value quietly (R6).

**Depends on.** Nothing.

⛔ **Read the class docstring before writing a message that formats a value.**
It carries the one rule this package's refusals must keep.

⚠️ **It subclasses `ValueError`, and that is a deliberate divergence from the
extraction source**, whose `LayoutError` and `RawDocError` subclass `Exception`
directly. Every failure here is one shape — *a caller passed a value this
package cannot accept* — which is what `ValueError` means, so code that already
handles bad input handles these too without importing anything from the
framework.

⚠️ That reasoning does **not** generalise to a document reader. "This file is
not an archive I can read" is not a bad argument; it is a bad file, and
`Exception` is right for it. Two different failure kinds, two different bases —
and the split is a precedent other packages follow rather than a rule this
one assumes.
"""

from __future__ import annotations


class AddressError(ValueError):
    """A value this package cannot accept as a slug, address or ordinal.

    ⛔ **The message never reproduces the offending value, and never says only
    "invalid" either.** Both halves are required: a message that names the
    offending value with `!r` would pass an R7 echo to every address segment,
    identity field and unit ordinal in the framework.

    ⚠️ **This sentence is part of the rule.** The next author to touch
    `slug.py` reads the module's documented policy and follows it, so the
    policy written here must be the one the code keeps.

    ⭐ **What a refusal owes the reader instead** (R6, and it is more
    actionable than the value was): the **field** that was wrong — `what` —
    the **class** that was expected, and for a string the **position** at
    which it failed. `slug.slug_fault` and `studyforge.describe` are how this
    package says those things.
    """
