package com.github.msorkhpar.claudejavatutor.memorymanagement;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Demonstrates Heap and Stack Memory Management concepts in Java.
 * Covers object allocation, method call stack behavior, and memory allocation/deallocation patterns.
 */
public class HeapStackMemory {

    /**
     * Demonstrates stack memory behavior with primitive types and method calls.
     * Each method call creates a new stack frame containing local variables.
     */
    public static class StackMemoryDemo {

        /**
         * Calculates factorial recursively, demonstrating stack frame creation.
         * Each recursive call adds a new stack frame to the call stack.
         *
         * @param n the number to calculate factorial for
         * @return the factorial of n
         * @throws IllegalArgumentException if n is negative
         * @throws StackOverflowError       if n is too large (stack exhaustion)
         */
        public long factorial(int n) {
            if (n < 0) {
                throw new IllegalArgumentException("Negative input not allowed: " + n);
            }
            if (n <= 1) {
                return 1;
            }
            return n * factorial(n - 1);
        }

        /**
         * Demonstrates that primitive local variables are stored on the stack
         * and are independent between method calls.
         */
        public int[] swapOnStack(int a, int b) {
            int temp = a;
            a = b;
            b = temp;
            return new int[]{a, b};
        }

        /**
         * Demonstrates stack depth by creating nested method calls.
         * Returns the depth reached before returning.
         */
        public int measureStackDepth(int currentDepth, int maxDepth) {
            if (currentDepth >= maxDepth) {
                return currentDepth;
            }
            return measureStackDepth(currentDepth + 1, maxDepth);
        }

        /**
         * Demonstrates that each thread has its own stack.
         * Returns the thread name and a local variable value.
         */
        public String threadLocalStackDemo(String threadName) {
            int localVar = threadName.length(); // stored on this thread's stack
            return threadName + ":" + localVar;
        }
    }

    /**
     * Demonstrates heap memory behavior with object allocation and references.
     */
    public static class HeapMemoryDemo {

        /**
         * Demonstrates that objects are always allocated on the heap,
         * while references to them live on the stack.
         */
        public List<String> createObjectsOnHeap(int count) {
            List<String> list = new ArrayList<>(count); // ArrayList on heap, 'list' ref on stack
            for (int i = 0; i < count; i++) {
                list.add("item-" + i); // String objects on heap
            }
            return list;
        }

        /**
         * Demonstrates that modifying an object through a reference
         * affects the heap object (pass-by-value of reference).
         */
        public void modifyList(List<String> list, String item) {
            list.add(item); // modifies the heap object
        }

        /**
         * Demonstrates object sharing on the heap - multiple references
         * can point to the same object.
         */
        public boolean demonstrateObjectSharing() {
            List<String> ref1 = new ArrayList<>();
            List<String> ref2 = ref1; // both point to same heap object
            ref1.add("shared");
            return ref2.contains("shared"); // true - same object
        }

        /**
         * Demonstrates String interning - strings with same content
         * may share the same heap location in the string pool.
         */
        public boolean demonstrateStringPool() {
            String s1 = "hello"; // from string pool
            String s2 = "hello"; // same reference from pool
            String s3 = new String("hello"); // new object on heap
            return s1 == s2 && s1 != s3;
        }

        /**
         * Demonstrates array allocation on the heap.
         */
        public int[] createArrayOnHeap(int size) {
            return new int[size]; // array object allocated on heap
        }

        /**
         * Estimates the approximate memory consumed by a list of strings.
         * This is a rough demonstration - actual memory depends on JVM implementation.
         */
        public long estimateMemoryUsage(int objectCount) {
            Runtime runtime = Runtime.getRuntime();
            runtime.gc(); // suggest GC to get baseline
            long before = runtime.totalMemory() - runtime.freeMemory();

            List<byte[]> objects = new ArrayList<>(objectCount);
            for (int i = 0; i < objectCount; i++) {
                objects.add(new byte[1024]); // each ~1KB
            }

            long after = runtime.totalMemory() - runtime.freeMemory();
            // Keep reference alive to prevent GC
            objects.size();
            return after - before;
        }
    }

    /**
     * Demonstrates memory allocation and deallocation patterns,
     * including reference types and their impact on garbage collection.
     */
    public static class MemoryAllocationDemo {

        /**
         * Demonstrates strong references - the default reference type.
         * Objects with strong references are never garbage collected.
         */
        public Object createStrongReference() {
            Object obj = new Object(); // strong reference
            return obj; // as long as this reference exists, obj won't be GC'd
        }

        /**
         * Demonstrates weak references using WeakReference.
         * Weakly referenced objects can be collected at next GC cycle.
         */
        public WeakReference<byte[]> createWeakReference() {
            byte[] data = new byte[1024 * 1024]; // 1MB
            return new WeakReference<>(data);
            // After this method returns, 'data' strong ref is gone.
            // Only the WeakReference remains, so GC can collect.
        }

        /**
         * Demonstrates soft references using SoftReference.
         * Softly referenced objects are collected only when memory is low.
         */
        public SoftReference<byte[]> createSoftReference() {
            byte[] data = new byte[1024 * 1024]; // 1MB
            return new SoftReference<>(data);
        }

        /**
         * Demonstrates that nullifying a reference makes the object
         * eligible for garbage collection.
         */
        public boolean demonstrateNullification() {
            Object obj = new Object();
            WeakReference<Object> weakRef = new WeakReference<>(obj);
            obj = null; // remove strong reference
            System.gc(); // suggest GC (not guaranteed)
            // After GC, weakRef.get() may return null
            return weakRef.get() == null;
        }

        /**
         * Demonstrates memory leak through static collections.
         * Objects added to static collections are never eligible for GC
         * unless explicitly removed.
         */
        private final List<Object> retainedObjects = new ArrayList<>();

        public void addToRetainedList(Object obj) {
            retainedObjects.add(obj);
        }

        public int getRetainedCount() {
            return retainedObjects.size();
        }

        public void clearRetainedList() {
            retainedObjects.clear();
        }

        /**
         * Demonstrates try-with-resources for automatic resource cleanup.
         * This is the preferred pattern for managing resources that implement AutoCloseable.
         */
        public String readWithAutoClose(String data) {
            try (var scanner = new java.util.Scanner(data)) {
                return scanner.hasNext() ? scanner.next() : "";
            }
        }

        /**
         * Demonstrates the escape analysis concept.
         * Objects that don't escape a method can potentially be allocated on the stack
         * by the JIT compiler (scalar replacement).
         */
        public int computeWithNonEscapingObject(int x, int y) {
            // The JIT compiler may optimize this Point allocation away
            // since it doesn't escape this method
            record Point(int x, int y) {}
            Point p = new Point(x, y);
            return p.x() + p.y();
        }

        /**
         * Demonstrates object pooling pattern for reducing GC pressure.
         * Reuses objects instead of creating new ones.
         */
        public static class SimpleObjectPool<T> {
            private final Deque<T> pool = new ArrayDeque<>();
            private final java.util.function.Supplier<T> factory;
            private final int maxSize;

            public SimpleObjectPool(java.util.function.Supplier<T> factory, int maxSize) {
                this.factory = factory;
                this.maxSize = maxSize;
            }

            public T borrow() {
                T obj = pool.pollFirst();
                return obj != null ? obj : factory.get();
            }

            public void returnToPool(T obj) {
                if (pool.size() < maxSize) {
                    pool.offerFirst(obj);
                }
                // else let it be GC'd
            }

            public int poolSize() {
                return pool.size();
            }
        }
    }
}
