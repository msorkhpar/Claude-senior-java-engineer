package practice;

import java.util.concurrent.ConcurrentHashMap;

public final class NullableMap {

    /** A private object no caller can store, so it can only mean "null". */
    private static final Object NULL = new Object();

    private final ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();

    /** Stores value (which may be null) under a non-null key. */
    public void put(String key, String value) {
        map.put(key, value == null ? NULL : value);
    }

    /** The stored value, or null when it is null or the key is absent. */
    public String get(String key) {
        Object v = map.get(key);
        return v == NULL ? null : (String) v;
    }

    /** Whether key was stored, even with a null value. */
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }
}
