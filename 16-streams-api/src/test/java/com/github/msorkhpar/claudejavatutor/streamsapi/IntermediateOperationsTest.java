package com.github.msorkhpar.claudejavatutor.streamsapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import static org.assertj.core.api.Assertions.*;

@DisplayName("4.4.3 - Intermediate Operations Tests")
class IntermediateOperationsTest {

    private final IntermediateOperations ops = new IntermediateOperations();

    @Nested
    @DisplayName("filter")
    class FilterTest {

        @Test
        @DisplayName("filterByLength should keep elements meeting minimum length")
        void testFilterByLength() {
            List<String> list = List.of("hi", "hello", "hey", "goodbye");
            assertThat(ops.filterByLength(list, 4)).containsExactly("hello", "goodbye");
        }

        @Test
        @DisplayName("filterByLength should return all elements when minLength is 0")
        void testFilterByLengthZero() {
            List<String> list = List.of("a", "b", "c");
            assertThat(ops.filterByLength(list, 0)).hasSize(3);
        }

        @Test
        @DisplayName("filterByLength should return empty list when nothing matches")
        void testFilterByLengthNoMatch() {
            List<String> list = List.of("hi", "no");
            assertThat(ops.filterByLength(list, 10)).isEmpty();
        }

        @Test
        @DisplayName("filterNulls should remove null elements")
        void testFilterNulls() {
            List<String> withNulls = Arrays.asList("a", null, "b", null, "c");
            assertThat(ops.filterNulls(withNulls)).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("filterNulls on all-null list returns empty")
        void testFilterNullsAllNull() {
            List<String> allNulls = Arrays.asList(null, null);
            assertThat(ops.filterNulls(allNulls)).isEmpty();
        }

        @Test
        @DisplayName("multiFilter should apply prefix and maxLength filters")
        void testMultiFilter() {
            List<String> list = List.of("apple", "apricot", "avocado", "banana", "apex");
            assertThat(ops.multiFilter(list, "ap", 6)).containsExactly("apple", "apex");
        }

        @Test
        @DisplayName("multiFilter on empty list returns empty")
        void testMultiFilterEmpty() {
            assertThat(ops.multiFilter(List.of(), "a", 10)).isEmpty();
        }
    }

    @Nested
    @DisplayName("map")
    class MapTest {

        @Test
        @DisplayName("mapToLengths should produce correct lengths")
        void testMapToLengths() {
            List<String> list = List.of("a", "bb", "ccc");
            assertThat(ops.mapToLengths(list)).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("mapToLengths on empty list returns empty")
        void testMapToLengthsEmpty() {
            assertThat(ops.mapToLengths(List.of())).isEmpty();
        }

        @Test
        @DisplayName("mapToUpperCase should uppercase all elements")
        void testMapToUpperCase() {
            List<String> list = List.of("hello", "world");
            assertThat(ops.mapToUpperCase(list)).containsExactly("HELLO", "WORLD");
        }

        @Test
        @DisplayName("extractNames should get names from Person records")
        void testExtractNames() {
            List<IntermediateOperations.Person> people = List.of(
                    new IntermediateOperations.Person("Alice", 30),
                    new IntermediateOperations.Person("Bob", 25),
                    new IntermediateOperations.Person("Charlie", 35)
            );
            assertThat(ops.extractNames(people)).containsExactly("Alice", "Bob", "Charlie");
        }
    }

    @Nested
    @DisplayName("flatMap")
    class FlatMapTest {

        @Test
        @DisplayName("flattenLists should flatten nested lists")
        void testFlattenLists() {
            List<List<String>> nested = List.of(
                    List.of("a", "b"),
                    List.of("c", "d"),
                    List.of("e")
            );
            assertThat(ops.flattenLists(nested)).containsExactly("a", "b", "c", "d", "e");
        }

        @Test
        @DisplayName("flattenLists with empty inner list should skip it")
        void testFlattenWithEmptyInnerList() {
            List<List<String>> nested = List.of(
                    List.of("a"),
                    List.of(),
                    List.of("b")
            );
            assertThat(ops.flattenLists(nested)).containsExactly("a", "b");
        }

        @Test
        @DisplayName("splitIntoWords should split sentences into individual words")
        void testSplitIntoWords() {
            List<String> sentences = List.of("Hello World", "Java Streams");
            assertThat(ops.splitIntoWords(sentences))
                    .containsExactly("Hello", "World", "Java", "Streams");
        }

        @Test
        @DisplayName("splitIntoWords with extra whitespace should still split correctly")
        void testSplitIntoWordsExtraSpace() {
            List<String> sentences = List.of("  Hello   World  ");
            assertThat(ops.splitIntoWords(sentences)).containsExactly("Hello", "World");
        }

