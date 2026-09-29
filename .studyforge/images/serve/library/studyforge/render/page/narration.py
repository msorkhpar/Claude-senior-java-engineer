r"""Which narrated element carries which clip, and how the page addresses it.

**What it does.** Holds the one lookup a renderer needs while it is building a
page — *where am I, and is there a clip for here?* — and turns the answer into
the attribute the transport reads.

**How you use it.**

    from studyforge.render.page.narration import Narration

    narration = Narration.of(clips, placement)   # clips: position -> filename
    narration.attribute(section_key, block_path)          # a whole block
    narration.attribute(section_key, block_path, index)   # an item or a row

    Narration.of(clips, placement, missing=gaps)  # gaps: positions promised and unkept

**Depends on.** `render.markup` for escaping, `page.assets` for the attribute's
spelling and for `Placement`, and `corpus.placement` for the name of the
directory a unit's audio was placed in. ⛔ **Not on `narrate`** — see below.

## ⛔ THE KEY IS `SpeechUnit.position`, AND THIS MODULE IMPORTS NOTHING TO KNOW IT

⚠️ **`narrate.speakable` is where a speech unit is minted, and this package must
not reach for it.** ⭐ The record was designed for exactly this hand-off and says
so in its own contract: *"`position` exists so the lookup key has one
spelling … a renderer attaching an id to a node it is building must find the
unit by where it is, not by recomputing the numbering — that second numbering
scheme is the whole defect this package exists to prevent."*

⛔ **So a caller passes the mapping in, keyed by that tuple, and the renderer
never derives a position of its own.** A `render` that imported the walker would
be a renderer that could disagree with the minter about what a unit is called —
and `narrate/speakable/naming.py` records what that costs: the synthesis runner
was handed a bare speech id while the page asked for the digest form, every clip
was missing, and every run re-synthesised the lot.

## ⛔ FILENAMES IN, HREFS OUT — and `Placement` is asked exactly once

⚠️ **The caller supplies a *filename*, never a path.** `audio/<clip>.mp3` is the
`tree` profile's answer and `audio/<stem>/<clip>.mp3` is `sibling`'s; a caller
that composed either would be right under one profile and silently wrong under
the other, with the page rendering identically both ways (R4).

⭐ **`of()` resolves every one of them through `Placement.media` up front**, so
what the block renderers hold is a finished href and no renderer below this line
addresses a file at all. ⛔ That is deliberate: `blocks/prose.py` states *"Not on
`page.assets`: nothing here addresses a file"*, and it is still true after this.

## ⚠️ WHERE THE FILENAME COMES FROM, AND WHY IT IS NOT COMPUTED HERE

⛔ **A clip's filename is `<speech-id>-<8 hex of sha256(spoken text)>.<format>`
(spec §8.2), and the format is NOT knowable at render time.** `narrate/client.py`
builds the placed name from a format the **synthesis service** answered with, so
a renderer that assumed `mp3` would be a second authority on it — and the symptom
is a page linking files that are not on disk, with the suite green. ⭐ The record
that settles it is `.studyforge/narration.json`, whose
`clips.<speech id>.filename` is what was actually placed. ⛔ **This module takes
what a build read out of that record and resolves it; it computes no name and
guesses no suffix.**

## ⛔ THREE STATES, AND A BROKEN PROMISE IS THE ONE THIS PAGE COMPLAINS ABOUT

⚠️ **A corpus that was NEVER NARRATED must not render identically to one whose
audio FAILED**, and the three states are what tell them apart:

| what the record says | what the page shows |
|---|---|
| there is no record at all | ⭐ clean prose — **no player, no notice** |
| a clip is recorded and on disk | the player, and it plays |
| ⛔ a clip is **promised** and is not on disk | the player, **and the page names the gap** |

⭐ **The first row is why nothing is marked by default.** §7's three states (C5)
and §11.0's reading floor say a corpus without narration is **COMPLETE, NOT
SHORT** — so a finished prose corpus carries no permanent "something is missing"
notice, and the rejected option (a visible disabled player on every page) is the
one that contradicts that.

⛔ **`missing` is the third row and NOTHING ELSE.** It is the positions whose
record entry EXISTS and cannot be played — `narrate.playable`'s `NOT_PLACED`,
`MISFILED` and `NOT_ON_DISK`. ⚠️ **`NOT_RECORDED` is not one of them and must
never be passed here**: a unit the record does not mention was never promised
anything, and a page that complained about it would carry a notice for every
corpus somebody narrated half of. ⭐ The caller partitions, because this package
must not import `narrate` (see the key's note below) — and `playable.py`
publishes those four sentences as module constants precisely so a caller can.

## ⛔ A PROMISED, UNDELIVERABLE CLIP IS AN **EMPTY** ATTRIBUTE

⚠️ **An element with no clip carries no attribute at all** — an unnarrated
paragraph never has `data-audio`. ⭐ **A promise the record
made and the disk did not keep is a different state**, and the empty attribute is
what says so, for three reasons:

1. ⛔ **`narration.js` was written for it** and says so in its own comment:
   *"a passage may arrive with an empty source — the renderer emitted the
   attribute and synthesis has not run"*. It maps such a passage to
   `playable = false`, keeps it in the passage count, and never assigns it to
   `audio.src` — so the *"a control that loads the page itself"* hazard is guarded by the
   transport rather than by withholding the
   attribute.
2. ⭐ **`page.document`'s player gate is derived from the body** — a page carries
   a player when its body carries `AUDIO_ATTRIBUTE` — so the transport arrives
   for a broken promise with **no new gate**, which is the whole reason that gate
   was written as a derivation.
3. ⛔ **Withholding it would make the two states identical.** With every clip
   absent and no attribute emitted, a broken corpus would render byte-for-byte
   like a corpus nobody had ever narrated.
"""

