"""What `validate` found, and — just as loudly — what it could not check.

**What it does.** Collects findings and unchecked claims from every check, and
renders them as lines a person reads and a script greps.

**How you use it.** A check yields `Finding(rule, where, message)`; a check
that could not run yields `Unchecked(rule, where, why)`. `Report.of(items)`
gathers both; `report.exit_code` is what the CLI returns.

**Depends on.** Nothing. ⛔ Deliberately: every check imports this, so a
dependency in the other direction is a cycle waiting for the second check.

## Fail loud has a sibling, and it is pass loud

⛔ **A check that could not run is never silent** (R6). `validate` is the only
signal an integrator has (R2), so a check that quietly did not happen turns
green into a lie — and the failure is invisible precisely when it matters, on
a source nobody has read before.

⭐ The precedent is narration's coverage: *the unit's speakable record
states how many blocks it withheld, and the report names units with unspoken
content, so the omission reads as a decision rather than as a bug in the
walker.* The same shape, one package over.

⚠️ **An `Unchecked` does not fail the run**, and the line between it and a
`Finding` is where the design lives, not where a compromise does. It is an
`Unchecked` only when the *absence of the input is itself a validated fact* —
the whole source tree is absent, not half of it. A half-present input is a
`Finding`, because that is where a short read hides. ⛔ So is an absent or
empty **archive** (`no-archive`): it is what this report judges, so
its absence is not a validated fact but a verdict with no subject.

## Every failure, never just the first

⛔ The checks are generators and the report drains all of them. An adapter
author fixing one problem per run against a corpus of 166 units is an adapter
author who stops using the tool.
"""

from __future__ import annotations

from collections.abc import Iterable
from dataclasses import dataclass

#: What the CLI returns. ⛔ `1` for "this archive is not valid", never for "the
#: tool broke" — a script cannot tell those apart from one code, and the
#: difference matters to CI.
OK = 0
INVALID = 1


@dataclass(frozen=True, slots=True)
class Finding:
    """One thing that is wrong, named by rule and by where it is.

    ## ⛔ R7 is the *raiser's* guarantee, and this class does not scrub

    ⚠️ **`message` is not promised to quote no value.** Half of `validate`'s
    findings are not written here — they are `str(error)` from a refusal raised
    upstream, and an upstream `{value!r}` reaches a report line through them
    (`tests/.../test_run.py::test_no_identifier_reaches_a_report_line` drives
    that end to end). The personal-data gate's **shape list** catches only the
    shapes it names; nothing here refuses.

    ⭐ **The boundary is upstream (R7) and it does not move here.** A
    scrub in this class would silence every leak *in the one report anybody
    reads*, which makes the upstream fix look unnecessary while the echo stays
    in every traceback and every other caller of the same function. So:

    - A message **originated** by a check in this package names the field and
      never the value, following `corpus.container.fields.said`.
    - A message **forwarded** from an upstream refusal is that module's
      guarantee to keep, and `validate` is where the composition is measured —
      trust enforced nowhere is not trust.
    """

    rule: str
    where: str
    message: str

    def line(self) -> str:
        """Render as one greppable line: `where: [rule] message`."""
        return f"{self.where}: [{self.rule}] {self.message}"


@dataclass(frozen=True, slots=True)
class Unchecked:
    """One claim this run could not make, and why.

    ⚠️ Not a failure and not a silence. It appears in the output, it is
    counted in the summary, and an integrator who ships anyway has been told
    which question went unanswered.
    """

    rule: str
    where: str
    why: str

    def line(self) -> str:
        """Render as one greppable line: `where: [rule] not checked — why`."""
        return f"{self.where}: [{self.rule}] not checked — {self.why}"


@dataclass(frozen=True, slots=True)
class Report:
    """Everything one run found and everything it could not check."""

    findings: tuple[Finding, ...]
    unchecked: tuple[Unchecked, ...]

    @classmethod
    def of(cls, items: Iterable[Finding | Unchecked]) -> Report:
        """Drain every check into one report.

        ⛔ Drains, rather than stopping at the first failure: an adapter author
        fixing one problem per run against 166 units stops using the tool.
        """
        findings: list[Finding] = []
        unchecked: list[Unchecked] = []
        for item in items:
            (findings if isinstance(item, Finding) else unchecked).append(item)
        return cls(tuple(findings), tuple(unchecked))

    @property
    def ok(self) -> bool:
        """Is the archive valid? ⚠️ An unchecked claim does not make it false."""
        return not self.findings

    @property
    def exit_code(self) -> int:
        """`0` when valid, `1` when not. Usable from a script."""
        return OK if self.ok else INVALID

    @property
    def rules(self) -> tuple[str, ...]:
        """Every rule broken, in the order first seen — what a test asserts on."""
        seen: list[str] = []
        for finding in self.findings:
            if finding.rule not in seen:
                seen.append(finding.rule)
        return tuple(seen)

    def lines(self) -> list[str]:
        """Every finding, then every unchecked claim, then one summary line."""
        out = [finding.line() for finding in self.findings]
        out += [item.line() for item in self.unchecked]
        out.append(self.summary())
        return out

    def summary(self) -> str:
        """One line naming both counts. ⛔ The unchecked count is never omitted.

        ⚠️ Not even when it is zero. A number that disappears when it is zero
        cannot be told from a number nobody wrote — the same argument the
        archive's `counts` makes, and it is why "0 unchecked" is printed.
        """
        verdict = "valid" if self.ok else "NOT valid"
        return (
            f"{verdict}: {len(self.findings)} finding(s), {len(self.unchecked)} unchecked claim(s)"
        )
