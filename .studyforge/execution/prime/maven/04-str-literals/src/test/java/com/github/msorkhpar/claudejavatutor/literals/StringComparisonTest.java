package com.github.msorkhpar.claudejavatutor.literals;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class StringComparisonTest {

    @Test
    void testCompareUsingEquals() {
        String str1 = "Hello";
        String str2 = new String("Hello");
        String str3 = "Hello";

        assertThat(StringComparison.compareUsingEquals(str1, str2)).isTrue();
        assertThat(StringComparison.compareUsingEquals(str1, str3)).isTrue();
        assertThat(StringComparison.compareUsingEquals(str1, "World")).isFalse();
    }

    @Test
    void testCompareUsingEqualOperator() {
        String str1 = "Hello";
        String str2 = new String("Hello");
        String str3 = "Hello";

        assertThat(StringComparison.compareUsingEqualOperator(str1, str2)).isFalse();
        assertThat(StringComparison.compareUsingEqualOperator(str1, str3)).isTrue();
    }

    @Test
    void testSafeStringCompare() {
        String str1 = "Hello";
        String str2 = null;
        String str3 = "Hello";

        assertThat(StringComparison.safeStringCompare(str1, str3)).isTrue();
        assertThat(StringComparison.safeStringCompare(str1, str2)).isFalse();
        assertThat(StringComparison.safeStringCompare(null, null)).isTrue();
    }

    @Test
    void testCaseInsensitiveCompare() {
        String str1 = "Hello";
        String str2 = "hello";
        String str3 = "HELLO";

        assertThat(StringComparison.caseInsensitiveCompare(str1, str2)).isTrue();
        assertThat(StringComparison.caseInsensitiveCompare(str1, str3)).isTrue();
        assertThat(StringComparison.caseInsensitiveCompare(str1, "World")).isFalse();
    }

    @Test
    void testCompareInternedStrings() {
        String str1 = "Hello";
        String str2 = new String("Hello").intern();
        String str3 = "Hel" + "lo"; // Compile-time constant

        assertThat(StringComparison.compareInternedStrings(str1, str2)).isTrue();
        assertThat(StringComparison.compareInternedStrings(str1, str3)).isTrue();
        assertThat(StringComparison.compareInternedStrings(str1, "World")).isFalse();
    }
}