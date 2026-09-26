package practice;

import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

public final class Ranks {

    private Ranks() {
    }

    /** The names from..to, both inclusive, sorted, as a new set. */
    public static NavigableSet<String> between(ConcurrentSkipListSet<String> set, String from, String to) {
        throw new UnsupportedOperationException("write between");
    }

    /** [first, last], or an empty list for an empty set. */
    public static List<String> ends(ConcurrentSkipListSet<String> set) {
        throw new UnsupportedOperationException("write ends");
    }
}
