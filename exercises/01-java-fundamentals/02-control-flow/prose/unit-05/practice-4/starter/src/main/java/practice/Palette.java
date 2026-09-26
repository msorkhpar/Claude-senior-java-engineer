package practice;

public final class Palette {

    /** The colours of a palette. */
    public enum Color { RED, ORANGE, YELLOW, GREEN, BLUE, VIOLET }

    private Palette() {
    }

    /** Returns the shade of a colour, or "None" for null. */
    public static String shade(Color color) {
        throw new UnsupportedOperationException("write shade");
    }
}
