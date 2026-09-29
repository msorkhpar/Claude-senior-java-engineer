"""`toc.json` — the stable half, its bytes, its reader, and `toc_api`.

**What it does.** Renders a `Contents` to the document a consumer caches, reads
one back to the same value, and mints the version key R9 gives this contract.

**How you use it.** `render(contents)` for the bytes, `write(path, contents)`
to put them somewhere, `parse(text, where)` / `load(path)` to read one back,
and `digest(contents)` for the fingerprint the local document annotates.

**Depends on.** `studyforge.address` for a unit key's inverse,
`studyforge.archive.scrub` for R7's gate, `studyforge.version` for R9's, and
this package's `entries`, `errors` and `writing`.

## R21: this contract is located, versioned and produced by exactly one party

| | |
|---|---|
| **File** | `toc.json` — `TOC_FILENAME` |
| **Version key** | `toc_api` — ⭐ **minted here**, and registered in `version.CONTRACT_FIELDS` |
| **Producer** | ⛔ **`generate`. The *local* document that annotates it carries the same key** |

⚠️ **One version key covers both documents, and that is deliberate.** The
spec's register calls it *"the TOC schema version"* for `toc.json` and for
`status.json` alike: they are two halves of one schema, and a build that spoke
one half and not the other could join a pair it does not understand.

## ⛔ An unknown `toc_api` is refused, never migrated (R9)

⭐ **Refused, unlike the discovery cache.** `site.json` is derived from a scan
that can simply be re-run, so it may be discarded and rebuilt. This
document is not that: a consumer holding a `toc.json` it cannot read has no
scan to fall back on and no way to tell a shape it does not speak from a shape
that means something different. ⛔ So this uses `version.check`, which raises,
and the exception is this package's own.

## ⛔ Byte-for-byte, and there is no clock in it (R10)

`TOC_KEYS` is a fixed tuple rather than a sort, `indent=2` and
`ensure_ascii=False` are stated, and the document carries no capture date —
§6 gives the *archive* that exemption because a capture date answers *"how
stale is this?"*, and here the same question is answered by `digest` against
the document itself.
"""

from __future__ import annotations

import hashlib
import json
from pathlib import Path, PurePosixPath

from studyforge.address import parse_unit_key
from studyforge.archive.scrub import assert_clean
from studyforge.contents.entries import (
    ENTRY_KEYS,
    GROUP_CHILD_KEY,
    GROUP_KEYS,
    UNIT_CHILD_KEY,
    Contents,
    Entry,
    Group,
)
from studyforge.contents.errors import ContentsError
from studyforge.contents.writing import write as write_text
from studyforge.describe import describe, describe_keys
from studyforge.version import check as check_version

#: R9's key for this contract. ⭐ Registered in
#: `version.CONTRACT_FIELDS` in the commit that mints it.
TOC_API = 1
KNOWN_TOC_API = frozenset({TOC_API})

#: What the stable document is called wherever it is written.
TOC_FILENAME = "toc.json"

#: The keys of the document, in the order they are written. ⛔ Fixed rather
#: than sorted: an unchanged corpus must re-render to identical bytes (R10).
TOC_KEYS = ("toc_api", "corpus", "title", "levels", "contents")


def to_document(contents: Contents) -> dict:
    """Return the stable contents as an object, in `TOC_KEYS` order."""
    written = {
        "toc_api": TOC_API,
        "corpus": contents.corpus,
        "title": contents.title,
        "levels": list(contents.levels),
        "contents": contents.document,
    }
    return {key: written[key] for key in TOC_KEYS}


def render(contents: Contents) -> str:
    """Return the document's bytes — identical on every machine for one corpus (R10)."""
    return json.dumps(to_document(contents), indent=2, ensure_ascii=False, sort_keys=False) + "\n"


def digest(contents: Contents) -> str:
    """Return the fingerprint the local document annotates, over this document's bytes.

    ⭐ Over the **rendered text**, not over the value: what a consumer holds is
    the file, so what a staleness check compares has to be the thing the
    consumer can compute for itself from what it holds.
    """
    return hashlib.sha256(render(contents).encode("utf-8")).hexdigest()


def write(path: Path | str, contents: Contents) -> None:
    """Write `toc.json`, staged and moved into place. ⛔ The one writer of it."""
    write_text(path, render(contents))


def parse(text: str, where: str = TOC_FILENAME) -> Contents:
    """Build a `Contents` from the text of a `toc.json`."""
    try:
        document = json.loads(text)
    except (TypeError, json.JSONDecodeError) as exc:
        raise ContentsError(f"{where} is not valid JSON: {exc}") from None
    if not isinstance(document, dict):
        raise ContentsError(f"{where} must be a JSON object, got {describe(document)}")
    return from_document(document, where)


