package practice;

import java.util.ArrayList;
import java.util.List;

public final class Growth {

    private Growth() {
    }

    /** The array lengths new ArrayList<>(initialCapacity) goes through while elements are added. */
    public static List<Integer> capacities(int initialCapacity, int elements) {
        List<Integer> out = new ArrayList<>();
        int capacity = initialCapacity;
        out.add(capacity);
        for (int size = 0; size < elements; size++) {
            if (size == capacity) {
                capacity = capacity + (capacity >> 1) + (capacity == 0 ? 1 : 0);
                out.add(capacity);
            }
        }
        return out;
    }

    /** The array lengths new ArrayList<>() goes through while elements are added. */
    public static List<Integer> defaultCapacities(int elements) {
        List<Integer> out = new ArrayList<>();
        out.add(0);
        if (elements > 0) {
            List<Integer> rest = capacities(10, elements);
            out.addAll(rest);
        }
        return out;
    }
}
