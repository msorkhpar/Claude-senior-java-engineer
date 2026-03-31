package com.github.msorkhpar.claudejavatutor.methodreferences;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Demonstrates using method references with the standard functional interfaces:
 * Consumer, Supplier, Function, Predicate, BiFunction, Comparator, and Optional.
 *
 * @see README_4.3.3.md
 */
public class MethodReferencesWithFunctionalInterfaces {

    // ─────────────────────────────────────────────────
    // Consumer<T> — void accept(T t)
    // ─────────────────────────────────────────────────

    /**
     * Demonstrates method references with {@link Consumer}.
     */
    public static class ConsumerMethodRefs {

        private final List<String> eventLog = new ArrayList<>();

        /**
         * Logs an event — static method, suitable for static method reference.
         */
        public static void logStatic(String message) {
            System.out.println("[LOG] " + message);
        }

        /**
         * Records an event to the instance log — instance method.
         */
        public void record(String event) {
            eventLog.add(event);
        }

        /**
         * Returns the event log.
         */
        public List<String> getEventLog() {
            return Collections.unmodifiableList(eventLog);
        }

        /**
         * Processes items using a chained Consumer pipeline:
         * first records each item, then prints it.
         */
        public void processItems(List<String> items) {
            Consumer<String> recordEvent = this::record;       // bound instance ref
            Consumer<String> printItem = System.out::println;  // bound instance ref

            Consumer<String> pipeline = recordEvent.andThen(printItem);
            items.forEach(pipeline);
        }

        /**
         * Demonstrates static method reference as Consumer.
         */
        public void logAll(List<String> messages) {
            messages.forEach(ConsumerMethodRefs::logStatic);   // static method ref
        }

        /**
         * Builds a result list using a Consumer (bound instance ref to List::add).
         */
        public List<String> collectUpperCase(List<String> items) {
            List<String> result = new ArrayList<>();
            items.stream()
                    .filter(Objects::nonNull)
                    .map(String::toUpperCase)
                    .forEach(result::add);     // bound instance ref: result is the receiver
            return result;
        }
    }

    // ─────────────────────────────────────────────────
    // Supplier<T> — T get()
    // ─────────────────────────────────────────────────

    /**
     * Demonstrates method references with {@link Supplier}.
     */
    public static class SupplierMethodRefs {

        /**
         * Returns a Supplier backed by a bound instance method reference.
         * The specific 'fixed' string is always the receiver.
         */
        public Supplier<String> upperCaseSupplier(String fixed) {
            return fixed::toUpperCase;    // bound instance ref
        }

        /**
         * Returns a Supplier backed by a constructor reference (no-arg).
         */
        public Supplier<ArrayList<String>> arrayListSupplier() {
            return ArrayList::new;        // constructor reference
        }

        /**
         * Returns a Supplier that always returns an empty list.
         * Static method reference: Collections.emptyList()
         */
        public Supplier<List<String>> emptyListSupplier() {
            return Collections::emptyList; // static method reference
        }

        /**
         * Lazily provides a value using a Supplier backed by a bound method reference.
         * The value is only computed on first access.
         */
        public <T> T getOrDefault(T value, Supplier<T> defaultSupplier) {
            return value != null ? value : defaultSupplier.get();
        }

        /**
         * Creates n new ArrayLists using a constructor reference Supplier.
         */
        public List<ArrayList<String>> createBuckets(int n) {
            Supplier<ArrayList<String>> factory = ArrayList::new;
            return IntStream.range(0, n)
                    .mapToObj(i -> factory.get())
                    .collect(Collectors.toList());
        }
    }

    // ─────────────────────────────────────────────────
    // Function<T, R> — R apply(T t)
    // ─────────────────────────────────────────────────

    /**
     * Demonstrates method references with {@link Function}.
     */
    public static class FunctionMethodRefs {

        /**
         * Returns a Function using an unbound instance method reference.
         * String::length — each String is the receiver.
         */
        public Function<String, Integer> lengthFunction() {
            return String::length;         // unbound instance method ref
        }

        /**
         * Returns a Function using a static method reference.
         * Integer::parseInt — single-arg static method.
         */
        public Function<String, Integer> parseIntFunction() {
            return Integer::parseInt;      // static method reference
        }

        /**
         * Returns a Function using a constructor reference.
         * StringBuilder::new — matches single-String constructor.
         */
        public Function<String, StringBuilder> stringBuilderFactory() {
            return StringBuilder::new;     // constructor reference
        }

