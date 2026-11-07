package practice;

import java.util.function.Supplier;

/**
 * The nightly settlement jobs all append to one ledger file, so they pass through this gate:
 * one job at a time.
 */
public final class Gate {

    /** Runs job while no other job runs in this gate, and returns what it returned. */
    public <T> T inside(Supplier<T> job) {
        synchronized (this) {
            return job.get();
        }
    }
}
