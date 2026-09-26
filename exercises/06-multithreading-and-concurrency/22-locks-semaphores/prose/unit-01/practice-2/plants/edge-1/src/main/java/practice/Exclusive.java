package practice;

import java.util.concurrent.locks.ReentrantLock;

/** Runs actions one at a time under the lock it is given. */
public final class Exclusive {

    private final ReentrantLock lock;

    public Exclusive(ReentrantLock lock) {
        this.lock = lock;
    }

    /** Runs {@code action} holding the lock; a caller interrupted while waiting leaves with InterruptedException. */
    public void run(Runnable action) throws InterruptedException {
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }
}
