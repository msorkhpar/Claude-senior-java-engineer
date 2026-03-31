package com.github.msorkhpar.claudejavatutor.memorymanagement;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Demonstrates Garbage Collection algorithms and tuning concepts in Java.
 * Covers Mark-and-Sweep, Generational GC, reference types, and GC monitoring.
 */
public class GarbageCollection {

    /**
     * Demonstrates Mark-and-Sweep garbage collection concepts.
     * In the mark phase, the GC traverses object graphs from GC roots.
     * In the sweep phase, unmarked objects are reclaimed.
     */
    public static class MarkAndSweepDemo {

        /**
         * Simulates a simple mark-and-sweep collector on a graph of nodes.
         * This is a pedagogical simulation, not the actual JVM GC.
         */
        public static class Node {
            private final String name;
            private final List<Node> references = new ArrayList<>();
            private boolean marked = false;

            public Node(String name) {
                this.name = name;
            }

            public void addReference(Node other) {
                references.add(other);
            }

            public List<Node> getReferences() {
                return Collections.unmodifiableList(references);
            }

            public String getName() {
                return name;
            }

            public boolean isMarked() {
                return marked;
            }

            public void setMarked(boolean marked) {
                this.marked = marked;
            }
        }

        /**
         * Mark phase: traverse from roots and mark all reachable nodes.
         */
        public void mark(List<Node> roots) {
            for (Node root : roots) {
                markNode(root);
            }
        }

        private void markNode(Node node) {
            if (node == null || node.isMarked()) {
                return;
            }
            node.setMarked(true);
            for (Node ref : node.getReferences()) {
                markNode(ref);
            }
        }

        /**
         * Sweep phase: collect all unmarked nodes and reset marks.
         *
         * @param allNodes all nodes in the heap simulation
         * @return list of unreachable (garbage) nodes
         */
        public List<Node> sweep(List<Node> allNodes) {
            List<Node> garbage = new ArrayList<>();
            for (Node node : allNodes) {
                if (!node.isMarked()) {
                    garbage.add(node);
                } else {
                    node.setMarked(false); // reset for next cycle
                }
            }
            return garbage;
        }

        /**
         * Full mark-and-sweep cycle.
         *
         * @param roots    GC roots
         * @param allNodes all nodes in heap
         * @return names of collected (garbage) nodes
         */
        public List<String> collectGarbage(List<Node> roots, List<Node> allNodes) {
            mark(roots);
            List<Node> garbage = sweep(allNodes);
            return garbage.stream().map(Node::getName).toList();
        }
    }

    /**
     * Demonstrates generational garbage collection concepts.
     * Java's heap is divided into Young Generation (Eden + Survivor spaces)
     * and Old Generation (Tenured).
     */
    public static class GenerationalGCDemo {

        /**
         * Represents the generational spaces for simulation purposes.
         */
        public enum Generation {
            EDEN, SURVIVOR, OLD
        }

        public record SimulatedObject(String id, Generation generation, int age) {
            public SimulatedObject promote() {
                return switch (generation) {
                    case EDEN -> new SimulatedObject(id, Generation.SURVIVOR, age + 1);
                    case SURVIVOR -> {
                        if (age >= 3) { // threshold for promotion to old
                            yield new SimulatedObject(id, Generation.OLD, age + 1);
                        }
                        yield new SimulatedObject(id, Generation.SURVIVOR, age + 1);
                    }
                    case OLD -> new SimulatedObject(id, Generation.OLD, age + 1);
                };
            }
        }

        /**
         * Simulates object allocation in Eden space.
         */
        public SimulatedObject allocate(String id) {
            return new SimulatedObject(id, Generation.EDEN, 0);
        }

        /**
         * Simulates a minor GC: promotes surviving objects from Eden/Survivor.
         *
         * @param objects  all objects in young gen
         * @param liveIds  set of IDs that are still reachable
         * @return list of surviving objects after promotion
         */
        public List<SimulatedObject> minorGC(List<SimulatedObject> objects, Set<String> liveIds) {
            List<SimulatedObject> survivors = new ArrayList<>();
            for (SimulatedObject obj : objects) {
                if (liveIds.contains(obj.id())) {
                    survivors.add(obj.promote());
                }
                // Dead objects are simply not added - simulating collection
            }
            return survivors;
        }

        /**
         * Simulates a major (full) GC: collects from all generations.
         *
         * @param allObjects all objects across all generations
         * @param liveIds    set of IDs that are still reachable
         * @return list of surviving objects
         */
        public List<SimulatedObject> majorGC(List<SimulatedObject> allObjects, Set<String> liveIds) {
            return allObjects.stream()
                    .filter(obj -> liveIds.contains(obj.id()))
                    .toList();
        }

        /**
         * Groups objects by their generation.
         */
        public Map<Generation, List<SimulatedObject>> groupByGeneration(List<SimulatedObject> objects) {
            Map<Generation, List<SimulatedObject>> grouped = new EnumMap<>(Generation.class);
            for (Generation gen : Generation.values()) {
                grouped.put(gen, new ArrayList<>());
            }
            for (SimulatedObject obj : objects) {
                grouped.get(obj.generation()).add(obj);
            }
            return grouped;
        }
    }

    /**
     * Demonstrates GC tuning and monitoring through JMX APIs.
     */
    public static class GCMonitoring {

        /**
         * Returns information about available garbage collectors.
         */
        public List<String> getGarbageCollectorNames() {
            return ManagementFactory.getGarbageCollectorMXBeans()
                    .stream()
                    .map(GarbageCollectorMXBean::getName)
                    .toList();
        }

