package com.github.msorkhpar.claudejavatutor.streamsapi;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.*;

/**
 * Demonstrates parallel streams and performance considerations.
 * Covers section 4.4.5 — Parallel streams and performance considerations.
 * <p>
 * Topics covered:
 * - Creating parallel streams with parallelStream() and parallel()
 * - Converting between parallel and sequential
 * - Thread safety in collectors
 * - Ordering guarantees (forEachOrdered vs forEach)
 * - Correct reduce operations (associativity requirement)
 * - Custom ForkJoinPool usage
 * - Performance benchmarking with PerformanceTestUtil
 * - When to use and when to avoid parallel streams
 */
public class ParallelStreams {

    // -----------------------------------------------------------------------
    // Creating parallel streams
    // -----------------------------------------------------------------------

    /** Creates a parallel stream from a collection. */
    public boolean isParallelFromCollection(List<Integer> list) {
        return list.parallelStream().isParallel();
    }

    /** Converts a sequential stream to parallel. */
    public boolean isParallelAfterConversion(List<Integer> list) {
        return list.stream().parallel().isParallel();
    }

    /** Converts a parallel stream back to sequential. */
    public boolean isSequentialAfterConversion(List<Integer> list) {
        return list.parallelStream().sequential().isParallel();
    }

    // -----------------------------------------------------------------------
    // Thread-safe collection with parallel streams
    // -----------------------------------------------------------------------

    /**
     * Demonstrates UNSAFE pattern: forEach with shared ArrayList (race condition).
     * Returns the list but results may be incorrect due to unsynchronized writes.
     * THIS IS INTENTIONALLY UNSAFE — shown for educational purposes only.
     */
    public List<Integer> unsafeForEachCollect(List<Integer> list) {
        List<Integer> result = new ArrayList<>(); // NOT thread-safe
        list.parallelStream().forEach(result::add); // Data corruption possible
        return result; // size may be wrong!
    }

    /**
     * Demonstrates SAFE pattern: use collect() with thread-safe collector.
     * This is the correct way to collect in parallel.
     */
    public List<Integer> safeCollect(List<Integer> list) {
        return list.parallelStream()
                .filter(n -> n % 2 == 0)
                .collect(Collectors.toList()); // Thread-safe
    }

    /**
     * Demonstrates collecting to a ConcurrentHashMap for thread-safe parallel accumulation.
     */
    public Map<Integer, Long> parallelGroupCount(List<Integer> list) {
        return list.parallelStream()
                .collect(Collectors.groupingByConcurrent(
                        n -> n % 10,
                        Collectors.counting()
                ));
    }

    // -----------------------------------------------------------------------
    // Ordering: forEach vs forEachOrdered
    // -----------------------------------------------------------------------

    /**
     * Collects elements in parallel using forEach — order NOT guaranteed.
     * Returns the list of collected elements (may be in any order).
     */
    public List<Integer> parallelForEach(List<Integer> list) {
        List<Integer> result = Collections.synchronizedList(new ArrayList<>());
        list.parallelStream().forEach(result::add);
        return result;
    }

    /**
     * Collects elements in parallel using forEachOrdered — order IS guaranteed.
     * More expensive than forEach in parallel, but preserves encounter order.
     */
    public List<Integer> parallelForEachOrdered(List<Integer> list) {
        List<Integer> result = new ArrayList<>();
        list.parallelStream().forEachOrdered(result::add);
        return result;
    }

    // -----------------------------------------------------------------------
    // Correctness: associative reduce
    // -----------------------------------------------------------------------

    /**
     * Correctly sums integers in parallel using associative addition.
     */
    public long parallelSum(List<Integer> list) {
        return list.parallelStream()
                .mapToLong(Integer::longValue)
                .sum();
    }

    /**
     * Demonstrates that multiplication (associative) is safe in parallel reduce.
     */
    public long parallelProduct(List<Integer> list) {
        return list.parallelStream()
                .mapToLong(Integer::longValue)
                .reduce(1L, (a, b) -> a * b);
    }

    /**
     * Demonstrates correct parallel max using Comparator.
     */
    public Optional<Integer> parallelMax(List<Integer> list) {
        return list.parallelStream().max(Comparator.naturalOrder());
    }

    /**
     * Demonstrates correct parallel min.
     */
    public Optional<Integer> parallelMin(List<Integer> list) {
        return list.parallelStream().min(Comparator.naturalOrder());
    }

    // -----------------------------------------------------------------------
    // unordered() hint for better parallel performance
    // -----------------------------------------------------------------------

    /**
     * Uses unordered() to give the stream engine more freedom in parallel processing.
     * distinct() and limit() are more efficient on unordered streams.
     */
    public List<Integer> parallelDistinctUnordered(List<Integer> list) {
        return list.parallelStream()
                .unordered()
                .distinct()
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Custom ForkJoinPool
    // -----------------------------------------------------------------------

    /**
     * Runs a parallel stream in a custom ForkJoinPool with specified parallelism.
     * This isolates the parallel work from the common pool.
     */
    public List<Integer> runInCustomPool(List<Integer> list, int parallelism) throws Exception {
        ForkJoinPool customPool = new ForkJoinPool(parallelism);
        try {
            return customPool.submit(() ->
                    list.parallelStream()
                            .filter(n -> n % 2 == 0)
                            .sorted()
                            .collect(Collectors.toList())
            ).get();
        } finally {
            customPool.shutdown();
        }
    }

    // -----------------------------------------------------------------------
    // Performance: sequential vs parallel
    // -----------------------------------------------------------------------

    /**
     * Sequentially computes the sum of squares of even numbers.
     * Used for benchmarking comparison with parallel version.
     */
    public long sumOfSquaresSequential(List<Integer> list) {
        return list.stream()
                .filter(n -> n % 2 == 0)
                .mapToLong(n -> (long) n * n)
                .sum();
    }

    /**
     * Computes the sum of squares of even numbers in parallel.
     * For large lists, this should be faster on multi-core machines.
     */
    public long sumOfSquaresParallel(List<Integer> list) {
        return list.parallelStream()
                .filter(n -> n % 2 == 0)
                .mapToLong(n -> (long) n * n)
                .sum();
    }

    /**
     * CPU-intensive task: computes whether a number is prime.
     * Parallel processing of such tasks benefits from parallelism.
     */
    public long countPrimesSequential(int limit) {
        return IntStream.rangeClosed(2, limit)
                .filter(this::isPrime)
                .count();
    }

    public long countPrimesParallel(int limit) {
        return IntStream.rangeClosed(2, limit)
                .parallel()
                .filter(this::isPrime)
                .count();
    }

    private boolean isPrime(int n) {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // Thread safety with stateful operations
    // -----------------------------------------------------------------------

    /**
     * Demonstrates a safe stateful deduplication using ConcurrentHashMap in parallel.
     */
    public List<String> parallelDistinctWithConcurrentSet(List<String> list) {
        Set<String> seen = ConcurrentHashMap.newKeySet();
        return list.parallelStream()
                .filter(seen::add)
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates groupingByConcurrent for parallel grouping into a ConcurrentHashMap.
     */
    public Map<String, List<String>> parallelGroupByFirstChar(List<String> list) {
        return list.parallelStream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.groupingByConcurrent(
                        s -> String.valueOf(s.charAt(0))
                ));
    }
}
