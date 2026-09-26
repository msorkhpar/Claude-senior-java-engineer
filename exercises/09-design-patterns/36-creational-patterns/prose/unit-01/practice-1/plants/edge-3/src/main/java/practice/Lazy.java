package practice;

import java.util.Objects;
import java.util.function.Supplier;

public final class Lazy<T> {

    private final Supplier<? extends T> supplier;
    private volatile T value;

    private Lazy(Supplier<? extends T> supplier) {
        this.supplier = Objects.requireNonNull(supplier);
    }

    /** Returns a holder that will call {@code supplier} on the first {@link #get()}. */
    public static <T> Lazy<T> of(Supplier<? extends T> supplier) {
        return new Lazy<>(supplier);
    }

    /** Returns the value, creating it on the first call. */
    public T get() {
        T result = value;
        if (result == null) {
            synchronized (this) {
                result = supplier.get();
                value = result;
            }
        }
        return result;
    }
}
