package practice;

import java.util.List;
import java.util.function.Consumer;

/** A customer's order. */
record Order(String id, double amount, String customer) {
}

public final class OrderDesk {

    private OrderDesk() {
    }

    /** Audits each order into auditLog, then stores it in processed. */
    public static Consumer<Order> pipeline(List<String> auditLog, List<Order> processed) {
        Consumer<Order> audit = order -> auditLog.add("PROCESSED: " + order.id() + " for " + order.customer());
        return audit.andThen(processed::add);
    }

    /** Feeds pipeline every order whose amount is above threshold, in order. */
    public static void processLarge(List<Order> orders, double threshold, Consumer<Order> pipeline) {
        if (orders.size() < 1_000) {
            orders.stream().filter(order -> order.amount() > threshold).forEach(pipeline);
            return;
        }
        orders.parallelStream().filter(order -> order.amount() > threshold).forEach(pipeline);
    }
}
