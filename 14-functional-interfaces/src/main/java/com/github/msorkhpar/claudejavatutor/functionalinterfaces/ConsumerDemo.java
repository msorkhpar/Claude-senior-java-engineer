package com.github.msorkhpar.claudejavatutor.functionalinterfaces;

import java.util.*;
import java.util.function.*;

/**
 * Demonstrates the Consumer and BiConsumer functional interfaces.
 *
 * <p>Consumer<T> - represents an operation that accepts one input and returns no result.
 * It is used for operations with side-effects (printing, storing, modifying state).
 *
 * <p>BiConsumer<T, U> - accepts two inputs and returns no result.
 *
 * <p>Key methods:
 * - void accept(T t)                    - performs the operation
 * - Consumer<T> andThen(Consumer<T>)    - chains two consumers sequentially
 */
public class ConsumerDemo {

    /**
     * Applies a Consumer to each element in a list.
     * Demonstrates basic Consumer usage for side-effects.
     */
    public void consumeStrings(List<String> strings, Consumer<String> consumer) {
        strings.forEach(consumer);
    }

    /**
     * Demonstrates Consumer that transforms before consuming.
     */
    public void consumeUpperCase(List<String> strings, Consumer<String> downstream) {
        Consumer<String> upperCaseConsumer = s -> downstream.accept(s.toUpperCase());
        strings.forEach(upperCaseConsumer);
    }

    /**
     * Demonstrates simple side-effect Consumer (printing).
     */
    public void printAll(List<String> items) {
        Consumer<String> printer = System.out::println;
        items.forEach(printer);
    }

    /**
     * Demonstrates Consumer chaining with andThen.
     * Both consumers are executed in sequence for each element.
     */
    public void consumeWithLogging(List<String> items, List<String> log) {
        Consumer<String> logger = item -> log.add("Processing: " + item);
        Consumer<String> printer = System.out::println;

        // andThen chains: first logger, then printer
        Consumer<String> combined = logger.andThen(printer);
        items.forEach(combined);
    }

    /**
     * Demonstrates chaining multiple consumers to record processing steps.
     */
    public void chainedConsume(String input, List<String> steps) {
        Consumer<String> step1 = s -> steps.add("step1: " + s);
        Consumer<String> step2 = s -> steps.add("step2: " + s.toUpperCase());
        Consumer<String> step3 = s -> steps.add("step3: " + s.length());

        Consumer<String> pipeline = step1.andThen(step2).andThen(step3);
        pipeline.accept(input);
    }

    /**
     * Demonstrates a null-safe Consumer that skips null elements.
     */
    public void consumeNullSafe(List<String> items, Consumer<String> consumer) {
        Consumer<String> nullSafeConsumer = item -> {
            if (item != null) {
                consumer.accept(item);
            }
        };
        items.forEach(nullSafeConsumer);
    }

    /**
     * Demonstrates BiConsumer accepting two different arguments.
     */
    public void biConsumeKeyValue(String key, String value, BiConsumer<String, String> biConsumer) {
        biConsumer.accept(key, value);
    }

    /**
     * Demonstrates BiConsumer to build a map from lists.
     * Maps each string to its length.
     */
    public Map<String, Integer> buildMapWithBiConsumer(List<String> strings) {
        Map<String, Integer> result = new LinkedHashMap<>();
        BiConsumer<String, Map<String, Integer>> mapBuilder =
                (s, map) -> map.put(s, s.length());

        strings.forEach(s -> mapBuilder.accept(s, result));
        return result;
    }

    /**
     * Demonstrates BiConsumer chaining with andThen.
     */
    public void biConsumerAndThen(String str, int num, List<String> log) {
        BiConsumer<String, Integer> logString = (s, n) -> log.add("String: " + s);
        BiConsumer<String, Integer> logInt = (s, n) -> log.add("Int: " + n);

        BiConsumer<String, Integer> combined = logString.andThen(logInt);
        combined.accept(str, num);
    }

    /**
     * Demonstrates BiConsumer with Map.forEach to iterate key-value pairs.
     */
    public void iterateMapWithBiConsumer(Map<String, Integer> map, List<String> results) {
        BiConsumer<String, Integer> entryProcessor =
                (key, value) -> results.add(key + " -> " + value);
        map.forEach(entryProcessor);
    }

    /**
     * Demonstrates Consumer for accumulation (sum pattern).
     */
    public int sumWithConsumer(List<Integer> numbers) {
        int[] sum = {0}; // effectively-final array trick for lambda capture
        Consumer<Integer> accumulator = n -> sum[0] += n;
        numbers.forEach(accumulator);
        return sum[0];
    }

    /**
     * Demonstrates validation with two consumers: one for valid, one for invalid.
     */
    public void validateAndConsume(
            List<String> items,
            Consumer<String> validConsumer,
            Consumer<String> invalidConsumer) {

        Consumer<String> dispatcher = item -> {
            if (item != null && !item.isEmpty()) {
                validConsumer.accept(item);
            } else {
                invalidConsumer.accept(item);
            }
        };
        items.forEach(dispatcher);
    }
}
