package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

public final class Ranks {

    private Ranks() {
    }

    /** The names from..to, both inclusive, sorted, as a new set. */
    public static NavigableSet<String> between(ConcurrentSkipListSet<String> set, String from, String to) {
        return new TreeSet<>(set.subSet(from, true, to, true));
    }

    /** [first, last], or an empty list for an empty set. */
    public static List<String> ends(ConcurrentSkipListSet<String> set) {
        List<String> all = new ArrayList<>(set);
        return all.isEmpty() ? List.of() : List.of(all.get(0), all.get(all.size() - 1));
    }
}
