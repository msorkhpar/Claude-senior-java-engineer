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
        return value > min && value < max;
    }

    /** Every problem with an order, in this order; an empty list means the order is valid. */
    public static List<String> validateOrder(String customerId, double amount, int quantity) {
        List<String> errors = new ArrayList<>();
        if (!isNonBlank(customerId)) {
            errors.add("Customer ID is required");
        }
        if (!isPositive(amount)) {
            errors.add("Amount must be positive");
        }
        if (!isPositive(quantity)) {
            errors.add("Quantity must be positive");
        }
        if (quantity > 10000) {
            errors.add("Quantity exceeds maximum allowed (10000)");
        }
        return errors;
    }

    /** Every problem with an employee, in this order; an empty list means the employee is valid. */
    public static List<String> validateEmployee(String name, int age, double salary) {
        List<String> errors = new ArrayList<>();
        if (!isNonBlank(name)) {
            errors.add("Name is required");
        }
        if (!isInRange(age, 18, 120)) {
            errors.add("Age must be between 18 and 120");
        }
        if (!isPositive(salary)) {
            errors.add("Salary must be positive");
        }
        return errors;
    }
}
