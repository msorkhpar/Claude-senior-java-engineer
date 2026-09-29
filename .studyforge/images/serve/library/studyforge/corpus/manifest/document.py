"""`corpus.json` — the file that makes a directory a source.

**What it does.** Reads and validates the manifest, and hands back one
immutable `Manifest` carrying every declaration a corpus makes about itself.

**How you use it.** `parse(text)` for the document, `load(path)` for the file.
⛔ `parse` does no I/O and `load` is four lines on top of it, so everything a
test needs to say can be said without a filesystem.

**Depends on.** `studyforge.address` for what a slug is, and this package's
`content`, `edits`, `media`, `runtimes` and `errors`. ⛔ Nothing source-specific, ever
(R1) — `tests/studyforge/corpus/manifest/test_document.py` asserts that of the
whole of `src/`, not just of this module.

## The three questions that are three answers

⚠️ **`variants` is a filing and presentation key and nothing more.** It says
how the archive is partitioned and what a variant selector offers the reader.
⛔ It never implies anything is buildable, runnable or gradable — that is
declared per exercise (§7) — and it is **not** a code fence's language, which
is a block's own attribute from the archive. The extraction source blocked
eight SQL courses for exactly this reason: one list answered both *"can this
be filed here?"* and *"can we generate a test for it?"*, so a language with no
grader could not be filed at all. Three questions, three answers, none of them
derived from another.

⭐ **There is a test that would have caught that**, and it is not a comment:
`test_no_module_maps_a_variant_to_a_capability` refuses a module-level
collection under `src/` whose name suggests one — which is what the extraction
source's `LANGUAGES` tuple was.

## Where depth is declared, and where it is checked

**This module declares it.** `levels` names the container levels, and
`len(levels)` is the corpus's depth. ⛔ **It does not check an address against
that depth** — `studyforge.address` owns the comparison, and a second check here with a
different message is how two tasks come to disagree about which is
authoritative.

⭐ `Manifest.parse_key(key)` is where the two halves meet: it supplies this
manifest's depth to `studyforge.address`'s `parse_key`, so no caller ever writes
`parse_key(key, len(manifest.levels))` and no caller ever gets it wrong.
"""

from __future__ import annotations

import json
from dataclasses import dataclass, field
from pathlib import Path

from studyforge.address import Address, parse_key
from studyforge.archive.scrub import assert_clean
from studyforge.corpus.manifest.content import (
    EACH_DIRECTORY_API,
    ContentPolicy,
    each_directory,
    parse_content,
)
from studyforge.corpus.manifest.curriculum import Curriculum, parse_curriculum
from studyforge.corpus.manifest.edits import PermittedEdit, parse_edits
from studyforge.corpus.manifest.errors import ManifestError
from studyforge.corpus.manifest.fields import (
    ONBOARDING_DOC,
    exercises_of,
    levels_of,
    narration_of,
    onboarding_doc_of,
    slug_of,
    title_of,
    variants_of,
)
from studyforge.corpus.manifest.media import MediaPolicy, parse_media
from studyforge.corpus.manifest.runtimes import NO_RUNTIMES, parse_runtimes
from studyforge.describe import describe, describe_keys
from studyforge.version import check as check_version

#: The manifest's filename. One spelling, because "what makes a directory a
#: source" is a question every tool in this project asks.
MANIFEST_FILENAME = "corpus.json"

#: The version this build writes, and every version it can read. ⛔ An unknown
#: one is refused and never migrated in place (R9): a migration that runs
#: because something merely wanted to render a page rewrites the record of
#: what was ingested.
#:
#: ⭐ **`2` added `content.not_material`**, ⭐ **`3` added
#: `media.max_files`**, ⭐ **`4` added `runtimes`**, ⭐ **`5`
#: added `narration`**, ⭐ **`6` added `onboarding_doc`** and
#: ⭐ **`7` added `curriculum`**, ⭐ **`8` added `curriculum.linked` and
#: the `*/name` form of a `not_material` glob**, and no
#: bump is about old manifests — each key is optional and an absent one has a
#: stated default, so every `1` still parses. ⛔ **A bump is about a manifest
#: that *uses* the key being unreadable to an older build**, which reports an
#: unknown key and blames the corpus for the framework's age. That is exactly
#: what R9 versions, and the field and the bump therefore land in one commit.
#:
#: ⚠️ **The accepted set is spelled out rather than derived from
#: `CORPUS_API`.** A set built as `{1, CORPUS_API}` silently stops speaking
#: `2` on the day somebody writes `3`, and the refusal for an unknown version
#: has to stay exactly as sharp as it is for `6` today.
CORPUS_API = 8
KNOWN_CORPUS_API = frozenset({1, 2, 3, 4, 5, 6, 7, 8})

