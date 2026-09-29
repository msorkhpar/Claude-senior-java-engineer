r"""The study server published with one compose: its runner and its editor, as the compose declared.

**What it does.** Turns what `execute.published` read from the container's
environment into the two seams `routes.runs.Runs` takes — how a corpus's runner
is made, and how its editor is probed — and writes the runner's allowlist from
the corpus's own records before every run.

**How you use it.** `cli/serve.py`'s `--published` form:

    config = from_environment(os.environ)
    Runs(discovered, sources, runner=runner_for(config), editor=editor_for(config))

**Depends on.** `execute` for the runner, the service, the declared editor, the
allowlist and the test commands, and `serve.discovery` for the corpus. ⛔ No
`docker` and no socket anywhere: the Docker socket never reaches the serving
process (spec §8.3).

## ⭐ THE ALLOWLIST IS WRITTEN FROM THE RECORDS, WHOLE, BEFORE EVERY RUN

⭐ **Every argv a record names**: each authored or shipped practice's
`run_command` and `test_command` (its archive document's `exercise`), and each
test file's command in the copy of the corpus's code (`execute.test_commands`).
⛔ **Never the argv a request carried**: the list is recomputed from the corpus
on disk, so a request can only ask for a command already in it, and a corpus
rebuilt while serving is read again at the next run.
"""

from __future__ import annotations

import json
from collections.abc import Callable, Iterable, Mapping, Sequence
from pathlib import Path

from studyforge.archive.scrub import PersonalDataLeak, assert_clean
from studyforge.execute import (
    DeclaredEditorProbe,
    EditorProbe,
    Published,
    Runner,
    RunRefused,
    declares_runner,
    from_environment,
    instance_problems,
    refuse_instance,
    test_commands,
    write_allowed,
)
from studyforge.serve.discovery import ServedCorpus

#: The working directory every recorded command runs in: the corpus root.
ROOT = "."

#: The practice documents of one unit's archive directory.
PRACTICES = "practice-*.json"

#: The two commands a practice's record may name.
COMMANDS = ("run_command", "test_command")


#: The files a container runtime leaves at a container's root; ⭐ one of them is
#: how the published form knows it is inside its own container.
CONTAINER_MARKERS = ("/.dockerenv", "/run/.containerenv")

#: What starting the published form may raise: a refusal, or an allowlist it cannot write.
REFUSED = (RunRefused, OSError)

#: What the published form says outside a container.
NOT_A_CONTAINER = (
    "--published binds every address of its own container and is refused outside one; "
    "serve on the host without it, or bring the course up with its compose file"
)


def published_config(environ: Mapping[str, str], markers: Sequence[str] | None = None) -> Published:
    """Return what the compose declared, or raise `RunRefused` outside a container or when bare.

    ⛔ The published form binds `0.0.0.0` and admits any peer, which is safe only
    because the compose file publishes that port on `127.0.0.1` alone; on a
    host, the same bind would be every interface, so it is refused there.
    """
    if not any(Path(marker).exists() for marker in markers or CONTAINER_MARKERS):
        raise RunRefused(NOT_A_CONTAINER)
    config = from_environment(environ)
    if config is None or config.service is None:
        raise RunRefused("--published needs the run service its compose file declares")
    return config


def instance_refusal(root: Path) -> str | None:
    """Return why this corpus's instance cannot be served, naming each key, or `None`."""
    found = instance_problems(root) if declares_runner(root) else []
    return "the instance cannot be served: " + "; ".join(found) if found else None


def prepared(
    environ: Mapping[str, str], corpora: Iterable[ServedCorpus], published: bool
) -> Published | None:
    """Refuse an instance that cannot work, by name; then start the published form if asked.

    ⭐ Both forms of `serve`: every corpus that declares a runner has its
    publisher's `instance.env` checked before anything binds (`execute.preflight`).
    """
    corpora = list(corpora)
    for corpus in corpora:
        if declares_runner(corpus.root):
            refuse_instance(corpus.root)
    return start_published(environ, corpora) if published else None


def start_published(environ: Mapping[str, str], corpora: Iterable[ServedCorpus]) -> Published:
    """Return the published config with every corpus's allowlist written, or raise `REFUSED`.

    ⭐ Written at start, so the runner holds a list before the first run asks.
    """
    config = published_config(environ)
    for corpus in corpora:
        write_allowed(corpus.root, allowed_runs(corpus))
    return config


def allowed_runs(corpus: ServedCorpus) -> list[tuple[str, list[str]]]:
    """Return every `(cwd, argv)` the corpus's records name, practices first."""
    found: list[tuple[str, list[str]]] = []
    for unit in corpus.corpus.units:
        for path in sorted(Path(unit.directory).glob(PRACTICES)):
            for argv in _commands(path):
                found.append((ROOT, argv))
    runtimes = corpus.corpus.manifest.runtimes
    found.extend((ROOT, argv) for argv in test_commands(corpus.root, runtimes))
    return found


def runner_for(config: Published) -> Callable[[ServedCorpus], Runner]:
    """Return the seam making a corpus's runner: the run service, after the allowlist."""

    def made(corpus: ServedCorpus) -> Runner:
        if config.service is None:
            raise RunRefused("this published server declares no run service")
        write_allowed(corpus.root, allowed_runs(corpus))
        return Runner(corpus.root, service=config.service)

    return made


def editor_for(config: Published) -> Callable[[ServedCorpus], DeclaredEditorProbe | EditorProbe]:
    """Return the seam that probes a corpus's editor: the one the compose declared.

    ⭐ A compose that declares no editor answers no editor, through the probe
    that asks nothing (`EditorProbe` with no container).
    """

    def probed(corpus: ServedCorpus) -> DeclaredEditorProbe | EditorProbe:
        return config.editor_probe() or EditorProbe(corpus.root, None)

    return probed


def _commands(path: Path) -> list[list[str]]:
    """Return the commands one practice document's record names; an unreadable one names none.

    ⛔ Gated like every document the framework decodes (R7): a document carrying
    personal data names no command, and nothing of it reaches the list.
    """
    try:
        document = json.loads(path.read_text(encoding="utf-8"))
        assert_clean(document, path.name)
    except OSError, UnicodeDecodeError, ValueError, PersonalDataLeak:
        return []
    exercise = document.get("exercise") if isinstance(document, dict) else None
    if not isinstance(exercise, dict):
        return []
    return [
        list(argv)
        for key in COMMANDS
        if isinstance(argv := exercise.get(key), list)
        and argv
        and all(isinstance(one, str) for one in argv)
    ]