        /**
         * Applies a pipeline of Functions to a list.
         * Demonstrates Function composition with andThen using method references.
         */
        public List<Integer> trimAndGetLength(List<String> strings) {
            Function<String, String> trim = String::trim;       // unbound ref
            Function<String, Integer> length = String::length;  // unbound ref
            Function<String, Integer> trimThenLength = trim.andThen(length);

            return strings.stream()
                    .filter(Objects::nonNull)
                    .map(trimThenLength)
                    .collect(Collectors.toList());
        }

        /**
         * Transforms a list using a provided Function (accepts method reference or lambda).
         */
        public <T, R> List<R> transform(List<T> items, Function<T, R> mapper) {
            return items.stream()
                    .filter(Objects::nonNull)
                    .map(mapper)
                    .collect(Collectors.toList());
        }
    }

    // ─────────────────────────────────────────────────
    // Predicate<T> — boolean test(T t)
    // ─────────────────────────────────────────────────

    /**
     * Demonstrates method references with {@link Predicate}.
     */
    public static class PredicateMethodRefs {

        /**
         * Returns a non-null Predicate using a static method reference.
         */
        public Predicate<Object> nonNullPredicate() {
            return Objects::nonNull;       // static method reference
        }

        /**
         * Returns an is-blank Predicate using an unbound instance method reference.
         */
        public Predicate<String> isBlankPredicate() {
            return String::isBlank;        // unbound instance method ref
        }

        /**
         * Returns a non-blank Predicate using Predicate.not with method reference.
         */
        public Predicate<String> isNotBlankPredicate() {
            return Predicate.not(String::isBlank); // negated unbound method ref
        }

        /**
         * Filters a list using a composed Predicate built from method references.
         * Keeps only non-null, non-blank, short strings.
         */
        public List<String> filterNonNullNonBlankShort(List<String> strings, int maxLength) {
            Predicate<String> nonNull = Objects::nonNull;            // static ref
            Predicate<String> nonBlank = Predicate.not(String::isBlank); // negated unbound ref
            Predicate<String> isShort = s -> s.length() <= maxLength;    // lambda for comparison

            return strings.stream()
                    .filter(nonNull.and(nonBlank).and(isShort))
                    .collect(Collectors.toList());
        }

        /**
         * Demonstrates predicate composition: keeps items matching either predicate.
         */
        public List<String> filterEmptyOrLong(List<String> strings, int minLength) {
            Predicate<String> isEmpty = String::isEmpty;           // unbound ref
            Predicate<String> isLong = s -> s.length() >= minLength; // lambda

            return strings.stream()
                    .filter(Objects::nonNull)
                    .filter(isEmpty.or(isLong))
                    .collect(Collectors.toList());
        }
    }

    // ─────────────────────────────────────────────────
    // BiFunction<T, U, R> — R apply(T t, U u)
    // ─────────────────────────────────────────────────

    /**
     * Demonstrates method references with {@link BiFunction}.
     */
    public static class BiFunctionMethodRefs {

        /**
         * Returns a BiFunction using a static two-arg method reference.
         * Math::max — Math.max(int, int) is a static two-arg method.
         */
        public BiFunction<Integer, Integer, Integer> maxFunction() {
            return Math::max;              // static two-arg method reference
        }

        /**
         * Returns a BiFunction using an unbound instance method reference.
         * String::concat — receiver is the first arg; second arg is the concat argument.
         */
        public BiFunction<String, String, String> concatFunction() {
            return String::concat;         // unbound instance ref: s1.concat(s2)
        }

        /**
         * Returns a BiFunction using an unbound instance method reference.
         * String::startsWith — receiver is first arg, second is the prefix to check.
         */
        public BiFunction<String, String, Boolean> startsWithFunction() {
            return String::startsWith;     // unbound instance ref: s1.startsWith(s2)
        }

        /**
         * Applies a BiFunction to two lists element-wise (zip-like).
         */
        public <T, U, R> List<R> zipWith(List<T> left, List<U> right,
                                          BiFunction<T, U, R> combiner) {
            int size = Math.min(left.size(), right.size());
            List<R> result = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                result.add(combiner.apply(left.get(i), right.get(i)));
            }
            return result;
        }

