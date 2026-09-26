package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MidpointTest {

    @Test
    void averagesTwoNumbers() {
        assertThat(Midpoint.midpoint(2, 8)).isEqualTo(5);
        assertThat(Midpoint.midpoint(3, 4)).isEqualTo(3);
        assertThat(Midpoint.midpoint(-4, -8)).isEqualTo(-6);
        assertThat(Midpoint.midpoint(0, 0)).isZero();
    }

    @Test
    void largeValuesDoNotOverflow() {
        assertThat(Midpoint.midpoint(Integer.MAX_VALUE, Integer.MAX_VALUE - 2)).isEqualTo(2147483646);
        assertThat(Midpoint.midpoint(Integer.MIN_VALUE, Integer.MIN_VALUE)).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    void halvesRoundDown() {
        assertThat(Midpoint.midpoint(-3, 0)).isEqualTo(-2);
        assertThat(Midpoint.midpoint(Integer.MIN_VALUE, Integer.MAX_VALUE)).isEqualTo(-1);
    }
}
