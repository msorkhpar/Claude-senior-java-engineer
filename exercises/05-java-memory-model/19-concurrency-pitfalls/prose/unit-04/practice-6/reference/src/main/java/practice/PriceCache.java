package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public final class PriceCache {

    private final Map<String, Integer> prices = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /** Stores a price under the write lock. */
    public void put(String product, Integer price) {
        lock.writeLock().lock();
        try {
            prices.put(product, price);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** The price, or null, read under the read lock. */
    public Integer get(String product) {
        lock.readLock().lock();
        try {
            return prices.get(product);
        } finally {
            lock.readLock().unlock();
        }
    }

    /** Applies reader to the price while holding the read lock. */
    public <R> R view(String product, Function<Integer, R> reader) {
        lock.readLock().lock();
        try {
            return reader.apply(prices.get(product));
        } finally {
            lock.readLock().unlock();
        }
    }

    /** A copy of every price, taken under the read lock. */
    public Map<String, Integer> snapshot() {
        lock.readLock().lock();
        try {
            return new HashMap<>(prices);
        } finally {
            lock.readLock().unlock();
        }
    }
}
