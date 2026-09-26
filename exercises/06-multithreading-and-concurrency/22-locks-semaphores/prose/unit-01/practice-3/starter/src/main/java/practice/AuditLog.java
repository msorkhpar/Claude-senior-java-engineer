package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/** An append-only log whose batches are never interleaved with other entries. */
public final class AuditLog {

    /** Fair: a thread that waits for the lock gets it before a thread that asks later. */
    private final ReentrantLock lock = new ReentrantLock(true);
    private final List<String> entries = new ArrayList<>();

    /** Appends one entry under the lock. */
    public void record(String entry) {
        throw new UnsupportedOperationException("write record");
    }

    /** Records every entry through {@link #record}, holding the lock for the whole batch. */
    public void recordAll(List<String> batch, Runnable afterEach) {
        throw new UnsupportedOperationException("write recordAll");
    }

    /** Every entry so far, joined by ",". */
    public String entries() {
        throw new UnsupportedOperationException("write entries");
    }
}
