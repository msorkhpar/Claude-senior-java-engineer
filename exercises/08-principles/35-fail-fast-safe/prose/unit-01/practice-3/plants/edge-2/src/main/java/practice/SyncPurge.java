package practice;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public final class SyncPurge {

    private SyncPurge() {
    }

    /** Removes every element doomed names, holding the wrapper's lock throughout. */
    public static void removeIf(List<String> syncList, Predicate<String> doomed) {
        List<String> gone = new java.util.ArrayList<>();
        synchronized (syncList) {
            for (String s : syncList) {
                if (doomed.test(s)) {
                    gone.add(s);
                }
            }
        }
        syncList.removeAll(gone);
    }
}
