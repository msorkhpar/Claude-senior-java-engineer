r"""The publisher's instance, checked before anything is served: each bad value, by name.

**What it does.** Reads a corpus's `instance.env` — the publisher's own file —
and its generated compose file, and answers one sentence per value that cannot
work, each naming its key: a port outside 1–65535, one port for two services, a
name compose or `docker` would refuse, a key this file does not carry, and a
published address off loopback while the editor runs with no authentication.

**How you use it.**

    problems(root)      # [] when the instance can be brought up and served
    refuse(root)        # raise RunRefused with every sentence, or return

`studyforge serve` calls `refuse` for every corpus that declares a runner
before it binds, and the compose file's `preflight` service runs
`studyforge preflight /corpus` before the site, the editor and the runner.

**Depends on.** `instance` for the file, its keys and its value checks. ⛔ No
process and no socket: two small files are read.

## ⭐ THE FILE IS THE PUBLISHER'S, SO A BAD VALUE IS A REFUSAL, NEVER A FINDING

⭐ A publisher sets ports in `instance.env` by hand, and that is never a
hand-edit (`skills.execution.written.PUBLISHERS`). ⛔ So what protects a reader
from a value that cannot work is this check, where the value is USED, and it
says which key and why — never the value, which may be personal.

## ⛔ A WIDER BIND NEEDS THE EDITOR'S AUTHENTICATION

⭐ The loopback address is written in the compose file, not in `instance.env`,
so a key here that tries to set one (`BIND_WORDS`) is refused by name rather
than silently ignored. ⛔ A compose file that publishes a port off loopback
while the editor runs `--auth=none` is refused too: loopback is that editor's
whole access control.
"""

from __future__ import annotations

import re
from pathlib import Path

from studyforge.execute.errors import RunRefused
from studyforge.execute.instance import COMPOSE_FILE, INSTANCE_FILE, VARIABLES, problems_in

#: The prefix every key of the file carries.
PREFIX = "STUDYFORGE_"

#: Words that mark a key as an attempt to set an address rather than a port.
BIND_WORDS = ("BIND", "HOST", "ADDRESS", "INTERFACE", "LISTEN")

#: The addresses a published port may name without the editor's authentication.
LOOPBACK = frozenset({"127.0.0.1", "localhost", "::1", "[::1]"})

#: What the editor's command says when it asks for no password.
NO_AUTH = "--auth=none"

#: One published port in the rendered compose file: its address, and the key its port is read from.
PUBLISHED = re.compile(
    r'^\s*-\s*"?(?P<address>[^":\s]*):\$\{(?P<key>STUDYFORGE_\w+?)(?::-[^}]*)?\}'
)


def problems(root: Path | str) -> list[str]:
    """Return one sentence per value of this instance that cannot work, each naming its key."""
    root = Path(root)
    entries = _entries(root / INSTANCE_FILE)
    known = {key: value for key, value in entries if key in VARIABLES}
    found = [f"{INSTANCE_FILE}: {sentence}" for sentence in problems_in(known)]
    for key, _ in entries:
        if key.startswith(PREFIX) and key not in VARIABLES:
            found.append(f"{INSTANCE_FILE}: {key} {_why_unknown(key)}")
    found.extend(_wide_binds(root / COMPOSE_FILE))
    return found


def refuse(root: Path | str) -> None:
    """Raise `RunRefused` naming every value that cannot work, or return when none."""
    found = problems(root)
    if found:
        raise RunRefused("the instance cannot be served: " + "; ".join(found))


def _entries(path: Path) -> list[tuple[str, str]]:
    """Return every `NAME=value` line of the file, in order; none when it is absent."""
    try:
        content = path.read_text(encoding="utf-8")
    except OSError, UnicodeDecodeError:
        return []
    found = []
    for line in content.splitlines():
        name, equals, value = line.partition("=")
        if equals and not line.lstrip().startswith("#"):
            found.append((name.strip(), value.strip()))
    return found


def _why_unknown(key: str) -> str:
    """Say why a key this file does not carry is refused rather than ignored."""
    if any(word in key for word in BIND_WORDS):
        return (
            "is not a value this file carries: every port is published on 127.0.0.1, "
            "written in the compose file, and a wider bind needs the editor's "
            "authentication, which this compose file does not give it"
        )
    return f"is not a value this file carries; it carries {', '.join(VARIABLES)}"


def _wide_binds(compose: Path) -> list[str]:
    """Name every port the compose file publishes off loopback while its editor has no password."""
    try:
        text = compose.read_text(encoding="utf-8")
    except OSError, UnicodeDecodeError:
        return []
    if NO_AUTH not in text:
        return []
    return [
        f"{COMPOSE_FILE}: {found['key']} is published on an address other than loopback "
        "while the editor runs with no password; restore the editor's authentication "
        "before widening a bind, or publish on 127.0.0.1"
        for found in (PUBLISHED.match(line) for line in text.splitlines())
        if found is not None and found["address"] not in LOOPBACK
    ]
