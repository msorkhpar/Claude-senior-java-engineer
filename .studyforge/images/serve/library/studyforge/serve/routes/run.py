r"""The run namespace: Run and Submit — a practice's own command, streamed and recorded.

**What it does.** Answers under `/api/v1/run/`:

- `GET` (empty) → `run-index`: the modes, the two endpoints, where each corpus's
  editor is if one is up, whether each corpus can run code now (`runnable`), and the
  live run if any;
- `GET client.js` → the page's execution client, `serve/assets/run-client.js`,
  which publishes `studyforge.run` (`available`, `start`, `stop`, `editor`) and
  draws nothing;
- `POST <corpus>/<mode>/<practice key>` → starts the ONE command `mode` names in that
  practice's workspace, streams its output line by line as `text/plain`, and records
  the outcome in the corpus's progress store when the stream ends;
- `POST <corpus>/editor/<practice key>` → `run-editor`: prepares that practice's
  workspace settings and answers the URL of each of its two editor windows;
- `POST <corpus>/code/<path>` and `POST <corpus>/code-test/<path>` → a
  lesson's code file in the editor beside its partner, and its test's run in
  the copy of the code — `routes.code`'s;
- `POST stop` → stops the live run, which then ends `--- exit stopped ---` and is
  recorded as stopped.

**How you use it.** `serve.instance` builds `Runs(discovered, sources)` and registers
`partial(route, runs)` under `NAMESPACE`, with `NAMESPACE` among `app`'s writers.

**Depends on.** `execute` — ⭐ the ONLY way this route runs anything —
through `routes.runs` and its `RunRefused`; `routes.code` for a lesson's code;
`exercise` for the two acts, `progress` for the key, `unit.served` for the
document, and `serve.response`. ⛔ This module, `routes.runs` and `routes.code`
are the only ones in `serve` that import `execute`, and `execute` never imports
`serve`.

## ⛔ Nothing a client sends becomes a command (spec §8.3, rule 3)

⭐ **The command is READ, never received.** The practice's workspace is read from its
unit's generated document — built from the archive on disk by the same `ContentSource`
the content namespace serves — and the argv under the mode's key is handed to
`Runner.start` exactly as the document holds it. A client names a corpus, a mode from
`MODES` and a practice key, and each only SELECTS: a `Request` carries no body and no
query string, so a client-sent command has no field to arrive through.

## Two modes, because Run and Submit are different acts

| mode | the command | recorded as | can complete a practice |
|---|---|---|---|
| `run` (Run) | the workspace's `run_command` | `run` | ⛔ never |
| `test` (Submit) | the workspace's `test_command` | `test` | only when it exits `0` |

⭐ **A Submit is recorded with its case breakdown** where the practice's record
declares one: this module reads the wall clock immediately before the run
starts and hands it, with the workspace, to `Outcome`. ⛔ **The pass rule is not
touched** — `progress.is_pass` still decides, and the breakdown is a report.

⚠️ **A workspace need not name both** (§7: a file with no test carries `main_path` and
`run_command` alone). A mode whose command the workspace does not name answers `409` and
starts nothing — so Submit is offered exactly where a test is named.

⭐ The URL's mode words ARE `exercise.COMMANDS`, which are `progress.MODES` — one
vocabulary from the data to the record, so a Run cannot be recorded as a Submit.

## ⛔ The page names a practice by `progress.practice_key`

The key is parsed by `parse_practice_key` at the corpus's own depth and the outcome is
recorded under the `Address`, ordinal and section it parsed to — the key the state
namespace reads back. ⛔ Nothing here composes a key.

## One run at a time, and every run ends recorded

A second start while one is live answers `409`: a reader has one workspace. How a run
ends is recorded however it ends, and every line is gated on the wire — both are
`routes.runs`'s, split from this module at that seam (R11).

## ⭐ `editor` is on the index because it can only be true at SERVE time

⭐ **`editor` maps a corpus's `source` to `{origin, folder}`** for every corpus
whose editor container is up over its own files, and carries nothing for the
rest. ⛔ **The built page cannot hold this and must not**: the editor's host
port is per-project and R8 forbids a built page naming an origin or
a port, so the one place the answer can be true is a response from the origin
the reader is actually reading at. ⚠️ **It is a MAP because an instance serves
every corpus it discovered**, and the panel knows which one it belongs to —
`editor: {origin, folder}` for a single corpus would have made a
several-corpus instance answer about the wrong one.

⛔ **Asking is `docker inspect` and nothing else** (`execute.EditorProbe`, spec
§8.3): this process never holds the Docker socket, never starts an editor, and
answers an absence rather than failing when it cannot ask.

## ⭐ A FILE is addressed under `editor`, which is where the two windows come from

⭐ **`editor` stands where a mode stands** — `POST <corpus>/editor/<practice>` —
because it selects exactly what a run selects: one corpus, one practice, one
act. ⛔ It is not one of `MODES` and starts nothing; it PREPARES: it writes that
practice's workspace settings (everything read-only, the practice's own source
excluded back out, the workbench closed) and answers the URL each of the two
windows opens.

⛔ **Two windows, because the window's own URL is the only thing that can tell
them apart.** An extension cannot read its own window's query string and both
windows share ONE workspace settings file, so anything an extension opened it
would open in BOTH. ⚠️ **The test's window is deliberately left read-only**: it
is the statement of what *done* means.

⚠️ **A practice the editor does not hold answers `404`, never a URL.** A
code-server URL naming a file that is not mounted opens an empty, dirty buffer
titled with the file's own name — it looks exactly like a corrupted file and is
not one. ⛔ **A quiz has no file at all** and answers `409`: no window, no Run,
no Submit.

⛔ **This is not a security boundary and must not be read as one.** An iframe of
an IDE with a shell is exactly as powerful as the process behind it; the
boundary is the container, the loopback bind and one exact origin.

## ⭐ The page's execution client is served HERE, never built into a page

A built site opens over `file://` naming no server (R8), and a client names the API on
every line that matters — so it is served by the namespace it talks to, and exists
exactly where an origin can answer it. ⚠️ How a page loads it is the practice panel's.

"""

