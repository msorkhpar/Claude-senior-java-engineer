r"""A code file and its partner — a source and the test that tests it — and how that test runs.

**What it does.** Answers, for one code file of a corpus, whether it is code a
page may open at all (`is_code`), which source and which test it stands
between (`pair`), and the one command that runs that test in the copy of the
corpus's code (`test_command`).

**How you use it.**

    found = pair(root, "01-basics/src/test/java/p/TypesTest.java", runtimes)
    found.source, found.test      # both corpus-relative, either may be None
    test_command(root, found, runtimes)   # argv, or None where none is known

**Depends on.** `conventions` for what a test, a build file and a source suffix
look like, and `codetree` for which files are code and where the copy is.
⛔ It reads the corpus and writes nothing, and it names no corpus (R1).

## ⭐ Either file reaches the other, and the NAME decides before the TEXT does

⭐ **By name first**: a test is `<source stem>` plus one of the test shapes
(`TypesTest` tests `Types`, `test_parse` tests `parse`), in the same build
module and with the same suffix. ⚠️ Where the author named the two differently
(`AutoboxingPerformanceTest` testing `WrapperVsPrimitive`), **by the text**: the
test names the source's stem as a word, and the source named most often is
its partner. The same two readings answer the other direction, from a source to
the test that names it.

⛔ **A tie is no partner**, never a guess. Two sources a test names equally
often are two things it tests, and opening either as *the* source would say
something the author did not; the file then opens alone. ⭐ Where several
files share one name, the one whose directory shares the longest tail with the
file's own — the package — is the partner, and a tie there is no partner too.

## ⭐ The build module is the scope

⭐ **A partner is looked for inside the file's own build module** — the
deepest directory above it holding a build file of a declared tool — because
two modules may each carry a `Main` and a `MainTest` that have nothing to do
with each other. A corpus with no build file is one module.

## ⛔ The test runs in the COPY, by the declared build tool's own command

⭐ **`test_command` is argv the runner starts verbatim**, pointed into
`codetree.CODE_COPY`, so what a run writes lands in the copy and never in the
author's tree. ⭐ **Maven**: the build is the topmost directory of consecutive
build files above the test — a reactor — so a module is built with what it
depends on (`-pl <module> -am`), offline, and only the one test class runs
(`-Dtest=<stem>`), which the modules it depends on need not carry
(`-Dsurefire.failIfNoSpecifiedTests=false`). ⭐ Not `-q`: the page's filter
(`execute.quiet`) drops Maven's own chatter and keeps the test's counts, so a
reader sees *Tests run: 8, Failures: 0* rather than an exit line alone.
⛔ **A corpus whose build tool has no command here gets none**, and the page
then offers no Run rather than one that cannot work: a command is never guessed
for a tool nobody measured.
"""

from __future__ import annotations

import re
from collections import Counter
from collections.abc import Callable
from dataclasses import dataclass
from pathlib import Path, PurePosixPath

from studyforge.execute.codetree import CodeRefused, code_files, in_copy
from studyforge.execute.conventions import (
    BUILD_FILES,
    is_a_test,
    source_suffixes,
    tested_stem,
)

#: The build tools a test command is known for, and each one's build file.
MAVEN = "maven"
POM = "pom.xml"

#: A word a test's text may name a source by.
WORD = re.compile(r"[A-Za-z_][A-Za-z0-9_]*")

#: The most of a test's text read for the names it carries.
MAX_TEXT = 1024 * 1024


@dataclass(frozen=True, slots=True)
class Pair:
    """One code file, the source and the test it stands between, and its build module.

    ⭐ Every path is corpus-relative. `opened` is the file asked for, and is
    `source` or `test`; the other may be `None` where no partner was found.
    `module` is the file's build module, `""` for the corpus root.
    """

    opened: str
    source: str | None
    test: str | None
    module: str


def is_code(path: str, runtimes: tuple[str, ...] | list[str]) -> bool:
    """Whether a corpus-relative path names code a declared runtime writes."""
    suffixes = source_suffixes(runtimes)
    return bool(suffixes) and path.endswith(suffixes)


def pair(root: Path, path: str, runtimes: tuple[str, ...] | list[str]) -> Pair | None:
    """Return `path`'s pair, or `None` when it is not a code file the copy holds."""
    return pair_in(code_files(Path(root)), path, runtimes)


def pairing(root: Path, runtimes: tuple[str, ...] | list[str]) -> Callable[[str], Pair | None]:
    """Return `pair` for one corpus, walking its code once and answering each file once.

    ⭐ A build asks this for every file its pages link, so the corpus is walked
    once per build rather than once per link. ⚠️ Code too large to copy pairs
    nothing, exactly as the served editor then opens nothing.
    """
    try:
        files = code_files(Path(root))
    except CodeRefused:
        files = {}
    known: dict[str, Pair | None] = {}

    def answer(path: str) -> Pair | None:
        if path not in known:
            known[path] = pair_in(files, path, runtimes)
        return known[path]

    return answer


