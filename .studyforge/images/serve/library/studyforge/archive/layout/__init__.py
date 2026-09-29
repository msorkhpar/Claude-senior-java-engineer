r"""Where an adapter writes every file, computed once so no adapter retypes it.

**What it does.** Turns *(corpus root, address, variant, unit, kind, ordinal)*
into the exact paths `studyforge validate` will walk — the manifest, each
container map, each unit directory, each archive document — plus the staging
path §6 requires a writer to build at before it moves anything into place.

**How you use it.** `Layout(root, archive_dir)`, then ask it for a path:

    layout = Layout(root, archive_dir="archive")
    layout.manifest                                   # <root>/corpus.json
    layout.container_map(address)                     # .../container.json
    layout.document(address, "prose", 3, "lesson", 2)

`archive_tree()` is the same arithmetic drawn for a reader — the four places an
adapter writes, with the values replaced by placeholders — so a page shows the
layout instead of retyping it.

**Depends on.** `address` (for `Address`, `unit_name` and `require_ordinal`)
`corpus.container`/`corpus.manifest` for the two filenames they own,
`corpus.placement` for the archive root, the `raw/` segment and the `units/`
segment, and `unit.content` for the third filename — the authored overlay's,
which `content` joins and never spells.
⛔ Not on `validate`: this module says where to *write*, `validate` says
whether what was written is right, and a writer that imported its own judge
would be checking itself.

## ⭐ Why it lives in `archive` and not in the adapter skill

⛔ **A build and the study server read the archive through this package**, and a
learner's copy of a course carries the server with no skill in it
(`studyforge.release`). ⭐ So the arithmetic is the archive's, and the adapter
skill re-exports it unchanged: an adapter still takes its whole vocabulary from
`studyforge.skills.adapter` (R19).

## ⛔ Why this is framework code and not four lines in every adapter

⚠️ R19: **anything a second source would have to retype is a hole in the
skills.** Path arithmetic is the purest instance — `raw/<variant>/unit-03/
lesson-2.json` is five joins, four of which are silent when wrong. An adapter
that writes to `unit-3/` instead of `unit-03/` produces a tree `validate`
reports as *unit missing*, at the reader rather than at the writer.

## ⛔ All THREE archive segments are IMPORTED; none is a literal here

⛔ **`ARCHIVE_DIR`, `RAW_DIR` and `UNITS_DIR` are `corpus.placement`'s
`ARCHIVE_DIRNAME`, `RAW_DIRNAME` and `UNITS_DIRNAME`, never a literal here**,
because a value a second package needs has one exported home (R21): three
spellings of the archive root let `plan` print a root neither this module nor
`validate` read, and `raw` was minted twice — here and in
`validate.corpus`, off both surfaces — so the two writers of one segment could
disagree with nothing failing.

⭐ **The names below are BOUND RE-EXPORTS, not definitions.** An adapter's
whole vocabulary arrives through this package (R19), so the skill keeps its
own spelling on its own surface; what it may never keep is a second *value*.
⛔ Two instruments hold that: `tests/studyforge/corpus/placement/test_names.py`
fails on a second `archive` or `raw` literal anywhere in `src/`, and
`tests/studyforge/archive/layout/test_init.py` lays out a tree with this
module and asserts `validate` reads exactly the documents it wrote — ⚠️ a
literal compared against the same literal would agree with itself.

⚠️ **`units` is held by a THIRD instrument, because a scan by spelling cannot hold it.**
It cannot be counted the way the other two are: `"units"` is also a JSON **key**
— `container.json` and the contents document both carry a `"units"` array — so
a spelled-once scan reds modules that mint nothing. ⭐ The instrument therefore
asks what a literal is **used as**: a module-level name bound to the bare
segment and joined into a path anywhere in `src/` is a MINT; the same literal
read as a mapping key is not.

⚠️ `unit_name` is *not* re-derived either. It is on
`studyforge.address.__all__`, so it is imported from its one home (R21).
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

from studyforge.address import Address, require_ordinal, unit_name
from studyforge.archive.document import KINDS
from studyforge.corpus.container import CONTAINER_FILENAME
from studyforge.corpus.manifest import MANIFEST_FILENAME
from studyforge.corpus.placement import ARCHIVE_DIRNAME, RAW_DIRNAME, UNITS_DIRNAME
from studyforge.unit.content import CONTENT_FILENAME

#: The archive root `validate`, `plan` and a build read: placement's one spelling,
#: on this skill's surface. ⚠️ `Layout` still takes it as a field, and any other
#: value writes where `validate` reads nothing, which it reports as `no-archive`.
ARCHIVE_DIR = ARCHIVE_DIRNAME

#: The directory under a container that holds its documents, by variant:
#: placement's one spelling, on this skill's surface. ⚠️ One variant per
#: container, so this is one directory and not a search.
RAW_DIR = RAW_DIRNAME

#: The directory under a container that holds each unit's **own** files — the
#: ones that belong to the unit rather than to one of its variants: its media,
#: its attachments, and the authored overlay. ⛔ **Placement's one spelling, on
#: this skill's surface** — never a second literal here, because a second
#: literal is a second authority (R21). ⚠️ The value is load-bearing in BOTH trees and they are
#: required to agree: a build reads a unit's media from the archive's
#: `<address>/units/unit-NN/` and writes it under the site's `units/unit-NN/`.
#: ⭐ Pinned behaviourally by the fixtures as well as by the mint scan, because
#: a literal compared against itself agrees either way.
UNITS_DIR = UNITS_DIRNAME

#: What a document file is called. ⛔ The `kind` half is a **closed set** —
#: `archive.document.KINDS` — and this module refuses anything else rather
#: than writing a file `validate` will not recognise as a document at all.
DOCUMENT_SUFFIX = ".json"


#: ⛔ The package's whole public surface: a sub-package, so it is its own owner.
__all__ = [
    "ARCHIVE_DIR",
    "DOCUMENT_SUFFIX",
    "RAW_DIR",
    "TREE_ROOT",
    "UNITS_DIR",
    "Layout",
    "LayoutError",
    "archive_tree",
    "document_name",
]


class LayoutError(ValueError):
    """A path this module will not compute, because what was written would be unreadable.

    ⛔ The message names the field and the permitted class, never the offending
    value (R7): an adapter's first caller is a person's own script, and any
    string in it can be an absolute path.
    """


@dataclass(frozen=True, slots=True)
class Layout:
    """Every path one corpus's adapter writes, derived from its root."""

    root: Path
    archive_dir: str = ARCHIVE_DIR

    def __post_init__(self) -> None:
        """Normalise the root, and refuse an archive directory that is not one segment."""
        object.__setattr__(self, "root", Path(self.root))
        if not isinstance(self.archive_dir, str) or not self.archive_dir:
            raise LayoutError("archive_dir must be a non-empty str naming one directory")
        if "/" in self.archive_dir or "\\" in self.archive_dir or self.archive_dir.startswith("."):
            raise LayoutError(
                "archive_dir must be a single directory name under the corpus root, "
                "with no separator and no leading dot"
            )

    @property
    def manifest(self) -> Path:
        """The corpus manifest — the first file `validate` reads and the only one it requires."""
        return self.root / MANIFEST_FILENAME

    @property
    def archive(self) -> Path:
        """The archive root: every container map lives somewhere beneath it."""
        return self.root / self.archive_dir

    @property
    def staging(self) -> Path:
        """Where §6 says to build before moving anything into place.

        ⛔ Beside the archive rather than inside it. `validate` walks the
        archive with `rglob`, so a half-written tree left *inside* it is a tree
        the next run reports on — and the failure then reads as the reader's
        rather than the writer's.
        """
        return self.root / f".{self.archive_dir}-staging"

    def container_dir(self, address: Address | list | tuple) -> Path:
        """Return the directory holding one container's map — which **is** its address (§6)."""
        return self.archive / _address(address).key

    def container_map(self, address: Address | list | tuple) -> Path:
        """One container's map file."""
        return self.container_dir(address) / CONTAINER_FILENAME

    def variant_dir(self, address: Address | list | tuple, variant: str) -> Path:
        """Return the directory holding one container's documents for one variant."""
        if not isinstance(variant, str) or not variant:
            raise LayoutError("variant must be a non-empty str")
        return self.container_dir(address) / RAW_DIR / variant

    def unit_dir(self, address: Address | list | tuple, variant: str, unit: int) -> Path:
        """One unit's directory. ⚠️ `unit_name` owns the zero padding, not this module."""
        return self.variant_dir(address, variant) / unit_name(unit)

    def unit_files(self, address: Address | list | tuple, unit: int) -> Path:
        """One unit's own directory, beside `raw/` rather than inside it.

        ⛔ **Not `unit_dir`, and the difference is the variant.** That one is
        per *variant* and holds the archive documents; this one is per *unit*
        and holds everything a unit owns that is not a document:

        - an asset's or attachment's `local` path resolves against it — which
          is what lets a build find the file a lesson's `<img>` names;
        - the authored overlay sits in it, and `content` below is the one
          method that says where — ⛔ **this docstring does not spell it, and
          R21 is why: a second spelling is a second authority.**

        ⚠️ **This is stated here because it was stated nowhere in `src/`.** The
        two shipped fixtures both use the directory, the spec's contract table
        names a file inside it, and every reader of it was composing the path
        for itself — which is the second-authority failure `layout.py` exists
        to remove.
        """
        return self.container_dir(address) / UNITS_DIR / unit_name(unit)

    def content(self, address: Address | list | tuple, unit: int) -> Path:
        """One unit's authored overlay — **the** address, so no consumer derives it.

        ⛔ **Without this method nothing in `src/` says where an overlay sits, so
        every build that wanted one would invent a place, and two builds would
        invent two.** `unit_files` is the directory; this is
        the file, and it is the last half of R21's *located* for the one
        contract this framework only ever reads.

        ⭐ **Neither half is a literal here.** The directory is `UNITS_DIR`,
        which is placement's; the filename is `unit.content`'s
        `CONTENT_FILENAME`, which is the contract's own and is minted beside
        `content_api`. ⛔ This method joins them and mints nothing, which is
        what *named once* means: change the contract's filename and this
        address follows, because there is nothing here to forget to change.

        ⚠️ **Declaring where an overlay sits is not APPLYING one, and v1's
        build applies none.** ⛔ A unit that carries an overlay is still built
        from its archive documents alone — that verb is unowned and is not
        this address's to answer. ⭐ Saying so
        is the point: a reader who finds this method must not read it as the
        feature.
        """
        return self.unit_files(address, unit) / CONTENT_FILENAME

    def document(
        self,
        address: Address | list | tuple,
        variant: str,
        unit: int,
        kind: str,
        ordinal: int,
    ) -> Path:
        """One archive document.

        ⛔ `kind` is checked against a closed set here rather than left to the
        writer. A file named `chapter-1.json` lands in a unit directory,
        parses as JSON, and is invisible to every check `validate` runs on
        documents — the one failure shape this layout can prevent outright.
        """
        return self.unit_dir(address, variant, unit) / document_name(kind, ordinal)

    def relative(self, path: Path) -> str:
        """`path` as a report may name it: relative to the root, posix, never absolute (R7)."""
        try:
            return path.relative_to(self.root).as_posix()
        except ValueError:
            return path.name


