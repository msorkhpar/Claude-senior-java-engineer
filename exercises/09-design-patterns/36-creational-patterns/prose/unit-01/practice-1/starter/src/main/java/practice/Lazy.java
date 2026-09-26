package practice;

import java.util.function.Supplier;

public final class Lazy<T> {

    private Lazy(Supplier<? extends T> supplier) {
    }

    /** Returns a holder that will call {@code supplier} on the first {@link #get()}. */
    public static <T> Lazy<T> of(Supplier<? extends T> supplier) {
        throw new UnsupportedOperationException("write of");
    }

    /** Returns the value, creating it on the first call. */
    public T get() {
        throw new UnsupportedOperationException("write get");
    }
}
