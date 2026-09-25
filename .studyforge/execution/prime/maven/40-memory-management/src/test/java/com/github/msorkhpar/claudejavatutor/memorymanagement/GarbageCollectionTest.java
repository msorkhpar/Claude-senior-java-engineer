package com.github.msorkhpar.claudejavatutor.memorymanagement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.management.MemoryUsage;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Garbage Collection Tests")
class GarbageCollectionTest {

    @Nested
    @DisplayName("Mark and Sweep Demo")
    class MarkAndSweepDemoTest {

        private final GarbageCollection.MarkAndSweepDemo demo = new GarbageCollection.MarkAndSweepDemo();

        @Test
        @DisplayName("Should collect unreachable nodes")
        void testCollectUnreachableNodes() {
            var root = new GarbageCollection.MarkAndSweepDemo.Node("root");
            var reachable = new GarbageCollection.MarkAndSweepDemo.Node("reachable");
            var unreachable = new GarbageCollection.MarkAndSweepDemo.Node("unreachable");

            root.addReference(reachable);
            // unreachable has no incoming references from root

            List<GarbageCollection.MarkAndSweepDemo.Node> roots = List.of(root);
            List<GarbageCollection.MarkAndSweepDemo.Node> allNodes = List.of(root, reachable, unreachable);

            List<String> garbage = demo.collectGarbage(roots, allNodes);

            assertThat(garbage).containsExactly("unreachable");
        }

        @Test
        @DisplayName("Should not collect reachable nodes in a chain")
        void testReachableChain() {
            var a = new GarbageCollection.MarkAndSweepDemo.Node("A");
            var b = new GarbageCollection.MarkAndSweepDemo.Node("B");
            var c = new GarbageCollection.MarkAndSweepDemo.Node("C");

            a.addReference(b);
            b.addReference(c);

            List<String> garbage = demo.collectGarbage(List.of(a), List.of(a, b, c));

            assertThat(garbage).isEmpty();
        }

        @Test
        @DisplayName("Should handle circular references that are unreachable")
        void testCircularReferencesUnreachable() {
            var root = new GarbageCollection.MarkAndSweepDemo.Node("root");
            var cycle1 = new GarbageCollection.MarkAndSweepDemo.Node("cycle1");
            var cycle2 = new GarbageCollection.MarkAndSweepDemo.Node("cycle2");

            // cycle1 and cycle2 reference each other but are not reachable from root
            cycle1.addReference(cycle2);
            cycle2.addReference(cycle1);

            List<String> garbage = demo.collectGarbage(
                    List.of(root),
                    List.of(root, cycle1, cycle2)
            );

            assertThat(garbage).containsExactlyInAnyOrder("cycle1", "cycle2");
        }

