package practice;

public final class CardMask {

    private CardMask() {
    }

    /** Masks every digit but the last four, keeping other characters. */
    public static String mask(String card) {
        StringBuilder sb = new StringBuilder(card);
        int keep = 4;
        for (int i = sb.length() - 1; i >= 0; i--) {
            if (Character.isDigit(sb.charAt(i))) {
                if (keep > 0) {
                    keep--;
                } else {
                    sb.setCharAt(i, '*');
                }
            }
        }
        return sb.toString();
    }
}
