"""The one exception the manifest package raises, and R7's one phrase about a path.

**What it does.** Names every way `corpus.json` can be unacceptable, so a
caller catches one type rather than six — and carries `_escape`, the sanctioned
way to say *how* a path left the source root without repeating the path.

**How you use it.** Catch `ManifestError`. Every rule this package applies
raises it — including where the rule is `studyforge.address`'s, as a `source` or a `variants`
entry that is not a slug is — and none of them repairs a value quietly (R6).

⛔ In particular, an unknown `corpus_api` raises rather than being migrated: a
migration that runs because something merely wanted to render a page rewrites
the record of what was ingested (R9).

⭐ **Why `_escape` lives here**. Two refusals need it and
they are in different packages: `content.parse._reject_absolute` and
`edits._reject_forbidden_target`, which share one function rather
than two copies. It was `content`'s, and `edits` reached past that package's
`__all__` for a private name — a surface R21 calls wrong. ⛔ It
did **not** become public to fix that: it answers *"how does this path leave the
root"*, which is neither package's subject, and exporting a private name to
satisfy a table is how a surface grows by accident. ⭐ Its subject is this
module's own — `ManifestError` already states the rule that the value is never
echoed (R7), and `_escape` is what a refusal says instead. The
underscore is accurate at the scope that matters: nothing outside
`studyforge.corpus.manifest` names it, and nothing may.

⚠️ **Two exceptions travel through, deliberately.**

1. `Manifest.parse_key` raises `studyforge.address`'s `AddressError`, because that call is the
   arity *comparison* and `studyforge.address` owns it outright — the manifest is only
   supplying the depth it declared. Reading the document is this package's
   answer; checking an address against it is not.
2. ⛔ **`PersonalDataLeak` from `archive.scrub` is not wrapped** (R7). This
   family exists so a caller walking a corpus catches one
   type per file, reports it and continues; an R7 refusal inside it would be
   logged as one more manifest that would not read, and the walk would finish
   green about the one thing R7 exists to make loud. ⭐ It is already
   load-bearing: `validate/corpus.py` catches `ManifestError` and **then**
   `PersonalDataLeak`, and while `document._gate` translated, that second arm
   could never fire — a home path in `corpus.json` was filed under
   `RULE_MANIFEST` rather than `RULE_PERSONAL_DATA`.

⭐ **A contract that names what crosses it is better than one that swallows
it** — which is the whole answer to *"a promise with one exception is not a
promise"*.

⛔ **A caller of `parse` catches `manifest.RAISES`, not this paragraph**.
⚠️ Item 1 is `parse_key`'s and not `parse`'s, which is exactly the
distinction a reader of this paragraph gets wrong; the tuple states it. It is
in the package contract and not here because this module depends on nothing.

**Depends on.** Nothing.

⚠️ **`ValueError`, following `studyforge.address`'s proposed precedent** — a *value* error
("you handed me something I cannot accept") subclasses `ValueError`; a
*document* error ("this file is not one I can read") subclasses `Exception`.
A manifest is a value read from a document, and both halves fail the same way
here, so one base is enough. ⚠️ If the address package's split of its own
errors ever changes, this line is the whole of this package's exposure to it.
"""

from __future__ import annotations


class ManifestError(ValueError):
    """A `corpus.json` this build will not accept.

    ⛔ **The message names the field, and the accepted values where a closed set
    was expected — never the offending value itself** (R7). It never formats an
    exception object into itself either, which would carry an absolute path into
    a log.

    ⚠️ **This sentence is part of the rule**: the next author follows the
    module's written policy, so the policy must be the one the code keeps.
    ⭐ `studyforge.describe` is how a refusal says what arrived without saying
    what it said.

    ⚠️ **And a manifest is exactly why.** It is the first file an integrator
    writes by hand, so its refusals are the first thing this framework ever
    says to them — and **any** string in a hand-written file can be an
    absolute path. ⭐ What the reader needs is the field and the permitted
    class; the value is in front of them already, in the file they just wrote.
    """


def _escape(pattern: str) -> str:
    """Name *how* a path leaves the source root, without reproducing it.

    ⛔ Three faults, named separately, because they are three different
    mistakes: an absolute path, a home-relative one, and one that climbs out
    with `..`. ⚠️ Reported in the order they are tested, so the sentence
    matches the branch a reader would go and look at.
    """
    if pattern.startswith("/"):
        return "begins with a slash"
    if pattern.startswith("~"):
        return "begins with a tilde"
    return "climbs above the root with '..'"
