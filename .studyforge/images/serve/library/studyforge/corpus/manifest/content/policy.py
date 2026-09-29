"""What a manifest declares about one path, once the declaration has been read.

**What it does.** Holds `corpus.json`'s `content` as an object — the `include`
globs, the `exclude` entries that each name one file and carry a `why`, and
the `not_material` globs that each carry one too — and answers one question
about one path: **included**, **excluded**, **not material**, **contested** or
**unclassified**?

**How you use it.** `policy.classify("src/whole-series.md")`. ⛔ It takes a
path and does no I/O; enumerating a source root is `studyforge validate`'s job,
and refusing the unclassified ones is its verdict.

**Depends on.** `pathlib.PurePosixPath` for glob semantics — a pure path
object that never touches a disk — and `errors`. ⛔ **Nothing from `parse`**,
and the direction is the seam: reading a declaration needs the model, and the
model never needs the reader. `test_init.py` asserts that rather than trusting
this sentence.

⚠️ **Exclusion wins over inclusion, and that is that shape's case exactly**:
`include: ["src/*.md"]` with `exclude: [{path: "src/whole-series.md", …}]`. A file may
match both, and when it does it is withheld.

## ⛔ The third state, and why it takes globs where an exclusion takes a path

⛔ **Two states are not enough, because a real repository is mostly a third.**
In a typical one, most files are **neither included nor excluded, and few of
those are material withheld from anybody**. The rest are a licence, ignore
files, an editor's workspace, a generated cache. Filing those under `exclude`
makes every `why` a small lie and makes an audit nobody reads.

⭐ **The three states are about whether a file's prose is read into the
archive**, never about materiality in the abstract: `include` reads it in,
`exclude` is prose that *would* be read and deliberately is not, and
`not_material` is not prose to read at all. ⭐ A source `README.md` lands in
the third by that test rather than as a special case — it is the corpus's own
navigation, and the reader loses nothing because every address, title and
ordinal it records is declared in the manifest's container maps.

⚠️ **An exclusion's refusal of globs is right and does not carry over**,
because the two audits are about different harms: a new member of an
exclusion's set is a new withholding and needs its own reason, and a new
member of a `not_material` glob's set is only a harm if it is **actually
material** — which is caught per file, against a real tree, as `CONTESTED`.
⛔ **The category also cannot be enumerated file by file:** writing down the
list adds a file the list does not name. ⭐ **`X1` is not weakened, and its
domain is stated** — an inclusion needs no justification, and every declaration that the
framework will *not* read a file needs one.
"""

from __future__ import annotations

from dataclasses import dataclass
from enum import Enum
from pathlib import PurePosixPath

from studyforge.corpus.manifest.errors import ManifestError
from studyforge.describe import describe


class Classification(Enum):
    """What the content policy says about one path."""

    INCLUDED = "included"
    EXCLUDED = "excluded"
    #: ⛔ Not material at all: the repository's own scaffolding, or content
    #: *about* the material. ⚠️ Nothing is withheld from a reader here, which
    #: is why it is not `EXCLUDED`.
    NOT_MATERIAL = "not-material"
    #: ⛔ Matched by `include` **and** by `not_material`, and this build
    #: refuses to choose. ⭐ **Never a precedence** — one would let a loose
    #: glob quietly drop material, or read the scaffolding aloud. `validate`
    #: exits 1 on it under its own rule id, which is what stops the third
    #: state becoming a place to sweep things into.
    CONTESTED = "contested"
    #: ⛔ Not a further kind of content — it is the absence of a decision, and
    #: `validate` exits 1 on it (R6). A corpus cannot grow a file without
    #: somebody saying what it is, which is the point: the alternative is a
    #: second aggregate appearing and being read as 38 more units.
    UNCLASSIFIED = "unclassified"


@dataclass(frozen=True, slots=True)
class Exclusion:
    """One file withheld from the reader, and the reason it is."""

    path: str
    why: str


@dataclass(frozen=True, slots=True)
class NotMaterial:
    """One glob of files that were never material, and the reason they are not."""

    glob: str
    why: str


@dataclass(frozen=True, slots=True)
class ContentPolicy:
    """`corpus.json`'s `content`: what is material here, and what is not."""

    include: tuple[str, ...]
    exclude: tuple[Exclusion, ...]
    #: ⛔ Defaults to empty, and that is the whole compatibility story: a
    #: manifest at `corpus_api: 1` declares no third state and classifies
    #: exactly as it did before.
    not_material: tuple[NotMaterial, ...] = ()

    def classify(self, path: str) -> Classification:
        """Say what this manifest declares about `path`, without choosing for it.

        `path` is relative to the source root and spelled with forward
        slashes, the way every path in every one of this project's documents
        is.

        ⛔ **Two matches are reported, not resolved.** An exclusion still wins
        over an inclusion — one file, named once and withheld deliberately —
        but `include` and `not_material` disagreeing is two glob authors
        contradicting each other, and the honest answer is `CONTESTED`.
        """
        if not isinstance(path, str) or not path:
            raise ManifestError(f"path to classify must be a non-empty str, got {describe(path)}")
        candidate = PurePosixPath(path)
        if any(exclusion.path == path for exclusion in self.exclude):
            return Classification.EXCLUDED
        included = any(candidate.full_match(pattern) for pattern in self.include)
        scaffolding = any(candidate.full_match(entry.glob) for entry in self.not_material)
        if included and scaffolding:
            return Classification.CONTESTED
        if included:
            return Classification.INCLUDED
        if scaffolding:
            return Classification.NOT_MATERIAL
        return Classification.UNCLASSIFIED

    def why_excluded(self, path: str) -> str | None:
        """Return the recorded reason `path` is withheld, or None if it is not."""
        for exclusion in self.exclude:
            if exclusion.path == path:
                return exclusion.why
        return None

    def why_not_material(self, path: str) -> str | None:
        """Return the recorded reason `path` was never material, or None.

        ⭐ The first matching entry: a `why` nobody can retrieve is a
        declaration nobody audits, which is the argument that makes it
        mandatory in the first place.
        """
        candidate = PurePosixPath(path)
        for entry in self.not_material:
            if candidate.full_match(entry.glob):
                return entry.why
        return None
