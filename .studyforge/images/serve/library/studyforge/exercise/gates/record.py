"""The gate record: what every gate answered, over which inputs, written whole or refused.

**What it does.** Reads and writes the record that ships beside an authored
bundle (spec §7 §10) — the digest of every input the reading was taken over,
every cited passage, and one verdict per gate — and refuses a record that is
missing a gate, names a gate nothing declares, or writes a key this build does
not define.

**How you use it.**

    from studyforge.exercise.gates.record import GateRecord, Verdict, record_of

    record = GateRecord(inputs=inputs, origins=origins, verdicts=verdicts)
    record.clears                       # did every gate hold
    document = record_document(record)  # what ships beside the bundle
    record_of(document, where)          # and back, byte for byte

**Depends on.** `families` for which gates exist, `digests` for what an input
is, `studyforge.version` for the version test, `studyforge.describe` for naming
a value without reproducing it (R7), and `studyforge.exercise.errors` for the
one exception. Standard library only.

## ⛔ THE RECORD IS VERSIONED, AND A RECORD WRITTEN BEFORE THE KEY HAS ONE RULE

⭐ **`gates_api` is the record's first key** (R9, R21): a corpus commits the
record and `validate` reads it back, so a build that does not speak its version
refuses it through `studyforge.version.check`, naming what it declares.

⚠️ **A corpus commits records written before the key.** ⭐ One closed rule:
a record with no `gates_api` whose keys are exactly `UNVERSIONED_KEYS` is read
as version 1, the shape it had; any other record without the key is refused,
naming it. Refusing them would force every exercise through its gates again.

## ⭐ ONE RECORD, AND A SECOND GATE FAMILY WRITES INTO IT

⛔ **The quiz gates share this record rather than opening a second one**,
and nothing in this module knows they exist. It reads the registry,
so the moment a family is registered its gates are ones this record *requires*,
its verdicts are ones this record *accepts*, and a record carrying only half of
them is refused — with no line here changed.

⭐ **What a family owns, and this module does not:** its gate ids, their order,
what each one means, and the keys of its own `recorded` evidence. ⭐ **What
this module owns:** that a record is complete, that its order is derived rather
than chosen, and that nothing in it can say a gate held when it did not.

## ⛔ COMPLETENESS IS THE PROPERTY, AND IT IS WHY THE ORDER IS DERIVED

⚠️ **A record that may omit a gate is not a record, it is a claim.** So:

- every gate of every family the record *names* is present, exactly once;
- the verdicts are written in `families.declared_order()` — family by
  name, then gate by its family's own order — so two records of one run are the same
  bytes (R10), and a record ordered any other way is refused;
- a gate id no registered family declares is refused, naming it;
- ⛔ **`clears` is DERIVED** — `all(verdict.held)` — so there is no field, no
  argument and no key by which a record with a failed gate can report that it
  cleared. ⚠️ A boolean stored beside the verdicts would be a second spelling
  of one claim, and the one that drifts is always the summary.

## ⛔ NOTHING HERE READS A CONFIGURATION, AND THAT IS THE POINT

⭐ **Spec §7 and R5, as gates: no gate may be disabled by
configuration.** There is no environment variable, no options argument, no
`skip` key and no `enabled` flag anywhere in this package — and because the
document's key sets are **closed**, a bundle that invents one is refused rather
than ignored. ⚠️ Ignoring an unknown key is how a gate gets turned off by
somebody who never had to be told no.

## ⭐ `recorded` is a family's own evidence, and it is free-form on purpose

⛔ **`Q1`–`Q3` are model judgements taken once at authoring** (spec §7 §7), and
what they must carry — the prompt, the pass, the outcome — is a quiz fact this
module has no business enumerating. ⭐ So a verdict carries `recorded`, ordered
text pairs whose keys are tokens and whose values are text, written in the
order the family wrote them. ⚠️ **It is evidence and never an input to the
answer**: `held` is the verdict, and nothing here reads `recorded` to decide
anything.
"""