#: The `corpus_api` each key added after version 1 requires, keyed by the block
#: it lives under and its name.
#:
#: ⚠️ **ONE map over every block, not one map per block.** It was
#: `CONTENT_KEY_VERSIONS` while `content` was the only block that had grown a
#: key, and the convention was written down as *"an entry in
#: `CONTENT_KEY_VERSIONS` **if it lives under `content`**"* — leaving the other
#: half of the sentence to whoever added a key somewhere else. ⛔ **A second
#: map beside the first is the branch beside the branch this constant's own
#: note warned about**: the gate would have been complete for one block and
#: silently absent for the rest.
#:
#: ⛔ **A TOP-LEVEL key is keyed under the block `None`**, so a top-level key
#: is gated by version exactly as a nested one is.
#:
#: ⭐ **On the package's surface**, to be read and never written, so onboarding's
#: `promote` raises the version it writes from THIS map rather than from one
#: constant per key, and a key a later version adds is covered with no edit there.
KEY_VERSIONS: dict[tuple[str | None, str], int] = {
    ("content", "not_material"): 2,
    ("media", "max_files"): 3,
    (None, "runtimes"): 4,
    (None, "narration"): 5,
    (None, "onboarding_doc"): 6,
    (None, "curriculum"): 7,
    ("curriculum", "linked"): 8,
}

#: The placement profiles that may be declared. ⚠️ **`placement.profile` owns the profiles;
#: this is only the set a manifest may name**, and the two must not drift.
#: This constant is where a third profile is registered, beside its
#: definition in `corpus.placement`.
PLACEMENT_PROFILES = ("tree", "sibling")

#: Every key a manifest may carry, in the order §4 writes them.
MANIFEST_KEYS = (
    "corpus_api",
    "source",
    "title",
    "levels",
    "variants",
    "curriculum",
    "exercises",
    "runtimes",
    "narration",
    "onboarding_doc",
    "placement",
    "content",
    "media",
    "permitted_edits",
)

#: The ones with no default.
REQUIRED_KEYS = (
    "corpus_api",
    "source",
    "title",
    "levels",
    "variants",
    "exercises",
    "placement",
    "content",
)


@dataclass(frozen=True, slots=True)
class Manifest:
    """One corpus's declarations about itself. Immutable once validated."""

    source: str
    title: str
    levels: tuple[str, ...]
    variants: tuple[str, ...]
    exercises: bool
    placement: str
    content: ContentPolicy
    media: MediaPolicy
    permitted_edits: tuple[PermittedEdit, ...] = field(default=())
    #: Sorted names from `runtimes.RUNTIMES`; empty is *no runner* (§7, C5).
    runtimes: tuple[str, ...] = NO_RUNTIMES
    #: Whether a build and a serve voice this corpus. ⭐ **Absent is
    #: `True`**, which is every corpus's behaviour before the key existed: a
    #: record's clips play. `False` is the reading floor exactly — never short.
    narration: bool = True
    #: Where onboarding writes its reader document, root-relative.
    #: ⭐ **Absent is `ONBOARDING.md` at the root**, as before the key; `None`
    #: (declared `false`) is no reader document at all.
    onboarding_doc: str | None = ONBOARDING_DOC
    #: Where the curriculum is recorded and what its groups are filed at
    #: ⭐ **Absent is `None`**: the adapter reads its record itself,
    #: as every corpus did before the key.
    curriculum: Curriculum | None = None
    corpus_api: int = CORPUS_API

    @property
    def depth(self) -> int:
        """How many container levels this corpus has — the address arity, declared here."""
        return len(self.levels)

    def parse_key(self, key: str) -> Address:
        """Return the `Address` `key` names, checked against **this** corpus's depth.

        ⭐ The one place the declaration and the comparison meet. Call this
        rather than `studyforge.address.parse_key(key, len(manifest.levels))`:
        the second spelling is where a caller eventually passes the wrong
        number.
        """
        return parse_key(key, self.depth)

    def allows_edit_to(self, path: str) -> bool:
        """Whether this corpus declared an edit to `path` (R3).

        ⛔ `validate.nondestructive` asks this rather than knowing any corpus's exception.
        """
        return any(edit.path == path for edit in self.permitted_edits)


