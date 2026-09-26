package practice;

public final class Quadrants {

    /** A point on a grid. */
    public record Point(int x, int y) {
    }

    private Quadrants() {
    }

    /** Returns the point's quadrant, 0 on an axis, or -1 for anything else. */
    public static int quadrant(Object obj) {
        if (obj instanceof Point(int x, int y)) {
            if (x == 0 || y == 0) {
                return 0;
            }
            if (x > 0) {
                return y > 0 ? 1 : 4;
            }
            return y > 0 ? 2 : 3;
        }
        return -1;
    }
}
