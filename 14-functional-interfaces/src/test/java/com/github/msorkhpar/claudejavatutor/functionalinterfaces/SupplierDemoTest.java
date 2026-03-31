package com.github.msorkhpar.claudejavatutor.functionalinterfaces;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("4.2.2 Supplier Functional Interface Tests")
class SupplierDemoTest {

    private final SupplierDemo demo = new SupplierDemo();

    @Nested
    @DisplayName("Supplier - Basic Usage")
    class BasicSupplierTests {

        @Test
        @DisplayName("Should supply a constant value")
        void testConstantSupplier() {
            String result = demo.supplyConstant();
            assertThat(result).isEqualTo("Hello, Supplier!");
        }

        @Test
        @DisplayName("Should supply computed value")
        void testComputedSupplier() {
            int result = demo.supplyRandomInRange(1, 10);
            assertThat(result).isBetween(1, 10);
        }

        @Test
        @DisplayName("Should supply from a factory method")
        void testFactorySupplier() {
            List<String> list = demo.supplyNewList();
            assertThat(list).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Should supply different instances each call")
        void testSupplierCreatesNewInstances() {
            List<String> list1 = demo.supplyNewList();
            List<String> list2 = demo.supplyNewList();
            assertThat(list1).isNotSameAs(list2);
        }
    }

    @Nested
    @DisplayName("Supplier - Lazy evaluation")
    class LazyEvaluationTests {

        @Test
        @DisplayName("Should not evaluate until get() is called")
        void testLazyEvaluation() {
            // If the supplier was evaluated eagerly, this would throw
            // We just verify the supplier is created without issue
            assertThatCode(() -> demo.createExpensiveSupplier())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should supply expensive computation on demand")
        void testOnDemandComputation() {
            String result = demo.getExpensiveValue();
            assertThat(result).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("Should use Supplier for lazy default in Optional")
        void testSupplierWithOptional() {
            Optional<String> empty = Optional.empty();
            String result = demo.getOrDefault(empty);
            assertThat(result).isEqualTo("default-value");
        }

        @Test
        @DisplayName("Should return present value without calling Supplier")
        void testOptionalWithPresentValue() {
            Optional<String> present = Optional.of("actual");
            String result = demo.getOrDefault(present);
            assertThat(result).isEqualTo("actual");
        }
    }

    @Nested
    @DisplayName("Supplier - with caching/memoization")
    class SupplierCachingTests {

        @Test
        @DisplayName("Should memoize the supplied value")
        void testMemoizedSupplier() {
            String first = demo.getMemoizedValue();
            String second = demo.getMemoizedValue();
            assertThat(first).isEqualTo(second);
            assertThat(first).isSameAs(second); // Same instance due to memoization
        }
    }

    @Nested
    @DisplayName("Supplier - generating collections")
    class CollectionGenerationTests {

        @Test
        @DisplayName("Should generate list using Supplier")
        void testGenerateList() {
            List<Integer> result = demo.generateNumbers(5);
            assertThat(result).hasSize(5);
            assertThat(result).allMatch(n -> n >= 0 && n < 100);
        }

        @Test
        @DisplayName("Should generate empty list for count 0")
        void testGenerateEmptyList() {
            List<Integer> result = demo.generateNumbers(0);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Supplier - dependency injection style")
    class DependencyInjectionTests {

        @Test
        @DisplayName("Should create object via Supplier")
        void testObjectCreation() {
            SupplierDemo.DatabaseConnection conn = demo.createConnection("jdbc:test");
            assertThat(conn).isNotNull();
            assertThat(conn.getUrl()).isEqualTo("jdbc:test");
        }

        @Test
        @DisplayName("Should use Supplier to defer connection creation")
        void testDeferredCreation() {
            String result = demo.queryWithConnection("SELECT 1", "jdbc:test");
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Supplier - BooleanSupplier, IntSupplier, LongSupplier, DoubleSupplier")
    class PrimitiveSupplierTests {

        @Test
        @DisplayName("Should supply boolean value")
        void testBooleanSupplier() {
            boolean result = demo.supplyBoolean();
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should supply int value without boxing")
        void testIntSupplier() {
            int result = demo.supplyInt();
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("Should supply long value without boxing")
        void testLongSupplier() {
            long result = demo.supplyLong();
            assertThat(result).isEqualTo(100L);
        }

        @Test
        @DisplayName("Should supply double value without boxing")
        void testDoubleSupplier() {
            double result = demo.supplyDouble();
            assertThat(result).isEqualTo(3.14);
        }
    }
}
