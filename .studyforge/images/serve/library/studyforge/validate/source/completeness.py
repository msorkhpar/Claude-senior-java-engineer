r"""How many headings one file carries, counted without the parser that read it.

**What it does.** Counts a structural feature directly in the raw source and
compares it against what the archive records — the check the whole `validate`
command exists to make honest.

**How you use it.** `check_completeness(walk)`, yielding `Finding`s and
`Unchecked`s like every other check.

**Depends on.** `validate.headings` for what a heading is and where a region
ends, `corpus.manifest` for what the manifest declares as source,
`corpus.placement` for the archive root, `validate.corpus`, `validate.report`.
⛔ **Not `archive.markdown`**, ever
— see below. ⛔ **And nothing in this package's other half**: the two checks
share no name.

## A completeness check counts something the parser did not produce

⛔ **Two readings from the same parser are not two readings.** A digest
computed from the blocks and compared against the blocks answers *"was this
corrupted after we wrote it?"*. It cannot answer *"did the adapter read
everything the source contained?"* — the two readings come from the same
place, so a construct the parser never recognised is absent from both, the
counts agree, and nothing raises.

⚠️ A guard that compares section containers against pairs derived by the same
regex family misses a section written in a shape neither recognises: it is
absent from both, `5 == 5`, and the lesson loses a section with nothing failing.

⚠️ **An adapter reading Markdown directly has only one reading of a lesson**,
so nothing disagrees for it. And C3
establishes the realistic failure: raw HTML in real Markdown yields a short,
well-formed, entirely plausible unit rather than an error.

⭐ **So this check does not use the Markdown reader.** It counts ATX heading
lines with a regex of its own — `validate.headings`, which imports nothing but
`re` — outside fenced code blocks, and compares the number against the
archive's `headings` count. A check that can only fail when the parser already
failed loudly is not a check.

## ⛔ A unit may be a REGION of a file, and then the count is the region's

⚠️ **Seventeen units sharing one `origin` were seventeen comparisons against
one number** — a real corpus holds 17 regions of a 361-heading file, so sixteen
short-read by construction. ⭐ **So `origin` has a second shape**
(`{path, section}`), bounded by `validate.headings` from the same fence-aware
regex that produces the count. ⛔ **A section occurring twice, or not at all,
is a finding with its own rule id**, never resolved by picking one: picking is
where the silence comes back.

## ⛔ A unit's PRACTICE may come from a file of its OWN, counted against IT

⭐ **A practice may live in a file of its own, which is why the accounting is
per ORIGIN rather than per unit.** A practice belongs on the topic page it practises, so it is a
`practice` document of that unit — but its prose is not in the unit's source
file, and a corpus may not rewrite that file to put it there (R3, and
`corpus.manifest.edits` refuses the edit however it is declared). So the unit
declares `practice_origin`: a **second, additive** file beside the material.

⛔ **The check is not relaxed by this; it is run twice.** A unit with a
`practice_origin` gets **two** comparisons — its `lesson` documents against
`origin`, its `practice` documents against `practice_origin` — and every
heading on both sides is still accounted for by exactly one file. ⚠️ **Both
buckets exist whether or not a document landed in them**, which is the half
that would otherwise be a hole: a `practice_origin` whose unit holds no
practice document compares its headings against **zero** and reads as the
short read it is, rather than as a file nobody counted.

## ⛔ An AUTHORED practice is not in the source, so it is not counted against it

⭐ **An authored practice is left out.** A practice whose exercise is `generated` was emitted from a
**bundle** (spec §7): its blocks are the bundle's statement and the
practice layout's headings, not a reading of any source file. Summing them
into the unit's buckets would report a short read on every unit that received one.
⛔ **So it is left out, and it is decided by what the document IS** — the
`provenance` its own `exercise` record carries, which `validate.corpus` has
already parsed — **never by its ordinal, its path or its bundle's place on
disk.** ⭐ It is the same trigger `validate.exercises` holds to a bundle and a
gate record, so a document this check leaves out is one that arm reads.
⛔ Every other document, a `bundled` practice included, is still counted.

## Where the source is, and what happens when it is not there

⛔ **All or nothing, and half is a failure.** If **some** origins are on disk,
all must be: a half-present source is exactly where a short read hides, and a
per-file skip would discount precisely the file that went missing.

⛔ **"The source tree is absent" is CHECKED, never inferred from the origins.**
When **no** origin is on disk, `_source_beside` reads the
WHOLE corpus root, skipping only the framework's own writing at the root
(`NOT_SOURCE`). A file anywhere under it that the manifest's own `content`
declares as source (included, excluded or contested) means the source IS
present, and every unit is refused `origin-missing`. ⚠️ A one-unit archive has
only one origin, and origins written against the wrong root name a top-level
directory that is absent, so a reading anchored where the origins point read
both valid beside the whole source. ⭐ Only when no such file is there is the
tree absent, and every source-dependent claim is reported `Unchecked`, loudly
and counted (R6's sibling). A file declared `not_material`, or not declared at
all, is not a present source. It asks no git and no plan, so a copy with no
repository reads the same.

⭐ The alternative — failing whenever the source is absent — was rejected
because an archive is a shippable artifact on its own, and `validate` is the
adapter's definition of done for *the archive* (R2). What it may not do is
pretend it checked.
"""

