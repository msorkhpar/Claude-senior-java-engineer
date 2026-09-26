package practice;

import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class Bulkhead {
    private final int limit;
    private final Semaphore slots;
    private final AtomicInteger inUse = new AtomicInteger();

    public Bulkhead(int limit) { this.limit = limit; this.slots = new Semaphore(limit); }

    public <T> Optional<T> tryCall(Supplier<T> task) {
        boolean got;
        try { got = slots.tryAcquire(1, TimeUnit.MILLISECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); return Optional.empty(); }
        if (!got) return Optional.empty();
        try { return Optional.of(task.get()); } finally { slots.release(); }
    }

    public int available() { return slots.availablePermits(); }
}
