package com.github.msorkhpar.claudejavatutor.streamsapi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.*;

/**
 * Demonstrates the various ways to create streams in Java.
 * Covers section 4.4.2 — Creating and using streams.
 * <p>
 * Sources covered:
 * - Collection.stream() and parallelStream()
 * - Arrays.stream() for object and primitive arrays
 * - Stream.of(), Stream.empty(), Stream.ofNullable()
 * - Stream.generate() and Stream.iterate()
 * - IntStream/LongStream/DoubleStream ranges
 * - Stream.builder()
 * - Stream.concat()
 * - Files.lines()
 */
public class StreamCreation {

    // -----------------------------------------------------------------------
    // From Collections
    // -----------------------------------------------------------------------

    /** Creates a stream from a List. */
    public Stream<String> fromList(List<String> list) {
        return list.stream();
    }

    /** Creates a stream from a Set. */
    public Stream<String> fromSet(Set<String> set) {
        return set.stream();
    }

    /** Creates a stream from Map entries. */
    public Stream<Map.Entry<String, Integer>> fromMapEntries(Map<String, Integer> map) {
        return map.entrySet().stream();
    }

    // -----------------------------------------------------------------------
    // From Arrays
    // -----------------------------------------------------------------------

    /** Creates a stream from an object array. */
    public Stream<String> fromStringArray(String[] arr) {
        return Arrays.stream(arr);
    }

    /** Creates an IntStream from a primitive int array (no boxing). */
    public IntStream fromIntArray(int[] arr) {
        return Arrays.stream(arr);
    }

    /** Creates a stream from a sub-range of an array. */
    public Stream<String> fromArrayRange(String[] arr, int start, int end) {
        return Arrays.stream(arr, start, end);
    }

    // -----------------------------------------------------------------------
    // Factory Methods
    // -----------------------------------------------------------------------

    /** Creates a stream from varargs. */
    public Stream<String> fromVarargs(String... elements) {
        return Stream.of(elements);
    }

    /** Returns an empty stream. */
    public Stream<String> emptyStream() {
        return Stream.empty();
    }

    /**
     * Returns an empty stream if the value is null, or a singleton stream otherwise.
     * Uses Stream.ofNullable (Java 9+).
     */
    public Stream<String> nullableStream(String value) {
        return Stream.ofNullable(value);
    }

    // -----------------------------------------------------------------------
    // Infinite Streams
    // -----------------------------------------------------------------------

    /**
     * Creates a limited stream using Stream.generate().
     * The Supplier is called for each element on demand.
     */
    public List<Double> generateRandomNumbers(int count) {
        return Stream.generate(Math::random)
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Creates a finite sequence using Stream.iterate() (Java 8 — infinite, bounded by limit).
     */
    public List<Integer> iteratePowersOfTwo(int count) {
        return Stream.iterate(1, n -> n * 2)
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Creates a bounded sequence using Stream.iterate() with a predicate (Java 9+).
     * Equivalent to a traditional for-loop.
     */
    public List<Integer> iterateBounded(int start, int endExclusive) {
        return Stream.iterate(start, n -> n < endExclusive, n -> n + 1)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Primitive Streams
    // -----------------------------------------------------------------------

    /** Creates an IntStream from start (inclusive) to end (exclusive). */
    public IntStream intRange(int start, int endExclusive) {
        return IntStream.range(start, endExclusive);
    }

    /** Creates an IntStream from start to end (both inclusive). */
    public IntStream intRangeClosed(int start, int endInclusive) {
        return IntStream.rangeClosed(start, endInclusive);
    }

    /** Sums a range of integers without boxing. */
    public int sumRange(int start, int endInclusive) {
        return IntStream.rangeClosed(start, endInclusive).sum();
    }

    /** Returns summary statistics for a range. */
    public IntSummaryStatistics rangeStatistics(int start, int endInclusive) {
        return IntStream.rangeClosed(start, endInclusive).summaryStatistics();
    }

    /** Boxes an IntStream to Stream<Integer>. */
    public Stream<Integer> boxedIntStream(int start, int endExclusive) {
        return IntStream.range(start, endExclusive).boxed();
    }

    // -----------------------------------------------------------------------
    // Stream.builder()
    // -----------------------------------------------------------------------

    /**
     * Uses Stream.Builder to conditionally add elements and then build the stream.
     */
    public List<String> buildConditionally(List<String> items, boolean includeHeader, boolean includeFooter) {
        Stream.Builder<String> builder = Stream.builder();
        if (includeHeader) {
            builder.add("HEADER");
        }
        items.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .forEach(builder);
        if (includeFooter) {
            builder.add("FOOTER");
        }
        return builder.build().collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Stream.concat()
    // -----------------------------------------------------------------------

    /** Concatenates two streams into one. */
    public <T> Stream<T> concat(Stream<T> first, Stream<T> second) {
        return Stream.concat(first, second);
    }

    /** Concatenates multiple streams using flatMap (preferred for many streams). */
    @SafeVarargs
    public final <T> List<T> concatMultiple(Stream<T>... streams) {
        return Stream.of(streams)
                .flatMap(s -> s)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Files.lines() — resource-backed stream
    // -----------------------------------------------------------------------

    /**
     * Counts non-blank lines in a file. Uses try-with-resources to ensure the stream is closed.
     */
    public long countNonBlankLines(Path filePath) throws IOException {
        try (Stream<String> lines = Files.lines(filePath)) {
            return lines.filter(line -> !line.isBlank()).count();
        }
    }

    /**
     * Reads all non-blank lines from a file into a list.
     */
    public List<String> readNonBlankLines(Path filePath) throws IOException {
        try (Stream<String> lines = Files.lines(filePath)) {
            return lines.filter(line -> !line.isBlank()).toList();
        }
    }

    // -----------------------------------------------------------------------
    // String-based streams
    // -----------------------------------------------------------------------

    /**
     * Creates an IntStream of char values from a String.
     * Each int represents a char's Unicode code point.
     */
    public long countVowels(String text) {
        return text.chars()
                .filter(c -> "aeiouAEIOU".indexOf(c) >= 0)
                .count();
    }

    /**
     * Splits a string by a delimiter and streams the parts.
     */
    public List<String> splitAndStream(String text, String delimiter) {
        return Arrays.stream(text.split(delimiter))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
