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
        throw new UnsupportedOperationException("write describe");
    }
}
