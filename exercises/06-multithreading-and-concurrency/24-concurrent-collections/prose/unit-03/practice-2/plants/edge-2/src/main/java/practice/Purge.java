package practice;

import java.util.List;

public final class Purge {

    private Purge() {
    }

    /** Removes every element equal to target, in place; returns how many were removed. */
    public static int removeAll(List<String> list, String target) {
        int before = list.size();
        list.removeIf(s -> s == target);
        return before - list.size();
    }
}
