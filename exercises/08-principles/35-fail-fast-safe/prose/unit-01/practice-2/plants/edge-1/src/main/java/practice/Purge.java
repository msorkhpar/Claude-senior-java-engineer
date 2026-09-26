package practice;

import java.util.Iterator;
import java.util.List;

public final class Purge {

    private Purge() {
    }

    /** Removes every element equal to target from the caller's list, in place. */
    public static void removeAll(List<String> items, String target) {
        for (int i = 0; i < items.size(); i++) {
            if (target.equals(items.get(i))) {
                items.remove(i);
            }
        }
    }
}
