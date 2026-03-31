package com.github.msorkhpar.claudejavatutor.enhancedenums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Generic Enum Methods Tests")
class GenericEnumMethodsTest {

    @Nested
    @DisplayName("DataConverter Enum")
    class DataConverterTest {

        @Test
        @DisplayName("Should convert string to Integer")
        void testStringToInteger() {
            Integer result = GenericEnumMethods.DataConverter.STRING_TO_NUMBER.convert("42", Integer.class);
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("Should convert string to Long")
        void testStringToLong() {
            Long result = GenericEnumMethods.DataConverter.STRING_TO_NUMBER.convert("999999999999", Long.class);
            assertThat(result).isEqualTo(999999999999L);
        }

        @Test
        @DisplayName("Should convert string to Double")
        void testStringToDouble() {
            Double result = GenericEnumMethods.DataConverter.STRING_TO_NUMBER.convert("3.14", Double.class);
            assertThat(result).isCloseTo(3.14, within(0.001));
        }

        @Test
        @DisplayName("Should convert string to Float")
        void testStringToFloat() {
            Float result = GenericEnumMethods.DataConverter.STRING_TO_NUMBER.convert("2.5", Float.class);
            assertThat(result).isCloseTo(2.5f, within(0.001f));
        }

        @Test
        @DisplayName("Should throw for unsupported target type")
        void testUnsupportedType() {
            assertThatThrownBy(() ->
                    GenericEnumMethods.DataConverter.STRING_TO_NUMBER.convert("42", Boolean.class))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should throw for invalid number format")
        void testInvalidNumber() {
            assertThatThrownBy(() ->
                    GenericEnumMethods.DataConverter.STRING_TO_NUMBER.convert("abc", Integer.class))
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("TO_STRING should convert any object to String")
        void testToString() {
            String result = GenericEnumMethods.DataConverter.TO_STRING.convert(42, String.class);
            assertThat(result).isEqualTo("42");
        }

        @Test
        @DisplayName("TO_STRING should throw for non-String target")
        void testToStringWrongTarget() {
            assertThatThrownBy(() ->
                    GenericEnumMethods.DataConverter.TO_STRING.convert(42, Integer.class))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("IDENTITY should pass through matching types")
        void testIdentity() {
            String result = GenericEnumMethods.DataConverter.IDENTITY.convert("hello", String.class);
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("IDENTITY should throw on type mismatch")
        void testIdentityMismatch() {
            assertThatThrownBy(() ->
                    GenericEnumMethods.DataConverter.IDENTITY.convert("hello", Integer.class))
                    .isInstanceOf(ClassCastException.class);
        }

        @Test
        @DisplayName("safeConvert should return Optional.empty on failure")
        void testSafeConvert() {
            Optional<Integer> result =
                    GenericEnumMethods.DataConverter.STRING_TO_NUMBER.safeConvert("abc", Integer.class);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("safeConvert should return value on success")
        void testSafeConvertSuccess() {
            Optional<Integer> result =
                    GenericEnumMethods.DataConverter.STRING_TO_NUMBER.safeConvert("42", Integer.class);
            assertThat(result).contains(42);
        }
    }

    @Nested
    @DisplayName("CollectionOp Enum")
    class CollectionOpTest {

        @Test
        @DisplayName("Should filter elements by predicate")
        void testFilter() {
            List<Integer> input = List.of(1, 2, 3, 4, 5);
            List<Integer> result = GenericEnumMethods.CollectionOp.FILTER.execute(input, n -> n > 3);
            assertThat(result).containsExactly(4, 5);
        }

        @Test
        @DisplayName("Should filter with no matches")
        void testFilterNoMatches() {
            List<Integer> input = List.of(1, 2, 3);
            List<Integer> result = GenericEnumMethods.CollectionOp.FILTER.execute(input, n -> n > 10);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should sort elements naturally")
        void testSort() {
            List<String> input = List.of("banana", "apple", "cherry");
            List<String> result = GenericEnumMethods.CollectionOp.SORT.execute(input);
            assertThat(result).containsExactly("apple", "banana", "cherry");
        }

        @Test
        @DisplayName("Should return distinct elements")
        void testDistinct() {
            List<Integer> input = List.of(1, 2, 2, 3, 3, 3);
            List<Integer> result = GenericEnumMethods.CollectionOp.DISTINCT.execute(input);
            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should reverse elements")
        void testReverse() {
            List<String> input = List.of("a", "b", "c");
            List<String> result = GenericEnumMethods.CollectionOp.REVERSE.execute(input);
            assertThat(result).containsExactly("c", "b", "a");
        }

        @Test
        @DisplayName("Should handle empty list")
        void testEmptyList() {
            List<Integer> result = GenericEnumMethods.CollectionOp.FILTER.execute(
                    Collections.emptyList(), n -> true);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle single element list")
        void testSingleElement() {
            List<Integer> result = GenericEnumMethods.CollectionOp.SORT.execute(List.of(42));
            assertThat(result).containsExactly(42);
        }
    }

    @Nested
    @DisplayName("Aggregator Enum")
    class AggregatorTest {

        @Test
        @DisplayName("Should compute sum")
        void testSum() {
            double result = GenericEnumMethods.Aggregator.SUM.aggregate(List.of(1, 2, 3, 4, 5));
            assertThat(result).isEqualTo(15.0);
        }

        @Test
        @DisplayName("Should compute average")
        void testAverage() {
            double result = GenericEnumMethods.Aggregator.AVERAGE.aggregate(List.of(10, 20, 30));
            assertThat(result).isEqualTo(20.0);
        }

        @Test
        @DisplayName("Should return 0 for average of empty list")
        void testAverageEmpty() {
            double result = GenericEnumMethods.Aggregator.AVERAGE.aggregate(Collections.emptyList());
            assertThat(result).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should compute min")
        void testMin() {
            double result = GenericEnumMethods.Aggregator.MIN.aggregate(List.of(5, 3, 8, 1));
            assertThat(result).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should compute max")
        void testMax() {
            double result = GenericEnumMethods.Aggregator.MAX.aggregate(List.of(5, 3, 8, 1));
            assertThat(result).isEqualTo(8.0);
        }

        @Test
        @DisplayName("Should throw for min/max on empty list")
        void testMinMaxEmpty() {
            assertThatThrownBy(() -> GenericEnumMethods.Aggregator.MIN.aggregate(Collections.emptyList()))
                    .isInstanceOf(NoSuchElementException.class);
            assertThatThrownBy(() -> GenericEnumMethods.Aggregator.MAX.aggregate(Collections.emptyList()))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("Should count elements")
        void testCount() {
            double result = GenericEnumMethods.Aggregator.COUNT.aggregate(List.of(1, 2, 3));
            assertThat(result).isEqualTo(3.0);
        }

        @Test
        @DisplayName("Should work with Double list")
        void testWithDoubles() {
            double result = GenericEnumMethods.Aggregator.SUM.aggregate(List.of(1.5, 2.5, 3.0));
            assertThat(result).isEqualTo(7.0);
        }

        @Test
        @DisplayName("Should sum empty list to zero")
        void testSumEmpty() {
            double result = GenericEnumMethods.Aggregator.SUM.aggregate(Collections.emptyList());
            assertThat(result).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("CollectionFactory Enum")
    class CollectionFactoryTest {

        @Test
        @DisplayName("Should create empty ArrayList")
        void testCreateArrayList() {
            Collection<String> result = GenericEnumMethods.CollectionFactory.ARRAY_LIST.create();
            assertThat(result).isEmpty();
            assertThat(result).isInstanceOf(ArrayList.class);
        }

        @Test
        @DisplayName("Should create empty LinkedList")
        void testCreateLinkedList() {
            Collection<String> result = GenericEnumMethods.CollectionFactory.LINKED_LIST.create();
            assertThat(result).isEmpty();
            assertThat(result).isInstanceOf(LinkedList.class);
        }

        @Test
        @DisplayName("Should create empty HashSet")
        void testCreateHashSet() {
            Collection<String> result = GenericEnumMethods.CollectionFactory.HASH_SET.create();
            assertThat(result).isEmpty();
            assertThat(result).isInstanceOf(HashSet.class);
        }

        @Test
        @DisplayName("Should create empty TreeSet")
        void testCreateTreeSet() {
            Collection<String> result = GenericEnumMethods.CollectionFactory.TREE_SET.create();
            assertThat(result).isEmpty();
            assertThat(result).isInstanceOf(TreeSet.class);
        }

        @Test
        @DisplayName("Should create collection from source")
        void testCreateFrom() {
            List<String> source = List.of("a", "b", "c");
            Collection<String> result = GenericEnumMethods.CollectionFactory.ARRAY_LIST.createFrom(source);
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("HashSet should deduplicate from source")
        void testHashSetDedup() {
            List<String> source = List.of("a", "a", "b");
            Collection<String> result = GenericEnumMethods.CollectionFactory.HASH_SET.createFrom(source);
            assertThat(result).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("Should create with varargs using of()")
        void testOfVarargs() {
            Collection<Integer> result = GenericEnumMethods.CollectionFactory.ARRAY_LIST.of(1, 2, 3);
            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("TreeSet should sort elements")
        void testTreeSetSorted() {
            Collection<String> result = GenericEnumMethods.CollectionFactory.TREE_SET.of("c", "a", "b");
            assertThat(result).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("CompareStrategy Enum")
    class CompareStrategyTest {

        @Test
        @DisplayName("NATURAL should compare naturally")
        void testNatural() {
            assertThat(GenericEnumMethods.CompareStrategy.NATURAL.compare("a", "b")).isNegative();
            assertThat(GenericEnumMethods.CompareStrategy.NATURAL.compare("b", "a")).isPositive();
            assertThat(GenericEnumMethods.CompareStrategy.NATURAL.compare("a", "a")).isZero();
        }

        @Test
        @DisplayName("REVERSE should compare in reverse order")
        void testReverse() {
            assertThat(GenericEnumMethods.CompareStrategy.REVERSE.compare("a", "b")).isPositive();
            assertThat(GenericEnumMethods.CompareStrategy.REVERSE.compare("b", "a")).isNegative();
        }

        @Test
        @DisplayName("BY_STRING should compare by string representation")
        void testByString() {
            assertThat(GenericEnumMethods.CompareStrategy.BY_STRING.compare(1, 2)).isNegative();
            assertThat(GenericEnumMethods.CompareStrategy.BY_STRING.compare("hello", "world")).isNegative();
        }

        @Test
        @DisplayName("Should create Comparator from strategy")
        void testToComparator() {
            Comparator<String> comparator = GenericEnumMethods.CompareStrategy.NATURAL.toComparator();
            List<String> list = new ArrayList<>(List.of("c", "a", "b"));
            list.sort(comparator);
            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should find min element")
        void testMin() {
            String min = GenericEnumMethods.CompareStrategy.NATURAL.min(List.of("c", "a", "b"));
            assertThat(min).isEqualTo("a");
        }

        @Test
        @DisplayName("Should find max element")
        void testMax() {
            String max = GenericEnumMethods.CompareStrategy.NATURAL.max(List.of("c", "a", "b"));
            assertThat(max).isEqualTo("c");
        }

        @Test
        @DisplayName("Should throw on empty list for min/max")
        void testMinMaxEmpty() {
            assertThatThrownBy(() ->
                    GenericEnumMethods.CompareStrategy.NATURAL.min(Collections.emptyList()))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("NATURAL should throw for non-Comparable types")
        void testNonComparable() {
            Object a = new Object();
            Object b = new Object();
            assertThatThrownBy(() -> GenericEnumMethods.CompareStrategy.NATURAL.compare(a, b))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("BY_HASH_CODE should compare by hash codes")
        void testByHashCode() {
            int result = GenericEnumMethods.CompareStrategy.BY_HASH_CODE.compare("a", "b");
            assertThat(result).isEqualTo(Integer.compare("a".hashCode(), "b".hashCode()));
        }
    }
}
