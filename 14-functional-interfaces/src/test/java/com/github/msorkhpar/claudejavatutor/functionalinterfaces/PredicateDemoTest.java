package com.github.msorkhpar.claudejavatutor.functionalinterfaces;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("4.2.4 Predicate and BiPredicate Functional Interface Tests")
class PredicateDemoTest {

    private final PredicateDemo demo = new PredicateDemo();

    @Nested
    @DisplayName("Predicate - Basic testing")
    class BasicPredicateTests {

        @Test
        @DisplayName("Should test if string is empty")
        void testIsEmpty() {
            assertThat(demo.isEmpty("")).isTrue();
            assertThat(demo.isEmpty("hello")).isFalse();
        }

        @Test
        @DisplayName("Should test if number is positive")
        void testIsPositive() {
            assertThat(demo.isPositive(5)).isTrue();
            assertThat(demo.isPositive(0)).isFalse();
            assertThat(demo.isPositive(-3)).isFalse();
        }

        @Test
        @DisplayName("Should test if string starts with prefix")
        void testStartsWith() {
            assertThat(demo.startsWith("Hello World", "Hello")).isTrue();
            assertThat(demo.startsWith("Hello World", "World")).isFalse();
        }

        @Test
        @DisplayName("Should filter a list using a Predicate")
        void testFilter() {
            List<Integer> numbers = List.of(1, -2, 3, -4, 5, 0);
            List<Integer> positives = demo.filter(numbers, n -> n > 0);
            assertThat(positives).containsExactly(1, 3, 5);
        }

        @Test
        @DisplayName("Should return empty list when no elements match")
        void testFilterNoMatch() {
            List<Integer> numbers = List.of(-1, -2, -3);
            List<Integer> result = demo.filter(numbers, n -> n > 0);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Predicate - and, or, negate composition")
    class PredicateCompositionTests {

        @Test
        @DisplayName("Should combine predicates with and()")
        void testAndComposition() {
            List<Integer> numbers = List.of(-5, -1, 0, 1, 5, 10, 15);
            // positive AND less than 10
            List<Integer> result = demo.filterWithAnd(numbers, n -> n > 0, n -> n < 10);
            assertThat(result).containsExactly(1, 5);
        }

        @Test
        @DisplayName("Should combine predicates with or()")
        void testOrComposition() {
            List<Integer> numbers = List.of(-5, 0, 1, 5, 10);
            // negative OR greater than 5
            List<Integer> result = demo.filterWithOr(numbers, n -> n < 0, n -> n > 5);
            assertThat(result).containsExactly(-5, 10);
        }

        @Test
        @DisplayName("Should negate a predicate")
        void testNegate() {
            List<String> strings = List.of("", "hello", "", "world");
            List<String> nonEmpty = demo.filterWithNegate(strings, String::isEmpty);
            assertThat(nonEmpty).containsExactly("hello", "world");
        }

        @Test
        @DisplayName("Should use Predicate.not() for readable negation")
        void testPredicateNot() {
            List<String> strings = List.of("", "hello", "  ", "world");
            List<String> result = demo.filterNotEmpty(strings);
            assertThat(result).containsExactly("hello", "world");
        }

        @Test
        @DisplayName("Should chain and/or/negate for complex conditions")
        void testComplexComposition() {
            List<String> words = List.of("hi", "hello", "java", "world", "j");
            // length > 1 AND (starts with 'h' OR starts with 'j')
            List<String> result = demo.complexFilter(words);
            assertThat(result).containsExactly("hi", "hello", "java");
        }
    }

    @Nested
    @DisplayName("Predicate - null handling")
    class PredicateNullTests {

        @Test
        @DisplayName("Should handle null with isEqual predicate")
        void testIsEqualNull() {
            assertThat(demo.isEqualTo(null, null)).isTrue();
            assertThat(demo.isEqualTo("hello", null)).isFalse();
            assertThat(demo.isEqualTo(null, "hello")).isFalse();
        }

        @Test
        @DisplayName("Should filter out nulls from a list")
        void testFilterNulls() {
            List<String> withNulls = java.util.Arrays.asList("a", null, "b", null, "c");
            List<String> result = demo.filterNulls(withNulls);
            assertThat(result).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("BiPredicate - testing two arguments")
    class BiPredicateTests {

        @Test
        @DisplayName("Should test if string contains substring")
        void testBiPredicateContains() {
            assertThat(demo.contains("Hello World", "World")).isTrue();
            assertThat(demo.contains("Hello World", "Java")).isFalse();
        }

        @Test
        @DisplayName("Should test integer range with BiPredicate")
        void testBiPredicateRange() {
            // isInRange(min, max, value): tests if value is in [min, max]
            assertThat(demo.isInRange(1, 10, 5)).isTrue();   // 5 is in [1, 10]
            assertThat(demo.isInRange(5, 10, 5)).isTrue();   // 5 is in [5, 10]
            assertThat(demo.isInRange(5, 10, 10)).isTrue();  // 10 is in [5, 10]
            assertThat(demo.isInRange(5, 10, 0)).isFalse();  // 0 is not in [5, 10]
            assertThat(demo.isInRange(5, 10, 11)).isFalse(); // 11 is not in [5, 10]
        }

        @Test
        @DisplayName("Should chain BiPredicates with and()")
        void testBiPredicateAnd() {
            // Both: length > 3 AND starts with prefix
            List<String> result = demo.filterWithBiPredicateAnd(
                    List.of("hi", "hello", "hey", "java"),
                    "he"
            );
            assertThat(result).containsExactly("hello");
        }

        @Test
        @DisplayName("Should negate a BiPredicate")
        void testBiPredicateNegate() {
            assertThat(demo.doesNotContain("Hello World", "Java")).isTrue();
            assertThat(demo.doesNotContain("Hello World", "Hello")).isFalse();
        }
    }

    @Nested
    @DisplayName("Primitive specialized Predicates")
    class PrimitivePredicateTests {

        @Test
        @DisplayName("Should use IntPredicate for int testing")
        void testIntPredicate() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6);
            List<Integer> evens = demo.filterEvens(numbers);
            assertThat(evens).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("Should use LongPredicate for large number testing")
        void testLongPredicate() {
            assertThat(demo.isLargeNumber(Long.MAX_VALUE)).isTrue();
            assertThat(demo.isLargeNumber(1_000_000L)).isFalse();
        }
    }

    @Nested
    @DisplayName("Predicate - real-world validation scenarios")
    class ValidationTests {

        @Test
        @DisplayName("Should validate email format")
        void testEmailValidation() {
            assertThat(demo.isValidEmail("user@example.com")).isTrue();
            assertThat(demo.isValidEmail("invalid-email")).isFalse();
            assertThat(demo.isValidEmail("")).isFalse();
            assertThat(demo.isValidEmail(null)).isFalse();
        }

        @Test
        @DisplayName("Should validate password strength")
        void testPasswordValidation() {
            assertThat(demo.isStrongPassword("Str0ng!Pass")).isTrue();
            assertThat(demo.isStrongPassword("weak")).isFalse();
            assertThat(demo.isStrongPassword("alllowercase1!")).isFalse();
        }

        @Test
        @DisplayName("Should count matching elements")
        void testCountMatching() {
            List<String> words = List.of("apple", "ant", "banana", "avocado", "cherry");
            long count = demo.countMatching(words, w -> w.startsWith("a"));
            assertThat(count).isEqualTo(3);
        }
    }
}
