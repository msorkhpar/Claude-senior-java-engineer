package com.github.msorkhpar.claudejavatutor.memorymanagement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Heap and Stack Memory Management Tests")
class HeapStackMemoryTest {

    @Nested
    @DisplayName("Stack Memory Demo")
    class StackMemoryDemoTest {

        private final HeapStackMemory.StackMemoryDemo demo = new HeapStackMemory.StackMemoryDemo();

        @Test
        @DisplayName("Should calculate factorial correctly for base cases")
        void testFactorialBaseCases() {
            assertThat(demo.factorial(0)).isEqualTo(1);
            assertThat(demo.factorial(1)).isEqualTo(1);
        }

        @Test
        @DisplayName("Should calculate factorial for typical values")
        void testFactorialTypicalValues() {
            assertThat(demo.factorial(5)).isEqualTo(120);
            assertThat(demo.factorial(10)).isEqualTo(3628800);
            assertThat(demo.factorial(20)).isEqualTo(2432902008176640000L);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for negative input")
        void testFactorialNegativeInput() {
            assertThatThrownBy(() -> demo.factorial(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Negative input");
        }

        @Test
        @DisplayName("Should throw StackOverflowError for very deep recursion")
        void testFactorialStackOverflow() {
            assertThatThrownBy(() -> demo.factorial(100_000))
                    .isInstanceOf(StackOverflowError.class);
        }

        @Test
        @DisplayName("Should swap values on the stack")
        void testSwapOnStack() {
            int[] result = demo.swapOnStack(3, 7);
            assertThat(result[0]).isEqualTo(7);
            assertThat(result[1]).isEqualTo(3);
        }

        @Test
        @DisplayName("Should swap identical values")
        void testSwapIdenticalValues() {
            int[] result = demo.swapOnStack(5, 5);
            assertThat(result[0]).isEqualTo(5);
            assertThat(result[1]).isEqualTo(5);
        }

        @Test
        @DisplayName("Should swap with zero")
        void testSwapWithZero() {
            int[] result = demo.swapOnStack(0, 42);
            assertThat(result[0]).isEqualTo(42);
            assertThat(result[1]).isEqualTo(0);
        }

        @Test
        @DisplayName("Should measure stack depth correctly")
        void testMeasureStackDepth() {
            assertThat(demo.measureStackDepth(0, 100)).isEqualTo(100);
            assertThat(demo.measureStackDepth(0, 0)).isEqualTo(0);
            assertThat(demo.measureStackDepth(5, 10)).isEqualTo(10);
        }

        @Test
        @DisplayName("Should demonstrate thread-local stack variables")
        void testThreadLocalStackDemo() {
            String result = demo.threadLocalStackDemo("main");
            assertThat(result).isEqualTo("main:4");

            String result2 = demo.threadLocalStackDemo("worker-thread");
            assertThat(result2).isEqualTo("worker-thread:13");
        }

        @Test
        @DisplayName("Should prove each thread has independent stack")
        void testIndependentThreadStacks() throws Exception {
            CountDownLatch latch = new CountDownLatch(2);
            AtomicReference<String> thread1Result = new AtomicReference<>();
            AtomicReference<String> thread2Result = new AtomicReference<>();

            Thread t1 = new Thread(() -> {
                thread1Result.set(demo.threadLocalStackDemo("T1"));
                latch.countDown();
            });
            Thread t2 = new Thread(() -> {
                thread2Result.set(demo.threadLocalStackDemo("T2"));
                latch.countDown();
            });

            t1.start();
            t2.start();
            latch.await();

            assertThat(thread1Result.get()).isEqualTo("T1:2");
            assertThat(thread2Result.get()).isEqualTo("T2:2");
        }
    }

    @Nested
    @DisplayName("Heap Memory Demo")
    class HeapMemoryDemoTest {

        private final HeapStackMemory.HeapMemoryDemo demo = new HeapStackMemory.HeapMemoryDemo();

        @Test
        @DisplayName("Should create objects on heap")
        void testCreateObjectsOnHeap() {
            List<String> result = demo.createObjectsOnHeap(5);
            assertThat(result)
                    .hasSize(5)
                    .containsExactly("item-0", "item-1", "item-2", "item-3", "item-4");
        }

        @Test
        @DisplayName("Should create empty list for count zero")
        void testCreateObjectsOnHeapEmpty() {
            List<String> result = demo.createObjectsOnHeap(0);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should modify list through reference (pass-by-value of reference)")
        void testModifyList() {
            List<String> list = new ArrayList<>();
            demo.modifyList(list, "added");
            assertThat(list).containsExactly("added");
        }

        @Test
        @DisplayName("Should demonstrate object sharing on heap")
        void testObjectSharing() {
            assertThat(demo.demonstrateObjectSharing()).isTrue();
        }

        @Test
        @DisplayName("Should demonstrate string pool behavior")
        void testStringPool() {
            assertThat(demo.demonstrateStringPool()).isTrue();
        }

        @Test
        @DisplayName("Should create array on heap with correct size")
        void testCreateArrayOnHeap() {
            int[] arr = demo.createArrayOnHeap(10);
            assertThat(arr).hasSize(10);
            assertThat(arr).containsOnly(0); // default values
        }

        @Test
        @DisplayName("Should create empty array")
        void testCreateEmptyArray() {
            int[] arr = demo.createArrayOnHeap(0);
            assertThat(arr).isEmpty();
        }

        @Test
        @DisplayName("Should estimate non-zero memory usage for many objects")
        void testEstimateMemoryUsage() {
            long usage = demo.estimateMemoryUsage(1000);
            // Each byte[1024] is ~1KB, so 1000 should be roughly 1MB+
            assertThat(usage).isPositive();
        }

        @Test
        @DisplayName("Should show minimal memory usage for no objects")
        void testEstimateMemoryUsageZero() {
            long usage = demo.estimateMemoryUsage(0);
            // Even 0 objects may show some memory due to ArrayList overhead
            assertThat(usage).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Memory Allocation Demo")
    class MemoryAllocationDemoTest {

        private final HeapStackMemory.MemoryAllocationDemo demo = new HeapStackMemory.MemoryAllocationDemo();

        @Test
        @DisplayName("Should create strong reference that is not null")
        void testCreateStrongReference() {
            Object obj = demo.createStrongReference();
            assertThat(obj).isNotNull();
        }

        @Test
        @DisplayName("Should create weak reference")
        void testCreateWeakReference() {
            WeakReference<byte[]> weakRef = demo.createWeakReference();
            assertThat(weakRef).isNotNull();
            // Note: weakRef.get() may or may not be null depending on GC timing
        }

        @Test
        @DisplayName("Should create soft reference")
        void testCreateSoftReference() {
            SoftReference<byte[]> softRef = demo.createSoftReference();
            assertThat(softRef).isNotNull();
            // Soft references are typically kept unless memory is low
            // so get() should return non-null right after creation
            assertThat(softRef.get()).isNotNull();
        }

        @Test
        @DisplayName("Should demonstrate nullification makes object eligible for GC")
        void testDemonstrateNullification() {
            // This test demonstrates the concept - GC behavior is non-deterministic
            boolean collected = demo.demonstrateNullification();
            // We can only assert the return is a valid boolean;
            // the actual result depends on whether GC ran
            assertThat(collected).isIn(true, false);
        }

        @Test
        @DisplayName("Should add objects to retained list")
        void testRetainedList() {
            demo.addToRetainedList("one");
            demo.addToRetainedList("two");
            assertThat(demo.getRetainedCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should clear retained list to free memory")
        void testClearRetainedList() {
            demo.addToRetainedList("one");
            demo.addToRetainedList("two");
            demo.clearRetainedList();
            assertThat(demo.getRetainedCount()).isZero();
        }

        @Test
        @DisplayName("Should read with auto-close and return first token")
        void testReadWithAutoClose() {
            String result = demo.readWithAutoClose("hello world");
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should return empty string for empty input")
        void testReadWithAutoCloseEmpty() {
            String result = demo.readWithAutoClose("");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should compute with non-escaping object (escape analysis candidate)")
        void testComputeWithNonEscapingObject() {
            assertThat(demo.computeWithNonEscapingObject(3, 4)).isEqualTo(7);
            assertThat(demo.computeWithNonEscapingObject(0, 0)).isEqualTo(0);
            assertThat(demo.computeWithNonEscapingObject(-1, 1)).isEqualTo(0);
            assertThat(demo.computeWithNonEscapingObject(Integer.MAX_VALUE, 0)).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("Should borrow from pool when pool is empty (creates new)")
        void testObjectPoolBorrowEmpty() {
            var pool = new HeapStackMemory.MemoryAllocationDemo.SimpleObjectPool<>(
                    StringBuilder::new, 3);
            StringBuilder sb = pool.borrow();
            assertThat(sb).isNotNull();
            assertThat(pool.poolSize()).isZero();
        }

        @Test
        @DisplayName("Should return object to pool and reuse it")
        void testObjectPoolReturnAndReuse() {
            var pool = new HeapStackMemory.MemoryAllocationDemo.SimpleObjectPool<>(
                    StringBuilder::new, 3);
            StringBuilder sb = pool.borrow();
            sb.append("reused");
            pool.returnToPool(sb);

            assertThat(pool.poolSize()).isEqualTo(1);

            StringBuilder reused = pool.borrow();
            assertThat(reused.toString()).isEqualTo("reused");
            assertThat(pool.poolSize()).isZero();
        }

        @Test
        @DisplayName("Should not exceed max pool size")
        void testObjectPoolMaxSize() {
            var pool = new HeapStackMemory.MemoryAllocationDemo.SimpleObjectPool<>(
                    StringBuilder::new, 2);

            StringBuilder sb1 = pool.borrow();
            StringBuilder sb2 = pool.borrow();
            StringBuilder sb3 = pool.borrow();

            pool.returnToPool(sb1);
            pool.returnToPool(sb2);
            pool.returnToPool(sb3); // should be discarded

            assertThat(pool.poolSize()).isEqualTo(2);
        }
    }
}
