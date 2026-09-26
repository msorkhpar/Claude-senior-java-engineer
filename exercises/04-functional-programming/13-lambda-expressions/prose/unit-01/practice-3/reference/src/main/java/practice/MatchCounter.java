package practice;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class MatchCounter {

    /** How many non-null items of {@code items} the {@code condition} accepts. */
    public int countMatches(List<String> items, Predicate<String> condition) {
        return (int) items.stream()
                .filter(Objects::nonNull)
                .filter(condition)
                .count();
    }
}
