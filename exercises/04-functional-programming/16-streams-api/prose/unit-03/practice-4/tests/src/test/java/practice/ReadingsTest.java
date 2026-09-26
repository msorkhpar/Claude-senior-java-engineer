package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingsTest {

    @Test
    void splitsAtTheThreshold() {
        List<Integer> rising = List.of(1, 2, 3, 4, 5, 6);

        assertThat(Readings.warmup(rising, 4)).containsExactly(1, 2, 3);
        assertThat(Readings.afterWarmup(rising, 4)).containsExactly(4, 5, 6);
        assertThat(Readings.warmup(List.of(7, 8), 5)).isEmpty();
        assertThat(Readings.afterWarmup(List.of(1, 2), 5)).isEmpty();
    }

    @Test
    void warmupEndsAtTheFirstHighReading() {
        assertThat(Readings.warmup(List.of(1, 2, 5, 3, 4), 5)).containsExactly(1, 2);
        assertThat(Readings.warmup(List.of(7, 1, 2), 5)).isEmpty();
        assertThat(Readings.warmup(List.of(Integer.MIN_VALUE, 1, 9), 5)).containsExactly(Integer.MIN_VALUE, 1);
    }

    @Test
    void lowReadingsAfterWarmupAreKept() {
        assertThat(Readings.afterWarmup(List.of(1, 2, 5, 3, 4), 5)).containsExactly(5, 3, 4);
        assertThat(Readings.afterWarmup(List.of(7, 1, 2), 5)).containsExactly(7, 1, 2);
        assertThat(Readings.afterWarmup(List.of(Integer.MIN_VALUE, 9, 1), 5)).containsExactly(9, 1);
    }
}
