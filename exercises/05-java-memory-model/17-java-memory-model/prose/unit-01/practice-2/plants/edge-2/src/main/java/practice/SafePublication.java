package practice;

import java.util.Collections;
import java.util.List;

public final class SafePublication {

    private SafePublication() {
    }

    public static final class Point {
        private final int x;
        private final int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public Point moved(int dx, int dy) {
            return new Point(x + dx, y + dy);
        }
    }

    public static final class Polygon {
        private final List<Point> points;

        public Polygon(List<Point> points) {
            this.points = points;
        }

        public List<Point> points() {
            return Collections.unmodifiableList(points);
        }
    }

    public static final class Publisher {
        private volatile Polygon current;

        public void publish(Polygon polygon) {
            current = polygon;
        }

        public Polygon current() {
            return current;
        }
    }
}
