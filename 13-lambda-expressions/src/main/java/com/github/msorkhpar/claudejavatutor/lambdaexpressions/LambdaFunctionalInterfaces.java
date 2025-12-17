package com.github.msorkhpar.claudejavatutor.lambdaexpressions;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Demonstrates using lambda expressions with functional interfaces.
 * Covers standard functional interfaces, custom interfaces, and composition.
 */
public class LambdaFunctionalInterfaces {

    /**
     * Demonstrates standard functional interfaces from java.util.function
     */
    public static class StandardInterfaces {

        /**
         * Consumer<T> - Takes input, returns nothing (side effects)
         */
        public void demonstrateConsumer(List<String> items) {
            Consumer<String> print = System.out::println;
            Consumer<String> logLength = s -> System.out.println("Length: " + s.length());
            Consumer<String> combined = print.andThen(logLength);

            items.forEach(combined);
        }

        /**
         * Supplier<T> - Takes nothing, returns output
         */
        public List<String> generateRandomIds(int count) {
            Supplier<String> idGenerator = () ->
                    "ID-" + UUID.randomUUID().toString().substring(0, 8);

            return java.util.stream.IntStream.range(0, count)
                    .mapToObj(i -> idGenerator.get())
                    .collect(Collectors.toList());
        }

        /**
         * Function<T, R> - Takes input, returns different type output
         */
        public Map<String, Integer> mapToLengths(List<String> strings) {
            Function<String, Integer> lengthFunction = String::length;
            Function<Integer, String> formatter = n -> "Length: " + n;

            // Chain functions
            Function<String, String> combined = lengthFunction.andThen(formatter);

            return strings.stream()
                    .collect(Collectors.toMap(
                            s -> s,
                            lengthFunction
                    ));
        }

        /**
         * Predicate<T> - Takes input, returns boolean
         */
        public List<String> filterStrings(List<String> strings) {
            Predicate<String> notNull = Objects::nonNull;
            Predicate<String> notEmpty = s -> !s.isEmpty();
            Predicate<String> longerThan3 = s -> s.length() > 3;

            // Compose predicates
            Predicate<String> valid = notNull.and(notEmpty).and(longerThan3);

            return strings.stream()
                    .filter(valid)
                    .collect(Collectors.toList());
        }

        /**
         * BiFunction<T, U, R> - Takes two inputs, returns output
         */
        public List<String> combineStrings(List<String> first, List<String> second) {
            BiFunction<String, String, String> combiner = (a, b) -> a + "-" + b;

            List<String> result = new ArrayList<>();
            for (int i = 0; i < Math.min(first.size(), second.size()); i++) {
                result.add(combiner.apply(first.get(i), second.get(i)));
            }
            return result;
        }

        /**
         * UnaryOperator<T> - Function where input and output are same type
         */
        public List<String> transformStrings(List<String> strings) {
            UnaryOperator<String> trim = String::trim;
            UnaryOperator<String> toLowerCase = String::toLowerCase;
            UnaryOperator<String> addPrefix = s -> "processed_" + s;

            // andThen returns Function, not UnaryOperator
            Function<String, String> pipeline = trim
                    .andThen(toLowerCase)
                    .andThen(addPrefix);

            return strings.stream()
                    .map(pipeline)
                    .collect(Collectors.toList());
        }

        /**
         * BinaryOperator<T> - BiFunction where both inputs and output are same type
         */
        public Optional<Integer> findMax(List<Integer> numbers) {
            BinaryOperator<Integer> max = (a, b) -> a > b ? a : b;
            return numbers.stream().reduce(max);
        }

        public String concatenateAll(List<String> strings) {
            BinaryOperator<String> concat = (a, b) -> a + ", " + b;
            return strings.stream().reduce(concat).orElse("");
        }
    }

    /**
     * Demonstrates primitive specializations for better performance
     */
    public static class PrimitiveSpecializations {

        /**
         * IntPredicate - avoids boxing for primitive int
         */
        public List<Integer> filterEvenNumbers(List<Integer> numbers) {
            IntPredicate isEven = n -> n % 2 == 0;
            return numbers.stream()
                    .filter(n -> isEven.test(n))
                    .collect(Collectors.toList());
        }

        /**
         * IntFunction<R> - takes int primitive, returns object
         */
        public List<String> formatNumbers(List<Integer> numbers) {
            IntFunction<String> formatter = n -> String.format("%05d", n);
            return numbers.stream()
                    .mapToInt(Integer::intValue)
                    .mapToObj(formatter)
                    .collect(Collectors.toList());
        }

        /**
         * ToIntFunction<T> - takes object, returns int primitive
         */
        public int sumStringLengths(List<String> strings) {
            ToIntFunction<String> lengthExtractor = String::length;
            return strings.stream()
                    .mapToInt(lengthExtractor)
                    .sum();
        }

        /**
         * IntConsumer - consumes int primitive
         */
        public void printNumbers(List<Integer> numbers) {
            IntConsumer printer = n -> System.out.println("Number: " + n);
            numbers.stream()
                    .mapToInt(Integer::intValue)
                    .forEach(printer);
        }

