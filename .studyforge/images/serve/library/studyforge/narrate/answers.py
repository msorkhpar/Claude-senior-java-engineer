r"""What the narration service answers, as values, and placing them: no wire here.

**What it does.** Holds the answers a narration run hands back (`Health`,
`Artifact`, `Narration`), the error family every refusal on this path belongs to
(`NarrationError`), the one shape a synthesis pass needs from a client
(`Narrator`), and `place`, which writes a run's artifacts into a directory.

**How you use it.** A build's read side and the synthesis pass import from here.
`place(narration, into)` writes the clips, `into` being the directory the caller
got from `corpus.placement`.

**Depends on.** `dataclasses`, `pathlib`, `typing` (standard library only) and
`narrate.speakable.records` for the unit type. ⛔ **Not on `narrate.wire` and not
on `narrate.client`**, and that is the reason this module exists.

## ⛔ THE WIRE SEAM, AND WHY THESE VALUES ARE ON THIS SIDE OF IT

⭐ **`narrate.wire` is bytes out and bytes in: `urllib`, the transport and
reading a JSON answer.** `narrate.client` speaks the service's protocol over it.
⛔ **A build never synthesises** (R8), but it does read the record,
and the record's `Conditions` are read off a `Health`. With `Health` inside the
client module, `import studyforge.generate` loaded the HTTP client,
and so did `cli/plan`. ⭐ So the values sit here and both sides import
them, and a fresh interpreter's `sys.modules` is the test, not source text.

## ⛔ R4: `place` HOLDS NO OPINION ABOUT WHERE AUDIO LIVES

⛔ `into` is an argument, with no default, no `"audio"` and no join against a
profile. The caller asks the placement policy and passes the answer in.
"""

from __future__ import annotations

from collections.abc import Sequence
from dataclasses import dataclass
from pathlib import Path
from typing import Protocol

from studyforge.narrate.speakable.records import SpeechUnit

#: Written while an artifact is being placed, then renamed over the target.
PARTIAL_SUFFIX = ".partial"


class NarrationError(Exception):
    """Synthesis did not happen.

    ⛔ **`PersonalDataLeak` is deliberately outside this family and is never
    translated into it**: a caller catching this reports and carries
    on, and an R7 refusal must stop the run. ⭐ Two exceptions travel through
    unconverted, deliberately — `PersonalDataLeak` and `SpeakableError`.
    """


@dataclass(frozen=True, slots=True)
class Health:
    """Whether the service is there, and what it says about itself.

    ⛔ `reachable=False` is an answer, not an error: `detail` is a sentence a
    build prints before carrying on without narration (R6, R8).
    """

    reachable: bool
    detail: str
    provides: int | None = None
    #: ⛔ In the content address AND a deployment setting: read it here, never
    #: from a constant.
    chunk_chars: int | None = None
    #: ⛔ The model the deployment synthesises with, as `/healthz` reports
    #: it. In the service's content address, so a change makes every clip stale.
    engine_model: str | None = None


@dataclass(frozen=True, slots=True)
class Artifact:
    """One placeable clip: what it is called, and what made it.

    `filename` comes from `speakable.naming`'s one minter, which takes the whole
    unit so an id cannot be paired with words that are not its own. ⚠️ `engine` and
    `engine_model` are *what made this file* — on a cache hit, the FIRST
    synthesis's, not necessarily the one deployed.
    """

    speech_id: str
    filename: str
    media_type: str
    audio: bytes
    engine: str
    engine_model: str
    status: str


@dataclass(frozen=True, slots=True)
class Narration:
    """One job's whole outcome, in memory and not yet on disk.

    ⭐ **Nothing is written until `place` is called** — *"no partial state"* as a
    property of the shape rather than a promise about error handling.
    """

    artifacts: tuple[Artifact, ...]
    failed: tuple[tuple[str, str], ...]
    retryable: tuple[str, ...]
    voice: str
    fmt: str
    provides: int | None


class Narrator(Protocol):
    """What the narrate stage and its pass need from a client: a probe, and one job.

    ⭐ `narrate.client.NarrateClient` is the one implementation. The stage and the
    pass name this shape instead of that class, so importing them loads no wire.
    """

    def probe(self) -> Health:
        """Report whether the service is there, and its deployment's settings."""
        ...

    def narrate(self, units: Sequence[SpeechUnit]) -> Narration:
        """Synthesise `units` as one job and return their artifacts, unplaced."""
        ...


def place(narration: Narration, into: Path) -> tuple[Path, ...]:
    """Write every artifact into `into` — ⛔ the directory the POLICY named.

    ⭐ `into` is an argument and never a default: this module holds no opinion about
    where a study site keeps its audio (R4). Each file lands in a temporary sibling
    and is renamed over its target, so an interrupted run leaves no truncated clip
    that looks finished — the service's own property, at this end.
    """
    destination = Path(into)
    destination.mkdir(parents=True, exist_ok=True)
    written: list[Path] = []
    for artifact in narration.artifacts:
        target = destination / artifact.filename
        partial = target.with_name(f".{target.name}{PARTIAL_SUFFIX}")
        partial.write_bytes(artifact.audio)
        partial.replace(target)
        written.append(target)
    return tuple(written)
