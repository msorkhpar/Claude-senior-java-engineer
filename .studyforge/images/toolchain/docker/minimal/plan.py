"""The runner image's build plan, read from `pins.json` — pure, no I/O beyond reading files.

**What it does.** Turns a DECLARED SET of runtime names and a platform into the
exact `docker build` arguments and the image tag, refusing anything `pins.json`
does not pin. It also carries the two static checks the component's tests run
over its own Dockerfile and README.

**How you use it.** `load(root)` reads the pins; `plan(pins, names, platform,
digest)` returns a `Plan` or raises `Refused`; `inputs_digest(root)` names the
build's inputs; `dockerfile_findings(text)` and `run_line_findings(text)` return
what is wrong with a Dockerfile or a documented run line, empty when nothing is.

**Depends on.** The standard library only.

## Why the refusals are here and not in the Dockerfile
A Dockerfile cannot say *"this name is not pinned; these are"*. It can only fail
somewhere later with a message about a missing argument. So every refusal a
person should read happens before `docker build` starts, and names what IS
permitted.

## A corpus's practice caches
⛔ A reader's graded run happens in THIS image, under `--network none`, so a
corpus's practice dependencies must already be inside it. `build.py --prime
DIR` hands `prime/prime.py`'s contract — shared with the editor and
unchanged — a consumer's prime directory; `plan()` turns it into one
`WITH_<TOOL>_PRIME` switch per warmer, folds its digest into the tag, and
points each tool at the seed the warmer wrote. Without one, nothing is warmed
and the image is what it was.

## What a refusal echoes
An unpinned name shaped like a runtime id (`NAME`) is NAMED, with the pinned
ones listed: `--runtimes java,cobol` says `cobol`, as the editor's refusal does,
so the two components answer one mistake alike. A name not shaped like an id is
never echoed: the refusal says only what is pinned.
"""

from __future__ import annotations

import hashlib
import json
import re
from dataclasses import dataclass
from pathlib import Path

PINS = "pins.json"
DOCKERFILE = "docker/minimal/Dockerfile"
#: The build's inputs, hashed into the tag: change any of them and the tag moves.
#: ⛔ `prime/` is NOT among them: its warmers run only in a PRIMED build, so
#: `build.py` folds them into that build's digest instead. Putting them
#: here would move every unprimed runner tag when a warmer changed, and the
#: rule that an editor-only change moves no runner tag says it must
#: not; `prime/` is an input of the editor's image as well.
INPUT_ROOTS = (PINS, "docker/minimal")
REPOSITORY = "code-server-toolchain/runner"
#: Where a corpus's warmed practice caches live in the image, and the seed each
#: of `prime/prime.py`'s warmers writes there. ⛔ The keys are that
#: module's `TOOLS`; a test asserts it, so the two cannot drift.
PRIME_ROOT = "/opt/prime"
PRIME_SEEDS = {"gradle": "gradle-home", "maven": "maven-repo"}
#: The runner's HOME, chosen ONCE here and read by the Dockerfile's `ENV HOME`.
RUNNER_HOME = "/tmp"

_SHA256 = re.compile(r"^[0-9a-f]{64}$")
_DIGEST = re.compile(r"^sha256:[0-9a-f]{64}$")
#: The shape of a runtime id: the only kind of arrived name a refusal echoes.
#: The editor's `_NAME` is the same pattern.
NAME = re.compile(r"^[a-z][a-z0-9-]{0,31}$")
_ARCHIVE_STAGES = ("maven", "gradle", "kotlin", "node")
#: What each runtime places under /opt — the Dockerfile's COPY destinations.
#: A runtime not named here places nothing there (it arrives in the base, or
#: from Debian's packages), so the image's /opt is exactly the union for the
#: declared set, and the build refuses one that is not.
OPT_DIRS = {"java": ("java",), "maven": ("maven", "maven-repo"), "gradle": ("gradle",),
            "kotlin": ("kotlinc",), "node": ("node",)}


class Refused(ValueError):
    """A declared set, or a platform, this component will not build."""


@dataclass(frozen=True)
class Plan:
    names: tuple[str, ...]
    arch: str
    platform: str
    tag: str
    build_args: dict[str, str]


