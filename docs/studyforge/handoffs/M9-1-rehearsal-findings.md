# Findings log — M9-1 rehearsal: onboarding the Java course on recommended answers (scratch only)

⛔ Written by the conversion before the run is declared done. Every finding asks: *could a skill have generated this?*

### M9-1/F1 `[structural]` — reconnaissance drafts include globs NN/*.md that sweep the 45 module contents pages the record links as groups, and includes CLAUDE.md from a link above the curriculum heading

  - *measured:* reconnaissance drafts include globs NN/*.md that sweep the 45 module contents pages the record links as groups, and includes CLAUDE.md from a link above the curriculum heading — 45 included-unread findings on first validate; CLAUDE.md drafted as material
  - *could a skill have generated this?* **yes** — a hole in reconnaissance

### M9-1/F2 `[structural]` — the curriculum block cannot declare a section > module > unit record whose module level is itself a linked entry

  - *measured:* the curriculum block cannot declare a section > module > unit record whose module level is itself a linked entry — declaring 45 modules refused: README.md groups its units under 10 label(s) and corpus.json declares 45
  - *could a skill have generated this?* **yes** — a hole in reconnaissance + adapter curriculum

### M9-1/F3 `[local]` — curriculum.containers[].address at depth 2 must be one 'a/b' string; a list is refused and corpus.md never says so

  - *measured:* curriculum.containers[].address at depth 2 must be one 'a/b' string; a list is refused and corpus.md never says so — address key must be a non-empty str, got a list
  - *could a skill have generated this?* **yes** — a hole in docs/authoring/corpus.md

### M9-1/F4 `[structural]` — a uniform multi-module repository needs 90 per-module not_material globs, each with its own reason; */pom.xml is refused

  - *measured:* a uniform multi-module repository needs 90 per-module not_material globs, each with its own reason; */pom.xml is refused — 99 globs drafted, 101 reasons demanded
  - *could a skill have generated this?* **yes** — a hole in onboarding + manifest

### M9-1/F5 `[local]` — onboarding step 2 fences onboard(draft) without reasons= and no fence shows survey(...).proposal

  - *measured:* onboarding step 2 fences onboard(draft) without reasons= and no fence shows survey(...).proposal — PromotionRefused on the fenced command
  - *could a skill have generated this?* **yes** — a hole in onboarding

### M9-1/F6 `[local]` — the skills run python3 -m pytest but the README install route installs no pytest

  - *measured:* the skills run python3 -m pytest but the README install route installs no pytest — No module named pytest
  - *could a skill have generated this?* **yes** — a hole in README + adapter step 4

### M9-1/F7 `[structural]` — the personal-data gate refuses sample addresses in code examples, reserved example domains included, with no manifest exception

  - *measured:* the personal-data gate refuses sample addresses in code examples, reserved example domains included, with no manifest exception — 17 blocks in 13 of 166 lesson files refused; x@example.invalid refused
  - *could a skill have generated this?* **open**

### M9-1/F8 `[local]` — validate's short-read heading count disagrees with a CommonMark reading at a 4-space fence inside a list item

  - *measured:* validate's short-read heading count disagrees with a CommonMark reading at a 4-space fence inside a list item — source carries 10 heading line(s), archive 11 (03-methods unit-02)
  - *could a skill have generated this?* **open**

### M9-1/F9 `[structural]` — a list item cannot hold a code block; about 695 code blocks sit inside list items in this course

  - *measured:* a list item cannot hold a code block; about 695 code blocks sit inside list items in this course — adapter closes the list and continues it with start
  - *could a skill have generated this?* **open**

### M9-1/F10 `[local]` — no Markdown reader ships and the scaffold never names studyforge.address for Address

  - *measured:* no Markdown reader ships and the scaffold never names studyforge.address for Address — a 250-line reader written by hand
  - *could a skill have generated this?* **yes** — a hole in adapter

### M9-1/F11 `[local]` — code blocks render with font ligatures: != shows as a not-equal sign

  - *measured:* code blocks render with font ligatures: != shows as a not-equal sign — screenshot of 1.1.1
  - *could a skill have generated this?* **yes** — a hole in page stylesheet

### M9-1/F12 `[local]` — index progress strip overflows at 45 containers; the depth-2 rail lists modules without sections

  - *measured:* index progress strip overflows at 45 containers; the depth-2 rail lists modules without sections — screenshot of index
  - *could a skill have generated this?* **yes** — a hole in index page

### M9-1/F13 `[structural]` — tree placement cannot keep every generated file under .studyforge/: corpus.json, archive/, index.html, ingest/, tests/ and the reader document sit outside it

  - *measured:* tree placement cannot keep every generated file under .studyforge/: corpus.json, archive/, index.html, ingest/, tests/ and the reader document sit outside it — plan and onboarding listings
  - *could a skill have generated this?* **open**
