package com.github.msorkhpar.claudejavatutor.methodreferences;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Demonstrates all four types of method references in Java:
 * 1. Static method reference: ClassName::staticMethod
 * 2. Bound instance method reference: instance::instanceMethod
 * 3. Unbound instance method reference: ClassName::instanceMethod
 * 4. Constructor reference: ClassName::new
 *
 * @see README_4.3.2.md
 */
public class MethodReferenceTypes {

    // ─────────────────────────────────────────────────
    // Type 1: Static Method Reference
    // ─────────────────────────────────────────────────

    /**
     * Demonstrates static method references.
     * ClassName::staticMethod — the functional interface args become the method's args.
     */
    public static class StaticMethodReferences {

        /**
         * Parse strings to integers using a static method reference.
         * Integer::parseInt is a static method: static int parseInt(String s)
         */
        public List<Integer> parseIntegers(List<String> strings) {
            return strings.stream()
                    .map(Integer::parseInt)    // static: Integer.parseInt(String)
                    .collect(Collectors.toList());
        }

        /**
         * Compute absolute values using Math::abs static method reference.
         */
        public List<Integer> absoluteValues(List<Integer> numbers) {
            return numbers.stream()
                    .map(Math::abs)           // static: Math.abs(int)
                    .collect(Collectors.toList());
        }

        /**
         * Filter non-null elements using Objects::nonNull static method reference.
         */
        public <T> List<T> filterNulls(List<T> items) {
            return items.stream()
                    .filter(Objects::nonNull)  // static: Objects.nonNull(Object)
                    .collect(Collectors.toList());
        }

        /**
         * Compare two integers using Integer::compare static method reference.
         * Demonstrates static method reference as a Comparator.
         */
        public List<Integer> sortAscending(List<Integer> numbers) {
            return numbers.stream()
                    .sorted(Integer::compare)  // static: Integer.compare(int, int)
                    .collect(Collectors.toList());
        }

        /**
         * Convert strings to integers (or 0 on failure) using a custom static method ref.
         */
        public static int safeParseInt(String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        public List<Integer> safeParseAll(List<String> strings) {
            return strings.stream()
                    .map(StaticMethodReferences::safeParseInt) // static method in this class
                    .collect(Collectors.toList());
        }
    }

    // ─────────────────────────────────────────────────
    // Type 2: Bound Instance Method Reference
    // ─────────────────────────────────────────────────

    /**
     * Demonstrates bound instance method references.
     * instance::instanceMethod — the receiver is fixed at creation time.
     */
    public static class BoundInstanceMethodReferences {

        /**
         * Creates a Predicate that tests whether each candidate is a prefix of a fixed text.
         * The 'text' object is captured and fixed at reference creation time, so
         * text::startsWith means text.startsWith(candidate). Testing whether each element
         * starts with a fixed prefix is the other direction and needs a lambda:
         * s -> s.startsWith(prefix).
         */
        public Predicate<String> isPrefixOf(String text) {
            return text::startsWith;    // bound: text is the receiver, each candidate is the argument
        }

        /**
         * Creates a filter that keeps strings contained in a fixed collection.
         * The captured List IS the receiver; each tested string is the argument.
         */
        public Predicate<String> containedInCollection(List<String> validValues) {
            // validValues::contains — bound ref: the List object is fixed;
            // each tested string is the argument to contains
            return validValues::contains;
        }

        /**
         * Demonstrates System.out::println as a bound Consumer.
         * System.out is the fixed receiver; each printed value is the argument.
         */
        public void printAll(List<String> items) {
            items.forEach(System.out::println);
        }

        /**
         * Demonstrates a bound instance ref to a specific StringBuilder.
         * The StringBuilder is the fixed receiver; appended strings are the arguments.
         */
        public Consumer<String> appendToBuilder(StringBuilder sb) {
            return sb::append;
        }

        /**
         * Demonstrates bound instance method reference with a comparator object.
         * The comparator instance is fixed; each compare call receives two strings.
         */
        public List<String> sortCaseInsensitive(List<String> strings) {
            Comparator<String> cmp = String.CASE_INSENSITIVE_ORDER;
            return strings.stream()
                    .sorted(cmp::compare)    // bound: cmp is fixed; (s1,s2) are the args
                    .collect(Collectors.toList());
        }

        /**
         * Demonstrates a bound Supplier: a specific string's toUpperCase.
         * The string 'fixed' is the receiver; no arguments needed.
         */
        public Supplier<String> createUpperCaseSupplier(String fixed) {
            return fixed::toUpperCase;  // bound: fixed is the receiver, no args
        }
    }

    // ─────────────────────────────────────────────────
    // Type 3: Unbound Instance Method Reference
    // ─────────────────────────────────────────────────

    /**
     * Demonstrates unbound instance method references.
     * ClassName::instanceMethod — the receiver is the first functional interface argument.
     */
    public static class UnboundInstanceMethodReferences {

        /**
         * Get lengths of strings using an unbound instance method reference.
         * String::length — receiver is each String, no additional args.
         */
        public List<Integer> getLengths(List<String> strings) {
            return strings.stream()
                    .map(String::length)        // unbound: each String is the receiver
                    .collect(Collectors.toList());
        }

