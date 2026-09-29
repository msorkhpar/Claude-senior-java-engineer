"""The run output filter: keep what a run tells the reader, drop what the build tool says.

**What it does.** Sits between the runner and the page (`serve.routes.runs`
applies it to every line a run streams) and drops the lines a build tool prints
about itself (plugin banners, task lines, separators,
timings, help links) so the reader sees their program's output and the
verdict. It only ever DROPS a line. A kept line is the runner's line, byte
for byte, in its original order.

**How you use it.**

    from studyforge.execute.quiet import filter_lines, select

    toolchain = select(corpus.runtimes)        # a declared runtime, or None
    for line in filter_lines(handle.lines(), toolchain):
        ...

`Quiet(toolchain).keeps(line)` answers for one line, for a caller that
streams line by line and has to decide as each line arrives.

**Depends on.** `re`, `dataclasses`, and `handle`'s spelling of the exit line.
It sees only what the runner yields, which is already relative to the source
root and scrubbed (`output`), so it rewrites nothing.

## ⭐ Per toolchain, DECLARED, and never guessed

A corpus declares its `runtimes` (`corpus.manifest.runtimes`). ⭐
**`TOOLCHAINS` maps a runtime name to its rules, and the rules are data.** A
Gradle corpus and a Maven corpus each get their own, and adding a third is one
entry. ⛔ **Nothing here looks at the command or the output to work out which
tool is running.** `select` returns a toolchain only when exactly ONE
declared runtime has rules. None, or two (a corpus declaring both `gradle`
and `maven`), means `None`, and `None` passes every line through UNFILTERED.
Guessing wrong would drop somebody's output. Not guessing costs some noise.

## ⛔ Filtering never removes a failure

A dropped line must never be the one that would have told the reader why
their code failed. **When in doubt the line survives**, so each toolchain
lists the NOISE it drops and everything unlisted is kept. Four things are
never dropped, whatever a toolchain's rules say, because `keeps` checks them
first:

- **the exit line.** The page reads the run's verdict off it.
- **a stack trace**, frame by frame: `at …` frames, `Caused by:`,
  `Suppressed:`, `... N more`, and `Exception in thread`. ⚠️ No frame is
  kept or dropped by the package it names (R1 forbids knowing that name): a
  stack trace survives INTACT. A trimmed trace is a claim about which frames matter, and
  that claim is wrong exactly when the reader's bug is somewhere unexpected.
- **a toolchain's `signal`**, checked before its `noise`: a Maven `[ERROR]`,
  a test tally, a Gradle compile error. These are lines that could look like
  noise.
- **any line no rule names**, which covers a program's own output.

Only `always_noise` is checked before `signal`. It holds lines that LOOK
like signal but are not (Maven's `[ERROR] -> [Help 1]` footer, and Gradle's
`> Task :test FAILED`, which is a task line ending in the word FAILED). ⛔ It
can never reach the four above, because the stack-trace check and the exit
line come first.

## ⚠️ What this module does not do, deliberately

- It never rewrites a line. The runner's `LineGate` trims paths, for every
  line, before this module sees any of them.
- It knows no corpus's output markers: R1 forbids naming one. A program's
  lines survive because no rule names them.
- It never collapses blank lines. A program's blank line is its output.
"""

from __future__ import annotations

import re
from collections.abc import Iterable, Iterator
from dataclasses import dataclass

from studyforge.execute.handle import exit_line

#: The runner's last line, whatever its code or word. Spelled from `exit_line`,
#: so the two cannot drift apart.
EXIT_LINE = re.escape(exit_line("CODE")).replace("CODE", r"\S+")

#: ⛔ A line of a stack trace, in any toolchain. Checked BEFORE every
#: toolchain's rules, so no toolchain's noise can reach a frame. JVM frames
#: are `\tat …` (a thrown exception) or `    at … (File.java:N)` (Maven's own
#: `-e` trace); Node's are `    at …` too.
STACK_TRACE = (
    r"^\s+at \S",
    r"^\s*Caused by: ",
    r"^\s*Suppressed: ",
    r"^\s+\.\.\. \d+ more\s*$",
    r"^Exception in thread ",
)


@dataclass(frozen=True)
class Toolchain:
    """One build tool's rules. ⭐ Data, never code: each field is a tuple of regexes.

    `always_noise` drops a line even when `signal` would keep it. `signal` keeps
    a line even when `noise` would drop it. `noise` drops a line. A line that
    none of them matches is kept.
    """

    name: str
    always_noise: tuple[str, ...]
    signal: tuple[str, ...]
    noise: tuple[str, ...]


