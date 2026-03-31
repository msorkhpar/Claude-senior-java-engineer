package com.github.msorkhpar.claudejavatutor.modernjava;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Demonstrates key features introduced in Java 21 (LTS).
 * Covers: Pattern Matching for switch, Record Patterns, Virtual Threads,
 * Structured Concurrency (Preview), Foreign Function & Memory API (Preview).
 */
public class Java21Features {

    // ========== Pattern Matching for switch ==========

    /**
     * Demonstrates pattern matching in switch expressions.
     */
    public static String formatValue(Object obj) {
        return switch (obj) {
            case Integer i when i < 0 -> "Negative integer: " + i;
            case Integer i -> "Positive integer: " + i;
            case String s when s.isEmpty() -> "Empty string";
            case String s -> "String: " + s;
            case Double d -> "Double: " + String.format("%.2f", d);
            case List<?> list when list.isEmpty() -> "Empty list";
            case List<?> list -> "List with " + list.size() + " elements";
            case null -> "null";
            default -> "Unknown: " + obj.getClass().getSimpleName();
        };
    }

    /**
     * Demonstrates guarded patterns with when clause.
     */
    public static String classifyNumber(Number number) {
        return switch (number) {
            case Integer i when i == 0 -> "zero";
            case Integer i when i > 0 -> "positive integer";
            case Integer i -> "negative integer";
            case Double d when d.isNaN() -> "not a number";
            case Double d when d.isInfinite() -> "infinite";
            case Double d when d == 0.0 -> "zero";
            case Double d when d > 0 -> "positive double";
            case Double d -> "negative double";
            case Long l when l == 0L -> "zero";
            case Long l when l > 0 -> "positive long";
            case Long l -> "negative long";
            default -> "other number type";
        };
    }

    /**
     * Demonstrates exhaustive switch with sealed classes.
     */
    public sealed interface Notification permits EmailNotification, SmsNotification, PushNotification {
    }

    public record EmailNotification(String to, String subject, String body) implements Notification {
    }

    public record SmsNotification(String phoneNumber, String message) implements Notification {
    }

    public record PushNotification(String deviceId, String title, String payload) implements Notification {
    }

    public static String processNotification(Notification notification) {
        return switch (notification) {
            case EmailNotification e -> "Email to " + e.to() + ": " + e.subject();
            case SmsNotification s -> "SMS to " + s.phoneNumber() + ": " + s.message();
            case PushNotification p -> "Push to " + p.deviceId() + ": " + p.title();
        };
    }

    // ========== Record Patterns ==========

    public record Point(int x, int y) {
    }

    public record Line(Point start, Point end) {
    }

    public record ColoredPoint(Point point, String color) {
    }

    /**
     * Demonstrates record pattern deconstruction in switch.
     */
    public static String describePoint(Object obj) {
        return switch (obj) {
            case Point(int x, int y) when x == 0 && y == 0 -> "Origin";
            case Point(int x, int y) when x == 0 -> "On Y-axis at y=" + y;
            case Point(int x, int y) when y == 0 -> "On X-axis at x=" + x;
            case Point(int x, int y) -> "Point at (" + x + ", " + y + ")";
            default -> "Not a point";
        };
    }

