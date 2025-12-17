package com.github.msorkhpar.claudejavatutor.lambdaexpressions;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Demonstrates best practices and readability considerations for lambda expressions.
 * Covers when to extract lambdas, formatting, and balancing functional style with clarity.
 */
public class LambdaBestPractices {

    /**
     * Demonstrates when to extract lambdas to named methods
     */
    public static class LambdaExtraction {

        public record Order(String id, double total, Status status, LocalDate orderDate,
                            Customer customer, List<Item> items) {
            public enum Status {PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED}
        }

        public record Customer(String name, boolean premium) {
        }

        public record Item(String name, double price, boolean urgent) {
        }

        /**
         * Bad: Complex lambda inline - hard to understand
         */
        public List<Order> getEligibleOrdersBad(List<Order> orders) {
            return orders.stream()
                    .filter(order -> order.status() == Order.Status.CONFIRMED &&
                            order.total() > 100 &&
                            order.orderDate().isAfter(LocalDate.now().minusDays(30)) &&
                            (order.customer().premium() ||
                                    order.items().stream().anyMatch(item -> item.urgent())))
                    .collect(Collectors.toList());
        }

        /**
         * Good: Extract complex logic to named method
         */
        public List<Order> getEligibleOrdersGood(List<Order> orders) {
            return orders.stream()
                    .filter(this::isEligibleOrder)
                    .collect(Collectors.toList());
        }

        private boolean isEligibleOrder(Order order) {
            return order.status() == Order.Status.CONFIRMED &&
                    order.total() > 100 &&
                    isRecent(order) &&
                    (order.customer().premium() || hasUrgentItems(order));
        }

        private boolean isRecent(Order order) {
            return order.orderDate().isAfter(LocalDate.now().minusDays(30));
        }

        private boolean hasUrgentItems(Order order) {
            return order.items().stream().anyMatch(Item::urgent);
        }

        /**
         * Extract reusable predicates as constants
         */
        public static final Predicate<Order> IS_CONFIRMED =
                order -> order.status() == Order.Status.CONFIRMED;

        public static final Predicate<Order> IS_HIGH_VALUE =
                order -> order.total() > 100;

        public List<Order> filterWithConstants(List<Order> orders) {
            return orders.stream()
                    .filter(IS_CONFIRMED.and(IS_HIGH_VALUE))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Demonstrates proper lambda formatting and readability
     */
    public static class FormattingAndReadability {

        /**
         * Good: Clear, well-formatted stream pipeline
         */
        public List<String> processStrings(List<String> strings) {
            return strings.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toLowerCase)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        }

        /**
         * Good: Use descriptive variable names even in short lambdas
         */
        public Map<String, List<String>> groupByFirstLetter(List<String> words) {
            return words.stream()
                    .collect(Collectors.groupingBy(word -> word.substring(0, 1)));
        }

        /**
         * Good: Break complex operations into steps
         */
        public record Product(String name, double price, String category, int stock) {
        }

        public List<Product> findDiscountEligible(List<Product> products) {
            // Step 1: Filter by category
            List<Product> electronics = products.stream()
                    .filter(product -> product.category().equals("Electronics"))
                    .collect(Collectors.toList());

            // Step 2: Filter by stock level
            List<Product> overstocked = electronics.stream()
                    .filter(product -> product.stock() > 50)
                    .collect(Collectors.toList());

            // Step 3: Sort by price
            return overstocked.stream()
                    .sorted(Comparator.comparingDouble(Product::price).reversed())
                    .collect(Collectors.toList());
        }

        /**
         * Better: Chain operations but keep it readable
         */
        public List<Product> findDiscountEligibleBetter(List<Product> products) {
            return products.stream()
                    .filter(this::isElectronics)
                    .filter(this::isOverstocked)
                    .sorted(this::byPriceDescending)
                    .collect(Collectors.toList());
        }

        private boolean isElectronics(Product p) {
            return p.category().equals("Electronics");
        }

        private boolean isOverstocked(Product p) {
            return p.stock() > 50;
        }

        private int byPriceDescending(Product p1, Product p2) {
            return Double.compare(p2.price(), p1.price());
        }

        /**
         * Good: Format multi-line lambdas properly
         */
        public List<String> formatComplexOperation(List<Integer> numbers) {
            return numbers.stream()
                    .map(num -> {
                        double doubled = num * 2.0;
                        double withTax = doubled * 1.08;
                        return String.format("$%.2f", withTax);
                    })
                    .collect(Collectors.toList());
        }
    }

