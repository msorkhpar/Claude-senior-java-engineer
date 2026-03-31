package com.github.msorkhpar.claudejavatutor.dryprinciple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DRY Definition and Purpose Tests")
class DryDefinitionTest {

    @Nested
    @DisplayName("Violation Example - Duplicated Validation")
    class ViolationExampleTest {

        private final DryDefinition.ViolationExample violation = new DryDefinition.ViolationExample();

        @Test
        @DisplayName("Should validate valid email in all methods identically")
        void testAllMethodsReturnSameResultForValidEmail() {
            String email = "user@example.com";
            assertThat(violation.validateEmail(email)).isTrue();
            assertThat(violation.validateEmailForRegistration(email)).isTrue();
            assertThat(violation.validateEmailForPasswordReset(email)).isTrue();
        }

        @Test
        @DisplayName("Should reject null email in all methods identically")
        void testAllMethodsRejectNull() {
            assertThat(violation.validateEmail(null)).isFalse();
            assertThat(violation.validateEmailForRegistration(null)).isFalse();
            assertThat(violation.validateEmailForPasswordReset(null)).isFalse();
        }

        @Test
        @DisplayName("Should reject blank email")
        void testRejectBlankEmail() {
            assertThat(violation.validateEmail("   ")).isFalse();
        }

        @Test
        @DisplayName("Should reject email without at sign")
        void testRejectEmailWithoutAtSign() {
            assertThat(violation.validateEmail("userexample.com")).isFalse();
        }

        @Test
        @DisplayName("Should reject email without dot")
        void testRejectEmailWithoutDot() {
            assertThat(violation.validateEmail("user@examplecom")).isFalse();
        }

        @Test
        @DisplayName("Duplicated formatting produces same output")
        void testDuplicatedFormattingIdentical() {
            String display = violation.formatUserForDisplay("Alice", 30);
            String report = violation.formatUserForReport("Alice", 30);
            assertThat(display).isEqualTo(report).isEqualTo("ALICE (Age: 30)");
        }
    }

    @Nested
    @DisplayName("DRY Refactored Example")
    class DryRefactoredTest {

        private final DryDefinition.DryRefactored dry = new DryDefinition.DryRefactored();

        @Test
        @DisplayName("All validation methods delegate to single source of truth")
        void testAllValidationMethodsConsistent() {
            String email = "user@domain.com";
            assertThat(dry.validateEmail(email)).isTrue();
            assertThat(dry.validateEmailForRegistration(email)).isTrue();
            assertThat(dry.validateEmailForPasswordReset(email)).isTrue();
        }

        @Test
        @DisplayName("Should reject null email via single validation path")
        void testRejectNullEmail() {
            assertThat(dry.validateEmail(null)).isFalse();
            assertThat(dry.validateEmailForRegistration(null)).isFalse();
        }

        @Test
        @DisplayName("Formatting methods delegate to single formatter")
        void testFormattingDelegation() {
            String base = dry.formatUser("Bob", 25);
            assertThat(dry.formatUserForDisplay("Bob", 25)).isEqualTo(base);
            assertThat(dry.formatUserForReport("Bob", 25)).isEqualTo(base);
            assertThat(base).isEqualTo("BOB (Age: 25)");
        }
    }

    @Nested
    @DisplayName("Knowledge Duplication - Magic Numbers")
    class KnowledgeDuplicationTest {

        @Test
        @DisplayName("Violation: magic number leads to fragile code")
        void testMagicNumberViolation() {
            var v = new DryDefinition.KnowledgeDuplication.MagicNumberViolation();
            assertThat(v.calculateShippingCost(10.0)).isEqualTo(1.5);
            assertThat(v.estimateDelivery(10.0)).isEqualTo(6.5);
        }

        @Test
        @DisplayName("Fixed: constants make intent clear")
        void testMagicNumberFixed() {
            var f = new DryDefinition.KnowledgeDuplication.MagicNumberFixed();
            assertThat(f.calculateShippingCost(10.0)).isEqualTo(1.5);
            assertThat(f.estimateDelivery(10.0)).isEqualTo(6.5);
        }

        @Test
        @DisplayName("Should handle zero weight")
        void testZeroWeight() {
            var f = new DryDefinition.KnowledgeDuplication.MagicNumberFixed();
            assertThat(f.calculateShippingCost(0.0)).isEqualTo(0.0);
            assertThat(f.estimateDelivery(0.0)).isEqualTo(5.0);
        }
    }

    @Nested
    @DisplayName("Template Method Pattern for DRY")
    class TemplateMethodTest {

        @Test
        @DisplayName("StringUpperCaseProcessor filters and transforms correctly")
        void testStringProcessor() {
            var processor = new DryDefinition.StringUpperCaseProcessor();
            List<String> input = Arrays.asList("hello", "", null, "  ", "world");

            List<String> result = processor.process(input);

            assertThat(result).containsExactly("HELLO", "WORLD");
        }

