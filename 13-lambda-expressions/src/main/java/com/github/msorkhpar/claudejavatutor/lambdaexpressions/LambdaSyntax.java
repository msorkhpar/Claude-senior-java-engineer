package com.github.msorkhpar.claudejavatutor.lambdaexpressions;

import java.io.PrintStream;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Demonstrates different syntax variations and structures of lambda expressions.
 * Covers parameter styles, body styles, and type inference.
 */
public class LambdaSyntax {

    /**
     * Demonstrates all parameter variations
     */
    public static class ParameterVariations {

        /**
         * No parameters - must use empty parentheses
         */
        public String generateGreeting() {
            Supplier<String> greeting = () -> "Hello, World!";
            return greeting.get();
        }

        /**
         * Single parameter - parentheses optional (without type)
         */
        public List<Integer> getLengths(List<String> strings) {
            // Style 1: No parentheses, no type
            Function<String, Integer> style1 = s -> s.length();

            // Style 2: With parentheses, no type
            Function<String, Integer> style2 = (s) -> s.length();

            // Style 3: With type (requires parentheses)
            Function<String, Integer> style3 = (String s) -> s.length();

            return strings.stream().map(style1).collect(Collectors.toList());
        }

        /**
         * Multiple parameters - parentheses required
         */
        public Map<String, Integer> combineToMap(List<String> keys, List<Integer> values) {
            BiFunction<String, Integer, Map.Entry<String, Integer>> combiner =
                    (key, value) -> Map.entry(key, value);

            Map<String, Integer> result = new HashMap<>();
            for (int i = 0; i < Math.min(keys.size(), values.size()); i++) {
                Map.Entry<String, Integer> entry = combiner.apply(keys.get(i), values.get(i));
                result.put(entry.getKey(), entry.getValue());
            }
            return result;
        }

        /**
         * Multiple parameters with explicit types
         */
        public int calculateSum(List<Integer> numbers) {
            BinaryOperator<Integer> sum = (Integer a, Integer b) -> a + b;
            return numbers.stream().reduce(0, sum);
        }

