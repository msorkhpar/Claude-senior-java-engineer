package com.github.msorkhpar.claudejavatutor.functionalinterfaces;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Demonstrates Function, BiFunction, UnaryOperator, and BinaryOperator functional interfaces.
 *
 * <p>Function<T, R> - transforms a value of type T into type R.
 * Key methods:
 *   - R apply(T t)
 *   - Function<V, R> compose(Function<T, V> before)  — before.apply then this.apply
 *   - Function<T, V> andThen(Function<R, V> after)   — this.apply then after.apply
 *   - static Function<T, T> identity()
 *
 * <p>BiFunction<T, U, R> - accepts two arguments of types T and U, returns R.
 *   - R apply(T t, U u)
 *   - BiFunction<T, U, V> andThen(Function<R, V> after)
 *
 * <p>UnaryOperator<T> extends Function<T, T> — input and output are the same type.
 *   - static UnaryOperator<T> identity()
 *
 * <p>BinaryOperator<T> extends BiFunction<T, T, T> — both inputs and output are same type.
 *   - static BinaryOperator<T> maxBy(Comparator<T>)
 *   - static BinaryOperator<T> minBy(Comparator<T>)
 */
public class FunctionDemo {

    /**
     * Applies a Function to convert String -> Integer (string length).
     */
    public int stringToLength(String input) {
        Function<String, Integer> lengthFunction = String::length;
        return lengthFunction.apply(input);
    }

    /**
     * Applies a Function to convert Integer -> String.
     */
    public String intToString(int number) {
        Function<Integer, String> toStr = String::valueOf;
        return toStr.apply(number);
    }

