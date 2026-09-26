package practice;

public final class Geometry {

    public record Point(int x, int y) {}

    public record Line(Point from, Point to) {}

    public record Box(Point corner, Point opposite) {}

    private Geometry() {
    }

    /** The shape's length in grid steps, or -1 for null and for anything that is not a shape. */
    public static int gridLength(Object shape) {
        return switch (shape) {
            case null -> -1;
            case Point p -> 0;
            case Line(Point(var x1, var y1), Point(var x2, var y2)) ->
                    Math.abs(x2 - x1) + Math.abs(y2 - y1);
            case Box(Point(var x1, var y1), Point(var x2, var y2)) ->
                    2 * (Math.abs(x2 - x1) + Math.abs(y2 - y1));
            default -> -1;
        };
    }
}
