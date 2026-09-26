package practice;

import java.util.Optional;
import java.util.function.Supplier;

/** Lets at most {@code limit} tasks run at once and rejects the rest without waiting. */
public final class Bulkhead {

    public Bulkhead(int limit) {
    }

    /** Runs {@code task} if a slot is free right now; otherwise returns empty without running it. */
    public <T> Optional<T> tryCall(Supplier<T> task) {
        throw new UnsupportedOperationException("write tryCall");
    }

    /** The number of free slots. */
    public int available() {
        throw new UnsupportedOperationException("write available");
    }
}
