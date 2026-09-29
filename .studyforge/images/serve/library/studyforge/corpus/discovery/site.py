"""What a scan found: the artifacts, and the files that could not say what they are.

**What it does.** Holds the scan's result — one `Artifact` per generated page
this build could identify, one `Unidentified` per page it could not — and the
lookups a consumer asks of it.

**How you use it.** `site.units`, `site.corpora`, `site.of(corpus)`,
`site.unit(corpus, address, 7)`.

**Depends on.** `studyforge.corpus.placement` for `Identity`. ⛔ No filesystem
and no JSON: this is the scan's *value*, and reading or writing it is `scan`'s
and `cache`'s work respectively.

## ⛔ A site is assembled from what each file SAYS IT IS, never from where it sits

⭐ **R4, and this type is its shape.** An `Artifact` carries both a path and an
identity, and every lookup here goes through the identity. The path is recorded
so a reader can be sent to the file and so the cache can name it — ⛔ **nothing
in this module ever decides what an artifact *is* by looking at it.**

⚠️ **Which is why a moved page still resolves and still renders wrong, and both
are correct** (R8). Identity survives the move; the page's stylesheet and its
sibling audio resolve relative to the page and do not. ⛔ Conflating them would
force absolute asset paths and break the `file://` floor.

## ⛔ Order is stated, never inherited from a filesystem

**R10.** `document` sorts by path before it emits anything, and it does so even
though `scan` already hands the artifacts over sorted. ⭐ The two are not one
check written twice: the scan's sort is what makes the *walk* reproducible, and
this one is what makes the *cache* reproducible for a `Site` assembled by
anything else — a merge of two roots, a test, a second scanner. A cache whose
bytes depend on how it was built is one no two machines can compare.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import PurePosixPath

from studyforge.address import Address
from studyforge.corpus.placement import Identity


@dataclass(frozen=True, slots=True)
class Artifact:
    """One generated page, where it was found, and what it says it is."""

    path: PurePosixPath
    identity: Identity

    @property
    def document(self) -> dict:
        """The artifact as the cache records it. ⛔ The path is relative (R7)."""
        return {"path": self.path.as_posix(), "identity": self.identity.document}


@dataclass(frozen=True, slots=True)
class Unidentified:
    """A page the scan opened and could not identify — recorded, never dropped.

    ⛔ **R6.** A page that quietly vanishes from a site is the failure discovery
    exists to make impossible: the reader sees a contents list that is short by
    one and has nothing at all to look at.
    """

    path: PurePosixPath
    fault: str

    @property
    def document(self) -> dict:
        """The finding as the cache records it, so a stale cache can notice it."""
        return {"path": self.path.as_posix(), "fault": self.fault}


@dataclass(frozen=True, slots=True)
class Site:
    """Everything one scan found, and the lookups that go through identity."""

    artifacts: tuple[Artifact, ...] = ()
    unidentified: tuple[Unidentified, ...] = ()

    @property
    def units(self) -> tuple[Artifact, ...]:
        """Every artifact that says it is a unit page."""
        return tuple(found for found in self.artifacts if found.identity.kind == "unit")

    @property
    def containers(self) -> tuple[Artifact, ...]:
        """Every artifact that says it is a container page."""
        return tuple(found for found in self.artifacts if found.identity.kind == "container")

    @property
    def corpora(self) -> tuple[str, ...]:
        """Every corpus named by an artifact, sorted.

        ⛔ Sorted rather than first-seen, and built through a `dict` rather than
        a `set`: R10 forbids an answer that depends on iteration order, and
        this one is printed.

        ⭐ **Two corpora under one root is the ordinary case, not the exotic
        one** — they may declare different placement profiles and the scan
        never learns which, because it reads identity and not layout.
        """
        return tuple(sorted({found.identity.corpus: None for found in self.artifacts}))

    @property
    def paths(self) -> tuple[str, ...]:
        """Every path the scan read, identified or not, sorted."""
        return tuple(sorted(_by_path(found) for found in (*self.artifacts, *self.unidentified)))

    def of(self, corpus: str) -> tuple[Artifact, ...]:
        """Every artifact declaring itself part of `corpus`, in the site's order."""
        return tuple(found for found in self.artifacts if found.identity.corpus == corpus)

    def unit(self, corpus: str, address: Address, ordinal: int) -> Artifact | None:
        """Return the unit page for one address, or `None` — found by identity alone.

        ⭐ **This is the acceptance in one method.** Move the file, rename it,
        put it under a different placement profile: the answer does not change,
        because nothing here consults `Artifact.path`.
        """
        for found in self.units:
            declared = found.identity
            if declared.corpus == corpus and declared.address == address:
                if declared.unit == ordinal:
                    return found
        return None

    @property
    def document(self) -> dict:
        """The scan's findings, in a stated order, as the cache records them.

        ⛔ **`site_api` is not here**, and its absence is the design. This
        object is the *content* signal; the version key is the *schema* signal,
        and a document that mixed them would make one number answer two
        questions — which is the trap a disposable, digest-checked cache avoids (R9, R10).
        """
        return {
            "artifacts": [found.document for found in sorted(self.artifacts, key=_by_path)],
            "unidentified": [found.document for found in sorted(self.unidentified, key=_by_path)],
        }


def _by_path(found: Artifact | Unidentified) -> str:
    """Sort key for anything a scan recorded. ⛔ The posix string, not the `Path`.

    ⚠️ `PurePosixPath` compares on its parts tuple, so `a/b` and `a-b` order
    differently under the two, and the cache is compared **byte for byte**
    (R10). Naming the key here is what stops a reader assuming the default.
    """
    return found.path.as_posix()