        @Test
        @DisplayName("flattenOptionals should keep only present values")
        void testFlattenOptionals() {
            List<Optional<String>> optionals = Arrays.asList(
                    Optional.of("a"),
                    Optional.empty(),
                    Optional.of("c"),
                    Optional.empty(),
                    Optional.of("e")
            );
            assertThat(ops.flattenOptionals(optionals)).containsExactly("a", "c", "e");
        }

        @Test
        @DisplayName("flattenOptionals with all empty returns empty list")
        void testFlattenAllEmpty() {
            List<Optional<String>> optionals = List.of(Optional.empty(), Optional.empty());
            assertThat(ops.flattenOptionals(optionals)).isEmpty();
        }
    }

    @Nested
    @DisplayName("distinct")
    class DistinctTest {

        @Test
        @DisplayName("removeDuplicates should remove duplicate integers")
        void testRemoveDuplicates() {
            List<Integer> list = List.of(1, 2, 3, 2, 1, 4, 5, 4);
            assertThat(ops.removeDuplicates(list)).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("removeDuplicates on list with no duplicates returns same list")
        void testRemoveDuplicatesNone() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);
            assertThat(ops.removeDuplicates(list)).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("deduplicateStrings should preserve first occurrence order")
        void testDeduplicateStrings() {
            List<String> list = List.of("a", "b", "a", "c", "b", "d");
            assertThat(ops.deduplicateStrings(list)).containsExactly("a", "b", "c", "d");
        }

        @Test
        @DisplayName("distinct on empty list returns empty")
        void testDistinctEmpty() {
            assertThat(ops.removeDuplicates(List.of())).isEmpty();
        }
    }

    @Nested
    @DisplayName("sorted")
    class SortedTest {

        @Test
        @DisplayName("sortNatural should sort alphabetically")
        void testSortNatural() {
            List<String> list = List.of("banana", "apple", "cherry");
            assertThat(ops.sortNatural(list)).containsExactly("apple", "banana", "cherry");
        }

        @Test
        @DisplayName("sortByLengthThenAlpha should sort by length first")
        void testSortByLengthThenAlpha() {
            List<String> list = List.of("banana", "fig", "apple", "kiwi", "cat");
            List<String> result = ops.sortByLengthThenAlpha(list);
            // Length 3: cat, fig; Length 4: kiwi; Length 5: apple; Length 6: banana
            assertThat(result).containsExactly("cat", "fig", "kiwi", "apple", "banana");
        }

        @Test
        @DisplayName("sortWithNulls should place nulls first")
        void testSortWithNulls() {
            List<String> list = Arrays.asList("banana", null, "apple", null, "cherry");
            List<String> result = ops.sortWithNulls(list);
            assertThat(result.get(0)).isNull();
            assertThat(result.get(1)).isNull();
            assertThat(result.subList(2, 5)).containsExactly("apple", "banana", "cherry");
        }
    }

    @Nested
    @DisplayName("peek")
    class PeekTest {

        @Test
        @DisplayName("peek should observe filtered elements without modifying them")
        void testPeekAndCollect() {
            List<String> list = List.of("hi", "hello", "hey", "goodbye");
            List<String> peekCapture = new ArrayList<>();

            List<String> result = ops.peekAndCollect(list, peekCapture);

            // After filter (length > 2): "hello", "hey", "goodbye"
            assertThat(peekCapture).containsExactly("hello", "hey", "goodbye");
            // After map (uppercase):
            assertThat(result).containsExactly("HELLO", "HEY", "GOODBYE");
        }

        @Test
        @DisplayName("peek on empty stream should not call consumer")
        void testPeekEmpty() {
            List<String> peekCapture = new ArrayList<>();
            ops.peekAndCollect(List.of(), peekCapture);
            assertThat(peekCapture).isEmpty();
        }
    }

    @Nested
    @DisplayName("limit and skip")
    class LimitSkipTest {

        @Test
        @DisplayName("firstN should return first n elements")
        void testFirstN() {
            List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            assertThat(ops.firstN(list, 3)).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("firstN(0) should return empty list")
        void testFirstNZero() {
            assertThat(ops.firstN(List.of(1, 2, 3), 0)).isEmpty();
        }

        @Test
        @DisplayName("firstN with n > list size should return all elements")
        void testFirstNBeyondSize() {
            assertThat(ops.firstN(List.of(1, 2), 10)).containsExactly(1, 2);
        }

        @Test
        @DisplayName("skipN should skip first n elements")
        void testSkipN() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);
            assertThat(ops.skipN(list, 2)).containsExactly(3, 4, 5);
        }

        @Test
        @DisplayName("skipN with n == size should return empty")
        void testSkipNAll() {
            assertThat(ops.skipN(List.of(1, 2, 3), 3)).isEmpty();
        }

