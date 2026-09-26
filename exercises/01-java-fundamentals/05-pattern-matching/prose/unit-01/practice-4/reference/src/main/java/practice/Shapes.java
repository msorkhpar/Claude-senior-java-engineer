package practice;

import java.util.List;

public final class Shapes {

    private Shapes() {
    }

    /** A shape knows its own area. */
    public interface Shape {
        double area();
    }

    /** A circle of the given radius. */
    public record Circle(double radius) implements Shape {
        @Override
        public double area() {
            return Math.PI * radius * radius;
        }
    }

    /** A rectangle of the given width and height. */
    public record Rectangle(double width, double height) implements Shape {
        @Override
        public double area() {
            return width * height;
        }
    }

    /** Returns the total area of the shapes. */
    public static double total(List<Shape> shapes) {
        double sum = 0;
        for (Shape shape : shapes) {
            sum += shape.area();
        }
        return sum;
    }
}
