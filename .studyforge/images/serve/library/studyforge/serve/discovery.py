r"""Discovery wiring: given a root, find every corpus under it, with no configured paths.

**What it does.** Walks a served root for `corpus.json`, reads each corpus's
declarations, runs `corpus.discovery` over each corpus root against that corpus's
own site cache, and holds the result: the corpora one instance serves, the depth
each is addressed at, the digest of the scan it started from, and every line
discovery reported (R6).

**How you use it.**

    discovered = discover(root)        # catch `RAISES`
    discovered.corpora                 # `ServedCorpus`, sorted by where each sits
    discovered.report                  # every line, relative paths only (R7)
    served.rescan()                    # the scan again — what a state request reads
    served.scan_root                   # where it scans: its root unless a site is elsewhere

**Depends on.** `corpus.manifest` for the file's name, `corpus.discovery` for the
scan, the verdict and the cache, `generate` for `read_corpus` and `Corpus`
(⚠️ see below), `progress` for each corpus's store, and `archive.scrub` for the
leak that travels through untranslated.

⚠️ **No progress store is named private here.** `routes.assets` refuses every
store on any resolved path, so a predicate here would be a second
refusal nothing could reach.

## ⛔ No configured paths

⭐ **The root is the only input.** A corpus is wherever a `corpus.json` is; its
pages are wherever their identity blocks say (R4); its depth and profile are its
manifest's. Two corpora with different placement profiles under one root are two
entries here and nothing more, because nothing downstream asks which profile it has.

## ⛔ The startup scan is a reference point, never an answer

`assemble` judges each corpus's `site.json` at startup and rewrites it unless it is
fresh — that verdict is kept, reported and served. ⭐ **The startup `Site` is kept
only as its digest**: a state request calls `rescan` and compares, so a tree that
changed after startup reads `stale` and the answer is the new scan's. ⛔ No field
here hands a caller the startup scan's artifacts to answer from.

## ⛔ Where the record lives and where the pages are scanned are TWO paths

⭐ **`root` is where a corpus's manifest, unit documents and progress store live;
`scan_root` is where its pages are scanned, and it defaults to `root`.** A corpus
built in place is scanned where it sits, so every corpus `discover` finds leaves
it unset. ⛔ **A site `build --out` wrote elsewhere is scanned THERE**, never in
the corpus root, which holds no page: the `--site` form names it
(`serve.instance.site_discovery`), and no subclass overrides `rescan` to say so.

## ⛔ Nothing under a corpus's own `.studyforge/` is a corpus

⚠️ **Measured:** the execution skill's copy of a corpus's code sits under
`.studyforge/execution/code/`, and while it mirrored a root-level corpus's
`corpus.json`, a restart found two corpora with one source and refused (exit 2).
⭐ **A manifest below a `.studyforge/` directory whose parent holds a manifest is
the framework's own bookkeeping and is never counted**, whatever wrote it — so
neither this rule nor the copy's own refusal to carry a manifest
(`execute.codetree`) is the only thing standing between a copy and a dead site.

## ⛔ Two corpora with one `source` refuse the instance

A URL addresses a corpus by its `source`, so two with the same one would put two
corpora behind one address — and which answered would be decided by walk order.
⛔ Refused before a socket exists, naming both manifests by relative path.

## ⚠️ Why `read_corpus` comes from the build package

It is the one reader of a corpus's manifest, container maps and contents together;
`CorpusContent` already consumes its value. A second reader here would be a second
opinion about what a corpus declares. It is imported, not re-derived, and the
package contract names the dependency.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path, PurePosixPath

from studyforge.archive.scrub import PersonalDataLeak
from studyforge.corpus.discovery import Discovery, Site, assemble, cache_path, scan
from studyforge.corpus.discovery import scan_sha256 as digest_of
from studyforge.corpus.manifest import MANIFEST_FILENAME
from studyforge.corpus.placement import GENERATED_ROOT
from studyforge.generate import RAISES as BUILD_RAISES
from studyforge.generate import Corpus, read_corpus
from studyforge.progress import Progress


class DiscoveryRefused(ValueError):
    """A root this module will not serve: no corpus in it, or two one address would name.

    ⛔ The message names manifests by their path **relative to the served root** and
    never the root itself (R7): the root is somebody's home directory plus a few segments.
    """


#: ⛔ What `discover` lets out. `PersonalDataLeak` travels as itself.
RAISES = (DiscoveryRefused, PersonalDataLeak)


@dataclass(frozen=True, slots=True)
class ServedCorpus:
    """One corpus an instance serves: its declarations, where it sits, how it started.

    ⭐ `scan_root` is where its pages are scanned; given as `None` it is `root`.
    """

    corpus: Corpus
    root: Path
    relative: PurePosixPath
    startup: Discovery
    digest: str
    scan_root: Path | None = None

    def __post_init__(self) -> None:
        """Default the scan root to the corpus root, so the field is always a path."""
        if self.scan_root is None:
            object.__setattr__(self, "scan_root", self.root)

    @property
    def source(self) -> str:
        """Return the corpus's `source`, which is how every URL names it."""
        return self.corpus.manifest.source

    @property
    def depth(self) -> int:
        """Return `len(levels)`, the number of segments its addresses have."""
        return self.corpus.manifest.depth

    @property
    def profile(self) -> str:
        """Return the placement profile's declared name, for reporting only."""
        return self.corpus.profile.name

    @property
    def verdict(self) -> str:
        """Return what `assemble` judged this corpus's `site.json` at startup."""
        return self.startup.verdict

    def rescan(self) -> Site:
        """Scan the pages again, at `scan_root`. ⛔ What every state request reads."""
        return scan(self.scan_root, {self.source: self.depth})

    def progress(self) -> Progress:
        """Return the corpus's progress store, bound to its depth; nothing is read yet."""
        return Progress(self.root, self.depth)

    def href(self, path: PurePosixPath | str) -> str:
        """Return a path relative to this corpus's root as a URL path on the instance."""
        return "/" + (self.relative / PurePosixPath(path)).as_posix()


