package practice;

/**
 * Ferry fares. Finance owns the fare rules; the ticket machine at the quay and the new web shop
 * both sell tickets. Amounts are in cents.
 */
public class Fares {

    public record Trip(int adults, int children, boolean car, boolean peak) {}

    /** Finance: adults 12.00, children 6.00, a car 35.00. */
    static class BaseFare {
        long of(Trip t) {
            return t.adults() * 1_200L + t.children() * 600L + (t.car() ? 3_500L : 0L);
        }
    }

    /** Finance: 10% off from a threshold. */
    static class GroupDiscount {
        private final long fromCents;

        GroupDiscount(long fromCents) {
            this.fromCents = fromCents;
        }

        long apply(long cents) {
            return cents >= fromCents ? cents * 90 / 100 : cents;
        }
    }

    /** Finance: 20% more on peak sailings. */
    static class PeakSurcharge {
        long apply(long cents, boolean peak) {
            return peak ? cents * 120 / 100 : cents;
        }
    }

    /** Finance's fare for a trip, with the group discount starting at the given amount. */
    public static class FarePolicy {
        private final BaseFare base = new BaseFare();
        private final GroupDiscount discount;
        private final PeakSurcharge surcharge = new PeakSurcharge();

        public FarePolicy(long groupDiscountFromCents) {
            this.discount = new GroupDiscount(groupDiscountFromCents);
        }

        /** What a trip costs. */
        public long price(Trip t) {
            return surcharge.apply(discount.apply(base.of(t)), t.peak());
        }
    }

    /** The ticket machine at the quay. It has charged correctly for years. */
    public static class TicketMachine {
        private final FarePolicy policy;

        public TicketMachine(FarePolicy policy) {
            this.policy = policy;
        }

        public long charge(Trip t) {
            BaseFare base = new BaseFare();
            GroupDiscount discount = new GroupDiscount(10_000);
            PeakSurcharge surcharge = new PeakSurcharge();
            return surcharge.apply(discount.apply(base.of(t)), t.peak());
        }
    }

    /** The new web shop: its quote is what the customer pays at the quay. */
    public static class WebShop {
        private final FarePolicy policy;

        public WebShop(FarePolicy policy) {
            this.policy = policy;
        }

        public long quote(Trip t) {
            return policy.price(t);
        }
    }
}
