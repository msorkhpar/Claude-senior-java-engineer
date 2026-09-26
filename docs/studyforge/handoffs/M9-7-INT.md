# M9-7-INT: the eight sections integrated

- **Branch:** `int/m9-onboard`. It was at `4a41e87` before the merges. The tip is the commit that adds this hand-off.
- **Library:** studyforge 0.1.0, built from `f72a1dde0fc04ba3131fcc31b50460b326dac858`. The wheel was built from a scratch clone at that commit and installed into a fresh scratch venv, following the README. The README's pinned `pytest` set was added to the same venv. `python3 -m studyforge.skills.onboarding.verify` reports that commit. Nothing was re-pinned.
- **Author line:** every commit is `po-int <po-int@example.invalid>`, passed with `git -c`. Nothing was pushed, and no remote was added or changed.
- **Narration:** none was made.

## Merge commits

The sections were merged in module order with `--no-ff`. Every section branch forked from `4a41e87`.

| commit | section | modules | conflicts |
|---|---|---|---|
| `f4644b23` | S1 `ab01d22` | 02-05 | none |
| `2d0b670a` | S2 `a83809e` | 06-10 | `exercises/ledger.json` |
| `9bdd4fb8` | S3 `9f501c27` | 11-16 | `exercises/ledger.json` |
| `1a833f1f` | S4 `dcffbed3` | 17-21 | `exercises/ledger.json` |
| `c532d433` | S5 `47a38b51` | 22-26 | `exercises/ledger.json` |
| `fb040db6` | S6 `0e27415` | 27-30 | `exercises/ledger.json` |
| `2c3571cc` | S7 `56192d6a` | 31-35 | `exercises/ledger.json` |
| `1dbfc4ec` | S8 `dcfc0ea` | 36-45 | `exercises/ledger.json` |

No other file conflicted. Git merged the shared built files, such as the indexes, without conflict. The first `build` then reproduced every one of them byte for byte, so nothing needed regenerating.

## How the ledger was reconciled

The exercises skill gives one route for merging ledgers: step 5 of the pass, `studyforge.skills.exercises.merge.merged(root, prior, fresh)`. A pass owns only the files it read. It replaces their rows and keeps every other row as it was, provided the file is still on disk.

A reuse pass through `author_corpus` needs every page's `Page` and its aspects. Those came from each section's driver, and the drivers were not committed. So each conflict was resolved by calling the library's `merged` directly:

- `prior` is the integration branch's committed ledger (`HEAD`).
- `fresh` is the section's own rows: every source and entry row whose file the base ledger at `4a41e87` did not carry.
- The result was written in the library's byte form. A check confirmed that form reproduces each section's committed ledger exactly.

Before each merge, the script checked two things. The section's rows for `01-java-basics` (the pilot) are byte-identical to the base in all eight sections. No section's `fresh` overlaps a file already merged. After each merge, the delta always read `changed: []` and `dropped: []`:

| merge | kept | added | sources | entries |
|---|---|---|---|---|
| S2 | 195 | 149 | 77 | 267 |
| S3 | 344 | 496 | 125 | 715 |
| S4 | 840 | 419 | 160 | 1099 |
| S5 | 1259 | 314 | 191 | 1382 |
| S6 | 1573 | 436 | 236 | 1773 |
| S7 | 2009 | 319 | 272 | 2056 |
| S8 | 2328 | 606 | 334 | 2600 |

That the reconciliation is correct is shown by `validate`: 0 findings, and 0 unchecked claims, so no page is `ledger-pending` anywhere.

## Gates at the tip

