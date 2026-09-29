"""The plain fields of `corpus.json`: `source`, `title`, `levels`, `variants`, two flags, a place.

⭐ The flags are `exercises` and `narration`; the place is
`onboarding_doc`.

**What it does.** Validates one top-level value each, and raises this
package's `ManifestError` naming the key it read.

⚠️ **One function per key, and the key is spelled inside it**, never passed
in: a key a caller supplies is caller data, and a refusal that quotes caller
data is an R7 echo.

**How you use it.** `document.from_document` calls these; nothing else does.
⛔ They are the manifest's own field rules, not a general validator.

**Depends on.** `studyforge.address` for what a slug is, and `errors`.

⭐ **Split from `document.py` at the seam between the document and its fields**:
the `runtimes` key took `document.py` past R11's bound,
and the field rules are the part that reads one value and knows nothing of the
document's version, its keys or its gate.
"""

from __future__ import annotations

from pathlib import PurePosixPath

from studyforge.address import AddressError, require_slug
from studyforge.corpus.manifest.errors import ManifestError
from studyforge.describe import describe

#: Where onboarding writes the document a reader opens first, when the corpus
#: says nothing. ⭐ A **stated** default, so every corpus onboarded before
#: the key existed reads exactly as it did.
ONBOARDING_DOC = "ONBOARDING.md"

#: What a place for that document may not contain. ⛔ It is also the glob that
#: classifies it, so a glob character would classify files nobody named.
GLOB_CHARACTERS = frozenset("*?[]\\")


def title_of(value: object, where: str) -> str:
    """Return the corpus's human-readable name. ⚠️ A title, deliberately not a slug."""
    if not isinstance(value, str) or not value.strip():
        raise ManifestError(f"{where} 'title' must be a non-empty str, got {describe(value)}")
    return value


def levels_of(value: object, where: str) -> tuple[str, ...]:
    """`levels`: the container level labels, which fix the depth.

    ⚠️ Labels, not slugs. §4 says `levels` supplies the display labels the
    breadcrumb and index use — "Section › Module › Lesson" — so how they are
    capitalised is the renderer's decision (R13) and not this module's to
    constrain.
    """
    entries = _non_empty_list(value, "levels", where)
    for position, entry in enumerate(entries, start=1):
        if not isinstance(entry, str) or not entry.strip():
            raise ManifestError(
                f"{where} 'levels[{position - 1}]' must be a non-empty str, got {describe(entry)}"
            )
    return tuple(entries)


def variants_of(value: object, where: str) -> tuple[str, ...]:
    """`variants`: each already a slug, because each names an archive partition."""
    entries = _non_empty_list(value, "variants", where)
    for position, entry in enumerate(entries, start=1):
        slug_of(entry, f"{where} 'variants[{position - 1}]'")
    return tuple(entries)


def slug_of(value: object, what: str) -> str:
    """`studyforge.address`'s slug rule, raised as this package's error.

    ⛔ `errors.ManifestError` promises that reading a manifest raises one
    type. `studyforge.address` owns what a slug **is**, so the rule is imported rather than
    restated — but a caller reading `corpus.json` should not have to know that
    a bad `source` fails through a different package, so the refusal is
    re-raised here with `studyforge.address`'s message intact.

    ⚠️ `Manifest.parse_key` deliberately does **not** do this: that is the
    arity *comparison*, which `studyforge.address` owns outright, and its `AddressError` is
    the honest answer.
    """
    try:
        return require_slug(value, what)
    except AddressError as exc:
        raise ManifestError(str(exc)) from None


def _non_empty_list(value: object, key: str, where: str) -> list:
    """Return a list with something in it, or refuse naming the key."""
    if not isinstance(value, list) or not value:
        raise ManifestError(f"{where} '{key}' must be a non-empty list, got {describe(value)}")
    return value


def exercises_of(value: object, where: str) -> bool:
    """`exercises`: a real bool, refusing anything that merely looks like one.

    ⛔ Not `1`, not `"true"`. A manifest is hand-written, and a string that
    looks like a flag is a mistake worth naming rather than coercing.
    """
    if not isinstance(value, bool):
        raise ManifestError(f"{where} 'exercises' must be true or false, got {describe(value)}")
    return value


def narration_of(value: object, where: str) -> bool:
    """`narration`: whether this corpus is voiced, as a real bool.

    ⭐ The author's answer to the onboarding skill's question, recorded where
    every later stage already reads. ⛔ Not `"off"`, not `0`: the refusal
    `exercises_of` gives a string that looks like a flag, for the same reason.
    """
    if not isinstance(value, bool):
        raise ManifestError(f"{where} 'narration' must be true or false, got {describe(value)}")
    return value


def onboarding_doc_of(value: object, where: str) -> str | None:
    """`onboarding_doc`: where the onboarding reader document goes, or `None` for none.

    ⭐ **A path relative to the corpus root, spelled the one way**: no `.` or
    `..` segment, no leading `/`, no glob character, and ending `.md`, because it
    is Markdown and because it doubles as the glob that classifies it.
    ⛔ `false` means onboarding writes no reader document; `true` says nothing
    and is refused. ⛔ **The value is never quoted** (R7): a path that is not
    a corpus path is usually somebody's home directory.
    """
    if value is False:
        return None
    rule = (
        f"{where} 'onboarding_doc' must be false, or a path inside the corpus ending .md "
        f"with no '.' or '..' segment and no glob character"
    )
    if not isinstance(value, str) or not value:
        raise ManifestError(f"{rule}, got {describe(value)}")
    path = PurePosixPath(value)
    clean = (
        path.as_posix() == value
        and not path.is_absolute()
        and not any(part in (".", "..") for part in path.parts)
        and not GLOB_CHARACTERS & set(value)
        and path.suffix == ".md"
        and path.stem != ""
    )
    if not clean:
        raise ManifestError(f"{rule}; the value is not reproduced, since it may be a path")
    return value
