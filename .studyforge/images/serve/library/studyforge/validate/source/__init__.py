r"""The two checks that read the material, not only what the adapter wrote.

**What it does.** Refuses a file the manifest classifies as neither included
nor excluded, and — the check this package exists for — **counts a structural
feature directly in the raw source and compares it against the archive**.

**How you use it.** `CHECKS`, which `validate.run` drains like every other
check's tuple; or `check_unclassified(walk)` and `check_completeness(walk)` by
name. Both yield `Finding`s and `Unchecked`s.

**Depends on.** `corpus.manifest` for the classification, `validate.headings`
for what a heading is and where a region ends, `validate.corpus`,
`validate.report`. ⛔ **Not `archive.markdown`**, ever — and
`tests/studyforge/validate/source/test_init.py` asserts that of **every**
module in this package rather than of one, because any of them could break
the rule.

| module | what it answers |
|---|---|
| `classification` | ⭐ what the corpus says its files **are** — material, output, or neither |
| `enumeration` | ⭐ what the corpus root **holds**, which `classification` judges |
| `completeness` | ⭐ what one file **contains**, counted without the parser that read it |
| `curriculum` | ⭐ whether the tree still says what `corpus.json`'s `curriculum` declares |
| `membership` | ⭐ what the archive root holds, and what the archive declares and does not hold |

## ⛔ Why the seam is here

⭐ **Classification and completeness are two concerns.** Classification is the
third state and the corpus's own declaration of what is generated output;
completeness is regions, sections and the two rule ids a section needs.
⚠️ **Neither uses a line of the other**, and the heading machinery they share
lives in `validate.headings`.

⭐ **Nothing crosses the seam.** No name defined in `classification` is read by
`completeness` or the other way round; what they share is `Walk`, `Finding`
and `Unchecked`, which every check in `validate` shares. ⚠️ `enumeration` is
`classification`'s own walk, split out at R11's bound: the one edge
inside the package, and it points one way.

## ⚠️ What the split does NOT buy, said because a guarantee does not extend to what sits beside it

⛔ **The two halves can still both be wrong about the same corpus**, and
nothing here cross-checks them: a file swept into the wrong state by
`classification` is not a file `completeness` will notice, and never was.
⭐ What the split buys is narrower and worth stating exactly — **a change to
one half cannot silently reach the other**, and each half's tests name the
module they are about.
"""

from __future__ import annotations

from studyforge.validate.source.classification import (
    RULE_CONTESTED,
    RULE_IGNORE_DECLARATION,
    RULE_INCLUDED_UNREAD,
    RULE_NESTED_REPOSITORY,
    RULE_UNCLASSIFIED,
    check_unclassified,
)
from studyforge.validate.source.completeness import (
    RULE_ORIGIN_MISSING,
    RULE_SECTION_AMBIGUOUS,
    RULE_SECTION_MISSING,
    RULE_SHORT_READ,
    check_completeness,
)
from studyforge.validate.source.curriculum import RULE_CURRICULUM, check_curriculum
from studyforge.validate.source.enumeration import (
    IGNORE_TIMEOUT,
    REPOSITORY_STORE,
    SKIP_DIRS,
    Scan,
    repository_ignores,
    source_files,
)
from studyforge.validate.source.membership import (
    RULE_ARCHIVE_STRAY,
    RULE_MEDIA_MISSING,
    archive_members,
    check_archive_members,
    check_declared_files,
)

#: The five checks, in the order a report reads best — what the archive root
#: holds, then what the archive SAYS it holds and does not, then what the files
#: beside it **are**, then whether they are filed as declared, then what one **contains**.
#: ⛔ `validate.run` splices this tuple into its own, so the order here is the
#: order in the report.
CHECKS = (
    check_archive_members,
    check_declared_files,
    check_unclassified,
    check_curriculum,
    check_completeness,
)

#: ⛔ The package's whole public surface. A consumer that has to import
#: `studyforge.validate.source.completeness` directly is a consumer this
#: contract failed. ⚠️ The rule ids are on it deliberately: a script that
#: filters a report by rule needs the constant rather than the string, and
#: that is what stopped `contested` and `ignore-declaration` being folded into
#: `unclassified` in the first place. ⭐ `REPOSITORY_STORE` is on it for the survey,
#: which names the store `validate` refuses by the same name.
__all__ = [
    "CHECKS",
    "IGNORE_TIMEOUT",
    "REPOSITORY_STORE",
    "RULE_ARCHIVE_STRAY",
    "RULE_CONTESTED",
    "RULE_CURRICULUM",
    "RULE_IGNORE_DECLARATION",
    "RULE_INCLUDED_UNREAD",
    "RULE_MEDIA_MISSING",
    "RULE_NESTED_REPOSITORY",
    "RULE_ORIGIN_MISSING",
    "RULE_SECTION_AMBIGUOUS",
    "RULE_SECTION_MISSING",
    "RULE_SHORT_READ",
    "RULE_UNCLASSIFIED",
    "SKIP_DIRS",
    "Scan",
    "archive_members",
    "check_archive_members",
    "check_completeness",
    "check_curriculum",
    "check_declared_files",
    "check_unclassified",
    "repository_ignores",
    "source_files",
]
