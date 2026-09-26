package practice;

import java.util.List;

public final class SafePublication {

    private SafePublication() {
    }

    public static final class Point {

        public Point(int x, int y) {
            throw new UnsupportedOperationException("write Point");
        }

        public int x() {
            throw new UnsupportedOperationException("write x");
        }

        public int y() {
            throw new UnsupportedOperationException("write y");
        }

        public Point moved(int dx, int dy) {
            throw new UnsupportedOperationException("write moved");
        }
    }

    public static final class Polygon {

        public Polygon(List<Point> points) {
            throw new UnsupportedOperationException("write Polygon");
        }

        public List<Point> points() {
            throw new UnsupportedOperationException("write points");
        }
    }

    public static final class Publisher {

        public void publish(Polygon polygon) {
            throw new UnsupportedOperationException("write publish");
        }

        public Polygon current() {
            throw new UnsupportedOperationException("write current");
        }
    }
}
