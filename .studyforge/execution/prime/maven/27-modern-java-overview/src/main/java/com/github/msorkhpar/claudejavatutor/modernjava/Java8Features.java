package com.github.msorkhpar.claudejavatutor.modernjava;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Demonstrates key features introduced in Java 8 (LTS).
 * Covers: Lambda Expressions, Functional Interfaces, Stream API,
 * Default/Static Methods in Interfaces, Optional Class, and Date/Time API.
 */
public class Java8Features {

    // ========== Lambda Expressions and Functional Interfaces ==========

    /**
     * Demonstrates sorting with a lambda expression vs anonymous inner class.
     */
    public static List<String> sortStrings(List<String> input) {
        List<String> result = new ArrayList<>(input);
        result.sort((a, b) -> a.compareToIgnoreCase(b));
        return result;
    }

    /**
     * Uses built-in functional interfaces: Predicate, Function, Consumer, Supplier.
     */
    public static <T> List<T> filterWith(List<T> input, Predicate<T> predicate) {
        return input.stream().filter(predicate).collect(Collectors.toList());
    }

    public static <T, R> List<R> mapWith(List<T> input, Function<T, R> mapper) {
        return input.stream().map(mapper).collect(Collectors.toList());
    }

    public static <T> void forEachWith(List<T> input, Consumer<T> action) {
        input.forEach(action);
    }

    public static <T> T supplyValue(Supplier<T> supplier) {
        return supplier.get();
    }

    /**
     * Demonstrates chaining predicates with and/or/negate.
     */
    public static <T> List<T> filterWithChainedPredicates(
            List<T> input, Predicate<T> first, Predicate<T> second) {
        return input.stream().filter(first.and(second)).collect(Collectors.toList());
    }

    // ========== Stream API ==========

    /**
     * Demonstrates creating streams from various sources.
     */
    public static IntStream rangeStream(int startInclusive, int endExclusive) {
        return IntStream.range(startInclusive, endExclusive);
    }

    /**
     * Intermediate operations: filter, map, flatMap, sorted, distinct, peek.
     */
    public static List<String> intermediateOps(List<List<String>> nestedLists) {
        return nestedLists.stream()
                .flatMap(Collection::stream)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Terminal operations: reduce, collect, count, findFirst, anyMatch.
     */
    public static int reduceToSum(List<Integer> numbers) {
        return numbers.stream().reduce(0, Integer::sum);
    }

    public static Optional<String> findFirstStartingWith(List<String> strings, String prefix) {
        return strings.stream()
                .filter(s -> s.startsWith(prefix))
                .findFirst();
    }

    public static boolean anyMatchGreaterThan(List<Integer> numbers, int threshold) {
        return numbers.stream().anyMatch(n -> n > threshold);
    }

    /**
     * Collectors: groupingBy, partitioningBy, joining.
     */
    public static Map<Integer, List<String>> groupByLength(List<String> strings) {
        return strings.stream().collect(Collectors.groupingBy(String::length));
    }

    public static Map<Boolean, List<Integer>> partitionEvenOdd(List<Integer> numbers) {
        return numbers.stream().collect(Collectors.partitioningBy(n -> n % 2 == 0));
    }

    public static String joinStrings(List<String> strings, String delimiter) {
        return strings.stream().collect(Collectors.joining(delimiter));
    }

    /**
     * Parallel stream demonstration.
     */
    public static long parallelSum(List<Long> numbers) {
        return numbers.parallelStream().mapToLong(Long::longValue).sum();
    }

    // ========== Default and Static Methods in Interfaces ==========

    /**
     * Interface with default and static methods.
     */
    public interface Greeter {
        String greet(String name);

        default String greetWithTitle(String title, String name) {
            return greet(title + " " + name);
        }

        static String defaultGreeting() {
            return "Hello, World!";
        }
    }

    /**
     * Demonstrates the diamond problem resolution with default methods.
     */
    public interface InterfaceA {
        default String identify() {
            return "InterfaceA";
        }
    }

    public interface InterfaceB {
        default String identify() {
            return "InterfaceB";
        }
    }

    public static class DiamondResolver implements InterfaceA, InterfaceB {
        @Override
        public String identify() {
            return InterfaceA.super.identify() + " + " + InterfaceB.super.identify();
        }
    }

    // ========== Optional Class ==========

    /**
     * Demonstrates Optional creation and usage.
     */
    public static Optional<String> findUserName(Map<Integer, String> users, int id) {
        return Optional.ofNullable(users.get(id));
    }

    public static String getUserNameOrDefault(Map<Integer, String> users, int id, String defaultName) {
        return findUserName(users, id).orElse(defaultName);
    }

    public static Optional<String> findAndTransform(Map<Integer, String> users, int id) {
        return findUserName(users, id)
                .filter(name -> name.length() > 3)
                .map(String::toUpperCase);
    }

    /**
     * Demonstrates Optional chaining with flatMap.
     */
    public static Optional<String> findAddress(Map<Integer, String> users,
                                                Map<String, String> addresses, int userId) {
        return findUserName(users, userId)
                .flatMap(name -> Optional.ofNullable(addresses.get(name)));
    }

    // ========== New Date and Time API ==========

    /**
     * Demonstrates LocalDate, LocalTime, LocalDateTime usage.
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalDate createDate(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }

    public static LocalTime createTime(int hour, int minute) {
        return LocalTime.of(hour, minute);
    }

    public static LocalDateTime createDateTime(int year, int month, int day, int hour, int minute) {
        return LocalDateTime.of(year, month, day, hour, minute);
    }

    /**
     * Demonstrates period and duration calculations.
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    public static Period periodBetween(LocalDate start, LocalDate end) {
        return Period.between(start, end);
    }

    /**
     * Demonstrates date formatting and parsing.
     */
    public static String formatDate(LocalDate date, String pattern) {
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate parseDate(String dateStr, String pattern) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Demonstrates Instant and ZonedDateTime.
     */
    public static ZonedDateTime convertTimezone(ZonedDateTime dateTime, ZoneId targetZone) {
        return dateTime.withZoneSameInstant(targetZone);
    }

    public static Instant toInstant(LocalDateTime dateTime, ZoneId zone) {
        return dateTime.atZone(zone).toInstant();
    }
}
