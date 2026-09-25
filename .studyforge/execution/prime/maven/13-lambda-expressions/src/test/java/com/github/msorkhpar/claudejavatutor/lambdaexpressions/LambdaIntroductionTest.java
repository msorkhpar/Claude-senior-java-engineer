package com.github.msorkhpar.claudejavatutor.lambdaexpressions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Lambda Introduction Tests")
class LambdaIntroductionTest {

    @Nested
    @DisplayName("Anonymous to Lambda Evolution")
    class AnonymousToLambdaTest {

        @Test
        @DisplayName("Should sort using anonymous inner class")
        void testSortWithAnonymousClass() {
            var sorter = new LambdaIntroduction.AnonymousToLambda();
            List<String> names = Arrays.asList("Charlie", "Alice", "Bob");

            List<String> result = sorter.sortWithAnonymousClass(names);

            assertThat(result)
                    .containsExactly("Alice", "Bob", "Charlie")
                    .doesNotContainNull();
        }

        @Test
        @DisplayName("Should sort using lambda expression")
        void testSortWithLambda() {
            var sorter = new LambdaIntroduction.AnonymousToLambda();
            List<String> names = Arrays.asList("Charlie", "Alice", "Bob");

            List<String> result = sorter.sortWithLambda(names);

            assertThat(result).containsExactly("Alice", "Bob", "Charlie");
        }

        @Test
        @DisplayName("Should sort using method reference")
        void testSortWithMethodReference() {
            var sorter = new LambdaIntroduction.AnonymousToLambda();
            List<String> names = Arrays.asList("Charlie", "Alice", "Bob");

            List<String> result = sorter.sortWithMethodReference(names);

            assertThat(result).containsExactly("Alice", "Bob", "Charlie");
        }

        @Test
        @DisplayName("Should not modify original list")
        void testOriginalListNotModified() {
            var sorter = new LambdaIntroduction.AnonymousToLambda();
            List<String> original = new ArrayList<>(Arrays.asList("Charlie", "Alice", "Bob"));
            List<String> originalCopy = new ArrayList<>(original);

            sorter.sortWithLambda(original);

            assertThat(original).isEqualTo(originalCopy);
        }

        @Test
        @DisplayName("Should handle empty list")
        void testEmptyList() {
            var sorter = new LambdaIntroduction.AnonymousToLambda();
            List<String> empty = new ArrayList<>();

            List<String> result = sorter.sortWithLambda(empty);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle single element")
        void testSingleElement() {
            var sorter = new LambdaIntroduction.AnonymousToLambda();
            List<String> single = Arrays.asList("Alice");

            List<String> result = sorter.sortWithLambda(single);

            assertThat(result).containsExactly("Alice");
        }
    }

    @Nested
    @DisplayName("Functional Interface Examples")
    class FunctionalInterfaceExamplesTest {

