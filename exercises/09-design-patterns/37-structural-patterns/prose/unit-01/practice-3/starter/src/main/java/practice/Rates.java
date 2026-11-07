package practice;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** The new pricing code's view of the legacy FX client: one future per lookup. */
public final class Rates {

    /** The legacy client's callback. Exactly one method is called per lookup that was accepted. */
    public interface RateListener {
        void onRate(String pair, BigDecimal rate);

        void onFailure(String pair, String reason);
    }

    /** The legacy FX client. Don't change it. */
    public interface RateClient {
        /**
         * Checks {@code pair} on the calling thread and throws IllegalArgumentException if it is not
         * of the form "EUR/USD"; otherwise returns at once and answers later, on a thread of its own.
         */
        void lookup(String pair, RateListener listener);
    }

    /** What a future fails with when the client reports a failure. */
    public static final class RateUnavailableException extends RuntimeException {
        public RateUnavailableException(String message) {
            super(message);
        }
    }

    private final RateClient client;

    public Rates(RateClient client) {
        this.client = Objects.requireNonNull(client);
    }

    /** The rate for {@code pair}, as a future that the client's answer completes. */
    public CompletableFuture<BigDecimal> rate(String pair) {
        // TODO: ask the client, and let its answer, whatever it is, settle the future.
        throw new IllegalStateException("TODO: rate");
    }
}
