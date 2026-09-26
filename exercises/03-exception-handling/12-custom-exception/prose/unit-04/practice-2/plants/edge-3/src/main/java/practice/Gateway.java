package practice;

/** The caller exceeded the rate limit; it may retry after the given delay. */
class RateLimitedException extends Exception {

    private final int retryAfterSeconds;

    RateLimitedException(String message, int retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}

/** The caller passed no request id. */
class BadRequestIdException extends RuntimeException {

    BadRequestIdException(String message) {
        super(message);
    }
}

public class Gateway {

    /** "sent " + requestId, or one of the two failures described in the statement. */
    public String send(String requestId, int callsThisMinute) throws RateLimitedException {
        if (requestId == null || requestId.isEmpty()) {
            throw new BadRequestIdException("Request id is required");
        }
        if (callsThisMinute > 10) {
            throw new RateLimitedException("Rate limit exceeded", 60);
        }
        return "sent " + requestId;
    }
}
