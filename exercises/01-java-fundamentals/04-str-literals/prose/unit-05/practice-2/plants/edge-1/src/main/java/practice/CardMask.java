package practice;

public final class CardMask {

    private CardMask() {
    }

    /** Masks every digit but the last four, keeping other characters. */
    public static String mask(String card) {
        StringBuilder sb = new StringBuilder(card);
        for (int i = 0; i < sb.length() - 4; i++) {
            sb.setCharAt(i, '*');
        }
        return sb.toString();
    }
}