| gate | result |
|---|---|
| `python3 -m ingest . 2026-09-24` | GREEN, exit 0: `valid: 0 finding(s), 0 unchecked claim(s)`. The tree was clean afterwards. |
| `studyforge validate .` | GREEN, exit 0: `valid: 0 finding(s), 0 unchecked claim(s)`, with 0 `ledger-pending` pages. |
| `studyforge plan .` | GREEN, exit 0: 0 refusals. It lists 0 paths to create, 213 to replace, 13007 to keep and 664 claimed. |
| `studyforge build . --out .`, twice | GREEN, exit 0 both times. `git status` was clean after the first build and after the second. |
| generated tests, `python3 -m pytest tests` (basetemp in scratch) | GREEN, exit 0: 17 passed in 53 min 10 s. The tree was clean afterwards. |
| `hand_edited('.')` | GREEN: `[]` |
| host Maven build, `mvn -q -B -Dmaven.repo.local=<scratch>/m2 verify` (sdkman JDK 21.0.12.1) | GREEN, exit 0. The tree was clean afterwards. |

## Sample grading

One code practice was graded per section, 8 in all. Each was taken from its section's bundles; the practice gates were not re-run in full. For each practice, three roles were staged:

- the bundle's `build/pom.xml`, the role's `src`, and the bundle's tests;
- laid out at the path its `test_command` names;
- run with that command in a fresh `docker run --rm --init --network none` of `code-server-toolchain/runner:java-maven-amd64-efc527c25c20`.

Only one container ran at a time, and each was removed. The reference must pass, and the starter and the plant must fail.

| practice | reference | starter | plant `edge-1` |
|---|---|---|---|
| S1 `03-methods/prose/unit-01/practice-1` | 0, pass | 1 (3 run, 3 errors) | 1 (2 failures) |
| S2 `08-object-oriented/prose/unit-01/practice-1` | 0, pass | 1 (3 run, 3 errors) | 1 (1 failure) |
| S3 `13-lambda-expressions/prose/unit-01/practice-1` | 0, pass | 1 (4 run, 4 errors) | 1 (1 failure) |
| S4 `19-concurrency-pitfalls/prose/unit-01/practice-1` | 0, pass | 1 (1 failure, 2 errors) | 1 (1 failure) |
| S5 `23-executors/prose/unit-01/practice-1` | 0, pass | 1 (5 run, 5 errors) | 1 (3 failures) |
| S6 `29-date-time-api/prose/unit-01/practice-1` | 0, pass | 1 (2 failures, 1 error) | 1 (2 failures) |
| S7 `33-dry-principle/prose/unit-01/practice-1` | 0, pass | 1 (3 run, 3 errors) | 1 (1 error) |
| S8 `41-reflection/prose/unit-01/practice-1` | 0, pass | 1 (5 run, 5 errors) | 1 (1 error) |

All 8 grade as their gate records say. The merged tree still grades.

## Per-module counts, and speech units that need clips

*Practices* are code practices: bundles with no `tests/quiz.json`. *Quizzes* are bundles with one.

The last column counts speech units that the committed narration record (`.studyforge/narration.json`) has no clip for. Each was worked out read-only:

- the units come from `studyforge.cli.narrate.stage.survey`;
- each is sorted by `studyforge.narrate.synth.incremental.plan` under the record's own conditions;
- nothing was sent to any service.

Every one of these units is practice text (`practice-prose`) with the reason `no record of this unit`. All 12839 lesson units are fresh, and the walk was whole.

