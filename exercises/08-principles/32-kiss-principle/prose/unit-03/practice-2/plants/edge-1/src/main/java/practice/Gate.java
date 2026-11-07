package practice;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * The nightly settlement jobs all append to one ledger file, so they pass through this gate:
 * one job at a time.
 */
public final class Gate {

    private final AtomicBoolean busy = new AtomicBoolean();

    /** Runs job while no other job runs in this gate, and returns what it returned. */
    public <T> T inside(Supplier<T> job) {
        while (!busy.compareAndSet(false, true)) Thread.onSpinWait();
        try {
            return job.get();
        } finally {
            busy.set(false);
        }
    }
}
