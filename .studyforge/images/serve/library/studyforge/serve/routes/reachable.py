r"""Whether each served corpus can run code now: the run index's `runnable`.

**What it does.** Holds one runner per corpus, made by the instance's own
runner seam, and answers `{source: bool}` — `False` for a corpus that declares
its runner while that runner is not up, `True` otherwise.

**How you use it.** `Runs` holds one `Reachable(runner)`, and the run index
publishes `runs.reachable.runnable(corpora)`. A page hides Run, Submit and
Run tests, with the sentence saying why, where it reads `False`.

**Depends on.** `execute` for the runner and its refusal.

## ⛔ A DECLARED RUNNER THAT IS DOWN RUNS NOTHING, AND THE PAGE IS TOLD FIRST

⭐ A corpus whose execution files declare a runner never falls back to the host
(`execute.Runner(required=True)`): its code runs in that runner or nowhere.
⭐ So a page learns BEFORE a click that a run would be refused, and offers no
control it cannot keep. ⭐ A corpus that declares no runner is not asked at all:
it runs on the host, as it always did, and answers `True`.

⭐ **One runner per corpus, kept**, so each answer rides that runner's own
probe cache (`MODE_TTL`, `SERVICE_TTL`): a page load forks no `docker` and
asks no service more often than a run would.
"""

from __future__ import annotations

import threading
from collections.abc import Callable, Iterable

from studyforge.execute import Runner, RunRefused
from studyforge.serve.discovery import ServedCorpus


class Reachable:
    """Answer, per corpus, whether its runner can run code now."""

    def __init__(self, runner: Callable[[ServedCorpus], Runner]) -> None:
        """Hold the seam that makes a corpus's runner; make none until asked."""
        self._make = runner
        self._lock = threading.Lock()
        self._held: dict[str, Runner] = {}

    def runnable(self, corpora: Iterable[ServedCorpus]) -> dict[str, bool]:
        """Return `{source: bool}` for every corpus: `False` only for a declared runner down."""
        return {corpus.source: self._one(corpus) for corpus in corpora}

    def _one(self, corpus: ServedCorpus) -> bool:
        """Ask this corpus's held runner; a refusal to make or to ask one is not runnable."""
        try:
            with self._lock:
                runner = self._held.get(corpus.source)
                if runner is None:
                    runner = self._held[corpus.source] = self._make(corpus)
            if not getattr(runner, "required", False):
                return True
            runner.mode()
        except RunRefused:
            return False
        return True
