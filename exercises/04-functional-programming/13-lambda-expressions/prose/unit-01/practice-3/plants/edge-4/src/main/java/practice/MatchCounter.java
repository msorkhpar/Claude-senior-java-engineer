package practice;

import java.util.List;
import java.util.function.Predicate;

public class MatchCounter {

    private int count;

    /** How many non-null items of {@code items} the {@code condition} accepts. */
    public int countMatches(List<String> items, Predicate<String> condition) {
        count = 0;
        items.forEach(item -> {
            if (item != null && condition.test(item)) {
                count++;
            }
        });
        return count;
    }
}
