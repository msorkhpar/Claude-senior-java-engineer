package practice;

import java.util.ArrayList;
import java.util.List;

public final class Resizes {

    private Resizes() {
    }

    /** The table capacities a map starting at capacity passes through while entries are put one by one. */
    public static List<Integer> capacities(int capacity, float loadFactor, int entries) {
        List<Integer> out = new ArrayList<>();
        out.add(capacity);
        for (int size = 1; size <= entries; size++) {
            if (size > (int) (capacity * loadFactor)) {
                capacity *= 2;
                out.add(capacity);
            }
        }
        return out;
    }

    /** The page's initial capacity for holding entries at load factor 0.75 with no resize. */
    public static int initialCapacityFor(int entries) {
        return (int) (entries / 0.75f) + 1;
    }
}
