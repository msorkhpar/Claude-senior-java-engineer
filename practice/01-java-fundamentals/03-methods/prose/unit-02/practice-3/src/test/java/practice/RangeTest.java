package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RangeTest {

    @Test
    void findsTheSmallestAndLargest() {
        assertThat(Range.of(new int[]{4, -2, 9, 3})).contains(new Range.MinMax(-2, 9));
        assertThat(Range.of(new int[]{1, 2, 3})).contains(new Range.MinMax(1, 3));
    }

    @Test
    void oneValueIsBoth() {
        assertThat(Range.of(new int[]{7})).contains(new Range.MinMax(7, 7));
        assertThat(Range.of(new int[]{-5})).contains(new Range.MinMax(-5, -5));
    }

    @Test
    void noValuesGiveNothing() {
        assertThat(Range.of(new int[]{})).isEmpty();
        assertThat(Range.of(null)).isEmpty();
    }
}
