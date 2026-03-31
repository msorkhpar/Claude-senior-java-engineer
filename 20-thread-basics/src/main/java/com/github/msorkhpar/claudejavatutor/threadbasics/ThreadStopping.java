package com.github.msorkhpar.claudejavatutor.threadbasics;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates cooperative thread stopping using interrupt() and volatile flags.
 * Java does not support forcible thread stopping -- threads must cooperate.
 */
public class ThreadStopping {

    /**
     * A task that checks the interrupt flag in a loop.
     * This is the recommended pattern for CPU-bound interruptible tasks.
     */
    public static class InterruptibleLoopTask implements Runnable {
        private final AtomicInteger iterations = new AtomicInteger(0);
        private final CountDownLatch startedLatch = new CountDownLatch(1);

        @Override
        public void run() {
            startedLatch.countDown();
            while (!Thread.currentThread().isInterrupted()) {
                iterations.incrementAndGet();
                // Simulate work
                doComputation();
            }
        }

        private void doComputation() {
            // lightweight computation to avoid tight spin
            Math.random();
        }

        public int getIterations() {
            return iterations.get();
        }

        public void awaitStarted() throws InterruptedException {
            startedLatch.await();
        }
    }

    /**
     * A task that handles InterruptedException properly when using blocking operations.
     * Demonstrates the correct pattern: restore the interrupt flag and exit.
     */
    public static class BlockingInterruptibleTask implements Runnable {
        private volatile boolean wasInterrupted = false;
        private final AtomicInteger cyclesCompleted = new AtomicInteger(0);
        private final CountDownLatch startedLatch = new CountDownLatch(1);

        @Override
        public void run() {
            startedLatch.countDown();
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(50); // blocking operation
                    cyclesCompleted.incrementAndGet();
                }
            } catch (InterruptedException e) {
                wasInterrupted = true;
                Thread.currentThread().interrupt(); // restore the flag
            }
        }

        public boolean wasInterrupted() {
            return wasInterrupted;
        }

        public int getCyclesCompleted() {
            return cyclesCompleted.get();
        }

        public void awaitStarted() throws InterruptedException {
            startedLatch.await();
        }
    }

    /**
     * A task that uses a volatile boolean flag for cancellation.
     * Useful for CPU-bound work where explicit control is desired.
     */
    public static class CancellableTask implements Runnable {
        private volatile boolean cancelled = false;
        private final AtomicInteger iterations = new AtomicInteger(0);
        private final CountDownLatch startedLatch = new CountDownLatch(1);

        @Override
        public void run() {
            startedLatch.countDown();
            while (!cancelled && !Thread.currentThread().isInterrupted()) {
                iterations.incrementAndGet();
                doWork();
            }
        }

        private void doWork() {
            Math.random();
        }

        public void cancel() {
            cancelled = true;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public int getIterations() {
            return iterations.get();
        }

        public void awaitStarted() throws InterruptedException {
            startedLatch.await();
        }
    }

    /**
     * A task that performs cleanup in a finally block when interrupted.
     */
    public static class CleanupOnInterruptTask implements Runnable {
        private volatile boolean cleanedUp = false;
        private volatile boolean running = false;
        private final CountDownLatch startedLatch = new CountDownLatch(1);

        @Override
        public void run() {
            running = true;
            startedLatch.countDown();
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                cleanedUp = true;
                running = false;
            }
        }

        public boolean isCleanedUp() {
            return cleanedUp;
        }

        public boolean isRunning() {
            return running;
        }

        public void awaitStarted() throws InterruptedException {
            startedLatch.await();
        }
    }

    /**
     * Demonstrates the difference between Thread.interrupted() (static, clears flag)
     * and Thread.currentThread().isInterrupted() (instance, does not clear).
     */
    public static boolean[] demonstrateInterruptedVsIsInterrupted() throws InterruptedException {
        boolean[] results = new boolean[4];
        CountDownLatch latch = new CountDownLatch(1);

        Thread thread = new Thread(() -> {
            Thread.currentThread().interrupt(); // set the flag

            // isInterrupted() does NOT clear the flag
            results[0] = Thread.currentThread().isInterrupted(); // true
            results[1] = Thread.currentThread().isInterrupted(); // still true

            // Thread.interrupted() CLEARS the flag
            results[2] = Thread.interrupted(); // true (and clears)
            results[3] = Thread.interrupted(); // false (flag was cleared)

            latch.countDown();
        });
        thread.start();
        latch.await();
        thread.join();
        return results;
    }

    /**
     * Demonstrates that interrupting a thread blocked on sleep() throws InterruptedException
     * and clears the interrupt flag.
     */
    public static boolean interruptSleepingThread() throws InterruptedException {
        boolean[] interruptedExceptionThrown = {false};
        CountDownLatch started = new CountDownLatch(1);

        Thread thread = new Thread(() -> {
            started.countDown();
            try {
                Thread.sleep(10_000); // long sleep
            } catch (InterruptedException e) {
                interruptedExceptionThrown[0] = true;
                Thread.currentThread().interrupt(); // restore
            }
        });

        thread.start();
        started.await();
        Thread.sleep(50); // give thread time to enter sleep
        thread.interrupt();
        thread.join();
        return interruptedExceptionThrown[0];
    }
}
