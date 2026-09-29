"""The one exception this package raises.

**What it does.** Names every way a scan cannot be run at all, so a caller
catches one type rather than four.

**How you use it.** Catch `DiscoveryError`.

**Depends on.** Nothing.

## ⚠️ Most of what goes wrong here is not raised

⛔ **A scan reports; it does not stop.** An artifact this build cannot identify
is a `site.Unidentified` in the result, **named** (R6) — not an exception that
ends the walk at the first bad file and hides every one after it. This type
exists for the failures that make the *whole* scan meaningless: a root that is
not a directory.

⚠️ It is also the internal carrier for a single artifact's fault, caught one
file later and turned into an `Unidentified`. ⭐ That is deliberate rather than
lazy: the message `placement` produces for a bad identity block is already the
message a reader needs, and re-deriving it here would give one fault two
spellings.

⛔ **One exception travels through, exactly as it does in `placement` and in
`unit`** (R7). `identity.from_document` gates the block it
reads, and `PersonalDataLeak` from `archive.scrub` is **not** caught by the
per-artifact handler. This family exists so a scan over a thousand files
catches one type per file, records it and carries on; an R7 refusal inside it
would be filed as one more page that could not be identified, in a report whose
whole purpose is that nobody reads it line by line. ⭐ The leak stops the scan.

⚠️ **`ValueError`, following `studyforge.address`'s split and `placement`'s.** A root that is
not a directory is "you handed me something I cannot scan", not a malformed
document — and the malformed documents this package meets are reported rather
than raised.
"""

from __future__ import annotations


class DiscoveryError(ValueError):
    """A scan that cannot be run, or one artifact that cannot be identified.

    ⛔ **The message names the file by its path relative to the scan root, and
    never by an absolute one** (R7). A scan runs over a real
    directory on somebody's machine and its findings go into a report; an
    absolute path in one of them carries a home directory out of the walk that
    was written to read a repository, not to describe where it sits.
    """
