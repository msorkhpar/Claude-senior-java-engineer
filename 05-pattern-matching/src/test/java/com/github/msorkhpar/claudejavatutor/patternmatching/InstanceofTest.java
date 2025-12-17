package com.github.msorkhpar.claudejavatutor.patternmatching;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class InstanceofTest {

    @ParameterizedTest
    @MethodSource("provideObjectsForTypeCheck")
    void testCheckType(Object input, String expected) {
        assertThat(InstanceofImpl.checkType(input)).isEqualTo(expected);
    }

    private static Stream<Arguments> provideObjectsForTypeCheck() {
        return Stream.of(
                Arguments.of("Hello", "String"),
                Arguments.of(42, "Integer"),
                Arguments.of(3.14, "Double"),
                Arguments.of(new Object[]{"array"}, "Object Array"),
                Arguments.of(new Object(), "Other")
        );
    }

    @ParameterizedTest
    @NullSource
    void testCheckTypeWithNull(Object input) {
        assertThat(InstanceofImpl.checkType(input)).isEqualTo("Null");
    }

    @ParameterizedTest
    @MethodSource("provideObjectsForNumberCheck")
    void testIsNumber(Object input, boolean expected) {
        assertThat(InstanceofImpl.isNumber(input)).isEqualTo(expected);
    }

    private static Stream<Arguments> provideObjectsForNumberCheck() {
        return Stream.of(
                Arguments.of(42, true),
                Arguments.of(3.14, true),
                Arguments.of(Long.valueOf(1000L), true),
                Arguments.of("42", false),
                Arguments.of(new Object(), false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideObjectsForStringOrNumberCheck")
    void testIsStringOrNumber(Object input, boolean expected) {
        assertThat(InstanceofImpl.isStringOrNumber(input)).isEqualTo(expected);
    }

    private static Stream<Arguments> provideObjectsForStringOrNumberCheck() {
        return Stream.of(
                Arguments.of("Hello", true),
                Arguments.of(42, true),
                Arguments.of(3.14, true),
                Arguments.of(new Object(), false),
                Arguments.of(null, false)
        );
    }
}