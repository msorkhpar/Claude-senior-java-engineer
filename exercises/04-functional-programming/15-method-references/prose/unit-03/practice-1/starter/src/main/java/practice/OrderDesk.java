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
        throw new UnsupportedOperationException("write pipeline");
    }

    /** Feeds pipeline every order whose amount is above threshold, in order. */
    public static void processLarge(List<Order> orders, double threshold, Consumer<Order> pipeline) {
        throw new UnsupportedOperationException("write processLarge");
    }
}
