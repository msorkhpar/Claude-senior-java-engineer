"""R3's declaration: the existing files a corpus may be added to, and how to undo it.

**What it does.** Models `corpus.json`'s `permitted_edits` — the complete,
enumerated set of existing files this corpus may have added to, each with its
insertion and its reason — and refuses the three targets no declaration can
make legal.

**How you use it.** `parse_edits(value, content)` while parsing a manifest;
`edit.reversal` to describe the undo. ⛔ **`validate.nondestructive` reads the declaration**; it
never hardcodes a corpus's exception. The first consuming corpus's additive
`<module>practice</module>` line in its root build file is one entry in this
list, not a special case in the framework.

**Depends on.** `content` (for the third prohibition — see below) and
`errors`. No filesystem: whether the declared edit *happened* is `validate.nondestructive`'s
question, not this module's.

⭐ **An empty list is the normal case**, and it is the one a purely additive
source should be able to keep. Two of the four designed shapes have nothing to
declare.

## The three edits no declaration can make legal

R3 names them, and this module refuses each **however declared**:

1. **The repository's root ignore file.** Write new ignore files *inside*
   generated directories instead — a generated directory that carries its own
   ignore file needs nothing from the root one, and it works.
2. **Any version-control configuration.** `.git/`, `.gitattributes`,
   `.gitmodules`.
3. ⭐ **Any file the material's own reader depends on as content.** It is
   checkable because `content` exists: **a file that the corpus's own
   `content` policy classifies as INCLUDED is content**, and editing it is
   refused. ⚠️ One designed shape is the live case: its
   `permitted_edits` is `[]` and its `README.md` is material, so the check
   holds it there structurally rather than by anyone remembering.

   ⛔ **Content is a property of the file in its repository**, not of
   the site's policy. **Repository-root documentation** — a root `README`,
   `LICENSE`, `LICENCE` or `COPYING`, of any suffix — is content its readers
   read whatever `content` classifies it as, so a corpus that declares its root
   `README.md` `not_material` still may not declare an edit to it.
   `reads_as_content` is the ONE predicate: this module refuses a declaration by
   it, and `validate.nondestructive` refuses a declared change by it.

## The reverse of every declared edit is recorded

⭐ An onboarding that cannot be undone is one nobody will run against a
repository they care about. ⚠️ **There is no `reverse` field, and that is
deliberate:** an `insert-line` declaration already records the insertion
exactly — the anchor it goes after and the line it is — so the reverse is that
line, removed. A second field would be a second thing to keep in step with the
first, and the failure mode of a stale undo is worse than of no undo. Declaring
the reversal rather than deriving it would be a schema change.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import PurePosixPath

from studyforge.corpus.manifest.content import (
    MIN_WHY_CHARS,
    Classification,
    ContentPolicy,
)
from studyforge.corpus.manifest.errors import ManifestError, _escape
from studyforge.describe import describe, describe_keys

#: The kinds of edit that can be declared. ⚠️ One, because one is what any
#: source in scope needs, and an unknown kind is refused rather than guessed
#: at. Widening this is a decision with a source behind it.
EDIT_KINDS = ("insert-line",)

#: Version-control configuration, at any depth. ⛔ Never editable, however
#: declared: a tool that can rewrite these can rewrite the record of what it
#: did.
VCS_NAMES = frozenset({".gitattributes", ".gitmodules", ".mailmap"})
VCS_DIRECTORIES = frozenset({".git", ".hg", ".svn"})

#: Ignore files. ⛔ The **root** one is never editable; one inside a generated
#: directory is how a corpus ignores generated output instead.
IGNORE_NAMES = frozenset({".gitignore", ".hgignore"})

#: ⛔ Repository-root documentation, by exact stem and in any case. A file
#: the repository's own readers read as its content, whatever the site calls it.
ROOT_DOCUMENTATION = frozenset({"readme", "license", "licence", "copying"})

#: Why `reads_as_content` reads a path as content: the corpus's policy, or the convention.
BY_POLICY = "policy"
BY_CONVENTION = "convention"


@dataclass(frozen=True, slots=True)
class Reversal:
    """How to undo one declared edit. Derived from the edit, never declared."""

    path: str
    kind: str
    content: str

    def __str__(self) -> str:
        """Return a one-line description a person can read in a plan."""
        return f"{self.kind} {describe(self.content)} from {self.path}"


@dataclass(frozen=True, slots=True)
class PermittedEdit:
    """One existing file this corpus may add to, the addition, and why."""

    path: str
    kind: str
    anchor: str
    content: str
    why: str

    @property
    def reversal(self) -> Reversal:
        """How to undo this edit.

        ⛔ Additive by construction: the only kind is an inserted line, so the
        only reversal is that line removed. A declared edit that proves not to
        be additive is `validate.nondestructive`'s failure to detect, not this module's to
        express.
        """
        return Reversal(self.path, "remove-line", self.content)


def reads_as_content(path: str, content: ContentPolicy) -> str:
    """Return why R3 reads `path` as content, `BY_POLICY` or `BY_CONVENTION`, or `""`.

    ⛔ The ONE predicate for R3's third category: the parser refuses a
    declaration by it, and `validate.nondestructive` refuses a declared change by it.
    """
    if content.classify(path) in (Classification.INCLUDED, Classification.CONTESTED):
        return BY_POLICY
    parts = PurePosixPath(path).parts
    if len(parts) == 1 and parts[0].split(".", 1)[0].lower() in ROOT_DOCUMENTATION:
        return BY_CONVENTION
    return ""


def parse_edits(value: object, content: ContentPolicy) -> tuple[PermittedEdit, ...]:
    """Build the declared edits, refusing the three R3 never permits.

    `content` is the corpus's own content policy, and it is required rather
    than optional: without it the third prohibition cannot be checked, and a
    prohibition that is only checked when convenient is not one.
    """
    if value is None:
        return ()
    if not isinstance(value, list):
        raise ManifestError(f"'permitted_edits' must be a list, got {describe(value)}")
    edits = []
    seen: set[str] = set()
    for index, entry in enumerate(value):
        edit = _edit_of(entry, f"permitted_edits[{index}]")
        if edit.path in seen:
            raise ManifestError(
                "permitted_edits declares the same path twice; a path declared twice "
                "is two reasons for one edit and no way to tell which was audited"
            )
        seen.add(edit.path)
        _reject_forbidden_target(edit, content)
        edits.append(edit)
    return tuple(edits)


def _edit_of(entry: object, where: str) -> PermittedEdit:
    """One entry, with every field present and of the right shape."""
    if not isinstance(entry, dict):
        raise ManifestError(f"{where} must be an object, got {describe(entry)}")
    expected = ("path", "kind", "anchor", "content", "why")
    unknown = sorted(set(entry) - set(expected))
    if unknown:
        raise ManifestError(
            f"{where} has unknown key(s), {describe_keys(unknown)}; expected {list(expected)}"
        )
    for field in expected:
        candidate = entry.get(field)
        if not isinstance(candidate, str) or not candidate.strip():
            raise ManifestError(
                f"{where}.{field} must be a non-empty str, got {describe(candidate)}"
            )
    if entry["kind"] not in EDIT_KINDS:
        raise ManifestError(
            f"{where}.kind must be one of {list(EDIT_KINDS)}, got {describe(entry['kind'])}"
        )
    if len(entry["why"].strip()) < MIN_WHY_CHARS:
        raise ManifestError(
            f"{where}.why must be at least {MIN_WHY_CHARS} characters of reason, "
            f"got {len(entry['why'])} — an edit nobody has to explain is one nobody audits"
        )
    return PermittedEdit(**{field: entry[field] for field in expected})


def _reject_forbidden_target(edit: PermittedEdit, content: ContentPolicy) -> None:
    """Refuse the three targets R3 never permits, however they are declared."""
    path = PurePosixPath(edit.path)
    if edit.path.startswith("/") or ".." in path.parts:
        # ⛔ The twin of `content._reject_absolute`, and the same rule: this
        # branch fires *because* the value is an absolute or escaping path, so
        # quoting it is the leak the check exists to prevent.
        raise ManifestError(
            f"permitted_edits path must be relative to the source root and stay "
            f"inside it; it {_escape(edit.path)}, and it is not reproduced here "
            f"because that shape is where a home directory lives"
        )
    if len(path.parts) == 1 and path.name in IGNORE_NAMES:
        # ⭐ Safe to name, and this is the distinction that rule turns on: by here the
        # value is one path component **and a member of `IGNORE_NAMES`** — this
        # framework's own closed vocabulary, not the caller's text.
        raise ManifestError(
            f"permitted_edits may never name the repository's root ignore file, got "
            f"{path.name!r} — write a new ignore file inside a generated directory instead"
        )
    if path.name in VCS_NAMES or VCS_DIRECTORIES & set(path.parts):
        raise ManifestError(
            f"permitted_edits may never name version-control configuration; this one "
            f"names {sorted(VCS_NAMES | VCS_DIRECTORIES & set(path.parts))}"
        )
    # ⛔ `CONTESTED` counts, and it is not a courtesy. It means `include` and
    # `not_material` both match this path and the manifest has not said which
    # holds; refusing the edit is the direction that cannot destroy material,
    # and reading it as "not included" would be this check going quiet on
    # exactly the file nobody has decided about.
    reason = reads_as_content(edit.path, content)
    if reason == BY_POLICY:
        raise ManifestError(
            "permitted_edits may never name a file the material's own reader depends on "
            "as content, and this corpus's 'content' includes it"
        )
    if reason == BY_CONVENTION:
        # ⭐ Safe to name: one path component whose stem is in this framework's
        # own closed vocabulary, `ROOT_DOCUMENTATION`.
        raise ManifestError(
            f"permitted_edits may never name {path.name!r}: it is repository-root "
            f"documentation, which the repository's own readers read as content whatever "
            f"'content' classifies it as for the site"
        )
