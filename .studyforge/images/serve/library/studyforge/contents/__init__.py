"""The table of contents and local status, as data rather than as markup.

**What it does.** Produces the corpus's structure — containers, units, order,
and how far the reader has got — as a versioned document. The renderer, the
server and the page's own script are all consumers of it.

**How you use it.**

    from studyforge import contents as toc

    built = toc.build(manifest, containers)   # the stable half, from declarations
    toc.render(built)                         # toc.json's bytes
    toc.order(built)                          # every unit, in reading order
    toc.links(built, key)                     # {'previous': {'href', 'label'}, ...}

    local = toc.status(built, toc.found(site, built.corpus), read=marks)
    toc.render_status(local)                  # status.json's bytes
    toc.join(built, local)                    # refuses a stale pair
    toc.missing(built, local)                 # declared units this machine lacks

**Depends on.** `address`, `corpus` — its `manifest`, `container`, `placement`
and `discovery` — plus `archive.scrub` for R7's gate and `version` for R9's.
⛔ **Not on `render`** — the direction is renderer-depends-on-contents, never
the reverse — and `links` returns plain strings so that stays true (R13).

⚠️ **Structure and status have different lifetimes** and are versioned
separately so a consumer can cache the stable half. Structure changes when the
corpus is rebuilt; status changes every time the reader finishes a section.

## ⛔ The stable half is built from what the corpus DECLARES

⭐ **`build` reads container maps, never a scan.** A contents document sourced
from what exists on disk would omit a declared unit nobody has generated yet,
be internally consistent, and raise nothing — the short parse with no symptom.
The declaration is the count; `status` says which of those units this machine
has, and `missing` names the rest (R6).

## ⛔ This package computes the reading order, and it computes it once

⚠️ **It is the first thing in the project that does.** The order is a
depth-first walk of the tree, and the tree's own order is a function of the
**addresses** — siblings by segment, in code-point order, units by the ordinals
the container reader has already checked are contiguous from 1. ⛔ Nothing here
consults a filesystem, a clock, or the order a caller happened to enumerate
anything in (R10), and no flattened copy of the order is stored beside the tree.

⭐ **A corpus therefore gets alphabetical sibling order unless it puts the order
into its segments** — `01-getting-started` before `02-going-further`, which is
what real material does. ⛔ Inferring one from anything else would be the
framework knowing something about a source (R1).

## What is in the package

| Module | Owns |
|---|---|
| `entries` | `Contents`, `Group`, `Entry` — the value, and how each is written |
| `tree` | `build` — container maps in, one hierarchy out, and the only orderer |
| `order` | the reading-order walk, `neighbours`, and `links` per asking page |
| `document` | `toc.json` — its bytes, its reader, `toc_api`, and `digest` |
| `status` | `status.json` — the local half, the checked join, and `missing` |
| `writing` | the staged write both documents reach the disk through |
| `errors` | `ContentsError`, the only exception any of it raises |
"""

from __future__ import annotations

from studyforge.contents.document import (
    KNOWN_TOC_API,
    TOC_API,
    TOC_FILENAME,
    TOC_KEYS,
    digest,
    from_document,
    load,
    parse,
    render,
    to_document,
    write,
)
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
from studyforge.contents.order import (
    LINK_FIELDS,
    LINK_KEYS,
    Neighbours,
    links,
    neighbours,
    order,
    positions,
)
from studyforge.contents.status import (
    STATUS_FILENAME,
    STATUS_KEYS,
    UNIT_STATUS_KEYS,
    LocalStatus,
    UnitStatus,
    found,
    from_status_document,
    join,
    load_status,
    missing,
    parse_status,
    render_status,
    status,
    status_document,
    write_status,
)
from studyforge.contents.tree import build

#: ⛔ The package's whole public surface. A consumer that has to import
#: `studyforge.contents.order` directly is a consumer this contract failed —
#: a package's `__init__.py` is its contract (R17).
__all__ = [
    "ENTRY_KEYS",
    "GROUP_CHILD_KEY",
    "GROUP_KEYS",
    "KNOWN_TOC_API",
    "LINK_FIELDS",
    "LINK_KEYS",
    "STATUS_FILENAME",
    "STATUS_KEYS",
    "TOC_API",
    "TOC_FILENAME",
    "TOC_KEYS",
    "UNIT_CHILD_KEY",
    "UNIT_STATUS_KEYS",
    "Contents",
    "ContentsError",
    "Entry",
    "Group",
    "LocalStatus",
    "Neighbours",
    "UnitStatus",
    "build",
    "digest",
    "found",
    "from_document",
    "from_status_document",
    "join",
    "links",
    "load",
    "load_status",
    "missing",
    "neighbours",
    "order",
    "parse",
    "parse_status",
    "positions",
    "render",
    "render_status",
    "status",
    "status_document",
    "to_document",
    "write",
    "write_status",
]
