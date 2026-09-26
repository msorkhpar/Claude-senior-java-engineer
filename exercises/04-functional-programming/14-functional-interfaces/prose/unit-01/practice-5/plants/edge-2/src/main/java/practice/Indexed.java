package practice;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;

public final class Indexed {

    private Indexed() {
    }

    /** Calls {@code action} with every element and its zero-based index, in order. */
    public static <T> void forEachIndexed(List<T> items, ObjIntConsumer<T> action) {
        for (int i = 0; i < items.size(); i++) {
            action.accept(items.get(i), i);
        }
    }

    /** Counts how often each value 0..buckets-1 occurs; other values are ignored. */
    public static int[] histogram(int[] values, int buckets) {
        int[] counts = new int[buckets];
        IntConsumer tally = v -> counts[v]++;
        Arrays.stream(values).forEach(tally);
        return counts;
    }
}
