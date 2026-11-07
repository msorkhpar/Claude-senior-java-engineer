package practice;

/**
 * The web layer of an ordering service: turns the order module's failures into the API's error
 * responses. Mobile and partner clients decide what to show from the response.
 */
public class OrderApi {

    /** The order module's failures, as published in the API documentation. */
    public enum ErrorCode { OUT_OF_STOCK, COUPON_EXPIRED, ORDER_LIMIT_REACHED }

    /** Every failure the order module reports on purpose. */
    public static class OrderException extends RuntimeException {
        private final ErrorCode code;

        public OrderException(ErrorCode code, String message) {
            super(message);
            this.code = code;
        }

        public ErrorCode code() {
            return code;
        }
    }

    /** What the client receives: an HTTP status, the documented code, and text it may display. */
    public record ApiError(int status, String code, String detail) {
    }

    /**
     * The response for a failure: OUT_OF_STOCK is 409, COUPON_EXPIRED is 422 and
     * ORDER_LIMIT_REACHED is 429. The code is the constant's name.
     */
    public static ApiError toResponse(OrderException e) {
        return switch (e.code()) {
            case OUT_OF_STOCK -> new ApiError(409, e.code().name(), "This item is out of stock.");
            case COUPON_EXPIRED -> new ApiError(422, e.code().name(), "This coupon has expired.");
            case ORDER_LIMIT_REACHED -> new ApiError(429, e.code().name(), "You've reached today's order limit.");
        };
    }
}
