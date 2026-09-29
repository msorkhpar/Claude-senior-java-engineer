"""The scan — R4's mechanism, and the half everything else in this package consumes.

**What it does.** Finds every generated page under a root and asks each one
what it is, by reading the identity block inside it.

**How you use it.** `scan(root, {corpus: depth})` returns a `Site`;
`pages(root)` is the walk on its own, in the order the scan reads them.

**Depends on.** `studyforge.corpus.placement` for the two page suffixes and for
the identity block, `studyforge.archive.scrub` for the R7 gate, and this
package's `site` and `errors`.

## ⛔ Find artifacts under a root, and assemble a site from what each file SAYS IT IS

**R4, spec §5.** The walk exists only to produce a list of files to open. Every
question that follows — which corpus, which address, which unit, unit page or
container page — is answered by the block inside the file. ⛔ **Nothing here
reads meaning out of a directory name**, which is what lets a corpus be told
*"artifacts go wherever suits the material"* and lets two corpora with
different placement profiles be found by one scan.

⚠️ **A moved page is still correctly identified and still renders wrong**, and
neither is a defect (R8). See `site.py`.

## ⛔ There is no skip list, and that is the R1-clean answer

⚠️ `validate/source.py` carries `SKIP_DIRS`, and its own docstring records that
two of the five names it started with were *"the framework knowing about two
ecosystems, which is R1 with the sign flipped"*. ⭐ **This walk needs no such
list at all**, because it does not enumerate a tree and then classify it: it
globs two suffixes **this framework minted** — `.unit.html` and `.section.html`
— so a `.git` object, a virtualenv and a `node_modules` are not skipped, they
simply do not match. ⛔ Adding a skip list here would be adding the R1 problem
to a walk that does not have it.

⚠️ **Hidden directories are walked, and that is load-bearing, not incidental.**
Under the `tree` profile every generated page lives below `.studyforge/`;
on this build's Python, `Path.rglob` matches inside dot-directories
and matches dot-prefixed names. ⛔ A walk that skipped them would find zero
pages for an entire placement profile and report a perfectly empty site.

## ⛔ Enumeration order is stated, because a scan is where R10 goes wrong

`rglob` yields in whatever order the filesystem answers in, and two machines
answer differently. ⭐ The walk therefore keys every hit by its **path relative
to the root, as a posix string**, and sorts on that — not on `Path`, whose
comparison is by parts, and not on the absolute path, which begins with
somebody's home directory.

## Why this module reads the block itself instead of calling `identity.parse`

⚠️ **The depth is per corpus, and only the block knows which corpus.**
`identity.parse` takes the declared depth up front, which is right for its
caller and impossible for this one: a root may hold two corpora of different
depths, and the scan cannot know which it is holding until it has read
`corpus` out of the very block it is about to validate. ⭐ So the pattern and
the decode are borrowed from `placement.identity` — never re-spelled — and
`identity.from_document` still does every piece of validation. ⛔ The one thing
this module must not do is grow a second opinion about what a valid identity
is.
"""

from __future__ import annotations

import json
from collections.abc import Mapping
from pathlib import Path, PurePosixPath
from typing import Never

from studyforge.archive.scrub import assert_clean
from studyforge.corpus.discovery.errors import DiscoveryError
from studyforge.corpus.discovery.site import Artifact, Site, Unidentified
from studyforge.corpus.placement import (
    CONTAINER_SUFFIX,
    UNIT_SUFFIX,
    PlacementError,
    identity,
)
from studyforge.describe import describe

#: What a scan globs for. ⛔ `placement.names`' own constants, never retyped —
#: four places already have to agree about `.unit.html` and a fifth spelling is
#: how they stop.
PAGE_SUFFIXES = (UNIT_SUFFIX, CONTAINER_SUFFIX)


def scan(root: Path | str, depths: Mapping[str, int]) -> Site:
    """Assemble a `Site` from every generated page under `root`.

    `depths` maps a corpus's own name to the depth its manifest declares, so an
    address is checked against the depth of the corpus that claims it. ⭐ A
    single-corpus caller passes `{manifest.source: manifest.depth}`; a server
    holding two corpora passes both, and one walk finds them both.

    ⛔ **Every page that cannot be identified is recorded by name** (R6), and
    the walk continues. A scan that stopped at the first unreadable file would
    report one fault and hide every other, and the site would be short by an
    unknown number of pages with nothing said.
    """
    root = Path(root)
    artifacts: list[Artifact] = []
    unidentified: list[Unidentified] = []
    for path in pages(root):
        relative = PurePosixPath(path.relative_to(root).as_posix())
        where = relative.as_posix()
        try:
            found = _identity_of(path, where, depths)
        except DiscoveryError as fault:
            # ⛔ `PersonalDataLeak` is NOT caught here and never will be.
            # It is not a `DiscoveryError`, so it travels as
            # itself and stops the scan; filed among these it would read as
            # one more page that could not be identified.
            unidentified.append(Unidentified(relative, str(fault)))
            continue
        artifacts.append(Artifact(relative, found))
    return Site(tuple(artifacts), tuple(unidentified))


