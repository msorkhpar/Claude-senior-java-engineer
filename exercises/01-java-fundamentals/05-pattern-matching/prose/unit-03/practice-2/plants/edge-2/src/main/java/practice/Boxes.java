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
        if (shape instanceof Rectangle r) {
            return Math.abs(r.bottomRight().x() - r.topLeft().x()) * Math.abs(r.bottomRight().y() - r.topLeft().y());
        }
        return -1;
    }
}
