r"""A lesson's code in the editor: one code file and its partner opened, and its test run.

**What it does.** Answers the two acts a page's link to a code file of the
corpus asks for, under the run namespace:

- `POST <corpus>/code/<path>` → `run-code`: brings the copy of the corpus's
  code up to date, writes the settings of the folder the file's two windows
  open, and answers the URL of each — the source and the test it stands between;
- `POST <corpus>/code-test/<path>` → the test the file names or is paired
  with, run in the copy and streamed exactly as a Submit is, and **not
  recorded**: it is no practice, and it completes nothing.

**How you use it.** `serve.routes.run.start` hands a request whose act is one
of `ACTS` to `route(runs, corpus, act, tail)`.

**Depends on.** `execute` for the copy (`sync`), the pair (`pair`), the test's
command (`test_command`), the editor's two windows (`practice_folder`,
`open_url`, `write_settings`) and the run; `routes.runs` for the one live slot
and the stream; `exercise.require_path` for what a path may be.

## ⭐ The SAME editor, the same two windows, the same lock

⛔ **Nothing here is a second editor mechanism.** The editor is the one the
corpus runs, found by the same probe and remembered for the frame policy by the
same record (`Runs.found`); its two windows are addressed by the same URLs
(`workbench.open_url`); the folder they open is the deepest one holding both
files and the module's build file (`workbench.practice_folder`); and the lock
is the practice's (`workbench.write_settings`): the source is editable, the
test that states what the code does is not. ⭐ Where no source was found, the
file opens alone and nothing in it is editable.

## ⛔ The author's tree is never written: the files open from the COPY

⭐ Both windows open the file's place in `execute.CODE_COPY`, and the test runs
there, so a reader's change and a build's output land in the copy and the
author's `git status` stays clean. The page says so.

## ⛔ Nothing a client sends becomes a command (spec §8.3, rule 3)

⭐ **The path only SELECTS a file.** It is decoded segment by segment, checked
against the one pattern a workspace path must match (`exercise.require_path`),
and must name a regular code file the copy mirrors (`execute.pair`); the
command is `execute.test_command`'s, read from the corpus's declared build
tool and the files on disk, and ⛔ no part of the request is ever an argument
of it but the file it selected.

## Every refusal is an answer the page can fall back from

⭐ No editor, a file that is not code, a copy that cannot be made — each is a
`404` or a `409` the page answers by following the link to the file's plain
view, which is never broken.
"""

from __future__ import annotations

from dataclasses import dataclass
from urllib.parse import unquote

from studyforge.execute import (
    BUILD_FILES,
    CodeRefused,
    Pair,
    RunRefused,
    WorkbenchRefused,
    in_copy,
    open_url,
    pair,
    practice_folder,
    sync,
    test_command,
    write_settings,
)
from studyforge.exercise import ExerciseError, require_path
from studyforge.serve.discovery import ServedCorpus
from studyforge.serve.response import NO_STORE, TEXT_TYPE, Response, error, json_response
from studyforge.serve.routes.runs import Live, Runs, Stream

#: The act that opens a code file's two windows, and the one that runs its test.
CODE = "code"
CODE_TEST = "code-test"
ACTS = (CODE, CODE_TEST)

NO_SUCH_FILE = "no such code file in this corpus"
NO_EDITOR = "no editor is running over this corpus's code"
NOT_COPIED = "the copy of this corpus's code could not be made"
REFUSED = "the editor's settings for this file could not be written"
NO_TEST = "this file names no test, or no command is known to run it"
NOT_RUN = "this test's command was refused by the runner"
BUSY = "a run is already live; stop it first"


@dataclass(frozen=True, slots=True)
class Unrecorded:
    """The outcome of a code test's run: said on the stream, and recorded nowhere.

    ⭐ **A lesson's test is not a practice**: it completes nothing and has no
    place in the reader's record, so this is `routes.runs.Outcome`'s shape with
    nothing written.
    """

    corpus: ServedCorpus

    def record(self, verdict: int | str) -> tuple[str, ...]:
        """Say nothing more; the exit line is the verdict."""
        return ()


def route(runs: Runs, corpus: ServedCorpus, act: str, tail: str) -> Response:
    """Answer `<act>/<path>` for one corpus: its two windows, or its test's run."""
    path = file_of(tail)
    runtimes = corpus.corpus.manifest.runtimes
    try:
        found = None if path is None else pair(corpus.root, path, runtimes)
    except CodeRefused:
        return error(409, NOT_COPIED)
    if found is None:
        return error(404, NO_SUCH_FILE)
    try:
        sync(corpus.root)
    except CodeRefused:
        return error(409, NOT_COPIED)
    if act == CODE:
        return windows(runs, corpus, found)
    argv = test_command(corpus.root, found, runtimes)
    if argv is None:
        return error(409, NO_TEST)
    try:
        live = runs.claim(lambda: Live(corpus.source, path, act, runs.runner(corpus).start([argv])))
    except RunRefused:
        return error(422, NOT_RUN)
    if live is None:
        return error(409, BUSY)
    headers = (("Content-Type", TEXT_TYPE), ("Cache-Control", NO_STORE))
    return Response(200, headers, stream=Stream(runs, live, Unrecorded(corpus)))


def windows(runs: Runs, corpus: ServedCorpus, found: Pair) -> Response:
    """Prepare the folder the pair's windows open and answer where each window is."""
    editor = runs.found().get(corpus.source)
    main = found.source or found.test
    test = found.test if found.source else None
    runtimes = corpus.corpus.manifest.runtimes
    # ⭐ The module's own build files weigh in the folder, so a JVM window opens
    # the project the language server reads, never a bare source directory.
    named = tuple(
        in_copy(f"{found.module}/{one}" if found.module else one)
        for tool in runtimes
        for one in BUILD_FILES.get(tool, ())
    )
    where = None
    if editor is not None:
        where = practice_folder(
            editor, in_copy(main), in_copy(test) if test else None, root=corpus.root, named=named
        )
    inside_main = None if where is None else where.inside(in_copy(main))
    if where is None or inside_main is None:
        return error(404, NO_EDITOR)
    inside_test = where.inside(in_copy(test)) if test else None
    try:
        editable = found.source is not None
        write_settings(corpus.root / where.base, inside_main, inside_test, editable=editable)
    except WorkbenchRefused:
        return error(409, REFUSED)
    return json_response(
        200,
        {
            "resource": "run-code",
            "origin": where.origin,
            "opened": "test" if found.opened == found.test and test else "main",
            "main": {"file": main, "url": open_url(where, in_copy(main))},
            "test": None if test is None else {"file": test, "url": open_url(where, in_copy(test))},
            # ⭐ The file a Run names: the test, wherever it opened, when a command runs it.
            "runs": found.test if test_command(corpus.root, found, runtimes) else None,
        },
    )


def file_of(tail: str) -> str | None:
    """Return the corpus-relative path a request names, or `None` for one no file has.

    ⛔ Split BEFORE decoding, so an encoded `/` cannot manufacture a segment, and
    checked by the one pattern a workspace path must match.
    """
    if not tail:
        return None
    parts = [unquote(part) for part in tail.split("/")]
    if any(not part or "/" in part for part in parts):
        return None
    try:
        return require_path("/".join(parts), "path", "a code file's path")
    except ExerciseError:
        return None
