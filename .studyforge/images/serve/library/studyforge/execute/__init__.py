"""The command runner — the only package in the framework that runs a corpus's commands.

⛔ It is also the only package that starts a process at all (spec §8.3): the
headless browser `studyforge.look` opens built pages in is launched here too.

**What it does.** Runs a corpus's commands — a unit's run and test commands,
read from its generated document — inside the corpus's runner container when
the reader has it up, and on the host otherwise, streaming the merged output
line by line and ending with exactly one exit line.

**How you use it.**

    from studyforge.execute import Runner, container_for

    runner = Runner(source_root, container_for(source))
    handle = runner.start([exercise.run_command, exercise.test_command])
    for line in handle.lines():  # ... and last, "--- exit 0 ---"
        ...

`handle.stop()` ends a run from another thread. A refused input raises
`RunRefused`; a command that runs and fails is output, never an exception.
For the page, filter the stream down to what the reader asked for:

    from studyforge.execute import filter_lines, select

    for line in filter_lines(handle.lines(), select(manifest.runtimes)):
        ...

**Depends on.** The standard library, `studyforge.exercise` for what a command
and a path may be, and `studyforge.archive`'s `scrub` for R7. ⛔ Not on `serve`:
the direction is serve-depends-on-execute, and inverting it is how the
web-facing process ends up holding the socket that spec §8.3 forbids it.

| module | what it owns |
|---|---|
| `runner` | `Runner`, the two modes' launchers, and the run's environment |
| `handle` | `RunHandle`: the sequence, the stream, the exit line, stop and timeout |
| `mode` | `ModeProbe`: is the runner container up over this root, cached briefly |
| `editor` | `EditorProbe`: where a running editor is, what it opens and what it holds |
| `workbench` | one practice's window URLs and the workspace settings it is read under |
| `conventions` | what a build tool and a language call their build files, sources and tests |
| `codetree` | the copy of a corpus's code its editor opens and its runner tests |
| `codepair` | a code file's source and test, and the command that runs that test in the copy |
| `output` | `LineGate`: every line relative to the source root, then scrubbed |
| `quiet` | the output filter: the declared build tool's own lines go, a failure never |
| `commands` | what the runner will start, checked before any process exists |
| `browser` | a headless browser this machine has, asked for one page's screenshot and DOM |
| `errors` | `RunRefused`, the one exception |

## The seam: the reader starts the runner

⭐ **The READER starts the runner container** with `code-server-toolchain`'s
documented run line — the source root alone at `/work`, `--network none`, no
port, no socket. ⭐ **This package PROBES that it is up and runs
`docker exec -w /work/<cwd>` with the command's argv VERBATIM; not up means
HOST mode, the same argv.** ⛔ It never starts, stops or builds a container, and
⛔ **the Docker socket is never mounted into the serving process** (§8.3) — not
behind a flag, not "only locally".

⭐ **Both modes are one contract** — the same argv, the same directory relative
to the source root, the same environment, output relative to the source root in
both — so a run from a page and a run from the reader's own terminal
agree. `runner`'s docstring is the table.

⚠️ **Reproducibility comes from the image, not the host** (R15). Host mode is
the honest fallback, not an equal: it runs whatever toolchain the host has.

⚠️ **Raw build output is not reader output.** `quiet` filters it down to what
the reader asked for, on top of this stream and after `LineGate`: the
one build tool a corpus declares in `runtimes` loses its banners, timings and
help footers, and every other line survives unedited, including every error,
every stack frame and the exit line. An undeclared, unknown or ambiguous
toolchain passes through unfiltered. ⛔ The page filters; a reader's own
terminal does not.
"""

from __future__ import annotations

from studyforge.execute.browser import (
    BROWSER_NAMES,
    BrowserProfile,
    PageSeen,
    capture_page,
    find_browser,
)
from studyforge.execute.codepair import Pair, is_code, pair, pairing, test_command, test_commands
from studyforge.execute.codetree import CODE_COPY, IGNORE_TEXT, CodeRefused, in_copy, sync
from studyforge.execute.commands import (
    CONTAINER_PREFIX,
    EDITOR_CONTAINER_TEMPLATE,
    ROOT_DIR,
    container_for,
    editor_container_for,
    require_commands,
    require_container,
    require_workdir,
)
from studyforge.execute.conventions import BUILD_FILES, SKIPPED, is_a_test, source_suffixes
from studyforge.execute.editor import EDITOR_TTL, Editor, EditorProbe
from studyforge.execute.errors import RunRefused
from studyforge.execute.handle import EXIT_STOPPED, EXIT_TIMEOUT, RunHandle, exit_line
from studyforge.execute.instance import INSTANCE_FILE, Names, declares_runner, recorded
from studyforge.execute.mode import CONTAINER, HOST, MODES, WORKDIR_IN_CONTAINER, ModeProbe
from studyforge.execute.output import LineGate
from studyforge.execute.preflight import problems as instance_problems
from studyforge.execute.preflight import refuse as refuse_instance
from studyforge.execute.published import (
    ALLOWED_DIR,
    ALLOWED_FILE,
    ALLOWED_IGNORE,
    DeclaredEditorProbe,
    Published,
    from_environment,
    write_allowed,
)
from studyforge.execute.quiet import TOOLCHAINS, Quiet, Toolchain, filter_lines, select
from studyforge.execute.remote import SERVICE_PORT, Service
from studyforge.execute.runner import RUN_ENVIRONMENT, RUNNER_DOWN, SERVICE, Runner
from studyforge.execute.workbench import (
    MAIN_KEY,
    SETTINGS_DIR,
    SETTINGS_FILE,
    TEST_KEY,
    WorkbenchRefused,
    open_url,
    practice_folder,
    settings,
    write_settings,
)

__all__ = [
    "ALLOWED_DIR",
    "ALLOWED_FILE",
    "ALLOWED_IGNORE",
    "BROWSER_NAMES",
    "BUILD_FILES",
    "CODE_COPY",
    "CONTAINER",
    "CONTAINER_PREFIX",
    "EDITOR_CONTAINER_TEMPLATE",
    "EDITOR_TTL",
    "EXIT_STOPPED",
    "EXIT_TIMEOUT",
    "HOST",
    "IGNORE_TEXT",
    "INSTANCE_FILE",
    "MAIN_KEY",
    "MODES",
    "ROOT_DIR",
    "RUNNER_DOWN",
    "RUN_ENVIRONMENT",
    "SERVICE",
    "SERVICE_PORT",
    "SETTINGS_DIR",
    "SETTINGS_FILE",
    "SKIPPED",
    "TEST_KEY",
    "TOOLCHAINS",
    "WORKDIR_IN_CONTAINER",
    "BrowserProfile",
    "CodeRefused",
    "DeclaredEditorProbe",
    "Editor",
    "EditorProbe",
    "LineGate",
    "ModeProbe",
    "Names",
    "PageSeen",
    "Pair",
    "Published",
    "Quiet",
    "RunHandle",
    "RunRefused",
    "Runner",
    "Service",
    "Toolchain",
    "WorkbenchRefused",
    "capture_page",
    "container_for",
    "declares_runner",
    "editor_container_for",
    "exit_line",
    "filter_lines",
    "find_browser",
    "from_environment",
    "in_copy",
    "instance_problems",
    "is_a_test",
    "is_code",
    "open_url",
    "pair",
    "pairing",
    "practice_folder",
    "recorded",
    "refuse_instance",
    "require_commands",
    "require_container",
    "require_workdir",
    "select",
    "settings",
    "source_suffixes",
    "sync",
    "test_command",
    "test_commands",
    "write_allowed",
    "write_settings",
]
