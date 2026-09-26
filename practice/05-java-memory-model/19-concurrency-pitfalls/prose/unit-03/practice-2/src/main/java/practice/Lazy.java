package practice;

import java.util.Objects;
import java.util.function.Supplier;

public final class Lazy<T> {

    private final Supplier<T> factory;

    public Lazy(Supplier<T> factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    /** The value, built by the factory exactly once, on first use. */
    public T get() {
        throw new UnsupportedOperationException("write get");
    }

    public boolean isInitialized() {
        throw new UnsupportedOperationException("write isInitialized");
    }
}
