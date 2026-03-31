package com.github.msorkhpar.claudejavatutor.solidprinciples;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Demonstrates the Interface Segregation Principle (ISP).
 * Clients should not be forced to depend on methods they do not use.
 */
public class InterfaceSegregation {

    // ========== VIOLATION EXAMPLE ==========

    /**
     * Fat interface that violates ISP: forces all implementations to handle
     * reading, writing, and deleting -- even if they only need one.
     */
    public interface DataStoreViolation {
        String read(String key);
        void write(String key, String value);
        void delete(String key);
        List<String> listKeys();
        void clear();
        int size();
        boolean exists(String key);
        void backup(String destination);
        void restore(String source);
    }

    /**
     * A read-only client is forced to provide stubs for write/delete/backup/restore.
     */
    public static class ReadOnlyStoreViolation implements DataStoreViolation {
        private final Map<String, String> data;

        public ReadOnlyStoreViolation(Map<String, String> data) {
            this.data = Map.copyOf(data);
        }

        @Override
        public String read(String key) {
            return data.get(key);
        }

        @Override
        public void write(String key, String value) {
            throw new UnsupportedOperationException("Read-only store");
        }

        @Override
        public void delete(String key) {
            throw new UnsupportedOperationException("Read-only store");
        }

        @Override
        public List<String> listKeys() {
            return List.copyOf(data.keySet());
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Read-only store");
        }

        @Override
        public int size() {
            return data.size();
        }

        @Override
        public boolean exists(String key) {
            return data.containsKey(key);
        }

        @Override
        public void backup(String destination) {
            throw new UnsupportedOperationException("Read-only store");
        }

        @Override
        public void restore(String source) {
            throw new UnsupportedOperationException("Read-only store");
        }
    }

    // ========== CORRECT EXAMPLE: ISP Applied ==========

    /**
     * Fine-grained interface: reading only.
     */
    public interface Readable<K, V> {
        V read(K key);
        boolean exists(K key);
    }

    /**
     * Fine-grained interface: writing only.
     */
    public interface Writable<K, V> {
        void write(K key, V value);
        void delete(K key);
    }

    /**
     * Fine-grained interface: listing/counting only.
     */
    public interface Listable<K> {
        List<K> listKeys();
        int size();
        boolean isEmpty();
    }

    /**
     * Composed read-write store implementing exactly what it needs.
     */
    public static class ReadWriteStore implements Readable<String, String>,
            Writable<String, String>, Listable<String> {

        private final ConcurrentHashMap<String, String> data = new ConcurrentHashMap<>();

        @Override
        public String read(String key) {
            if (key == null) throw new NullPointerException("Key cannot be null");
            return data.get(key);
        }

        @Override
        public boolean exists(String key) {
            if (key == null) throw new NullPointerException("Key cannot be null");
            return data.containsKey(key);
        }

        @Override
        public void write(String key, String value) {
            if (key == null) throw new NullPointerException("Key cannot be null");
            if (value == null) throw new NullPointerException("Value cannot be null");
            data.put(key, value);
        }

        @Override
        public void delete(String key) {
            if (key == null) throw new NullPointerException("Key cannot be null");
            data.remove(key);
        }

        @Override
        public List<String> listKeys() {
            return List.copyOf(data.keySet());
        }

        @Override
        public int size() {
            return data.size();
        }

        @Override
        public boolean isEmpty() {
            return data.isEmpty();
        }
    }

    /**
     * Read-only store: only implements Readable and Listable.
     * Not forced to handle write/delete.
     */
    public static class ReadOnlyStore implements Readable<String, String>, Listable<String> {
        private final Map<String, String> data;

        public ReadOnlyStore(Map<String, String> data) {
            this.data = data == null ? Map.of() : Map.copyOf(data);
        }

        @Override
        public String read(String key) {
            if (key == null) throw new NullPointerException("Key cannot be null");
            return data.get(key);
        }

        @Override
        public boolean exists(String key) {
            if (key == null) throw new NullPointerException("Key cannot be null");
            return data.containsKey(key);
        }

        @Override
        public List<String> listKeys() {
            return List.copyOf(data.keySet());
        }

        @Override
        public int size() {
            return data.size();
        }

        @Override
        public boolean isEmpty() {
            return data.isEmpty();
        }
    }

    // ========== ISP IN CONCURRENT PROGRAMMING ==========

    /**
     * Fine-grained interface for task submission.
     */
    public interface TaskSubmitter<T> {
        Future<T> submit(Callable<T> task);
    }

    /**
     * Fine-grained interface for lifecycle management.
     */
    public interface LifecycleManageable {
        void shutdown();
        boolean isShutdown();
        boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
    }

    /**
     * Fine-grained interface for monitoring.
     */
    public interface Monitorable {
        int getActiveCount();
        long getCompletedCount();
    }

    /**
     * Full-featured executor implementing all segregated interfaces.
     * Clients only depend on what they need.
     */
    public static class ManagedExecutor implements TaskSubmitter<Object>,
            LifecycleManageable, Monitorable {

        private final ThreadPoolExecutor executor;
        private final AtomicBoolean isShutdown = new AtomicBoolean(false);

        public ManagedExecutor(int poolSize) {
            if (poolSize <= 0) throw new IllegalArgumentException("Pool size must be positive");
            this.executor = new ThreadPoolExecutor(
                    poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()
            );
        }

        @Override
        @SuppressWarnings("unchecked")
        public Future<Object> submit(Callable<Object> task) {
            if (task == null) throw new NullPointerException("Task cannot be null");
            if (isShutdown.get()) throw new IllegalStateException("Executor is shut down");
            return (Future<Object>) (Future<?>) executor.submit(task);
        }

        @Override
        public void shutdown() {
            isShutdown.set(true);
            executor.shutdown();
        }

        @Override
        public boolean isShutdown() {
            return isShutdown.get();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return executor.awaitTermination(timeout, unit);
        }

        @Override
        public int getActiveCount() {
            return executor.getActiveCount();
        }

        @Override
        public long getCompletedCount() {
            return executor.getCompletedTaskCount();
        }
    }

    /**
     * A client that only needs to submit tasks -- depends on TaskSubmitter only.
     */
    public static class TaskClient {
        private final TaskSubmitter<Object> submitter;

        public TaskClient(TaskSubmitter<Object> submitter) {
            this.submitter = Objects.requireNonNull(submitter);
        }

        public Future<Object> runTask(Callable<Object> task) {
            return submitter.submit(task);
        }
    }

    /**
     * A client that only needs lifecycle management -- depends on LifecycleManageable only.
     */
    public static class LifecycleManager {
        private final LifecycleManageable manageable;

        public LifecycleManager(LifecycleManageable manageable) {
            this.manageable = Objects.requireNonNull(manageable);
        }

        public void gracefulShutdown(long timeoutMs) throws InterruptedException {
            manageable.shutdown();
            manageable.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
        }

        public boolean isRunning() {
            return !manageable.isShutdown();
        }
    }
}
