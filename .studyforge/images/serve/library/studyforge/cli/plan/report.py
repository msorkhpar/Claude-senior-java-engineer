"""What a plan is, and how one line of it reads.

**What it does.** Holds the records a plan is made of — a creation, a
superseded clip, a refusal, the media projection, and the plan itself — and
renders each as one greppable line.

**How you use it.** `derive.plan_for` builds these; `cli.main` prints
`Plan.lines()`; onboarding and `validate.nondestructive` read `Plan.paths`, not the text.

**Depends on.** `corpus.manifest` for the edit record it reports, and
`cli.plan.media` for the `media` lines, which are their own module.
⛔ No filesystem: this module knows what a plan says, never how one was found out —
`derive` takes the reading and hands it here.

## The line format, and why it is shaped like this

One fact per line, `<verb> <subject>  <detail>`, so a plan is greppable by verb
and diffable by path. ⛔ **The verbs are a closed set** — `plan`, `placement`,
`read`, `create`, `replace`, `keep`, `claim`, `expect`, `superseded`, `edit`,
`ignore`, `media`, `refuse` — because the whole point is that a consumer can read this
without a parser and a person can read it without a consumer.

## ⛔ A path's verb says who writes it and whether it is there

A path on disk is never a `create`: the build writes it again (`replace`), or
does not (`keep`). A path the build does not write is never a `create` either:
a unit's media directory is `claim`ed, since a build creates it only when it
copies a file into it, and a path another command writes is `expect`ed,
naming that command. ⭐ `Plan.paths` still names every path, whatever its verb.

⛔ **A superseded clip is none of those, and is not in `Plan.paths`**: it
is on disk, `studyforge narrate` wrote it, and no build ever copies or replaces
it, so a build's footprint taken from `Plan.paths` must never own it.

⛔ **Creations are printed sorted by path** (R10), which `derive` does. Not
grouped by container: the acceptance is a path-for-path diff against what a
build writes, a build enumerates in its own order, and a sorted list is the only
one that cannot disagree for a reason nobody cares about.
"""

from __future__ import annotations

from dataclasses import dataclass

from studyforge.cli.plan.media import MediaProjection
from studyforge.corpus.container import CONTAINER_FILENAME
from studyforge.corpus.manifest import MANIFEST_FILENAME, PermittedEdit
from studyforge.validate.report import INVALID, OK

#: ⛔ The verbs a named path is printed with, by who writes it and whether it is there.
CREATE, REPLACE, KEEP, CLAIM, EXPECT = "create", "replace", "keep", "claim", "expect"
CREATION_VERBS = (CREATE, REPLACE, KEEP, CLAIM, EXPECT)

#: What a `claim` line says: the copy rule, which a plan cannot decide without the unit documents.
WHEN_FILLED = "a build creates it only when it copies a file into it"

#: ⛔ The verb a clip the record names as superseded is printed with, never a creation's.
SUPERSEDED = "superseded"


@dataclass(frozen=True, slots=True)
class Creation:
    """One path a build will create, and what it is."""

    path: str
    what: str
    #: ⭐ Whether the path is narration's: a unit's audio directory, or one clip
    #: `studyforge narrate` wrote there that a build copies into another output.
    #: ⛔ Carried as data so no reader recovers it from a directory's name.
    narration: bool = False
    #: ⭐ The command that writes it when a build does not. Empty means a build does.
    writer: str = ""
    #: ⭐ A unit's media directory, made only when a file is copied into it.
    when_filled: bool = False
    #: ⭐ Whether the path was on disk at the corpus root when the plan was taken.
    present: bool = False

    @property
    def verb(self) -> str:
        """Return the line's verb: never `create` for a path on disk or one a build never writes."""
        if self.when_filled:
            return KEEP if self.present else CLAIM
        if self.writer:
            return KEEP if self.present else EXPECT
        return REPLACE if self.present else CREATE

    def line(self) -> str:
        """Render as one greppable line, saying who writes a path a build does not."""
        note = ""
        if self.when_filled:
            note = f" — {WHEN_FILLED}" if not self.present else " — a build never removes it"
        elif self.writer:
            note = f" — {self.writer} writes it; a build into this root does not"
        return f"{self.verb} {self.path}  {self.what}{note}"


@dataclass(frozen=True, slots=True)
class SupersededClip:
    """One clip an earlier wording or directory wrote, still at the corpus root.

    ⛔ **Never a `Creation`.** A build does not copy it into any output and does
    not write it at the root, so it is named beside the paths, never among them.
    """

    #: Where the record locates it, relative to the corpus root.
    path: str
    #: The speech id whose entry names it.
    speech_id: str

    def line(self) -> str:
        """Render as one greppable line, saying who removes it."""
        return (
            f"{SUPERSEDED} {self.path}  {self.speech_id}'s earlier clip — no build copies it; "
            f"`studyforge narrate --prune` deletes it"
        )


