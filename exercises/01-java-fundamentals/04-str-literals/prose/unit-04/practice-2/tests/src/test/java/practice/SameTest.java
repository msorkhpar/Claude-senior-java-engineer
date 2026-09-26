package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SameTest {

    @Test
    void comparesTexts() {
        assertThat(Same.sameText("hello", "hello")).isTrue();
        assertThat(Same.sameText("hello", "world")).isFalse();
        assertThat(Same.sameText("", "")).isTrue();
    }

    @Test
    void aNullFirstArgumentIsSafe() {
        assertThat(Same.sameText(null, "hello")).isFalse();
        assertThat(Same.sameText(null, null)).isTrue();
    }

    @Test
    void aNullSecondArgumentIsSafe() {
        assertThat(Same.sameText("hello", null)).isFalse();
    }

    @Test
    void equalTextsBuiltAtRunTimeAreTheSame() {
        assertThat(Same.sameText("hello", new String("hello"))).isTrue();
        assertThat(Same.sameText(new String(""), "")).isTrue();
    }
}
