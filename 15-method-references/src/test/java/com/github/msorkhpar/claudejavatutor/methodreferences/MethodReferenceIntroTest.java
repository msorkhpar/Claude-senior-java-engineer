package com.github.msorkhpar.claudejavatutor.methodreferences;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("4.3.1 - Method Reference Introduction Tests")
class MethodReferenceIntroTest {

    // ─────────────────────────────────────────────────
    // LambdaToMethodRefEvolution
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Lambda to Method Reference Evolution")
    class LambdaToMethodRefEvolutionTest {

        private final MethodReferenceIntro.LambdaToMethodRefEvolution evolution =
                new MethodReferenceIntro.LambdaToMethodRefEvolution();

        @Test
        @DisplayName("Anonymous inner class sorts alphabetically")
        void sortAnonymous_sortsAlphabetically() {
            List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
            assertThat(evolution.sortAnonymous(names)).containsExactly("Alice", "Bob", "Charlie");
        }

        @Test
        @DisplayName("Lambda sort produces same result as anonymous sort")
        void sortLambda_matchesAnonymousResult() {
            List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
            assertThat(evolution.sortLambda(names)).isEqualTo(evolution.sortAnonymous(names));
        }

        @Test
        @DisplayName("Method reference sort produces same result as lambda sort")
        void sortMethodReference_matchesLambdaResult() {
            List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
            assertThat(evolution.sortMethodReference(names)).isEqualTo(evolution.sortLambda(names));
        }

        @Test
        @DisplayName("All three approaches produce identical results")
        void allProduceSameResult_isTrue() {
            List<String> names = Arrays.asList("Zebra", "Mango", "Apple", "Kiwi");
            assertThat(evolution.allProduceSameResult(names)).isTrue();
        }

        @Test
        @DisplayName("Sorting does not modify the original list")
        void sort_doesNotModifyOriginalList() {
            List<String> original = new ArrayList<>(Arrays.asList("Charlie", "Alice", "Bob"));
            List<String> copy = new ArrayList<>(original);

            evolution.sortMethodReference(original);

            assertThat(original).isEqualTo(copy);
        }

        @Test
        @DisplayName("Empty list returns empty list for all approaches")
        void sort_emptyList_returnsEmpty() {
            List<String> empty = new ArrayList<>();
            assertThat(evolution.sortAnonymous(empty)).isEmpty();
            assertThat(evolution.sortLambda(empty)).isEmpty();
            assertThat(evolution.sortMethodReference(empty)).isEmpty();
        }

        @Test
        @DisplayName("Single-element list is returned unchanged")
        void sort_singleElement_returnsSameElement() {
            List<String> single = Arrays.asList("OnlyOne");
            assertThat(evolution.sortMethodReference(single)).containsExactly("OnlyOne");
        }

        @Test
        @DisplayName("Duplicate strings are preserved after sort")
        void sort_duplicates_preservedInOrder() {
            List<String> withDupes = Arrays.asList("b", "a", "b", "a");
            assertThat(evolution.sortMethodReference(withDupes)).containsExactly("a", "a", "b", "b");
        }
    }

    // ─────────────────────────────────────────────────
    // LambdaEquivalence
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Lambda Equivalence Tests")
    class LambdaEquivalenceTest {

        private final MethodReferenceIntro.LambdaEquivalence equiv =
                new MethodReferenceIntro.LambdaEquivalence();

        @Test
        @DisplayName("lengthViaLambda returns correct length")
        void lengthViaLambda_returnsCorrectLength() {
            Function<String, Integer> fn = equiv.lengthViaLambda();
            assertThat(fn.apply("hello")).isEqualTo(5);
            assertThat(fn.apply("")).isEqualTo(0);
            assertThat(fn.apply("Java")).isEqualTo(4);
        }

