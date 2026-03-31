package com.github.msorkhpar.claudejavatutor.behavioralpatterns;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Strategy Pattern Tests")
class StrategyPatternTest {

    // ===================== Classic Strategy Tests =====================

    @Nested
    @DisplayName("BubbleSortStrategy")
    class BubbleSortStrategyTest {

        @Test
        @DisplayName("Should sort integers in ascending order")
        void testSortIntegers() {
            var strategy = new StrategyPattern.BubbleSortStrategy<Integer>();
            List<Integer> data = Arrays.asList(5, 3, 8, 1, 9, 2);

            List<Integer> result = strategy.sort(data);

            assertThat(result).containsExactly(1, 2, 3, 5, 8, 9);
        }

        @Test
        @DisplayName("Should sort strings alphabetically")
        void testSortStrings() {
            var strategy = new StrategyPattern.BubbleSortStrategy<String>();
            List<String> data = Arrays.asList("Charlie", "Alice", "Bob");

            List<String> result = strategy.sort(data);

            assertThat(result).containsExactly("Alice", "Bob", "Charlie");
        }

        @Test
        @DisplayName("Should handle empty list")
        void testEmptyList() {
            var strategy = new StrategyPattern.BubbleSortStrategy<Integer>();

            List<Integer> result = strategy.sort(Collections.emptyList());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle single element")
        void testSingleElement() {
            var strategy = new StrategyPattern.BubbleSortStrategy<Integer>();

            List<Integer> result = strategy.sort(List.of(42));

            assertThat(result).containsExactly(42);
        }

        @Test
        @DisplayName("Should handle already sorted list")
        void testAlreadySorted() {
            var strategy = new StrategyPattern.BubbleSortStrategy<Integer>();

            List<Integer> result = strategy.sort(Arrays.asList(1, 2, 3, 4, 5));

            assertThat(result).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should handle list with duplicates")
        void testDuplicates() {
            var strategy = new StrategyPattern.BubbleSortStrategy<Integer>();

            List<Integer> result = strategy.sort(Arrays.asList(3, 1, 3, 2, 1));

            assertThat(result).containsExactly(1, 1, 2, 3, 3);
        }

        @Test
        @DisplayName("Should throw on null input")
        void testNullInput() {
            var strategy = new StrategyPattern.BubbleSortStrategy<Integer>();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> strategy.sort(null))
                    .withMessageContaining("null");
        }

        @Test
        @DisplayName("Should not modify original list")
        void testOriginalListNotModified() {
            var strategy = new StrategyPattern.BubbleSortStrategy<Integer>();
            List<Integer> original = new ArrayList<>(Arrays.asList(3, 1, 2));

            strategy.sort(original);

            assertThat(original).containsExactly(3, 1, 2);
        }

        @Test
        @DisplayName("Should return correct strategy name")
        void testStrategyName() {
            var strategy = new StrategyPattern.BubbleSortStrategy<Integer>();

            assertThat(strategy.name()).isEqualTo("BubbleSort");
        }
    }

    @Nested
    @DisplayName("SelectionSortStrategy")
    class SelectionSortStrategyTest {

        @Test
        @DisplayName("Should sort integers in ascending order")
        void testSortIntegers() {
            var strategy = new StrategyPattern.SelectionSortStrategy<Integer>();

            List<Integer> result = strategy.sort(Arrays.asList(5, 3, 8, 1, 9, 2));

            assertThat(result).containsExactly(1, 2, 3, 5, 8, 9);
        }

        @Test
        @DisplayName("Should handle empty list")
        void testEmptyList() {
            var strategy = new StrategyPattern.SelectionSortStrategy<Integer>();

            assertThat(strategy.sort(Collections.emptyList())).isEmpty();
        }

        @Test
        @DisplayName("Should throw on null input")
        void testNullInput() {
            var strategy = new StrategyPattern.SelectionSortStrategy<Integer>();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> strategy.sort(null));
        }

        @Test
        @DisplayName("Should return correct strategy name")
        void testStrategyName() {
            assertThat(new StrategyPattern.SelectionSortStrategy<Integer>().name()).isEqualTo("SelectionSort");
        }
    }

