"""Write the archive: build it beside its destination, then move it (§6).

**What it does.** Takes what `read` returned and writes every container map and every archive document at the paths `studyforge validate` walks. ⛔ Nothing reaches the destination until every file has been built, and every map and document of one run carries that run's one `ingested`.

**How you use it.** `emit(root, ingested='YYYY-MM-DD')` returns the paths it wrote, sorted.

**Depends on.** `studyforge.skills.adapter` for the layout, `studyforge.archive.document` and `studyforge.corpus` for the two formats. ⛔ Never on `read`'s internals — only on what it returns.
"""

from __future__ import annotations


import dataclasses
import shutil
from pathlib import Path

from studyforge.archive.document import build
from studyforge.archive.document import render as render_document
from studyforge.corpus.container import render as render_map
from studyforge.corpus.manifest import MANIFEST_FILENAME
from studyforge.corpus.manifest import load as load_manifest
from studyforge.skills.adapter import Layout, plan_for

from ingest import read


class EmitRefused(RuntimeError):
    """An emission this adapter will not perform, and the field that is why.

    ⛔ Names the field and what would settle it, never a path or a value.
    """


def emit(root, *, ingested: str, into=None, replace: bool = False) -> list[str]:
    """Write the whole archive, or write nothing at all.

    `root` is where the material and `corpus.json` are; `into` is where the
    archive goes, and defaults to `root`. ⭐ The two are separable so a test
    can emit this corpus into a temporary directory and validate it there,
    without the suite writing into the repository it is reading.
    """
    root = Path(root)
    target = Path(root if into is None else into)
    plan = plan_for(load_manifest(root / MANIFEST_FILENAME))
    layout = Layout(target, plan.archive_dir)
    if layout.archive.exists() and not replace:
        raise EmitRefused(
            "the archive directory already exists; pass replace=True to rebuild it. "
            "⛔ Nothing outside it is written, moved or renamed either way."
        )
    staging = Layout(layout.staging, plan.archive_dir)
    if layout.staging.exists():
        shutil.rmtree(layout.staging)
    written = []
    for reading in read.containers(root):
        # ⛔ One run, one date. The run's `ingested` is applied AFTER
        # `read`, never handed to it, so whatever date `read` recorded is replaced
        # and a map can never disagree with the documents beneath it.
        container = dataclasses.replace(reading, ingested=ingested)
        written.append(_write(staging.container_map(container.address),
                              render_map(container), staging))
        for fields in read.documents(root, container):
            document = build(source=plan.source, ingested=ingested, **fields)
            where = staging.document(
                document["address"],
                document["variant"],
                document["unit"],
                document["kind"],
                document["ordinal"],
            )
            written.append(_write(where, render_document(document), staging))
    if not written:
        shutil.rmtree(layout.staging, ignore_errors=True)
        raise EmitRefused(
            "read.containers returned nothing, so this run would move an empty "
            "archive into place. An empty corpus is a silent failure, not a result."
        )
    # ⛔ §6: only now does anything move. A map that could not be built has
    # already raised, and nothing was ever left at the path validate reads.
    if layout.archive.exists():
        shutil.rmtree(layout.archive)
    (layout.staging / plan.archive_dir).replace(layout.archive)
    shutil.rmtree(layout.staging, ignore_errors=True)
    return sorted(written)


def _write(path: Path, text: str, staging: Layout) -> str:
    """Write one staged file and return where it will land, root-relative, so no home path is printed."""
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")
    return staging.relative(path)
