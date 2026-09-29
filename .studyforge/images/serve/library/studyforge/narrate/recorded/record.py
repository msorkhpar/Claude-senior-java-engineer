r"""`.studyforge/narration.json` — what a clip was synthesised UNDER (R9).

| | |
|---|---|
| **File** | `.studyforge/narration.json` — ⭐ one located file |
| **Version key** | `narration_api` — ⭐ **minted here**, registered in `version.CONTRACT_FIELDS` |
| **Written by** | `narrate.synth` — ⛔ **the one writer** (unchanged) |

⛔ **Locating the file deliberately left the FIELDS to this module**: the
property it fixed is *for each speech unit, whether the clip on disk was
synthesised under the conditions in force now*. The argument for which facts are
conditions and which are provenance is in the package docstring next door.

## ⛔ `check`, NOT `is_supported` — the opposite of `site_api`, from its argument

⭐ The discovery cache may be discarded on an unknown version because the tree
rebuilds it. ⛔ **This record is rebuildable only by re-synthesising every clip
in the corpus** — hours, a service, and no message — so R9's refusal is spent by
**stopping**. That is the whole difference between derived state and a record of
what happened.

## ⛔ VERSION 2 LOCATES EVERY CLIP IT WROTE

⭐ An entry records `where`, the directory its clip was written into relative to
the corpus root, and `superseded`, every clip an earlier wording or directory
wrote that no prune has removed yet. ⛔ **A version-1 record still reads**: its
entries carry no directory until a run finds their clip, and an entry nobody
can place stays in the record and is held by name, never dropped.

## ⛔ THE CONDITIONS CARRY `engine_model`, AND A RECORD WITHOUT IT STILL READS

⭐ **No version bump: every entry's keys are the same**, and the top-level
`conditions` object carries one optional key, which `_conditions_of` reads as
absent. ⛔ **A record without it reads, and every clip in it is judged STALE, never
current**: its fingerprints were taken over conditions that named no model, so
they cannot equal one that does. The next run asks for every clip once, with the
reason *the conditions changed*, and drops no entry. ⚠️ That is the correct cost,
not a regression: the old record cannot say which model made its clips.

## ⛔ NOTHING HERE KNOWS WHERE A CORPUS KEEPS ITS AUDIO

`state_file(root)` composes the record's own name against the placement
package's `GENERATED_ROOT`, and nothing else. The directory is the policy's;
only the filename is this contract's (R4).
"""

from __future__ import annotations

import json
from collections.abc import Iterable, Mapping
from dataclasses import dataclass, replace
from pathlib import Path

from studyforge.archive.scrub import assert_clean
from studyforge.corpus.placement import GENERATED_ROOT
from studyforge.describe import describe
from studyforge.narrate.answers import Health, NarrationError
from studyforge.narrate.recorded.location import Superseded, checked_where, order
from studyforge.narrate.speakable.naming import digest_of
from studyforge.version import check

ENCODING = "utf-8"

#: R9's key for this contract. ⭐ Minted here and registered
#: in `version.CONTRACT_FIELDS` in the same commit, per that tuple's convention.
NARRATION_API = 2
#: ⛔ Version 1 still reads: its entries are merely unlocated.
KNOWN_NARRATION_API = frozenset({1, NARRATION_API})

#: The record's own name. ⚠️ Its **directory** is the placement policy's
#: `GENERATED_ROOT` and is never spelled here — one authority on layout (R4).
NARRATION_STATE_FILENAME = "narration.json"

#: Fixed rather than sorted, so an unchanged corpus renders identical bytes (R10).
STATE_KEYS = ("narration_api", "conditions", "clips")
CLIP_KEYS = ("filename", "where", "conditions", "engine", "engine_model")

#: Written after `CLIP_KEYS`, and only when an entry has superseded a clip.
SUPERSEDED_KEY = "superseded"

#: Appended while the record is being written. A torn `*.writing` file reads as
#: absent; a record half-overwritten in place reads as *present and wrong*.
WRITING_SUFFIX = ".writing"


