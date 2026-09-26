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
            String item = it.next();
            if (item != null && item.equalsIgnoreCase(target)) {
                it.remove();
            }
        }
    }
}
