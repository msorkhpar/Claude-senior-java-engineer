package com.github.msorkhpar.claudejavatutor.streamsapi;

import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

/**
 * Demonstrates terminal stream operations.
 * Covers section 4.4.4 — Terminal operations (forEach, reduce, collect).
 * <p>
 * Operations covered:
 * forEach, forEachOrdered, collect (toList, toSet, toMap, groupingBy, partitioningBy, joining),
 * reduce (with and without identity), count, min, max, findFirst, findAny,
 * anyMatch, allMatch, noneMatch, toArray, Collectors.teeing (Java 12+)
 */
public class TerminalOperations {

    // -----------------------------------------------------------------------
    // forEach / forEachOrdered
    // -----------------------------------------------------------------------

    /** Collects elements into a list using forEach. */
    public List<String> collectViaForEach(List<String> list) {
        List<String> result = new ArrayList<>();
        list.stream()
                .filter(s -> s.length() > 2)
                .forEach(result::add);
        return result;
    }

    // -----------------------------------------------------------------------
    // collect — toList, toSet, toMap
    // -----------------------------------------------------------------------

    /** Collects to a mutable list. */
    public List<String> toMutableList(List<String> list) {
        return list.stream().collect(Collectors.toList());
    }

    /** Collects to an unmodifiable list (Java 10+). */
    public List<String> toUnmodifiableList(List<String> list) {
        return list.stream().collect(Collectors.toUnmodifiableList());
    }

    /** Collects to a Set (removes duplicates). */
    public Set<String> toSet(List<String> list) {
        return list.stream().collect(Collectors.toSet());
    }

    /** Collects to a Map with no duplicate keys. */
    public Map<String, Integer> toWordLengthMap(List<String> words) {
        return words.stream()
                .collect(Collectors.toMap(Function.identity(), String::length));
    }

    /** Collects to a Map with duplicate key merge (keep longest). */
    public Map<Integer, String> toLengthToWordMap(List<String> words) {
        return words.stream()
                .collect(Collectors.toMap(
                        String::length,
                        Function.identity(),
                        (existing, newVal) -> existing.length() >= newVal.length() ? existing : newVal
                ));
    }

    /** Collects to a LinkedHashMap preserving insertion order. */
    public Map<String, Integer> toLinkedHashMap(List<String> words) {
        return words.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        String::length,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    // -----------------------------------------------------------------------
    // groupingBy
    // -----------------------------------------------------------------------

    /** Groups strings by their first character. */
    public Map<Character, List<String>> groupByFirstChar(List<String> list) {
        return list.stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.groupingBy(s -> s.charAt(0)));
    }

    /** Groups strings by length, counting occurrences. */
    public Map<Integer, Long> groupByLengthCount(List<String> list) {
        return list.stream()
                .collect(Collectors.groupingBy(String::length, Collectors.counting()));
    }

    /** Groups and collects names per group. */
    public record Employee(String name, String department, int salary) {}

