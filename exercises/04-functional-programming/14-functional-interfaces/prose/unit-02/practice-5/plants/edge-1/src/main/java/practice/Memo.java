package practice;

import java.util.function.Supplier;

public final class Memo {

    private Memo() {
    }

    /** Returns a Supplier that runs {@code source} once, on first use, and caches its result. */
    public static <T> Supplier<T> memoize(Supplier<T> source) {
        T value = source.get();
        return () -> value;
    }
}
