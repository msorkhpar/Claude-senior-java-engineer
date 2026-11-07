package practice;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.InstantSource;
import java.util.Map;

/**
 * Exchange rates against the euro for card payments at checkout, from the treasury service.
 * Single-threaded: the payment worker that owns it is the only caller.
 */
public class ExchangeRates {

    /** One call to the treasury service: today's rate of every currency it quotes. */
    public interface Treasury {
        Map<String, BigDecimal> allRates();
    }

    static final Duration MAX_AGE = Duration.ofMinutes(10);

    private final Treasury treasury;
    private final InstantSource clock;

    public ExchangeRates(Treasury treasury, InstantSource clock) {
        this.treasury = treasury;
        this.clock = clock;
    }

    /** The rate to convert a payment in this currency, such as "USD". */
    public BigDecimal rate(String currency) {
        // TODO: Return the treasury's rate for this currency. A payment must never be converted
        //       at a rate fetched more than MAX_AGE ago, and the treasury accepts at most one
        //       call per MAX_AGE from this service. Read the time from clock.
        throw new IllegalStateException("TODO: rate");
    }
}
