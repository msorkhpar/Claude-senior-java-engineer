"""The exit code that means *the tool could not run*, spelled once for every command.

**What it does.** Defines `UNUSABLE`, the code a command line returns when the
invocation itself was a mistake — no verb, an unknown verb, a root that is not
a directory, a service that was refused. ⭐ It is the one exit code that is not
a verdict about anybody's material, which is exactly why it belongs to no
stage.

**How you use it.** `from studyforge.exitcodes import UNUSABLE`, and return it
from a `main` when the command could not do its job at all. ⭐ The `validate`
package re-exports the same object, so every caller that already reads it from
there keeps working and nothing is renumbered.

**Depends on.** Nothing, and that is the whole requirement. ⛔ The dispatcher
imports this module in its own body, so a dependency here on any stage would
load that stage with the installed command — the defect this module exists to
remove.

## ⛔ Why `2` lives outside `validate` and `0`/`1` do not

⚠️ **The command resolves a verb only when it is dispatched**, so importing
`UNUSABLE` from `validate.cli` would make `studyforge --help` load the whole
validator, and the isolation test would have to hold `validate` exempt.

⭐ **The split is by what the code SAYS, not by who happened to write it first.**
`0` and `1` are `validate`'s verdicts about an archive (`validate.report`),
and no other stage may return them meaning anything else. `2` says the tool
never got as far as a verdict, which is a sentence every stage needs — five
modules under `cli/` already imported it across a package boundary. ⛔ A
constant a second package needs is on a surface both may reach, or the two
packages do not share it (R21).

⚠️ **Placed at the top of the package rather than under `cli/`** because
`validate` imports it too, and ⛔ no stage may import the dispatcher's package:
the direction is one-way, so a stage stays runnable as
`python3 -m studyforge.<stage>` with the dispatcher absent.
"""

from __future__ import annotations

#: The tool could not run — ⛔ never a verdict about an archive. Deliberately
#: distinct from `validate`'s `INVALID`: a missing directory is a mistake in the
#: invocation, and reporting it as "invalid" teaches an adapter author to
#: distrust the one signal they have.
UNUSABLE = 2
