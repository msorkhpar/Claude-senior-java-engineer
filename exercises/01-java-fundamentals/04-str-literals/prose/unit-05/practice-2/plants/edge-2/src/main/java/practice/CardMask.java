package practice;

public final class CardMask {

    private CardMask() {
    }

    /** Masks every digit but the last four, keeping other characters. */
    public static String mask(String card) {
        int hidden = card.length() - 4;
        return new StringBuilder(card).replace(0, hidden, "*".repeat(hidden)).toString();
    }
}
