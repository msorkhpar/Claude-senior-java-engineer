package practice;

import java.util.List;
import java.util.concurrent.TimeUnit;

/** A fixed pool of resources whose free count is a semaphore's permits. */
public final class Pool<R> {

    public Pool(List<R> resources) {
    }

    /** Waits until a resource is free and returns it. */
    public R acquire() throws InterruptedException {
        throw new UnsupportedOperationException("write acquire");
    }

    /** Waits at most {@code timeout} for a free resource; null if none came free. */
    public R tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("write tryAcquire");
    }

    /** Returns a currently leased resource; anything else is refused with IllegalArgumentException. */
    public void release(R resource) {
        throw new UnsupportedOperationException("write release");
    }

    /** The number of free resources. */
    public int available() {
        throw new UnsupportedOperationException("write available");
    }
}
