package practice;

import java.util.List;

public final class Dedupe {

    private Dedupe() {
    }

    /** Returns the points without duplicates; points with equal coordinates are the same. */
    public static List<Point> distinctPoints(List<Point> points) {
        return points.stream().distinct().toList();
    }

    /** Returns the names without duplicates, in first-seen order. */
    public static List<String> firstSeen(List<String> names) {
        return names.stream().distinct().toList();
    }
}

/** A point on a grid; a record defines equals() and hashCode() from its components. */
record Point(int x, int y) {

    @Override
    public String toString() {
        return "Point(" + x + "," + y + ")";
    }
}
