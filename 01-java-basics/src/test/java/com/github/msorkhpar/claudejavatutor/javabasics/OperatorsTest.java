package com.github.msorkhpar.claudejavatutor.javabasics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.*;

class OperatorsTest {

    @ParameterizedTest
    @CsvSource({"1, 2, 3", "-1, 1, 0", "0, 0, 0"})
    void testAdd(int a, int b, int expected) {
        assertThat(Operators.add(a, b)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"5, 3, 2", "0, 0, 0", "-1, -1, 0"})
    void testSubtract(int a, int b, int expected) {
        assertThat(Operators.subtract(a, b)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"2, 3, 6", "-2, 3, -6", "0, 5, 0"})
    void testMultiply(int a, int b, int expected) {
        assertThat(Operators.multiply(a, b)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"6, 2, 3", "-6, 2, -3", "5, 2, 2.5"})
    void testDivide(double a, double b, double expected) {
        assertThat(Operators.divide(a, b)).isEqualTo(expected);
    }

    @Test
    void testDivideByZero() {
        assertThatExceptionOfType(ArithmeticException.class)
            .isThrownBy(() -> Operators.divide(5, 0));
    }

    @ParameterizedTest
    @CsvSource({"7, 3, 1", "-7, 3, -1", "0, 5, 0"})
    void testModulus(int a, int b, int expected) {
        assertThat(Operators.modulus(a, b)).isEqualTo(expected);
    }

    @Test
    void testModulusByZero() {
        assertThatExceptionOfType(ArithmeticException.class)
            .isThrownBy(() -> Operators.modulus(5, 0));
    }

    @Test
    void testIncrement() {
        assertThat(Operators.increment(5)).isEqualTo(6);
    }

    @Test
    void testDecrement() {
        assertThat(Operators.decrement(5)).isEqualTo(4);
    }

    @ParameterizedTest
    @CsvSource({"5, 5, true", "4, 5, false", "-1, -1, true"})
    void testIsEqual(int a, int b, boolean expected) {
        assertThat(Operators.isEqual(a, b)).isEqualTo(expected);
    }


    @ParameterizedTest
    @CsvSource({"true, true, true", "true, false, false", "false, true, false"})
    void testLogicalAnd(boolean a, boolean b, boolean expected) {
        assertThat(Operators.logicalAnd(a, b)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"true, true, true", "true, false, true", "false, false, false"})
    void testLogicalOr(boolean a, boolean b, boolean expected) {
        assertThat(Operators.logicalOr(a, b)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"true, false", "false, true"})
    void testLogicalNot(boolean a, boolean expected) {
        assertThat(Operators.logicalNot(a)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"5, 3, 1", "12, 10, 8", "-1, 0, 0"})
    void testBitwiseAnd(int a, int b, int expected) {
        assertThat(Operators.bitwiseAnd(a, b)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"5, 3, 7", "12, 10, 14", "-1, 0, -1"})
    void testBitwiseOr(int a, int b, int expected) {
        assertThat(Operators.bitwiseOr(a, b)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"5, 3, 6", "12, 10, 6", "-1, 0, -1"})
    void testBitwiseXor(int a, int b, int expected) {
        assertThat(Operators.bitwiseXor(a, b)).isEqualTo(expected);
    }


    @Test
    void testRelationalOperators() {
        assertThat(Operators.isEqual(5, 5)).isTrue();
        assertThat(Operators.isEqual(5, 6)).isFalse();
        assertThat(Operators.isNotEqual(5, 6)).isTrue();
        assertThat(Operators.isGreaterThan(6, 5)).isTrue();
        assertThat(Operators.isLessThan(5, 6)).isTrue();
        assertThat(Operators.isGreaterThanOrEqual(5, 5)).isTrue();
        assertThat(Operators.isLessThanOrEqual(5, 6)).isTrue();
    }

    @Test
    void testFloatingPointComparison() {
        assertThat(Operators.areFloatsEqual(0.1 + 0.2, 0.3)).isTrue();
    }
}