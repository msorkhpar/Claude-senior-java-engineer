package practice;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public final class TypedConfig {

    /** A key whose values have the type T. */
    public record Key<T>(String name, Class<T> type, T defaultValue) {
    }

    public static final Key<Integer> PORT = new Key<>("port", Integer.class, 8080);
    public static final Key<String> HOST = new Key<>("host", String.class, "localhost");

    private final Map<Key<?>, Object> values = new HashMap<>();

    /** Stores {@code value} under {@code key}, checked against the key's type. */
    public <T> void put(Key<T> key, T value) {
        Objects.requireNonNull(value, "value");
        values.put(key, value);
    }

    /** The value stored under {@code key}, or its default. */
    @SuppressWarnings("unchecked")
    public <T> T get(Key<T> key) {
        Object value = values.get(key);
        return value == null ? key.defaultValue() : (T) value;
    }

    /** Whether a value is stored under {@code key}. */
    public boolean contains(Key<?> key) {
        return values.containsKey(key);
    }

    /** How many values are stored. */
    public int size() {
        return values.size();
    }
}