from __future__ import annotations

import time
from pathlib import Path

from studyforge.archive.scrub import PersonalDataLeak
from studyforge.execute import RunRefused, WorkbenchRefused
from studyforge.exercise import COMMANDS, RUN, TEST
from studyforge.progress import RAISES as PROGRESS_RAISES
from studyforge.progress import parse_practice_key
from studyforge.serve.discovery import ServedCorpus
from studyforge.serve.response import (
    API_PREFIX,
    NO_STORE,
    TEXT_TYPE,
    Request,
    Response,
    error,
    json_response,
)
from studyforge.serve.routes import code
from studyforge.serve.routes.content import ContentSource
from studyforge.serve.routes.runs import Live, Outcome, Runs, Stream
from studyforge.unit import served
from studyforge.unit.errors import ContentError

#: The name this namespace is registered under. ⭐ The framework's one spelling of
#: it: `skills.buildserve.states` imports it from here.
NAMESPACE = "run"

#: The modes a page may name, in the data's own words.
MODES = COMMANDS

#: Which workspace key each mode reads. ⛔ The only commands this route can start.
COMMAND_OF = {RUN: "run_command", TEST: "test_command"}

#: The path that stops the live run.
STOP = "stop"

#: The index's key for where each corpus's editor is, and — the SAME word — the
#: path segment that addresses one practice's two windows. ⭐ The
#: framework's ONE spelling of it: the page's client reads it from here rather
#: than retyping it (`tests/studyforge/serve/routes/test_run_client.py`). ⛔ It
#: stands where a mode stands and is not one of `MODES`: it starts nothing.
EDITOR = "editor"

#: The path the page's execution client is served at, and the file it is.
CLIENT = "client.js"
CLIENT_FILE = Path(__file__).resolve().parent.parent / "assets" / "run-client.js"
SCRIPT_TYPE = "text/javascript; charset=utf-8"

