package com.github.msorkhpar.claudejavatutor.modernjava;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Java 8 Features Tests")
class Java8FeaturesTest {

    @Nested
    @DisplayName("Lambda Expressions and Functional Interfaces")
    class LambdaAndFunctionalInterfaceTests {

        @Test
        @DisplayName("Should sort strings case-insensitively")
        void testSortStrings() {
            List<String> input = Arrays.asList("Charlie", "alice", "Bob");
            List<String> result = Java8Features.sortStrings(input);
            assertThat(result).containsExactly("alice", "Bob", "Charlie");
        }

        @Test
        @DisplayName("Should sort empty list")
        void testSortEmptyList() {
            List<String> result = Java8Features.sortStrings(Collections.emptyList());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should sort single element list")
        void testSortSingleElement() {
            List<String> result = Java8Features.sortStrings(List.of("only"));
            assertThat(result).containsExactly("only");
        }

        @Test
        @DisplayName("Should not modify original list")
        void testSortDoesNotModifyOriginal() {
            List<String> original = new ArrayList<>(Arrays.asList("C", "A", "B"));
            Java8Features.sortStrings(original);
            assertThat(original).containsExactly("C", "A", "B");
        }

        @Test
        @DisplayName("Should filter with predicate")
        void testFilterWith() {
            List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);
            List<Integer> evens = Java8Features.filterWith(numbers, n -> n % 2 == 0);
            assertThat(evens).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("Should filter with no matches")
        void testFilterWithNoMatches() {
            List<Integer> numbers = Arrays.asList(1, 3, 5);
            List<Integer> evens = Java8Features.filterWith(numbers, n -> n % 2 == 0);
            assertThat(evens).isEmpty();
        }

        @Test
        @DisplayName("Should map with function")
        void testMapWith() {
            List<String> strings = Arrays.asList("hello", "world");
            List<Integer> lengths = Java8Features.mapWith(strings, String::length);
            assertThat(lengths).containsExactly(5, 5);
        }

        @Test
        @DisplayName("Should supply value")
        void testSupplyValue() {
            String result = Java8Features.supplyValue(() -> "Hello");
            assertThat(result).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should filter with chained predicates")
        void testChainedPredicates() {
            List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            List<Integer> result = Java8Features.filterWithChainedPredicates(
                    numbers, n -> n % 2 == 0, n -> n > 5);
            assertThat(result).containsExactly(6, 8, 10);
        }

        @Test
        @DisplayName("Should handle forEach with consumer")
        void testForEachWith() {
            List<String> collected = new ArrayList<>();
            Java8Features.forEachWith(Arrays.asList("a", "b", "c"), collected::add);
            assertThat(collected).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("Stream API")
    class StreamApiTests {

        @Test
        @DisplayName("Should create range stream")
        void testRangeStream() {
            int sum = Java8Features.rangeStream(1, 5).sum();
            assertThat(sum).isEqualTo(10); // 1+2+3+4
        }

        @Test
        @DisplayName("Should handle empty range")
        void testEmptyRangeStream() {
            long count = Java8Features.rangeStream(5, 5).count();
            assertThat(count).isZero();
        }

        @Test
        @DisplayName("Should perform intermediate operations on nested lists")
        void testIntermediateOps() {
            List<List<String>> nested = Arrays.asList(
                    Arrays.asList("apple", "banana"),
                    Arrays.asList("banana", "cherry", ""),
                    Arrays.asList("date")
            );
            List<String> result = Java8Features.intermediateOps(nested);
            assertThat(result).containsExactly("APPLE", "BANANA", "CHERRY", "DATE");
        }

        @Test
        @DisplayName("Should handle empty nested lists")
        void testIntermediateOpsEmpty() {
            List<List<String>> nested = Arrays.asList(
                    Collections.emptyList(),
                    Collections.emptyList()
            );
            assertThat(Java8Features.intermediateOps(nested)).isEmpty();
        }

        @Test
        @DisplayName("Should reduce to sum")
        void testReduceToSum() {
            assertThat(Java8Features.reduceToSum(Arrays.asList(1, 2, 3, 4, 5))).isEqualTo(15);
        }

        @Test
        @DisplayName("Should reduce empty list to zero")
        void testReduceEmptyList() {
            assertThat(Java8Features.reduceToSum(Collections.emptyList())).isZero();
        }

        @Test
        @DisplayName("Should find first starting with prefix")
        void testFindFirstStartingWith() {
            List<String> strings = Arrays.asList("apple", "banana", "avocado");
            assertThat(Java8Features.findFirstStartingWith(strings, "a")).hasValue("apple");
        }

        @Test
        @DisplayName("Should return empty optional when no match found")
        void testFindFirstNoMatch() {
            List<String> strings = Arrays.asList("apple", "banana");
            assertThat(Java8Features.findFirstStartingWith(strings, "z")).isEmpty();
        }

        @Test
        @DisplayName("Should check anyMatch greater than threshold")
        void testAnyMatchGreaterThan() {
            assertThat(Java8Features.anyMatchGreaterThan(Arrays.asList(1, 5, 10), 7)).isTrue();
            assertThat(Java8Features.anyMatchGreaterThan(Arrays.asList(1, 2, 3), 10)).isFalse();
        }

        @Test
        @DisplayName("Should group strings by length")
        void testGroupByLength() {
            Map<Integer, List<String>> grouped = Java8Features.groupByLength(
                    Arrays.asList("hi", "hey", "hello", "ok"));
            assertThat(grouped.get(2)).containsExactlyInAnyOrder("hi", "ok");
            assertThat(grouped.get(3)).containsExactly("hey");
            assertThat(grouped.get(5)).containsExactly("hello");
        }

        @Test
        @DisplayName("Should partition even and odd numbers")
        void testPartitionEvenOdd() {
            Map<Boolean, List<Integer>> partitioned = Java8Features.partitionEvenOdd(
                    Arrays.asList(1, 2, 3, 4, 5));
            assertThat(partitioned.get(true)).containsExactly(2, 4);
            assertThat(partitioned.get(false)).containsExactly(1, 3, 5);
        }

        @Test
        @DisplayName("Should join strings with delimiter")
        void testJoinStrings() {
            assertThat(Java8Features.joinStrings(Arrays.asList("a", "b", "c"), ", "))
                    .isEqualTo("a, b, c");
        }

        @Test
        @DisplayName("Should join empty list")
        void testJoinEmptyList() {
            assertThat(Java8Features.joinStrings(Collections.emptyList(), ", ")).isEmpty();
        }

        @Test
        @DisplayName("Should calculate parallel sum")
        void testParallelSum() {
            List<Long> numbers = IntStream.rangeClosed(1, 1000)
                    .mapToLong(i -> (long) i)
                    .boxed()
                    .toList();
            assertThat(Java8Features.parallelSum(numbers)).isEqualTo(500500L);
        }
    }

    @Nested
    @DisplayName("Default and Static Methods in Interfaces")
    class DefaultStaticMethodTests {

        @Test
        @DisplayName("Should use default method in interface")
        void testDefaultMethod() {
            Java8Features.Greeter greeter = name -> "Hello, " + name + "!";
            assertThat(greeter.greetWithTitle("Mr", "Smith")).isEqualTo("Hello, Mr Smith!");
        }

        @Test
        @DisplayName("Should use static method in interface")
        void testStaticMethod() {
            assertThat(Java8Features.Greeter.defaultGreeting()).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should resolve diamond problem")
        void testDiamondResolver() {
            Java8Features.DiamondResolver resolver = new Java8Features.DiamondResolver();
            assertThat(resolver.identify()).isEqualTo("InterfaceA + InterfaceB");
        }
    }

    @Nested
    @DisplayName("Optional Class")
    class OptionalTests {

        @Test
        @DisplayName("Should find user name from map")
        void testFindUserName() {
            Map<Integer, String> users = Map.of(1, "Alice", 2, "Bob");
            assertThat(Java8Features.findUserName(users, 1)).hasValue("Alice");
        }

        @Test
        @DisplayName("Should return empty optional for missing user")
        void testFindUserNameMissing() {
            Map<Integer, String> users = Map.of(1, "Alice");
            assertThat(Java8Features.findUserName(users, 99)).isEmpty();
        }

        @Test
        @DisplayName("Should return default when user not found")
        void testGetUserNameOrDefault() {
            Map<Integer, String> users = Map.of(1, "Alice");
            assertThat(Java8Features.getUserNameOrDefault(users, 99, "Unknown"))
                    .isEqualTo("Unknown");
        }

        @Test
        @DisplayName("Should find and transform user name")
        void testFindAndTransform() {
            Map<Integer, String> users = Map.of(1, "Alice", 2, "Bo");
            assertThat(Java8Features.findAndTransform(users, 1)).hasValue("ALICE");
            assertThat(Java8Features.findAndTransform(users, 2)).isEmpty(); // "Bo" too short
        }

        @Test
        @DisplayName("Should chain optional with flatMap for address lookup")
        void testFindAddress() {
            Map<Integer, String> users = Map.of(1, "Alice");
            Map<String, String> addresses = Map.of("Alice", "123 Main St");
            assertThat(Java8Features.findAddress(users, addresses, 1)).hasValue("123 Main St");
        }

        @Test
        @DisplayName("Should return empty when address chain breaks")
        void testFindAddressChainBreaks() {
            Map<Integer, String> users = Map.of(1, "Alice");
            Map<String, String> addresses = Map.of("Bob", "456 Elm St");
            assertThat(Java8Features.findAddress(users, addresses, 1)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Date and Time API")
    class DateTimeApiTests {

        @Test
        @DisplayName("Should return today's date")
        void testToday() {
            assertThat(Java8Features.today()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("Should create specific date")
        void testCreateDate() {
            LocalDate date = Java8Features.createDate(2024, 3, 15);
            assertThat(date.getYear()).isEqualTo(2024);
            assertThat(date.getMonthValue()).isEqualTo(3);
            assertThat(date.getDayOfMonth()).isEqualTo(15);
        }

        @Test
        @DisplayName("Should create specific time")
        void testCreateTime() {
            LocalTime time = Java8Features.createTime(14, 30);
            assertThat(time.getHour()).isEqualTo(14);
            assertThat(time.getMinute()).isEqualTo(30);
        }

        @Test
        @DisplayName("Should create date-time")
        void testCreateDateTime() {
            LocalDateTime dt = Java8Features.createDateTime(2024, 6, 15, 10, 30);
            assertThat(dt.getYear()).isEqualTo(2024);
            assertThat(dt.getHour()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should calculate days between dates")
        void testDaysBetween() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 1, 31);
            assertThat(Java8Features.daysBetween(start, end)).isEqualTo(30);
        }

        @Test
        @DisplayName("Should calculate days between same date as zero")
        void testDaysBetweenSameDate() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            assertThat(Java8Features.daysBetween(date, date)).isZero();
        }

        @Test
        @DisplayName("Should calculate period between dates")
        void testPeriodBetween() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 3, 15);
            Period period = Java8Features.periodBetween(start, end);
            assertThat(period.getMonths()).isEqualTo(2);
            assertThat(period.getDays()).isEqualTo(14);
        }

        @Test
        @DisplayName("Should format date with pattern")
        void testFormatDate() {
            LocalDate date = LocalDate.of(2024, 3, 15);
            assertThat(Java8Features.formatDate(date, "dd/MM/yyyy")).isEqualTo("15/03/2024");
        }

        @Test
        @DisplayName("Should parse date from string")
        void testParseDate() {
            LocalDate date = Java8Features.parseDate("15/03/2024", "dd/MM/yyyy");
            assertThat(date).isEqualTo(LocalDate.of(2024, 3, 15));
        }

        @Test
        @DisplayName("Should throw exception for invalid date string")
        void testParseDateInvalid() {
            assertThatThrownBy(() -> Java8Features.parseDate("invalid", "dd/MM/yyyy"))
                    .isInstanceOf(DateTimeParseException.class);
        }

        @Test
        @DisplayName("Should convert timezone")
        void testConvertTimezone() {
            ZonedDateTime nyTime = ZonedDateTime.of(
                    2024, 3, 15, 10, 0, 0, 0, ZoneId.of("America/New_York"));
            ZonedDateTime londonTime = Java8Features.convertTimezone(nyTime, ZoneId.of("Europe/London"));
            assertThat(londonTime.getHour()).isNotEqualTo(nyTime.getHour());
            assertThat(londonTime.getZone()).isEqualTo(ZoneId.of("Europe/London"));
        }

        @Test
        @DisplayName("Should convert LocalDateTime to Instant")
        void testToInstant() {
            LocalDateTime ldt = LocalDateTime.of(2024, 1, 1, 12, 0);
            Instant instant = Java8Features.toInstant(ldt, ZoneId.of("UTC"));
            assertThat(instant).isNotNull();
            assertThat(instant.toString()).contains("2024-01-01T12:00:00Z");
        }
    }
}
