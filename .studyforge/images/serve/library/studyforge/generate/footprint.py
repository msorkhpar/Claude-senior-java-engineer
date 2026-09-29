r"""What the build's own output is, taken from the plan and never re-listed here.

**What it does.** Holds `Footprint` — the set of paths under an output root
that belong to the build rather than to whoever owns the tree — and the one
call that derives it, `footprint_for(root, profile)`.

**How you use it.**

    fp = footprint_for(corpus_root, profile)
    fp.owns(PurePosixPath("index.html"))     # True: the build writes it

`Footprint()` owns nothing, which is the safe direction: every path already on
disk is refused, exactly as a build behaved before a rebuild had a policy.

**Depends on.** `cli.plan` for the enumeration — deferred, and `footprint_for`
says why — and `corpus.placement` for the archive's location under a corpus's
own profile. ⛔ Nothing here writes, opens an output root, or knows any
source (R1).

## ⛔ THE ENUMERATION IS `studyforge plan`'S, AND THERE IS NO SECOND ONE

⭐ **`plan_for(root).paths` already answers *"what does a build create"***, it
is goldened under `tests/fixtures/golden/*.plan.txt`, and R3's clause is
that the plan and the build agree path for path. ⛔ **So this module asks that
question rather than answering it.** A list of *"files a build writes"* spelled
out here would be a second declaration of the one thing this repository has
most often got wrong by copying — and it would agree with the build for the
same reason the build agrees with itself, which is not agreement.

⚠️ **A plan line ending in `/` is a directory and every line that does not is a
file**, which is the plan's own printed distinction and the only structure this
module reads out of it. Both are needed: the plan enumerates a unit's media as
a **directory**, and the files a build copies into one are named nowhere else.

## ⛔ THE ARCHIVE IS EXCLUDED, AND IT IS THE ONE EXCLUSION

⚠️ The plan declares `…/archive/` because a plan answers *"what will be in my
repository afterwards"* — but **an adapter writes it (R2) and every build only
reads it**. ⭐ It is excluded here because it is the one planned *directory*
whose prefix covers the corpus's entire source material: were a pass ever to
address a path beneath it, "the build's own output" would silently mean
"everything the reader has". ⛔ Asked of `Profile.corpus()`, the one owner of
where an archive sits, never spelled.

## ⛔ NARRATION IS `narrate`'S BESIDE THE MATERIAL, AND A COPY IS OWNED ONLY ELSEWHERE

⭐ **A unit's audio directory is where `studyforge narrate` writes clips beside
the material, so it is NOT a prefix here** — owning it would make narrate's
clips "the build's own" at `--out` = the corpus root. ⛔ **The files a build
copies into it are `clips`, enumerated one by one by the plan from the
narration record** (R8), and `without_clips()` is the
footprint a build into the corpus root uses, where nothing is copied. ⚠️ Which
plan lines are narration is the plan's own `Creation.narration`, never a name
read back off a path.

⚠️ **`site.json` is NOT excluded and that is deliberate.** No pass writes it
either, but it is a single regenerable cache file — *"never the authority"* —
so a wrong answer about it costs a rebuild rather than a corpus.

## ⚠️ A plan that refused anything yields a footprint that owns NOTHING

⛔ A build cannot claim a file as its own on the strength of an enumeration
that is admittedly incomplete. ⭐ The fallback is the previous behaviour —
refuse every occupied path, by name — so the failure mode of a bad plan is a
build that does too little rather than one that overwrites the wrong thing.
"""

from __future__ import annotations

from collections.abc import Iterable
from dataclasses import dataclass, replace
from pathlib import Path, PurePosixPath

from studyforge.corpus.placement import Profile


