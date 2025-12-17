package com.github.msorkhpar.claudejavatutor.lambdaexpressions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Lambda Best Practices Tests")
class LambdaBestPracticesTest {

    @Nested
    @DisplayName("Lambda Extraction")
    class LambdaExtractionTest {

        @Test
        @DisplayName("Should get eligible orders - bad approach works but is unreadable")
        void testGetEligibleOrdersBad() {
            var extraction = new LambdaBestPractices.LambdaExtraction();
            List<LambdaBestPractices.LambdaExtraction.Order> orders = createTestOrders();

            List<LambdaBestPractices.LambdaExtraction.Order> result = extraction.getEligibleOrdersBad(orders);

            assertThat(result)
                    .hasSize(1)
                    .extracting(LambdaBestPractices.LambdaExtraction.Order::id)
                    .containsExactly("ORDER1");
        }

        @Test
        @DisplayName("Should get eligible orders - good approach is more readable")
        void testGetEligibleOrdersGood() {
            var extraction = new LambdaBestPractices.LambdaExtraction();
            List<LambdaBestPractices.LambdaExtraction.Order> orders = createTestOrders();

            List<LambdaBestPractices.LambdaExtraction.Order> result = extraction.getEligibleOrdersGood(orders);

            assertThat(result)
                    .hasSize(1)
                    .extracting(LambdaBestPractices.LambdaExtraction.Order::id)
                    .containsExactly("ORDER1");
        }

        @Test
        @DisplayName("Both approaches should produce same results")
        void testBothApproachesProduceSameResults() {
            var extraction = new LambdaBestPractices.LambdaExtraction();
            List<LambdaBestPractices.LambdaExtraction.Order> orders = createTestOrders();

            List<LambdaBestPractices.LambdaExtraction.Order> bad = extraction.getEligibleOrdersBad(orders);
            List<LambdaBestPractices.LambdaExtraction.Order> good = extraction.getEligibleOrdersGood(orders);

            assertThat(bad).isEqualTo(good);
        }

        @Test
        @DisplayName("Should filter with constants")
        void testFilterWithConstants() {
            var extraction = new LambdaBestPractices.LambdaExtraction();
            List<LambdaBestPractices.LambdaExtraction.Order> orders = createTestOrders();

            List<LambdaBestPractices.LambdaExtraction.Order> result = extraction.filterWithConstants(orders);

            assertThat(result)
                    .hasSize(1)
                    .extracting(LambdaBestPractices.LambdaExtraction.Order::id)
                    .containsExactly("ORDER1");
        }

        private List<LambdaBestPractices.LambdaExtraction.Order> createTestOrders() {
            var premium = new LambdaBestPractices.LambdaExtraction.Customer("Premium", true);
            var regular = new LambdaBestPractices.LambdaExtraction.Customer("Regular", false);
            var urgentItem = new LambdaBestPractices.LambdaExtraction.Item("Urgent", 10.0, true);
            var normalItem = new LambdaBestPractices.LambdaExtraction.Item("Normal", 10.0, false);

            return Arrays.asList(
                    new LambdaBestPractices.LambdaExtraction.Order(
                            "ORDER1",
                            150.0,
                            LambdaBestPractices.LambdaExtraction.Order.Status.CONFIRMED,
                            LocalDate.now().minusDays(10),
                            premium,
                            List.of(urgentItem)
                    ),
                    new LambdaBestPractices.LambdaExtraction.Order(
                            "ORDER2",
                            50.0,  // Too low
                            LambdaBestPractices.LambdaExtraction.Order.Status.CONFIRMED,
                            LocalDate.now().minusDays(10),
                            premium,
                            List.of(normalItem)
                    ),
                    new LambdaBestPractices.LambdaExtraction.Order(
                            "ORDER3",
                            150.0,
                            LambdaBestPractices.LambdaExtraction.Order.Status.DELIVERED,  // Wrong status
                            LocalDate.now().minusDays(10),
                            premium,
                            List.of(urgentItem)
                    )
            );
        }
    }

    @Nested
    @DisplayName("Formatting And Readability")
    class FormattingAndReadabilityTest {

        @Test
        @DisplayName("Should process strings with proper formatting")
        void testProcessStrings() {
            var formatting = new LambdaBestPractices.FormattingAndReadability();
            List<String> strings = Arrays.asList(null, "  HELLO  ", "", "  WORLD  ", "  hello  ");

            List<String> result = formatting.processStrings(strings);

            assertThat(result).containsExactly("hello", "world");
        }

