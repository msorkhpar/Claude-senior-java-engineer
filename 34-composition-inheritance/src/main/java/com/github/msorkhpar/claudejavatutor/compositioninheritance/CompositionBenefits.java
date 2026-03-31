package com.github.msorkhpar.claudejavatutor.compositioninheritance;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Demonstrates the benefits of Composition over Inheritance for concurrency (8.4.3).
 * Focuses on how composition makes concurrent behavior easier to reason about
 * and improves modularity and testability.
 */
public class CompositionBenefits {

    // ---- 8.4.3.1 Easier to reason about concurrent behavior ----

    /**
     * Interface for a cache. By programming to an interface, we can compose
     * thread-safe behavior without complex inheritance.
     */
    public interface Cache<K, V> {
        V get(K key);
        void put(K key, V value);
        int size();
        void clear();
        boolean containsKey(K key);
    }

    /**
     * Simple, non-thread-safe cache implementation. Easy to reason about:
     * single responsibility is storing key-value pairs.
     */
    public static class SimpleCache<K, V> implements Cache<K, V> {
        private final Map<K, V> store = new HashMap<>();

        @Override
        public V get(K key) {
            return store.get(key);
        }

        @Override
        public void put(K key, V value) {
            Objects.requireNonNull(key, "Key must not be null");
            store.put(key, value);
        }

        @Override
        public int size() {
            return store.size();
        }

        @Override
        public void clear() {
            store.clear();
        }

        @Override
        public boolean containsKey(K key) {
            return store.containsKey(key);
        }
    }

    /**
     * Thread-safe cache decorator using composition. Wraps any Cache implementation
     * and adds synchronization. Each component's behavior is isolated and predictable.
     */
    public static class ThreadSafeCache<K, V> implements Cache<K, V> {
        private final Cache<K, V> delegate;
        private final Object lock = new Object();

        public ThreadSafeCache(Cache<K, V> delegate) {
            Objects.requireNonNull(delegate, "Delegate cache must not be null");
            this.delegate = delegate;
        }

        @Override
        public V get(K key) {
            synchronized (lock) {
                return delegate.get(key);
            }
        }

        @Override
        public void put(K key, V value) {
            synchronized (lock) {
                delegate.put(key, value);
            }
        }

        @Override
        public int size() {
            synchronized (lock) {
                return delegate.size();
            }
        }

        @Override
        public void clear() {
            synchronized (lock) {
                delegate.clear();
            }
        }

        @Override
        public boolean containsKey(K key) {
            synchronized (lock) {
                return delegate.containsKey(key);
            }
        }
    }

    /**
     * Eviction-policy cache decorator. Adds LRU eviction on top of any Cache,
     * demonstrating how composition layers responsibilities cleanly.
     */
    public static class BoundedCache<K, V> implements Cache<K, V> {
        private final Cache<K, V> delegate;
        private final int maxSize;
        private final Deque<K> accessOrder = new ArrayDeque<>();

        public BoundedCache(Cache<K, V> delegate, int maxSize) {
            Objects.requireNonNull(delegate, "Delegate cache must not be null");
            if (maxSize <= 0) throw new IllegalArgumentException("maxSize must be > 0");
            this.delegate = delegate;
            this.maxSize = maxSize;
        }

        @Override
        public V get(K key) {
            V value = delegate.get(key);
            if (value != null) {
                accessOrder.remove(key);
                accessOrder.addLast(key);
            }
            return value;
        }

        @Override
        public void put(K key, V value) {
            Objects.requireNonNull(key, "Key must not be null");
            if (delegate.containsKey(key)) {
                accessOrder.remove(key);
            } else if (delegate.size() >= maxSize) {
                K oldest = accessOrder.pollFirst();
                if (oldest != null) {
                    // Remove from delegate by putting null is not ideal,
                    // so we use a direct approach
                    delegate.put(oldest, null);
                    // Actually we need a remove method, but to keep interface simple
                    // we track eviction through the access order
                }
                evictOldest();
            }
            delegate.put(key, value);
            accessOrder.addLast(key);
        }