| module | section | practices | quizzes | speech units needing clips |
|---|---|---|---|---|
| `01-java-basics` | pilot | 23 | 4 | 251 |
| `02-control-flow` | S1 | 27 | 5 | 292 |
| `03-methods` | S1 | 18 | 4 | 183 |
| `04-str-literals` | S1 | 21 | 5 | 202 |
| `05-pattern-matching` | S1 | 17 | 4 | 182 |
| `06-class-obj` | S2 | 20 | 4 | 272 |
| `07-encapsulation` | S2 | 14 | 3 | 178 |
| `08-object-oriented` | S2 | 15 | 3 | 196 |
| `09-records` | S2 | 15 | 3 | 191 |
| `10-sealed` | S2 | 15 | 3 | 255 |
| `11-try-catch` | S3 | 21 | 4 | 256 |
| `12-custom-exception` | S3 | 22 | 4 | 249 |
| `13-lambda-expressions` | S3 | 24 | 4 | 314 |
| `14-functional-interfaces` | S3 | 24 | 4 | 266 |
| `15-method-references` | S3 | 17 | 3 | 228 |
| `16-streams-api` | S3 | 31 | 5 | 362 |
| `17-java-memory-model` | S4 | 7 | 3 | 109 |
| `18-happens-before` | S4 | 6 | 2 | 90 |
| `19-concurrency-pitfalls` | S4 | 19 | 4 | 252 |
| `20-thread-basics` | S4 | 18 | 3 | 207 |
| `21-synchronization` | S4 | 8 | 2 | 121 |
| `22-locks-semaphores` | S5 | 9 | 2 | 134 |
| `23-executors` | S5 | 11 | 2 | 159 |
| `24-concurrent-collections` | S5 | 17 | 4 | 207 |
| `25-fork-join` | S5 | 18 | 5 | 224 |
| `26-virtual-threads` | S5 | 9 | 2 | 121 |
| `27-modern-java-overview` | S6 | 20 | 5 | 267 |
| `28-enhanced-enums` | S6 | 25 | 5 | 393 |
| `29-date-time-api` | S6 | 35 | 8 | 491 |
| `30-text-blocks` | S6 | 21 | 5 | 301 |
| `31-solid-principles` | S7 | 11 | 5 | 136 |
| `32-kiss-principle` | S7 | 10 | 3 | 129 |
| `33-dry-principle` | S7 | 11 | 4 | 177 |
| `34-composition-inheritance` | S7 | 8 | 3 | 95 |
| `35-fail-fast-safe` | S7 | 11 | 3 | 128 |
| `36-creational-patterns` | S8 | 15 | 3 | 199 |
| `37-structural-patterns` | S8 | 15 | 3 | 216 |
| `38-behavioral-patterns` | S8 | 16 | 3 | 231 |
| `39-data-structures` | S8 | 12 | 2 | 164 |
| `40-memory-management` | S8 | 6 | 2 | 79 |
| `41-reflection` | S8 | 19 | 4 | 283 |
| `42-annotations` | S8 | 21 | 4 | 288 |
| `43-jvm-internals` | S8 | 8 | 4 | 134 |
| `44-java-security` | S8 | 15 | 3 | 197 |
| `45-java-persistence` | S8 | 18 | 3 | 235 |
| **total** | | **743** | **163** | **9644** |

The sections' figures match. The pilot has 23 practices, and the sections have 83, 79, 139, 58, 64, 101, 51 and 145, for 743 in all. The quizzes are 4 + 18 + 16 + 24 + 14 + 15 + 23 + 18 + 31 = 163.

## Findings

- **F1. No documented way to integrate ledgers from sections authored in parallel.** The skill merges ledgers only inside a pass. A reuse pass needs the `Page` objects and aspects each section built in a driver it did not commit. From committed files alone, the only route was to call `exercises.merge.merged` directly. Writing the result in the library's byte form also needed the private `exercises.corpus._document_bytes`. The skill should document an integration route, or `validate` should offer one, perhaps as a `studyforge ledger merge`.
- **F2. No way to count clips owed without requesting them.** `studyforge narrate` has no dry-run or plan mode. `studyforge plan` reports media directories but not speech units. The count above needed `cli.narrate.stage.survey`, `synth.incremental.plan`, and the private `synth.record._conditions_of` fed the record's `conditions` block.
- **F3. The pilot's practice text was never narrated either.** `01-java-basics` has 251 practice speech units with no clip. So the practice-text gap covers all 45 modules, not only the sections' modules.
- **F4. The brief's expected total was stated as about 587.** The per-section figures it lists add up to 743, and that is exactly what the tree holds.
- **F5. The generated tests take 53 minutes on the whole course.** There are 17 tests. The three that copy the corpus take about 64 MB of temporary space each.
