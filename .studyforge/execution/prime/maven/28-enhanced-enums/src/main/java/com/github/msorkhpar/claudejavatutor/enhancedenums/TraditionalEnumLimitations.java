package com.github.msorkhpar.claudejavatutor.enhancedenums;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Demonstrates the limitations of traditional Java enums and why enhanced patterns are needed.
 * Covers type erasure issues, inability to be generic, fixed constant sets, and serialization constraints.
 */
public class TraditionalEnumLimitations {

    // --- Limitation 1: Enums cannot be generic ---

    /**
     * A traditional enum that stores a value as Object because enums cannot be parameterized.
     * This forces unsafe casts when retrieving the value.
     */
    public enum UntypedSetting {
        MAX_RETRIES("maxRetries", 3),
        TIMEOUT_MS("timeout", 5000L),
        APP_NAME("appName", "MyApp"),
        DEBUG_MODE("debug", true);

        private final String key;
        private final Object value;

        UntypedSetting(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        /**
         * Returns the value as Object, requiring unsafe casts at call sites.
         */
        public Object getValue() {
            return value;
        }

        /**
         * Retrieves value with an unsafe cast - demonstrates the limitation.
         */
        @SuppressWarnings("unchecked")
        public <T> T getTypedValue() {
            return (T) value;
        }
    }

    // --- Limitation 2: Enums cannot extend other classes ---

    /**
     * Shows that enums implicitly extend java.lang.Enum and therefore cannot extend any other class.
     * Workaround: Use composition by delegating to an internal helper object.
     */
    public static class MathHelper {
        public double compute(double a, double b) {
            return a + b;
        }
    }

    public enum OperationWithComposition {
        ADD {
            @Override
            public double apply(double a, double b) {
                return helper.compute(a, b);
            }
        },
        SUBTRACT {
            @Override
            public double apply(double a, double b) {
                return a - b;
            }
        };

        protected static final MathHelper helper = new MathHelper();

        public abstract double apply(double a, double b);
    }

    // --- Limitation 3: Fixed set of constants ---

    /**
     * Enums have a fixed set of instances determined at compile time.
     * This is both a strength (type safety) and a limitation (no runtime extensibility).
     * Workaround: Use an interface that enums implement, allowing multiple enum types.
     */
    public interface Severity {
        String label();

        int level();
    }

    public enum StandardSeverity implements Severity {
        LOW("Low", 1),
        MEDIUM("Medium", 2),
        HIGH("High", 3);

        private final String label;
        private final int level;

        StandardSeverity(String label, int level) {
            this.label = label;
            this.level = level;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public int level() {
            return level;
        }
    }

    public enum ExtendedSeverity implements Severity {
        CRITICAL("Critical", 4),
        CATASTROPHIC("Catastrophic", 5);

        private final String label;
        private final int level;

        ExtendedSeverity(String label, int level) {
            this.label = label;
            this.level = level;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public int level() {
            return level;
        }
    }

    /**
     * Collects all severities from multiple enum types implementing the Severity interface.
     */
    public static List<Severity> allSeverities() {
        List<Severity> all = new ArrayList<>();
        Collections.addAll(all, StandardSeverity.values());
        Collections.addAll(all, ExtendedSeverity.values());
        all.sort(Comparator.comparingInt(Severity::level));
        return Collections.unmodifiableList(all);
    }

    // --- Limitation 4: Enums cannot be instantiated at runtime ---

    /**
     * Traditional enums have no public constructors; instances are created by the JVM at class loading time.
     * This means you cannot create new enum-like instances at runtime.
     * Workaround: Use a registry pattern with an interface.
     */
    public static class DynamicRegistry<T> {
        private final Map<String, T> entries = new LinkedHashMap<>();

        public void register(String name, T value) {
            if (entries.containsKey(name)) {
                throw new IllegalArgumentException("Already registered: " + name);
            }
            entries.put(name, value);
        }

        public T get(String name) {
            T value = entries.get(name);
            if (value == null) {
                throw new NoSuchElementException("Not found: " + name);
            }
            return value;
        }

        public Collection<T> values() {
            return Collections.unmodifiableCollection(entries.values());
        }

        public Set<String> names() {
            return Collections.unmodifiableSet(entries.keySet());
        }

        public int size() {
            return entries.size();
        }
    }

    // --- Limitation 5: All enum constants share the same type parameter constraints ---

    /**
     * Demonstrates that all enum constants share the same compile-time type.
     * You cannot have one constant return Integer and another return String in a type-safe way.
     */
    public enum DataFormat {
        JSON {
            @Override
            public String serialize(Object obj) {
                return "{\"value\":\"" + obj + "\"}";
            }

            @Override
            public Object deserialize(String input) {
                // Simplified JSON parsing
                int start = input.indexOf("\"value\":\"") + 9;
                int end = input.lastIndexOf("\"");
                return input.substring(start, end);
            }
        },
        CSV {
            @Override
            public String serialize(Object obj) {
                return String.valueOf(obj);
            }

            @Override
            public Object deserialize(String input) {
                return input.trim();
            }
        };

        public abstract String serialize(Object obj);

        public abstract Object deserialize(String input);
    }

    // --- Utility methods to demonstrate limitations ---

    /**
     * Demonstrates that EnumSet and EnumMap work only with a single enum type.
     */
    public static <E extends Enum<E>> EnumSet<E> createEnumSet(Class<E> enumClass, E... values) {
        if (values.length == 0) {
            return EnumSet.noneOf(enumClass);
        }
        return EnumSet.of(values[0], values);
    }

    /**
     * Demonstrates lookup by name - enums provide this built-in,
     * but it throws IllegalArgumentException for unknown names.
     */
    public static <E extends Enum<E>> Optional<E> safeValueOf(Class<E> enumClass, String name) {
        try {
            return Optional.of(Enum.valueOf(enumClass, name));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }

    /**
     * Creates a lookup map for an enum by a custom key extractor.
     */
    public static <E extends Enum<E>, K> Map<K, E> createLookupMap(
            Class<E> enumClass, Function<E, K> keyExtractor) {
        return Arrays.stream(enumClass.getEnumConstants())
                .collect(Collectors.toUnmodifiableMap(keyExtractor, Function.identity()));
    }
}
