r"""The instance's runs: the one live slot, a run's streamed body, and its recorded outcome.

**What it does.** `Runs` holds what a run is read from (every corpus discovered and its
content), the one run in flight, one `EditorProbe` per corpus, and the editor origins this
instance has EVER discovered — `editors()` is where a running editor is, for the index to
publish, `origins()` is the frame policy's reading of that record (it never forks, spec
§8.3), and `practice_editor()` is ONE practice's two windows and the settings they are
read under; `Stream` is a run's response body — each line filtered and gated, the verdict
recorded just before the exit line, which is last; `Outcome` records it, with a Submit's
case breakdown beside it.

**How you use it.** `serve.routes.run` claims the slot with `Runs.claim`, and answers
with `Response(200, headers, stream=Stream(runs, live, Outcome(...)))`.

**Depends on.** `execute` (the handle, the exit line, the filter), `progress`,
`routes.breakdown` for a Submit's cases, `archive.scrub` and `serve.withheld` for the wire.

## ⭐ A Submit is recorded with its breakdown, which is never a verdict

⛔ **`is_pass` is untouched here and must stay untouched**: a practice completes when
a test-mode run exits zero, and a breakdown is a REPORT about that run. ⭐ **The fold,
its clock and what a refusal says are [`routes.breakdown`](breakdown.py)'s**, which
says why it is a module; this one holds the two values and writes what comes back.

⭐ **Split from `routes.run`** (R11): that module is what a request selects; this one is
what a started run IS until it ends. ⛔ Neither takes a command but the unit document's.

## One run at a time, and every run ends recorded

A second start while one is live is refused. Whatever ends a stream — the last line,
`stop`, a timeout, or a page that hung up (`app` cancels it) — the outcome is recorded:
the status, `timeout` or `stopped`. ⚠️ A signal's status is recorded as `128 + n`.

## ⛔ The frame policy's record of origins does NOT expire

⭐ **`origins()` is composed from every origin this instance has EVER discovered, never
from a reading that must still be fresh.** ⛔ `EditorProbe`'s TTL is right for deciding
whether to RUN a command against a container and wrong for deciding which origin a page
may NAME, and the ground does not transfer between the two: a reading older than
`EDITOR_TTL` is not a reason to start anything, but it is a perfectly good reason to say
which loopback port a document may embed.

⚠️ **Why this record exists.** Read straight through the probe, a served `frame-src`
would name the editor for `EDITOR_TTL` seconds after an ask and `'none'` after that. ⛔ **A
policy that oscillates between correct and `'none'` is a worse failure than one that
names an origin a moment too long.**

⭐ **A stale entry grants no capability**: the policy names a loopback origin where
nothing listens, and the frame fails to load, as it does with no editor at all. ⛔ **A
wildcard `frame-src` of `http://127.0.0.1:*` was REFUSED**: it would cost no reload, but
it lets a page frame ANY local service, on any port, forever. ⭐ This record names only
ports an editor for THIS source root was discovered on, and a widening is argued rather
than defaulted into.

⚠️ **Forgetting is not offered.** ⭐ A published instance does not wait for discovery: its
configured origin seeds the record, so a learner's first page load frames the editor.

## ⛔ Output is filtered, then gated on the wire

Every line `execute` yields is already relative to the source root and scrubbed. ⭐
`execute.quiet` drops what the corpus's one declared build tool says about itself
(Maven's rerun advice names switches the page cannot pass), never an error, a frame
or the exit line. A kept line is scrubbed again as it leaves the process (R7;
`scrub` is idempotent), ⛔ after `withheld.OutputGate` replaced any quiz-key line.
"""

from __future__ import annotations

import shlex
import threading
from collections.abc import Callable, Iterable, Iterator, Mapping
from dataclasses import dataclass
from datetime import UTC, datetime

from studyforge.address import Address
from studyforge.archive.scrub import scrub
from studyforge.execute import (
    EXIT_STOPPED,
    EXIT_TIMEOUT,
    Editor,
    EditorProbe,
    Quiet,
    RunHandle,
    Runner,
    declares_runner,
    exit_line,
    open_url,
    practice_folder,
    recorded,
    select,
    write_settings,
)
from studyforge.progress import CASES_KEY
from studyforge.progress import RAISES as PROGRESS_RAISES
from studyforge.serve.discovery import Discovered, ServedCorpus
from studyforge.serve.routes.breakdown import fold
from studyforge.serve.routes.content import ContentSource
from studyforge.serve.routes.reachable import Reachable
from studyforge.serve.withheld import OutputGate, marks_of

#: The line said, just before the exit line, when the store refused the outcome.
NOT_RECORDED = "--- the outcome could not be recorded ---"

RunnerFor = Callable[[ServedCorpus], Runner]
EditorFor = Callable[[ServedCorpus], EditorProbe]


def runner_for(corpus: ServedCorpus) -> Runner:
    """Return the corpus's runner, by the name THIS checkout recorded; required if declared."""
    name = recorded(corpus.root, corpus.source).runner
    return Runner(corpus.root, name, required=declares_runner(corpus.root))