        /**
         * Demonstrates BiFunction.andThen with method references.
         * Concatenates two strings, then gets the length of the result.
         */
        public BiFunction<String, String, Integer> concatThenLength() {
            BiFunction<String, String, String> concat = String::concat; // unbound ref
            Function<String, Integer> length = String::length;          // unbound ref
            return concat.andThen(length);
        }
    }

    // ─────────────────────────────────────────────────
    // Comparator<T> — int compare(T o1, T o2)
    // ─────────────────────────────────────────────────

    /**
     * Demonstrates method references with {@link Comparator}.
     */
    public static class ComparatorMethodRefs {

        record Person(String firstName, String lastName, int age) {}

        /**
         * Demonstrates String::compareTo as an unbound Comparator method reference.
         */
        public List<String> sortAlphabetically(List<String> strings) {
            return strings.stream()
                    .sorted(String::compareTo)   // unbound: (s1,s2) -> s1.compareTo(s2)
                    .collect(Collectors.toList());
        }

        /**
         * Demonstrates Comparator.comparing with method references.
         * Multi-level sort: last name, then first name, then age.
         */
        public List<Person> sortPeople(List<Person> people) {
            return people.stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(Person::lastName)
                            .thenComparing(Person::firstName)
                            .thenComparingInt(Person::age))
                    .collect(Collectors.toList());
        }

        /**
         * Demonstrates reverse sort using Comparator with method reference.
         */
        public List<String> sortByLengthDescending(List<String> strings) {
            return strings.stream()
                    .sorted(Comparator.comparingInt(String::length).reversed())
                    .collect(Collectors.toList());
        }

        /**
         * Returns a bound Comparator using a captured comparator's compare method.
         */
        public Comparator<String> caseInsensitiveComparator() {
            Comparator<String> cmp = String.CASE_INSENSITIVE_ORDER;
            return cmp::compare;  // bound instance method ref to the comparator object
        }
    }

    // ─────────────────────────────────────────────────
    // Optional — map, filter, ifPresent
    // ─────────────────────────────────────────────────

    /**
     * Demonstrates method references with {@link Optional}.
     */
    public static class OptionalMethodRefs {

        /**
         * Converts Optional<String> to Optional<Integer> (length) using method reference.
         */
        public Optional<Integer> getLength(Optional<String> opt) {
            return opt.map(String::length);    // unbound instance method ref
        }

        /**
         * Filters Optional<String> to keep only non-blank values.
         */
        public Optional<String> filterBlank(Optional<String> opt) {
            return opt.filter(Predicate.not(String::isBlank)); // negated unbound ref
        }

        /**
         * Converts Optional<String> to Optional<String> in uppercase.
         */
        public Optional<String> toUpperCase(Optional<String> opt) {
            return opt.map(String::toUpperCase); // unbound instance method ref
        }

        /**
         * Prints the value if present using a bound method reference as Consumer.
         */
        public void printIfPresent(Optional<String> opt) {
            opt.ifPresent(System.out::println);  // bound: System.out is the receiver
        }

        /**
         * Chains multiple Optional transformations using method references.
         */
        public Optional<Integer> trimAndGetLength(Optional<String> opt) {
            return opt
                    .filter(Objects::nonNull)       // static method ref as predicate
                    .map(String::trim)               // unbound ref: trim whitespace
                    .filter(Predicate.not(String::isBlank)) // negated unbound ref
                    .map(String::length);            // unbound ref: get length
        }
    }

    // ─────────────────────────────────────────────────
    // Complete pipeline combining all interfaces
    // ─────────────────────────────────────────────────

    /**
     * Demonstrates a complete real-world pipeline using method references
     * with multiple functional interfaces.
     */
    public static class CompletePipeline {

        record Employee(String name, String department, double salary, boolean active) {}

        /**
         * Returns names of active employees sorted alphabetically.
         * Uses: Predicate (filter), Function (map), Comparator (sort).
         */
        public List<String> getActiveEmployeeNames(List<Employee> employees) {
            return employees.stream()
                    .filter(Objects::nonNull)            // static method ref as Predicate
                    .filter(Employee::active)            // unbound instance ref as Predicate
                    .sorted(Comparator.comparing(Employee::name)) // unbound ref as key extractor
                    .map(Employee::name)                 // unbound instance ref as Function
                    .collect(Collectors.toList());
        }

        /**
         * Groups employees by department using method reference as classifier.
         */
        public Map<String, List<Employee>> groupByDepartment(List<Employee> employees) {
            return employees.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(Employee::department)); // unbound ref as classifier
        }

        /**
         * Gets average salary by department.
         */
        public Map<String, Double> averageSalaryByDepartment(List<Employee> employees) {
            return employees.stream()
                    .filter(Objects::nonNull)
                    .filter(Employee::active)
                    .collect(Collectors.groupingBy(
                            Employee::department,                    // unbound ref as classifier
                            Collectors.averagingDouble(Employee::salary))); // unbound ref for value
        }

        /**
         * Collects employees to a typed array using array constructor reference.
         */
        public Employee[] toArray(List<Employee> employees) {
            return employees.stream()
                    .filter(Objects::nonNull)
                    .toArray(Employee[]::new);    // array constructor reference
        }
    }
}
