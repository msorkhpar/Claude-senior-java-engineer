"""The one exception this package raises.

**What it does.** Names the family a consumer catches when contents are built,
read back, or joined with a local status document.

**How you use it.** `except ContentsError` — and nothing else, because every
refusal in this package is raised as one.

**Depends on.** Nothing. ⛔ Deliberately not on `VersionError`: `studyforge.
version` hands the caller's own type back, and this package is that caller.
"""

from __future__ import annotations


class ContentsError(ValueError):
    """A contents document that cannot be built, read, or joined.

    ⛔ **Not a subclass of `VersionError`**, for the reason `version.py`'s own
    docstring gives: each contract keeps its own front door, and a consumer
    catching this one wants *"the contents are wrong"*, never *"something in
    this build disagreed about a version"*.

    ⚠️ `PersonalDataLeak` is deliberately **outside** this family and travels
    through as itself (R7). A caller that folded an R7 refusal into
    *"the contents would not build"* would log the leak as one more corpus
    that did not render, with the leak the thing nobody looked at.
    """