        /**
         * IntSupplier - supplies int primitive
         */
        public List<Integer> generateRandomNumbers(int count) {
            IntSupplier random = () -> (int) (Math.random() * 100);
            return java.util.stream.IntStream.range(0, count)
                    .map(i -> random.getAsInt())
                    .boxed()
                    .collect(Collectors.toList());
        }

        /**
         * IntBinaryOperator - combines two ints
         */
        public int calculateProduct(List<Integer> numbers) {
            IntBinaryOperator multiply = (a, b) -> a * b;
            return numbers.stream()
                    .mapToInt(Integer::intValue)
                    .reduce(1, multiply);
        }

        /**
         * IntUnaryOperator - transforms int to int
         */
        public List<Integer> doubleAllNumbers(List<Integer> numbers) {
            IntUnaryOperator doubler = n -> n * 2;
            return numbers.stream()
                    .mapToInt(Integer::intValue)
                    .map(doubler)
                    .boxed()
                    .collect(Collectors.toList());
        }
    }

    /**
     * Demonstrates creating and using custom functional interfaces
     */
    public static class CustomFunctionalInterfaces {

        /**
         * Custom functional interface for validation
         */
        @FunctionalInterface
        public interface Validator<T> {
            boolean validate(T value);

            default Validator<T> and(Validator<T> other) {
                return value -> this.validate(value) && other.validate(value);
            }

            default Validator<T> or(Validator<T> other) {
                return value -> this.validate(value) || other.validate(value);
            }

            default Validator<T> negate() {
                return value -> !this.validate(value);
            }
        }

        public record User(String name, int age, String email) {
        }

        public List<User> validateUsers(List<User> users) {
            Validator<User> hasName = user -> user.name() != null && !user.name().isEmpty();
            Validator<User> isAdult = user -> user.age() >= 18;
            Validator<User> hasEmail = user -> user.email() != null && user.email().contains("@");

            Validator<User> fullValidator = hasName.and(isAdult).and(hasEmail);

            return users.stream()
                    .filter(fullValidator::validate)
                    .collect(Collectors.toList());
        }

        /**
         * Custom functional interface for transformation with context
         */
        @FunctionalInterface
        public interface Transformer<T, R, C> {
            R transform(T input, C context);

            default <V> Transformer<T, V, C> andThen(BiFunction<R, C, V> after) {
                return (input, context) -> after.apply(this.transform(input, context), context);
            }
        }

        public List<String> transformWithContext(List<Integer> numbers, String prefix) {
            Transformer<Integer, String, String> formatter =
                    (num, ctx) -> ctx + num;

            return numbers.stream()
                    .map(n -> formatter.transform(n, prefix))
                    .collect(Collectors.toList());
        }

        /**
         * Custom functional interface that can throw checked exceptions
         */
        @FunctionalInterface
        public interface CheckedFunction<T, R> {
            R apply(T t) throws Exception;

            static <T, R> Function<T, R> unchecked(CheckedFunction<T, R> function) {
                return t -> {
                    try {
                        return function.apply(t);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };
            }

            static <T, R> Function<T, Optional<R>> lift(CheckedFunction<T, R> function) {
                return t -> {
                    try {
                        return Optional.ofNullable(function.apply(t));
                    } catch (Exception e) {
                        return Optional.empty();
                    }
                };
            }
        }

        public List<Integer> parseWithExceptionHandling(List<String> strings) {
            Function<String, Optional<Integer>> parser =
                    CheckedFunction.lift(Integer::parseInt);

            return strings.stream()
                    .map(parser)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }

        /**
         * Custom functional interface for business logic
         */
        @FunctionalInterface
        public interface PriceCalculator {
            double calculate(double basePrice, int quantity, double discount);

            default PriceCalculator withTax(double taxRate) {
                return (base, qty, disc) -> {
                    double price = this.calculate(base, qty, disc);
                    return price * (1 + taxRate);
                };
            }

            static PriceCalculator standard() {
                return (base, qty, disc) -> base * qty * (1 - disc);
            }

            static PriceCalculator withMinimumPrice(double minimum) {
                return (base, qty, disc) -> Math.max(minimum, base * qty * (1 - disc));
            }
        }

        public double calculateFinalPrice(double basePrice, int quantity, double discount) {
            PriceCalculator calculator = PriceCalculator.standard().withTax(0.08);
            return calculator.calculate(basePrice, quantity, discount);
        }
    }

    /**
     * Demonstrates composition and chaining of functional interfaces
     */
    public static class Composition {

        /**
         * Function composition with andThen and compose
         */
        public List<String> demonstrateFunctionComposition(List<Integer> numbers) {
            Function<Integer, Integer> square = n -> n * n;
            Function<Integer, String> format = n -> "Result: " + n;
            Function<String, String> addSuffix = s -> s + "!";

            // Chain with andThen: square -> format -> addSuffix
            Function<Integer, String> pipeline = square.andThen(format).andThen(addSuffix);

            return numbers.stream()
                    .map(pipeline)
                    .collect(Collectors.toList());
        }