class StateError(NarrationError):
    """The regeneration record cannot be read, so nothing may be decided from it.

    ⛔ Deliberately a refusal and not a rebuild: discarding this record
    re-synthesises a whole corpus without saying so.
    """


@dataclass(frozen=True, slots=True)
class Conditions:
    """What a clip is synthesised **under** — everything its filename cannot carry.

    ⛔ `voice` and `fmt` are required and non-empty. A build that leaves them to
    the service's default cannot say what its clips were made under, and the
    record would be a claim rather than a fact.
    """

    voice: str
    fmt: str
    provides: int | None = None
    chunk_chars: int | None = None
    #: ⛔ The deployment's model as `/healthz` reports it, never a guess.
    engine_model: str | None = None

    def __post_init__(self) -> None:
        """Refuse an unstated voice or format, naming which one is missing."""
        for label, value in (("voice", self.voice), ("format", self.fmt)):
            if not isinstance(value, str) or not value.strip():
                raise ValueError(
                    f"a {label} for the record is a non-empty str, got {describe(value)}; "
                    f"state it rather than letting the service choose, or the record "
                    f"cannot say what a clip was made under"
                )

    @classmethod
    def of(cls, health: Health, *, voice: str, fmt: str) -> Conditions:
        """Read the deployment's half off a `probe()` and pair it with the asked-for half."""
        if not isinstance(health, Health):
            raise TypeError(f"conditions are read from a Health, got {describe(health)}")
        return cls(voice, fmt, health.provides, health.chunk_chars, health.engine_model)

    def document(self) -> dict[str, object]:
        """Return the conditions as the object the record carries."""
        return {
            "voice": self.voice,
            "format": self.fmt,
            "provides": self.provides,
            "chunk_chars": self.chunk_chars,
            "engine_model": self.engine_model,
        }

    @property
    def fingerprint(self) -> str:
        """Return the one string a recorded clip is compared against.

        ⛔ Over `sort_keys=True` bytes, so the fingerprint is a property of the
        conditions and not of the order this module happens to write them in.

        ⛔ **`speakable.naming.digest_of`, never a second truncation.** That
        module is the framework's ONE minter of a short hash and a test asserts
        it — `test_exactly_one_module_in_the_whole_framework_truncates_a_digest`.
        """
        return digest_of(json.dumps(self.document(), sort_keys=True, ensure_ascii=False))


@dataclass(frozen=True, slots=True)
class Clip:
    """One recorded clip: what it is called, what it was made under, and by what."""

    filename: str
    conditions: str
    engine: str = ""
    engine_model: str = ""
    #: The directory the clip was written into, relative to the corpus root.
    where: str | None = None
    #: Clips this entry wrote before, still on disk until a prune.
    superseded: tuple[Superseded, ...] = ()

    def document(self) -> dict[str, object]:
        """Return this clip's entry, in `CLIP_KEYS` order, `superseded` only when it has any."""
        written = {
            "filename": self.filename,
            "where": self.where,
            "conditions": self.conditions,
            "engine": self.engine,
            "engine_model": self.engine_model,
        }
        document = {key: written[key] for key in CLIP_KEYS}
        if self.superseded:
            document[SUPERSEDED_KEY] = [
                item.document() for item in sorted(self.superseded, key=order)
            ]
        return document


@dataclass(frozen=True, slots=True)
class State:
    """The record as this build reads it. ⛔ Absent is a state, not a failure."""

    clips: Mapping[str, Clip]
    present: bool = True
    #: The voice the record's top-level conditions name, or `None` (it says what to re-run).
    voice: str | None = None


def state_file(root: Path | str) -> Path:
    """Return where the record lives under `root`. ⛔ The directory is the policy's."""
    return Path(root) / GENERATED_ROOT / NARRATION_STATE_FILENAME


