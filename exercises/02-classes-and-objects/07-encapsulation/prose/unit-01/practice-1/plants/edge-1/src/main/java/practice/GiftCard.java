package practice;

/**
 * A prepaid gift card. Balances are in cents. A card holds at most {@link #CAP} cents, by law,
 * and a cancelled card takes no new money.
 */
public class GiftCard {

    public static final long CAP = 50_000;

    /** Thrown when a card refuses an operation; the card is left as it was. */
    public static class RefusedException extends RuntimeException {
        public RefusedException(String reason) {
            super(reason);
        }
    }

    private final String number;
    private long balance;
    private boolean cancelled;

    public GiftCard(String number) {
        this.number = number;
    }

    public void cancel() {
        cancelled = true;
    }

    /** Adds money to this card, if the card's rules allow it. */
    public void load(long cents) {
        if (cents <= 0) throw new RefusedException(number + ": amount must be positive");
        if (cancelled) throw new RefusedException(number + ": card is cancelled");
        if (balance + cents > CAP) throw new RefusedException(number + ": would exceed the cap");
        balance += cents;
    }

    /** Takes money off this card, if it holds enough. */
    public void spend(long cents) {
        if (cents <= 0) throw new RefusedException(number + ": amount must be positive");
        if (cents > balance) throw new RefusedException(number + ": insufficient balance");
        balance -= cents;
    }

    public long balance() {
        return balance;
    }

    /**
     * Moves money from this card to the target card. Either both cards change, or, when either
     * card refuses, neither does and RefusedException is thrown.
     */
    public void transferTo(GiftCard target, long cents) {
        if (cents <= 0) throw new RefusedException(number + ": amount must be positive");
        if (cents > balance) throw new RefusedException(number + ": insufficient balance");
        target.balance += cents;
        balance -= cents;
    }
}
