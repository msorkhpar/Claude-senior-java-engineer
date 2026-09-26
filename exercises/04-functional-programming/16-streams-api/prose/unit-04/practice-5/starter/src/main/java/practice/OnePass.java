package practice;

import java.util.List;
import java.util.Optional;

public final class OnePass {

    private OnePass() {
    }

    public record MinMax(int min, int max) {
    }

    /** The smallest and the largest number, found in one pass; empty when there are none. */
    public static Optional<MinMax> range(List<Integer> numbers) {
        throw new UnsupportedOperationException("write range");
    }

    /** The average of the numbers; 0.0 when there are none. */
    public static double mean(List<Integer> numbers) {
        throw new UnsupportedOperationException("write mean");
    }
}
