package practice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Loyalty points per member. Partner files are imported on worker threads, one file at a time
 * under the ledger lock; a deploy stops the workers by interrupting them.
 */
public class PointsLedger {

    /** One line of a partner file: points to add (or, if negative, to take) for a member. */
    public record Entry(String member, int points) {}

    final ReentrantLock lock = new ReentrantLock();
    private final Map<String, Integer> balances = new HashMap<>();

    /**
     * Applies a whole file under the ledger lock. Throws IllegalArgumentException, and changes
     * nothing, when the file would take a member below zero.
     */
    public void importFile(List<Entry> file) throws InterruptedException {
        try {
            lock.lockInterruptibly();
            applyAll(file);
        } finally {
            lock.unlock();
        }
    }

    /** Checks the whole file, then applies it. Call it only while holding the lock. */
    void applyAll(List<Entry> file) {
        Map<String, Integer> after = new HashMap<>(balances);
        for (Entry e : file) {
            int balance = after.getOrDefault(e.member(), 0) + e.points();
            if (balance < 0) {
                throw new IllegalArgumentException(e.member() + " would end at " + balance + " points");
            }
            after.put(e.member(), balance);
        }
        balances.putAll(after);
    }

    public int balance(String member) {
        lock.lock();
        try {
            return balances.getOrDefault(member, 0);
        } finally {
            lock.unlock();
        }
    }
}
