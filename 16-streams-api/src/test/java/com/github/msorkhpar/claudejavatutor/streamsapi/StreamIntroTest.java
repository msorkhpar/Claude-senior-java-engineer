package com.github.msorkhpar.claudejavatutor.streamsapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("4.4.1 - Stream Introduction Tests")
class StreamIntroTest {

    private final StreamIntro intro = new StreamIntro();

    @Nested
    @DisplayName("Stream Pipeline Demo")
    class PipelineDemoTest {

        @Test
        @DisplayName("Should filter and uppercase names longer than 3 characters")
        void testPipelineDemo() {
            List<String> names = List.of("Hi", "Alice", "Bob", "Charlie", "Ed");
            List<String> result = intro.pipelineDemo(names);
            assertThat(result).containsExactly("ALICE", "CHARLIE");
        }

        @Test
        @DisplayName("Should return empty list when no names match filter")
        void testPipelineDemoNoMatch() {
            List<String> names = List.of("Hi", "Jo", "Ed");
            assertThat(intro.pipelineDemo(names)).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for empty input")
        void testPipelineDemoEmpty() {
            assertThat(intro.pipelineDemo(List.of())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Lazy Evaluation — Short Circuit")
    class LazyEvaluationTest {

        @Test
        @DisplayName("findFirst should stop processing after first match")
        void testLazyEvaluationStopsEarly() {
            // Names: "Alice", "Bob", "Anna", "Charlie" — first starting with 'A' is at index 0
            // Filter should be called only 1 time before finding "Alice"
            List<String> names = List.of("Alice", "Bob", "Anna", "Charlie");
            int callCount = intro.countFilterCallsWithFindFirst(names, "A");
            // Should stop at "Alice" (index 0) — only 1 filter call
            assertThat(callCount).isEqualTo(1);
        }

        @Test
        @DisplayName("findFirst on list where match is in the middle should stop early")
        void testLazyEvaluationMatchInMiddle() {
            List<String> names = List.of("Bob", "Charlie", "Alice", "David");
            int callCount = intro.countFilterCallsWithFindFirst(names, "A");
            // Should examine "Bob", "Charlie", "Alice" — stops at index 2, so 3 calls
            assertThat(callCount).isEqualTo(3);
        }

        @Test
        @DisplayName("findFirst with no match should examine all elements")
        void testLazyEvaluationNoMatch() {
            List<String> names = List.of("Bob", "Charlie", "David");
            int callCount = intro.countFilterCallsWithFindFirst(names, "Z");
            // No match — must examine all 3
            assertThat(callCount).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Stream Does Not Modify Source")
    class SourceImmutabilityTest {

        @Test
        @DisplayName("Original list should be unchanged after streaming")
        void testSourceNotModified() {
            List<String> original = new ArrayList<>(List.of("hi", "hello", "hey"));
            List<String> beforeStream = new ArrayList<>(original);

            intro.streamDoesNotModifySource(original);

            assertThat(original).isEqualTo(beforeStream);
        }
    }

    @Nested
    @DisplayName("Single-Use Stream")
    class SingleUseTest {

        @Test
        @DisplayName("Consuming a stream twice should throw IllegalStateException")
        void testSingleUse() {
            assertThat(intro.demonstrateSingleUse()).isTrue();
        }
    }

    @Nested
    @DisplayName("Stream.toList() — Immutable")
    class ToListTest {

        @Test
        @DisplayName("toList() should return list with null-filtered, trimmed elements")
        void testToListDemo() {
            List<String> input = Arrays.asList("  hello  ", null, "  world  ", "  ", null);
            List<String> result = intro.toListDemo(input);
            assertThat(result).containsExactly("hello", "world");
        }

        @Test
        @DisplayName("toList() result should be unmodifiable")
        void testToListIsUnmodifiable() {
            assertThat(intro.toListIsUnmodifiable()).isTrue();
        }

        @Test
        @DisplayName("toList() on empty input returns empty list")
        void testToListEmpty() {
            assertThat(intro.toListDemo(List.of())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Match Operations on Empty Stream")
    class EmptyStreamMatchTest {

        @Test
        @DisplayName("allMatch on empty stream returns true (vacuous truth)")
        void testAllMatchEmpty() {
            StreamIntro.MatchResults results = intro.matchOnEmptyStream();
            assertThat(results.allMatch()).isTrue();
        }

        @Test
        @DisplayName("anyMatch on empty stream returns false")
        void testAnyMatchEmpty() {
            StreamIntro.MatchResults results = intro.matchOnEmptyStream();
            assertThat(results.anyMatch()).isFalse();
        }

        @Test
        @DisplayName("noneMatch on empty stream returns true")
        void testNoneMatchEmpty() {
            StreamIntro.MatchResults results = intro.matchOnEmptyStream();
            assertThat(results.noneMatch()).isTrue();
        }
    }

    @Nested
    @DisplayName("Null Handling")
    class NullHandlingTest {

        @Test
        @DisplayName("Should filter null elements and sort the rest")
        void testSafelyHandleNulls() {
            List<String> withNulls = Arrays.asList("banana", null, "apple", null, "cherry");
            List<String> result = intro.safelyHandleNulls(withNulls);
            assertThat(result).containsExactly("APPLE", "BANANA", "CHERRY");
        }

        @Test
        @DisplayName("Should return empty list when all elements are null")
        void testAllNulls() {
            List<String> allNulls = Arrays.asList(null, null, null);
            assertThat(intro.safelyHandleNulls(allNulls)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Internal vs External Iteration Equivalence")
    class IterationEquivalenceTest {

        @Test
        @DisplayName("Loop and stream should produce identical results")
        void testEquivalentResults() {
            List<String> names = List.of("hi", "hello", "hey", "goodbye", "morning");
            List<String> loopResult = intro.filterAndUpperCaseWithLoop(names, 4);
            List<String> streamResult = intro.filterAndUpperCaseWithStream(names, 4);
            assertThat(streamResult).isEqualTo(loopResult);
        }

        @Test
        @DisplayName("Should return empty list when nothing meets minimum length")
        void testNoMatchMinLength() {
            List<String> names = List.of("hi", "jo");
            assertThat(intro.filterAndUpperCaseWithStream(names, 10)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findFirst with Optional")
    class FindFirstTest {

        @Test
        @DisplayName("Should return first string longer than minLength")
        void testFindFirstPresent() {
            List<String> names = List.of("hi", "hello", "world");
            Optional<String> result = intro.findFirstLongerThan(names, 3);
            assertThat(result).isPresent().contains("hello");
        }

        @Test
        @DisplayName("Should return empty Optional when no match")
        void testFindFirstAbsent() {
            List<String> names = List.of("hi", "no");
            Optional<String> result = intro.findFirstLongerThan(names, 10);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty Optional for empty stream")
        void testFindFirstEmptyStream() {
            Optional<String> result = intro.findFirstLongerThan(List.of(), 0);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Infinite Stream with limit()")
    class InfiniteStreamTest {

        @Test
        @DisplayName("Should return first N natural numbers")
        void testFirstNNaturalNumbers() {
            List<Integer> result = intro.firstNNaturalNumbers(5);
            assertThat(result).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should return empty list when count is 0")
        void testFirstZeroNaturalNumbers() {
            assertThat(intro.firstNNaturalNumbers(0)).isEmpty();
        }

        @Test
        @DisplayName("Should return single element when count is 1")
        void testFirstOneNaturalNumber() {
            assertThat(intro.firstNNaturalNumbers(1)).containsExactly(1);
        }

        @Test
        @DisplayName("Should return 100 numbers correctly")
        void testFirst100NaturalNumbers() {
            List<Integer> result = intro.firstNNaturalNumbers(100);
            assertThat(result).hasSize(100);
            assertThat(result.get(0)).isEqualTo(1);
            assertThat(result.get(99)).isEqualTo(100);
        }
    }
}