def editor_for(corpus: ServedCorpus) -> EditorProbe:
    """Return the probe for the corpus's editor: its root, and the name this checkout recorded."""
    return EditorProbe(corpus.root, recorded(corpus.root, corpus.source).editor)


@dataclass(frozen=True, slots=True)
class Live:
    """The run in flight: what it is, and its handle."""

    corpus: str
    practice: str
    mode: str
    handle: RunHandle


class Runs:
    """The instance's runs: at most one live, each read from its unit document."""

    def __init__(
        self,
        discovered: Discovered,
        sources: Mapping[str, ContentSource],
        runner: RunnerFor = runner_for,
        clock: Callable[[], str] | None = None,
        editor: EditorFor = editor_for,
        declared: Iterable[str] = (),
    ) -> None:
        """Hold the corpora, their content, the runner and probe makers, the clock, `declared`."""
        self.discovered = discovered
        self.sources = dict(sources)
        self.runner = runner
        self.editor = editor
        self.clock = clock or _now
        self._lock = threading.Lock()
        self._probes_lock = threading.Lock()
        self._probes: dict[str, EditorProbe] = {}
        self.reachable = Reachable(runner)  # ⭐ the index's `runnable`, one runner each
        #: ⛔ Every editor origin this instance has EVER discovered, which is what the
        #: frame policy composes from and which never expires (the module
        #: docstring carries the ground). Emptied only by the process ending.
        self._origins_lock = threading.Lock()
        self._discovered: set[str] = set(declared)
        self._live: Live | None = None

    def editors(self) -> dict[str, dict[str, str]]:
        """Where each served corpus's editor is, for every one that is up.

        ⭐ **One probe per corpus, kept**, because a probe's whole economy is its
        cache: a fresh one per request would fork `docker` on every page load.
        ⛔ A corpus whose editor is not up, or which cannot be asked about, is
        simply absent — the page then shows the sentence it already ships.
        """
        return {
            source: {"origin": found.origin, "folder": found.folder}
            for source, found in self.found().items()
        }

    def origins(self) -> tuple[str, ...]:
        """Return each origin a served page may frame: every one EVER discovered.

        ⛔ **This asks nothing, and that is a rule rather than an optimisation**
        (spec §8.3): `serve.app` composes `frame-src` from it on EVERY response, so
        a version that asked would fork `docker` to render a static page.
        ⛔ **And it does not EXPIRE**: reading the probe's cache alone named the
        editor for `EDITOR_TTL` seconds after anything asked and `'none'` after;
        the module docstring carries the whole ground.
        ⭐ **A published instance's configured origin is in the record from the
        start** (`declared`): never a wildcard, never a request's value.
        ⚠️ **A cold development instance frames nothing** until the index or the
        practice-editor route, which a reader's own client asks for, fills the record.
        """
        self.found(ask=False)
        with self._origins_lock:
            return tuple(sorted(self._discovered))

    def found(self, ask: bool = True) -> dict[str, Editor]:
        """Return the editor up for each served corpus; `ask=False` reads, never forks."""
        up: dict[str, Editor] = {}
        for corpus in self.discovered.corpora:
            if corpus.source not in self.sources:
                continue
            probe = self._probe(corpus)
            where = probe.editor() if ask else probe.known()
            if where is not None:
                up[corpus.source] = self._remember(where)
        return up

    def _remember(self, where: Editor) -> Editor:
        """Record where this editor is among the origins ever discovered; return it.

        ⭐ **Every reader of a probe passes through here**, so an origin the
        index published, or a practice-editor route prepared, is one the frame
        policy will still name a minute later.
        """
        with self._origins_lock:
            self._discovered.add(where.origin)
        return where

    def practice_editor(
        self, corpus: ServedCorpus, main: str, test: str | None, named: tuple[str, ...] = ()
    ) -> dict | None:
        """Prepare one practice's workspace and say where its two windows are, or `None`.

        ⭐ **`None` is the ordinary answer** — no editor up, or an editor that
        does not hold this practice's file. ⛔ A path the editor does not hold
        is never answered as a URL: a code-server URL naming an unmounted file
        opens an empty, dirty buffer titled with the file's own name, which
        looks exactly like a corrupted file and is not one.

        ⛔ **Each practice opens its OWN folder, and its settings are its own**:
        one shared folder was one lock naming one file, so opening a
        practice locked every other and two at once refused one. `named` is the
        files the practice's commands name. Raises `WorkbenchRefused`.
        """
        asked = self._probe(corpus).editor()
        if asked is None:
            return None
        where = practice_folder(self._remember(asked), main, test, root=corpus.root, named=named)
        inside_main = None if where is None else where.inside(main)
        if where is None or inside_main is None:
            return None
        inside_test = where.inside(test) if test else None
        write_settings(corpus.root / where.base, inside_main, inside_test)
        return {
            "origin": where.origin,
            "main": {"path": inside_main, "url": open_url(where, main)},
            "test": None
            if inside_test is None or test is None
            else {"path": inside_test, "url": open_url(where, test)},
        }

    def _probe(self, corpus: ServedCorpus) -> EditorProbe:
        """Return the one probe held for `corpus`, made on first ask."""
        with self._probes_lock:
            probe = self._probes.get(corpus.source)
            if probe is None:
                probe = self._probes[corpus.source] = self.editor(corpus)
            return probe

    @property
    def live(self) -> Live | None:
        """The run in flight, or `None`."""
        with self._lock:
            return self._live

    def stop(self) -> bool:
        """Stop the live run; `True` if there was one to stop."""
        live = self.live
        return live is not None and live.handle.stop()

    def claim(self, live: Callable[[], Live]) -> Live | None:
        """Start `live()` unless a run is live; `None` if one is."""
        with self._lock:
            if self._live is not None:
                return None
            self._live = live()
            return self._live

    def release(self, live: Live) -> None:
        """Forget `live` if it is still the run in flight."""
        with self._lock:
            if self._live is live:
                self._live = None


