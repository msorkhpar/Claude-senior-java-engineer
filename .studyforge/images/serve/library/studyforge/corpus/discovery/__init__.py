"""Discovery: find artifacts under a root, and assemble a site from what each says it is.

**What it does.** Scans a source root for `*.unit.html` and `*.section.html`,
reads the identity block inside each one, and assembles a site from what it
finds — never from where the files sit. It also owns `site.json`, the written
record of that scan.

**How you use it.**

    from studyforge.corpus.discovery import assemble, cache_path

    found = assemble(root, {manifest.source: manifest.depth},
                     cache=cache_path(root, profile_for(manifest.placement)))
    found.site.unit(manifest.source, address, 7)   # by identity, never by path
    found.report                                   # R6, every line of it

**Depends on.** `studyforge.corpus.placement` for the page suffixes and the
identity block, `studyforge.archive.scrub` for the R7 gate, and
`studyforge.version` for R9. ⛔ Not on `render`, `serve` or any adapter (R1).

## ⛔ R4: the framework never infers what a file is from where it sits

⭐ **This package is that ruling's mechanism**, and the reason a corpus can be
told *"artifacts go wherever suits the material"*. A moved page is still
correctly identified. A renamed page is still found. Two corpora with different
placement profiles are found by one scan, because the scan never learns that
placement profiles exist.

⚠️ **Identity survives a move; presentation need not, and that is not a
defect** (R8). A moved page still identifies itself and still appears in the
contents, and it renders unstyled and silent, because its stylesheet and its
audio resolve **relative to the page**. ⛔ Conflating the two would force either
absolute asset paths, breaking the `file://` floor, or a fixed tree, breaking
placement.

## ⛔ `site.json` is a cache of the scan and never the authority

⭐ **Both halves of §5's clause are here and they are different mechanisms:**

- *"the scan wins"* is **`assemble`**: the scan runs every time and its result
  is what is returned. ⛔ No branch hands back a cached site.
- *"a stale cache is detectable"* is **`freshness`**: a content digest recorded
  in the file, and three verdicts. ⛔ **No `mtime` anywhere**.

⛔ **`site_api` is neither of them.** It is R9's schema version for this
contract, minted in `cache.py` and registered in `version.CONTRACT_FIELDS` in
the same commit. An unsupported or absent one **does not raise**: the cache is
not read, the scan runs, the file is rewritten at the current version, and all
of it is reported. See `cache.py`, which carries the ruling in full.

## What is in the package

| Module | Owns |
|---|---|
| `scan` | the walk, and identity-from-content — ⛔ R4's mechanism itself |
| `site` | what a scan found, and the lookups that go through identity |
| `cache` | `site.json` — its bytes, its read, `site_api`, and the one writer |
| `freshness` | the content digest and the three verdicts |
| `assemble` | the one call a consumer makes, and the report R6 requires |
| `errors` | `DiscoveryError`, the only exception any of it raises |
"""

from __future__ import annotations

from studyforge.corpus.discovery.assemble import Discovery, assemble
from studyforge.corpus.discovery.cache import (
    KNOWN_SITE_API,
    SITE_API,
    SITE_KEYS,
    Cached,
    cache_path,
)
from studyforge.corpus.discovery.errors import DiscoveryError
from studyforge.corpus.discovery.freshness import (
    FRESH,
    STALE,
    UNVERIFIABLE,
    VERDICTS,
    freshness,
    scan_sha256,
)
from studyforge.corpus.discovery.scan import PAGE_SUFFIXES, pages, scan
from studyforge.corpus.discovery.site import Artifact, Site, Unidentified

#: ⛔ The package's whole public surface.
__all__ = [
    "FRESH",
    "KNOWN_SITE_API",
    "PAGE_SUFFIXES",
    "SITE_API",
    "SITE_KEYS",
    "STALE",
    "UNVERIFIABLE",
    "VERDICTS",
    "Artifact",
    "Cached",
    "Discovery",
    "DiscoveryError",
    "Site",
    "Unidentified",
    "assemble",
    "cache_path",
    "freshness",
    "pages",
    "scan",
    "scan_sha256",
]
