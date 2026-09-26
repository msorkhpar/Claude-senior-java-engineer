package practice;

import java.util.concurrent.ConcurrentHashMap;

public final class NullableMap {

    private final ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();

    /** Stores value (which may be null) under a non-null key. */
    public void put(String key, String value) {
        throw new UnsupportedOperationException("write put");
    }

    /** The stored value, or null when it is null or the key is absent. */
    public String get(String key) {
        throw new UnsupportedOperationException("write get");
    }

    /** Whether key was stored, even with a null value. */
    public boolean containsKey(String key) {
        throw new UnsupportedOperationException("write containsKey");
    }
}
