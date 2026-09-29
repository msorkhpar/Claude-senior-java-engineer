"""The R9 gate: one implementation of *"is this a version I speak?"*.

**What it does.** Answers that question for every contract R9 versions, and
refuses the ones it does not — naming the contract, what was declared, and
what this build accepts.

**How you use it.** `check(contract, declared, accepted, where=..., error=...)`
returns the version or raises; `is_supported(declared, accepted)` is the
predicate, for a caller that reports rather than refuses. ⭐ Each contract's
own task owns **which** versions it accepts and passes them in; this module
owns only the test.

**Depends on.** `studyforge.describe`, which depends on nothing. ⛔ Deliberately
not on any contract's package: this is the module every one of them imports, so
a dependency in the other direction is a cycle waiting for the second caller.

## Why this is a module and not a convention

⛔ **`declared in accepted` is porous, and the hole is on the read path.**
`bool` is a subclass of `int` and `True == 1`, so a JSON `true` walks straight
through a check that accepts `1` — silently, with nothing raised, and the
document is then processed as though it declared a version it never declared.
`1.0` passes the same way. ⭐ **`isinstance(declared, bool)` is not a redundant
clause; it is the clause**, and the type is tested before the value.

⚠️ **How many contracts R9 versions is `CONTRACT_FIELDS` below, and it is not
restated here.** ⛔ A list written twice is a list that disagrees with itself.

⭐ The argument stands however many there are: one independent membership test
per contract is one chance per contract to write the porous one, in front of
authors who will never have met this defect. One extraction today, or N
divergent re-implementations later.

⛔ **The refusal is a raise, never a migration** (R9). A migration that runs
because something merely wanted to render a page rewrites the record of what
was ingested, and the reader has no way to know it happened.

## The `error` argument, and why the caller supplies it

⭐ Each package promises its own exception type — `corpus.manifest` documents
that reading a manifest raises `ManifestError` **and nothing else** — and that
promise is worth more than uniformity here. So `check` raises what the caller
names, and `VersionError` is only the default for callers with no type of
their own. One implementation of the *test* and one of the *message*; each
contract keeps its own front door.
"""

from __future__ import annotations

from collections.abc import Collection

from studyforge.describe import describe

#: The fields R9 versions, for the tree check in `tests/studyforge/
#: test_version.py` that refuses a second implementation. ⭐ `identity_api`
#: joined it the day its contract was minted, and so did `content_api`,
#: `site_api`, ⭐ **`toc_api`**, and ⭐ **`narration_api`** (R21) — which is the
#: convention this line asks for: a task that versions a new contract
#: registers it here in the same commit, or the guard cannot see it.
#:
#: ⚠️ **`narration_api` is read through `check` and NOT through `is_supported`,
#: which is the opposite of `site_api` below and is the same argument reaching
#: the other answer.** ⛔ The discovery cache may be discarded because the tree
#: rebuilds it; `.studyforge/narration.json` is rebuildable only by
#: re-synthesising every clip in the corpus, so discarding it spends the whole
#: cost the incremental pass exists to avoid, silently. R9's refusal is spent
#: by stopping.
#:
#: ⚠️ **`toc_api` versions TWO documents** — `toc.json` and the `status.json`
#: that annotates it — because they are two halves of one schema (spec §5's
#: register calls it *"the TOC schema version"* for both). ⛔ A build that
#: spoke one half and not the other could join a pair it does not understand.
#:
#: ⚠️ **`site_api` is registered here and is read through `is_supported`, not
#: through `check`** (R9). It is the one member of this tuple whose
#: contract does **not** raise on an unknown version: the discovery cache is
#: derived and rebuildable, so R9's refusal is spent by discarding the document
#: whole and re-deriving it rather than by stopping. ⛔ Nothing is migrated,
#: which is the part of R9 that is about all of them.
#:
#: ⭐ **Every `*_api` key the framework writes is here**, the skills' records
#: among them (the exercise bundle, its quiz document, the coverage report and
#: its plan, the source ledger, the onboarding and execution records and the
#: framework pin, and a standalone course's release manifest), and
#: `tests/studyforge/test_version.py` fails on one that is
#: written and not registered. A key registered here that nothing reads back
#: costs nothing; a reader of one compares through `check`.
#:
#: ⛔ **Two tasks appending here conflict, and the resolution is always
#: keep-both**. The explicitness is the mechanism — a tuple that
#: merges cleanly is one nobody had to look at — so a conflict here is the
#: guard working, and it recurred at the TOC schema version exactly as this
#: line predicted it would.
CONTRACT_FIELDS = (
    "corpus_api",
    "container_api",
    "raw_api",
    "api",
    "consuming_api",
    "identity_api",
    "content_api",
    "site_api",
    "toc_api",
    "narration_api",
    "progress_api",
    "personal_archive_api",
    "gates_api",
    "bundle_api",
    "quiz_api",
    "coverage_api",
    "plan_api",
    "ledger_api",
    "installed_api",
    "pin_api",
    "written_api",
    "release_api",
)


