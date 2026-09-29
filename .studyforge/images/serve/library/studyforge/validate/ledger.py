r"""Refuse a committed ledger that no longer accounts for a page the corpus carries.

**What it does.** Where a corpus commits the authoring pass's ledger, requires
it to account for every material page a pass was handed: the page is a file
the ledger read, each fenced example on it is a row that is the basis of an
exercise or carries a written reason, and so is each test file a unit's
coverage report declares as that page's grader. ⛔ Anything else is a finding.
⭐ A material page NO pass has been handed is **pending**: reported, counted and
named by module, and never a finding (`RULE_LEDGER_PENDING`).

**How you use it.** `CHECKS`, which `validate.run` drains like every other
check's tuple. Each function takes the `Walk` and yields `Finding`s.

**Depends on.** `validate.corpus` for the container maps and the root,
`validate.report`, `archive.scrub` for R7, `exercise.bundle` for the bundles' directory,
`studyforge.sourcepath` for what a report's path may be, and — deferred, see
below — `skills.exercises` for where the ledger is, how its rows are keyed and
read, and what a page's fences are. Standard library only.

## ⛔ WHY `validate` AND NOT ONLY THE PASS

⚠️ **A ledger can lose a whole container's rows and still read as 0
findings.** `exercises/ledger.json` is one file per corpus, and a pass that
rewrites it with one container's entries is the very thing that should know
better — so the proof that *nothing is lost* cannot live only in the
writer. ⭐ This check reads the ledger against what the corpus DECLARES, which
no pass can narrow: every unit's `origin` in every container map, and every
page and grader a committed coverage report names.

## ⭐ THE PAGE IS RE-SCANNED, NOT TRUSTED FROM THE LEDGER

⛔ **A ledger that lost a page's rows also lost the count of its fences**, so
the fences are read off the page on disk by `skills.exercises.scan` — the same
walk the ledger was taken with, never a second one. A page whose file is not
on disk is not this check's: nothing here can say what it carries.

## ⚠️ THE IMPORT IS DEFERRED, AND THAT IS THE CYCLE `membership` NAMES

`skills.exercises.scan` imports `validate.headings`, so importing the skill at
module level here would make importing the skill first a cycle through this
package's `__init__`. ⭐ The import is taken inside the check, as
`validate.source.membership` takes `skills.adapter`.

## ⭐ A PAGE NO PASS WAS HANDED IS PENDING, NOT UNACCOUNTED

⚠️ **Measured on a course authored one module at a time:** after the first
module's pass, every other page read as a finding (162 of them), and the one
way to a clean read was an author's `excuse` per entry — a reason kept while the
page's bytes are unchanged, so it outlived the authoring it deferred.
⭐ **So a page is pending until the first pass reads it**, and that is a visible
state and not a verdict: one `Unchecked` line counts the pages and names their
modules. ⛔ **A page a pass DID author and the ledger no longer reads is still a
finding**: its unit's committed coverage report names it, which is the evidence
it was handed — so a ledger clobbered by a one-container pass still reads as
the list of pages it lost, which is what this check exists for.

⛔ **Every check yields; none raises** (R6). ⭐ **Nothing happens without a
ledger**: a corpus with no authored exercises validates exactly as before.
"""

from __future__ import annotations

import json
from collections.abc import Iterator
from pathlib import Path

from studyforge.archive.scrub import PersonalDataLeak, assert_clean
from studyforge.exercise.bundle import BUNDLES_DIRNAME
from studyforge.sourcepath import is_source_path
from studyforge.validate.corpus import Walk
from studyforge.validate.report import Finding, Unchecked

#: A committed ledger that will not read as a ledger this build wrote.
RULE_LEDGER = "ledger"

#: ⛔ A page, a fence or a declared grader the committed ledger does not account for.
RULE_LEDGER_UNACCOUNTED = "ledger-unaccounted"

#: ⭐ Material pages no authoring pass has been handed yet: a state, never a finding.
RULE_LEDGER_PENDING = "ledger-pending"

#: ⛔ A ledger or coverage report carrying personal data. ⚠️ `validate.corpus`'
#: own spelling, because it is the same rule and two ids for one fact is two
#: audits.
RULE_PERSONAL_DATA = "personal-data"


def check_ledger_accounts(walk: Walk) -> Iterator[Finding]:
    """Every page a pass was handed, its every fence and its every grader is accounted for.

    ⭐ **One finding per page and per grader**, naming the first row missing,
    so a clobbered ledger reads as the list of pages it lost rather than as
    one line per fence.
    """
    from studyforge.skills.exercises import LEDGER_PATH

    path = walk.root / LEDGER_PATH
    if not path.is_file():
        return
    rows = yield from _rows(path, LEDGER_PATH)
    if rows is None:
        return
    pages, graders, authored, leaks = _declared(walk)
    yield from leaks
    read = {row["path"] for row in rows["sources"]}
    entries = _by_key(rows["entries"])
    pending: list[str] = []
    for page in sorted(pages):
        if not (walk.root / page).is_file():
            continue
        if page not in read and page not in authored:
            pending.append(page)
            continue
        if page not in read:
            yield _unaccounted(page, "was authored and the ledger no longer reads it")
            continue
        yield from _fences(walk.root, page, entries)
    if pending:
        yield _pending(walk, pending)
    for grader in sorted(graders):
        said = _ending(entries.get(f"tests:{grader}"))
        if said is not None:
            yield _unaccounted(grader, f"is a declared grader, and its ledger row {said}")


