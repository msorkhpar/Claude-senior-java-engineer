"""The one exception this package raises.

**What it does.** Names every way a media footprint cannot be measured or a
media policy cannot be honoured, so a caller catches one type rather than
three.

**How you use it.** Catch `MediaError`.

**Depends on.** Nothing.

⚠️ **`ValueError`, following the split `placement.errors` states**: every
failure here is *"what you handed me cannot be weighed against this policy"* —
a root that is not a directory, an `auto` policy with no measurement behind it,
a corpus whose generated media has outgrown the limits it declared. ⛔ None of
them is a *document* error: this package never reads `corpus.json`, it is
handed the policy that was read from it.
"""

from __future__ import annotations


class MediaError(ValueError):
    """A media footprint that cannot be measured, or a policy that refuses it.

    ⛔ **The message names the limit, the measured number and the generated file
    responsible — and never a path this framework did not mint** (R7). A
    measured path here is always relative to the corpus root, because that is
    the only shape the measurement records; the root itself is described and
    never echoed, since it is an absolute path on somebody's machine and this
    refusal is one a build prints into a log.

    ⭐ **The refusal is the product, not a side effect.** Crossing the ceiling
    is a decision (§5), and this exception is how it reaches a person early —
    naming the number, the limit it crossed and the ways forward — instead of
    at a push that has already become impossible.
    """
