package com.github.msorkhpar.claudejavatutor.creationalpatterns;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates various Singleton pattern implementations in Java.
 * Covers eager, lazy, double-checked locking, holder idiom, and enum-based approaches.
 */
public class SingletonPattern {

    /**
     * Eager initialization Singleton.
     * The instance is created when the class is loaded.
     * Thread-safe due to class loading guarantees.
     */
    public static final class EagerSingleton {
        private static final EagerSingleton INSTANCE = new EagerSingleton();
        private final AtomicInteger accessCount = new AtomicInteger(0);

        private EagerSingleton() {
            // Prevent reflection attack
            if (INSTANCE != null) {
                throw new IllegalStateException("Use getInstance()");
            }
        }

        public static EagerSingleton getInstance() {
            return INSTANCE;
        }

        public int getAccessCount() {
            return accessCount.get();
        }

        public int incrementAndGetAccessCount() {
            return accessCount.incrementAndGet();
        }

        public void resetAccessCount() {
            accessCount.set(0);
        }
    }

    /**
     * Double-checked locking Singleton with volatile.
     * Lazy initialization with minimal synchronization overhead.
     */
    public static final class DoubleCheckedSingleton {
        private static volatile DoubleCheckedSingleton instance;
        private String configuration;

        private DoubleCheckedSingleton() {
            this.configuration = "default";
        }

        public static DoubleCheckedSingleton getInstance() {
            if (instance == null) {
                synchronized (DoubleCheckedSingleton.class) {
                    if (instance == null) {
                        instance = new DoubleCheckedSingleton();
                    }
                }
            }
            return instance;
        }

        public String getConfiguration() {
            return configuration;
        }

        public void setConfiguration(String configuration) {
            this.configuration = configuration;
        }

        /**
         * Resets the singleton for testing purposes only.
         * In production code, this method should not exist.
         */
        static void resetForTesting() {
            instance = null;
        }
    }

    /**
     * Bill Pugh Singleton (Initialization-on-Demand Holder idiom).
     * Lazy, thread-safe, no synchronization overhead.
     */
    public static final class HolderSingleton {
        private final String id;

        private HolderSingleton() {
            this.id = "holder-" + System.nanoTime();
        }

        private static class Holder {
            private static final HolderSingleton INSTANCE = new HolderSingleton();
        }

        public static HolderSingleton getInstance() {
            return Holder.INSTANCE;
        }

        public String getId() {
            return id;
        }
    }

    /**
     * Enum-based Singleton -- the recommended approach (Effective Java, Item 3).
     * Thread-safe, serialization-safe, reflection-safe.
     */
    public enum EnumSingleton {
        INSTANCE;

        private int value;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public String describe() {
            return "EnumSingleton with value=" + value;
        }
    }

    /**
     * Serialization-safe Singleton with readResolve().
     * Demonstrates how to protect against deserialization attacks.
     */
    public static final class SerializableSingleton implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private static final SerializableSingleton INSTANCE = new SerializableSingleton();
        private final String name;

        private SerializableSingleton() {
            this.name = "serializable-singleton";
        }

        public static SerializableSingleton getInstance() {
            return INSTANCE;
        }

        public String getName() {
            return name;
        }

        @Serial
        private Object readResolve() {
            return INSTANCE;
        }
    }

    /**
     * Singleton with interface -- demonstrates testability improvement.
     * The Singleton implements an interface, allowing mocks in tests.
     */
    public interface CacheService {
        void put(String key, String value);
        String get(String key);
        int size();
        void clear();
    }

    public static final class SingletonCacheService implements CacheService {
        private static final SingletonCacheService INSTANCE = new SingletonCacheService();
        private final java.util.Map<String, String> cache = new java.util.concurrent.ConcurrentHashMap<>();

        private SingletonCacheService() {}

        public static SingletonCacheService getInstance() {
            return INSTANCE;
        }

        @Override
        public void put(String key, String value) {
            if (key == null) {
                throw new NullPointerException("Key cannot be null");
            }
            if (value == null) {
                throw new NullPointerException("Value cannot be null");
            }
            cache.put(key, value);
        }

        @Override
        public String get(String key) {
            if (key == null) {
                throw new NullPointerException("Key cannot be null");
            }
            return cache.get(key);
        }

        @Override
        public int size() {
            return cache.size();
        }

        @Override
        public void clear() {
            cache.clear();
        }
    }
}