    /**
     * Demonstrates preferring method references over lambdas
     */
    public static class MethodReferences {

        /**
         * Use method references when they're more readable
         */
        public void demonstrateMethodReferences(List<String> strings) {
            // Less readable
            strings.forEach(s -> System.out.println(s));

            // More readable
            strings.forEach(System.out::println);

            // Less readable
            List<Integer> lengths1 = strings.stream()
                    .map(s -> s.length())
                    .collect(Collectors.toList());

            // More readable
            List<Integer> lengths2 = strings.stream()
                    .map(String::length)
                    .collect(Collectors.toList());

            // Less readable
            List<String> sorted1 = strings.stream()
                    .sorted((a, b) -> a.compareTo(b))
                    .collect(Collectors.toList());

            // More readable
            List<String> sorted2 = strings.stream()
                    .sorted(String::compareTo)
                    .collect(Collectors.toList());
        }

        /**
         * When lambdas are more readable than method references
         */
        public void whenLambdasAreClearer(List<Integer> numbers) {
            // Method reference might be less clear
            numbers.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());

            // Lambda shows the operation more clearly
            numbers.stream()
                    .map(n -> "Number: " + n)
                    .collect(Collectors.toList());

            // Method reference
            numbers.stream()
                    .filter(n -> n > 0)
                    .collect(Collectors.toList());  // Lambda is fine here
        }
    }

    /**
     * Demonstrates avoiding side effects in lambdas
     */
    public static class AvoidingSideEffects {

        /**
         * Bad: Side effects in filter/map operations
         */
        public List<String> processWithSideEffectsBad(List<String> strings) {
            List<String> processed = new ArrayList<>();
            return strings.stream()
                    .filter(s -> {
                        processed.add(s);  // Side effect!
                        return !s.isEmpty();
                    })
                    .collect(Collectors.toList());
        }

        /**
         * Good: Separate concerns - no side effects in filter
         */
        public List<String> processWithoutSideEffects(List<String> strings) {
            List<String> filtered = strings.stream()
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            // Separate step if you need to track processed items
            List<String> processed = new ArrayList<>(filtered);

            return filtered;
        }

        /**
         * Bad: Modifying external state
         */
        public long countWithSideEffectBad(List<String> strings) {
            final int[] count = {0};  // Workaround for final requirement
            strings.forEach(s -> {
                count[0]++;  // Side effect!
                System.out.println(s);
            });
            return count[0];
        }

        /**
         * Good: Use stream operations properly
         */
        public long countWithoutSideEffect(List<String> strings) {
            strings.forEach(System.out::println);  // Intentional side effect
            return strings.size();  // Or use stream().count() if filtering
        }

        /**
         * When side effects are acceptable: forEach and peek
         */
        public List<String> loggingIsOk(List<String> strings) {
            return strings.stream()
                    .peek(s -> System.out.println("Processing: " + s))  // OK in peek
                    .map(String::toUpperCase)
                    .peek(s -> System.out.println("Result: " + s))  // OK in peek
                    .collect(Collectors.toList());
        }
    }

    /**
     * Demonstrates balancing functional and imperative styles
     */
    public static class BalancingStyles {

        public record User(String name, int age, boolean active) {
        }

        /**
         * Too imperative - not using functional features
         */
        public List<String> getAdultNamesTooImperative(List<User> users) {
            List<String> result = new ArrayList<>();
            for (User user : users) {
                if (user.age() >= 18 && user.active()) {
                    result.add(user.name());
                }
            }
            Collections.sort(result);
            return result;
        }

