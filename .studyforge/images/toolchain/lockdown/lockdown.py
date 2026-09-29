"""The workbench lockdown extension: its identity, and packing it into a `.vsix`.

**What it does.** `lockdown/` holds the practice-focus extension — plain
CommonJS against the `vscode` module the workbench provides, no build step and
no dependencies. This module is the rest of the artifact: `identity(root)`
reads the ONE place its identifier is written (`package.json` beside it) and
`pack(root, target)` writes the `.vsix` the editor image installs, with the
standard library only. `manifest_findings(manifest)` says what is wrong with
the manifest's shape, and is empty when nothing is.

**How you use it.** The editor's build calls `identity()` through
`docker/editor/editor_plan.py`, which puts the id in `EXPECTED_EXTENSIONS` and
the file name in `LOCKDOWN_VSIX`; the image's own build stage runs this file:

    python3 lockdown/lockdown.py lockdown "/packed/<publisher>.<name>-<version>.vsix"

⛔ The target's file name is checked against the manifest, so a plan and a
manifest that disagree stop the build instead of installing something the
build's expected list does not name.

**Depends on.** The standard library, and NOTHING else in this component: the
image's pack stage holds `lockdown/` alone, so an import from `docker/` would
work on the host and fail inside the build. `docker/editor/editor_plan.py`
translates this module's `Refused` into the component's own, so a build still
refuses with one class.

## ⛔ Why INSTALLED is not RUNNING
⚠️ An extension can be packed, installed, listed and present in
`extensions.json` and still never activate, as this one once did. The two clauses
`_workbench_findings` adds are what the workbench reads BEFORE it will run
anything, and `Lockdown.banner` is the line the extension prints once it HAS
run, which the image's build greps out of a real session's extension-host log
before it tags the image.

## Why it is PACKAGED and INSTALLED, never copied
⚠️ The workbench reads `extensions.json` in its extensions directory and never
scans it, so an extension folder COPIED in is present, correct, and silently
never loaded. Measured in the extraction source. A `.vsix` is a zip with a
manifest, so no marketplace tool is needed to make one: `extension/` holds the
manifest and the script, `extension.vsixmanifest` names the identity, and
`[Content_Types].xml` types the three extensions used.

## Why the identifier lives in exactly one place
⭐ A consumer pins the documented id. That id is `package.json`'s
`<publisher>.<name>` and NOTHING else derives it by hand: the file name, the
`.vsixmanifest` written into the zip, the image's expected-extension list and
the README's documented id all come from this function, so they cannot drift
apart and a rename moves all of them at once.
"""

from __future__ import annotations

import json
import sys
import zipfile
from dataclasses import dataclass
from pathlib import Path
from xml.sax.saxutils import escape, quoteattr


class Refused(Exception):
    """A manifest or a target this packer will not turn into a `.vsix`."""


HERE = Path(__file__).resolve().parent
MANIFEST = "package.json"
#: ⛔ This framework's publisher, and the namespace of every setting the
#: extension contributes. The extension belongs to NO consumer and names none,
#: so the two are the same word and a manifest that disagrees is a
#: finding rather than a matter of taste.
PUBLISHER = "studyforge"
#: A fixed timestamp for every entry: the same inputs must give the same bytes,
#: or the image layer that installs it changes for no reason.
EPOCH = (1980, 1, 1, 0, 0, 0)
#: What `keybindings.js` writes its derived keybinding removals to, inside the
#: log directory the workbench hands the extension. ⛔ The name is
#: spelled in two languages because the two halves are in two: the extension
#: writes it and `docker/editor/confinement.py` reads it. ⭐ This is the Python
#: side, and `tests/test_lockdown.py` asserts the JavaScript side says the
#: same word — so a rename in either is a failing test, not a gate that
#: quietly reads nothing.
DERIVED_KEYBINDINGS = "keybindings.json"
CONTENT_TYPES = (
    '<?xml version="1.0" encoding="utf-8"?>'
    '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
    '<Default Extension="json" ContentType="application/json"/>'
    '<Default Extension="js" ContentType="application/javascript"/>'
    '<Default Extension="vsixmanifest" ContentType="text/xml"/>'
    "</Types>"
)


