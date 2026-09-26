package practice;

public final class Quadrants {

    /** A point on a grid. */
    public record Point(int x, int y) {
    }

    private Quadrants() {
    }

    /** Returns the point's quadrant, 0 on an axis, or -1 for anything else. */
    public static int quadrant(Object obj) {
        throw new UnsupportedOperationException("write quadrant");
    }
}
