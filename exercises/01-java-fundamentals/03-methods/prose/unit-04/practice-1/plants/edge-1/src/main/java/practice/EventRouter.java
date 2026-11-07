package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * Takes events off the billing queue and hands each one to the handler for its kind. The queue
 * delivers every event as an Object, in the order it was published.
 */
public class EventRouter {

    /** A customer paid. */
    public static class Payment {
        final String id;
        final long cents;

        public Payment(String id, long cents) {
            this.id = id;
            this.cents = cents;
        }
    }

    /** Money went back to a customer. A refund is booked as a negative payment, so it is one. */
    public static class Refund extends Payment {
        public Refund(String id, long cents) {
            super(id, cents);
        }
    }

    private final List<String> journal = new ArrayList<>();

    /** What the handlers booked, one line per event. */
    public List<String> journal() {
        return journal;
    }

    /** Routes every event of a batch, in order. */
    public void routeAll(List<Object> batch) {
        for (Object event : batch) {
            route(event);
        }
    }

    void handle(Payment payment) {
        journal.add("credit " + payment.id + " " + payment.cents);
    }

    void handle(Refund refund) {
        journal.add("debit " + refund.id + " " + refund.cents);
    }

    void handle(Object unknown) {
        journal.add("parked " + unknown);
    }

    /** Hands one event to the handler for its kind; anything else is parked. */
    void route(Object event) {
        handle(event);
    }
}