@dataclass(frozen=True, slots=True)
class Discovered:
    """Every corpus found under one served root, and what finding them reported."""

    root: Path
    corpora: tuple[ServedCorpus, ...]
    report: tuple[str, ...]

    @property
    def by_source(self) -> dict[str, ServedCorpus]:
        """Return the corpora keyed by `source`."""
        return {served.source: served for served in self.corpora}

    @property
    def depths(self) -> dict[str, int]:
        """Return each corpus's depth keyed by `source` — what addressing is given."""
        return {served.source: served.depth for served in self.corpora}


def manifests(root: Path | str) -> tuple[Path, ...]:
    """Every `corpus.json` under `root`, sorted by its posix path relative to it (R10).

    ⛔ Never one under a corpus's own `GENERATED_ROOT` (`_bookkeeping`).
    """
    root = Path(root)
    found = {
        path.relative_to(root).as_posix(): path
        for path in root.rglob(MANIFEST_FILENAME)
        if path.is_file() and not _bookkeeping(root, path)
    }
    return tuple(found[key] for key in sorted(found))


def _bookkeeping(root: Path, manifest: Path) -> bool:
    """Whether `manifest` sits below a `GENERATED_ROOT` whose parent holds a manifest."""
    here = root
    for part in manifest.parent.relative_to(root).parts:
        if part == GENERATED_ROOT and (here / MANIFEST_FILENAME).is_file():
            return True
        here = here / part
    return False


def discover(root: Path | str) -> Discovered:
    """Find and read every corpus under `root`, judge each one's cache, and report it all."""
    root = Path(root)
    if not root.is_dir():
        raise DiscoveryRefused("the served root is not a directory, so no corpus is in it")
    corpora: list[ServedCorpus] = []
    report: list[str] = []
    seen: dict[str, str] = {}
    for manifest in manifests(root):
        relative = PurePosixPath(manifest.parent.relative_to(root).as_posix())
        named = (relative / MANIFEST_FILENAME).as_posix()
        try:
            corpus = read_corpus(manifest.parent)
        except PersonalDataLeak:
            raise  # ⛔ R7's refusal is never demoted to a skipped corpus.
        except BUILD_RAISES as fault:
            # ⭐ What `generate` lets out is its `RAISES`, never a member retyped
            # here; the leak above is this site's own arm, and
            # `RAISES` is what it reaches when the corpus is merely unreadable.
            report.append(f"{named} is not served: {fault}")
            continue
        source = corpus.manifest.source
        if source in seen:
            raise DiscoveryRefused(
                f"{seen[source]} and {named} declare the same source, and one instance "
                f"addresses a corpus by its source; serve them from separate roots"
            )
        seen[source] = named
        startup = _assemble(corpus, manifest.parent, relative, report)
        corpora.append(
            ServedCorpus(corpus, manifest.parent, relative, startup, digest_of(startup.site))
        )
    if not corpora:
        said = "; ".join(report) if report else "none was found"
        raise DiscoveryRefused(f"no servable {MANIFEST_FILENAME} is under the served root: {said}")
    return Discovered(root, tuple(corpora), tuple(report))


def _assemble(corpus: Corpus, root: Path, relative: PurePosixPath, report: list[str]) -> Discovery:
    """Run discovery over one corpus root against its own cache, reporting every line."""
    depths = {corpus.manifest.source: corpus.manifest.depth}
    where = (relative / corpus.shared.site_cache).as_posix()
    try:
        found = assemble(root, depths, cache=cache_path(root, corpus.profile), where=where)
    except OSError as fault:
        # ⛔ `strerror`, never the exception: it renders with the absolute path (R7).
        report.append(f"{where} could not be written ({fault.strerror}); it was not judged")
        found = assemble(root, depths)
    report.extend(f"{relative.as_posix()}: {line}" for line in found.report)
    return found
