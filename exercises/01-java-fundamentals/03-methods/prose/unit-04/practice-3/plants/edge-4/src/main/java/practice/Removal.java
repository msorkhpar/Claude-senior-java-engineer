package practice;

import java.util.List;

public final class Removal {

    private Removal() {
    }

    /** Removes the first element equal to value; returns whether one was removed. */
    public static boolean removeValue(List<Integer> values, int value) {
        Integer target = value;
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) == target) {
                values.remove(i);
                return true;
            }
        }
        return false;
    }
}
