package practice;

import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

/** Lets at most {@code limit} tasks run at once and rejects the rest without waiting. */
public final class Bulkhead {

    private final Semaphore slots;

    public Bulkhead(int limit) {
        this.slots = new Semaphore(limit);
    }

    /** Runs {@code task} if a slot is free right now; otherwise returns empty without running it. */
    public <T> Optional<T> tryCall(Supplier<T> task) {
        if (!slots.tryAcquire()) {
            return Optional.empty();
        }
        T result;
        try {
            result = task.get();
        } catch (RuntimeException e) {
            slots.release();
            throw e;
        }
        slots.release();
        return Optional.of(result);
    }

    /** The number of free slots. */
    public int available() {
        return slots.availablePermits();
    }
}
