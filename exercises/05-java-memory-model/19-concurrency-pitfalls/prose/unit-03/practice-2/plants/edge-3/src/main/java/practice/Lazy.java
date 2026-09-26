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
    public synchronized T get() {
        if (value == null) {
            value = Objects.requireNonNull(factory.get(), "the factory returned null");
        }
        return value;
    }

    public boolean isInitialized() {
        return value != null;
    }
}
