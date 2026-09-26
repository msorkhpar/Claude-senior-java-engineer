package practice;

public final class Geometry {

    public record Point(int x, int y) {}

    public record Line(Point from, Point to) {}

    public record Box(Point corner, Point opposite) {}

    private Geometry() {
    }

    /** The shape's length in grid steps, or -1 for null and for anything that is not a shape. */
    public static int gridLength(Object shape) {
        throw new UnsupportedOperationException("write gridLength");
    }
}
