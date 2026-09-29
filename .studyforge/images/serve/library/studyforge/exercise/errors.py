"""The one exception the exercise package raises.

**What it does.** Names every way an `exercise` object can be unacceptable, so
a caller catches one type rather than four.

**How you use it.** Catch `ExerciseError`.

**Depends on.** Nothing. ⭐ The same reason `archive/errors.py` and
`unit/errors.py` exist as modules of their own: `safety` and `record` both
raise it, and putting it in either would make the other import a module it has
no business knowing.

⚠️ **`ValueError`, following the package error split and the two contracts either side of
this one.** An exercise is a *value read from a document*, so it fails the way
a manifest and an overlay fail.

⛔ **It is wrapped at the archive boundary, never re-raised through it.**
`archive.errors` states the rule: reading an archive document raises
`ArchiveError` and nothing else, *including where the rule being applied is
somebody else's*. So `archive.document.parse` catches this and re-raises —
a caller reading a file wants one answer to "can I use this?".
"""

from __future__ import annotations


class ExerciseError(ValueError):
    """An `exercise` record this build will not accept.

    ⛔ The message names the field and what is **permitted**, and never the
    offending value (R7). A `main_path` is corpus data read out of a file
    somebody else wrote, and the one shape being refused by the safe pattern is
    exactly the shape that carries a home directory — so a refusal that quoted
    it would copy personal data into a build log, from the check that exists to
    stop it reaching one.

    ⭐ Naming what is permitted is also the more useful message: it tells an
    adapter author what to write, where echoing the value only shows them what
    they already typed. Precedent: `placement.names.label_of`.
    """
