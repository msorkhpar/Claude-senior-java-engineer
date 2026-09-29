r"""What the corpus root holds as material, asked of the repository and the plan.

**What it does.** Enumerates the corpus root: every file that is material rather
than output, and every nested repository store. It gives no verdict;
`classification` judges what it returns.

**How you use it.** `source_files(root)` returns a `Scan`. `repository_ignores` is
the one ignore reader, and `REPOSITORY_STORE` and `SKIP_DIRS` name what a walk
never enters.

**Depends on.** `corpus.placement`, `cli.plan` for what a build writes (deferred,
`_generated_output` says why), and `git` on the path. ⛔ **No other module of this
package**: `classification` reads this one, never the other way round.

## ⛔ What counts as material is the corpus's declaration, not this file's guess

⭐ **The repository already says which of its files are generated, and
`source_files` asks it.** Spec §11.2 clause 11 named `git status` from the
start; the code simply never implemented the definition it had been given, and
A framework-kept list of directory names to skip (`node_modules`,
`__pycache__`) is the framework knowing about ecosystems, which is R1 with the
sign flipped.

⚠️ **A real corpus's walk can be mostly its own declared output**, and
reporting all of it as unclassified material files the few genuinely withheld
files among a hundred that were never material — an audit nobody reads, the
exact failure `content.exclude`'s mandatory `why` exists to prevent.

⚠️ **And the number has no fixed point.** A generated directory grows by the
hour, so a framework carrying its own exclusion list is wrong again tomorrow. The declaration
moves with the corpus because it belongs to the corpus.

## ⛔ A build's own output is recognised from the plan, not ignored and not declared

⭐ **A build's pages and media are committed** (§5), so git does not
declare them output. `studyforge plan` already enumerates them from this
corpus's declarations, so the scan asks it: a planned file is recognised by
its exact path, and everything under a planned directory is recognised with it.
⚠️ No glob. The manifest refuses a leading-wildcard
`not_material` glob, because its correctness depends on which files happen not
to exist. A placed path doesn't. ⛔ A planned path the manifest includes as
material is `contested`, never resolved by precedence. A plan that refused
recognises nothing, and the report says so.

⛔ **A root that is not a git working tree gets the old walk and an
`Unchecked`** (`ignore-declaration`), never a guess. Same rule as the absent
source tree in `completeness`, for the same reason: a half-applied ignore rule
is a half-present input, and the dangerous half is the one that looks clean.
"""

from __future__ import annotations

import shutil
import subprocess
from dataclasses import dataclass
from pathlib import Path

from studyforge.corpus.placement import ARCHIVE_DIRNAME

#: The name of a repository's store, a directory or a submodule's gitfile.
REPOSITORY_STORE = ".git"

#: What a source scan never enters, **at the corpus root only**.
#:
#: ⛔ **These two are the framework's own and nobody else's.** `.studyforge` is
#: this tool's, and `.git` holds the declaration this module now reads rather
#: than guesses at. ⚠️ **Beneath the root neither is the framework's**: a
#: nested `.studyforge` is material, and a nested `.git` is refused by name
#: (`RULE_NESTED_REPOSITORY`). ⚠️ **The archive root is not here**: it
#: is skipped at the corpus root only, and `membership` refuses each file
#: beneath it that is not an archive member. A source's own nested `archive/`
#: is material.
#: No ecosystem's directory (`node_modules`, `__pycache__`) is here: that would
#: be the framework knowing about ecosystems it was told nothing about (R1), and
#: wrong for any corpus that uses another.
SKIP_DIRS = (".git", ".studyforge")

#: How long git is given to answer. ⚠️ A validator that hangs is worse than one
#: that says it could not check.
IGNORE_TIMEOUT = 30


@dataclass(frozen=True, slots=True)
class Scan:
    """What the corpus root holds as material, and whether it was asked.

    ⛔ **One value, because the two halves may never be read apart.** A caller
    that took the file list without `consulted` would report a hundred
    generated files as unclassified and print a clean-looking run; a caller
    that took `consulted` without the list would have nothing to classify. The
    fallback and the announcement of the fallback are the same fact.
    """

    files: tuple[Path, ...]
    consulted: bool
    #: ⭐ The files a build of this corpus writes, recognised by the plan and
    #: therefore not in `files`. Carried rather than dropped, so an instrument
    #: can print the population it judged.
    generated: tuple[Path, ...] = ()
    #: ⛔ False when the plan refused: nothing was recognised, which is not
    #: the same as nothing having been generated.
    planned: bool = True
    #: ⛔ Each nested repository store the repository does not declare as
    #: output, refused by name. Its contents are never in `files`.
    stores: tuple[Path, ...] = ()


