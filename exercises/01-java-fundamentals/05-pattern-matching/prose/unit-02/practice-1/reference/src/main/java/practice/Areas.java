package practice;

public final class Areas {

    /** A square with the given side. */
    public record Square(double side) {
    }

    /** A circle with the given radius. */
    public record Circle(double radius) {
    }

    /** A rectangle with the given width and height. */
    public record Rectangle(double width, double height) {
    }

    private Areas() {
    }

    /** Returns the area of a Square, Circle or Rectangle; refuses anything else. */
    public static double area(Object obj) {
        if (obj instanceof Square square) {
            return square.side() * square.side();
        } else if (obj instanceof Circle circle) {
            return Math.PI * circle.radius() * circle.radius();
        } else if (obj instanceof Rectangle rectangle) {
            return rectangle.width() * rectangle.height();
        }
        throw new IllegalArgumentException("Unknown shape: " + obj);
    }
}
