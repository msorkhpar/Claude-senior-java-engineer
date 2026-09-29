"""The one exception this package raises.

**What it does.** Names every way an address cannot be turned into a location,
so a caller catches one type rather than four.

**How you use it.** Catch `PlacementError`.

⚠️ **One exception travels through, deliberately** (R7).
`identity.from_document` gates the block it reads, and `PersonalDataLeak` from
`archive.scrub` is **not** wrapped: R7's refusal is louder than a placement
error, and this family exists so a caller sweeping a site catches one type per
artifact and carries on. ⛔ An R7 refusal inside it would be logged as one more
file that could not be placed, and the leak would be the thing nobody looked
at. ⭐ **A contract that names what crosses it is better than one that swallows
it** — which is the whole answer to *"a promise with one exception is not a
promise"*.

**Depends on.** Nothing.

⚠️ **`ValueError`, following `studyforge.address`'s split**: every failure here is "you handed
me something I cannot place" — an unknown profile name, an origin that escapes
the source root, a title that slugifies to nothing. ⛔ None of them is a
*document* error, because this package reads no files at all (that is `corpus.discovery`).
"""

from __future__ import annotations


class PlacementError(ValueError):
    """An address, origin or profile this build cannot turn into a location.

    ⛔ **The message names the field, and the accepted values where a closed set
    was expected — never the offending value itself** (R7). It never formats an
    exception object into itself either, which would carry an absolute path into
    a log.

    ⚠️ **This sentence is part of the rule**: the next author follows the
    module's written policy, so the policy must be the one the code keeps.
    ⭐ `studyforge.describe` is how a refusal says what arrived without saying
    what it said.
    """
