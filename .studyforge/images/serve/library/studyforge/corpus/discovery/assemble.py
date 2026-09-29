"""One call: scan the root, judge the cache, rewrite it when it is not fresh.

**What it does.** The whole of discovery as a consumer meets it — the scan, the
verdict on `site.json`, the rewrite, and the report that makes all of it
visible.

**How you use it.**

    found = assemble(root, {manifest.source: manifest.depth}, cache=where)
    found.site        # the authority
    found.verdict     # fresh | stale | unverifiable
    found.report      # every line R6 requires, in order

**Depends on.** This package's `scan`, `cache`, `freshness` and `site`.

## ⛔ The scan wins, and that is structural rather than a rule anybody follows

⭐ **`scan` runs on every call and its result is what is returned.** There is no
branch on which a cached `Site` is handed back, and there is deliberately no
argument that would create one. §5 says the server *scans* at startup and that
`site.json` is *"a cache of that scan, never the authority"* — so the cache's
job is to be the **written record** a later reader can check, not a shortcut
past the tree.

⚠️ **That is what makes the staleness question answerable at all.** A design
where the cache could be used instead of the scan has to decide whether to
trust it, and every such decision is a chance to be silently wrong. Here the
only thing riding on the verdict is whether the file is rewritten and what the
report says.

## ⛔ Reported, never silent (R6) — and the report is a return value

⭐ **Findings are returned as data, exactly as `validate` returns them**, rather
than written to a logger this package would have to choose. A caller that
throws the report away has made a visible decision; a package that logged
somewhere the caller never looks would have made an invisible one.

**The report names, in order:** every page that could not be identified, by
path; what state the cache was in and what it declared; the verdict; whether
the cache was rewritten; and what the ignore file beside it had to say.

## ⛔ The cache is ignored where it sits, and this is where that is ensured

⭐ **The cache is ignored, never committed.** It is written into somebody's repository by the act of
serving it, and nothing reads it back — `scan` runs on every call and its
result is what is returned — so a corpus that committed it would carry a file
no clone uses and go dirty for doing the one thing the tool is for. ⛔ So
`cache.ensure_ignored` runs on **both** paths through this function, because a
fresh cache is written by nothing and would otherwise never gain the rule.
"""

from __future__ import annotations

from collections.abc import Mapping
from dataclasses import dataclass
from pathlib import Path

from studyforge.corpus.discovery import cache as cache_module
from studyforge.corpus.discovery.freshness import FRESH, UNVERIFIABLE, freshness
from studyforge.corpus.discovery.scan import scan
from studyforge.corpus.discovery.site import Site


@dataclass(frozen=True, slots=True)
class Discovery:
    """One run of discovery: what was found, what the cache was, what was done."""

    site: Site
    verdict: str
    rewritten: bool
    report: tuple[str, ...]


def assemble(
    root: Path | str,
    depths: Mapping[str, int],
    *,
    cache: Path | str | None = None,
    where: str | None = None,
) -> Discovery:
    """Scan `root`, judge the cache at `cache`, and rewrite it unless it is fresh.

    `cache` is the path to `site.json`, which `cache_path(root, profile)`
    answers; passing `None` runs the scan and judges nothing, which is what
    `studyforge validate` wants and what a caller with no write access needs.

    `where` is how the cache is named in the report, and defaults to the path
    **relative to the root** so no absolute path reaches a line anybody prints
    (R7).
    """
    root = Path(root)
    site = scan(root, depths)
    report = [f"{found.path.as_posix()}: {found.fault}" for found in site.unidentified]

    if cache is None:
        report.append("no discovery cache was named, so its freshness was not judged")
        return Discovery(site, UNVERIFIABLE, False, tuple(report))

    path = Path(cache)
    named = where if where is not None else _relative(path, root)
    cached = cache_module.read(path, named)
    if cached is None:
        report.append(f"{named} does not exist yet")
    else:
        report.append(cached.says)

    recorded = cached.scan_sha256 if cached is not None and cached.supported else None
    verdict = freshness(site, recorded)
    report.append(f"the discovery cache is {verdict}; the scan is the authority either way")
    if verdict == FRESH:
        # ⛔ **Ensured on the fresh path too, and that is not belt and braces**
        #: a corpus whose cache is fresh is rewritten by nothing, so
        # the one moment the rule could be written would never come.
        report.extend(cache_module.ensure_ignored(path))
        return Discovery(site, verdict, False, tuple(report))

    said = cache_module.write(path, site)
    report.append(f"{named} was rewritten from this scan at site_api {cache_module.SITE_API}")
    report.extend(said)
    return Discovery(site, verdict, True, tuple(report))


def _relative(path: Path, root: Path) -> str:
    """How the cache is named in the report — relative to the root wherever it can be.

    ⛔ **Falls back to the bare filename, never to the absolute path** (R7). A
    cache written outside the scanned root is unusual but representable, and
    the unusual case is not a licence to print somebody's home directory.
    """
    try:
        return path.relative_to(root).as_posix()
    except ValueError:
        return path.name
