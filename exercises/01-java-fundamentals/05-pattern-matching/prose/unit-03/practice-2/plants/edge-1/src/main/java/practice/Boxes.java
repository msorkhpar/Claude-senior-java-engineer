package practice;

public final class Boxes {

    /** A point on a grid. */
    public record Point(int x, int y) {
    }

    /** A rectangle given by two opposite corners, in either order. */
    public record Rectangle(Point topLeft, Point bottomRight) {
    }

    private Boxes() {
    }

    /** Returns the rectangle's area, or -1. */
    public static int area(Object shape) {
        if (shape instanceof Rectangle(Point(var x1, var y1), Point(var x2, var y2))) {
            return (x2 - x1) * (y2 - y1);
        }
        return -1;
    }
}
