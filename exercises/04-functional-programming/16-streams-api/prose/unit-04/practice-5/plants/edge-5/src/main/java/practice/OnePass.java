package practice;

import java.util.*;
import java.util.stream.Collectors;

public final class OnePass {

    private OnePass() {
    }

    public record MinMax(int min, int max) {
    }

    public static Optional<MinMax> range(List<Integer> numbers) {
        MinMax r = numbers.stream().reduce(new MinMax(Integer.MAX_VALUE, Integer.MIN_VALUE),
                (acc, n) -> new MinMax(Math.min(acc.min(), n), Math.max(acc.max(), n)),
                (a, b) -> new MinMax(Math.min(a.min(), b.min()), Math.max(a.max(), b.max())));
        return r.min() == Integer.MAX_VALUE ? Optional.empty() : Optional.of(r);
    }

    public static double mean(List<Integer> numbers) {
        return numbers.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }
}
