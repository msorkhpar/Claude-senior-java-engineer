package practice;

import java.util.concurrent.Semaphore;

public class ResourcePool {

    private final int capacity;

    private final Semaphore semaphore;

    /** Creates a pool of capacity resources. */
    public ResourcePool(int capacity) {
        throw new UnsupportedOperationException("write ResourcePool");
    }

    /** Takes a resource if one is free, without waiting. */
    public boolean tryAcquire() {
        throw new UnsupportedOperationException("write tryAcquire");
    }

    /** Gives a resource back. */
    public void release() {
        throw new UnsupportedOperationException("write release");
    }

    /** Returns how many resources are free. */
    public int available() {
        throw new UnsupportedOperationException("write available");
    }
}
