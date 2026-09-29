r"""What a path and a command may be, checked before either can reach a file.

**What it does.** Defines the safe pattern for the four workspace values and
refuses anything outside it.

**How you use it.** `require_path(value, "main_path", where)` and
`require_command(value, "run_command", where)`. Both return the validated
value; neither repairs one.

**Depends on.** `errors`. No filesystem — this asks what a value *is*, never
whether anything is there.

## ⛔ Why this is checked here and not where it is used

⚠️ **These four values leave the framework.** `main_path` and `test_path` reach
an editor's task file and a runner's arguments; `run_command` and
`test_command` are executed. They are validated **before they can
reach a generated document** — so the check is at the point the record is read,
not at the point somebody runs it, because by then the value is in a file that
several other things also read.

## ⛔ A command is argv. It is never a shell line.

⭐ **This is the whole security property and it is structural rather than a
blocklist.** `run_command` is a **list of arguments**, so there is no shell to
inject into: no `;`, no `|`, no `$(…)`, no glob, no quoting to get wrong.
⚠️ A string is refused outright rather than split on whitespace — splitting one
would be a shell parser, written here, badly, and `"mvn test; rm -rf ~"` would
become five perfectly valid tokens.

⭐ The token pattern is then belt to that brace. It carries no metacharacter
and no whitespace, so a value that somehow reached `sh -c` anyway is still one
word. ⛔ It is a permitted set, never a forbidden list, because a
forbidden list is an open set and cannot be finished: every shape nobody
listed walks through it.

## A path is relative, stays inside the workspace, and cannot become a flag

⛔ Absolute paths, `..`, backslashes and a leading `-` on any segment are all
refused. The first three put files outside the workspace (R3's prohibition
reached by accident); the fourth is the one that looks harmless — a file named
`-rf` handed to a command as an argument is an option, not a path.

⚠️ **An *argument* may not be absolute either**: `SAFE_ARGUMENT` permits `/`,
because a real command carries `-pl practice/basics-01`, so
`/home/<name>/run.sh` passes it. See `_is_rooted`; the reason it is refused is only partly R7.
"""

from __future__ import annotations

import re

from studyforge.exercise.errors import ExerciseError

# ⛔ **Anchored `\A…\Z`, never `^…$`**: Python's `$` also matches
# BEFORE a trailing newline, so `^…$` admitted `ok\n` — a character the set
# never names. `\Z` is the true end, so `.match` is a whole-value test for
# every caller, including `execute/commands.py`, which calls it directly.

#: One path segment. ⛔ A permitted set. The negative lookahead is the
#: leading-hyphen rule: `-rf` is an option wherever a path is passed as an
#: argument, and no material needs a file named that.
SAFE_SEGMENT = re.compile(r"\A(?!-)[A-Za-z0-9._-]+\Z")

#: One argv token. ⚠️ Wider than a segment on purpose — a real command carries
#: `-q`, `-pl`, `--batch-mode` and `key=value` — and still holds no whitespace
#: and no shell metacharacter.
SAFE_ARGUMENT = re.compile(r"\A[A-Za-z0-9._:=/@+-]+\Z")

#: Said in a refusal instead of the value, so the message tells an author what
#: to write. ⛔ Written out rather than derived from the patterns: a regex
#: pasted into a sentence is not a sentence.
PATH_PERMITTED = (
    "a relative path whose segments carry only ASCII letters, digits, '.', '_' "
    "and '-', with no segment beginning '-', no '..' and no backslash"
)
ARGUMENT_PERMITTED = (
    "a list of arguments, each carrying only ASCII letters, digits and "
    "'. _ - : = / @ +', none of them an absolute path or one climbing out of "
    "the workspace — and never one string, which would be a shell line"
)

#: Segments that name somewhere other than themselves.
TRAVERSAL = (".", "..")


def require_path(value: object, what: str, where: str) -> str:
    """Return `value` unchanged, or raise naming the field and what is permitted.

    ⛔ It never repairs one. Normalising `../x` to `x` would silently move an
    artifact to a different file from the one the adapter recorded, and the
    adapter is the only thing that knows which was meant (R2).
    """
    if not isinstance(value, str) or not value:
        raise ExerciseError(
            f"{where}: '{what}' must be a non-empty str; the value is not reproduced here, since "
            f"a refusal never quotes a value that may be personal"
        )
    if "\\" in value or value.startswith("/") or _is_drive_qualified(value):
        raise ExerciseError(_refusal(what, where))
    segments = value.split("/")
    if any(segment in TRAVERSAL or not SAFE_SEGMENT.match(segment) for segment in segments):
        raise ExerciseError(_refusal(what, where))
    return value


def require_command(value: object, what: str, where: str) -> tuple[str, ...]:
    """Return `value` as a tuple of argv tokens, or raise naming what is permitted.

    ⛔ A `str` is refused rather than split. See the module docstring: splitting
    one is writing a shell parser at the boundary that exists to avoid needing
    a shell at all.
    """
    if isinstance(value, str) or not isinstance(value, (list, tuple)) or not value:
        raise ExerciseError(
            f"{where}: '{what}' must be {ARGUMENT_PERMITTED}. The value is not "
            f"reproduced here, since a refusal never quotes a value that may be personal."
        )
    for token in value:
        if not _is_safe_argument(token):
            raise ExerciseError(
                f"{where}: one of '{what}'s arguments is outside the permitted "
                f"set. It must be {ARGUMENT_PERMITTED}. The argument is not "
                f"reproduced here, since a refusal never quotes a value that may be personal."
            )
    return tuple(value)


def _is_safe_argument(token: object) -> bool:
    """Return whether this argv token may be recorded and later executed."""
    return (
        isinstance(token, str)
        and bool(token)
        and SAFE_ARGUMENT.match(token) is not None
        and not _is_rooted(token)
    )


def _is_rooted(token: str) -> bool:
    """Return whether this argument is absolute, or climbs out of the workspace.

    ⛔ **Refused, and not only for R7.** `/home/<name>/run.sh` in a command is
    a home directory written into an archive — and the gate that would catch
    it is a shape list, which catches only the shapes it names. But
    `/usr/bin/mvn` carries no identifier and is refused too: a command runs in
    a **pinned toolchain image** (spec §8.1) where the program is on `PATH`, so
    an absolute path is a fact about the machine that wrote the record rather
    than about the corpus, and a build that depends on one is not reproducible
    (R10).

    ⚠️ `./gradlew` and `practice/basics-01` stay legal: relative is the whole
    point, and a workspace-relative argument is what the record is for.
    """
    return token.startswith("/") or _is_drive_qualified(token) or ".." in token.split("/")


def _refusal(what: str, where: str) -> str:
    """One sentence for every path refusal: the field, the rule, and no value."""
    return (
        f"{where}: '{what}' must be {PATH_PERMITTED}. These values reach a file "
        f"a runner executes against, so the shape is checked before the record "
        f"is accepted. The value is not reproduced here, since a refusal never quotes a value "
        f"that may be personal."
    )


def _is_drive_qualified(value: str) -> bool:
    r"""Return whether this is `C:\…` — absolute on a platform we do not target.

    ⚠️ Cheap, and it earns its place: `C:\Users\<name>\…` is a home path the
    personal-data gate's shape list does not recognise. Refusing the shape here
    means the gate never has to.
    """
    return len(value) > 1 and value[1] == ":" and value[0].isalpha()
