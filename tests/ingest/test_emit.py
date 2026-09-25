"""The adapter's whole obligation: what it emits is what `validate` accepts.

**What it does.** Emits this corpus into a temporary directory and validates it there. ⭐ One assertion, and it is the definition of done — there is no other agreement between an adapter and the framework.

**How you use it.** `python3 -m pytest tests/ingest/test_emit.py` from the corpus root.

**Depends on.** `ingest.emit` and `studyforge.validate`. ⛔ Never on the archive already on disk: a test that read the last run's output would pass on a corpus this run could no longer produce.
"""

from __future__ import annotations


import json
import shutil
import warnings
from pathlib import Path

from studyforge.corpus.container import CONTAINER_FILENAME
from studyforge.validate import validate
from studyforge.validate.source import SKIP_DIRS, repository_ignores, source_files

from ingest.emit import emit

CORPUS_ROOT = Path(__file__).resolve().parents[2]

#: Fixed, never today's date. ⚠️ Reproducible: two runs differ only in `ingested`, so a
#: test that passed a moving date could not compare two runs byte for byte.
INGESTED = "2026-01-01"

#: What this suite's own emission writes at the corpus root, so a copy never
#: carries the last run's. ⛔ Not an ignore list: everything else a copy leaves
#: behind is the repository's own answer, read through `repository_ignores`.
WRITTEN_HERE = ("archive", ".archive-staging")

#: ⛔ What a copy leaves out by name is `validate`'s own `SKIP_DIRS`, and only at
#: the corpus root, as `validate` asks it. A nested store is copied, so
#: `validate` refuses it by name in the copy. No store name is typed here.

#: Said, never assumed, when there is no ignore declaration to read.
UNDECLARED = (
    "this corpus root is not a git working tree (an export, say), so there is no "
    "ignore declaration to read and nothing was left out as ignored: the copy is "
    "every file but the archive. Run this suite in a working tree to copy less."
)


def _copy(tmp_path: Path) -> Path:
    """This corpus, less its archive and what it ignores, where a test may write."""
    where = tmp_path / "corpus"
    declared = repository_ignores(CORPUS_ROOT, []) is not None
    if not declared:
        warnings.warn(UNDECLARED, stacklevel=2)
    stores = frozenset(source_files(CORPUS_ROOT).stores if declared else ())

    def left_out(directory: str, names: list[str]) -> set[str]:
        return _left_out(Path(directory), names, declared=declared, stores=stores)

    shutil.copytree(CORPUS_ROOT, where, ignore=left_out)
    return where


def _left_out(
    here: Path, names: list[str], *, declared: bool, stores: frozenset
) -> set[str]:
    """What one directory's copy skips: this suite's output, and what git ignores.

    ⭐ Asked once per directory, so an ignored directory is never walked, let
    alone copied. ⛔ A repository that stops answering part-way refuses the
    copy: a copy that quietly took everything would test the wrong tree.
    ⛔ A nested store `validate` refuses is copied and never asked about.
    """
    left: set[str] = set()
    if here == CORPUS_ROOT:
        left = {name for name in names if name in SKIP_DIRS or name in WRITTEN_HERE}
    if not declared or any(here.is_relative_to(store) for store in stores):
        return left
    asked = [here / name for name in names if name not in left]
    asked = [path for path in asked if path not in stores]
    ignored = repository_ignores(CORPUS_ROOT, asked)
    if ignored is None:
        raise RuntimeError("git stopped answering part-way through the copy")
    return left | {path.name for path in ignored}


def _emitted(root: Path) -> list:
    """Every JSON file under `root`, with its bytes — what the reproducibility check compares."""
    return sorted(
        (path.relative_to(root).as_posix(), path.read_bytes())
        for path in root.rglob("*.json")
    )


def test_what_this_adapter_emits_is_what_validate_accepts(tmp_path):
    # ⭐ The whole obligation, in one assertion. Everything else in this
    # package exists to make this line reachable.
    root = _copy(tmp_path)
    written = emit(root, ingested=INGESTED)
    assert written, "the emission wrote nothing"
    report = validate(root)
    assert report.ok, "\n".join(report.lines())


def test_two_runs_produce_the_same_bytes(tmp_path):
    # ⭐ Reproducibility. `ingested` is held fixed above, so anything that differs between
    # these two runs is non-determinism in the reader — a dict order, a
    # directory listing, a set — and every one of them is a real defect.
    root = _copy(tmp_path)
    emit(root, ingested=INGESTED)
    first = _emitted(root)
    emit(root, ingested=INGESTED, replace=True)
    assert _emitted(root) == first, "two runs of this adapter disagree"


def test_every_container_is_dated_with_its_documents(tmp_path):
    # ⛔ One run, one date. `emit` applies the run's date after `read`,
    # so a container map can never carry a date its documents were not given.
    root = _copy(tmp_path)
    emit(root, ingested=INGESTED)
    archive = root / "archive"
    maps = sorted(archive.rglob(CONTAINER_FILENAME))
    documents = [p for p in archive.rglob("*.json") if p.name != CONTAINER_FILENAME]
    assert maps and documents, "nothing to compare: no container map or no document"
    stray = {}
    for path in [*maps, *sorted(documents)]:
        when = json.loads(path.read_text(encoding="utf-8"))["ingested"]
        if when != INGESTED:
            stray[path.relative_to(root).as_posix()] = when
    assert not stray, f"dated otherwise than this run ({INGESTED}): {stray}"
