package practice;

/** The caller exceeded the rate limit; it may retry after the given delay. Choose its superclass. */
class RateLimitedException extends Exception {

    RateLimitedException(String message, int retryAfterSeconds) {
        throw new UnsupportedOperationException("write RateLimitedException(String, int)");
    }

    int getRetryAfterSeconds() {
        throw new UnsupportedOperationException("write getRetryAfterSeconds");
    }
}

/** The caller passed no request id. Choose its superclass. */
class BadRequestIdException extends Exception {

    BadRequestIdException(String message) {
        throw new UnsupportedOperationException("write BadRequestIdException(String)");
    }
}

public class Gateway {

    /** "sent " + requestId, or one of the two failures described in the statement. */
    public String send(String requestId, int callsThisMinute) {
        throw new UnsupportedOperationException("write send");
    }
}