    @Nested
    @DisplayName("JavaSortStrategy")
    class JavaSortStrategyTest {

        @Test
        @DisplayName("Should sort using Java built-in sort")
        void testSort() {
            var strategy = new StrategyPattern.JavaSortStrategy<Integer>();

            List<Integer> result = strategy.sort(Arrays.asList(5, 3, 8, 1));

            assertThat(result).containsExactly(1, 3, 5, 8);
        }

        @Test
        @DisplayName("Should throw on null input")
        void testNullInput() {
            var strategy = new StrategyPattern.JavaSortStrategy<Integer>();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> strategy.sort(null));
        }
    }

    @Nested
    @DisplayName("Sorter Context")
    class SorterTest {

        @Test
        @DisplayName("Should sort using the configured strategy")
        void testSortWithStrategy() {
            var sorter = new StrategyPattern.Sorter<>(new StrategyPattern.BubbleSortStrategy<Integer>());

            List<Integer> result = sorter.sort(Arrays.asList(3, 1, 2));

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should switch strategies at runtime")
        void testSwitchStrategy() {
            var sorter = new StrategyPattern.Sorter<>(new StrategyPattern.BubbleSortStrategy<Integer>());
            assertThat(sorter.getStrategy().name()).isEqualTo("BubbleSort");

            sorter.setStrategy(new StrategyPattern.JavaSortStrategy<>());
            assertThat(sorter.getStrategy().name()).isEqualTo("JavaSort");

            List<Integer> result = sorter.sort(Arrays.asList(3, 1, 2));
            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("All strategies produce the same sorted result")
        void testAllStrategiesProduceSameResult() {
            List<Integer> data = Arrays.asList(5, 2, 8, 1, 9, 3);

            List<Integer> bubble = new StrategyPattern.BubbleSortStrategy<Integer>().sort(data);
            List<Integer> selection = new StrategyPattern.SelectionSortStrategy<Integer>().sort(data);
            List<Integer> javaSrt = new StrategyPattern.JavaSortStrategy<Integer>().sort(data);

            assertThat(bubble).isEqualTo(selection).isEqualTo(javaSrt);
        }

        @Test
        @DisplayName("Should throw on null strategy in constructor")
        void testNullStrategyConstructor() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new StrategyPattern.Sorter<Integer>(null));
        }

        @Test
        @DisplayName("Should throw on null strategy in setter")
        void testNullStrategySetter() {
            var sorter = new StrategyPattern.Sorter<>(new StrategyPattern.BubbleSortStrategy<Integer>());

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> sorter.setStrategy(null));
        }
    }

    // ===================== Lambda-based Strategy Tests =====================

    @Nested
    @DisplayName("TextProcessor (Lambda Strategy)")
    class TextProcessorTest {

        @Test
        @DisplayName("Should apply upper case strategy")
        void testUpperCase() {
            var processor = new StrategyPattern.TextProcessor(StrategyPattern.TextProcessor.UPPER_CASE);

            assertThat(processor.process("hello world")).isEqualTo("HELLO WORLD");
        }

        @Test
        @DisplayName("Should apply lower case strategy")
        void testLowerCase() {
            var processor = new StrategyPattern.TextProcessor(StrategyPattern.TextProcessor.LOWER_CASE);

            assertThat(processor.process("HELLO WORLD")).isEqualTo("hello world");
        }

        @Test
        @DisplayName("Should apply reverse strategy")
        void testReverse() {
            var processor = new StrategyPattern.TextProcessor(StrategyPattern.TextProcessor.REVERSE);

            assertThat(processor.process("hello")).isEqualTo("olleh");
        }

        @Test
        @DisplayName("Should apply trim and upper strategy")
        void testTrimAndUpper() {
            var processor = new StrategyPattern.TextProcessor(StrategyPattern.TextProcessor.TRIM_AND_UPPER);

            assertThat(processor.process("  hello  ")).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("Should apply remove whitespace strategy")
        void testRemoveWhitespace() {
            var processor = new StrategyPattern.TextProcessor(StrategyPattern.TextProcessor.REMOVE_WHITESPACE);

            assertThat(processor.process("h e l l o")).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should switch lambda strategy at runtime")
        void testSwitchLambdaStrategy() {
            var processor = new StrategyPattern.TextProcessor(StrategyPattern.TextProcessor.UPPER_CASE);
            assertThat(processor.process("test")).isEqualTo("TEST");

            processor.setStrategy(StrategyPattern.TextProcessor.REVERSE);
            assertThat(processor.process("test")).isEqualTo("tset");
        }

        @Test
        @DisplayName("Should accept custom lambda strategy")
        void testCustomLambdaStrategy() {
            UnaryOperator<String> addExclamation = s -> s + "!";
            var processor = new StrategyPattern.TextProcessor(addExclamation);

            assertThat(processor.process("hello")).isEqualTo("hello!");
        }

        @Test
        @DisplayName("Should handle empty string")
        void testEmptyString() {
            var processor = new StrategyPattern.TextProcessor(StrategyPattern.TextProcessor.UPPER_CASE);

            assertThat(processor.process("")).isEqualTo("");
        }

        @Test
        @DisplayName("Should throw on null text")
        void testNullText() {
            var processor = new StrategyPattern.TextProcessor(StrategyPattern.TextProcessor.UPPER_CASE);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> processor.process(null));
        }

        @Test
        @DisplayName("Should throw on null strategy in constructor")
        void testNullStrategyConstructor() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new StrategyPattern.TextProcessor(null));
        }

        @Test
        @DisplayName("Should throw on null strategy in setter")
        void testNullStrategySetter() {
            var processor = new StrategyPattern.TextProcessor(StrategyPattern.TextProcessor.UPPER_CASE);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> processor.setStrategy(null));
        }
    }

    // ===================== Sealed Interface Strategy Tests =====================

    @Nested
    @DisplayName("PaymentProcessor (Sealed Strategy)")
    class PaymentProcessorTest {

        @Test
        @DisplayName("Should process credit card payment")
        void testCreditCardPayment() {
            var payment = new StrategyPattern.CreditCardPayment("4111111111111111", "12/25");
            var processor = new StrategyPattern.PaymentProcessor(payment);

            var result = processor.processPayment(100.0);

            assertThat(result.success()).isTrue();
            assertThat(result.transactionId()).startsWith("CC-");
            assertThat(result.message()).contains("credit card").contains("1111");
        }

        @Test
        @DisplayName("Should process PayPal payment")
        void testPayPalPayment() {
            var payment = new StrategyPattern.PayPalPayment("user@example.com");
            var processor = new StrategyPattern.PaymentProcessor(payment);

            var result = processor.processPayment(50.0);

            assertThat(result.success()).isTrue();
            assertThat(result.transactionId()).startsWith("PP-");
            assertThat(result.message()).contains("PayPal").contains("user@example.com");
        }

        @Test
        @DisplayName("Should process crypto payment")
        void testCryptoPayment() {
            var payment = new StrategyPattern.CryptoPayment("0xABC123");
            var processor = new StrategyPattern.PaymentProcessor(payment);

            var result = processor.processPayment(200.0);

            assertThat(result.success()).isTrue();
            assertThat(result.transactionId()).startsWith("CRYPTO-");
            assertThat(result.message()).contains("crypto wallet");
        }

        @Test
        @DisplayName("Should fail on zero amount")
        void testZeroAmount() {
            var payment = new StrategyPattern.CreditCardPayment("4111111111111111", "12/25");
            var processor = new StrategyPattern.PaymentProcessor(payment);

            var result = processor.processPayment(0);

            assertThat(result.success()).isFalse();
            assertThat(result.transactionId()).isNull();
        }

        @Test
        @DisplayName("Should fail on negative amount")
        void testNegativeAmount() {
            var payment = new StrategyPattern.PayPalPayment("user@example.com");
            var processor = new StrategyPattern.PaymentProcessor(payment);

            var result = processor.processPayment(-10.0);

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("Should describe credit card strategy via pattern matching")
        void testDescribeCreditCard() {
            var processor = new StrategyPattern.PaymentProcessor(
                    new StrategyPattern.CreditCardPayment("4111111111111111", "12/25"));

            assertThat(processor.describeStrategy()).contains("Credit card").contains("1111");
        }

        @Test
        @DisplayName("Should describe PayPal strategy via pattern matching")
        void testDescribePayPal() {
            var processor = new StrategyPattern.PaymentProcessor(
                    new StrategyPattern.PayPalPayment("user@example.com"));

            assertThat(processor.describeStrategy()).contains("PayPal").contains("user@example.com");
        }

        @Test
        @DisplayName("Should describe crypto strategy via pattern matching")
        void testDescribeCrypto() {
            var processor = new StrategyPattern.PaymentProcessor(
                    new StrategyPattern.CryptoPayment("0xABC123"));

            assertThat(processor.describeStrategy()).contains("Crypto").contains("0xABC123");
        }

        @Test
        @DisplayName("Should switch payment strategy")
        void testSwitchPaymentStrategy() {
            var processor = new StrategyPattern.PaymentProcessor(
                    new StrategyPattern.CreditCardPayment("4111111111111111", "12/25"));
            assertThat(processor.describeStrategy()).contains("Credit card");

            processor.setStrategy(new StrategyPattern.PayPalPayment("test@test.com"));
            assertThat(processor.describeStrategy()).contains("PayPal");
        }

        @Test
        @DisplayName("Should throw on blank card number")
        void testBlankCardNumber() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new StrategyPattern.CreditCardPayment("", "12/25"));
        }

        @Test
        @DisplayName("Should throw on null email for PayPal")
        void testNullPayPalEmail() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new StrategyPattern.PayPalPayment(null));
        }

        @Test
        @DisplayName("Should throw on blank wallet address for crypto")
        void testBlankCryptoWallet() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new StrategyPattern.CryptoPayment("  "));
        }

        @Test
        @DisplayName("Should throw on null strategy in constructor")
        void testNullPaymentStrategy() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new StrategyPattern.PaymentProcessor(null));
        }
    }

    // ===================== DataPipeline (Composition) Tests =====================

    @Nested
    @DisplayName("DataPipeline (Composable Strategies)")
    class DataPipelineTest {

        @Test
        @DisplayName("Should execute pipeline with multiple stages")
        void testMultipleStages() {
            var pipeline = new StrategyPattern.DataPipeline<Integer>();
            pipeline.addStage(list -> list.stream().filter(n -> n > 2).toList())
                    .addStage(list -> list.stream().sorted().toList());

            List<Integer> result = pipeline.execute(Arrays.asList(5, 1, 3, 2, 4));

            assertThat(result).containsExactly(3, 4, 5);
        }

        @Test
        @DisplayName("Should handle empty pipeline")
        void testEmptyPipeline() {
            var pipeline = new StrategyPattern.DataPipeline<String>();

            List<String> result = pipeline.execute(List.of("a", "b"));

            assertThat(result).containsExactly("a", "b");
        }

        @Test
        @DisplayName("Should count stages")
        void testStageCount() {
            var pipeline = new StrategyPattern.DataPipeline<Integer>();
            assertThat(pipeline.stageCount()).isEqualTo(0);

            pipeline.addStage(list -> list);
            assertThat(pipeline.stageCount()).isEqualTo(1);

            pipeline.addStage(list -> list);
            assertThat(pipeline.stageCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should throw on null stage")
        void testNullStage() {
            var pipeline = new StrategyPattern.DataPipeline<Integer>();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> pipeline.addStage(null));
        }

        @Test
        @DisplayName("Should throw on null input")
        void testNullInput() {
            var pipeline = new StrategyPattern.DataPipeline<Integer>();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> pipeline.execute(null));
        }

        @Test
        @DisplayName("Should handle empty input list")
        void testEmptyInput() {
            var pipeline = new StrategyPattern.DataPipeline<Integer>();
            pipeline.addStage(list -> list.stream().filter(n -> n > 0).toList());

            List<Integer> result = pipeline.execute(Collections.emptyList());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should support method chaining")
        void testMethodChaining() {
            var pipeline = new StrategyPattern.DataPipeline<String>()
                    .addStage(list -> list.stream().map(String::toUpperCase).toList())
                    .addStage(list -> list.stream().sorted().toList());

            List<String> result = pipeline.execute(Arrays.asList("banana", "apple"));

            assertThat(result).containsExactly("APPLE", "BANANA");
        }
    }
}
