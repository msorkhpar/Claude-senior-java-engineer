package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReverseDigitsTest {

    @Test
    void reversesTheDigits() {
        assertThat(ReverseDigits.reverse(123)).isEqualTo(321);
        assertThat(ReverseDigits.reverse(1200)).isEqualTo(21);
        assertThat(ReverseDigits.reverse(7)).isEqualTo(7);
        assertThat(ReverseDigits.reverse(0)).isEqualTo(0);
    }

    @Test
    void keepsTheSign() {
        assertThat(ReverseDigits.reverse(-123)).isEqualTo(-321);
        assertThat(ReverseDigits.reverse(-10)).isEqualTo(-1);
    }

    @Test
    void anOverflowingReversalIsZero() {
        assertThat(ReverseDigits.reverse(1_534_236_469)).isEqualTo(0);
        assertThat(ReverseDigits.reverse(Integer.MAX_VALUE)).isEqualTo(0);
        assertThat(ReverseDigits.reverse(-1_563_847_412)).isEqualTo(0);
        assertThat(ReverseDigits.reverse(Integer.MIN_VALUE)).isEqualTo(0);
        assertThat(ReverseDigits.reverse(1_463_847_412)).isEqualTo(2_147_483_641);
    }
}
