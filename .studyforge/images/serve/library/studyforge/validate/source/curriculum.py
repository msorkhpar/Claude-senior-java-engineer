r"""The manifest's `curriculum` declaration, held to the tree by `validate` itself.

**What it does.** When `corpus.json` declares its record's groups, refuses a
tree that disagrees: a missing record, labels that are not the record's own, an
ordinal out of place, or a filename prefix that partitions the files differently
from the record.

**How you use it.** `check_curriculum(walk)`, drained by `validate.run` through
this package's `CHECKS`. It yields `Finding`s.

**Depends on.** `enumeration` for the files the corpus holds — its ignore rules
read, a build's output set aside — and the adapter skill's
`curriculum.filed`, which does the filing and names every disagreement.
⛔ **One filing, not two**: the rules live once, where an adapter reads them.

## ⛔ Why `validate` runs it, and not only the adapter

⚠️ A declaration enforced only by the adapter that reads it is enforced for a
scaffolded adapter and never for a hand-written one. ⭐ `validate` is the
adapter's definition of done (R2), so a stale declaration is refused there,
whoever wrote `read.py`.

⭐ **The population is `validate`'s own**: the files `source_files` returns and
the manifest includes. A file the repository ignores is not material here and
is not material to the filing either.
"""

from __future__ import annotations

from collections.abc import Iterator

from studyforge.corpus.manifest import MANIFEST_FILENAME, Classification
from studyforge.validate.corpus import Walk
from studyforge.validate.report import Finding
from studyforge.validate.source.enumeration import source_files

#: ⛔ The tree does not say what `curriculum` declares. Its own rule id, because
#: the fix is in the record or in `corpus.json`, never in `content`.
RULE_CURRICULUM = "curriculum-disagrees"


def check_curriculum(walk: Walk) -> Iterator[Finding]:
    """Refuse a declared curriculum the record or the names on disk disagree with."""
    manifest = walk.manifest
    if manifest is None or manifest.curriculum is None or not manifest.curriculum.containers:
        return
    # ⚠️ Deferred: the adapter skill reads this package's `source_files`.
    from studyforge.skills.adapter import CurriculumDisagrees, filed

    included = {
        where
        for where in (walk.relative(path) for path in source_files(walk.root).files)
        if manifest.content.classify(where) is Classification.INCLUDED
    }
    try:
        filed(walk.root, manifest, included=included)
    except CurriculumDisagrees as refused:
        yield Finding(RULE_CURRICULUM, MANIFEST_FILENAME, str(refused))
