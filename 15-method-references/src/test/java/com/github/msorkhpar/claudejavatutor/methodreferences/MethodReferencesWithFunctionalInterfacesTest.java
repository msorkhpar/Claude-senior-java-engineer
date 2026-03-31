package com.github.msorkhpar.claudejavatutor.methodreferences;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@DisplayName("4.3.3 - Method References with Functional Interfaces Tests")
class MethodReferencesWithFunctionalInterfacesTest {

    // ─────────────────────────────────────────────────
    // Consumer<T>
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Consumer Method References Tests")
    class ConsumerMethodRefsTest {

        @Test
        @DisplayName("processItems records all items and logs them")
        void processItems_recordsAllItems() {
            var consumer = new MethodReferencesWithFunctionalInterfaces.ConsumerMethodRefs();
            consumer.processItems(List.of("event1", "event2", "event3"));
            assertThat(consumer.getEventLog()).containsExactly("event1", "event2", "event3");
        }

        @Test
        @DisplayName("processItems handles empty list without error")
        void processItems_emptyList_noError() {
            var consumer = new MethodReferencesWithFunctionalInterfaces.ConsumerMethodRefs();
            assertThatCode(() -> consumer.processItems(Collections.emptyList())).doesNotThrowAnyException();
            assertThat(consumer.getEventLog()).isEmpty();
        }

        @Test
        @DisplayName("processItems accumulates events across multiple calls")
        void processItems_accumulatesAcrossMultipleCalls() {
            var consumer = new MethodReferencesWithFunctionalInterfaces.ConsumerMethodRefs();
            consumer.processItems(List.of("a", "b"));
            consumer.processItems(List.of("c", "d"));
            assertThat(consumer.getEventLog()).containsExactly("a", "b", "c", "d");
        }

        @Test
        @DisplayName("collectUpperCase returns uppercase strings via bound Consumer")
        void collectUpperCase_returnsUpperCaseStrings() {
            var consumer = new MethodReferencesWithFunctionalInterfaces.ConsumerMethodRefs();
            List<String> result = consumer.collectUpperCase(List.of("hello", "world", "java"));
            assertThat(result).containsExactly("HELLO", "WORLD", "JAVA");
        }

        @Test
        @DisplayName("collectUpperCase skips null elements")
        void collectUpperCase_skipsNullElements() {
            var consumer = new MethodReferencesWithFunctionalInterfaces.ConsumerMethodRefs();
            List<String> result = consumer.collectUpperCase(Arrays.asList("hello", null, "java", null));
            assertThat(result).containsExactly("HELLO", "JAVA");
        }

