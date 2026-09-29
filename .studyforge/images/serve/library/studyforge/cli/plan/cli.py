"""The command line: one root in, one plan out, an exit code a script reads.

**What it does.** Parses the arguments `studyforge plan` takes, derives the
plan, prints it and returns the exit code.

**How you use it.** `main(argv) -> int`, and `python3 -m studyforge.cli.plan`.
⭐ The dispatcher registers this same callable as the `plan` verb, so the two
cannot disagree about what the command does.

**Depends on.** `cli.plan.derive`, `validate.cli` for `UNUSABLE`, and
`argparse`.

⛔ **Exit codes are usable from a script** and mean one thing each: `0` a plan
was produced, `1` some of the corpus could not be planned — or the media measured
on disk crosses a limit `corpus.json` declares (§5) — `2` the tool could
not run at all. ⚠️ The third is `validate`'s own, imported rather than
respelled — a script that cannot tell *"your corpus is broken"* from *"you
gave me a directory that does not exist"* treats one as the other, and CI goes
green on a typo.
"""

from __future__ import annotations

import argparse
from pathlib import Path

from studyforge.cli.plan.derive import plan_for
from studyforge.validate.cli import UNUSABLE


def build_parser() -> argparse.ArgumentParser:
    """Return the argument parser, so a test can read the interface."""
    parser = argparse.ArgumentParser(
        prog="studyforge plan",
        description=(
            "Say what a build will write into a repository and what it will edit, "
            "from the corpus's own declarations and before anything is generated."
        ),
    )
    parser.add_argument("root", help="the corpus root — the directory holding corpus.json")
    parser.add_argument(
        "--bytes-per-unit",
        type=int,
        default=None,
        metavar="N",
        help=(
            "also project the media footprint at N bytes of generated media per unit; "
            "the media already on disk is measured with or without it"
        ),
    )
    return parser


def main(argv: list[str] | None = None, out=None) -> int:
    """Print the plan for one corpus root and return an exit code a script reads."""
    import sys

    stream = sys.stdout if out is None else out
    arguments = build_parser().parse_args(argv)
    root = Path(arguments.root)
    if not root.is_dir():
        # ⛔ Names what was asked for, never the absolute path it resolved to
        # (R7): a plan is the most-pasted artifact this command produces.
        print(f"{arguments.root}: not a directory", file=stream)
        return UNUSABLE
    plan = plan_for(root, bytes_per_unit=arguments.bytes_per_unit)
    for line in plan.lines():
        print(line, file=stream)
    return plan.exit_code
