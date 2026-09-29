r"""What the corpus says each of its files is — material, output, or nothing.

**What it does.** Refuses a file the manifest classifies as neither included
nor excluded, one it classifies as both, and one it INCLUDES that no unit's
origin names.

**How you use it.** `check_unclassified(walk)`, yielding `Finding`s and
`Unchecked`s like every other check. What it judges is `enumeration`'s
`source_files(root)`, re-exported here with the rest of that module's public
names, so an importer that named them from this module still reads them.

**Depends on.** `corpus.manifest` for the classification, `enumeration` for what
the corpus root holds, `validate.corpus` and `validate.report`. ⛔ **Nothing in
this package's other half** — the two checks share no name.
"""

from __future__ import annotations

from collections.abc import Iterator
from pathlib import Path

from studyforge.corpus.manifest import Classification
from studyforge.validate.corpus import Walk
from studyforge.validate.report import Finding, Unchecked
from studyforge.validate.source.enumeration import (
    IGNORE_TIMEOUT,
    REPOSITORY_STORE,
    SKIP_DIRS,
    Scan,
    repository_ignores,
    source_files,
)

#: ⛔ This module's names, and `enumeration`'s that importers named from here before
#: the split. The package surface, not this list, is what a consumer reads.
__all__ = [
    "IGNORE_TIMEOUT",
    "REPOSITORY_STORE",
    "RULE_CONTESTED",
    "RULE_IGNORE_DECLARATION",
    "RULE_INCLUDED_UNREAD",
    "RULE_NESTED_REPOSITORY",
    "RULE_UNCLASSIFIED",
    "SKIP_DIRS",
    "Scan",
    "check_unclassified",
    "repository_ignores",
    "source_files",
]

RULE_UNCLASSIFIED = "unclassified"

#: ⛔ A file matched by **both** `include` and `content.not_material`, under
#: its own rule id rather than `unclassified`'s. ⭐ **Never a precedence**: the
#: manifest classified this file twice and disagreed with itself, which is a
#: different fact from never having classified it, and refusing it is what
#: stops the third state becoming somewhere to sweep material into.
RULE_CONTESTED = "contested"

#: ⛔ Its own rule id rather than `unclassified`'s. The classification check did
#: run; what could not be read is the repository's declaration of what is
#: output — a different fact, and one a script filters on separately.
RULE_IGNORE_DECLARATION = "ignore-declaration"

#: ⛔ A repository's store nested inside the corpus — a vendored repository or
#: a submodule checkout — refused by name, never skipped and never scanned.
#: Its own rule id, because the fix is not a `content` pattern.
RULE_NESTED_REPOSITORY = "nested-repository"

#: ⛔ A file the manifest INCLUDES that no UNIT's `origin` names, so no unit reads it —
#: an aggregate un-excluded, or a stray. ⚠️ A container's `origin` PLACES its page and reads
#: nothing. Judged over this scan only, while a unit origin is on disk (`origin-missing`).
RULE_INCLUDED_UNREAD = "included-unread"


