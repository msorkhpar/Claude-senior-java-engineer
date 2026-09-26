package practice;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public final class Exclusive {
    private final ReentrantLock lock;

    public Exclusive(ReentrantLock lock) { this.lock = lock; }

    public void run(Runnable action) throws InterruptedException {
        if (!lock.tryLock(5, TimeUnit.SECONDS)) {
            return;
        }
        try { action.run(); } finally { lock.unlock(); }
    }
}
