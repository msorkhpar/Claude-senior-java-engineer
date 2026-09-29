"""The one exception the archive package raises.

**What it does.** Names every way an archive document can be unacceptable, so
a caller catches one type rather than five.

**How you use it.** Catch `ArchiveError`. ⛔ Reading or building an archive
document raises it and nothing else — including where the rule being applied
is somebody else's, as an address that is not a slug is (`studyforge.address`'s), because a
caller reading a file wants one answer to *"can I use this?"*.

**Depends on.** Nothing. ⭐ That is the point of a separate module: both
`blocks` and `document` raise it, and putting it in either would make the
other import a module it otherwise has no business knowing. The same reason
`markdown/errors.py` exists inside the reader.

⚠️ **Two exceptions travel through, deliberately.** `PersonalDataLeak` from
`archive.scrub` is **not** wrapped: R7's refusal is louder than a format
error and a caller that catches `ArchiveError` should not accidentally
swallow a leak. `VersionError` never appears because `version`'s guard is handed
`error=ArchiveError` — each contract keeps its own front door.

⚠️ **`ValueError`, following the manifest's precedent.** A document error is
a value error: somebody handed us something we cannot accept.
"""

from __future__ import annotations


class ArchiveError(ValueError):
    """An archive document this build will not accept.

    ⛔ The message names the file and the field, and — where a closed set was
    expected — what the accepted values are. ⛔ It never formats an exception
    object into itself: `json.JSONDecodeError` and `OSError` both carry text
    written by whoever raised them, and `OSError`'s is an absolute path (R7).
    """
