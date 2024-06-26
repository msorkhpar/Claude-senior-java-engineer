package com.github.msorkhpar.claudejavatutor.literals;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class StringCreationTest {

    @Test
    void testCreateLiteral() {
        String result = StringCreation.createLiteral();
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    void testCreateWithNew() {
        String result = StringCreation.createWithNew();
        assertThat(result).isEqualTo("Hello, World!");
        assertThat(result).isNotSameAs("Hello, World!");
    }

    @Test
    void testCreateFromCharArray() {
        String result = StringCreation.createFromCharArray();
        assertThat(result).isEqualTo("Hello");
    }

    @Test
    void testCreateFromByteArray() {
        String result = StringCreation.createFromByteArray();
        assertThat(result).isEqualTo("Hello");
    }

    @Test
    void testCreateFromStringBuilder() {
        String result = StringCreation.createFromStringBuilder();
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    void testCompareStrings() {
        String s1 = "Hello";
        String s2 = "Hello";
        String s3 = new String("Hello");

        assertThat(StringCreation.compareStrings(s1, s2)).isTrue();
        assertThat(StringCreation.compareStrings(s1, s3)).isFalse();
    }

    @Test
    void testInternString() {
        String s1 = new String("Hello");
        String s2 = "Hello";

        assertThat(s1 == s2).isFalse();
        assertThat(StringCreation.internString(s1) == s2).isTrue();
    }

    @Test
    void testNullString() {
        assertThatNullPointerException().isThrownBy(() -> {
            String nullString = null;
            nullString.length(); // This will throw NullPointerException
        });
    }

    @Test
    void testEmptyString() {
        String emptyString = "";
        assertThat(emptyString).isEmpty();
        assertThat(emptyString).isNotNull();
    }
}