        /**
         * Convert all strings to uppercase using an unbound instance method reference.
         */
        public List<String> toUpperCase(List<String> strings) {
            return strings.stream()
                    .map(String::toUpperCase)   // unbound: each String is the receiver
                    .collect(Collectors.toList());
        }

        /**
         * Sort strings alphabetically using an unbound Comparator method reference.
         * String::compareTo takes two Strings: the receiver + one argument.
         * This maps to Comparator<String>.compare(String s1, String s2).
         */
        public List<String> sortAlphabetically(List<String> strings) {
            return strings.stream()
                    .sorted(String::compareTo)  // unbound: s1.compareTo(s2) pattern
                    .collect(Collectors.toList());
        }

        /**
         * Filter empty lists using List::isEmpty as an unbound Predicate.
         */
        public List<List<String>> filterNonEmpty(List<List<String>> lists) {
            return lists.stream()
                    .filter(Predicate.not(List::isEmpty)) // unbound: each List is the receiver
                    .collect(Collectors.toList());
        }

        /**
         * Trim all strings using an unbound instance method reference.
         */
        public List<String> trimAll(List<String> strings) {
            return strings.stream()
                    .map(String::trim)          // unbound: each String is the receiver
                    .collect(Collectors.toList());
        }

        /**
         * Check if strings are blank using an unbound Predicate method reference.
         */
        public List<String> filterBlanks(List<String> strings) {
            return strings.stream()
                    .filter(Predicate.not(String::isBlank)) // unbound Predicate
                    .collect(Collectors.toList());
        }
    }

    // ─────────────────────────────────────────────────
    // Type 4: Constructor Reference
    // ─────────────────────────────────────────────────

    /**
     * Demonstrates constructor references.
     * ClassName::new — creates new instances using the matching constructor.
     */
    public static class ConstructorReferences {

        /**
         * Creates StringBuilders from strings using a constructor reference.
         * StringBuilder::new matches StringBuilder(String) constructor.
         */
        public List<StringBuilder> wrapInStringBuilders(List<String> strings) {
            return strings.stream()
                    .map(StringBuilder::new)    // constructor ref: new StringBuilder(String)
                    .collect(Collectors.toList());
        }

        /**
         * Creates a factory for ArrayList using a no-arg constructor reference.
         */
        public Supplier<ArrayList<String>> createListFactory() {
            return ArrayList::new;              // constructor ref: new ArrayList<>()
        }

        /**
         * Creates a list of ArrayLists using a no-arg constructor reference.
         */
        public List<ArrayList<String>> createEmptyLists(int count) {
            Supplier<ArrayList<String>> factory = ArrayList::new;
            List<ArrayList<String>> result = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                result.add(factory.get());
            }
            return result;
        }

        /**
         * Collects stream elements into a typed array using an array constructor reference.
         * String[]::new matches the IntFunction<String[]> required by toArray.
         */
        public String[] toArray(List<String> strings) {
            return strings.stream()
                    .toArray(String[]::new);    // array constructor reference
        }

        /**
         * Demonstrates a custom class constructor reference.
         */
        public record Point(double x, double y) {}

        /**
         * Creates Point objects from two coordinates using a constructor reference.
         * Since Point has a two-arg constructor, BiFunction is needed.
         */
        public BiFunction<Double, Double, Point> createPointFactory() {
            return Point::new;                  // constructor ref: new Point(double, double)
        }

        /**
         * Generates 'count' new ArrayLists using constructor reference as factory.
         */
        public <T> List<List<T>> generateBuckets(int count) {
            Supplier<List<T>> factory = ArrayList::new;
            List<List<T>> buckets = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                buckets.add(factory.get());
            }
            return buckets;
        }
    }

    // ─────────────────────────────────────────────────
    // Combining all four types
    // ─────────────────────────────────────────────────

    /**
     * Demonstrates method reference types combined in a realistic processing pipeline.
     */
    public static class CombinedTypesDemo {

        record Product(String name, double price) {}

        /**
         * Processes raw price strings into formatted product lines.
         * Uses:
         * 1. Static: Objects::nonNull
         * 3. Unbound: String::trim, String::isEmpty
         * and a lambda to combine the prefix with each price (two values to combine).
         */
        public List<String> processRawData(List<String> rawPrices, String productPrefix) {
            return rawPrices.stream()
                    .filter(Objects::nonNull)              // 1. Static: Objects::nonNull
                    .map(String::trim)                     // 3. Unbound: String::trim
                    .filter(Predicate.not(String::isEmpty)) // 3. Unbound: String::isEmpty
                    .map(s -> productPrefix + ": " + s)   // lambda (two parts to combine)
                    .collect(Collectors.toList());
        }

        /**
         * Sorts products by price using Comparator with method references.
         */
        public List<Product> sortProductsByPrice(List<Product> products) {
            return products.stream()
                    .filter(Objects::nonNull)              // 1. Static
                    .sorted(Comparator.comparingDouble(Product::price)) // 3. Unbound
                    .collect(Collectors.toList());
        }
    }
}
