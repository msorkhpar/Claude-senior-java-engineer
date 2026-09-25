"""The audit is written, and its count from the source agrees with the archive.

**What it does.** Asserts that `expected_units` has been written — while it returns `None` the audit reports an unchecked claim — and that the audit exits clean.

**How you use it.** `python3 -m pytest tests/ingest/test_audit.py` from the corpus root.

**Depends on.** `ingest.audit`. ⛔ Not on `validate` directly: the audit is the caller.
"""

from __future__ import annotations


from pathlib import Path

from studyforge.validate import OK

from ingest.audit import UNCHECKED, audit
from ingest.read import expected_units

CORPUS_ROOT = Path(__file__).resolve().parents[2]


def test_the_source_side_count_is_written():
    # ⛔ This is the check `studyforge validate` cannot make. Until it is
    # written the ingest has never been counted from anything but its own
    # output, and an output that agrees with itself has agreed with nothing.
    assert expected_units(CORPUS_ROOT) is not None, UNCHECKED


def test_the_audit_exits_clean_on_this_corpus():
    lines, code = audit(CORPUS_ROOT)
    assert code == OK, "\n".join(lines)
