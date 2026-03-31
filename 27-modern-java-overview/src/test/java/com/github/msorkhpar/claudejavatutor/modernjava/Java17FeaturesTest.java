package com.github.msorkhpar.claudejavatutor.modernjava;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Java 17 Features Tests")
class Java17FeaturesTest {

    @Nested
    @DisplayName("Enhanced Pseudo-Random Number Generators")
    class RandomGeneratorTests {

        @Test
        @DisplayName("Should generate random int within bounds")
        void testGenerateRandomInt() {
            RandomGenerator generator = RandomGeneratorFactory.of("L64X128MixRandom").create(42L);
            int result = Java17Features.generateRandomInt(generator, 100);
            assertThat(result).isBetween(0, 99);
        }

        @Test
        @DisplayName("Should create generator by algorithm name")
        void testCreateGenerator() {
            RandomGenerator generator = Java17Features.createGenerator("L64X128MixRandom");
            assertThat(generator).isNotNull();
            assertThat(generator.nextInt()).isNotNull();
        }

        @Test
        @DisplayName("Should create seeded generator for reproducibility")
        void testSeededGenerator() {
            RandomGenerator gen1 = Java17Features.createSeededGenerator("L64X128MixRandom", 12345L);
            RandomGenerator gen2 = Java17Features.createSeededGenerator("L64X128MixRandom", 12345L);
            // Same seed should produce same sequence
            assertThat(gen1.nextInt(100)).isEqualTo(gen2.nextInt(100));
        }

        @Test
        @DisplayName("Should list available algorithms")
        void testListAlgorithms() {
            List<String> algorithms = Java17Features.listAvailableAlgorithms();
            assertThat(algorithms).isNotEmpty();
            assertThat(algorithms).contains("L64X128MixRandom");
        }

        @Test
        @DisplayName("Should generate specified count of random ints in range")
        void testGenerateRandomInts() {
            RandomGenerator generator = RandomGeneratorFactory.of("L64X128MixRandom").create(42L);
            List<Integer> result = Java17Features.generateRandomInts(generator, 10, 0, 50);
            assertThat(result).hasSize(10);
            assertThat(result).allMatch(n -> n >= 0 && n < 50);
        }

        @Test
        @DisplayName("Should generate random doubles in range")
        void testGenerateRandomDoubles() {
            RandomGenerator generator = RandomGeneratorFactory.of("L64X128MixRandom").create(42L);
            List<Double> result = Java17Features.generateRandomDoubles(generator, 5, 0.0, 1.0);
            assertThat(result).hasSize(5);
            assertThat(result).allMatch(d -> d >= 0.0 && d < 1.0);
        }

        @Test
        @DisplayName("Should generate random longs in range")
        void testGenerateRandomLongs() {
            RandomGenerator generator = RandomGeneratorFactory.of("L64X128MixRandom").create(42L);
            List<Long> result = Java17Features.generateRandomLongs(generator, 5, 100L, 200L);
            assertThat(result).hasSize(5);
            assertThat(result).allMatch(l -> l >= 100L && l < 200L);
        }

        @Test
        @DisplayName("Should generate Gaussian values centered around zero")
        void testGenerateGaussianValues() {
            RandomGenerator generator = RandomGeneratorFactory.of("L64X128MixRandom").create(42L);
            List<Double> values = Java17Features.generateGaussianValues(generator, 1000);
            assertThat(values).hasSize(1000);
            double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            assertThat(mean).isBetween(-0.5, 0.5); // Should be close to 0
        }

        @Test
        @DisplayName("Should generate with jumpable generator")
        void testGenerateWithJumpable() {
            List<Integer> values = Java17Features.generateWithJumpable(42L, 3, 5);
            assertThat(values).hasSize(15); // 3 streams x 5 values
            assertThat(values).allMatch(n -> n >= 0 && n < 100);
        }

        @Test
        @DisplayName("Should get generator properties")
        void testGetGeneratorProperties() {
            Map<String, Object> props = Java17Features.getGeneratorProperties("L64X128MixRandom");
            assertThat(props).containsKey("name");
            assertThat(props.get("name")).isEqualTo("L64X128MixRandom");
            assertThat(props).containsKeys("isJumpable", "isSplittable", "isStreamable");
        }

        @Test
        @DisplayName("Should produce old-style random numbers")
        void testOldStyleRandom() {
            List<Integer> result = Java17Features.oldStyleRandom(42L, 10);
            assertThat(result).hasSize(10);
            assertThat(result).allMatch(n -> n >= 0 && n < 100);
        }

        @Test
        @DisplayName("Should produce new-style random numbers")
        void testNewStyleRandom() {
            List<Integer> result = Java17Features.newStyleRandom(42L, 10);
            assertThat(result).hasSize(10);
            assertThat(result).allMatch(n -> n >= 0 && n < 100);
        }

        @Test
        @DisplayName("Old style random should be reproducible with same seed")
        void testOldStyleReproducible() {
            List<Integer> first = Java17Features.oldStyleRandom(42L, 5);
            List<Integer> second = Java17Features.oldStyleRandom(42L, 5);
            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("New style random should be reproducible with same seed")
        void testNewStyleReproducible() {
            List<Integer> first = Java17Features.newStyleRandom(42L, 5);
            List<Integer> second = Java17Features.newStyleRandom(42L, 5);
            assertThat(first).isEqualTo(second);
        }
    }

    @Nested
    @DisplayName("Security Manager Deprecation")
    class SecurityManagerTests {

        @Test
        @DisplayName("Should report no security manager present")
        @SuppressWarnings("removal")
        void testNoSecurityManager() {
            assertThat(Java17Features.isSecurityManagerPresent()).isFalse();
        }

        @Test
        @DisplayName("Should explain deprecation")
        void testExplainDeprecation() {
            String explanation = Java17Features.explainSecurityManagerDeprecation();
            assertThat(explanation).contains("deprecated");
            assertThat(explanation).contains("Container-level isolation");
            assertThat(explanation).contains("Module system");
        }

        @Test
        @DisplayName("Should describe module system benefits")
        void testModuleSystemBenefits() {
            String benefits = Java17Features.demonstrateModuleSystemBenefits();
            assertThat(benefits).contains("Strong encapsulation");
            assertThat(benefits).contains("Explicit dependencies");
        }
    }

    @Nested
    @DisplayName("Sealed Classes (finalized in Java 17)")
    class SealedClassesTests {

        @Test
        @DisplayName("Should describe credit card")
        void testDescribeCreditCard() {
            Java17Features.PaymentMethod cc =
                    new Java17Features.CreditCard("4111111111111111", "12/25");
            assertThat(Java17Features.describePaymentMethod(cc))
                    .isEqualTo("Credit Card ending in 1111");
        }

        @Test
        @DisplayName("Should describe debit card")
        void testDescribeDebitCard() {
            Java17Features.PaymentMethod dc =
                    new Java17Features.DebitCard("5555444433332222", "1234");
            assertThat(Java17Features.describePaymentMethod(dc))
                    .isEqualTo("Debit Card ending in 2222");
        }

        @Test
        @DisplayName("Should describe digital wallet")
        void testDescribeDigitalWallet() {
            Java17Features.PaymentMethod dw =
                    new Java17Features.DigitalWallet("PayPal", "user@example.com");
            assertThat(Java17Features.describePaymentMethod(dw))
                    .isEqualTo("Digital Wallet: PayPal (user@example.com)");
        }
    }
}
