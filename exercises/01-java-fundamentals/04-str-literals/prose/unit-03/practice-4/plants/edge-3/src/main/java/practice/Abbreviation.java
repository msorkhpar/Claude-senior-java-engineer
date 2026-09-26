package practice;

public final class Abbreviation {

    private Abbreviation() {
    }

    /** Shortens text to at most max characters, ending in "...". */
    public static String abbreviate(String text, int max) {
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max - 3) + "...";
    }
}
