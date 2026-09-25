package practice;

public final class IntegerMean {

    private IntegerMean() {
    }

    /** Returns the mean of {@code values}, truncated as int division truncates, or 0 for no values. */
    public static int mean(int[] values) {
        long sum = 0;
        for (int value : values) {
            sum += value;
        }
        return (int) (sum / values.length);
    }
}