@dataclass(frozen=True, slots=True)
class Outcome:
    """Where one run's outcome is recorded, what it ran, and what it folds through."""

    runs: Runs
    corpus: ServedCorpus
    practice: tuple[Address, int, str]
    mode: str
    argv: list[str]
    #: The practice's workspace as the unit document holds it, and the wall clock
    #: read BEFORE the run started — omitted by a caller with no breakdown.
    workspace: dict | None = None
    started: float | None = None

    def record(self, verdict: int | str) -> tuple[str, ...]:
        """Record the run's verdict and breakdown; return the lines to say, if any.

        ⛔ The breakdown is folded BEFORE the write and never decides it: a
        report that cannot be read honestly costs the breakdown, not the run.
        """
        cases, said = fold(self.mode, self.workspace, self.corpus.root, self.started)
        address, ordinal, section = self.practice
        try:
            self.corpus.progress().record_run(
                address,
                ordinal,
                section,
                mode=self.mode,
                exit_code=verdict,
                commands=[shlex.join(self.argv)],
                when=self.runs.clock(),
                **{CASES_KEY: cases},
            )
        except PROGRESS_RAISES:
            return (*said, NOT_RECORDED)
        return said


class Stream:
    """One run's response body: each gated line, the verdict recorded before the exit line.

    ⭐ **The exit line is told from output by the handle, never by its text**: a run
    is ONE command, whose status exists only once its output has ended, so a line
    that arrives while `returncode` is `None` is the program's — even one that
    prints `--- exit 0 ---` itself.

    ⛔ **Closed early — the client hung up, or `app` closed it before the first
    chunk — the run is stopped and recorded as stopped**, and the slot is freed.
    A plain generator could not promise that: closing one that never started runs
    none of its body.
    """

    def __init__(self, runs: Runs, live: Live, outcome: Outcome) -> None:
        """Hold the run; nothing is read until the first chunk is asked for."""
        self._runs = runs
        self._live = live
        self._outcome = outcome
        self._lines = live.handle.lines()
        self._gate = OutputGate(marks_of(runs.sources.get(outcome.corpus.source)))
        self._quiet = Quiet(select(outcome.corpus.corpus.manifest.runtimes))
        self._chunks = self._generate()
        self._recorded = False
        self._finished = False

    def __iter__(self) -> Stream:
        """Return this stream."""
        return self

    def __next__(self) -> bytes:
        """Return the next chunk."""
        return next(self._chunks)

    def cancel(self) -> None:
        """Stop the run from another thread — the page hung up; it ends `stopped`."""
        self._live.handle.stop()

    def close(self) -> None:
        """End the stream; an unfinished run is stopped and recorded as stopped."""
        self._chunks.close()
        self._finish()

    def _generate(self) -> Iterator[bytes]:
        handle = self._live.handle
        try:
            for line in self._lines:
                if handle.returncode is not None and not self._recorded:
                    self._recorded = True
                    for said in self._outcome.record(verdict(handle, line)):
                        yield (said + "\n").encode("utf-8")
                if self._quiet.keeps(text := line.rstrip("\n")):
                    yield (scrub(self._gate(text)) + "\n").encode("utf-8")
        finally:
            self._finish()

    def _finish(self) -> None:
        if self._finished:
            return
        self._finished = True
        try:
            if not self._recorded:
                self._recorded = True
                self._live.handle.stop()
                self._lines.close()
                self._outcome.record(EXIT_STOPPED)
        finally:
            self._runs.release(self._live)


def verdict(handle: RunHandle, last: str) -> int | str:
    """Return the verdict the exit line `last` spells, as the record takes it."""
    for word in (EXIT_STOPPED, EXIT_TIMEOUT):
        if last == exit_line(word):
            return word
    code = handle.returncode if handle.returncode is not None else 0
    return code if code >= 0 else 128 - code


def _now() -> str:
    """Return the time a run ended, in UTC, as the record's ISO 8601."""
    return datetime.now(UTC).isoformat(timespec="seconds")
