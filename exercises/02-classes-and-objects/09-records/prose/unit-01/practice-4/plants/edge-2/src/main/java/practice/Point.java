package practice;

public record Point(int x, int y) {

    /** The point (0, 0). */
    public static final Point ORIGIN = new Point(0, 0);

    /** Read "x,y"; anything else is refused with IllegalArgumentException. */
    public static Point parse(String text) {
        String[] parts = text.split(",", -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Expected x,y");
        }
        try {
            return new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a whole number", e);
        }
    }

    /** The straight-line distance from ORIGIN, with no int overflow. */
    public double distanceFromOrigin() {
        long dx = (long) x - ORIGIN.x;
        long dy = (long) y - ORIGIN.y;
        return Math.sqrt((double) (dx * dx) + (double) (dy * dy));
    }
}