#: ⭐ Where a page asks for the client — the framework's ONE spelling of it, so
#: the namespace that serves it and the static route that adds it to a served
#: page cannot come apart. ⛔ `index()` reads it rather than
#: composing a second copy.
CLIENT_PATH = f"{API_PREFIX}/{NAMESPACE}/{CLIENT}"

#: The section kind a workspace belongs to.
PRACTICE = "practice"

NO_SUCH_RUN = "no such run endpoint"
NO_SUCH_CORPUS = "no such corpus"
NO_SUCH_MODE = "no such mode"
NO_SUCH_PRACTICE = "no such practice"
UNGRADED = "this practice has no workspace, so there is nothing to run"
NO_SUCH_COMMAND = "this practice's workspace names no command for this mode"
NO_FILE = "this practice sets no file to open, so there is no editor window for it"
NO_EDITOR = "no editor is running over this practice's own file"
WORKSPACE_REFUSED = "this practice's workspace settings could not be written for the editor"
UNRECOGNISED = "the unit document failed validation"
GATED = "the unit document failed the personal-data gate"
REFUSED = "the unit document's command was refused by the runner"
BUSY = "a run is already live; stop it first"
NOT_FROM_A_FILE = "a page opened from a file cannot start a run"
ACT_BY_POST = "a run is started by POST"


def route(runs: Runs, request: Request, rest: str) -> Response:
    """Answer one request under `/api/v1/run/`; `rest` is the path after it."""
    if rest in ("", CLIENT):
        if request.method == "POST":
            return _not_allowed("GET, HEAD")
        if rest == CLIENT:
            headers = (("Content-Type", SCRIPT_TYPE), ("Cache-Control", NO_STORE))
            return Response(200, headers, CLIENT_FILE.read_bytes())
        return json_response(200, index(runs))
    if request.method != "POST":
        return _not_allowed("POST")
    if request.headers.get("Origin", "").strip() == "null":
        return error(403, NOT_FROM_A_FILE)
    if rest == STOP:
        return json_response(200, {"resource": "run-stop", "stopped": runs.stop()})
    return start(runs, rest)


def index(runs: Runs) -> dict:
    """Return what this namespace offers, and the run in flight."""
    live = runs.live
    return {
        "resource": "run-index",
        "modes": list(MODES),
        "start": f"{API_PREFIX}/{NAMESPACE}/{{corpus}}/{{mode}}/{{practice}}",
        "stop": f"{API_PREFIX}/{NAMESPACE}/{STOP}",
        # ⭐ The index can address a FILE and not only a folder. `editor`
        # below says WHERE an editor is; this says how to ask it for one
        # practice's two windows, which is the only thing that can open two
        # different files in two windows of one code-server.
        "practice_editor": f"{API_PREFIX}/{NAMESPACE}/{{corpus}}/{EDITOR}/{{practice}}",
        # ⭐ A lesson's link to a code file: its two windows, and its test's run.
        "code": f"{API_PREFIX}/{NAMESPACE}/{{corpus}}/{code.CODE}/{{path}}",
        "code_test": f"{API_PREFIX}/{NAMESPACE}/{{corpus}}/{code.CODE_TEST}/{{path}}",
        "client": CLIENT_PATH,
        EDITOR: runs.editors(),
        # ⭐ Whether each corpus can run code now: `False` is a declared runner that is
        # down, and a page then offers no Run, Submit or Run tests, and says why.
        "runnable": runs.reachable.runnable(runs.discovered.corpora),
        "live": None
        if live is None
        else {"corpus": live.corpus, "practice": live.practice, "mode": live.mode},
    }


