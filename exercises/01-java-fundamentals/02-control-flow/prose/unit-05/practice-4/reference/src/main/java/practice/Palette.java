package practice;

public final class Palette {

    /** The colours of a palette. */
    public enum Color { RED, ORANGE, YELLOW, GREEN, BLUE, VIOLET }

    private Palette() {
    }

    /** Returns the shade of a colour, or "None" for null. */
    public static String shade(Color color) {
        return switch (color) {
            case null -> "None";
            case RED, ORANGE, YELLOW -> "Warm";
            case GREEN, BLUE, VIOLET -> "Cool";
        };
    }
}
