package practice;

/**
 * Decides which parcels may ship next-day and what a parcel costs. The team declared its own
 * Rule and Adjustment interfaces so that the checkout code reads in the shop's own words.
 */
public class Checkout {

    public record Parcel(String country, String postcode, int grams) {}

    /** One acceptance rule for a parcel. */
    @FunctionalInterface
    public interface Rule {
        boolean accepts(Parcel parcel);

        /** A rule that accepts a parcel only when this rule and {@code other} both accept it. */
        default Rule and(Rule other) {
            // TODO: The next-day rule is built by chaining rules with and(). Return a rule that
            //       accepts a parcel only when both rules do, and that behaves well when an
            //       earlier rule in the chain has already turned the parcel down.
            throw new IllegalStateException("TODO: and");
        }

        /** A rule that accepts a parcel when this rule or {@code other} accepts it. */
        default Rule or(Rule other) {
            // TODO: Return a rule that accepts a parcel when either rule does, asking no more
            //       than it needs to.
            throw new IllegalStateException("TODO: or");
        }
    }

    /** One step that changes a price, in cents. */
    @FunctionalInterface
    public interface Adjustment {
        long apply(long cents);

        /** This step, and then {@code next} on its result. */
        default Adjustment then(Adjustment next) {
            // TODO: Return one step that does this step's work and then next's, in that order.
            throw new IllegalStateException("TODO: then");
        }
    }

    static Rule hasPostcode() {
        return parcel -> parcel.postcode() != null;
    }

    static Rule postcodeStartsWith(String prefix) {
        return parcel -> parcel.postcode().startsWith(prefix);
    }

    static Rule atMostGrams(int grams) {
        return parcel -> parcel.grams() <= grams;
    }

    /** Next-day delivery: a known postcode in the courier's zone, and light enough to carry. */
    public static Rule nextDay() {
        return hasPostcode().and(postcodeStartsWith("NL-")).and(atMostGrams(5_000));
    }

    /** Members get a percentage off the goods; the courier fee is added after the discount. */
    public static Adjustment price(int percentOff, long courierFee) {
        Adjustment discount = cents -> cents * (100 - percentOff) / 100;
        Adjustment fee = cents -> cents + courierFee;
        return discount.then(fee);
    }
}
