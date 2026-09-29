"""One way to say what arrived, when saying *what* arrived would be a leak.

**What it does.** Turns any value into a short phrase naming its **type** —
`a str`, `an int`, `nothing` — so a refusal can say what it was given without
reproducing it (R7).

**How you use it.** `describe(value)` wherever a message would otherwise have
written `{value!r}`:

    raise ContentError(f"section kind must be one of {list(KINDS)}, got {describe(kind)}")

**Depends on.** Nothing, and that is the point — this is the module every
package imports, so a dependency in any direction is a cycle waiting for the
second caller (`studyforge.version`'s argument, for the same reason).

⛔ **Why one module rather than one function per package** (R7). Copies of
this rule in `version`, `corpus.container.fields` and `unit.errors` would
drift — one quoting integers and another returning *"an int"* — and nobody
would choose that difference. ⭐ A constant or a rule written twice is a rule
that disagrees with itself, so the three import this one.

⭐ **Integers are quoted; booleans are not** (this is the one place
the three copies genuinely disagreed). An integer cannot carry an identifier
and a refusal that will not say `unit 4` is a refusal nobody can act on — so
`3` is quoted. ⛔ A boolean cannot carry one either, so R7 is indifferent, and
the question is purely which sentence helps: **an integrator who wrote JSON
`true` where `1` was wanted is told `True`, which is not what they typed.**
*"a bool"* names the mistake they actually made. ⚠️ `version._said` had that
argument written down and pinned by a test while the extraction quoted the
opposite; the extraction was the one that had never made the case.

⭐ **An empty string is `an empty string`**, which `corpus.container.fields.said`
had and the extraction lost. It reproduces nothing and it separates the two
mistakes a reader can make — *absent* and *present but blank* — which is a
distinction `optional_text` exists to draw. ⚠️ Whitespace counts as empty: a
field of three spaces is somebody who meant to write something.

⛔ **What this deliberately does not do is guess.** It never truncates, never
hashes, never redacts a substring: a value is named by its type or it is a
number. A describer that emitted "the first eight characters" would be a
policy about how much of an identifier is acceptable in a log, and there is no
acceptable amount (R7).
"""

from __future__ import annotations

#: Types whose *values* a refusal may reproduce. ⛔ Closed, and closed on a
#: property rather than on taste: an `int` cannot carry a path, an email
#: address, a hostname or a username. Widening it to `str` is the whole defect
#: this module exists to remove, so a second entry needs an argument that a
#: string does not also satisfy.
#:
#: ⚠️ **`bool` is not here**, although quoting it would be equally safe:
#: `True` is not what the integrator typed, and *"a bool"* is. ⭐ Safety is not
#: the question for that type.
SAFE_TO_QUOTE = (int,)


def describe_keys(keys: object) -> str:
    """Name the keys that can be named safely, and count the ones that cannot.

    ⭐ **A different guarantee from `describe`, and the difference is why this
    is a function rather than a call to it.** `describe` refuses to reproduce a
    value because *any* string may carry an identifier. A **key** that is a
    plain lowercase identifier structurally cannot: no `/`, no `@`, no `:` and
    no space fits the pattern, so there is no path, address or token it could
    be — and naming it is what makes "you misspelled this field" actionable.

    ⛔ **Structural, never a shape list.** A measurement shows what the
    shape-list argument is worth: 4 of 10 poison shapes came back clean because
    the personal-data gate's list happened to name them, not because anything
    refused. This asks what a string *can hold*, which does not depend on
    anybody keeping a list current.

    ⚠️ **What a plain key can still be, said plainly: a bare lowercase word —
    and a person's given name, surname or username is one.** `jane` is a valid
    lowercase identifier, so this function names it. ⛔ The guarantee is
    *categorical, not total*: it rules out the composite forms — a path, an
    address, a URL, a token, a hostname — because each needs a character the
    pattern forbids, and it does not rule out a word. ⭐ What makes that the
    right trade is that a key is a **field name reported apart from its
    value**: naming an unrecognised key says which slot was misspelled and
    reproduces nothing anybody wrote *into* it. ⛔ So pass keys here, never
    values — `describe` is the function for a value, and it refuses them all.

    ⚠️ Anything else is counted rather than shown, so a caller always learns how
    many keys it did not recognise even when none of them can be quoted.
    """
    keys = list(keys)
    plain = sorted(k for k in keys if isinstance(k, str) and k.isidentifier() and k.islower())
    if len(plain) != len(keys):
        return f"{len(keys)} of which {len(plain)} can be named safely: {plain}"
    return str(plain)


def describe(value: object) -> str:
    """Name what `value` is, without reproducing what it says.

    ⭐ **Type, not value.** A wrong *value* and a wrong *type* are different
    mistakes and deserve different sentences, and naming the type describes an
    unexpected payload rather than reproducing it into a message that lands in
    a log, a bug report or a paste.

    ⚠️ `None` is `nothing` rather than `a NoneType`: the reader's mistake is
    an absent field, and the type name is Python trivia at that point.

    ⚠️ An empty or blank string is named as such rather than as `a str`. ⛔ It
    is the one thing a string can be that says nothing about anybody, and
    *absent* and *present but blank* are different mistakes — the second is
    somebody who meant to write something.
    """
    if value is None:
        return "nothing"
    if isinstance(value, bool):
        return f"a {type(value).__name__}"
    if isinstance(value, SAFE_TO_QUOTE):
        return repr(value)
    if isinstance(value, str) and not value.strip():
        return "an empty string"
    name = type(value).__name__
    return f"{'an' if name[:1] in 'aeiou' else 'a'} {name}"
