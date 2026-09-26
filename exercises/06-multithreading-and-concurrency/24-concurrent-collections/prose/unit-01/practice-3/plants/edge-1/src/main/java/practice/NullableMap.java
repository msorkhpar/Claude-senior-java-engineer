package practice;

import java.util.concurrent.ConcurrentHashMap;

public final class NullableMap {

    private final ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();

    /** Stores value (which may be null) under a non-null key. */
    public void put(String key, String value) {
        if (value == null) {
            map.remove(key);
        } else {
            map.put(key, value);
        }
    }

    /** The stored value, or null when it is null or the key is absent. */
    public String get(String key) {
        return (String) map.get(key);
    }

    /** Whether key was stored, even with a null value. */
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }
}