        @Test
        @DisplayName("logAll does not throw for any string input")
        void logAll_doesNotThrowForAnyInput() {
            var consumer = new MethodReferencesWithFunctionalInterfaces.ConsumerMethodRefs();
            assertThatCode(() -> consumer.logAll(List.of("msg1", "msg2"))).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("getEventLog returns unmodifiable view")
        void getEventLog_returnsUnmodifiableView() {
            var consumer = new MethodReferencesWithFunctionalInterfaces.ConsumerMethodRefs();
            consumer.processItems(List.of("x"));
            List<String> log = consumer.getEventLog();
            assertThatThrownBy(() -> log.add("tampering"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // ─────────────────────────────────────────────────
    // Supplier<T>
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Supplier Method References Tests")
    class SupplierMethodRefsTest {

        private final MethodReferencesWithFunctionalInterfaces.SupplierMethodRefs refs =
                new MethodReferencesWithFunctionalInterfaces.SupplierMethodRefs();

        @Test
        @DisplayName("upperCaseSupplier always returns uppercase of the captured string")
        void upperCaseSupplier_returnsUpperCase() {
            Supplier<String> sup = refs.upperCaseSupplier("java");
            assertThat(sup.get()).isEqualTo("JAVA");
            assertThat(sup.get()).isEqualTo("JAVA"); // deterministic
        }

        @Test
        @DisplayName("upperCaseSupplier for empty string returns empty string")
        void upperCaseSupplier_emptyString_returnsEmpty() {
            Supplier<String> sup = refs.upperCaseSupplier("");
            assertThat(sup.get()).isEqualTo("");
        }

        @Test
        @DisplayName("arrayListSupplier creates new empty ArrayList on each get()")
        void arrayListSupplier_createsNewListEachCall() {
            Supplier<ArrayList<String>> factory = refs.arrayListSupplier();
            ArrayList<String> list1 = factory.get();
            ArrayList<String> list2 = factory.get();
            assertThat(list1).isNotSameAs(list2);
            assertThat(list1).isEmpty();
        }

        @Test
        @DisplayName("emptyListSupplier returns an empty list")
        void emptyListSupplier_returnsEmptyList() {
            Supplier<List<String>> sup = refs.emptyListSupplier();
            assertThat(sup.get()).isEmpty();
        }

        @Test
        @DisplayName("getOrDefault returns value when non-null")
        void getOrDefault_returnsValueWhenNonNull() {
            String result = refs.getOrDefault("actual", () -> "default");
            assertThat(result).isEqualTo("actual");
        }

        @Test
        @DisplayName("getOrDefault invokes Supplier when value is null")
        void getOrDefault_invokesSupplierForNull() {
            String result = refs.getOrDefault(null, () -> "fallback");
            assertThat(result).isEqualTo("fallback");
        }

        @Test
        @DisplayName("createBuckets creates n new empty ArrayLists")
        void createBuckets_createsCorrectCount() {
            List<ArrayList<String>> buckets = refs.createBuckets(4);
            assertThat(buckets).hasSize(4);
            buckets.forEach(b -> assertThat(b).isEmpty());
        }

        @Test
        @DisplayName("createBuckets creates independent instances")
        void createBuckets_createsIndependentInstances() {
            List<ArrayList<String>> buckets = refs.createBuckets(3);
            buckets.get(0).add("only-in-first");
            assertThat(buckets.get(1)).isEmpty();
        }

        @Test
        @DisplayName("createBuckets with 0 returns empty list")
        void createBuckets_zeroCount_returnsEmptyList() {
            assertThat(refs.createBuckets(0)).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────
    // Function<T, R>
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Function Method References Tests")
    class FunctionMethodRefsTest {

        private final MethodReferencesWithFunctionalInterfaces.FunctionMethodRefs refs =
                new MethodReferencesWithFunctionalInterfaces.FunctionMethodRefs();

        @Test
        @DisplayName("lengthFunction returns correct string length")
        void lengthFunction_returnsCorrectLength() {
            Function<String, Integer> fn = refs.lengthFunction();
            assertThat(fn.apply("hello")).isEqualTo(5);
            assertThat(fn.apply("")).isEqualTo(0);
        }

        @Test
        @DisplayName("parseIntFunction parses valid integer strings")
        void parseIntFunction_parsesValidStrings() {
            Function<String, Integer> fn = refs.parseIntFunction();
            assertThat(fn.apply("42")).isEqualTo(42);
            assertThat(fn.apply("-7")).isEqualTo(-7);
        }

        @Test
        @DisplayName("parseIntFunction throws NumberFormatException for invalid input")
        void parseIntFunction_throwsForInvalidInput() {
            Function<String, Integer> fn = refs.parseIntFunction();
            assertThatThrownBy(() -> fn.apply("abc")).isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("stringBuilderFactory creates StringBuilder with correct content")
        void stringBuilderFactory_createsWithContent() {
            Function<String, StringBuilder> fn = refs.stringBuilderFactory();
            StringBuilder sb = fn.apply("hello");
            assertThat(sb.toString()).isEqualTo("hello");
        }

        @Test
        @DisplayName("stringBuilderFactory creates independent instances")
        void stringBuilderFactory_createsIndependentInstances() {
            Function<String, StringBuilder> fn = refs.stringBuilderFactory();
            StringBuilder sb1 = fn.apply("a");
            StringBuilder sb2 = fn.apply("b");
            assertThat(sb1).isNotSameAs(sb2);
        }

        @Test
        @DisplayName("trimAndGetLength trims then measures length")
        void trimAndGetLength_trimsAndMeasures() {
            List<Integer> result = refs.trimAndGetLength(List.of("  hello  ", " ab ", "test"));
            assertThat(result).containsExactly(5, 2, 4);
        }

        @Test
        @DisplayName("trimAndGetLength skips null elements")
        void trimAndGetLength_skipsNullElements() {
            List<Integer> result = refs.trimAndGetLength(Arrays.asList("  hi  ", null, " abc "));
            assertThat(result).containsExactly(2, 3);
        }

        @Test
        @DisplayName("transform applies Function to each element")
        void transform_appliesFunction() {
            List<Integer> result = refs.transform(List.of("a", "bb", "ccc"), String::length);
            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("transform accepts constructor reference as Function")
        void transform_acceptsConstructorReference() {
            List<StringBuilder> result = refs.transform(List.of("hello", "world"), StringBuilder::new);
            assertThat(result).extracting(StringBuilder::toString).containsExactly("hello", "world");
        }

        @Test
        @DisplayName("transform skips null elements")
        void transform_skipsNullElements() {
            List<Integer> result = refs.transform(Arrays.asList("a", null, "bbb"), String::length);
            assertThat(result).containsExactly(1, 3);
        }
    }

    // ─────────────────────────────────────────────────
    // Predicate<T>
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Predicate Method References Tests")
    class PredicateMethodRefsTest {

        private final MethodReferencesWithFunctionalInterfaces.PredicateMethodRefs refs =
                new MethodReferencesWithFunctionalInterfaces.PredicateMethodRefs();

        @Test
        @DisplayName("nonNullPredicate returns true for non-null and false for null")
        void nonNullPredicate_testsBothCases() {
            Predicate<Object> pred = refs.nonNullPredicate();
            assertThat(pred.test("hello")).isTrue();
            assertThat(pred.test(42)).isTrue();
            assertThat(pred.test(null)).isFalse();
        }

        @Test
        @DisplayName("isBlankPredicate returns true for blank strings")
        void isBlankPredicate_testsBlankStrings() {
            Predicate<String> pred = refs.isBlankPredicate();
            assertThat(pred.test("")).isTrue();
            assertThat(pred.test("   ")).isTrue();
            assertThat(pred.test("\t")).isTrue();
            assertThat(pred.test("hello")).isFalse();
        }

        @Test
        @DisplayName("isNotBlankPredicate is the negation of isBlankPredicate")
        void isNotBlankPredicate_isNegationOfIsBlank() {
            Predicate<String> blank = refs.isBlankPredicate();
            Predicate<String> notBlank = refs.isNotBlankPredicate();
            List<String> testCases = Arrays.asList("hello", "", "  ", "world", "\n");
            for (String s : testCases) {
                assertThat(notBlank.test(s)).isEqualTo(!blank.test(s));
            }
        }

        @Test
        @DisplayName("filterNonNullNonBlankShort keeps only qualifying strings")
        void filterNonNullNonBlankShort_filtersCorrectly() {
            List<String> input = Arrays.asList("hi", null, "hello", "  ", "ab", "toolongstring", "ok");
            List<String> result = refs.filterNonNullNonBlankShort(input, 5);
            // "hi"(2<=5 ok), null(skip), "hello"(5<=5 ok), "  "(blank skip),
            // "ab"(2<=5 ok), "toolongstring"(13>5 skip), "ok"(2<=5 ok)
            assertThat(result).containsExactly("hi", "hello", "ab", "ok");
        }

        @Test
        @DisplayName("filterNonNullNonBlankShort with maxLength 0 keeps only empty strings")
        void filterNonNullNonBlankShort_maxLength0_keepsOnlyEmpty() {
            // With maxLength 0: isShort = s.length() <= 0 — only empty string qualifies,
            // but isNotBlank filter removes it. So result should be empty.
            List<String> input = Arrays.asList("hi", "a", "", "  ");
            List<String> result = refs.filterNonNullNonBlankShort(input, 0);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("filterEmptyOrLong keeps empty strings and long strings")
        void filterEmptyOrLong_keepsEmptyAndLong() {
            List<String> input = Arrays.asList("hi", "", "helloworld", "ab", "superlongstring");
            // minLength = 10: keep "" (empty) and "helloworld"(10) and "superlongstring"(15)
            List<String> result = refs.filterEmptyOrLong(input, 10);
            assertThat(result).containsExactly("", "helloworld", "superlongstring");
        }

        @Test
        @DisplayName("filterEmptyOrLong skips null elements")
        void filterEmptyOrLong_skipsNullElements() {
            List<String> input = Arrays.asList("hi", null, "", "toolongstring");
            List<String> result = refs.filterEmptyOrLong(input, 10);
            assertThat(result).containsExactly("", "toolongstring");
        }
    }

    // ─────────────────────────────────────────────────
    // BiFunction<T, U, R>
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("BiFunction Method References Tests")
    class BiFunctionMethodRefsTest {

        private final MethodReferencesWithFunctionalInterfaces.BiFunctionMethodRefs refs =
                new MethodReferencesWithFunctionalInterfaces.BiFunctionMethodRefs();

        @Test
        @DisplayName("maxFunction returns the larger of two integers")
        void maxFunction_returnsLarger() {
            BiFunction<Integer, Integer, Integer> max = refs.maxFunction();
            assertThat(max.apply(3, 7)).isEqualTo(7);
            assertThat(max.apply(10, 2)).isEqualTo(10);
            assertThat(max.apply(5, 5)).isEqualTo(5);
        }

        @Test
        @DisplayName("maxFunction handles negative numbers")
        void maxFunction_handlesNegativeNumbers() {
            BiFunction<Integer, Integer, Integer> max = refs.maxFunction();
            assertThat(max.apply(-3, -7)).isEqualTo(-3);
            assertThat(max.apply(-1, 1)).isEqualTo(1);
        }

        @Test
        @DisplayName("concatFunction concatenates two strings")
        void concatFunction_concatenatesStrings() {
            BiFunction<String, String, String> concat = refs.concatFunction();
            assertThat(concat.apply("Hello, ", "World!")).isEqualTo("Hello, World!");
            assertThat(concat.apply("", "suffix")).isEqualTo("suffix");
            assertThat(concat.apply("prefix", "")).isEqualTo("prefix");
        }

        @Test
        @DisplayName("startsWithFunction checks prefix correctly")
        void startsWithFunction_checksPrefix() {
            BiFunction<String, String, Boolean> startsWith = refs.startsWithFunction();
            assertThat(startsWith.apply("Hello, World!", "Hello")).isTrue();
            assertThat(startsWith.apply("Hello, World!", "World")).isFalse();
            assertThat(startsWith.apply("Hello", "")).isTrue(); // empty prefix always matches
        }

        @Test
        @DisplayName("zipWith combines elements from two lists using BiFunction")
        void zipWith_combinesElements() {
            List<String> lefts = List.of("a", "b", "c");
            List<String> rights = List.of("1", "2", "3");
            List<String> result = refs.zipWith(lefts, rights, String::concat);
            assertThat(result).containsExactly("a1", "b2", "c3");
        }

        @Test
        @DisplayName("zipWith stops at the shorter list")
        void zipWith_stopsAtShorterList() {
            List<String> lefts = List.of("a", "b", "c", "d");
            List<String> rights = List.of("1", "2");
            List<String> result = refs.zipWith(lefts, rights, String::concat);
            assertThat(result).containsExactly("a1", "b2");
        }

        @Test
        @DisplayName("zipWith with empty list returns empty")
        void zipWith_emptyList_returnsEmpty() {
            assertThat(refs.zipWith(Collections.emptyList(), List.of("a"), String::concat)).isEmpty();
        }

        @Test
        @DisplayName("concatThenLength concatenates and measures total length")
        void concatThenLength_measuresConcatenatedLength() {
            BiFunction<String, String, Integer> fn = refs.concatThenLength();
            assertThat(fn.apply("Hello", "World")).isEqualTo(10); // "HelloWorld".length()
            assertThat(fn.apply("", "")).isEqualTo(0);
            assertThat(fn.apply("abc", "")).isEqualTo(3);
        }
    }

    // ─────────────────────────────────────────────────
    // Comparator
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Comparator Method References Tests")
    class ComparatorMethodRefsTest {

        private final MethodReferencesWithFunctionalInterfaces.ComparatorMethodRefs refs =
                new MethodReferencesWithFunctionalInterfaces.ComparatorMethodRefs();

        private MethodReferencesWithFunctionalInterfaces.ComparatorMethodRefs.Person person(
                String first, String last, int age) {
            return new MethodReferencesWithFunctionalInterfaces.ComparatorMethodRefs.Person(first, last, age);
        }

        @Test
        @DisplayName("sortAlphabetically sorts in natural alphabetical order")
        void sortAlphabetically_sortsNaturally() {
            List<String> words = Arrays.asList("cherry", "apple", "banana", "date");
            assertThat(refs.sortAlphabetically(words)).containsExactly("apple", "banana", "cherry", "date");
        }

        @Test
        @DisplayName("sortAlphabetically handles empty list")
        void sortAlphabetically_handlesEmptyList() {
            assertThat(refs.sortAlphabetically(Collections.emptyList())).isEmpty();
        }

        @Test
        @DisplayName("sortPeople sorts by lastName then firstName then age")
        void sortPeople_sortsByMultipleKeys() {
            List<MethodReferencesWithFunctionalInterfaces.ComparatorMethodRefs.Person> people = Arrays.asList(
                    person("Bob", "Smith", 30),
                    person("Alice", "Smith", 25),
                    person("Charlie", "Adams", 35),
                    person("Alice", "Smith", 20)
            );
            List<MethodReferencesWithFunctionalInterfaces.ComparatorMethodRefs.Person> sorted =
                    refs.sortPeople(people);
            // Adams Charlie, Smith Alice 20, Smith Alice 25, Smith Bob 30
            assertThat(sorted).extracting(p -> p.firstName() + " " + p.lastName())
                    .containsExactly("Charlie Adams", "Alice Smith", "Alice Smith", "Bob Smith");
            assertThat(sorted.get(1).age()).isEqualTo(20);
            assertThat(sorted.get(2).age()).isEqualTo(25);
        }

        @Test
        @DisplayName("sortPeople filters null persons")
        void sortPeople_filtersNullPersons() {
            List<MethodReferencesWithFunctionalInterfaces.ComparatorMethodRefs.Person> people =
                    Arrays.asList(person("Alice", "A", 25), null, person("Bob", "B", 30));
            assertThat(refs.sortPeople(people)).hasSize(2);
        }

        @Test
        @DisplayName("sortByLengthDescending sorts strings longest first")
        void sortByLengthDescending_sortsLongestFirst() {
            List<String> words = Arrays.asList("hi", "hello", "a", "world", "Java");
            List<String> sorted = refs.sortByLengthDescending(words);
            // "hello"(5), "world"(5), "Java"(4), "hi"(2), "a"(1)
            assertThat(sorted.get(0)).hasSize(5);
            assertThat(sorted.get(4)).hasSize(1);
        }

        @Test
        @DisplayName("caseInsensitiveComparator treats case differences as equal")
        void caseInsensitiveComparator_ignoresCase() {
            Comparator<String> cmp = refs.caseInsensitiveComparator();
            assertThat(cmp.compare("abc", "ABC")).isEqualTo(0);
            assertThat(cmp.compare("apple", "BANANA")).isLessThan(0);
            assertThat(cmp.compare("ZEBRA", "apple")).isGreaterThan(0);
        }
    }

    // ─────────────────────────────────────────────────
    // Optional
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Optional Method References Tests")
    class OptionalMethodRefsTest {

        private final MethodReferencesWithFunctionalInterfaces.OptionalMethodRefs refs =
                new MethodReferencesWithFunctionalInterfaces.OptionalMethodRefs();

        @Test
        @DisplayName("getLength returns length of present string")
        void getLength_returnsPresentLength() {
            assertThat(refs.getLength(Optional.of("hello"))).contains(5);
        }

        @Test
        @DisplayName("getLength returns empty for empty Optional")
        void getLength_returnsEmptyForEmptyOptional() {
            assertThat(refs.getLength(Optional.empty())).isEmpty();
        }

        @Test
        @DisplayName("getLength returns 0 for empty string")
        void getLength_returnsZeroForEmptyString() {
            assertThat(refs.getLength(Optional.of(""))).contains(0);
        }

        @Test
        @DisplayName("filterBlank returns empty Optional for blank string")
        void filterBlank_returnsEmptyForBlank() {
            assertThat(refs.filterBlank(Optional.of("   "))).isEmpty();
            assertThat(refs.filterBlank(Optional.of(""))).isEmpty();
        }

        @Test
        @DisplayName("filterBlank passes through non-blank string")
        void filterBlank_passesThroughNonBlank() {
            assertThat(refs.filterBlank(Optional.of("hello"))).contains("hello");
        }

        @Test
        @DisplayName("filterBlank handles empty Optional")
        void filterBlank_handlesEmptyOptional() {
            assertThat(refs.filterBlank(Optional.empty())).isEmpty();
        }

        @Test
        @DisplayName("toUpperCase transforms present value to uppercase")
        void toUpperCase_transformsToUpperCase() {
            assertThat(refs.toUpperCase(Optional.of("hello"))).contains("HELLO");
        }

        @Test
        @DisplayName("toUpperCase returns empty for empty Optional")
        void toUpperCase_returnsEmptyForEmpty() {
            assertThat(refs.toUpperCase(Optional.empty())).isEmpty();
        }

        @Test
        @DisplayName("printIfPresent does not throw for present or empty Optional")
        void printIfPresent_doesNotThrow() {
            assertThatCode(() -> refs.printIfPresent(Optional.of("hello"))).doesNotThrowAnyException();
            assertThatCode(() -> refs.printIfPresent(Optional.empty())).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("trimAndGetLength trims then returns length for non-blank")
        void trimAndGetLength_trimsAndReturnsLength() {
            assertThat(refs.trimAndGetLength(Optional.of("  hello  "))).contains(5);
        }

        @Test
        @DisplayName("trimAndGetLength returns empty for blank string")
        void trimAndGetLength_returnsEmptyForBlankAfterTrim() {
            assertThat(refs.trimAndGetLength(Optional.of("    "))).isEmpty();
        }

        @Test
        @DisplayName("trimAndGetLength returns empty for empty Optional")
        void trimAndGetLength_returnsEmptyForEmptyOptional() {
            assertThat(refs.trimAndGetLength(Optional.empty())).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────
    // Complete Pipeline
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Complete Pipeline Tests")
    class CompletePipelineTest {

        private final MethodReferencesWithFunctionalInterfaces.CompletePipeline pipeline =
                new MethodReferencesWithFunctionalInterfaces.CompletePipeline();

        private MethodReferencesWithFunctionalInterfaces.CompletePipeline.Employee employee(
                String name, String dept, double salary, boolean active) {
            return new MethodReferencesWithFunctionalInterfaces.CompletePipeline.Employee(
                    name, dept, salary, active);
        }

        @Test
        @DisplayName("getActiveEmployeeNames returns sorted names of active employees")
        void getActiveEmployeeNames_returnsSortedActiveNames() {
            List<MethodReferencesWithFunctionalInterfaces.CompletePipeline.Employee> employees =
                    Arrays.asList(
                            employee("Charlie", "Engineering", 90000, true),
                            employee("Alice", "Marketing", 75000, true),
                            employee("Bob", "Engineering", 80000, false),  // inactive
                            employee("Diana", "Marketing", 85000, true)
                    );
            assertThat(pipeline.getActiveEmployeeNames(employees))
                    .containsExactly("Alice", "Charlie", "Diana");
        }

        @Test
        @DisplayName("getActiveEmployeeNames filters null employees")
        void getActiveEmployeeNames_filtersNullEmployees() {
            List<MethodReferencesWithFunctionalInterfaces.CompletePipeline.Employee> employees =
                    Arrays.asList(
                            employee("Alice", "Dept", 80000, true),
                            null,
                            employee("Bob", "Dept", 70000, true)
                    );
            assertThat(pipeline.getActiveEmployeeNames(employees)).containsExactly("Alice", "Bob");
        }

        @Test
        @DisplayName("getActiveEmployeeNames returns empty when no active employees")
        void getActiveEmployeeNames_returnsEmptyWhenNoneActive() {
            List<MethodReferencesWithFunctionalInterfaces.CompletePipeline.Employee> employees =
                    List.of(employee("Alice", "Dept", 80000, false));
            assertThat(pipeline.getActiveEmployeeNames(employees)).isEmpty();
        }

        @Test
        @DisplayName("groupByDepartment groups employees correctly")
        void groupByDepartment_groupsCorrectly() {
            List<MethodReferencesWithFunctionalInterfaces.CompletePipeline.Employee> employees =
                    Arrays.asList(
                            employee("Alice", "Engineering", 90000, true),
                            employee("Bob", "Marketing", 75000, true),
                            employee("Charlie", "Engineering", 80000, true)
                    );
            Map<String, List<MethodReferencesWithFunctionalInterfaces.CompletePipeline.Employee>> result =
                    pipeline.groupByDepartment(employees);
            assertThat(result).containsKeys("Engineering", "Marketing");
            assertThat(result.get("Engineering")).hasSize(2);
            assertThat(result.get("Marketing")).hasSize(1);
        }

        @Test
        @DisplayName("averageSalaryByDepartment calculates averages for active employees")
        void averageSalaryByDepartment_calculatesAverages() {
            List<MethodReferencesWithFunctionalInterfaces.CompletePipeline.Employee> employees =
                    Arrays.asList(
                            employee("Alice", "Engineering", 90000, true),
                            employee("Bob", "Engineering", 80000, true),
                            employee("Charlie", "Marketing", 70000, true),
                            employee("Diana", "Engineering", 100000, false) // inactive, excluded
                    );
            Map<String, Double> avgs = pipeline.averageSalaryByDepartment(employees);
            assertThat(avgs.get("Engineering")).isEqualTo(85000.0);
            assertThat(avgs.get("Marketing")).isEqualTo(70000.0);
            assertThat(avgs).doesNotContainKey("Diana");
        }

        @Test
        @DisplayName("toArray converts employee list to typed array")
        void toArray_convertsToTypedArray() {
            List<MethodReferencesWithFunctionalInterfaces.CompletePipeline.Employee> employees =
                    List.of(
                            employee("Alice", "Eng", 90000, true),
                            employee("Bob", "Mkt", 75000, true)
                    );
            MethodReferencesWithFunctionalInterfaces.CompletePipeline.Employee[] arr =
                    pipeline.toArray(employees);
            assertThat(arr).hasSize(2);
            assertThat(arr[0].name()).isEqualTo("Alice");
            assertThat(arr[1].name()).isEqualTo("Bob");
        }

        @Test
        @DisplayName("toArray filters null employees")
        void toArray_filtersNullEmployees() {
            List<MethodReferencesWithFunctionalInterfaces.CompletePipeline.Employee> employees =
                    Arrays.asList(
                            employee("Alice", "Eng", 90000, true),
                            null
                    );
            assertThat(pipeline.toArray(employees)).hasSize(1);
        }

        @Test
        @DisplayName("toArray returns empty array for empty list")
        void toArray_emptyList_returnsEmptyArray() {
            assertThat(pipeline.toArray(Collections.emptyList())).isEmpty();
        }
    }
}
