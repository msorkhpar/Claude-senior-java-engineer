package practice;

/** Shipping quotes for the checkout page. */
public class ShippingQuotes {

    public record Parcel(String id, int grams, String zone) {}

    /** The carrier's rate table. In production each lookup is a remote call of up to 300 ms. */
    public interface RateTable {
        long centsPerKg(String zone);
    }

    public interface ShippingPolicy {
        long quoteCents(Parcel parcel);
    }

    /**
     * The version in production. Its quotes are right in every unit test, and under load some
     * customers are shown the price of another customer's parcel.
     */
    public static final class ByWeightV1 implements ShippingPolicy {
        private final RateTable rates;
        private long kg;
        private String zone;

        public ByWeightV1(RateTable rates) {
            this.rates = rates;
        }

        @Override
        public long quoteCents(Parcel parcel) {
            weigh(parcel);
            long rate = rates.centsPerKg(zone);
            return withSurcharge(kg * rate);
        }

        private void weigh(Parcel parcel) {
            kg = Math.max(1, (parcel.grams() + 999) / 1000);
            zone = parcel.zone();
        }

        private long withSurcharge(long cents) {
            return zone.equals("island") ? cents * 120 / 100 : cents;
        }
    }

    /**
     * Its replacement: the same prices, from one instance that serves every request thread.
     * Billable kilograms are the grams rounded up, at least 1; the price is kilograms times the
     * zone's rate, plus 20 % for the zone "island".
     */
    public static final class ByWeight implements ShippingPolicy {
        private final RateTable rates;

        public ByWeight(RateTable rates) {
            this.rates = rates;
        }

        @Override
        public long quoteCents(Parcel parcel) {
            long kg = Math.max(1, (parcel.grams() + 999) / 1000);
            long cents = kg * rates.centsPerKg(parcel.zone());
            return parcel.zone().equals("island") ? cents * 120 / 100 : cents;
        }
    }
}
