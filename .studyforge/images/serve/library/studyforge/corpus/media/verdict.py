"""Should this corpus's media be in git? — answered from the policy and a measurement.

**What it does.** Weighs a measured `MediaFootprint` against the corpus's
declared `MediaPolicy` and returns the verdict: whether the media is committed,
which ignore lines follow from that, and — when a limit has been crossed —
what crossed it, by how much, and which file is responsible.

**How you use it.** `verdict_for(manifest.media, footprint)`, then
`ignore_lines(profile, verdict)` for the rules and `require_committable(verdict)`
where a build must stop.

    verdict = verdict_for(manifest.media, measure(root, units))
    verdict.commits          # True — 'auto' commits
    verdict.refuses          # True — and .report() says what crossed
    require_committable(verdict)   # raises MediaError carrying that report

**Depends on.** `studyforge.corpus.manifest` for the policy,
`studyforge.corpus.placement` for the profile that owns the ignore lines, this
package's `footprint` and `errors`, and the standard library.

## ⛔ `auto` never silently switches, in either direction

⭐ **`auto` means *commit, and stop loudly when the limits say to*.** It is not
"decide later" and it is not "ignore it once it gets big".

- A generator that quietly started ignoring media would produce a corpus whose
  clones are **silent, with no error and no symptom** until a reader complains.
- A generator that quietly kept committing produces the extraction source's
  outcome: **11.42 GiB of pack against a ~5 GB soft limit and one file at
  150.9 MiB against a hard 100 MiB per-file limit**, discovered when the push
  became *impossible* rather than merely large — after the history already
  held the blob.

⛔ **So a crossing changes nothing about what is ignored.** `ignore_media` is
the policy inverted and only the policy inverted; a crossed limit produces a
**refusal**, and the manifest is what a person edits. ⚠️ This is asserted
rather than promised: the ignore lines of a refusing verdict and of a fitting
one under the same policy are the same lines.

## ⭐ `always` and `never` are honoured without measurement

Both are the corpus owner's decision already taken, so `verdict_for` is defined
with no footprint at all for them and never walks a disk to second-guess one.
⛔ **`auto` with no measurement is the refused case** — a mode whose whole
content is "compare against the limits" cannot answer with nothing to compare.

## ⭐ Three limits, and the third is not a bigger version of the other two

⚠️ **`max_files` can be crossed while both byte limits are comfortably under.**
Narration generates many small clips, and 20 000 of them at 20 KB each is
400 MB — a fifth of a 2 GiB total and a five-hundredth of a 100 MiB per-file
block. ⛔ **A repository is paid for per path as well as per byte**, so the
count is a limit a corpus can be right to stop at, and the crossing reports it
**in files**. ⚠️ It has no default: unstated is unbounded, and a ceiling
invented here would refuse builds on a number nobody measured.

## ⛔ The limits are data, and this module names no host

⚠️ **The numbers live in `corpus.json` and their defaults live in the
manifest's own module** (R1). Nothing here knows which service a corpus is
pushed to, what that service blocks, or that any such service exists — a
corpus on a host with different numbers changes one field and the verdict
changes with it, and nothing else does.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import PurePosixPath

from studyforge.corpus.manifest import MediaPolicy
from studyforge.corpus.media.errors import MediaError
from studyforge.corpus.media.footprint import MediaFootprint
from studyforge.corpus.placement import Profile

#: The manifest field names a crossing is reported against. ⛔ Frozen under R9:
#: they are written by fixtures and read by a shipped command
#: whose output is a committed golden, so renaming one is a migration.
LIMIT_TOTAL = "max_total_bytes"
LIMIT_FILE = "max_file_bytes"

#: ⚠️ **Spelled `LIMIT_COUNT`, not `LIMIT_FILES`.** One letter would have been
#: the whole difference between the per-file byte ceiling and the file-count
#: ceiling, on adjacent lines. ⛔ The manifest field it names is `max_files`.
#: ⚠️ **Not frozen under R9**, which froze the two above; it is new here
#: and R9 versions it instead — `corpus_api: 3`.
LIMIT_COUNT = "max_files"

#: What a person can do about a crossing, in the order they are usually
#: considered. ⭐ Stated as a pair because the whole point of refusing early is
#: that there is a decision to take; a refusal with no way forward is a wall.
#: ⛔ Neither of them is *"the build quietly picks one"*.
WAYS_FORWARD = (
    "declare media.commit 'never' in corpus.json and deliver the narration clips outside "
    "the repository as release volumes — an href never encodes how a file arrived (§5), "
    "so the pages do not change and a reader who restores the clips still hears them; "
    "images, video and attachments copy the archive's own files and stay committed",
    "raise the limit in corpus.json, if the place this repository is pushed to accepts "
    "what was measured — the limits are the corpus's own declaration, not this "
    "framework's",
)


#: How many responsible files a sentence names before it counts the rest. ⚠️ A
#: bound on the SENTENCE and never on the record: `Crossing.responsible` still
#: carries every one of them, so a caller that wants the whole list has it.
MOST_NAMED = 3


@dataclass(frozen=True, slots=True)
class Crossing:
    """One limit a measured footprint went past, and what went past it."""

    #: The manifest field, so the sentence names the thing a person edits.
    limit: str
    allowed: int
    measured: int
    #: The generated files responsible, relative to the corpus root — empty for
    #: a limit whose subject is the whole footprint rather than any one file.
    responsible: tuple[PurePosixPath, ...] = ()
    #: What `measured` and `allowed` are counted in. ⛔ **A crossing that
    #: reported a file COUNT in bytes would print `20001 byte(s)` for a corpus
    #: whose problem is 20 001 files** — the number would be right, the
    #: sentence wrong, and a person would go looking for a size.
    unit: str = "byte"

    def sentence(self) -> str:
        """One line naming the limit, the measured value and what is responsible."""
        head = (
            f"media {self.limit} crossed: {self.measured} {self.unit}(s) "
            f"against a limit of {self.allowed}"
        )
        if not self.responsible:
            return head
        named = ", ".join(path.as_posix() for path in self.responsible[:MOST_NAMED])
        rest = len(self.responsible) - MOST_NAMED
        if rest > 0:
            named = f"{named}, and {rest} more"
        return f"{head} — {len(self.responsible)} file(s) over it: {named}"


@dataclass(frozen=True, slots=True)
class MediaVerdict:
    """What this corpus does with its generated media, and why."""

    policy: MediaPolicy
    #: `None` for `always` and `never`, which are honoured without measurement.
    footprint: MediaFootprint | None = None
    crossings: tuple[Crossing, ...] = ()

    @property
    def commits(self) -> bool:
        """Whether generated media is committed.

        ⛔ **The policy, and only the policy.** A crossing does not answer this
        question — it refuses the build so a person answers it.
        """
        return self.policy.commits

    @property
    def ignore_media(self) -> bool:
        """Whether the ignore rules must cover generated media — the inverse."""
        return not self.commits

    @property
    def refuses(self) -> bool:
        """Whether a limit was crossed, and the build must stop and say so."""
        return bool(self.crossings)

    def lines(self) -> list[str]:
        """Return the verdict as a person reads it, in a stated order."""
        committed = "committed" if self.commits else "NOT committed"
        out = [f"media commit {self.policy.commit!r}  generated media is {committed}"]
        if self.footprint is None:
            out.append(f"media measured  not weighed: only 'auto' weighs {self.policy.commit!r}")
            return out
        out.append(
            f"media measured  {self.footprint.total_bytes} byte(s) in "
            f"{self.footprint.count} file(s)"
        )
        # ⛔ What the reading could not weigh is said by name, before any
        # crossing, so a fitting total is never read as the whole corpus.
        out.extend(f"media unweighed  {said}" for said in self.footprint.unweighed)
        if not self.refuses:
            return out
        out.extend(crossing.sentence() for crossing in self.crossings)
        out.append(
            "crossing a limit is a decision, not a switch this build may make for you; "
            "the two ways forward are:"
        )
        out.extend(f"  - {way}" for way in WAYS_FORWARD)
        return out

    def report(self) -> str:
        """Return the verdict as one block of text."""
        return "\n".join(self.lines())


def verdict_for(policy: MediaPolicy, footprint: MediaFootprint | None = None) -> MediaVerdict:
    """Decide what `policy` does with the media `footprint` measured.

    ⭐ `always` and `never` are honoured with no footprint at all. ⛔ `auto`
    with no footprint is refused: its entire content is a comparison, and a
    mode that answered without one would be `always` wearing another name.
    """
    if not policy.has_limits:
        return MediaVerdict(policy)
    if footprint is None:
        raise MediaError(
            f"media commit {policy.commit!r} weighs the generated media against its "
            "limits, and no measurement was taken; measure the corpus root first, or "
            "declare 'always' or 'never' if the decision has already been made"
        )
    return MediaVerdict(policy, footprint, _crossings(policy, footprint))


def ignore_lines(profile: Profile, verdict: MediaVerdict) -> tuple[str, ...]:
    """Return the `.gitignore` lines this corpus's build requires, under this verdict.

    ⛔ **One place inverts the policy.** A second caller writing
    `media=not manifest.media.commits` for itself is how a corpus acquires
    ignore rules that disagree with its own verdict — and the disagreement is
    invisible, because both spellings are right until one of them is edited.
    """
    return profile.ignore_lines(media=verdict.ignore_media)


def require_committable(verdict: MediaVerdict) -> None:
    """Stop the build when a limit has been crossed, carrying the whole report.

    ⚠️ **Early and explained.** This is the moment the extraction source never
    had: the number, the limit it crossed, the file responsible and the two
    ways forward, in front of a person while the decision is still cheap.
    """
    if verdict.refuses:
        raise MediaError(verdict.report())


def _crossings(policy: MediaPolicy, footprint: MediaFootprint) -> tuple[Crossing, ...]:
    """Every limit this footprint crossed, in the order `corpus.json` declares them.

    ⭐ **The count is weighed independently of the bytes, which is the whole
    reason it exists.** Many small clips clear both byte ceilings and still
    make a repository every clone pays for; a corpus can therefore cross
    `max_files` alone, and the report says so in files rather than in bytes.

    ⛔ **An undeclared `max_files` is not a ceiling of zero.** `None` skips the
    comparison outright — the failure this branch must never have is refusing
    the first clip a corpus generates.
    """
    found: list[Crossing] = []
    if footprint.total_bytes > policy.max_total_bytes:
        found.append(Crossing(LIMIT_TOTAL, policy.max_total_bytes, footprint.total_bytes))
    over = footprint.over(policy.max_file_bytes)
    if over:
        found.append(
            Crossing(
                LIMIT_FILE,
                policy.max_file_bytes,
                max(one.size for one in over),
                tuple(one.path for one in over),
            )
        )
    if policy.max_files is not None and footprint.count > policy.max_files:
        found.append(Crossing(LIMIT_COUNT, policy.max_files, footprint.count, unit="file"))
    return tuple(found)
