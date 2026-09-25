package com.github.msorkhpar.claudejavatutor.streamsapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("4.4.2 - Stream Creation Tests")
class StreamCreationTest {

    private final StreamCreation creation = new StreamCreation();

    @Nested
    @DisplayName("From Collections")
    class FromCollectionsTest {

        @Test
        @DisplayName("Should stream from a List")
        void testFromList() {
            List<String> list = List.of("a", "b", "c");
            assertThat(creation.fromList(list)).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should stream from an empty List")
        void testFromEmptyList() {
            assertThat(creation.fromList(List.of())).isEmpty();
        }

        @Test
        @DisplayName("Should stream from a Set (any order)")
        void testFromSet() {
            Set<String> set = Set.of("a", "b", "c");
            assertThat(creation.fromSet(set)).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("Should stream map entries")
        void testFromMapEntries() {
            Map<String, Integer> map = Map.of("apple", 5, "cat", 3);
            assertThat(creation.fromMapEntries(map)).hasSize(2);
        }
    }

    @Nested
    @DisplayName("From Arrays")
    class FromArraysTest {

        @Test
        @DisplayName("Should stream from a String array")
        void testFromStringArray() {
            String[] arr = {"x", "y", "z"};
            assertThat(creation.fromStringArray(arr)).containsExactly("x", "y", "z");
        }

        @Test
        @DisplayName("Should stream from a primitive int array as IntStream")
        void testFromIntArray() {
            int[] arr = {1, 2, 3, 4, 5};
            assertThat(creation.fromIntArray(arr).sum()).isEqualTo(15);
        }

        @Test
        @DisplayName("Should stream sub-range of array")
        void testFromArrayRange() {
            String[] arr = {"a", "b", "c", "d", "e"};
            assertThat(creation.fromArrayRange(arr, 1, 4)).containsExactly("b", "c", "d");
        }

        @Test
        @DisplayName("Should return empty stream for same start/end range")
        void testFromArrayRangeEmpty() {
            String[] arr = {"a", "b", "c"};
            assertThat(creation.fromArrayRange(arr, 2, 2)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodsTest {

        @Test
        @DisplayName("Stream.of with varargs should contain all elements")
        void testFromVarargs() {
            assertThat(creation.fromVarargs("a", "b", "c")).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Empty stream should have no elements")
        void testEmptyStream() {
            assertThat(creation.emptyStream()).isEmpty();
        }

        @Test
        @DisplayName("Stream.ofNullable(null) should return empty stream")
        void testNullableStreamWithNull() {
            assertThat(creation.nullableStream(null)).isEmpty();
        }

        @Test
        @DisplayName("Stream.ofNullable(value) should return singleton stream")
        void testNullableStreamWithValue() {
            assertThat(creation.nullableStream("hello")).containsExactly("hello");
        }
    }

    @Nested
    @DisplayName("Infinite Streams")
    class InfiniteStreamsTest {

        @Test
        @DisplayName("Generate should produce correct number of random doubles")
        void testGenerateRandomNumbers() {
            List<Double> randoms = creation.generateRandomNumbers(10);
            assertThat(randoms)
                    .hasSize(10)
                    .allMatch(d -> d >= 0.0 && d < 1.0);
        }

        @Test
        @DisplayName("Generate should produce 0 elements when count is 0")
        void testGenerateZero() {
            assertThat(creation.generateRandomNumbers(0)).isEmpty();
        }

        @Test
        @DisplayName("Iterate powers of 2 should produce correct sequence")
        void testIteratePowersOfTwo() {
            List<Integer> powers = creation.iteratePowersOfTwo(6);
            assertThat(powers).containsExactly(1, 2, 4, 8, 16, 32);
        }

        @Test
        @DisplayName("Bounded iterate should produce correct range")
        void testIterateBounded() {
            List<Integer> range = creation.iterateBounded(3, 8);
            assertThat(range).containsExactly(3, 4, 5, 6, 7);
        }

        @Test
        @DisplayName("Bounded iterate with same start and end returns empty")
        void testIterateBoundedEmpty() {
            assertThat(creation.iterateBounded(5, 5)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Primitive Streams")
    class PrimitiveStreamsTest {

        @Test
        @DisplayName("IntStream.range should produce exclusive end")
        void testIntRange() {
            assertThat(creation.intRange(0, 5).boxed()).containsExactly(0, 1, 2, 3, 4);
        }

        @Test
        @DisplayName("IntStream.rangeClosed should produce inclusive end")
        void testIntRangeClosed() {
            assertThat(creation.intRangeClosed(1, 5).boxed()).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("sumRange should compute correct sum")
        void testSumRange() {
            // 1+2+3+4+5 = 15
            assertThat(creation.sumRange(1, 5)).isEqualTo(15);
        }

        @Test
        @DisplayName("rangeStatistics should have correct values")
        void testRangeStatistics() {
            var stats = creation.rangeStatistics(1, 5);
            assertThat(stats.getCount()).isEqualTo(5);
            assertThat(stats.getSum()).isEqualTo(15);
            assertThat(stats.getMin()).isEqualTo(1);
            assertThat(stats.getMax()).isEqualTo(5);
            assertThat(stats.getAverage()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("boxedIntStream should box correctly")
        void testBoxedIntStream() {
            assertThat(creation.boxedIntStream(0, 4)).containsExactly(0, 1, 2, 3);
        }

        @Test
        @DisplayName("Empty range should return empty stream")
        void testEmptyRange() {
            assertThat(creation.intRange(5, 5)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Stream.builder()")
    class BuilderTest {

        @Test
        @DisplayName("Builder should include header and footer when flags are true")
        void testBuilderWithHeaderAndFooter() {
            List<String> items = List.of("item1", "item2", "item3");
            List<String> result = creation.buildConditionally(items, true, true);
            assertThat(result).containsExactly("HEADER", "item1", "item2", "item3", "FOOTER");
        }

        @Test
        @DisplayName("Builder should exclude header when flag is false")
        void testBuilderNoHeader() {
            List<String> items = List.of("item1");
            List<String> result = creation.buildConditionally(items, false, true);
            assertThat(result).containsExactly("item1", "FOOTER");
        }

        @Test
        @DisplayName("Builder should filter blank items")
        void testBuilderFiltersBlank() {
            List<String> items = Arrays.asList("item1", "  ", null, "item2");
            List<String> result = creation.buildConditionally(items, false, false);
            assertThat(result).containsExactly("item1", "item2");
        }

        @Test
        @DisplayName("Builder with empty items list should produce only header/footer")
        void testBuilderEmptyItems() {
            List<String> result = creation.buildConditionally(List.of(), true, true);
            assertThat(result).containsExactly("HEADER", "FOOTER");
        }
    }

    @Nested
    @DisplayName("Stream.concat()")
    class ConcatTest {

        @Test
        @DisplayName("concat should combine two streams in order")
        void testConcat() {
            Stream<String> first = Stream.of("a", "b", "c");
            Stream<String> second = Stream.of("d", "e", "f");
            assertThat(creation.concat(first, second)).containsExactly("a", "b", "c", "d", "e", "f");
        }

        @Test
        @DisplayName("concat with empty first stream should return second stream")
        void testConcatFirstEmpty() {
            Stream<String> first = Stream.empty();
            Stream<String> second = Stream.of("d", "e");
            assertThat(creation.concat(first, second)).containsExactly("d", "e");
        }

        @Test
        @DisplayName("concatMultiple should combine all streams in order")
        void testConcatMultiple() {
            List<String> result = creation.concatMultiple(
                    Stream.of("a", "b"),
                    Stream.of("c", "d"),
                    Stream.of("e", "f")
            );
            assertThat(result).containsExactly("a", "b", "c", "d", "e", "f");
        }
    }

    @Nested
    @DisplayName("Files.lines()")
    class FilesLinesTest {

        @Test
        @DisplayName("countNonBlankLines should count correctly")
        void testCountNonBlankLines(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "line1\n\nline2\n  \nline3\n");

            assertThat(creation.countNonBlankLines(file)).isEqualTo(3);
        }

        @Test
        @DisplayName("readNonBlankLines should return non-blank lines")
        void testReadNonBlankLines(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "hello\n\nworld\n  \n");

            List<String> lines = creation.readNonBlankLines(file);
            assertThat(lines).containsExactly("hello", "world");
        }

        @Test
        @DisplayName("countNonBlankLines on empty file returns 0")
        void testCountNonBlankLinesEmptyFile(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("empty.txt");
            Files.writeString(file, "");
            assertThat(creation.countNonBlankLines(file)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("String-based streams")
    class StringStreamTest {

        @Test
        @DisplayName("countVowels should count vowels correctly")
        void testCountVowels() {
            assertThat(creation.countVowels("hello world")).isEqualTo(3); // e, o, o
        }

        @Test
        @DisplayName("countVowels should be case insensitive")
        void testCountVowelsCaseInsensitive() {
            assertThat(creation.countVowels("AEIOU")).isEqualTo(5);
        }

        @Test
        @DisplayName("countVowels on empty string returns 0")
        void testCountVowelsEmpty() {
            assertThat(creation.countVowels("")).isEqualTo(0);
        }

        @Test
        @DisplayName("splitAndStream should split and trim correctly")
        void testSplitAndStream() {
            List<String> parts = creation.splitAndStream("  a , b , c , ", ",");
            assertThat(parts).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("splitAndStream should filter empty parts")
        void testSplitAndStreamEmpty() {
            assertThat(creation.splitAndStream("", ",")).isEmpty();
        }
    }
}
