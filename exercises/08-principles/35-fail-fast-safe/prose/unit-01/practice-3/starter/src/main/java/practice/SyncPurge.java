package practice;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public final class SyncPurge {

    private SyncPurge() {
    }

    /** Removes every element doomed names, holding the wrapper's lock throughout. */
    public static void removeIf(List<String> syncList, Predicate<String> doomed) {
        throw new UnsupportedOperationException("write removeIf");
    }
}
