package practice;

import java.util.List;

record Order(List<String> items, double total, String customer) {
}

@FunctionalInterface
public interface OrderValidator {

    boolean validate(Order order);

    default OrderValidator and(OrderValidator other) {
        return order -> this.validate(order) & other.validate(order);
    }

    default OrderValidator or(OrderValidator other) {
        return order -> this.validate(order) || other.validate(order);
    }

    default OrderValidator negate() {
        return order -> !this.validate(order);
    }

    static OrderValidator hasItems() {
        return order -> !order.items().isEmpty();
    }

    static OrderValidator hasCustomer() {
        return order -> order.customer() != null;
    }
}