    /**
     * Demonstrates nested record patterns.
     */
    public static String describeLine(Object obj) {
        return switch (obj) {
            case Line(Point(int x1, int y1), Point(int x2, int y2))
                    when x1 == x2 && y1 == y2 -> "Degenerate line (single point)";
            case Line(Point(int x1, var y1), Point(int x2, var y2))
                    when x1 == x2 -> "Vertical line at x=" + x1;
            case Line(Point(var x1, int y1), Point(var x2, int y2))
                    when y1 == y2 -> "Horizontal line at y=" + y1;
            case Line(Point(int x1, int y1), Point(int x2, int y2)) ->
                    "Line from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")";
            default -> "Not a line";
        };
    }

    /**
     * Demonstrates record patterns with if statements.
     */
    public static String describeColoredPoint(Object obj) {
        if (obj instanceof ColoredPoint(Point(int x, int y), String color)) {
            return color + " point at (" + x + ", " + y + ")";
        }
        return "Not a colored point";
    }

    // ========== Virtual Threads ==========

    /**
     * Creates and runs a virtual thread.
     */
    public static String runOnVirtualThread(Callable<String> task) throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> future = executor.submit(task);
            return future.get(5, TimeUnit.SECONDS);
        }
    }

    /**
     * Demonstrates creating many virtual threads for high concurrency.
     */
    public static List<Integer> runManyVirtualThreads(int count) throws InterruptedException {
        List<Integer> results = Collections.synchronizedList(new ArrayList<>());
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            final int value = i;
            Thread vt = Thread.ofVirtual().name("vt-" + i).start(() -> {
                results.add(value);
            });
            threads.add(vt);
        }

        for (Thread thread : threads) {
            thread.join();
        }

        return results.stream().sorted().collect(Collectors.toList());
    }

    /**
     * Demonstrates virtual thread executor service.
     */
    public static <T> List<T> executeTasksOnVirtualThreads(List<Callable<T>> tasks) throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<T>> futures = tasks.stream()
                    .map(executor::submit)
                    .collect(Collectors.toList());

            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get(10, TimeUnit.SECONDS));
            }
            return results;
        }
    }

    /**
     * Checks if the current thread is a virtual thread.
     */
    public static boolean isVirtualThread() throws Exception {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        Thread.ofVirtual().start(() -> result.complete(Thread.currentThread().isVirtual()));
        return result.get(5, TimeUnit.SECONDS);
    }

    /**
     * Demonstrates the difference in resource usage between platform and virtual threads.
     */
    public static Map<String, Long> compareThreadCreationTime(int threadCount) throws InterruptedException {
        Map<String, Long> results = new LinkedHashMap<>();

        // Virtual threads
        long startVirtual = System.nanoTime();
        List<Thread> virtualThreads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            virtualThreads.add(Thread.ofVirtual().start(() -> {
                // Lightweight task
                Thread.yield();
            }));
        }
        for (Thread t : virtualThreads) {
            t.join();
        }
        long virtualTime = System.nanoTime() - startVirtual;
        results.put("virtualThreadsNanos", virtualTime);

        // Platform threads (use fewer to avoid resource exhaustion)
        int platformCount = Math.min(threadCount, 100);
        long startPlatform = System.nanoTime();
        List<Thread> platformThreads = new ArrayList<>();
        for (int i = 0; i < platformCount; i++) {
            platformThreads.add(Thread.ofPlatform().start(() -> {
                Thread.yield();
            }));
        }
        for (Thread t : platformThreads) {
            t.join();
        }
        long platformTime = System.nanoTime() - startPlatform;
        results.put("platformThreadsNanos", platformTime);
        results.put("virtualCount", (long) threadCount);
        results.put("platformCount", (long) platformCount);

        return results;
    }

    // ========== Structured Concurrency (Preview in Java 21) ==========

    /**
     * Simulates structured concurrency concepts.
     * StructuredTaskScope is a preview API in Java 21.
     * This demonstrates the pattern without using the preview API directly.
     */
    public record TaskResult<T>(T value, boolean success, String error) {
        public static <T> TaskResult<T> success(T value) {
            return new TaskResult<>(value, true, null);
        }

        public static <T> TaskResult<T> failure(String error) {
            return new TaskResult<>(null, false, error);
        }
    }

    /**
     * Demonstrates the structured concurrency pattern: run tasks together, fail together.
     */
    public static <T> List<TaskResult<T>> runStructuredTasks(List<Callable<T>> tasks) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<T>> futures = tasks.stream()
                    .map(executor::submit)
                    .collect(Collectors.toList());

            return futures.stream()
                    .map(future -> {
                        try {
                            T result = future.get(10, TimeUnit.SECONDS);
                            return TaskResult.success(result);
                        } catch (Exception e) {
                            return TaskResult.<T>failure(e.getMessage());
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    // ========== Foreign Function & Memory API (Preview in Java 21) ==========

    /**
     * Explains the Foreign Function & Memory API concepts.
     * The actual API is in preview and requires --enable-preview flag.
     */
    public static String explainFFMApi() {
        return """
                Foreign Function & Memory API (JEP 442):

                Purpose:
                - Interoperate with code and data outside the Java runtime
                - Replace JNI with a pure-Java solution
                - Safely access off-heap (native) memory

                Key abstractions:
                1. MemorySegment - contiguous region of memory
                2. MemoryLayout - describes the layout of memory
                3. Linker - links Java code to foreign functions
                4. SymbolLookup - looks up foreign functions by name
                5. Arena - controls the lifecycle of memory segments

                Benefits over JNI:
                - Type-safe and memory-safe
                - No need for native code (C/C++ headers)
                - Better performance (no JNI overhead)
                - Deterministic memory deallocation
                """;
    }

    /**
     * Demonstrates sequenced collections (finalized in Java 21).
     */
    public static <T> T getFirst(SequencedCollection<T> collection) {
        return collection.getFirst();
    }

    public static <T> T getLast(SequencedCollection<T> collection) {
        return collection.getLast();
    }

    public static <T> SequencedCollection<T> reversed(SequencedCollection<T> collection) {
        return collection.reversed();
    }

    /**
     * Demonstrates string template-like formatting (preview in Java 21).
     * Using standard String formatting as the actual StringTemplate is preview.
     */
    public static String formatGreeting(String name, int age) {
        return "Hello, %s! You are %d years old.".formatted(name, age);
    }
}
