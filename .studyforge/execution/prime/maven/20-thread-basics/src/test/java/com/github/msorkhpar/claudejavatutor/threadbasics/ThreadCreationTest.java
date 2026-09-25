package com.github.msorkhpar.claudejavatutor.threadbasics;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for ThreadCreation demonstrating various thread creation methods.
 */
class ThreadCreationTest {

    // ---- Thread subclass tests ----

    @Test
    void testCountingThreadExecutes() throws InterruptedException {
        ThreadCreation.CountingThread thread = ThreadCreation.createViaThreadSubclass("counter");
        assertThat(thread.getState()).isEqualTo(Thread.State.NEW);

        thread.start();
        thread.join();

        assertThat(thread.getCount()).isEqualTo(5);
        assertThat(thread.getName()).isEqualTo("counter");
    }

    @Test
    void testCountingThreadNameIsSet() {
        ThreadCreation.CountingThread thread = ThreadCreation.createViaThreadSubclass("my-thread");
        assertThat(thread.getName()).isEqualTo("my-thread");
    }

    // ---- Runnable tests ----

    @Test
    void testCountingTaskViaRunnable() throws InterruptedException {
        ThreadCreation.CountingTask task = new ThreadCreation.CountingTask();
        Thread thread = ThreadCreation.createViaRunnable("runnable-thread", task);

        assertThat(thread.getName()).isEqualTo("runnable-thread");

        thread.start();
        thread.join();

        assertThat(task.getCount()).isEqualTo(5);
    }

    @Test
    void testSameRunnableCanBeUsedByMultipleThreads() throws InterruptedException {
        AtomicBoolean ran1 = new AtomicBoolean(false);
        AtomicBoolean ran2 = new AtomicBoolean(false);

        Runnable task1 = () -> ran1.set(true);
        Runnable task2 = () -> ran2.set(true);

        Thread t1 = ThreadCreation.createViaRunnable("t1", task1);
        Thread t2 = ThreadCreation.createViaRunnable("t2", task2);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertThat(ran1).isTrue();
        assertThat(ran2).isTrue();
    }

    // ---- Lambda tests ----

    @Test
    void testCreateViaLambda() throws InterruptedException {
        AtomicReference<String> threadName = new AtomicReference<>();

        Thread thread = ThreadCreation.createViaLambda("lambda-thread",
                () -> threadName.set(Thread.currentThread().getName()));

        thread.start();
        thread.join();

        assertThat(threadName.get()).isEqualTo("lambda-thread");
    }

    // ---- Callable tests ----

    @Test
    void testSumCallableReturnsResult() throws Exception {
        ThreadCreation.SumCallable callable = new ThreadCreation.SumCallable(1, 10);
        assertThat(callable.call()).isEqualTo(55);
    }

    @Test
    void testCallableWithExecutorService() throws Exception {
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<Integer> future = ThreadCreation.submitCallable(
                    executor, new ThreadCreation.SumCallable(1, 100));

            Integer result = future.get(5, TimeUnit.SECONDS);
            assertThat(result).isEqualTo(5050);
        }
    }

    @Test
    void testCallableSingleElement() throws Exception {
        ThreadCreation.SumCallable callable = new ThreadCreation.SumCallable(7, 7);
        assertThat(callable.call()).isEqualTo(7);
    }

    // ---- Virtual thread tests ----

    @Test
    void testCreateVirtualThread() throws InterruptedException {
        AtomicBoolean executed = new AtomicBoolean(false);
        Thread vThread = ThreadCreation.createVirtualThread("vt-test", () -> executed.set(true));

        assertThat(vThread.isVirtual()).isTrue();
        assertThat(vThread.getState()).isEqualTo(Thread.State.NEW);

        vThread.start();
        vThread.join();

        assertThat(executed).isTrue();
    }

    @Test
    void testVirtualThreadName() throws InterruptedException {
        Thread vThread = ThreadCreation.createVirtualThread("my-virtual", () -> {});
        assertThat(vThread.getName()).isEqualTo("my-virtual");
        vThread.start();
        vThread.join();
    }

    @Test
    void testVirtualThreadIsDaemonByDefault() {
        Thread vThread = ThreadCreation.createVirtualThread("vt-daemon", () -> {});
        assertThat(vThread.isDaemon()).isTrue();
    }

    // ---- Platform thread tests ----

    @Test
    void testCreatePlatformThread() throws InterruptedException {
        AtomicBoolean executed = new AtomicBoolean(false);
        Thread pThread = ThreadCreation.createPlatformThread("pt-test", () -> executed.set(true));

        assertThat(pThread.isVirtual()).isFalse();
        pThread.start();
        pThread.join();

        assertThat(executed).isTrue();
    }

    // ---- Daemon thread tests ----

    @Test
    void testCreateDaemonThread() {
        Thread daemon = ThreadCreation.createDaemonThread("bg-worker", () -> {});
        assertThat(daemon.isDaemon()).isTrue();
        assertThat(daemon.getName()).isEqualTo("bg-worker");
    }

    @Test
    void testNonDaemonThreadByDefault() {
        Thread thread = new Thread(() -> {});
        assertThat(thread.isDaemon()).isFalse();
    }

    @Test
    void testCannotSetDaemonAfterStart() {
        Thread thread = new Thread(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        thread.start();

        assertThatThrownBy(() -> thread.setDaemon(true))
                .isInstanceOf(IllegalThreadStateException.class);

        thread.interrupt();
    }

    // ---- Priority tests ----

    @Test
    void testCreateThreadWithPriority() {
        Thread thread = ThreadCreation.createThreadWithPriority("high-prio", Thread.MAX_PRIORITY, () -> {});
        assertThat(thread.getPriority()).isEqualTo(Thread.MAX_PRIORITY);
    }

    @Test
    void testDefaultPriority() {
        Thread thread = new Thread(() -> {});
        assertThat(thread.getPriority()).isEqualTo(Thread.NORM_PRIORITY);
    }

    @Test
    void testInvalidPriorityThrows() {
        assertThatThrownBy(() -> ThreadCreation.createThreadWithPriority("bad", 0, () -> {}))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> ThreadCreation.createThreadWithPriority("bad", 11, () -> {}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---- Null runnable test ----

    @Test
    void testNullRunnableDoesNotCrash() throws InterruptedException {
        Thread thread = ThreadCreation.createThreadWithNullRunnable();
        thread.start();
        thread.join();
        assertThat(thread.getState()).isEqualTo(Thread.State.TERMINATED);
    }
}
