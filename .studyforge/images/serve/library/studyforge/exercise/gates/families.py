"""Which gates exist, who declares them, and how a second family joins without editing this.

**What it does.** Holds the one registry of gate families. A family is a name
and the ordered gate ids it declares — `code` declares `G1`–`G5`, and a second
family declares its own — and this module answers which family a gate id
belongs to, in which order a record writes them, and refuses a family that
collides with one already registered.

**How you use it.**

    from studyforge.exercise.gates.families import Family, register, registered

    MY_FAMILY = Family("quiz", ("Q1", "Q2", "Q3", "Q4", "Q5"))
    register(MY_FAMILY)          # at your module's import, once

    registered()                 # every family, in a fixed order
    family_of("G3")              # the family that declares it, or None

**Depends on.** `studyforge.describe` for naming a value without reproducing
it (R7) and `studyforge.exercise.errors` for the one exception this package
raises. Standard library only.

## ⭐ THE SEAM: a family declares its own gates, and nothing here is edited

⛔ **A second gate family adds NO line to this module, to `record`, or to
`digests`**: it declares a `Family`, calls `register` at its own
import, and every completeness rule, every refusal and the whole write order
reach it from that moment. ⭐ **What this module owns is the rules; what a
family owns is its gates**, and the two never have to be edited together.

⚠️ **Registration happens at import, and the import is STATIC.** The one line
that makes a family exist is in the package's own contract,
`studyforge/exercise/gates/__init__.py` — where R17 requires the parent to name
a sub-package anyway, so it is a line a new family owes regardless.
⛔ **Discovery was written first and REMOVED**: it walked this package with
`pkgutil` and `importlib.import_module`, and
`tests/harness/test_isolation.py`'s run-time-import arm
refuses that for the whole framework — a run-time import is the door a closed
set of imports cannot see. ⭐ **The rule is right and the loss is one line**: a
registry populated by static imports is one a reader can enumerate by reading,
which is strictly better than one only a run can answer for.

## ⛔ The order is DERIVED, so two records of the same run are the same bytes

⭐ **Families are ordered by name and gates by the order their family declares
them** (R10). Nothing depends on the order they were imported in: the order a
record is written in is computed from the names, so a contract that listed its
imports differently would not move a byte.

## ⛔ A collision is refused, and that is the only way a gate is ever weakened

⚠️ **The interesting attack on a gate suite is not deleting a gate — it is
REDECLARING one.** A second family that declared `G3` could answer for it with
a weaker rule, and every reader of the record would still see a `G3` that held.
⛔ So a gate id belongs to exactly one family for the life of the process, a
second registration of the same family name is refused, and re-registering a
family with a different gate list is refused too. ⭐ Registration is additive
and never replaces.
"""

from __future__ import annotations

from dataclasses import dataclass

from studyforge.describe import describe
from studyforge.exercise.errors import ExerciseError

#: What a family name and a gate id may be. ⛔ A permitted set, and
#: narrow: these are written into a record, printed in a refusal and compared
#: byte for byte, so they carry no whitespace and nothing a path could hide in.
TOKEN_PERMITTED = "ASCII letters, digits, '.', '_' and '-', with no whitespace"


#: The registry itself: family name to family, and gate id to family name.
#: ⛔ Module state, which is what makes it one registry rather than one per
#: caller — and it is append-only, which is what makes a gate unweakenable.
_REGISTRY: dict[str, Family] = {}
_OWNERS: dict[str, str] = {}


@dataclass(frozen=True, slots=True)
class Family:
    """One family of gates: its name, and the gates it declares, in order.

    ⛔ Frozen and validated at construction, because a family is registered
    once at import and every record written afterwards is measured against it.
    """

    name: str
    gates: tuple[str, ...]

    def __post_init__(self) -> None:
        """Refuse a family nothing could write a complete record for."""
        _require_token(self.name, "a gate family's name")
        if not self.gates:
            raise ExerciseError(
                "a gate family declares at least one gate. A family with none is a "
                "name that can never make a record incomplete, which is the only "
                "thing a family is for."
            )
        for gate in self.gates:
            _require_token(gate, "a gate's id")
        if len(set(self.gates)) != len(self.gates):
            raise ExerciseError(
                f"the gate family {self.name!r} declares one gate id more than once. "
                f"A record names each gate exactly once, so a repeated id is a "
                f"verdict that could be written twice and read once."
            )


def register(family: Family) -> Family:
    """Register a family, refusing one that collides with a gate already declared.

    ⭐ Returns the family, so a module can register at the point it defines it:
    `CODE = register(Family("code", (...)))`.
    """
    if not isinstance(family, Family):
        raise ExerciseError(f"only a Family is registered as one. The value is {describe(family)}.")
    existing = _REGISTRY.get(family.name)
    if existing is not None:
        if existing.gates != family.gates:
            raise ExerciseError(
                f"the gate family {family.name!r} is already registered declaring "
                f"different gates. A family is registered once and never replaced: "
                f"a second declaration could answer for a gate with a weaker rule "
                f"while every reader still saw the gate hold."
            )
        return existing
    for gate in family.gates:
        owner = _OWNERS.get(gate)
        if owner is not None:
            raise ExerciseError(
                f"the gate family {family.name!r} declares a gate id the family "
                f"{owner!r} already declares. A gate id belongs to exactly one "
                f"family, so a second claim on one is a gate that could be answered "
                f"for twice. The id is {describe(gate)}."
            )
    _REGISTRY[family.name] = family
    for gate in family.gates:
        _OWNERS[gate] = family.name
    return family


def registered() -> tuple[Family, ...]:
    """Every registered family, ordered by name — ⛔ never by the order they were imported."""
    return tuple(_REGISTRY[name] for name in sorted(_REGISTRY))


def family_of(gate: object) -> str | None:
    """Name the family that declares this gate id, or answer `None` if nothing does."""
    return _OWNERS.get(gate) if isinstance(gate, str) else None


def declared_order() -> tuple[tuple[str, str], ...]:
    """Every `(family, gate)` pair, in the order a record writes them (R10)."""
    return tuple((family.name, gate) for family in registered() for gate in family.gates)


def _require_token(value: object, what: str) -> None:
    """Refuse a name a record could not carry, print or compare."""
    if not isinstance(value, str) or not value or not _is_token(value):
        raise ExerciseError(f"{what} must be {TOKEN_PERMITTED}. The value is {describe(value)}.")


def _is_token(value: str) -> bool:
    """Whether every character is one the permitted set names."""
    return all(letter.isascii() and (letter.isalnum() or letter in "._-") for letter in value)
