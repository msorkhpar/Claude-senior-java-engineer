package practice;

import java.util.function.Supplier;

public final class Memo {

    private Memo() {
    }

    /** Returns a Supplier that runs {@code source} once, on first use, and caches its result. */
    public static <T> Supplier<T> memoize(Supplier<T> source) {
        Object lock = new Object();
        Object[] cached = {null};
        boolean[] computed = {false};
        return () -> {
            synchronized (lock) {
                if (!computed[0]) {
                    cached[0] = source.get();
                    computed[0] = true;
                }
                @SuppressWarnings("unchecked")
                T value = (T) cached[0];
                return value;
            }
        };
    }
}
