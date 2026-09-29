r"""The two records narration travels in, and the one refusal this package raises.

**What it does.** Defines `SpeechUnit` — one thing the narrator says, and the
name it is addressed by — and `Speakable`, one unit's whole ordered script plus
the count of what it deliberately did not say.

**How you use it.** Both come back from `speakable.speakable_of(document)`;
this module is where their fields are documented. `SpeakableError` is what a
document the walker cannot read raises.

**Depends on.** `dataclasses`. ⛔ Nothing else, not even this package's own
walker: a record that could not be read without the code that built it is not a
record.

## ⛔ The id is 1-based and the coordinates are 0-based, on purpose

⚠️ **`id` carries human-facing positions counted from one; `block_path` and
`sub_index` are Python subscripts counted from zero.** ⭐ The two bases differ
deliberately, and the reason is inherited from the extraction source: an id is a
*name* a reader will read aloud in a bug report, and the coordinates are a
*subscript* a renderer indexes with. ⛔ Quietly making one look like the other is
how an off-by-one hides — and it hides in audio, where nothing renders wrong.

## ⛔ `position` exists so the lookup key has one spelling

⚠️ A renderer attaching an id to a node it is building must find the unit by
where it is, not by recomputing the numbering — that second numbering scheme is
the whole defect this package exists to prevent. ⭐ So the key is a property of
the record rather than a tuple every consumer writes out again.

## ⚠️ What `withheld` counts, and what it does not

⛔ **`withheld` is the blocks a `disclosure` holds back** — its body, recursively,
every one of which gets no speech id and therefore cannot be addressed by any
clip. ⭐ That is the ruled case: narration speaks a disclosure's summary and
stops, and the count is what lets a coverage report name a unit with unspoken
content rather than leaving the omission to look like a walker bug (R6).

⛔ **It is NOT a total count of everything the audio leaves out, and saying so is
the point of this paragraph.** An `image`, a `video`, a `rule` and an `html`
block each yield no speech unit by their own type, and an empty paragraph yields
none by emptiness; none of those is counted here. ⚠️ A coverage report that reads
this field as *"blocks the listener does not hear"* would under-report a unit
that is mostly raw HTML — R17's rule that a
guarantee does not extend to what sits beside it, stated here rather than
discovered downstream.
"""

from __future__ import annotations

from dataclasses import dataclass


class SpeakableError(Exception):
    """A document cannot be turned into a script, or a name cannot be minted.

    ⛔ **`PersonalDataLeak` is deliberately outside this family and is never
    translated into it**. A caller looping over a corpus catches this
    per unit and reports the rest; an R7 refusal must stop the run rather than be
    logged as one more unit that did not narrate.
    """


@dataclass(frozen=True, slots=True)
class SpeechUnit:
    """One thing the narrator says, and the name it is addressed by.

    `id` is `<unit>.<section>.b<n>` for a whole block, with a `.b<m>` segment per
    level of nesting, and `.i<m>` or `.r<m>` appended for a list item or a table
    row. `speak` is the string handed to synthesis, already gated (R7).
    `section` is the served section's key; `block_path` indexes the blocks lists
    from the section down; `sub_index` is the item or row inside that block, or
    `None`. `kind` is the block type the speech came from.
    """

    id: str
    speak: str
    section: str
    block_path: tuple[int, ...]
    sub_index: int | None
    kind: str

    @property
    def position(self) -> tuple[str, tuple[int, ...], int | None]:
        """Return where this unit sits — the renderer's lookup key, spelled once."""
        return (self.section, self.block_path, self.sub_index)


@dataclass(frozen=True, slots=True)
class Speakable:
    """One unit's whole script: what is said, in order, and what was held back.

    `unit_key` is the unit's logical address as `<address>/unit-NN` — the
    identity every id in `units` is minted from. `withheld` is documented in this
    module's contract, including what it does not count.
    """

    unit_key: str
    units: tuple[SpeechUnit, ...]
    withheld: int
