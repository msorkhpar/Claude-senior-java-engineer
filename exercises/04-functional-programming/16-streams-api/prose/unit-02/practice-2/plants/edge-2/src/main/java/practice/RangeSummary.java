package practice;

import java.util.Arrays;
import java.util.IntSummaryStatistics;

public final class RangeSummary {

    private RangeSummary() {
    }

    /** The count, sum and average of a slice; an empty slice averages 0.0. */
    public record Summary(long count, long sum, double average) {
    }

    /** Summarises values[from] up to, not including, values[to]. */
    public static Summary summarize(int[] values, int from, int to) {
        IntSummaryStatistics stats = Arrays.stream(values, from, to).summaryStatistics();
        int sum = Arrays.stream(values, from, to).sum();
        return new Summary(stats.getCount(), sum, stats.getAverage());
    }
}
