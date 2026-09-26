package practice;

import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** Lets at most {@code limit} tasks run at once and rejects the rest without waiting. */
public final class Bulkhead {

    private final Semaphore slots;

    public Bulkhead(int limit) {
        this.slots = new Semaphore(limit);
    }

    /** Runs {@code task} if a slot is free right now; otherwise returns empty without running it. */
    public <T> Optional<T> tryCall(Supplier<T> task) {
        try {
            if (!slots.tryAcquire(100, TimeUnit.MILLISECONDS)) {
                return Optional.empty();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
        try {
            return Optional.of(task.get());
        } finally {
            slots.release();
        }
    }

    /** The number of free slots. */
    public int available() {
        return slots.availablePermits();
    }
}
