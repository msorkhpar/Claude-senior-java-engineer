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
        if (numbers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(numbers.stream().collect(Collectors.teeing(
                Collectors.reducing(Integer.MAX_VALUE, Integer::min),
                Collectors.reducing(0, Integer::max),
                MinMax::new)));
    }

    /** The average of the numbers; 0.0 when there are none. */
    public static double mean(List<Integer> numbers) {
        return numbers.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }
}
