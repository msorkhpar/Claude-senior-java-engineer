r"""R3 as a check: after a build, nothing that already existed was harmed.

**What it does.** Compares a repository before a build with the same repository
after it, and reports — by rule and by relative path — every pre-existing file
that was modified, moved or deleted, except where the corpus manifest declares
the change in `permitted_edits` and the change is genuinely additive.

**How you use it.**

    before = snapshot(root, manifest)
    …the build runs…
    report = check_untouched(before, snapshot(root, manifest), manifest, footprint)
    report.exit_code        # 0 untouched, 1 not — the same `Report` `validate` returns

`footprint` is anything with `owns(PurePosixPath) -> bool`; `generate.Footprint`
is the one that exists. Omitted, it owns nothing.

**Depends on.** `corpus.manifest` for the declaration and its closed vocabulary
of forbidden targets, `validate.report`, and the standard library. ⛔ Not on
`generate`: the check takes the footprint as a value, so a build package is
never imported to judge a build.

## ⛔ THE CHECK READS THE DECLARATION, AND KNOWS NO CORPUS (R1)

Every exception comes from `manifest.permitted_edits`. There is no path, file
name, anchor or line spelled here that belongs to any corpus, and
`tests/studyforge/validate/test_nondestructive.py` asserts that three ways.

## ⛔ BY BYTES, NEVER BY PRESENCE

A file is compared by a SHA-256 of its bytes. Presence, size and mtime are all
blind to a same-size rewrite in place, which is the change most likely to pass
unnoticed. A symbolic link is compared by its target and never followed.

## What is permitted, in the order it is decided

1. ⛔ **A change to a forbidden target is a finding however it is declared** —
   the root ignore file, version-control configuration at any depth (the
   manifest parser's own vocabulary), and a file the corpus's `content` policy
   includes or contests. The parser refuses such a DECLARATION; this refuses the
   observed CHANGE, so a hand-built `Manifest` cannot smuggle one through.
   Creating a root ignore file or version-control configuration counts too.
2. ⛔ **A deletion is always a finding**, and is named a move when the same
   bytes appear at a path that did not exist before. No build deletes.
3. ⭐ **A path the footprint owns may change** — R3 distinguishes the build's
   own prior output from the user's material, by path. ⚠️ By
   path only: a hand-edited copy of a generated page reads as prior output.
4. **A declared path may change only additively**: the after-file is the
   before-file with exactly the declared line inserted immediately after a line
   containing the declared anchor. An unchanged declared file is a pass —
   re-running an onboarding changes nothing.
5. Every other changed pre-existing file is a finding.

## ⚠️ What this cannot decide mechanically

*"A file the material's own reader depends on as content"* is decided by the
manifest parser's own predicate, `reads_as_content`: the corpus's
`content` policy, or repository-root documentation by convention. ⚠️ The
convention refuses a DECLARED change; an undeclared one is `modified`, as it
was. A file the reader depends on that neither covers — a picture a lesson
embeds by a relative link, say — is invisible to that test, and no mechanical
reading of a corpus supplies it. It is still protected by rule 5 unless it is
declared; it is not protected from a declaration.

## ⚠️ Pass loud

A before-snapshot holding no file compares nothing, and says so as an
`Unchecked` rather than returning a bare pass.
"""

from __future__ import annotations

import hashlib
import os
from collections.abc import Mapping
from dataclasses import dataclass, field
from pathlib import Path, PurePosixPath
from typing import Protocol

from studyforge.corpus.manifest import Manifest, PermittedEdit
from studyforge.corpus.manifest.edits import (
    BY_CONVENTION,
    BY_POLICY,
    IGNORE_NAMES,
    VCS_DIRECTORIES,
    VCS_NAMES,
    reads_as_content,
)
from studyforge.validate.report import Finding, Report, Unchecked

RULE_DELETED = "deleted"
RULE_MOVED = "moved"
RULE_MODIFIED = "modified"
RULE_NOT_ADDITIVE = "not-additive"
RULE_FORBIDDEN = "forbidden-edit"
RULE_NOTHING_COMPARED = "nothing-compared"

#: Every rule this check can emit. ⛔ One tuple, so a rule nobody can produce
#: and a finding with no rule are both test failures.
RULES = (
    RULE_DELETED,
    RULE_MOVED,
    RULE_MODIFIED,
    RULE_NOT_ADDITIVE,
    RULE_FORBIDDEN,
    RULE_NOTHING_COMPARED,
)


class Owner(Protocol):
    """What a footprint answers — whether a path is the build's own output."""

    def owns(self, at: PurePosixPath) -> bool:
        """Whether a build writing `at` would be replacing its own prior output."""
        ...


class _Nothing:
    """The default footprint: owns nothing, so every change is judged."""

    def owns(self, at: PurePosixPath) -> bool:
        return False


@dataclass(frozen=True, slots=True)
class Snapshot:
    """Every file under one root, by relative POSIX path, as a digest of its bytes.

    `kept` holds the full bytes of the paths the manifest declares an edit to,
    because additivity is a question about lines and a digest has none.
    """

    digests: Mapping[str, str]
    kept: Mapping[str, bytes] = field(default_factory=dict)

    def __len__(self) -> int:
        """How many files this snapshot holds — the population a check compared."""
        return len(self.digests)