        /**
         * Predicate composition with and, or, negate
         */
        public List<String> demonstratePredicateComposition(List<String> strings) {
            Predicate<String> notNull = Objects::nonNull;
            Predicate<String> notEmpty = s -> !s.isEmpty();
            Predicate<String> startsWithA = s -> s.startsWith("A");
            Predicate<String> longerThan5 = s -> s.length() > 5;

            // Complex composition
            Predicate<String> valid = notNull
                    .and(notEmpty)
                    .and(startsWithA.or(longerThan5));

            return strings.stream()
                    .filter(valid)
                    .collect(Collectors.toList());
        }

        /**
         * Consumer composition with andThen
         */
        public void demonstrateConsumerComposition(List<String> items) {
            Consumer<String> print = System.out::println;
            Consumer<String> log = s -> System.err.println("LOG: " + s);
            Consumer<String> count = s -> System.out.println("Length: " + s.length());

            Consumer<String> pipeline = print.andThen(log).andThen(count);
            items.forEach(pipeline);
        }

        /**
         * Comparator composition
         */
        public record Person(String name, int age, String department) {
        }

        public List<Person> sortWithComposition(List<Person> people) {
            Comparator<Person> byDepartment = Comparator.comparing(Person::department);
            Comparator<Person> byAge = Comparator.comparingInt(Person::age);
            Comparator<Person> byName = Comparator.comparing(Person::name);

            // Compose: first by department, then age, then name
            Comparator<Person> fullComparator = byDepartment
                    .thenComparing(byAge)
                    .thenComparing(byName);

            return people.stream()
                    .sorted(fullComparator)
                    .collect(Collectors.toList());
        }

        /**
         * Building reusable function pipelines
         */
        public static class StringProcessor {
            private final List<UnaryOperator<String>> operations = new ArrayList<>();

            public StringProcessor add(UnaryOperator<String> operation) {
                operations.add(operation);
                return this;
            }

            public Function<String, String> build() {
                return input -> {
                    String result = input;
                    for (UnaryOperator<String> op : operations) {
                        result = op.apply(result);
                    }
                    return result;
                };
            }
        }

        public List<String> processWithPipeline(List<String> strings) {
            Function<String, String> processor = new StringProcessor()
                    .add(String::trim)
                    .add(String::toLowerCase)
                    .add(s -> s.replaceAll("\\s+", "_"))
                    .add(s -> s.substring(0, Math.min(s.length(), 20)))
                    .build();

            return strings.stream()
                    .map(processor)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Demonstrates advanced patterns with functional interfaces
     */
    public static class AdvancedPatterns {

        /**
         * Currying - transforming multi-parameter function to chain of single-parameter functions
         */
        public Function<Integer, Function<Integer, Integer>> curriedAdd() {
            return a -> b -> a + b;
        }

        public int useCurrying() {
            Function<Integer, Function<Integer, Integer>> add = curriedAdd();
            Function<Integer, Integer> add5 = add.apply(5);
            return add5.apply(3);  // Returns 8
        }

        /**
         * Partial application
         */
        public Function<Integer, Integer> createMultiplier(int factor) {
            BiFunction<Integer, Integer, Integer> multiply = (a, b) -> a * b;
            return n -> multiply.apply(factor, n);
        }

        public List<Integer> applyMultiplier(List<Integer> numbers, int factor) {
            Function<Integer, Integer> multiplier = createMultiplier(factor);
            return numbers.stream()
                    .map(multiplier)
                    .collect(Collectors.toList());
        }

        /**
         * Memoization with functional interfaces
         */
        public static class Memoizer<T, R> {
            private final Map<T, R> cache = new HashMap<>();
            private final Function<T, R> function;

            public Memoizer(Function<T, R> function) {
                this.function = function;
            }

            public Function<T, R> memoize() {
                return input -> cache.computeIfAbsent(input, function);
            }
        }

        public Function<Integer, Integer> createMemoizedFactorial() {
            Function<Integer, Integer> factorial = new Function<Integer, Integer>() {
                @Override
                public Integer apply(Integer n) {
                    if (n <= 1) return 1;
                    return n * this.apply(n - 1);
                }
            };

            return new Memoizer<>(factorial).memoize();
        }

        /**
         * Lazy evaluation with Supplier
         */
        public static class Lazy<T> {
            private Supplier<T> supplier;
            private T value;
            private boolean computed = false;

            public Lazy(Supplier<T> supplier) {
                this.supplier = supplier;
            }

            public T get() {
                if (!computed) {
                    value = supplier.get();
                    computed = true;
                    supplier = null;  // Allow GC
                }
                return value;
            }

            public static <T> Lazy<T> of(Supplier<T> supplier) {
                return new Lazy<>(supplier);
            }
        }

        public String demonstrateLazyEvaluation() {
            Lazy<String> expensive = Lazy.of(() -> {
                System.out.println("Computing expensive value...");
                return "Expensive Result";
            });

            // Value not computed yet
            System.out.println("Lazy value created");

            // Now it's computed (only once)
            String result1 = expensive.get();
            String result2 = expensive.get();  // Returns cached value

            return result1;
        }
    }
}