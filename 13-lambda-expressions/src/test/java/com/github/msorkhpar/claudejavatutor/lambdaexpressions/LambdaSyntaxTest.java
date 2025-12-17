package com.github.msorkhpar.claudejavatutor.lambdaexpressions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Lambda Syntax Tests")
class LambdaSyntaxTest {

    @Nested
    @DisplayName("Parameter Variations")
    class ParameterVariationsTest {

        @Test
        @DisplayName("Should generate greeting with no parameters")
        void testNoParameters() {
            var params = new LambdaSyntax.ParameterVariations();
            String result = params.generateGreeting();
            assertThat(result).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should get lengths with single parameter")
        void testSingleParameter() {
            var params = new LambdaSyntax.ParameterVariations();
            List<String> strings = Arrays.asList("a", "ab", "abc");
            List<Integer> result = params.getLengths(strings);
            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should combine with multiple parameters")
        void testMultipleParameters() {
            var params = new LambdaSyntax.ParameterVariations();
            List<String> keys = Arrays.asList("a", "b", "c");
            List<Integer> values = Arrays.asList(1, 2, 3);
            Map<String, Integer> result = params.combineToMap(keys, values);
            assertThat(result)
                    .containsEntry("a", 1)
                    .containsEntry("b", 2)
                    .containsEntry("c", 3);
        }

        @Test
        @DisplayName("Should calculate sum with explicit types")
        void testExplicitTypes() {
            var params = new LambdaSyntax.ParameterVariations();
            List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
            int result = params.calculateSum(numbers);
            assertThat(result).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("Body Variations")
    class BodyVariationsTest {

        @Test
        @DisplayName("Should square numbers with expression body")
        void testExpressionBody() {
            var bodies = new LambdaSyntax.BodyVariations();
            List<Integer> numbers = Arrays.asList(2, 3, 4);
            List<Integer> result = bodies.squareNumbers(numbers);
            assertThat(result).containsExactly(4, 9, 16);
        }

        @Test
        @DisplayName("Should format with block body")
        void testBlockBody() {
            var bodies = new LambdaSyntax.BodyVariations();
            List<Integer> numbers = Arrays.asList(5, -3, 0);
            List<String> result = bodies.formatNumbers(numbers);
            assertThat(result).containsExactly("+5", "-3", "+0");
        }

        @Test
        @DisplayName("Should classify with ternary operator")
        void testTernaryOperator() {
            var bodies = new LambdaSyntax.BodyVariations();
            List<Integer> numbers = Arrays.asList(5, -3, 0);
            List<String> result = bodies.classifyNumbers(numbers);
            assertThat(result).containsExactly("positive", "negative", "zero");
        }

        @Test
        @DisplayName("Should calculate discounted prices")
        void testDiscountedPrices() {
            var bodies = new LambdaSyntax.BodyVariations();
            List<Double> prices = Arrays.asList(100.0, 50.0, 25.0);
            List<Double> result = bodies.calculateDiscountedPrices(prices, 10.0);
            assertThat(result).containsExactly(90.0, 45.0, 22.5);
        }

        @Test
        @DisplayName("Should categorize prices")
        void testCategorizePrices() {
            var bodies = new LambdaSyntax.BodyVariations();
            List<Double> prices = Arrays.asList(5.0, 50.0, 500.0, 5000.0);
            List<String> result = bodies.categorizePrices(prices);
            assertThat(result).containsExactly("cheap", "moderate", "expensive", "premium");
        }

        @Test
        @DisplayName("Should validate and transform")
        void testValidateAndTransform() {
            var bodies = new LambdaSyntax.BodyVariations();
            List<String> strings = Arrays.asList(null, "", "  hello  ");
            List<String> result = bodies.validateAndTransform(strings);
            assertThat(result).containsExactly("NULL", "EMPTY", "HELLO");
        }
    }

    @Nested
    @DisplayName("Type Inference")
    class TypeInferenceTest {

        @Test
        @DisplayName("Should infer types from variable declaration")
        void testTypeInference() {
            var inference = new LambdaSyntax.TypeInference();
            List<Integer> numbers = Arrays.asList(1, 2, 3);
            int result = inference.sumNumbers(numbers);
            assertThat(result).isEqualTo(6);
        }

        @Test
        @DisplayName("Should use transformer")
        void testUseTransform() {
            var inference = new LambdaSyntax.TypeInference();
            List<String> strings = Arrays.asList("hello", "world");
            List<String> result = inference.useTransform(strings);
            assertThat(result).containsExactly("HELLO", "WORLD");
        }

        @Test
        @DisplayName("Should create range predicate")
        void testCreateRangePredicate() {
            var inference = new LambdaSyntax.TypeInference();
            var predicate = inference.createRangePredicate(10, 20);
            assertThat(predicate.test(15)).isTrue();
            assertThat(predicate.test(5)).isFalse();
            assertThat(predicate.test(25)).isFalse();
        }

        @Test
        @DisplayName("Should filter by condition")
        void testFilterByCondition() {
            var inference = new LambdaSyntax.TypeInference();
            List<String> items = Arrays.asList("apple", "banana", "apricot");
            List<String> result = inference.filterByCondition(items, s -> s.startsWith("a"));
            assertThat(result).containsExactly("apple", "apricot");
        }
    }

    @Nested
    @DisplayName("Method References")
    class MethodReferencesTest {

        @Test
        @DisplayName("Should use method reference")
        void testMethodReferences() {
            var refs = new LambdaSyntax.MethodReferences();
            List<String> strings = Arrays.asList("hello", "world");
            List<String> result = refs.toUpperCaseWithReference(strings);
            assertThat(result).containsExactly("HELLO", "WORLD");
        }

        @Test
        @DisplayName("Should parse integers")
        void testParseIntegers() {
            var refs = new LambdaSyntax.MethodReferences();
            List<String> strings = Arrays.asList("1", "2", "3");
            List<Integer> result = refs.parseIntegers(strings);
            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should create builders")
        void testCreateBuilders() {
            var refs = new LambdaSyntax.MethodReferences();
            List<String> strings = Arrays.asList("hello", "world");
            List<StringBuilder> result = refs.createBuilders(strings);
            assertThat(result).hasSize(2);
            assertThat(result.get(0).toString()).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should get person names")
        void testGetPersonNames() {
            var refs = new LambdaSyntax.MethodReferences();
            List<LambdaSyntax.MethodReferences.Person> people = Arrays.asList(
                    new LambdaSyntax.MethodReferences.Person("Alice", 30),
                    new LambdaSyntax.MethodReferences.Person("Bob", 25)
            );
            List<String> result = refs.getPersonNames(people);
            assertThat(result).containsExactly("Alice", "Bob");
        }
    }

    @Nested
    @DisplayName("Advanced Patterns")
    class AdvancedPatternsTest {

        @Test
        @DisplayName("Should use nested lambda")
        void testNestedLambda() {
            var advanced = new LambdaSyntax.AdvancedPatterns();
            int result = advanced.useNestedLambda();
            assertThat(result).isEqualTo(8);
        }

        @Test
        @DisplayName("Should create null-safe predicate")
        void testNullSafePredicate() {
            var advanced = new LambdaSyntax.AdvancedPatterns();
            var predicate = advanced.<String>createNullSafePredicate(s -> s.length() > 3);
            assertThat(predicate.test("hello")).isTrue();
            assertThat(predicate.test("hi")).isFalse();
            assertThat(predicate.test(null)).isFalse();
        }

        @Test
        @DisplayName("Should process with multiple lambdas")
        void testMultipleLambdas() {
            var advanced = new LambdaSyntax.AdvancedPatterns();
            List<String> strings = Arrays.asList("apple", "", "BANANA", "apricot");
            List<String> result = advanced.processWithMultipleLambdas(strings);
            assertThat(result).containsExactly("APPLE", "APRICOT");
        }

        @Test
        @DisplayName("Should sort and filter")
        void testSortAndFilter() {
            var advanced = new LambdaSyntax.AdvancedPatterns();
            List<String> items = Arrays.asList("cherry", "apple", "banana");
            List<String> result = advanced.sortAndFilter(items, s -> s.length() > 5);
            assertThat(result).containsExactly("banana", "cherry");
        }
    }
}
