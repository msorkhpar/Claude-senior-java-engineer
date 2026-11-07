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
        // TODO: Damaged and wrong items get 60 days; not as described and late deliveries get 30;
        //       a change of mind gets 14. When the returns service adds a reason, no customer
        //       may get a window that nobody here decided on.
        throw new IllegalStateException("TODO: windowDays");
    }
}
