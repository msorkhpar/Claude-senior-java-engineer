package practice;

import java.util.Map;
import java.util.function.Function;

public final class PriceCache {

    /** Stores a price under the write lock. */
    public void put(String product, Integer price) {
        throw new UnsupportedOperationException("write put");
    }

    /** The price, or null, read under the read lock. */
    public Integer get(String product) {
        throw new UnsupportedOperationException("write get");
    }

    /** Applies reader to the price while holding the read lock. */
    public <R> R view(String product, Function<Integer, R> reader) {
        throw new UnsupportedOperationException("write view");
    }

    /** A copy of every price, taken under the read lock. */
    public Map<String, Integer> snapshot() {
        throw new UnsupportedOperationException("write snapshot");
    }
}
