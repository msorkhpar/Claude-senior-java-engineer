package practice;

public final class Shapes {

    /** A point on a grid. */
    public record Point(int x, int y) {
    }

    /** A rectangle given by two corners. */
    public record Rectangle(Point topLeft, Point bottomRight) {
    }

    /** A circle given by its centre and radius. */
    public record Circle(Point center, int radius) {
    }

    private Shapes() {
    }

    /** Describes a rectangle or a circle. */
    public static String describe(Object shape) {
        return switch (shape) {
            case null -> "Null shape";
            case Rectangle(Point(var x1, var y1), Point(var x2, var y2)) ->
                    String.format("Rectangle from (%d,%d) to (%d,%d)", x1, y1, x2, y2);
            case Circle(Point(var x, var y), var r) -> String.format("Circle at (%d,%d) with radius %d", x, y, r);
            default -> "Unknown shape";
        };
    }
}
