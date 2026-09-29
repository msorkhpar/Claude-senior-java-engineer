"""Fetch the editor's pinned files and refuse any whose sha256 is not the recorded one.

**What it does.** Reads lines `<file name>|<url>|<sha256>` (from `build.py`,
out of `editor-pins.json`), downloads each in order into a directory, and
exits non-zero naming the first file whose bytes do not hash to the value
recorded in the repository. It is `ADD --checksum` for a list whose length a
Dockerfile cannot know.

**How you use it.** Only inside the Dockerfile's `fetch` stage:

    python3 fetch.py <lines file> <output directory>

⛔ **No checksum is fetched.** The value checked against is the one recorded
at pin time, so a host that serves a different file fails the build instead of
agreeing with itself. ⛔ The request carries a placeholder User-Agent and no
identity and no credentials.

**Depends on.** The standard library only.
"""

from __future__ import annotations

import hashlib
import sys
import urllib.request
from pathlib import Path

USER_AGENT = "Example/0.1 (+https://example.invalid)"


def parse(text: str) -> list[tuple[str, str, str]]:
    """The non-empty lines, each split into (name, url, sha256)."""
    entries = []
    for line in text.splitlines():
        if line.strip():
            name, url, sha256 = line.strip().split("|")
            entries.append((name, url, sha256))
    return entries


def check(name: str, data: bytes, sha256: str) -> str | None:
    """None when the bytes are the pinned ones; otherwise what is wrong, naming the file."""
    actual = hashlib.sha256(data).hexdigest()
    if actual != sha256:
        return f"{name}: sha256 {actual} is not the value editor-pins.json records ({sha256})"
    return None


def main(argv: list[str]) -> int:
    lines, out = Path(argv[0]), Path(argv[1])
    out.mkdir(parents=True, exist_ok=True)
    for name, url, sha256 in parse(lines.read_text(encoding="utf-8")):
        request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
        with urllib.request.urlopen(request, timeout=600) as response:
            data = response.read()
        problem = check(name, data, sha256)
        if problem:
            print(problem, file=sys.stderr)
            return 1
        (out / name).write_bytes(data)
        print(f"{name}: {sha256}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
