package practice;

/** Checked: a card payment was declined. Carries the card's last four digits only. */
class CardDeclinedException extends Exception {

    private final String lastFour;

    CardDeclinedException(String message, String lastFour) {
        super(message);
        this.lastFour = lastFour;
    }

    String getLastFour() {
        return lastFour;
    }
}

public final class Payments {

    private Payments() {
    }

    /** "approved" within the limit, else CardDeclinedException naming only the last four digits. */
    public static String authorize(String cardNumber, long amountCents, long limitCents) throws CardDeclinedException {
        if (amountCents > limitCents) {
            String lastFour = cardNumber.substring(12);
            throw new CardDeclinedException(
                    "Card ending " + lastFour + " declined: " + amountCents + " over limit " + limitCents, lastFour);
        }
        return "approved";
    }
}
