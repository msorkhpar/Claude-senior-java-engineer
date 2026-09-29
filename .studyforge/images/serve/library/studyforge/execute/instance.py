r"""Which containers belong to THIS checkout of a corpus: the names its instance recorded.

**What it does.** Reads the one file a corpus's execution onboarding keeps for
the values that differ between two instances of one corpus on one host — the
compose project, the editor's and the study server's host ports, and the
editor's, the runner's and the study server's container names — and answers
the two names `serve` looks its containers up by.

**How you use it.**

    names = recorded(corpus_root, source)    # Names(runner=..., editor=...)
    Runner(corpus_root, names.runner)

`defaults(source)` is what an instance that recorded nothing is called, and
`text(values)` is the file's bytes, which the execution skill's record step
writes (`skills.execution.instance`).

**Depends on.** `commands` for the one spelling of each default name and of a
safe container name. ⛔ Nothing source-specific (R1), and no process: this reads
one small file under the corpus root.

## ⛔ WHY A SECOND INSTANCE NEEDS THIS AT ALL

⚠️ A second checkout of one corpus — the verification a pin advance needs,
beside the reader's live site — must come up without an override file and be
served against its own runner. ⭐ **So each of them is a compose
interpolation whose default is the fixed value**, and the value an instance chose is
recorded here, in the file its compose command reads, which is also the file
this module reads. ⛔ **One record, two readers — never two spellings.**

## ⭐ AN INSTANCE THAT RECORDED NOTHING IS THE ONE IT ALWAYS WAS

An absent file, or an absent key, answers the default — the fixed names a corpus
without this file uses — so such a corpus is served exactly as it always
is. ⚠️ **A value that is not one safe container name is read as
absent, not raised**: every probe that uses a name also checks that the
container binds THIS corpus root (`EditorProbe`, `ModeProbe`), so a default that
reaches another checkout's container finds it is not this one's and answers
*no editor* / `host` — the answer for no container at all.
"""

from __future__ import annotations

import re
from collections.abc import Mapping
from dataclasses import dataclass
from pathlib import Path

from studyforge.execute.commands import container_for, editor_container_for, require_container
from studyforge.execute.errors import RunRefused
from studyforge.exercise import SAFE_SEGMENT

#: ⛔ **The file an instance's values are recorded in, and its ONE spelling.**
#: Beside the two tag files under the execution skill's directory, and read by
#: the reader's compose command with them (`skills.execution.onboard`).
INSTANCE_FILE = ".studyforge/execution/instance.env"

#: ⛔ **The compose file the execution skill writes, and its ONE spelling here.** A
#: corpus holding it DECLARES a runner, and a run of it never falls back to the host
#: (`Runner(required=True)`): its code runs in that runner or not at all.
COMPOSE_FILE = ".studyforge/execution/compose.yaml"

#: The compose project: what the file's `name:` interpolates, so a second
#: instance's volumes, network and service identities are its own.
PROJECT = "STUDYFORGE_PROJECT"
#: The editor's published host port. ⛔ The PORT only: the loopback address is
#: written in the compose file itself and no recorded value can widen it.
EDITOR_PORT = "STUDYFORGE_EDITOR_PORT"
#: The editor's `container_name`, which `serve` probes.
EDITOR_NAME = "STUDYFORGE_EDITOR_NAME"
#: The runner's `container_name`, which a Run or a Submit execs into.
RUNNER_NAME = "STUDYFORGE_RUNNER_NAME"

#: The study server's published host port, when a course is published with one
#: compose. ⛔ The PORT only, as for the editor's.
SITE_PORT = "STUDYFORGE_SITE_PORT"

#: The study server's `container_name` in that compose.
SITE_NAME = "STUDYFORGE_SITE_NAME"

#: Every variable the file holds, in the order it is written.
VARIABLES = (PROJECT, EDITOR_PORT, EDITOR_NAME, RUNNER_NAME, SITE_PORT, SITE_NAME)

#: The four every instance records. ⭐ The study server's two are optional: a
#: file that predates them is read as it stands, compose's defaults filling in.
REQUIRED = VARIABLES[:4]

#: The ports an instance must record. ⭐ Each is checked as one.
PORT_VARIABLES = (EDITOR_PORT, SITE_PORT)

#: The port a published study server listens on when an instance recorded none:
#: the one `serve` listens on when given none (`serve.app.DEFAULT_PORT`, held equal
#: by a test, since this package may not import the server).
DEFAULT_SITE_PORT = 8765

#: The study server's container name, `source` in its slot.
SITE_CONTAINER_TEMPLATE = "studyforge-site-{source}"

#: The header the file is written under. ⭐ It says whose file it is.
PUBLISHER_HEADER = (
    "# Written once by studyforge's execution skill, with defaults; from then on this\n"
    "# file is yours, and no regeneration writes over it. Set the ports here by hand,\n"
    "# or with the skill's record step, which checks every value first.\n"
)

#: What the file says it holds, written under the caller's header.
HOLDS = (
    "# This instance's compose project, host ports and container names: the one\n"
    "# place a port is set. Each is checked before anything is served: a port\n"
    "# outside 1-65535, one port for two services, or a name that is not one word\n"
    "# is refused by `studyforge serve` and by the compose preflight, by its name.\n"
)

