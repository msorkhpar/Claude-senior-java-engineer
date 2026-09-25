package com.github.msorkhpar.claudejavatutor.functionalinterfaces;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Demonstrates the Supplier functional interface and its primitive specializations.
 *
 * <p>Supplier<T> - represents a supplier of results. It takes no arguments and returns a value.
 * Used for lazy evaluation, factories, and deferred computation.
 *
 * <p>Primitive specializations:
 * - BooleanSupplier: getAsBoolean()
 * - IntSupplier:     getAsInt()
 * - LongSupplier:    getAsLong()
 * - DoubleSupplier:  getAsDouble()
 *
 * <p>Key method:
 * - T get() - produces the result
 */
public class SupplierDemo {

    /**
     * Supplies a constant string value.
     * Demonstrates the simplest form of a Supplier.
     */
    public String supplyConstant() {
        Supplier<String> supplier = () -> "Hello, Supplier!";
        return supplier.get();
    }

    /**
     * Supplies a random integer in the given range [min, max].
     */
    public int supplyRandomInRange(int min, int max) {
        Supplier<Integer> randomSupplier = () -> min + (int) (Math.random() * (max - min + 1));
        return randomSupplier.get();
    }

    /**
     * Supplies a new empty ArrayList each time called.
     * Demonstrates Supplier as a factory for new instances.
     */
    public List<String> supplyNewList() {
        Supplier<List<String>> listFactory = ArrayList::new;
        return listFactory.get();
    }

    /**
     * Creates a Supplier for an expensive operation without evaluating it yet.
     * Demonstrates the lazy evaluation benefit: the computation is deferred until needed.
     */
    public Supplier<String> createExpensiveSupplier() {
        // The computation is NOT performed here - just wrapped
        return () -> {
            // Simulate expensive computation
            return "expensive-result-" + System.currentTimeMillis();
        };
    }

    /**
     * Calls the expensive supplier to get the value.
     */
    public String getExpensiveValue() {
        Supplier<String> supplier = createExpensiveSupplier();
        return supplier.get(); // Only computed when we call get()
    }

    /**
     * Demonstrates Supplier with Optional.orElseGet() for lazy default computation.
     * Uses orElseGet(Supplier) instead of orElse(value) to avoid computing the default
     * when the Optional is present.
     */
    public String getOrDefault(Optional<String> optional) {
        Supplier<String> defaultSupplier = () -> "default-value";
        return optional.orElseGet(defaultSupplier);
    }

    /**
     * Demonstrates memoization: the Supplier computes the value once and caches it.
     */
    private String memoizedValue = null;

    public String getMemoizedValue() {
        if (memoizedValue == null) {
            Supplier<String> expensive = () -> "computed-once-" + 42;
            memoizedValue = expensive.get();
        }
        return memoizedValue;
    }

    /**
     * Generates a list of random numbers using a Supplier.
     * Demonstrates Supplier with Stream.generate().
     */
    public List<Integer> generateNumbers(int count) {
        Supplier<Integer> randomSupplier = () -> (int) (Math.random() * 100);
        return Stream.generate(randomSupplier)
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Simple record for demonstrating dependency injection via Supplier.
     */
    public record DatabaseConnection(String url) {
        public String getUrl() {
            return url;
        }
    }

    /**
     * Creates a DatabaseConnection using a Supplier.
     * Demonstrates Supplier as a factory/constructor reference pattern.
     */
    public DatabaseConnection createConnection(String url) {
        Supplier<DatabaseConnection> connectionFactory = () -> new DatabaseConnection(url);
        return connectionFactory.get();
    }

    /**
     * Defers connection creation until actually needed for the query.
     */
    public String queryWithConnection(String sql, String url) {
        Supplier<DatabaseConnection> connectionSupplier = () -> createConnection(url);
        // Only creates the connection when needed
        DatabaseConnection conn = connectionSupplier.get();
        return "Executed '" + sql + "' on " + conn.getUrl();
    }

    // =========================================================================
    // Primitive specializations - avoid boxing overhead
    // =========================================================================

    /**
     * BooleanSupplier - supplies a boolean without boxing.
     */
    public boolean supplyBoolean() {
        BooleanSupplier supplier = () -> true;
        return supplier.getAsBoolean();
    }

    /**
     * IntSupplier - supplies an int without boxing.
     */
    public int supplyInt() {
        IntSupplier supplier = () -> 42;
        return supplier.getAsInt();
    }

    /**
     * LongSupplier - supplies a long without boxing.
     */
    public long supplyLong() {
        LongSupplier supplier = () -> 100L;
        return supplier.getAsLong();
    }

    /**
     * DoubleSupplier - supplies a double without boxing.
     */
    public double supplyDouble() {
        DoubleSupplier supplier = () -> 3.14;
        return supplier.getAsDouble();
    }
}
