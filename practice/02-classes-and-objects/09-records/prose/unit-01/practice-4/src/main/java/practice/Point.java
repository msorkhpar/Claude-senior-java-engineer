package practice;

public record Point(int x, int y) {

    /** The point (0, 0). */
    public static final Point ORIGIN = null;

    /** Read "x,y"; anything else is refused with IllegalArgumentException. */
    public static Point parse(String text) {
        throw new UnsupportedOperationException("write parse");
    }

    /** The straight-line distance from ORIGIN, with no int overflow. */
    public double distanceFromOrigin() {
        throw new UnsupportedOperationException("write distanceFromOrigin");
    }
}
