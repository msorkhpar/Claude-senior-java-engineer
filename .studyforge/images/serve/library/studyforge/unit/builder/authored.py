r"""The shape a unit has when somebody curated it: the author's order, verbatim.

**What it does.** Uses an overlay's `sections` array exactly as written, adding
only the two fields that are not the author's to write.

**How you use it.** `sections(overlay, material)` returns the section records.

**Depends on.** `unit.content` for what an overlay is, `builder.parts`.

## ⛔ The order is the author's and is never re-derived

⚠️ **This is the seam, and its failure is silent.** Two consumers ordering one
unit differently mint **different speech ids for the same section**, and the
page then asks for audio that belongs to another sentence: every page renders,
every clip exists, and they no longer correspond.

⭐ **So there is no ordering code in this module at all.** Not a sort that
happens to be stable, not a comparison guarded by a flag — none, so that the
authored path cannot reach one by accident. The computing half lives in
`builder.derived`, which is a different module for exactly this reason.

## ⭐ The blocks are the author's; the archive supplies two fields and no more

⚠️ **This is where the two shapes actually differ in content, not only in
order.** A derived section's blocks *are* the archive's; an authored section's
are what the author wrote, used as written. ⛔ The archive document behind it is
consulted for `video` and `workspace` and for nothing else.

## ⚠️ What an author may not write, and where it comes from instead

`unit.content.DERIVED_FIELDS` refuses `workspace` and `video` in a
`content.json` rather than silently overwriting them, so a person who wrote one
by mistake is **told** instead of watching it vanish on the next run. This
module is the other half of that promise: they arrive from the archive, through
`builder.parts`.

## ⛔ A section the archive cannot supply is a refusal, not a gap

⚠️ An authored `practice-java` with no practice document behind it would render
as an exercise with no workspace and no grader — ⭐ **which is exactly the
ungraded state**, and therefore indistinguishable from a corpus that meant it.
So it is refused, naming the key.

## ⚠️ What this module does **not** assert

⛔ The neighbours are named: it does not check that the overlay's
`address` and `unit` match the material's — `builder.document` does that once,
where both are in hand — and it does not check that a section's `blocks` are
well formed, which is `archive.blocks`' vocabulary and `validate`'s sweep.
"""

from __future__ import annotations

from studyforge.unit.builder.material import Material
from studyforge.unit.builder.parts import section
from studyforge.unit.content import Overlay, Section
from studyforge.unit.errors import ContentError
from studyforge.unit.sections import KIND_OF


def sections(overlay: Overlay, material: Material) -> tuple[dict, ...]:
    """Return the author's sections, in the author's order, with the derived fields."""
    behind = _by_key(overlay, material)
    return tuple(
        section(
            key=authored.key,
            kind=authored.kind,
            heading=authored.heading,
            blocks=list(authored.blocks),
            document=behind[authored.key],
        )
        for authored in overlay.sections
    )


def _by_key(overlay: Overlay, material: Material) -> dict[str, dict]:
    """Which archive document stands behind each authored section.

    ⚠️ **Matched by the section key, which is what the archive's own `kind` and
    `variant` derive to** — never by position, because the author's order is
    the one thing here that is not the archive's.
    """
    from studyforge.unit.sections import derived_section_key

    available: dict[str, dict] = {}
    for kind in KIND_OF:
        for index, document in enumerate(material.of_kind(kind)):
            available[derived_section_key(material.variant, kind, index)] = document
    behind: dict[str, dict] = {}
    for authored in overlay.sections:
        document = available.get(authored.key) or _shared(authored, material)
        if document is None:
            raise ContentError(
                f"the overlay names a section {authored.key!r} and no ingested "
                f"document stands behind it; a section with no material would "
                f"render as an ungraded exercise and read as one the corpus meant"
            )
        behind[authored.key] = document
    return behind


def _shared(authored: Section, material: Material) -> dict | None:
    """Return the document behind a `shared` section: the unit's first lesson.

    ⚠️ Stated rather than assumed: `shared` has no variant in its key, so it
    cannot be matched by the derivation above, and the material behind it is
    the unit's first lesson.
    """
    if authored.kind != "shared":
        return None
    lessons = material.of_kind("lesson")
    return lessons[0] if lessons else None
