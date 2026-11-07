package practice;

import java.io.IOException;
import java.math.BigDecimal;

/** Exchange rates for the checkout: from the primary provider, or from a mirror when it fails. */
public class RateClient {

    /** One provider of rates, such as "GBP-JPY". */
    public interface Source {
        BigDecimal rate(String pair) throws IOException;
    }

    /** Neither provider could give a rate. */
    public static class RatesUnavailableException extends Exception {
        public RatesUnavailableException(String message) {
            super(message);
        }

        public RatesUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final Source primary;
    private final Source mirror;

    public RateClient(Source primary, Source mirror) {
        this.primary = primary;
        this.mirror = mirror;
    }

    /** The rate for the pair, from the primary if it answers. */
    public BigDecimal rate(String pair) throws RatesUnavailableException {
        try {
            return primary.rate(pair);
        } catch (IOException fromPrimary) {
            return fromMirror(pair, fromPrimary);
        }
    }

    /** Asks the mirror, after the primary failed with {@code fromPrimary}. */
    BigDecimal fromMirror(String pair, IOException fromPrimary) throws RatesUnavailableException {
        try {
            return mirror.rate(pair);
        } catch (IOException fromMirror) {
            throw new RatesUnavailableException("no rate for " + pair + " (mirror: " + fromMirror.getMessage() + ")", fromPrimary);
        }
    }
}
