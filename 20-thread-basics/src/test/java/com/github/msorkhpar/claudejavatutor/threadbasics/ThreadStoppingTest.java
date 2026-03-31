package com.github.msorkhpar.claudejavatutor.threadbasics;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.CancellationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for ThreadStopping demonstrating interrupt(), cooperative cancellation,
 * and related concepts.
 */
class ThreadStoppingTest {

    // ---- InterruptibleLoopTask ----

    @Test
    void testInterruptibleLoopTaskStopsOnInterrupt() throws InterruptedException {
        ThreadStopping.InterruptibleLoopTask task = new ThreadStopping.InterruptibleLoopTask();
        Thread thread = new Thread(task);
        thread.start();
        task.awaitStarted();

        // Let it run briefly
        Thread.sleep(100);
        thread.interrupt();
        thread.join(2000);

        assertThat(thread.isAlive()).isFalse();
        assertThat(task.getIterations()).isGreaterThan(0);
    }

    @Test
    void testInterruptibleLoopTaskIterates() throws InterruptedException {
        ThreadStopping.InterruptibleLoopTask task = new ThreadStopping.InterruptibleLoopTask();
        Thread thread = new Thread(task);
        thread.start();
        task.awaitStarted();

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(task.getIterations()).isGreaterThan(10));

        thread.interrupt();
        thread.join(2000);
    }

    // ---- BlockingInterruptibleTask ----

    @Test
    void testBlockingTaskStopsOnInterrupt() throws InterruptedException {
        ThreadStopping.BlockingInterruptibleTask task = new ThreadStopping.BlockingInterruptibleTask();
        Thread thread = new Thread(task);
        thread.start();
        task.awaitStarted();

        Thread.sleep(200); // let it run a couple cycles
        thread.interrupt();
        thread.join(2000);

        assertThat(thread.isAlive()).isFalse();
        assertThat(task.wasInterrupted()).isTrue();
    }

    @Test
    void testBlockingTaskCompletesMultipleCycles() throws InterruptedException {
        ThreadStopping.BlockingInterruptibleTask task = new ThreadStopping.BlockingInterruptibleTask();
        Thread thread = new Thread(task);
        thread.start();
        task.awaitStarted();

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(task.getCyclesCompleted()).isGreaterThanOrEqualTo(2));

        thread.interrupt();
        thread.join(2000);
        assertThat(task.getCyclesCompleted()).isGreaterThanOrEqualTo(2);
    }

    // ---- CancellableTask (volatile flag) ----

    @Test
    void testCancellableTaskStopsOnCancel() throws InterruptedException {
        ThreadStopping.CancellableTask task = new ThreadStopping.CancellableTask();
        Thread thread = new Thread(task);
        thread.start();
        task.awaitStarted();

        Thread.sleep(100);
        task.cancel();
        thread.join(2000);

        assertThat(thread.isAlive()).isFalse();
        assertThat(task.isCancelled()).isTrue();
        assertThat(task.getIterations()).isGreaterThan(0);
    }

    @Test
    void testCancellableTaskAlsoStopsOnInterrupt() throws InterruptedException {
        ThreadStopping.CancellableTask task = new ThreadStopping.CancellableTask();
        Thread thread = new Thread(task);
        thread.start();
        task.awaitStarted();

        Thread.sleep(100);
        thread.interrupt(); // interrupt also works because of combined check
        thread.join(2000);

        assertThat(thread.isAlive()).isFalse();
    }

    // ---- CleanupOnInterruptTask ----

    @Test
    void testCleanupRunsOnInterrupt() throws InterruptedException {
        ThreadStopping.CleanupOnInterruptTask task = new ThreadStopping.CleanupOnInterruptTask();
        Thread thread = new Thread(task);
        thread.start();
        task.awaitStarted();

        assertThat(task.isRunning()).isTrue();

        thread.interrupt();
        thread.join(2000);

        assertThat(task.isCleanedUp()).isTrue();
        assertThat(task.isRunning()).isFalse();
    }

    // ---- Thread.interrupted() vs isInterrupted() ----

    @Test
    void testInterruptedVsIsInterrupted() throws InterruptedException {
        boolean[] results = ThreadStopping.demonstrateInterruptedVsIsInterrupted();

        // isInterrupted() does NOT clear
        assertThat(results[0]).isTrue();  // first call: true
        assertThat(results[1]).isTrue();  // second call: still true

        // Thread.interrupted() CLEARS the flag
        assertThat(results[2]).isTrue();  // first call: true (and clears)
        assertThat(results[3]).isFalse(); // second call: false (flag was cleared)
    }

    // ---- Interrupting a sleeping thread ----

    @Test
    void testInterruptSleepingThread() throws InterruptedException {
        boolean thrown = ThreadStopping.interruptSleepingThread();
        assertThat(thrown).isTrue();
    }

    // ---- Future.cancel(true) uses interrupt ----

    @Test
    void testFutureCancelInterruptsRunningTask() throws InterruptedException {
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            ThreadStopping.BlockingInterruptibleTask task = new ThreadStopping.BlockingInterruptibleTask();
            Future<?> future = executor.submit(task);
            task.awaitStarted();

            boolean cancelled = future.cancel(true);

            Awaitility.await()
                    .atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> {
                        assertThat(future.isCancelled()).isTrue();
                        assertThat(future.isDone()).isTrue();
                    });

            assertThat(cancelled).isTrue();

            assertThatThrownBy(future::get)
                    .isInstanceOf(CancellationException.class);
        }
    }

    // ---- Edge case: interrupting an already-terminated thread ----

    @Test
    void testInterruptingTerminatedThreadIsIgnored() throws InterruptedException {
        Thread thread = new Thread(() -> {});
        thread.start();
        thread.join();

        // Interrupting a terminated thread should be silently ignored
        thread.interrupt();
        assertThat(thread.getState()).isEqualTo(Thread.State.TERMINATED);
    }

    // ---- Edge case: interrupt flag set before blocking call ----

    @Test
    void testInterruptFlagSetBeforeBlocking() throws InterruptedException {
        boolean[] caughtInterrupt = {false};

        Thread thread = new Thread(() -> {
            Thread.currentThread().interrupt(); // set flag first
            try {
                Thread.sleep(1000); // immediately throws InterruptedException
            } catch (InterruptedException e) {
                caughtInterrupt[0] = true;
                Thread.currentThread().interrupt();
            }
        });

        thread.start();
        thread.join(2000);

        assertThat(caughtInterrupt[0]).isTrue();
    }
}
