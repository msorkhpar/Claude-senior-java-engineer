package practice;

public final class Parsing {

    private Parsing() {
    }

    /** Returns the int {@code text} spells, ignoring surrounding spaces, or null when it spells none. */
    public static Integer parseOrNull(String text) {
        if (text == null) {
            return null;
        }
        return Integer.parseInt(text.strip());
    }

    /** Returns the int {@code text} spells, or {@code fallback} when it spells none. */
    public static int parseOrDefault(String text, int fallback) {
        Integer parsed = parseOrNull(text);
        return parsed != null ? parsed : fallback;
    }
}