        @Test
        @DisplayName("Should group by first letter")
        void testGroupByFirstLetter() {
            var formatting = new LambdaBestPractices.FormattingAndReadability();
            List<String> words = Arrays.asList("apple", "banana", "apricot", "cherry", "blueberry");

            Map<String, List<String>> result = formatting.groupByFirstLetter(words);

            assertThat(result)
                    .containsKey("a")
                    .containsKey("b")
                    .containsKey("c");
            assertThat(result.get("a")).containsExactly("apple", "apricot");
            assertThat(result.get("b")).containsExactly("banana", "blueberry");
        }

        @Test
        @DisplayName("Should find discount eligible products - step by step approach")
        void testFindDiscountEligible() {
            var formatting = new LambdaBestPractices.FormattingAndReadability();
            List<LambdaBestPractices.FormattingAndReadability.Product> products = createTestProducts();

            List<LambdaBestPractices.FormattingAndReadability.Product> result = formatting.findDiscountEligible(products);

            assertThat(result)
                    .hasSize(2)
                    .extracting(LambdaBestPractices.FormattingAndReadability.Product::name)
                    .containsExactly("TV", "Laptop");  // Sorted by price descending
        }

        @Test
        @DisplayName("Should find discount eligible products - better approach")
        void testFindDiscountEligibleBetter() {
            var formatting = new LambdaBestPractices.FormattingAndReadability();
            List<LambdaBestPractices.FormattingAndReadability.Product> products = createTestProducts();

            List<LambdaBestPractices.FormattingAndReadability.Product> result = formatting.findDiscountEligibleBetter(products);

            assertThat(result)
                    .hasSize(2)
                    .extracting(LambdaBestPractices.FormattingAndReadability.Product::name)
                    .containsExactly("TV", "Laptop");
        }

        @Test
        @DisplayName("Both approaches should produce same results")
        void testBothApproachesMatch() {
            var formatting = new LambdaBestPractices.FormattingAndReadability();
            List<LambdaBestPractices.FormattingAndReadability.Product> products = createTestProducts();

            List<LambdaBestPractices.FormattingAndReadability.Product> result1 = formatting.findDiscountEligible(products);
            List<LambdaBestPractices.FormattingAndReadability.Product> result2 = formatting.findDiscountEligibleBetter(products);

            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("Should format complex operation")
        void testFormatComplexOperation() {
            var formatting = new LambdaBestPractices.FormattingAndReadability();
            List<Integer> numbers = Arrays.asList(10, 20, 30);

            List<String> result = formatting.formatComplexOperation(numbers);

            assertThat(result).containsExactly("$21.60", "$43.20", "$64.80");
        }

        private List<LambdaBestPractices.FormattingAndReadability.Product> createTestProducts() {
            return Arrays.asList(
                    new LambdaBestPractices.FormattingAndReadability.Product("Laptop", 1000.0, "Electronics", 60),
                    new LambdaBestPractices.FormattingAndReadability.Product("TV", 1500.0, "Electronics", 55),
                    new LambdaBestPractices.FormattingAndReadability.Product("Phone", 500.0, "Electronics", 30),  // Not enough stock
                    new LambdaBestPractices.FormattingAndReadability.Product("Book", 20.0, "Books", 100)  // Wrong category
            );
        }
    }

    @Nested
    @DisplayName("Method References")
    class MethodReferencesTest {

