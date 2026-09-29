"""Where a practice's grader came from, and what it may claim about itself.

**What it does.** Owns the provenance and trust vocabulary a practice test is
recorded in, and the one rule that binds them.

**How you use it.** `check_test_record("generated")` → `("generated",
"advisory")`.

**Depends on.** `errors`.

⭐ **It is here rather than in `sections` because it answers a different
question with a different consumer.** A section key is *what is this called*;
this is *may this grader claim to be the source's*. `exercise` consumes it and R5
enforces it, and putting both in one module would file the exercise-trust rule
under naming.

## ⛔ `authoritative` ⟹ `bundled`, stated positively

⛔ **Only a test that came with the source may claim to be the source's.** R5
was written for a source whose grader is hidden and ungettable, so a test
written locally against it is *ours*, advisory, and reviewed by the reader.
Presenting our own reading as the source's grader is exactly what R5 forbids,
and it is the kind of claim nobody notices is false until a reader trusts a
green tick that was never earned.

⚠️ **Coming with the source is necessary and not sufficient.** A shipped test
is not authoritative by being shipped: the claim is earned by the blanking
derivation's two gates (spec §7), and `studyforge validate` refuses an
`authoritative` exercise with no derivation record behind it. This module
decides only which provenances may *make* the claim; `validate.derived` checks
that the claim was earned. A shipped test nobody derived is `bundled` and
`advisory`.

⚠️ **A list of forbidden pairs fails open.** One naming only
`("generated", "authoritative")` accepts `user` + `authoritative`, and a grader
the reader wrote could declare itself the source's own. ⭐ So the rule is
stated from the other side: `MAY_BE_AUTHORITATIVE` is a closed set of one, and
**a provenance nobody has decided about is
non-authoritative automatically** rather than by somebody remembering to add a
row. *Enumerate the legal, never the illegal* (R5, R6).

⚠️ **`trust` defaults from `provenance` rather than being required.** For
`generated` and `user` the default is the only legal value. For `bundled` it is
`authoritative`, which is a claim like any other: `validate` holds it to a
derivation record whether it was written out or defaulted, so a shipped test
that was not derived says `advisory` in its record.
"""

from __future__ import annotations

from studyforge.unit.errors import ContentError, describe

#: Where a practice test came from. `bundled` is a copy of upstream and is
#: replaced wholesale on re-import; `generated` and `user` are work somebody
#: did and are preserved.
PROVENANCE = ("bundled", "generated", "user")

#: The only authority statement a test carries. ⚠️ `advisory` is the honest
#: word for anything written locally against a grader that cannot be seen.
TRUST = ("authoritative", "advisory")

#: What each provenance means when nobody says. ⛔ Only material that came
#: with the source may default to authoritative, and `validate` holds that
#: default to a derivation record as it holds a written-out claim.
DEFAULT_TRUST = {
    "bundled": "authoritative",
    "generated": "advisory",
    "user": "advisory",
}

#: ⛔ The provenances that may claim `authoritative`, stated as data so a test
#: can assert the rule rather than the message. ⭐ **The legal set, not the
#: illegal one** (R5): a fourth provenance added to `PROVENANCE` is
#: refused `authoritative` on the day it is added, without anybody deciding —
#: which is the opposite of what the forbidden-pair list did.
MAY_BE_AUTHORITATIVE = ("bundled",)


def check_test_record(provenance: object, trust: object = None) -> tuple[str, str]:
    """Return the validated `(provenance, trust)` pair, or raise naming the fault."""
    if provenance not in PROVENANCE:
        raise ContentError(
            f"a practice test's provenance must be one of {list(PROVENANCE)}, "
            f"got {describe(provenance)}"
        )
    if trust is None:
        trust = DEFAULT_TRUST[provenance]
    if trust not in TRUST:
        raise ContentError(
            f"a practice test's trust must be one of {list(TRUST)}, got {describe(trust)}"
        )
    if trust == "authoritative" and provenance not in MAY_BE_AUTHORITATIVE:
        raise ContentError(
            f"only {list(MAY_BE_AUTHORITATIVE)} may be marked authoritative, not "
            f"{provenance}: no check written here may claim to be the source's "
            f"grader"
        )
    return provenance, trust
