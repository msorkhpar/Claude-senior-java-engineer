package com.github.msorkhpar.claudejavatutor.functionalinterfaces;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Demonstrates the Predicate and BiPredicate functional interfaces.
 *
 * <p>Predicate<T> - tests a condition on an input T and returns a boolean.
 * Key methods:
 *   - boolean test(T t)
 *   - Predicate<T> and(Predicate<T> other)    — logical AND (short-circuit)
 *   - Predicate<T> or(Predicate<T> other)     — logical OR  (short-circuit)
 *   - Predicate<T> negate()                   — logical NOT
 *   - static Predicate<T> not(Predicate<T>)   — static negation (Java 11+)
 *   - static Predicate<T> isEqual(Object)     — tests object equality
 *
 * <p>BiPredicate<T, U> - tests a condition on two inputs T and U.
 *   - boolean test(T t, U u)
 *   - BiPredicate<T, U> and(BiPredicate<T, U> other)
 *   - BiPredicate<T, U> or(BiPredicate<T, U> other)
 *   - BiPredicate<T, U> negate()
 *
 * <p>Primitive specializations: IntPredicate, LongPredicate, DoublePredicate
 */
public class PredicateDemo {

    /**
     * Tests if a string is empty using a Predicate.
     */
    public boolean isEmpty(String s) {
        Predicate<String> emptyCheck = String::isEmpty;
        return emptyCheck.test(s);
    }

    /**
     * Tests if a number is positive.
     */
    public boolean isPositive(int n) {
        Predicate<Integer> positive = x -> x > 0;
        return positive.test(n);
    }

    /**
     * Tests if a string starts with a given prefix.
     */
    public boolean startsWith(String str, String prefix) {
        Predicate<String> check = s -> s.startsWith(prefix);
        return check.test(str);
    }

