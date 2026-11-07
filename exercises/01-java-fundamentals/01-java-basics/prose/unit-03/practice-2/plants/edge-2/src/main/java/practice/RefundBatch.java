package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns the refunds a partner's feed reports, in euros as doubles, into payout instructions in
 * whole cents. A negative amount is a reversal: money taken back from the customer.
 */
public class RefundBatch {

    /** One payout instruction for the bank. */
    public record Payout(String orderId, long cents) {}

    /** One line of the partner's feed. */
    public record FeedLine(String orderId, double euros) {}

    /** The payouts for every line of the feed, in feed order. */
    public List<Payout> payouts(List<FeedLine> feed) {
        List<Payout> result = new ArrayList<>();
        for (FeedLine line : feed) {
            result.add(new Payout(line.orderId(), toCents(line.euros())));
        }
        return result;
    }

    /** The amount in whole cents. */
    long toCents(double euros) {
        // TODO: Return the amount the feed means, to the nearest cent, for refunds and
        //       reversals alike. A feed line whose amount isn't a usable number must stop the
        //       batch with an IllegalArgumentException instead of becoming a payout.
        if (!Double.isFinite(euros)) throw new IllegalArgumentException("not an amount: " + euros);
        return (long) (euros * 100 + 0.5);
    }
}