def load(path: Path | str) -> Contents:
    """Read, parse and version-check one `toc.json`.

    ⛔ `exc.strerror`, never `exc`: an `OSError` formats itself with the
    filename it was given, and a refusal is read in a log and pasted into a
    bug report. An absolute path in one carries a home directory (R7).
    """
    path = Path(path)
    try:
        text = path.read_text(encoding="utf-8")
    except OSError as exc:
        reason = exc.strerror or exc.__class__.__name__
        raise ContentsError(f"cannot read {path.name}: {reason}") from None
    except UnicodeDecodeError:
        raise ContentsError(f"{path.name} is not UTF-8 text") from None
    return parse(text, path.name)


def from_document(document: dict, where: str = TOC_FILENAME) -> Contents:
    """Build a `Contents` from a decoded `toc.json`, refusing anything unexpected."""
    check_version(
        "toc_api", document.get("toc_api"), KNOWN_TOC_API, where=where, error=ContentsError
    )
    # ⛔ The gate runs over the whole decoded document (R7). It is generated,
    # but it is generated into a repository somebody clones, and a title or a
    # path that arrived from a person's machine is exactly what it is for.
    assert_clean(document, where)
    unknown = sorted(set(document) - set(TOC_KEYS))
    if unknown:
        raise ContentsError(
            f"{where} carries unknown key(s), {describe_keys(unknown)}; a table of "
            f"contents is "
            f"{list(TOC_KEYS)}"
        )
    levels = _levels(document.get("levels"), where)
    return Contents(
        corpus=_text(document.get("corpus"), "corpus", where),
        title=_text(document.get("title"), "title", where),
        levels=levels,
        groups=tuple(
            _group(entry, levels, 0, where) for entry in _listed(document.get("contents"), where)
        ),
    )


def _group(value: object, levels: tuple[str, ...], depth: int, where: str) -> Group:
    """One group and everything under it, checked against the declared depth."""
    if not isinstance(value, dict):
        raise ContentsError(f"{where} has a group that is {describe(value)}, not an object")
    unknown = sorted(set(value) - set(GROUP_KEYS) - {GROUP_CHILD_KEY, UNIT_CHILD_KEY})
    if unknown:
        raise ContentsError(
            f"{where} has a group carrying unknown key(s), {describe_keys(unknown)}"
        )
    shared = {key: _text(value.get(key), key, where) for key in GROUP_KEYS}
    if depth + 1 < len(levels):
        return Group(
            **shared,
            groups=tuple(
                _group(child, levels, depth + 1, where)
                for child in _listed(value.get(GROUP_CHILD_KEY), where)
            ),
        )
    return Group(
        **shared,
        entries=tuple(
            _entry(child, levels, where) for child in _listed(value.get(UNIT_CHILD_KEY), where)
        ),
    )


def _entry(value: object, levels: tuple[str, ...], where: str) -> Entry:
    """One unit's entry, with its address read back through `Address`'s own inverse."""
    if not isinstance(value, dict):
        raise ContentsError(f"{where} has a unit entry that is {describe(value)}, not an object")
    unknown = sorted(set(value) - set(ENTRY_KEYS))
    if unknown:
        raise ContentsError(
            f"{where} has a unit entry carrying unknown key(s), {describe_keys(unknown)}"
        )
    try:
        address, ordinal = parse_unit_key(_text(value.get("key"), "key", where), len(levels))
    except ValueError as error:
        raise ContentsError(f"{where} has a unit entry whose key is unreadable: {error}") from None
    return Entry(
        address=address,
        ordinal=ordinal,
        title=_text(value.get("title"), "title", where),
        numbering=_text(value.get("numbering"), "numbering", where),
        page=PurePosixPath(_text(value.get("page"), "page", where)),
        practices=_count(value.get("practices"), "practices", where),
    )


def _levels(value: object, where: str) -> tuple[str, ...]:
    """Return the corpus's level labels — at least one, because depth 1 is common."""
    if not isinstance(value, list) or not value:
        raise ContentsError(
            f"{where} must declare a non-empty 'levels' list; the contents' depth is "
            f"len(levels) and a document that did not say could not be read back"
        )
    return tuple(_text(level, "level", where) for level in value)


def _listed(value: object, where: str) -> list:
    """Return a list of children, or refuse. ⛔ Absent is refused, never read as empty."""
    if not isinstance(value, list):
        raise ContentsError(
            f"{where} has a group whose children are {describe(value)}, not a list; "
            f"a missing list read as an empty one is a contents document that is "
            f"short and says nothing"
        )
    return value


def _text(value: object, what: str, where: str) -> str:
    """Return a required non-empty string. ⛔ Described, never echoed (R7)."""
    if not isinstance(value, str) or not value:
        raise ContentsError(f"{where} needs a non-empty {what}, and has {describe(value)}")
    return value


def _count(value: object, what: str, where: str) -> int:
    """Return a required count. ⛔ `bool` is refused for the reason `version` gives."""
    if not isinstance(value, int) or isinstance(value, bool) or value < 0:
        raise ContentsError(f"{where} needs {what} as a count of 0 or more, got {describe(value)}")
    return value
