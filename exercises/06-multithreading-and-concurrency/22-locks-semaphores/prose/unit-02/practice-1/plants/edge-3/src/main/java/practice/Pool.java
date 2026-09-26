package practice;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/** A fixed pool of resources whose free count is a semaphore's permits. */
public final class Pool<R> {

    private final Semaphore permits;
    private final Queue<R> free;
    private final Set<R> leased = Collections.newSetFromMap(new IdentityHashMap<>());

    public Pool(List<R> resources) {
        this.free = new ConcurrentLinkedQueue<>(resources);
        this.permits = new Semaphore(resources.size(), true);
    }

    /** Waits until a resource is free and returns it. */
    public R acquire() throws InterruptedException {
        permits.acquire();
        return lease();
    }

    /** Waits at most {@code timeout} for a free resource; null if none came free. */
    public R tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
        if (!permits.tryAcquire()) {
            return null;
        }
        return lease();
    }

    private R lease() {
        R resource = free.poll();
        synchronized (leased) {
            leased.add(resource);
        }
        return resource;
    }

    /** Returns a currently leased resource; anything else is refused with IllegalArgumentException. */
    public void release(R resource) {
        synchronized (leased) {
            if (!leased.remove(resource)) {
                throw new IllegalArgumentException("not leased from this pool: " + resource);
            }
        }
        free.offer(resource);
        permits.release();
    }

    /** The number of free resources. */
    public int available() {
        return permits.availablePermits();
    }
}