#: The compose project a corpus that chose none is brought up under.
PROJECT_PREFIX = "studyforge-"

#: What compose accepts as a project name.
PROJECT_NAME = re.compile(r"\A[a-z0-9][a-z0-9_-]*\Z")

#: The ports a recorded value may name: every TCP port there is. ⭐ A publisher
#: may choose a low port; the daemon publishes it and the site's container
#: binds it, since Docker leaves unprivileged ports open from 0 inside one.
PORTS = range(1, 65536)


@dataclass(frozen=True, slots=True)
class Names:
    """The two containers one instance of a corpus runs: the runner and the editor."""

    runner: str
    editor: str


def declares_runner(root: Path) -> bool:
    """Whether the corpus at `root` declares a runner: its execution files are written."""
    return (Path(root) / COMPOSE_FILE).is_file()


def project_for(source: str) -> str:
    """Return the compose project an instance of `source` that chose none runs under."""
    return PROJECT_PREFIX + source


def site_container_for(source: str) -> str:
    """Name the study server's container a course published with one compose runs."""
    return require_container(SITE_CONTAINER_TEMPLATE.format(source=source))


def defaults(source: str, *, port: int) -> dict[str, str]:
    """Return every recorded value an instance of `source` that chose none has."""
    return {
        PROJECT: project_for(source),
        EDITOR_PORT: str(port),
        EDITOR_NAME: editor_container_for(source),
        RUNNER_NAME: container_for(source),
    }


def site_defaults(source: str) -> dict[str, str]:
    """Return the study server's two values an instance of `source` that chose none has."""
    return {SITE_PORT: str(DEFAULT_SITE_PORT), SITE_NAME: site_container_for(source)}


def recorded(root: Path, source: str) -> Names:
    """Return the names the instance at `root` recorded, each defaulted when it recorded none."""
    values = read(root)
    return Names(
        runner=_name(values.get(RUNNER_NAME)) or container_for(source),
        editor=_name(values.get(EDITOR_NAME)) or editor_container_for(source),
    )


def read(root: Path) -> dict[str, str]:
    """Return every `NAME=value` line of the instance file at `root`; `{}` if there is none."""
    try:
        content = (root / INSTANCE_FILE).read_text(encoding="utf-8")
    except OSError, UnicodeDecodeError:
        return {}
    return parsed(content)


def parsed(content: str) -> dict[str, str]:
    """Return the variables `content` sets, in compose's plain `NAME=value` form."""
    values: dict[str, str] = {}
    for line in content.splitlines():
        name, equals, value = line.partition("=")
        if equals and name in VARIABLES:
            values[name] = value.strip()
    return values


def checked(values: Mapping[str, str]) -> dict[str, str]:
    """Return `values` if every one is legal to record, or raise `RunRefused`.

    ⛔ **Each is checked because compose interpolates it verbatim**: a port
    that is not a whole number in `PORTS` could carry an address, and a name
    that is not one safe word could carry anything. Neither is reproduced (R7).
    """
    missing = [one for one in REQUIRED if one not in values]
    if missing:
        raise RunRefused(f"an instance records every one of {list(REQUIRED)}; missing {missing}")
    found = problems_in(values)
    if found:
        raise RunRefused("; ".join(found))
    return {one: values[one] for one in VARIABLES if one in values}


def problems_in(values: Mapping[str, str]) -> list[str]:
    """Return one sentence, naming its key, for every value here that cannot work.

    ⭐ Every present value is checked and every problem said — the one sentence
    a publisher needs per key — and an absent value is compose's default, fine.
    """
    found = []
    for one in (port for port in PORT_VARIABLES if port in values):
        port = values[one]
        if not port.isdecimal() or int(port) not in PORTS:
            found.append(
                f"{one} must be a whole port number from {PORTS.start} to {PORTS.stop - 1}; "
                "the value is not reproduced here, since a refusal never quotes a value that "
                "may be personal"
            )
    if EDITOR_PORT in values and values[EDITOR_PORT] == values.get(SITE_PORT):
        found.append(
            f"{EDITOR_PORT} and {SITE_PORT} name one port: two services cannot both publish it"
        )
    for one in (name for name in (PROJECT, EDITOR_NAME, RUNNER_NAME, SITE_NAME) if name in values):
        if _name(values[one]) is None:
            found.append(
                f"{one} must be one word of ASCII letters, digits, '.', '_' and '-', not "
                "beginning '-'; the value is not reproduced here, since a refusal never "
                "quotes a value that may be personal"
            )
    if PROJECT in values and PROJECT_NAME.match(values[PROJECT]) is None:
        found.append(
            f"{PROJECT} must be lower-case letters, digits, '-' and '_', beginning with a "
            "letter or a digit: compose refuses any other project name"
        )
    return found


def text(values: Mapping[str, str], *, header: str) -> str:
    """Return the instance file's bytes: `header`, what it holds, then each variable."""
    kept = checked(values)
    return header + HOLDS + "".join(f"{one}={value}\n" for one, value in kept.items())


def _name(value: str | None) -> str | None:
    """Return `value` if it is one safe container name, else `None`."""
    if value is None or SAFE_SEGMENT.match(value) is None:
        return None
    return value
