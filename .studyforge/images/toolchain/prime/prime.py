"""The prime contract: read a consumer's prime directory, and guard it against the pins.

**What it does.** The build-time warm cache is the per-consumer part of an
image: it is built from THAT consumer's build files, so a reader's
first offline build needs no download. `read(directory)` checks a prime
directory's shape and reads the versions its build files name, refusing
(naming why) a shape the warmers cannot use. `guard(prime, pins, declared)`
refuses a prime whose versions disagree with the image's pins, or whose
build tool the declared set does not hold. `docker/editor/editor_plan.py`
calls `guard` from its `plan()` and `docker/minimal/build.py` from its
`planned()`, so a refusal comes before Docker starts.

**How you use it.** Through `docker/editor/build.py --prime DIR` (a
consumer's caches in the editor) and `docker/minimal/build.py --prime
DIR` (a corpus's practice dependencies in the runner, where a graded run
happens with no network). The contract, in full:

- ``DIR/gradle/`` — a Gradle build (``settings.gradle`` or
  ``settings.gradle.kts``) with ``gradle/verification-metadata.xml`` recording
  a sha256 for every file it fetches. Warmed when ``gradle`` is declared.
- ``DIR/maven/`` — a Maven build (``pom.xml``; a parent POM copied verbatim
  and reached by ``relativePath``). Warmed with Maven Central's own checksums
  enforced, when ``maven`` is declared.
- Nothing else at the top but plain files (a README). At least one of the two.

Either build mounts DIR read-only as the named context ``consumer-prime``,
runs ``prime/warm-gradle.sh`` and ``prime/warm-maven.sh`` with the network,
then again with NO network from a copy of what they warmed. What each image
then guarantees is in the README's section for it. ⛔ The warmers take a
PROJECT and a DESTINATION and know nothing about either image, which is what
lets the two share them unchanged.

**Depends on.** The standard library, and `docker/minimal/plan.py` for the
runner's `Refused`.

## Why a version disagreement is refused
⛔ A cache warmed for another version is worse than no cache: it misses in
ways nobody looks for. So a wrapper that names another Gradle or Maven, a
Kotlin plugin other than the pinned Kotlin, a Gradle toolchain other than
the pinned JDK (an offline build cannot provision one), or a Maven release
the pinned JDK cannot compile, is refused naming the file and both versions.
"""

from __future__ import annotations

import hashlib
import re
import sys
import xml.etree.ElementTree as ElementTree
from dataclasses import dataclass, field
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "docker" / "minimal"))
from plan import Refused  # noqa: E402

#: Each project directory a prime may carry, and the declared runtime that builds it.
TOOLS = {"gradle": "gradle", "maven": "maven"}
GRADLE_SETTINGS = ("settings.gradle", "settings.gradle.kts")
GRADLE_VERIFICATION = "gradle/verification-metadata.xml"
GRADLE_WRAPPER = "gradle/wrapper/gradle-wrapper.properties"
MAVEN_WRAPPER = ".mvn/wrapper/maven-wrapper.properties"

_KOTLIN = (re.compile(r'kotlin\(\s*"[\w.-]+"\s*\)\s*version\s*"([^"]+)"'),
           re.compile(r'id\s*\(?\s*["\']org\.jetbrains\.kotlin\.[\w.-]+["\']\s*\)?\s*version\s*\(?\s*["\']([^"\']+)["\']'))
_TOML_KOTLIN = re.compile(r'^\s*kotlin\s*=\s*"([^"]+)"', re.M)
_TOOLCHAIN = (re.compile(r"JavaLanguageVersion\.of\(\s*(\d+)\s*\)"), re.compile(r"jvmToolchain\(\s*(\d+)\s*\)"))
_GRADLE_DIST = re.compile(r"gradle-(?P<version>[0-9][^/]*?)-(?P<kind>bin|all)\.zip$")
_MAVEN_DIST = re.compile(r"apache-maven-(?P<version>[0-9][^/]*?)-bin\.(?:zip|tar\.gz)$")
_POM = "{http://maven.apache.org/POM/4.0.0}"
_RELEASE_PROPERTIES = ("maven.compiler.release", "maven.compiler.source", "maven.compiler.target")
_PROPERTY = re.compile(r"^\$\{([^}]+)\}$")


