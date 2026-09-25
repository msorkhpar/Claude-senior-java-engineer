package com.github.msorkhpar.claudejavatutor.virtualthreads;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Virtual Thread Basics Tests")
class VirtualThreadBasicsTest {

    @Nested
    @DisplayName("Creating Virtual Threads")
    class CreatingVirtualThreadsTest {

        @Test
        @DisplayName("Should create and start a virtual thread using startVirtualThread")
        void testStartSimpleVirtualThread() throws InterruptedException {
            AtomicBoolean executed = new AtomicBoolean(false);

            Thread vt = VirtualThreadBasics.startSimpleVirtualThread(() -> executed.set(true));
            vt.join(5000);

            assertThat(executed.get()).isTrue();
            assertThat(vt.isVirtual()).isTrue();
        }

        @Test
        @DisplayName("Should create a virtual thread with builder pattern")
        void testCreateWithBuilder() throws InterruptedException {
            AtomicBoolean executed = new AtomicBoolean(false);

            Thread vt = VirtualThreadBasics.createWithBuilder(() -> executed.set(true));
            vt.join(5000);

            assertThat(executed.get()).isTrue();
            assertThat(vt.isVirtual()).isTrue();
            assertThat(vt.getName()).isEqualTo("custom-virtual-thread");
        }

