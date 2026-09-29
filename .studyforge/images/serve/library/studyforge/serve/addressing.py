r"""Addressing: an N-segment unit address inside a URL, for one corpus among several.

**What it does.** Reads `<corpus>/<segment>/…/<segment>/unit-NN` — the corpus's
`source`, then exactly as many address segments as **that** corpus's manifest
declares levels, then the unit — into a `Located`, and refuses every other
spelling. `CorporaContent` is the content namespace's `ContentSource` over every
corpus one instance serves, keyed the same way, so content and state name a unit
by one string.

**How you use it.**

    located = locate("depth2-demo/basics/01-getting-started/unit-01", depths)
    located.corpus, located.address, located.ordinal
    located.key        # 'basics/01-getting-started/unit-01' — the corpus's own key
    source = CorporaContent({"depth2-demo": CorpusContent(corpus)}, depths)

**Depends on.** `address` for the parse and every key, `archive.scrub` for R7's
gate on each contents document before it is decoded, `contents` for that
document's name, `serve.routes.content` for the `ContentSource` seam, and
`serve.withheld` for what marks every quiz no file may carry.
⛔ No key is composed here: `Address.unit_key` spells it, and `parse_unit_key`
reads it.

## ⛔ The depth is the corpus's, found by the first segment

⭐ `depths` maps each served corpus to `len(levels)` from its own manifest, so a
depth-2 address sent to a depth-1 corpus has one segment too many **for that
corpus** and is not located — never truncated to fit, and never tried against
the other corpus, whose depth it happens to match.

## ⛔ One spelling per unit

`parse_unit_key` refuses every spelling `Address.unit_key` would not write
(`unit-1`, `unit-001`, a capital, a trailing separator), so a unit is reachable
under one URL only. ⚠️ **That refusal is the address package's and is not
re-checked here**: nothing a second check could catch reaches it. A unit reachable under two
spellings would have two strong validators for one document in the content namespace.

⚠️ **No URL segment is decoded.** A slug cannot hold `%`, so an encoded
separator fails the parse rather than manufacturing a segment.
"""

from __future__ import annotations

import json
from collections.abc import Mapping
from dataclasses import dataclass

from studyforge.address import SEPARATOR, Address, AddressError, parse_unit_key
from studyforge.archive.scrub import assert_clean
from studyforge.contents import TOC_FILENAME
from studyforge.serve.routes.content import ContentSource
from studyforge.serve.withheld import Marks, marks_of

#: The key of the multi-corpus contents document's one list.
CORPORA = "corpora"


@dataclass(frozen=True, slots=True)
class Located:
    """One unit of one served corpus, as a URL named it."""

    corpus: str
    address: Address
    ordinal: int

    @property
    def key(self) -> str:
        """Return the unit's key inside its corpus — `Address.unit_key`'s spelling."""
        return self.address.unit_key(self.ordinal)

    @property
    def path(self) -> str:
        """Return the corpus-qualified spelling `locate` reads back."""
        return f"{self.corpus}{SEPARATOR}{self.key}"


def locate(path: str, depths: Mapping[str, int]) -> Located | None:
    """Return the unit `path` names at its corpus's own depth, or `None`."""
    if not isinstance(path, str):
        return None
    corpus, separator, key = path.partition(SEPARATOR)
    depth = depths.get(corpus) if separator else None
    if depth is None:
        return None
    try:
        address, ordinal = parse_unit_key(key, depth)
    except AddressError:
        return None
    return Located(corpus, address, ordinal)


class CorporaContent:
    """A `ContentSource` over several corpora, each unit keyed `<corpus>/<its own key>`.

    ⚠️ **The contents document is `{"corpora": [{"corpus", "contents"}, …]}`**, sorted by
    corpus, whatever the count — one shape for one corpus and for two, so a consumer
    never branches on how many an instance happens to hold.
    """

    def __init__(self, sources: Mapping[str, ContentSource], depths: Mapping[str, int]) -> None:
        """Hold one source and one depth per corpus; refuse a corpus holding only one."""
        if set(sources) != set(depths):
            raise ValueError("every corpus served needs both a content source and a depth")
        self._sources = dict(sorted(sources.items()))
        self._depths = dict(depths)

    def toc(self) -> str:
        """Return every corpus's contents document, in one envelope-ready document."""
        listed = [
            {"corpus": name, "contents": _decoded(source.toc())}
            for name, source in self._sources.items()
        ]
        return json.dumps({CORPORA: listed}, indent=2, ensure_ascii=False) + "\n"

    def unit(self, key: str) -> str | None:
        """Return the unit document a corpus-qualified key names, or `None`."""
        located = locate(key, self._depths)
        return None if located is None else self._sources[located.corpus].unit(located.key)

    def declares(self, key: str) -> bool:
        """Say whether the corpus the key names declares that unit."""
        located = locate(key, self._depths)
        return located is not None and self._sources[located.corpus].declares(located.key)

    def withheld(self) -> Marks:
        """Return the marks of every quiz of every corpus served."""
        found = Marks()
        for source in self._sources.values():
            found |= marks_of(source)
        return found


def _decoded(text: str) -> object:
    """Gate one corpus's contents document, then decode it. ⛔ Never the other order."""
    assert_clean(text, TOC_FILENAME)
    return json.loads(text)
