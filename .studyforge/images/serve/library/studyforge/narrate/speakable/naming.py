r"""⛔ The one minter: what a spoken unit is called, and what its clip file is called.

**What it does.** Mints the speech id from a unit's logical address and a block's
position, and mints the clip filename from that id and the words that are said.

**How you use it.** `unit_token(unit_key)` flattens `<address>/unit-NN` into one
filename component; `speech_id(...)` names a spoken unit; `clip_name(unit)` names
its audio file; `parse_clip_name(name)` is that last one's inverse.

**Depends on.** `hashlib`, `studyforge.address` for what a slug is, and this
package's `records`. ⛔ Not the walker, not `render`, not `corpus.placement` —
where a clip *lands* is a placement decision (R4) and this module mints a name,
never a path.

## ⛔ There is exactly ONE minter, and that is a property this module must keep

⚠️ **A second minter is a page asking for a file the placer never wrote, with no
symptom but silence.** ⛔ It is the specific way the extraction source broke this:
the synthesis runner was handed a bare speech id while the page asked for the
digest form, so every clip was missing and every run re-synthesised the lot.

⭐ **So `clip_name` takes a `SpeechUnit` and nothing else.** There is deliberately
no signature that accepts an id and a string separately: a caller cannot pair one
unit's id with another unit's words, because it never holds the two apart.
⛔ **Both the renderer that links a clip and the client that places it call this
one function**, and `test_naming.py` asserts over the whole tree that no second
module composes the name itself.

## ⛔ The id is positional; the filename is positional plus a digest

⭐ **They answer different questions and the filename carries both** (spec §8.2).
The **id** is what a *structure* edit must not renumber — retitling a section must
not orphan a unit's audio — so it comes from position and from the unit's logical
address, never from content. The **digest** is what a *text* edit must change: fix
a typo and the name changes, the page links a file that is not on disk, and the
client synthesises it. ⛔ A stale clip cannot be addressed, which needs no
discipline and survives a stage being run on its own.

## ⛔ `Q23`: neither half of the name comes from the source path

⚠️ **Seventeen units can share one `origin.path`.** ⭐ The id comes from the unit's
**logical address** and the digest from the **spoken text**, so those
seventeen units key to seventeen distinct clips by construction, with no corpus
knowledge anywhere (R1). ⛔ `origin` is provenance, and provenance is never
identity.

## ⭐ Why `--` flattens the address, and why that is injective rather than merely tidy

⛔ **A unit key is `<slug>/<slug>/…/unit-NN`, and a filename component may carry no
`/`** — so the separator has to become something. ⚠️ **Replacing it with a single
hyphen would NOT be injective**: `a-b/c` and `a/b-c` both flatten to `a-b-c`, which
is precisely the ASCII-collision `unit.sections` refuses to create by removing
slugification rather than by checking for it.

⭐ **A doubled hyphen cannot appear inside a slug** — `address.slug_fault` names
*"whose hyphens are doubled somewhere inside it"* as one of the three ways to fail
`is_slug` — so `--` is a separator the segments provably cannot contain, every
`--` in a token came from a `/`, and `unit_key_of` is a true inverse. ⛔ The
property is only as good as the check, so `unit_token` **refuses** a key whose
segments are not slugs rather than flattening it anyway.
"""

from __future__ import annotations

import hashlib

from studyforge.address import SEPARATOR, is_slug
from studyforge.narrate.speakable.records import SpeakableError, SpeechUnit

#: What the address separator becomes inside one filename component. ⛔ Two
#: characters, not one, and the module contract is why.
UNIT_SEPARATOR = "--"

#: What joins the unit, the section and the block path into an id. ⭐ Safe as a
#: separator because no slug carries a dot: `slugify` turns one into a hyphen.
SEGMENT = "."

#: The letter that marks a block's position in an id.
BLOCK = "b"

#: `block type -> the letter that marks one of its parts`. ⛔ A list's items and a
#: table's rows are the only two things addressed below block level, and the
#: grammar for both lives here so the walker never spells a letter itself.
SUB_MARKER = {"list": "i", "table": "r"}

#: How many hex characters of the digest the filename carries (spec §8.2).
DIGEST_LENGTH = 8

