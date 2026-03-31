package com.github.msorkhpar.claudejavatutor.creationalpatterns;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Singleton Pattern Tests")
class SingletonPatternTest {

    @Nested
    @DisplayName("Eager Singleton")
    class EagerSingletonTest {

        @BeforeEach
        void setUp() {
            SingletonPattern.EagerSingleton.getInstance().resetAccessCount();
        }

        @Test
        @DisplayName("Should return the same instance every time")
        void testSameInstance() {
            var instance1 = SingletonPattern.EagerSingleton.getInstance();
            var instance2 = SingletonPattern.EagerSingleton.getInstance();

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("Should return non-null instance")
        void testNonNull() {
            assertThat(SingletonPattern.EagerSingleton.getInstance()).isNotNull();
        }

        @Test
        @DisplayName("Should maintain state across accesses")
        void testStateMaintained() {
            var instance = SingletonPattern.EagerSingleton.getInstance();

            instance.incrementAndGetAccessCount();
            instance.incrementAndGetAccessCount();

            assertThat(SingletonPattern.EagerSingleton.getInstance().getAccessCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return same instance from multiple threads")
        void testThreadSafety() throws Exception {
            int threadCount = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            Set<SingletonPattern.EagerSingleton> instances = ConcurrentHashMap.newKeySet();

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                futures.add(executor.submit(() -> {
                    instances.add(SingletonPattern.EagerSingleton.getInstance());
                }));
            }

            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }
            executor.shutdown();

            assertThat(instances).hasSize(1);
        }

        @Test
        @DisplayName("Should correctly increment access count atomically")
        void testAtomicAccessCount() throws Exception {
            int threadCount = 50;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            var instance = SingletonPattern.EagerSingleton.getInstance();

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                futures.add(executor.submit(instance::incrementAndGetAccessCount));
            }

            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }
            executor.shutdown();

