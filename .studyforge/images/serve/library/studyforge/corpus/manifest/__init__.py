"""`corpus.json` — the declaration that makes a directory a source.

**What it does.** Owns the manifest: its version, the corpus's identity, the
container levels that fix its depth, its variants, its placement profile, what
counts as its content, whether it commits its media, the runtimes its material
needs, whether it is narrated, where onboarding's reader document goes, where
its curriculum is recorded, and the enumerated set of existing files it may add
to.

**How you use it.** `load(path)` or `parse(text)`; then ask the `Manifest`.

    from studyforge.corpus.manifest import load

    manifest = load("corpus.json")
    manifest.depth                       # 2 — len(levels)
    manifest.parse_key("basics/01-intro") # an Address, checked against that depth
    manifest.content.classify("src/whole-series.md")  # Classification.EXCLUDED
    manifest.media.commits               # True — 'auto' commits
    manifest.runtimes                    # () — absent means no runner (§7, C5)
    manifest.narration                   # True — absent means voiced; False is the floor
    manifest.onboarding_doc              # 'ONBOARDING.md' — absent; None when declared false
    manifest.curriculum                  # None — absent; the adapter reads its record itself
    manifest.allows_edit_to("pom.xml")   # R3's declaration, asked not assumed

**Depends on.** `studyforge.address`, `studyforge.version` for the R9 gate,
`studyforge.archive.scrub` for R7's, and the standard library. ⛔ Nothing
source-specific (R1), asserted over the whole of `src/` rather than promised.

⭐ **This file is where a corpus's customisation lives** (onboarding writes it).
Everything that differs between two sources and is not the source's own
content is a field here — which is what makes "every artifact is generated" and "every
corpus is different" both true at once. ⛔ If a corpus needs something this
file cannot express, **the manifest is missing a field**, and that is the
finding; it is never a hand-edit to generated output.

## What is in the package

| Module | Owns |
|---|---|
| `document` | `Manifest`, `parse`, `load` — the document and its version (R9) |
| `content` | `include` / `exclude` / `not_material`, and `classify` (C2) |
| `edits` | `permitted_edits`, the three targets R3 never permits, and the undo |
| `media` | the commit mode and its limits; an absent key is a **stated** default |
| `runtimes` | the closed vocabulary of runtimes, spelled once, and its refusals |
| `curriculum` | where the curriculum is recorded, its groups' addresses, the prefix check |
| `fields` | the plain field rules `document` applies, one function per key |
| `errors` | `ManifestError`, the only exception it raises, and R7's one phrase about a path |

⛔ **An unknown `corpus_api` is refused, never migrated at read time** (R9),
and the test itself is `studyforge.version`'s — this package owns the *set* of
versions it speaks, not the check.
⛔ **A file matching none of `include`, `exclude` and `not_material` is
unclassified**, and that is a refusal at validate time, not a shrug — silence
is the failure C2 describes. ⚠️ **A file matching `include` *and*
`not_material` is refused too**, under its own rule, because a precedence
between them would decide by rule order what nobody declared.
"""

from __future__ import annotations

from studyforge.archive.scrub import PersonalDataLeak
from studyforge.corpus.manifest.content import (
    MIN_WHY_CHARS,
    Classification,
    ContentPolicy,
    Exclusion,
    NotMaterial,
    parse_content,
)
from studyforge.corpus.manifest.curriculum import (
    Curriculum,
    DeclaredContainer,
    parse_curriculum,
    prefix_of,
)
from studyforge.corpus.manifest.document import (
    CORPUS_API,
    KEY_VERSIONS,
    KNOWN_CORPUS_API,
    MANIFEST_FILENAME,
    MANIFEST_KEYS,
    PLACEMENT_PROFILES,
    REQUIRED_KEYS,
    Manifest,
    from_document,
    load,
    parse,
    versions_needed,
)
from studyforge.corpus.manifest.edits import (
    EDIT_KINDS,
    PermittedEdit,
    Reversal,
    parse_edits,
)
from studyforge.corpus.manifest.errors import ManifestError
from studyforge.corpus.manifest.fields import ONBOARDING_DOC
from studyforge.corpus.manifest.media import (
    COMMIT_MODES,
    DEFAULT_MEDIA,
    MediaPolicy,
    parse_media,
)
from studyforge.corpus.manifest.runtimes import (
    NO_RUNTIMES,
    REQUIRES_JAVA,
    RUNTIMES,
    SOURCE_SUFFIXES,
    parse_runtimes,
    source_suffixes,
)

#: ⛔ **What `parse`, `load` and `from_document` let out, as a tuple a caller
#: catches**, rather than a paragraph in `errors.py` a caller retypes.
#: `ManifestError`, plus `PersonalDataLeak`, which is never wrapped (R7).
#:
#: ⚠️ **`AddressError` is NOT a member, deliberately.**
#: `errors.py` names it as a pass-through of `Manifest.parse_key`, which no
#: reader calls; `parse` translates `studyforge.address`'s slug refusal in `fields.slug_of`. A
#: caller of `parse_key` catches `AddressError` itself.
#:
#: ⛔ **Adding a member costs a fixture**: `tests/studyforge/corpus/manifest/`
#: reaches every one from `parse`, so a member nothing raises fails too.
RAISES: tuple[type[Exception], ...] = (ManifestError, PersonalDataLeak)

#: ⛔ The package's whole public surface. A consumer that has to import
#: `studyforge.corpus.manifest.document` directly is a consumer this contract
#: failed.
__all__ = [
    "COMMIT_MODES",
    "CORPUS_API",
    "DEFAULT_MEDIA",
    "EDIT_KINDS",
    "KEY_VERSIONS",
    "KNOWN_CORPUS_API",
    "MANIFEST_FILENAME",
    "MANIFEST_KEYS",
    "MIN_WHY_CHARS",
    "NO_RUNTIMES",
    "ONBOARDING_DOC",
    "PLACEMENT_PROFILES",
    "RAISES",
    "REQUIRED_KEYS",
    "REQUIRES_JAVA",
    "RUNTIMES",
    "SOURCE_SUFFIXES",
    "Classification",
    "ContentPolicy",
    "Curriculum",
    "DeclaredContainer",
    "Exclusion",
    "Manifest",
    "ManifestError",
    "MediaPolicy",
    "NotMaterial",
    "PermittedEdit",
    "Reversal",
    "from_document",
    "load",
    "parse",
    "parse_content",
    "parse_curriculum",
    "parse_edits",
    "parse_media",
    "parse_runtimes",
    "source_suffixes",
    "prefix_of",
    "versions_needed",
]
