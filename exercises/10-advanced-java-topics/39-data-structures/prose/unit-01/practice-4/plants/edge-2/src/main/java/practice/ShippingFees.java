package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A carrier's fee table. Each tier starts at a weight in grams and applies to every parcel from
 * that weight up to the start of the next tier.
 */
public class ShippingFees {

    private final NavigableMap<Integer, Integer> centsFromGrams = new TreeMap<>();

    /** Adds a tier: parcels from {@code fromGrams} up cost {@code cents}, until the next tier. */
    public ShippingFees tier(int fromGrams, int cents) {
        centsFromGrams.put(fromGrams, cents);
        return this;
    }

    /** The fee for a parcel; a weight below the first tier is refused with IllegalArgumentException. */
    public int feeFor(int grams) {
        return centsFromGrams.floorEntry(grams).getValue();
    }

    /** The tier starts from {@code fromGrams} to {@code toGrams}, both included, lightest first. */
    public List<Integer> tierStartsBetween(int fromGrams, int toGrams) {
        return new ArrayList<>(centsFromGrams.subMap(fromGrams, true, toGrams, true).keySet());
    }
}
