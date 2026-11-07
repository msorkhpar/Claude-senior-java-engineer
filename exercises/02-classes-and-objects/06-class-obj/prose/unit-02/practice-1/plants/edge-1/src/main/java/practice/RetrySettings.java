package practice;

import java.util.Locale;

/**
 * How the payment client retries a call to one endpoint. The service's settings loader creates
 * these from YAML documents: it calls the no-argument constructor, then sets each field the
 * document names. Application code uses the other two constructors.
 */
public class RetrySettings {

    static final String DEFAULT_ENDPOINT = "http://127.0.0.1:8080";
    static final int DEFAULT_ATTEMPTS = 3;
    static final long DEFAULT_BACKOFF_MILLIS = 200;

    private String endpoint;
    private int maxAttempts;
    private long backoffMillis;

    /** Used by the settings loader, which then sets the fields the document names. */
    RetrySettings() {
    }

    /** An endpoint with the default number of attempts and backoff. */
    public RetrySettings(String endpoint) {
        this(endpoint, DEFAULT_ATTEMPTS, DEFAULT_BACKOFF_MILLIS);
    }

    public RetrySettings(String endpoint, int maxAttempts, long backoffMillis) {
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("endpoint is required");
        }
        if (maxAttempts < 1 || maxAttempts > 10) {
            throw new IllegalArgumentException("maxAttempts must be 1 to 10: " + maxAttempts);
        }
        if (backoffMillis <= 0) {
            throw new IllegalArgumentException("backoffMillis must be positive: " + backoffMillis);
        }
        this.endpoint = normalise(endpoint);
        this.maxAttempts = maxAttempts;
        this.backoffMillis = backoffMillis;
    }

    /** Endpoints are compared as text by the client's circuit breaker, so they are kept in one form. */
    private static String normalise(String endpoint) {
        String e = endpoint.strip().toLowerCase(Locale.ROOT);
        while (e.endsWith("/")) e = e.substring(0, e.length() - 1);
        return e;
    }

    public String endpoint() { return endpoint; }
    public int maxAttempts() { return maxAttempts; }
    public long backoffMillis() { return backoffMillis; }
}