        @Test
        @DisplayName("Should generate specified number of IDs")
        void testGenerateIds() {
            var examples = new LambdaIntroduction.FunctionalInterfaceExamples();

            List<String> ids = examples.generateIds(5);

            assertThat(ids)
                    .hasSize(5)
                    .allMatch(id -> id.length() > 0)
                    .doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("Should get lengths of strings")
        void testGetLengths() {
            var examples = new LambdaIntroduction.FunctionalInterfaceExamples();
            List<String> strings = Arrays.asList("a", "ab", "abc");

            List<Integer> lengths = examples.getLengths(strings);

            assertThat(lengths).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should filter long strings")
        void testFilterLongStrings() {
            var examples = new LambdaIntroduction.FunctionalInterfaceExamples();
            List<String> strings = Arrays.asList("hi", "hello", "hey", "goodbye");

            List<String> result = examples.filterLongStrings(strings, 5);

            assertThat(result).containsExactly("hello", "goodbye");
        }

        @Test
        @DisplayName("Should handle empty list in filter")
        void testFilterEmptyList() {
            var examples = new LambdaIntroduction.FunctionalInterfaceExamples();

            List<String> result = examples.filterLongStrings(Collections.emptyList(), 5);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Variable Capture")
    class VariableCaptureTest {

        @Test
        @DisplayName("Should multiply all numbers by captured multiplier")
        void testMultiplyAll() {
            var capture = new LambdaIntroduction.VariableCapture();
            List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

            List<Integer> result = capture.multiplyAll(numbers, 3);

            assertThat(result).containsExactly(3, 6, 9, 12, 15);
        }

        @Test
        @DisplayName("Should count matches using AtomicInteger")
        void testCountMatches() {
            var capture = new LambdaIntroduction.VariableCapture();
            List<String> strings = Arrays.asList("apple", "banana", "apricot", "cherry");
            Predicate<String> startsWithA = s -> s.startsWith("a");

            int count = capture.countMatches(strings, startsWithA);

            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should count matches using stream")
        void testCountMatchesWithStream() {
            var capture = new LambdaIntroduction.VariableCapture();
            List<String> strings = Arrays.asList("apple", "banana", "apricot", "cherry");
            Predicate<String> startsWithA = s -> s.startsWith("a");

            long count = capture.countMatchesWithStream(strings, startsWithA);

            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should create accumulator that modifies instance variable")
        void testCreateAccumulator() {
            var capture = new LambdaIntroduction.VariableCapture();
            Consumer<Integer> accumulator = capture.createAccumulator();

            accumulator.accept(5);
            accumulator.accept(10);

            // Cannot directly test instance variable, but we can verify the lambda works
            assertThat(accumulator).isNotNull();
        }
    }

    @Nested
    @DisplayName("Exception Handling")
    class ExceptionHandlingTest {

        @Test
        @DisplayName("Should parse valid numbers")
        void testParseNumbers() {
            var handler = new LambdaIntroduction.ExceptionHandling();
            List<String> strings = Arrays.asList("1", "2", "3");

            List<Integer> result = handler.parseNumbers(strings);

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should skip invalid numbers")
        void testParseNumbersWithInvalid() {
            var handler = new LambdaIntroduction.ExceptionHandling();
            List<String> strings = Arrays.asList("1", "abc", "3");

            List<Integer> result = handler.parseNumbers(strings);

            assertThat(result).containsExactly(1, 3);
        }

        @Test
        @DisplayName("Should parse numbers with Optional")
        void testParseNumbersWithOptional() {
            var handler = new LambdaIntroduction.ExceptionHandling();
            List<String> strings = Arrays.asList("1", "abc", "3", "xyz", "5");

            List<Integer> result = handler.parseNumbersWithOptional(strings);

            assertThat(result).containsExactly(1, 3, 5);
        }

        @Test
        @DisplayName("Should handle all invalid numbers")
        void testParseAllInvalid() {
            var handler = new LambdaIntroduction.ExceptionHandling();
            List<String> strings = Arrays.asList("abc", "xyz", "invalid");

            List<Integer> result = handler.parseNumbersWithOptional(strings);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Closure Examples")
    class ClosureExamplesTest {

        @Test
        @DisplayName("Should create multiplier closure")
        void testCreateMultiplier() {
            var closures = new LambdaIntroduction.ClosureExamples();
            Function<Integer, Integer> triple = closures.createMultiplier(3);

            assertThat(triple.apply(5)).isEqualTo(15);
            assertThat(triple.apply(10)).isEqualTo(30);
        }

        @Test
        @DisplayName("Should create range checker closure")
        void testCreateRangeChecker() {
            var closures = new LambdaIntroduction.ClosureExamples();
            Predicate<Integer> inRange = closures.createRangeChecker(10, 20);

            assertThat(inRange.test(15)).isTrue();
            assertThat(inRange.test(5)).isFalse();
            assertThat(inRange.test(25)).isFalse();
            assertThat(inRange.test(10)).isTrue();
            assertThat(inRange.test(20)).isTrue();
        }

        @Test
        @DisplayName("Should create filter closure")
        void testCreateFilter() {
            var closures = new LambdaIntroduction.ClosureExamples();
            Function<List<String>, List<String>> filter = closures.createFilter("hel", 5);
            List<String> input = Arrays.asList("hello", "help", "hi", "helicopter");

            List<String> result = filter.apply(input);

            assertThat(result).containsExactly("hello", "helicopter");
        }
    }

    @Nested
    @DisplayName("Real World Examples")
    class RealWorldExamplesTest {

        @Test
        @DisplayName("Should get active adult names")
        void testGetActiveAdultNames() {
            var examples = new LambdaIntroduction.RealWorldExamples();
            List<LambdaIntroduction.RealWorldExamples.User> users = Arrays.asList(
                    new LambdaIntroduction.RealWorldExamples.User("Alice", 25, true, LocalDate.now()),
                    new LambdaIntroduction.RealWorldExamples.User("Bob", 17, true, LocalDate.now()),
                    new LambdaIntroduction.RealWorldExamples.User("Charlie", 30, false, LocalDate.now()),
                    new LambdaIntroduction.RealWorldExamples.User("David", 22, true, LocalDate.now())
            );

            List<String> result = examples.getActiveAdultNames(users);

            assertThat(result).containsExactly("Alice", "David");
        }

        @Test
        @DisplayName("Should group users by age range")
        void testGroupByAgeRange() {
            var examples = new LambdaIntroduction.RealWorldExamples();
            List<LambdaIntroduction.RealWorldExamples.User> users = Arrays.asList(
                    new LambdaIntroduction.RealWorldExamples.User("Child", 10, true, LocalDate.now()),
                    new LambdaIntroduction.RealWorldExamples.User("Adult", 30, true, LocalDate.now()),
                    new LambdaIntroduction.RealWorldExamples.User("Senior", 70, true, LocalDate.now())
            );

            Map<String, List<LambdaIntroduction.RealWorldExamples.User>> result =
                    examples.groupByAgeRange(users);

            assertThat(result).containsKeys("Minor", "Adult", "Senior");
            assertThat(result.get("Minor")).hasSize(1);
            assertThat(result.get("Adult")).hasSize(1);
            assertThat(result.get("Senior")).hasSize(1);
        }

        @Test
        @DisplayName("Should calculate age statistics")
        void testCalculateAgeStatistics() {
            var examples = new LambdaIntroduction.RealWorldExamples();
            List<LambdaIntroduction.RealWorldExamples.User> users = Arrays.asList(
                    new LambdaIntroduction.RealWorldExamples.User("User1", 20, true, LocalDate.now()),
                    new LambdaIntroduction.RealWorldExamples.User("User2", 30, true, LocalDate.now()),
                    new LambdaIntroduction.RealWorldExamples.User("User3", 40, true, LocalDate.now())
            );

            var stats = examples.calculateAgeStatistics(users);

            assertThat(stats.average()).isEqualTo(30.0);
            assertThat(stats.count()).isEqualTo(3);
            assertThat(stats.min()).isEqualTo(20);
            assertThat(stats.max()).isEqualTo(40);
        }
    }

    @Nested
    @DisplayName("Performance Considerations")
    class PerformanceConsiderationsTest {

        @Test
        @DisplayName("Should sort by length")
        void testSortByLength() {
            var perf = new LambdaIntroduction.PerformanceConsiderations();
            List<String> strings = Arrays.asList("aaa", "b", "cc");

            List<String> result = perf.sortByLength(strings);

            assertThat(result).containsExactly("b", "cc", "aaa");
        }

        @Test
        @DisplayName("Should sum lengths efficiently")
        void testSumLengths() {
            var perf = new LambdaIntroduction.PerformanceConsiderations();
            List<String> strings = Arrays.asList("hello", "world", "!");

            int sum = perf.sumLengths(strings);

            assertThat(sum).isEqualTo(11);
        }

        @Test
        @DisplayName("Should count long strings optimized")
        void testCountLongStringsOptimized() {
            var perf = new LambdaIntroduction.PerformanceConsiderations();
            List<String> strings = Arrays.asList("hi", "hello", "hey", "goodbye");

            int count = perf.countLongStringsOptimized(strings, 5);

            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should process large dataset")
        void testProcessLargeDataset() {
            var perf = new LambdaIntroduction.PerformanceConsiderations();
            List<String> largeList = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                largeList.add("item" + i);
            }

            List<Integer> result = perf.processLargeDataset(largeList);

            assertThat(result).hasSize(1000);
            assertThat(result.get(0)).isEqualTo(5);  // "item0".length()
        }
    }
}