from __future__ import annotations

from collections.abc import Iterable
from dataclasses import dataclass

from studyforge.describe import describe, describe_keys
from studyforge.exercise.errors import ExerciseError
from studyforge.exercise.gates.digests import (
    CITED_KEYS,
    INPUT_KEYS,
    Cited,
    Input,
    require_digest,
    require_role,
)
from studyforge.exercise.gates.families import declared_order, family_of, registered
from studyforge.exercise.safety import require_path
from studyforge.version import check

#: The record format's version, written as its first key.
GATES_API = 1

#: The key the version is written under.
VERSION_KEY = "gates_api"

#: The keys of the record itself, in the order it is written. ⛔ Closed, all
#: four required: a record with no `origins` writes an empty array, so a
#: reader never has to tell *no passages cited* from *the key was forgotten*.
RECORD_KEYS = (VERSION_KEY, "inputs", "origins", "gates")

#: ⛔ The whole shape of a record written before `gates_api` existed, which is
#: read as version 1 and is the only record read without the key.
UNVERSIONED_KEYS = ("inputs", "origins", "gates")

#: The keys of one verdict, in the order it is written. ⛔ Closed, all five
#: required — `recorded` is written as an empty object by a family with no
#: evidence to add, for `origins`' reason.
VERDICT_KEYS = ("id", "family", "held", "says", "recorded")


@dataclass(frozen=True, slots=True)
class Verdict:
    """One gate's answer: whether it held, and the one sentence saying why.

    ⛔ Frozen, and `says` is required whether the gate held or not. A gate that
    holds silently is a gate nobody can tell from one that did not run, and
    spec §7's *nothing is lost* turns on being able to tell.
    """

    id: str
    family: str
    held: bool
    says: str
    recorded: tuple[tuple[str, str], ...] = ()


@dataclass(frozen=True, slots=True)
class GateRecord:
    """Every gate's verdict, over every input the reading was taken over.

    ⛔ Frozen and derived: `clears` is read off the verdicts, so a record
    cannot say it cleared while carrying a gate that did not hold. ⚠️
    Constructing one by hand asserts nothing about completeness —
    `record_of(record_document(...))` is what checks that.
    """

    inputs: tuple[Input, ...] = ()
    origins: tuple[Cited, ...] = ()
    verdicts: tuple[Verdict, ...] = ()

    @property
    def clears(self) -> bool:
        """Did every gate hold? ⛔ Derived, never stored, never argued with.

        ⚠️ **A record with NO verdicts clears nothing**, and that arm is not
        pedantry: `all(())` is `True`, so without it the cheapest way to make a
        failed bundle read green would be to delete the gates that refused it.
        ⭐ `record_of` refuses such a document outright; this is the same answer
        for one built in memory.
        """
        return bool(self.verdicts) and all(verdict.held for verdict in self.verdicts)

    @property
    def refused_by(self) -> tuple[str, ...]:
        """The id of each gate that did not hold, in the order written."""
        return tuple(verdict.id for verdict in self.verdicts if not verdict.held)

    def verdict(self, gate: str) -> Verdict | None:
        """Return this gate's verdict, or `None` if the record carries none for it."""
        return next((entry for entry in self.verdicts if entry.id == gate), None)


def record_document(record: GateRecord) -> dict:
    """Return the record as the decoded object that ships beside the bundle (R10)."""
    return {
        VERSION_KEY: GATES_API,
        "inputs": [
            {"role": entry.role, "path": entry.path, "digest": entry.digest}
            for entry in record.inputs
        ],
        "origins": [
            {
                "role": entry.role,
                "path": entry.path,
                "section": entry.section,
                "digest": entry.digest,
            }
            for entry in record.origins
        ],
        "gates": [
            {
                "id": entry.id,
                "family": entry.family,
                "held": entry.held,
                "says": entry.says,
                "recorded": dict(entry.recorded),
            }
            for entry in record.verdicts
        ],
    }


