"""Reading `corpus.json`'s `content` object, and every way it is refused.

**What it does.** Turns the decoded `content` object into a `ContentPolicy`,
refusing anything that would make the declaration unauditable: a corpus that
includes nothing, an exclusion with no reason, an exclusion spelled as a glob,
a `not_material` entry whose wildcard has no directory above it, a path that
escapes the source root.

**How you use it.** `parse_content(document["content"])`, and every refusal is
a `ManifestError`.

**Depends on.** `policy` for the objects it builds, `errors` for the exception
**and** for `_escape` — the one phrase R7 lets a refusal say about a path
— and `studyforge.describe` for the shape it reports back.
⛔ **Nothing here is
imported by `policy`**, which is the package's seam.

## ⭐ Why the reasons are mandatory, and only on one side

⭐ **The asymmetry is the design.** An inclusion needs no justification; an
**exclusion is material withheld from the reader**, and a withholding nobody
has to explain is one nobody audits — the same argument `permitted_edits`
already makes about an edit. So `include` is plain globs and every `exclude`
entry carries its `why`, long enough to be a reason.

⭐ **An exclusion names one path, never a glob**, and that follows from the
`why`. One justification covering a pattern is one justification for a set
whose membership changes when somebody adds a file — which is the audit
quietly widening itself. Each withheld file is named and explained on its own.

## ⛔ Rule 1a — an entry is an exact path, or one directory's wildcard

⛔ **A wildcard whose fixed prefix is not a directory is refused**, however
exactly it matches today. ⚠️ **`CONTESTED` catches a loose glob only when the
swept file is *also* in `include`**, so it is not this rule: the hole is the
file that does not exist yet, classified under a `why` that was never about it
— ⛔ **and `UNCLASSIFIED`, the catch that would have surfaced it, goes quiet
because the file is now classified.** ⭐ A directory-scoped glob can only
silence that *inside* a directory already declared not-material, which is the
declaration doing its job.

⚠️ **The rule refuses the clever pattern that covers today's tree exactly**: a
bracket class matching three root files because a fourth happens to begin with
another letter encodes the collision rather than the intent, and a `why` must
be true of everything its glob matches, including what nobody has written yet.

## ⭐ Rule 1b — one fixed name in each directory at the root

⚠️ **A repository of many uniform modules** keeps the same scaffolding in every
one of them: a build file, a source tree. Rule 1a alone makes that one
declaration per module, each with its own copy of one reason, and a module
added later is unclassified until somebody copies the reason again.

⭐ **So a leading `*/` is admitted where a fixed name follows it**: `*/pom.xml`
is every root directory's `pom.xml`, and `*/src/**` everything under every root
directory's `src/`. What stands after the `*/` is judged by rule 1a, so
`*/*.md` and `*/**` are still refused. ⭐ The reason stays true of a file nobody
has written yet, because what it names is the fixed name, and a directory added
later has that name for the same reason. ⛔ Material swept by one is `CONTESTED`
against `include`, never silently lost. The form is `corpus_api` 8's
(`EACH_DIRECTORY_API`), because a build that predates it refuses it.
"""

from __future__ import annotations

from pathlib import PurePosixPath

from studyforge.corpus.manifest.content.policy import ContentPolicy, Exclusion, NotMaterial
from studyforge.corpus.manifest.errors import ManifestError, _escape
from studyforge.describe import describe, describe_keys

#: Minimum characters of reason on an exclusion, and on a `not_material`
#: entry. ⛔ Not a quality bar — it only stops `"why": "n/a"` from being a way
#: through the gate, which is the same argument and the same number
#: the size floor already asks of a size exception's reason. Two rules, one
#: precedent.
MIN_WHY_CHARS = 20

#: The characters that make a pattern a glob rather than a path. ⚠️ Stated
#: once because two checks ask the same question of a string: an exclusion
#: refuses all of them, and rule 1a asks where the first one falls.
WILDCARDS = ("*", "?", "[")

#: Rule 1b's leading segment: one directory at the root, whatever its name.
_EACH_DIRECTORY = "*/"

#: The `corpus_api` that admits rule 1b. ⚠️ A form, not a key: the manifest's
#: version gate asks `each_directory` of every `not_material` glob.
EACH_DIRECTORY_API = 8


