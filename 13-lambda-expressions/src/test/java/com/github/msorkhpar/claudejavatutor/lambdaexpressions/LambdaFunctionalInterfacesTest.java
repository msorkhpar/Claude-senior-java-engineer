package com.github.msorkhpar.claudejavatutor.lambdaexpressions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Lambda Functional Interfaces Tests")
class LambdaFunctionalInterfacesTest {

    @Nested
    @DisplayName("Standard Interfaces")
    class StandardInterfacesTest {

        @Test
        @DisplayName("Should demonstrate consumer with andThen composition")
        void testDemonstrateConsumer() {
            var standard = new LambdaFunctionalInterfaces.StandardInterfaces();
            List<String> items = Arrays.asList("hello", "world");

            // Capture output
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));

            standard.demonstrateConsumer(items);

            System.setOut(System.out);

            String output = outContent.toString();
            assertThat(output)
                    .contains("hello")
                    .contains("world")
                    .contains("Length: 5");
        }

        @Test
        @DisplayName("Should generate random IDs")
        void testGenerateRandomIds() {
            var standard = new LambdaFunctionalInterfaces.StandardInterfaces();

            List<String> ids = standard.generateRandomIds(5);

            assertThat(ids)
                    .hasSize(5)
                    .allMatch(id -> id.startsWith("ID-"))
                    .doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("Should map strings to lengths")
        void testMapToLengths() {
            var standard = new LambdaFunctionalInterfaces.StandardInterfaces();
            List<String> strings = Arrays.asList("a", "ab", "abc");

            Map<String, Integer> result = standard.mapToLengths(strings);

            assertThat(result)
                    .containsEntry("a", 1)
                    .containsEntry("ab", 2)
                    .containsEntry("abc", 3);
        }

        @Test
        @DisplayName("Should filter strings with composed predicates")
        void testFilterStrings() {
            var standard = new LambdaFunctionalInterfaces.StandardInterfaces();
            List<String> strings = Arrays.asList(null, "", "hi", "hello", "world");

            List<String> result = standard.filterStrings(strings);

            assertThat(result)
                    .containsExactly("hello", "world")
                    .doesNotContain(null, "", "hi");
        }

        @Test
        @DisplayName("Should combine two lists of strings")
        void testCombineStrings() {
            var standard = new LambdaFunctionalInterfaces.StandardInterfaces();
            List<String> first = Arrays.asList("a", "b", "c");
            List<String> second = Arrays.asList("1", "2", "3");

            List<String> result = standard.combineStrings(first, second);

            assertThat(result).containsExactly("a-1", "b-2", "c-3");
        }

        @Test
        @DisplayName("Should combine strings with mismatched sizes")
        void testCombineStringsUnequalLists() {
            var standard = new LambdaFunctionalInterfaces.StandardInterfaces();
            List<String> first = Arrays.asList("a", "b");
            List<String> second = Arrays.asList("1", "2", "3");

            List<String> result = standard.combineStrings(first, second);

            assertThat(result).containsExactly("a-1", "b-2");
        }

        @Test
        @DisplayName("Should transform strings with chained operations")
        void testTransformStrings() {
            var standard = new LambdaFunctionalInterfaces.StandardInterfaces();
            List<String> strings = Arrays.asList("  HELLO  ", "  WORLD  ");

            List<String> result = standard.transformStrings(strings);

            assertThat(result).containsExactly("processed_hello", "processed_world");
        }

        @Test
        @DisplayName("Should find maximum number")
        void testFindMax() {
            var standard = new LambdaFunctionalInterfaces.StandardInterfaces();
            List<Integer> numbers = Arrays.asList(3, 7, 2, 9, 1);

            Optional<Integer> result = standard.findMax(numbers);

            assertThat(result).hasValue(9);
        }

        @Test
        @DisplayName("Should return empty optional for empty list")
        void testFindMaxEmptyList() {
            var standard = new LambdaFunctionalInterfaces.StandardInterfaces();
            List<Integer> numbers = Collections.emptyList();

            Optional<Integer> result = standard.findMax(numbers);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should concatenate all strings")
        void testConcatenateAll() {
            var standard = new LambdaFunctionalInterfaces.StandardInterfaces();
            List<String> strings = Arrays.asList("a", "b", "c");

            String result = standard.concatenateAll(strings);

            assertThat(result).isEqualTo("a, b, c");
        }

        @Test
        @DisplayName("Should return empty string for empty list")
        void testConcatenateAllEmptyList() {
            var standard = new LambdaFunctionalInterfaces.StandardInterfaces();
            List<String> strings = Collections.emptyList();

            String result = standard.concatenateAll(strings);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Primitive Specializations")
    class PrimitiveSpecializationsTest {

        @Test
        @DisplayName("Should filter even numbers")
        void testFilterEvenNumbers() {
            var primitives = new LambdaFunctionalInterfaces.PrimitiveSpecializations();
            List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);

            List<Integer> result = primitives.filterEvenNumbers(numbers);

            assertThat(result).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("Should format numbers with padding")
        void testFormatNumbers() {
            var primitives = new LambdaFunctionalInterfaces.PrimitiveSpecializations();
            List<Integer> numbers = Arrays.asList(1, 42, 999);

            List<String> result = primitives.formatNumbers(numbers);

            assertThat(result).containsExactly("00001", "00042", "00999");
        }

        @Test
        @DisplayName("Should sum string lengths")
        void testSumStringLengths() {
            var primitives = new LambdaFunctionalInterfaces.PrimitiveSpecializations();
            List<String> strings = Arrays.asList("a", "ab", "abc");

            int result = primitives.sumStringLengths(strings);

            assertThat(result).isEqualTo(6);
        }

        @Test
        @DisplayName("Should print numbers with IntConsumer")
        void testPrintNumbers() {
            var primitives = new LambdaFunctionalInterfaces.PrimitiveSpecializations();
            List<Integer> numbers = Arrays.asList(1, 2, 3);

            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));

            primitives.printNumbers(numbers);

            System.setOut(System.out);

            String output = outContent.toString();
            assertThat(output)
                    .contains("Number: 1")
                    .contains("Number: 2")
                    .contains("Number: 3");
        }

        @Test
        @DisplayName("Should generate random numbers")
        void testGenerateRandomNumbers() {
            var primitives = new LambdaFunctionalInterfaces.PrimitiveSpecializations();

            List<Integer> result = primitives.generateRandomNumbers(10);

            assertThat(result)
                    .hasSize(10)
                    .allMatch(n -> n >= 0 && n < 100);
        }

        @Test
        @DisplayName("Should calculate product of numbers")
        void testCalculateProduct() {
            var primitives = new LambdaFunctionalInterfaces.PrimitiveSpecializations();
            List<Integer> numbers = Arrays.asList(2, 3, 4);

            int result = primitives.calculateProduct(numbers);

            assertThat(result).isEqualTo(24);
        }

        @Test
        @DisplayName("Should return 1 for empty list product")
        void testCalculateProductEmptyList() {
            var primitives = new LambdaFunctionalInterfaces.PrimitiveSpecializations();
            List<Integer> numbers = Collections.emptyList();

            int result = primitives.calculateProduct(numbers);

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("Should double all numbers")
        void testDoubleAllNumbers() {
            var primitives = new LambdaFunctionalInterfaces.PrimitiveSpecializations();
            List<Integer> numbers = Arrays.asList(1, 2, 3);

            List<Integer> result = primitives.doubleAllNumbers(numbers);

            assertThat(result).containsExactly(2, 4, 6);
        }
    }

    @Nested
    @DisplayName("Custom Functional Interfaces")
    class CustomFunctionalInterfacesTest {

        @Test
        @DisplayName("Should validate users with custom validator")
        void testValidateUsers() {
            var custom = new LambdaFunctionalInterfaces.CustomFunctionalInterfaces();
            List<LambdaFunctionalInterfaces.CustomFunctionalInterfaces.User> users = Arrays.asList(
                    new LambdaFunctionalInterfaces.CustomFunctionalInterfaces.User("Alice", 25, "alice@example.com"),
                    new LambdaFunctionalInterfaces.CustomFunctionalInterfaces.User("Bob", 17, "bob@example.com"),
                    new LambdaFunctionalInterfaces.CustomFunctionalInterfaces.User("Charlie", 30, null),
                    new LambdaFunctionalInterfaces.CustomFunctionalInterfaces.User("", 20, "test@example.com")
            );

            List<LambdaFunctionalInterfaces.CustomFunctionalInterfaces.User> result = custom.validateUsers(users);

            assertThat(result)
                    .hasSize(1)
                    .extracting(LambdaFunctionalInterfaces.CustomFunctionalInterfaces.User::name)
                    .containsExactly("Alice");
        }

        @Test
        @DisplayName("Should transform with context")
        void testTransformWithContext() {
            var custom = new LambdaFunctionalInterfaces.CustomFunctionalInterfaces();
            List<Integer> numbers = Arrays.asList(1, 2, 3);

            List<String> result = custom.transformWithContext(numbers, "Item-");

            assertThat(result).containsExactly("Item-1", "Item-2", "Item-3");
        }

        @Test
        @DisplayName("Should parse with exception handling using lift")
        void testParseWithExceptionHandling() {
            var custom = new LambdaFunctionalInterfaces.CustomFunctionalInterfaces();
            List<String> strings = Arrays.asList("1", "abc", "3", "xyz", "5");

            List<Integer> result = custom.parseWithExceptionHandling(strings);

            assertThat(result).containsExactly(1, 3, 5);
        }

        @Test
        @DisplayName("Should return empty list for all invalid parses")
        void testParseWithExceptionHandlingAllInvalid() {
            var custom = new LambdaFunctionalInterfaces.CustomFunctionalInterfaces();
            List<String> strings = Arrays.asList("abc", "xyz", "invalid");

            List<Integer> result = custom.parseWithExceptionHandling(strings);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should calculate final price with tax")
        void testCalculateFinalPrice() {
            var custom = new LambdaFunctionalInterfaces.CustomFunctionalInterfaces();

            double result = custom.calculateFinalPrice(100.0, 2, 0.1);

            // 100 * 2 * (1 - 0.1) * (1 + 0.08) = 180 * 1.08 = 194.4
            assertThat(result).isEqualTo(194.4, within(0.001));
        }
    }

    @Nested
    @DisplayName("Composition")
    class CompositionTest {

        @Test
        @DisplayName("Should demonstrate function composition")
        void testDemonstrateFunctionComposition() {
            var composition = new LambdaFunctionalInterfaces.Composition();
            List<Integer> numbers = Arrays.asList(2, 3);

            List<String> result = composition.demonstrateFunctionComposition(numbers);

            assertThat(result).containsExactly("Result: 4!", "Result: 9!");
        }

        @Test
        @DisplayName("Should demonstrate predicate composition")
        void testDemonstratePredicateComposition() {
            var composition = new LambdaFunctionalInterfaces.Composition();
            List<String> strings = Arrays.asList(null, "", "Apple", "Short", "Banana", "A");

            List<String> result = composition.demonstratePredicateComposition(strings);

            // Valid: not null, not empty, and (starts with A OR longer than 5)
            // "A" is valid because it starts with "A"
            assertThat(result).containsExactly("Apple", "Banana", "A");
        }

        @Test
        @DisplayName("Should demonstrate consumer composition")
        void testDemonstrateConsumerComposition() {
            var composition = new LambdaFunctionalInterfaces.Composition();
            List<String> items = Arrays.asList("test");

            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(outContent));

            composition.demonstrateConsumerComposition(items);

            System.setOut(originalOut);
            System.setErr(originalErr);

            String output = outContent.toString();
            assertThat(output)
                    .contains("test")
                    .contains("LOG: test")
                    .contains("Length: 4");
        }

        @Test
        @DisplayName("Should sort with composition")
        void testSortWithComposition() {
            var composition = new LambdaFunctionalInterfaces.Composition();
            List<LambdaFunctionalInterfaces.Composition.Person> people = Arrays.asList(
                    new LambdaFunctionalInterfaces.Composition.Person("Charlie", 30, "HR"),
                    new LambdaFunctionalInterfaces.Composition.Person("Alice", 25, "Engineering"),
                    new LambdaFunctionalInterfaces.Composition.Person("Bob", 25, "Engineering"),
                    new LambdaFunctionalInterfaces.Composition.Person("David", 35, "Engineering")
            );

            List<LambdaFunctionalInterfaces.Composition.Person> result = composition.sortWithComposition(people);

            // Sorted by: department, then age, then name
            // Engineering comes before HR alphabetically
            assertThat(result)
                    .extracting(LambdaFunctionalInterfaces.Composition.Person::name)
                    .containsExactly("Alice", "Bob", "David", "Charlie");
        }

        @Test
        @DisplayName("Should process with pipeline")
        void testProcessWithPipeline() {
            var composition = new LambdaFunctionalInterfaces.Composition();
            List<String> strings = Arrays.asList(
                    "  HELLO WORLD  ",
                    "This is a very long string that should be truncated"
            );

            List<String> result = composition.processWithPipeline(strings);

            assertThat(result).containsExactly(
                    "hello_world",
                    "this_is_a_very_long_"
            );
        }
    }

    @Nested
    @DisplayName("Advanced Patterns")
    class AdvancedPatternsTest {

        @Test
        @DisplayName("Should use currying")
        void testUseCurrying() {
            var advanced = new LambdaFunctionalInterfaces.AdvancedPatterns();

            int result = advanced.useCurrying();

            assertThat(result).isEqualTo(8);
        }

        @Test
        @DisplayName("Should curry add function")
        void testCurriedAdd() {
            var advanced = new LambdaFunctionalInterfaces.AdvancedPatterns();

            var add = advanced.curriedAdd();
            var add10 = add.apply(10);

            assertThat(add10.apply(5)).isEqualTo(15);
            assertThat(add10.apply(20)).isEqualTo(30);
        }

        @Test
        @DisplayName("Should apply multiplier with partial application")
        void testApplyMultiplier() {
            var advanced = new LambdaFunctionalInterfaces.AdvancedPatterns();
            List<Integer> numbers = Arrays.asList(2, 3, 4);

            List<Integer> result = advanced.applyMultiplier(numbers, 5);

            assertThat(result).containsExactly(10, 15, 20);
        }

        @Test
        @DisplayName("Should create multiplier and reuse it")
        void testCreateMultiplier() {
            var advanced = new LambdaFunctionalInterfaces.AdvancedPatterns();

            var triple = advanced.createMultiplier(3);

            assertThat(triple.apply(5)).isEqualTo(15);
            assertThat(triple.apply(10)).isEqualTo(30);
        }

        @Test
        @DisplayName("Should memoize factorial computation")
        void testCreateMemoizedFactorial() {
            var advanced = new LambdaFunctionalInterfaces.AdvancedPatterns();

            var factorial = advanced.createMemoizedFactorial();

            // First computation
            int result1 = factorial.apply(5);
            assertThat(result1).isEqualTo(120);

            // Should return cached value
            int result2 = factorial.apply(5);
            assertThat(result2).isEqualTo(120);

            // Different input
            int result3 = factorial.apply(6);
            assertThat(result3).isEqualTo(720);
        }

        @Test
        @DisplayName("Should demonstrate lazy evaluation")
        void testDemonstrateLazyEvaluation() {
            var advanced = new LambdaFunctionalInterfaces.AdvancedPatterns();

            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));

            String result = advanced.demonstrateLazyEvaluation();

            System.setOut(System.out);

            assertThat(result).isEqualTo("Expensive Result");

            String output = outContent.toString();
            // "Computing expensive value..." should appear once
            assertThat(output)
                    .contains("Lazy value created")
                    .contains("Computing expensive value...");
        }

        @Test
        @DisplayName("Should cache lazy value after first access")
        void testLazyValueCaching() {
            var advanced = new LambdaFunctionalInterfaces.AdvancedPatterns();

            var lazy = LambdaFunctionalInterfaces.AdvancedPatterns.Lazy.of(() -> "Computed");

            String result1 = lazy.get();
            String result2 = lazy.get();

            assertThat(result1).isEqualTo("Computed");
            assertThat(result2).isEqualTo("Computed");
            assertThat(result1).isSameAs(result2); // Same instance
        }
    }
}
