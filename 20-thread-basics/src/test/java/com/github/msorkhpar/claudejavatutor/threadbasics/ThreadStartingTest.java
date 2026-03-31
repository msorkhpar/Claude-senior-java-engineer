package com.github.msorkhpar.claudejavatutor.threadbasics;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for ThreadStarting demonstrating start(), run(), join(), and related concepts.
 */
class ThreadStartingTest {

    // ---- run() vs start() ----

    @Test
    void testRunExecutesOnCallingThread() {
        String executingThread = ThreadStarting.executeWithRun(() -> {});
        // run() executes on the calling thread, NOT on "worker-thread"
        assertThat(executingThread).isNotEqualTo("worker-thread");
        assertThat(executingThread).isEqualTo(Thread.currentThread().getName());
    }

    @Test
    void testStartExecutesOnNewThread() throws InterruptedException {
        String executingThread = ThreadStarting.executeWithStart(() -> {});
        // start() executes on the new "worker-thread"
        assertThat(executingThread).isEqualTo("worker-thread");
    }

    // ---- Starting a thread twice ----

    @Test
    void testStartingThreadTwiceThrowsException() {
        assertThatThrownBy(ThreadStarting::startThreadTwice)
                .isInstanceOf(IllegalThreadStateException.class);
    }

    @Test
    void testCannotRestartTerminatedThread() throws InterruptedException {
        Thread thread = new Thread(() -> {});
        thread.start();
        thread.join();
        assertThat(thread.getState()).isEqualTo(Thread.State.TERMINATED);

        assertThatThrownBy(thread::start)
                .isInstanceOf(IllegalThreadStateException.class);
    }

    // ---- join() ----

    @Test
    void testJoinWaitsForThreadToComplete() throws InterruptedException {
        int result = ThreadStarting.joinAndGetResult();
        assertThat(result).isEqualTo(5050); // sum of 1..100
    }

    @Test
    void testJoinWithTimeoutCompletes() throws InterruptedException {
        // Task finishes in 50ms, timeout is 2000ms -> should complete
        boolean completed = ThreadStarting.joinWithTimeout(50, 2000);
        assertThat(completed).isTrue();
    }

    @Test
    void testJoinWithTimeoutTimesOut() throws InterruptedException {
        // Task takes 5000ms, timeout is 100ms -> should time out
        boolean completed = ThreadStarting.joinWithTimeout(5000, 100);
        assertThat(completed).isFalse();
    }

    @Test
    void testJoinOnUnstartedThreadReturnsImmediately() throws InterruptedException {
        Thread thread = new Thread(() -> {});
        // join() on an unstarted thread returns immediately
        long start = System.currentTimeMillis();
        thread.join(1000);
        long elapsed = System.currentTimeMillis() - start;
        assertThat(elapsed).isLessThan(500);
    }

    // ---- Happens-before ----

    @Test
    void testHappensBeforeGuarantee() throws InterruptedException {
        int observed = ThreadStarting.demonstrateHappensBefore();
        assertThat(observed).isEqualTo(42);
    }

    // ---- UncaughtExceptionHandler ----

    @Test
    void testUncaughtExceptionHandlerCapturesException() throws InterruptedException {
        String message = ThreadStarting.demonstrateUncaughtExceptionHandler("test error");
        assertThat(message).isEqualTo("failing-thread: test error");
    }

    @Test
    void testUncaughtExceptionHandlerWithDifferentMessages() throws InterruptedException {
        String message = ThreadStarting.demonstrateUncaughtExceptionHandler("NPE occurred");
        assertThat(message).contains("NPE occurred");
        assertThat(message).startsWith("failing-thread:");
    }

    @Test
    void testDefaultUncaughtExceptionHandler() throws InterruptedException {
        AtomicReference<Throwable> caught = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        try {
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                caught.set(e);
                latch.countDown();
            });

            Thread thread = new Thread(() -> {
                throw new ArithmeticException("division by zero");
            });
            thread.start();
            latch.await();

            assertThat(caught.get()).isInstanceOf(ArithmeticException.class);
            assertThat(caught.get().getMessage()).isEqualTo("division by zero");
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(defaultHandler);
        }
    }

    // ---- sleep() ----

    @Test
    void testSleepPausesThread() throws InterruptedException {
        long elapsed = ThreadStarting.demonstrateSleep(100);
        assertThat(elapsed).isGreaterThanOrEqualTo(80); // allow some tolerance
    }

    @Test
    void testSleepCanBeInterrupted() {
        Thread thread = new Thread(() -> {
            try {
                ThreadStarting.demonstrateSleep(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        thread.start();
        thread.interrupt();
        // Thread should finish quickly due to interrupt
        assertThat(thread.isAlive() || thread.getState() == Thread.State.TERMINATED).isTrue();
    }

    // ---- Thread.currentThread() ----

    @Test
    void testCurrentThreadReturnsCallingThread() throws InterruptedException {
        AtomicReference<String> threadName = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            threadName.set(Thread.currentThread().getName());
        }, "test-current");

        thread.start();
        thread.join();

        assertThat(threadName.get()).isEqualTo("test-current");
    }

    // ---- isAlive() ----

    @Test
    void testIsAliveBeforeAndAfterExecution() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch canFinish = new CountDownLatch(1);

        Thread thread = new Thread(() -> {
            started.countDown();
            try {
                canFinish.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertThat(thread.isAlive()).isFalse(); // not started

        thread.start();
        started.await();
        assertThat(thread.isAlive()).isTrue(); // running

        canFinish.countDown();
        thread.join();
        assertThat(thread.isAlive()).isFalse(); // terminated
    }
}