@dataclass(frozen=True, slots=True)
class Footprint:
    """Which paths under an output root are the build's own to replace.

    ⭐ Both members are relative to the output root, exactly as placement named
    them and exactly as `Written` reports them, so a caller never has to
    normalise one against the other.

    ⛔ **The default owns nothing.** A pass that is handed no footprint, a
    corpus whose plan refused, and the emission census's own filler all land
    here, and all three get R3's floor rather than a licence to overwrite.
    """

    #: Paths the plan names as files — the pages, the index.
    files: frozenset[PurePosixPath] = frozenset()
    #: Paths the plan names as directories, whose contents a build fills — the
    #: shared bundle and each unit's media. ⚠️ A prefix, not a member.
    directories: tuple[PurePosixPath, ...] = ()
    #: Copies of `narrate`'s clips under an output root that is NOT the corpus
    #: root, one path each. ⛔ Never a directory: see this module's contract.
    clips: frozenset[PurePosixPath] = frozenset()

    def without_clips(self) -> Footprint:
        """Return this footprint for a build into the corpus root, where clips are narrate's."""
        return replace(self, clips=frozenset())

    def owns(self, at: PurePosixPath) -> bool:
        """Whether a build writing `at` would be replacing its own prior output.

        ⛔ **By PATH, and never by content.** Nothing is opened, hashed or
        compared: a path the plan names is the build's, a path it does not name
        is somebody else's, and that is the whole rule. ⚠️ So this cannot tell
        the build's own last answer from a copy of it a person has since edited
        — see `generate/writing.py`'s contract, where the consequence is stated
        and R19 is why it is the right one.
        """
        if at in self.files or at in self.clips:
            return True
        return any(at.is_relative_to(directory) for directory in self.directories)


def footprint_for(root: Path | str, profile: Profile) -> Footprint:
    """Derive one corpus's footprint from its plan. ⛔ Nothing is read twice for fun.

    ⚠️ **This re-reads the declarations that `read_corpus` has already read**,
    and that is the price of having exactly one enumeration: `plan_for` is the
    call that knows what a build creates, and handing it the in-memory records
    is not a seam it has. ⭐ The alternative — computing the same paths a second
    way from a `Corpus` — is the defect this module exists to avoid.

    ⛔ **The import is deferred, and it is a CYCLE rather than a preference.**
    `plan_for` lives under `studyforge.cli`, whose package contract imports the
    dispatcher, which registers every verb including `build`, which imports
    `studyforge.generate` — so a module-level import here fails at interpreter
    start with a partially initialised package. ⚠️ Deferring it is the smallest
    repair available inside this package; the real one is a change where
    the enumeration a library needs stops living inside a command. Recorded as
    a finding rather than patched from here.
    """
    from studyforge.cli.plan import plan_for

    plan = plan_for(root)
    if plan.refusals:
        # ⛔ An incomplete enumeration is not a footprint. See the docstring.
        return Footprint()
    narration = [creation.path for creation in plan.creations if creation.narration]
    return of(plan.paths, excluding=profile.corpus().archive, narration=narration)


def of(
    paths: Iterable[str], *, excluding: PurePosixPath, narration: Iterable[str] = ()
) -> Footprint:
    """Split a plan's `create` paths into the files and the directory prefixes.

    ⭐ Separated from `footprint_for` so the split is testable against a list
    of plan lines with no corpus on disk, which is how the goldens are read.
    `narration` names the lines the plan marks as narration: a directory among
    them is no prefix, and a file among them is a clip copy.
    """
    files: set[PurePosixPath] = set()
    clips: set[PurePosixPath] = set()
    directories: list[PurePosixPath] = []
    excluded = PurePosixPath(excluding)
    marked = frozenset(narration)
    for path in paths:
        if path in marked:
            if not path.endswith("/"):
                clips.add(PurePosixPath(path))
            continue
        if path.endswith("/"):
            directory = PurePosixPath(path.rstrip("/"))
            if directory != excluded:
                directories.append(directory)
            continue
        files.add(PurePosixPath(path))
    return Footprint(
        files=frozenset(files), directories=tuple(sorted(directories)), clips=frozenset(clips)
    )
