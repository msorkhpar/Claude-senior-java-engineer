"""The digest of every input a gate reading was taken over, and whether it has drifted.

**What it does.** Digests the files a gate suite read — the statement, the
starter, the reference, the tests, each plant — and the passages of the source
an exercise was built from, and answers whether either has moved since the
reading was taken.

**How you use it.**

    from studyforge.exercise.gates.digests import Cited, Input, digest_of_bytes, taken_over

    inputs = taken_over(root, (("starter", "starter.py"), ("tests", "test_x.py")))
    drifted(root, inputs)        # every input whose file no longer digests the same

**Depends on.** `hashlib` and `pathlib`, `studyforge.describe` for R7,
`studyforge.exercise.errors` for the one exception, and
`studyforge.exercise.safety` for what a path inside a bundle may be. Standard
library only.

## ⛔ Two kinds of input, because they are re-read from two different places

| what it is | where it lives | what re-reading it means |
|---|---|---|
| an **`Input`** | in the bundle, beside the record | ⭐ re-digested, and a mismatch refuses |
| a **`Cited` passage** | in the SOURCE it was built from | ⛔ compared against the **ledger** |

⚠️ **Collapsing the two would make gate `G5` unreadable.** An `origin` names a
file the framework may not even have — a corpus's source repository — and the
ledger is what carries its digest. ⭐ So a `Cited` entry records what
the source digested to **when the exercise was authored**, and `G5` asks the
ledger what it digests to now.

## ⛔ The role is a TOKEN and the set of roles is OPEN, deliberately

⭐ **This is the half of the record a second gate family extends**.
`code` writes `statement`, `starter`, `reference`, `tests` and one
`plant:<case id>` per edge; a quiz family writes one cited passage per
question. ⛔ Closing the set here would mean every new family editing this
module, which is exactly the seam `families` exists to avoid. ⚠️ **What is
closed is the SHAPE** — three keys for an input, four for a cited passage, a
role that is a printable token and a digest this build can read — so an
unforeseen role is carried and an unforeseen *shape* is refused.

## ⛔ One algorithm, named in the value

⭐ The digest is written `sha256:<64 hex characters>`, so a record says which
algorithm it was taken with rather than leaving a reader to count characters.
⛔ The set of algorithms is closed at one: a second entry is a reader here and
a line in `ALGORITHMS`, and until somebody writes it an unknown prefix is
refused rather than trusted.
"""

from __future__ import annotations

import hashlib
import re
from dataclasses import dataclass
from pathlib import Path

from studyforge.describe import describe
from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.safety import require_path

#: The one digest this build takes and reads. ⛔ Closed at one.
SHA256 = "sha256"

#: ⛔ Closed. A prefix this build cannot compute is refused, never trusted.
ALGORITHMS = (SHA256,)

#: What a written digest looks like: the algorithm, a colon, and its hex.
DIGEST = re.compile(r"\A(?P<algorithm>[a-z0-9]+):(?P<hex>[0-9a-f]+)\Z")

#: How many hex characters each algorithm's digest carries.
DIGEST_WIDTH = {SHA256: 64}

#: Said in a refusal instead of the value (R7), for `safety`'s reason: the
#: message tells an author what to write and the value is corpus data.
DIGEST_PERMITTED = "'<algorithm>:<hex>', the algorithm one of " + ", ".join(ALGORITHMS)

#: Said in a refusal instead of the value (R7). ⚠️ As wide as a case id, because
#: the `code` family's plant roles carry one: `plant:<case id>`.
ROLE_PERMITTED = "ASCII letters, digits and '. _ - : # $ / @ + = , ( ) [ ]', with no whitespace"

#: The keys of one input, in the order a record writes them (R10).
INPUT_KEYS = ("role", "path", "digest")

#: The keys of one cited passage. ⚠️ `section` is the second half of one
#: declared field and is `null` when the whole file is cited; it is written
#: either way, so a round trip does not depend on which shape was authored.
CITED_KEYS = ("role", "path", "section", "digest")

#: How the `code` family names the passage an exercise's own `origin` cites.
#: ⭐ Named here rather than in `code` because `record` prints it in a refusal.
ORIGIN_ROLE = "origin"


