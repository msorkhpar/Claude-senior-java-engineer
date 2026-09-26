package practice;

public final class Abbreviation {

    private Abbreviation() {
    }

    /** Shortens text to at most max characters, ending in "...". */
    public static String abbreviate(String text, int max) {
        if (max < 3) {
            throw new IllegalArgumentException("max must be at least 3");
        }
        if (text.length() == max) {
            return text;
        }
        return text.substring(0, max - 3) + "...";
    }
}