            assertThat(instance.getAccessCount()).isEqualTo(threadCount);
        }
    }

    @Nested
    @DisplayName("Double-Checked Locking Singleton")
    class DoubleCheckedSingletonTest {

        @BeforeEach
        void setUp() {
            SingletonPattern.DoubleCheckedSingleton.resetForTesting();
        }

        @Test
        @DisplayName("Should return the same instance every time")
        void testSameInstance() {
            var instance1 = SingletonPattern.DoubleCheckedSingleton.getInstance();
            var instance2 = SingletonPattern.DoubleCheckedSingleton.getInstance();

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("Should initialize with default configuration")
        void testDefaultConfiguration() {
            var instance = SingletonPattern.DoubleCheckedSingleton.getInstance();

            assertThat(instance.getConfiguration()).isEqualTo("default");
        }

        @Test
        @DisplayName("Should persist configuration changes")
        void testConfigurationChange() {
            var instance = SingletonPattern.DoubleCheckedSingleton.getInstance();
            instance.setConfiguration("custom");

            assertThat(SingletonPattern.DoubleCheckedSingleton.getInstance().getConfiguration())
                    .isEqualTo("custom");
        }

        @Test
        @DisplayName("Should return same instance from multiple threads")
        void testThreadSafety() throws Exception {
            int threadCount = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            Set<SingletonPattern.DoubleCheckedSingleton> instances = ConcurrentHashMap.newKeySet();
            CountDownLatch latch = new CountDownLatch(1);

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                futures.add(executor.submit(() -> {
                    try {
                        latch.await(); // All threads start simultaneously
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    instances.add(SingletonPattern.DoubleCheckedSingleton.getInstance());
                }));
            }

            latch.countDown(); // Release all threads at once
            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }
            executor.shutdown();

            assertThat(instances).hasSize(1);
        }

        @Test
        @DisplayName("Should create new instance after reset")
        void testResetCreatesFreshInstance() {
            var instance1 = SingletonPattern.DoubleCheckedSingleton.getInstance();
            instance1.setConfiguration("modified");

            SingletonPattern.DoubleCheckedSingleton.resetForTesting();

            var instance2 = SingletonPattern.DoubleCheckedSingleton.getInstance();
            assertThat(instance2.getConfiguration()).isEqualTo("default");
            assertThat(instance2).isNotSameAs(instance1);
        }
    }

    @Nested
    @DisplayName("Holder Singleton (Bill Pugh)")
    class HolderSingletonTest {

        @Test
        @DisplayName("Should return the same instance every time")
        void testSameInstance() {
            var instance1 = SingletonPattern.HolderSingleton.getInstance();
            var instance2 = SingletonPattern.HolderSingleton.getInstance();

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("Should have a non-null ID")
        void testNonNullId() {
            assertThat(SingletonPattern.HolderSingleton.getInstance().getId())
                    .isNotNull()
                    .startsWith("holder-");
        }

        @Test
        @DisplayName("Should have the same ID across accesses")
        void testConsistentId() {
            String id1 = SingletonPattern.HolderSingleton.getInstance().getId();
            String id2 = SingletonPattern.HolderSingleton.getInstance().getId();

            assertThat(id1).isEqualTo(id2);
        }

        @Test
        @DisplayName("Should return same instance from multiple threads")
        void testThreadSafety() throws Exception {
            int threadCount = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            Set<SingletonPattern.HolderSingleton> instances = ConcurrentHashMap.newKeySet();

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                futures.add(executor.submit(() ->
                        instances.add(SingletonPattern.HolderSingleton.getInstance())
                ));
            }

            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }
            executor.shutdown();

            assertThat(instances).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Enum Singleton")
    class EnumSingletonTest {

        @BeforeEach
        void setUp() {
            SingletonPattern.EnumSingleton.INSTANCE.setValue(0);
        }

        @Test
        @DisplayName("Should have exactly one instance")
        void testSingleInstance() {
            var instance1 = SingletonPattern.EnumSingleton.INSTANCE;
            var instance2 = SingletonPattern.EnumSingleton.INSTANCE;

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("Should maintain state")
        void testStateMaintained() {
            SingletonPattern.EnumSingleton.INSTANCE.setValue(42);

            assertThat(SingletonPattern.EnumSingleton.INSTANCE.getValue()).isEqualTo(42);
        }

        @Test
        @DisplayName("Should describe itself correctly")
        void testDescribe() {
            SingletonPattern.EnumSingleton.INSTANCE.setValue(10);

            assertThat(SingletonPattern.EnumSingleton.INSTANCE.describe())
                    .isEqualTo("EnumSingleton with value=10");
        }

        @Test
        @DisplayName("Should survive serialization and return same instance")
        void testSerializationSafe() throws Exception {
            SingletonPattern.EnumSingleton original = SingletonPattern.EnumSingleton.INSTANCE;
            original.setValue(99);

            // Serialize
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(original);
            oos.close();

            // Deserialize
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            SingletonPattern.EnumSingleton deserialized = (SingletonPattern.EnumSingleton) ois.readObject();
            ois.close();

            assertThat(deserialized).isSameAs(original);
        }

        @Test
        @DisplayName("Should have only one enum constant")
        void testOnlyOneConstant() {
            assertThat(SingletonPattern.EnumSingleton.values()).hasSize(1);
        }

        @Test
        @DisplayName("Should be retrievable via valueOf")
        void testValueOf() {
            assertThat(SingletonPattern.EnumSingleton.valueOf("INSTANCE"))
                    .isSameAs(SingletonPattern.EnumSingleton.INSTANCE);
        }

        @Test
        @DisplayName("Should throw for invalid valueOf")
        void testInvalidValueOf() {
            assertThatThrownBy(() -> SingletonPattern.EnumSingleton.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Serializable Singleton")
    class SerializableSingletonTest {

        @Test
        @DisplayName("Should return the same instance after deserialization")
        void testSerializationSafe() throws Exception {
            var original = SingletonPattern.SerializableSingleton.getInstance();

            // Serialize
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(original);
            oos.close();

            // Deserialize
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            var deserialized = (SingletonPattern.SerializableSingleton) ois.readObject();
            ois.close();

            assertThat(deserialized).isSameAs(original);
        }

        @Test
        @DisplayName("Should have correct name")
        void testName() {
            assertThat(SingletonPattern.SerializableSingleton.getInstance().getName())
                    .isEqualTo("serializable-singleton");
        }

        @Test
        @DisplayName("Should return same instance every time")
        void testSameInstance() {
            var instance1 = SingletonPattern.SerializableSingleton.getInstance();
            var instance2 = SingletonPattern.SerializableSingleton.getInstance();

            assertThat(instance1).isSameAs(instance2);
        }
    }

    @Nested
    @DisplayName("Singleton Cache Service (interface-based)")
    class SingletonCacheServiceTest {

        @BeforeEach
        void setUp() {
            SingletonPattern.SingletonCacheService.getInstance().clear();
        }

        @Test
        @DisplayName("Should return same instance")
        void testSameInstance() {
            var instance1 = SingletonPattern.SingletonCacheService.getInstance();
            var instance2 = SingletonPattern.SingletonCacheService.getInstance();

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("Should implement CacheService interface")
        void testImplementsInterface() {
            SingletonPattern.CacheService service = SingletonPattern.SingletonCacheService.getInstance();
            assertThat(service).isInstanceOf(SingletonPattern.CacheService.class);
        }

        @Test
        @DisplayName("Should put and get values")
        void testPutAndGet() {
            var cache = SingletonPattern.SingletonCacheService.getInstance();

            cache.put("key1", "value1");

            assertThat(cache.get("key1")).isEqualTo("value1");
        }

        @Test
        @DisplayName("Should return null for missing keys")
        void testMissingKey() {
            var cache = SingletonPattern.SingletonCacheService.getInstance();

            assertThat(cache.get("nonexistent")).isNull();
        }

        @Test
        @DisplayName("Should track size correctly")
        void testSize() {
            var cache = SingletonPattern.SingletonCacheService.getInstance();

            cache.put("a", "1");
            cache.put("b", "2");
            cache.put("c", "3");

            assertThat(cache.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should clear all entries")
        void testClear() {
            var cache = SingletonPattern.SingletonCacheService.getInstance();
            cache.put("key", "value");

            cache.clear();

            assertThat(cache.size()).isZero();
            assertThat(cache.get("key")).isNull();
        }

        @Test
        @DisplayName("Should throw NullPointerException for null key in put")
        void testNullKeyPut() {
            var cache = SingletonPattern.SingletonCacheService.getInstance();

            assertThatThrownBy(() -> cache.put(null, "value"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Key");
        }

        @Test
        @DisplayName("Should throw NullPointerException for null value in put")
        void testNullValuePut() {
            var cache = SingletonPattern.SingletonCacheService.getInstance();

            assertThatThrownBy(() -> cache.put("key", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Value");
        }

        @Test
        @DisplayName("Should throw NullPointerException for null key in get")
        void testNullKeyGet() {
            var cache = SingletonPattern.SingletonCacheService.getInstance();

            assertThatThrownBy(() -> cache.get(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Key");
        }

        @Test
        @DisplayName("Should overwrite existing key")
        void testOverwrite() {
            var cache = SingletonPattern.SingletonCacheService.getInstance();

            cache.put("key", "value1");
            cache.put("key", "value2");

            assertThat(cache.get("key")).isEqualTo("value2");
            assertThat(cache.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should be thread-safe for concurrent put operations")
        void testConcurrentAccess() throws Exception {
            var cache = SingletonPattern.SingletonCacheService.getInstance();
            int threadCount = 50;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                final int idx = i;
                futures.add(executor.submit(() -> cache.put("key-" + idx, "value-" + idx)));
            }

            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }
            executor.shutdown();

            assertThat(cache.size()).isEqualTo(threadCount);
        }
    }
}
