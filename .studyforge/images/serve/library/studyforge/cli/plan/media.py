"""What a plan says about a corpus's media: the policy, the footprint, the verdict.

**What it does.** Holds `MediaProjection` — the `media` lines of a plan, in one
place: the policy, the per-unit directories, the limits `corpus.json` declares,
the footprint projected at a rate and measured off the disk, and the verdict
`corpus.media` returns on that measurement.

**How you use it.** `derive` builds one and hands it to `Plan`; `cli.site` asks
its `verdict` for the stop a build owes.

**Depends on.** `corpus.manifest` for the policy, `corpus.media` for the
measurement and the verdict, `corpus.placement` for the media kinds. ⛔ No
filesystem, and no second comparison: every verdict here is `verdict_for`'s.

## ⛔ A SPLIT AT A SEAM, and the seam is the subject

⚠️ R11: the report is split. ⭐ **The line taken is the one the report itself
draws**: what a plan
says about **media** is here, and what it says about **paths, edits and
refusals** stays in `report.py`. ⛔ The dependency runs one way — `report`
imports this module for `Plan.media` and nothing here imports `report`, so a
crossing becomes a `Refusal` in `derive`, where a plan is assembled.

## ⛔ The projection and the measurement are different numbers

⭐ A *projection* is a question asked at a rate, before the bytes exist; a
*measurement* is the reading of the disk. ⛔ **Only the measurement may decide a
commit** (§5), so a projection that crosses a limit prints `EXCEEDS` and refuses
nothing, and a measurement that crosses one is the plan's refusal and the
build's stop.
"""

from __future__ import annotations

from dataclasses import dataclass

from studyforge.corpus.manifest import MediaPolicy
from studyforge.corpus.media import MediaFootprint, MediaVerdict, verdict_for
from studyforge.corpus.placement import UNIT_MEDIA_DIRNAMES

#: What the footprint line says when there is neither a reading of the disk nor a rate.
#:
#: ⚠️ **Stated once, as a value, because it is the honest answer and not a
#: placeholder.** ⛔ **A number invented here would be a guess wearing a
#: measurement's clothes**, in the one report a person reads to decide whether
#: to let this tool near a repository they care about. ⭐ A footprint is either
#: MEASURED — the bytes on disk, which `derive` reads for every corpus whose
#: policy weighs its media — or PROJECTED from a rate the person supplied.
#: ⛔ This sentence names no future owner of the measurement, because nothing is owed.
UNPROJECTED = (
    "not stated — this run took no reading of the disk and was given no rate, so "
    "there is no number to weigh. A footprint is measured from the media on disk, "
    "or projected with --bytes-per-unit; the unit count above is known without either."
)

#: What the measured footprint line says the reading covered. ⛔ The population
#: is `corpus.media.measure`'s, and this sentence names it rather than widening it
#: — nor narrowing it: the reading covers every clip the narration record
#: locates, not only the media directories.
MEASURED_OVER = (
    "on disk under the declared units' media directories and wherever the narration "
    "record locates a clip"
)

#: What a refusal for a crossed limit adds: that a build stops on it too.
STOPS = "; a build stops here until corpus.json answers it (§5)"

#: What a measured line adds when the reading found nothing: why, and what fills it.
NOTHING_ON_DISK = (
    "nothing is there yet — `studyforge narrate` writes the clips there, "
    "and --bytes-per-unit projects a footprint before it does"
)


