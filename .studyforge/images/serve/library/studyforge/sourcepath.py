r"""What a path inside the source may be, spelled once, as a permitted set.

**What it does.** Answers one question — *is this string a location inside the
corpus, relative to its root?* — and, when it is not, names the fault without
reproducing the value.

**How you use it.** `source_path_fault(value)` returns `None` for a legal path
or a noun phrase for the first fault it finds; `is_source_path(value)` when a
caller wants only the verdict. Each caller raises its own package's error with
its own sentence — ⭐ the rule is this module's, the document is the caller's.

**Depends on.** `pathlib`, and nothing else.

## ⛔ This enumerates the legal, and that is the whole design

⚠️ **Two readers spelling this rule twice do not agree.** A reader refusing
`startswith("/")`, `startswith("~")` and `".." in parts`, and another refusing
`PurePosixPath.is_absolute()` and `".." in parts`, are both forbidden lists,
both open sets, and the gap between them is reachable:
`C:/Users/<name>/material/README.md` is refused by *neither* predicate — no
leading slash, no tilde, no `..`, and `is_absolute()` is `False` because a
drive letter means nothing to POSIX — and `\\host\home\<name>\README.md` is
refused by neither either. ⛔ Two overlapping forbidden lists cannot be
reconciled by adding entries to both; **the answer is to state the permitted
set instead, which has an end.**

A source path is legal when **every one of these holds**:

| requirement | what it excludes |
|---|---|
| at least one segment | `""`, `"   "`, `"/"` |
| not absolute | `/home/<name>/x`, `/export/home/<name>/x`, `/anything` |
| first segment does not begin with `~` | `~/x`, `~<name>/x` |
| no segment is `..` | `../outside`, `a/../../b` |
| no segment carries `\` | `\\host\home\<name>`, `a\b` |
| no segment carries `:` | `C:/Users/<name>/x`, `file:///x` |
| no segment carries `#` | `TestCases.md#3. Card issuance` |

⛔ **The fragment row refuses a shape that means nothing.** Nothing reads the
part after a `#` in `TestCases.md#…`, so the unit would silently be read as
the whole file. ⭐ **A region is declared as `{path, section}`**, and this rule
keeps the fragment spelling out. ⚠️ **It also refuses a real
POSIX filename containing `#`**, on the same terms as the two rows above it.

⭐ **The last three narrow the legal set below what POSIX permits, deliberately.**
A file really may be named `notes:draft.md` on Linux, and this rule refuses to
place it. That is the price of the row above it: `C:/Users/<name>` and
`\\host\home\<name>` are home directories that no POSIX predicate calls
absolute, and a rule that admits a colon must find another way to say so. ⚠️
The cost is bounded and visible — an adapter is told exactly which character it
may not use — where the cost of the alternative is a home path on disk that
nothing refused.

## The fault is described, never quoted

⛔ Every value this module refuses is, by construction, a candidate home
directory: quoting it in the refusal would copy personal data into a log from
the check that exists to catch it (R7). So the return value is a noun phrase
about the *shape*, and the caller's sentence names the field. This is the same
contract `address.slug.slug_fault` keeps, for the same reason.
"""

from __future__ import annotations

from pathlib import PurePosixPath

#: How a legal source path is described to somebody who supplied an illegal
#: one. ⭐ Positive, because the permitted set is what this module knows.
SOURCE_PATH_DESCRIBED = "a location inside the source, relative to the corpus root"


def source_path_fault(value: str) -> str | None:
    """Name the first way `value` is not a source path, or `None` if it is one.

    ⛔ Returns a noun phrase about the shape and never the value itself.
    """
    if not value.strip():
        return "an empty path"
    path = PurePosixPath(value)
    if path.is_absolute():
        return "an absolute path"
    parts = path.parts
    if not parts:
        return "an empty path"
    if parts[0].startswith("~"):
        # ⭐ Both tilde forms at once. `~/x` is rooted at *a* home directory and
        # `~<name>/x` is rooted at a *named* one; the second is personal data
        # and the first is not, but neither is a location inside the source, so
        # this rule needs no opinion about which.
        return "a path rooted at a home directory"
    if ".." in parts:
        return "a path leaving the source root"
    for part in parts:
        if "\\" in part:
            return "a path written with Windows separators"
        if ":" in part:
            return "a path carrying a drive letter or scheme"
        if "#" in part:
            # ⛔ A fragment is a *region*, and a region is declared
            # as an object with its own `section`. Left legal here, the old
            # spelling keeps validating and keeps meaning nothing.
            return "a path carrying a fragment"
    return None


def is_source_path(value: object) -> bool:
    """Return whether `value` is a location inside the source, relative to its root."""
    return isinstance(value, str) and source_path_fault(value) is None
