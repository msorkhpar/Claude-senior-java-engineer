package com.github.msorkhpar.claudejavatutor.lambdaexpressions;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Demonstrates the introduction to lambda expressions in Java.
 * Covers basic lambda concepts, closures, and common use cases.
 */
public class LambdaIntroduction {

    /**
     * Demonstrates the evolution from anonymous inner classes to lambda expressions
     */
    public static class AnonymousToLambda {

        /**
         * Traditional approach using anonymous inner class
         */
        public List<String> sortWithAnonymousClass(List<String> names) {
            List<String> sorted = new ArrayList<>(names);
            Collections.sort(sorted, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s1.compareTo(s2);
                }
            });
            return sorted;
        }

        /**
         * Modern approach using lambda expression
         */
        public List<String> sortWithLambda(List<String> names) {
            List<String> sorted = new ArrayList<>(names);
            sorted.sort((s1, s2) -> s1.compareTo(s2));
            return sorted;
        }

        /**
         * Even more concise with method reference
         */
        public List<String> sortWithMethodReference(List<String> names) {
            List<String> sorted = new ArrayList<>(names);
            sorted.sort(String::compareTo);
            return sorted;
        }
    }

    /**
     * Demonstrates lambda expressions with different functional interfaces
     */
    public static class FunctionalInterfaceExamples {

        /**
         * Uses Consumer to process each element (takes input, no output)
         */
        public void printAllUpperCase(List<String> strings) {
            Consumer<String> printUpper = s -> System.out.println(s.toUpperCase());
            strings.forEach(printUpper);
        }

        /**
         * Uses Supplier to generate values (no input, returns output)
         */
        public List<String> generateIds(int count) {
            Supplier<String> idGenerator = () -> UUID.randomUUID().toString();
            return java.util.stream.IntStream.range(0, count)
                    .mapToObj(i -> idGenerator.get())
                    .collect(Collectors.toList());
        }

        /**
         * Uses Function to transform values (takes input, returns output)
         */
        public List<Integer> getLengths(List<String> strings) {
            Function<String, Integer> getLength = s -> s.length();
            return strings.stream()
                    .map(getLength)
                    .collect(Collectors.toList());
        }

        /**
         * Uses Predicate to filter values (takes input, returns boolean)
         */
        public List<String> filterLongStrings(List<String> strings, int minLength) {
            Predicate<String> isLong = s -> s.length() >= minLength;
            return strings.stream()
                    .filter(isLong)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Demonstrates variable capture and effectively final concept
     */
    public static class VariableCapture {

        private int instanceVariable = 10;

        /**
         * Demonstrates capturing effectively final local variables
         */
        public List<Integer> multiplyAll(List<Integer> numbers, int multiplier) {
            // multiplier is effectively final - can be captured
            return numbers.stream()
                    .map(n -> n * multiplier)
                    .collect(Collectors.toList());
        }

        /**
         * Demonstrates capturing instance variables (can be modified)
         */
        public Consumer<Integer> createAccumulator() {
            return n -> {
                instanceVariable += n;  // Can modify instance variable
            };
        }

        /**
         * Demonstrates workaround for modifying local variables using AtomicInteger
         */
        public int countMatches(List<String> strings, Predicate<String> condition) {
            AtomicInteger count = new AtomicInteger(0);
            strings.forEach(s -> {
                if (condition.test(s)) {
                    count.incrementAndGet();
                }
            });
            return count.get();
        }

        /**
         * Better approach: use stream operations instead of mutation
         */
        public long countMatchesWithStream(List<String> strings, Predicate<String> condition) {
            return strings.stream()
                    .filter(condition)
                    .count();
        }
    }

    /**
     * Demonstrates exception handling in lambda expressions
     */
    public static class ExceptionHandling {

        /**
         * Handles exceptions inside the lambda
         */
        public List<Integer> parseNumbers(List<String> strings) {
            List<Integer> result = new ArrayList<>();
            strings.forEach(s -> {
                try {
                    result.add(Integer.parseInt(s));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid number: " + s);
                }
            });
            return result;
        }

        /**
         * Uses Optional to handle exceptions functionally
         */
        public List<Integer> parseNumbersWithOptional(List<String> strings) {
            return strings.stream()
                    .map(this::tryParse)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }

        private Optional<Integer> tryParse(String s) {
            try {
                return Optional.of(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }

        /**
         * Wraps checked exceptions in a functional interface
         */
        @FunctionalInterface
        public interface CheckedFunction<T, R> {
            R apply(T t) throws Exception;
        }

        public static <T, R> Function<T, R> wrapException(CheckedFunction<T, R> function) {
            return t -> {
                try {
                    return function.apply(t);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    /**
     * Demonstrates closure behavior in lambda expressions
     */
    public static class ClosureExamples {

        /**
         * Creates a closure that captures the multiplier
         */
        public Function<Integer, Integer> createMultiplier(int multiplier) {
            return n -> n * multiplier;  // Captures multiplier
        }

        /**
         * Creates a predicate that checks if value is in range
         */
        public Predicate<Integer> createRangeChecker(int min, int max) {
            return n -> n >= min && n <= max;  // Captures min and max
        }

        /**
         * Creates a filter function that captures search criteria
         */
        public Function<List<String>, List<String>> createFilter(String prefix, int minLength) {
            return list -> list.stream()
                    .filter(s -> s.startsWith(prefix))  // Captures prefix
                    .filter(s -> s.length() >= minLength)  // Captures minLength
                    .collect(Collectors.toList());
        }
    }

    /**
     * Demonstrates real-world lambda expression use cases
     */
    public static class RealWorldExamples {

        record User(String name, int age, boolean active, LocalDate registrationDate) {
        }

        /**
         * Filters and transforms user data
         */
        public List<String> getActiveAdultNames(List<User> users) {
            return users.stream()
                    .filter(user -> user.active())
                    .filter(user -> user.age() >= 18)
                    .map(User::name)
                    .sorted()
                    .collect(Collectors.toList());
        }

        /**
         * Groups users by age range
         */
        public Map<String, List<User>> groupByAgeRange(List<User> users) {
            return users.stream()
                    .collect(Collectors.groupingBy(user -> {
                        if (user.age() < 18) return "Minor";
                        else if (user.age() < 65) return "Adult";
                        else return "Senior";
                    }));
        }

        /**
         * Calculates statistics using lambda expressions
         */
        public record Statistics(double average, int count, int min, int max) {
        }

        public Statistics calculateAgeStatistics(List<User> users) {
            IntSummaryStatistics stats = users.stream()
                    .mapToInt(User::age)
                    .summaryStatistics();

            return new Statistics(
                    stats.getAverage(),
                    (int) stats.getCount(),
                    stats.getMin(),
                    stats.getMax()
            );
        }

        /**
         * Chains multiple operations
         */
        public List<String> processUsers(List<User> users) {
            return users.stream()
                    .filter(this::isEligible)
                    .map(this::formatUserInfo)
                    .sorted()
                    .limit(10)
                    .collect(Collectors.toList());
        }

        private boolean isEligible(User user) {
            return user.active() &&
                    user.age() >= 18 &&
                    user.registrationDate().isBefore(LocalDate.now().minusMonths(1));
        }

        private String formatUserInfo(User user) {
            return String.format("%s (%d years old)", user.name(), user.age());
        }
    }

    /**
     * Demonstrates performance considerations with lambda expressions
     */
    public static class PerformanceConsiderations {

        /**
         * Demonstrates lambda caching for reusability
         */
        private static final Comparator<String> LENGTH_COMPARATOR =
                (s1, s2) -> Integer.compare(s1.length(), s2.length());

        private static final Predicate<String> IS_NOT_EMPTY = s -> !s.isEmpty();

        public List<String> sortByLength(List<String> strings) {
            List<String> result = new ArrayList<>(strings);
            result.sort(LENGTH_COMPARATOR);  // Reuse cached lambda
            return result;
        }

        /**
         * Uses primitive stream for better performance
         */
        public int sumLengths(List<String> strings) {
            return strings.stream()
                    .mapToInt(String::length)  // Primitive stream avoids boxing
                    .sum();
        }

        /**
         * Demonstrates when to avoid lambdas in hot paths
         */
        public int countLongStringsOptimized(List<String> strings, int minLength) {
            // For simple operations in tight loops, traditional approach might be faster
            int count = 0;
            for (String s : strings) {
                if (s.length() >= minLength) {
                    count++;
                }
            }
            return count;
        }

        /**
         * Uses parallel stream for large datasets
         */
        public List<Integer> processLargeDataset(List<String> largeList) {
            return largeList.parallelStream()
                    .filter(IS_NOT_EMPTY)
                    .map(String::length)
                    .collect(Collectors.toList());
        }
    }
}