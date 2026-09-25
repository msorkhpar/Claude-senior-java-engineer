package com.github.msorkhpar.claudejavatutor.annotations;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates built-in annotations in Java: @Override, @Deprecated, @SuppressWarnings,
 * @FunctionalInterface, @SafeVarargs, and others.
 */
public class BuiltInAnnotations {

    // ========================
    // @Override demonstration
    // ========================

    /**
     * Base class to demonstrate @Override annotation.
     */
    public static class Animal {
        public String speak() {
            return "...";
        }

        public String describe() {
            return "I am an animal";
        }
    }

    /**
     * Subclass correctly using @Override.
     */
    public static class Dog extends Animal {
        @Override
        public String speak() {
            return "Woof!";
        }

        @Override
        public String describe() {
            return "I am a dog";
        }
    }

    /**
     * Demonstrates @Override with interface implementation.
     */
    public interface Greetable {
        String greet(String name);
    }

    public static class FriendlyGreeter implements Greetable {
        @Override
        public String greet(String name) {
            return "Hello, " + name + "!";
        }
    }

    // ========================
    // @Deprecated demonstration
    // ========================

    /**
     * Class demonstrating @Deprecated with different configurations.
     */
    public static class LegacyApi {

        /**
         * Old method deprecated with a replacement suggestion.
         */
        @Deprecated(since = "2.0", forRemoval = true)
        public String oldMethod() {
            return "old result";
        }

        /**
         * New replacement method.
         */
        public String newMethod() {
            return "new result";
        }

        /**
         * Deprecated with only 'since' — not marked for removal.
         */
        @Deprecated(since = "1.5")
        public int legacyCalculation(int a, int b) {
            return a + b;
        }

        /**
         * Modern replacement.
         */
        public int modernCalculation(int a, int b) {
            return Math.addExact(a, b);
        }
    }

    // ========================
    // @SuppressWarnings demonstration
    // ========================

    /**
     * Demonstrates @SuppressWarnings with various warning types.
     */
    public static class WarningSuppressionExamples {

        /**
         * Suppresses unchecked cast warnings.
         */
        @SuppressWarnings("unchecked")
        public List<String> unsafeCast(Object obj) {
            return (List<String>) obj;
        }

        /**
         * Suppresses deprecation warnings when intentionally using deprecated API.
         */
        @SuppressWarnings("deprecation")
        public String callDeprecatedMethod() {
            LegacyApi api = new LegacyApi();
            return api.oldMethod();
        }

        /**
         * Suppresses multiple warnings.
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        public List rawToTyped(List rawList) {
            return new ArrayList<String>(rawList);
        }
    }

    // ========================
    // @FunctionalInterface demonstration
    // ========================

    /**
     * Valid functional interface with exactly one abstract method.
     */
    @FunctionalInterface
    public interface Transformer<T, R> {
        R transform(T input);

        // Default methods are allowed
        default <V> Transformer<T, V> andThen(Transformer<R, V> after) {
            return input -> after.transform(this.transform(input));
        }
    }

    /**
     * Another valid functional interface — inherits from Object class methods are allowed.
     */
    @FunctionalInterface
    public interface Validator<T> {
        boolean validate(T input);

        // toString() from Object doesn't count as an abstract method
    }

    /**
     * Utility to demonstrate functional interface usage with lambdas.
     */
    public static class TransformerUtils {

        public static <T, R> R applyTransformation(T input, Transformer<T, R> transformer) {
            return transformer.transform(input);
        }

        public static <T> boolean applyValidation(T input, Validator<T> validator) {
            return validator.validate(input);
        }
    }

    // ========================
    // @SafeVarargs demonstration
    // ========================

    /**
     * Demonstrates @SafeVarargs with generic varargs methods.
     */
    public static class SafeVarargsExamples {

        @SafeVarargs
        public static <T> List<T> safeListOf(T... elements) {
            List<T> list = new ArrayList<>();
            for (T element : elements) {
                list.add(element);
            }
            return list;
        }

        @SafeVarargs
        public final <T> List<List<T>> groupElements(T[]... arrays) {
            List<List<T>> result = new ArrayList<>();
            for (T[] array : arrays) {
                result.add(List.of(array));
            }
            return result;
        }
    }
}
