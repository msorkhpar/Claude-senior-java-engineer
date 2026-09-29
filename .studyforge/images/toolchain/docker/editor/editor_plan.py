"""The editor image's build plan — pure, no I/O beyond reading files.

**What it does.** Turns a DECLARED SET of runtimes, a platform and the
runner's own plan for that set into the exact `docker build` arguments and the
editor's tag, refusing before Docker starts anything the set or
`editor-pins.json` does not allow. It also carries the editor's own static
checks.

**How you use it.** `load(root)` reads `editor-pins.json`; `selection(pins,
names)` checks a declared set before the runner is planned; `plan(pins,
editor_pins, runner, digest)` returns an `EditorPlan` or raises `Refused` (the
runner's own refusal class); `inputs_digest(root)` names the build's inputs;
`pins_findings` and `install_findings` return what is wrong, empty when nothing
is.

**Depends on.** The standard library, and `docker/minimal/plan.py`, whose
functions are reused unchanged.

## Why the runner's plan is an input
⭐ The editor chooses NO runtime version: it
copies every runtime out of the runner image built for the same set, so
`pins.json` stays the one place a runtime version is chosen. The runner's
checks, its `/opt` expectation and its tag are therefore read from the
runner's `Plan`, never re-derived here.

## The selection
A consumer declares a subset of `pins.json`'s runtimes; `DEFAULT_SET` is the
image's first, five-runtime set. Everything the image carries is a function of that set:
the runner it copies, the extensions (each names the runtime it serves in
`for`), TypeScript and readline, `PATH`, `JAVA_HOME` and the seed's
per-runtime settings blocks. ⛔ A runtime that is not selected leaves no trace:
no tree, no `PATH` entry, no `JAVA_HOME`, no extension and no setting.

## The prime
A consumer's prime directory is read by `prime/prime.py` and handed to
`plan()`, which guards it against `pins.json` (never a pin of its own) and
turns it into one `WITH_<TOOL>_PRIME` argument per warmer. Its digest is
folded into the tag, so an image warmed for one prime never carries
another's name.

## The lockdown
⭐ The workbench lockdown extension is this repository's own artifact, not a
consumer's choice: every declared set installs it. So it is NOT an
`editor-pins.json` entry with a `for` — `lockdown/lockdown.py` reads its
identity from the one manifest that carries it, `plan()` puts the file name in
`LOCKDOWN_VSIX` and the id in `EXPECTED_EXTENSIONS`, and the image's own
installed-list check fails the build naming the id when it is missing.
⚠️ That check proves INSTALLATION and nothing more; `build.py` proves
the extension RUNS, in a real session, before it tags the image.
"""

from __future__ import annotations

import hashlib
import json
import re
import sys
from dataclasses import dataclass
from pathlib import Path

COMPONENT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "minimal"))
sys.path.insert(0, str(COMPONENT / "prime"))
sys.path.insert(0, str(COMPONENT / "lockdown"))
import lockdown as lockdown_extension  # noqa: E402
import plan as runner_plan  # noqa: E402
import prime as prime_contract  # noqa: E402

Refused = runner_plan.Refused
EDITOR_PINS = "editor-pins.json"
DOCKERFILE = "docker/editor/Dockerfile"
#: Where the workbench lockdown extension lives, relative to the component.
LOCKDOWN = "lockdown"
#: The editor's own inputs. The runner's digest is folded in as well, so the
#: editor's tag moves whenever the runner's does — and `docker/minimal/` is
#: not touched, so no runner tag moves because the editor exists.
OWN_INPUTS = (EDITOR_PINS, "docker/editor", "prime", LOCKDOWN)
REPOSITORY = "code-server-toolchain/editor"
#: The set built when none is declared: the extraction source's five, which is
#: the image's first set.
DEFAULT_SET = ("gradle", "java", "kotlin", "node", "python")
#: What the editor needs for each declared runtime. Removing one of these from
#: editor-pins.json, or pinning it for another runtime, is refused before
#: Docker starts.
REQUIRED_EXTENSIONS = {
    "gradle": ("vscjava.vscode-gradle",),
    "java": ("redhat.java", "vscjava.vscode-java-debug", "vscjava.vscode-java-test"),
    "kotlin": ("fwcd.kotlin",),
    "python": ("ms-python.debugpy", "ms-python.python"),
}
#: Pinned for the runner, but the editor cannot carry it: it copies only /opt
#: and /usr/local out of the runner, and this runtime lives in neither.
NOT_CARRIED = {
    "sqlite": "the runner installs it from Debian's packages into /usr/bin, and the editor "
              "copies only /opt and /usr/local out of the runner",
}
#: Where each runtime's commands live, in PATH order. A runtime not named here
#: needs no entry (python is in /usr/local/bin, shell in the base).
PATH_DIRS = (("java", "/opt/java/openjdk/bin"), ("maven", "/opt/maven/bin"), ("gradle", "/opt/gradle/bin"),
             ("kotlin", "/opt/kotlinc/bin"), ("node", "/opt/node/bin"))
