package com.github.msorkhpar.claudejavatutor.threadbasics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Demonstrates the thread lifecycle and states in Java.
 * Thread states: NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED.
 */
public class ThreadLifecycle {

    /**
     * Returns the state of a newly created (not started) thread.
     * Expected: Thread.State.NEW
     */
    public static Thread.State getNewThreadState() {
        Thread thread = new Thread(() -> {});
        return thread.getState();
    }

    /**
     * Starts a thread and captures its state while running.
     * Expected: Thread.State.RUNNABLE
     */
    public static Thread.State getRunnableState() throws InterruptedException {
        CountDownLatch running = new CountDownLatch(1);
        CountDownLatch canFinish = new CountDownLatch(1);

        Thread thread = new Thread(() -> {
            running.countDown();
            while (canFinish.getCount() > 0) {
                // busy-wait to stay RUNNABLE
                Thread.yield();
            }
        });

        thread.start();
        running.await();
        Thread.State state = thread.getState();
        canFinish.countDown();
        thread.join();
        return state;
    }

    /**
     * Returns the state of a terminated thread.
     * Expected: Thread.State.TERMINATED
     */
    public static Thread.State getTerminatedState() throws InterruptedException {
        Thread thread = new Thread(() -> {});
        thread.start();
        thread.join();
        return thread.getState();
    }

    /**
     * Demonstrates TIMED_WAITING state (e.g., Thread.sleep()).
     * Expected: Thread.State.TIMED_WAITING
     */
    public static Thread.State getTimedWaitingState() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        Thread thread = new Thread(() -> {
            started.countDown();
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        thread.start();
        started.await();
        // Give thread time to enter sleep
        Thread.sleep(100);
        Thread.State state = thread.getState();
        thread.interrupt();
        thread.join();
        return state;
    }

    /**
     * Demonstrates WAITING state (e.g., Object.wait() or thread.join()).
     * Expected: Thread.State.WAITING
     */
    public static Thread.State getWaitingState() throws InterruptedException {
        Object lock = new Object();
        CountDownLatch started = new CountDownLatch(1);

        Thread thread = new Thread(() -> {
            synchronized (lock) {
                started.countDown();
                try {
                    lock.wait(); // WAITING until notified
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        thread.start();
        started.await();
        Thread.sleep(100); // give thread time to enter wait
        Thread.State state = thread.getState();

        // Clean up: notify so thread can finish
        synchronized (lock) {
            lock.notify();
        }
        thread.join();
        return state;
    }

    /**
     * Demonstrates BLOCKED state (waiting to enter a synchronized block).
     * Expected: Thread.State.BLOCKED
     */
    public static Thread.State getBlockedState() throws InterruptedException {
        Object lock = new Object();
        CountDownLatch holderReady = new CountDownLatch(1);
        CountDownLatch blockerStarted = new CountDownLatch(1);
        CountDownLatch canRelease = new CountDownLatch(1);

        // Thread 1: holds the lock
        Thread holder = new Thread(() -> {
            synchronized (lock) {
                holderReady.countDown();
                try {
                    canRelease.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "lock-holder");

        // Thread 2: tries to acquire the same lock -> BLOCKED
        Thread blocker = new Thread(() -> {
            blockerStarted.countDown();
            synchronized (lock) {
                // finally got the lock
            }
        }, "blocked-thread");

        holder.start();
        holderReady.await(); // holder has the lock

        blocker.start();
        blockerStarted.countDown(); // blocker is started
        Thread.sleep(100); // give blocker time to become BLOCKED

        Thread.State state = blocker.getState();

        // Cleanup
        canRelease.countDown();
        holder.join();
        blocker.join();
        return state;
    }

    /**
     * Captures the full lifecycle of a thread through its state transitions.
     * Returns a list of distinct observed states in order.
     */
    public static List<Thread.State> captureLifecycleStates() throws InterruptedException {
        List<Thread.State> states = Collections.synchronizedList(new ArrayList<>());
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

        // NEW
        states.add(thread.getState());

        thread.start();
        running.await();

        // RUNNABLE or WAITING (depends on timing; after await it's likely WAITING)
        states.add(thread.getState());

        canFinish.countDown();
        thread.join();

        // TERMINATED
        states.add(thread.getState());

        return states;
    }

    /**
     * Demonstrates that a thread transitions from RUNNABLE to TERMINATED
     * when its run() method completes normally.
     */
    public static boolean threadTerminatesAfterRunCompletes() throws InterruptedException {
        Thread thread = new Thread(() -> {
            // Simple task
            int sum = 0;
            for (int i = 0; i < 100; i++) {
                sum += i;
            }
        });

        boolean isNewBefore = thread.getState() == Thread.State.NEW;
        thread.start();
        thread.join();
        boolean isTerminatedAfter = thread.getState() == Thread.State.TERMINATED;

        return isNewBefore && isTerminatedAfter;
    }

    /**
     * Demonstrates that a thread transitions to TERMINATED even when run() throws
     * an unchecked exception.
     */
    public static Thread.State stateAfterException() throws InterruptedException {
        Thread thread = new Thread(() -> {
            throw new RuntimeException("intentional failure");
        });
        thread.setUncaughtExceptionHandler((t, e) -> { /* suppress output */ });
        thread.start();
        thread.join();
        return thread.getState();
    }
}