def document_name(kind: str, ordinal: int) -> str:
    """Return the filename one document takes: `lesson-1.json`, `practice-3.json`."""
    if kind not in KINDS:
        raise LayoutError(f"kind must be one of {list(KINDS)}")
    return f"{kind}-{require_ordinal(ordinal)}{DOCUMENT_SUFFIX}"


#: What the drawn tree calls the corpus root. ⛔ One segment, so the drawing
#: joins with `/` and never has to know what a reader's own root looks like.
TREE_ROOT = "<corpus-root>"

#: The values the tree is COMPUTED at before it is shown. ⚠️ They are real —
#: `Address` refuses `<address>` and `unit_name` refuses a placeholder — so the
#: drawing is this module's own arithmetic and then a substitution, rather than
#: a picture of it somebody keeps in step by hand (R19).
_DRAWN_ADDRESS = Address(["a"])
_DRAWN_VARIANT = "v"
_DRAWN_UNIT = 1
_DRAWN_ORDINAL = 1

#: What a reader is shown where the tree was drawn at a value. ⛔ Keyed by PATH
#: SEGMENT and applied one segment at a time, never as a text replace: a
#: replace would be free to rewrite a character inside `archive`, `raw` or
#: `units`, which are the three segments `corpus.placement` owns and this
#: drawing must reproduce untouched.
_SHOWN_AS = {
    _DRAWN_ADDRESS.key: "<address>",
    _DRAWN_VARIANT: "<variant>",
    unit_name(_DRAWN_UNIT): "unit-NN",
    document_name(KINDS[0], _DRAWN_ORDINAL): "<kind>-N.json",
}


