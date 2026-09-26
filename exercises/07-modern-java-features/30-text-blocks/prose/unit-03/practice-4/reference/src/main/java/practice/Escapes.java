package practice;

public final class Escapes {

    private Escapes() {
    }

    /** Returns {@code raw} with its written escape sequences turned into characters. */
    public static String decode(String raw) {
        return raw.translateEscapes();
    }
}
