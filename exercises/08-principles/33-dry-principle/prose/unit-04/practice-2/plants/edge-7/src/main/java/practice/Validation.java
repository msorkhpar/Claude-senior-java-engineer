package practice;
import java.util.ArrayList;
import java.util.List;
public final class Validation {
    private Validation() {}
    public static boolean isNonBlank(String value) { return value != null && !value.isBlank(); }
    public static boolean isPositive(Number value) { return value != null && !(value.doubleValue() <= 0); }
    public static boolean isInRange(double value, double min, double max) { return value >= min && value <= max; }
    public static List<String> validateOrder(String customerId, double amount, int quantity) {
        List<String> p = new ArrayList<>();
        if (!isNonBlank(customerId)) p.add("Customer ID is required");
        if (!isPositive(amount)) p.add("Amount must be positive");
        if (!isPositive(quantity)) p.add("Quantity must be positive"); else if (quantity > 10000) p.add("Quantity exceeds maximum allowed (10000)");
        return p;
    }
    public static List<String> validateEmployee(String name, int age, double salary) {
        List<String> p = new ArrayList<>();
        if (!isNonBlank(name)) p.add("Name is required");
        if (!isInRange(age, 18, 120)) p.add("Age must be between 18 and 120");
        if (!isPositive(salary)) p.add("Salary must be positive");
        return p;
    }
}
