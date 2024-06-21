package com.github.msorkhpar.claudejavatutor.javabasics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrimitiveTypesTest {

        @Test
        void testPrimitiveRanges() {
            PrimitiveTypes primitiveTypes = new PrimitiveTypes();

            // Byte
            assertThat(primitiveTypes.getByteRange()).containsExactly((byte)-128, (byte)127);

            // Short
            assertThat(primitiveTypes.getShortRange()).containsExactly((short)-32768, (short)32767);

            // Integer
            assertThat(primitiveTypes.getIntRange()).containsExactly(-2147483648, 2147483647);

            // Long
            assertThat(primitiveTypes.getLongRange()).containsExactly(-9223372036854775808L, 9223372036854775807L);

            // Float
            assertThat(primitiveTypes.getFloatRange()).containsExactly(1.4E-45f, 3.4028235E38f);


            // Double
            double[] expectedDoubleRange = primitiveTypes.getDoubleRange();
            assertThat(primitiveTypes.getDoubleRange()).containsExactly(4.9E-324, 1.7976931348623157E308);

            // Boolean
            assertThat(primitiveTypes.getBooleanValues()).containsExactly(false, true);

            // Char
            assertThat(primitiveTypes.getCharRange()).containsExactly('\u0000', '\uffff');
        }

    @Test
    void testIntegerOverflow() {
        PrimitiveTypes primitiveTypes = new PrimitiveTypes();
        assertThat(primitiveTypes.demonstrateIntegerOverflow()).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    void testFloatingPointPrecision() {
        PrimitiveTypes primitiveTypes = new PrimitiveTypes();
        assertThat(primitiveTypes.demonstrateFloatingPointPrecision()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"0.1, 0.2, 0.3", "1.0, 2.0, 3.0", "0.7, 0.1, 0.8"})
    void testFloatingPointComparison(double a, double b, double expected) {
        PrimitiveTypes primitiveTypes = new PrimitiveTypes();
        assertThat(primitiveTypes.compareFloatingPoint(a, b, expected)).isTrue();
    }

    @Test
    void testDivisionByZero() {
        PrimitiveTypes primitiveTypes = new PrimitiveTypes();
        assertThrows(ArithmeticException.class, primitiveTypes::demonstrateDivisionByZero);
    }

    @Test
    void testCharOperations() {
        PrimitiveTypes primitiveTypes = new PrimitiveTypes();
        assertThat(primitiveTypes.demonstrateCharOperations()).isEqualTo('b');
    }
}