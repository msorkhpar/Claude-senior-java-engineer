package practice;

import java.util.Optional;

public final class Range {

    /** The smallest and the largest value of an array, returned together. */
    public record MinMax(int min, int max) {
    }

    private Range() {
    }

    /** Returns the smallest and largest values, or empty for no values. */
    public static Optional<MinMax> of(int[] numbers) {
        if (numbers == null || numbers.length == 0) {
            return Optional.empty();
        }
        int min = Integer.MAX_VALUE;
        int max = 0;
        for (int n : numbers) {
            if (n < min) {
                min = n;
            }
            if (n > max) {
                max = n;
            }
        }
        return Optional.of(new MinMax(min, max));
    }
}
