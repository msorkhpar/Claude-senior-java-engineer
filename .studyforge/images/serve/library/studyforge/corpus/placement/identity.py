"""The identity block every generated artifact embeds — R4's whole mechanism.

**What it does.** Defines what a generated page says about itself, renders it,
and reads it back.

**How you use it.** `render(Identity(...))` gives the markup the page renderer embeds;
`parse(html)` gives it back, which is what `corpus.discovery`'s scan does with every file it
finds.

**Depends on.** `studyforge.address`, `studyforge.version`, and `errors`.

## Why it is here and not in `corpus.discovery`

⭐ **The page renderer writes it; discovery reads it.** A definition arriving after
its first writer is a definition two tasks each guess at differently — so it is
defined once, here, with **both halves and the round-trip tested now**, a
milestone before anything depends on it.

## Location is data; identity is embedded

⛔ **The framework never infers what a file is from where it sits** (R4). A
page moved to another directory, or renamed, is still exactly the unit it says
it is: discovery reads this block, never the path.

⚠️ **Identity survives a move; presentation need not, and that is not a
defect.** A moved page still identifies itself, still appears in the contents,
still resolves as the unit it is — and renders unstyled and silent, because its
stylesheet and its audio resolve **relative to the page** (R8). ⛔ Conflating
the two would force either absolute asset paths, breaking the `file://` floor,
or a fixed tree, breaking placement. Both are true at once.

## The marker is a contract, not presentation

⛔ A scan must find this block in thousands of files without parsing HTML, so
the element, its `type` and its `id` are fixed here rather than chosen by a
renderer — which is the one place R13's "markup lives in template files" gives
way, and it gives way because this markup is **machine-read**, not shown. It is
an inline wrapper around JSON; a template file for it would remove no
duplication and would put the scan's contract somewhere the scanner cannot see.
"""

from __future__ import annotations

import json
import re
from dataclasses import dataclass

from studyforge.address import Address, AddressError, is_slug, require_ordinal, require_slug
from studyforge.archive.scrub import assert_clean
from studyforge.corpus.placement.errors import PlacementError
from studyforge.describe import describe, describe_keys
from studyforge.version import check

#: R9's version for this contract. ⛔ Refused when unknown, never migrated.
IDENTITY_API = 1
KNOWN_IDENTITY_API = frozenset({IDENTITY_API})

#: What a scan looks for. ⛔ Fixed here, not chosen by a renderer.
IDENTITY_ELEMENT_ID = "studyforge-identity"
IDENTITY_MIME = "application/json"

#: The one pattern `corpus.discovery` needs. ⚠️ Deliberately tolerant of attribute order
#: and whitespace, because it reads files this build may not have written.
IDENTITY_PATTERN = re.compile(
    r"<script[^>]*\bid=[\"']" + re.escape(IDENTITY_ELEMENT_ID) + r"[\"'][^>]*>(.*?)</script>",
    re.DOTALL,
)

#: The keys of the block, in the order they are written. ⛔ Fixed rather than
#: sorted: an unchanged page must re-render to identical bytes (R10).
IDENTITY_KEYS = ("identity_api", "corpus", "address", "unit", "kind", "variant")

#: What an artifact can be. ⚠️ `container` is a page about a container, not a
#: unit, and it carries no ordinal.
KINDS = ("unit", "container")


@dataclass(frozen=True, slots=True)
class Identity:
    """What one generated artifact says it is, wherever it ends up."""

    corpus: str
    address: Address
    variant: str
    kind: str = "unit"
    unit: int | None = None

    def __post_init__(self) -> None:
        """Refuse an identity that could not be read back the way it was written."""
        require_slug(self.corpus, "identity 'corpus'")
        require_slug(self.variant, "identity 'variant'")
        if self.kind not in KINDS:
            raise PlacementError(
                f"identity 'kind' must be one of {list(KINDS)}, got {describe(self.kind)}"
            )
        if self.kind == "unit":
            require_ordinal(self.unit, "identity 'unit'")
        elif self.unit is not None:
            raise PlacementError("a container page addresses no unit; 'unit' must be absent")

    @property
    def document(self) -> dict:
        """The block as an object, in `IDENTITY_KEYS` order."""
        written = {
            "identity_api": IDENTITY_API,
            "corpus": self.corpus,
            "address": list(self.address.segments),
            "unit": self.unit,
            "kind": self.kind,
            "variant": self.variant,
        }
        return {key: written[key] for key in IDENTITY_KEYS if written[key] is not None}


def render(identity: Identity) -> str:
    """Return the markup a generated artifact embeds, on one line.

    ⛔ One line and `separators` fixed, because pages are compared byte for
    byte (R10) and because a scan that had to tolerate reflowed JSON would be
    a scan tolerating anything.
    """
    payload = json.dumps(identity.document, separators=(",", ":"), ensure_ascii=False)
    return f'<script type="{IDENTITY_MIME}" id="{IDENTITY_ELEMENT_ID}">{payload}</script>'