@dataclass(frozen=True)
class Prime:
    """What a prime directory carries: its projects, the versions they name, and its digest."""

    tools: tuple[str, ...]
    digest: str
    #: (file, version) pairs, each file relative to the prime directory.
    gradle_wrapper: tuple[str, str, str] | None = None  # (file, version, sha256 or "")
    maven_wrapper: tuple[str, str] | None = None
    kotlin_plugins: tuple[tuple[str, str], ...] = field(default=())
    toolchains: tuple[tuple[str, int], ...] = field(default=())
    maven_releases: tuple[tuple[str, int], ...] = field(default=())


def read(directory) -> Prime:
    """The prime at `directory`, or `Refused` naming why its shape cannot be warmed."""
    root = Path(directory)
    if not root.is_dir():
        raise Refused("the prime is not a directory")
    entries = sorted(p.name for p in root.iterdir() if p.is_dir())
    stray = [name for name in entries if name not in TOOLS]
    if stray:
        raise Refused(f"the prime carries {stray}; a prime holds only {sorted(TOOLS)} project directories")
    tools = tuple(name for name in sorted(TOOLS) if name in entries)
    if not tools:
        raise Refused(f"the prime holds no project: it primes nothing; add one of {sorted(TOOLS)}")
    facts: dict = {}
    if "gradle" in tools:
        facts.update(_gradle(root))
    if "maven" in tools:
        facts.update(_maven(root))
    return Prime(tools=tools, digest=digest(root), **facts)


def guard(prime: Prime, pins: dict, declared) -> None:
    """Refuse a prime the declared set cannot build, or whose versions disagree with the pins."""
    runtimes = pins["runtimes"]
    undeclared = [tool for tool in prime.tools if TOOLS[tool] not in declared]
    if undeclared:
        raise Refused(f"the prime carries {undeclared} project(s), and the declared set does not hold "
                      f"{[TOOLS[t] for t in undeclared]}: nothing could warm them; declare it or remove the project")
    problems = []
    gradle, maven, kotlin = (runtimes[name]["version"] for name in ("gradle", "maven", "kotlin"))
    jdk = int(runtimes["java"]["version"].split(".")[0].split("+")[0])
    if prime.gradle_wrapper:
        file, version, sha256 = prime.gradle_wrapper
        if version != gradle:
            problems.append(f"{file} names Gradle {version}, and the image's Gradle is {gradle}")
        pinned = runtimes["gradle"]["archives"]["any"]["sha256"]
        if sha256 != pinned:
            problems.append(f"{file} does not pin distributionSha256Sum={pinned}, the pinned Gradle's sha256, "
                            f"so its wrapper would fetch an unpinned file")
    if prime.maven_wrapper and prime.maven_wrapper[1] != maven:
        problems.append(f"{prime.maven_wrapper[0]} names Maven {prime.maven_wrapper[1]}, "
                        f"and the image's Maven is {maven}")
    problems += [f"{file} names the Kotlin plugin {version}, and the image's Kotlin is {kotlin}"
                 for file, version in prime.kotlin_plugins if version != kotlin]
    problems += [f"{file} asks for a Java {number} toolchain, and the image's JDK is {jdk}; "
                 f"an offline build cannot provision another"
                 for file, number in prime.toolchains if number != jdk]
    problems += [f"{file} compiles for Java {number}, which the image's JDK {jdk} cannot"
                 for file, number in prime.maven_releases if number > jdk]
    if problems:
        raise Refused("the prime disagrees with pins.json, and a cache warmed for another version misses "
                      "in ways nobody looks for: " + "; ".join(problems))


def digest(root: Path) -> str:
    """sha256 over every file's relative path and bytes, in sorted order: the prime is a build input."""
    root = Path(root)
    hashed = hashlib.sha256(b"prime\0")
    for path in sorted((p for p in root.rglob("*") if p.is_file()), key=lambda p: p.relative_to(root).as_posix()):
        hashed.update(path.relative_to(root).as_posix().encode() + b"\0" + path.read_bytes() + b"\0")
    return hashed.hexdigest()


