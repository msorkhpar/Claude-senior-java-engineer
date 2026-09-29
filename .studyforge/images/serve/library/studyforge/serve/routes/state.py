r"""The state namespace: what THIS machine has, and what its reader has earned — never cached.

**What it does.** Answers under `/api/v1/state/`:

- (empty) or `corpora` → `state-index`: the corpora discovered, and discovery's report;
- `<corpus>` → `corpus-state`: `contents.status`'s local document, what is missing,
  every believed practice and every disagreement;
- `<corpus>/units/<segments>/unit-NN` → `unit-state`: one unit's pages, practices and
  disagreements.

Every answer is `json_response`'s `no-store`, with no validator of any kind.

**How you use it.** `route(discovered, request, rest)`; `serve.instance` binds the
first argument and registers the result under `NAMESPACE`.

**Depends on.** `serve.discovery`, `serve.addressing`, `contents` for the local
document, `corpus.discovery` for the verdict, `progress` for the record,
`archive.scrub` and `serve.response`.

## ⛔ Derived from the filesystem on every request

Each request re-runs its corpus's scan. The startup scan survives only as a digest,
and a tree changed since reads `since_startup: stale` — the answer is the new scan's.

## ⛔ A claim that something exists, with nothing on disk, is a DISAGREEMENT

A practice entry in the progress record claims its unit exists. It is reported under
`practices` only when the corpus declares the unit **and** a page on this machine
identifies itself as it; otherwise it is listed under `disagreements`, saying what it
claims and what the disk says, and no `passed` is reported for it.

## ⛔ A read mark is never a pass, and a run never completes anything

Read marks live in the browser (spec §8.5), and nothing a request carries
reaches this module — a route is given a path and nothing is read from its query,
headers or body — so `status` is built with no read marks and every `read` is false,
whatever the record holds. A practice's `passed` is its `first_passed_at` being set,
which `progress` does only for a test run exiting 0; `last.passed` is `is_pass`.
⭐ `last.cases` is the Submit's per-case breakdown or `null` — a report beside
that verdict, never a second one.

⛔ **A failure answers a fixed message**: `422` for a malformed record (left exactly
as it is, never reset), `500` for an unreadable one, a scan that failed, or the gate.
"""

from __future__ import annotations

from dataclasses import dataclass

from studyforge.archive.scrub import PersonalDataLeak
from studyforge.contents import found, missing, order, status, status_document
from studyforge.corpus.discovery import DiscoveryError, freshness
from studyforge.progress import CASES_KEY, ProgressFormatError, is_pass, parse_practice_key
from studyforge.progress import RAISES as PROGRESS_RAISES
from studyforge.serve.addressing import Located, locate
from studyforge.serve.discovery import Discovered, ServedCorpus
from studyforge.serve.response import API_PREFIX, Request, Response, error, json_response

#: The name this namespace is registered under.
NAMESPACE = "state"

CORPORA = "corpora"
UNITS = "units/"

NO_SUCH_STATE = "no such state"
NO_SUCH_CORPUS = "no such corpus"
NO_SUCH_UNIT = "no such unit"
MALFORMED = "the progress record is malformed, and it is left exactly as it is"
UNREADABLE = "the progress record could not be read"
UNSCANNABLE = "the corpus could not be scanned"
GATED = "state failed the personal-data gate"

#: What a disagreement says the disk shows instead.
NOT_DECLARED = "the corpus declares no such unit"
NO_PAGE = "no page on this machine identifies itself as this unit"


def route(discovered: Discovered, request: Request, rest: str) -> Response:
    """Answer one request under `/api/v1/state/`; `rest` is the path after it."""
    try:
        return _answer(discovered, rest)
    except PersonalDataLeak:
        return error(500, GATED)
    except ProgressFormatError:
        return error(422, MALFORMED)
    except PROGRESS_RAISES:
        return error(500, UNREADABLE)
    except DiscoveryError:
        return error(500, UNSCANNABLE)


def _answer(discovered: Discovered, rest: str) -> Response:
    """Dispatch on the path; every answer goes through `json_response` or `error`."""
    if rest in ("", CORPORA):
        return json_response(200, index(discovered))
    name, _, tail = rest.partition("/")
    served = discovered.by_source.get(name)
    if served is None:
        return error(404, NO_SUCH_CORPUS)
    if not tail:
        return json_response(200, corpus_state(served))
    if not tail.startswith(UNITS):
        return error(404, NO_SUCH_STATE)
    located = locate(f"{name}/{tail[len(UNITS) :]}", discovered.depths)
    payload = None if located is None else unit_state(served, located)
    return error(404, NO_SUCH_UNIT) if payload is None else json_response(200, payload)


