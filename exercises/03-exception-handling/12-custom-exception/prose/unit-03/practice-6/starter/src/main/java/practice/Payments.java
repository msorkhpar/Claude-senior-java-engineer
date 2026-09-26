package practice;

/** Checked: a card payment was declined. Carries the card's last four digits only. */
class CardDeclinedException extends Exception {

    CardDeclinedException(String message, String lastFour) {
        throw new UnsupportedOperationException("write CardDeclinedException(String, String)");
    }

    String getLastFour() {
        throw new UnsupportedOperationException("write getLastFour");
    }
}

public final class Payments {

    private Payments() {
    }

    /** "approved" within the limit, else CardDeclinedException naming only the last four digits. */
    public static String authorize(String cardNumber, long amountCents, long limitCents) throws CardDeclinedException {
        throw new UnsupportedOperationException("write authorize");
    }
}