def pair_in(
    files: dict[str, Path], path: str, runtimes: tuple[str, ...] | list[str]
) -> Pair | None:
    """Return `path`'s pair among `files` (`codetree.code_files`'s answer), or `None`."""
    if path not in files or not is_code(path, runtimes):
        return None
    module = module_of(files, path, runtimes)
    suffix = PurePosixPath(path).suffix
    inside = [one for one in files if one.endswith(suffix) and _under(one, module)]
    if is_a_test(path):
        sources = [one for one in inside if not is_a_test(one)]
        partner = _by_name(path, sources, tested_stem(_stem(path)))
        if partner is None:
            partner = _by_text(files[path], sources)
        return Pair(opened=path, source=partner, test=path, module=module)
    tests = [one for one in inside if is_a_test(one)]
    named = [one for one in tests if tested_stem(_stem(one)) == _stem(path)]
    partner = _by_name(path, named, None)
    if partner is None:
        partner = _naming(path, {one: files[one] for one in tests})
    return Pair(opened=path, source=path, test=partner, module=module)


def module_of(files: dict[str, Path], path: str, runtimes: tuple[str, ...] | list[str]) -> str:
    """Return the deepest directory above `path` holding a declared tool's build file."""
    names = {one for tool in runtimes for one in BUILD_FILES.get(tool, ())}
    parts = PurePosixPath(path).parts[:-1]
    for depth in range(len(parts), -1, -1):
        here = "/".join(parts[:depth])
        if any((f"{here}/{one}" if here else one) in files for one in names):
            return here
    return ""


def test_command(
    root: Path, found: Pair, runtimes: tuple[str, ...] | list[str]
) -> list[str] | None:
    """Return the argv that runs `found`'s test in the copy, or `None` when none is known."""
    if found.test is None or MAVEN not in runtimes:
        return None
    return _test_argv(code_files(Path(root)), found)


def _test_argv(files: dict[str, Path], found: Pair) -> list[str] | None:
    """Return the Maven argv that runs `found`'s test among `files`, or `None`."""
    if _pom(found.module) not in files:
        return None
    reactor = found.module
    while reactor and _pom(_parent(reactor)) in files:
        reactor = _parent(reactor)
    argv = ["mvn", "-B", "-o", "-f", in_copy(_pom(reactor))]
    if found.module != reactor:
        cut = len(reactor) + 1 if reactor else 0
        argv += ["-pl", found.module[cut:], "-am"]
    return [*argv, "test", f"-Dtest={_stem(found.test)}", "-Dsurefire.failIfNoSpecifiedTests=false"]


def test_commands(root: Path, runtimes: tuple[str, ...] | list[str]) -> list[list[str]]:
    """Return the argv that runs each test file the copy holds, every one a command may run.

    ⭐ **The run service's allowlist reads this** (`serve.published`): a test's
    command names its build module and its own stem and never its source, so
    each test is asked for on its own and no text is read. ⚠️ Code too large to
    copy runs no test, exactly as the served example then opens nothing.
    """
    if MAVEN not in runtimes:
        return []
    try:
        files = code_files(Path(root))
    except CodeRefused:
        return []
    found = []
    for path in files:
        if is_code(path, runtimes) and is_a_test(path):
            module = module_of(files, path, runtimes)
            opened = Pair(opened=path, source=None, test=path, module=module)
            argv = _test_argv(files, opened)  # ⭐ one walk for every test, not one each
            if argv is not None:
                found.append(argv)
    return found


def _by_name(path: str, candidates: list[str], stem: str | None) -> str | None:
    """Return the candidate named `stem` (any, when `None`) nearest `path`, or `None`."""
    named = [one for one in candidates if stem is None or _stem(one) == stem]
    if not named:
        return None
    scored = sorted(((_shared_tail(path, one), one) for one in named), reverse=True)
    if len(scored) > 1 and scored[0][0] == scored[1][0]:
        return None
    return scored[0][1]


def _by_text(test: Path, sources: list[str]) -> str | None:
    """Return the one source a test's text names most often, or `None` on none or a tie."""
    counted = _words(test)
    scored = sorted(
        ((counted[_stem(one)], one) for one in sources if counted[_stem(one)]), reverse=True
    )
    if not scored or (len(scored) > 1 and scored[0][0] == scored[1][0]):
        return None
    return scored[0][1]


def _naming(path: str, tests: dict[str, Path]) -> str | None:
    """Return the one test naming `path`'s stem most often, or `None` on none or a tie."""
    stem = _stem(path)
    scored = sorted(
        ((count, one) for one, file in tests.items() if (count := _words(file)[stem])),
        reverse=True,
    )
    if not scored or (len(scored) > 1 and scored[0][0] == scored[1][0]):
        return None
    return scored[0][1]


def _words(file: Path) -> Counter[str]:
    """Count the words a file's text carries; a file that cannot be read carries none."""
    try:
        with file.open(encoding="utf-8", errors="replace") as handle:
            return Counter(WORD.findall(handle.read(MAX_TEXT)))
    except OSError:
        return Counter()


def _shared_tail(first: str, second: str) -> int:
    """How many trailing directory names two paths share."""
    one = PurePosixPath(first).parts[:-1][::-1]
    other = PurePosixPath(second).parts[:-1][::-1]
    shared = 0
    for left, right in zip(one, other, strict=False):
        if left != right:
            break
        shared += 1
    return shared


def _stem(path: str) -> str:
    return PurePosixPath(path).stem


def _under(path: str, module: str) -> bool:
    return not module or path.startswith(f"{module}/")


def _parent(directory: str) -> str:
    return directory.rpartition("/")[0]


def _pom(directory: str) -> str:
    return f"{directory}/{POM}" if directory else POM
