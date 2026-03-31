package com.github.msorkhpar.claudejavatutor.methodreferences;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("4.3.2 - Method Reference Types Tests")
class MethodReferenceTypesTest {

    // ─────────────────────────────────────────────────
    // Static Method References
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Type 1: Static Method References")
    class StaticMethodReferencesTest {

        private final MethodReferenceTypes.StaticMethodReferences refs =
                new MethodReferenceTypes.StaticMethodReferences();

        @Test
        @DisplayName("parseIntegers converts valid string list to integers")
        void parseIntegers_convertsValidStrings() {
            assertThat(refs.parseIntegers(List.of("1", "2", "3", "100")))
                    .containsExactly(1, 2, 3, 100);
        }

        @Test
        @DisplayName("parseIntegers throws NumberFormatException for invalid strings")
        void parseIntegers_throwsForInvalidInput() {
            assertThatThrownBy(() -> refs.parseIntegers(List.of("1", "two", "3")))
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("parseIntegers handles empty list")
        void parseIntegers_handlesEmptyList() {
            assertThat(refs.parseIntegers(Collections.emptyList())).isEmpty();
        }

        @Test
        @DisplayName("absoluteValues returns positive values for all inputs")
        void absoluteValues_returnsPositiveValues() {
            assertThat(refs.absoluteValues(Arrays.asList(-5, 3, -10, 0, 7)))
                    .containsExactly(5, 3, 10, 0, 7);
        }

        @Test
        @DisplayName("absoluteValues handles empty list")
        void absoluteValues_handlesEmptyList() {
            assertThat(refs.absoluteValues(Collections.emptyList())).isEmpty();
        }

        @Test
        @DisplayName("absoluteValues handles Integer.MIN_VALUE edge case")
        void absoluteValues_handlesMinValue() {
            // Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE due to overflow
            List<Integer> result = refs.absoluteValues(List.of(Integer.MIN_VALUE));
            assertThat(result).containsExactly(Integer.MIN_VALUE);
        }

        @Test
        @DisplayName("filterNulls removes null elements from list")
        void filterNulls_removesNullElements() {
            List<String> withNulls = Arrays.asList("a", null, "b", null, "c");
            assertThat(refs.filterNulls(withNulls)).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("filterNulls returns empty list for all-null input")
        void filterNulls_returnsEmptyForAllNulls() {
            List<String> allNulls = Arrays.asList(null, null, null);
            assertThat(refs.<String>filterNulls(allNulls)).isEmpty();
        }

        @Test
        @DisplayName("sortAscending sorts integers in ascending order")
        void sortAscending_sortsList() {
            assertThat(refs.sortAscending(Arrays.asList(5, 1, 3, 2, 4)))
                    .containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("safeParseAll handles mixed valid and invalid strings")
        void safeParseAll_handlesMixedInput() {
            assertThat(refs.safeParseAll(List.of("1", "abc", "  3  ", "xyz", "100")))
                    .containsExactly(1, 0, 3, 0, 100);
        }

        @Test
        @DisplayName("safeParseInt returns 0 for null-like invalid input")
        void safeParseInt_returnsZeroForInvalid() {
            assertThat(MethodReferenceTypes.StaticMethodReferences.safeParseInt("abc")).isEqualTo(0);
        }
    }

    // ─────────────────────────────────────────────────
    // Bound Instance Method References
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Type 2: Bound Instance Method References")
    class BoundInstanceMethodReferencesTest {

        private final MethodReferenceTypes.BoundInstanceMethodReferences refs =
                new MethodReferenceTypes.BoundInstanceMethodReferences();

        @Test
        @DisplayName("containedInCollection filters items present in the collection")
        void containedInCollection_filtersCorrectly() {
            List<String> validNames = Arrays.asList("Alice", "Bob", "Charlie");
            Predicate<String> inValid = refs.containedInCollection(validNames);

            assertThat(inValid.test("Alice")).isTrue();
            assertThat(inValid.test("Bob")).isTrue();
            assertThat(inValid.test("Diana")).isFalse();
            assertThat(inValid.test("unknown")).isFalse();
        }

        @Test
        @DisplayName("containedInCollection handles empty valid collection")
        void containedInCollection_emptyCollectionNeverMatches() {
            Predicate<String> inEmpty = refs.containedInCollection(Collections.emptyList());
            assertThat(inEmpty.test("anything")).isFalse();
        }

        @Test
        @DisplayName("appendToBuilder appends strings to the captured StringBuilder")
        void appendToBuilder_appendsToCaptures() {
            StringBuilder sb = new StringBuilder("start-");
            Consumer<String> appender = refs.appendToBuilder(sb);

            appender.accept("middle-");
            appender.accept("end");

            assertThat(sb.toString()).isEqualTo("start-middle-end");
        }

        @Test
        @DisplayName("appendToBuilder different consumers use different StringBuilders")
        void appendToBuilder_differentConsumersDifferentBuilders() {
            StringBuilder sb1 = new StringBuilder("A");
            StringBuilder sb2 = new StringBuilder("B");
            Consumer<String> appender1 = refs.appendToBuilder(sb1);
            Consumer<String> appender2 = refs.appendToBuilder(sb2);

            appender1.accept("1");
            appender2.accept("2");

            assertThat(sb1.toString()).isEqualTo("A1");
            assertThat(sb2.toString()).isEqualTo("B2");
        }

        @Test
        @DisplayName("sortCaseInsensitive sorts ignoring case")
        void sortCaseInsensitive_sortsIgnoringCase() {
            List<String> words = Arrays.asList("Banana", "apple", "CHERRY", "date");
            List<String> sorted = refs.sortCaseInsensitive(words);
            // Case-insensitive order: apple < Banana < CHERRY < date
            assertThat(sorted).containsExactly("apple", "Banana", "CHERRY", "date");
        }

        @Test
        @DisplayName("createUpperCaseSupplier always returns uppercase of the captured string")
        void createUpperCaseSupplier_alwaysReturnsSameUpperCase() {
            Supplier<String> supplier = refs.createUpperCaseSupplier("hello");
            assertThat(supplier.get()).isEqualTo("HELLO");
            assertThat(supplier.get()).isEqualTo("HELLO"); // second call same result
        }

        @Test
        @DisplayName("createUpperCaseSupplier for different strings creates different suppliers")
        void createUpperCaseSupplier_differentStringsDifferentSuppliers() {
            Supplier<String> sup1 = refs.createUpperCaseSupplier("hello");
            Supplier<String> sup2 = refs.createUpperCaseSupplier("world");
            assertThat(sup1.get()).isEqualTo("HELLO");
            assertThat(sup2.get()).isEqualTo("WORLD");
        }
    }

    // ─────────────────────────────────────────────────
    // Unbound Instance Method References
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Type 3: Unbound Instance Method References")
    class UnboundInstanceMethodReferencesTest {

        private final MethodReferenceTypes.UnboundInstanceMethodReferences refs =
                new MethodReferenceTypes.UnboundInstanceMethodReferences();

        @Test
        @DisplayName("getLengths returns correct lengths for each string")
        void getLengths_returnsCorrectLengths() {
            assertThat(refs.getLengths(List.of("", "a", "ab", "abc")))
                    .containsExactly(0, 1, 2, 3);
        }

        @Test
        @DisplayName("getLengths handles empty list")
        void getLengths_handlesEmptyList() {
            assertThat(refs.getLengths(Collections.emptyList())).isEmpty();
        }

        @Test
        @DisplayName("toUpperCase converts all strings to uppercase")
        void toUpperCase_convertsAll() {
            assertThat(refs.toUpperCase(List.of("hello", "World", "JAVA")))
                    .containsExactly("HELLO", "WORLD", "JAVA");
        }

        @Test
        @DisplayName("sortAlphabetically sorts strings in natural order")
        void sortAlphabetically_sortsList() {
            assertThat(refs.sortAlphabetically(Arrays.asList("cherry", "apple", "banana")))
                    .containsExactly("apple", "banana", "cherry");
        }

        @Test
        @DisplayName("sortAlphabetically handles single-element list")
        void sortAlphabetically_singleElement() {
            assertThat(refs.sortAlphabetically(List.of("only"))).containsExactly("only");
        }

        @Test
        @DisplayName("filterNonEmpty removes empty lists")
        void filterNonEmpty_removesEmptyLists() {
            List<List<String>> input = Arrays.asList(
                    List.of("a", "b"),
                    Collections.emptyList(),
                    List.of("c"),
                    Collections.emptyList()
            );
            List<List<String>> result = refs.filterNonEmpty(input);
            assertThat(result).hasSize(2);
            assertThat(result.get(0)).containsExactly("a", "b");
            assertThat(result.get(1)).containsExactly("c");
        }

        @Test
        @DisplayName("trimAll trims whitespace from all strings")
        void trimAll_trimsWhitespace() {
            assertThat(refs.trimAll(List.of("  hello  ", " world ", "no-spaces")))
                    .containsExactly("hello", "world", "no-spaces");
        }

        @Test
        @DisplayName("filterBlanks removes blank strings")
        void filterBlanks_removesBlankStrings() {
            List<String> withBlanks = Arrays.asList("hello", "", "  ", "world", "\t");
            assertThat(refs.filterBlanks(withBlanks)).containsExactly("hello", "world");
        }

        @Test
        @DisplayName("filterBlanks handles list with no blanks")
        void filterBlanks_noBlanks_returnsAll() {
            assertThat(refs.filterBlanks(List.of("a", "b", "c"))).containsExactly("a", "b", "c");
        }
    }

    // ─────────────────────────────────────────────────
    // Constructor References
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Type 4: Constructor References")
    class ConstructorReferencesTest {

        private final MethodReferenceTypes.ConstructorReferences refs =
                new MethodReferenceTypes.ConstructorReferences();

        @Test
        @DisplayName("wrapInStringBuilders creates StringBuilders with correct initial content")
        void wrapInStringBuilders_createsWithContent() {
            List<StringBuilder> result = refs.wrapInStringBuilders(List.of("hello", "world"));
            assertThat(result).hasSize(2);
            assertThat(result.get(0).toString()).isEqualTo("hello");
            assertThat(result.get(1).toString()).isEqualTo("world");
        }

        @Test
        @DisplayName("wrapInStringBuilders creates independent instances")
        void wrapInStringBuilders_createsIndependentInstances() {
            List<StringBuilder> result = refs.wrapInStringBuilders(List.of("a", "b"));
            result.get(0).append("-modified");
            assertThat(result.get(1).toString()).isEqualTo("b"); // unchanged
        }

        @Test
        @DisplayName("createListFactory creates new empty ArrayList on each get()")
        void createListFactory_createsNewListEachCall() {
            Supplier<ArrayList<String>> factory = refs.createListFactory();
            ArrayList<String> list1 = factory.get();
            ArrayList<String> list2 = factory.get();
            assertThat(list1).isNotSameAs(list2);
            assertThat(list1).isEmpty();
            assertThat(list2).isEmpty();
        }

        @Test
        @DisplayName("createEmptyLists creates the right number of lists")
        void createEmptyLists_createsCorrectCount() {
            List<ArrayList<String>> buckets = refs.createEmptyLists(5);
            assertThat(buckets).hasSize(5);
            buckets.forEach(b -> assertThat(b).isEmpty());
        }

        @Test
        @DisplayName("createEmptyLists creates independent list instances")
        void createEmptyLists_createsIndependentInstances() {
            List<ArrayList<String>> buckets = refs.createEmptyLists(3);
            buckets.get(0).add("only-in-first");
            assertThat(buckets.get(1)).isEmpty();
            assertThat(buckets.get(2)).isEmpty();
        }

        @Test
        @DisplayName("toArray converts list to String array")
        void toArray_convertsListToArray() {
            String[] arr = refs.toArray(List.of("a", "b", "c"));
            assertThat(arr).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("toArray returns empty array for empty list")
        void toArray_emptyListReturnsEmptyArray() {
            String[] arr = refs.toArray(Collections.emptyList());
            assertThat(arr).isEmpty();
        }

        @Test
        @DisplayName("createPointFactory creates Points with correct coordinates")
        void createPointFactory_createsPointsCorrectly() {
            BiFunction<Double, Double, MethodReferenceTypes.ConstructorReferences.Point> factory =
                    refs.createPointFactory();
            var p = factory.apply(3.0, 4.0);
            assertThat(p.x()).isEqualTo(3.0);
            assertThat(p.y()).isEqualTo(4.0);
        }

        @Test
        @DisplayName("generateBuckets creates independent mutable lists")
        void generateBuckets_createsIndependentLists() {
            List<List<String>> buckets = refs.generateBuckets(4);
            assertThat(buckets).hasSize(4);
            buckets.get(0).add("item");
            assertThat(buckets.get(1)).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────
    // Combined Types Demo
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Combined Types Demo Tests")
    class CombinedTypesDemoTest {

        private final MethodReferenceTypes.CombinedTypesDemo demo =
                new MethodReferenceTypes.CombinedTypesDemo();

        @Test
        @DisplayName("processRawData trims and prepends prefix to valid entries")
        void processRawData_trimsAndPrependsPrefix() {
            List<String> raw = Arrays.asList("  10.99  ", null, "", "  25.50  ", "  ");
            List<String> result = demo.processRawData(raw, "Product");
            assertThat(result).containsExactly("Product: 10.99", "Product: 25.50");
        }

        @Test
        @DisplayName("processRawData returns empty for all-null/blank input")
        void processRawData_allNullOrBlank_returnsEmpty() {
            List<String> raw = Arrays.asList(null, "", "  ", null);
            assertThat(demo.processRawData(raw, "Prefix")).isEmpty();
        }

        @Test
        @DisplayName("sortProductsByPrice sorts in ascending price order")
        void sortProductsByPrice_sortsAscending() {
            List<MethodReferenceTypes.CombinedTypesDemo.Product> products = Arrays.asList(
                    new MethodReferenceTypes.CombinedTypesDemo.Product("C", 30.0),
                    new MethodReferenceTypes.CombinedTypesDemo.Product("A", 10.0),
                    new MethodReferenceTypes.CombinedTypesDemo.Product("B", 20.0)
            );
            List<MethodReferenceTypes.CombinedTypesDemo.Product> sorted = demo.sortProductsByPrice(products);
            assertThat(sorted).extracting(MethodReferenceTypes.CombinedTypesDemo.Product::name)
                    .containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("sortProductsByPrice filters null products")
        void sortProductsByPrice_filtersNulls() {
            List<MethodReferenceTypes.CombinedTypesDemo.Product> products = Arrays.asList(
                    new MethodReferenceTypes.CombinedTypesDemo.Product("A", 10.0),
                    null,
                    new MethodReferenceTypes.CombinedTypesDemo.Product("B", 5.0)
            );
            assertThat(demo.sortProductsByPrice(products)).hasSize(2);
        }
    }
}
