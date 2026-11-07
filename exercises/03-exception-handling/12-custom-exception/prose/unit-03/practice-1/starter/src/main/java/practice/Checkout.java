package practice;

/**
 * The last step of checkout: books a shipment with the shipping module and tells the customer
 * what happened.
 */
public class Checkout {

    /** Every failure the shipping module reports on purpose extends this one. */
    public static class ShippingException extends RuntimeException {
        public ShippingException(String message) {
            super(message);
        }
    }

    /** The carrier does not deliver to this postcode. */
    public static class AddressRejectedException extends ShippingException {
        public AddressRejectedException(String postcode) {
            super("no delivery to postcode " + postcode);
        }
    }

    /** No carrier has capacity today. */
    public static class NoCarrierException extends ShippingException {
        public NoCarrierException(String message) {
            super(message);
        }
    }

    /** The shipping module. Its team adds new failure types in later releases. */
    public interface Shipping {
        /** Books a shipment and returns its tracking number. */
        String book(String orderId, String postcode);
    }

    static final String ADDRESS_ANSWER = "We can't deliver to that postcode. Please check it.";
    static final String LATER_ANSWER = "We can't ship this order right now. We'll email you.";

    private final Shipping shipping;

    public Checkout(Shipping shipping) {
        this.shipping = shipping;
    }

    /** Books the order's shipment and returns what the customer sees. */
    public String placeOrder(String orderId, String postcode) {
        // TODO: Book the shipment with shipping.book(...) and return "Shipped, tracking number "
        //       followed by the tracking number. When shipping refuses the order, answer the
        //       customer: ADDRESS_ANSWER when the postcode was the problem, LATER_ANSWER for any
        //       other refusal, including the kinds of refusal the shipping team adds later.
        //       A bug in the shipping code is not a refusal: let it reach the error handler.
        throw new IllegalStateException("TODO: placeOrder");
    }
}
