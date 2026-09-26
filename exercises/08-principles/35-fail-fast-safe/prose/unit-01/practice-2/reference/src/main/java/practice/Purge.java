package practice;

import java.util.Iterator;
import java.util.List;

public final class Purge {

    private Purge() {
    }

    /** Removes every element equal to target from the caller's list, in place. */
    public static void removeAll(List<String> items, String target) {
        Iterator<String> it = items.iterator();
        while (it.hasNext()) {
            if (target.equals(it.next())) {
                it.remove();
            }
        }
    }
}