def record_of(document: object, where: str) -> GateRecord:
    """Read a gate record, refusing every way one can be incomplete or invented."""
    _require_version(document, where)
    verdicts = _verdicts(document["gates"], where)
    _require_every_gate(verdicts, where)
    return GateRecord(
        inputs=_inputs(document["inputs"], where),
        origins=_origins(document["origins"], where),
        verdicts=verdicts,
    )


def _require_version(document: object, where: str) -> None:
    """Refuse a record this build does not speak, or one whose key set is not its shape."""
    unversioned = isinstance(document, dict) and VERSION_KEY not in document
    shape = UNVERSIONED_KEYS if unversioned else RECORD_KEYS
    if not isinstance(document, dict) or set(document) != set(shape):
        present = document if isinstance(document, dict) else {}
        missing = [key for key in RECORD_KEYS if key not in present]
        raise ExerciseError(
            f"{where}: a gate record is {list(RECORD_KEYS)}, all of them required and "
            f"nothing else. A key this build does not define is refused rather than "
            f"ignored, because ignoring one is how a gate gets switched off by "
            f"somebody who was never told no. It is missing {describe_keys(missing)}, "
            f"carries {describe_keys(_unknown(document, shape))} the record does not "
            f"define, and the value is {describe(document)}. Only a record with exactly "
            f"{list(UNVERSIONED_KEYS)} is read without {VERSION_KEY!r}, as version 1."
        )
    if not unversioned:
        check(VERSION_KEY, document[VERSION_KEY], {GATES_API}, where=where, error=ExerciseError)


def _inputs(value: object, where: str) -> tuple[Input, ...]:
    """Read the files the reading was taken over, in the order written."""
    entries = _array(value, "inputs", INPUT_KEYS, where)
    inputs = tuple(
        Input(
            role=require_role(entry["role"], f"{where}: an 'inputs' entry"),
            path=require_path(entry["path"], "an input's path", where),
            digest=require_digest(entry["digest"], f"{where}: an 'inputs' entry"),
        )
        for entry in entries
    )
    _require_distinct((entry.role for entry in inputs), "inputs", where)
    return inputs


def _origins(value: object, where: str) -> tuple[Cited, ...]:
    """Read the passages of the source the exercise was built from."""
    entries = _array(value, "origins", CITED_KEYS, where)
    origins = tuple(
        Cited(
            role=require_role(entry["role"], f"{where}: an 'origins' entry"),
            path=require_path(entry["path"], "a cited passage's path", where),
            section=_section(entry["section"], where),
            digest=require_digest(entry["digest"], f"{where}: an 'origins' entry"),
        )
        for entry in entries
    )
    _require_distinct((entry.role for entry in origins), "origins", where)
    return origins


def _verdicts(value: object, where: str) -> tuple[Verdict, ...]:
    """Read one verdict per gate, refusing a gate no registered family declares."""
    entries = _array(value, "gates", VERDICT_KEYS, where)
    return tuple(_verdict(entry, where) for entry in entries)


def _verdict(entry: dict, where: str) -> Verdict:
    """Read one gate's answer, refusing an id nothing declares and a claim with no sentence."""
    owner = family_of(entry["id"])
    if owner is None:
        raise ExerciseError(
            f"{where}: the record carries a verdict for a gate no registered family "
            f"declares, so nothing in this build knows what it answered. The "
            f"families are {[family.name for family in registered()]}."
        )
    if entry["family"] != owner:
        raise ExerciseError(
            f"{where}: a verdict names the family {describe(entry['family'])} for a "
            f"gate the family {owner!r} declares. A gate belongs to exactly one "
            f"family, and a record that says otherwise is answering for somebody "
            f"else's gate."
        )
    if not isinstance(entry["held"], bool):
        raise ExerciseError(
            f"{where}: a verdict's 'held' is true or false — did the gate hold. The "
            f"value is {describe(entry['held'])}."
        )
    return Verdict(
        id=entry["id"],
        family=owner,
        held=entry["held"],
        says=_says(entry["says"], where),
        recorded=_recorded(entry["recorded"], where),
    )


