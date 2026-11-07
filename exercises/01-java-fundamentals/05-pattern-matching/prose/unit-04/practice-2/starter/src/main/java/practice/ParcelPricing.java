package practice;

/** Quotes the price of sending a parcel with the carrier. */
public final class ParcelPricing {

    /**
     * The kinds of parcel the carrier accepts. This hierarchy is generated from the carrier's
     * library, which gains a kind now and then: never edit it here.
     */
    public sealed interface Parcel permits Letter, Box, Tube {
    }

    public record Letter(int grams) implements Parcel {
    }

    public record Box(int grams, boolean fragile) implements Parcel {
    }

    public record Tube(int lengthCm) implements Parcel {
    }

    private ParcelPricing() {
    }

    /** The price in cents of sending this parcel. */
    public static int priceCents(Parcel parcel) {
        // TODO: Price each kind of parcel as the statement's table says. When the carrier adds a
        //       kind, this method must not quote it before someone here has decided its price.
        throw new IllegalStateException("TODO: priceCents");
    }
}
