#!/bin/bash
# validate-module.sh — Validates a curriculum module for structural correctness.
# Usage: bash scripts/validate-module.sh <module-dir>
# Example: bash scripts/validate-module.sh 14-functional-interfaces

set -euo pipefail

MODULE_DIR="${1:-}"
if [ -z "$MODULE_DIR" ]; then
    echo "Usage: $0 <module-dir>"
    echo "Example: $0 14-functional-interfaces"
    exit 1
fi

# Strip trailing slash if present
MODULE_DIR="${MODULE_DIR%/}"

ERRORS=0
WARNINGS=0
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
FULL_MODULE_PATH="$ROOT_DIR/$MODULE_DIR"

echo "======================================================"
echo "  Validating: $MODULE_DIR"
echo "======================================================"

# ── 1. Directory exists ──────────────────────────────────
if [ ! -d "$FULL_MODULE_PATH" ]; then
    echo "ERROR: Module directory not found: $FULL_MODULE_PATH"
    exit 1
fi

# ── 2. pom.xml exists ────────────────────────────────────
if [ ! -f "$FULL_MODULE_PATH/pom.xml" ]; then
    echo "ERROR: Missing pom.xml"
    ERRORS=$((ERRORS + 1))
else
    echo "  [OK] pom.xml found"
fi

# ── 3. README.md exists ──────────────────────────────────
if [ ! -f "$FULL_MODULE_PATH/README.md" ]; then
    echo "ERROR: Missing README.md"
    ERRORS=$((ERRORS + 1))
else
    echo "  [OK] README.md found"
    # Check README has minimum required sections
    for section in "## " "Q&A\|Interview\|interview"; do
        if ! grep -q "$section" "$FULL_MODULE_PATH/README.md"; then
            echo "WARNING: README.md may be missing sections (checked pattern: $section)"
            WARNINGS=$((WARNINGS + 1))
        fi
    done
fi

# ── 4. Source directories exist ──────────────────────────
MAIN_SRC="$FULL_MODULE_PATH/src/main/java"
TEST_SRC="$FULL_MODULE_PATH/src/test/java"

if [ ! -d "$MAIN_SRC" ]; then
    echo "ERROR: Missing src/main/java"
    ERRORS=$((ERRORS + 1))
else
    echo "  [OK] src/main/java exists"
fi

if [ ! -d "$TEST_SRC" ]; then
    echo "ERROR: Missing src/test/java"
    ERRORS=$((ERRORS + 1))
else
    echo "  [OK] src/test/java exists"
fi

# ── 5. 1:1 test mapping ───────────────────────────────────
IMPL_COUNT=0
MISSING_TESTS=0
if [ -d "$MAIN_SRC" ]; then
    while IFS= read -r -d '' impl_file; do
        IMPL_COUNT=$((IMPL_COUNT + 1))
        rel_path="${impl_file#$MAIN_SRC/}"
        test_file="$TEST_SRC/${rel_path%.java}Test.java"
        if [ ! -f "$test_file" ]; then
            echo "ERROR: Missing test file for: $rel_path"
            echo "       Expected: src/test/java/${rel_path%.java}Test.java"
            MISSING_TESTS=$((MISSING_TESTS + 1))
            ERRORS=$((ERRORS + 1))
        fi
    done < <(find "$MAIN_SRC" -name "*.java" -print0 2>/dev/null)
    echo "  [OK] Implementation files found: $IMPL_COUNT"
    if [ $MISSING_TESTS -eq 0 ] && [ $IMPL_COUNT -gt 0 ]; then
        echo "  [OK] All implementation files have corresponding test files"
    fi
fi

# ── 6. Test classes are non-empty ─────────────────────────
if [ -d "$TEST_SRC" ]; then
    while IFS= read -r -d '' test_file; do
        # Count @Test annotations
        test_count=$(grep -c "@Test" "$test_file" 2>/dev/null || echo 0)
        if [ "$test_count" -eq 0 ]; then
            echo "WARNING: Test file has no @Test methods: ${test_file#$ROOT_DIR/}"
            WARNINGS=$((WARNINGS + 1))
        fi
    done < <(find "$TEST_SRC" -name "*Test.java" -print0 2>/dev/null)
fi

# ── 7. Package naming convention ─────────────────────────
EXPECTED_BASE="com.github.msorkhpar.claudejavatutor"
PACKAGE_ERRORS=0
if [ -d "$MAIN_SRC" ] || [ -d "$TEST_SRC" ]; then
    while IFS= read -r -d '' java_file; do
        actual_pkg=$(grep -m1 "^package " "$java_file" 2>/dev/null | awk '{print $2}' | tr -d ';' || echo "")
        if [ -n "$actual_pkg" ] && [[ "$actual_pkg" != "$EXPECTED_BASE"* ]]; then
            echo "ERROR: Wrong package in ${java_file#$ROOT_DIR/}"
            echo "       Found:    $actual_pkg"
            echo "       Expected: $EXPECTED_BASE.<modulename>"
            PACKAGE_ERRORS=$((PACKAGE_ERRORS + 1))
            ERRORS=$((ERRORS + 1))
        fi
    done < <(find "$FULL_MODULE_PATH/src" -name "*.java" -print0 2>/dev/null)
    if [ $PACKAGE_ERRORS -eq 0 ]; then
        echo "  [OK] Package naming correct"
    fi
fi

# ── 8. Module registered in parent pom.xml ───────────────
if ! grep -q "<module>$MODULE_DIR</module>" "$ROOT_DIR/pom.xml" 2>/dev/null; then
    echo "ERROR: Module not registered in root pom.xml"
    echo "       Add: <module>$MODULE_DIR</module>"
    ERRORS=$((ERRORS + 1))
else
    echo "  [OK] Registered in parent pom.xml"
fi

# ── 9. Maven build ───────────────────────────────────────
echo ""
echo "  Running: mvn test -pl $MODULE_DIR ..."
if mvn test -pl "$MODULE_DIR" -q 2>&1; then
    echo "  [OK] All tests pass"
else
    echo "ERROR: Maven tests failed — see output above"
    ERRORS=$((ERRORS + 1))
fi

# ── Summary ──────────────────────────────────────────────
echo ""
echo "======================================================"
if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo "  RESULT: PASS — $MODULE_DIR is valid"
elif [ $ERRORS -eq 0 ]; then
    echo "  RESULT: PASS with $WARNINGS warning(s) — $MODULE_DIR"
else
    echo "  RESULT: FAIL — $ERRORS error(s), $WARNINGS warning(s) in $MODULE_DIR"
fi
echo "======================================================"

exit $ERRORS
