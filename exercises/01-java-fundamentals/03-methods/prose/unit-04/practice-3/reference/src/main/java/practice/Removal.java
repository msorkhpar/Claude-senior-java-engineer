package practice;

import java.util.List;

public final class Removal {

    private Removal() {
    }

    /** Removes the first element equal to value; returns whether one was removed. */
    public static boolean removeValue(List<Integer> values, int value) {
        return values.remove(Integer.valueOf(value));
    }
}