@dataclass(frozen=True, slots=True)
class Input:
    """One file the gate reading was taken over, and what it digested to then."""

    role: str
    path: str
    digest: str


@dataclass(frozen=True, slots=True)
class Cited:
    """One passage of the source an exercise was built from, and its digest then.

    ⛔ `path` is the source's path as the ledger spells it, so `G5` can look it
    up; `section` is the heading that bounds the region, or `None` for a whole
    file.
    """

    role: str
    path: str
    section: str | None
    digest: str


def digest_of_bytes(data: bytes) -> str:
    """Return `sha256:<hex>` for these bytes — ⭐ the one place the algorithm is named.

    ⚠️ **Not `digest_of`, and the name is not a preference.** That name is the
    `narrate.speakable` clip minter's and belongs to exactly one module in this
    tree, asserted over `src/` by that package's own naming test.
    """
    return f"{SHA256}:{hashlib.sha256(data).hexdigest()}"


def digest_of_file(root: Path, path: object, where: str) -> str:
    """Digest one file inside a bundle, naming the file rather than the machine (R7).

    ⛔ **The path is checked BEFORE it is quoted, and that order is the R7
    rule.** `require_path` refuses an absolute path, a `..` and a backslash
    without reproducing the value, so by the time the sentence below names a
    file the value cannot carry a home directory. ⚠️ **MEASURED** by
    `tests/test_emission.py::test_no_refusal_reproduces_the_value_it_refused`,
    which poisoned this parameter and read the home path straight back out
    while the check was missing.
    """
    inside = require_path(path, "an input's path", where)
    try:
        return digest_of_bytes((root / inside).read_bytes())
    except OSError:
        raise ExerciseError(
            f"{where}: the input at '{inside}' could not be read, so no digest can be "
            f"taken over it. A gate record states what it was taken over, and a file "
            f"that is not there is a reading that was never taken."
        ) from None


def taken_over(root: Path, files: tuple[tuple[str, str], ...], where: str) -> tuple[Input, ...]:
    """Digest each `(role, path)` in the order given, refusing a role or path that cannot be."""
    return tuple(_input(root, role, path, where) for role, path in files)


def _input(root: Path, role: object, path: object, where: str) -> Input:
    """One input, checked before it is read: the role, then the path, then the file."""
    named = require_role(role, where)
    inside = require_path(path, "an input's path", where)
    return Input(role=named, path=inside, digest=digest_of_file(root, inside, where))


def drifted(root: Path, inputs: tuple[Input, ...], where: str) -> tuple[str, ...]:
    """Every input whose file no longer digests to what the record says — one sentence each.

    ⭐ The answer is sentences and not a boolean, because a caller refusing a
    bundle has to be able to name the file (spec §7 §10).
    """
    return tuple(
        f"the input at '{entry.path}' has changed since the gates were run, so the "
        f"record's reading was taken over a file that is no longer there"
        for entry in inputs
        if digest_of_file(root, entry.path, where) != entry.digest
    )


def require_role(value: object, where: str) -> str:
    """Refuse a role a record could not carry, print or compare — ⛔ the shape, not the set."""
    if not isinstance(value, str) or not value or not _is_role(value):
        raise ExerciseError(
            f"{where}: an input's 'role' names what the file is to the exercise — "
            f"{ROLE_PERMITTED}. The value is {describe(value)}."
        )
    return value


def require_digest(value: object, where: str) -> str:
    """Refuse a digest this build cannot read, or one of the wrong width for its algorithm."""
    match = DIGEST.match(value) if isinstance(value, str) else None
    if match is None or match["algorithm"] not in ALGORITHMS:
        raise ExerciseError(
            f"{where}: a digest is written {DIGEST_PERMITTED}. The value is {describe(value)}."
        )
    width = DIGEST_WIDTH[match["algorithm"]]
    if len(match["hex"]) != width:
        raise ExerciseError(
            f"{where}: a {match['algorithm']!r} digest carries {width} hex characters "
            f"and this one carries {len(match['hex'])}. A truncated digest compares "
            f"equal to nothing and would refuse every bundle it was written into."
        )
    return value


def _is_role(value: str) -> bool:
    """Whether every character is one the permitted set names."""
    return all(
        letter.isascii() and (letter.isalnum() or letter in "._-:#$()[],+=@/") for letter in value
    )
