package practice;

import java.util.concurrent.ConcurrentHashMap;

public final class Visits {

    private Visits() {
    }

    /** Adds one to page's count (1 for a new page); safe to call from many threads at once. */
    public static void record(ConcurrentHashMap<String, Integer> counts, String page) {
        counts.computeIfPresent(page, (k, v) -> v + 1);
    }
}