def _require_every_gate(verdicts: tuple[Verdict, ...], where: str) -> None:
    """⛔ Refuse a record missing a gate, repeating one, or ordering them by choice."""
    if not verdicts:
        raise ExerciseError(
            f"{where}: a gate record names no gate at all. ⛔ A bundle whose failed "
            f"gates were deleted would otherwise read as one that cleared, because "
            f"every gate in it held — and there were none."
        )
    named = {verdict.family for verdict in verdicts}
    expected = [pair for pair in declared_order() if pair[0] in named]
    written = [(verdict.family, verdict.id) for verdict in verdicts]
    if written != expected:
        raise ExerciseError(
            f"{where}: a gate record carries every gate of every family it names, "
            f"exactly once, in the order those families declare them — and this one "
            f"does not. A record that may omit a gate is a claim rather than a "
            f"record, and an order somebody chose is two records of one run with "
            f"different bytes. The order is {expected}."
        )


def _array(value: object, key: str, keys: tuple[str, ...], where: str) -> tuple[dict, ...]:
    """Read an array of objects, refusing one whose keys are not exactly `keys`."""
    if isinstance(value, str) or not isinstance(value, (list, tuple)):
        raise ExerciseError(
            f"{where}: '{key}' is an array of objects, each {list(keys)}. The value "
            f"is {describe(value)}."
        )
    for entry in value:
        if not isinstance(entry, dict) or set(entry) != set(keys):
            unknown = _unknown(entry, keys)
            raise ExerciseError(
                f"{where}: a '{key}' entry is {list(keys)}, all of them required and "
                f"nothing else. This one carries {describe_keys(unknown)} the entry "
                f"does not define, and it is {describe(entry)}."
            )
    return tuple(value)


def _unknown(value: object, keys: tuple[str, ...]) -> list[str]:
    """Return the keys this build does not define, or none when the value is not an object."""
    return [name for name in value if name not in keys] if isinstance(value, dict) else []


def _require_distinct(roles: Iterable[str], key: str, where: str) -> None:
    """⛔ Refuse two entries claiming one role: a digest recorded twice is one nobody checks."""
    written = list(roles)
    repeated = len(written) - len(set(written))
    if repeated:
        raise ExerciseError(
            f"{where}: '{key}' names {repeated} role more than once. Each role is one "
            f"file, so a repeated role is two digests for it and no answer about "
            f"which was read. The roles are not reproduced here, since a refusal never quotes a "
            f"value that may be personal."
        )


def _section(value: object, where: str) -> str | None:
    """Read a cited passage's heading, or `None` where the whole file is cited."""
    if value is None:
        return None
    if not isinstance(value, str) or not value.strip():
        raise ExerciseError(
            f"{where}: a cited passage's 'section' is the heading that bounds the "
            f"region, or null where the whole file is cited. The value "
            f"is {describe(value)}."
        )
    return value


def _says(value: object, where: str) -> str:
    """Refuse a verdict with nothing to say — a silent gate reads like one nobody ran."""
    if not isinstance(value, str) or not value.strip():
        raise ExerciseError(
            f"{where}: a verdict's 'says' is the one sentence stating what the gate "
            f"found, and it is required whether the gate held or not. The value is "
            f"{describe(value)}."
        )
    return value


def _recorded(value: object, where: str) -> tuple[tuple[str, str], ...]:
    """Read a family's own evidence: token keys, text values, in the order written."""
    if not isinstance(value, dict):
        raise ExerciseError(
            f"{where}: a verdict's 'recorded' is the family's own evidence — an "
            f"object of text, empty where a family records none. The value is "
            f"{describe(value)}."
        )
    for key, item in value.items():
        require_role(key, f"{where}: a 'recorded' key")
        if not isinstance(item, str):
            raise ExerciseError(
                f"{where}: a 'recorded' value is text, so a reader of the record "
                f"needs nothing but the record to read it. The value is "
                f"{describe(item)}."
            )
    return tuple(value.items())
