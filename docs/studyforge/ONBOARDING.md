# Claude Senior Java Engineer Interview Preparation

This repository was onboarded by `studyforge`. Everything below is
generated from `corpus.json` and from what the build reads — edit the
manifest, not this file: a regeneration writes over it.

## What this corpus declares

- source: `claude-senior-java-engineer-interview-preparation`
- 2 container level(s): section, module
- variants: prose
- placement profile: `tree`
- graded practices: yes
- media: not committed

## Running it from a fresh clone

The framework is the `studyforge` library, installed into the Python that
runs these commands — never a submodule, and never a checkout this
repository reaches by path. This corpus is pinned to `studyforge` version
`0.1.0`, built from commit `a5a84efbfd89339d0f2d93b0593b4267df9187e6`.
The library is not published to a package index: build a wheel from the
framework at that commit and install it, for example with
`python3 -m pip install --no-index <the wheel>`.

Then, from this repository's root, these check that the installed
library is the pinned version, list the skill procedures it ships,
ingest with this corpus's adapter, check the archive, say what a
build would write before building, and run the checks onboarding
generated. Those need no test runner: each prints a line per check and
exits non-zero on a failure, and `python3 -m pytest tests` runs the
same checks where pytest is installed:

```
python3 -m studyforge.skills.onboarding.verify .
python3 -m studyforge.skills.documents
python3 -m ingest .
python3 -m studyforge.cli validate .
python3 -m studyforge.cli plan .
python3 -m studyforge.cli build . --out .
python3 tests/test_framework_pin.py
python3 tests/test_non_destructive.py
```

A skill's procedure is printed by naming it — `python3 -m studyforge.skills.documents onboarding`
is this corpus's onboarding procedure — and `.studyforge/skills/` names each
skill this corpus was pointed at.

The adapter stamps today's date as `ingested`; pass a date after `.` to
reproduce an earlier archive byte for byte. Narration needs a running
narration service, so it is not run here; its options are:

```
python3 -m studyforge.cli narrate --help
```

## Where it stands

How many units this corpus has, how many are narrated and whether any
needs a container are read from the archive and the narration record,
and they move whenever either does — narrating writes clips, ingesting
again rewrites the archive. Nothing rewrites this file when they move,
so it states no figure: this reads them as they are now.

```
python3 -m studyforge.skills.onboarding .
```

## What you get

This corpus declares graded practices, so it reaches the execution
track as well as the reading floor: pages, narration, contents,
navigation and progress offline, plus Run and Submit against a
pinned toolchain.

## Narration

Narration is optional: the site is complete without it. Pages, practices,
quizzes, contents and progress need no clip; the clips add the voice.
This corpus does not commit its clips; they are published as release
volumes of this repository. From the root of a clone,
`sh .studyforge/narration-release/restore.sh` downloads them, checks every volume against
its checksum, puts each clip where its page plays it, and deletes the
downloaded volumes; on Windows, `powershell -File .studyforge/narration-release/restore.ps1` does
the same. A private repository needs `GITHUB_TOKEN` set, or `gh auth login`.
The next page load plays them, with no rebuild. A site you built into
another directory keeps its own copies: build it again after restoring.

## The one file that is yours

- `ingest/read.py`

Every other file here is generated. A hand-edit to one is reverted the
next time onboarding runs, so a difference you need is a field the
manifest is missing — which is a finding, not an edit.

## What generation touches

Nothing that already exists. Every artifact is an addition, and
`tests/test_non_destructive.py` fails the build if that stops being true.
