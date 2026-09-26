package practice;

import java.util.List;
import java.util.function.ObjIntConsumer;

public final class Indexed {

    private Indexed() {
    }

    /** Calls {@code action} with every element and its zero-based index, in order. */
    public static <T> void forEachIndexed(List<T> items, ObjIntConsumer<T> action) {
        throw new UnsupportedOperationException("write forEachIndexed");
    }

    /** Counts how often each value 0..buckets-1 occurs; other values are ignored. */
    public static int[] histogram(int[] values, int buckets) {
        throw new UnsupportedOperationException("write histogram");
    }
}
