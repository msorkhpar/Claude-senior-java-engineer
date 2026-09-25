package practice;

public final class Guards {

    private Guards() {
    }

    /** Returns whether {@code text} starts with a digit; null and empty text do not. */
    public static boolean startsWithDigit(String text) {
        return text != null && !text.isEmpty() && Character.isDigit(text.charAt(0));
    }

    /** Returns whether exactly one of {@code a} and {@code b} is true. */
    public static boolean exactlyOne(boolean a, boolean b) {
        return a || b;
    }
}