        @Test
        @DisplayName("Method references are often more readable than lambdas")
        void testMethodReferencesUsage() {
            var methodRefs = new LambdaBestPractices.MethodReferences();
            List<String> strings = Arrays.asList("hello", "world");

            // This test just verifies the method doesn't throw
            assertThatCode(() -> methodRefs.demonstrateMethodReferences(strings))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Sometimes lambdas are clearer than method references")
        void testWhenLambdasAreClearer() {
            var methodRefs = new LambdaBestPractices.MethodReferences();
            List<Integer> numbers = Arrays.asList(1, 2, 3);

            assertThatCode(() -> methodRefs.whenLambdasAreClearer(numbers))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Avoiding Side Effects")
    class AvoidingSideEffectsTest {

        @Test
        @DisplayName("Processing with side effects is bad practice")
        void testProcessWithSideEffectsBad() {
            var sideEffects = new LambdaBestPractices.AvoidingSideEffects();
            List<String> strings = Arrays.asList("a", "bb", "ccc");

            List<String> result = sideEffects.processWithSideEffectsBad(strings);

            // Returns filtered (non-empty) strings, not transformed
            assertThat(result).containsExactly("a", "bb", "ccc");
        }

        @Test
        @DisplayName("Processing without side effects is better")
        void testProcessWithoutSideEffects() {
            var sideEffects = new LambdaBestPractices.AvoidingSideEffects();
            List<String> strings = Arrays.asList("a", "bb", "ccc");

            List<String> result = sideEffects.processWithoutSideEffects(strings);

            // Returns filtered strings, not transformed
            assertThat(result).containsExactly("a", "bb", "ccc");
        }

        @Test
        @DisplayName("Counting with side effect counts all items")
        void testCountWithSideEffectBad() {
            var sideEffects = new LambdaBestPractices.AvoidingSideEffects();
            List<String> strings = Arrays.asList("a", "bb", "ccc", "dddd", "eeeee");

            long result = sideEffects.countWithSideEffectBad(strings);

            // Counts all items via side effect
            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("Counting without side effect is reliable")
        void testCountWithoutSideEffect() {
            var sideEffects = new LambdaBestPractices.AvoidingSideEffects();
            List<String> strings = Arrays.asList("a", "bb", "ccc", "dddd", "eeeee");

            long result = sideEffects.countWithoutSideEffect(strings);

            // Returns list size
            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("Logging as side effect is acceptable")
        void testLoggingIsOk() {
            var sideEffects = new LambdaBestPractices.AvoidingSideEffects();
            List<String> strings = Arrays.asList("hello", "world");

            List<String> result = sideEffects.loggingIsOk(strings);

            assertThat(result).containsExactly("HELLO", "WORLD");
        }
    }

    @Nested
    @DisplayName("Balancing Styles")
    class BalancingStylesTest {

        @Test
        @DisplayName("Too imperative approach works but misses functional benefits")
        void testGetAdultNamesTooImperative() {
            var balancing = new LambdaBestPractices.BalancingStyles();
            List<LambdaBestPractices.BalancingStyles.User> users = createTestUsers();

            List<String> result = balancing.getAdultNamesTooImperative(users);

            // Returns names as-is (not uppercased), sorted
            assertThat(result).containsExactly("Alice", "Charlie");
        }

        @Test
        @DisplayName("Too functional approach is hard to read")
        void testGetAdultNamesTooFunctional() {
            var balancing = new LambdaBestPractices.BalancingStyles();
            List<LambdaBestPractices.BalancingStyles.User> users = createTestUsers();

            List<String> result = balancing.getAdultNamesTooFunctional(users);

            // Returns names as-is (not uppercased), sorted
            assertThat(result).containsExactly("Alice", "Charlie");
        }

        @Test
        @DisplayName("Balanced approach is best")
        void testGetAdultNamesBalanced() {
            var balancing = new LambdaBestPractices.BalancingStyles();
            List<LambdaBestPractices.BalancingStyles.User> users = createTestUsers();

            List<String> result = balancing.getAdultNamesBalanced(users);

            // Returns names as-is (not uppercased), sorted
            assertThat(result).containsExactly("Alice", "Charlie");
        }

        @Test
        @DisplayName("All three approaches produce same results")
        void testAllApproachesMatch() {
            var balancing = new LambdaBestPractices.BalancingStyles();
            List<LambdaBestPractices.BalancingStyles.User> users = createTestUsers();

            List<String> imperative = balancing.getAdultNamesTooImperative(users);
            List<String> functional = balancing.getAdultNamesTooFunctional(users);
            List<String> balanced = balancing.getAdultNamesBalanced(users);

            assertThat(imperative).isEqualTo(functional);
            assertThat(functional).isEqualTo(balanced);
        }

        @Test
        @DisplayName("Sometimes loops are clearer than streams")
        void testWhenToUseLoops() {
            var balancing = new LambdaBestPractices.BalancingStyles();
            List<LambdaBestPractices.BalancingStyles.User> users = createTestUsers();

            assertThatCode(() -> balancing.whenToUseLoops(users))
                    .doesNotThrowAnyException();
        }

        private List<LambdaBestPractices.BalancingStyles.User> createTestUsers() {
            return Arrays.asList(
                    new LambdaBestPractices.BalancingStyles.User("Alice", 25, true),
                    new LambdaBestPractices.BalancingStyles.User("Bob", 17, true),  // Too young
                    new LambdaBestPractices.BalancingStyles.User("Charlie", 30, true),
                    new LambdaBestPractices.BalancingStyles.User("David", 22, false)  // Not active
            );
        }
    }

    @Nested
    @DisplayName("Performance Awareness")
    class PerformanceAwarenessTest {

        @Test
        @DisplayName("Should sort multiple lists by length")
        void testSortMultipleLists() {
            var performance = new LambdaBestPractices.PerformanceAwareness();
            List<List<String>> lists = Arrays.asList(
                    Arrays.asList("c", "a", "b"),
                    Arrays.asList("f", "d", "e")
            );

            List<String> result = performance.sortMultipleLists(lists);

            // Sorts by length, then original order
            assertThat(result).containsExactly("c", "a", "b", "f", "d", "e");
        }

        @Test
        @DisplayName("Should calculate average")
        void testCalculateAverage() {
            var performance = new LambdaBestPractices.PerformanceAwareness();
            List<Integer> numbers = Arrays.asList(10, 20, 30, 40, 50);

            double result = performance.calculateAverage(numbers);

            assertThat(result).isEqualTo(30.0);
        }

        @Test
        @DisplayName("Should handle empty list for average")
        void testCalculateAverageEmptyList() {
            var performance = new LambdaBestPractices.PerformanceAwareness();
            List<Integer> numbers = Collections.emptyList();

            double result = performance.calculateAverage(numbers);

            assertThat(result).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should process large dataset efficiently")
        void testProcessLargeDataset() {
            var performance = new LambdaBestPractices.PerformanceAwareness();
            List<String> largeList = IntStream.range(0, 1000)
                    .mapToObj(i -> "item" + i)
                    .collect(Collectors.toList());

            List<Integer> result = performance.processLargeDataset(largeList);

            assertThat(result).hasSize(1000);
            assertThat(result.get(0)).isEqualTo(5);  // "item0".length()
        }

        @Test
        @DisplayName("Hot path optimization matters")
        void testDemonstrateHotPath() {
            var performance = new LambdaBestPractices.PerformanceAwareness();
            List<String> strings = Arrays.asList("a", "bb", "ccc", "dddd");

            assertThatCode(() -> performance.demonstrateHotPath(strings))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should count occurrences thread-safe")
        void testCountOccurrencesThreadSafe() {
            var performance = new LambdaBestPractices.PerformanceAwareness();
            List<String> items = Arrays.asList("a", "b", "a", "c", "b", "a");

            Map<String, Integer> result = performance.countOccurrencesThreadSafe(items);

            assertThat(result)
                    .containsEntry("a", 3)
                    .containsEntry("b", 2)
                    .containsEntry("c", 1);
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTest {

        @Test
        @DisplayName("Should get lengths safely handling nulls")
        void testGetLengthsSafe() {
            var errorHandling = new LambdaBestPractices.ErrorHandling();
            List<String> strings = Arrays.asList("hello", null, "world", null, "!");

            List<Integer> result = errorHandling.getLengthsSafe(strings);

            assertThat(result).containsExactly(5, 5, 1);
        }

        @Test
        @DisplayName("Should parse numbers safely")
        void testParseNumbersSafe() {
            var errorHandling = new LambdaBestPractices.ErrorHandling();
            List<String> strings = Arrays.asList("1", "abc", "3", "xyz", "5");

            List<Integer> result = errorHandling.parseNumbersSafe(strings);

            assertThat(result).containsExactly(1, 3, 5);
        }

        @Test
        @DisplayName("Should process with validation")
        void testProcessWithValidation() {
            var errorHandling = new LambdaBestPractices.ErrorHandling();
            List<String> strings = Arrays.asList("valid@example.com", "invalid", "test@test.com");

            List<String> result = errorHandling.processWithValidation(strings);

            // All strings are valid (not null, not empty, <= 100 chars)
            assertThat(result).containsExactly(
                    "VALID@EXAMPLE.COM",
                    "INVALID",
                    "TEST@TEST.COM"
            );
        }

        @Test
        @DisplayName("Should get first match or default")
        void testGetFirstMatchOrDefault() {
            var errorHandling = new LambdaBestPractices.ErrorHandling();
            List<String> strings = Arrays.asList("hello", "world", "java");

            String result = errorHandling.getFirstMatchOrDefault(strings, s -> s.startsWith("j"));

            assertThat(result).isEqualTo("java");
        }

        @Test
        @DisplayName("Should return 'No match found' when no match")
        void testGetFirstMatchOrDefaultNoMatch() {
            var errorHandling = new LambdaBestPractices.ErrorHandling();
            List<String> strings = Arrays.asList("hello", "world");

            String result = errorHandling.getFirstMatchOrDefault(strings, s -> s.startsWith("z"));

            assertThat(result).isEqualTo("No match found");
        }
    }

    @Nested
    @DisplayName("Documentation")
    class DocumentationTest {

        @Test
        @DisplayName("Should get eligible user names with good documentation")
        void testGetEligibleUserNames() {
            var documentation = new LambdaBestPractices.Documentation();
            List<LambdaBestPractices.Documentation.User> users = Arrays.asList(
                    new LambdaBestPractices.Documentation.User("Alice", 25, true),
                    new LambdaBestPractices.Documentation.User("Bob", 17, true),
                    new LambdaBestPractices.Documentation.User("Charlie", 30, false)
            );

            List<String> result = documentation.getEligibleUserNames(users);

            assertThat(result).containsExactly("Alice");
        }

        @Test
        @DisplayName("Should score and rank users")
        void testScoreAndRankUsers() {
            var documentation = new LambdaBestPractices.Documentation();
            List<LambdaBestPractices.Documentation.User> users = Arrays.asList(
                    new LambdaBestPractices.Documentation.User("Alice", 25, true),
                    new LambdaBestPractices.Documentation.User("Bob", 30, true),
                    new LambdaBestPractices.Documentation.User("Charlie", 20, false)
            );

            List<LambdaBestPractices.Documentation.User> result = documentation.scoreAndRankUsers(users);

            assertThat(result).hasSize(3);
            // Scores: Alice=25, Bob=30, Charlie=10 (not active)
            // Ranked: Bob(30), Alice(25), Charlie(10)
            assertThat(result)
                    .extracting(LambdaBestPractices.Documentation.User::name)
                    .containsExactly("Bob", "Alice", "Charlie");
        }
    }

    @Nested
    @DisplayName("Testing Considerations")
    class TestingConsiderationsTest {

        @Test
        @DisplayName("UserService should filter eligible users")
        void testUserServiceGetEligibleUsers() {
            var service = new LambdaBestPractices.TestingConsiderations.UserService();
            List<LambdaBestPractices.TestingConsiderations.User> users = Arrays.asList(
                    new LambdaBestPractices.TestingConsiderations.User("Alice", 25, true),
                    new LambdaBestPractices.TestingConsiderations.User("Bob", 17, true),
                    new LambdaBestPractices.TestingConsiderations.User("Charlie", 30, false)
            );

            List<LambdaBestPractices.TestingConsiderations.User> result = service.getEligibleUsers(users);

            assertThat(result)
                    .hasSize(1)
                    .extracting(LambdaBestPractices.TestingConsiderations.User::name)
                    .containsExactly("Alice");
        }

        @Test
        @DisplayName("UserService should check user eligibility")
        void testUserServiceIsUserEligible() {
            var service = new LambdaBestPractices.TestingConsiderations.UserService();

            var eligible = new LambdaBestPractices.TestingConsiderations.User("Alice", 25, true);
            var tooYoung = new LambdaBestPractices.TestingConsiderations.User("Bob", 17, true);
            var notActive = new LambdaBestPractices.TestingConsiderations.User("Charlie", 30, false);

            assertThat(service.isUserEligible(eligible)).isTrue();
            assertThat(service.isUserEligible(tooYoung)).isFalse();
            assertThat(service.isUserEligible(notActive)).isFalse();
        }

        @Test
        @DisplayName("OrderProcessor should process orders with injected functions")
        void testOrderProcessorProcessOrders() {
            var processor = new LambdaBestPractices.TestingConsiderations.OrderProcessor(
                    order -> order.total() > 100,
                    order -> order.id()
            );
            List<LambdaBestPractices.TestingConsiderations.Order> orders = Arrays.asList(
                    new LambdaBestPractices.TestingConsiderations.Order("O1", 150.0),
                    new LambdaBestPractices.TestingConsiderations.Order("O2", 50.0),
                    new LambdaBestPractices.TestingConsiderations.Order("O3", 200.0)
            );

            List<String> result = processor.processOrders(orders);

            // Orders > 100: O1 and O3
            assertThat(result).containsExactly("O1", "O3");
        }
    }
}