def load(root: Path) -> dict:
    return json.loads((Path(root) / PINS).read_text(encoding="utf-8"))


def inputs_digest(root: Path, roots=INPUT_ROOTS) -> str:
    """sha256 over every input file's relative path and bytes, in sorted order: same inputs, same tag.

    ⛔ `__pycache__` is skipped: a digest that moved because a module had been
    imported would give one image two tags.
    """
    root = Path(root)
    files: list[Path] = []
    for entry in roots:
        path = root / entry
        files.extend([path] if path.is_file() else sorted(p for p in path.rglob("*") if p.is_file()))
    digest = hashlib.sha256()
    for path in sorted(files, key=lambda p: p.relative_to(root).as_posix()):
        if "__pycache__" in path.parts:
            continue
        digest.update(path.relative_to(root).as_posix().encode() + b"\0")
        digest.update(path.read_bytes() + b"\0")
    return digest.hexdigest()


def tag_for(names: tuple[str, ...], arch: str, digest: str) -> str:
    """The image tag: the sorted set, the architecture, and the inputs' digest.

    Anybody holding the same `pins.json` and `docker/minimal/` recomputes it,
    which is what lets a report name the toolchain that produced it.
    """
    label = "-".join(names) if names else "none"
    return f"{REPOSITORY}:{label}-{arch}-{digest[:12]}"


def plan(pins: dict, names, platform: str, digest: str, prime=None) -> Plan:
    """Return the build for the declared set on `platform`, or raise `Refused`.

    With a `prime` — a `prime.Prime`, read and guarded by `build.py` — the
    warmers run in this build, its digest moves the tag, and the image holds
    one seed per warmer under `PRIME_ROOT`.
    """
    runtimes = pins["runtimes"]
    platforms = pins["platforms"]
    if platform not in platforms:
        raise Refused(f"no pins are recorded for that platform; pinned platforms are {sorted(platforms)}")
    arch = platforms[platform]
    names = list(names)
    if any(not isinstance(name, str) or not NAME.match(name) for name in names):
        raise Refused(f"a declared name is not a runtime id; the pinned runtimes are {sorted(runtimes)}")
    declared = tuple(sorted(set(names)))
    if len(declared) != len(names):
        raise Refused("a runtime is declared twice; declare each once")
    unknown = [name for name in declared if name not in runtimes]
    if unknown:
        raise Refused(f"{unknown} not pinned in pins.json, so the runner cannot build it; "
                      f"the pinned runtimes are {sorted(runtimes)}")
    for name in declared:
        missing = [need for need in runtimes[name].get("requires", []) if need not in declared]
        if missing:
            raise Refused(f"'{name}' runs on {missing}; declare {missing} as well")
    args = _base_args(pins, declared)
    for name in _ARCHIVE_STAGES:
        args[f"WITH_{name.upper()}"] = "yes" if name in declared else "no"
        if name in declared:
            archive = _archive(runtimes[name], arch, name)
            args[f"{name.upper()}_URL"] = archive["url"]
            args[f"{name.upper()}_SHA256"] = archive["sha256"]
    args["WITH_JAVA"] = "yes" if "java" in declared else "no"
    args["WITH_MAVENREPO"] = args["WITH_MAVEN"]
    args["WITH_PYTHON"] = "yes" if "python" in declared else "no"
    args["WITH_SQLITE"] = "yes" if "sqlite" in declared else "no"
    args["SQLITE_SNAPSHOT"] = runtimes["sqlite"]["snapshot"]
    args["SQLITE_PACKAGES"] = " ".join(f"{k}={v}" for k, v in sorted(runtimes["sqlite"]["packages"].items()))
    args["PYTEST_REQUIREMENTS"] = "\n".join(
        f"{p['name']}=={p['version']} --hash=sha256:{p['sha256']}" for p in runtimes["python"]["packages"]
    )
    args["CHECKS"] = "\n".join(
        f"{name}|{check['command']}|{check['expect'].format(version=runtimes[name]['version'])}"
        for name in declared
        for check in runtimes[name]["checks"]
    )
    args["DECLARED"] = " ".join(declared)
    args["RUNNER_HOME"] = RUNNER_HOME
    args.update(primed(prime))
    if prime is not None:
        digest = hashlib.sha256(f"{digest}\0{prime.digest}".encode()).hexdigest()
    args["OPT_EXPECTED"] = " ".join(sorted(opt_dirs(declared) + ([PRIME_ROOT.rpartition("/")[2]]
                                                                 if prime is not None else [])))
    tag = tag_for(declared, arch, digest)
    # ⛔ The runner stage's first RUN reads this, so its layers are cached per
    # tag and never served to another selection or another set of pins.
    args["CACHE_KEY"] = tag
    return Plan(declared, arch, platform, tag, args)


