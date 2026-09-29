r"""The command line: one root in, one report out, an exit code a script reads.

**What it does.** Parses the arguments `studyforge validate` takes, runs the
checks, prints the report and returns the exit code.

**How you use it.** `main(argv) -> int`, and `python3 -m studyforge.validate`.
⭐ The dispatcher registers this same callable as the `validate` verb, so the
two cannot disagree about what the command does.

**Depends on.** `validate.run`, `validate.report`, `studyforge.exitcodes`, and
`argparse`.

⛔ **Exit codes are usable from a script** and mean one thing each: `0` the
archive is valid, `1` it is not, `2` the tool could not run at all. A script
that cannot tell "invalid" from "you gave me a directory that does not exist"
will treat one as the other, and CI will go green on a typo.

⚠️ **`2` is DEFINED in `studyforge.exitcodes` and re-exported here**.
⭐ It is the one of the three that is not a verdict about an archive, and the
dispatcher plus five modules under `cli/` already needed it; while it was
defined here, importing the installed command loaded this verb. ⛔ Nothing was
renumbered and this module's surface did not move: `from
studyforge.validate.cli import UNUSABLE` and `from studyforge.validate import
UNUSABLE` both still resolve, to the same object.
"""

from __future__ import annotations

import argparse
from pathlib import Path

from studyforge.exitcodes import UNUSABLE
from studyforge.validate.report import INVALID, OK
from studyforge.validate.run import validate

#: ⭐ Re-exported, not respelled: `UNUSABLE` above is the one object
#: `studyforge.exitcodes` defines, and `studyforge.validate.__all__` carries it
#: on. ⛔ A second `= 2` here would be a second definition to keep in step.
__all__ = ["UNUSABLE", "build_parser", "main"]


def build_parser() -> argparse.ArgumentParser:
    """Return the argument parser, so a test can read the interface."""
    parser = argparse.ArgumentParser(
        prog="studyforge validate",
        description=(
            "Decide whether a corpus's archive is valid. This is an adapter's "
            "definition of done: exit 0 means the archive is acceptable."
        ),
    )
    parser.add_argument("root", help="the corpus root — the directory holding corpus.json")
    parser.add_argument(
        "--no-narration",
        dest="narration",
        action="store_const",
        const=False,
        default=None,
        help=(
            "judge the archive with narration off for this run: no clip is checked "
            "against the words its paragraph says now"
        ),
    )
    return parser


def main(argv: list[str] | None = None, out=None) -> int:
    """Run the checks over one corpus root and print every finding."""
    import sys

    stream = sys.stdout if out is None else out
    arguments = build_parser().parse_args(argv)
    root = Path(arguments.root)
    if not root.is_dir():
        # ⛔ Names what was asked for and not the absolute path it resolved to
        # (R7): a report is the most-pasted artifact this tool produces.
        print(f"{arguments.root}: not a directory", file=stream)
        return UNUSABLE
    report = validate(root, narration=arguments.narration)
    for line in report.lines():
        print(line, file=stream)
    return report.exit_code if report.exit_code in (OK, INVALID) else INVALID
