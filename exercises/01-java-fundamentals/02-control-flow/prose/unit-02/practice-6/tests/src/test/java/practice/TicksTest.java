package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TicksTest {

    @Test
    void marksEachStep() {
        assertThat(Ticks.ticks(0.0, 0.5, 5)).containsExactly(0.0, 0.5, 1.0, 1.5, 2.0);
        assertThat(Ticks.ticks(2.0, 0.25, 3)).containsExactly(2.0, 2.25, 2.5);
    }

    @Test
    void tenthsAddUpExactly() {
        double[] values = Ticks.ticks(0.0, 0.1, 11);
        assertThat(values).hasSize(11);
        assertThat(values[10]).isEqualTo(1.0);
        assertThat(values[3]).isEqualTo(3 * 0.1);
    }

    @Test
    void zeroTicksIsEmpty() {
        assertThat(Ticks.ticks(3.0, 1.0, 0)).isEmpty();
    }
}
