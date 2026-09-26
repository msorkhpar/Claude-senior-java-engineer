package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public final class PriceCache {

    private final Map<String, Integer> prices = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    /** Stores a price under the write lock. */
    public void put(String product, Integer price) {
        lock.lock();
        try {
            prices.put(product, price);
        } finally {
            lock.unlock();
        }
    }

    /** The price, or null, read under the read lock. */
    public Integer get(String product) {
        lock.lock();
        try {
            return prices.get(product);
        } finally {
            lock.unlock();
        }
    }

    /** Applies reader to the price while holding the read lock. */
    public <R> R view(String product, Function<Integer, R> reader) {
        lock.lock();
        try {
            return reader.apply(prices.get(product));
        } finally {
            lock.unlock();
        }
    }

    /** A copy of every price, taken under the read lock. */
    public Map<String, Integer> snapshot() {
        lock.lock();
        try {
            return new HashMap<>(prices);
        } finally {
            lock.unlock();
        }
    }
}
