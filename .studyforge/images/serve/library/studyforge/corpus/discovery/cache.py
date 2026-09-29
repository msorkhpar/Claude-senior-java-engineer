"""`site.json` — the written record of a scan, and never the authority.

**What it does.** Renders a `Site` to the discovery cache, reads one back, and
says — without believing anything in it — what state the file on disk is in.

**How you use it.** `write(path, site)`, `read(path)` → a `Cached`,
`cache_path(root, profile)` for where it goes.

**Depends on.** `studyforge.version` for R9, `studyforge.archive.scrub` for the
R7 gate, `studyforge.corpus.placement` for the file's name and location, and
this package's `site` and `errors`.

## R21: this contract is located, versioned and produced by exactly one party

| | |
|---|---|
| **File** | `.studyforge/site.json` — `SITE_CACHE_FILENAME`, which already existed |
| **Version key** | `site_api` — ⭐ **minted here** |
| **Producer** | ⛔ **`corpus.discovery`, and nothing else ever writes it** |

⭐ **`unit.content`'s `content_api` is the worked example for the MECHANICS**: the key
is registered in `version.CONTRACT_FIELDS` in the same commit that mints it, or
the shared guard cannot see it. ⛔ **It is not the example for the FAILURE, and
a builder copying it faithfully would get this wrong.**

## ⛔ An unsupported or absent `site_api` does not raise

⭐ It means *the cache is not read*: the scan runs, and the cache is rewritten
at the current version. ⛔ **That IS R9's refusal.** R9 forbids *migration*, and
nothing here is carried forward — the old document is discarded whole and the
authority, which is the scan, produces a new one. There is no reading of a
stale `site.json` that could survive, because every field in it is derived.

⚠️ **Raising would be wrong twice over.** It contradicts §5's *"a stale cache is
detected and the scan wins"*, and it turns a derived, rebuildable artifact into
a hard failure — for a corpus whose only mistake was to have been
built by an older version of this framework.

⛔ **So this module uses `version.is_supported`, not `version.check`.** That
module's own docstring names `is_supported` as *"the predicate, for a caller
that reports rather than refuses"*, and this is that caller. ⛔ **Reported,
never silent** (R6): `Cached.says` names the path, what was declared, and what
this build speaks, and `assemble` puts it in the report.

## ⛔ `site_api` is NOT the staleness mechanism

It answers *"is this the shape of cache I speak?"* and nothing else. ⚠️ The
tempting move — bump it per scan and compare — makes a member of
`CONTRACT_FIELDS` into **mutable data**, and that tuple is read by every other
contract in the build: a version whose value changes per run breaks R9's
meaning for all of them.

⭐ **Two signals, two tests.** Content staleness is `freshness.py`'s, and the
cache carries `scan_sha256` for it — a digest of the scan's own findings, which
is why `Site.document` deliberately does not include the version key.
"""

from __future__ import annotations

import json
import os
from dataclasses import dataclass
from pathlib import Path

from studyforge.archive.scrub import assert_clean
from studyforge.corpus.discovery.freshness import scan_sha256
from studyforge.corpus.discovery.site import Site
from studyforge.corpus.placement import (
    GENERATED_IGNORE_HOME,
    IGNORE_FILENAME,
    SITE_CACHE_FILENAME,
    STAGING_SUFFIX,
    IgnoreFile,
    Profile,
    cache_ignore_lines,
)
from studyforge.describe import describe
from studyforge.version import is_supported

#: R9's key for this contract. ⭐ Registered in
#: `version.CONTRACT_FIELDS` in the same commit, per that tuple's own
#: convention.
SITE_API = 1
KNOWN_SITE_API = frozenset({SITE_API})

#: The keys of the cache, in the order they are written. ⛔ Fixed rather than
#: sorted: an unchanged tree must re-render to identical bytes (R10).
SITE_KEYS = ("site_api", "scan_sha256", "artifacts", "unidentified")

#: What is appended to a cache path while it is being written. ⚠️ A torn cache
#: is a cache that reads as absent, which is a rebuild; a cache half-overwritten
#: in place is a cache that reads as *present and wrong*.
#: ⛔ `placement`'s, never a second literal: the ignore rule written beside the
#: cache has to cover exactly this name.
WRITING_SUFFIX = STAGING_SUFFIX


