package practice;

import java.util.concurrent.Semaphore;

/** A fixed stock of buffers, leased several at a time. */
public final class Leases {

    private final Semaphore buffers;

    public Leases(int buffers) {
        this.buffers = new Semaphore(buffers);
    }

    /** Waits until {@code n} buffers are free, then takes them all at once. */
    public void lease(int n) throws InterruptedException {
        buffers.acquire(n);
    }

    /** Takes {@code n} buffers only if all are free now; otherwise takes none. */
    public boolean tryLease(int n) {
        for (int i = 0; i < n; i++) {
            if (!buffers.tryAcquire()) {
                return false;
            }
        }
        return true;
    }

    /** Returns {@code n} buffers. */
    public void giveBack(int n) {
        buffers.release(n);
    }

    /** The number of free buffers. */
    public int available() {
        return buffers.availablePermits();
    }
}