        @Test
        @DisplayName("Should create an unstarted virtual thread")
        void testCreateUnstartedVirtualThread() throws InterruptedException {
            AtomicBoolean executed = new AtomicBoolean(false);

            Thread vt = VirtualThreadBasics.createUnstartedVirtualThread(() -> executed.set(true));

            assertThat(vt.isVirtual()).isTrue();
            assertThat(vt.getState()).isEqualTo(Thread.State.NEW);
            assertThat(executed.get()).isFalse();

            vt.start();
            vt.join(5000);
            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("Should create a named virtual thread with prefix and index")
        void testCreateNamedVirtualThread() throws InterruptedException {
            AtomicBoolean executed = new AtomicBoolean(false);

            Thread vt = VirtualThreadBasics.createNamedVirtualThread("worker-", 5,
                    () -> executed.set(true));
            vt.join(5000);

            assertThat(vt.getName()).isEqualTo("worker-5");
            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("Should create a platform thread for comparison")
        void testCreatePlatformThread() throws InterruptedException {
            AtomicBoolean executed = new AtomicBoolean(false);

            Thread pt = VirtualThreadBasics.createPlatformThread(() -> executed.set(true));
            pt.join(5000);

            assertThat(pt.isVirtual()).isFalse();
            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("Should throw IllegalThreadStateException when starting an already started thread")
        void testDoubleStartThrowsException() throws InterruptedException {
            Thread vt = VirtualThreadBasics.startSimpleVirtualThread(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            assertThatThrownBy(vt::start)
                    .isInstanceOf(IllegalThreadStateException.class);

            vt.join(5000);
        }
    }

    @Nested
    @DisplayName("Identifying Virtual Threads")
    class IdentifyingVirtualThreadsTest {

        @Test
        @DisplayName("Should correctly identify virtual thread")
        void testIsVirtualThread() {
            Thread vt = Thread.ofVirtual().unstarted(() -> {});
            assertThat(VirtualThreadBasics.isVirtualThread(vt)).isTrue();
        }

        @Test
        @DisplayName("Should correctly identify platform thread")
        void testIsNotVirtualThread() {
            Thread pt = Thread.ofPlatform().unstarted(() -> {});
            assertThat(VirtualThreadBasics.isVirtualThread(pt)).isFalse();
        }
    }

    @Nested
    @DisplayName("Virtual Thread Executor")
    class VirtualThreadExecutorTest {

        @Test
        @DisplayName("Should create a virtual thread per task executor")
        void testCreateVirtualThreadExecutor() throws ExecutionException, InterruptedException {
            try (ExecutorService executor = VirtualThreadBasics.createVirtualThreadExecutor()) {
                Future<Boolean> future = executor.submit(() -> Thread.currentThread().isVirtual());
                assertThat(future.get()).isTrue();
            }
        }

        @Test
        @DisplayName("Should execute multiple tasks with virtual thread executor")
        void testExecutorMultipleTasks() throws ExecutionException, InterruptedException {
            AtomicInteger counter = new AtomicInteger(0);
            try (ExecutorService executor = VirtualThreadBasics.createVirtualThreadExecutor()) {
                List<Future<?>> futures = new java.util.ArrayList<>();
                for (int i = 0; i < 100; i++) {
                    futures.add(executor.submit(counter::incrementAndGet));
                }
                for (Future<?> f : futures) {
                    f.get();
                }
            }
            assertThat(counter.get()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Creating Many Virtual Threads")
    class ManyVirtualThreadsTest {

        @Test
        @DisplayName("Should create and run many virtual threads concurrently")
        void testCreateManyVirtualThreads() throws InterruptedException {
            List<String> results = VirtualThreadBasics.createManyVirtualThreads(1000);
            assertThat(results).hasSize(1000);
        }

        @Test
        @DisplayName("Should handle zero threads gracefully")
        void testCreateZeroVirtualThreads() throws InterruptedException {
            List<String> results = VirtualThreadBasics.createManyVirtualThreads(0);
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should handle single virtual thread")
        void testCreateSingleVirtualThread() throws InterruptedException {
            List<String> results = VirtualThreadBasics.createManyVirtualThreads(1);
            assertThat(results).hasSize(1);
        }
    }

    @Nested
    @DisplayName("I/O Bound Task Performance")
    class IOBoundTasksTest {

        @Test
        @DisplayName("Should run I/O-bound tasks concurrently with virtual threads")
        void testRunIOBoundTasks() throws InterruptedException {
            // 100 tasks each sleeping 50ms - with virtual threads should complete much faster than 5000ms
            long elapsed = VirtualThreadBasics.runIOBoundTasks(100, 50);

            // All tasks run concurrently, so total time should be close to 50ms, not 5000ms
            assertThat(elapsed).isLessThan(2000);
        }

        @Test
        @DisplayName("Should handle zero tasks")
        void testRunZeroTasks() throws InterruptedException {
            long elapsed = VirtualThreadBasics.runIOBoundTasks(0, 50);
            assertThat(elapsed).isLessThan(1000);
        }
    }

    @Nested
    @DisplayName("Virtual Thread Properties")
    class VirtualThreadPropertiesTest {

        @Test
        @DisplayName("Should confirm virtual threads are daemon threads")
        void testVirtualThreadIsDaemon() {
            assertThat(VirtualThreadBasics.isVirtualThreadDaemon()).isTrue();
        }

        @Test
        @DisplayName("Should confirm virtual threads have NORM_PRIORITY")
        void testVirtualThreadPriority() {
            assertThat(VirtualThreadBasics.getVirtualThreadPriority()).isEqualTo(Thread.NORM_PRIORITY);
        }
    }

    @Nested
    @DisplayName("Virtual Thread Factory")
    class VirtualThreadFactoryTest {

        @Test
        @DisplayName("Should create a thread factory that produces virtual threads")
        void testCreateVirtualThreadFactory() throws InterruptedException {
            ThreadFactory factory = VirtualThreadBasics.createVirtualThreadFactory("test-");
            AtomicBoolean executed = new AtomicBoolean(false);

            Thread vt = factory.newThread(() -> executed.set(true));
            assertThat(vt.isVirtual()).isTrue();
            assertThat(vt.getName()).startsWith("test-");

            vt.start();
            vt.join(5000);
            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("Should execute tasks with factory-based executor and return thread names")
        void testExecuteWithFactory() throws InterruptedException, ExecutionException {
            List<String> names = VirtualThreadBasics.executeWithFactory(5);
            assertThat(names).hasSize(5);
            assertThat(names).allMatch(name -> name.startsWith("worker-"));
        }
    }

    @Nested
    @DisplayName("Virtual Thread Interruption")
    class InterruptionTest {

        @Test
        @DisplayName("Should interrupt virtual thread cooperatively")
        void testInterruptVirtualThread() throws InterruptedException {
            String result = VirtualThreadBasics.interruptVirtualThread();
            assertThat(result).contains("interrupted: true");
            assertThat(result).contains("Counter reached:");
        }
    }

    @Nested
    @DisplayName("Thread-Local Variables")
    class ThreadLocalTest {

        @Test
        @DisplayName("Should support thread-local variables in virtual threads")
        void testUseThreadLocal() throws InterruptedException {
            String result = VirtualThreadBasics.useThreadLocal();
            assertThat(result).isEqualTo("virtual-thread-value");
        }

        @Test
        @DisplayName("Should isolate thread-locals between virtual threads")
        void testThreadLocalIsolation() throws InterruptedException {
            ThreadLocal<String> tl = new ThreadLocal<>();
            AtomicReference<String> result1 = new AtomicReference<>();
            AtomicReference<String> result2 = new AtomicReference<>();

            Thread vt1 = Thread.startVirtualThread(() -> {
                tl.set("thread-1");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                result1.set(tl.get());
            });

            Thread vt2 = Thread.startVirtualThread(() -> {
                tl.set("thread-2");
                result2.set(tl.get());
            });

            vt1.join(5000);
            vt2.join(5000);

            assertThat(result1.get()).isEqualTo("thread-1");
            assertThat(result2.get()).isEqualTo("thread-2");
        }
    }
}
