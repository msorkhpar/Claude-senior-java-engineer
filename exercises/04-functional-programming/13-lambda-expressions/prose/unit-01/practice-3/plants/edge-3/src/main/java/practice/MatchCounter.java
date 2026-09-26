package practice;

import java.util.List;
import java.util.function.Predicate;

public class MatchCounter {

    /** How many non-null items of {@code items} the {@code condition} accepts. */
    public int countMatches(List<String> items, Predicate<String> condition) {
        return (int) items.stream()
                .filter(item -> {
                    try {
                        return condition.test(item);
                    } catch (NullPointerException e) {
                        return false;
                    }
                })
                .filter(item -> item != null)
                .count();
    }
}
