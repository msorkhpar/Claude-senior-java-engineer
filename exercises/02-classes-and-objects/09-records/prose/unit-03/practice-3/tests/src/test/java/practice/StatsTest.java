package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatsTest {

    @Test
    void findsTheSmallestAndLargest() {
        Stats.MinMax result = Stats.minMax(new int[] {3, 1, 4, 1, 5});
        assertThat(result).isEqualTo(new Stats.MinMax(1, 5));
        assertThat(result.spread()).isEqualTo(4L);
        assertThat(Stats.minMax(new int[] {7})).isEqualTo(new Stats.MinMax(7, 7));
    }

    @Test
    void allNegativeValues() {
        assertThat(Stats.minMax(new int[] {-8, -3, -5})).isEqualTo(new Stats.MinMax(-8, -3));
    }

    @Test
    void anEmptyArrayIsRefused() {
        assertThatThrownBy(() -> Stats.minMax(new int[] {})).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theSpreadMayExceedAnInt() {
        assertThat(Stats.minMax(new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE}).spread()).isEqualTo(4294967295L);
    }
}
