package practice;

import java.util.function.Supplier;

public final class Memo {

    private Memo() {
    }

    /** Returns a Supplier that runs {@code source} once, on first use, and caches its result. */
    public static <T> Supplier<T> memoize(Supplier<T> source) {
        Object lock = new Object();
        Object[] cached = {null};
        return () -> {
            synchronized (lock) {
                if (cached[0] == null) {
                    cached[0] = source.get();
                }
                @SuppressWarnings("unchecked")
                T value = (T) cached[0];
                return value;
            }
        };
    }
}
