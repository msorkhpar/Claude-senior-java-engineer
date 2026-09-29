"""Which runtimes a corpus's material needs to run — the `runtimes` key.

**What it does.** Models `corpus.json`'s optional `runtimes` key: the set of
runtimes a runner must carry for this corpus's commands to run, named from a
closed vocabulary and held sorted.

**How you use it.** `parse_runtimes(value, exercises=…)`; an absent key yields
`NO_RUNTIMES`, the empty tuple, so no caller ever asks "did they declare one?".
`source_suffixes(runtimes)` is every suffix a declared set's code is written in.

**Depends on.** `errors`.

⭐ **The vocabulary is spelled ONCE, here**, and the package exports
it rather than any caller restating it. ⛔ **Its home is its own module,
never `document.py`**, which sat near its R11 bound: the key is a
split at the seam `media` already cut, one optional block per module.

## ⛔ Names, never versions

A corpus says *which* runtimes; the runner image's pin file says *which
version*. ⛔ A version here would be a second place a version is chosen, which
the runner image forbids — so an entry is a bare name and nothing else.

## ⭐ Absent means none, and none is a complete answer

⛔ **A corpus that declares no runtimes needs no runner and is complete at the
reading floor** (spec §7, C5, §11.0). The framework neither builds, probes nor
starts a container for it. `exercises: true` with no `runtimes` is permitted:
an ungraded corpus runs nothing.

## ⛔ What is refused, each by name

- an entry outside `RUNTIMES`, naming the vocabulary;
- a duplicate entry — a set is what a runner builds, and a repeated name is a
  mistake worth naming rather than folding;
- a name in `REQUIRES_JAVA` without `java` in the same list — nothing is
  inferred, the rule `corpus_api` follows: a declaration says what it means;
- the key at all beside `exercises: false` — it would declare a runner for a
  corpus that sets no runnable unit (§7).

⚠️ **Order carries no meaning and the parsed value is sorted**, so two equal
declarations are one value (R10). ⛔ **`runtimes` is `corpus_api: 4`'s key**
(`document.py`'s `KEY_VERSIONS`): an older build refuses the key by name and
blames the corpus for the framework's age, which is what R9 versions.
"""

from __future__ import annotations

from studyforge.corpus.manifest.errors import ManifestError
from studyforge.describe import describe

#: ⛔ **The closed vocabulary, sorted.** It agrees, name for name, with the
#: runtimes the runner image's pin file pins. ⚠️ `sqlite` is spelled for the
#: engine, deliberately: *SQL* names a language, not something an image can
#: carry, and a second engine is a second entry and a `corpus_api` rise.
#: ⭐ `shell` adds nothing to an image that already carries `bash`, and it is
#: still declarable, because the declaration is what lets a build assert it.
RUNTIMES = ("gradle", "java", "kotlin", "maven", "node", "python", "shell", "sqlite")

#: The source suffixes a declared **runtime** writes, by that runtime's name.
#: ⛔ **Keyed on `runtimes`, never on `variants`** — and the distinction is the
#: defect §4 records: a single module-level map that answered both *"can this be
#: filed here?"* and *"can we run it?"* left eight SQL courses unfileable. ⭐
#: `runtimes` is the declaration that IS about running (§7), so a map keyed on it
#: derives nothing from a filing key; it is `SOURCE_SUFFIXES`, not `LANGUAGES`,
#: for that reason (`tests/studyforge/corpus/manifest/test_document.py`).
SOURCE_SUFFIXES: dict[str, tuple[str, ...]] = {
    "java": (".java",),
    "kotlin": (".kt",),
    "node": (".js", ".mjs", ".cjs", ".ts"),
    "python": (".py",),
    "shell": (".sh", ".bash"),
    "sqlite": (".sql",),
}

#: The build tools and the language that run on a JVM: each is refused without
#: `java` beside it in the same list.
REQUIRES_JAVA = ("gradle", "kotlin", "maven")

#: ⭐ Returned when `runtimes` is absent. Stated as a value, so a test compares
#: against it rather than against an assumption.
NO_RUNTIMES: tuple[str, ...] = ()


def parse_runtimes(value: object, *, exercises: bool, present: bool = True) -> tuple[str, ...]:
    """Return the declared runtimes, sorted; an absent key yields `NO_RUNTIMES`.

    ⚠️ **`present` is the key's presence, not its value**, because an explicit
    `null` is a corpus that tried to declare something and wrote no list — it
    is refused like any other bad value, never read as *absent*.
    """
    if not present:
        return NO_RUNTIMES
    if not exercises:
        raise ManifestError(
            "'runtimes' is declared beside 'exercises: false'; a corpus that sets no "
            "runnable unit needs no runner, so it declares no runtimes (§7)"
        )
    if not isinstance(value, list):
        raise ManifestError(f"'runtimes' must be a list of names, got {describe(value)}")
    for position, entry in enumerate(value):
        if not isinstance(entry, str) or entry not in RUNTIMES:
            raise ManifestError(
                f"'runtimes[{position}]' must be one of {list(RUNTIMES)}, got {describe(entry)}"
            )
    repeated = sorted({entry for entry in value if value.count(entry) > 1})
    if repeated:
        raise ManifestError(f"'runtimes' names {repeated} more than once; each is named once")
    orphaned = [name for name in REQUIRES_JAVA if name in value and "java" not in value]
    if orphaned:
        raise ManifestError(
            f"'runtimes' declares {orphaned} without 'java'; each runs on a JVM, and "
            f"nothing is inferred — declare 'java' beside it"
        )
    return tuple(sorted(value))


def source_suffixes(runtimes: tuple[str, ...] | list[str]) -> tuple[str, ...]:
    """Return every source suffix the declared `runtimes` write, sorted, once each."""
    return tuple(sorted({one for name in runtimes for one in SOURCE_SUFFIXES.get(name, ())}))