JAVA_HOME = "/opt/java/openjdk"
#: The base image's own PATH, which every set keeps after its toolchains.
BASE_PATH = "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
#: The runtimes whose settings the seed carries in a `// @runtime <name>` block;
#: the build drops each block whose runtime is not declared.
SEED_RUNTIMES = ("java", "python")
_NAME = re.compile(r"^[a-z][a-z0-9-]{0,31}$")
#: The Open VSX target platform for each architecture pins.json names.
TARGETS = {"amd64": "linux-x64", "arm64": "linux-arm64"}

_SHA256 = re.compile(r"^[0-9a-f]{64}$")
_DIGEST = re.compile(r"^sha256:[0-9a-f]{64}$")


@dataclass(frozen=True)
class EditorPlan:
    names: tuple[str, ...]
    arch: str
    platform: str
    tag: str
    build_args: dict[str, str]


def load(root: Path) -> dict:
    return json.loads((Path(root) / EDITOR_PINS).read_text(encoding="utf-8"))


def inputs_digest(root: Path) -> str:
    """sha256 over the runner's inputs digest, then every editor input file: one tag per input set."""
    root = Path(root)
    digest = hashlib.sha256(b"runner\0" + runner_plan.inputs_digest(root).encode() + b"\0")
    files: list[Path] = []
    for entry in OWN_INPUTS:
        path = root / entry
        files.extend([path] if path.is_file() else sorted(p for p in path.rglob("*") if p.is_file()))
    for path in sorted(files, key=lambda p: p.relative_to(root).as_posix()):
        if "__pycache__" in path.parts:
            continue
        digest.update(path.relative_to(root).as_posix().encode() + b"\0")
        digest.update(path.read_bytes() + b"\0")
    return digest.hexdigest()


def selection(pins: dict, names) -> list[str]:
    """The declared set, checked before the runner is planned, or `Refused`.

    ⭐ An unpinned toolchain is NAMED, as the runner's refusal names it:
    only a name shaped like a runtime id is ever echoed, and a
    malformed one is answered with what is pinned, by both components.
    """
    names = list(names)
    pinned = sorted(pins["runtimes"])
    if any(not _NAME.match(name) for name in names):
        raise Refused(f"a declared name is not a runtime id; the pinned runtimes are {pinned}")
    unpinned = sorted({name for name in names if name not in pins["runtimes"]})
    if unpinned:
        raise Refused(f"{unpinned} not pinned in pins.json, so the editor cannot select it; "
                      f"the pinned runtimes are {pinned}")
    _carried(names)
    return names


def _carried(names) -> None:
    blocked = sorted(set(names) & set(NOT_CARRIED))
    if blocked:
        reasons = "; ".join(f"'{name}': {NOT_CARRIED[name]}" for name in blocked)
        raise Refused(f"{blocked} pinned for the runner, but the editor cannot carry it: {reasons}")


def extensions_for(pins: dict, editor_pins: dict, declared) -> dict:
    """The pinned extensions serving the declared set, or `Refused` naming what is missing."""
    pinned = editor_pins["extensions"]
    stray = sorted(ext for ext, entry in pinned.items() if entry.get("for") not in pins["runtimes"])
    if stray:
        raise Refused(f"{stray} name no pinned runtime in 'for'; the pinned runtimes are {sorted(pins['runtimes'])}")
    chosen = {ext: entry for ext, entry in pinned.items() if entry["for"] in declared}
    for name in declared:
        missing = [ext for ext in REQUIRED_EXTENSIONS.get(name, ()) if ext not in chosen]
        if missing:
            raise Refused(f"the editor requires extension(s) {missing} for '{name}', "
                          f"which editor-pins.json does not pin for it")
    for ext, entry in sorted(chosen.items()):
        unpinned = [d for d in entry["depends"] if d not in chosen]
        unpinned += [p for p in entry["pack"] if p not in chosen and p not in entry.get("pack_absent", {})]
        if unpinned:
            raise Refused(f"'{ext}' needs {unpinned}; pin each for '{entry['for']}', "
                          f"or record why a pack member is absent")
    return chosen


