"""The authoring gates, and the one record that carries what they answered.

**What it does.** Answers spec §7's gates over an authored exercise and writes
the record that ships beside its bundle: the digest of every input the reading
was taken over, every passage of the source it was built from, and one verdict
per gate. ⛔ A bundle ships only if every gate held, and the record is what
`studyforge validate` re-reads afterwards.

**How you use it.**

    from studyforge.exercise.gates import Evidence, GateRecord, check, folded, taken_over

    def attempt(role, number):              # run the tests with `role` in place
        started = time.time()
        exit_code = run_them(role)          # in the pinned runner image
        return folded(exercise, root, role, number, exit_code, where, started=started)

    evidence = Evidence.taken(exercise, attempt, where)
    record = GateRecord(
        inputs=taken_over(root, files, where),
        origins=origins,
        verdicts=check(exercise, evidence, origins, ledger, where),
    )
    record.clears                           # did the bundle earn its place
    record_document(record)                 # what ships beside it

**Depends on.** `studyforge.exercise`'s own modules — `cases` for the case
vocabulary, `record` for `Exercise`, `report` for the fold, `safety` for what
a path may be, `errors` for the one exception — plus `hashlib` and `re`.
Standard library only. ⛔ **Not on `execute`**: that package
imports `studyforge.exercise`, so a gate reaching for it would make the two
circular; the runs arrive through `runs.Attempt` instead, which is also what
lets a suite be taken in the pinned runner image with nothing here knowing a
container exists.

## What is in the package

| Module | Owns |
|---|---|
| `families` | which gates exist, and how a second family registers itself |
| `digests` | the digest of every input, and whether one has drifted |
| `record` | `GateRecord` and `Verdict`: the document, complete or refused |
| `runs` | what one run of the tests reported, and the `Attempt` seam |
| `evidence` | every run a suite needs, derived from the exercise's own cases |
| `code` | `G1`–`G5`, and the `code` family |
| `derivation` | `D1`–`D2`, the `derivation` family: what an `authoritative` exercise records |
| `quiz` | ⭐ a SUB-PACKAGE: `Q1`–`Q5`, the `quiz` family, and its own contract |

## ⭐ THE SEAM — a second gate family shares this record and edits nothing here

⛔ **The quiz family lives in `gates/quiz/` inside this package and writes `Q1`–`Q5`
into THIS record**, rather than opening a second one. Three properties make
that work, and each is asserted rather than promised:

1. ⭐ **A family declares its own gates and registers itself**, and `families`
   holds the rules rather than a list of who exists. ⛔ **The only line a new
   family adds anywhere outside its own sub-package is the import in THIS
   file** — the one R17 already obliges it to add, because a parent contract is
   where a reader finds out a sub-package exists. ⚠️ **Discovery by
   `pkgutil` + `importlib` was written first and removed**: the framework
   reaches no module by name at run time
   (`tests/harness/test_isolation.py`, its run-time-import arm),
   and the rule is right — a registry populated by static imports is one a
   reader can enumerate by reading.
2. ⭐ **`record` is family-agnostic.** It requires every gate of every family a
   record names, refuses a gate id nothing declares, and derives the write
   order from the registry — so a quiz family's completeness is enforced on the
   day it registers, by code that has never heard of a quiz.
3. ⭐ **`digests` leaves the ROLE open and closes the SHAPE.** `code` writes
   `statement`, `starter`, `reference`, `tests` and one `plant:<case id>`; a
   quiz family writes one cited passage per question. ⚠️ An unforeseen role is
   carried; an unforeseen *shape* is refused.

⭐ **And `Verdict.recorded` is where a family's own evidence goes** — `Q1`–`Q3`
are model judgements taken once at authoring and shipped, so they carry a
prompt and an outcome that no mechanical gate has. ⛔ Nothing here reads
`recorded` to decide anything: `held` is the verdict.

⛔ **What the quiz family does NOT touch**: `families.py`, `record.py`, `digests.py`
and `runs.py` take no edit at all, and every completeness rule, refusal and
write-order reaches `Q1`–`Q5` from the moment they register.

## ⛔ NO GATE CAN BE DISABLED, SKIPPED OR WEAKENED BY CONFIGURATION

⭐ **R5's honesty as gates, and it is structural in four places rather than
stated in one:**

- ⛔ **`code.check` takes no options** and returns all five verdicts, always. A
  gate with nothing to read answers *did not hold*, never *held vacuously*.
- ⛔ **`Evidence.taken` derives its run list from the case map**, so there is
  no argument by which a caller asks for a suite missing a gate's evidence.
- ⛔ **`record_of` refuses an incomplete record**, a gate no family declares,
  and every key this build does not define — so a bundle inventing `"skip"` or
  `"enabled"` is refused rather than ignored.
- ⛔ **`GateRecord.clears` is derived** from the verdicts, so nothing can say a
  bundle cleared while carrying a gate that did not hold. ⚠️ And no module in
  this package reads an environment variable, a file outside the bundle, or any
  other thing an installation could set.

## ⭐ WHAT IS NOT HERE, AND WHOSE IT IS

⛔ **Running anything.** The suite is handed its runs (`runs.Attempt`), and
wiring that to the pinned runner image is the authoring skill's.
⛔ **The bundle on disk** and `validate`'s arm over this record are `bundle`'s —
`digests.drifted` is the reading that arm takes. ⛔ **The ledger** is the exercises skill's;
`G5` reads it as a mapping of source path to digest and knows nothing else
about it.
"""