def source_files(root: Path) -> Scan:
    """Every file in the corpus root that is material rather than output.

    ⭐ **The corpus already declares what is output, and this asks it.** Spec
    §11.2 clause 11 names `git status`; what this reads is the same
    declaration, one call, in the file every repository has. That is why it is
    R1-clean: no ecosystem is named here, and the answer arrives as data from
    the source rather than as a list of names the framework carries.

    ⚠️ **Where the root is not a git working tree the walk stands and the
    caller says so** — see `Scan`. Refusing a non-repository corpus is wrong
    (R2: an archive is a shippable artifact on its own), and quietly scanning
    everything is worse than either, because it looks like a clean run.
    """
    walked, stores = _walk(root)
    recognised = _generated_output(root, walked)
    generated = tuple(path for path in walked if recognised and path in recognised)
    candidates = [path for path in walked if not recognised or path not in recognised]
    declared = repository_ignores(root, candidates + stores)
    planned = recognised is not None
    if declared is None:
        return Scan(
            tuple(candidates),
            consulted=False,
            generated=generated,
            planned=planned,
            stores=tuple(stores),
        )
    files = tuple(path for path in candidates if path not in declared)
    refused = tuple(store for store in stores if store not in declared)
    return Scan(files, consulted=True, generated=generated, planned=planned, stores=refused)


def _generated_output(root: Path, candidates: list[Path]) -> frozenset[Path] | None:
    """Which of `candidates` a build of this corpus writes, by the plan's own enumeration.

    ⛔ **The plan's answer, never a list here**. A plan line
    ending in `/` is a directory whose contents a build or `narrate` fill, and
    every other line is a file — the plan's printed distinction, which
    `generate.footprint` reads the same way. ⚠️ Unlike a footprint, a unit's
    audio directory IS a prefix here: narrate's clips are generated media
    whoever wrote them.

    ⛔ **`None` when the plan refused.** An incomplete enumeration recognises
    nothing, and the caller says so.

    ⚠️ **The import is deferred, for `generate.footprint`'s reason:** `cli`
    imports the dispatcher, which imports this package.
    """
    from studyforge.cli.plan import plan_for

    plan = plan_for(root)
    if plan.refusals:
        return None
    files = {root / path for path in plan.paths if not path.endswith("/")}
    directories = [root / path.rstrip("/") for path in plan.paths if path.endswith("/")]
    return frozenset(
        path
        for path in candidates
        if path in files or any(path.is_relative_to(directory) for directory in directories)
    )


def _walk(root: Path) -> tuple[list[Path], list[Path]]:
    """Every file under `root` that is not the framework's own writing, and every nested store.

    ⛔ **`SKIP_DIRS` is asked of the first part only**: a nested
    `.studyforge` is walked like any directory. A nested `.git`, directory or
    gitfile, is returned as a store and never entered.
    """
    found: list[Path] = []
    stores: set[Path] = set()
    for path in sorted(root.rglob("*")):
        parts = path.relative_to(root).parts
        if parts[0] in SKIP_DIRS:
            continue
        if len(parts) > 1 and parts[0] == ARCHIVE_DIRNAME:
            # ⛔ Not silent: `membership` accounts for every file here.
            continue
        if REPOSITORY_STORE in parts:
            stores.add(root.joinpath(*parts[: parts.index(REPOSITORY_STORE) + 1]))
            continue
        if not path.is_file():
            continue
        if len(parts) == 1 and parts[0] == "corpus.json":
            continue
        found.append(path)
    return found, sorted(stores)


def repository_ignores(root: Path, candidates: list[Path]) -> frozenset[Path] | None:
    """Which of `candidates` the repository declares as generated output.

    ⭐ **Public because it is the ONE ignore reader, and a second
    caller imports it rather than keeping a list**: the scaffolded
    `test_emit` asks it which directories a working copy leaves behind.

    ⛔ **Git's own answer, never a reimplementation of it.** Ignore rules have
    precedence, negation, per-directory files, an index that makes a tracked
    file un-ignorable and a user's global configuration. A framework that
    reproduced four of those five would be wrong on the fifth, silently, in
    somebody else's repository.

    ⛔ **`None` means "not answered", and it is not the same as "nothing is
    ignored".** Fail-open is what this refuses: the caller turns
    `None` into an `Unchecked`, loudly and counted, exactly as an absent source
    tree is reported. An empty frozenset means git answered "none of them".

    ⚠️ One subprocess for the whole tree, never one per file — `--stdin` takes
    the batch, and `-z` is what makes a filename with a newline in it a
    pathname rather than two.
    """
    git = shutil.which("git")
    if git is None:
        return None
    try:
        names = [path.relative_to(root).as_posix() for path in candidates]
    except ValueError:
        # ⛔ R7: `relative_to`'s own message quotes both paths, and `root` is the
        # one input guaranteed to carry a home directory (the census).
        raise ValueError("a candidate is not beneath the root it was asked about") from None
    payload = "\0".join(names)
    try:
        result = subprocess.run(  # noqa: S603 - fixed argv, no shell
            [git, "check-ignore", "--stdin", "-z"],
            input=payload,
            capture_output=True,
            text=True,
            cwd=root,
            check=False,
            timeout=IGNORE_TIMEOUT,
        )
    except OSError, subprocess.SubprocessError:
        return None
    # 0: some path is ignored. 1: none is. 128: not a repository, or worse —
    # and "or worse" is exactly why an unexpected code is not read as "none".
    if result.returncode not in (0, 1):
        return None
    return frozenset(root / name for name in result.stdout.split("\0") if name)
