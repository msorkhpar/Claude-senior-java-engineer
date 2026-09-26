package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RangeSummaryTest {

    private static final int[] VALUES = {3, 1, 4, 1, 5, 9, 2, 6};

    @Test
    void summarisesTheSlice() {
        assertThat(RangeSummary.summarize(VALUES, 0, 8)).isEqualTo(new RangeSummary.Summary(8, 31, 3.875));
        assertThat(RangeSummary.summarize(VALUES, 1, 4)).isEqualTo(new RangeSummary.Summary(3, 6, 2.0));
        assertThat(RangeSummary.summarize(VALUES, 7, 8)).isEqualTo(new RangeSummary.Summary(1, 6, 6.0));
    }

    @Test
    void anEmptySliceAveragesZero() {
        assertThat(RangeSummary.summarize(VALUES, 2, 2)).isEqualTo(new RangeSummary.Summary(0, 0, 0.0));
        assertThat(RangeSummary.summarize(new int[0], 0, 0)).isEqualTo(new RangeSummary.Summary(0, 0, 0.0));
    }

    @Test
    void sumsBeyondIntDoNotOverflow() {
        int[] big = {Integer.MAX_VALUE, Integer.MAX_VALUE};

        assertThat(RangeSummary.summarize(big, 0, 2))
                .isEqualTo(new RangeSummary.Summary(2, 2L * Integer.MAX_VALUE, Integer.MAX_VALUE));
    }
}
