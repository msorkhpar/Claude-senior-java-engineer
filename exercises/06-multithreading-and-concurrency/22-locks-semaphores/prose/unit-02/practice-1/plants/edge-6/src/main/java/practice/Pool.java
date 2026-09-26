package practice;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public final class Pool<R> {
    private final Semaphore permits;
    private final Queue<R> free;
    private final Set<R> leased = Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

    public Pool(List<R> resources) {
        this.free = new ConcurrentLinkedQueue<>(resources);
        this.permits = new Semaphore(resources.size(), true);
    }

    public R acquire() throws InterruptedException {
        permits.acquire();
        return lease();
    }

    public R tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
        if (!permits.tryAcquire(unit.toSeconds(timeout), TimeUnit.SECONDS)) return null;
        return lease();
    }

    private R lease() {
        R r = free.poll();
        leased.add(r);
        return r;
    }

    public void release(R resource) {
        if (!leased.remove(resource)) throw new IllegalArgumentException("not leased");
        free.offer(resource);
        permits.release();
    }

    public int available() { return permits.availablePermits(); }
}
