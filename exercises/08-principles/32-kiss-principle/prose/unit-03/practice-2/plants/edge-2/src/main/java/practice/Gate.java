package practice;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * The nightly settlement jobs all append to one ledger file, so they pass through this gate:
 * one job at a time.
 */
public final class Gate {

    private final ReentrantLock lock = new ReentrantLock();

    /** Runs job while no other job runs in this gate, and returns what it returned. */
    public <T> T inside(Supplier<T> job) {
        lock.lock();
        T result = job.get();
        lock.unlock();
        return result;
    }
}