        @Test
        @DisplayName("paginate should return correct page")
        void testPaginate() {
            List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            // Page 0, size 3 → [1, 2, 3]
            assertThat(ops.paginate(list, 0, 3)).containsExactly(1, 2, 3);
            // Page 1, size 3 → [4, 5, 6]
            assertThat(ops.paginate(list, 1, 3)).containsExactly(4, 5, 6);
            // Page 3, size 3 → [10]
            assertThat(ops.paginate(list, 3, 3)).containsExactly(10);
        }
    }

    @Nested
    @DisplayName("takeWhile and dropWhile (Java 9+)")
    class TakeWhileDropWhileTest {

        @Test
        @DisplayName("takeWhile should stop at first non-matching element")
        void testTakeWhileLessThan() {
            List<Integer> sorted = List.of(1, 2, 3, 4, 5, 6, 7);
            assertThat(ops.takeWhileLessThan(sorted, 5)).containsExactly(1, 2, 3, 4);
        }

        @Test
        @DisplayName("takeWhile should return empty when first element does not match")
        void testTakeWhileFirstNoMatch() {
            List<Integer> list = List.of(5, 1, 2, 3);
            assertThat(ops.takeWhileLessThan(list, 5)).isEmpty();
        }

        @Test
        @DisplayName("takeWhile should not include elements after first non-match even if they match")
        void testTakeWhileStopsEarly() {
            List<Integer> list = List.of(1, 2, 5, 3, 4); // 5 breaks the < 5 predicate
            assertThat(ops.takeWhileLessThan(list, 5)).containsExactly(1, 2);
        }

        @Test
        @DisplayName("dropWhile should pass all elements after first non-match")
        void testDropWhileLessThan() {
            List<Integer> list = List.of(1, 2, 3, 4, 5, 6);
            assertThat(ops.dropWhileLessThan(list, 4)).containsExactly(4, 5, 6);
        }

        @Test
        @DisplayName("dropWhile should keep subsequent matching elements")
        void testDropWhileKeepsLater() {
            List<Integer> list = List.of(1, 2, 5, 3, 4); // 3,4 < 5 but come after non-match
            assertThat(ops.dropWhileLessThan(list, 5)).containsExactly(5, 3, 4);
        }
    }

    @Nested
    @DisplayName("mapToInt / mapToLong / mapToDouble")
    class PrimitiveMapTest {

        @Test
        @DisplayName("sumLengths should compute sum of string lengths without boxing")
        void testSumLengths() {
            List<String> list = List.of("hello", "world", "!");
            assertThat(ops.sumLengths(list)).isEqualTo(11);
        }

        @Test
        @DisplayName("sumLengths on empty list should return 0")
        void testSumLengthsEmpty() {
            assertThat(ops.sumLengths(List.of())).isEqualTo(0);
        }

        @Test
        @DisplayName("averageOfValues should compute correct average")
        void testAverageOfValues() {
            List<String> numbers = List.of("2", "4", "6");
            OptionalDouble avg = ops.averageOfValues(numbers);
            assertThat(avg).isPresent();
            assertThat(avg.getAsDouble()).isEqualTo(4.0);
        }

        @Test
        @DisplayName("averageOfValues on empty list returns empty OptionalDouble")
        void testAverageEmpty() {
            assertThat(ops.averageOfValues(List.of())).isEmpty();
        }

        @Test
        @DisplayName("sumLongValues should handle large sums")
        void testSumLongValues() {
            List<Integer> list = List.of(Integer.MAX_VALUE, Integer.MAX_VALUE);
            assertThat(ops.sumLongValues(list)).isEqualTo(2L * Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("intRangeToStrings should produce correct string range")
        void testIntRangeToStrings() {
            assertThat(ops.intRangeToStrings(0, 4)).containsExactly("0", "1", "2", "3");
        }
    }

    @Nested
    @DisplayName("mapMulti (Java 16+)")
    class MapMultiTest {

        @Test
        @DisplayName("expandToValueAndSquare should emit each value and its square")
        void testExpandToValueAndSquare() {
            List<Integer> list = List.of(2, 3, 4);
            assertThat(ops.expandToValueAndSquare(list)).containsExactly(2, 4, 3, 9, 4, 16);
        }

        @Test
        @DisplayName("expandToValueAndSquare on empty returns empty")
        void testExpandEmpty() {
            assertThat(ops.expandToValueAndSquare(List.of())).isEmpty();
        }

        @Test
        @DisplayName("filterStringsFromMixed should extract and uppercase only strings")
        void testFilterStringsFromMixed() {
            List<Object> mixed = List.of(1, "hello", 2.0, "world", 3);
            assertThat(ops.filterStringsFromMixed(mixed)).containsExactly("HELLO", "WORLD");
        }

        @Test
        @DisplayName("filterStringsFromMixed with no strings returns empty")
        void testFilterStringsNoStrings() {
            List<Object> mixed = List.of(1, 2, 3.0, 4L);
            assertThat(ops.filterStringsFromMixed(mixed)).isEmpty();
        }
    }
}