        @Test
        @DisplayName("lengthViaMethodRef returns correct length")
        void lengthViaMethodRef_returnsCorrectLength() {
            Function<String, Integer> fn = equiv.lengthViaMethodRef();
            assertThat(fn.apply("hello")).isEqualTo(5);
            assertThat(fn.apply("")).isEqualTo(0);
            assertThat(fn.apply("Java")).isEqualTo(4);
        }

        @Test
        @DisplayName("Lambda and method reference produce identical length results")
        void lengthFunctions_produceSameResults() {
            Function<String, Integer> lambda = equiv.lengthViaLambda();
            Function<String, Integer> ref = equiv.lengthViaMethodRef();
            List<String> words = List.of("test", "method", "reference", "");
            for (String w : words) {
                assertThat(lambda.apply(w)).isEqualTo(ref.apply(w));
            }
        }

        @Test
        @DisplayName("nonNullViaLambda correctly tests null and non-null")
        void nonNullViaLambda_testsBothCases() {
            Predicate<Object> pred = equiv.nonNullViaLambda();
            assertThat(pred.test("hello")).isTrue();
            assertThat(pred.test(null)).isFalse();
        }

        @Test
        @DisplayName("nonNullViaMethodRef correctly tests null and non-null")
        void nonNullViaMethodRef_testsBothCases() {
            Predicate<Object> pred = equiv.nonNullViaMethodRef();
            assertThat(pred.test("hello")).isTrue();
            assertThat(pred.test(null)).isFalse();
            assertThat(pred.test(42)).isTrue();
        }

        @Test
        @DisplayName("Lambda and method reference Predicates give same results")
        void predicates_produceSameResults() {
            Predicate<Object> lambda = equiv.nonNullViaLambda();
            Predicate<Object> ref = equiv.nonNullViaMethodRef();
            assertThat(lambda.test("test")).isEqualTo(ref.test("test"));
            assertThat(lambda.test(null)).isEqualTo(ref.test(null));
        }

        @Test
        @DisplayName("listFactoryViaLambda creates a new ArrayList each call")
        void listFactoryViaLambda_createsNewListEachCall() {
            Supplier<ArrayList<String>> factory = equiv.listFactoryViaLambda();
            ArrayList<String> list1 = factory.get();
            ArrayList<String> list2 = factory.get();
            assertThat(list1).isNotSameAs(list2);
            assertThat(list1).isEmpty();
        }

        @Test
        @DisplayName("listFactoryViaConstructorRef creates a new ArrayList each call")
        void listFactoryViaConstructorRef_createsNewListEachCall() {
            Supplier<ArrayList<String>> factory = equiv.listFactoryViaConstructorRef();
            ArrayList<String> list1 = factory.get();
            ArrayList<String> list2 = factory.get();
            assertThat(list1).isNotSameAs(list2);
            assertThat(list1).isEmpty();
        }

