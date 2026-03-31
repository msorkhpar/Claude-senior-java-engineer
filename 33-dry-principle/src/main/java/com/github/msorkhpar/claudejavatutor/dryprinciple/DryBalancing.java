package com.github.msorkhpar.claudejavatutor.dryprinciple;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Demonstrates balancing DRY with other principles (SRP, KISS, etc.).
 * Shows when DRY is beneficial and when over-applying it causes harm.
 */
public class DryBalancing {

    // ========== Over-DRY: Harmful Abstraction ==========

    /**
     * OVER-DRY VIOLATION: Merging unrelated domain logic into a shared "generic" method.
     * The tax calculation and discount calculation happen to share some structure,
     * but they represent different business rules that evolve independently.
     */
    public static class OverDryViolation {

        /**
         * A "generic" calculation that tries to handle both tax and discount --
         * this couples unrelated domain logic and violates SRP.
         */
        public double genericCalculation(double amount, double rate, boolean isTax) {
            if (isTax) {
                return amount * (1 + rate);
            } else {
                return amount * (1 - rate);
            }
        }
    }

    /**
     * BALANCED: Separate methods for separate business rules.
     * Even though the structure looks similar, these represent different
     * domain concepts that may diverge in the future.
     */
    public static class BalancedApproach {

        public double calculateTax(double amount, double taxRate) {
            if (amount < 0) {
                throw new IllegalArgumentException("Amount must not be negative");
            }
            if (taxRate < 0 || taxRate > 1) {
                throw new IllegalArgumentException("Tax rate must be between 0 and 1");
            }
            return amount * (1 + taxRate);
        }

        public double calculateDiscount(double amount, double discountRate) {
            if (amount < 0) {
                throw new IllegalArgumentException("Amount must not be negative");
            }
            if (discountRate < 0 || discountRate > 1) {
                throw new IllegalArgumentException("Discount rate must be between 0 and 1");
            }
            return amount * (1 - discountRate);
        }
    }

    // ========== DRY vs SRP: Finding the Right Balance ==========

    /**
     * Demonstrates where DRY and SRP conflict.
     * A shared utility for validation is DRY, but if it grows to handle
     * too many domains, it violates SRP.
     */
    public static class ValidationUtils {

        /**
         * DRY: Reusable non-blank string check.
         * This is a general-purpose utility -- appropriate for DRY.
         */
        public static boolean isNonBlank(String value) {
            return value != null && !value.isBlank();
        }

        /**
         * DRY: Reusable positive number check.
         */
        public static boolean isPositive(Number value) {
            return value != null && value.doubleValue() > 0;
        }

        /**
         * DRY: Reusable range check.
         */
        public static boolean isInRange(double value, double min, double max) {
            return value >= min && value <= max;
        }
    }

    /**
     * Domain-specific validators that USE the shared utilities (DRY)
     * but encapsulate domain rules (SRP).
     */
    public static class OrderValidator {

        public List<String> validate(String customerId, double amount, int quantity) {
            List<String> errors = new ArrayList<>();

            if (!ValidationUtils.isNonBlank(customerId)) {
                errors.add("Customer ID is required");
            }
            if (!ValidationUtils.isPositive(amount)) {
                errors.add("Amount must be positive");
            }
            if (!ValidationUtils.isPositive(quantity)) {
                errors.add("Quantity must be positive");
            }
            if (quantity > 10000) {
                errors.add("Quantity exceeds maximum allowed (10000)");
            }

            return errors;
        }
    }

    public static class EmployeeValidator {

        public List<String> validate(String name, int age, double salary) {
            List<String> errors = new ArrayList<>();

            if (!ValidationUtils.isNonBlank(name)) {
                errors.add("Name is required");
            }
            if (!ValidationUtils.isInRange(age, 18, 120)) {
                errors.add("Age must be between 18 and 120");
            }
            if (!ValidationUtils.isPositive(salary)) {
                errors.add("Salary must be positive");
            }

            return errors;
        }
    }

    // ========== Incidental vs Intentional Duplication ==========

    /**
     * Demonstrates the difference between incidental and intentional duplication.
     * Incidental duplication: code that looks the same but serves different purposes.
     * Intentional duplication: true knowledge duplication that should be eliminated.
     */
    public static class DuplicationAnalysis {

        /**
         * These two methods look similar but serve DIFFERENT business domains.
         * They may diverge in the future (e.g., employee addresses need department info,
         * customer addresses need shipping preferences).
         * This is INCIDENTAL duplication -- acceptable to keep separate.
         */
        public record EmployeeAddress(String street, String city, String zipCode) {
            public String format() {
                return street + ", " + city + " " + zipCode;
            }
        }

        public record CustomerAddress(String street, String city, String zipCode) {
            public String format() {
                return street + ", " + city + " " + zipCode;
            }
        }

        /**
         * These two methods represent the SAME business rule applied in different contexts.
         * This is INTENTIONAL duplication -- should be extracted.
         */
        public static double applyBulkDiscountForOnlineOrders(double price, int quantity) {
            if (quantity >= 100) return price * 0.80;
            if (quantity >= 50) return price * 0.90;
            if (quantity >= 10) return price * 0.95;
            return price;
        }

        public static double applyBulkDiscountForInStoreOrders(double price, int quantity) {
            // Same exact logic -- this is true DRY violation
            if (quantity >= 100) return price * 0.80;
            if (quantity >= 50) return price * 0.90;
            if (quantity >= 10) return price * 0.95;
            return price;
        }

        /**
         * DRY FIX: Single method for the shared discount rule.
         */
        public static double applyBulkDiscount(double price, int quantity) {
            if (quantity >= 100) return price * 0.80;
            if (quantity >= 50) return price * 0.90;
            if (quantity >= 10) return price * 0.95;
            return price;
        }
    }

    // ========== Rule of Three ==========

    /**
     * Demonstrates the "Rule of Three" heuristic:
     * Don't abstract until you see duplication three times.
     */
    public static class RuleOfThree {

        /**
         * First occurrence: specific to orders. Keep as is.
         */
        public String formatOrderSummary(String id, String description, double total) {
            return String.format("[%s] %s - $%.2f", id, description, total);
        }

        /**
         * Second occurrence: specific to invoices. Looks similar but keep separate for now.
         */
        public String formatInvoiceSummary(String invoiceNumber, String client, double amount) {
            return String.format("[%s] %s - $%.2f", invoiceNumber, client, amount);
        }

        /**
         * Third occurrence: now we have a pattern! Time to extract.
         */
        public String formatReceiptSummary(String receiptId, String store, double amount) {
            return String.format("[%s] %s - $%.2f", receiptId, store, amount);
        }

        /**
         * DRY: Extracted common formatter after seeing the pattern three times.
         */
        public String formatSummary(String identifier, String description, double amount) {
            return String.format("[%s] %s - $%.2f", identifier, description, amount);
        }
    }
}