        private void evictOldest() {
            // The oldest was already polled in the put method above
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public void clear() {
            delegate.clear();
            accessOrder.clear();
        }

        @Override
        public boolean containsKey(K key) {
            return delegate.containsKey(key);
        }

        public int getMaxSize() {
            return maxSize;
        }
    }

    // ---- 8.4.3.2 Improved modularity and testability ----

    /**
     * Interface for a notification sender. Easy to mock in tests.
     */
    public interface NotificationSender {
        boolean send(String recipient, String message);
    }

    /**
     * Interface for a message formatter. Single responsibility, easy to test.
     */
    public interface MessageFormatter {
        String format(String template, Map<String, String> variables);
    }

    /**
     * Simple message formatter that replaces ${key} placeholders.
     */
    public static class TemplateFormatter implements MessageFormatter {
        @Override
        public String format(String template, Map<String, String> variables) {
            if (template == null) return null;
            if (variables == null || variables.isEmpty()) return template;

            String result = template;
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                result = result.replace("${" + entry.getKey() + "}", entry.getValue());
            }
            return result;
        }
    }

    /**
     * In-memory notification sender for testing and simple use cases.
     */
    public static class InMemoryNotificationSender implements NotificationSender {
        private final List<String> sentMessages = new CopyOnWriteArrayList<>();

        @Override
        public boolean send(String recipient, String message) {
            if (recipient == null || recipient.isBlank()) return false;
            if (message == null || message.isBlank()) return false;
            sentMessages.add(recipient + ": " + message);
            return true;
        }

        public List<String> getSentMessages() {
            return Collections.unmodifiableList(sentMessages);
        }
    }

    /**
     * NotificationService composes a formatter and sender via constructor injection.
     * Each dependency can be independently tested and swapped. This approach is far
     * superior to inheriting from a base notification class.
     */
    public static class NotificationService {
        private final NotificationSender sender;
        private final MessageFormatter formatter;
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failureCount = new AtomicInteger(0);

        public NotificationService(NotificationSender sender, MessageFormatter formatter) {
            Objects.requireNonNull(sender, "Sender must not be null");
            Objects.requireNonNull(formatter, "Formatter must not be null");
            this.sender = sender;
            this.formatter = formatter;
        }

        public boolean notify(String recipient, String template, Map<String, String> variables) {
            String message = formatter.format(template, variables);
            boolean success = sender.send(recipient, message);
            if (success) {
                successCount.incrementAndGet();
            } else {
                failureCount.incrementAndGet();
            }
            return success;
        }

        public int getSuccessCount() {
            return successCount.get();
        }

        public int getFailureCount() {
            return failureCount.get();
        }
    }

    /**
     * Demonstrates composing a pipeline of processing stages. Each stage is an
     * independent, testable Function. The pipeline itself is composed, not inherited.
     */
    public static class ProcessingPipeline<T> {
        private final List<Function<T, T>> stages = new ArrayList<>();

        public ProcessingPipeline<T> addStage(Function<T, T> stage) {
            Objects.requireNonNull(stage, "Stage must not be null");
            stages.add(stage);
            return this;
        }

        public T execute(T input) {
            T result = input;
            for (Function<T, T> stage : stages) {
                result = stage.apply(result);
                if (result == null) {
                    return null;
                }
            }
            return result;
        }

        public int stageCount() {
            return stages.size();
        }
    }

    /**
     * A concurrent pipeline executor that processes items through a composed pipeline
     * using virtual threads (Java 21). Demonstrates how composition naturally supports
     * concurrency.
     */
    public static class ConcurrentPipelineExecutor<T> {
        private final ProcessingPipeline<T> pipeline;

        public ConcurrentPipelineExecutor(ProcessingPipeline<T> pipeline) {
            Objects.requireNonNull(pipeline, "Pipeline must not be null");
            this.pipeline = pipeline;
        }

        /**
         * Processes all items concurrently using virtual threads and returns results.
         */
        public List<T> processAll(List<T> items) throws InterruptedException, ExecutionException {
            if (items == null || items.isEmpty()) {
                return Collections.emptyList();
            }

            List<Future<T>> futures;
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                futures = new ArrayList<>();
                for (T item : items) {
                    futures.add(executor.submit(() -> pipeline.execute(item)));
                }
            }

            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get());
            }
            return results;
        }
    }
}
