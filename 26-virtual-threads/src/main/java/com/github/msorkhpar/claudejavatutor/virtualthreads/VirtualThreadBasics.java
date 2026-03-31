package com.github.msorkhpar.claudejavatutor.virtualthreads;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Demonstrates the basics of virtual threads introduced in Java 21 (Project Loom).
 * Covers creation, management, and fundamental properties of virtual threads.
 */
public class VirtualThreadBasics {

    /**
     * Creates and starts a virtual thread using Thread.startVirtualThread().
     * This is the simplest way to create a virtual thread.
     */
    public static Thread startSimpleVirtualThread(Runnable task) {
        return Thread.startVirtualThread(task);
    }

    /**
     * Creates a virtual thread using Thread.ofVirtual().start().
     * Provides more control over thread configuration.
     */
    public static Thread createWithBuilder(Runnable task) {
        return Thread.ofVirtual()
                .name("custom-virtual-thread")
                .start(task);
    }

    /**
     * Creates an unstarted virtual thread that can be started later.
     */
    public static Thread createUnstartedVirtualThread(Runnable task) {
        return Thread.ofVirtual()
                .name("unstarted-virtual")
                .unstarted(task);
    }

    /**
     * Creates a virtual thread with a named prefix and index.
     */
    public static Thread createNamedVirtualThread(String prefix, long index, Runnable task) {
        return Thread.ofVirtual()
                .name(prefix, index)
                .start(task);
    }

    /**
     * Creates a platform thread for comparison purposes.
     */
    public static Thread createPlatformThread(Runnable task) {
        return Thread.ofPlatform()
                .name("platform-thread")
                .start(task);
    }

    /**
     * Checks if a thread is virtual.
     */
    public static boolean isVirtualThread(Thread thread) {
        return thread.isVirtual();
    }

    /**
     * Creates a virtual thread per-task executor.
     * Each submitted task runs in a new virtual thread.
     */
    public static ExecutorService createVirtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Demonstrates creating many virtual threads concurrently.
     * Virtual threads are cheap to create - thousands or millions are feasible.
     *
     * @param numberOfThreads the number of virtual threads to create
     * @return list of results from each thread
     */
    public static List<String> createManyVirtualThreads(int numberOfThreads) throws InterruptedException {
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            Thread vt = Thread.startVirtualThread(() -> {
                results.add("Result-" + index);
            });
            threads.add(vt);
        }

        for (Thread t : threads) {
            t.join();
        }
        return results;
    }

    /**
     * Demonstrates using a virtual thread executor to run tasks that simulate I/O.
     *
     * @param taskCount    number of tasks to execute
     * @param sleepMillis  simulated I/O delay per task
     * @return the total time taken in milliseconds
     */
    public static long runIOBoundTasks(int taskCount, long sleepMillis) throws InterruptedException {
        Instant start = Instant.now();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < taskCount; i++) {
                executor.submit(() -> {
                    try {
                        Thread.sleep(Duration.ofMillis(sleepMillis));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }
        return Duration.between(start, Instant.now()).toMillis();
    }

    /**
     * Demonstrates that virtual threads are daemon threads by default.
     */
    public static boolean isVirtualThreadDaemon() {
        Thread vt = Thread.ofVirtual().unstarted(() -> {});
        return vt.isDaemon();
    }

    /**
     * Demonstrates that virtual threads have a fixed NORM_PRIORITY.
     */
    public static int getVirtualThreadPriority() {
        Thread vt = Thread.ofVirtual().unstarted(() -> {});
        return vt.getPriority();
    }

    /**
     * Demonstrates virtual thread factory for creating threads with consistent naming.
     */
    public static ThreadFactory createVirtualThreadFactory(String prefix) {
        return Thread.ofVirtual().name(prefix, 0).factory();
    }

    /**
     * Demonstrates using a virtual thread factory with an executor service.
     */
    public static List<String> executeWithFactory(int taskCount) throws InterruptedException, ExecutionException {
        ThreadFactory factory = Thread.ofVirtual().name("worker-", 0).factory();
        List<String> threadNames = Collections.synchronizedList(new ArrayList<>());

        try (var executor = Executors.newThreadPerTaskExecutor(factory)) {
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                futures.add(executor.submit(() -> {
                    String name = Thread.currentThread().getName();
                    threadNames.add(name);
                    return name;
                }));
            }
            for (Future<String> f : futures) {
                f.get();
            }
        }
        return threadNames;
    }

    /**
     * Demonstrates cooperative cancellation with virtual threads via interruption.
     */
    public static String interruptVirtualThread() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        Thread vt = Thread.startVirtualThread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                counter.incrementAndGet();
                try {
                    Thread.sleep(Duration.ofMillis(10));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        Thread.sleep(50);
        vt.interrupt();
        vt.join(1000);
        return "Counter reached: " + counter.get() + ", interrupted: " + !vt.isAlive();
    }

    /**
     * Demonstrates that virtual threads support thread-local variables.
     */
    public static String useThreadLocal() throws InterruptedException {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        StringBuilder result = new StringBuilder();

        Thread vt = Thread.startVirtualThread(() -> {
            threadLocal.set("virtual-thread-value");
            result.append(threadLocal.get());
        });
        vt.join();
        return result.toString();
    }
}