    /**
     * Filters a list based on a Predicate condition.
     */
    public <T> List<T> filter(List<T> items, Predicate<T> predicate) {
        return items.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Predicate composition: and(), or(), negate()
    // =========================================================================

    /**
     * Demonstrates Predicate.and() for combining two predicates with logical AND.
     * Returns elements satisfying BOTH conditions.
     */
    public <T> List<T> filterWithAnd(List<T> items, Predicate<T> first, Predicate<T> second) {
        Predicate<T> combined = first.and(second);
        return items.stream()
                .filter(combined)
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates Predicate.or() for combining two predicates with logical OR.
     * Returns elements satisfying AT LEAST ONE condition.
     */
    public <T> List<T> filterWithOr(List<T> items, Predicate<T> first, Predicate<T> second) {
        Predicate<T> combined = first.or(second);
        return items.stream()
                .filter(combined)
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates Predicate.negate() to invert a condition.
     */
    public <T> List<T> filterWithNegate(List<T> items, Predicate<T> predicate) {
        return items.stream()
                .filter(predicate.negate())
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates Predicate.not() (Java 11+) as a static factory for negation.
     * Filters strings that are not empty (and not blank).
     */
    public List<String> filterNotEmpty(List<String> strings) {
        return strings.stream()
                .filter(Predicate.not(String::isEmpty))
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates complex predicate composition.
     * Filters words with length > 1 AND (starting with 'h' OR starting with 'j').
     */
    public List<String> complexFilter(List<String> words) {
        Predicate<String> longerThanOne = s -> s.length() > 1;
        Predicate<String> startsWithH = s -> s.startsWith("h");
        Predicate<String> startsWithJ = s -> s.startsWith("j");

        Predicate<String> combined = longerThanOne.and(startsWithH.or(startsWithJ));
        return words.stream()
                .filter(combined)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Predicate.isEqual and null handling
    // =========================================================================

    /**
     * Demonstrates Predicate.isEqual() for null-safe equality testing.
     */
    public boolean isEqualTo(Object target, Object value) {
        Predicate<Object> equalsPredicate = Predicate.isEqual(value);
        return equalsPredicate.test(target);
    }

    /**
     * Filters null values from a list using a null-check Predicate.
     */
    public <T> List<T> filterNulls(List<T> items) {
        return items.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // BiPredicate
    // =========================================================================

    /**
     * Demonstrates BiPredicate testing if a string contains a substring.
     */
    public boolean contains(String str, String substring) {
        BiPredicate<String, String> containsCheck = String::contains;
        return containsCheck.test(str, substring);
    }

    /**
     * Demonstrates BiPredicate for range checking.
     * Returns true if value is between min and max (inclusive).
     */
    public boolean isInRange(int min, int max, int value) {
        BiPredicate<Integer, Integer> inRange = (mn, mx) -> value >= mn && value <= mx;
        return inRange.test(min, max);
    }

    /**
     * Demonstrates BiPredicate.and() chaining.
     * Returns strings from the list that: have length > 3 AND start with the given prefix.
     */
    public List<String> filterWithBiPredicateAnd(List<String> strings, String prefix) {
        BiPredicate<String, String> longerThan3 = (s, p) -> s.length() > 3;
        BiPredicate<String, String> startsWith = (s, p) -> s.startsWith(p);

        BiPredicate<String, String> combined = longerThan3.and(startsWith);
        return strings.stream()
                .filter(s -> combined.test(s, prefix))
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates BiPredicate.negate() — tests that a string does NOT contain a substring.
     */
    public boolean doesNotContain(String str, String substring) {
        BiPredicate<String, String> containsCheck = String::contains;
        return containsCheck.negate().test(str, substring);
    }

    // =========================================================================
    // Primitive specialized Predicates
    // =========================================================================

    /**
     * Demonstrates IntPredicate to filter even numbers (no boxing).
     */
    public List<Integer> filterEvens(List<Integer> numbers) {
        IntPredicate isEven = n -> n % 2 == 0;
        return numbers.stream()
                .filter(n -> isEven.test(n))
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates LongPredicate for large number testing.
     */
    public boolean isLargeNumber(long n) {
        LongPredicate largeCheck = value -> value > 1_000_000_000L;
        return largeCheck.test(n);
    }

    // =========================================================================
    // Real-world validation scenarios
    // =========================================================================

    /**
     * Validates email format using Predicate composition.
     */
    public boolean isValidEmail(String email) {
        Predicate<String> notNull = s -> s != null;
        Predicate<String> notEmpty = s -> !s.isEmpty();
        Predicate<String> hasAtSign = s -> s.contains("@");
        Predicate<String> hasDot = s -> s.contains(".");
        Predicate<String> atBeforeDot = s -> {
            int atIndex = s.indexOf('@');
            int lastDotIndex = s.lastIndexOf('.');
            return atIndex > 0 && lastDotIndex > atIndex + 1;
        };

        Predicate<String> validEmail = notNull
                .and(notEmpty)
                .and(hasAtSign)
                .and(hasDot)
                .and(atBeforeDot);

        return validEmail.test(email);
    }

    /**
     * Validates password strength using Predicate composition.
     * Strong password: min 8 chars, has uppercase, has digit, has special char.
     */
    public boolean isStrongPassword(String password) {
        Predicate<String> minLength = p -> p != null && p.length() >= 8;
        Predicate<String> hasUpperCase = p -> p.chars().anyMatch(Character::isUpperCase);
        Predicate<String> hasDigit = p -> p.chars().anyMatch(Character::isDigit);
        Predicate<String> hasSpecial = p -> p.chars()
                .anyMatch(c -> !Character.isLetterOrDigit(c));

        Predicate<String> strongPassword = minLength
                .and(hasUpperCase)
                .and(hasDigit)
                .and(hasSpecial);

        return strongPassword.test(password);
    }

    /**
     * Counts elements matching a Predicate.
     */
    public <T> long countMatching(List<T> items, Predicate<T> predicate) {
        return items.stream()
                .filter(predicate)
                .count();
    }
}
