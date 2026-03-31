# Validate Module: $ARGUMENTS

Run a full quality check on the specified module. Report all issues and optionally fix them.

---

## Step 1 — Run Validation Script

```bash
bash scripts/validate-module.sh $ARGUMENTS
```

Read the full output carefully.

---

## Step 2 — Check Results

For each reported **ERROR**:
1. Identify the root cause
2. Fix it
3. Re-run `bash scripts/validate-module.sh $ARGUMENTS`
4. Repeat until the script reports `RESULT: PASS`

For each reported **WARNING**:
- Assess whether it needs fixing (missing Q&A section in README = fix it; minor style = document it)

---

## Step 3 — Deep Content Check

Beyond the script, manually verify:

### 3a. Test quality
- Each `@Test` method has a clear `// Given / When / Then` structure or equivalent
- No test method is trivially empty or just `assertTrue(true)`
- Exception tests use `assertThatThrownBy` or `@Test(expected=...)` patterns
- Edge cases are tested: null inputs, empty collections, boundary values, overflow conditions

### 3b. Implementation quality
- No dead code or unused imports
- All public methods in implementation classes are exercised by at least one test
- Java 21 features used where appropriate (pattern matching, records, sealed, text blocks, etc.)

### 3c. README completeness
- Has a Q&A section with at least one question per sub-topic
- Code examples compile and match the actual implementation
- Common pitfalls section is present

---

## Step 4 — Final Maven Run

```bash
mvn test -pl $ARGUMENTS
```

Confirm: `BUILD SUCCESS` with all tests passing.

---

## Step 5 — Report

Print a summary:
```
Module: {module-dir}
Status: PASS / FAIL
Implementation classes: N
Test classes: N
Total @Test methods: N
Issues found: list any remaining warnings
```