from __future__ import annotations

from collections.abc import Iterable, Mapping
from dataclasses import dataclass, field
from types import MappingProxyType

from studyforge.corpus.placement import AUDIO_DIRNAME
from studyforge.describe import describe
from studyforge.render.markup import escape_attribute
from studyforge.render.page.assets import AUDIO_ATTRIBUTE, Placement
from studyforge.render.page.errors import PageError

#: The lookup key a renderer holds while it walks: the served section's key, the
#: block indices from that section down, and the item or row inside the block —
#: or `None` for the block as a whole. ⛔ It is `SpeechUnit.position`'s tuple and
#: is never spelled a second way.
Position = tuple[str, tuple[int, ...], "int | None"]


@dataclass(frozen=True, slots=True)
class Narration:
    """Every clip this page links, by where the element that plays it sits.

    ⭐ A value, not a service: it is built once per page and read while the page
    is composed, so no renderer holds a placement decision or a filename.
    """

    #: `position -> the href the page writes`, already relative to this page.
    hrefs: Mapping[Position, str] = field(default_factory=dict)
    #: Every position the record **promised** a clip for and cannot deliver, in
    #: the order the caller gave them. ⛔ Never a unit the record does not
    #: mention — see this module's contract; that one is not a promise at all.
    #: ⭐ A tuple rather than a set, because a page's bytes may not depend on set
    #: iteration (R10) and because the caller's order is reading order.
    missing: tuple[Position, ...] = ()

    @classmethod
    def of(
        cls,
        clips: Mapping[Position, str],
        placement: Placement,
        missing: Iterable[Position] = (),
    ) -> Narration:
        """Return the narration for one page, resolving each filename to an href.

        ⛔ **`Placement.media` is asked once per clip and never bypassed** — the
        profile knows where a unit's audio was placed and nothing here does.
        ⚠️ A clip whose filename is blank is the same answer as no clip at all:
        the record has an entry and synthesis has not produced one, and a page
        that linked `""` as an *href* would give the reader a control that loads
        the page itself. ⭐ Naming that position in `missing` is how the page
        says so out loud instead.

        ⛔ **`missing` is the promises this page cannot keep**, and a position in
        both arguments is refused rather than resolved: a clip that plays and is
        missing is two answers to one question, and whichever won would be an
        accident of argument order.
        """
        resolved = {
            position: placement.media(AUDIO_DIRNAME, filename)
            for position, filename in dict(clips).items()
            if isinstance(filename, str) and filename.strip()
        }
        gaps = tuple(_position(entry) for entry in missing)
        both = [gap for gap in gaps if gap in resolved]
        if both:
            raise PageError(
                f"{len(both)} narrated element(s) are both playable and named as a gap; "
                f"a clip cannot be on disk and missing, and the page would say either "
                f"depending on which argument was read last"
            )
        return cls(MappingProxyType(resolved), gaps)

    @property
    def promised(self) -> int:
        """How many elements on this page the record undertook to narrate.

        ⭐ The denominator the gap notice quotes, and it is `hrefs` **plus**
        `missing` rather than the page's speech units: a unit the record never
        mentions was never promised, so counting it would make every
        half-narrated corpus look broken.
        """
        return len(self.hrefs) + len(self.missing)

    def attribute(
        self,
        section: str,
        block_path: tuple[int, ...],
        sub_index: int | None = None,
    ) -> str:
        """Return the audio attribute for one element, with its leading space, or `''`.

        ⭐ The leading space is here rather than at every call site: a renderer
        writing `<p{audio}>` cannot then emit `<p >` for an unnarrated paragraph,
        which would be a golden page that differs by one character per block for
        no reason a reader could see (R10 makes that a diff somebody has to read).

        ⛔ **Three answers, not two.** A clip that plays gives its href; a clip
        this page was **promised** and cannot play gives the attribute **empty**,
        which is what puts the element in the transport's passage list and opens
        the player's derived gate; anything else gives nothing at all.
        """
        key = (section, tuple(block_path), sub_index)
        href = self.hrefs.get(key)
        if href:
            return f' {AUDIO_ATTRIBUTE}="{escape_attribute(href)}"'
        # ⚠️ A linear scan over a page's own gaps, deliberately: `missing` is
        # ordered because R10 forbids a page's bytes depending on set iteration,
        # and a page holds tens of units rather than thousands.
        if key in self.missing:
            return f' {AUDIO_ATTRIBUTE}=""'
        return ""

    def __bool__(self) -> bool:
        """Whether this page narrates anything at all.

        ⚠️ **A page whose every promised clip is missing is still narrated**, and
        that is the distinction the whole row exists for: it is a corpus whose
        audio broke, not one nobody ever narrated.
        """
        return bool(self.hrefs or self.missing)


def _position(entry: object) -> Position:
    """Return one gap as the exact key `attribute` looks up, or refuse describing it.

    ⛔ **Normalised here rather than trusted**, for the reason `attribute` calls
    `tuple(block_path)`: a caller holding a list for the block path would name a
    gap no element could ever match, and the symptom would be a page that quietly
    said nothing was wrong.
    """
    try:
        section, block_path, sub_index = entry  # type: ignore[misc]
        return (section, tuple(block_path), sub_index)
    except TypeError, ValueError:
        # ⛔ Described, never echoed (R7): a position carries a corpus's own
        # section key and this refusal goes into a build log.
        raise PageError(
            f"a narration gap is a position — a section key, a block path and an "
            f"item index or None — and this one is {describe(entry)}"
        ) from None


#: The narration of a page that has none. ⛔ A value rather than `None`, so every
#: renderer below calls the same method and no module grows a branch on whether
#: narration exists — which is the branch that would be forgotten in one renderer
#: and leave one block type silently unnarrated.
SILENT = Narration(MappingProxyType({}))