@dataclass(frozen=True)
class Lockdown:
    """The extension's identity, read from its manifest and derived nowhere else."""

    id: str
    version: str
    #: The configuration section whose rewrite means "the practice changed".
    section: str
    #: The `main` script, relative to the extension directory.
    main: str

    @property
    def filename(self) -> str:
        return f"{self.id}-{self.version}.vsix"

    @property
    def expected(self) -> str:
        """The `id@version` the image's installed-extension check looks for."""
        return f"{self.id}@{self.version}"

    @property
    def record(self) -> str:
        """The file the extension appends its banner to, inside the log directory the workbench gives it."""
        return f"{self.id.split('.', 1)[1]}.log"

    @property
    def banner(self) -> str:
        """The line the extension prints once it has RUN, which the image's gate greps for.

        ⛔ `expected` above is the INSTALLED fact and this one is the RUNNING
        fact; an extension that was installed and never ran is why both exist. ⭐ `extension.js` builds the same
        string from the same manifest, so neither side spells the id.
        """
        return f"{self.id}: confined"


def read(root: Path = HERE) -> dict:
    return json.loads((Path(root) / MANIFEST).read_text(encoding="utf-8"))


def identity(root: Path = HERE) -> Lockdown:
    """The extension's identity, or `Refused` naming what its manifest gets wrong."""
    manifest = read(root)
    found = manifest_findings(manifest)
    if found:
        raise Refused(f"{MANIFEST} is not a lockdown manifest: {'; '.join(found)}")
    return Lockdown(id=f"{manifest['publisher']}.{manifest['name']}", version=manifest["version"],
                    section=_sections(manifest)[0], main=manifest["main"].removeprefix("./"))


def _properties(manifest: dict) -> dict:
    return manifest.get("contributes", {}).get("configuration", {}).get("properties", {})


def _sections(manifest: dict) -> list[str]:
    """The `<publisher>.<section>` namespace of every contributed key, deduplicated."""
    return sorted({".".join(key.split(".")[:2]) for key in _properties(manifest)})


def manifest_findings(manifest: dict) -> list[str]:
    """What is wrong with the manifest, empty when nothing is.

    ⭐ The rules are POSITIVE and name no source: the publisher is this
    framework's, and every setting the extension contributes sits under it.
    An identifier belonging to a consumer fails both without this file having
    to know that consumer exists.
    """
    found: list[str] = []
    for key in ("name", "publisher", "version", "main", "engines", "activationEvents"):
        if not manifest.get(key):
            found.append(f"'{key}' is missing")
    if manifest.get("publisher") and manifest["publisher"] != PUBLISHER:
        found.append(f"the publisher is '{manifest['publisher']}'; this framework publishes as '{PUBLISHER}'")
    properties = _properties(manifest)
    if not properties:
        found.append("no configuration property is contributed, so the extension declares no section")
    stray = sorted(key for key in properties if not key.startswith(f"{PUBLISHER}."))
    if stray:
        found.append(f"{stray} do not sit under '{PUBLISHER}.'; the extension belongs to no consumer")
    sections = _sections(manifest)
    if len(sections) > 1:
        found.append(f"the contributed keys span sections {sections}; the extension watches exactly one")
    if manifest.get("activationEvents") and manifest["activationEvents"] != ["onStartupFinished"]:
        found.append("the extension activates on 'onStartupFinished' and nothing else")
    found += _workbench_findings(manifest)
    return found


#: The declared extension kind. ⛔ A manifest that declares NONE is not
#: neutral: in a remote or web workbench VS Code INFERS one, and an extension
#: inferred as `ui` has nowhere to run under code-server. The kind is stated so
#: nothing is inferred.
EXTENSION_KIND = ["workspace"]