def the_one_file(path: Path | str) -> Path:
    """Return `path`, refusing anything that is not this contract's one filename.

    ⛔ **R9 and R21 say one file and one writer; this is that, enforced.**
    A writer that would put the record anywhere it was pointed is a writer whose
    contract is a convention, and the record is then discoverable only by
    whoever wrote it. ⭐ `state_file(root)` is how a caller obtains the path.

    ⚠️ **A gate rather than a note.** Without it `write_state` would write a file
    named by any caller's argument — a probe filling parameters with `alpha`
    would put one in the repository root, and a stray file changes what an
    unrelated module's scan refuses.
    """
    file = Path(path)
    if file.name != NARRATION_STATE_FILENAME:
        raise StateError(
            f"the regeneration record is {NARRATION_STATE_FILENAME} and this path "
            f"ends in {file.name!r}; ask `state_file(root)` for it rather than "
            f"composing one, because this contract has one file and one writer"
        )
    return file


def read_state(path: Path | str) -> State:
    """Read the record, or report it absent. ⛔ An unreadable one raises (R9).

    ⚠️ **`check`, not `is_supported`** — see the module docstring. An unknown
    `narration_api` stops rather than quietly re-synthesising a corpus.
    """
    where = NARRATION_STATE_FILENAME
    file = the_one_file(path)
    if not file.is_file():
        return State(clips={}, present=False)
    try:
        payload = json.loads(file.read_text(encoding=ENCODING))
    except ValueError, UnicodeDecodeError:
        raise StateError(f"{where} is not readable as JSON") from None
    if not isinstance(payload, dict):
        raise StateError(f"{where} holds {describe(payload)}, not an object")
    api = payload.get("narration_api")
    check("narration_api", api, KNOWN_NARRATION_API, where=where, error=StateError)
    said = payload.get("conditions")
    voice = said.get("voice") if isinstance(said, dict) else None
    voice = voice if isinstance(voice, str) and voice.strip() else None
    return State(clips=_clips_of(payload.get("clips"), where), voice=voice)


def _clips_of(entries: object, where: str) -> dict[str, Clip]:
    """Return the recorded clips, refusing a shape this build cannot decide from."""
    if not isinstance(entries, dict):
        raise StateError(f"{where} holds clips that are {describe(entries)}, not an object")
    clips: dict[str, Clip] = {}
    for speech_id, entry in entries.items():
        if not isinstance(entry, dict):
            raise StateError(f"{where} records {describe(entry)} for a clip, not an object")
        filename, fingerprint = entry.get("filename"), entry.get("conditions")
        if not isinstance(filename, str) or not isinstance(fingerprint, str):
            raise StateError(f"{where} records a clip with no filename or no conditions")
        clips[str(speech_id)] = Clip(
            filename=filename,
            conditions=fingerprint,
            engine=str(entry.get("engine", "")),
            engine_model=str(entry.get("engine_model", "")),
            where=_directory_of(entry.get("where"), where),
            superseded=_superseded_of(entry.get(SUPERSEDED_KEY, []), where),
        )
    return clips


def _directory_of(value: object, where: str) -> str | None:
    """Return a recorded directory, or refuse one that leaves the root — quoting nothing (R7)."""
    if value is None:
        return None
    checked = checked_where(value)
    if checked is None:
        raise StateError(f"{where} records a clip directory that is not under the corpus root")
    return checked


def _superseded_of(value: object, where: str) -> tuple[Superseded, ...]:
    """Return an entry's superseded clips, refusing a shape this build cannot locate."""
    if not isinstance(value, list):
        raise StateError(f"{where} records superseded clips that are {describe(value)}, not a list")
    found: list[Superseded] = []
    for item in value:
        if not isinstance(item, dict) or not isinstance(item.get("filename"), str):
            raise StateError(f"{where} records a superseded clip with no filename")
        found.append(Superseded(item["filename"], _directory_of(item.get("where"), where)))
    return tuple(sorted(set(found), key=order))


def render_state(clips: Mapping[str, Clip], conditions: Conditions) -> str:
    """Return the record's bytes — identical for an unchanged corpus (R10).

    ⚠️ **There is no clock in this document and there is deliberately no room for
    one.** *How stale is this?* is answered by the fingerprints against the
    conditions in force, which is a better answer than a timestamp and needs no
    exemption from R10.

    ⚠️ **The top-level `conditions` are the ones in force at the last write, and
    a clip's own are authoritative.** After a partial failure the two disagree on
    purpose: that is what says which clips still owe a re-synthesis.
    """
    written = {
        "narration_api": NARRATION_API,
        "conditions": conditions.document(),
        "clips": {key: clips[key].document() for key in sorted(clips)},
    }
    document = {key: written[key] for key in STATE_KEYS}
    assert_clean(document, NARRATION_STATE_FILENAME)
    return json.dumps(document, indent=2, ensure_ascii=False, sort_keys=False) + "\n"


