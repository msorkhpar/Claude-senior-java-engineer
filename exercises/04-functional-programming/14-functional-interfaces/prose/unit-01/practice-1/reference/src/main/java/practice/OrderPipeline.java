package practice;

import java.util.function.Consumer;

/**
 * Puts together what happens to each order the shop accepts. The checkout calls the consumer
 * that {@link #build} returns once per order, and shows the customer an error when it throws.
 */
public final class OrderPipeline {

    /** An order for {@code quantity} units of one product. */
    public record Order(String id, String sku, int quantity) {}

    private OrderPipeline() {
    }

    /**
     * Builds the consumer the checkout runs for every order.
     *
     * @param reserve takes the stock for the order; throws IllegalStateException when there is not enough
     * @param confirm emails the customer that the order is on its way; throws when the mail server is down
     * @param audit   records the order in the compliance log; null on deployments that have no such log
     */
    public static Consumer<Order> build(Consumer<Order> reserve, Consumer<Order> confirm, Consumer<Order> audit) {
        Consumer<Order> placed = reserve.andThen(confirm);
        return audit == null ? placed : placed.andThen(audit);
    }
}