@dataclass(frozen=True, slots=True)
class MediaProjection:
    """What this corpus's generated media will weigh, against what it may."""

    policy: MediaPolicy
    units: int
    bytes_per_unit: int | None = None
    #: ⭐ What the corpus's generated media weighs on disk, read by `derive`
    #: — the population `MEASURED_OVER` names.
    measured: MediaFootprint | None = None
    #: Why the disk could not be read, when a reading was attempted and refused.
    unmeasured: str = ""

    @property
    def total(self) -> int | None:
        """The projected total, or None when no rate was supplied."""
        return None if self.bytes_per_unit is None else self.bytes_per_unit * self.units

    @property
    def verdict(self) -> MediaVerdict | None:
        """`corpus.media`'s verdict on the measurement, or None when none could be taken.

        ⛔ **Asked of `verdict_for`, never re-derived**: the plan's
        refusal and the build's stop read this one verdict. `always` and `never`
        get their verdict with no footprint, which is what `verdict_for` defines.
        """
        if self.policy.has_limits and self.measured is None:
            return None
        return verdict_for(self.policy, self.measured)

    def crossed(self) -> list[str]:
        """One sentence per limit the measured media crossed, naming the number and the limit.

        ⛔ **A projection is never refused** — it is a question asked at a rate,
        not a reading, and only a measurement may decide a commit (§5). ⭐ The
        sentences are the verdict's own; `derive` is where each becomes a
        `Refusal`, because a plan is assembled there.
        """
        verdict = self.verdict
        crossings = () if verdict is None else verdict.crossings
        return [f"{crossing.sentence()}{STOPS}" for crossing in crossings]

    @property
    def ignored(self) -> bool:
        """Whether the ignore lines must cover generated media.

        ⛔ **The inverse of the policy, and never a default of this module's.**
        Generated media is committed by default (§5), so a framework that
        ignored a corpus's narration by reflex would produce clones that are
        silent with no error — the outcome the whole media policy refuses.
        """
        return not self.policy.commits

    def lines(self) -> list[str]:
        """Return every `media` line, in a stated order."""
        committed = "is committed" if self.policy.commits else "is NOT committed"
        out = [
            f"media commit {self.policy.commit!r}  generated media {committed} under this policy",
            # ⛔ The kinds are read from placement's own tuple, never retyped:
            # a fifth kind must not leave this sentence quietly listing four.
            f"media units {self.units}  each may get one directory per kind, made only when "
            f"a build copies a file into it: "
            f"{', '.join(UNIT_MEDIA_DIRNAMES)}",
        ]
        if not self.policy.has_limits:
            out.append(f"media limits  not consulted: only 'auto' weighs {self.policy.commit!r}")
            return out
        out.append(f"media limit max_total_bytes {self.policy.max_total_bytes}")
        out.append(f"media limit max_file_bytes {self.policy.max_file_bytes}")
        out += [f"media footprint  {said}" for said in self._footprints()]
        # ⛔ A clip the reading could not weigh is named, as the verdict names it.
        unweighed = () if self.measured is None else self.measured.unweighed
        out += [f"media unweighed  {said}" for said in unweighed]
        return out

    def _footprints(self) -> list[str]:
        """Return the projection, the measurement, or the honest absence of both.

        ⛔ **A rate never hides a reading.** A person who asks *"would 200 MB a
        unit fit?"* gets the projection first, and the bytes already on disk
        still get their own line: a projection that fits beside a disk that
        does not would otherwise be the one line that overclaims.
        """
        said = [] if self.total is None else [self._projection(self.total)]
        if self.measured is not None:
            said.append(self._measurement(self.measured))
        elif self.unmeasured:
            said.append(f"not measured — {self.unmeasured}")
        return said or [UNPROJECTED]

    def _measurement(self, footprint: MediaFootprint) -> str:
        """Return what the disk weighs and the policy's verdict — ⛔ asked, never re-derived."""
        reading = f"{footprint.total_bytes} byte(s) in {footprint.count} file(s) {MEASURED_OVER}"
        if not footprint.count:
            return f"measured — {reading}; {NOTHING_ON_DISK}"
        verdict = self.verdict
        crossings = () if verdict is None else verdict.crossings
        if crossings:
            crossed = "; ".join(crossing.sentence() for crossing in crossings)
            return f"EXCEEDS — measured {reading}: {crossed}. Crossing a limit is a decision (§5)."
        return f"fits — measured {reading}, within every limit corpus.json declares"

    def _projection(self, total: int) -> str:
        """Return the projection at the supplied rate and its verdict."""
        projected = (
            f"{total} byte(s) projected from {self.units} unit(s) at {self.bytes_per_unit} each"
        )
        if total > self.policy.max_total_bytes:
            return (
                f"EXCEEDS max_total_bytes — {projected}, against a limit of "
                f"{self.policy.max_total_bytes}. Crossing it is a decision (§5), so it is "
                f"reported here rather than at a push that has already become impossible."
            )
        return f"fits — {projected}, within {self.policy.max_total_bytes}"
