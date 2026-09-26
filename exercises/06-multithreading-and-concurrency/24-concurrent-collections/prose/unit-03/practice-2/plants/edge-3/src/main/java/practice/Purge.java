package practice;

import java.util.Iterator;
import java.util.List;

public final class Purge {

    private Purge() {
    }

    /** Removes every element equal to target, in place; returns how many were removed. */
    public static int removeAll(List<String> list, String target) {
        int removed = 0;
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            if (it.next().equals(target)) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }
}
