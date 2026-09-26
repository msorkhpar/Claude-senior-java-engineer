package practice;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class Sources {

    private Sources() {
    }

    /** Returns an IntSupplier yielding start, start + step, start + 2 * step, ... */
    public static IntSupplier counter(int start, int step) {
        throw new UnsupportedOperationException("write counter");
    }

    /** Returns a Supplier yielding the items in order, over and over; rejects an empty list. */
    public static <T> Supplier<T> cycling(List<T> items) {
        throw new UnsupportedOperationException("write cycling");
    }
}
