package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Shipping rates by destination zone, in cents. Checkout threads read them all day; a zone that
 * isn't cached yet is loaded once from the slow rates service; the tariff job replaces rates.
 */
public class RateCache {

    final ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    private final Map<String, Integer> rates = new HashMap<>();
    private final ToIntFunction<String> ratesService;

    public RateCache(ToIntFunction<String> ratesService) {
        this.ratesService = ratesService;
    }

    /** The rate for the zone, loaded from the rates service the first time it is asked for. */
    public int rateFor(String zone) {
        rw.readLock().lock();
        try {
            Integer cached = rates.get(zone);
            if (cached != null) return cached;
        } finally {
            rw.readLock().unlock();
        }
        rw.writeLock().lock();
        try {
            Integer cached = rates.get(zone);
            if (cached != null) return cached;
            int loaded = ratesService.applyAsInt(zone);
            rates.put(zone, loaded);
            return loaded;
        } finally {
            rw.writeLock().unlock();
        }
    }

    /** The tariff job sets a new rate for the zone. */
    public void replace(String zone, int cents) {
        rw.writeLock().lock();
        try {
            rates.put(zone, cents);
        } finally {
            rw.writeLock().unlock();
        }
    }
}
