r"""The shape a unit has when nobody has curated it: one section per archive file.

**What it does.** Turns a unit's ingested documents into sections, **computing**
the order — lessons before practices, each in its ordinal order.

**How you use it.** `sections(material)` returns the section records.

**Depends on.** `unit.sections` for the key vocabulary, `builder.parts`.

## ⭐ This is the common case, and it is why ingestion is worth running early

⛔ **A unit with no overlay is readable the day it is ingested.** That is what
makes capture worth doing before any judgement has been made about the
material, and it is the shape most units are in for most of their life.

## ⛔ This module computes order. Its sibling must never re-derive it.

⚠️ **`authored` is a separate module for exactly this reason**, and the split is
a seam rather than a slice at a convenient line. Here the order is *derived* —
lessons, then practices, by ordinal. There it is the **author's**, used
verbatim, and re-deriving it would mint different speech ids for the same unit
and desynchronise the page from its audio.

⭐ **Split, the authored path cannot reach the ordering code by accident.** In
one module they are two branches of one function and the only thing keeping
them apart is that nobody has edited it carelessly yet. ⛔ The failure is
silent — every page renders, every clip exists, and they belong to different
sentences.
"""

from __future__ import annotations

from studyforge.unit.builder.material import Material
from studyforge.unit.builder.parts import section
from studyforge.unit.sections import derived_section_key


def sections(material: Material) -> tuple[dict, ...]:
    """One section per archive document, in the order this module computes.

    ⚠️ The position handed to `derived_section_key` is the document's place
    **among its own kind**, never its ordinal: an ingestion that skipped
    `lesson-2` must not leave a hole in the speech ids, because those ids name
    audio files on disk.
    """
    built: list[dict] = []
    for kind in ("lesson", "practice"):
        for index, document in enumerate(material.of_kind(kind)):
            built.append(
                section(
                    key=derived_section_key(material.variant, kind, index),
                    kind=kind,
                    heading=document["title"],
                    blocks=document.get("blocks") or [],
                    document=document,
                )
            )
    return tuple(built)
