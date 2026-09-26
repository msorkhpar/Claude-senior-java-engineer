package practice;

import java.util.List;

record Order(List<String> items, double total, String customer) {
}

@FunctionalInterface
public interface OrderValidator {

    boolean validate(Order order);

    default OrderValidator and(OrderValidator other) {
        throw new UnsupportedOperationException("write and");
    }

    default OrderValidator or(OrderValidator other) {
        throw new UnsupportedOperationException("write or");
    }

    default OrderValidator negate() {
        throw new UnsupportedOperationException("write negate");
    }

    static OrderValidator hasItems() {
        throw new UnsupportedOperationException("write hasItems");
    }

    static OrderValidator hasCustomer() {
        throw new UnsupportedOperationException("write hasCustomer");
    }
}
