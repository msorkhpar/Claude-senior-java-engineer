package practice;

import java.util.concurrent.Semaphore;

public class ResourcePool {

    private final int capacity;

    private final Semaphore semaphore;

    /** Creates a pool of capacity resources. */
    public ResourcePool(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
        this.semaphore = new Semaphore(capacity);
    }

    /** Takes a resource if one is free, without waiting. */
    public boolean tryAcquire() {
        return semaphore.tryAcquire();
    }

    /** Gives a resource back. */
    public void release() {
        if (semaphore.availablePermits() >= capacity) {
            throw new IllegalStateException("release without a matching acquire");
        }
        semaphore.release();
    }

    /** Returns how many resources are free. */
    public int available() {
        return semaphore.availablePermits();
    }
}
