package com.github.msorkhpar.claudejavatutor.functionalinterfaces;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

@DisplayName("4.2.3 Function, BiFunction, UnaryOperator, BinaryOperator Tests")
class FunctionDemoTest {

    private final FunctionDemo demo = new FunctionDemo();

    @Nested
    @DisplayName("Function - Basic transformation")
    class BasicFunctionTests {

        @Test
        @DisplayName("Should transform String to Integer")
        void testStringToIntTransform() {
            int result = demo.stringToLength("hello");
            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("Should transform Integer to String")
        void testIntToStringTransform() {
            String result = demo.intToString(42);
            assertThat(result).isEqualTo("42");
        }

        @Test
        @DisplayName("Should apply function to each element in a list")
        void testApplyToList() {
            List<String> strings = List.of("hello", "world", "java");
            List<Integer> lengths = demo.applyToAll(strings, String::length);
            assertThat(lengths).containsExactly(5, 5, 4);
        }

        @Test
        @DisplayName("Should handle empty list")
        void testApplyToEmptyList() {
            List<Integer> result = demo.applyToAll(List.of(), String::length);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Function - compose and andThen")
    class FunctionCompositionTests {

        @Test
        @DisplayName("Should compose two functions: f.compose(g) = f(g(x))")
        void testCompose() {
            // g: trim, f: toUpperCase
            // compose: toUpperCase(trim(input))
            String result = demo.composeTrimAndUpperCase("  hello  ");
            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("Should chain with andThen: f.andThen(g) = g(f(x))")
        void testAndThen() {
            // f: toUpperCase, g: trim
            // andThen: trim(toUpperCase(input))
            String result = demo.andThenUpperCaseAndTrim("  hello  ");
            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("Should compose multiple functions")
        void testMultipleComposition() {
            String result = demo.processString("  hello world  ");
            assertThat(result).isEqualTo("HELLO_WORLD");
        }

        @Test
        @DisplayName("Should use identity function")
        void testIdentityFunction() {
            Function<String, String> identity = Function.identity();
            String input = "unchanged";
            assertThat(identity.apply(input)).isEqualTo(input);
        }
    }

    @Nested
    @DisplayName("BiFunction - two argument transformation")
    class BiFunctionTests {

        @Test
        @DisplayName("Should combine two strings")
        void testBiFunctionCombine() {
            String result = demo.combineStrings("Hello", "World");
            assertThat(result).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("Should compute power using BiFunction")
        void testBiFunctionPower() {
            double result = demo.power(2.0, 10);
            assertThat(result).isEqualTo(1024.0);
        }

        @Test
        @DisplayName("Should apply BiFunction to produce a map")
        void testBiFunctionToMap() {
            List<String> keys = List.of("a", "b", "c");
            List<Integer> values = List.of(1, 2, 3);
            Map<String, Integer> result = demo.zipToMap(keys, values);
            assertThat(result)
                    .containsEntry("a", 1)
                    .containsEntry("b", 2)
                    .containsEntry("c", 3);
        }

        @Test
        @DisplayName("Should chain BiFunction with andThen")
        void testBiFunctionAndThen() {
            int result = demo.addThenDouble(3, 4);
            assertThat(result).isEqualTo(14); // (3+4)*2
        }
    }

    @Nested
    @DisplayName("UnaryOperator - same type in and out")
    class UnaryOperatorTests {

        @Test
        @DisplayName("Should double a number")
        void testDoubleNumber() {
            int result = demo.doubleValue(5);
            assertThat(result).isEqualTo(10);
        }

        @Test
        @DisplayName("Should normalize string")
        void testNormalizeString() {
            String result = demo.normalizeString("  Hello World  ");
            assertThat(result).isEqualTo("hello world");
        }

        @Test
        @DisplayName("Should chain UnaryOperators")
        void testChainUnaryOperators() {
            String result = demo.chainStringOps("  hello world  ");
            assertThat(result).isEqualTo("HELLO_WORLD");
        }

        @Test
        @DisplayName("Should apply UnaryOperator to list elements")
        void testApplyToListElements() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5);
            List<Integer> result = demo.applyUnaryToList(numbers, n -> n * n);
            assertThat(result).containsExactly(1, 4, 9, 16, 25);
        }
    }

    @Nested
    @DisplayName("BinaryOperator - two same-type inputs, same-type output")
    class BinaryOperatorTests {

        @Test
        @DisplayName("Should add two integers")
        void testAddIntegers() {
            int result = demo.add(3, 4);
            assertThat(result).isEqualTo(7);
        }

        @Test
        @DisplayName("Should concatenate strings")
        void testConcatenateStrings() {
            String result = demo.concatenate("Hello", " World");
            assertThat(result).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("Should reduce list using BinaryOperator")
        void testReduceList() {
            List<Integer> numbers = List.of(1, 2, 3, 4, 5);
            int sum = demo.reduceList(numbers, 0, Integer::sum);
            assertThat(sum).isEqualTo(15);
        }

        @Test
        @DisplayName("Should find max using BinaryOperator")
        void testFindMax() {
            List<Integer> numbers = List.of(3, 1, 4, 1, 5, 9, 2, 6);
            int max = demo.findMax(numbers);
            assertThat(max).isEqualTo(9);
        }

        @Test
        @DisplayName("Should find min using BinaryOperator")
        void testFindMin() {
            List<Integer> numbers = List.of(3, 1, 4, 1, 5, 9, 2, 6);
            int min = demo.findMin(numbers);
            assertThat(min).isEqualTo(1);
        }

        @Test
        @DisplayName("Should use BinaryOperator.maxBy")
        void testMaxBy() {
            String result = demo.longestString(List.of("hi", "hello", "world", "a"));
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should use BinaryOperator.minBy")
        void testMinBy() {
            String result = demo.shortestString(List.of("hi", "hello", "world", "a"));
            assertThat(result).isEqualTo("a");
        }
    }

    @Nested
    @DisplayName("Primitive specialized Functions")
    class PrimitiveFunctionTests {

        @Test
        @DisplayName("Should use IntUnaryOperator for int operations")
        void testIntUnaryOperator() {
            int result = demo.applyIntUnary(5, n -> n * n);
            assertThat(result).isEqualTo(25);
        }

        @Test
        @DisplayName("Should use ToIntFunction to convert to int")
        void testToIntFunction() {
            int result = demo.stringToInt("hello");
            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("Should use IntFunction to produce object from int")
        void testIntFunction() {
            String result = demo.intToStringPadded(7);
            assertThat(result).isEqualTo("007");
        }
    }
}