def lockdown_identity(root: Path = COMPONENT) -> lockdown_extension.Lockdown:
    """This repository's lockdown extension, with its own refusal translated into ours.

    ⭐ `lockdown/` imports nothing from this component — the image packs it in
    a stage that holds that directory alone — so its refusal class is its own,
    and this is the one place the two meet.
    """
    try:
        return lockdown_extension.identity(Path(root) / LOCKDOWN)
    except lockdown_extension.Refused as refusal:
        raise Refused(f"the lockdown extension: {refusal}") from refusal


def plan(pins: dict, editor_pins: dict, runner: runner_plan.Plan, digest: str,
         prime: prime_contract.Prime | None = None,
         lock: lockdown_extension.Lockdown | None = None) -> EditorPlan:
    """Return the editor build for the runner's set and platform, or raise `Refused`.

    With a `prime`, it is guarded against `pins` first, and its digest moves the tag.
    `lock` defaults to this repository's own lockdown extension, which every set installs.
    """
    lock = lock or lockdown_identity()
    declared, arch = runner.names, runner.arch
    _carried(declared)
    if prime is not None:
        prime_contract.guard(prime, pins, declared)
        digest = hashlib.sha256(f"{digest}\0{prime.digest}".encode()).hexdigest()
    extensions = extensions_for(pins, editor_pins, declared)
    fetch = [f"{ext}-{entry['version']}.vsix|{_file(ext, entry, arch)['url']}|{_file(ext, entry, arch)['sha256']}"
             for ext, entry in sorted(extensions.items())]
    typescript = editor_pins["typescript"]
    with_typescript = typescript["for"] in declared
    if with_typescript:
        fetch.append(f"typescript.tgz|{typescript['url']}|{typescript['sha256']}")
    readline = editor_pins["readline"]
    face = editor_pins["face"]
    checks = runner.build_args["CHECKS"].splitlines()
    if with_typescript:
        checks += [f"typescript|{c['command']}|{c['expect'].format(version=typescript['version'])}"
                   for c in typescript["checks"]]
    args = {
        "RUNNER_IMAGE": runner.tag,
        "EDITOR_BASE": f"{editor_pins['base']['image']}@{editor_pins['base']['digest']}",
        # A build tool only: the pinned Python image runs fetch.py. It reaches
        # the editor only through the runner, when python is declared.
        "FETCH_IMAGE": runner.build_args["UNPACK_IMAGE"],
        "FETCH": "\n".join(fetch),
        "EXPECTED_EXTENSIONS": " ".join(sorted([f"{ext}@{extensions[ext]['version']}" for ext in extensions]
                                               + [lock.expected])),
        "LOCKDOWN_VSIX": lock.filename,
        "WITH_TYPESCRIPT": "yes" if with_typescript else "no",
        "WITH_READLINE": "yes" if readline["for"] in declared else "no",
        "READLINE_SNAPSHOT": readline["snapshot"],
        "READLINE_PACKAGES": " ".join(f"{k}={v}" for k, v in sorted(readline["packages"].items())),
        # ⭐ The page's code face, for every set: it serves no runtime.
        "FACE_URL": face["url"],
        "FACE_SHA256": face["sha256"],
        "FACE_FILES": "\n".join(f"{member}|{entry['weight']}|{entry['sha256']}"
                                for member, entry in face["files"].items()),
        "CHECKS": "\n".join(checks),
        "OPT_EXPECTED": " ".join(sorted(runner_plan.opt_dirs(declared) + ["code-server"])),
        "JAVA_RUNTIME": java_runtime(pins),
        "DECLARED": " ".join(declared),
        **environment(declared),
        **primed(prime),
    }
    tag = runner_plan.tag_for(declared, arch, digest).replace(runner_plan.REPOSITORY, REPOSITORY, 1)
    return EditorPlan(declared, arch, runner.platform, tag, args)


def primed(prime: prime_contract.Prime | None) -> dict[str, str]:
    """One switch per warmer, and the key that caches the warm per prime."""
    tools = prime.tools if prime else ()
    args = {f"WITH_{tool.upper()}_PRIME": "yes" if tool in tools else "no" for tool in prime_contract.TOOLS}
    args["PRIME_KEY"] = prime.digest if prime else "none"
    return args


