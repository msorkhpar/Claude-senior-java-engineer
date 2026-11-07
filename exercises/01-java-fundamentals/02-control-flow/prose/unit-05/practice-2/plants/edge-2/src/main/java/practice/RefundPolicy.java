package practice;

/** How long a customer has to send an order back, by the reason they give. */
public final class RefundPolicy {

    /**
     * The reasons the returns service offers. It is generated from the returns service's schema,
     * which gains a reason a few times a year: never edit it here.
     */
    public enum Reason { DAMAGED, WRONG_ITEM, NOT_AS_DESCRIBED, LATE_DELIVERY, CHANGED_MIND }

    private RefundPolicy() {
    }

    /** The number of days after delivery in which a return for this reason is accepted. */
    public static int windowDays(Reason reason) {
        try {
            return switch (reason) {
                case DAMAGED, WRONG_ITEM -> 60;
                case NOT_AS_DESCRIBED, LATE_DELIVERY -> 30;
                case CHANGED_MIND -> 14;
            };
        } catch (MatchException unknownReason) {
            return 14;
        }
    }
}
