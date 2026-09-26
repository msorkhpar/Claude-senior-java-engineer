package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/** A read-mostly map: shared reads, exclusive writes. */
public final class Catalog {

    private final Map<String, String> entries = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /** Reads {@code key} under the read lock, calling {@code whileReading} while holding it. */
    public String lookup(String key, Runnable whileReading) {
        throw new UnsupportedOperationException("write lookup");
    }

    /** Stores {@code value} under the write lock, then calls {@code whileWriting} while still holding it. */
    public void publish(String key, String value, Runnable whileWriting) {
        throw new UnsupportedOperationException("write publish");
    }

    /** Returns the stored value, or loads, stores and returns it on a miss. */
    public String lookupOrLoad(String key, Function<String, String> loader) {
        throw new UnsupportedOperationException("write lookupOrLoad");
    }
}
