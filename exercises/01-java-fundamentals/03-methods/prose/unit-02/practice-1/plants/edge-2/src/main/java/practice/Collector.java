package practice;

import java.util.List;

public final class Collector {

    private Collector() {
    }

    /** Adds every non-null item to target and returns how many were added. */
    public static int addAll(List<String> target, String... items) {
        for (String item : items) {
            target.add(item);
        }
        return items.length;
    }
}
