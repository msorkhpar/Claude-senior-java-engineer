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
        return order.items().stream()
                .mapToDouble(Item::price)
                .sum();
    }

    public static double customerTotal(Customer customer) {
        return customer.orders().stream()
                .mapToDouble(Totals::orderTotal)
                .sum();
    }

    public static List<Double> customerTotals(List<Customer> customers) {
        return customers.stream()
                .map(Totals::customerTotal)
                .toList();
    }

    public static double completedRevenue(List<Order> orders) {
        return orders.stream()
                .filter(Totals::isCompleted)
                .flatMap(order -> order.items().stream())
                .mapToDouble(Item::price)
                .filter(price -> price > 0)
                .sum();
    }

    private static boolean isCompleted(Order order) {
        return order.status() == Status.COMPLETED;
    }
}
