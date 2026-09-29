r"""The speakable contract: what is said out loud, and what every spoken unit is called.

**What it does.** Turns one served unit document into its ordered script — deciding
per block type what becomes speech — and mints both names that script is addressed
by: the positional speech id, and the clip filename that carries a digest of the
words. It is the single source of truth for both halves of narration.

**How you use it.** `speakable_of(document)` returns a `Speakable` for one unit;
`clip_name(unit)` is the one minter every consumer calls for a clip's filename;
`by_position(units)` is the renderer's lookup from a node's coordinates back to the
unit belonging to it.

**Depends on.** `studyforge.address` for what a unit key is, `studyforge.archive`
for the block vocabulary and the personal-data gate, and
`studyforge.render.markup` for the one inline-marker parser (see `voice.py`'s
contract for why that dependency is taken deliberately), and
`corpus.manifest.SOURCE_SUFFIXES`, the framework's constant table of what a code
file is called. ⛔ Nothing of a corpus is read, not `serve`, and nothing here
knows where a clip lands (R4).

## ⛔ Displayed text and spoken text are two renderings of ONE list

⚠️ **Not two lists that happen to agree.** This one module owns both the string
handed to synthesis and the stable id it is addressed by; every consumer copies
those ids rather than deriving its own. The page writes them into its markup, the
synthesis pass names its audio from them, and drift is structurally impossible
rather than merely unlikely.

⛔ **A second minter is a page asking for a file the placer never wrote, with no
symptom but silence** — `naming.py`'s contract records how the extraction source
did exactly that.

## ⛔ The three assertions this package owes, and why the third is not a restatement

⭐ **Clip names are distinct.** *Every id in a page resolves to a clip* and *every clip is named
by a page* are **both** satisfied by a collision: seventeen clips landing on one
filename still resolve in both directions, the suite stays green, and sixteen units
play the wrong audio. ⛔ **So the owed assertion is a cardinality —
`|clip names| == |spoken units|`** — which is the only form that states injectivity.
⭐ And the negative belongs here because the minter is here: **two spoken units that
differ only in their unit's `origin.section` mint different names.**

## ⛔ Narration speaks a lesson's prose, and never a practice

⭐ **Register ruling (2026-09-26): narration covers a lesson's prose only.** A
section whose kind is `practice` yields no speech unit at all: not its
statement, not its lesson heading, not its examples, and nothing its panel
carries. ⚠️ A practice is worked at, not listened to, and before the ruling
its clips were most of a corpus's audio. ⭐ The page follows by construction:
its narration is joined from these units (`narrate.playable`), so a practice
part carries no audio attribute however old the record is. A code example is
never spoken either, and `script.py` says why; that includes a lesson's
code-example panel, and a heading over nothing spoken.

⭐ **Skipping a section renumbers nothing.** A speech id is minted from the
section's own key and the block's position inside it, so every lesson unit
keeps the id, and the clip, it had while practices were narrated; `narrate
--prune` retires the practice clips no page speaks any more.

## ⚠️ Identifiers are always split, and there is no knob

⛔ **No manifest field selects how an identifier is pronounced.** The extraction
source carried a `SPLIT`/`VERBATIM` switch; nothing in this framework's manifest
declares one, nobody has asked for one, and a knob with no constituency is the
flexibility §4's YAGNI refuses. ⭐ If a real source wants verbatim identifiers that
is a finding with a source behind it, not a parameter shipped ahead of one.
"""

from __future__ import annotations

from studyforge.address import Address, is_slug
from studyforge.narrate.speakable.naming import (
    BLOCK,
    DIGEST_JOIN,
    DIGEST_LENGTH,
    SEGMENT,
    SUB_MARKER,
    UNIT_SEPARATOR,
    clip_name,
    digest_of,
    parse_clip_name,
    speech_id,
    unit_key_of,
    unit_token,
)
from studyforge.narrate.speakable.records import Speakable, SpeakableError, SpeechUnit
from studyforge.narrate.speakable.script import SPEECH_OF, ordinal_word, units_of
from studyforge.narrate.speakable.voice import URL_PHRASE, spoken_text