from __future__ import annotations

import os
from collections.abc import Iterator
from dataclasses import dataclass
from pathlib import Path

from studyforge.corpus.container import Unit as Declared
from studyforge.corpus.manifest import Classification
from studyforge.corpus.placement import ARCHIVE_DIRNAME
from studyforge.exercise import ExerciseError
from studyforge.exercise import of as exercise_of
from studyforge.validate.corpus import Walk
from studyforge.validate.exercises import GENERATED
from studyforge.validate.headings import count_headings, region
from studyforge.validate.report import Finding, Unchecked

RULE_SHORT_READ = "short-read"
RULE_ORIGIN_MISSING = "origin-missing"

#: ⛔ Two rule ids, not one. A section the file does not carry is a renamed
#: heading; one it carries twice is a corpus whose regions are ambiguous. ⚠️
#: They are fixed differently, so a script filters on them separately.
RULE_SECTION_MISSING = "origin-section-missing"
RULE_SECTION_AMBIGUOUS = "origin-section-ambiguous"

#: The document kind whose material `practice_origin` names. ⛔ The
#: archive's own word, and this module's only knowledge of what a kind is.
PRACTICE = "practice"

#: ⭐ What the manifest declares as source prose: read in, withheld, or claimed twice.
SOURCE_STATES = frozenset(
    {Classification.INCLUDED, Classification.EXCLUDED, Classification.CONTESTED}
)

#: What the presence reading never enters, at the corpus root: the framework's own writing.
NOT_SOURCE = frozenset({ARCHIVE_DIRNAME, ".git", ".studyforge", "corpus.json"})


@dataclass(frozen=True, slots=True)
class _Origin:
    """One source file, and the archive headings that must come out of it.

    ⭐ **One of these per (unit, role), not per unit**: a unit that
    declares a `practice_origin` yields two, and each is compared against its
    own file.
    """

    where: str
    path: Path
    section: str | None
    recorded: int
    practice: bool

    @property
    def what(self) -> str:
        """`an origin` or `a practice origin` — what a refusal calls this file."""
        return "a practice origin" if self.practice else "an origin"

    @property
    def whose(self) -> str:
        """`its source` or `its practice source` — what a short read calls this file."""
        return "its practice source" if self.practice else "its source"


def check_completeness(walk: Walk) -> Iterator[Finding | Unchecked]:
    """Compare a heading count taken from the raw source against the archive.

    ⛔ The count on the left is produced by this module's own regex and the
    count on the right by the adapter's parser. That is the whole point: two
    readings from one parser agree by construction.
    """
    if walk.manifest is None:  # pragma: no cover - the walk stops without one
        return
    origins = _origins(walk)
    present = [origin for origin in origins if origin.path.exists()]
    if not origins:
        yield Unchecked(
            RULE_SHORT_READ,
            ".",
            "no container map declares an 'origin', so there is no source file to count against",
        )
        return
    if not present and not _source_beside(walk):
        yield Unchecked(
            RULE_SHORT_READ,
            ".",
            f"none of the {len(origins)} declared origin file(s) is present, and no file under "
            f"the corpus root is source the manifest's 'content' declares, so the source tree "
            f"is absent and no unit's completeness was checked",
        )
        return
    if not present:
        # ⛔ Source on disk and every origin missing is not an absent tree.
        for origin in origins:
            yield Finding(
                RULE_ORIGIN_MISSING,
                origin.where,
                f"names {origin.what} that is not on disk, while files the manifest's "
                f"'content' declares as source are beside the archive. No declared origin "
                f"is present, so no unit's completeness was checked, and an origin that "
                f"names no file is where a short read hides.",
            )
        return
    for origin in origins:
        if not origin.path.exists():
            # ⛔ Half a source tree is a failure, not a discount: a per-file
            # skip would excuse precisely the file that went missing.
            yield Finding(
                RULE_ORIGIN_MISSING,
                origin.where,
                f"names {origin.what} that is not on disk, while other origins in this "
                f"corpus are. Either the whole source tree is beside the archive or "
                f"none of it is; half of it is where a short read hides.",
            )
            continue
        yield from _compare(origin)


def _source_beside(walk: Walk) -> bool:
    """Whether any file under the corpus root is source that the manifest's `content` declares.

    ⛔ **Read from the disk, never inferred from the origins**, ⭐ **and over the
    WHOLE root**: an origin that is not on disk says nothing about where the
    source is. Each file is asked of the manifest's own declaration, and never of git or
    of the plan. It stops at the first file declared.
    """
    assert walk.manifest is not None
    content = walk.manifest.content
    for path in _files_under_root(walk):
        if content.classify(path.relative_to(walk.root).as_posix()) in SOURCE_STATES:
            return True
    return False