        @Test
        @DisplayName("StringUpperCaseProcessor handles empty list")
        void testStringProcessorEmptyList() {
            var processor = new DryDefinition.StringUpperCaseProcessor();

            List<String> result = processor.process(Collections.emptyList());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("StringUpperCaseProcessor rejects null input list")
        void testStringProcessorNullInput() {
            var processor = new DryDefinition.StringUpperCaseProcessor();

            assertThatNullPointerException()
                    .isThrownBy(() -> processor.process(null))
                    .withMessageContaining("items must not be null");
        }

        @Test
        @DisplayName("IntegerDoublerProcessor filters and transforms correctly")
        void testIntegerProcessor() {
            var processor = new DryDefinition.IntegerDoublerProcessor();
            List<Integer> input = Arrays.asList(3, -1, 0, null, 5);

            List<Integer> result = processor.process(input);

            assertThat(result).containsExactly(6, 10);
        }

        @Test
        @DisplayName("IntegerDoublerProcessor handles all-negative input")
        void testIntegerProcessorAllNegative() {
            var processor = new DryDefinition.IntegerDoublerProcessor();
            List<Integer> input = Arrays.asList(-1, -2, -3);

            List<Integer> result = processor.process(input);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("IntegerDoublerProcessor handles single valid element")
        void testIntegerProcessorSingleElement() {
            var processor = new DryDefinition.IntegerDoublerProcessor();
            List<Integer> input = Collections.singletonList(7);

            List<Integer> result = processor.process(input);

            assertThat(result).containsExactly(14);
        }
    }

    @Nested
    @DisplayName("Functional DRY with Higher-Order Functions")
    class FunctionalDryTest {

        private final DryDefinition.FunctionalDry functionalDry = new DryDefinition.FunctionalDry();

        @Test
        @DisplayName("filterAndTransform filters and maps correctly")
        void testFilterAndTransform() {
            List<String> input = Arrays.asList("hello", "hi", "world", "wow");

            List<Integer> result = functionalDry.filterAndTransform(
                    input,
                    s -> s.length() > 2,
                    String::length
            );

            assertThat(result).containsExactly(5, 5, 3);
        }

        @Test
        @DisplayName("filterAndTransform handles empty list")
        void testFilterAndTransformEmpty() {
            List<String> result = functionalDry.filterAndTransform(
                    Collections.<String>emptyList(),
                    s -> true,
                    Function.identity()
            );

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("filterAndTransform rejects null items")
        void testFilterAndTransformNullItems() {
            assertThatNullPointerException()
                    .isThrownBy(() -> functionalDry.filterAndTransform(null, s -> true, s -> s))
                    .withMessageContaining("items must not be null");
        }

        @Test
        @DisplayName("filterAndTransform rejects null filter")
        void testFilterAndTransformNullFilter() {
            assertThatNullPointerException()
                    .isThrownBy(() -> functionalDry.filterAndTransform(List.of("a"), null, s -> s))
                    .withMessageContaining("filter must not be null");
        }

        @Test
        @DisplayName("filterAndTransform rejects null transformer")
        void testFilterAndTransformNullTransformer() {
            assertThatNullPointerException()
                    .isThrownBy(() -> functionalDry.filterAndTransform(List.of("a"), s -> true, null))
                    .withMessageContaining("transformer must not be null");
        }

        @Test
        @DisplayName("reduceWith finds maximum")
        void testReduceWithMax() {
            List<Integer> input = Arrays.asList(3, 7, 2, 9, 1);

            var result = functionalDry.reduceWith(input, Integer::max);

            assertThat(result).isPresent().contains(9);
        }

        @Test
        @DisplayName("reduceWith on empty list returns empty optional")
        void testReduceWithEmptyList() {
            var result = functionalDry.reduceWith(Collections.<Integer>emptyList(), Integer::sum);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("reduceWith concatenates strings")
        void testReduceWithConcatenation() {
            List<String> input = Arrays.asList("a", "b", "c");

            var result = functionalDry.reduceWith(input, String::concat);

            assertThat(result).isPresent().contains("abc");
        }

        @Test
        @DisplayName("reduceWith rejects null items")
        void testReduceWithNullItems() {
            assertThatNullPointerException()
                    .isThrownBy(() -> functionalDry.reduceWith(null, Integer::sum))
                    .withMessageContaining("items must not be null");
        }

        @Test
        @DisplayName("reduceWith rejects null accumulator")
        void testReduceWithNullAccumulator() {
            assertThatNullPointerException()
                    .isThrownBy(() -> functionalDry.reduceWith(List.of(1), null))
                    .withMessageContaining("accumulator must not be null");
        }
    }
}
