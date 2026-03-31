# Continue Curriculum (Parallel)

Build the next batch of curriculum modules in parallel. Each agent is fully responsible for one module end-to-end — implementation, tests, fixes, and validation.

**Usage:** `/continue-curriculum [N]` where N = number of parallel agents (default: 3, max: 5)

---

## Step 1 — Read Progress

Read `PROGRESS.md` and collect all rows with status `⬜ pending`, in order.

If **none** are pending → print `🎉 Curriculum complete! All 45 modules are done.` and stop.

---

## Step 2 — Select Batch

- N = $ARGUMENTS (use 3 if not specified; cap at 5)
- Select the **first N** pending modules from the list
- Print: `Starting batch: modules {list}` so the user can see what's being worked on

---

## Step 3 — Claim All Modules (Sequential — prevents conflicts)

For each of the N selected modules, update `PROGRESS.md` status to `🔄 in-progress`.

**Do this sequentially and completely before launching any agents.**
This prevents two agents from ever claiming the same module.

---

## Step 4 — Pre-register in Parent POM (Sequential)

For each of the N modules, add it to the root `pom.xml` `<modules>` section (in numeric order) and create the bare module directory + `pom.xml`.

This ensures `mvn test -pl {module-dir}` works when agents run it.

Only create the skeleton; leave implementation to the agents:
```
{module-dir}/
└── pom.xml   ← with correct parent and artifactId
```

---

## Step 5 — Launch Parallel Agents

Launch N Task agents **simultaneously** (`run_in_background: true`), one per module.

Each agent receives this prompt (fill in `{module-dir}` and `{section}` for each):

---
*Agent prompt template:*

```
You are a Java curriculum content agent for a senior Java engineer interview prep repository.

Your assignment:
- Module directory: {module-dir}
- Curriculum section: {section} (see CLAUDE.md for full sub-topic list)

IMPORTANT rules for this task:
1. Do NOT modify PROGRESS.md — the orchestrator manages it
2. Do NOT modify the root pom.xml — it is already updated
3. Work ONLY inside the {module-dir} directory

Follow this workflow precisely:

1. Read CLAUDE.md completely (especially Content Creation Guidelines, Testing Best Practices, Module Completion Checklist)
2. Read the ENTIRE 13-lambda-expressions/ module as your style reference:
   - Read README.md (it is a table of contents only — link list to sub-topic files)
   - Read README_4.1.1.md, README_4.1.2.md, README_4.1.3.md, README_4.1.4.md (these are the detailed per-sub-topic files)
   - Read at least one implementation class and its test to understand depth and style
3. Create src/main/java and src/test/java directory trees with correct package: com.github.msorkhpar.claudejavatutor.{pkg}

4. DOCUMENTATION — this is mandatory and must match module 13 depth exactly:
   a. Create README.md as a TABLE OF CONTENTS ONLY — numbered list of sub-topics linking to their README_X.Y.Z.md files
   b. For EACH sub-topic in section {section}, create a dedicated README_X.Y.Z.md file (e.g., README_4.2.1.md, README_4.2.2.md, etc.)
   c. Each README_X.Y.Z.md MUST contain ALL of these sections with deep, in-depth content:
      - ## Concept Explanation — thorough explanation with real-world analogy
      - ## Key Points to Remember — bullet list of critical facts
      - ## Relevant Java 21 Features — modern practices, evolution across Java versions
      - ## Common Pitfalls and How to Avoid Them — numbered list with code examples showing the problem and fix
      - ## Best Practices and Optimization Techniques — production-quality guidance
      - ## Edge Cases and Their Handling — boundary conditions, null handling, empty collections
      - ## Interview-specific Insights — what interviewers focus on, tricky questions to expect
      - ## Interview Q&A Section — minimum 5 Q&A pairs per sub-topic:
          * Each Q&A must have BOTH a text explanation block AND a code block
          * Text blocks use ```text fenced blocks
          * Code blocks use ```java fenced blocks
          * Answers must be detailed, not superficial
      - ## Code Examples — links to the implementation and test files

5. For EACH sub-topic:
   a. Write test class first (TDD) — JUnit 5 + AssertJ, cover happy paths, edge cases, null inputs, exceptions, boundary conditions
   b. Write implementation class
   c. Run: mvn test -pl {module-dir}
   d. If any test fails: read the error, fix the root cause, re-run — do NOT proceed until green

6. Run: bash scripts/validate-module.sh {module-dir}
7. Fix every ERROR reported. Re-run until PASS.
8. Run final: mvn test -pl {module-dir} — confirm BUILD SUCCESS

Return a summary with:
- Status: SUCCESS or FAILED
- Classes created (implementation + test)
- README files created (list all README_X.Y.Z.md files)
- Total @Test methods
- Any issues encountered and how they were resolved
```
---

---

## Step 6 — Wait for All Agents

Wait for all N background agents to complete. Collect their result summaries.

---

## Step 7 — Integration Verification

Run all newly created modules together to catch cross-module issues:
```bash
mvn test -pl {mod1},{mod2},{mod3}
```

If failures: investigate (may be a shared dependency or pom issue), fix, re-run.

---

## Step 8 — Update Progress

For each agent result:
- **SUCCESS** → update `PROGRESS.md` status to `✅ done`
- **FAILED** → reset `PROGRESS.md` status to `⬜ pending` and print the error summary

Update the **Summary** block at the top of `PROGRESS.md` (Completed / In Progress / Pending counts).

---

## Step 9 — Report

Print a batch summary:
```
Batch complete:
  ✅ Succeeded: {list}
  ❌ Failed:    {list} (if any)
  ⬜ Remaining: {count} modules pending
```

Ask: **"Continue with next batch? Run `/continue-curriculum {N}` to proceed, or check failed modules first."**
