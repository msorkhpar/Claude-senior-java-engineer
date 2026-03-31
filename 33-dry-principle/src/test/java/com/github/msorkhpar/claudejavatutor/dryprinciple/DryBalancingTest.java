package com.github.msorkhpar.claudejavatutor.dryprinciple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DRY Balancing with Other Principles Tests")
class DryBalancingTest {

    @Nested
    @DisplayName("Over-DRY Violation")
    class OverDryViolationTest {

        private final DryBalancing.OverDryViolation overDry = new DryBalancing.OverDryViolation();

        @Test
        @DisplayName("Generic calculation as tax adds to amount")
        void testGenericCalculationAsTax() {
            double result = overDry.genericCalculation(100, 0.2, true);
            assertThat(result).isEqualTo(120.0);
        }

        @Test
        @DisplayName("Generic calculation as discount subtracts from amount")
        void testGenericCalculationAsDiscount() {
            double result = overDry.genericCalculation(100, 0.2, false);
            assertThat(result).isEqualTo(80.0);
        }
    }

    @Nested
    @DisplayName("Balanced Approach - Separate Business Rules")
    class BalancedApproachTest {

        private final DryBalancing.BalancedApproach balanced = new DryBalancing.BalancedApproach();

        @Test
        @DisplayName("Should calculate tax correctly")
        void testCalculateTax() {
            assertThat(balanced.calculateTax(100, 0.2)).isEqualTo(120.0);
        }

        @Test
        @DisplayName("Should calculate discount correctly")
        void testCalculateDiscount() {
            assertThat(balanced.calculateDiscount(100, 0.15)).isEqualTo(85.0);
        }

        @Test
        @DisplayName("Should handle zero tax rate")
        void testZeroTaxRate() {
            assertThat(balanced.calculateTax(100, 0.0)).isEqualTo(100.0);
        }

        @Test
        @DisplayName("Should handle zero discount rate")
        void testZeroDiscountRate() {
            assertThat(balanced.calculateDiscount(100, 0.0)).isEqualTo(100.0);
        }

        @Test
        @DisplayName("Should handle full tax rate (1.0)")
        void testFullTaxRate() {
            assertThat(balanced.calculateTax(100, 1.0)).isEqualTo(200.0);
        }

        @Test
        @DisplayName("Should handle full discount rate (1.0)")
        void testFullDiscountRate() {
            assertThat(balanced.calculateDiscount(100, 1.0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should reject negative amount for tax")
        void testNegativeAmountTax() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> balanced.calculateTax(-1, 0.1))
                    .withMessageContaining("Amount must not be negative");
        }

        @Test
        @DisplayName("Should reject negative amount for discount")
        void testNegativeAmountDiscount() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> balanced.calculateDiscount(-1, 0.1))
                    .withMessageContaining("Amount must not be negative");
        }