#: ⭐ Maven 3.9, measured against real `mvn -B test` output (see the tests).
#: Maven prefixes every line it prints with a level: `[INFO]`, `[WARNING]` or
#: `[ERROR]`. A test's own output and a thrown exception's trace arrive
#: UNPREFIXED, which is why no rule here touches an unprefixed line.
MAVEN = Toolchain(
    name="maven",
    always_noise=(
        # The help footer after a failure. It explains how to rerun Maven, never
        # why the build failed.
        r"^\[ERROR\] ?$",
        r"^\[ERROR\] -> \[Help \d+\]$",
        r"^\[ERROR\] To see the full stack trace of the errors, re-run Maven with the -e switch",
        r"^\[ERROR\] Re-run Maven using the -X switch to enable full debug logging",
        r"^\[ERROR\] For more information about the errors and possible solutions, please read",
        r"^\[ERROR\] \[Help \d+\] https?://",
        r"^\[ERROR\] After correcting the problems, you can resume the build with the command",
        r"^\[ERROR\]   mvn .* -rf :",
        # Surefire's pointers at its own files: `Please refer to` before 3.x,
        # `See` in the pinned one.
        r"^\[ERROR\] (Please refer to|See) .* for the individual test results",
        r"^\[ERROR\] (Please refer to|See) dump files",
    ),
    signal=(
        # ⛔ Every other [ERROR] line: a compile error, a failed test, the goal
        # that failed and why.
        r"^\[ERROR\]",
        # The test tally is the verdict, at every level Maven prints it.
        r"Tests run: \d+",
    ),
    noise=(
        r"^\[INFO\] ?$",
        r"^\[INFO\] Scanning for projects\.\.\.$",
        r"^\[INFO\] Error stacktraces are turned on\.$",
        # Every rule and banner: `---< g:a >---`, `---[ jar ]---`,
        # `--- plugin:1.0:goal (id) @ project ---` and the bare `-------`.
        r"^\[INFO\] -{3}.*-{3}$",
        r"^\[INFO\] Building .+$",
        r"^\[INFO\]   from \S+$",
        r"^\[INFO\] skip non existing resourceDirectory ",
        r"^\[INFO\] Copying \d+ resources? ",
        r"^\[INFO\] Recompiling the module because of ",
        r"^\[INFO\] Nothing to compile - all classes are up to date",
        r"^\[INFO\] Compiling \d+ source files? with ",
        r"^\[INFO\] Using auto detected provider ",
        r"^\[INFO\]  T E S T S$",
        r"^\[INFO\] Results:$",
        r"^\[INFO\] BUILD (SUCCESS|FAILURE)$",
        r"^\[INFO\] (Total time|Finished at): ",
        r"^\[INFO\] Reactor Summary",
        r"^\[INFO\] .+ \.+ (SUCCESS|SKIPPED)( \[.*\])?$",
        r"^\[INFO\] (Downloading|Downloaded) from ",
    ),
)

#: ⭐ Gradle. No rule names a reader's frames or output markers (R1, and a
#: trace survives intact), and a `… STANDARD_OUT` header is kept, because it
#: is the only label a test's output has.
GRADLE = Toolchain(
    name="gradle",
    always_noise=(
        # `> Task :test FAILED` ends in FAILED and would otherwise be kept as a
        # verdict, when the verdict is the test line further down.
        r"^> Task :",
        r"^BUILD (SUCCESSFUL|FAILED)\b",
        r"^\d+ actionable tasks?:",
        r"^FAILURE: Build failed with an exception\.",
        r"^(?:> )?Execution failed for task ",
        r"^(?:> )?There were failing tests\b",
    ),
    signal=(
        r"^e: ",
        r"^error: ",
        r"\b(FAILED|PASSED|SKIPPED)\s*$",
        r"^\d+ tests? completed",
        r"(AssertionError|AssertionFailedError|Exception|Error):",
        r"^\s+(expected:|actual:)",
    ),
    noise=(
        r"^\* (What went wrong|Try|Get more help|Where):",
        r"^> Run with --",
        r"^> Get more help at ",
        r"^Deprecated Gradle features were used",
        r"^You can use '--warning-mode all'",
        r"^For more on this, please refer to ",
        r"^Consider enabling ",
        r"docs\.gradle\.org",
        r"^(Starting|Daemon|Welcome to Gradle)\b.*Gradle",
        r"^<[-=]+>\s*\d+%",
        r"^w: ",
        r"^warning: ",
        r"^Note: .*(deprecat|unchecked)",
        r"^Picked up JAVA_TOOL_OPTIONS",
    ),
)

#: ⭐ THE declaration: a runtime name, as a corpus spells it in `runtimes`, to its
#: rules. ⛔ A name that is not here has no filter, and its output passes through.
TOOLCHAINS: dict[str, Toolchain] = {toolchain.name: toolchain for toolchain in (GRADLE, MAVEN)}


def select(runtimes: Iterable[str]) -> Toolchain | None:
    """Return the ONE declared runtime's rules, or `None`, which filters nothing.

    ⛔ `None` when no declared runtime has rules, and also when more than one
    does. With two build tools declared, the run could be either, and this
    module does not guess which.
    """
    found = {name for name in runtimes if name in TOOLCHAINS}
    return TOOLCHAINS[found.pop()] if len(found) == 1 else None


class Quiet:
    """One toolchain's rules, compiled once per run; `None` keeps every line."""

    def __init__(self, toolchain: Toolchain | None) -> None:
        """Compile the rules; with no toolchain there is nothing to compile."""
        self.toolchain = toolchain
        self._kept = _compile((EXIT_LINE, *STACK_TRACE))
        if toolchain is not None:
            self._always_noise = _compile(toolchain.always_noise)
            self._signal = _compile(toolchain.signal)
            self._noise = _compile(toolchain.noise)

    def keeps(self, line: str) -> bool:
        """Answer whether the reader is shown `line`. ⛔ When in doubt, yes."""
        if self.toolchain is None or _any(self._kept, line):
            return True
        if _any(self._always_noise, line):
            return False
        if _any(self._signal, line):
            return True
        return not _any(self._noise, line)


def filter_lines(lines: Iterable[str], toolchain: Toolchain | None) -> Iterator[str]:
    """Yield the lines `toolchain` keeps, unchanged and in order, as they arrive."""
    quiet = Quiet(toolchain)
    return (line for line in lines if quiet.keeps(line))


def _compile(patterns: Iterable[str]) -> tuple[re.Pattern[str], ...]:
    return tuple(re.compile(pattern) for pattern in patterns)


def _any(patterns: tuple[re.Pattern[str], ...], line: str) -> bool:
    return any(pattern.search(line) for pattern in patterns)
