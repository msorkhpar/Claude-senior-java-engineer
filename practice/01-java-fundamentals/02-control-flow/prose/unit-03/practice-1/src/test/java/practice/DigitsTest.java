package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DigitsTest {

    @Test
    void countsTheDigits() {
        assertThat(Digits.count(7)).isEqualTo(1);
        assertThat(Digits.count(120)).isEqualTo(3);
        assertThat(Digits.count(9876543210L)).isEqualTo(10);
    }

    @Test
    void zeroHasOneDigit() {
        assertThat(Digits.count(0)).isEqualTo(1);
    }

    @Test
    void theSignIsNotADigit() {
        assertThat(Digits.count(-120)).isEqualTo(3);
        assertThat(Digits.count(-5)).isEqualTo(1);
        assertThat(Digits.count(Long.MIN_VALUE)).isEqualTo(19);
    }
}
