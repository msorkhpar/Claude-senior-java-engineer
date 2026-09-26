package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public final class AuditLog {
    private final ReentrantLock lock = new ReentrantLock(true);
    private final List<String> entries = new ArrayList<>();

    public void record(String entry) {
        lock.lock();
        entries.add(entry);
        lock.unlock();
    }

    public void recordAll(List<String> batch, Runnable afterEach) {
        lock.lock();
        for (String entry : batch) {
            record(entry);
            afterEach.run();
        }
        lock.unlock();
    }

    public String entries() {
        lock.lock();
        try { return String.join(",", entries); } finally { lock.unlock(); }
    }
}
