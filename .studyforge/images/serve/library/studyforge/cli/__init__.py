"""The framework's entry points: build, serve, plan, reconcile, validate, check.

**What it does.** The command line is the framework's public surface. A corpus
is configuration passed to these commands; it is never a caller of framework
internals, and the direction is one-way in both senses — `studyforge` never
imports a consumer, and a consumer never imports past this seam.

**How you use it.**

    studyforge validate <archive>   an adapter's definition of done (R2)
    studyforge plan <corpus>        what would be written, before it is
    studyforge narrate <corpus> --voice <voice>
    studyforge build <corpus> --out <directory>
    studyforge serve <root>         every built corpus under it
    studyforge reconcile <corpus>   artifacts whose source is gone
    studyforge check <file>         the test for a unit's file the reader edited

`main(argv) -> int` is the installed command, and `VERBS` is the table it
dispatches on.

**Depends on.** Every other package. Nothing depends on this one.

⭐ **`plan` exists because the first thing a corpus owner wants is not a
build.** It is an answer to "what are you about to write into my repository",
and R3 is only credible if that question can be asked without taking the risk.

⚠️ **Orchestration lives here, not in a consumer.** A build pipeline that lives
inside one corpus is a framework with one consumer (R19); what a corpus
contributes is its configuration.

⛔ **Every verb above but one is REGISTERED.**
`validate`, `plan`, `narrate`, `build`, `serve` and `check` are in
`dispatch.VERBS` and run today. ⚠️ `reconcile` is named here as the shape the command line will
have and is deliberately absent from the table, because
a verb registered against a callable that does not exist yet makes an installed
command that fails on first invocation. ⚠️ `serve` is one module, `cli.serve`,
not a package: it runs as `studyforge serve` or `python3 -m studyforge.cli serve`.

⭐ **`plan` is the shape the other commands copy**, and the shape is
`studyforge.validate`'s: a `report` module for what the answer *is*, one module
for working it out, a `cli` for the arguments and the exit code, and a
`__main__` that is four lines on top of `cli.main`. ⭐ Every stage stays
independently invocable as `python3 -m studyforge.<stage>`, so regenerating one
part of a site never requires the dispatcher.
"""

from __future__ import annotations

from studyforge.cli.dispatch import PROGRAM, VERBS, Verb, main, usage

#: ⛔ The package's whole public surface.
__all__ = ["PROGRAM", "VERBS", "Verb", "main", "usage"]