def pages(root: Path | str) -> tuple[Path, ...]:
    """Every generated page under `root`, in the order a scan reads them.

    ⛔ Sorted by the path **relative to the root**, so the order is the same on
    two machines and does not depend on where the root sits (R10, R7).
    """
    root = Path(root)
    if not root.is_dir():
        # ⛔ The root is DESCRIBED and never echoed (R7): it is an
        # absolute path on somebody's machine, and this refusal is the one a
        # misconfigured server prints into a log.
        raise DiscoveryError(
            "the scan root is not a directory, so there is nothing to discover; "
            "a scan is given the corpus root that holds the generated pages"
        )
    found: dict[str, Path] = {}
    for suffix in PAGE_SUFFIXES:
        for path in root.rglob(f"*{suffix}"):
            if path.is_file():
                found[path.relative_to(root).as_posix()] = path
    return tuple(found[key] for key in sorted(found))


def _identity_of(path: Path, where: str, depths: Mapping[str, int]) -> identity.Identity:
    """Return what one page says it is, or raise `DiscoveryError` naming the page.

    ⛔ **The gate runs on the decoded block before anything is read out of it**
    (R7). This module decodes a document somebody else's build may have
    written, and the field this module reaches for first — `corpus` — is read
    *before* `identity.from_document` gets its own chance to gate.
    """
    document = _block_of(_text_of(path, where), where)
    assert_clean(document, where)
    depth = _depth_for(document, where, depths)
    try:
        return identity.from_document(document, depth, where)
    except PlacementError as fault:
        # ⭐ `placement`'s message is already the message a reader needs, and it
        # is already R7-clean. Re-deriving it here would give one fault two
        # spellings, which is how two of them come to disagree.
        raise DiscoveryError(str(fault)) from None


def _depth_for(document: object, where: str, depths: Mapping[str, int]) -> int:
    """Return the declared depth of the corpus this page claims to belong to.

    ⛔ A page naming a corpus this scan was given no depth for is **reported by
    name**, never silently skipped and never guessed at from the number of
    segments it happens to carry. ⭐ Guessing would defeat the depth check
    entirely: an address of the wrong arity is exactly what it exists to catch.
    """
    corpus = document.get("corpus") if isinstance(document, dict) else None
    if isinstance(corpus, str) and corpus in depths:
        return depths[corpus]
    # ⛔ The corpus name is DESCRIBED rather than echoed. It is a slug read out
    # of a file this build may not have written, and R1 forbids any corpus's
    # name reaching this package by any route — including a log line.
    return _refuse(
        f"{where}'s identity block names a corpus this scan holds no depth for; "
        f"it declares {describe(corpus)} and the scan was given "
        f"{sorted(depths)!r}"
    )


def _text_of(path: Path, where: str) -> str:
    """Return the page's text, or raise `DiscoveryError` naming its relative path."""
    try:
        return path.read_text(encoding="utf-8")
    except OSError as fault:
        # ⛔ `strerror`, never the exception: an `OSError` renders with the
        # absolute path it was given (R7).
        return _refuse(f"{where} cannot be read: {fault.strerror}")
    except UnicodeDecodeError:
        # ⛔ The offending bytes are not echoed, for the same reason the JSON
        # payload is not: a refusal that quotes an undecodable file has only
        # relocated whatever it held into a log.
        return _refuse(f"{where} is not valid UTF-8, so it carries no identity")


def _block_of(text: str, where: str) -> object:
    """Return the decoded identity block, or raise `DiscoveryError` naming the page.

    ⛔ **The pattern is `placement.identity`'s and is never re-spelled here.**
    It is deliberately tolerant of attribute order and whitespace, because a
    scan reads files this build may not have written, and a second regex would
    be a second and quietly different definition of what a scan can find.
    """
    found = identity.IDENTITY_PATTERN.search(text)
    if found is None:
        return _refuse(
            f"{where} carries no identity block; every generated artifact embeds "
            f"one, and a file without one is reported rather than skipped"
        )
    try:
        return json.loads(found.group(1))
    except TypeError, ValueError:
        # ⛔ The payload is never echoed (R7).
        return _refuse(f"{where}'s identity block is not valid JSON")


def _refuse(message: str) -> Never:
    """Raise `DiscoveryError`. ⭐ A function so a caller can `return` the refusal.

    ⚠️ Cosmetic in one place and load-bearing in three: `_text_of` and
    `_block_of` refuse from inside an `except` clause, where a bare `raise`
    chains the original exception and drags its absolute path into the
    traceback. `from None` on every one of them is what this centralises.
    """
    raise DiscoveryError(message) from None
