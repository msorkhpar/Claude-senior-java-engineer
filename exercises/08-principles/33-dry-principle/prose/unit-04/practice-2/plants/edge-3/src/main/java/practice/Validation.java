package practice;

import java.util.ArrayList;
import java.util.List;

/** Shared primitive checks, and one validator per domain that uses them. */
public final class Validation {

    private Validation() {
    }

    /** Whether the value is neither null nor blank. */
    public static boolean isNonBlank(String value) {
        return value != null && !value.isBlank();
    }

    /** Whether the value is not null and greater than zero. */
    public static boolean isPositive(Number value) {
        return value != null && value.doubleValue() > 0;
    }

    /** Whether min <= value <= max. */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /** Every problem with an order, in this order; an empty list means the order is valid. */
    public static List<String> validateOrder(String customerId, double amount, int quantity) {
        if (!isNonBlank(customerId)) {
            return List.of("Customer ID is required");
        }
        if (!isPositive(amount)) {
            return List.of("Amount must be positive");
        }
        if (!isPositive(quantity)) {
            return List.of("Quantity must be positive");
        }
        if (quantity > 10000) {
            return List.of("Quantity exceeds maximum allowed (10000)");
        }
        return new ArrayList<>();
    }

    /** Every problem with an employee, in this order; an empty list means the employee is valid. */
    public static List<String> validateEmployee(String name, int age, double salary) {
        if (!isNonBlank(name)) {
            return List.of("Name is required");
        }
        if (!isInRange(age, 18, 120)) {
            return List.of("Age must be between 18 and 120");
        }
        if (!isPositive(salary)) {
            return List.of("Salary must be positive");
        }
        return new ArrayList<>();
    }
}