def forget(path: Path | str, speech_ids: Iterable[str]) -> tuple[str, ...]:
    """Remove the named entries from the record, keeping everything else; return those removed.

    ⛔ **The smallest removal**: it deletes no file, decides
    nothing about which entries are dead, and keeps the record's top-level
    conditions as they were — the prune in `cli/narrate/` is the caller that
    decides. ⭐ Here so the record keeps ONE writer. An id the
    record does not hold is passed over, and nothing removed writes nothing.
    """
    file = the_one_file(path)
    known = read_state(file)
    removed = tuple(sorted({str(speech_id) for speech_id in speech_ids} & set(known.clips)))
    if not removed:
        return ()
    payload = json.loads(file.read_text(encoding=ENCODING))
    kept = {key: clip for key, clip in known.clips.items() if key not in removed}
    write_state(file, kept, _conditions_of(payload.get("conditions")))
    return removed


def forget_superseded(
    path: Path | str, cleared: Iterable[tuple[str, Superseded]]
) -> tuple[tuple[str, Superseded], ...]:
    """Remove the named superseded clips from their entries; return those removed.

    ⛔ **The prune's removal, as small as `forget`**: it deletes no file and decides
    nothing — the prune decides. ⭐ Here so the record keeps ONE writer. A clip
    the record does not name is passed over, and nothing removed writes nothing.
    """
    file = the_one_file(path)
    known = read_state(file)
    named = {
        (speech_id, item) for speech_id, clip in known.clips.items() for item in clip.superseded
    }
    removed = tuple(sorted(set(cleared) & named, key=lambda pair: (pair[0], order(pair[1]))))
    if not removed:
        return ()
    payload = json.loads(file.read_text(encoding=ENCODING))
    kept = {
        speech_id: replace(
            clip,
            superseded=tuple(item for item in clip.superseded if (speech_id, item) not in removed),
        )
        for speech_id, clip in known.clips.items()
    }
    write_state(file, kept, _conditions_of(payload.get("conditions")))
    return removed


def _conditions_of(document: object) -> Conditions:
    """Return the record's top-level conditions, or refuse without reproducing any of them."""
    where = NARRATION_STATE_FILENAME
    if not isinstance(document, dict):
        raise StateError(f"{where} holds conditions that are {describe(document)}, not an object")
    provides, chunk_chars = document.get("provides"), document.get("chunk_chars")
    for value in (provides, chunk_chars):
        if value is not None and (not isinstance(value, int) or isinstance(value, bool)):
            raise StateError(f"{where} records a deployment condition that is not an int")
    engine_model = document.get("engine_model")
    if engine_model is not None and not isinstance(engine_model, str):
        raise StateError(f"{where} records an engine model that is not a str")
    try:
        return Conditions(
            document.get("voice"), document.get("format"), provides, chunk_chars, engine_model
        )
    except ValueError:
        raise StateError(f"{where} records conditions with no voice or no format") from None


def write_state(path: Path | str, clips: Mapping[str, Clip], conditions: Conditions) -> bool:
    """Write the record if its bytes moved, and report whether they did.

    ⛔ **Identical bytes are not rewritten**, so *"re-running with no content
    change writes nothing"* is true of the record as well as of the clips.
    Staged beside the target and moved into place.
    """
    file = the_one_file(path)
    rendered = render_state(clips, conditions).encode(ENCODING)
    if file.is_file() and file.read_bytes() == rendered:
        return False
    file.parent.mkdir(parents=True, exist_ok=True)
    staged = file.with_name(file.name + WRITING_SUFFIX)
    try:
        staged.write_bytes(rendered)
        staged.replace(file)
    finally:
        staged.unlink(missing_ok=True)
    return True