@dataclass(frozen=True, slots=True)
class Refusal:
    """One thing this plan could not work out, and why.

    ⛔ **A refusal is not an `Unchecked`.** A container declaring no `origin`
    under `sibling` has no plan at all, so the exit code says so — an
    integrator who reads a short plan as a small one is exactly the reader
    this command exists for.

    ⛔ **A limit the measured media crossed is a refusal too**, one per
    crossing, at `corpus.json` — the file a person edits to answer it (§5). The
    corpus as it stands cannot be committed as planned, so the plan exits `1`.
    """

    where: str
    why: str
    #: The `validate` rule this refusal is, when it is one. ⭐ A build reads it
    #: to refuse a path two artifacts claim without parsing a sentence.
    rule: str | None = None

    def line(self) -> str:
        """Render as one greppable line."""
        return f"refuse {self.where}  {self.why}"


@dataclass(frozen=True, slots=True)
class Plan:
    """What a build will do to one repository. ⛔ Nothing here has happened."""

    source: str
    title: str
    profile: str
    describes: str
    #: Every file the run opened, relative to the corpus root. ⭐ The exact
    #: list rather than a count, because *"reads no file inside the source
    #: material"* is an acceptance clause and a count cannot be checked
    #: against it.
    read_files: tuple[str, ...]
    creations: tuple[Creation, ...]
    edits: tuple[PermittedEdit, ...]
    ignore: tuple[str, ...]
    media: MediaProjection | None
    refusals: tuple[Refusal, ...] = ()
    #: The file inside a generated directory that holds `ignore`, relative to
    #: the corpus root. ⛔ Never the root ignore file (R3); None when `ignore`
    #: is empty, which it is whenever media is committed.
    ignore_home: str | None = None
    #: ⛔ Clips the record names as superseded. Never in `paths`.
    superseded: tuple[SupersededClip, ...] = ()

    @property
    def paths(self) -> tuple[str, ...]:
        """Every path to be created, in the order the plan prints them.

        ⭐ What `validate.nondestructive` compares a finished build against and what the
        onboarding skill renders from — both take this rather than re-parsing the text.
        """
        return tuple(creation.path for creation in self.creations)

    @property
    def exit_code(self) -> int:
        """`0` when the whole corpus could be planned, `1` when any of it could not.

        ⛔ A crossed media limit is among `refusals`, so it exits `1` here.
        """
        return INVALID if self.refusals else OK

    def lines(self) -> list[str]:
        """Return the whole report, one fact per line."""
        out = [
            f"plan {self.source}  {self.title}",
            f"placement {self.profile}  {self.describes}",
            f"read {MANIFEST_FILENAME} + {self._maps()} {CONTAINER_FILENAME}{self._others()}"
            f"  no file inside the source material was opened",
        ]
        out += [creation.line() for creation in self.creations]
        out += [clip.line() for clip in self.superseded]
        for edit in self.edits:
            out += edit_lines(edit)
        out += [f"ignore {line}  in {self.ignore_home}" for line in self.ignore]
        out += self.media.lines() if self.media is not None else []
        out += [refusal.line() for refusal in self.refusals]
        out.append(self.summary())
        return out

    def _maps(self) -> int:
        """How many container maps were read."""
        return sum(1 for read in self.read_files[1:] if read.endswith(CONTAINER_FILENAME))

    def _others(self) -> str:
        """Every other file read, named: the narration record, when there is one."""
        others = [read for read in self.read_files[1:] if not read.endswith(CONTAINER_FILENAME)]
        return "".join(f" + {read}" for read in others)

    def summary(self) -> str:
        """One line naming every count. ⛔ Including the zeroes.

        ⚠️ A number that disappears when it is zero cannot be told from a
        number nobody wrote — the archive's `counts` makes the same argument,
        and *"this build edits nothing of yours"* is the single line a
        repository owner most wants stated.
        """
        said = {verb: sum(1 for c in self.creations if c.verb == verb) for verb in CREATION_VERBS}
        return (
            f"plan: {said[CREATE]} path(s) to create, {said[REPLACE]} to replace, "
            f"{said[KEEP]} to keep, {said[CLAIM]} claimed, {said[EXPECT]} expected from "
            f"another command, {len(self.superseded)} superseded clip(s) no build copies, "
            f"{len(self.edits)} file(s) to edit, {len(self.ignore)} ignore line(s), "
            f"{len(self.refusals)} refusal(s)"
        )


def edit_lines(edit: PermittedEdit) -> list[str]:
    """Return the four lines one declared edit gets: what, addition, reason, undo.

    ⛔ **Four, not one.** R3 permits an edit only where the manifest declares
    it and only if it is additive, so a reader auditing one needs the anchor,
    the exact text going in, the reason somebody gave and how to take it back
    out — and a single line long enough to hold all four is one nobody reads.

    ⚠️ **The undo line is composed from `Reversal`'s fields rather than from
    its `__str__`**, which describes the content as *"a str"*. That is right
    for a refusal and useless in a plan, and the module owning it says the
    line is *"a one-line description a person can read in a plan"* — so the
    wording is that module's to correct, never this one's.
    """
    undo = edit.reversal
    return [
        f"edit {edit.path}  {edit.kind} after {edit.anchor!r}",
        f"edit {edit.path}  adds {edit.content!r}",
        f"edit {edit.path}  why {edit.why}",
        f"edit {edit.path}  undo {undo.kind} {undo.content!r} from {undo.path}",
    ]
