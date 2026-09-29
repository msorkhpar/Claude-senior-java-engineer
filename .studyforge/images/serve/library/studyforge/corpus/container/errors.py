"""The one exception the container-map reader raises.

**What it does.** Names every way a `container.json` can be unacceptable, so a
caller catches one type rather than eight.

**How you use it.** Catch `ContainerError`.

**Depends on.** Nothing. ⭐ That is why it is its own module: both `fields` and
`document` raise it, and putting it in either would make the other import a
module it has no other business with — the same seam `archive/errors.py` has.

⚠️ **One deliberate exception, following `corpus.manifest`'s precedent exactly.**
An address whose arity disagrees with the corpus raises `address`'s `AddressError`,
because that call is the arity *comparison* and `address` owns it outright; the
container map only supplies the address it declared. ⛔ Every other rule of
`studyforge.address`'s applied here — what a slug is, what an ordinal is — is **converted**,
because the rule is `studyforge.address`'s but the document is this contract's.

⚠️ `PersonalDataLeak` from `archive.scrub` is not wrapped either: R7's refusal
is louder than a format error, and a caller writing `except ContainerError:
skip_this_file()` must not silently swallow one.

⛔ **A caller catches `container.RAISES`, not this paragraph.** The three names
above are the tuple's members and the tuple is in the package contract, where a
caller already looks. ⚠️ **This module still depends on nothing**, which is why
the tuple is not here: `fields` and `document` both import this one, and giving
it two imports of its own would put them in a module that has no other business
with either.

⭐ **The tuple exists because this paragraph was read PARTIALLY**.
`cli/plan/derive.py` caught `PersonalDataLeak` and not `AddressError` — the
second pass-through applied and the first missed, which is how you can tell it
was read and half-copied rather than never read.
"""

from __future__ import annotations


class ContainerError(ValueError):
    """A `container.json` this build will not accept.

    ⛔ The message names the file and the field, and — where a closed set was
    expected — what the accepted values are. ⛔ It never formats an exception
    object into itself and never reproduces a value that might be carrying a
    path: see `fields.said`.
    """