def parse(text: str, where: str = MANIFEST_FILENAME) -> Manifest:
    """Build a `Manifest` from the text of a `corpus.json`."""
    try:
        document = json.loads(text)
    except (TypeError, json.JSONDecodeError) as exc:
        raise ManifestError(f"{where} is not valid JSON: {exc}") from None
    if not isinstance(document, dict):
        raise ManifestError(f"{where} must be a JSON object, got {type(document).__name__}")
    return from_document(document, where)


def load(path: str | Path) -> Manifest:
    """Read, parse and version-check one `corpus.json`."""
    path = Path(path)
    try:
        text = path.read_text(encoding="utf-8")
    except OSError as exc:
        # ⛔ `exc.strerror`, never `exc`: an OSError formats itself with the
        # filename it was given, and a refusal is read in a log and pasted
        # into a bug report. An absolute path in one is the user's home
        # directory (R7).
        reason = exc.strerror or exc.__class__.__name__
        raise ManifestError(f"cannot read {path.name}: {reason}") from None
    return parse(text, path.name)


def from_document(document: dict, where: str = MANIFEST_FILENAME) -> Manifest:
    """Build a `Manifest` from an already-parsed object.

    ⛔ **The personal-data gate runs over the whole decoded document** (R7),
    before any field is read. ⚠️ **Without it `corpus.json` — the corpus's
    front door — would gate nothing** while the archive, the container map and
    the overlay all do: a home path in `title` would validate green, and
    because `validate` is wired to report a leak here, nothing would look
    wrong from either side.

    ⭐ The fields this module validates are not the fields a leak turns up in:
    `title` is free authored text and `content.exclude[].why` is a sentence
    somebody wrote. That is why the gate reads the document rather than the
    fields, exactly as `unit.content` does.
    """
    _gate(document, where)
    corpus_api = _check_version(document, where)
    unknown = sorted(set(document) - set(MANIFEST_KEYS))
    if unknown:
        raise ManifestError(
            f"{where} has unknown key(s), {describe_keys(unknown)}; "
            f"this build reads {list(MANIFEST_KEYS)}"
        )
    missing = [key for key in REQUIRED_KEYS if key not in document]
    if missing:
        raise ManifestError(f"{where} is missing required key(s) {missing}")

    _check_key_versions(document, corpus_api, where)
    content = parse_content(document["content"])
    exercises = exercises_of(document["exercises"], where)
    levels = levels_of(document["levels"], where)
    return Manifest(
        source=slug_of(document["source"], f"{where} 'source'"),
        title=title_of(document["title"], where),
        levels=levels,
        variants=variants_of(document["variants"], where),
        exercises=exercises,
        placement=_placement_of(document["placement"], where),
        content=content,
        media=parse_media(document.get("media")),
        permitted_edits=parse_edits(document.get("permitted_edits"), content),
        runtimes=parse_runtimes(
            document.get("runtimes"), exercises=exercises, present="runtimes" in document
        ),
        narration=narration_of(document.get("narration", True), where),
        onboarding_doc=onboarding_doc_of(document.get("onboarding_doc", ONBOARDING_DOC), where),
        curriculum=(
            parse_curriculum(document["curriculum"], where, len(levels), levels=levels)
            if "curriculum" in document
            else None
        ),
        corpus_api=corpus_api,
    )


