"""Check a warmed Maven repository against pins.json, or record it.

**What it does.** `verify_repo.py PINS REPO` exits 0 only when the set of files
under REPO is EXACTLY the set `runtimes.maven.warm.files` records and every
file's sha256 matches. `verify_repo.py --record REPO` prints that mapping as JSON
for a person to review and copy into pins.json.

**Why exactly, and not "at least".** A file the pins do not name is an input
nobody pinned; a file they name that is missing is a warm that will fail
offline. Both are refused, each by its relative path.

**Depends on.** The standard library only; it runs inside the build.
"""

from __future__ import annotations

import hashlib
import json
import sys
from pathlib import Path


def digest_tree(repo: Path) -> dict[str, str]:
    """Every regular file under `repo`, by POSIX relative path, to its sha256."""
    found = {}
    for path in sorted(p for p in repo.rglob("*") if p.is_file()):
        found[path.relative_to(repo).as_posix()] = hashlib.sha256(path.read_bytes()).hexdigest()
    return found


def compare(expected: dict[str, str], actual: dict[str, str]) -> list[str]:
    """What differs, one line per file; empty when the two agree exactly."""
    problems = [f"unpinned file: {name}" for name in sorted(set(actual) - set(expected))]
    problems += [f"missing file: {name}" for name in sorted(set(expected) - set(actual))]
    problems += [
        f"checksum differs from pins.json: {name}"
        for name in sorted(set(expected) & set(actual))
        if expected[name] != actual[name]
    ]
    return problems


def main(argv: list[str]) -> int:
    if len(argv) == 2 and argv[0] == "--record":
        print(json.dumps(digest_tree(Path(argv[1])), indent=2, sort_keys=True))
        return 0
    if len(argv) != 2:
        print("usage: verify_repo.py PINS REPO | --record REPO", file=sys.stderr)
        return 2
    pins = json.loads(Path(argv[0]).read_text(encoding="utf-8"))
    expected = pins["runtimes"]["maven"]["warm"]["files"]
    if not expected:
        print("pins.json records no warmed Maven files; record them first", file=sys.stderr)
        return 1
    problems = compare(expected, digest_tree(Path(argv[1])))
    for line in problems:
        print(line, file=sys.stderr)
    if problems:
        return 1
    print("maven warm: every file matches pins.json")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
