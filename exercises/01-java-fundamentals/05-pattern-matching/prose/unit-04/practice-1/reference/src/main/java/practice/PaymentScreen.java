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
        return switch (payment) {
            case Transfer t when t.cents() > 5_000_000 -> Decision.HOLD;
            case Transfer t when t.cents() > 500_000 -> Decision.REVIEW;
            case Transfer t -> Decision.ACCEPT;
            case Card c -> {
                int score = risk.score(c);
                yield score >= 90 ? Decision.DECLINE : score >= 60 ? Decision.REVIEW : Decision.ACCEPT;
            }
            case Voucher v -> Decision.ACCEPT;
        };
    }
}
