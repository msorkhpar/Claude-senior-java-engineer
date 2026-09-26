package practice;

import java.util.List;

public final class Purge {

    private Purge() {
    }

    /** Removes every element equal to target, in place; returns how many were removed. */
    public static int removeAll(List<String> list, String target) {
        int removed = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(target)) {
                list.remove(i);
                removed++;
            }
        }
        return removed;
    }
}