#: ⛔ The package's whole public surface. A consumer reaching past this into a
#: module is a consumer this contract failed.
__all__ = [
    "BLOCK",
    "DIGEST_JOIN",
    "DIGEST_LENGTH",
    "SEGMENT",
    "SPEECH_OF",
    "SUB_MARKER",
    "UNIT_SEPARATOR",
    "URL_PHRASE",
    "Speakable",
    "SpeakableError",
    "SpeechUnit",
    "by_position",
    "clip_name",
    "clip_names",
    "digest_of",
    "ordinal_word",
    "parse_clip_name",
    "speakable_of",
    "speech_id",
    "spoken_text",
    "unit_key_of",
    "unit_token",
    "units_of",
]


#: The served section kind a practice is, in the unit document's own word.
PRACTICE = "practice"


def speakable_of(document: dict) -> Speakable:
    """Return one served unit document's whole script, in reading order.

    ⛔ Takes the document the builder wrote and nothing else: the identity every id
    is minted from is the unit's own `address` and `unit`, so a caller cannot hand
    this a section list and have it guess whose it is.

    ⚠️ A repeated section key is refused rather than silently merged. Two sections
    sharing a key mint colliding ids, and the failure would surface much later as
    one clip playing under two paragraphs.

    ⛔ A practice section yields nothing (see the module contract), and its key is
    still checked: a practice whose key repeats a lesson's is the same defect.
    """
    if not isinstance(document, dict):
        raise SpeakableError("a speakable script is derived from a served unit document")
    unit = unit_token(Address(_field(document, "address")).unit_key(_ordinal(document)))
    sections = _field(document, "sections")
    if not isinstance(sections, list):
        raise SpeakableError("a served unit document's 'sections' must be a list")
    spoken: list[SpeechUnit] = []
    withheld = 0
    seen: set[str] = set()
    for section in sections:
        key = _section_key(section, seen)
        seen.add(key)
        if section.get("kind") == PRACTICE:
            continue
        found, held = units_of(unit, key, section.get("blocks"))
        spoken += found
        withheld += held
    return Speakable(unit_key=unit_key_of(unit), units=tuple(spoken), withheld=withheld)


def by_position(units: tuple[SpeechUnit, ...]) -> dict:
    """Return `{position: unit}` — the renderer's lookup, so it derives no second numbering.

    ⭐ Keyed on `SpeechUnit.position`, which is the record's own spelling of the
    tuple, so a consumer never writes the key out again (R1's smaller cousin: one
    spelling per fact).
    """
    return {unit.position: unit for unit in units}


def clip_names(units: tuple[SpeechUnit, ...]) -> tuple[str, ...]:
    """Return one clip name per unit, in order — ⛔ through `clip_name`, never beside it.

    ⚠️ Published so a caller that wants the whole set does not loop and compose:
    `|set(clip_names(units))| == len(units)` is that cardinality, asked of
    the names this package actually mints.
    """
    return tuple(clip_name(unit) for unit in units)


def _field(document: dict, name: str) -> object:
    """Return a required field of the served document, or say which one is missing."""
    if name not in document:
        raise SpeakableError(f"a served unit document must carry {name!r}, and this one does not")
    return document[name]


def _ordinal(document: dict) -> int:
    """Return the unit's ordinal, refusing anything a unit key could not be built from."""
    ordinal = _field(document, "unit")
    if not isinstance(ordinal, int) or isinstance(ordinal, bool):
        raise SpeakableError("a served unit document's 'unit' must be an int")
    return ordinal


def _section_key(section: object, seen: set[str]) -> str:
    """Return one section's key, refusing a non-slug and a repeat.

    ⛔ The refusal never reproduces the key: a section key comes from a file a person
    edits, so one of them can be an absolute path (R7).
    """
    if not isinstance(section, dict):
        raise SpeakableError("every entry in 'sections' must be a section record")
    key = section.get("key")
    if not is_slug(key):
        raise SpeakableError(
            "a section's key must already be a slug — lowercase, hyphen-separated — "
            "because a clip filename is minted from it"
        )
    if key in seen:
        raise SpeakableError(
            "two sections share one key, which would mint colliding speech ids and "
            "surface later as one clip playing under two paragraphs"
        )
    return key
