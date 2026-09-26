package practice;

import java.util.concurrent.ConcurrentHashMap;

public final class NullableMap {

    private static final Object NULL = new Object();
    private static final String NULL_KEY = "\u0000null";

    private final ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();

    /** Stores value (which may be null) under a non-null key. */
    public void put(String key, String value) {
        map.put(key == null ? NULL_KEY : key, value == null ? NULL : value);
    }

    /** The stored value, or null when it is null or the key is absent. */
    public String get(String key) {
        Object v = map.get(key == null ? NULL_KEY : key);
        return v == NULL ? null : (String) v;
    }

    /** Whether key was stored, even with a null value. */
    public boolean containsKey(String key) {
        return map.containsKey(key == null ? NULL_KEY : key);
    }
}