from __future__ import annotations

from studyforge.exercise.gates.code import CODE, G1, G2, G3, G4, G5, check
from studyforge.exercise.gates.derivation import D1, D2, DERIVATION, TESTS, faults, hole, holes
from studyforge.exercise.gates.digests import (
    ALGORITHMS,
    CITED_KEYS,
    DIGEST,
    DIGEST_PERMITTED,
    DIGEST_WIDTH,
    INPUT_KEYS,
    ORIGIN_ROLE,
    ROLE_PERMITTED,
    SHA256,
    Cited,
    Input,
    digest_of_bytes,
    digest_of_file,
    drifted,
    require_digest,
    require_role,
    taken_over,
)
from studyforge.exercise.gates.evidence import (
    Evidence,
    declared_cases,
    edges,
    required_roles,
)
from studyforge.exercise.gates.families import (
    TOKEN_PERMITTED,
    Family,
    declared_order,
    family_of,
    register,
    registered,
)
from studyforge.exercise.gates.quiz import QUIZ
from studyforge.exercise.gates.record import (
    RECORD_KEYS,
    VERDICT_KEYS,
    GateRecord,
    Verdict,
    record_document,
    record_of,
)
from studyforge.exercise.gates.runs import (
    FIRST,
    PLANT,
    REFERENCE,
    SECOND,
    STARTER,
    Attempt,
    Run,
    folded,
    plant_role,
    require_run,
)

#: ⛔ The package's whole public surface. A consumer that has to import
#: `studyforge.exercise.gates.code` directly is a consumer this contract
#: failed — R17 makes the package's `__init__.py` its
#: contract, and this is what it says.
__all__ = [
    "ALGORITHMS",
    "Attempt",
    "CITED_KEYS",
    "CODE",
    "Cited",
    "D1",
    "D2",
    "DERIVATION",
    "DIGEST",
    "DIGEST_PERMITTED",
    "DIGEST_WIDTH",
    "Evidence",
    "FIRST",
    "Family",
    "G1",
    "G2",
    "G3",
    "G4",
    "G5",
    "GateRecord",
    "INPUT_KEYS",
    "Input",
    "ORIGIN_ROLE",
    "PLANT",
    "QUIZ",
    "RECORD_KEYS",
    "REFERENCE",
    "ROLE_PERMITTED",
    "Run",
    "SECOND",
    "SHA256",
    "STARTER",
    "TESTS",
    "TOKEN_PERMITTED",
    "VERDICT_KEYS",
    "Verdict",
    "check",
    "declared_cases",
    "declared_order",
    "digest_of_bytes",
    "digest_of_file",
    "drifted",
    "edges",
    "family_of",
    "faults",
    "folded",
    "hole",
    "holes",
    "plant_role",
    "record_document",
    "record_of",
    "register",
    "registered",
    "require_digest",
    "require_role",
    "require_run",
    "required_roles",
    "taken_over",
]