def _workbench_findings(manifest: dict) -> list[str]:
    """What the WORKBENCH would refuse to run, which a manifest can be perfectly valid and still get wrong.

    ⛔ Both clauses come from that defect, and both were measured on code-server 4.137.0
    (VS Code 1.137.0) against a bind-mounted corpus, which is an UNTRUSTED
    folder and is the normal shape of this product:

    * **Restricted Mode.** A workbench whose folder is untrusted disables every
      extension that does not declare support, and it does so with NO error and
      NO log line. Measured: this extension was installed, listed and never
      activated, while the built-in `onStartupFinished` extensions activated in
      the same sessions. The image also passes
      `--disable-workspace-trust`, and this declaration is the half that
      survives a consumer who replaces the image's command.
    * **The extension kind**, above.

    ⚠️ Neither is a taste: a manifest that fails either installs cleanly,
    lists cleanly, and never runs.
    """
    found: list[str] = []
    supported = manifest.get("capabilities", {}).get("untrustedWorkspaces", {}).get("supported")
    if supported is not True:
        found.append("capabilities.untrustedWorkspaces.supported is not true, so a workbench in Restricted "
                     "Mode disables the extension silently")
    if manifest.get("extensionKind") != EXTENSION_KIND:
        found.append(f"extensionKind is {manifest.get('extensionKind')!r}; under code-server it is "
                     f"{EXTENSION_KIND!r}, and an inferred 'ui' kind has nowhere to run")
    return found


def vsixmanifest(lock: Lockdown, manifest: dict) -> str:
    """The gallery manifest, written from the identity rather than kept beside it."""
    publisher, name = lock.id.split(".", 1)
    return (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<PackageManifest Version="2.0.0" xmlns="http://schemas.microsoft.com/developer/vsx-schema/2011">\n'
        f"  <Metadata>\n"
        f"    <Identity Language=\"en-US\" Id={quoteattr(name)} Version={quoteattr(lock.version)} "
        f"Publisher={quoteattr(publisher)}/>\n"
        f"    <DisplayName>{escape(manifest.get('displayName', name))}</DisplayName>\n"
        f"    <Description xml:space=\"preserve\">{escape(manifest.get('description', ''))}</Description>\n"
        "  </Metadata>\n"
        "  <Installation>\n"
        '    <InstallationTarget Id="Microsoft.VisualStudio.Code"/>\n'
        "  </Installation>\n"
        "  <Dependencies/>\n"
        "  <Assets>\n"
        '    <Asset Type="Microsoft.VisualStudio.Code.Manifest" Path="extension/package.json" Addressable="true"/>\n'
        "  </Assets>\n"
        "</PackageManifest>\n"
    )


def sources(root: Path) -> list[Path]:
    """Everything that goes into `extension/`: the manifest and every script beside it.

    ⛔ Every `.js` in the directory is packed, not just `main`: a helper this
    module required and the packer left out would install cleanly and fail
    the moment it was activated.
    """
    root = Path(root)
    return [root / MANIFEST] + sorted(path for path in root.glob("*.js"))


def pack(root: Path, target: Path) -> Lockdown:
    """Write the `.vsix` for the extension in `root` at `target`, or raise `Refused`."""
    root, target = Path(root), Path(target)
    lock, manifest = identity(root), read(root)
    if target.name != lock.filename:
        raise Refused(f"asked to write '{target.name}'; {MANIFEST} names '{lock.filename}'")
    files = sources(root)
    if root / lock.main not in files:
        raise Refused(f"'{lock.main}' is the manifest's main and is not beside it")
    target.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(target, "w", zipfile.ZIP_DEFLATED) as archive:
        _write(archive, "extension.vsixmanifest", vsixmanifest(lock, manifest).encode("utf-8"))
        _write(archive, "[Content_Types].xml", CONTENT_TYPES.encode("utf-8"))
        for path in files:
            _write(archive, f"extension/{path.name}", path.read_bytes())
    return lock


def _write(archive: zipfile.ZipFile, name: str, data: bytes) -> None:
    info = zipfile.ZipInfo(name, date_time=EPOCH)
    info.compress_type = zipfile.ZIP_DEFLATED
    info.external_attr = 0o644 << 16
    archive.writestr(info, data)


def main(argv: list[str]) -> int:
    if len(argv) != 2:
        print(f"usage: {Path(__file__).name} <extension directory> <target .vsix>", file=sys.stderr)
        return 2
    try:
        lock = pack(Path(argv[0]), Path(argv[1]))
    except Refused as refusal:
        print(f"refused: {refusal}", file=sys.stderr)
        return 2
    print(f"packed {lock.expected} into {Path(argv[1]).name}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
