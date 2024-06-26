package com.github.msorkhpar.claudejavatutor.literals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class StringMethodsTest {

    @Test
    void testGetStringLength() {
        assertThat(StringMethods.getStringLength("Hello")).isEqualTo(5);
        assertThat(StringMethods.getStringLength("")).isZero();
    }

    @ParameterizedTest
    @CsvSource({"Hello,0,H", "World,4,d", "Java,2,v"})
    void testGetCharAt(String input, int index, char expected) {
        assertThat(StringMethods.getCharAt(input, index)).isEqualTo(expected);
    }

    @Test
    void testGetCharAtThrowsException() {
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> StringMethods.getCharAt("Hello", 5));
    }

    @Test
    void testGetSubstring() {
        assertThat(StringMethods.getSubstring("Hello", 1, 4)).isEqualTo("ell");
        assertThat(StringMethods.getSubstring("World", 0, 5)).isEqualTo("World");
        assertThat(StringMethods.getSubstring("Java", 2, 2)).isEmpty();
    }

    @Test
    void testGetSubstringToEnd() {
        assertThat(StringMethods.getSubstringToEnd("Hello", 2)).isEqualTo("llo");
        assertThat(StringMethods.getSubstringToEnd("World", 5)).isEmpty();
    }

    @Test
    void testIsStringEmpty() {
        assertThat(StringMethods.isStringEmpty("")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t", "\n"})
    void testIsStringBlank(String input) {
        assertThat(StringMethods.isStringBlank(input)).isTrue();
    }


    @ParameterizedTest
    @ValueSource(strings = {"Hello", " ", "A"})
    void testIsStringNotEmpty(String input) {
        assertThat(StringMethods.isStringEmpty(input)).isFalse();
    }

    @Test
    void testGetLastChar() {
        assertThat(StringMethods.getLastChar("Hello")).isEqualTo('o');
        assertThat(StringMethods.getLastChar("A")).isEqualTo('A');
    }

    @Test
    void testGetLastCharThrowsException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> StringMethods.getLastChar(""));
    }
}