    public Map<String, List<String>> groupEmployeesByDepartment(List<Employee> employees) {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::department,
                        Collectors.mapping(Employee::name, Collectors.toList())
                ));
    }

    /** Groups and averages salary per department. */
    public Map<String, Double> averageSalaryByDepartment(List<Employee> employees) {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::department,
                        Collectors.averagingInt(Employee::salary)
                ));
    }

    // -----------------------------------------------------------------------
    // partitioningBy
    // -----------------------------------------------------------------------

    /** Partitions strings into long (>= minLength) and short. */
    public Map<Boolean, List<String>> partitionByLength(List<String> list, int minLength) {
        return list.stream()
                .collect(Collectors.partitioningBy(s -> s.length() >= minLength));
    }

    /** Partitions employees into high and low salary. */
    public Map<Boolean, Long> partitionAndCountBySalary(List<Employee> employees, int threshold) {
        return employees.stream()
                .collect(Collectors.partitioningBy(
                        e -> e.salary() >= threshold,
                        Collectors.counting()
                ));
    }

    // -----------------------------------------------------------------------
    // joining
    // -----------------------------------------------------------------------

    /** Joins all strings with a delimiter. */
    public String joinWithDelimiter(List<String> list, String delimiter) {
        return list.stream().collect(Collectors.joining(delimiter));
    }

    /** Joins with delimiter, prefix, and suffix. */
    public String joinFormatted(List<String> list, String delimiter, String prefix, String suffix) {
        return list.stream().collect(Collectors.joining(delimiter, prefix, suffix));
    }

    /** Joins non-empty strings. */
    public String joinNonEmpty(List<String> list) {
        return list.stream()
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
    }

    // -----------------------------------------------------------------------
    // reduce
    // -----------------------------------------------------------------------

    /** Sums integers using reduce with identity. */
    public int sumWithIdentity(List<Integer> list) {
        return list.stream().reduce(0, Integer::sum);
    }

    /** Multiplies integers using reduce with identity. */
    public int productWithIdentity(List<Integer> list) {
        return list.stream().reduce(1, (a, b) -> a * b);
    }

    /** Sums integers using reduce without identity (returns Optional). */
    public Optional<Integer> sumWithoutIdentity(List<Integer> list) {
        return list.stream().reduce(Integer::sum);
    }

    /** Concatenates strings using reduce. */
    public String concatenate(List<String> list) {
        return list.stream().reduce("", String::concat);
    }

    /** Finds maximum using reduce. */
    public Optional<Integer> findMax(List<Integer> list) {
        return list.stream().reduce(Integer::max);
    }

    // -----------------------------------------------------------------------
    // count, min, max
    // -----------------------------------------------------------------------

    /** Counts elements matching a condition. */
    public long countLongStrings(List<String> list, int minLength) {
        return list.stream().filter(s -> s.length() >= minLength).count();
    }

    /** Finds minimum string by natural order. */
    public Optional<String> minString(List<String> list) {
        return list.stream().min(Comparator.naturalOrder());
    }

    /** Finds maximum integer. */
    public Optional<Integer> maxInt(List<Integer> list) {
        return list.stream().max(Comparator.naturalOrder());
    }

    /** Finds shortest string. */
    public Optional<String> shortestString(List<String> list) {
        return list.stream().min(Comparator.comparingInt(String::length));
    }

    // -----------------------------------------------------------------------
    // findFirst / findAny
    // -----------------------------------------------------------------------

    /** Returns the first element matching the prefix. */
    public Optional<String> findFirstWithPrefix(List<String> list, String prefix) {
        return list.stream()
                .filter(s -> s.startsWith(prefix))
                .findFirst();
    }

    /** Returns any element matching (non-deterministic, useful in parallel). */
    public Optional<String> findAnyWithPrefix(List<String> list, String prefix) {
        return list.stream()
                .filter(s -> s.startsWith(prefix))
                .findAny();
    }

    // -----------------------------------------------------------------------
    // anyMatch / allMatch / noneMatch
    // -----------------------------------------------------------------------

    /** Returns true if any element is longer than minLength. */
    public boolean anyLongerThan(List<String> list, int minLength) {
        return list.stream().anyMatch(s -> s.length() > minLength);
    }

    /** Returns true if all elements are longer than minLength. */
    public boolean allLongerThan(List<String> list, int minLength) {
        return list.stream().allMatch(s -> s.length() > minLength);
    }

    /** Returns true if no element starts with the given prefix. */
    public boolean noneStartsWith(List<String> list, String prefix) {
        return list.stream().noneMatch(s -> s.startsWith(prefix));
    }

    // -----------------------------------------------------------------------
    // toArray
    // -----------------------------------------------------------------------

    /** Collects to a String array. */
    public String[] toStringArray(List<String> list) {
        return list.stream().toArray(String[]::new);
    }

    // -----------------------------------------------------------------------
    // Collectors.teeing (Java 12+)
    // -----------------------------------------------------------------------

    public record MinMaxPair(int min, int max) {}

    /** Computes min and max in a single stream pass using teeing. */
    public MinMaxPair minMaxInOnePass(List<Integer> list) {
        return list.stream().collect(
                Collectors.<Integer, Optional<Integer>, Optional<Integer>, MinMaxPair>teeing(
                        Collectors.minBy(Comparator.<Integer>naturalOrder()),
                        Collectors.maxBy(Comparator.<Integer>naturalOrder()),
                        (min, max) -> new MinMaxPair(
                                min.orElseThrow(),
                                max.orElseThrow()
                        )
                )
        );
    }

    public record SummaryStats(long count, double average) {}

    /** Computes count and average in a single stream pass using teeing. */
    public SummaryStats countAndAverage(List<Integer> list) {
        return list.stream().collect(
                Collectors.teeing(
                        Collectors.counting(),
                        Collectors.averagingInt(Integer::intValue),
                        SummaryStats::new
                )
        );
    }

    // -----------------------------------------------------------------------
    // Edge case demonstrations
    // -----------------------------------------------------------------------

    /** Returns all three empty-stream match results. */
    public record EmptyMatchResults(boolean allMatch, boolean anyMatch, boolean noneMatch) {}

    public EmptyMatchResults matchResultsForEmpty() {
        return new EmptyMatchResults(
                Stream.<String>empty().allMatch(s -> s.length() > 0),
                Stream.<String>empty().anyMatch(s -> s.length() > 0),
                Stream.<String>empty().noneMatch(s -> s.length() > 0)
        );
    }

    /** Returns Optional.empty() for reduce on empty stream without identity. */
    public Optional<Integer> reduceEmptyWithoutIdentity() {
        return Stream.<Integer>empty().reduce(Integer::sum);
    }

    /** Returns identity (0) for reduce on empty stream with identity. */
    public int reduceEmptyWithIdentity() {
        return Stream.<Integer>empty().reduce(0, Integer::sum);
    }
}