def start(runs: Runs, rest: str) -> Response:
    """Answer `<corpus>/<mode>/<practice key>`: a run, or that practice's editor.

    ⭐ **One parse, because the three parts are the same three** — a corpus, an
    act and a practice — and the workspace is read from the unit's own document
    either way. ⛔ `EDITOR` stands where a mode stands and starts nothing; the
    two in `MODES` each start the ONE command the document names.
    """
    name, _, tail = rest.partition("/")
    mode, _, key = tail.partition("/")
    corpus = runs.discovered.by_source.get(name)
    if corpus is None or name not in runs.sources:
        return error(404, NO_SUCH_CORPUS if tail else NO_SUCH_RUN)
    if mode in code.ACTS:
        return code.route(runs, corpus, mode, key)
    if mode != EDITOR and mode not in MODES:
        return error(404, NO_SUCH_MODE)
    try:
        address, ordinal, section = parse_practice_key(key, corpus.depth)
    except PROGRESS_RAISES:
        return error(404, NO_SUCH_PRACTICE)
    try:
        workspace = workspace_of(runs.sources[name], address.unit_key(ordinal), section)
    except PersonalDataLeak:
        return error(500, GATED)
    except ContentError:
        return error(422, UNRECOGNISED)
    except LookupError:
        return error(404, NO_SUCH_PRACTICE)
    if workspace is None:
        return error(409, UNGRADED)
    if mode == EDITOR:
        return editor(runs, corpus, workspace)
    argv = workspace.get(COMMAND_OF[mode])
    if argv is None:
        return error(409, NO_SUCH_COMMAND)
    # ⛔ The breakdown's clock: read BEFORE the run and never after it. A report is a file that
    # outlives the run that wrote it, so the only thing that can tell this run's
    # report from the previous one is the instant this run started; a clock read
    # afterwards makes every report look fresh. ⭐ The fold is `routes.breakdown`'s.
    started = time.time()
    try:
        live = runs.claim(lambda: Live(name, key, mode, runs.runner(corpus).start([argv])))
    except RunRefused:
        return error(422, REFUSED)
    if live is None:
        return error(409, BUSY)
    outcome = Outcome(runs, corpus, (address, ordinal, section), mode, argv, workspace, started)
    headers = (("Content-Type", TEXT_TYPE), ("Cache-Control", NO_STORE))
    return Response(200, headers, stream=Stream(runs, live, outcome))


def editor(runs: Runs, corpus: ServedCorpus, workspace: dict) -> Response:
    """Prepare this practice's workspace and answer where each of its two windows is.

    ⛔ **Two windows of ONE editor, and each is addressed by its own URL** — the
    only discriminator there is. ⚠️ `test` is `None` where the record
    names no test: the page then offers one window and no second tab,
    which is the same honesty as offering no Submit.
    """
    main = workspace.get("main_path")
    if not isinstance(main, str) or not main:
        return error(409, NO_FILE)
    test = workspace.get("test_path")
    # ⭐ The files a command names weigh in the practice's own folder.
    named = tuple(
        argument
        for key in ("run_command", "test_command")
        if isinstance(workspace.get(key), list)
        for argument in workspace[key]
        if isinstance(argument, str)
    )
    try:
        where = runs.practice_editor(
            corpus, main, test if isinstance(test, str) and test else None, named
        )
    except WorkbenchRefused:
        # ⛔ The refusal's own sentence names a file inside somebody else's
        # container and a host errno; the wire gets this route's constant
        # instead, which says the same thing and carries neither (R7).
        return error(409, WORKSPACE_REFUSED)
    if where is None:
        return error(404, NO_EDITOR)
    return json_response(200, {"resource": "run-editor", **where})


def workspace_of(source: ContentSource, unit: str, section: str) -> dict | None:
    """Return the practice section's workspace from the unit's generated document.

    Raises `LookupError` when the unit has no document or no practice `section`;
    `None` is a practice with no workspace — ungraded, nothing to run.
    """
    text = source.unit(unit)
    if text is None:
        raise LookupError(unit)
    document = served.parse(text, served.UNIT_FILENAME)
    for found in document["sections"]:
        if found.get("key") == section and found.get("kind") == PRACTICE:
            return found.get("workspace")
    raise LookupError(section)


def _not_allowed(allow: str) -> Response:
    """Answer `405` naming the one method this path takes."""
    answer = error(405, ACT_BY_POST if allow == "POST" else "method not allowed")
    return Response(405, (*answer.headers, ("Allow", allow)), answer.body)
