package practice;

import java.util.Objects;

public class Point {

    private final int x;
    private final int y;

    /** Creates the point (x, y). */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Says whether o is a Point with the same coordinates. */
    @Override
    public boolean equals(Object o) {
        return o instanceof Point p && x == p.x && y == p.y;
    }

    /** Returns a hash code that agrees with equals. */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