def _rows(path: Path, where: str):
    """Read the ledger's two row lists, yielding a finding and returning `None` if it will not.

    ⚠️ **Three `try` blocks, not one, on purpose.** The R7 gate is `archive`'s
    reader, and `tests/test_raises_convention.py` holds that a caller wrapping
    an `archive` reader catches only `archive`'s own exceptions there; the
    ledger's refusal is this package's, so it is caught around its own call.
    """
    from studyforge.skills.exercises import LedgerError, ledger_rows

    try:
        document = json.loads(path.read_text(encoding="utf-8"))
    except OSError:
        yield Finding(RULE_LEDGER, where, "could not be read.")
        return None
    except UnicodeDecodeError, ValueError:
        # ⛔ The kind of failure, never a value read out of the file (R7).
        yield Finding(RULE_LEDGER, where, "is not UTF-8 JSON, so it accounts for nothing.")
        return None
    try:
        assert_clean(document, where)
    except PersonalDataLeak as error:
        # ⛔ Its own rule, as `validate.exercises` has it.
        yield Finding(RULE_PERSONAL_DATA, where, str(error))
        return None
    try:
        return ledger_rows(document, where)
    except LedgerError as error:
        yield Finding(RULE_LEDGER, where, str(error))
    return None


def _declared(walk: Walk) -> tuple[set[str], set[str], set[str], list[Finding]]:
    """Return every material page, every declared grader, and every page a report names.

    ⭐ **Pages**: each unit's `origin` in each container map, and each page a
    committed coverage report names. **Graders**: each file a coverage report
    fingerprints beside its page — `corpus._fingerprint`'s page plus graders.
    ⛔ A path a report carries is a source path or it is not read: the report is
    a document somebody could edit, and this check opens what it names. ⛔ And
    each report is gated for personal data before a string of it is used (R7).
    """
    from studyforge.skills.exercises import COVERAGE_FILENAME

    pages = {
        unit.origin
        for held in walk.containers
        for unit in held.container.units
        if unit.origin is not None
    }
    graders: set[str] = set()
    authored: set[str] = set()
    leaks: list[Finding] = []
    for report in sorted(walk.root.glob(f"{BUNDLES_DIRNAME}/**/{COVERAGE_FILENAME}")):
        where = report.relative_to(walk.root).as_posix()
        document = _report(report)
        try:
            assert_clean(document, where)
        except PersonalDataLeak as error:
            leaks.append(Finding(RULE_PERSONAL_DATA, where, str(error)))
            continue
        page = document.get("page")
        digests = document.get("digests")
        if is_source_path(page) and isinstance(digests, dict):
            pages.add(page)
            authored.add(page)
            graders |= {path for path in digests if is_source_path(path) and path != page}
    return pages, graders, authored, leaks


def _report(path: Path) -> dict:
    """Read one coverage report, or nothing: ⛔ a report that will not read is not this rule's."""
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
    except OSError, UnicodeDecodeError, ValueError:
        return {}
    return value if isinstance(value, dict) else {}


def _by_key(entries: list[dict]) -> dict[str, dict]:
    """Every entry row by `row_key`, the one spelling of an entry's identity."""
    from studyforge.skills.exercises import row_key

    return {row_key(row): row for row in entries}


def _fences(root: Path, page: str, entries: dict[str, dict]) -> Iterator[Finding]:
    """Refuse a page whose fences the ledger does not each account for, naming the first."""
    from studyforge.skills.exercises import scan

    try:
        fences = scan((root / page).read_text(encoding="utf-8")).fences
    except OSError, UnicodeDecodeError:
        return
    keys = [f"example:{page}:{fence.ordinal}" for fence in fences]
    missing = [(key, said) for key in keys if (said := _ending(entries.get(key))) is not None]
    if missing:
        key, said = missing[0]
        yield _unaccounted(
            page,
            f"carries {len(fences)} fenced example(s) and {len(missing)} of them is not "
            f"accounted for, the first '{key}', whose ledger row {said}",
        )


def _ending(row: dict | None) -> str | None:
    """Return what is wrong with how a row ends, or `None` when it ends one way (spec §7 §3)."""
    if row is None:
        return "is missing"
    built = isinstance(row.get("exercises"), list) and bool(row["exercises"])
    reason = row.get("reason")
    excused = isinstance(reason, str) and bool(reason.strip())
    if built and excused:
        return "names an exercise and also a reason none was built"
    if not built and not excused:
        return "names no exercise and no reason"
    return None


def _pending(walk: Walk, pending: list[str]) -> Unchecked:
    """One line counting the pages no pass has been handed, and naming their modules."""
    from studyforge.skills.exercises import LEDGER_PATH

    module = {
        unit.origin: held.container.address.key
        for held in walk.containers
        for unit in held.container.units
        if unit.origin is not None
    }
    counts: dict[str, int] = {}
    for page in pending:
        name = module.get(page, "a coverage report")
        counts[name] = counts.get(name, 0) + 1
    named = ", ".join(f"{name} ({count})" for name, count in sorted(counts.items()))
    return Unchecked(
        RULE_LEDGER_PENDING,
        LEDGER_PATH,
        f"{len(pending)} material page(s) are pending: no authoring pass has been handed "
        f"them yet, so none is unaccounted. By module: {named}. The first pass that "
        f"reads a page ends its pending state",
    )


def _unaccounted(path: str, why: str) -> Finding:
    """One finding for one page or grader, filed against the ledger."""
    from studyforge.skills.exercises import LEDGER_PATH

    return Finding(
        RULE_LEDGER_UNACCOUNTED,
        LEDGER_PATH,
        f"'{path}' {why}. Every fenced example and every declared grader of every "
        f"material page is the basis of an exercise or carries a written reason (spec "
        f"§7 §3); run the authoring pass over that page's container, which keeps "
        f"every other page's rows.",
    )


#: Is there a ledger, and does it still account for the whole corpus.
CHECKS = (check_ledger_accounts,)