    /**
     * Applies a Function to each element in a list and returns results.
     */
    public <T, R> List<R> applyToAll(List<T> items, Function<T, R> function) {
        return items.stream()
                .map(function)
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates compose(): the given function is applied BEFORE this function.
     * f.compose(g) == f(g(x))
     * Here: toUpperCase.compose(trim) == toUpperCase(trim(input))
     */
    public String composeTrimAndUpperCase(String input) {
        Function<String, String> trim = String::trim;
        Function<String, String> toUpperCase = String::toUpperCase;
        // toUpperCase composed with trim: trim first, then upperCase
        Function<String, String> trimThenUpper = toUpperCase.compose(trim);
        return trimThenUpper.apply(input);
    }

    /**
     * Demonstrates andThen(): the given function is applied AFTER this function.
     * f.andThen(g) == g(f(x))
     * Here: toUpperCase.andThen(trim) == trim(toUpperCase(input))
     */
    public String andThenUpperCaseAndTrim(String input) {
        Function<String, String> toUpperCase = String::toUpperCase;
        Function<String, String> trim = String::trim;
        // upperCase first, then trim
        Function<String, String> upperThenTrim = toUpperCase.andThen(trim);
        return upperThenTrim.apply(input);
    }

    /**
     * Demonstrates composing multiple functions into a pipeline.
     * Steps: trim -> toUpperCase -> replace spaces with underscores
     */
    public String processString(String input) {
        Function<String, String> trim = String::trim;
        Function<String, String> toUpperCase = String::toUpperCase;
        Function<String, String> replaceSpaces = s -> s.replace(" ", "_");

        Function<String, String> pipeline = trim.andThen(toUpperCase).andThen(replaceSpaces);
        return pipeline.apply(input);
    }

    // =========================================================================
    // BiFunction
    // =========================================================================

    /**
     * Demonstrates BiFunction to combine two strings.
     */
    public String combineStrings(String first, String second) {
        BiFunction<String, String, String> combiner = (a, b) -> a + " " + b;
        return combiner.apply(first, second);
    }

    /**
     * Demonstrates BiFunction for mathematical operation.
     */
    public double power(double base, int exponent) {
        BiFunction<Double, Integer, Double> powerFn = (b, e) -> Math.pow(b, e);
        return powerFn.apply(base, exponent);
    }

    /**
     * Demonstrates BiFunction to zip two lists into a map.
     */
    public Map<String, Integer> zipToMap(List<String> keys, List<Integer> values) {
        BiFunction<List<String>, List<Integer>, Map<String, Integer>> zipper = (ks, vs) -> {
            Map<String, Integer> map = new LinkedHashMap<>();
            for (int i = 0; i < Math.min(ks.size(), vs.size()); i++) {
                map.put(ks.get(i), vs.get(i));
            }
            return map;
        };
        return zipper.apply(keys, values);
    }

    /**
     * Demonstrates BiFunction.andThen() to chain an additional Function.
     * addThenDouble: (a, b) -> (a + b) * 2
     */
    public int addThenDouble(int a, int b) {
        BiFunction<Integer, Integer, Integer> add = Integer::sum;
        Function<Integer, Integer> doubler = n -> n * 2;
        BiFunction<Integer, Integer, Integer> addThenDouble = add.andThen(doubler);
        return addThenDouble.apply(a, b);
    }

    // =========================================================================
    // UnaryOperator
    // =========================================================================

    /**
     * Demonstrates UnaryOperator for integer transformation.
     */
    public int doubleValue(int n) {
        UnaryOperator<Integer> doubler = x -> x * 2;
        return doubler.apply(n);
    }

    /**
     * Demonstrates UnaryOperator for string normalization.
     */
    public String normalizeString(String input) {
        UnaryOperator<String> normalizer = s -> s.trim().toLowerCase();
        return normalizer.apply(input);
    }

    /**
     * Chains UnaryOperators to process a string through multiple steps.
     */
    public String chainStringOps(String input) {
        UnaryOperator<String> trim = String::trim;
        UnaryOperator<String> toUpper = String::toUpperCase;
        UnaryOperator<String> replaceSpaces = s -> s.replace(" ", "_");

        // UnaryOperator extends Function, so andThen works
        Function<String, String> pipeline = trim.andThen(toUpper).andThen(replaceSpaces);
        return pipeline.apply(input);
    }

    /**
     * Applies a UnaryOperator to each element in a list (replaceAll).
     */
    public List<Integer> applyUnaryToList(List<Integer> numbers, UnaryOperator<Integer> operator) {
        List<Integer> result = new ArrayList<>(numbers);
        result.replaceAll(operator);
        return result;
    }

    // =========================================================================
    // BinaryOperator
    // =========================================================================

    /**
     * Demonstrates BinaryOperator for addition.
     */
    public int add(int a, int b) {
        BinaryOperator<Integer> adder = Integer::sum;
        return adder.apply(a, b);
    }

    /**
     * Demonstrates BinaryOperator for string concatenation.
     */
    public String concatenate(String a, String b) {
        BinaryOperator<String> concat = (x, y) -> x + y;
        return concat.apply(a, b);
    }

    /**
     * Reduces a list to a single value using a BinaryOperator.
     */
    public <T> T reduceList(List<T> list, T identity, BinaryOperator<T> operator) {
        T result = identity;
        for (T item : list) {
            result = operator.apply(result, item);
        }
        return result;
    }

    /**
     * Finds maximum using BinaryOperator.
     */
    public int findMax(List<Integer> numbers) {
        return numbers.stream()
                .reduce(BinaryOperator.maxBy(Integer::compareTo))
                .orElseThrow();
    }

    /**
     * Finds minimum using BinaryOperator.
     */
    public int findMin(List<Integer> numbers) {
        return numbers.stream()
                .reduce(BinaryOperator.minBy(Integer::compareTo))
                .orElseThrow();
    }

    /**
     * Finds longest string using BinaryOperator.maxBy with length comparator.
     */
    public String longestString(List<String> strings) {
        BinaryOperator<String> longerOf = BinaryOperator.maxBy(Comparator.comparingInt(String::length));
        return strings.stream()
                .reduce(longerOf)
                .orElseThrow();
    }

    /**
     * Finds shortest string using BinaryOperator.minBy with length comparator.
     */
    public String shortestString(List<String> strings) {
        BinaryOperator<String> shorterOf = BinaryOperator.minBy(Comparator.comparingInt(String::length));
        return strings.stream()
                .reduce(shorterOf)
                .orElseThrow();
    }

    // =========================================================================
    // Primitive specializations
    // =========================================================================

    /**
     * Demonstrates IntUnaryOperator — avoids boxing for int operations.
     */
    public int applyIntUnary(int n, IntUnaryOperator operator) {
        return operator.applyAsInt(n);
    }

    /**
     * Demonstrates ToIntFunction — converts an object to an int (no boxing).
     */
    public int stringToInt(String s) {
        ToIntFunction<String> lengthFn = String::length;
        return lengthFn.applyAsInt(s);
    }

    /**
     * Demonstrates IntFunction — takes an int and produces an object.
     */
    public String intToStringPadded(int n) {
        IntFunction<String> padded = i -> String.format("%03d", i);
        return padded.apply(n);
    }
}
