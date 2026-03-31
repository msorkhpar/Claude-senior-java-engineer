package com.github.msorkhpar.claudejavatutor.threadbasics;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Demonstrates starting threads with start(), the difference between start() and run(),
 * join(), UncaughtExceptionHandler, and happens-before guarantees.
 */
public class ThreadStarting {

    /**
     * Demonstrates the difference between calling run() and start().
     * run() executes on the calling thread; start() creates a new thread.
     *
     * @return the name of the thread that executed the task
     */
    public static String executeWithRun(Runnable task) {
        AtomicReference<String> executingThread = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            executingThread.set(Thread.currentThread().getName());
            task.run();
        }, "worker-thread");
        // Calling run() directly -- executes on CURRENT thread, not "worker-thread"
        thread.run();
        return executingThread.get();
    }

    /**
     * Starts a thread with start() and returns the executing thread's name via a latch.
     */
    public static String executeWithStart(Runnable task) throws InterruptedException {
        AtomicReference<String> executingThread = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(() -> {
            executingThread.set(Thread.currentThread().getName());
            task.run();
            latch.countDown();
        }, "worker-thread");
        thread.start();
        latch.await();
        return executingThread.get();
    }

    /**
     * Demonstrates that starting a thread twice throws IllegalThreadStateException.
     */
    public static void startThreadTwice() throws InterruptedException {
        Thread thread = new Thread(() -> {});
        thread.start();
        thread.join();
        thread.start(); // throws IllegalThreadStateException
    }

    /**
     * Demonstrates join() -- blocks caller until the target thread completes.
     * Returns the result computed by the thread.
     */
    public static int joinAndGetResult() throws InterruptedException {
        int[] result = new int[1];
        Thread thread = new Thread(() -> {
            int sum = 0;
            for (int i = 1; i <= 100; i++) {
                sum += i;
            }
            result[0] = sum;
        });
        thread.start();
        thread.join(); // blocks until thread finishes
        return result[0];
    }

    /**
     * Demonstrates join(millis) with a timeout.
     * Returns true if the thread completed within the timeout.
     */
    public static boolean joinWithTimeout(long taskDurationMs, long timeoutMs) throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(taskDurationMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        thread.start();
        thread.join(timeoutMs);
        boolean completed = !thread.isAlive();
        if (thread.isAlive()) {
            thread.interrupt();
            thread.join(); // wait for cleanup
        }
        return completed;
    }

    /**
     * Demonstrates the happens-before relationship established by start().
     * Writes made before start() are visible to the child thread.
     */
    public static int demonstrateHappensBefore() throws InterruptedException {
        int[] sharedValue = new int[1];
        int[] observedValue = new int[1];

        sharedValue[0] = 42; // write before start()

        Thread thread = new Thread(() -> {
            observedValue[0] = sharedValue[0]; // guaranteed to see 42
        });
        thread.start();
        thread.join();
        return observedValue[0];
    }

    /**
     * Sets an UncaughtExceptionHandler and returns the captured exception message.
     */
    public static String demonstrateUncaughtExceptionHandler(String exceptionMessage)
            throws InterruptedException {
        AtomicReference<String> capturedMessage = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Thread thread = new Thread(() -> {
            throw new RuntimeException(exceptionMessage);
        }, "failing-thread");

        thread.setUncaughtExceptionHandler((t, e) -> {
            capturedMessage.set(t.getName() + ": " + e.getMessage());
            latch.countDown();
        });

        thread.start();
        latch.await();
        return capturedMessage.get();
    }

    /**
     * Demonstrates Thread.sleep() pausing the current thread.
     * Returns the approximate elapsed time in milliseconds.
     */
    public static long demonstrateSleep(long sleepMs) throws InterruptedException {
        long start = System.currentTimeMillis();
        Thread.sleep(sleepMs);
        return System.currentTimeMillis() - start;
    }
}