class VersionError(ValueError):
    """A contract version this build does not speak (R9).

    The default for callers with no exception type of their own. ⛔ Not a base
    class for the others: `ManifestError` is a `ValueError` for its own
    reasons, and making it inherit from this one would tie two packages
    together to save a line.
    """


def is_supported(declared: object, accepted: Collection[int]) -> bool:
    """Report whether `declared` is an integer version in `accepted`.

    ⛔ The `bool` clause is the point of this module. `True in {1}` is true in
    Python, so membership alone admits a JSON `true` wherever 1 is supported.
    A `float` is refused for the same reason with no special case: `1.0` is
    not an `int`.
    """
    return isinstance(declared, int) and not isinstance(declared, bool) and declared in accepted


def check(
    contract: str,
    declared: object,
    accepted: Collection[int],
    *,
    where: str,
    error: type[Exception] = VersionError,
) -> int:
    """Return `declared` if this build speaks it, or raise `error` (R9).

    ⛔ `where` is keyword-only and has no default. A refusal that cannot say
    which file it read is a refusal nobody can act on (R6), and the first
    reader of most of these messages is an integrator hand-writing the file.

    ⛔ **`contract` must be one of `CONTRACT_FIELDS`, and that is a gate rather
    than a note** (R9). This module's own convention already said a
    task that versions a new contract registers it in that tuple *in the same
    commit*; until now the only thing holding the convention was a tree test
    that a caller outside `src/` never runs. ⭐ Making the unregistered name
    unrepresentable is cheaper than listing the places it could arrive (R7):
    `contract` is interpolated into the refusal twice, so a `contract` that
    could be anything would be a refusal that could reproduce anything.
    """
    if contract not in CONTRACT_FIELDS:
        raise error(
            f"{describe(contract)} is not a contract this build versions; it "
            f"versions {list(CONTRACT_FIELDS)}, and a new one is registered in "
            f"CONTRACT_FIELDS in the commit that mints it."
        )
    if is_supported(declared, accepted):
        return declared  # type: ignore[return-value]
    raise error(
        f"{where} declares {_said(contract, declared)}; this build speaks "
        f"{contract} {sorted(accepted)}. An unknown version is refused and "
        f"never migrated in place — a migration that runs at read time "
        f"rewrites the record of what was ingested."
    )


def _said(contract: str, declared: object) -> str:
    """Describe what was declared: the number if it is one, the type if not.

    ⭐ A wrong *value* and a wrong *type* are different mistakes and deserve
    different sentences. "declares `corpus_api` 99" tells an integrator to
    upgrade; "declares `corpus_api` as a bool" tells them they wrote `true`
    where JSON wanted `1` — which echoing the value would have obscured, since
    Python prints `True` and the integrator wrote `true`.
    ⛔ Naming the type also means an unexpected payload is described rather
    than reproduced into the message (R7).

    ⭐ **This is `studyforge.describe` with the contract's name in front of
    it** (R7). ⛔ The argument above is why `describe` says *"a bool"*, rather
    than why this module keeps its own answer.
    """
    if declared is None:
        return f"no {contract}"
    described = describe(declared)
    return f"{contract} {described}" if described[:1].isdigit() else f"{contract} as {described}"