def archive_tree() -> str:
    """Return the four places an adapter writes, drawn as a page shows them.

    ⛔ **Every line is a path this module computed**, so a page that draws this
    tree is not a second spelling of the layout and cannot drift from it
    (R19). ⚠️ The line a skill was missing is the last one: a unit's
    **own** files — each asset's and attachment's `local`, and the authored
    overlay — sit beside `raw/`, never inside a variant, and an adapter that
    wrote them anywhere else produced a page whose every figure is a broken
    glyph while `validate` said nothing.

    ⭐ `<address>` stands for however many slugs the corpus declares levels,
    joined by `/`; the tree is drawn at one and the shape is the same at three.
    """
    layout = Layout(Path(TREE_ROOT))
    address, unit = _DRAWN_ADDRESS, _DRAWN_UNIT
    return "\n".join(
        (
            _shown(layout.manifest),
            _shown(layout.container_map(address)),
            _shown(layout.document(address, _DRAWN_VARIANT, unit, KINDS[0], _DRAWN_ORDINAL)),
            _shown(layout.unit_files(address, unit)) + "/",
        )
    )


def _shown(path: Path) -> str:
    """Return one computed path with each drawn value replaced by its placeholder."""
    inside = path.relative_to(Path(TREE_ROOT)).parts
    return "/".join((TREE_ROOT, *(_SHOWN_AS.get(part, part) for part in inside)))


def _address(address: Address | list | tuple) -> Address:
    """Accept what an archive document carries — a list of slugs — as well as an `Address`."""
    return address if isinstance(address, Address) else Address(address)