def opt_dirs(declared) -> list[str]:
    """The directories the declared set places under /opt, sorted as `ls` sorts them."""
    return sorted(d for name in declared for d in OPT_DIRS.get(name, ()))


def primed(prime) -> dict[str, str]:
    """One switch per warmer, and where each tool then looks for its cache.

    ⛔ Unprimed, every value is what the image already had: no seed under
    `/opt`, Gradle's own default under `HOME`, and no Maven argument at all. So
    a corpus that declares no practice dependency is untouched, and that is
    asserted as well as the primed case.

    ⭐ There is no cache key of its own here, unlike the editor's `PRIME_KEY`:
    `CACHE_KEY` is this image's TAG, the tag folds the prime's digest, and the
    runner stage's first RUN reads it — so every layer the warmers write
    is already cached per prime.
    """
    tools = prime.tools if prime is not None else ()
    args = {f"WITH_{tool.upper()}_PRIME": ("yes" if tool in tools else "no") for tool in sorted(PRIME_SEEDS)}
    args["PRIME_GRADLE_HOME"] = (f"{PRIME_ROOT}/{PRIME_SEEDS['gradle']}" if "gradle" in tools
                                 else f"{RUNNER_HOME}/.gradle")
    args["PRIME_MAVEN_ARGS"] = (f"-Dmaven.repo.local={PRIME_ROOT}/{PRIME_SEEDS['maven']}"
                                if "maven" in tools else "")
    return args


def _image(entry: dict) -> str:
    return f"{entry['image']}@{entry['digest']}"


def _base_args(pins: dict, declared: tuple[str, ...]) -> dict[str, str]:
    runtimes = pins["runtimes"]
    python = _image(runtimes["python"])
    return {
        # ⭐ Python arrives AS the base: the official image is Debian trixie-slim
        # plus Python, so a set declaring python gets exactly that, and a set
        # that does not gets the plain base and no Python at all.
        "RUNNER_BASE": python if "python" in declared else _image(pins["base"]),
        # The unpacking stages use the Python image as a build tool only; it
        # reaches the runner image only when python is declared.
        "UNPACK_IMAGE": python,
        "JAVA_IMAGE": _image(runtimes["java"]),
    }


def _archive(entry: dict, arch: str, name: str) -> dict:
    archives = entry["archives"]
    if "any" in archives:
        return archives["any"]
    if arch not in archives:
        raise Refused(f"'{name}' has no archive pinned for {arch}; pinned: {sorted(archives)}")
    return archives[arch]


def pins_findings(pins: dict) -> list[str]:
    """What is wrong with a pins document's shape, empty when nothing is."""
    found: list[str] = []
    for label, entry in [("base", pins["base"])] + sorted(pins["runtimes"].items()):
        if "digest" in entry and not _DIGEST.match(entry["digest"]):
            found.append(f"{label}: an image is pinned by a sha256 digest")
        for arch, archive in entry.get("archives", {}).items():
            if not _SHA256.match(archive.get("sha256", "")):
                found.append(f"{label} ({arch}): an archive is pinned by a recorded sha256")
        if not entry.get("sources") or not all(s.get("host") and s.get("taken") for s in entry["sources"]):
            found.append(f"{label}: every pin records its source host and when it was taken")
        if not isinstance(entry.get("single_source"), bool):
            found.append(f"{label}: every pin says whether it is single-source")
        if entry.get("single_source") is True and not entry.get("why_single"):
            found.append(f"{label}: a single-source pin says why no second source exists")
    for name, entry in pins["runtimes"].items():
        if not entry.get("checks"):
            found.append(f"{name}: every runtime is checked for its version at build time")
    return found


