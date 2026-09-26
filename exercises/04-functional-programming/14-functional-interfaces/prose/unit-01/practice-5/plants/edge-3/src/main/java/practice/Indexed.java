package practice;

import java.util.List;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;

public final class Indexed {
    private Indexed() {
    }

    public static <T> void forEachIndexed(List<T> items, ObjIntConsumer<T> action) {
        IntStream.range(0, items.size()).forEach(i -> action.accept(items.get(i), i));
    }

    public static int[] histogram(int[] values, int buckets) {
        int[] counts = new int[buckets];
        IntStream.of(values).parallel().filter(v -> v >= 0 && v < buckets).forEach(v -> counts[v]++);
        return counts;
    }
}
