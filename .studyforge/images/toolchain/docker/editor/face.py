"""Take the editor's code face out of its pinned archive, and write the rules that load it.

**What it does.** Reads lines `<member>|<weight or "licence">|<sha256>` (from
`build.py`, out of `editor-pins.json`'s `face`), copies each member of the
release archive into a directory under its own base name, and refuses the
first whose bytes do not hash to the recorded value. It then writes
`faces.css`: one `@font-face` rule per weight, each loading its file by a URL
relative to the stylesheet it is appended to, and the licence beside them.

**Why.** The study page sets code in JetBrains Mono, vendored byte for byte.
The workbench draws in the READER's browser, so a face the image merely holds
on disk would never reach it: the browser must be served the file and told its
name. ⛔ The workbench's content policy allows fonts from its own origin only
(`font-src 'self' blob:`), so the faces are served beside the workbench's
stylesheet rather than embedded as `data:` URIs the way the page embeds them.

**How you use it.** Only inside the Dockerfile's `face` stage:

    python3 face.py <archive> <lines> <output directory>

**Depends on.** The standard library only. ⛔ It fetches nothing: the archive
arrived by `ADD --checksum` against the sha256 recorded at pin time.
"""

from __future__ import annotations

import hashlib
import sys
import zipfile
from pathlib import PurePosixPath, Path

#: The family name every rule declares, and the marker the Dockerfile checks.
FAMILY = "JetBrains Mono"
MARKER = "/* studyforge: the page's code face */"


def parse(text: str) -> list[tuple[str, str, str]]:
    """The non-empty lines, each split into (member, weight or 'licence', sha256)."""
    return [tuple(line.strip().split("|")) for line in text.splitlines() if line.strip()]


def rules(entries: list[tuple[str, str, str]]) -> str:
    """The stylesheet: the marker, then one `@font-face` per weight, in the order given.

    ⭐ `font-display: block`: the editor measures its character width once the
    face is there, never against a fallback it would then have to re-measure.
    """
    out = [MARKER]
    for member, weight, _ in entries:
        if weight == "licence":
            continue
        name = PurePosixPath(member).name
        out.append(f'@font-face{{font-family:"{FAMILY}";font-style:normal;font-weight:{weight};'
                   f'font-display:block;src:url("faces/{name}") format("woff2")}}')
    return "\n".join(out) + "\n"


def main(argv: list[str]) -> int:
    archive, lines, out = Path(argv[0]), Path(argv[1]), Path(argv[2])
    entries = parse(lines.read_text(encoding="utf-8"))
    out.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(archive) as release:
        for member, _, sha256 in entries:
            data = release.read(member)
            actual = hashlib.sha256(data).hexdigest()
            if actual != sha256:
                print(f"{member}: sha256 {actual} is not the value editor-pins.json records ({sha256})",
                      file=sys.stderr)
                return 1
            (out / PurePosixPath(member).name).write_bytes(data)
            print(f"{member}: {sha256}")
    (out / "faces.css").write_text(rules(entries), encoding="utf-8")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
