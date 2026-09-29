"""The one installed command, and the table of verbs it dispatches to.

**What it does.** Reads the first argument as a verb, hands the rest to that
verb's own `cli.main`, and returns its exit code unchanged. It parses nothing
else: every flag a verb takes belongs to that verb's parser, so the dispatcher
cannot come to disagree with a stage about what an argument means.

**How you use it.** `main(argv) -> int`, which is what `[project.scripts]`
registers as `studyforge`, and `python3 -m studyforge.cli`. `VERBS` is the
registered table, read by the authoring checks rather than re-listed by them.

**Depends on.** `studyforge.exitcodes` for `UNUSABLE` — a module that belongs to
no stage and imports nothing. Each stage's `cli` module is imported by a loader
function here, and only when its verb is dispatched. ⛔ No stage imports this
one — the direction is one-way, so a stage stays runnable as
`python3 -m studyforge.<stage>` with the dispatcher absent.

## ⛔ A verb is resolved when it is DISPATCHED, not when this module loads

⭐ `import studyforge.cli.<anything>` runs this package, so a module-level import
of every verb's entry point made importing one verb load all of them.
⭐ A `Verb` carries a `load` function, a plain import statement in a body, and
`Verb.run` calls it when read. ⚠️ It is an IMPORT STATEMENT and never a module
reached by name: `tests/harness/test_isolation.py` refuses `importlib` in
framework source (R1). ⛔ **A loader registers nothing: `VERBS` is the only
place a verb is named**, and `run` is the same callable object the
verb's module defines.

⚠️ **The shared exit code is no verb's.** `UNUSABLE` lives in
`studyforge.exitcodes`, not in `validate.cli`, so **importing this module loads
no verb at all** and the mirror holds no verb exempt.

## ⛔ A verb is registered here only when it can be RUN

⭐ `reconcile` is named in this package's contract as a command the framework
will have; it is **not** in `VERBS`, because registering a verb against a
callable that does not exist yet makes an installed command that fails on
first invocation. ⚠️ That is the state `pyproject.toml` refused for
the whole command and the reasoning does not change one level down.

⛔ **The table is the population the authoring checks derive from.** A fenced
`studyforge <verb>` line in any document is checked against `VERBS`, reached
from `[project.scripts]`, so a document cannot offer a command this table does
not provide and a verb cannot be retired while a document still gives it.
"""

from __future__ import annotations

from collections.abc import Callable, Mapping
from dataclasses import dataclass

from studyforge.exitcodes import UNUSABLE

#: The command a user types. ⛔ One name, and `pyproject.toml` registers this
#: same spelling — `tests/studyforge/cli/test_dispatch.py` asserts they agree,
#: because a usage line that names a command nobody installed is worse than no
#: usage line.
PROGRAM = "studyforge"


@dataclass(frozen=True)
class Verb:
    """One registered subcommand: what it is called, what it does, what runs it.

    `load` imports the verb's module and returns its entry point.
    """

    name: str
    summary: str
    load: Callable[[], Callable[..., int]]

    @property
    def run(self) -> Callable[..., int]:
        """The verb's entry point, imported now rather than when `VERBS` was built."""
        return self.load()


def _validate() -> Callable[..., int]:
    from studyforge.validate.cli import main

    return main


def _plan() -> Callable[..., int]:
    from studyforge.cli.plan.cli import main

    return main


def _narrate() -> Callable[..., int]:
    from studyforge.cli.narrate.cli import main

    return main


def _build() -> Callable[..., int]:
    from studyforge.cli.site.cli import main

    return main


def _serve() -> Callable[..., int]:
    from studyforge.cli.serve import main

    return main


def _preflight() -> Callable[..., int]:
    from studyforge.cli.preflight import main

    return main


def _check() -> Callable[..., int]:
    from studyforge.cli.check import main

    return main


#: ⛔ **The registered table.** Ordered as a reader meets them: check the
#: archive, ask what a build would write, narrate it, write it, serve it, check
#: the instance it is served with, then check a unit's file the reader edited.
#: ⭐ `narrate` precedes `build` because clips are a build's INPUT (a build
#: only copies what `narrate` recorded), and `serve` follows `build` because
#: it serves what a build wrote.
VERBS: Mapping[str, Verb] = {
    verb.name: verb
    for verb in (
        Verb("validate", "decide whether a corpus's archive is valid", _validate),
        Verb("plan", "say what a build would write, before it writes it", _plan),
        Verb("narrate", "synthesise a corpus's clips from a narration service", _narrate),
        Verb("build", "write the site for one corpus into a directory you name", _build),
        Verb("serve", "serve a built site on loopback, adding the content API", _serve),
        Verb("preflight", "check a corpus's instance.env before it is brought up", _preflight),
        Verb("check", "run the test for a unit's file you edited, or its program if none", _check),
    )
}


def usage() -> list[str]:
    """Return the whole help text, one line at a time."""
    width = max(len(name) for name in VERBS)
    out = [
        f"usage: {PROGRAM} <verb> [options]",
        "",
        "Turn a body of teaching material into a local, offline study site.",
        "",
        "verbs:",
    ]
    out += [f"  {verb.name.ljust(width)}  {verb.summary}" for verb in VERBS.values()]
    out += [
        "",
        f"Run `{PROGRAM} <verb> --help` for the arguments one verb takes.",
    ]
    return out


def main(argv: list[str] | None = None, out=None) -> int:
    """Dispatch one verb and return its exit code, or say what the verbs are.

    ⛔ `2` for no verb and for an unknown one, which is the shared `UNUSABLE`
    imported rather than respelled: the tool could not run, and that is not a
    verdict about anybody's corpus.
    """
    import sys

    stream = sys.stdout if out is None else out
    arguments = list(sys.argv[1:] if argv is None else argv)
    if not arguments or arguments[0] in ("-h", "--help", "help"):
        for line in usage():
            print(line, file=stream)
        return 0 if arguments else UNUSABLE
    verb = arguments[0]
    if verb not in VERBS:
        print(f"{verb}: not a studyforge verb", file=stream)
        for line in usage():
            print(line, file=stream)
        return UNUSABLE
    return VERBS[verb].run(arguments[1:], out=out)
