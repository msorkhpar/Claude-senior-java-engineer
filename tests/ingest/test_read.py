"""What this source records: containers, each with an address it was given.

**What it does.** Asserts that reading this repository yields containers, and that each one records its address and its origin rather than deriving them (§6).

**How you use it.** `python3 -m pytest tests/ingest` from the corpus root.

**Depends on.** `ingest.read`, and nothing else — this is the step before emission.
"""

from __future__ import annotations


from pathlib import Path

from ingest import read

CORPUS_ROOT = Path(__file__).resolve().parents[2]


def test_this_source_records_at_least_one_container():
    # ⛔ Until read.containers is written this fails with the refusal's own
    # message, which names what to return. That failure IS the next step.
    found = read.containers(CORPUS_ROOT)
    assert found, (
        "read.containers returned nothing; an empty corpus is a silent failure"
    )


def test_every_container_records_its_address_rather_than_deriving_one():
    for container in read.containers(CORPUS_ROOT):
        assert container.address.segments, (
            "a container reached emission with no address; §6 says an address is recorded"
        )
        assert container.origin, (
            f"{container.address.key} records no origin. It is the permanent link "
            f"from every generated page back into the material this was read from."
        )


def test_every_unit_is_numbered_and_titled():
    for container in read.containers(CORPUS_ROOT):
        for unit in container.units:
            assert unit.n >= 1, f"{container.address.key} declares a unit with no ordinal"
            assert unit.title.strip(), (
                f"{container.address.key} unit {unit.n} has no title; a unit with "
                f"no title reaches the contents page as a blank row"
            )
