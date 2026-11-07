package practice;

import java.util.HashMap;
import java.util.Map;

/** Quoted shipping rates, cached per shipment. */
public final class RateCache {

    /** A shipment: a carrier, and the weight in grams of each parcel by parcel id. */
    public record Shipment(String carrier, Map<String, Integer> parcels) {

        public Shipment {
            // TODO: once built, a shipment must keep its parcels exactly as they were passed in,
            // whatever happens later to the map the caller passed, or to the one parcels() returns.
            throw new IllegalStateException("TODO: Shipment's compact constructor");
        }

        /** A shipment with one more parcel. This shipment stays as it is. */
        public Shipment withParcel(String id, int grams) {
            // TODO: return a new shipment with the same carrier and parcels, plus this one.
            throw new IllegalStateException("TODO: withParcel");
        }
    }

    private final Map<Shipment, Long> rates = new HashMap<>();

    public void put(Shipment shipment, long cents) {
        rates.put(shipment, cents);
    }

    /** The cached rate in cents, or null when this shipment was never quoted. */
    public Long rateFor(Shipment shipment) {
        return rates.get(shipment);
    }

    public int size() {
        return rates.size();
    }
}
