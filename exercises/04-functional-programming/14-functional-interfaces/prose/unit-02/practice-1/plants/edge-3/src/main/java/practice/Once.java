package practice;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Hands every caller the value its source produced the first time anyone asked. The FX service
 * wraps its rate-card loader in one of these and shares it between all request threads.
 */
public final class Once<T> implements Supplier<T> {

    private final Supplier<T> source;
    private final Object lock = new Object();
    private volatile T value;

    public Once(Supplier<T> source) {
        this.source = Objects.requireNonNull(source, "source");
    }

    /** The source's value: loaded on the first call, the same value on every call after that. */
    @Override
    public T get() {
        if (value == null) {
            synchronized (lock) {
                if (value == null) {
                    value = source.get();
                }
            }
        }
        return value;
    }
}
