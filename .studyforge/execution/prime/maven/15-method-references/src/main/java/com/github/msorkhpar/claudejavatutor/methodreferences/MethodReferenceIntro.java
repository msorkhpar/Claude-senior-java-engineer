package com.github.msorkhpar.claudejavatutor.methodreferences;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Demonstrates the introduction to method references in Java.
 * Shows the relationship between lambdas and method references,
 * basic syntax, and common usage patterns.
 *
 * @see README_4.3.1.md
 */
public class MethodReferenceIntro {

    /**
     * Shows the evolution from anonymous inner class to lambda to method reference.
     */
    public static class LambdaToMethodRefEvolution {

        /**
         * Sort using anonymous inner class — pre-Java 8 style.
         */
        public List<String> sortAnonymous(List<String> names) {
            List<String> result = new ArrayList<>(names);
            Collections.sort(result, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s1.compareTo(s2);
                }
            });
            return result;
        }

        /**
         * Sort using a lambda expression.
         */
        public List<String> sortLambda(List<String> names) {
            List<String> result = new ArrayList<>(names);
            result.sort((s1, s2) -> s1.compareTo(s2));
            return result;
        }

        /**
         * Sort using a method reference — most concise and readable.
         */
        public List<String> sortMethodReference(List<String> names) {
            List<String> result = new ArrayList<>(names);
            result.sort(String::compareTo);
            return result;
        }

        /**
         * All three produce identical results.
         */
        public boolean allProduceSameResult(List<String> names) {
            return sortAnonymous(names).equals(sortLambda(names)) &&
                   sortLambda(names).equals(sortMethodReference(names));
        }
    }

    /**
     * Demonstrates that method references are equivalent to their lambda counterparts.
     */
    public static class LambdaEquivalence {

        /**
         * Returns the length using a lambda expression.
         */
        public Function<String, Integer> lengthViaLambda() {
            return s -> s.length();
        }

        /**
         * Returns the length using a method reference.
         * Equivalent to the lambda above.
         */
        public Function<String, Integer> lengthViaMethodRef() {
            return String::length;
        }

        /**
         * Prints using a lambda expression.
         */
        public Consumer<String> printViaLambda() {
            return s -> System.out.println(s);
        }

        /**
         * Prints using a method reference — bound instance reference to System.out.
         */
        public Consumer<String> printViaMethodRef() {
            return System.out::println;
        }

        /**
         * Filters non-null values using a lambda expression.
         */
        public Predicate<Object> nonNullViaLambda() {
            return obj -> obj != null;
        }

        /**
         * Filters non-null values using a static method reference.
         */
        public Predicate<Object> nonNullViaMethodRef() {
            return Objects::nonNull;
        }

        /**
         * Creates an ArrayList using a lambda expression.
         */
        public Supplier<ArrayList<String>> listFactoryViaLambda() {
            return () -> new ArrayList<>();
        }

        /**
         * Creates an ArrayList using a constructor reference.
         */
        public Supplier<ArrayList<String>> listFactoryViaConstructorRef() {
            return ArrayList::new;
        }
    }

    /**
     * Demonstrates null safety when using method references in stream pipelines.
     */
    public static class NullSafeMethodRefUsage {

        /**
         * Gets uppercase strings, safely skipping nulls.
         *
         * @param input list that may contain null elements
         * @return list of uppercase non-null strings
         */
        public List<String> toUpperCaseSafe(List<String> input) {
            if (input == null) {
                return Collections.emptyList();
            }
            return input.stream()
                    .filter(Objects::nonNull)      // method reference for null check
                    .map(String::toUpperCase)       // method reference for transformation
                    .collect(Collectors.toList());
        }

        /**
         * Collects string lengths, filtering out nulls and blanks.
         *
         * @param input list of strings
         * @return list of lengths for non-null, non-blank strings
         */
        public List<Integer> getLengthsNonBlank(List<String> input) {
            if (input == null) {
                return Collections.emptyList();
            }
            return input.stream()
                    .filter(Objects::nonNull)
                    .filter(Predicate.not(String::isBlank))
                    .map(String::length)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Demonstrates exception handling considerations with method references.
     */
    public static class ExceptionHandlingWithMethodRefs {

        /**
         * Parses integers using a method reference (only works if no exceptions expected).
         * For a clean list of valid strings, Integer::parseInt works perfectly.
         */
        public List<Integer> parseAllValid(List<String> validNumbers) {
            return validNumbers.stream()
                    .map(Integer::parseInt)    // static method reference
                    .collect(Collectors.toList());
        }

        /**
         * Parses integers using a helper method reference that handles exceptions.
         * This pattern allows using a method reference even with checked/runtime exceptions.
         */
        public List<Integer> parseSafely(List<String> numbers) {
            return numbers.stream()
                    .map(ExceptionHandlingWithMethodRefs::tryParseInt)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        /**
         * Safe integer parsing helper — returns null for invalid strings.
         */
        public static Integer tryParseInt(String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    /**
     * Demonstrates how method references improve readability in real-world scenarios.
     */
    public static class ReadabilityDemo {

        record Person(String firstName, String lastName, int age, boolean active) {}

        /**
         * Extracts the full names of active adults, sorted alphabetically.
         * Uses method references throughout for maximum readability.
         */
        public List<String> getActiveAdultFullNames(List<Person> people) {
            return people.stream()
                    .filter(Objects::nonNull)
                    .filter(Person::active)
                    .filter(p -> p.age() >= 18)
                    .sorted(Comparator.comparing(Person::lastName)
                            .thenComparing(Person::firstName))
                    .map(p -> p.firstName() + " " + p.lastName())
                    .collect(Collectors.toList());
        }

        /**
         * Demonstrates System.out::println as a Consumer in forEach.
         */
        public void printAll(List<String> items) {
            items.stream()
                    .filter(Objects::nonNull)
                    .forEach(System.out::println);
        }

        /**
         * Demonstrates method references to extract properties for grouping.
         */
        public Map<Boolean, List<Person>> partitionByActive(List<Person> people) {
            return people.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.partitioningBy(Person::active));
        }
    }
}