_ARG = re.compile(r"^\s*ARG\s+(?P<body>.+)$")
_FROM = re.compile(r"^\s*FROM\s+(?:--platform=\S+\s+)?(?P<ref>\S+)(?:\s+AS\s+(?P<name>\S+))?\s*$", re.I)


def dockerfile_findings(text: str) -> list[str]:
    """No ARG carries a default, every FROM is a stage, `scratch` or an ARG, and
    no `scratch` stage is bare.

    ⛔ An `ARG X=value` is a second place a version could be chosen, and a
    literal `FROM image:tag` is an input no pin governs. ⛔ A bare `FROM scratch`
    stage, copied from, is served from the cache of whatever other stage the
    same COPY last read on the same base: the runtime a set did NOT
    declare arrives anyway. ⛔ A stage that copies from a SELECTED stage
    (`FROM java-${WITH_JAVA} AS java`) must first run a RUN that reads
    `${CACHE_KEY}`: without it, a warm cache served a five-runtime build
    the `python`-only build's layers.
    """
    found: list[str] = []
    stages: set[str] = set()
    bare: int | None = None
    found += keyed_copy_findings(text)
    for number, line in enumerate(text.splitlines(), start=1):
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        if bare is not None and not _FROM.match(line):
            bare = None
        elif bare is not None:
            found.append(f"line {bare}: a scratch stage is bare; give it content (a `-no` stage takes `WORKDIR /opt`)")
            bare = None
        arg = _ARG.match(line)
        if arg and "=" in arg.group("body"):
            found.append(f"line {number}: an ARG carries a default; every value comes from pins.json")
        match = _FROM.match(line)
        if match:
            ref = match.group("ref")
            ok = ref == "scratch" or ref.startswith("${") or ref in stages or _stage_template(ref, stages)
            if not ok:
                found.append(f"line {number}: FROM names an image directly; name it by an ARG from pins.json")
            if match.group("name"):
                stages.add(match.group("name"))
            if ref == "scratch":
                bare = number
    if bare is not None:
        found.append(f"line {bare}: a scratch stage is bare; give it content (a `-no` stage takes `WORKDIR /opt`)")
    return found


def keyed_copy_findings(text: str) -> list[str]:
    """Every `COPY --from=<a selected stage>` sits above a RUN that reads `${CACHE_KEY}` in its own stage."""
    found: list[str] = []
    stages: set[str] = set()
    selected: set[str] = set()
    keyed = False
    for number, line in enumerate(text.splitlines(), start=1):
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        match = _FROM.match(line)
        if match:
            keyed = False
            if _stage_template(match.group("ref"), stages) and match.group("name"):
                selected.add(match.group("name"))
            if match.group("name"):
                stages.add(match.group("name"))
            continue
        if re.match(r"^\s*RUN\b", line) and "${CACHE_KEY}" in line:
            keyed = True
        copied = re.match(r"^\s*COPY\s+--from=(\S+)", line)
        if copied and copied.group(1) in selected and not keyed:
            found.append(f"line {number}: COPY --from={copied.group(1)} copies a selected stage before a RUN "
                         "reads ${CACHE_KEY}; a warm cache can serve it another selection's layer")
    return found


def _stage_template(ref: str, stages: set[str]) -> bool:
    """`java-${WITH_JAVA}` selects a stage declared earlier."""
    prefix, _, rest = ref.partition("${")
    return bool(rest) and any(stage.startswith(prefix) for stage in stages)


def run_line_findings(text: str) -> list[str]:
    """The documented `docker run` line: no socket, no network, no port."""
    lines = [line for line in _joined(text) if line.lstrip().startswith("docker run")]
    if not lines:
        return ["no documented `docker run` line"]
    found = []
    for line in lines:
        if "docker.sock" in line:
            found.append("the run line mounts the Docker socket")
        if "--network none" not in line:
            found.append("the run line does not say --network none")
        if re.search(r"(^|\s)(-p|--publish)(\s|=)", line):
            found.append("the run line publishes a port")
    return found


def _joined(text: str) -> list[str]:
    """Lines with shell continuations joined, so a wrapped command reads as one."""
    joined, current = [], ""
    for line in text.splitlines():
        if line.rstrip().endswith("\\"):
            current += line.rstrip()[:-1] + " "
            continue
        joined.append(current + line)
        current = ""
    return joined
