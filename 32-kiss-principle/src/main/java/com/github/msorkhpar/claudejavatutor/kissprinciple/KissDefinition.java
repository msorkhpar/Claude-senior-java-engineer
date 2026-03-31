package com.github.msorkhpar.claudejavatutor.kissprinciple;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Demonstrates the KISS (Keep It Simple, Stupid) principle through contrasting examples.
 * Each pair shows a simple approach vs. an over-engineered approach to achieve the same goal.
 */
public class KissDefinition {

    // ==================== Email Validation: Simple vs Over-Engineered ====================

    /**
     * KISS approach: Simple email validation with clear, readable logic.
     * Checks basic structure without unnecessary complexity.
     */
    public static class SimpleEmailValidator {

        public boolean isValid(String email) {
            if (email == null || email.isEmpty()) {
                return false;
            }
            int atIndex = email.indexOf('@');
            return atIndex > 0 && atIndex < email.length() - 1;
        }
    }

    /**
     * Over-engineered approach: Unnecessary abstraction layers, strategy pattern,
     * and builder for a simple validation task.
     * This demonstrates what happens when KISS is violated.
     */
    public static class OverEngineeredEmailValidator {

        // Unnecessary interface hierarchy
        interface ValidationRule {
            boolean validate(String input);
            String getRuleName();
        }

        // Unnecessary concrete rule classes
        static class NotNullRule implements ValidationRule {
            @Override
            public boolean validate(String input) { return input != null; }
            @Override
            public String getRuleName() { return "NotNull"; }
        }

        static class NotEmptyRule implements ValidationRule {
            @Override
            public boolean validate(String input) { return input != null && !input.isEmpty(); }
            @Override
            public String getRuleName() { return "NotEmpty"; }
        }

        static class ContainsAtRule implements ValidationRule {
            @Override
            public boolean validate(String input) {
                return input != null && input.indexOf('@') > 0;
            }
            @Override
            public String getRuleName() { return "ContainsAt"; }
        }

        static class HasDomainRule implements ValidationRule {
            @Override
            public boolean validate(String input) {
                if (input == null) return false;
                int atIndex = input.indexOf('@');
                return atIndex > 0 && atIndex < input.length() - 1;
            }
            @Override
            public String getRuleName() { return "HasDomain"; }
        }

        private final List<ValidationRule> rules = List.of(
                new NotNullRule(),
                new NotEmptyRule(),
                new ContainsAtRule(),
                new HasDomainRule()
        );

        public boolean isValid(String email) {
            return rules.stream().allMatch(rule -> rule.validate(email));
        }
    }

    // ==================== Data Transformation: Simple vs Over-Engineered ====================

    /**
     * KISS approach: Get unique sorted words from a string using standard library methods.
     */
    public static class SimpleDataTransformer {

        public List<String> getUniqueSortedWords(String input) {
            if (input == null || input.isBlank()) {
                return Collections.emptyList();
            }
            return Arrays.stream(input.split("\\s+"))
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    /**
     * Over-engineered approach: Custom data pipeline with unnecessary abstractions.
     * Achieves the same result with far more complexity.
     */
    public static class OverEngineeredDataTransformer {

        interface TransformationStage<I, O> {
            O transform(I input);
        }

        static class SplitStage implements TransformationStage<String, List<String>> {
            @Override
            public List<String> transform(String input) {
                return Arrays.asList(input.split("\\s+"));
            }
        }

        static class DeduplicateStage implements TransformationStage<List<String>, Set<String>> {
            @Override
            public Set<String> transform(List<String> input) {
                return new LinkedHashSet<>(input);
            }
        }

        static class SortStage implements TransformationStage<Set<String>, List<String>> {
            @Override
            public List<String> transform(Set<String> input) {
                List<String> result = new ArrayList<>(input);
                Collections.sort(result);
                return result;
            }
        }

        public List<String> getUniqueSortedWords(String input) {
            if (input == null || input.isBlank()) {
                return Collections.emptyList();
            }
            List<String> split = new SplitStage().transform(input);
            Set<String> deduped = new DeduplicateStage().transform(split);
            return new SortStage().transform(deduped);
        }
    }

    // ==================== Configuration: Simple vs Over-Engineered ====================

    /**
     * KISS approach: Simple key-value configuration using a HashMap.
     */
    public static class SimpleConfig {

        private final Map<String, String> properties = new HashMap<>();

        public void set(String key, String value) {
            if (key == null) {
                throw new IllegalArgumentException("Key must not be null");
            }
            properties.put(key, value);
        }

        public String get(String key) {
            return properties.get(key);
        }

        public String getOrDefault(String key, String defaultValue) {
            return properties.getOrDefault(key, defaultValue);
        }
    }

    // ==================== Retry Logic: Simple vs Over-Engineered ====================

    /**
     * KISS approach: Simple retry mechanism with straightforward loop logic.
     */
    public static class SimpleRetry {

        public <T> T executeWithRetry(Supplier<T> action, int maxAttempts) {
            if (maxAttempts <= 0) {
                throw new IllegalArgumentException("maxAttempts must be positive");
            }
            RuntimeException lastException = null;
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    return action.get();
                } catch (RuntimeException e) {
                    lastException = e;
                }
            }
            throw lastException;
        }
    }
}