def index(discovered: Discovered) -> dict:
    """Return what the instance discovered: each corpus, and every reported line."""
    return {
        "resource": "state-index",
        "corpora": [
            {
                "corpus": served.source,
                "profile": served.profile,
                "depth": served.depth,
                "root": served.relative.as_posix(),
                "state": f"{API_PREFIX}/{NAMESPACE}/{served.source}",
            }
            for served in discovered.corpora
        ],
        "report": list(discovered.report),
    }


@dataclass(frozen=True, slots=True)
class Observed:
    """One scan's reading of a corpus: its pages by unit key, and the verdict against startup."""

    present: frozenset[str]
    pages: dict[str, tuple[str, ...]]
    since_startup: str
    unidentified: tuple[dict, ...]


def observe(served: ServedCorpus) -> Observed:
    """Scan the corpus now. ⛔ Never the startup scan's artifacts."""
    site = served.rescan()
    pages: dict[str, list[str]] = {}
    for artifact in site.units:
        said = artifact.identity
        if said.corpus == served.source and said.unit is not None:
            pages.setdefault(said.address.unit_key(said.unit), []).append(
                served.href(artifact.path)
            )
    return Observed(
        present=found(site, served.source),
        pages={key: tuple(sorted(hrefs)) for key, hrefs in sorted(pages.items())},
        since_startup=freshness(site, served.digest),
        unidentified=tuple(
            {"path": served.href(item.path), "fault": item.fault} for item in site.unidentified
        ),
    )


def corpus_state(served: ServedCorpus) -> dict:
    """Return one corpus's state, derived from a scan taken now and the record read now."""
    observed = observe(served)
    contents = served.corpus.contents
    declared = frozenset(entry.key for entry in order(contents))
    local = status(contents, observed.present)  # ⛔ no read marks: they are the browser's
    believed, disagreements = _records(served, declared, observed.present)
    return {
        "resource": "corpus-state",
        "corpus": served.source,
        "profile": served.profile,
        "depth": served.depth,
        "discovery": _discovery(served, observed),
        "status": status_document(local),
        "missing": list(missing(contents, local)),
        "undeclared_pages": sorted(observed.present - declared),
        "unidentified": list(observed.unidentified),
        "practices": {key: summary for key, (_, _, summary) in believed.items()},
        "disagreements": disagreements,
    }


def unit_state(served: ServedCorpus, located: Located) -> dict | None:
    """Return one unit's state, or `None` when nothing declares, shows or records it."""
    observed = observe(served)
    declared = frozenset(entry.key for entry in order(served.corpus.contents))
    believed, disagreements = _records(served, declared, observed.present)
    unit = located.key
    practices = {section: summary for key, section, summary in believed.values() if key == unit}
    mine = [said for said in disagreements if said["unit"] == unit]
    pages = observed.pages.get(unit, ())
    if unit not in declared and not pages and not mine:
        return None
    return {
        "resource": "unit-state",
        "corpus": served.source,
        "key": unit,
        "declared": unit in declared,
        "present": unit in observed.present,
        "pages": list(pages),
        "discovery": _discovery(served, observed),
        "practices": practices,
        "disagreements": mine,
    }


def _records(
    served: ServedCorpus, declared: frozenset[str], present: frozenset[str]
) -> tuple[dict[str, tuple[str, str, dict]], list[dict]]:
    """Split the record into entries the disk agrees with and claims it does not."""
    believed: dict[str, tuple[str, str, dict]] = {}
    disagreements: list[dict] = []
    for key, entry in served.progress().read()["practices"].items():
        address, ordinal, section = parse_practice_key(key, served.depth)
        unit = address.unit_key(ordinal)
        if unit in declared and unit in present:
            believed[key] = (unit, section, _summary(entry))
            continue
        disagreements.append(
            {
                "practice": key,
                "unit": unit,
                "claims": "a pass" if entry["first_passed_at"] is not None else "a run",
                "but": NO_PAGE if unit in declared else NOT_DECLARED,
            }
        )
    return believed, disagreements


def _summary(entry: dict) -> dict:
    """Return what state reports of one believed practice entry.

    ⭐ **`last.cases` is published and `None` where there is none**:
    absent and empty are
    different claims, and the record refuses an empty map. ⛔ **It is a REPORT
    and not a second `passed`** — the key beside it is `is_pass` and this one
    never touches it, so a Submit that exited zero with a failed edge is
    published as exactly that.
    """
    last = entry["last"]
    return {
        "passed": entry["first_passed_at"] is not None,
        "first_passed_at": entry["first_passed_at"],
        "runs": entry["runs"],
        "last": {
            "at": last["at"],
            "mode": last["mode"],
            "exit": last["exit"],
            "passed": is_pass(last["mode"], last["exit"]),
            "cases": last.get(CASES_KEY),
        },
    }


def _discovery(served: ServedCorpus, observed: Observed) -> dict:
    """Return the cache's startup verdict and the tree's verdict against the startup scan."""
    return {"cache": served.verdict, "since_startup": observed.since_startup}
