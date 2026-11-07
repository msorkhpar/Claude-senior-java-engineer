package practice;

/** Decides what happens to each incoming payment before it is booked. */
public final class PaymentScreen {

    /** A payment as the gateway delivers it. Amounts are in cents. */
    public sealed interface Payment permits Card, Transfer, Voucher {
    }

    public record Card(String customer, long cents) implements Payment {
    }

    public record Transfer(String customer, long cents) implements Payment {
    }

    public record Voucher(String code, long cents) implements Payment {
    }

    public enum Decision { ACCEPT, REVIEW, HOLD, DECLINE }

    /** The card network's fraud score for a card payment, from 0 to 100. Every call is billed. */
    public interface RiskScores {
        int score(Card card);
    }

    private final RiskScores risk;

    public PaymentScreen(RiskScores risk) {
        this.risk = risk;
    }

    /** The decision for one payment. */
    public Decision screen(Payment payment) {
        // TODO: Decide every payment by the rules in the statement. Large transfers must reach the
        //       decision meant for them, and a card payment's score is a billed call.
        throw new IllegalStateException("TODO: screen");
    }
}