        @Test
        @DisplayName("Should reject invalid tax rate")
        void testInvalidTaxRate() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> balanced.calculateTax(100, 1.5))
                    .withMessageContaining("Tax rate must be between 0 and 1");
        }

        @Test
        @DisplayName("Should reject negative discount rate")
        void testNegativeDiscountRate() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> balanced.calculateDiscount(100, -0.1))
                    .withMessageContaining("Discount rate must be between 0 and 1");
        }
    }

    @Nested
    @DisplayName("ValidationUtils - Shared Utility Methods")
    class ValidationUtilsTest {

        @Test
        @DisplayName("isNonBlank returns true for valid strings")
        void testIsNonBlankValid() {
            assertThat(DryBalancing.ValidationUtils.isNonBlank("hello")).isTrue();
            assertThat(DryBalancing.ValidationUtils.isNonBlank("a")).isTrue();
        }

        @Test
        @DisplayName("isNonBlank returns false for null, empty, and blank strings")
        void testIsNonBlankInvalid() {
            assertThat(DryBalancing.ValidationUtils.isNonBlank(null)).isFalse();
            assertThat(DryBalancing.ValidationUtils.isNonBlank("")).isFalse();
            assertThat(DryBalancing.ValidationUtils.isNonBlank("   ")).isFalse();
        }

        @Test
        @DisplayName("isPositive returns true for positive numbers")
        void testIsPositiveValid() {
            assertThat(DryBalancing.ValidationUtils.isPositive(1)).isTrue();
            assertThat(DryBalancing.ValidationUtils.isPositive(0.001)).isTrue();
        }

        @Test
        @DisplayName("isPositive returns false for zero, negative, and null")
        void testIsPositiveInvalid() {
            assertThat(DryBalancing.ValidationUtils.isPositive(0)).isFalse();
            assertThat(DryBalancing.ValidationUtils.isPositive(-5)).isFalse();
            assertThat(DryBalancing.ValidationUtils.isPositive(null)).isFalse();
        }

        @Test
        @DisplayName("isInRange returns true when value is within bounds")
        void testIsInRangeValid() {
            assertThat(DryBalancing.ValidationUtils.isInRange(5, 1, 10)).isTrue();
            assertThat(DryBalancing.ValidationUtils.isInRange(1, 1, 10)).isTrue();
            assertThat(DryBalancing.ValidationUtils.isInRange(10, 1, 10)).isTrue();
        }

        @Test
        @DisplayName("isInRange returns false when value is out of bounds")
        void testIsInRangeInvalid() {
            assertThat(DryBalancing.ValidationUtils.isInRange(0, 1, 10)).isFalse();
            assertThat(DryBalancing.ValidationUtils.isInRange(11, 1, 10)).isFalse();
        }
    }

    @Nested
    @DisplayName("OrderValidator - Domain-Specific Validator Using Shared Utils")
    class OrderValidatorTest {

        private final DryBalancing.OrderValidator validator = new DryBalancing.OrderValidator();

        @Test
        @DisplayName("Should return no errors for valid order")
        void testValidOrder() {
            List<String> errors = validator.validate("CUST-1", 99.99, 5);
            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("Should detect missing customer ID")
        void testMissingCustomerId() {
            List<String> errors = validator.validate("", 99.99, 5);
            assertThat(errors).contains("Customer ID is required");
        }

        @Test
        @DisplayName("Should detect null customer ID")
        void testNullCustomerId() {
            List<String> errors = validator.validate(null, 99.99, 5);
            assertThat(errors).contains("Customer ID is required");
        }

        @Test
        @DisplayName("Should detect non-positive amount")
        void testNonPositiveAmount() {
            List<String> errors = validator.validate("CUST-1", 0, 5);
            assertThat(errors).contains("Amount must be positive");
        }

        @Test
        @DisplayName("Should detect non-positive quantity")
        void testNonPositiveQuantity() {
            List<String> errors = validator.validate("CUST-1", 99.99, 0);
            assertThat(errors).contains("Quantity must be positive");
        }

        @Test
        @DisplayName("Should detect excessive quantity")
        void testExcessiveQuantity() {
            List<String> errors = validator.validate("CUST-1", 99.99, 10001);
            assertThat(errors).contains("Quantity exceeds maximum allowed (10000)");
        }

        @Test
        @DisplayName("Should accumulate multiple errors")
        void testMultipleErrors() {
            List<String> errors = validator.validate("", -1, 0);
            assertThat(errors).hasSize(3);
        }
    }

    @Nested
    @DisplayName("EmployeeValidator - Domain-Specific Validator Using Shared Utils")
    class EmployeeValidatorTest {

        private final DryBalancing.EmployeeValidator validator = new DryBalancing.EmployeeValidator();

        @Test
        @DisplayName("Should return no errors for valid employee")
        void testValidEmployee() {
            List<String> errors = validator.validate("Alice", 30, 50000);
            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("Should detect missing name")
        void testMissingName() {
            List<String> errors = validator.validate("", 30, 50000);
            assertThat(errors).contains("Name is required");
        }

        @Test
        @DisplayName("Should detect invalid age below range")
        void testAgeBelowRange() {
            List<String> errors = validator.validate("Alice", 17, 50000);
            assertThat(errors).contains("Age must be between 18 and 120");
        }

        @Test
        @DisplayName("Should detect invalid age above range")
        void testAgeAboveRange() {
            List<String> errors = validator.validate("Alice", 121, 50000);
            assertThat(errors).contains("Age must be between 18 and 120");
        }

        @Test
        @DisplayName("Should accept boundary ages")
        void testBoundaryAges() {
            assertThat(validator.validate("Alice", 18, 50000)).isEmpty();
            assertThat(validator.validate("Alice", 120, 50000)).isEmpty();
        }

        @Test
        @DisplayName("Should detect non-positive salary")
        void testNonPositiveSalary() {
            List<String> errors = validator.validate("Alice", 30, 0);
            assertThat(errors).contains("Salary must be positive");
        }
    }

    @Nested
    @DisplayName("DuplicationAnalysis - Incidental vs Intentional")
    class DuplicationAnalysisTest {

        @Test
        @DisplayName("Employee and Customer addresses format independently")
        void testAddressFormatsIndependent() {
            var empAddr = new DryBalancing.DuplicationAnalysis.EmployeeAddress("123 Main", "Springfield", "12345");
            var custAddr = new DryBalancing.DuplicationAnalysis.CustomerAddress("123 Main", "Springfield", "12345");

            // They look the same but are independent domain objects
            assertThat(empAddr.format()).isEqualTo(custAddr.format());
            assertThat(empAddr.format()).isEqualTo("123 Main, Springfield 12345");
        }

        @Test
        @DisplayName("Intentional duplication: both discount methods produce same results")
        void testIntentionalDuplication() {
            double online = DryBalancing.DuplicationAnalysis.applyBulkDiscountForOnlineOrders(100, 100);
            double inStore = DryBalancing.DuplicationAnalysis.applyBulkDiscountForInStoreOrders(100, 100);
            double fixed = DryBalancing.DuplicationAnalysis.applyBulkDiscount(100, 100);

            assertThat(online).isEqualTo(inStore).isEqualTo(fixed).isEqualTo(80.0);
        }

        @Test
        @DisplayName("Bulk discount tiers work correctly")
        void testBulkDiscountTiers() {
            assertThat(DryBalancing.DuplicationAnalysis.applyBulkDiscount(100, 1)).isEqualTo(100.0);
            assertThat(DryBalancing.DuplicationAnalysis.applyBulkDiscount(100, 10)).isEqualTo(95.0);
            assertThat(DryBalancing.DuplicationAnalysis.applyBulkDiscount(100, 50)).isEqualTo(90.0);
            assertThat(DryBalancing.DuplicationAnalysis.applyBulkDiscount(100, 100)).isEqualTo(80.0);
        }

        @Test
        @DisplayName("Bulk discount boundary values")
        void testBulkDiscountBoundaries() {
            assertThat(DryBalancing.DuplicationAnalysis.applyBulkDiscount(100, 9)).isEqualTo(100.0);
            assertThat(DryBalancing.DuplicationAnalysis.applyBulkDiscount(100, 49)).isEqualTo(95.0);
            assertThat(DryBalancing.DuplicationAnalysis.applyBulkDiscount(100, 99)).isEqualTo(90.0);
        }
    }

    @Nested
    @DisplayName("Rule of Three")
    class RuleOfThreeTest {

        private final DryBalancing.RuleOfThree ruleOfThree = new DryBalancing.RuleOfThree();

        @Test
        @DisplayName("All specific formatters produce same format")
        void testAllFormattersMatch() {
            String order = ruleOfThree.formatOrderSummary("ORD-1", "Widget order", 99.99);
            String invoice = ruleOfThree.formatInvoiceSummary("INV-1", "Acme Corp", 99.99);
            String receipt = ruleOfThree.formatReceiptSummary("REC-1", "Store A", 99.99);

            // Verify same format pattern
            assertThat(order).matches("\\[.*\\] .* - \\$\\d+\\.\\d{2}");
            assertThat(invoice).matches("\\[.*\\] .* - \\$\\d+\\.\\d{2}");
            assertThat(receipt).matches("\\[.*\\] .* - \\$\\d+\\.\\d{2}");
        }

        @Test
        @DisplayName("Extracted formatSummary matches all specific formatters")
        void testExtractedFormatterMatchesSpecific() {
            String specific = ruleOfThree.formatOrderSummary("ORD-1", "Test", 50.0);
            String generic = ruleOfThree.formatSummary("ORD-1", "Test", 50.0);
            assertThat(specific).isEqualTo(generic);
        }

        @Test
        @DisplayName("formatSummary handles various amounts")
        void testFormatSummaryAmounts() {
            assertThat(ruleOfThree.formatSummary("ID", "Desc", 0.0)).isEqualTo("[ID] Desc - $0.00");
            assertThat(ruleOfThree.formatSummary("ID", "Desc", 1234.56)).isEqualTo("[ID] Desc - $1234.56");
        }
    }
}
