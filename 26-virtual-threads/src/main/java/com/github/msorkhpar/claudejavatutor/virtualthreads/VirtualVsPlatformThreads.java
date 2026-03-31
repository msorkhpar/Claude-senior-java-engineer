package com.github.msorkhpar.claudejavatutor.virtualthreads;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates the differences between virtual threads and platform threads.
 * Covers scalability, resource consumption, blocking behavior, synchronization,
 * and compatibility with existing code and libraries.
 */
public class VirtualVsPlatformThreads {

    /**
     * Demonstrates scalability: creates many virtual threads to show they are lightweight.
     * Platform threads are limited by OS resources; virtual threads are not.
     *
     * @param count number of threads to create
     * @return the number of threads that completed successfully
     */
    public static int scaleVirtualThreads(int count) throws InterruptedException {
        AtomicInteger completed = new AtomicInteger(0);
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Thread vt = Thread.startVirtualThread(() -> {
                try {
                    Thread.sleep(Duration.ofMillis(1));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                completed.incrementAndGet();
            });
            threads.add(vt);
        }

        for (Thread t : threads) {
            t.join();
        }
        return completed.get();
    }

    /**
     * Measures execution time of I/O-bound tasks using virtual threads.
     *
     * @param taskCount   number of concurrent tasks
     * @param sleepMillis simulated I/O delay per task
     * @return elapsed time in milliseconds
     */
    public static long measureVirtualThreadIOPerformance(int taskCount, long sleepMillis)
            throws InterruptedException {
        Instant start = Instant.now();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                futures.add(executor.submit(() -> {
                    try {
                        Thread.sleep(Duration.ofMillis(sleepMillis));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }));
            }
            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return Duration.between(start, Instant.now()).toMillis();
    }

