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
        return switch (parcel) {
            case Letter l -> l.grams() <= 100 ? 140 : 290;
            case Box b -> 650 + 120 * ((b.grams() + 999) / 1000) + (b.fragile() ? 400 : 0);
            case Tube t -> t.lengthCm() > 90 ? 1500 : 980;
        };
    }
}
