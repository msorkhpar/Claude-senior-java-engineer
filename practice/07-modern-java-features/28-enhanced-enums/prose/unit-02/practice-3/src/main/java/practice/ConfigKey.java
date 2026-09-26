package practice;

import java.util.Arrays;
import java.util.Optional;

public enum ConfigKey {
    MAX_CONNECTIONS("max.connections", Integer.class, 10),
    SERVER_HOST("server.host", String.class, "localhost"),
    ENABLE_SSL("enable.ssl", Boolean.class, false),
    RATE_LIMIT("rate.limit", Double.class, 100.0);

    private final String key;
    private final Class<?> type;
    private final Object defaultValue;

    ConfigKey(String key, Class<?> type, Object defaultValue) {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public String key() {
        return key;
    }

    public Class<?> type() {
        return type;
    }

    /** {@code value} as this key's type; null gives the default; a wrong type throws here. */
    @SuppressWarnings("unchecked")
    public <T> T cast(Object value) {
        throw new UnsupportedOperationException("write cast");
    }

    /** The constant whose key equals {@code key}, or empty. */
    public static Optional<ConfigKey> fromKey(String key) {
        throw new UnsupportedOperationException("write fromKey");
    }
}
