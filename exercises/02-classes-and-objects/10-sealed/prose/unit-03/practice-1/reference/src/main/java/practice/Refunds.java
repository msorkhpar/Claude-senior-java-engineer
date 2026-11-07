package practice;

/** Sends refunds to the payment gateway, one order at a time. */
public class Refunds {

    /** The outcome of a step that can refuse: a value, or the reason it refused. */
    public sealed interface Result<T> permits Ok, Err {}
    public record Ok<T>(T value) implements Result<T> {}
    public record Err<T>(String reason) implements Result<T> {}

    public record Order(String id, long paidCents, long refundedCents) {}
    public record Refund(String orderId, long cents, String gatewayRef) {}

    /** Looks an order up; an unknown id is an Err with the reason. */
    public interface Orders {
        Result<Order> find(String orderId);
    }

    /** Pays money back; the bank may refuse, which is an Err with the reason. Returns the gateway's reference. */
    public interface Gateway {
        Result<String> send(String orderId, long cents);
    }

    private final Orders orders;
    private final Gateway gateway;

    public Refunds(Orders orders, Gateway gateway) {
        this.orders = orders;
        this.gateway = gateway;
    }

    /** How much of this order may still be refunded: an Err when the amount isn't allowed. */
    static Result<Long> refundable(Order order, long cents) {
        long left = order.paidCents() - order.refundedCents();
        if (cents <= 0) return new Err<>("refund must be positive, was " + cents);
        if (cents > left) return new Err<>("only " + left + " cents left to refund on " + order.id());
        return new Ok<>(cents);
    }

    /**
     * Refunds cents on the order: finds it, checks the amount, then sends it to the gateway.
     * Every refusal comes back as an Err carrying the reason of the step that refused.
     */
    public Result<Refund> refund(String orderId, long cents) {
        Order order;
        switch (orders.find(orderId)) {
            case Ok<Order> ok -> order = ok.value();
            case Err<Order> err -> { return new Err<>(err.reason()); }
        }
        switch (refundable(order, cents)) {
            case Ok<Long> ok -> { }
            case Err<Long> err -> { return new Err<>(err.reason()); }
        }
        return switch (gateway.send(orderId, cents)) {
            case Ok<String> ref -> new Ok<>(new Refund(orderId, cents, ref.value()));
            case Err<String> err -> new Err<>(err.reason());
        };
    }
}
