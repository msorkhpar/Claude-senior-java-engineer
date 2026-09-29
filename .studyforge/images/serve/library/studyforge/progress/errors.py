"""The progress store's two refusals: a bad request, and a file that is not a record.

**What it does.** Defines `ProgressError`, raised for anything this package
refuses, and `ProgressFormatError`, its subclass for a file on disk that is not
a progress document.

**How you use it.** Catch `studyforge.progress.RAISES` around any call into the
package; catch `ProgressFormatError` alone where a malformed record needs its
own sentence.

**Depends on.** Nothing.

⛔ **No message in this package reproduces a value it refused** (R7,
R6). A practice key, a command or a path is exactly where a home directory
arrives, so a refusal names the field and describes the value by type.
"""

from __future__ import annotations


class ProgressError(Exception):
    """The store refused a request, or could not do what was asked of it."""


class ProgressFormatError(ProgressError):
    """The file on disk is not a progress document, and it is left exactly as it is.

    ⛔ Never repaired and never reset: it is the reader's own record of what
    they passed, and a surprise in it is something to look at.
    """