def _gate(document: dict, where: str) -> None:
    """Refuse a manifest carrying personal data, naming the shape and not the value.

    ⛔ **`PersonalDataLeak` is raised as itself, not translated** (R7). ⚠️ A
    module that cites a neighbour's translation as its justification spreads
    one translating site into many.
    ⭐ *A promise with one exception is not a promise* is answered where it
    belongs: `manifest/errors.py` names what crosses, rather than swallowing it.

    ⛔ It is already load-bearing. `validate/corpus.py` catches `ManifestError`
    and **then** `PersonalDataLeak`; while this translated, the second arm
    could never fire, and a home path in `corpus.json` was filed under
    `RULE_MANIFEST` rather than `RULE_PERSONAL_DATA`.
    """
    assert_clean(document, where)


def _check_version(document: dict, where: str) -> int:
    """Return the `corpus_api` declared, refusing one this build cannot speak (R9).

    ⛔ The test itself is `studyforge.version`'s, not this module's: R9
    versions several contracts, and a second copy is the one people forget. What stays here is
    the set — `KNOWN_CORPUS_API` — because which versions a manifest may
    declare is this contract's business and nobody else's.

    ⚠️ **The checked version is returned rather than discarded**: a
    `Manifest` that always reported the default would report the truth only
    while `KNOWN_CORPUS_API` held one number. ⛔ A manifest declaring `1` must
    say `1`, because the
    field records what the corpus declared and not what this build writes.
    """
    return check_version(
        "corpus_api",
        document.get("corpus_api"),
        KNOWN_CORPUS_API,
        where=where,
        error=ManifestError,
    )


def _check_key_versions(document: dict, corpus_api: int, where: str) -> None:
    """Refuse a key from a version this manifest does not declare (R9).

    ⛔ **The version is the corpus's statement of which contract it was written
    to, and it is never inferred from the keys present.** A manifest using
    `not_material` under a `1` is unreadable to exactly the build it claims to
    be readable by, which is the whole thing R9 versions — and the refusal is
    a raise naming both numbers, never a quiet upgrade of the declaration.

    ⚠️ **Every gated block, not one of them.** The check ran over `content`
    alone while `content` was the only block that had grown a key; a key added
    under `media` would have shipped ungated and the omission would have looked
    exactly like a decision.
    """
    for name, needed in versions_needed(document):
        if corpus_api < needed:
            raise ManifestError(
                f"{where} declares corpus_api {corpus_api} and uses '{name}', "
                f"which corpus_api {needed} added; declare corpus_api {needed}. The "
                f"version is what tells an older build it cannot read this manifest, "
                f"and it is never inferred from the keys present."
            )


def versions_needed(document: dict) -> list[tuple[str, int]]:
    """Every key and form `document` uses that a later version added, with that version.

    ⭐ **One answer for the gate and for the writer**: `parse` refuses by it
    and onboarding's `promote` writes the version it needs, so no key or form a
    version adds can be written under a version that refuses it.
    ⚠️ A form is not a key: rule 1b's `*/name` glob is a value under
    `content.not_material`, and an older build refuses it just the same.
    """
    needed = []
    for (block, key), version in KEY_VERSIONS.items():
        declared = document if block is None else document.get(block)
        if isinstance(declared, dict) and key in declared:
            needed.append((key if block is None else f"{block}.{key}", version))
    content = document.get("content")
    entries = content.get("not_material") if isinstance(content, dict) else None
    if isinstance(entries, list) and any(
        isinstance(entry, dict)
        and isinstance(entry.get("glob"), str)
        and each_directory(entry["glob"])
        for entry in entries
    ):
        needed.append(("content.not_material */<name>", EACH_DIRECTORY_API))
    return needed


def _placement_of(value: object, where: str) -> str:
    """One of the declared placement profiles (spec §5)."""
    if value not in PLACEMENT_PROFILES:
        raise ManifestError(
            f"{where} 'placement' must be one of {list(PLACEMENT_PROFILES)}, got {describe(value)}"
        )
    return value
