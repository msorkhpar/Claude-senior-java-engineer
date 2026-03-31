package com.github.msorkhpar.claudejavatutor.kissprinciple;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Demonstrates applying the KISS principle in concurrent programming.
 * Shows how simple designs lead to more correct and maintainable concurrent code,
 * while over-engineered approaches introduce unnecessary complexity and bug risk.
 */
public class KissConcurrency {

    // ==================== Counter: Simple vs Over-Engineered ====================

    /**
     * KISS approach: Use AtomicInteger for a thread-safe counter.
     * Simple, correct, and leverages Java's built-in concurrency utilities.
     */
    public static class SimpleAtomicCounter {

        private final AtomicInteger count = new AtomicInteger(0);

        public void increment() {
            count.incrementAndGet();
        }

        public void decrement() {
            count.decrementAndGet();
        }

        public int get() {
            return count.get();
        }
    }

    /**
     * Over-engineered approach: Custom locking, notification, and event system
     * for something that AtomicInteger handles perfectly.
     * Demonstrates unnecessary complexity that introduces potential for bugs.
     */
    public static class OverEngineeredCounter {

        private int count = 0;
        private final ReentrantLock lock = new ReentrantLock();
        private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

        public void increment() {
            lock.lock();
            try {
                count++;
                notifyListeners();
            } finally {
                lock.unlock();
            }
        }

        public void decrement() {
            lock.lock();
            try {
                count--;
                notifyListeners();
            } finally {
                lock.unlock();
            }
        }

        public int get() {
            lock.lock();
            try {
                return count;
            } finally {
                lock.unlock();
            }
        }

        public void addListener(Runnable listener) {
            listeners.add(listener);
        }

        private void notifyListeners() {
            listeners.forEach(Runnable::run);
        }
    }

    // ==================== Task Execution: Simple Approach ====================

    /**
     * KISS approach: Execute a list of tasks concurrently using ExecutorService.
     * Leverages the standard library directly without unnecessary wrappers.
     */
    public static class SimpleTaskExecutor {

        public <T> List<T> executeAll(List<Callable<T>> tasks) throws InterruptedException, ExecutionException {
            if (tasks.isEmpty()) {
                return Collections.emptyList();
            }
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<T>> futures = new ArrayList<>();
                for (Callable<T> task : tasks) {
                    futures.add(executor.submit(task));
                }
                List<T> results = new ArrayList<>();
                for (Future<T> future : futures) {
                    results.add(future.get());
                }
                return results;
            }
        }
    }

    // ==================== Producer-Consumer: Simple Approach ====================

    /**
     * KISS approach: Use BlockingQueue for producer-consumer pattern.
     * The JDK's BlockingQueue handles all synchronization internally.
     */
    public static class SimpleProducerConsumer<T> {

        private final BlockingQueue<T> queue;

        public SimpleProducerConsumer(int capacity) {
            this.queue = new LinkedBlockingQueue<>(capacity);
        }

        public void produce(T item) throws InterruptedException {
            queue.put(item);
        }

        public T consume() throws InterruptedException {
            return queue.take();
        }

        public int size() {
            return queue.size();
        }
    }

    // ==================== Caching: Simple vs Over-Engineered ====================

    /**
     * KISS approach: Use ConcurrentHashMap for thread-safe caching.
     * Minimal API, maximum correctness.
     */
    public static class SimpleConcurrentCache<K, V> {

        private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();

        public void put(K key, V value) {
            cache.put(key, value);
        }

        public V get(K key) {
            return cache.get(key);
        }

        public V computeIfAbsent(K key, Function<K, V> mappingFunction) {
            return cache.computeIfAbsent(key, mappingFunction);
        }

        public void remove(K key) {
            cache.remove(key);
        }
    }

    // ==================== Synchronized List: Simple Readable Wrapper ====================

    /**
     * KISS approach: A simple synchronized list using CopyOnWriteArrayList.
     * Clear, readable, and correct for concurrent use.
     */
    public static class SimpleSynchronizedList<T> {

        private final CopyOnWriteArrayList<T> list = new CopyOnWriteArrayList<>();

        public void add(T item) {
            list.add(item);
        }

        public T get(int index) {
            return list.get(index);
        }

        public int size() {
            return list.size();
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }
    }
}
