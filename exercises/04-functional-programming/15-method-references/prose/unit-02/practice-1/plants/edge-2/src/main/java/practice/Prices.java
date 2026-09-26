package practice;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class Prices {

    private Prices() {
    }

    /** Parses every usable entry in order; nulls and blank entries are skipped, spaces ignored. */
    public static List<Integer> parse(List<String> raw) {
        return raw.stream()
                .filter(Objects::nonNull)
                .filter(Predicate.not(String::isBlank))
                .map(Integer::parseInt)
                .toList();
    }

    /** Each value's absolute value, in order. */
    public static List<Integer> magnitudes(List<Integer> values) {
        return values.stream().map(Math::abs).toList();
    }
}
