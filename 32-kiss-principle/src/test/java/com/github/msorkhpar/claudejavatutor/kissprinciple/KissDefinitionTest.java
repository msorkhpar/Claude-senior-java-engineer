package com.github.msorkhpar.claudejavatutor.kissprinciple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("KISS Definition and Purpose Tests")
class KissDefinitionTest {

    @Nested
    @DisplayName("Simple vs Complex Validation")
    class SimpleVsComplexValidationTest {

        @Test
        @DisplayName("Simple validator should accept valid email")
        void testSimpleValidatorAcceptsValidEmail() {
            var validator = new KissDefinition.SimpleEmailValidator();

            assertThat(validator.isValid("user@example.com")).isTrue();
        }

        @Test
        @DisplayName("Simple validator should reject email without @")
        void testSimpleValidatorRejectsNoAt() {
            var validator = new KissDefinition.SimpleEmailValidator();

            assertThat(validator.isValid("userexample.com")).isFalse();
        }

        @Test
        @DisplayName("Simple validator should reject email without domain part")
        void testSimpleValidatorRejectsNoDomain() {
            var validator = new KissDefinition.SimpleEmailValidator();

            assertThat(validator.isValid("user@")).isFalse();
        }

        @Test
        @DisplayName("Simple validator should reject null input")
        void testSimpleValidatorRejectsNull() {
            var validator = new KissDefinition.SimpleEmailValidator();

            assertThat(validator.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("Simple validator should reject empty string")
        void testSimpleValidatorRejectsEmpty() {
            var validator = new KissDefinition.SimpleEmailValidator();

            assertThat(validator.isValid("")).isFalse();
        }

        @Test
        @DisplayName("Simple validator should reject email without local part")
        void testSimpleValidatorRejectsNoLocalPart() {
            var validator = new KissDefinition.SimpleEmailValidator();

            assertThat(validator.isValid("@example.com")).isFalse();
        }

        @Test
        @DisplayName("Over-engineered validator should also accept valid email")
        void testOverEngineeredValidatorAcceptsValidEmail() {
            var validator = new KissDefinition.OverEngineeredEmailValidator();

            assertThat(validator.isValid("user@example.com")).isTrue();
        }

        @Test
        @DisplayName("Over-engineered validator should also reject invalid email")
        void testOverEngineeredValidatorRejectsInvalid() {
            var validator = new KissDefinition.OverEngineeredEmailValidator();

            assertThat(validator.isValid("invalid")).isFalse();
        }

        @Test
        @DisplayName("Both validators should agree on basic inputs")
        void testBothValidatorsAgree() {
            var simple = new KissDefinition.SimpleEmailValidator();
            var complex = new KissDefinition.OverEngineeredEmailValidator();

            List<String> inputs = List.of("user@example.com", "invalid", "a@b.c", "@no.com", "no@");

            for (String input : inputs) {
                assertThat(simple.isValid(input))
                        .as("Both validators should agree on: %s", input)
                        .isEqualTo(complex.isValid(input));
            }
        }
    }

    @Nested
    @DisplayName("Simple vs Complex Data Transformation")
    class SimpleVsComplexTransformationTest {

        @Test
        @DisplayName("Simple approach should extract unique words sorted")
        void testSimpleUniqueWordsSorted() {
            var transformer = new KissDefinition.SimpleDataTransformer();

            List<String> result = transformer.getUniqueSortedWords("banana apple cherry apple banana");

            assertThat(result).containsExactly("apple", "banana", "cherry");
        }

        @Test
        @DisplayName("Simple approach should handle empty string")
        void testSimpleUniqueWordsEmpty() {
            var transformer = new KissDefinition.SimpleDataTransformer();

            List<String> result = transformer.getUniqueSortedWords("");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Simple approach should handle null input")
        void testSimpleUniqueWordsNull() {
            var transformer = new KissDefinition.SimpleDataTransformer();

            List<String> result = transformer.getUniqueSortedWords(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Simple approach should handle single word")
        void testSimpleUniqueWordsSingleWord() {
            var transformer = new KissDefinition.SimpleDataTransformer();

            List<String> result = transformer.getUniqueSortedWords("hello");

            assertThat(result).containsExactly("hello");
        }

        @Test
        @DisplayName("Over-engineered approach should produce same results")
        void testOverEngineeredProducesSameResults() {
            var simple = new KissDefinition.SimpleDataTransformer();
            var complex = new KissDefinition.OverEngineeredDataTransformer();

            String input = "banana apple cherry apple banana date";

            assertThat(simple.getUniqueSortedWords(input))
                    .isEqualTo(complex.getUniqueSortedWords(input));
        }
    }

    @Nested
    @DisplayName("Simple vs Complex Configuration")
    class SimpleVsComplexConfigurationTest {

        @Test
        @DisplayName("Simple config should store and retrieve values")
        void testSimpleConfigStoreAndRetrieve() {
            var config = new KissDefinition.SimpleConfig();

            config.set("host", "localhost");
            config.set("port", "8080");

            assertThat(config.get("host")).isEqualTo("localhost");
            assertThat(config.get("port")).isEqualTo("8080");
        }

        @Test
        @DisplayName("Simple config should return default for missing key")
        void testSimpleConfigDefaultForMissing() {
            var config = new KissDefinition.SimpleConfig();

            assertThat(config.getOrDefault("missing", "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("Simple config should return null for missing key without default")
        void testSimpleConfigNullForMissing() {
            var config = new KissDefinition.SimpleConfig();

            assertThat(config.get("missing")).isNull();
        }

        @Test
        @DisplayName("Simple config should overwrite existing value")
        void testSimpleConfigOverwrite() {
            var config = new KissDefinition.SimpleConfig();

            config.set("key", "value1");
            config.set("key", "value2");

            assertThat(config.get("key")).isEqualTo("value2");
        }

        @Test
        @DisplayName("Simple config should handle null key gracefully")
        void testSimpleConfigNullKey() {
            var config = new KissDefinition.SimpleConfig();

            assertThatThrownBy(() -> config.set(null, "value"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Simple vs Complex Retry Logic")
    class SimpleVsComplexRetryTest {

        @Test
        @DisplayName("Simple retry should succeed on first try")
        void testSimpleRetrySucceedsFirstTry() {
            var retry = new KissDefinition.SimpleRetry();
            var counter = new int[]{0};

            String result = retry.executeWithRetry(() -> {
                counter[0]++;
                return "success";
            }, 3);

            assertThat(result).isEqualTo("success");
            assertThat(counter[0]).isEqualTo(1);
        }

        @Test
        @DisplayName("Simple retry should retry on failure then succeed")
        void testSimpleRetryRetriesAndSucceeds() {
            var retry = new KissDefinition.SimpleRetry();
            var counter = new int[]{0};

            String result = retry.executeWithRetry(() -> {
                counter[0]++;
                if (counter[0] < 3) {
                    throw new RuntimeException("fail");
                }
                return "success";
            }, 3);

            assertThat(result).isEqualTo("success");
            assertThat(counter[0]).isEqualTo(3);
        }

        @Test
        @DisplayName("Simple retry should throw after exhausting retries")
        void testSimpleRetryExhaustsRetries() {
            var retry = new KissDefinition.SimpleRetry();

            assertThatThrownBy(() -> retry.executeWithRetry(() -> {
                throw new RuntimeException("always fails");
            }, 3)).isInstanceOf(RuntimeException.class)
                    .hasMessage("always fails");
        }

        @Test
        @DisplayName("Simple retry should throw on zero max attempts")
        void testSimpleRetryZeroAttempts() {
            var retry = new KissDefinition.SimpleRetry();

            assertThatThrownBy(() -> retry.executeWithRetry(() -> "value", 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
