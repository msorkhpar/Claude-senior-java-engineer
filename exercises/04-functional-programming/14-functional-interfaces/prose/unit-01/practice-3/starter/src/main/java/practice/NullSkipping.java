package practice;

import java.util.List;
import java.util.function.Consumer;

public final class NullSkipping {

    private NullSkipping() {
    }

    /** Returns a consumer that forwards non-null inputs to {@code downstream} and ignores null. */
    public static <T> Consumer<T> nullSafe(Consumer<T> downstream) {
        throw new UnsupportedOperationException("write nullSafe");
    }

    /** Runs {@code action} on every non-null item, in order; returns how many items it ran on. */
    public static <T> int forEachCounting(List<T> items, Consumer<T> action) {
        throw new UnsupportedOperationException("write forEachCounting");
    }
}
