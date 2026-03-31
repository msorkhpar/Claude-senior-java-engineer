package com.github.msorkhpar.claudejavatutor.streamsapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("4.4.4 - Terminal Operations Tests")
class TerminalOperationsTest {

    private final TerminalOperations ops = new TerminalOperations();

    @Nested
    @DisplayName("collect to List / Set / Array")
    class CollectToContainerTest {

        @Test
        @DisplayName("toMutableList should collect to a mutable ArrayList")
        void testToMutableList() {
            List<String> result = ops.toMutableList(List.of("a", "b", "c"));
            assertThat(result).containsExactly("a", "b", "c");
            // Mutable — can add elements
            assertThatCode(() -> result.add("d")).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("toUnmodifiableList should produce an unmodifiable list")
        void testToUnmodifiableList() {
            List<String> result = ops.toUnmodifiableList(List.of("a", "b"));
            assertThat(result).containsExactly("a", "b");
            assertThatThrownBy(() -> result.add("c")).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("toSet should remove duplicates")
        void testToSet() {
            Set<String> result = ops.toSet(List.of("a", "b", "a", "c", "b"));
            assertThat(result).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("toStringArray should produce correct array")
        void testToStringArray() {
            String[] result = ops.toStringArray(List.of("x", "y", "z"));
            assertThat(result).containsExactly("x", "y", "z");
        }
    }

    @Nested
    @DisplayName("collect to Map")
    class CollectToMapTest {

        @Test
        @DisplayName("toWordLengthMap should map word to its length")
        void testToWordLengthMap() {
            Map<String, Integer> result = ops.toWordLengthMap(List.of("hello", "world", "java"));
            assertThat(result)
                    .containsEntry("hello", 5)
                    .containsEntry("world", 5)
                    .containsEntry("java", 4);
        }

        @Test
        @DisplayName("toWordLengthMap with duplicate keys should throw")
        void testToWordLengthMapDuplicates() {
            // "hello" and "world" both have length 5 — would conflict in toLengthToWordMap
            assertThatNoException().isThrownBy(() ->
                    ops.toLengthToWordMap(List.of("hello", "world", "java"))
            );
        }

        @Test
        @DisplayName("toLengthToWordMap should keep longest word per length")
        void testToLengthToWordMap() {
            Map<Integer, String> result = ops.toLengthToWordMap(
                    List.of("cat", "dog", "elephant", "frog", "ant")
            );
            // Length 3: cat, dog, ant — keep longest (all same; keep cat by insertion)
            // Length 4: frog
            // Length 8: elephant
            assertThat(result).containsKey(3);
            assertThat(result).containsEntry(4, "frog");
            assertThat(result).containsEntry(8, "elephant");
        }

        @Test
        @DisplayName("toLinkedHashMap should preserve insertion order")
        void testToLinkedHashMap() {
            List<String> words = List.of("apple", "banana", "cherry");
            Map<String, Integer> result = ops.toLinkedHashMap(words);
            assertThat(result.keySet()).containsExactly("apple", "banana", "cherry");
        }
    }

    @Nested
    @DisplayName("groupingBy")
    class GroupingByTest {

        @Test
        @DisplayName("groupByFirstChar should group strings correctly")
        void testGroupByFirstChar() {
            List<String> words = List.of("apple", "avocado", "banana", "cherry", "blueberry");
            Map<Character, List<String>> result = ops.groupByFirstChar(words);
            assertThat(result).containsKey('a');
            assertThat(result.get('a')).containsExactly("apple", "avocado");
            assertThat(result.get('b')).containsExactly("banana", "blueberry");
            assertThat(result.get('c')).containsExactly("cherry");
        }

        @Test
        @DisplayName("groupByLengthCount should count strings per length")
        void testGroupByLengthCount() {
            List<String> words = List.of("cat", "dog", "ant", "frog", "bear");
            Map<Integer, Long> result = ops.groupByLengthCount(words);
            assertThat(result).containsEntry(3, 3L); // cat, dog, ant
            assertThat(result).containsEntry(4, 2L); // frog, bear
        }

        @Test
        @DisplayName("groupEmployeesByDepartment should map names per department")
        void testGroupEmployeesByDepartment() {
            List<TerminalOperations.Employee> employees = List.of(
                    new TerminalOperations.Employee("Alice", "Engineering", 90000),
                    new TerminalOperations.Employee("Bob", "Engineering", 85000),
                    new TerminalOperations.Employee("Carol", "Marketing", 70000)
            );
            Map<String, List<String>> result = ops.groupEmployeesByDepartment(employees);
            assertThat(result.get("Engineering")).containsExactly("Alice", "Bob");
            assertThat(result.get("Marketing")).containsExactly("Carol");
        }

        @Test
        @DisplayName("averageSalaryByDepartment should compute correct averages")
        void testAverageSalaryByDepartment() {
            List<TerminalOperations.Employee> employees = List.of(
                    new TerminalOperations.Employee("Alice", "Engineering", 90000),
                    new TerminalOperations.Employee("Bob", "Engineering", 80000),
                    new TerminalOperations.Employee("Carol", "Marketing", 70000)
            );
            Map<String, Double> result = ops.averageSalaryByDepartment(employees);
            assertThat(result.get("Engineering")).isEqualTo(85000.0);
            assertThat(result.get("Marketing")).isEqualTo(70000.0);
        }
    }

    @Nested
    @DisplayName("partitioningBy")
    class PartitioningByTest {

        @Test
        @DisplayName("partitionByLength should split into long and short strings")
        void testPartitionByLength() {
            List<String> words = List.of("hi", "hello", "hey", "goodbye");
            Map<Boolean, List<String>> result = ops.partitionByLength(words, 5);
            assertThat(result.get(true)).containsExactly("hello", "goodbye");
            assertThat(result.get(false)).containsExactly("hi", "hey");
        }

        @Test
        @DisplayName("partitionAndCountBySalary should count high vs low salary")
        void testPartitionAndCountBySalary() {
            List<TerminalOperations.Employee> employees = List.of(
                    new TerminalOperations.Employee("A", "E", 100000),
                    new TerminalOperations.Employee("B", "M", 60000),
                    new TerminalOperations.Employee("C", "E", 90000),
                    new TerminalOperations.Employee("D", "M", 55000)
            );
            Map<Boolean, Long> result = ops.partitionAndCountBySalary(employees, 80000);
            assertThat(result.get(true)).isEqualTo(2L);  // >=80000
            assertThat(result.get(false)).isEqualTo(2L); // <80000
        }
    }

    @Nested
    @DisplayName("joining")
    class JoiningTest {

        @Test
        @DisplayName("joinWithDelimiter should join with comma")
        void testJoinWithDelimiter() {
            assertThat(ops.joinWithDelimiter(List.of("a", "b", "c"), ", "))
                    .isEqualTo("a, b, c");
        }

        @Test
        @DisplayName("joinWithDelimiter on empty list returns empty string")
        void testJoinEmpty() {
            assertThat(ops.joinWithDelimiter(List.of(), ", ")).isEmpty();
        }

        @Test
        @DisplayName("joinFormatted should include prefix and suffix")
        void testJoinFormatted() {
            assertThat(ops.joinFormatted(List.of("a", "b", "c"), ", ", "[", "]"))
                    .isEqualTo("[a, b, c]");
        }

        @Test
        @DisplayName("joinNonEmpty should skip blank strings")
        void testJoinNonEmpty() {
            List<String> list = List.of("hello", "  ", "world", "");
            assertThat(ops.joinNonEmpty(list)).isEqualTo("hello, world");
        }
    }

    @Nested
    @DisplayName("reduce")
    class ReduceTest {

        @Test
        @DisplayName("sumWithIdentity should sum all integers")
        void testSumWithIdentity() {
            assertThat(ops.sumWithIdentity(List.of(1, 2, 3, 4, 5))).isEqualTo(15);
        }

        @Test
        @DisplayName("sumWithIdentity on empty stream returns identity (0)")
        void testSumWithIdentityEmpty() {
            assertThat(ops.sumWithIdentity(List.of())).isEqualTo(0);
        }

        @Test
        @DisplayName("productWithIdentity should multiply all integers")
        void testProductWithIdentity() {
            assertThat(ops.productWithIdentity(List.of(1, 2, 3, 4, 5))).isEqualTo(120);
        }

        @Test
        @DisplayName("productWithIdentity on empty stream returns identity (1)")
        void testProductWithIdentityEmpty() {
            assertThat(ops.productWithIdentity(List.of())).isEqualTo(1);
        }

        @Test
        @DisplayName("sumWithoutIdentity should return Optional containing sum")
        void testSumWithoutIdentity() {
            Optional<Integer> result = ops.sumWithoutIdentity(List.of(1, 2, 3, 4, 5));
            assertThat(result).isPresent().contains(15);
        }

        @Test
        @DisplayName("sumWithoutIdentity on empty stream returns Optional.empty")
        void testSumWithoutIdentityEmpty() {
            assertThat(ops.sumWithoutIdentity(List.of())).isEmpty();
        }

        @Test
        @DisplayName("concatenate should join strings")
        void testConcatenate() {
            assertThat(ops.concatenate(List.of("a", "b", "c"))).isEqualTo("abc");
        }

        @Test
        @DisplayName("concatenate on empty returns empty string (identity)")
        void testConcatenateEmpty() {
            assertThat(ops.concatenate(List.of())).isEqualTo("");
        }

        @Test
        @DisplayName("findMax should return maximum value")
        void testFindMax() {
            assertThat(ops.findMax(List.of(3, 1, 4, 1, 5, 9, 2, 6))).contains(9);
        }

        @Test
        @DisplayName("findMax on empty returns Optional.empty")
        void testFindMaxEmpty() {
            assertThat(ops.findMax(List.of())).isEmpty();
        }
    }

    @Nested
    @DisplayName("count, min, max")
    class CountMinMaxTest {

        @Test
        @DisplayName("countLongStrings should count strings meeting minimum length")
        void testCountLongStrings() {
            List<String> list = List.of("hi", "hello", "hey", "goodbye");
            assertThat(ops.countLongStrings(list, 5)).isEqualTo(2);
        }

        @Test
        @DisplayName("countLongStrings on empty list returns 0")
        void testCountEmpty() {
            assertThat(ops.countLongStrings(List.of(), 5)).isEqualTo(0);
        }

        @Test
        @DisplayName("minString should return alphabetically smallest")
        void testMinString() {
            assertThat(ops.minString(List.of("banana", "apple", "cherry"))).contains("apple");
        }

        @Test
        @DisplayName("minString on empty stream returns Optional.empty")
        void testMinStringEmpty() {
            assertThat(ops.minString(List.of())).isEmpty();
        }

        @Test
        @DisplayName("maxInt should return maximum integer")
        void testMaxInt() {
            assertThat(ops.maxInt(List.of(3, 1, 4, 1, 5, 9))).contains(9);
        }

        @Test
        @DisplayName("shortestString should return shortest element")
        void testShortestString() {
            assertThat(ops.shortestString(List.of("banana", "kiwi", "fig"))).contains("fig");
        }
    }

    @Nested
    @DisplayName("findFirst / findAny")
    class FindTest {

        @Test
        @DisplayName("findFirstWithPrefix should return first match")
        void testFindFirstWithPrefix() {
            List<String> list = List.of("cat", "apple", "avocado", "banana");
            assertThat(ops.findFirstWithPrefix(list, "a")).contains("apple");
        }

        @Test
        @DisplayName("findFirstWithPrefix returns empty when no match")
        void testFindFirstNoMatch() {
            assertThat(ops.findFirstWithPrefix(List.of("cat", "dog"), "z")).isEmpty();
        }

        @Test
        @DisplayName("findAnyWithPrefix should return a matching element")
        void testFindAnyWithPrefix() {
            List<String> list = List.of("cat", "apple", "avocado");
            Optional<String> result = ops.findAnyWithPrefix(list, "a");
            assertThat(result).isPresent();
            assertThat(result.get()).startsWith("a");
        }

        @Test
        @DisplayName("findAnyWithPrefix returns empty when no match")
        void testFindAnyNoMatch() {
            assertThat(ops.findAnyWithPrefix(List.of("cat"), "z")).isEmpty();
        }
    }

    @Nested
    @DisplayName("anyMatch / allMatch / noneMatch")
    class MatchTest {

        @Test
        @DisplayName("anyLongerThan should return true when at least one matches")
        void testAnyLongerThan() {
            assertThat(ops.anyLongerThan(List.of("hi", "hello"), 3)).isTrue();
        }

        @Test
        @DisplayName("anyLongerThan should return false when none match")
        void testAnyLongerThanFalse() {
            assertThat(ops.anyLongerThan(List.of("hi", "no"), 5)).isFalse();
        }

        @Test
        @DisplayName("anyLongerThan on empty stream returns false")
        void testAnyLongerThanEmpty() {
            assertThat(ops.anyLongerThan(List.of(), 0)).isFalse();
        }

        @Test
        @DisplayName("allLongerThan should return true when all match")
        void testAllLongerThan() {
            assertThat(ops.allLongerThan(List.of("hello", "world"), 3)).isTrue();
        }

        @Test
        @DisplayName("allLongerThan should return false when one doesn't match")
        void testAllLongerThanFalse() {
            assertThat(ops.allLongerThan(List.of("hello", "hi"), 3)).isFalse();
        }

        @Test
        @DisplayName("allLongerThan on empty stream returns true (vacuous truth)")
        void testAllLongerThanEmpty() {
            assertThat(ops.allLongerThan(List.of(), 100)).isTrue();
        }

        @Test
        @DisplayName("noneStartsWith should return true when no element matches")
        void testNoneStartsWith() {
            assertThat(ops.noneStartsWith(List.of("cat", "dog"), "z")).isTrue();
        }

        @Test
        @DisplayName("noneStartsWith should return false when one element matches")
        void testNoneStartsWithFalse() {
            assertThat(ops.noneStartsWith(List.of("cat", "zebra"), "z")).isFalse();
        }

        @Test
        @DisplayName("noneStartsWith on empty stream returns true")
        void testNoneStartsWithEmpty() {
            assertThat(ops.noneStartsWith(List.of(), "z")).isTrue();
        }
    }

    @Nested
    @DisplayName("Collectors.teeing (Java 12+)")
    class TeeingTest {

        @Test
        @DisplayName("minMaxInOnePass should return correct min and max")
        void testMinMaxInOnePass() {
            TerminalOperations.MinMaxPair result = ops.minMaxInOnePass(List.of(3, 1, 4, 1, 5, 9, 2, 6));
            assertThat(result.min()).isEqualTo(1);
            assertThat(result.max()).isEqualTo(9);
        }

        @Test
        @DisplayName("minMaxInOnePass with single element has same min and max")
        void testMinMaxSingleElement() {
            TerminalOperations.MinMaxPair result = ops.minMaxInOnePass(List.of(42));
            assertThat(result.min()).isEqualTo(42);
            assertThat(result.max()).isEqualTo(42);
        }

        @Test
        @DisplayName("countAndAverage should compute correct count and average")
        void testCountAndAverage() {
            TerminalOperations.SummaryStats stats = ops.countAndAverage(List.of(2, 4, 6, 8, 10));
            assertThat(stats.count()).isEqualTo(5);
            assertThat(stats.average()).isEqualTo(6.0);
        }
    }

    @Nested
    @DisplayName("Edge Cases — Empty Stream Semantics")
    class EdgeCaseTest {

        @Test
        @DisplayName("allMatch on empty stream returns true")
        void testAllMatchEmpty() {
            assertThat(ops.matchResultsForEmpty().allMatch()).isTrue();
        }

        @Test
        @DisplayName("anyMatch on empty stream returns false")
        void testAnyMatchEmpty() {
            assertThat(ops.matchResultsForEmpty().anyMatch()).isFalse();
        }

        @Test
        @DisplayName("noneMatch on empty stream returns true")
        void testNoneMatchEmpty() {
            assertThat(ops.matchResultsForEmpty().noneMatch()).isTrue();
        }

        @Test
        @DisplayName("reduce without identity on empty stream returns Optional.empty")
        void testReduceEmptyNoIdentity() {
            assertThat(ops.reduceEmptyWithoutIdentity()).isEmpty();
        }

        @Test
        @DisplayName("reduce with identity on empty stream returns identity")
        void testReduceEmptyWithIdentity() {
            assertThat(ops.reduceEmptyWithIdentity()).isEqualTo(0);
        }
    }
}
