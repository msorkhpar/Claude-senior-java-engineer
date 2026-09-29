"""What a slug is, and how a title becomes a candidate one.

**What it does.** Defines the one shape every address segment must already
have — lowercase, ASCII alphanumerics separated by single hyphens — and offers
`slugify` for turning a human title into a candidate of that shape.

**How you use it.** `require_slug(value, what)` at any boundary that accepts a
segment; `is_slug(value)` to ask without raising; `slug_fault(value)` for why
a string is not one; `slugify(title)` in an **adapter**, which then records
what it produced. ⛔ Framework code slugifies nothing at read time: §6 rules
that an address is *recorded, never derived*.

**Depends on.** `re`, `errors` and `studyforge.describe`. No filesystem, no
corpus, no manifest.

## ⛔ Why no refusal here reproduces the value (R7)

⚠️ **The branch fires *because* the value is not a slug — which is precisely
when it may be an absolute path.** A refusal that quoted it would take the one
input guaranteed to carry a home directory and put it in a log, from inside
the check that exists to catch it. ⭐ **This is the leverage:** `require_slug`
and `require_ordinal` are two lines that many refusals in the tree reach
through their callers — every address segment, every identity field and every
unit ordinal would inherit an echo.

⛔ **And the diagnosis is not what was removed.** *"did you pass a title?"* is
the most useful sentence in this module and it stays; what replaces the value
is the **class and position that failed**, which is more actionable than the
value was. A refusal that says nothing is a different defect from a refusal
that says too much, and this module must commit neither.

⭐ **A slug is a fixed point of `slugify`**, and that is the whole definition —
`is_slug(v)` is `v == slugify(v)` and `v` non-empty. One rule, stated once, so
"is this a slug?" and "make me a slug" can never drift apart. The extraction
source had the same property and used it the same way.

⚠️ **`slugify` is lossy, and the design tolerates that because nothing depends
on it being reversible.** Every character outside `[a-z0-9]` becomes a
separator, so two distinct titles *can* collide.

⛔ **The collision class is punctuation, not accents:**

```text
'Streams: an API'  ->  'streams-an-api'      ⛔ collides
'Streams, an API'  ->  'streams-an-api'      ⛔ with the line above
'Café'             ->  'caf'                 ⭐ does NOT collide with
'Cafe'             ->  'cafe'                ⭐ the line above
```

⚠️ An accented character collapses to a **separator** and is then stripped or
kept as a hyphen — it is not *deleted*, so `Café` and `Cafe` produce different
slugs. ⭐ The real class is wide and likely, because every English title with
a colon, a comma or a dash in the same place is in it. That is survivable only
because §6 rules an address is **recorded, never derived**: a real catalogue
serves about one unit in eight at a slug its title does not produce, so
deriving one would send one link in eight to a page that is not there. ⛔ An
adapter that slugifies its titles owes itself a collision check; the framework
cannot make one for it, because by the time the framework sees an address the
title is gone.
"""

from __future__ import annotations

import re

from studyforge.address.errors import AddressError
from studyforge.describe import describe

#: Apostrophes are **elided** before the separator rule runs, so `Beginner's`
#: becomes `beginners` and not `beginner-s`. Inherited, and it is measured
#: rather than tasteful: the extraction source serves a lesson at
#: `...-a-beginners-guide`, and the derived `...-a-beginner-s-guide` 404s. An
#: apostrophe sits INSIDE a word where every other mark this rule meets sits
#: BETWEEN words, so dropping it keeps the word whole. Both the typewriter and
#: the curly form are listed: a title copied out of a rendered page carries
#: U+2019 far more often than U+0027.
_APOSTROPHE = re.compile(r"['’ʼ]")

#: Every other run of non-alphanumerics collapses to one hyphen.
_NON_ALNUM = re.compile(r"[^a-z0-9]+")


def slugify(text: str) -> str:
    """Return `text` as a candidate slug: lowercased, apostrophes dropped, hyphenated.

    Returns the empty string when nothing survives, rather than raising —
    "this title has no slug" is an answer the caller often wants to handle
    (`require_slug` is where it becomes an error).
    """
    lowered = _APOSTROPHE.sub("", (text or "").lower())
    return _NON_ALNUM.sub("-", lowered).strip("-")


def is_slug(value: object) -> bool:
    """Return whether `value` is already a slug — a non-empty fixed point of `slugify`."""
    return isinstance(value, str) and bool(value) and value == slugify(value)


#: The characters a slug may carry. ⭐ **Derived from `is_slug`, never
#: re-typed** (R8: a permitted set is derived from its predicate, never
#: exported as a second list): the permitted class is *what a slug accepts*, asked rather than
#: restated, so a second spelling of the rule cannot exist here to drift from
#: the first. ⚠️ `corpus.container.fields` derives its filename class from
#: this same constant for exactly that reason.
SLUG_PERMITTED = frozenset(
    character for character in map(chr, range(128)) if is_slug(f"a{character}a")
)

#: How a refusal names that class. ⛔ Stated once, so no two callers describe
#: one class two ways.
SLUG_PERMITTED_DESCRIBED = "lowercase ASCII letters, digits and hyphens"


def slug_fault(value: str) -> str:
    """Say why `value` is not a slug — the class and position that failed, never the value.

    ⭐ **The replacement for `{value!r}`, and it is strictly more actionable.**
    `got '/home/…/corpus'` told a reader what they already typed; *"character
    1 is outside lowercase ASCII letters, digits and hyphens"* tells them
    which rule they broke and where — and reproduces nothing (R7).

    ⚠️ **Total over non-slugs, by construction rather than by a fallback
    branch.** A non-empty string that carries only permitted characters, does
    not begin or end with a hyphen and has no doubled hyphen **is** a slug —
    `slugify` is the identity on it — so the three branches below are the only
    ways to fail, and none of them can be reached by a value this function is
    not asked about. ⛔ An unreachable `else` here would be a branch no test
    could cover and no reader could check.
    """
    for position, character in enumerate(value, start=1):
        if character not in SLUG_PERMITTED:
            return f"whose character {position} is outside {SLUG_PERMITTED_DESCRIBED}"
    if value.startswith("-") or value.endswith("-"):
        return "which begins or ends with a hyphen"
    return "whose hyphens are doubled somewhere inside it"


def require_slug(value: object, what: str) -> str:
    """Return `value` unchanged, or raise `AddressError` naming `what` it was.

    ⛔ **It never slugifies for you**, and that is the point of the function.
    Accepting a title here would make "pass a title where a slug is required"
    a silent success, and the failure surfaces later as a directory nobody
    created or a link nobody can follow. `what` names the field so the message
    says where to look — `require_slug(value, "address segment 1")`.

    ⛔ **The refusal never reproduces `value`** — see this module's contract.
    `what` is the caller's own label for the field and *is* reproduced,
    because naming the field is what makes the refusal actionable (R6).
    """
    if not isinstance(value, str):
        raise AddressError(f"{what} must be a non-empty str, got {describe(value)}")
    if not value:
        raise AddressError(f"{what} must be a non-empty str, got an empty str")
    if not is_slug(value):
        raise AddressError(
            f"{what} must already be a slug, got a {len(value)}-character str "
            f"{slug_fault(value)} "
            f"(did you pass a title? slugify() it first, and record the result)"
        )
    return value
