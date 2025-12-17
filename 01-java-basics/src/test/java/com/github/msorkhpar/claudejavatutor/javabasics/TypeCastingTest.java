package com.github.msorkhpar.claudejavatutor.javabasics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TypeCastingTest {

    private final TypeCasting typeCasting = new TypeCasting();

    @Test
    void testImplicitCasting() {
        assertThat(typeCasting.implicitIntToLong(100)).isEqualTo(100L);
        assertThat(typeCasting.implicitIntToDouble(100)).isEqualTo(100.0);
    }

    @Test
    void testExplicitCasting() {
        assertThat(typeCasting.explicitDoubleToInt(3.14)).isEqualTo(3);
        assertThat(typeCasting.explicitLongToInt(1234567890L)).isEqualTo(1234567890);
    }

    @Test
    void testPrimitiveToObject() {
        assertThat(typeCasting.intToInteger(42)).isEqualTo(Integer.valueOf(42));
        assertThat(typeCasting.charToCharacter('A')).isEqualTo(Character.valueOf('A'));
    }

    @Test
    void testObjectToPrimitive() {
        assertThat(typeCasting.integerToInt(Integer.valueOf(42))).isEqualTo(42);
        assertThat(typeCasting.characterToChar(Character.valueOf('A'))).isEqualTo('A');
    }

    @Test
    void testStringConversions() {
        assertThat(typeCasting.stringToInt("123")).isEqualTo(123);
        assertThat(typeCasting.intToString(456)).isEqualTo("456");
    }

    @Test
    void testDataLoss() {
        assertThat(typeCasting.demonstrateDataLoss(1234.56)).isEqualTo(1234);
    }

    @Test
    void testOverflow() {
        assertThat(typeCasting.demonstrateOverflow(Integer.MAX_VALUE + 1L)).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    void testInvalidStringToNumber() {
        assertThatThrownBy(() -> typeCasting.stringToInt("not a number"))
                .isInstanceOf(NumberFormatException.class);
    }
}