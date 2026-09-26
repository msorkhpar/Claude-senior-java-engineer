package practice;

import java.util.*;
import java.util.stream.Collectors;

public final class OnePass {

    private OnePass() {
    }

    public record MinMax(int min, int max) {
    }

    public static Optional<MinMax> range(List<Integer> numbers) {
        return numbers.stream().collect(Collectors.teeing(
                Collectors.minBy(Comparator.<Integer>naturalOrder()),
                Collectors.maxBy(Comparator.<Integer>naturalOrder()),
                (min, max) -> min.map(lo -> new MinMax(lo, max.orElseThrow()))));
    }

    public static double mean(List<Integer> numbers) {
        int sum = numbers.stream().mapToInt(Integer::intValue).sum();
        return sum > 0 ? (double) sum / numbers.size() : 0.0;
    }
}
