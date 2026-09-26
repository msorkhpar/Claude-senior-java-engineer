package practice;

import java.util.ArrayList;
import java.util.List;

/** Shared primitive checks, and one validator per domain that uses them. */
public final class Validation {

    private Validation() {
    }

    /** Whether the value is neither null nor blank. */
    public static boolean isNonBlank(String value) {
        throw new UnsupportedOperationException("write isNonBlank");
    }

    /** Whether the value is not null and greater than zero. */
    public static boolean isPositive(Number value) {
        throw new UnsupportedOperationException("write isPositive");
    }

    /** Whether min <= value <= max. */
    public static boolean isInRange(double value, double min, double max) {
        throw new UnsupportedOperationException("write isInRange");
    }

    /** Every problem with an order, in this order; an empty list means the order is valid. */
    public static List<String> validateOrder(String customerId, double amount, int quantity) {
        throw new UnsupportedOperationException("write validateOrder");
    }

    /** Every problem with an employee, in this order; an empty list means the employee is valid. */
    public static List<String> validateEmployee(String name, int age, double salary) {
        throw new UnsupportedOperationException("write validateEmployee");
    }
}
