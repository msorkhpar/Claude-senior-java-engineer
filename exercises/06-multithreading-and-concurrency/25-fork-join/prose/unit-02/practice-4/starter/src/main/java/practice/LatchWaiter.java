package practice;

import java.util.concurrent.CountDownLatch;

public final class LatchWaiter {

    private LatchWaiter() {
    }

    /** Waits, through ForkJoinPool.managedBlock, until the latch reaches zero. */
    public static void await(CountDownLatch latch) throws InterruptedException {
        throw new UnsupportedOperationException("write await");
    }
}