@dataclass(frozen=True, slots=True)
class Cached:
    """What a `site.json` on disk says, before anything in it is believed.

    ⛔ **Nothing here is validated except the version**, and that is the shape
    the ruling asks for: an unsupported cache is not read, so parsing its
    contents in order to discard them would be work done to reach a conclusion
    already reached.
    """

    where: str
    declared: object = None
    scan_sha256: str | None = None
    fault: str | None = None

    @property
    def supported(self) -> bool:
        """Whether this build speaks the shape of cache on disk.

        ⛔ `is_supported`, never `check`. The refusal here is *"do not read
        it"*, not *"stop"*.
        """
        return self.fault is None and is_supported(self.declared, KNOWN_SITE_API)

    @property
    def says(self) -> str:
        """One line naming the path and what it declares, for the report (R6).

        ⛔ **The path is the one the caller passed and is expected to be
        relative** (R7). `assemble` passes the path relative to the scan root
        for exactly that reason.
        """
        if self.fault is not None:
            return f"{self.where} {self.fault}"
        if not self.supported:
            return (
                f"{self.where} declares {_said(self.declared)}; this build speaks "
                f"site_api {sorted(KNOWN_SITE_API)}, so the cache is not read — "
                f"the scan runs and the cache is rewritten"
            )
        return f"{self.where} declares site_api {self.declared}"


def document(site: Site) -> dict:
    """Return the cache as an object, in `SITE_KEYS` order."""
    written = {"site_api": SITE_API, "scan_sha256": scan_sha256(site), **site.document}
    return {key: written[key] for key in SITE_KEYS}


def render(site: Site) -> str:
    """Return the cache's bytes — reproducible byte for byte for an unchanged tree (R10).

    ⚠️ **There is no clock in this document and there is deliberately no
    `ingested`-shaped exemption for one.** §6 gives the archive that exemption
    because a capture date answers *"how stale is this?"*; here the same
    question is answered by `scan_sha256` against the tree itself, which is a
    better answer and needs no exemption.
    """
    return json.dumps(document(site), indent=2, ensure_ascii=False, sort_keys=False) + "\n"


def write(path: Path | str, site: Site) -> tuple[str, ...]:
    """Write the cache, creating its directory. ⛔ The one writer of `site.json`.

    ⚠️ **Staged beside the target and moved into place**, the discipline §6
    states for a generator. A cache half-overwritten in place reads as present
    and wrong, which is precisely the state this whole contract exists to make
    impossible; a torn `*.writing` file reads as absent, which is a rebuild.

    ⭐ **Returns what the ignore file beside the cache had to say** — the lines
    R6 asks a caller to report. The ignore file is ensured *here*,
    where the cache is written, because that is the one moment that cannot be
    skipped: a corpus with no such file yet is covered the first time
    somebody serves it, without anything being regenerated.
    """
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    said = ensure_ignored(path)
    staged = path.with_name(path.name + WRITING_SUFFIX)
    try:
        staged.write_text(render(site), encoding="utf-8", newline="\n")
        os.replace(staged, path)
    finally:
        staged.unlink(missing_ok=True)
    return said


def ensure_ignored(path: Path | str) -> tuple[str, ...]:
    """Make sure the cache at `path` is ignored where it sits, and say what was found.

    ⭐ **The same judgement `progress.store` makes one directory over**: a
    record this machine keeps for itself never enters a commit, and the
    mechanism R3 leaves is an ignore file *inside* the generated directory,
    never the repository's root one.

    ⛔ **The shapes differ, and so does the text.** `progress/` is a directory
    this framework owns whole, so `*` is right there. `.studyforge/` is
    **tracked** and carries the asset bundle a clone must read, so the rules
    here name the cache and its staging file — and the file, when it carries
    nothing else, names itself, because a file holding only machine-local
    rules is itself machine-local.

    ⛔ **An ignore file already there is never rewritten.** It is not this
    writer's to change: it may carry the corpus's own media policy, which a
    clone has to read. One that does not cover the cache is **reported** and
    left exactly as it is — a corpus whose file lacks the rule is not
    silently repaired, it is regenerated.

    ⚠️ **Written in one call rather than staged and renamed.** A staged
    `.gitignore.writing` is a file nothing ignores until the rename lands, so
    the discipline that protects the cache would dirty the corpus it is
    protecting; the file is ~150 bytes and a torn one is reported on the next
    serve rather than rewritten.
    """
    ignore = Path(path).parent / IGNORE_FILENAME
    try:
        existing = ignore.read_text(encoding="utf-8")
    except FileNotFoundError:
        return _write_ignore(ignore)
    except OSError, UnicodeDecodeError:
        return (
            f"{IGNORE_FILENAME} beside the discovery cache cannot be read, so whether "
            f"{SITE_CACHE_FILENAME} is ignored could not be judged; it was not rewritten",
        )
    if _covers_cache(existing):
        return ()
    return (
        f"{IGNORE_FILENAME} beside the discovery cache does not ignore "
        f"{SITE_CACHE_FILENAME}, and it is not this writer's to rewrite; regenerate the "
        f"corpus so its generated ignore file carries the rule",
    )


