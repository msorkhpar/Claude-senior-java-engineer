package com.github.msorkhpar.claudejavatutor.enhancedenums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Enum Use Cases Tests")
class EnumUseCasesTest {

    @Nested
    @DisplayName("OrderState - State Machine")
    class OrderStateTest {

        @Test
        @DisplayName("Should allow valid transitions from CREATED")
        void testCreatedTransitions() {
            assertThat(EnumUseCases.OrderState.CREATED.canTransitionTo(EnumUseCases.OrderState.PENDING_PAYMENT))
                    .isTrue();
            assertThat(EnumUseCases.OrderState.CREATED.canTransitionTo(EnumUseCases.OrderState.CANCELLED))
                    .isTrue();
        }

        @Test
        @DisplayName("Should reject invalid transitions")
        void testInvalidTransition() {
            assertThat(EnumUseCases.OrderState.CREATED.canTransitionTo(EnumUseCases.OrderState.DELIVERED))
                    .isFalse();
        }

        @Test
        @DisplayName("Should throw on invalid transition attempt")
        void testTransitionToThrows() {
            assertThatThrownBy(() ->
                    EnumUseCases.OrderState.CREATED.transitionTo(EnumUseCases.OrderState.DELIVERED))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot transition");
        }

        @Test
        @DisplayName("Should perform valid transition")
        void testValidTransition() {
            EnumUseCases.OrderState next =
                    EnumUseCases.OrderState.CREATED.transitionTo(EnumUseCases.OrderState.PENDING_PAYMENT);
            assertThat(next).isEqualTo(EnumUseCases.OrderState.PENDING_PAYMENT);
        }

