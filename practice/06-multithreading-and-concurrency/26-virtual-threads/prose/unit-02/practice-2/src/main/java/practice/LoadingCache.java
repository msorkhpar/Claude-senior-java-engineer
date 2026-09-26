package practice;

import java.util.function.Function;

public final class LoadingCache {

    public LoadingCache(Function<String, String> loader) {
    }

    /** The cached value for the key, loading it under the cache's ReentrantLock when missing. */
    public String get(String key) {
        throw new UnsupportedOperationException("write get");
    }
}