        @Test
        @DisplayName("Lambda and constructor reference produce equivalent lists")
        void listFactories_produceSameSizeEmptyLists() {
            Supplier<ArrayList<String>> lambda = equiv.listFactoryViaLambda();
            Supplier<ArrayList<String>> ctor = equiv.listFactoryViaConstructorRef();
            assertThat(lambda.get()).isEmpty();
            assertThat(ctor.get()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────
    // NullSafeMethodRefUsage
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Null Safe Method Reference Usage Tests")
    class NullSafeMethodRefUsageTest {

        private final MethodReferenceIntro.NullSafeMethodRefUsage nullSafe =
                new MethodReferenceIntro.NullSafeMethodRefUsage();

        @Test
        @DisplayName("toUpperCaseSafe converts valid strings")
        void toUpperCaseSafe_convertsValidStrings() {
            assertThat(nullSafe.toUpperCaseSafe(List.of("hello", "world")))
                    .containsExactly("HELLO", "WORLD");
        }

        @Test
        @DisplayName("toUpperCaseSafe skips null elements")
        void toUpperCaseSafe_skipsNullElements() {
            List<String> withNulls = Arrays.asList("hello", null, "world", null);
            assertThat(nullSafe.toUpperCaseSafe(withNulls)).containsExactly("HELLO", "WORLD");
        }

        @Test
        @DisplayName("toUpperCaseSafe returns empty for null input")
        void toUpperCaseSafe_returnsEmptyForNullInput() {
            assertThat(nullSafe.toUpperCaseSafe(null)).isEmpty();
        }

        @Test
        @DisplayName("toUpperCaseSafe handles empty list")
        void toUpperCaseSafe_handlesEmptyList() {
            assertThat(nullSafe.toUpperCaseSafe(Collections.emptyList())).isEmpty();
        }

        @Test
        @DisplayName("getLengthsNonBlank returns lengths for valid strings")
        void getLengthsNonBlank_returnsLengthsForValidStrings() {
            assertThat(nullSafe.getLengthsNonBlank(List.of("hello", "ab", "test")))
                    .containsExactly(5, 2, 4);
        }

        @Test
        @DisplayName("getLengthsNonBlank skips blank strings")
        void getLengthsNonBlank_skipsBlankStrings() {
            List<String> withBlanks = Arrays.asList("hello", "", "  ", "world", "\t");
            assertThat(nullSafe.getLengthsNonBlank(withBlanks)).containsExactly(5, 5);
        }

        @Test
        @DisplayName("getLengthsNonBlank skips null elements")
        void getLengthsNonBlank_skipsNullElements() {
            List<String> withNulls = Arrays.asList("hello", null, "world");
            assertThat(nullSafe.getLengthsNonBlank(withNulls)).containsExactly(5, 5);
        }

        @Test
        @DisplayName("getLengthsNonBlank returns empty for null input")
        void getLengthsNonBlank_returnsEmptyForNullInput() {
            assertThat(nullSafe.getLengthsNonBlank(null)).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────
    // ExceptionHandlingWithMethodRefs
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Exception Handling with Method References Tests")
    class ExceptionHandlingWithMethodRefsTest {

        private final MethodReferenceIntro.ExceptionHandlingWithMethodRefs handler =
                new MethodReferenceIntro.ExceptionHandlingWithMethodRefs();

        @Test
        @DisplayName("parseAllValid parses a clean list of integers")
        void parseAllValid_parsesCleanList() {
            assertThat(handler.parseAllValid(List.of("1", "2", "3", "100")))
                    .containsExactly(1, 2, 3, 100);
        }

        @Test
        @DisplayName("parseAllValid throws NumberFormatException for invalid input")
        void parseAllValid_throwsForInvalidInput() {
            assertThatThrownBy(() -> handler.parseAllValid(List.of("1", "abc", "3")))
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("parseSafely returns valid integers and skips invalid ones")
        void parseSafely_skipsInvalidValues() {
            assertThat(handler.parseSafely(List.of("1", "abc", "3", "xyz", "100")))
                    .containsExactly(1, 3, 100);
        }

        @Test
        @DisplayName("parseSafely returns empty list for all invalid input")
        void parseSafely_returnsEmptyForAllInvalid() {
            assertThat(handler.parseSafely(List.of("abc", "def", "ghi"))).isEmpty();
        }

        @Test
        @DisplayName("parseSafely handles empty list")
        void parseSafely_handlesEmptyList() {
            assertThat(handler.parseSafely(Collections.emptyList())).isEmpty();
        }

        @Test
        @DisplayName("tryParseInt returns null for invalid input")
        void tryParseInt_returnsNullForInvalid() {
            assertThat(MethodReferenceIntro.ExceptionHandlingWithMethodRefs.tryParseInt("abc")).isNull();
            assertThat(MethodReferenceIntro.ExceptionHandlingWithMethodRefs.tryParseInt("")).isNull();
        }

        @Test
        @DisplayName("tryParseInt returns parsed integer for valid input")
        void tryParseInt_returnsIntegerForValid() {
            assertThat(MethodReferenceIntro.ExceptionHandlingWithMethodRefs.tryParseInt("42")).isEqualTo(42);
            assertThat(MethodReferenceIntro.ExceptionHandlingWithMethodRefs.tryParseInt("-7")).isEqualTo(-7);
            assertThat(MethodReferenceIntro.ExceptionHandlingWithMethodRefs.tryParseInt("0")).isEqualTo(0);
        }
    }

    // ─────────────────────────────────────────────────
    // ReadabilityDemo
    // ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Readability Demo Tests")
    class ReadabilityDemoTest {

        private final MethodReferenceIntro.ReadabilityDemo demo =
                new MethodReferenceIntro.ReadabilityDemo();

        private MethodReferenceIntro.ReadabilityDemo.Person person(String first, String last, int age, boolean active) {
            return new MethodReferenceIntro.ReadabilityDemo.Person(first, last, age, active);
        }

        @Test
        @DisplayName("getActiveAdultFullNames returns sorted names of active adults")
        void getActiveAdultFullNames_returnsSortedActiveAdults() {
            List<MethodReferenceIntro.ReadabilityDemo.Person> people = Arrays.asList(
                    person("Alice", "Smith", 25, true),
                    person("Bob", "Jones", 17, true),     // minor — excluded
                    person("Charlie", "Adams", 30, false), // inactive — excluded
                    person("Diana", "Brown", 22, true),
                    person("Eve", "Adams", 28, true)
            );
            List<String> result = demo.getActiveAdultFullNames(people);
            // Sorted by last name then first name: Adams Eve, Brown Diana, Smith Alice
            assertThat(result).containsExactly("Eve Adams", "Diana Brown", "Alice Smith");
        }

        @Test
        @DisplayName("getActiveAdultFullNames returns empty for no qualifying persons")
        void getActiveAdultFullNames_returnsEmptyWhenNoQualify() {
            List<MethodReferenceIntro.ReadabilityDemo.Person> people = Arrays.asList(
                    person("Minor", "X", 15, true),
                    person("Inactive", "Y", 30, false)
            );
            assertThat(demo.getActiveAdultFullNames(people)).isEmpty();
        }

        @Test
        @DisplayName("getActiveAdultFullNames handles null elements in list")
        void getActiveAdultFullNames_handlesNullElements() {
            List<MethodReferenceIntro.ReadabilityDemo.Person> people = Arrays.asList(
                    person("Alice", "Smith", 25, true),
                    null,
                    person("Bob", "Jones", 30, true)
            );
            assertThat(demo.getActiveAdultFullNames(people)).containsExactly("Bob Jones", "Alice Smith");
        }

        @Test
        @DisplayName("partitionByActive separates active from inactive persons")
        void partitionByActive_separatesCorrectly() {
            List<MethodReferenceIntro.ReadabilityDemo.Person> people = Arrays.asList(
                    person("Alice", "A", 25, true),
                    person("Bob", "B", 30, false),
                    person("Charlie", "C", 22, true)
            );
            var result = demo.partitionByActive(people);
            assertThat(result.get(true)).hasSize(2);
            assertThat(result.get(false)).hasSize(1);
        }

        @Test
        @DisplayName("partitionByActive handles empty list")
        void partitionByActive_handlesEmptyList() {
            var result = demo.partitionByActive(Collections.emptyList());
            assertThat(result.get(true)).isEmpty();
            assertThat(result.get(false)).isEmpty();
        }

        @Test
        @DisplayName("partitionByActive handles all-active list")
        void partitionByActive_handlesAllActive() {
            List<MethodReferenceIntro.ReadabilityDemo.Person> people = Arrays.asList(
                    person("A", "A", 20, true),
                    person("B", "B", 21, true)
            );
            var result = demo.partitionByActive(people);
            assertThat(result.get(true)).hasSize(2);
            assertThat(result.get(false)).isEmpty();
        }
    }
}