def _files_under_root(walk: Walk) -> Iterator[Path]:
    """Every file under the corpus root, in sorted order, beside the framework's own at the root."""
    for top in sorted(walk.root.iterdir()):
        if top.name in NOT_SOURCE:
            continue
        if top.is_file():
            yield top
        elif top.is_dir():
            for directory, subdirectories, files in os.walk(top):
                subdirectories.sort()
                yield from (Path(directory) / name for name in sorted(files))


def _compare(origin: _Origin) -> Iterator[Finding]:
    text = _read(origin.path)
    if text is None:
        return
    if origin.section is None:
        in_source = count_headings(text)
    else:
        found = region(text, origin.section)
        if found.occurrences != 1:
            yield _ambiguous(origin.where, found.occurrences)
            return
        in_source = found.headings
    if in_source != origin.recorded:
        yield Finding(
            RULE_SHORT_READ,
            origin.where,
            f"{origin.whose} carries {in_source} heading line(s) and the archive records "
            f"{origin.recorded} heading block(s). The digest cannot see this: it is taken "
            f"over what the parser produced, so a construct the parser skipped is "
            f"missing from both sides of it.",
        )


def _ambiguous(where: str, occurrences: int) -> Finding:
    """Refuse a `section` that does not name exactly one region.

    ⛔ **Never resolved by picking one** — that is the *sixteen silent
    short reads* above. ⚠️ The section is **not** reproduced: it is read out of a
    file somebody else wrote, and a refusal names the field (R7).
    """
    if occurrences == 0:
        return Finding(
            RULE_SECTION_MISSING,
            where,
            "declares an origin region whose section is not a heading of that file. "
            "The section is the exact text of the heading the region opens with, so a "
            "renamed one breaks it here rather than by silently reading the whole "
            "file as this unit.",
        )
    return Finding(
        RULE_SECTION_AMBIGUOUS,
        where,
        f"declares an origin region whose section is a heading of that file "
        f"{occurrences} times. A region must be named exactly once: taking the first "
        f"match gives every other unit sharing this file a region beginning somewhere "
        f"else, and nothing reports it.",
    )


def _origins(walk: Walk) -> list[_Origin]:
    """Return one `_Origin` per **(unit, source file)**, keyed and summed.

    ⚠️ **Summed across the documents that share a file, deliberately.** A unit
    may hold several archive documents — `depth1`'s third unit holds two
    lessons. Comparing one document against the whole file would report a short
    read on every multi-document unit in the corpus, which is the shape of a
    check that gets switched off.

    ⭐ **The sum is unchanged by regions, which is the point**:
    units sharing one `path` have **disjoint** sections and key separately, so
    it is seventeen comparisons against seventeen numbers, not against 361.

    ⛔ **Both of a unit's buckets are opened before a single document is
    counted**. A `practice_origin` whose unit holds no `practice`
    document then compares that file's headings against **zero**, which is the
    short read it is; opening a bucket only where a document landed would leave
    the file counted by nobody and reported by nothing.

    ⛔ **An authored practice opens its unit's buckets and adds nothing to
    them**: its headings came from a bundle, never from either file.
    """
    totals: dict[tuple[str, int, bool], int] = {}
    declared_by: dict[tuple[str, int, bool], Declared] = {}
    for unit in walk.units:
        n = unit.document.get("unit")
        declared = next((d for d in unit.container.units if d.n == n), None)
        if declared is None or declared.origin is None:
            continue
        prefix = (unit.container.address.unit_key(declared.n), declared.n)
        for role in (False, True) if declared.practice_origin is not None else (False,):
            totals.setdefault((*prefix, role), 0)
            declared_by[(*prefix, role)] = declared
        if _authored(unit.document, unit.where):
            continue
        practice = declared.practice_origin is not None and unit.document.get("kind") == PRACTICE
        headings = (unit.document.get("counts") or {}).get("headings", 0)
        totals[(*prefix, practice)] += headings
    return [_origin(walk, key, declared_by[key], totals[key]) for key in sorted(totals)]


def _authored(document: dict, where: str) -> bool:
    """Whether this document is an authored practice, read off its own `exercise` record.

    ⛔ **Its provenance, not its place**: an ordinal, a path or a bundle
    on disk says where a document sits, and only the record says where its
    material came from.
    """
    try:
        exercise = exercise_of(document, where)
    except ExerciseError:  # pragma: no cover - `validate.corpus` refuses it first
        return False
    return exercise is not None and exercise.provenance == GENERATED


def _origin(walk: Walk, key: tuple[str, int, bool], declared: Declared, recorded: int) -> _Origin:
    """Resolve one bucket's key into the file it is compared against."""
    if key[2]:
        path, section = declared.practice_origin, declared.practice_origin_section
    else:
        path, section = declared.origin, declared.origin_section
    assert path is not None
    return _Origin(key[0], walk.root / path, section, recorded, key[2])


def _read(path: Path) -> str | None:
    try:
        return path.read_text(encoding="utf-8")
    except OSError, UnicodeDecodeError:  # pragma: no cover - reported by the walk
        return None
