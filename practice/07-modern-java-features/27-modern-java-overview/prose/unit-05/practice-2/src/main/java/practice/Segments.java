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
        throw new UnsupportedOperationException("write describe");
    }
}