#: What joins the id to the digest in a clip's filename (spec §8.2).
DIGEST_JOIN = "-"


def unit_token(unit_key: str) -> str:
    """Return `unit_key` as ONE filename component, reversibly.

    ⛔ Refuses a key whose segments are not slugs, because the flattening is only
    injective over slugs — see this module's contract. The refusal names the
    position that failed and never reproduces the value, which may be a path (R7).
    """
    if not isinstance(unit_key, str) or not unit_key:
        raise SpeakableError("a unit key must be a non-empty str")
    segments = unit_key.split(SEPARATOR)
    for position, segment in enumerate(segments, start=1):
        if not is_slug(segment):
            raise SpeakableError(
                f"segment {position} of {len(segments)} in this unit key is not a slug, so the "
                f"address cannot be flattened into a filename component without two distinct "
                f"units colliding on one clip name"
            )
    return UNIT_SEPARATOR.join(segments)


def unit_key_of(token: str) -> str:
    """Return the `<address>/unit-NN` key a token was flattened from.

    ⭐ A true inverse of `unit_token`, because no slug contains a doubled hyphen —
    so every `--` in a token marks a separator and nothing else.
    """
    if not isinstance(token, str) or not token:
        raise SpeakableError("a unit token must be a non-empty str")
    return SEPARATOR.join(token.split(UNIT_SEPARATOR))


def speech_id(
    unit: str,
    section: str,
    block_path: tuple[int, ...],
    sub_index: int | None = None,
    sub_of: str | None = None,
) -> str:
    """Return the stable name of one spoken unit.

    `unit` is a flattened unit token, `section` a served section's key,
    `block_path` the 0-based indices from the section down, and `sub_index` the
    0-based item or row inside the block `sub_of` names. Every positional half is
    written 1-based, which is the only place the two bases meet.
    """
    if not block_path:
        raise SpeakableError("a speech id needs at least one block position, and got none")
    parts = [unit, section, *(f"{BLOCK}{index + 1}" for index in block_path)]
    if sub_index is not None:
        marker = SUB_MARKER.get(sub_of or "")
        if marker is None:
            raise SpeakableError(
                f"only {sorted(SUB_MARKER)} address anything below block level, so a sub-position "
                f"cannot be named for a block of this type"
            )
        parts.append(f"{marker}{sub_index + 1}")
    return SEGMENT.join(parts)


def digest_of(spoken: str) -> str:
    """Return the first `DIGEST_LENGTH` hex characters of the spoken text's sha256."""
    return hashlib.sha256((spoken or "").encode("utf-8")).hexdigest()[:DIGEST_LENGTH]


def clip_name(unit: SpeechUnit) -> str:
    """Return what `unit`'s audio file is called — ⛔ the ONE minter (spec §8.2).

    ⚠️ Takes the whole record rather than an id and a string, so no caller can
    pair one unit's name with another unit's words. It is a filename **component**
    and carries no extension and no directory: what a clip is stored as, and where,
    are the synthesis pass's and placement's answers (R4).
    """
    if not isinstance(unit, SpeechUnit):
        raise SpeakableError(
            "a clip is named from a whole SpeechUnit, so that an id can never be paired "
            "with words that are not its own"
        )
    return f"{unit.id}{DIGEST_JOIN}{digest_of(unit.speak)}"


def parse_clip_name(name: str) -> tuple[str, str]:
    """Return the `(speech id, digest)` a clip name carries, or raise.

    ⭐ Exists so a placer or a reconciliation pass can tell which unit a file on
    disk belongs to **without** re-deriving the name — which would be the second
    minter this module refuses to allow.
    """
    if not isinstance(name, str) or not name:
        raise SpeakableError("a clip name must be a non-empty str")
    identifier, separator, digest = name.rpartition(DIGEST_JOIN)
    if not separator or not identifier or len(digest) != DIGEST_LENGTH:
        raise SpeakableError(
            f"a clip name is '<speech id>{DIGEST_JOIN}<{DIGEST_LENGTH} hex>', and this one "
            f"is not that shape"
        )
    if any(character not in "0123456789abcdef" for character in digest):
        raise SpeakableError(
            f"a clip name's last {DIGEST_LENGTH} characters are lowercase hex, and these are not"
        )
    return identifier, digest
