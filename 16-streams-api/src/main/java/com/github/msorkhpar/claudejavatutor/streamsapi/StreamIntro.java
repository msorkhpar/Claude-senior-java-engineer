package com.github.msorkhpar.claudejavatutor.streamsapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Demonstrates the fundamental concepts of the Java Streams API:
 * pipeline structure, lazy evaluation, single-use nature, and
 * differences from Collections.
 * <p>
 * Covers section 4.4.1 — Introduction to the Streams API.
 */
public class StreamIntro {

    /**
     * Demonstrates the three-part stream pipeline: source, intermediate ops, terminal op.
     * Returns the names that are longer than 3 characters, uppercased.
     */
    public List<String> pipelineDemo(List<String> names) {
        return names.stream()               // Source
                .filter(s -> s.length() > 3) // Intermediate: filter
                .map(String::toUpperCase)     // Intermediate: map
                .collect(Collectors.toList()); // Terminal: collect
    }

    /**
     * Demonstrates lazy evaluation: the filter predicate tracks how many
     * elements it actually examines when using a short-circuit terminal op.
     * Returns how many filter calls were made (should be fewer than list.size()).
     */
    public int countFilterCallsWithFindFirst(List<String> list, String targetPrefix) {
        int[] callCount = {0};
        list.stream()
                .filter(s -> {
                    callCount[0]++;
                    return s.startsWith(targetPrefix);
                })
                .findFirst(); // Short-circuit — stops after first match
        return callCount[0];
    }

    /**
     * Shows that streams do NOT modify the source collection.
     * The original list must be unchanged after streaming.
     */
    public List<String> streamDoesNotModifySource(List<String> original) {
        // This stream produces a new list; original is untouched
        original.stream()
                .filter(s -> s.length() > 2)
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        return new ArrayList<>(original); // return copy of original — should be unchanged
    }

    /**
     * Demonstrates that a stream is single-use: consuming it twice
     * throws IllegalStateException.
     */
    public boolean demonstrateSingleUse() {
        Stream<String> stream = Stream.of("a", "b", "c");
        stream.count(); // First consumption
        try {
            stream.count(); // Second consumption — should throw
            return false;
        } catch (IllegalStateException e) {
            return true; // Expected
        }
    }

    /**
     * Uses Stream.toList() (Java 16+) for an immutable result.
     * Demonstrates the difference from collect(Collectors.toList()).
     */
    public List<String> toListDemo(List<String> names) {
        return names.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList(); // Java 16+: unmodifiable
    }

    /**
     * Demonstrates that allMatch returns true on an empty stream (vacuous truth),
     * anyMatch returns false, and noneMatch returns true.
     */
    public record MatchResults(boolean allMatch, boolean anyMatch, boolean noneMatch) {}

    public MatchResults matchOnEmptyStream() {
        Stream<String> s1 = Stream.empty();
        Stream<String> s2 = Stream.empty();
        Stream<String> s3 = Stream.empty();
        return new MatchResults(
                s1.allMatch(s -> s.length() > 100),
                s2.anyMatch(s -> s.length() > 100),
                s3.noneMatch(s -> s.length() > 100)
        );
    }

    /**
     * Demonstrates how to safely handle null elements in a stream.
     * Nulls are filtered out before any operation that would throw NPE.
     */
    public List<String> safelyHandleNulls(List<String> withNulls) {
        return withNulls.stream()
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .sorted()
                .toList();
    }

    /**
     * Demonstrates Stream.toList() returns an unmodifiable list.
     * Returns true if attempting to modify throws UnsupportedOperationException.
     */
    public boolean toListIsUnmodifiable() {
        List<String> list = Stream.of("a", "b", "c").toList();
        try {
            list.add("d");
            return false;
        } catch (UnsupportedOperationException e) {
            return true;
        }
    }

    /**
     * Contrasts internal iteration (stream) with external iteration (for-loop).
     * Both produce the same result but stream expresses intent more declaratively.
     */
    public List<String> filterAndUpperCaseWithLoop(List<String> names, int minLength) {
        List<String> result = new ArrayList<>();
        for (String name : names) {
            if (name.length() >= minLength) {
                result.add(name.toUpperCase());
            }
        }
        return result;
    }

    public List<String> filterAndUpperCaseWithStream(List<String> names, int minLength) {
        return names.stream()
                .filter(name -> name.length() >= minLength)
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }

    /**
     * Uses Optional correctly from a stream terminal operation.
     * Demonstrates proper Optional handling vs. unsafe .get().
     */
    public Optional<String> findFirstLongerThan(List<String> names, int minLength) {
        return names.stream()
                .filter(s -> s.length() > minLength)
                .findFirst();
    }

    /**
     * Demonstrates short-circuit evaluation with limit() on an infinite stream.
     * Only the first 'count' natural numbers are produced.
     */
    public List<Integer> firstNNaturalNumbers(int count) {
        return Stream.iterate(1, n -> n + 1)
                .limit(count)
                .collect(Collectors.toList());
    }
}
