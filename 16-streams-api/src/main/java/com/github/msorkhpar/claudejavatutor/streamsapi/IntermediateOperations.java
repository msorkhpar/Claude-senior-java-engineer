package com.github.msorkhpar.claudejavatutor.streamsapi;

import java.util.*;
import java.util.stream.*;

/**
 * Demonstrates intermediate stream operations.
 * Covers section 4.4.3 — Intermediate operations (filter, map, flatMap).
 * <p>
 * Operations covered:
 * filter, map, flatMap, distinct, sorted, peek, limit, skip,
 * takeWhile, dropWhile (Java 9), mapToInt/mapToLong/mapToDouble, mapMulti (Java 16)
 */
public class IntermediateOperations {

    // -----------------------------------------------------------------------
    // filter
    // -----------------------------------------------------------------------

    /** Keeps only strings longer than the given minimum length. */
    public List<String> filterByLength(List<String> list, int minLength) {
        return list.stream()
                .filter(s -> s.length() >= minLength)
                .collect(Collectors.toList());
    }

    /** Filters out null elements. */
    public List<String> filterNulls(List<String> list) {
        return list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** Chains multiple filter predicates. */
    public List<String> multiFilter(List<String> list, String prefix, int maxLength) {
        return list.stream()
                .filter(s -> s.startsWith(prefix))
                .filter(s -> s.length() <= maxLength)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // map
    // -----------------------------------------------------------------------

    /** Converts strings to their lengths. */
    public List<Integer> mapToLengths(List<String> list) {
        return list.stream()
                .map(String::length)
                .collect(Collectors.toList());
    }

    /** Uppercases each string. */
    public List<String> mapToUpperCase(List<String> list) {
        return list.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }

    /** Maps objects to a specific field. */
    public record Person(String name, int age) {}

    public List<String> extractNames(List<Person> people) {
        return people.stream()
                .map(Person::name)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // flatMap
    // -----------------------------------------------------------------------

    /** Flattens a list of lists into a single list. */
    public List<String> flattenLists(List<List<String>> nested) {
        return nested.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /** Splits sentences into individual words. */
    public List<String> splitIntoWords(List<String> sentences) {
        return sentences.stream()
                .flatMap(s -> Arrays.stream(s.split("\\s+")))
                .filter(w -> !w.isEmpty())
                .collect(Collectors.toList());
    }

    /** Flattens Optionals using Optional.stream() (Java 9+). */
    public List<String> flattenOptionals(List<Optional<String>> optionals) {
        return optionals.stream()
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // distinct
    // -----------------------------------------------------------------------

    /** Removes duplicate integers. */
    public List<Integer> removeDuplicates(List<Integer> list) {
        return list.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    /** Removes duplicate strings, preserving first occurrence order. */
    public List<String> deduplicateStrings(List<String> list) {
        return list.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // sorted
    // -----------------------------------------------------------------------

    /** Sorts strings in natural order. */
    public List<String> sortNatural(List<String> list) {
        return list.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /** Sorts strings by length, then alphabetically. */
    public List<String> sortByLengthThenAlpha(List<String> list) {
        return list.stream()
                .sorted(Comparator.comparingInt(String::length)
                        .thenComparing(Comparator.naturalOrder()))
                .collect(Collectors.toList());
    }

    /** Sorts with null-safe comparator. */
    public List<String> sortWithNulls(List<String> list) {
        return list.stream()
                .sorted(Comparator.nullsFirst(Comparator.naturalOrder()))
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // peek
    // -----------------------------------------------------------------------

    /**
     * Uses peek for debugging by collecting all elements seen at an intermediate stage.
     * In production code, use peek only for debugging/logging.
     */
    public List<String> peekAndCollect(List<String> list, List<String> peekCapture) {
        return list.stream()
                .filter(s -> s.length() > 2)
                .peek(peekCapture::add) // Capture elements after filter for debugging
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // limit and skip
    // -----------------------------------------------------------------------

    /** Returns the first n elements. */
    public List<Integer> firstN(List<Integer> list, int n) {
        return list.stream()
                .limit(n)
                .collect(Collectors.toList());
    }

    /** Skips the first n elements and returns the rest. */
    public List<Integer> skipN(List<Integer> list, int n) {
        return list.stream()
                .skip(n)
                .collect(Collectors.toList());
    }

    /** Returns a page of elements: skip (page * size) and take size elements. */
    public List<Integer> paginate(List<Integer> list, int page, int size) {
        return list.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // takeWhile and dropWhile (Java 9+)
    // -----------------------------------------------------------------------

    /**
     * Takes elements from the beginning of the stream while the predicate holds.
     * Stops at the first element that does NOT match.
     */
    public List<Integer> takeWhileLessThan(List<Integer> list, int threshold) {
        return list.stream()
                .takeWhile(n -> n < threshold)
                .collect(Collectors.toList());
    }

    /**
     * Drops elements from the beginning of the stream while the predicate holds.
     * Passes all remaining elements once the predicate first fails.
     */
    public List<Integer> dropWhileLessThan(List<Integer> list, int threshold) {
        return list.stream()
                .dropWhile(n -> n < threshold)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // mapToInt / mapToLong / mapToDouble
    // -----------------------------------------------------------------------

    /** Sums string lengths using primitive IntStream (no boxing). */
    public int sumLengths(List<String> list) {
        return list.stream()
                .mapToInt(String::length)
                .sum();
    }

    /** Gets average of integer values avoiding boxing. */
    public OptionalDouble averageOfValues(List<String> numbers) {
        return numbers.stream()
                .mapToInt(Integer::parseInt)
                .average();
    }

    /** Sums long values for large numbers. */
    public long sumLongValues(List<Integer> list) {
        return list.stream()
                .mapToLong(Integer::longValue)
                .sum();
    }

    /** Converts IntStream to Stream<String> using mapToObj. */
    public List<String> intRangeToStrings(int start, int endExclusive) {
        return IntStream.range(start, endExclusive)
                .mapToObj(Integer::toString)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // mapMulti (Java 16+)
    // -----------------------------------------------------------------------

    /**
     * Uses mapMulti to expand each element into multiple elements.
     * Here: each number n emits n and its square.
     */
    public List<Integer> expandToValueAndSquare(List<Integer> list) {
        return list.stream()
                .<Integer>mapMulti((n, downstream) -> {
                    downstream.accept(n);
                    downstream.accept(n * n);
                })
                .collect(Collectors.toList());
    }

    /**
     * Uses mapMulti to filter and transform in one step
     * (emits only String instances from a mixed list).
     */
    public List<String> filterStringsFromMixed(List<Object> mixed) {
        return mixed.stream()
                .<String>mapMulti((obj, downstream) -> {
                    if (obj instanceof String s) {
                        downstream.accept(s.toUpperCase());
                    }
                })
                .collect(Collectors.toList());
    }
}
