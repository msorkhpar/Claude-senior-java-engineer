package practice;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The seat map of one concert. Customers' hold requests and the box-office re-sync, which can keep
 * the map locked for several seconds, share one lock.
 */
public class SeatMap {

    public enum Answer { HELD, TAKEN, BUSY }

    final ReentrantLock lock = new ReentrantLock();
    private final Map<String, String> holders = new HashMap<>();
    private final Duration budget;

    /** @param budget how long a hold request may wait for the map before it answers */
    public SeatMap(Duration budget) {
        this.budget = budget;
    }

    /**
     * Holds the seat for the customer. Answers HELD or TAKEN, or BUSY when the map stayed locked
     * for longer than the budget; a BUSY answer changes nothing.
     */
    public Answer hold(String seat, String customer) throws InterruptedException {
        lock.lock();
        try {
            return holdLocked(seat, customer);
        } finally {
            lock.unlock();
        }
    }

    /** Does the hold. Call it only while holding the lock. */
    Answer holdLocked(String seat, String customer) {
        String holder = holders.putIfAbsent(seat, customer);
        return holder == null || holder.equals(customer) ? Answer.HELD : Answer.TAKEN;
    }

    /** Who holds the seat, or null. */
    public String holder(String seat) {
        lock.lock();
        try {
            return holders.get(seat);
        } finally {
            lock.unlock();
        }
    }
}
