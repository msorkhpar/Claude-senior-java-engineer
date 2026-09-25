package com.github.msorkhpar.claudejavatutor.enhancedenums;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Demonstrates workaround patterns for achieving generic-like behavior in Java enums.
 * Since Java enums cannot have type parameters directly, we use interfaces, abstract classes,
 * and sealed hierarchies to simulate generic enums.
 */
public class GenericEnumPatterns {

    // --- Pattern 1: Interface-based generic enum simulation ---

    /**
     * A generic interface that enum-like classes can implement.
     *
     * @param <T> the type of value held by the setting
     */
    public interface TypedSetting<T> {
        String key();

        T defaultValue();

        Class<T> valueType();

        /**
         * Validates and casts a raw value to the expected type.
         */
        default T cast(Object raw) {
            if (raw == null) {
                return defaultValue();
            }
            if (valueType().isInstance(raw)) {
                return valueType().cast(raw);
            }
            throw new ClassCastException(
                    "Cannot cast " + raw.getClass().getName() + " to " + valueType().getName()
            );
        }
    }

    /**
     * A sealed interface hierarchy for type-safe configuration settings.
     * Each record acts like an enum constant with its own generic type.
     */
    public sealed interface AppSetting<T> extends TypedSetting<T>
            permits AppSetting.MaxRetries, AppSetting.AppName, AppSetting.DebugMode, AppSetting.TimeoutMs {

        record MaxRetries() implements AppSetting<Integer> {
            @Override
            public String key() {
                return "max.retries";
            }

            @Override
            public Integer defaultValue() {
                return 3;
            }

            @Override
            public Class<Integer> valueType() {
                return Integer.class;
            }
        }

        record AppName() implements AppSetting<String> {
            @Override
            public String key() {
                return "app.name";
            }

            @Override
            public String defaultValue() {
                return "DefaultApp";
            }

            @Override
            public Class<String> valueType() {
                return String.class;
            }
        }

        record DebugMode() implements AppSetting<Boolean> {
            @Override
            public String key() {
                return "debug.mode";
            }

            @Override
            public Boolean defaultValue() {
                return false;
            }

            @Override
            public Class<Boolean> valueType() {
                return Boolean.class;
            }
        }

        record TimeoutMs() implements AppSetting<Long> {
            @Override
            public String key() {
                return "timeout.ms";
            }

            @Override
            public Long defaultValue() {
                return 5000L;
            }

            @Override
            public Class<Long> valueType() {
                return Long.class;
            }
        }

        /**
         * Provides all known settings (like Enum.values()).
         */
        static List<AppSetting<?>> values() {
            return List.of(
                    new MaxRetries(),
                    new AppName(),
                    new DebugMode(),
                    new TimeoutMs()
            );
        }
    }

    // --- Pattern 2: Type-safe heterogeneous container for settings ---

    /**
     * A configuration store that preserves type safety for each setting.
     */
    public static class TypeSafeConfig {
        private final Map<String, Object> store = new HashMap<>();

        public <T> void put(TypedSetting<T> setting, T value) {
            Objects.requireNonNull(setting, "Setting must not be null");
            Objects.requireNonNull(value, "Value must not be null");
            store.put(setting.key(), value);
        }

        public <T> T get(TypedSetting<T> setting) {
            Objects.requireNonNull(setting, "Setting must not be null");
            Object raw = store.get(setting.key());
            return setting.cast(raw);
        }

        public boolean contains(TypedSetting<?> setting) {
            return store.containsKey(setting.key());
        }

        public int size() {
            return store.size();
        }
    }

    // --- Pattern 3: Enum with Class<T> token for partial type safety ---

    /**
     * An enum that stores a Class token to achieve partial type safety.
     * The generic type is carried by the class token rather than by the enum itself.
     */
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

        @SuppressWarnings("unchecked")
        public <T> T defaultValue() {
            return (T) defaultValue;
        }

        @SuppressWarnings("unchecked")
        public <T> T cast(Object value) {
            if (value == null) {
                return defaultValue();
            }
            if (!type.isInstance(value)) {
                throw new ClassCastException(
                        "Expected " + type.getName() + " but got " + value.getClass().getName()
                );
            }
            return (T) value;
        }

        public static Optional<ConfigKey> fromKey(String key) {
            for (ConfigKey ck : values()) {
                if (ck.key.equals(key)) {
                    return Optional.of(ck);
                }
            }
            return Optional.empty();
        }
    }

    // --- Pattern 4: Generic enum-like class using static final instances ---

    /**
     * Simulates a generic enum using a class with static final instances.
     * Each instance carries its own type parameter.
     *
     * @param <T> the type of value this key represents
     */
    public static final class TypedKey<T> {
        private static final List<TypedKey<?>> ALL_KEYS = new ArrayList<>();

        public static final TypedKey<Integer> PORT = new TypedKey<>("port", Integer.class, 8080);
        public static final TypedKey<String> HOST = new TypedKey<>("host", String.class, "0.0.0.0");
        public static final TypedKey<Boolean> VERBOSE = new TypedKey<>("verbose", Boolean.class, false);
        public static final TypedKey<List<String>> ALLOWED_ORIGINS =
                new TypedKey<>("allowed.origins", null, List.of("*"));

        private final String name;
        private final Class<T> type;
        private final T defaultValue;

        @SuppressWarnings("unchecked")
        private TypedKey(String name, Class<T> type, T defaultValue) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
            ALL_KEYS.add(this);
        }

        public String name() {
            return name;
        }

        public T defaultValue() {
            return defaultValue;
        }

        public Class<T> type() {
            return type;
        }

        public static List<TypedKey<?>> values() {
            return Collections.unmodifiableList(ALL_KEYS);
        }

        @Override
        public String toString() {
            return "TypedKey{" + name + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TypedKey<?> that)) return false;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    // --- Pattern 5: Supplier-based lazy enum pattern ---

    /**
     * An enum that uses Supplier for lazy, type-safe value generation.
     */
    public enum LazyDefault {
        RANDOM_INT(() -> new Random().nextInt(100)),
        CURRENT_TIME(() -> System.currentTimeMillis()),
        EMPTY_LIST(ArrayList::new),
        DEFAULT_MAP(HashMap::new);

        private final Supplier<?> supplier;

        LazyDefault(Supplier<?> supplier) {
            this.supplier = supplier;
        }

        @SuppressWarnings("unchecked")
        public <T> T generate() {
            return (T) supplier.get();
        }

        public Supplier<?> supplier() {
            return supplier;
        }
    }
}
