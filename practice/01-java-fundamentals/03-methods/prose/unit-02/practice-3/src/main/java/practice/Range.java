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
        throw new UnsupportedOperationException("write of");
    }
}