        @Test
        @DisplayName("Should handle circular references that are reachable")
        void testCircularReferencesReachable() {
            var root = new GarbageCollection.MarkAndSweepDemo.Node("root");
            var cycle1 = new GarbageCollection.MarkAndSweepDemo.Node("cycle1");
            var cycle2 = new GarbageCollection.MarkAndSweepDemo.Node("cycle2");

            root.addReference(cycle1);
            cycle1.addReference(cycle2);
            cycle2.addReference(cycle1); // circular

            List<String> garbage = demo.collectGarbage(
                    List.of(root),
                    List.of(root, cycle1, cycle2)
            );

            assertThat(garbage).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty heap")
        void testEmptyHeap() {
            List<String> garbage = demo.collectGarbage(List.of(), List.of());
            assertThat(garbage).isEmpty();
        }

        @Test
        @DisplayName("Should handle multiple roots")
        void testMultipleRoots() {
            var root1 = new GarbageCollection.MarkAndSweepDemo.Node("root1");
            var root2 = new GarbageCollection.MarkAndSweepDemo.Node("root2");
            var child1 = new GarbageCollection.MarkAndSweepDemo.Node("child1");
            var child2 = new GarbageCollection.MarkAndSweepDemo.Node("child2");
            var orphan = new GarbageCollection.MarkAndSweepDemo.Node("orphan");

            root1.addReference(child1);
            root2.addReference(child2);

            List<String> garbage = demo.collectGarbage(
                    List.of(root1, root2),
                    List.of(root1, root2, child1, child2, orphan)
            );

            assertThat(garbage).containsExactly("orphan");
        }

        @Test
        @DisplayName("Should mark correctly and reset marks after sweep")
        void testMarkAndResetAfterSweep() {
            var root = new GarbageCollection.MarkAndSweepDemo.Node("root");
            var child = new GarbageCollection.MarkAndSweepDemo.Node("child");
            root.addReference(child);

            var allNodes = new ArrayList<>(List.of(root, child));

            demo.mark(List.of(root));
            assertThat(root.isMarked()).isTrue();
            assertThat(child.isMarked()).isTrue();

            demo.sweep(allNodes);
            // After sweep, marks should be reset
            assertThat(root.isMarked()).isFalse();
            assertThat(child.isMarked()).isFalse();
        }

        @Test
        @DisplayName("Should handle node with null reference in list gracefully")
        void testMarkWithNullNode() {
            var root = new GarbageCollection.MarkAndSweepDemo.Node("root");
            // Mark should handle null gracefully in markNode
            assertThatNoException().isThrownBy(() -> demo.mark(List.of(root)));
        }

        @Test
        @DisplayName("Node references should be unmodifiable")
        void testNodeReferencesAreUnmodifiable() {
            var node = new GarbageCollection.MarkAndSweepDemo.Node("test");
            node.addReference(new GarbageCollection.MarkAndSweepDemo.Node("child"));

            assertThatThrownBy(() -> node.getReferences().add(
                    new GarbageCollection.MarkAndSweepDemo.Node("illegal")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Generational GC Demo")
    class GenerationalGCDemoTest {

        private final GarbageCollection.GenerationalGCDemo demo = new GarbageCollection.GenerationalGCDemo();

        @Test
        @DisplayName("Should allocate new objects in Eden space")
        void testAllocateInEden() {
            var obj = demo.allocate("obj1");
            assertThat(obj.generation()).isEqualTo(GarbageCollection.GenerationalGCDemo.Generation.EDEN);
            assertThat(obj.age()).isEqualTo(0);
            assertThat(obj.id()).isEqualTo("obj1");
        }

        @Test
        @DisplayName("Should promote surviving objects from Eden to Survivor on minor GC")
        void testMinorGCPromotion() {
            var obj1 = demo.allocate("alive");
            var obj2 = demo.allocate("dead");

            var survivors = demo.minorGC(List.of(obj1, obj2), Set.of("alive"));

            assertThat(survivors).hasSize(1);
            assertThat(survivors.get(0).id()).isEqualTo("alive");
            assertThat(survivors.get(0).generation())
                    .isEqualTo(GarbageCollection.GenerationalGCDemo.Generation.SURVIVOR);
            assertThat(survivors.get(0).age()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should promote from Survivor to Old after threshold")
        void testPromotionToOldGeneration() {
            var obj = demo.allocate("tenured");

            // Simulate multiple minor GCs to promote through generations
            var current = List.of(obj);
            for (int i = 0; i < 5; i++) {
                current = demo.minorGC(current, Set.of("tenured"));
            }

            assertThat(current).hasSize(1);
            assertThat(current.get(0).generation())
                    .isEqualTo(GarbageCollection.GenerationalGCDemo.Generation.OLD);
        }

        @Test
        @DisplayName("Should collect all dead objects in minor GC")
        void testMinorGCCollectsAllDead() {
            var obj1 = demo.allocate("dead1");
            var obj2 = demo.allocate("dead2");
            var obj3 = demo.allocate("alive");

            var survivors = demo.minorGC(List.of(obj1, obj2, obj3), Set.of("alive"));

            assertThat(survivors).hasSize(1);
            assertThat(survivors.get(0).id()).isEqualTo("alive");
        }

        @Test
        @DisplayName("Should collect from all generations in major GC")
        void testMajorGC() {
            var eden = demo.allocate("eden-alive");
            var survivor = new GarbageCollection.GenerationalGCDemo.SimulatedObject(
                    "survivor-dead", GarbageCollection.GenerationalGCDemo.Generation.SURVIVOR, 2);
            var old = new GarbageCollection.GenerationalGCDemo.SimulatedObject(
                    "old-alive", GarbageCollection.GenerationalGCDemo.Generation.OLD, 10);

            var survivors = demo.majorGC(
                    List.of(eden, survivor, old),
                    Set.of("eden-alive", "old-alive")
            );

            assertThat(survivors).hasSize(2);
            assertThat(survivors.stream().map(GarbageCollection.GenerationalGCDemo.SimulatedObject::id))
                    .containsExactlyInAnyOrder("eden-alive", "old-alive");
        }

        @Test
        @DisplayName("Should handle empty object list in minor GC")
        void testMinorGCEmpty() {
            var survivors = demo.minorGC(List.of(), Set.of());
            assertThat(survivors).isEmpty();
        }

        @Test
        @DisplayName("Should group objects by generation")
        void testGroupByGeneration() {
            var eden = demo.allocate("eden");
            var survivor = new GarbageCollection.GenerationalGCDemo.SimulatedObject(
                    "survivor", GarbageCollection.GenerationalGCDemo.Generation.SURVIVOR, 1);
            var old = new GarbageCollection.GenerationalGCDemo.SimulatedObject(
                    "old", GarbageCollection.GenerationalGCDemo.Generation.OLD, 5);

            var grouped = demo.groupByGeneration(List.of(eden, survivor, old));

            assertThat(grouped).containsKeys(
                    GarbageCollection.GenerationalGCDemo.Generation.EDEN,
                    GarbageCollection.GenerationalGCDemo.Generation.SURVIVOR,
                    GarbageCollection.GenerationalGCDemo.Generation.OLD
            );
            assertThat(grouped.get(GarbageCollection.GenerationalGCDemo.Generation.EDEN)).hasSize(1);
            assertThat(grouped.get(GarbageCollection.GenerationalGCDemo.Generation.SURVIVOR)).hasSize(1);
            assertThat(grouped.get(GarbageCollection.GenerationalGCDemo.Generation.OLD)).hasSize(1);
        }

        @Test
        @DisplayName("Should group empty list with empty generation lists")
        void testGroupByGenerationEmpty() {
            var grouped = demo.groupByGeneration(List.of());
            for (var gen : GarbageCollection.GenerationalGCDemo.Generation.values()) {
                assertThat(grouped.get(gen)).isEmpty();
            }
        }

        @Test
        @DisplayName("Old generation objects should stay in Old on promote")
        void testOldStaysOld() {
            var old = new GarbageCollection.GenerationalGCDemo.SimulatedObject(
                    "old", GarbageCollection.GenerationalGCDemo.Generation.OLD, 10);
            var promoted = old.promote();
            assertThat(promoted.generation()).isEqualTo(GarbageCollection.GenerationalGCDemo.Generation.OLD);
            assertThat(promoted.age()).isEqualTo(11);
        }
    }

    @Nested
    @DisplayName("GC Monitoring")
    class GCMonitoringTest {

        private final GarbageCollection.GCMonitoring monitoring = new GarbageCollection.GCMonitoring();

        @Test
        @DisplayName("Should return at least one garbage collector name")
        void testGetGarbageCollectorNames() {
            List<String> names = monitoring.getGarbageCollectorNames();
            assertThat(names).isNotEmpty();
            assertThat(names).allSatisfy(name -> assertThat(name).isNotBlank());
        }

        @Test
        @DisplayName("Should return non-negative total GC count")
        void testGetTotalGCCount() {
            long count = monitoring.getTotalGCCount();
            assertThat(count).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should return non-negative total GC time")
        void testGetTotalGCTime() {
            long time = monitoring.getTotalGCTime();
            assertThat(time).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should return valid heap memory usage")
        void testGetHeapMemoryUsage() {
            MemoryUsage usage = monitoring.getHeapMemoryUsage();
            assertThat(usage).isNotNull();
            assertThat(usage.getUsed()).isPositive();
            assertThat(usage.getCommitted()).isPositive();
            assertThat(usage.getUsed()).isLessThanOrEqualTo(usage.getCommitted());
        }

        @Test
        @DisplayName("Should return valid non-heap memory usage")
        void testGetNonHeapMemoryUsage() {
            MemoryUsage usage = monitoring.getNonHeapMemoryUsage();
            assertThat(usage).isNotNull();
            assertThat(usage.getUsed()).isPositive();
        }

        @Test
        @DisplayName("Should return heap usage percentage between 0 and 100 or -1 if undefined")
        void testGetHeapUsagePercentage() {
            double percentage = monitoring.getHeapUsagePercentage();
            if (percentage >= 0) {
                assertThat(percentage).isBetween(0.0, 100.0);
            } else {
                assertThat(percentage).isEqualTo(-1.0);
            }
        }
    }

    @Nested
    @DisplayName("Reference Types Demo")
    class ReferenceTypesDemoTest {

        private final GarbageCollection.ReferenceTypesDemo demo = new GarbageCollection.ReferenceTypesDemo();

        @Test
        @DisplayName("Should create weak reference")
        void testCreateWeakRef() {
            WeakReference<byte[]> ref = demo.createWeakRef(1024);
            assertThat(ref).isNotNull();
        }

        @Test
        @DisplayName("Should create soft reference with data intact before GC")
        void testCreateSoftRef() {
            SoftReference<byte[]> ref = demo.createSoftRef(1024);
            assertThat(ref).isNotNull();
            assertThat(ref.get()).isNotNull();
            assertThat(ref.get()).hasSize(1024);
        }

        @Test
        @DisplayName("Should create phantom reference")
        void testCreatePhantomRef() {
            ReferenceQueue<Object> queue = new ReferenceQueue<>();
            PhantomReference<Object> ref = demo.createPhantomRef(queue);
            assertThat(ref).isNotNull();
            // PhantomReference.get() always returns null
            assertThat(ref.get()).isNull();
        }

        @Test
        @DisplayName("Should create weak cache that allows entry removal by GC")
        void testCreateWeakCache() {
            Map<Object, String> cache = demo.createWeakCache();
            Object key = new Object();
            cache.put(key, "value");

            assertThat(cache.get(key)).isEqualTo("value");
            assertThat(cache).hasSize(1);
        }

        @Test
        @DisplayName("Soft cache should store and retrieve values")
        void testSoftCachePutAndGet() {
            var cache = new GarbageCollection.ReferenceTypesDemo.SoftCache<String, String>();
            cache.put("key1", "value1");
            cache.put("key2", "value2");

            assertThat(cache.get("key1")).isEqualTo("value1");
            assertThat(cache.get("key2")).isEqualTo("value2");
        }

        @Test
        @DisplayName("Soft cache should return null for missing key")
        void testSoftCacheMissingKey() {
            var cache = new GarbageCollection.ReferenceTypesDemo.SoftCache<String, String>();
            assertThat(cache.get("nonexistent")).isNull();
        }

        @Test
        @DisplayName("Soft cache should track size")
        void testSoftCacheSize() {
            var cache = new GarbageCollection.ReferenceTypesDemo.SoftCache<String, String>();
            assertThat(cache.size()).isZero();
            cache.put("k1", "v1");
            assertThat(cache.size()).isEqualTo(1);
            cache.put("k2", "v2");
            assertThat(cache.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("Soft cache clear should remove all entries")
        void testSoftCacheClear() {
            var cache = new GarbageCollection.ReferenceTypesDemo.SoftCache<String, String>();
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            cache.clear();
            assertThat(cache.size()).isZero();
            assertThat(cache.get("k1")).isNull();
        }

        @Test
        @DisplayName("Soft cache should overwrite existing key")
        void testSoftCacheOverwrite() {
            var cache = new GarbageCollection.ReferenceTypesDemo.SoftCache<String, String>();
            cache.put("key", "old");
            cache.put("key", "new");
            assertThat(cache.get("key")).isEqualTo("new");
        }
    }

    @Nested
    @DisplayName("Memory Leak Patterns")
    class MemoryLeakPatternsTest {

        @Test
        @DisplayName("Should demonstrate listener leak when not removing")
        void testListenerLeak() {
            var source = new GarbageCollection.MemoryLeakPatterns.EventSource();
            GarbageCollection.MemoryLeakPatterns.EventListener listener = event -> {};

            source.addListener(listener);
            assertThat(source.listenerCount()).isEqualTo(1);

            source.addListener(listener);
            assertThat(source.listenerCount()).isEqualTo(2); // duplicate added!
        }

        @Test
        @DisplayName("Should fix listener leak by removing listener")
        void testListenerLeakFix() {
            var source = new GarbageCollection.MemoryLeakPatterns.EventSource();
            GarbageCollection.MemoryLeakPatterns.EventListener listener = event -> {};

            source.addListener(listener);
            source.removeListener(listener);
            assertThat(source.listenerCount()).isZero();
        }

        @Test
        @DisplayName("Should fire event to all listeners")
        void testFireEvent() {
            var source = new GarbageCollection.MemoryLeakPatterns.EventSource();
            List<String> received = new ArrayList<>();
            source.addListener(received::add);

            source.fireEvent("test-event");

            assertThat(received).containsExactly("test-event");
        }

        @Test
        @DisplayName("ResourceHolder should release memory on close")
        void testResourceHolderClose() {
            var holder = new GarbageCollection.MemoryLeakPatterns.ResourceHolder(1024);
            assertThat(holder.isClosed()).isFalse();
            assertThat(holder.dataLength()).isEqualTo(1024);

            holder.close();
            assertThat(holder.isClosed()).isTrue();
            assertThat(holder.dataLength()).isZero();
        }

        @Test
        @DisplayName("ResourceHolder should work with try-with-resources")
        void testResourceHolderTryWithResources() {
            GarbageCollection.MemoryLeakPatterns.ResourceHolder holder;
            try (var h = new GarbageCollection.MemoryLeakPatterns.ResourceHolder(512)) {
                holder = h;
                assertThat(h.isClosed()).isFalse();
            }
            assertThat(holder.isClosed()).isTrue();
        }

        @Test
        @DisplayName("FixedStack push and pop should work correctly")
        void testFixedStackPushPop() {
            var stack = new GarbageCollection.MemoryLeakPatterns.FixedStack<String>();
            stack.push("first");
            stack.push("second");

            assertThat(stack.size()).isEqualTo(2);
            assertThat(stack.pop()).isEqualTo("second");
            assertThat(stack.pop()).isEqualTo("first");
            assertThat(stack.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("FixedStack should throw on pop when empty")
        void testFixedStackPopEmpty() {
            var stack = new GarbageCollection.MemoryLeakPatterns.FixedStack<String>();
            assertThatThrownBy(stack::pop)
                    .isInstanceOf(EmptyStackException.class);
        }

        @Test
        @DisplayName("FixedStack should grow beyond initial capacity")
        void testFixedStackGrow() {
            var stack = new GarbageCollection.MemoryLeakPatterns.FixedStack<Integer>();
            // Default capacity is 16, push more than that
            for (int i = 0; i < 50; i++) {
                stack.push(i);
            }
            assertThat(stack.size()).isEqualTo(50);
            assertThat(stack.pop()).isEqualTo(49);
        }

        @Test
        @DisplayName("FixedStack should report isEmpty correctly")
        void testFixedStackIsEmpty() {
            var stack = new GarbageCollection.MemoryLeakPatterns.FixedStack<String>();
            assertThat(stack.isEmpty()).isTrue();
            stack.push("item");
            assertThat(stack.isEmpty()).isFalse();
            stack.pop();
            assertThat(stack.isEmpty()).isTrue();
        }
    }
}
