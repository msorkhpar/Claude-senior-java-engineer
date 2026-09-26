package practice;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class Sources {

    private static int next;
    private static int position;

    private Sources() {
    }

    /** Returns an IntSupplier yielding start, start + step, start + 2 * step, ... */
    public static IntSupplier counter(int start, int step) {
        next = start;
        return () -> {
            int value = next;
            next += step;
            return value;
        };
    }

    /** Returns a Supplier yielding the items in order, over and over; rejects an empty list. */
    public static <T> Supplier<T> cycling(List<T> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
        List<T> snapshot = List.copyOf(items);
        position = 0;
        return () -> {
            T item = snapshot.get(position);
            position = (position + 1) % snapshot.size();
            return item;
        };
    }
}
