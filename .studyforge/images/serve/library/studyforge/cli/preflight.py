r"""The `preflight` verb: say whether a corpus's instance can be brought up, naming each bad value.

**What it does.** Reads the corpus's `instance.env` — the publisher's own file —
and its generated compose file through `execute.preflight`, prints one line per
value that cannot work, each naming its key, and exits `0` only when there is
none. ⭐ The compose file's `preflight` service runs it before the site, the
editor and the runner start, so a bad port is refused by name before anything
binds; a publisher may run it by hand too.

**How you use it.**

    studyforge preflight <corpus-root>

`main(argv) -> int` is the callable the dispatcher registers.

**Depends on.** `execute.preflight` for every check, `archive.scrub` so a
printed path names no home, `validate` for the exit codes, and `argparse`.
⛔ Nothing here knows any source or material, and nothing is started: two files
are read.

⛔ **Exit codes:** `0` every value can work; `1` at least one cannot, each
named; `2` the root is not a directory.
"""

from __future__ import annotations

import argparse
from pathlib import Path

from studyforge.archive.scrub import scrub
from studyforge.execute import instance_problems
from studyforge.validate.cli import UNUSABLE
from studyforge.validate.report import INVALID, OK

#: What a clean instance says.
CLEAN = "preflight clean: every value in instance.env can be brought up and served"


def build_parser() -> argparse.ArgumentParser:
    """Return the argument parser, so a test can read the interface."""
    parser = argparse.ArgumentParser(
        prog="studyforge preflight",
        description="Check a corpus's instance.env before it is brought up, naming each bad value.",
    )
    parser.add_argument("root", help="the corpus root, the directory holding corpus.json")
    return parser


def main(argv: list[str] | None = None, out=None) -> int:
    """Check the instance at the root; print each problem; return an exit code."""
    import sys

    stream = sys.stdout if out is None else out
    arguments = build_parser().parse_args(argv)
    root = Path(arguments.root)
    if not root.is_dir():
        print(scrub(f"{arguments.root}: not a directory"), file=stream, flush=True)
        return UNUSABLE
    found = instance_problems(root)
    for line in found or [CLEAN]:
        print(scrub(f"preflight {line}" if found else line), file=stream, flush=True)
    return INVALID if found else OK
