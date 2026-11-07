package practice;

import java.util.Arrays;
import java.util.OptionalDouble;

/** Figures for the file-transfer dashboard. */
public class TransferStats {

    /** One line of the dashboard: total bytes moved, and the mean duration of the recent transfers. */
    public String summary(int[] sizes, int[] durationsMs, int recent) {
        OptionalDouble mean = meanOfLast(durationsMs, recent);
        return "total " + totalBytes(sizes) + " B, recent mean "
                + (mean.isPresent() ? String.format("%.1f ms", mean.getAsDouble()) : "n/a");
    }

    /** Total bytes of all the transfers. Each size fits in an int; their total need not. */
    long totalBytes(int[] sizes) {
        return Arrays.stream(sizes).asLongStream().sum();
    }

    /**
     * The mean of the last n durations, or of all of them if there are fewer than n. Empty when
     * there are none: the dashboard shows "n/a" then.
     */
    OptionalDouble meanOfLast(int[] durationsMs, int n) {
        int from = Math.max(0, durationsMs.length - n);
        return Arrays.stream(durationsMs, from, Math.max(from, durationsMs.length - 1)).average();
    }
}
