package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LargestTest {

    @Test
    void choosesTheLargerValue() {
        assertThat(Largest.max(5, 3, 1)).isEqualTo(5);
        assertThat(Largest.max(1, 5, 3)).isEqualTo(5);
        assertThat(Largest.max(3, 1, 5)).isEqualTo(5);
        assertThat(Largest.max(5, 5, 5)).isEqualTo(5);
        assertThat(Largest.status(20)).isEqualTo("Adult");
        assertThat(Largest.status(3)).isEqualTo("Minor");
    }

    @Test
    void allNegativeValuesStillHaveAMaximum() {
        assertThat(Largest.max(-5, -3, -9)).isEqualTo(-3);
        assertThat(Largest.max(-1, -1, -2)).isEqualTo(-1);
        assertThat(Largest.max(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE))
                .isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    void eighteenIsAnAdult() {
        assertThat(Largest.status(18)).isEqualTo("Adult");
        assertThat(Largest.status(17)).isEqualTo("Minor");
    }
}
