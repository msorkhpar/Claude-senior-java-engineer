# New Module: $ARGUMENTS

Create a complete, production-quality curriculum module. Follow every step in order. Do not skip steps.

---

## Step 1 — Pre-flight

1. Read `PROGRESS.md` and locate the row for module `$ARGUMENTS` (match by module number or directory name).
   - Extract: **module number**, **directory name**, **curriculum section** (e.g., `4.2 Functional Interfaces`)
2. Read `CLAUDE.md` — specifically these sections:
   - "Module Structure Pattern"
   - "Package Naming Convention"
   - "Testing Best Practices"
   - "Content Creation Guidelines"
   - "Module Completion Checklist"
3. Verify the module directory does NOT already exist. If it does, validate it instead with `/validate-module`.

---

## Step 2 — Claim

Update `PROGRESS.md`: change this module's status from `⬜ pending` → `🔄 in-progress`.

---

## Step 3 — Register in Parent POM

Add the module to the root `pom.xml` `<modules>` section (in numeric order):
```xml
<module>{module-dir}</module>
```

---

## Step 4 — Create Module Skeleton

Create the following (substitute `{pkg}` = module directory name with hyphens removed, lowercase):

```
{module-dir}/
├── pom.xml                          ← copy structure from an existing module, update artifactId
└── src/
    ├── main/java/com/github/msorkhpar/claudejavatutor/{pkg}/
    └── test/java/com/github/msorkhpar/claudejavatutor/{pkg}/
```

**pom.xml rules:**
- `<artifactId>` = module directory name (e.g., `14-functional-interfaces`)
- `<name>` = human-readable topic name
- Parent = `com.github.msorkhpar.claudejavatutor:claude-java-tutor`
- Include dependency on `00-base` only if performance tests are needed

---

## Step 5 — Write Documentation

**Before writing anything**, read the entire `13-lambda-expressions/` module:
- `README.md` — it is a table-of-contents only (numbered list with links)
- `README_4.1.1.md` through `README_4.1.4.md` — these are the detailed per-sub-topic files; match their depth exactly

### 5a. Create README.md (table of contents only)

`{module-dir}/README.md` must be a numbered list of sub-topics with links to their `README_X.Y.Z.md` files.
No content goes in README.md itself — only the ToC link list.

### 5b. Create README_X.Y.Z.md for EACH sub-topic (mandatory — no exceptions)

For every sub-topic listed under this section in `CLAUDE.md`, create a dedicated file (e.g., `README_4.2.1.md`).

Each file MUST contain ALL of these sections with deep, thorough content:

1. **## Concept Explanation** — thorough explanation with a real-world analogy that makes the concept accessible
2. **## Key Points to Remember** — bullet list of the most critical facts an interviewer expects you to know
3. **## Relevant Java 21 Features** — how this concept evolved across Java versions, modern practices
4. **## Common Pitfalls and How to Avoid Them** — numbered list; each pitfall shows the broken code AND the fix
5. **## Best Practices and Optimization Techniques** — production-quality guidance, performance implications
6. **## Edge Cases and Their Handling** — null inputs, empty collections, boundary conditions with code
7. **## Interview-specific Insights** — what interviewers actually focus on, tricky questions to expect, whiteboard tips
8. **## Interview Q&A Section** — minimum 5 Q&A pairs per sub-topic:
   - Each Q&A must have BOTH a `\`\`\`text` block (explanation) AND a `\`\`\`java` block (code example)
   - Answers must be detailed — a paragraph of explanation, not a one-liner
9. **## Code Examples** — links to the implementation and test source files

---

## Step 6 — Implement (TDD — repeat for EACH sub-topic)

For **each sub-topic** in the curriculum section:

### 6a. Write test class first
- File: `src/test/java/.../ClassNameTest.java`
- Use JUnit 5 + AssertJ
- Cover: happy paths, edge cases, null/empty inputs, expected exceptions, boundary conditions
- Use DataFaker for realistic data where appropriate

### 6b. Write implementation class
- File: `src/main/java/.../ClassName.java`
- Minimal code that clearly demonstrates the concept
- Use Java 21 features where appropriate

### 6c. Run tests immediately after each class pair
```bash
mvn test -pl {module-dir}
```

### 6d. Fix failures before moving to the next sub-topic
- Read the full stack trace
- Fix the root cause (never suppress or skip tests)
- Re-run until green
- **Do not proceed to the next sub-topic until all current tests pass**

---

## Step 7 — Final Validation

Run the validation script:
```bash
bash scripts/validate-module.sh {module-dir}
```

Fix **every** reported ERROR. Re-run validation until it exits with `RESULT: PASS`.

Run one final clean test:
```bash
mvn test -pl {module-dir}
```

---

## Step 8 — Complete

Update `PROGRESS.md`: change status from `🔄 in-progress` → `✅ done`.

Print a completion summary:
- Module: `{module-dir}`
- Curriculum section covered: `{section}`
- Implementation classes: list all
- Test classes: list all
- Total `@Test` methods written
- Validation result
