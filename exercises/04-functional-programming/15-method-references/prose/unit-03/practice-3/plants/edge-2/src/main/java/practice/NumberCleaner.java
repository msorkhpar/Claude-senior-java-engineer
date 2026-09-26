package practice;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class NumberCleaner {

    private NumberCleaner() {
    }

    /** Every usable number in the batches, once each, highest first. */
    public static List<Integer> clean(List<List<String>> batches) {
        return batches.stream()
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .filter(Predicate.not(String::isEmpty))
                .map(String::trim)
                .map(Integer::parseInt)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
    }
}