    /**
     * Measures execution time of I/O-bound tasks using a fixed platform thread pool.
     *
     * @param taskCount   number of tasks
     * @param poolSize    size of the fixed thread pool
     * @param sleepMillis simulated I/O delay per task
     * @return elapsed time in milliseconds
     */
    public static long measurePlatformThreadIOPerformance(int taskCount, int poolSize, long sleepMillis)
            throws InterruptedException {
        Instant start = Instant.now();
        try (var executor = Executors.newFixedThreadPool(poolSize)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                futures.add(executor.submit(() -> {
                    try {
                        Thread.sleep(Duration.ofMillis(sleepMillis));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }));
            }
            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return Duration.between(start, Instant.now()).toMillis();
    }

    /**
     * Demonstrates that virtual threads release their carrier thread when blocked.
     * When a virtual thread calls Thread.sleep() or blocking I/O, the underlying
     * carrier (platform) thread is freed to run other virtual threads.
     *
     * @return the carrier thread names observed during blocking operations
     */
    public static List<String> demonstrateCarrierThreadSharing(int taskCount) throws InterruptedException {
        List<String> carrierNames = Collections.synchronizedList(new ArrayList<>());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                futures.add(executor.submit(() -> {
                    // Record the carrier thread before blocking
                    String beforeSleep = getCarrierThreadName();
                    try {
                        Thread.sleep(Duration.ofMillis(10));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    // After waking up, may be on a different carrier
                    String afterSleep = getCarrierThreadName();
                    carrierNames.add(beforeSleep);
                    carrierNames.add(afterSleep);
                }));
            }
            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return carrierNames;
    }

    /**
     * Returns the carrier thread name for a virtual thread.
     * For platform threads, returns the thread name itself.
     */
    private static String getCarrierThreadName() {
        Thread current = Thread.currentThread();
        if (current.isVirtual()) {
            // The toString() of a virtual thread includes carrier info
            return current.toString();
        }
        return current.getName();
    }

    /**
     * Demonstrates that synchronized blocks can cause carrier thread pinning.
     * When a virtual thread enters a synchronized block, it becomes pinned to its
     * carrier thread and cannot unmount during blocking operations within the block.
     */
    public static class PinningDemonstration {
        private final Object monitor = new Object();
        private final ReentrantLock lock = new ReentrantLock();
        private final AtomicInteger counter = new AtomicInteger(0);

        /**
         * Uses synchronized block - causes pinning when blocking inside.
         */
        public void incrementWithSynchronized() {
            synchronized (monitor) {
                try {
                    Thread.sleep(Duration.ofMillis(1));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                counter.incrementAndGet();
            }
        }

        /**
         * Uses ReentrantLock - does NOT cause pinning, preferred for virtual threads.
         */
        public void incrementWithLock() {
            lock.lock();
            try {
                try {
                    Thread.sleep(Duration.ofMillis(1));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                counter.incrementAndGet();
            } finally {
                lock.unlock();
            }
        }

        public int getCount() {
            return counter.get();
        }

        public void resetCount() {
            counter.set(0);
        }
    }

    /**
     * Demonstrates compatibility: virtual threads work with existing Thread API.
     */
    public static class CompatibilityExamples {

        /**
         * Virtual threads work with ExecutorService and Callable/Future.
         */
        public static <T> T submitCallable(Callable<T> callable) throws ExecutionException, InterruptedException {
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                Future<T> future = executor.submit(callable);
                return future.get();
            }
        }

        /**
         * Virtual threads work with CountDownLatch for synchronization.
         */
        public static int coordinateWithLatch(int threadCount) throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger sum = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                final int value = i;
                Thread.startVirtualThread(() -> {
                    sum.addAndGet(value);
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            return sum.get();
        }

        /**
         * Virtual threads work with CompletableFuture.
         */
        public static CompletableFuture<String> runWithCompletableFuture(String input) {
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(Duration.ofMillis(10));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return input.toUpperCase();
            }, executor);
        }

        /**
         * Virtual threads work with BlockingQueue for producer-consumer patterns.
         */
        public static List<String> producerConsumerWithVirtualThreads(int itemCount)
                throws InterruptedException {
            BlockingQueue<String> queue = new LinkedBlockingQueue<>();
            List<String> consumed = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch producerDone = new CountDownLatch(1);
            CountDownLatch consumerDone = new CountDownLatch(1);

            // Producer virtual thread
            Thread.startVirtualThread(() -> {
                for (int i = 0; i < itemCount; i++) {
                    try {
                        queue.put("item-" + i);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                producerDone.countDown();
            });

            // Consumer virtual thread
            Thread.startVirtualThread(() -> {
                try {
                    producerDone.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                while (!queue.isEmpty()) {
                    try {
                        String item = queue.poll(100, TimeUnit.MILLISECONDS);
                        if (item != null) {
                            consumed.add(item);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                consumerDone.countDown();
            });

            consumerDone.await(5, TimeUnit.SECONDS);
            return consumed;
        }

        /**
         * Virtual threads do NOT support ThreadGroup meaningfully.
         * Virtual threads are always in the "VirtualThreads" thread group.
         */
        public static String getVirtualThreadGroup() {
            Thread vt = Thread.ofVirtual().unstarted(() -> {});
            ThreadGroup group = vt.getThreadGroup();
            return group != null ? group.getName() : "null";
        }

        /**
         * Virtual threads are always daemon threads.
         * Calling setDaemon(false) throws IllegalArgumentException.
         */
        public static boolean virtualThreadAlwaysDaemon() {
            Thread vt = Thread.ofVirtual().unstarted(() -> {});
            return vt.isDaemon(); // Always returns true
        }

        /**
         * Demonstrates that setDaemon(false) throws on virtual threads.
         */
        public static void setDaemonFalseOnVirtualThread() {
            Thread vt = Thread.ofVirtual().unstarted(() -> {});
            vt.setDaemon(false); // Throws IllegalArgumentException
        }

        /**
         * Virtual threads have fixed priority (Thread.NORM_PRIORITY).
         * Calling setPriority with a different value throws IllegalArgumentException.
         */
        public static int virtualThreadFixedPriority() {
            Thread vt = Thread.ofVirtual().unstarted(() -> {});
            return vt.getPriority(); // Always returns NORM_PRIORITY
        }

        /**
         * Demonstrates that setPriority is ignored on virtual threads.
         * The priority remains NORM_PRIORITY regardless of what is set.
         */
        public static int setPriorityOnVirtualThread(int priority) {
            Thread vt = Thread.ofVirtual().unstarted(() -> {});
            vt.setPriority(priority); // Silently ignored
            return vt.getPriority(); // Still NORM_PRIORITY
        }
    }
}