def parse(html: str, depth: int, where: str = "artifact") -> Identity:
    """Return the `Identity` embedded in `html`, or raise naming the file.

    `depth` is the corpus's declared depth, supplied so the address is checked
    against it — `studyforge.address` owns that comparison and this module does not restate
    it.

    ⛔ An artifact with no identity block is **reported by name**, never
    skipped silently (R6): a page that quietly vanishes from a site is the
    failure discovery exists to make impossible.
    """
    if not isinstance(html, str):
        raise PlacementError(f"{where} is not text, so it carries no identity")
    found = IDENTITY_PATTERN.search(html)
    if found is None:
        raise PlacementError(
            f"{where} carries no identity block; every generated artifact embeds one,"
            f" and a file without one is reported rather than skipped"
        )
    return from_document(_loads(found.group(1), where), depth, where)


def from_document(document: object, depth: int, where: str = "artifact") -> Identity:
    """Build an `Identity` from an already-parsed block.

    ⛔ **Gated like every other document reader**. ⚠️ It is tempting to
    argue this one is exempt because the framework wrote the block it reads —
    but `parse`'s own contract says it reads *"files this build may not have
    written"*, which is the whole reason `IDENTITY_PATTERN` tolerates
    attribute order. ⭐ A reader whose exemption rests on an assumption its
    neighbour explicitly refuses is not exempt.
    """
    if not isinstance(document, dict):
        raise PlacementError(f"{where}'s identity block is not an object")
    # ⛔ **`PersonalDataLeak` travels through as itself, not translated**
    # (R7). `PlacementError` exists so a caller sweeping a
    # site catches one type per artifact and carries on; an R7 refusal inside
    # that family would be logged as one more file that could not be placed,
    # and the leak would be the thing nobody looked at. ⭐ See
    # `placement/errors.py`, which names what crosses this contract.
    assert_clean(document, where)
    check(
        "identity_api",
        document.get("identity_api"),
        KNOWN_IDENTITY_API,
        where=where,
        error=PlacementError,
    )
    unknown = sorted(set(document) - set(IDENTITY_KEYS))
    if unknown:
        raise PlacementError(
            f"{where}'s identity block has unknown key(s), {describe_keys(unknown)}"
        )
    return Identity(
        corpus=_str_of(document.get("corpus"), "corpus", where),
        address=_address_of(document.get("address"), depth, where),
        variant=_str_of(document.get("variant"), "variant", where),
        kind=document.get("kind", "unit"),
        unit=document.get("unit"),
    )


def _address_of(segments: object, depth: int, where: str) -> Address:
    """Return the recorded address, built from the list the block stores.

    ⛔ **Built from the segments, never from a joined key.** Joining first let
    a `TypeError` escape on three malformed shapes — a list of ints, a list
    with a `None` in it, a nested list — and this function is on the path `corpus.discovery`
    walks over **every file in a site**. A caller reading a thousand artifacts
    must be able to catch one type.

    ⚠️ **Reading a document raises `PlacementError`, including for arity.**
    `studyforge.address` still owns what a slug is and what an address of the wrong depth is;
    what changes is the front door. The delegation belongs where a *caller asks
    a question* — `Manifest.parse_key` — not where this package *reads a file*.

    ⛔ **And the refusal names the position, never the segment** (R7).
    `studyforge.address`'s own message echoes the value, which is right when a
    caller passed a literal and wrong here: this reads a file somebody else
    wrote, so the offending segment can be an absolute path — and this function
    runs over
    **every artifact in a site**, into a log.
    """
    if not isinstance(segments, list):
        raise PlacementError(f"{where}'s identity block has no 'address' list")
    if not segments:
        raise PlacementError(f"{where}'s identity block has an empty 'address'")
    for position, segment in enumerate(segments, start=1):
        if not isinstance(segment, str):
            raise PlacementError(
                f"{where}'s identity block: address segment {position} of "
                f"{len(segments)} is a {type(segment).__name__}, not a slug"
            )
        if not is_slug(segment):
            raise PlacementError(
                f"{where}'s identity block: address segment {position} of "
                f"{len(segments)} is not a slug. It is recorded, never derived (§6), "
                f"so an adapter slugifies and records the result"
            )
    try:
        return Address(tuple(segments)).require_depth(depth)
    except AddressError as error:
        # Arity only — every segment is already known to be a slug, and this
        # message carries counts rather than content.
        raise PlacementError(f"{where}'s identity block: {error}") from None


def _loads(text: str, where: str) -> object:
    """Parse the block's JSON, or refuse naming the file and not the payload."""
    try:
        return json.loads(text)
    except TypeError, ValueError:
        # ⛔ The payload is never echoed: a refusal that quotes an unreadable
        # block has only relocated whatever it held into a log (R7).
        raise PlacementError(f"{where}'s identity block is not valid JSON") from None


def _str_of(value: object, key: str, where: str) -> str:
    """One required string field of the block."""
    if not isinstance(value, str) or not value:
        raise PlacementError(f"{where}'s identity block has no usable {key!r}")
    return value
