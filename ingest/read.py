"""Read this source. ⛔ THE ONE MODULE IN THIS PACKAGE YOU WRITE BY HAND.

**What it does.** Turns this repository's material into the two things the archive is made of. ⭐ `containers` and `expected_units` are already written: `corpus.json` declares where the curriculum is recorded and what each group is filed at, and `studyforge.skills.adapter.curriculum` reads it and refuses a tree that disagrees. `documents` is yours.

**How you use it.** Write `documents`. Run the tests beside this package after it: they fail carrying the refusal's own message, and pass when the archive validates. ⛔ Change the filing in `corpus.json`, never here.

**Depends on.** `studyforge.corpus.manifest` for the declaration, `studyforge.skills.adapter.curriculum` for the filing and the name count, and `studyforge.corpus.container` for `Container`. ⭐ `studyforge.archive.markdown` reads Markdown into blocks for `documents`; ⚠️ add whatever else it needs to read this source's material.
"""

from __future__ import annotations


import dataclasses
import json
from pathlib import Path

from studyforge.archive.markdown import parse
from studyforge.corpus.container import Container
from studyforge.corpus.manifest import MANIFEST_FILENAME, load
from studyforge.exercise.bundle import BUNDLE_FILENAME, Places, bundle_of, emit
from studyforge.skills.adapter.curriculum import counted, filed

#: What every unwritten step says. ⭐ One sentence of what to return, and
#: one of why a guess would be worse than a refusal.
UNWRITTEN = (
    "ingest.read.{step} is not written yet. {what} "
    "Until it is, this adapter refuses rather than emitting an archive "
    "that validates and holds the wrong material."
)

#: The one variant this corpus declares.
VARIANT = 'prose'


def containers(root: Path) -> list[Container]:
    """Return one `Container` per group `corpus.json` declares, filed by its record.

    ⛔ **The address is recorded, never derived** (§6): the manifest records
    it, and the filing refuses when the record or a declared prefix disagrees.
    ⚠️ `ingested` is replaced by `emit` with the run's own date.
    """
    manifest = load(Path(root) / MANIFEST_FILENAME)
    return [
        Container(
            address=group.address,
            titles=group.titles,
            variant=VARIANT,
            ingested="1970-01-01",
            units=[
                dataclasses.replace(unit, practices=len(_practices(root, group.address, unit.n)))
                for unit in group.units
            ],
            origin=group.origin,
        )
        for group in filed(root, manifest)
    ]


def documents(root: Path, container: Container) -> list[dict]:
    """Return the fields for each document of `container`, in reading order.

    Each dict is the keyword arguments of `studyforge.archive.document.build`
    minus `source` and `ingested`, which `emit` supplies: `address`,
    `variant`, `unit`, `kind`, `ordinal`, `title`, `blocks`.

    ⛔ **Every block comes from the block vocabulary** (`studyforge.archive.blocks`).
    A construct the reader does not recognise is reported, never dropped: a
    dropped block is absent from the digest and from the counts alike, so
    nothing downstream can notice it went missing.

    ⭐ **Markdown material has a reader already**:
    `studyforge.archive.markdown.parse(text)` returns the blocks, and raises
    `MarkdownError` naming what it cannot hold rather than dropping it.
    """
    found = []
    for unit in container.units:
        found.append(
            {
                "address": container.address,
                "variant": VARIANT,
                "unit": unit.n,
                "kind": "lesson",
                "ordinal": 1,
                "title": unit.title,
                "blocks": parse((Path(root) / unit.origin).read_text("utf-8")),
            }
        )
        found.extend(_practices(root, container.address, unit.n))
    return found


#: The fields of an archive document `emit` in ingest.emit fills in itself.
_SUPPLIED = ("raw_api", "source", "ingested")

#: What `studyforge.archive.document.build` takes from a practice document.
_TAKEN = ("address", "variant", "unit", "kind", "ordinal", "title", "blocks", "starting_code", "exercise")


def _practices(root: Path, address, unit: int) -> list[dict]:
    """Return one practice document's fields per exercise bundle the unit carries, in ordinal order.

    ⭐ The exercises skill committed each bundle under `exercises/`, and a code
    bundle becomes its practice document through `studyforge.exercise.bundle.emit`.
    ⛔ Nothing is written here: the skill already created the reader's workspace
    files. ⚠️ This corpus has no quiz yet, so a quiz bundle is not read: one would
    stop the count at its ordinal, and `validate` would then name the unread bundle.
    """
    base = Path(root)
    unit_dir = base / Places(address, VARIANT, unit, 1).bundle.rsplit("/", 1)[0]
    found = []
    ordinal = 1
    while (unit_dir / f"practice-{ordinal}").is_dir():
        where = Places(address, VARIANT, unit, ordinal).bundle
        code = base / where / BUNDLE_FILENAME
        if code.is_file():
            bundle = bundle_of(json.loads(code.read_text(encoding="utf-8")), where)
            document = emit(base, bundle, source="-", ingested="1970-01-01").document
            found.append({key: document[key] for key in _TAKEN if key in document})
        else:
            break
        ordinal += 1
    return found


def expected_units(root: Path) -> dict[str, int] | None:
    """Return `{address key: unit count}` counted from the names on disk, or None.

    ⭐ **The second reading**: every included file carrying a declared prefix,
    or under a linked level, every included file in each linked file's
    directory, taken without the record. ⚠️ `None` while a declared group
    has no prefix, and the audit then reports an unchecked claim — count
    that group here.
    """
    return counted(Path(root), load(Path(root) / MANIFEST_FILENAME))
