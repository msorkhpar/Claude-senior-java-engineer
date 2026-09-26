package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class KindsTest {

    @Test
    void classifiesCommonTypes() {
        assertThat(Kinds.kind(3.5)).isEqualTo("number");
        assertThat(Kinds.kind(7L)).isEqualTo("number");
        assertThat(Kinds.kind("hi")).isEqualTo("text");
        assertThat(Kinds.kind(new StringBuilder("x"))).isEqualTo("text");
        assertThat(Kinds.kind(new Object())).isEqualTo("other");
    }

    @Test
    void anIntegerIsMoreThanANumber() {
        assertThat(Kinds.kind(42)).isEqualTo("integer");
    }

    @Test
    void arraysAreTyped() {
        assertThat(Kinds.kind(new int[]{1, 2})).isEqualTo("int array");
        assertThat(Kinds.kind(new String[]{"a"})).isEqualTo("object array");
    }

    @Test
    void nullIsNull() {
        assertThat(Kinds.kind(null)).isEqualTo("null");
    }
}