def parse_content(value: object) -> ContentPolicy:
    """Build a `ContentPolicy` from the manifest's `content` object."""
    if not isinstance(value, dict):
        raise ManifestError(f"'content' must be an object, got {describe(value)}")
    unknown = sorted(set(value) - {"include", "exclude", "not_material"})
    if unknown:
        raise ManifestError(
            f"'content' has unknown key(s), {describe_keys(unknown)}; "
            f"expected include, exclude, not_material"
        )
    return ContentPolicy(_include_of(value), _exclude_of(value), _not_material_of(value))


def _include_of(value: dict) -> tuple[str, ...]:
    """Return the `include` globs, which must exist and must include something."""
    include = value.get("include")
    if not isinstance(include, list) or not include:
        raise ManifestError(
            f"'content.include' must be a non-empty list of globs, got {describe(include)} "
            f"(a corpus that includes nothing has no material)"
        )
    for position, pattern in enumerate(include, start=1):
        if not isinstance(pattern, str) or not pattern:
            raise ManifestError(
                f"'content.include[{position - 1}]' must be a non-empty str, "
                f"got {describe(pattern)}"
            )
        _reject_absolute(pattern, f"content.include[{position - 1}]")
    return tuple(include)


def _exclude_of(value: dict) -> tuple[Exclusion, ...]:
    """Return the `exclude` entries — may be empty, each present one with its reason."""
    exclude = value.get("exclude", [])
    if not isinstance(exclude, list):
        raise ManifestError(f"'content.exclude' must be a list, got {describe(exclude)}")
    entries = []
    seen: dict[str, int] = {}
    for index, entry in enumerate(exclude):
        where = f"content.exclude[{index}]"
        if not isinstance(entry, dict):
            raise ManifestError(f"{where} must be an object, got {describe(entry)}")
        unknown = sorted(set(entry) - {"path", "why"})
        if unknown:
            raise ManifestError(
                f"{where} has unknown key(s), {describe_keys(unknown)}; expected path, why"
            )
        path = entry.get("path")
        if not isinstance(path, str) or not path:
            raise ManifestError(f"{where}.path must be a non-empty str, got {describe(path)}")
        _reject_absolute(path, f"{where}.path")
        if any(wildcard in path for wildcard in WILDCARDS):
            raise ManifestError(
                f"{where}.path must name one file and this one is a glob — "
                f"one reason cannot explain a set whose membership changes"
            )
        if path in seen:
            # ⛔ Both positions, never the path. The reader has the file
            # in front of them; what they cannot see is which other entry
            # collides, and two reasons for one exclusion is the actual defect.
            raise ManifestError(
                f"{where}.path is already excluded by content.exclude[{seen[path]}]; "
                f"two reasons for one exclusion is two audits and no record of which held"
            )
        seen[path] = index
        why = _why_of(
            entry.get("why"),
            where,
            says="why this file is withheld from the reader",
            noun="an exclusion",
        )
        entries.append(Exclusion(path, why))
    return tuple(entries)


def _not_material_of(value: dict) -> tuple[NotMaterial, ...]:
    """Return the `not_material` globs — the third state, optional and defaulted.

    ⛔ **Absent means an empty tuple**, which is why a manifest written before
    this key existed still parses unchanged. ⚠️ The version bump is not about
    those manifests — it is about a manifest that *uses* the key being
    unreadable to an older build, which is what R9 versions.
    """
    declared = value.get("not_material", [])
    if not isinstance(declared, list):
        raise ManifestError(f"'content.not_material' must be a list, got {describe(declared)}")
    entries = []
    seen: dict[str, int] = {}
    for index, entry in enumerate(declared):
        where = f"content.not_material[{index}]"
        if not isinstance(entry, dict):
            raise ManifestError(f"{where} must be an object, got {describe(entry)}")
        unknown = sorted(set(entry) - {"glob", "why"})
        if unknown:
            raise ManifestError(
                f"{where} has unknown key(s), {describe_keys(unknown)}; expected glob, why"
            )
        glob = entry.get("glob")
        if not isinstance(glob, str) or not glob:
            raise ManifestError(f"{where}.glob must be a non-empty str, got {describe(glob)}")
        _reject_absolute(glob, f"{where}.glob")
        _reject_loose_glob(glob, f"{where}.glob")
        if glob in seen:
            # ⛔ Both positions, never the pattern — the exclusion twin's rule,
            # for the twin's reason.
            raise ManifestError(
                f"{where}.glob is already declared by content.not_material[{seen[glob]}]; "
                f"two reasons for one declaration is two audits and no record of which held"
            )
        seen[glob] = index
        why = _why_of(
            entry.get("why"),
            where,
            says="why this file was never material",
            noun="a declaration that the framework will not read a file",
        )
        entries.append(NotMaterial(glob, why))
    return tuple(entries)


