package practice;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/** Monthly subscriptions: each renews one calendar month after it was paid, by the customer's calendar. */
public class Renewals {

    /** A paid subscription; zone is where the customer lives. */
    public record Subscription(String customer, ZoneId zone, Instant paidAt) {}

    /** The customers whose subscription renews at or before {@code now}, in the given order. */
    public static List<String> dueBy(List<Subscription> subscriptions, Instant now) {
        return subscriptions.stream()
                .filter(s -> !nextRenewal(s).isAfter(now))
                .map(Subscription::customer)
                .toList();
    }

    /** The line printed on the customer's receipt. */
    public static String receiptLine(Subscription s) {
        return s.customer() + ": renews on " + renewalDate(s);
    }

    /** The moment the subscription renews: one calendar month after payment, same local time. */
    static Instant nextRenewal(Subscription s) {
        // TODO: One calendar month after the payment, at the same time of day, for this customer.
        throw new IllegalStateException("TODO: nextRenewal");
    }

    /** The date of the renewal as the customer's own calendar shows it. */
    static LocalDate renewalDate(Subscription s) {
        // TODO: The date the customer will see on their own calendar when the renewal happens.
        throw new IllegalStateException("TODO: renewalDate");
    }
}
