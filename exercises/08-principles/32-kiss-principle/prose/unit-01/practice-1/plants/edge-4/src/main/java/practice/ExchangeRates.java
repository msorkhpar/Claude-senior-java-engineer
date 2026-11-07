package practice;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.HashMap;
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
    private final Map<String, BigDecimal> rates = new HashMap<>();
    private final Map<String, Instant> fetchedAt = new HashMap<>();

    public ExchangeRates(Treasury treasury, InstantSource clock) {
        this.treasury = treasury;
        this.clock = clock;
    }

    /** The rate to convert a payment in this currency, such as "USD". */
    public BigDecimal rate(String currency) {
        Instant now = clock.instant();
        Instant at = fetchedAt.get(currency);
        if (at == null || !now.isBefore(at.plus(MAX_AGE))) {
            rates.put(currency, treasury.allRates().get(currency));
            fetchedAt.put(currency, now);
        }
        return rates.get(currency);
    }
}