def _gradle(root: Path) -> dict:
    project = root / "gradle"
    if not any((project / name).is_file() for name in GRADLE_SETTINGS):
        raise Refused(f"the prime's gradle/ has no {' or '.join(GRADLE_SETTINGS)}, so it is not a Gradle build")
    _verification(project)
    facts: dict = {}
    wrapper = project / GRADLE_WRAPPER
    if wrapper.is_file():
        properties = _properties(wrapper)
        match = _GRADLE_DIST.search(properties.get("distributionUrl", "").replace("\\:", ":"))
        if not match:
            raise Refused(f"gradle/{GRADLE_WRAPPER} names no Gradle distribution the guard can read")
        facts["gradle_wrapper"] = (f"gradle/{GRADLE_WRAPPER}", match["version"],
                                   properties.get("distributionSha256Sum", ""))
    kotlin, toolchains = [], []
    for path in sorted(project.rglob("*")):
        if not path.is_file() or not path.name.endswith((".gradle", ".gradle.kts", ".versions.toml")):
            continue
        text = path.read_text(encoding="utf-8")
        name = path.relative_to(root).as_posix()
        found = [m for pattern in _KOTLIN for m in pattern.findall(text)]
        found += _TOML_KOTLIN.findall(text) if name.endswith(".toml") else []
        kotlin += [(name, version) for version in found]
        toolchains += [(name, int(number)) for pattern in _TOOLCHAIN for number in pattern.findall(text)]
    facts["kotlin_plugins"], facts["toolchains"] = tuple(kotlin), tuple(toolchains)
    return facts


def _verification(project: Path) -> None:
    """Every file Gradle fetches must be pinned by a recorded sha256, or the build is not repeatable."""
    path = project / GRADLE_VERIFICATION
    why = "so the files its warm fetches would be pinned by nothing"
    if not path.is_file():
        raise Refused(f"the prime's gradle/ carries no {GRADLE_VERIFICATION}, {why}")
    try:
        tree = ElementTree.parse(path)
    except ElementTree.ParseError as error:
        raise Refused(f"gradle/{GRADLE_VERIFICATION} is not well-formed XML ({error})") from None
    local = {element.tag.rsplit("}", 1)[-1]: element for element in tree.iter()}
    verify = local.get("verify-metadata")
    if verify is None or (verify.text or "").strip() != "true":
        raise Refused(f"gradle/{GRADLE_VERIFICATION} does not set verify-metadata to true, {why}")
    artifacts = [e for e in tree.iter() if e.tag.rsplit("}", 1)[-1] == "artifact"]
    unpinned = sorted(a.get("name", "?") for a in artifacts
                      if not any(c.tag.rsplit("}", 1)[-1] == "sha256" for c in a))
    if not artifacts or unpinned:
        raise Refused(f"gradle/{GRADLE_VERIFICATION} records no sha256 for {unpinned or 'any file'}, {why}")


def _maven(root: Path) -> dict:
    project = root / "maven"
    if not (project / "pom.xml").is_file():
        raise Refused("the prime's maven/ has no pom.xml, so it is not a Maven build")
    facts: dict = {}
    wrapper = project / MAVEN_WRAPPER
    if wrapper.is_file():
        match = _MAVEN_DIST.search(_properties(wrapper).get("distributionUrl", "").replace("\\:", ":"))
        if not match:
            raise Refused(f"maven/{MAVEN_WRAPPER} names no Maven distribution the guard can read")
        facts["maven_wrapper"] = (f"maven/{MAVEN_WRAPPER}", match["version"])
    poms = []
    for path in sorted(project.rglob("pom.xml")):
        try:
            poms.append((path.relative_to(root).as_posix(), ElementTree.parse(path).getroot()))
        except ElementTree.ParseError as error:
            raise Refused(f"{path.relative_to(root).as_posix()} is not well-formed XML ({error})") from None
    properties = {}
    for _, pom in poms:
        for element in pom.findall(f"{_POM}properties/*"):
            properties.setdefault(element.tag.replace(_POM, ""), (element.text or "").strip())
    releases = []
    for name, pom in poms:
        values = [(pom.findtext(f"{_POM}properties/{_POM}{key}") or "") for key in _RELEASE_PROPERTIES]
        for plugin in pom.iter(f"{_POM}plugin"):
            if plugin.findtext(f"{_POM}artifactId") == "maven-compiler-plugin":
                values += [e.text or "" for key in ("release", "source", "target")
                           for e in plugin.iter(f"{_POM}{key}")]
        releases += [(name, number) for number in (_java_number(v, properties) for v in values) if number]
    facts["maven_releases"] = tuple(releases)
    return facts


def _java_number(value: str, properties: dict) -> int | None:
    """`25` or `1.8` or `${prop}` (one level) as a Java feature number; None when it is not one."""
    value = value.strip()
    reference = _PROPERTY.match(value)
    if reference:
        value = properties.get(reference.group(1), "")
    match = re.fullmatch(r"(?:1\.)?(\d+)", value)
    return int(match.group(1)) if match else None


def _properties(path: Path) -> dict[str, str]:
    found = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith(("#", "!")) and "=" in line:
            key, value = line.split("=", 1)
            found[key.strip()] = value.strip()
    return found
