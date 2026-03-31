package com.github.msorkhpar.claudejavatutor.threadbasics;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ThreadLifecycle demonstrating thread states:
 * NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED.
 */
class ThreadLifecycleTest {

    @Test
    void testNewState() {
        Thread.State state = ThreadLifecycle.getNewThreadState();
        assertThat(state).isEqualTo(Thread.State.NEW);
    }

    @Test
    void testRunnableState() throws InterruptedException {
        Thread.State state = ThreadLifecycle.getRunnableState();
        assertThat(state).isEqualTo(Thread.State.RUNNABLE);
    }

    @Test
    void testTerminatedState() throws InterruptedException {
        Thread.State state = ThreadLifecycle.getTerminatedState();
        assertThat(state).isEqualTo(Thread.State.TERMINATED);
    }

    @Test
    void testTimedWaitingState() throws InterruptedException {
        Thread.State state = ThreadLifecycle.getTimedWaitingState();
        assertThat(state).isEqualTo(Thread.State.TIMED_WAITING);
    }

    @Test
    void testWaitingState() throws InterruptedException {
        Thread.State state = ThreadLifecycle.getWaitingState();
        assertThat(state).isEqualTo(Thread.State.WAITING);
    }

    @Test
    void testBlockedState() throws InterruptedException {
        Thread.State state = ThreadLifecycle.getBlockedState();
        assertThat(state).isEqualTo(Thread.State.BLOCKED);
    }

    @Test
    void testLifecycleStatesContainNewAndTerminated() throws InterruptedException {
        List<Thread.State> states = ThreadLifecycle.captureLifecycleStates();

        assertThat(states).hasSize(3);
        assertThat(states.getFirst()).isEqualTo(Thread.State.NEW);
        assertThat(states.getLast()).isEqualTo(Thread.State.TERMINATED);
    }

    @Test
    void testThreadTerminatesAfterRunCompletes() throws InterruptedException {
        boolean result = ThreadLifecycle.threadTerminatesAfterRunCompletes();
        assertThat(result).isTrue();
    }

    @Test
    void testStateAfterExceptionIsTerminated() throws InterruptedException {
        Thread.State state = ThreadLifecycle.stateAfterException();
        assertThat(state).isEqualTo(Thread.State.TERMINATED);
    }

    // ---- Additional state transition tests ----

    @Test
    void testThreadStateTransitionsFromNewToRunnable() throws InterruptedException {
        CountDownLatch running = new CountDownLatch(1);
        CountDownLatch canFinish = new CountDownLatch(1);

        Thread thread = new Thread(() -> {
            running.countDown();
            try {
                canFinish.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertThat(thread.getState()).isEqualTo(Thread.State.NEW);

        thread.start();
        running.await();

        // After start, thread is either RUNNABLE or WAITING (due to canFinish.await())
        Thread.State state = thread.getState();
        assertThat(state).isIn(Thread.State.RUNNABLE, Thread.State.WAITING);

        canFinish.countDown();
        thread.join();

        assertThat(thread.getState()).isEqualTo(Thread.State.TERMINATED);
    }

    @Test
    void testTimedWaitingViaJoinWithTimeout() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);

        Thread longRunning = new Thread(() -> {
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        longRunning.start();

        Thread joiner = new Thread(() -> {
            started.countDown();
            try {
                longRunning.join(5_000); // TIMED_WAITING
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        joiner.start();
        started.await();
        Thread.sleep(100);

        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() ->
                        assertThat(joiner.getState()).isEqualTo(Thread.State.TIMED_WAITING));

        longRunning.interrupt();
        joiner.interrupt();
        longRunning.join(2000);
        joiner.join(2000);
    }

    @Test
    void testAllThreadStatesAreValid() {
        // Ensure all six states exist
        Thread.State[] states = Thread.State.values();
        assertThat(states).containsExactlyInAnyOrder(
                Thread.State.NEW,
                Thread.State.RUNNABLE,
                Thread.State.BLOCKED,
                Thread.State.WAITING,
                Thread.State.TIMED_WAITING,
                Thread.State.TERMINATED
        );
    }
}
