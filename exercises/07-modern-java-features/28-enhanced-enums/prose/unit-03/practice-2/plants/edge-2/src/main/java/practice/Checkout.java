package practice;

import java.util.List;

/**
 * Checkout shows the merchant's customer the cheapest way to pay. Which rules a merchant offers is
 * set per merchant: some offer only part of the standard methods, and some have negotiated rates
 * of their own, loaded from their contracts.
 */
public class Checkout {

    /** What a way of paying costs the merchant. */
    public interface FeeRule {
        long fee(long amountCents);

        String label();
    }

    /** The standard methods every merchant can offer. */
    public enum PaymentMethod implements FeeRule {
        CARD("card", 25, 240),
        BANK_TRANSFER("bank transfer", 40, 0),
        WALLET("wallet", 0, 310);

        private final String label;
        private final long fixedCents;
        private final int basisPoints;

        PaymentMethod(String label, long fixedCents, int basisPoints) {
            this.label = label;
            this.fixedCents = fixedCents;
            this.basisPoints = basisPoints;
        }

        @Override
        public long fee(long amountCents) {
            return fixedCents + amountCents * basisPoints / 10_000;
        }

        @Override
        public String label() {
            return label;
        }
    }

    /** A rate one merchant negotiated, read from its contract. */
    public record NegotiatedRate(String label, long fixedCents, int basisPoints) implements FeeRule {
        @Override
        public long fee(long amountCents) {
            return fixedCents + amountCents * basisPoints / 10_000;
        }
    }

    /**
     * The rule among {@code offered} with the lowest fee for {@code amountCents}.
     * {@code offered} is never empty.
     */
    public static FeeRule cheapest(List<? extends FeeRule> offered, long amountCents) {
        PaymentMethod best = (PaymentMethod) offered.get(0);
        for (FeeRule rule : offered) {
            PaymentMethod method = (PaymentMethod) rule;
            if (method.fee(amountCents) < best.fee(amountCents)) best = method;
        }
        return best;
    }
}
