package practice;

import java.util.List;

enum Status { PENDING, COMPLETED }

record Item(double price) {
}

record Order(Status status, List<Item> items) {
}

record Customer(String name, List<Order> orders) {
}

public final class Totals {

    private Totals() {
    }

    public static double orderTotal(Order order) {
        throw new UnsupportedOperationException("write orderTotal");
    }

    public static double customerTotal(Customer customer) {
        throw new UnsupportedOperationException("write customerTotal");
    }

    public static List<Double> customerTotals(List<Customer> customers) {
        throw new UnsupportedOperationException("write customerTotals");
    }

    public static double completedRevenue(List<Order> orders) {
        throw new UnsupportedOperationException("write completedRevenue");
    }
}