        /**
         * Demonstrates when explicit types improve clarity
         */
        public <T> List<T> filterAndTransform(
                List<String> strings,
                Predicate<String> filter,
                Function<String, T> transformer) {
            return strings.stream()
                    .filter(filter)
                    .map(transformer)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Demonstrates expression body vs block body
     */
    public static class BodyVariations {

        /**
         * Expression body - single expression, implicit return
         */
        public List<Integer> squareNumbers(List<Integer> numbers) {
            Function<Integer, Integer> square = n -> n * n;
            return numbers.stream().map(square).collect(Collectors.toList());
        }

        /**
         * Expression body with method call
         */
        public List<String> toUpperCase(List<String> strings) {
            Function<String, String> upper = s -> s.toUpperCase();
            return strings.stream().map(upper).collect(Collectors.toList());
        }

        /**
         * Expression body with ternary operator
         */
        public List<String> classifyNumbers(List<Integer> numbers) {
            Function<Integer, String> classify =
                    n -> n > 0 ? "positive" : (n < 0 ? "negative" : "zero");
            return numbers.stream().map(classify).collect(Collectors.toList());
        }

        /**
         * Block body - multiple statements, explicit return
         */
        public List<String> formatNumbers(List<Integer> numbers) {
            Function<Integer, String> format = n -> {
                String sign = n >= 0 ? "+" : "";
                String value = String.valueOf(n);
                return sign + value;
            };
            return numbers.stream().map(format).collect(Collectors.toList());
        }

        /**
         * Block body with local variables
         */
        public List<Double> calculateDiscountedPrices(List<Double> prices, double discountPercent) {
            Function<Double, Double> applyDiscount = price -> {
                double discount = price * (discountPercent / 100);
                double finalPrice = price - discount;
                return Math.round(finalPrice * 100.0) / 100.0;  // Round to 2 decimals
            };
            return prices.stream().map(applyDiscount).collect(Collectors.toList());
        }

        /**
         * Block body with conditional logic
         */
        public List<String> categorizePrices(List<Double> prices) {
            Function<Double, String> categorize = price -> {
                if (price < 10) return "cheap";
                else if (price < 100) return "moderate";
                else if (price < 1000) return "expensive";
                else return "premium";
            };
            return prices.stream().map(categorize).collect(Collectors.toList());
        }

        /**
         * Void return type - no return statement needed
         */
        public void printWithPrefix(List<String> items, String prefix) {
            Consumer<String> printer = item -> {
                String formatted = prefix + item;
                System.out.println(formatted);
            };
            items.forEach(printer);
        }

        /**
         * Early return in block body
         */
        public List<String> validateAndTransform(List<String> strings) {
            Function<String, String> processor = s -> {
                if (s == null) return "NULL";
                if (s.isEmpty()) return "EMPTY";
                return s.trim().toUpperCase();
            };
            return strings.stream().map(processor).collect(Collectors.toList());
        }
    }

    /**
     * Demonstrates type inference and target typing
     */
    public static class TypeInference {

        /**
         * Compiler infers types from variable declaration
         */
        public int sumNumbers(List<Integer> numbers) {
            BinaryOperator<Integer> adder = (a, b) -> a + b;
            return numbers.stream().reduce(0, adder);
        }

        /**
         * Compiler infers types from method parameter
         */
        public List<String> transformStrings(List<String> strings, Function<String, String> transformer) {
            return strings.stream().map(transformer).collect(Collectors.toList());
        }

        public List<String> useTransform(List<String> strings) {
            // Type inferred from method parameter
            return transformStrings(strings, s -> s.toUpperCase());
        }

        /**
         * Compiler infers types from return type
         */
        public Predicate<Integer> createRangePredicate(int min, int max) {
            return n -> n >= min && n <= max;  // Types inferred from Predicate<Integer>
        }

        /**
         * Explicit types can improve clarity in complex scenarios
         */
        public void demonstrateExplicitTypes() {
            // Explicit types for clarity
            Function<String, Integer> lengthFunction = (String s) -> s.length();

            // Can be used with the function
            List<String> strings = List.of("hello", "world");
            strings.stream().map(lengthFunction).forEach(System.out::println);
        }

        /**
         * Generic type inference
         */
        public <T> List<T> filterByCondition(List<T> items, Predicate<T> condition) {
            return items.stream()
                    .filter(condition)  // T inferred from context
                    .collect(Collectors.toList());
        }

        /**
         * Complex generic type inference
         */
        public Map<String, List<Integer>> groupAndTransform(List<String> strings) {
            return strings.stream()
                    .collect(Collectors.groupingBy(
                            s -> s.substring(0, 1),  // Key mapper
                            Collectors.mapping(
                                    s -> s.length(),  // Value mapper
                                    Collectors.toList()
                            )
                    ));
        }
    }

    /**
     * Demonstrates method reference as lambda alternative
     */
    public static class MethodReferences {

        /**
         * Instance method reference
         */
        public List<String> toUpperCaseWithReference(List<String> strings) {
            return strings.stream()
                    .map(String::toUpperCase)  // Equivalent to: s -> s.toUpperCase()
                    .collect(Collectors.toList());
        }

        /**
         * Static method reference
         */
        public List<Integer> parseIntegers(List<String> strings) {
            return strings.stream()
                    .map(Integer::parseInt)  // Equivalent to: s -> Integer.parseInt(s)
                    .collect(Collectors.toList());
        }

        /**
         * Constructor reference
         */
        public List<StringBuilder> createBuilders(List<String> strings) {
            return strings.stream()
                    .map(StringBuilder::new)  // Equivalent to: s -> new StringBuilder(s)
                    .collect(Collectors.toList());
        }

        /**
         * Method reference on specific object
         */
        public void printAll(List<String> strings) {
            PrintStream out = System.out;
            strings.forEach(out::println);  // Equivalent to: s -> out.println(s)
        }

        /**
         * Comparing lambda vs method reference readability
         */
        public static class Person {
            private String name;
            private int age;

            public Person(String name, int age) {
                this.name = name;
                this.age = age;
            }

            public String getName() {
                return name;
            }

            public int getAge() {
                return age;
            }
        }

        public List<String> getPersonNames(List<Person> people) {
            // Lambda style
            List<String> names1 = people.stream()
                    .map(p -> p.getName())
                    .collect(Collectors.toList());

            // Method reference style (more concise)
            List<String> names2 = people.stream()
                    .map(Person::getName)
                    .collect(Collectors.toList());

            return names2;
        }
    }

    /**
     * Demonstrates common syntax mistakes and corrections
     */
    public static class CommonMistakes {

        /**
         * Correct: Single expression body without braces or return
         */
        public Function<Integer, Integer> correctDoubleValue() {
            return x -> x * 2;
        }

        /**
         * Correct: Block body with explicit return
         */
        public Function<Integer, Integer> correctDoubleValueWithBlock() {
            return x -> {
                return x * 2;
            };
        }

        /**
         * Correct: Multiple parameters with parentheses
         */
        public BiFunction<Integer, Integer, Integer> correctSum() {
            return (a, b) -> a + b;
        }

        /**
         * Correct: No parameters with empty parentheses
         */
        public Supplier<Integer> correctRandomNumber() {
            return () -> (int) (Math.random() * 100);
        }

        /**
         * Correct: Void return with no return statement
         */
        public Consumer<String> correctPrint() {
            return s -> System.out.println(s);
        }

        /**
         * Correct: Void return with block and no return
         */
        public Consumer<String> correctPrintWithBlock() {
            return s -> {
                String upper = s.toUpperCase();
                System.out.println(upper);
            };
        }

        /**
         * Demonstrates proper formatting for readability
         */
        public Function<String, String> wellFormattedLambda() {
            return input -> {
                String trimmed = input.trim();
                String lower = trimmed.toLowerCase();
                String replaced = lower.replaceAll("\\s+", "_");
                return replaced;
            };
        }

        /**
         * Demonstrates when to break long lambda chains
         */
        public List<String> processStrings(List<String> strings) {
            return strings.stream()
                    .filter(s -> s != null)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toLowerCase)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    /**
     * Demonstrates advanced syntax patterns
     */
    public static class AdvancedPatterns {

        /**
         * Lambda returning lambda
         */
        public Function<Integer, Function<Integer, Integer>> createAdder() {
            return x -> (y -> x + y);
        }

        public int useNestedLambda() {
            Function<Integer, Function<Integer, Integer>> adder = createAdder();
            Function<Integer, Integer> add5 = adder.apply(5);
            return add5.apply(3);  // Returns 8
        }

        /**
         * Lambda with generic parameters
         */
        public <T> Predicate<T> createNullSafePredicate(Predicate<T> predicate) {
            return item -> item != null && predicate.test(item);
        }

        /**
         * Currying with lambdas
         */
        public Function<String, Function<String, String>> createFormatter() {
            return prefix -> (suffix -> prefix + ":" + suffix);
        }

        /**
         * Combining multiple lambdas
         */
        public List<String> processWithMultipleLambdas(List<String> strings) {
            Predicate<String> notEmpty = s -> !s.isEmpty();
            Function<String, String> toUpper = String::toUpperCase;
            Predicate<String> startsWithA = s -> s.startsWith("A");

            return strings.stream()
                    .filter(notEmpty)
                    .map(toUpper)
                    .filter(startsWithA)
                    .collect(Collectors.toList());
        }

        /**
         * Lambda with intersection types (implicit)
         */
        public <T extends Comparable<T>> List<T> sortAndFilter(
                List<T> items,
                Predicate<T> filter) {
            return items.stream()
                    .filter(filter)
                    .sorted()  // Uses Comparable
                    .collect(Collectors.toList());
        }
    }
}