def _reject_loose_glob(glob: str, where: str) -> None:
    """Rule 1a: an entry is an exact path, or one directory's wildcard.

    ⛔ **One sentence, mechanical rather than a matter of taste — reject a
    wildcard whose fixed prefix is not a directory.** A pattern whose
    correctness depends on which files happen *not* to exist is refused
    however exactly it matches the tree in front of its author today.
    ⚠️ The pattern is not quoted back: the actionable half is which rule was
    broken, and the author has what they wrote in front of them.
    """
    judged = glob[len(_EACH_DIRECTORY) :] if each_directory(glob) else glob
    cut = min((judged.find(wildcard) for wildcard in WILDCARDS if wildcard in judged), default=-1)
    if cut < 0:
        return
    if not judged[:cut].endswith("/"):
        raise ManifestError(
            f"{where} puts a wildcard where no directory precedes it. A not_material "
            f"entry is an exact path, a wildcard under a directory that is itself "
            f"entirely not material, or either of those behind a leading '*/' naming "
            f"the same fixed name in every root directory (corpus_api "
            f"{EACH_DIRECTORY_API}); a pattern whose correctness depends on which files "
            f"happen not to exist silences the unclassified check for a file nobody has "
            f"considered yet"
        )


def each_directory(glob: str) -> bool:
    """Whether `glob` is rule 1b's form: a leading `*/`, then a fixed name.

    ⛔ The segment after the `*/` carries no wildcard, so `*/*.md` is not this
    form and is judged, and refused, by rule 1a as a whole.
    """
    if not glob.startswith(_EACH_DIRECTORY):
        return False
    head = glob[len(_EACH_DIRECTORY) :].split("/", 1)[0]
    return bool(head) and not any(wildcard in head for wildcard in WILDCARDS)


def _why_of(why: object, where: str, *, says: str, noun: str) -> str:
    """Return the reason a declaration gives, refused if it is not one.

    ⭐ `says` and `noun` are the caller's because the two declarations are
    different sentences: an exclusion withholds material from a reader, and a
    `not_material` entry says there was never any to withhold. ⛔ Writing
    *"withheld"* into the second is the small lie the third state removes.
    """
    if not isinstance(why, str) or not why.strip():
        raise ManifestError(f"{where}.why must say {says}, got {describe(why)}")
    if len(why.strip()) < MIN_WHY_CHARS:
        raise ManifestError(
            f"{where}.why must be at least {MIN_WHY_CHARS} characters of reason, "
            f"got {len(why)} — {noun} nobody has to explain is one nobody audits"
        )
    return why


def _reject_absolute(pattern: str, where: str) -> None:
    """Refuse a path that escapes the source root — and never quote it (R7).

    ⛔ **This branch fires *because* the value is an absolute or escaping path,
    which is precisely when it carries a home directory.** Quoting the value
    would put the path the check keeps out of the corpus into the log instead.
    ⚠️ **Worse than a slug refusal's echo** — `require_slug` fires on "not a
    slug", which is only *sometimes* a path; this one tests `startswith("/")`.

    ⭐ The fault is named instead, and it is the actionable half: a reader who
    wrote `/opt/material/x` knows what they wrote and needs to be told which
    rule it broke.
    """
    if pattern.startswith("/") or pattern.startswith("~") or ".." in PurePosixPath(pattern).parts:
        raise ManifestError(
            f"{where} must be relative to the source root and stay inside it; "
            f"it {_escape(pattern)}, and it is not reproduced here because that "
            f"shape is where a home directory lives"
        )
