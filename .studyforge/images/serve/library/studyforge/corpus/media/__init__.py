"""The media footprint policy — when a corpus has outgrown committing its own audio.

**What it does.** Answers one question: *should this corpus's generated media
be in git?* It measures what was actually written to disk, weighs it against
the `media` policy the manifest declared, and returns a verdict carrying the
ignore rules that follow and — when a limit has been crossed — the number, the
limit, the file responsible and the ways forward.

**How you use it.** Measure, then decide, then act:

    from studyforge.corpus.media import measure, verdict_for, ignore_lines

    verdict = verdict_for(manifest.media, measure(root, units))
    ignore_lines(profile, verdict)   # what a build writes
    require_committable(verdict)     # where it must stop

**Depends on.** `corpus.manifest` for the policy, `corpus.placement` for the
locations and the ignore lines, `narrate.synth` for the narration record the
footprint reads, and the standard library. ⛔ Nothing
source-specific and no host named anywhere (R1).

## ⛔ Committed by default, and the default has a ceiling

⭐ **Generated media is committed**, because *regenerable is not the
same as available*: a clone that carries its own audio speaks with no synthesis
service, no GPU and no network, which is what R8 is for. ⛔ **That default
holds until a corpus is too big for it, and this package is what knows the
difference** — from the manifest's policy and a reading of the disk, never from
a prediction and never from a rule about any one repository.

## ⛔ What this package does NOT do

⚠️ **It does not implement extraction.** ⭐ This is the **awareness**:
measure, compare, report, and produce the right ignore rules for whichever
answer applies. Packing clips into release volumes and restoring them is
`narrate.release`'s, which plugs in behind this decision without touching a
page, because §5 rules delivery orthogonal to placement.

## What is in the package

| Module | Owns |
|---|---|
| `footprint` | the walk and the reading — `MediaFile`, `MediaFootprint`, `measure` |
| `recorded` | the clips the narration record locates, which the reading also weighs |
| `verdict` | the decision — `Crossing`, `MediaVerdict`, `verdict_for`, `ignore_lines` |
| `errors` | `MediaError`, the only exception raised here |

⛔ **The projection and the measurement are different numbers and are kept
apart.** A *projected* footprint is `cli/plan/report.py`'s, taken at a rate
before the bytes exist, so the question can be asked before the gigabytes are
on disk; the *measured* one is this package's, and it is the only number a
commit decision may rest on. ⭐ **`studyforge plan` prints both**: the
measured line is `measure` and `verdict_for` asked from here, never a second
measurement, so the plan reads whatever population this package weighs.

⛔ **The population is wider than the declared units' media directories**:
the declared units' media directories, and every
clip the narration record locates under the corpus root. A recorded clip that
cannot be located is named in `MediaFootprint.unweighed`, never dropped.
"""

from __future__ import annotations

from studyforge.corpus.media.errors import MediaError
from studyforge.corpus.media.footprint import (
    MediaFile,
    MediaFootprint,
    measure,
    measure_directories,
)
from studyforge.corpus.media.verdict import (
    LIMIT_COUNT,
    LIMIT_FILE,
    LIMIT_TOTAL,
    MOST_NAMED,
    WAYS_FORWARD,
    Crossing,
    MediaVerdict,
    ignore_lines,
    require_committable,
    verdict_for,
)

__all__ = [
    "Crossing",
    "LIMIT_COUNT",
    "LIMIT_FILE",
    "LIMIT_TOTAL",
    "MOST_NAMED",
    "MediaError",
    "MediaFile",
    "MediaFootprint",
    "MediaVerdict",
    "WAYS_FORWARD",
    "ignore_lines",
    "measure",
    "measure_directories",
    "require_committable",
    "verdict_for",
]
