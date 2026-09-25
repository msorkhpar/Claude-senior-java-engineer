"""Audit this ingest: what `validate` checks, plus the count only this side makes.

**What it does.** Runs `studyforge validate` and adds the one check it cannot: a unit count taken from the **source**, not from the archive. ⚠️ A check that recounts the parser's own output agrees with itself by construction and catches nothing.

**How you use it.** `python3 -m ingest.audit <corpus-root>`. Exit 0 means both halves agree.

**Depends on.** `studyforge.validate`, `studyforge.skills.adapter`, and this package's `read`.
"""

from __future__ import annotations


import sys
from pathlib import Path

from studyforge.corpus.container import CONTAINER_FILENAME
from studyforge.corpus.manifest import MANIFEST_FILENAME
from studyforge.corpus.manifest import load as load_manifest
from studyforge.skills.adapter import RAW_DIR, Layout, plan_for
from studyforge.validate import INVALID, UNUSABLE, validate

from ingest import read

#: Why the source-side count is the check that matters. ⛔ Reported as an
#: unchecked claim while it is unwritten — saying so is not the same as
#: saying the archive is fine.
UNCHECKED = (
    "expected_units is not written, so nothing has counted this corpus from its "
    "own source. `studyforge validate` recounts what the adapter wrote; only "
    "this function can disagree with it."
)

#: The rule id this module adds to the ones `validate` reports.
RULE_SOURCE_COUNT = "source-count"


def audit(root) -> tuple[list[str], int]:
    """Return the lines a person reads and the exit code a script reads."""
    root = Path(root)
    report = validate(root)
    lines = list(report.lines())
    code = report.exit_code
    expected = read.expected_units(root)
    if expected is None:
        lines.append(f"corpus.json: [{RULE_SOURCE_COUNT}] not checked — {UNCHECKED}")
        return lines, INVALID
    found = _on_disk(root)
    for key in sorted(set(expected) | set(found)):
        if expected.get(key) != found.get(key):
            lines.append(
                f"{key}: [{RULE_SOURCE_COUNT}] the source records "
                f"{expected.get(key, 0)} unit(s); the archive holds "
                f"{found.get(key, 0)}"
            )
            code = INVALID
    return lines, code


def _on_disk(root: Path) -> dict[str, int]:
    """Count the unit directories the archive actually holds, by address."""
    plan = plan_for(load_manifest(root / MANIFEST_FILENAME))
    layout = Layout(root, plan.archive_dir)
    found: dict[str, int] = {}
    for path in sorted(layout.archive.rglob(CONTAINER_FILENAME)):
        key = path.parent.relative_to(layout.archive).as_posix()
        raw = path.parent / RAW_DIR
        found[key] = sum(1 for unit in raw.rglob("unit-*") if unit.is_dir())
    return found


def main(argv=None) -> int:
    """Audit one corpus root named on the command line."""
    argv = list(sys.argv[1:] if argv is None else argv)
    if len(argv) != 1:
        print("usage: python3 -m ingest.audit <corpus-root>")
        return UNUSABLE
    lines, code = audit(argv[0])
    for line in lines:
        print(line)
    return code


if __name__ == "__main__":  # pragma: no cover - the command line
    raise SystemExit(main())
