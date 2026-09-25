package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GuardsTest {

    @Test
    void answersForOrdinaryInputs() {
        assertThat(Guards.startsWithDigit("7up")).isTrue();
        assertThat(Guards.startsWithDigit("abc")).isFalse();
        assertThat(Guards.startsWithDigit("a1")).isFalse();
        assertThat(Guards.exactlyOne(true, false)).isTrue();
        assertThat(Guards.exactlyOne(false, true)).isTrue();
        assertThat(Guards.exactlyOne(false, false)).isFalse();
    }

    @Test
    void nullDoesNotStartWithADigit() {
        assertThat(Guards.startsWithDigit(null)).isFalse();
    }

    @Test
    void emptyTextDoesNotStartWithADigit() {
        assertThat(Guards.startsWithDigit("")).isFalse();
    }

    @Test
    void bothTrueIsNotExactlyOne() {
        assertThat(Guards.exactlyOne(true, true)).isFalse();
    }
}