def ignore_text() -> str:
    """Return the file `ensure_ignored` writes when there is none. ⛔ One spelling.

    ⭐ Composed from `placement`'s own `IgnoreFile`, so the file this writer
    leaves behind and the file a corpus is generated with are the same bytes
    for a corpus whose media is committed — and a later regeneration writes
    over it with a superset rather than fighting it.
    """
    return IgnoreFile(home=GENERATED_IGNORE_HOME, lines=cache_ignore_lines()).text()


def _write_ignore(ignore: Path) -> tuple[str, ...]:
    """Write the ignore file, reporting either what was written or why it was not."""
    try:
        ignore.write_text(ignore_text(), encoding="utf-8", newline="\n")
    except OSError as fault:
        # ⛔ `strerror`, never the exception: it renders with the absolute path (R7).
        return (f"{IGNORE_FILENAME} could not be written beside the cache: {fault.strerror}",)
    return (
        f"{IGNORE_FILENAME} was written beside the discovery cache, so neither it nor "
        f"{SITE_CACHE_FILENAME} ever enters a commit",
    )


def _covers_cache(text: str) -> bool:
    """Whether an ignore file's text already ignores the cache where it sits.

    ⚠️ **Read as git reads it, and no further.** A bare `site.json` line and an
    anchored `/site.json` both ignore the file beside them; `*` ignores the
    whole directory, which a caller may not want here but which does cover the
    cache. ⛔ A negation anywhere means the file is making a decision this
    writer cannot read, so it is treated as not covering.
    """
    lines = [line.strip() for line in text.splitlines()]
    if any(line.startswith("!") for line in lines):
        return False
    return any(line in {SITE_CACHE_FILENAME, f"/{SITE_CACHE_FILENAME}", "*"} for line in lines)


def read(path: Path | str, where: str | None = None) -> Cached | None:
    """Return what the cache at `path` says, or `None` when there is no file there.

    `where` is how the file is named in every sentence this produces, and a
    caller passes the path **relative to the scan root** (R7). It defaults to
    the bare filename rather than to `path`, because the default must be safe:
    an absolute path is what a caller has, and it is the one thing a refusal
    may not carry.

    ⚠️ **A malformed cache reads as a `Cached` with a fault, never as an
    exception.** It is a local, derived, rebuildable artifact; a half-written
    one is a rebuild the reader needs to be **told** about, not a crash in a
    server's startup. ⭐ That is the same judgement this package's `freshness`
    makes about a cache it cannot read, reached for the same reason.

    ⛔ **`None` means "no file", and it is not the same as "nothing readable".**
    Collapsing them would make an absent cache and a corrupt one produce the
    same sentence, and only one of those is a thing somebody should look at.
    """
    path = Path(path)
    where = path.name if where is None else where
    try:
        text = path.read_text(encoding="utf-8")
    except FileNotFoundError:
        return None
    except OSError as fault:
        # ⛔ `strerror`, never the exception: an `OSError` renders with the
        # absolute path it was given (R7).
        return Cached(where, fault=f"cannot be read: {fault.strerror}")
    except UnicodeDecodeError:
        return Cached(where, fault="is not valid UTF-8")
    return _from_text(text, where)


def cache_path(root: Path | str, profile: Profile) -> Path:
    """Where this corpus's cache goes. ⛔ One spelling, and it is `placement`'s.

    ⚠️ The answer is the same under every profile — `Profile.corpus()` is
    shared, because a profile that could move the cache would make two corpora
    on one disk unfindable by one scan. ⭐ It is still *asked* rather than
    composed here, so this module holds no path of its own to drift.
    """
    return Path(root) / profile.corpus().site_cache


def _from_text(text: str, where: str) -> Cached:
    """Decode the cache, gate it, and read the two fields that are believed."""
    try:
        decoded = json.loads(text)
    except TypeError, ValueError:
        # ⛔ The payload is never echoed (R7).
        return Cached(where, fault="is not valid JSON")
    if not isinstance(decoded, dict):
        return Cached(where, fault="is not a JSON object")
    # ⛔ **The gate runs over the whole decoded document** (R7). This file
    # is generated, but it is generated into a repository somebody clones, and
    # it is the one document in this contract a person can hand-edit without
    # anything noticing — which is exactly the case the gate exists for.
    assert_clean(decoded, where)
    digest = decoded.get("scan_sha256")
    return Cached(
        where,
        declared=decoded.get("site_api"),
        scan_sha256=digest if isinstance(digest, str) and digest else None,
    )


def _said(declared: object) -> str:
    """Describe what the cache declared: the number if it is one, the type if not.

    ⭐ The same argument `version._said` makes, and it is not reachable from
    here: that function is private to the module whose refusal it composes, and
    this caller does not refuse.
    """
    if declared is None:
        return "no site_api"
    described = describe(declared)
    return f"site_api {described}" if described[:1].isdigit() else f"site_api as {described}"