def environment(declared) -> dict[str, str]:
    """`PATH`, `JAVA_HOME`, profile.d and the seed, naming ONLY declared runtimes."""
    dirs = [path for name, path in PATH_DIRS if name in declared]
    profile = [f"export JAVA_HOME={JAVA_HOME}"] if "java" in declared else []
    profile += [f"export PATH={':'.join(dirs)}:$PATH"] if dirs else []
    return {
        "WITH_JAVA": "yes" if "java" in declared else "no",
        "JAVA_HOME_DIR": JAVA_HOME,
        "EDITOR_PATH": ":".join(dirs + [BASE_PATH]),
        "PROFILE_D": "\n".join(profile or ["# the declared set places no toolchain under /opt"]),
        "SEED_DROP": " ".join(name for name in SEED_RUNTIMES if name not in declared),
    }


def java_runtime(pins: dict) -> str:
    """The execution-environment name the Java extension expects, from the pinned JDK.

    ⛔ Never written by hand: the extraction source's seed said `JavaSE-26`
    against a JDK pinned elsewhere, and the two disagreed.
    """
    return f"JavaSE-{pins['runtimes']['java']['version'].split('.')[0]}"


def _file(ext: str, entry: dict, arch: str) -> dict:
    files = entry["files"]
    target = TARGETS.get(arch)
    if target in files:
        return files[target]
    if "universal" in files:
        return files["universal"]
    raise Refused(f"'{ext}' has no file pinned for {target}; pinned: {sorted(files)}")


def pins_findings(editor_pins: dict) -> list[str]:
    """What is wrong with the editor pins' shape, empty when nothing is."""
    found: list[str] = []
    entries = [("base", editor_pins["base"]), ("typescript", editor_pins["typescript"]),
               ("readline", editor_pins["readline"]), ("face", editor_pins["face"])] \
        + sorted(editor_pins["extensions"].items())
    if not _DIGEST.match(editor_pins["base"].get("digest", "")):
        found.append("base: the image is pinned by a sha256 index digest")
    if not _SHA256.match(editor_pins["typescript"].get("sha256", "")):
        found.append("typescript: the tarball is pinned by a recorded sha256")
    if not editor_pins["typescript"].get("integrity", "").startswith("sha512-"):
        found.append("typescript: the registry's published integrity is recorded")
    for arch, debs in editor_pins["readline"]["debs"].items():
        if set(debs) != set(editor_pins["readline"]["packages"]) or not all(_SHA256.match(v) for v in debs.values()):
            found.append(f"readline ({arch}): every package has a recorded sha256")
    face = editor_pins["face"]
    if not _SHA256.match(face.get("sha256", "")) or not face.get("files") \
            or not all(_SHA256.match(f.get("sha256", "")) for f in face["files"].values()):
        found.append("face: the archive and every file taken out of it are pinned by a recorded sha256")
    if not any(f.get("weight") == "licence" for f in face.get("files", {}).values()):
        found.append("face: the licence ships beside the faces")
    for ext, entry in sorted(editor_pins["extensions"].items()):
        if not isinstance(entry.get("for"), str) or not entry["for"]:
            found.append(f"{ext}: every extension names the runtime it serves in 'for'")
        for target, file in entry["files"].items():
            if not _SHA256.match(file.get("sha256", "")):
                found.append(f"{ext} ({target}): a .vsix is pinned by a recorded sha256")
            if f"/{entry['version']}/" not in file.get("url", ""):
                found.append(f"{ext} ({target}): the url names the pinned version")
    for label, entry in entries:
        if not entry.get("sources") or not all(s.get("host") and s.get("taken") for s in entry["sources"]):
            found.append(f"{label}: every pin records its source host and when it was taken")
        if not isinstance(entry.get("single_source"), bool):
            found.append(f"{label}: every pin says whether it is single-source")
        if entry.get("single_source") is True and not entry.get("why_single"):
            found.append(f"{label}: a single-source pin says why no second source exists")
    return found


_INSTALL = re.compile(r"--install-extension[=\s]+(?P<arg>\S+)")


def install_findings(text: str) -> list[str]:
    """Every `--install-extension` names a FILE, never a bare marketplace id."""
    found = []
    for number, line in enumerate(text.splitlines(), start=1):
        if line.lstrip().startswith("#"):
            continue
        for match in _INSTALL.finditer(line):
            if not match.group("arg").strip("\"'").startswith(("/", "$")):
                found.append(f"line {number}: an extension is installed by id; install the pinned file")
    return found
