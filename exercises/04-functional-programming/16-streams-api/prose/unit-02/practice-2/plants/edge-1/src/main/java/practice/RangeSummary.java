package practice;

import java.util.Arrays;

public final class RangeSummary {

    private RangeSummary() {
    }

    /** The count, sum and average of a slice; an empty slice averages 0.0. */
    public record Summary(long count, long sum, double average) {
    }

    /** Summarises values[from] up to, not including, values[to]. */
    public static Summary summarize(int[] values, int from, int to) {
        long count = Arrays.stream(values, from, to).count();
        long sum = Arrays.stream(values, from, to).asLongStream().sum();
        double average = Arrays.stream(values, from, to).average().getAsDouble();
        return new Summary(count, sum, average);
    }
}
