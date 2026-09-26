package practice;

public final class Shift {

    private Shift() {
    }

    /** Returns {@code text} with up to {@code n} leading whitespace characters removed from each line. */
    public static String left(String text, int n) {
        String shifted = text.indent(-n);
        return text.endsWith("\n") ? shifted : shifted.substring(0, shifted.length() - 1);
    }
}
