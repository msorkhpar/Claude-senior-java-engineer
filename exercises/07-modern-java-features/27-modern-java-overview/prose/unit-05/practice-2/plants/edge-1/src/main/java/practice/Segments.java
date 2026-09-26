package practice;

public final class Segments {

    private Segments() {
    }

    public record Point(int x, int y) {
    }

    public record Line(Point start, Point end) {
    }

    /** A description of a point or a line, from the first case that matches. */
    public static String describe(Object obj) {
        return switch (obj) {
            case Line(Point(int x1, int y1), Point(int x2, int y2))
                    when x1 == x2 -> "vertical at x=" + x1;
            case Line(Point(int x1, int y1), Point(int x2, int y2))
                    when x1 == x2 && y1 == y2 -> "degenerate at (" + x1 + ", " + y1 + ")";
            case Line(Point(int x1, int y1), Point(int x2, int y2))
                    when y1 == y2 -> "horizontal at y=" + y1;
            case Line(Point(int x1, int y1), Point(int x2, int y2)) ->
                    "from (" + x1 + ", " + y1 + ") to (" + x2 + ", " + y2 + ")";
            case Point(int x, int y) when x == 0 && y == 0 -> "origin";
            case Point(int x, int y) -> "point (" + x + ", " + y + ")";
            default -> "not a shape";
        };
    }
}
