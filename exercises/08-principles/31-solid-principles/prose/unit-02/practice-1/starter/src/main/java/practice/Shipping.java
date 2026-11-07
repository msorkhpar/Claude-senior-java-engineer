package practice;

import java.time.LocalDate;
import java.util.List;

public final class Shipping {

    private Shipping() {
    }

    /** A kind of parcel. Other teams add kinds of their own, in their own modules. */
    public interface Parcel {
        /** Postage before any insurance, in cents. */
        long baseCents();

        /** Days from handing the parcel over to delivery. */
        int transitDays();
    }

    /** A parcel whose declared value is insured in transit. */
    public interface Insured extends Parcel {
        long declaredValueCents();
    }

    /** Letters go by post: 2 days. */
    public record Letter(int grams) implements Parcel {
        public long baseCents() {
            return 85 + (grams / 20) * 10L;
        }

        public int transitDays() {
            return 2;
        }
    }

    /** Boxes go by road: 4 days, always insured. */
    public record Box(int grams, long declaredValueCents) implements Insured {
        public long baseCents() {
            return 450 + (grams / 1000) * 120L;
        }

        public int transitDays() {
            return 4;
        }
    }

    public static final class Manifest {

        /**
         * The postage for everything on the manifest: each parcel's base postage, plus 2 % of the
         * declared value of each insured parcel, rounded up to the next cent.
         */
        long totalCents(List<? extends Parcel> parcels) {
            throw new IllegalStateException("TODO: total postage, insurance included");
        }

        /** The day the last parcel on the manifest is delivered, when all leave on {@code shipped}. */
        LocalDate deliveredBy(LocalDate shipped, List<? extends Parcel> parcels) {
            throw new IllegalStateException("TODO: delivery date of the last parcel");
        }
    }
}