        /**
         * Too functional - unnecessarily complex
         */
        public List<String> getAdultNamesTooFunctional(List<User> users) {
            return users.stream()
                    .collect(Collectors.partitioningBy(u -> u.age() >= 18))
                    .get(true)
                    .stream()
                    .filter(User::active)
                    .map(User::name)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toList(),
                            list -> {
                                list.sort(String::compareTo);
                                return list;
                            }
                    ));
        }

        /**
         * Balanced - clear and functional
         */
        public List<String> getAdultNamesBalanced(List<User> users) {
            return users.stream()
                    .filter(user -> user.age() >= 18)
                    .filter(User::active)
                    .map(User::name)
                    .sorted()
                    .collect(Collectors.toList());
        }

        /**
         * When to use loops instead of streams
         */
        public void whenToUseLoops(List<User> users) {
            // Use loop for: Early termination
            for (User user : users) {
                if (user.name().equals("Admin")) {
                    System.out.println("Admin found");
                    break;  // Early exit - easier with loop
                }
            }

            // Use loop for: Debugging (easier to step through)
            for (User user : users) {
                String name = user.name();
                boolean active = user.active();
                // Easy to set breakpoints and inspect
                processUser(user);
            }

            // Use stream for: Transformations and filtering
            List<String> activeNames = users.stream()
                    .filter(User::active)
                    .map(User::name)
                    .collect(Collectors.toList());
        }

        private void processUser(User user) {
        }
    }

    /**
     * Demonstrates performance-aware lambda usage
     */
    public static class PerformanceAwareness {

        /**
         * Cache lambda instances when reused frequently
         */
        private static final Comparator<String> LENGTH_COMPARATOR =
                Comparator.comparingInt(String::length);

        private static final Predicate<String> NOT_EMPTY =
                s -> !s.isEmpty();

        public List<String> sortMultipleLists(List<List<String>> lists) {
            return lists.stream()
                    .flatMap(List::stream)
                    .filter(NOT_EMPTY)  // Reuse cached predicate
                    .sorted(LENGTH_COMPARATOR)  // Reuse cached comparator
                    .collect(Collectors.toList());
        }

        /**
         * Use primitive streams to avoid boxing
         */
        public double calculateAverage(List<Integer> numbers) {
            // Inefficient: boxing/unboxing
            double avg1 = numbers.stream()
                    .mapToDouble(n -> n.doubleValue())
                    .average()
                    .orElse(0.0);

            // Efficient: primitive stream
            double avg2 = numbers.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);

            return avg2;
        }

        /**
         * Consider parallel streams for large datasets
         */
        public List<Integer> processLargeDataset(List<String> largeList) {
            // Sequential for small lists
            if (largeList.size() < 1000) {
                return largeList.stream()
                        .map(String::length)
                        .collect(Collectors.toList());
            }

            // Parallel for large lists
            return largeList.parallelStream()
                    .map(String::length)
                    .collect(Collectors.toList());
        }

        /**
         * Avoid creating lambdas in hot paths
         */
        public void demonstrateHotPath(List<String> strings) {
            // Bad: Creating lambda in loop
            for (int i = 0; i < 1000000; i++) {
                strings.stream()
                        .filter(s -> s.length() > 5)  // Lambda created each iteration
                        .count();
            }

            // Better: Extract lambda
            Predicate<String> lengthCheck = s -> s.length() > 5;
            for (int i = 0; i < 1000000; i++) {
                strings.stream()
                        .filter(lengthCheck)  // Reuse same lambda
                        .count();
            }

            // Best: Hoist operation out of loop if possible
            long count = strings.stream()
                    .filter(s -> s.length() > 5)
                    .count();
        }

        /**
         * Thread-safe mutable reduction
         */
        public Map<String, Integer> countOccurrencesThreadSafe(List<String> items) {
            // Thread-safe for parallel streams
            return items.parallelStream()
                    .collect(Collectors.toConcurrentMap(
                            s -> s,
                            s -> 1,
                            Integer::sum,
                            ConcurrentHashMap::new
                    ));
        }
    }

    /**
     * Demonstrates error handling and robustness
     */
    public static class ErrorHandling {

        /**
         * Handle nulls gracefully
         */
        public List<Integer> getLengthsSafe(List<String> strings) {
            return strings.stream()
                    .filter(Objects::nonNull)
                    .map(String::length)
                    .collect(Collectors.toList());
        }

        /**
         * Use Optional for safe transformations
         */
        public List<Integer> parseNumbersSafe(List<String> strings) {
            return strings.stream()
                    .map(this::tryParseInt)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }

        private Optional<Integer> tryParseInt(String s) {
            try {
                return Optional.of(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }

        /**
         * Validate inputs before processing
         */
        public List<String> processWithValidation(List<String> strings) {
            if (strings == null || strings.isEmpty()) {
                return Collections.emptyList();
            }

            return strings.stream()
                    .filter(this::isValid)
                    .map(this::process)
                    .collect(Collectors.toList());
        }

        private boolean isValid(String s) {
            return s != null && !s.isEmpty() && s.length() <= 100;
        }

        private String process(String s) {
            return s.toUpperCase();
        }

        /**
         * Provide meaningful defaults
         */
        public String getFirstMatchOrDefault(List<String> strings, Predicate<String> condition) {
            return strings.stream()
                    .filter(condition)
                    .findFirst()
                    .orElse("No match found");
        }
    }

    /**
     * Demonstrates documentation and self-documenting code
     */
    public static class Documentation {

        /**
         * Self-documenting code with clear names
         */
        public List<String> getEligibleUserNames(List<User> users) {
            return users.stream()
                    .filter(this::isActive)
                    .filter(this::isAdult)
                    .filter(this::hasCompletedProfile)
                    .map(User::name)
                    .collect(Collectors.toList());
        }

        private boolean isActive(User user) {
            return user.active();
        }

        private boolean isAdult(User user) {
            return user.age() >= 18;
        }

        private boolean hasCompletedProfile(User user) {
            return user.name() != null && !user.name().isEmpty();
        }

        /**
         * Document complex algorithms
         */
        /**
         * Calculates weighted score based on multiple factors.
         * <p>
         * Scoring weights:
         * - User activity: 40%
         * - Account age: 30%
         * - Profile completeness: 20%
         * - Engagement: 10%
         *
         * @param users List of users to score
         * @return Sorted list by score (highest first)
         */
        public List<User> scoreAndRankUsers(List<User> users) {
            return users.stream()
                    .map(user -> new ScoredUser(user, calculateScore(user)))
                    .sorted(Comparator.comparingDouble(ScoredUser::score).reversed())
                    .map(ScoredUser::user)
                    .collect(Collectors.toList());
        }

        private double calculateScore(User user) {
            // Scoring logic implementation
            return user.age() * 0.5;  // Simplified
        }

        public record User(String name, int age, boolean active) {
        }

        private record ScoredUser(User user, double score) {
        }
    }

    /**
     * Demonstrates testing considerations
     */
    public static class TestingConsiderations {

        /**
         * Extract lambdas for testability
         */
        public static class UserService {
            // Testable predicates
            public static final Predicate<User> IS_ACTIVE = User::active;
            public static final Predicate<User> IS_ADULT = u -> u.age() >= 18;

            public List<User> getEligibleUsers(List<User> users) {
                return users.stream()
                        .filter(IS_ACTIVE.and(IS_ADULT))
                        .collect(Collectors.toList());
            }

            // Can test predicates independently
            public boolean isUserEligible(User user) {
                return IS_ACTIVE.and(IS_ADULT).test(user);
            }
        }

        public record User(String name, int age, boolean active) {
        }

        /**
         * Use dependency injection for functions
         */
        public static class OrderProcessor {
            private final Predicate<Order> eligibilityCheck;
            private final Function<Order, String> formatter;

            public OrderProcessor(
                    Predicate<Order> eligibilityCheck,
                    Function<Order, String> formatter) {
                this.eligibilityCheck = eligibilityCheck;
                this.formatter = formatter;
            }

            public List<String> processOrders(List<Order> orders) {
                return orders.stream()
                        .filter(eligibilityCheck)
                        .map(formatter)
                        .collect(Collectors.toList());
            }
        }

        public record Order(String id, double total) {
        }
    }
}