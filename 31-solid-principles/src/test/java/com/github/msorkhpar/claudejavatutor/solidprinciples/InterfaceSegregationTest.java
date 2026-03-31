package com.github.msorkhpar.claudejavatutor.solidprinciples;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Interface Segregation Principle Tests")
class InterfaceSegregationTest {

    @Nested
    @DisplayName("ISP Violation: Fat Interface")
    class ViolationTest {

        @Test
        @DisplayName("Read-only store throws UnsupportedOperationException for write operations")
        void testReadOnlyStoreThrowsOnWrite() {
            var store = new InterfaceSegregation.ReadOnlyStoreViolation(Map.of("key", "value"));
            assertThat(store.read("key")).isEqualTo("value");
            assertThat(store.exists("key")).isTrue();
            assertThat(store.size()).isEqualTo(1);

            assertThatThrownBy(() -> store.write("k", "v"))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> store.delete("key"))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> store.clear())
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> store.backup("path"))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> store.restore("path"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("ReadWriteStore - ISP Applied")
    class ReadWriteStoreTest {

        @Test
        @DisplayName("Should write and read values")
        void testWriteAndRead() {
            var store = new InterfaceSegregation.ReadWriteStore();
            store.write("name", "Alice");
            assertThat(store.read("name")).isEqualTo("Alice");
        }

        @Test
        @DisplayName("Should return null for missing key")
        void testReadMissing() {
            var store = new InterfaceSegregation.ReadWriteStore();
            assertThat(store.read("missing")).isNull();
        }

        @Test
        @DisplayName("Should check existence")
        void testExists() {
            var store = new InterfaceSegregation.ReadWriteStore();
            store.write("key", "value");
            assertThat(store.exists("key")).isTrue();
            assertThat(store.exists("other")).isFalse();
        }

        @Test
        @DisplayName("Should delete entries")
        void testDelete() {
            var store = new InterfaceSegregation.ReadWriteStore();
            store.write("key", "value");
            store.delete("key");
            assertThat(store.exists("key")).isFalse();
        }

        @Test
        @DisplayName("Should list keys and report size")
        void testListKeysAndSize() {
            var store = new InterfaceSegregation.ReadWriteStore();
            store.write("a", "1");
            store.write("b", "2");
            assertThat(store.listKeys()).containsExactlyInAnyOrder("a", "b");
            assertThat(store.size()).isEqualTo(2);
            assertThat(store.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("Should report empty correctly")
        void testIsEmpty() {
            var store = new InterfaceSegregation.ReadWriteStore();
            assertThat(store.isEmpty()).isTrue();
            assertThat(store.size()).isZero();
        }

        @Test
        @DisplayName("Should reject null key for write")
        void testNullKeyWrite() {
            var store = new InterfaceSegregation.ReadWriteStore();
            assertThatThrownBy(() -> store.write(null, "v"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should reject null value for write")
        void testNullValueWrite() {
            var store = new InterfaceSegregation.ReadWriteStore();
            assertThatThrownBy(() -> store.write("k", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should reject null key for read")
        void testNullKeyRead() {
            var store = new InterfaceSegregation.ReadWriteStore();
            assertThatThrownBy(() -> store.read(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should reject null key for exists")
        void testNullKeyExists() {
            var store = new InterfaceSegregation.ReadWriteStore();
            assertThatThrownBy(() -> store.exists(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should reject null key for delete")
        void testNullKeyDelete() {
            var store = new InterfaceSegregation.ReadWriteStore();
            assertThatThrownBy(() -> store.delete(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("ReadOnlyStore - ISP Applied")
    class ReadOnlyStoreTest {

        @Test
        @DisplayName("Should read from initial data")
        void testRead() {
            var store = new InterfaceSegregation.ReadOnlyStore(Map.of("a", "1", "b", "2"));
            assertThat(store.read("a")).isEqualTo("1");
            assertThat(store.exists("b")).isTrue();
            assertThat(store.exists("c")).isFalse();
        }

        @Test
        @DisplayName("Should handle null data map")
        void testNullDataMap() {
            var store = new InterfaceSegregation.ReadOnlyStore(null);
            assertThat(store.isEmpty()).isTrue();
            assertThat(store.size()).isZero();
        }

        @Test
        @DisplayName("Should handle empty data map")
        void testEmptyDataMap() {
            var store = new InterfaceSegregation.ReadOnlyStore(Map.of());
            assertThat(store.isEmpty()).isTrue();
            assertThat(store.listKeys()).isEmpty();
        }

        @Test
        @DisplayName("Should reject null key for read")
        void testNullKeyRead() {
            var store = new InterfaceSegregation.ReadOnlyStore(Map.of("a", "1"));
            assertThatThrownBy(() -> store.read(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("ManagedExecutor - ISP in Concurrency")
    class ManagedExecutorTest {

        private InterfaceSegregation.ManagedExecutor executor;

        @AfterEach
        void tearDown() {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
            }
        }

        @Test
        @DisplayName("Should submit and execute tasks")
        void testSubmitTask() throws Exception {
            executor = new InterfaceSegregation.ManagedExecutor(2);
            Future<Object> future = executor.submit(() -> "result");
            assertThat(future.get(5, TimeUnit.SECONDS)).isEqualTo("result");
        }

        @Test
        @DisplayName("Should report shutdown status")
        void testShutdownStatus() {
            executor = new InterfaceSegregation.ManagedExecutor(1);
            assertThat(executor.isShutdown()).isFalse();
            executor.shutdown();
            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("Should reject task after shutdown")
        void testRejectAfterShutdown() {
            executor = new InterfaceSegregation.ManagedExecutor(1);
            executor.shutdown();
            assertThatThrownBy(() -> executor.submit(() -> "late"))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Should reject null task")
        void testNullTask() {
            executor = new InterfaceSegregation.ManagedExecutor(1);
            assertThatThrownBy(() -> executor.submit(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should reject non-positive pool size")
        void testInvalidPoolSize() {
            assertThatThrownBy(() -> new InterfaceSegregation.ManagedExecutor(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("TaskClient - Depends only on TaskSubmitter")
    class TaskClientTest {

        @Test
        @DisplayName("Should run task via submitter interface")
        void testRunTask() throws Exception {
            var executor = new InterfaceSegregation.ManagedExecutor(1);
            var client = new InterfaceSegregation.TaskClient(executor);

            Future<Object> result = client.runTask(() -> 42);
            assertThat(result.get(5, TimeUnit.SECONDS)).isEqualTo(42);
            executor.shutdown();
        }

        @Test
        @DisplayName("Should reject null submitter")
        void testNullSubmitter() {
            assertThatThrownBy(() -> new InterfaceSegregation.TaskClient(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("LifecycleManager - Depends only on LifecycleManageable")
    class LifecycleManagerTest {

        @Test
        @DisplayName("Should manage lifecycle via interface")
        void testGracefulShutdown() throws Exception {
            var executor = new InterfaceSegregation.ManagedExecutor(1);
            var manager = new InterfaceSegregation.LifecycleManager(executor);

            assertThat(manager.isRunning()).isTrue();
            manager.gracefulShutdown(1000);
            assertThat(manager.isRunning()).isFalse();
        }

        @Test
        @DisplayName("Should reject null manageable")
        void testNullManageable() {
            assertThatThrownBy(() -> new InterfaceSegregation.LifecycleManager(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
