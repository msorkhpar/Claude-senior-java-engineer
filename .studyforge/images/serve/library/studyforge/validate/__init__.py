"""`studyforge validate` — the definition of done for an adapter, and for a build.

**What it does.** Decides whether an archive is valid, and whether a build left
the source repository as it found it. This is the executable half of the
adapter seam: an adapter's obligation is to write an archive this command
accepts (R2), and no other agreement between an adapter and the framework
exists.

**How you use it.** `studyforge validate <archive>`. It exits non-zero and
names what is wrong — by address, by file, by rule. For a build:
`check_untouched(snapshot(root, m), snapshot(root, m), m, footprint)` around it
returns the same `Report` (`validate.nondestructive`).

**Depends on.** `archive`, `corpus`, `address`. ⛔ Not on `render` or `serve`:
validity is a property of the input, and a validator that needed the renderer
would be answering "does this build" instead.

⛔ **Fail loud, never silently short** (R6). A unit with no content, a broken
link, an unmatched class, a declared practice that is absent — each is reported
by name and exits non-zero. A validator that reports nine problems out of ten
and exits zero is worse than none.

⚠️ **A completeness check counts something the parser did not produce.** A
check that recounts the parser's own output agrees with itself by construction
and catches nothing.

⛔ **The non-destructive check reads the manifest's declaration** (R3) and never
hardcodes any corpus's exception. It is framework code for the same reason: a
guarantee that lives in one consumer is a guarantee the next consumer does not
get.

## ⛔ What another package takes, it takes from HERE (R21's producer half)

⭐ **Seven names below are on this surface because a package outside `validate`
needs them** — `Held` and `Walk` for a walk a caller already has, and
`RULE_DUPLICATE_PATH`, `check_placement`, `headings`, `HEADING_LINE` and
`FENCE` for the one question each answers. ⚠️ **The last two serve the
exercises skill**, whose source ledger walks a material file asking which headings
enclose each fenced example — ⛔ an interleaved walk neither `headings` nor
`region` performs, so the two PATTERNS are shared while the WALK is not, and
the tree keeps one definition of what a heading and a fence are.

⛔ **They are exported rather than reached for:** a name a second package needs
is on the first package's `__all__`, or the two packages do not share it, and
`tests/studyforge/validate/test_init.py` reds when one leaves.

## ⛔ `headings` is a CALLABLE here, and `validate.headings` is still the MODULE

⚠️ **The export shadows the submodule attribute, and that is a decision rather
than an oversight**. ⭐ **It is this project's standing shape, not a novelty:** a
dozen `(package, name)` pairs in `src/` export a callable whose name is one of
that package's own modules — `address.identifier`, `contents.order` and
`skills.reconnaissance.survey` among them. ⛔ **Refusing it here would make this
package the exception and would leave `skills.reconnaissance` reaching past this
surface permanently**, which is the defect one exported home prevents.

⭐ **Nothing is hidden by it.** `from studyforge.validate.headings import
count_headings, region` still resolves: the binding rebinds this PACKAGE's
attribute and never touches `sys.modules`, so the module keeps its name and
its other two callables, and the exported function IS the module's own.

⚠️ **The one wart, written down because it is the reader's trap:** after
`import studyforge.validate.headings`, the expression
`studyforge.validate.headings` is the FUNCTION, not the module. ⛔ No module in
this tree writes that form — every caller uses a `from` import — and the
mirror asserts both halves rather than trusting this sentence.

⚠️ **The two renames were weighed and refused, and the ground is scope, not
taste.** Renaming the function reaches into `skills.reconnaissance` (its
import, its call and its contract prose); renaming the module reaches into
that package *and* `corpus.container`, whose contract names
`validate.headings` in prose. ⭐ Both move a correct name to dodge a shadowing
twelve packages already accept.
"""

from __future__ import annotations

from studyforge.validate.cli import UNUSABLE, main
from studyforge.validate.corpus import Held, Walk
from studyforge.validate.headings import FENCE, HEADING_LINE, headings
from studyforge.validate.nondestructive import Snapshot, check_untouched, snapshot
from studyforge.validate.paths import RULE_DUPLICATE_PATH, check_placement
from studyforge.validate.report import INVALID, OK, Finding, Report, Unchecked
from studyforge.validate.run import CHECKS, validate

__all__ = [
    "CHECKS",
    "FENCE",
    "HEADING_LINE",
    "INVALID",
    "OK",
    "RULE_DUPLICATE_PATH",
    "UNUSABLE",
    "Finding",
    "Held",
    "Report",
    "Snapshot",
    "Unchecked",
    "Walk",
    "check_placement",
    "check_untouched",
    "headings",
    "main",
    "snapshot",
    "validate",
]
