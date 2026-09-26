package practice;

public final class LenientParser {

    private LenientParser() {
    }

    public static int parseOrDefault(String text, int fallback) {
        String s = text.strip();
        if (!s.matches("[+-]?\\d+")) {
            return fallback;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
