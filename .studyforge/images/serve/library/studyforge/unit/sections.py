"""What a section is called — derived from kind and variant, never from a heading.

**What it does.** Owns the section-key vocabulary every downstream consumer
keys off: `shared`, `<variant>`, `practice-<variant>`.

**How you use it.** `section_key("lang", "java")` → `"java"`;
`derived_section_key("java", "lesson", 0)` for a unit with no overlay.

**Depends on.** `studyforge.address` for what a slug is, and `errors`.

## Why a key never comes from a heading

⛔ **A retitled section must not renumber every speech id beneath it, because
those ids name audio files on disk.** A content-derived key would make a
copy-edit silently orphan a unit's narration: the page would ask for clips that
no longer exist, the clips that do exist would belong to nothing, and neither
half would fail. So the key comes from `kind` and `variant`, both of which are
structural, and an explicit `key` is the author's escape hatch.

## The key is required to be a slug — it is never *made* into one

⛔ **`require_slug`, never `slugify`**, and this is the ASCII-collision
question arriving inside this package rather than at an adapter's door. A
component which slugifies owes itself a collision check the
framework cannot make; the answer here is to remove the slugification instead.

⭐ **It costs nothing, because every input is already a slug.** `variants` are
validated as slugs by `corpus.manifest`, so deriving `<variant>` is the
*identity function* and cannot collide however the corpus spells its variants.
The only remaining input is an explicit `key`, and refusing a non-slug there is
`studyforge.address`'s own precedent: *a title passed where a slug is required raises; it does
not slugify for you*.

⚠️ **Contrast §6's rule for addresses — recorded, never derived.** A section key
is the opposite case and deliberately so: an *address* names something outside
this framework, whose real spelling was measured to differ from its title
**157 times in 1,290**, so it must be recorded; a *section key* names something
this framework mints, from two fields that are already canonical. Derived is
safe exactly where the derivation is total and injective.
"""

from __future__ import annotations

from studyforge.address import is_slug
from studyforge.unit.errors import ContentError, describe

#: A section is one of these. `shared` is the teaching every variant has in
#: common; `lang` is one variant's delta; `practice` is one variant's exercise.
#: ⚠️ `lang` keeps the extraction source's spelling because it is the word in
#: the authored files, and renaming a field an author types is a migration.
SECTION_KINDS = ("shared", "lang", "practice")

#: The section kinds that carry a `lang` field, and must.
#:
#: ⚠️ Named for the field rather than for "variant", and that is not cosmetic:
#: the manifest's tripwire — *no module-level collection derives a capability from a
#: variant name* — fired on the first spelling, `KINDS_WITH_A_LANG`. It was a false
#: positive by intent and a true positive by rule, so the constant moved rather
#: than the rule. It lists section KINDS, never variants, and nothing here maps
#: a variant to anything it can do.
KINDS_WITH_A_LANG = ("lang", "practice")

#: An archived document's kind -> the section kind it derives. ⛔ One mapping,
#: because the builder and whatever names a section's media must agree.
KIND_OF = {"lesson": "lang", "practice": "practice"}

#: The key a `shared` section always gets.
SHARED_KEY = "shared"

#: What a practice's key is prefixed with.
PRACTICE_PREFIX = "practice"


#: What a heading's anchor puts between its section's key and its position.
#: ⛔ Spelled here, beside the key it extends: the page mints the id from it
#: (`render.page.anchors.block_anchor`) and `unit.headings` links to it, so the two
#: cannot disagree about where a heading is.
BLOCK_INFIX = "-b"


def heading_anchor(key: str, position: int) -> str:
    """Return the id a page gives the block at `position` of the section keyed `key`.

    ⭐ Structural, never textual: `java`, 3 gives `java-b3`, whatever the
    heading says. ⚠️ Only a block of the section's own run has one; a heading
    nested in a quote is addressed by nothing.
    """
    return f"{key}{BLOCK_INFIX}{position}"


def heading_reference(key: str, position: int) -> str:
    """Return how the page's own prose links that heading: `java`, 3 gives `#java-b3`.

    ⛔ **The served document's one fragment composer.** This package may not
    reach for a renderer, so it cannot ask `render.markup.anchor`, and a served
    link to a heading of the page (`unit.headings`) is composed here and nowhere
    else in it. ⭐ It composes the same reference `anchor` does, of the id
    `heading_anchor` spells, and `tests/studyforge/render/markup/test_fragment.py`
    holds both to that.
    """
    return f"#{heading_anchor(key, position)}"


def section_key(kind: object, variant: object = None, key: object = None) -> str:
    """Return the stable key of one section.

    `key` is the author's escape hatch and wins when set; it must still be a
    legal slug, because a filename is minted from it.
    """
    if key is not None:
        return _legal(key, "an explicit section key")
    if kind == "shared":
        return SHARED_KEY
    if kind in KINDS_WITH_A_LANG:
        slug = _legal(variant, f"the variant of a {kind!r} section")
        return slug if kind == "lang" else f"{PRACTICE_PREFIX}-{slug}"
    raise ContentError(f"section kind must be one of {list(SECTION_KINDS)}, got {describe(kind)}")


def derived_section_key(variant: object, archive_kind: object, index: int) -> str:
    """Return the key a unit with **no overlay** gives its `index`-th archive file.

    `java`, `java-2`, `practice-java`, `practice-java-2`, …

    ⭐ The first file of a kind keys on the plain name, so the common case —
    one lesson and one practice — reads as it did before a second one existed.
    ⚠️ The position is the file's place among its own kind, **not its ordinal**:
    a capture that skips `lesson-2` must not leave a hole in the speech ids.
    """
    if archive_kind not in KIND_OF:
        raise ContentError(
            f"a section derives from an archive kind in {list(KIND_OF)}, "
            f"got {describe(archive_kind)}"
        )
    if not isinstance(index, int) or isinstance(index, bool) or index < 0:
        raise ContentError(
            f"a section's position among its kind must be 0 or more, got {describe(index)}"
        )
    key = section_key(KIND_OF[archive_kind], variant)
    return key if index == 0 else f"{key}-{index + 1}"


def _legal(value: object, what: str) -> str:
    """Return `value` if it is already a slug, or raise saying which field.

    ⛔ **`studyforge.address`'s rule, this package's message.** `require_slug` quotes the
    value it refuses, which is right where a caller passed a literal and wrong
    here: these values come from a file a person edits, so one of them can be
    an absolute path (R7).
    """
    if not isinstance(value, str) or not value:
        raise ContentError(f"{what} must be a non-empty str, got {describe(value)}")
    if not is_slug(value):
        raise ContentError(
            f"{what} must already be a slug — lowercase, hyphen-separated. It is not "
            f"slugified for you: a filename is minted from it, so a value that is not "
            f"slug-stable makes two sections collide on disk"
        )
    return value
