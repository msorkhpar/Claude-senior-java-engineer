package practice;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class OnePass {

    private OnePass() {
    }

    public record MinMax(int min, int max) {
    }

    /** The smallest and the largest number, found in one pass; empty when there are none. */
    public static Optional<MinMax> range(List<Integer> numbers) {
        return numbers.stream().collect(Collectors.teeing(
                Collectors.minBy(Comparator.<Integer>naturalOrder()),
                Collectors.maxBy(Comparator.<Integer>naturalOrder()),
                (min, max) -> min.map(lo -> new MinMax(lo, max.orElseThrow()))));
    }

    /** The average of the numbers; 0.0 when there are none. */
    public static double mean(List<Integer> numbers) {
        return numbers.stream().collect(Collectors.teeing(
                Collectors.counting(),
                Collectors.summingDouble(Integer::doubleValue),
                (count, sum) -> sum / count));
    }
}
