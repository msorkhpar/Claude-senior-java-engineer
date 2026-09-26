package practice;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public final class States {

    private States() {
    }

    /** Returns a daemon thread that is in {@code state}, or soon will be, until the test lets it go. */
    public static Thread enter(Thread.State state, Object monitor, ReentrantLock lock, CountDownLatch done)
            throws InterruptedException {
        throw new UnsupportedOperationException("write enter");
    }
}
