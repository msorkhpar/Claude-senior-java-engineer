"""The one exception this package raises.

**What it does.** Names every way the page's assets can be unusable, so a
caller catches one type rather than three.

**How you use it.** Catch `AssetError`.

**Depends on.** Nothing.

⚠️ **`Exception`, not `ValueError`, and that follows the package error split rather than
inventing one.** A *value* error means "you handed me something I cannot
accept"; a *document* error means "this file is not one I can read". Every
failure here is the second kind — a part missing from the package, a vendored
bundle without its licence — and none of them is caused by a caller's argument
being wrong in a way the caller could fix by passing something else.

⛔ **A missing asset is loud and immediate.** A page rendered without its
stylesheet is well-formed, unreadable, and fails nothing (R6).
"""

from __future__ import annotations


class AssetError(Exception):
    """A page asset this build cannot compose.

    ⛔ The message names the part and what was wrong with it, and **never
    formats an exception object into itself**: an `OSError` renders with the
    absolute path it was given, which is the user's home directory in a build
    log (R7).
    """
