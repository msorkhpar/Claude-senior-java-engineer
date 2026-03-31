package com.github.msorkhpar.claudejavatutor.dryprinciple;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Demonstrates the DRY (Don't Repeat Yourself) principle definition and purpose.
 * Shows violations and how to refactor toward DRY code.
 */
public class DryDefinition {

    // ========== Violation Examples ==========

    /**
     * VIOLATION: Duplicated validation logic across multiple methods.
     */
    public static class ViolationExample {

        public boolean validateEmail(String email) {
            if (email == null || email.isBlank()) {
                return false;
            }
            return email.contains("@") && email.contains(".");
        }

        public boolean validateEmailForRegistration(String email) {
            // Duplicated logic!
            if (email == null || email.isBlank()) {
                return false;
            }
            return email.contains("@") && email.contains(".");
        }

        public boolean validateEmailForPasswordReset(String email) {
            // Duplicated logic again!
            if (email == null || email.isBlank()) {
                return false;
            }
            return email.contains("@") && email.contains(".");
        }

        /**
         * VIOLATION: Duplicated formatting logic.
         */
        public String formatUserForDisplay(String name, int age) {
            return name.toUpperCase() + " (Age: " + age + ")";
        }

        public String formatUserForReport(String name, int age) {
            return name.toUpperCase() + " (Age: " + age + ")";
        }
    }

    // ========== DRY Refactored Examples ==========

    /**
     * DRY: Single source of truth for validation and formatting logic.
     */
    public static class DryRefactored {

        public boolean validateEmail(String email) {
            return isValidEmail(email);
        }

        public boolean validateEmailForRegistration(String email) {
            return isValidEmail(email);
        }

        public boolean validateEmailForPasswordReset(String email) {
            return isValidEmail(email);
        }

        private boolean isValidEmail(String email) {
            if (email == null || email.isBlank()) {
                return false;
            }
            return email.contains("@") && email.contains(".");
        }

        public String formatUser(String name, int age) {
            return name.toUpperCase() + " (Age: " + age + ")";
        }

        public String formatUserForDisplay(String name, int age) {
            return formatUser(name, age);
        }

        public String formatUserForReport(String name, int age) {
            return formatUser(name, age);
        }
    }

    // ========== Knowledge Duplication ==========

    /**
     * Demonstrates knowledge duplication vs structural duplication.
     * Sometimes code looks similar but represents different domain concepts.
     */
    public static class KnowledgeDuplication {

        /**
         * VIOLATION: Magic number repeated everywhere.
         */
        public static class MagicNumberViolation {
            public double calculateShippingCost(double weight) {
                return weight * 0.15; // What is 0.15?
            }

            public double estimateDelivery(double weight) {
                return weight * 0.15 + 5.0; // Same magic number!
            }
        }

        /**
         * DRY: Centralized constant.
         */
        public static class MagicNumberFixed {
            private static final double COST_PER_KG = 0.15;
            private static final double BASE_DELIVERY_FEE = 5.0;

            public double calculateShippingCost(double weight) {
                return weight * COST_PER_KG;
            }

            public double estimateDelivery(double weight) {
                return weight * COST_PER_KG + BASE_DELIVERY_FEE;
            }
        }
    }

    // ========== Template Method Pattern for DRY ==========

    /**
     * Demonstrates using the Template Method pattern to eliminate duplication
     * in processing pipelines.
     */
    public abstract static class DataProcessor<T, R> {

        public final List<R> process(List<T> items) {
            Objects.requireNonNull(items, "items must not be null");
            return items.stream()
                    .filter(this::isValid)
                    .map(this::transform)
                    .collect(Collectors.toList());
        }

        protected abstract boolean isValid(T item);

        protected abstract R transform(T item);
    }

    /**
     * Concrete processor for strings: filters non-empty and converts to uppercase.
     */
    public static class StringUpperCaseProcessor extends DataProcessor<String, String> {

        @Override
        protected boolean isValid(String item) {
            return item != null && !item.isBlank();
        }

        @Override
        protected String transform(String item) {
            return item.toUpperCase();
        }
    }

    /**
     * Concrete processor for integers: filters positive and doubles them.
     */
    public static class IntegerDoublerProcessor extends DataProcessor<Integer, Integer> {

        @Override
        protected boolean isValid(Integer item) {
            return item != null && item > 0;
        }

        @Override
        protected Integer transform(Integer item) {
            return item * 2;
        }
    }

    // ========== Functional DRY with Higher-Order Functions ==========

    /**
     * Demonstrates DRY through higher-order functions (Java 21 style).
     */
    public static class FunctionalDry {

        /**
         * Generic filter-and-transform pipeline eliminating repeated stream patterns.
         */
        public <T, R> List<R> filterAndTransform(
                List<T> items,
                Predicate<T> filter,
                Function<T, R> transformer) {
            Objects.requireNonNull(items, "items must not be null");
            Objects.requireNonNull(filter, "filter must not be null");
            Objects.requireNonNull(transformer, "transformer must not be null");

            return items.stream()
                    .filter(filter)
                    .map(transformer)
                    .collect(Collectors.toList());
        }

        /**
         * Generic aggregation eliminating repeated reduce patterns.
         */
        public <T> Optional<T> reduceWith(List<T> items, java.util.function.BinaryOperator<T> accumulator) {
            Objects.requireNonNull(items, "items must not be null");
            Objects.requireNonNull(accumulator, "accumulator must not be null");

            return items.stream().reduce(accumulator);
        }
    }
}