        /**
         * Returns total GC count across all collectors.
         */
        public long getTotalGCCount() {
            return ManagementFactory.getGarbageCollectorMXBeans()
                    .stream()
                    .mapToLong(GarbageCollectorMXBean::getCollectionCount)
                    .filter(count -> count >= 0)
                    .sum();
        }

        /**
         * Returns total time spent in GC in milliseconds.
         */
        public long getTotalGCTime() {
            return ManagementFactory.getGarbageCollectorMXBeans()
                    .stream()
                    .mapToLong(GarbageCollectorMXBean::getCollectionTime)
                    .filter(time -> time >= 0)
                    .sum();
        }

        /**
         * Returns current heap memory usage information.
         */
        public MemoryUsage getHeapMemoryUsage() {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            return memoryBean.getHeapMemoryUsage();
        }

        /**
         * Returns current non-heap memory usage (metaspace, code cache, etc.).
         */
        public MemoryUsage getNonHeapMemoryUsage() {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            return memoryBean.getNonHeapMemoryUsage();
        }

        /**
         * Calculates heap usage percentage.
         */
        public double getHeapUsagePercentage() {
            MemoryUsage usage = getHeapMemoryUsage();
            if (usage.getMax() <= 0) {
                return -1.0; // max not defined
            }
            return (double) usage.getUsed() / usage.getMax() * 100.0;
        }
    }

    /**
     * Demonstrates reference types and their interaction with GC.
     */
    public static class ReferenceTypesDemo {

        /**
         * Demonstrates WeakReference behavior with GC.
         * WeakReferences are collected eagerly when no strong refs exist.
         */
        public WeakReference<byte[]> createWeakRef(int sizeBytes) {
            byte[] data = new byte[sizeBytes];
            Arrays.fill(data, (byte) 1);
            return new WeakReference<>(data);
        }

        /**
         * Demonstrates SoftReference behavior.
         * SoftReferences are kept until memory pressure forces collection.
         */
        public SoftReference<byte[]> createSoftRef(int sizeBytes) {
            byte[] data = new byte[sizeBytes];
            Arrays.fill(data, (byte) 2);
            return new SoftReference<>(data);
        }

        /**
         * Demonstrates PhantomReference with ReferenceQueue.
         * PhantomReferences are enqueued after the referent is finalized.
         */
        public PhantomReference<Object> createPhantomRef(ReferenceQueue<Object> queue) {
            Object obj = new Object();
            return new PhantomReference<>(obj, queue);
            // After method returns, obj has no strong reference
        }

        /**
         * Demonstrates a simple cache using WeakHashMap.
         * Entries are automatically removed when keys are garbage collected.
         */
        public Map<Object, String> createWeakCache() {
            return new WeakHashMap<>();
        }

        /**
         * Demonstrates building a simple soft-reference cache.
         */
        public static class SoftCache<K, V> {
            private final Map<K, SoftReference<V>> cache = new ConcurrentHashMap<>();

            public void put(K key, V value) {
                cache.put(key, new SoftReference<>(value));
            }

            public V get(K key) {
                SoftReference<V> ref = cache.get(key);
                if (ref == null) {
                    return null;
                }
                V value = ref.get();
                if (value == null) {
                    cache.remove(key); // clean up stale entry
                }
                return value;
            }

            public int size() {
                return cache.size();
            }

            public void clear() {
                cache.clear();
            }
        }
    }

    /**
     * Demonstrates common memory leak patterns and how to avoid them.
     */
    public static class MemoryLeakPatterns {

        /**
         * Demonstrates a memory leak caused by not removing listeners.
         */
        public interface EventListener {
            void onEvent(String event);
        }

        public static class EventSource {
            private final List<EventListener> listeners = new ArrayList<>();

            public void addListener(EventListener listener) {
                listeners.add(listener);
            }

            public void removeListener(EventListener listener) {
                listeners.remove(listener);
            }

            public void fireEvent(String event) {
                for (EventListener listener : listeners) {
                    listener.onEvent(event);
                }
            }

            public int listenerCount() {
                return listeners.size();
            }
        }

        /**
         * Demonstrates memory leak through unclosed resources.
         * Always use try-with-resources for AutoCloseable objects.
         */
        public static class ResourceHolder implements AutoCloseable {
            private byte[] data;
            private boolean closed = false;

            public ResourceHolder(int sizeBytes) {
                this.data = new byte[sizeBytes];
            }

            public boolean isClosed() {
                return closed;
            }

            public int dataLength() {
                return data != null ? data.length : 0;
            }

            @Override
            public void close() {
                data = null;
                closed = true;
            }
        }

        /**
         * Demonstrates a custom stack that can leak memory if not properly implemented.
         * The "leaky" version keeps obsolete references; the "fixed" version nulls them out.
         */
        public static class FixedStack<E> {
            private Object[] elements;
            private int size = 0;
            private static final int DEFAULT_CAPACITY = 16;

            public FixedStack() {
                elements = new Object[DEFAULT_CAPACITY];
            }

            public void push(E element) {
                ensureCapacity();
                elements[size++] = element;
            }

            @SuppressWarnings("unchecked")
            public E pop() {
                if (size == 0) {
                    throw new EmptyStackException();
                }
                E result = (E) elements[--size];
                elements[size] = null; // eliminate obsolete reference
                return result;
            }

            public int size() {
                return size;
            }

            public boolean isEmpty() {
                return size == 0;
            }

            private void ensureCapacity() {
                if (size == elements.length) {
                    elements = Arrays.copyOf(elements, 2 * size + 1);
                }
            }
        }
    }
}
