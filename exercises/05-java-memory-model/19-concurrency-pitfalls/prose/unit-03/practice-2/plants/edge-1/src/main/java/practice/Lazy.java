package practice;

import java.util.Objects;
import java.util.function.Supplier;

public final class Lazy<T> {

    private final Supplier<T> factory;
    private volatile T value;

    public Lazy(Supplier<T> factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    /** The value, built by the factory exactly once, on first use. */
    public T get() {
        T result = value;
        if (result == null) {
            result = Objects.requireNonNull(factory.get(), "the factory returned null");
            value = result;
        }
        return result;
    }

    public boolean isInitialized() {
        return value != null;
    }
}