def snapshot(root: Path | str, manifest: Manifest | None = None) -> Snapshot:
    """Read every file under `root`. ⛔ Symlinks are recorded, never followed."""
    root = Path(root)
    declared = {edit.path for edit in manifest.permitted_edits} if manifest else set()
    digests: dict[str, str] = {}
    kept: dict[str, bytes] = {}
    for directory, subdirectories, files in os.walk(root, followlinks=False):
        here = Path(directory)
        linked = [name for name in subdirectories if (here / name).is_symlink()]
        subdirectories[:] = sorted(set(subdirectories) - set(linked))
        for name in sorted([*files, *linked]):
            path = here / name
            relative = path.relative_to(root).as_posix()
            digests[relative] = _digest(path)
            if relative in declared and not path.is_symlink() and path.is_file():
                kept[relative] = path.read_bytes()
    return Snapshot(digests=digests, kept=kept)


def _digest(path: Path) -> str:
    try:
        if path.is_symlink():
            target = os.readlink(path).encode("utf-8", "surrogateescape")
            return "symlink:" + hashlib.sha256(target).hexdigest()
        digest = hashlib.sha256()
        with path.open("rb") as stream:
            for chunk in iter(lambda: stream.read(1 << 20), b""):
                digest.update(chunk)
        return "sha256:" + digest.hexdigest()
    except OSError as exc:
        # ⛔ `strerror`, never `exc`: an OSError formats itself with the absolute
        # path it was given (R7). An unreadable file still has a value to compare.
        return f"unreadable:{exc.strerror or exc.__class__.__name__}"


def check_untouched(
    before: Snapshot,
    after: Snapshot,
    manifest: Manifest,
    footprint: Owner | None = None,
) -> Report:
    """Every harm a build did to what already existed, drained into one report."""
    owner = _Nothing() if footprint is None else footprint
    edits = {edit.path: edit for edit in manifest.permitted_edits}
    items: list[Finding | Unchecked] = []
    if not before.digests:
        items.append(
            Unchecked(
                RULE_NOTHING_COMPARED,
                ".",
                "the before-snapshot holds no file, so no pre-existing file was compared",
            )
        )
    created = {path: digest for path, digest in after.digests.items() if path not in before.digests}
    for path, digest in before.digests.items():
        items.extend(_judge(path, digest, before, after, manifest, owner, edits, created))
    for path in created:
        if _forbidden_to_create(path):
            items.append(
                Finding(RULE_FORBIDDEN, path, f"was created, and {_forbidden_to_create(path)}")
            )
    return Report.of(items)


def _judge(
    path: str,
    digest: str,
    before: Snapshot,
    after: Snapshot,
    manifest: Manifest,
    owner: Owner,
    edits: Mapping[str, PermittedEdit],
    created: Mapping[str, str],
) -> list[Finding]:
    now = after.digests.get(path)
    if now == digest:
        return []
    forbidden = _forbidden(path, manifest, declared=path in edits)
    if now is not None and forbidden:
        return [Finding(RULE_FORBIDDEN, path, f"was changed, and {forbidden} — however declared")]
    if now is None:
        moved_to = sorted(new for new, value in created.items() if value == digest)
        if moved_to:
            return [
                Finding(RULE_MOVED, path, f"existed before the build and moved to {moved_to[0]}")
            ]
        return [Finding(RULE_DELETED, path, "existed before the build and is gone")]
    if owner.owns(PurePosixPath(path)):
        return []
    if path in edits:
        return _additive(path, edits[path], before, after)
    return [
        Finding(
            RULE_MODIFIED,
            path,
            "existed before the build and its bytes changed; no permitted_edits entry declares it",
        )
    ]


def _forbidden(path: str, manifest: Manifest, declared: bool = True) -> str:
    """Why R3 forbids changing `path` whatever is declared, or `""`.

    ⛔ Content is the parser's own predicate, never a second copy of it.
    """
    reason = _forbidden_to_create(path)
    if reason:
        return reason
    content = reads_as_content(path, manifest.content)
    if content == BY_POLICY:
        return "the corpus's own content policy includes it, so the reader depends on it as content"
    if content == BY_CONVENTION and declared:
        return (
            "it is repository-root documentation, which the repository's own readers read "
            "as content whatever the site's content policy classifies it as"
        )
    return ""


def _forbidden_to_create(path: str) -> str:
    """Name the category forbidden to create as well as to change, or return `""`."""
    parts = PurePosixPath(path).parts
    if len(parts) == 1 and parts[0] in IGNORE_NAMES:
        return "it is the repository's root ignore file"
    if parts[-1] in VCS_NAMES or VCS_DIRECTORIES & set(parts):
        return "it is version-control configuration"
    return ""


def _additive(path: str, edit: PermittedEdit, before: Snapshot, after: Snapshot) -> list[Finding]:
    """Whether a declared file changed by exactly its declared insertion and nothing else."""
    old, new = before.kept.get(path), after.kept.get(path)
    if old is None or new is None:
        return [
            Finding(
                RULE_NOT_ADDITIVE,
                path,
                "is declared and changed, but a snapshot kept no bytes for it, so additivity "
                "could not be shown — take both snapshots with the manifest",
            )
        ]
    old_lines = old.splitlines(keepends=True)
    new_lines = new.splitlines(keepends=True)
    added = len(new_lines) - len(old_lines)
    if added != 1:
        return [
            Finding(
                RULE_NOT_ADDITIVE,
                path,
                f"is declared as one inserted line but its line count changed by {added}",
            )
        ]
    content = edit.content.encode("utf-8")
    anchor = edit.anchor.encode("utf-8")
    for at, line in enumerate(new_lines):
        if line.rstrip(b"\r\n") != content or at == 0 or anchor not in new_lines[at - 1]:
            continue
        if new_lines[:at] + new_lines[at + 1 :] == old_lines:
            return []
    return [
        Finding(
            RULE_NOT_ADDITIVE,
            path,
            "is declared, but the change is not the declared line inserted after its anchor "
            "with every existing line kept byte for byte — an existing line was rewritten, "
            "moved or removed, or the insertion is not the declared one",
        )
    ]
