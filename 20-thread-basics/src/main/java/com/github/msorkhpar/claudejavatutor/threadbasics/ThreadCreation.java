package com.github.msorkhpar.claudejavatutor.threadbasics;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Demonstrates various ways to create threads in Java.
 * Covers: Thread subclassing, Runnable, Callable, lambdas, virtual threads, daemon threads.
 */
public class ThreadCreation {

    // ---- Method 1: Extending Thread ----

    /**
     * A thread created by extending the Thread class.
     * Not recommended in practice because it consumes the single-inheritance slot.
     */
    public static class CountingThread extends Thread {
        private volatile int count = 0;

        public CountingThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                count++;
            }
        }

        public int getCount() {
            return count;
        }
    }

    // ---- Method 2: Implementing Runnable ----

    /**
     * A task created by implementing the Runnable interface.
     * Preferred over Thread subclassing because it separates task from execution.
     */
    public static class CountingTask implements Runnable {
        private volatile int count = 0;

        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                count++;
            }
        }

        public int getCount() {
            return count;
        }
    }

    // ---- Method 3: Callable (returns a value) ----

    /**
     * A task using Callable that returns a result and can throw checked exceptions.
     */
    public static class SumCallable implements Callable<Integer> {
        private final int from;
        private final int to;

        public SumCallable(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public Integer call() throws Exception {
            int sum = 0;
            for (int i = from; i <= to; i++) {
                sum += i;
            }
            return sum;
        }
    }

    /**
     * Creates and starts a thread by extending Thread.
     */
    public static CountingThread createViaThreadSubclass(String name) {
        CountingThread thread = new CountingThread(name);
        return thread;
    }

    /**
     * Creates a thread using the Runnable interface.
     */
    public static Thread createViaRunnable(String name, Runnable task) {
        return new Thread(task, name);
    }

    /**
     * Creates a thread using a lambda expression (since Runnable is a functional interface).
     */
    public static Thread createViaLambda(String name, Runnable task) {
        return new Thread(task, name);
    }

    /**
     * Submits a Callable to an ExecutorService and returns the Future result.
     */
    public static <T> Future<T> submitCallable(ExecutorService executor, Callable<T> callable) {
        return executor.submit(callable);
    }

    /**
     * Creates a virtual thread (Java 21) with the given name and task.
     * Virtual threads are lightweight and managed by the JVM, not the OS.
     */
    public static Thread createVirtualThread(String name, Runnable task) {
        return Thread.ofVirtual().name(name).unstarted(task);
    }

    /**
     * Creates a platform thread using the Thread.Builder API (Java 21).
     */
    public static Thread createPlatformThread(String name, Runnable task) {
        return Thread.ofPlatform().name(name).unstarted(task);
    }

    /**
     * Creates a daemon thread. Daemon threads do not prevent JVM shutdown.
     */
    public static Thread createDaemonThread(String name, Runnable task) {
        Thread thread = new Thread(task, name);
        thread.setDaemon(true);
        return thread;
    }

    /**
     * Creates a thread with a specific priority.
     * Priority is a hint to the OS scheduler, not a guarantee.
     */
    public static Thread createThreadWithPriority(String name, int priority, Runnable task) {
        Thread thread = new Thread(task, name);
        thread.setPriority(priority);
        return thread;
    }

    /**
     * Demonstrates that passing null Runnable to Thread is allowed.
     * The thread's run() method simply does nothing.
     */
    public static Thread createThreadWithNullRunnable() {
        return new Thread((Runnable) null, "null-runnable-thread");
    }
}
