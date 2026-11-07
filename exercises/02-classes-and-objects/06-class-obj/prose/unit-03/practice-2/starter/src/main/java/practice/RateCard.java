package practice;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Exchange rates for the shop's prices, loaded from the rate service. Every request thread of the
 * pricing service reads them through this class.
 */
public class RateCard {

    /** Where the rates come from: the rate service in production, a stand-in in tests. */
    public static final class Source {
        public static volatile Supplier<Map<String, Long>> rates =
                () -> { throw new IllegalStateException("no rate source configured"); };

        private Source() {
        }
    }

    /** What the health check reports. It needs no rates. */
    public static String version() {
        return "rate-card 3.2";
    }

    /** How many cents of the currency one euro buys. */
    public static long centsPerEuro(String currency) {
        Long cents = rates().get(currency);
        if (cents == null) {
            throw new IllegalArgumentException("no rate for " + currency);
        }
        return cents;
    }

    static Map<String, Long> rates() {
        // TODO: Return the rates, asking Source.rates for them only when they are first needed.
        //       The health check must answer while the rate service is down; a load that fails
        //       must be tried again by the next request; and the requests that arrive together
        //       at startup must not each ask the rate service.
        throw new IllegalStateException("TODO: rates");
    }
}