def check_unclassified(walk: Walk) -> Iterator[Finding | Unchecked]:
    """Refuse a source file the manifest classifies as neither in nor out.

    ⛔ **Silence is the double ingest.** A file matching none of `content`'s
    three states is *unclassified*, and an unclassified file is refused rather
    than guessed at — one corpus ships per-unit files beside whole-series
    aggregates that are digest-identical concatenations of them, so a glob
    that swept both would read every unit twice and nothing would complain.

    ⚠️ **A file matched by both `include` and `not_material` is refused under
    its own rule id** — that is two glob authors contradicting each other, and
    picking a winner would decide by precedence what nobody declared.

    ⭐ `ContentPolicy.classify` does no I/O by design. Enumerating the root is
    this function's half of that split.
    """
    if walk.manifest is None:  # pragma: no cover - the walk stops without one
        return
    scan = source_files(walk.root)
    for path in scan.generated:
        where = walk.relative(path)
        if walk.manifest.content.classify(where) in (
            Classification.INCLUDED,
            Classification.CONTESTED,
        ):
            yield Finding(
                RULE_CONTESTED,
                where,
                "a build writes this path, and the manifest includes it as material. "
                "Neither is guessed: one would read generated output as the material, the "
                "other would let a build overwrite the material. Narrow the 'include' "
                "pattern, or move the material.",
            )
    for store in scan.stores:
        yield Finding(
            RULE_NESTED_REPOSITORY,
            walk.relative(store),
            "is another repository's store inside this corpus — a vendored repository or "
            "a submodule checkout. Its history is not material and is not read, and this "
            "repository's ignore rules do not answer for another repository's files. It "
            "is refused, not skipped: move that repository out of the corpus root, or "
            "declare its directory as output in this repository's ignore rules.",
        )
    if not scan.files:
        yield Unchecked(
            RULE_UNCLASSIFIED,
            ".",
            "no source material is present beside the archive, so there is nothing to classify",
        )
        return
    if not scan.planned:
        yield Unchecked(
            RULE_UNCLASSIFIED,
            ".",
            "`studyforge plan` refused this corpus, so what a build writes here could not be "
            "told from material. This corpus's own generated pages and media, if any, are "
            "reported below as unclassified rather than recognised. Run the plan to see why.",
        )
    if not scan.consulted:
        # ⛔ The walk stands and the report says so. A half-applied ignore rule
        # is the half-present source tree this package's docstring refuses.
        yield Unchecked(
            RULE_IGNORE_DECLARATION,
            ".",
            "the repository's own declaration of what is generated output could not "
            "be read — this corpus root is not a git working tree, or git is not "
            "installed. Every file beside the archive was scanned as material, so "
            "generated output is reported below as unclassified rather than skipped.",
        )
    read = _read_by_origins(walk)
    for path in scan.files:
        where = walk.relative(path)
        classification = walk.manifest.content.classify(where)
        if classification is Classification.UNCLASSIFIED:
            yield Finding(
                RULE_UNCLASSIFIED,
                where,
                "matches no 'include' pattern, no 'exclude' entry and no "
                "'not_material' glob. A file the manifest does not classify is not "
                "ingested and not refused — it is simply unaccounted for, which is "
                "how a corpus is read twice or not at all.",
            )
        elif classification is Classification.CONTESTED:
            yield Finding(
                RULE_CONTESTED,
                where,
                "matches an 'include' pattern and a 'not_material' glob at once, and "
                "the manifest does not say which holds. Neither answer is guessed: "
                "one would drop material the reader was promised, the other would "
                "read the repository's own scaffolding aloud. Narrow one of the two "
                "patterns, or move this file to 'exclude' with its reason.",
            )
        elif classification is Classification.INCLUDED and read and path not in read:
            yield Finding(
                RULE_INCLUDED_UNREAD,
                where,
                "matches an 'include' pattern and no unit's 'origin' names it, so no unit reads "
                "it: material the corpus carries twice or not at all (C2). Name it as a unit's "
                "origin, or move it to 'exclude' with its reason.",
            )


def _read_by_origins(walk: Walk) -> frozenset[Path]:
    """Every file a unit `origin` names; ⛔ EMPTY when that cannot be judged.

    Empty when no named file is on disk (`origin-missing`'s answer) or a map did not parse.
    """
    maps = [held.container for held in walk.containers]
    # ⭐ **Both origins**: a unit's practice may be read from a file of
    # its own, and that file is read by that unit exactly as its prose file is.
    named = {walk.root / u.origin for c in maps for u in c.units if u.origin}
    named |= {walk.root / u.practice_origin for c in maps for u in c.units if u.practice_origin}
    if any(item.container is None for item in walk.refused):
        return frozenset()
    return frozenset(named) if any(path.is_file() for path in named) else frozenset()
