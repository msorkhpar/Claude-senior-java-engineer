package com.github.msorkhpar.claudejavatutor.functionalinterfaces;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("4.2.1 Consumer and BiConsumer Functional Interface Tests")
class ConsumerDemoTest {

    private final ConsumerDemo demo = new ConsumerDemo();

    @Nested
    @DisplayName("Consumer - Basic Usage")
    class BasicConsumerTests {

        @Test
        @DisplayName("Should collect items processed by a Consumer")
        void testCollectProcessedItems() {
            List<String> results = new ArrayList<>();
            demo.consumeStrings(List.of("hello", "world"), results::add);
            assertThat(results).containsExactly("hello", "world");
        }

        @Test
        @DisplayName("Should apply transformation via Consumer")
        void testConsumerTransformation() {
            List<String> collected = new ArrayList<>();
            demo.consumeUpperCase(List.of("hello", "java"), collected::add);
            assertThat(collected).containsExactly("HELLO", "JAVA");
        }

        @Test
        @DisplayName("Should handle empty list without error")
        void testConsumerWithEmptyList() {
            List<String> collected = new ArrayList<>();
            demo.consumeStrings(List.of(), collected::add);
            assertThat(collected).isEmpty();
        }

        @Test
        @DisplayName("Should print each item (side-effect Consumer)")
        void testPrintConsumer() {
            // Just verifying no exception is thrown and method returns normally
            assertThatCode(() -> demo.printAll(List.of("a", "b", "c")))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Consumer - andThen chaining")
    class ConsumerChainingTests {

        @Test
        @DisplayName("Should execute chained consumers in order")
        void testAndThenChaining() {
            List<String> log = new ArrayList<>();
            demo.consumeWithLogging(List.of("item1", "item2"), log);
            // log should contain both the original and uppercase versions
            assertThat(log).containsExactly("Processing: item1", "Processing: item2");
        }

        @Test
        @DisplayName("Should chain multiple consumers")
        void testMultipleChaining() {
            List<String> steps = new ArrayList<>();
            demo.chainedConsume("test", steps);
            assertThat(steps).hasSize(3);
            assertThat(steps.get(0)).isEqualTo("step1: test");
            assertThat(steps.get(1)).isEqualTo("step2: TEST");
            assertThat(steps.get(2)).isEqualTo("step3: 4");
        }
    }

    @Nested
    @DisplayName("Consumer - with null handling")
    class ConsumerNullTests {

        @Test
        @DisplayName("Should handle null-safe consumer")
        void testNullSafeConsumer() {
            List<String> results = new ArrayList<>();
            demo.consumeNullSafe(Arrays.asList("hello", null, "world"), results::add);
            assertThat(results).containsExactly("hello", "world");
        }
    }

    @Nested
    @DisplayName("BiConsumer - Basic Usage")
    class BiConsumerTests {

        @Test
        @DisplayName("Should process two arguments with BiConsumer")
        void testBiConsumerBasic() {
            List<String> results = new ArrayList<>();
            demo.biConsumeKeyValue("key", "value", (k, v) -> results.add(k + "=" + v));
            assertThat(results).containsExactly("key=value");
        }

        @Test
        @DisplayName("Should build a map using BiConsumer")
        void testBiConsumerBuildMap() {
            Map<String, Integer> map = demo.buildMapWithBiConsumer(
                    List.of("apple", "banana", "cherry")
            );
            assertThat(map)
                    .containsEntry("apple", 5)
                    .containsEntry("banana", 6)
                    .containsEntry("cherry", 6);
        }

        @Test
        @DisplayName("Should chain BiConsumers with andThen")
        void testBiConsumerChaining() {
            List<String> log = new ArrayList<>();
            demo.biConsumerAndThen("name", 42, log);
            assertThat(log).hasSize(2);
            assertThat(log.get(0)).isEqualTo("String: name");
            assertThat(log.get(1)).isEqualTo("Int: 42");
        }

        @Test
        @DisplayName("Should use BiConsumer for map iteration")
        void testBiConsumerMapForEach() {
            Map<String, Integer> input = Map.of("a", 1, "b", 2);
            List<String> results = new ArrayList<>();
            demo.iterateMapWithBiConsumer(input, results);
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(s -> s.contains("->"));
        }
    }

    @Nested
    @DisplayName("Consumer - Real-world scenarios")
    class RealWorldConsumerTests {

        @Test
        @DisplayName("Should accumulate results using Consumer")
        void testAccumulation() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5);
            int sum = demo.sumWithConsumer(numbers);
            assertThat(sum).isEqualTo(15);
        }

        @Test
        @DisplayName("Should validate and consume items")
        void testValidateAndConsume() {
            List<String> valid = new ArrayList<>();
            List<String> invalid = new ArrayList<>();
            demo.validateAndConsume(
                    Arrays.asList("valid", "", null, "also-valid"),
                    valid::add,
                    invalid::add
            );
            assertThat(valid).containsExactly("valid", "also-valid");
            assertThat(invalid).hasSize(2);
        }
    }
}
