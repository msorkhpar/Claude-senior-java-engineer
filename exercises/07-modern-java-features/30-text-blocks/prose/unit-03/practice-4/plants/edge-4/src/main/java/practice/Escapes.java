package practice;

public final class Escapes {

    private Escapes() {
    }

    /** Returns {@code raw} with its written escape sequences turned into characters. */
    public static String decode(String raw) {
        try {
            return raw.translateEscapes();
        } catch (IllegalArgumentException e) {
            return raw; // leave text with an unknown escape as it was
        }
    }
}