        @Test
        @DisplayName("Terminal states should have no valid transitions")
        void testTerminalStates() {
            assertThat(EnumUseCases.OrderState.CANCELLED.isTerminal()).isTrue();
            assertThat(EnumUseCases.OrderState.REFUNDED.isTerminal()).isTrue();
            assertThat(EnumUseCases.OrderState.CREATED.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("Should walk full happy path")
        void testHappyPath() {
            EnumUseCases.OrderState state = EnumUseCases.OrderState.CREATED;
            state = state.transitionTo(EnumUseCases.OrderState.PENDING_PAYMENT);
            state = state.transitionTo(EnumUseCases.OrderState.PAID);
            state = state.transitionTo(EnumUseCases.OrderState.PROCESSING);
            state = state.transitionTo(EnumUseCases.OrderState.SHIPPED);
            state = state.transitionTo(EnumUseCases.OrderState.DELIVERED);

            assertThat(state).isEqualTo(EnumUseCases.OrderState.DELIVERED);
        }

        @Test
        @DisplayName("Should support return/refund path")
        void testReturnPath() {
            EnumUseCases.OrderState state = EnumUseCases.OrderState.DELIVERED;
            state = state.transitionTo(EnumUseCases.OrderState.RETURNED);
            state = state.transitionTo(EnumUseCases.OrderState.REFUNDED);

            assertThat(state).isEqualTo(EnumUseCases.OrderState.REFUNDED);
            assertThat(state.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("Cancelled state should not transition anywhere")
        void testCancelledIsTerminal() {
            assertThat(EnumUseCases.OrderState.CANCELLED.validTransitions()).isEmpty();
            assertThatThrownBy(() ->
                    EnumUseCases.OrderState.CANCELLED.transitionTo(EnumUseCases.OrderState.CREATED))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("DiscountStrategy - Strategy Pattern")
    class DiscountStrategyTest {

        @Test
        @DisplayName("NONE should return original amount")
        void testNoDiscount() {
            assertThat(EnumUseCases.DiscountStrategy.NONE.applyDiscount(100.0)).isEqualTo(100.0);
        }

        @Test
        @DisplayName("PERCENTAGE_10 should apply 10% off")
        void testPercentage10() {
            assertThat(EnumUseCases.DiscountStrategy.PERCENTAGE_10.applyDiscount(100.0)).isEqualTo(90.0);
        }

        @Test
        @DisplayName("PERCENTAGE_20 should apply 20% off")
        void testPercentage20() {
            assertThat(EnumUseCases.DiscountStrategy.PERCENTAGE_20.applyDiscount(100.0)).isEqualTo(80.0);
        }

        @Test
        @DisplayName("FLAT_5 should subtract 5")
        void testFlat5() {
            assertThat(EnumUseCases.DiscountStrategy.FLAT_5.applyDiscount(100.0)).isEqualTo(95.0);
        }

        @Test
        @DisplayName("FLAT_10 should not go below zero")
        void testFlat10Floor() {
            assertThat(EnumUseCases.DiscountStrategy.FLAT_10.applyDiscount(5.0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should throw on negative amount")
        void testNegativeAmount() {
            assertThatThrownBy(() -> EnumUseCases.DiscountStrategy.NONE.applyDiscount(-10.0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should find best discount for given amount")
        void testBestDiscount() {
            EnumUseCases.DiscountStrategy best = EnumUseCases.DiscountStrategy.bestDiscount(100.0);
            // For $100, BUY_ONE_GET_HALF gives $75 which is the biggest savings
            assertThat(best).isEqualTo(EnumUseCases.DiscountStrategy.BUY_ONE_GET_HALF);
        }

        @Test
        @DisplayName("Should find best discount for small amount")
        void testBestDiscountSmallAmount() {
            // For $3, flat $10 off gives $0 which is the best discount
            EnumUseCases.DiscountStrategy best = EnumUseCases.DiscountStrategy.bestDiscount(3.0);
            assertThat(best.applyDiscount(3.0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should have description")
        void testDescription() {
            assertThat(EnumUseCases.DiscountStrategy.PERCENTAGE_10.description()).isEqualTo("10% Off");
        }

        @Test
        @DisplayName("Should handle zero amount")
        void testZeroAmount() {
            assertThat(EnumUseCases.DiscountStrategy.PERCENTAGE_10.applyDiscount(0.0)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("ValidationRule - Validation")
    class ValidationRuleTest {

        @Test
        @DisplayName("NOT_NULL should reject null")
        void testNotNull() {
            assertThat(EnumUseCases.ValidationRule.NOT_NULL.isValid(null)).isFalse();
            assertThat(EnumUseCases.ValidationRule.NOT_NULL.isValid("hello")).isTrue();
        }

        @Test
        @DisplayName("NOT_EMPTY should reject empty string")
        void testNotEmpty() {
            assertThat(EnumUseCases.ValidationRule.NOT_EMPTY.isValid("")).isFalse();
            assertThat(EnumUseCases.ValidationRule.NOT_EMPTY.isValid("hello")).isTrue();
            assertThat(EnumUseCases.ValidationRule.NOT_EMPTY.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("NOT_BLANK should reject blank string")
        void testNotBlank() {
            assertThat(EnumUseCases.ValidationRule.NOT_BLANK.isValid("   ")).isFalse();
            assertThat(EnumUseCases.ValidationRule.NOT_BLANK.isValid("hello")).isTrue();
        }

        @Test
        @DisplayName("POSITIVE_NUMBER should validate positive numbers")
        void testPositiveNumber() {
            assertThat(EnumUseCases.ValidationRule.POSITIVE_NUMBER.isValid(5)).isTrue();
            assertThat(EnumUseCases.ValidationRule.POSITIVE_NUMBER.isValid(0)).isFalse();
            assertThat(EnumUseCases.ValidationRule.POSITIVE_NUMBER.isValid(-1)).isFalse();
        }

        @Test
        @DisplayName("NON_NEGATIVE_NUMBER should accept zero")
        void testNonNegative() {
            assertThat(EnumUseCases.ValidationRule.NON_NEGATIVE_NUMBER.isValid(0)).isTrue();
            assertThat(EnumUseCases.ValidationRule.NON_NEGATIVE_NUMBER.isValid(-1)).isFalse();
        }

        @Test
        @DisplayName("VALID_EMAIL should validate email format")
        void testValidEmail() {
            assertThat(EnumUseCases.ValidationRule.VALID_EMAIL.isValid("user@example.com")).isTrue();
            assertThat(EnumUseCases.ValidationRule.VALID_EMAIL.isValid("invalid")).isFalse();
            assertThat(EnumUseCases.ValidationRule.VALID_EMAIL.isValid("")).isFalse();
        }

        @Test
        @DisplayName("validate should return error message on failure")
        void testValidateReturnsMessage() {
            Optional<String> error = EnumUseCases.ValidationRule.NOT_NULL.validate(null);
            assertThat(error).isPresent();
            assertThat(error.get()).contains("must not be null");
        }

        @Test
        @DisplayName("validate should return empty on success")
        void testValidateReturnsEmpty() {
            assertThat(EnumUseCases.ValidationRule.NOT_NULL.validate("ok")).isEmpty();
        }

        @Test
        @DisplayName("validateAll should return all violations")
        void testValidateAll() {
            List<String> errors = EnumUseCases.ValidationRule.validateAll(
                    null,
                    EnumUseCases.ValidationRule.NOT_NULL,
                    EnumUseCases.ValidationRule.NOT_EMPTY,
                    EnumUseCases.ValidationRule.POSITIVE_NUMBER
            );
            assertThat(errors).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("validateAll should return empty for valid value")
        void testValidateAllSuccess() {
            List<String> errors = EnumUseCases.ValidationRule.validateAll(
                    "hello",
                    EnumUseCases.ValidationRule.NOT_NULL,
                    EnumUseCases.ValidationRule.NOT_EMPTY,
                    EnumUseCases.ValidationRule.NOT_BLANK
            );
            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("Should have error messages")
        void testMessages() {
            assertThat(EnumUseCases.ValidationRule.NOT_NULL.message()).isNotBlank();
            assertThat(EnumUseCases.ValidationRule.VALID_EMAIL.message()).contains("email");
        }
    }

    @Nested
    @DisplayName("CacheType - Singleton Services")
    class CacheTypeTest {

        @Test
        @DisplayName("LRU cache should store and retrieve values")
        void testLruPutGet() {
            EnumUseCases.CacheType.LRU.clear();
            EnumUseCases.CacheType.LRU.put("key1", "value1");

            assertThat(EnumUseCases.CacheType.LRU.get("key1")).contains("value1");
        }

        @Test
        @DisplayName("LRU cache should return empty for missing key")
        void testLruMissing() {
            EnumUseCases.CacheType.LRU.clear();
            assertThat(EnumUseCases.CacheType.LRU.get("nonexistent")).isEmpty();
        }

        @Test
        @DisplayName("FIFO cache should store and retrieve values")
        void testFifoPutGet() {
            EnumUseCases.CacheType.FIFO.clear();
            EnumUseCases.CacheType.FIFO.put("key1", "value1");

            assertThat(EnumUseCases.CacheType.FIFO.get("key1")).contains("value1");
        }

        @Test
        @DisplayName("FIFO cache should return empty for missing key")
        void testFifoMissing() {
            EnumUseCases.CacheType.FIFO.clear();
            assertThat(EnumUseCases.CacheType.FIFO.get("nonexistent")).isEmpty();
        }

        @Test
        @DisplayName("Cache should track size")
        void testSize() {
            EnumUseCases.CacheType.FIFO.clear();
            assertThat(EnumUseCases.CacheType.FIFO.size()).isZero();

            EnumUseCases.CacheType.FIFO.put("a", 1);
            EnumUseCases.CacheType.FIFO.put("b", 2);
            assertThat(EnumUseCases.CacheType.FIFO.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("Clear should empty the cache")
        void testClear() {
            EnumUseCases.CacheType.LRU.clear();
            EnumUseCases.CacheType.LRU.put("key", "value");
            EnumUseCases.CacheType.LRU.clear();

            assertThat(EnumUseCases.CacheType.LRU.size()).isZero();
        }
    }

    @Nested
    @DisplayName("Result - Pattern Matching with Enum")
    class ResultTest {

        @Test
        @DisplayName("Success result should carry value")
        void testSuccess() {
            EnumUseCases.Result<String> result = EnumUseCases.Result.success("hello");

            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getValue()).contains("hello");
            assertThat(result.status()).isEqualTo(EnumUseCases.ResultStatus.SUCCESS);
        }

        @Test
        @DisplayName("Failure result should carry message")
        void testFailure() {
            EnumUseCases.Result<String> result = EnumUseCases.Result.failure("error occurred");

            assertThat(result.isSuccessful()).isFalse();
            assertThat(result.status().isFailed()).isTrue();
            assertThat(result.message()).isEqualTo("error occurred");
            assertThat(result.getValue()).isEmpty();
        }

        @Test
        @DisplayName("Pending result should have pending status")
        void testPending() {
            EnumUseCases.Result<String> result = EnumUseCases.Result.pending();

            assertThat(result.status().isPending()).isTrue();
            assertThat(result.isSuccessful()).isFalse();
        }

        @Test
        @DisplayName("Should map success value")
        void testMap() {
            EnumUseCases.Result<String> result = EnumUseCases.Result.success("hello");
            EnumUseCases.Result<Integer> mapped = result.map(String::length);

            assertThat(mapped.isSuccessful()).isTrue();
            assertThat(mapped.getValue()).contains(5);
        }

        @Test
        @DisplayName("Should not map failure")
        void testMapFailure() {
            EnumUseCases.Result<String> result = EnumUseCases.Result.failure("error");
            EnumUseCases.Result<Integer> mapped = result.map(String::length);

            assertThat(mapped.isSuccessful()).isFalse();
            assertThat(mapped.getValue()).isEmpty();
        }

        @Test
        @DisplayName("Should flatMap success value")
        void testFlatMap() {
            EnumUseCases.Result<String> result = EnumUseCases.Result.success("42");
            EnumUseCases.Result<Integer> flatMapped = result.flatMap(
                    s -> EnumUseCases.Result.success(Integer.parseInt(s)));

            assertThat(flatMapped.isSuccessful()).isTrue();
            assertThat(flatMapped.getValue()).contains(42);
        }

        @Test
        @DisplayName("Should not flatMap failure")
        void testFlatMapFailure() {
            EnumUseCases.Result<String> result = EnumUseCases.Result.failure("error");
            EnumUseCases.Result<Integer> flatMapped = result.flatMap(
                    s -> EnumUseCases.Result.success(Integer.parseInt(s)));

            assertThat(flatMapped.isSuccessful()).isFalse();
        }
    }

    @Nested
    @DisplayName("Permission - EnumSet Patterns")
    class PermissionTest {

        @Test
        @DisplayName("READ_ONLY should contain only READ")
        void testReadOnly() {
            assertThat(EnumUseCases.Permission.READ_ONLY)
                    .containsExactly(EnumUseCases.Permission.READ);
        }

        @Test
        @DisplayName("FULL_ACCESS should contain all permissions")
        void testFullAccess() {
            assertThat(EnumUseCases.Permission.FULL_ACCESS)
                    .containsExactlyInAnyOrder(EnumUseCases.Permission.values());
        }

        @Test
        @DisplayName("NO_ACCESS should be empty")
        void testNoAccess() {
            assertThat(EnumUseCases.Permission.NO_ACCESS).isEmpty();
        }

        @Test
        @DisplayName("Should check if permission is granted")
        void testIsGrantedIn() {
            assertThat(EnumUseCases.Permission.READ.isGrantedIn(EnumUseCases.Permission.READ_WRITE)).isTrue();
            assertThat(EnumUseCases.Permission.DELETE.isGrantedIn(EnumUseCases.Permission.READ_WRITE)).isFalse();
        }

        @Test
        @DisplayName("Should combine permission sets")
        void testCombine() {
            Set<EnumUseCases.Permission> combined = EnumUseCases.Permission.combine(
                    EnumUseCases.Permission.READ_ONLY,
                    EnumSet.of(EnumUseCases.Permission.WRITE)
            );
            assertThat(combined).containsExactlyInAnyOrder(
                    EnumUseCases.Permission.READ, EnumUseCases.Permission.WRITE
            );
        }

        @Test
        @DisplayName("Should intersect permission sets")
        void testIntersect() {
            Set<EnumUseCases.Permission> intersection = EnumUseCases.Permission.intersect(
                    EnumUseCases.Permission.READ_WRITE,
                    EnumUseCases.Permission.READ_ONLY
            );
            assertThat(intersection).containsExactly(EnumUseCases.Permission.READ);
        }

        @Test
        @DisplayName("Should return empty for no-args intersect")
        void testIntersectEmpty() {
            Set<EnumUseCases.Permission> result = EnumUseCases.Permission.intersect();
            assertThat(result).isEmpty();
        }
    }
}
