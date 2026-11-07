package practice;

import java.util.HashMap;
import java.util.Map;

/** Rate cards of the carriers the shop ships with. */
public class Shipping {

    /** What a carrier charges to ship one parcel to a zone. */
    public interface RateCard {
        /** The price, in minor units of {@link #currency()}, of one parcel to {@code zone}. */
        long rateCents(String zone);

        /** The ISO 4217 code of the currency the rates are in, such as "EUR". */
        String currency();
    }

    /**
     * Base class for carriers whose rates come from a slow lookup (a carrier's API, a file).
     * Subclasses say how to look one rate up; this class remembers what it looked up.
     */
    public abstract static class CachedRateCard implements RateCard {
        private final String currency;
        private final Map<String, Long> known = new HashMap<>();

        protected CachedRateCard(String currency) {
            if (currency == null || !currency.matches("[A-Z]{3}")) {
                throw new IllegalArgumentException("not an ISO 4217 code: " + currency);
            }
            this.currency = currency;
        }

        /** Looks one rate up; slow, so called at most once per zone for each card. */
        protected abstract long lookUp(String zone);

        @Override
        public final long rateCents(String zone) {
            return known.computeIfAbsent(zone, this::lookUp);
        }

        @Override
        public String currency() {
            return currency;
        }
    }

    /** Parcel Post: its rates come from a table it is given. */
    public static class ParcelPost extends CachedRateCard {
        private final Map<String, Long> table;

        public ParcelPost(String currency, Map<String, Long> table) {
            super(currency);
            this.table = Map.copyOf(table);
        }

        @Override
        protected long lookUp(String zone) {
            Long rate = table.get(zone);
            if (rate == null) throw new IllegalArgumentException("Parcel Post doesn't ship to " + zone);
            return rate;
        }
    }
}
