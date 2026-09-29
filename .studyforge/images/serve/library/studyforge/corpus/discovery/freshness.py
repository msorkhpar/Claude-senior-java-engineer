"""⛔ The staleness signal — and it is not `site_api`.

**What it does.** Digests what a scan found, and turns that digest and the one
a cache recorded into one of three verdicts.

**How you use it.** `scan_sha256(site)` for the digest, `freshness(site,
recorded)` for the verdict.

**Depends on.** `hashlib`, `json`, and this package's `site`. ⛔ **Deliberately
not on `cache`**: the verdict is a comparison of two digests and needs no
opinion about the file one of them came out of, and a dependency in that
direction would be a cycle the moment `cache` records a digest — which it does.

## ⛔ A stale cache is DETECTABLE rather than silently wrong

**§5, and it is the clause this module exists for.** The other half —
*"the scan wins"* — is structural and lives in `assemble`: the scan runs every
time and its result is what is returned, so there is no code path on which a
cache is believed over the tree. ⭐ **This module's job is the narrower one: to
be able to say that the file on disk no longer matches.**

## ⛔ The signal is content, never a clock and never the filesystem

⚠️ **`mtime` is not used here and must not be added.** An mtime verdict and a
content verdict can disagree **in opposite directions at once**; an mtime
verdict can flip while nothing in the tree has moved; and ⛔ **a fresh checkout
or `git worktree add` resets every mtime**, so an mtime signal fails in every
new checkout.

⭐ **So the right signal is put in the file.** `scan_sha256` is a digest of the
scan's own findings — every artifact's path and identity, and every page that
could not be identified — and a cache carries the digest of the scan it was
written from. Two runs over an unchanged tree produce the same digest on any
machine (R10); a page added, moved, renamed, re-identified or broken changes
it.

⛔ **What the digest deliberately does not cover: `site_api`.** Two signals, two
tests. Folding the version key in would make one number answer both questions,
so an old-but-accurate cache would read as *stale* and a current-but-wrong one
could read as *fresh* — and neither reading could be told from the other.

## ⭐ Three verdicts, because "I cannot answer" is not "the answer is no"

⚠️ An absent cache, an unreadable one and one declaring a `site_api` this build
does not speak are all states where the question **could not be put**. Calling
any of them `stale` is a verdict the evidence does not support, and R6's *fail
loud* means saying **which**, not guessing.
"""

from __future__ import annotations

import hashlib
import json

from studyforge.corpus.discovery.site import Site

#: The three verdicts. ⛔ Three rather than two, on purpose — see above.
FRESH = "fresh"
STALE = "stale"
UNVERIFIABLE = "unverifiable"
VERDICTS = (FRESH, STALE, UNVERIFIABLE)


def scan_sha256(site: Site) -> str:
    """Return a digest of everything one scan found.

    ⛔ **Over `Site.document`, which is already sorted and already excludes the
    version key**, and serialised with fixed separators and no re-sorting of
    keys — so the bytes digested are the bytes the cache holds, and one
    definition of the canonical form serves both (R10).
    """
    canonical = json.dumps(
        site.document, separators=(",", ":"), ensure_ascii=False, sort_keys=False
    )
    return hashlib.sha256(canonical.encode("utf-8")).hexdigest()


def freshness(site: Site, recorded: str | None) -> str:
    """`FRESH`, `STALE` or `UNVERIFIABLE` for a cache that recorded `recorded`.

    `recorded` is the digest the cache on disk carries, and it is `None`
    wherever the question cannot be put: there is no cache, it could not be
    read, or it declares a `site_api` this build does not speak. ⭐ **The caller
    reduces those three to `None` and reports which one it was** — this
    function never sees a file, so it can neither name one nor be tempted to
    guess between them.
    """
    if recorded is None:
        return UNVERIFIABLE
    return FRESH if recorded == scan_sha256(site) else STALE
