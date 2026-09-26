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
            if (shape instanceof Circle) {
                Circle c = (Circle) shape;
                sum += Math.PI * c.radius() * c.radius();
            } else if (shape instanceof Rectangle) {
                Rectangle r = (Rectangle) shape;
                sum += r.width() * r.height();
            }
        }
        return sum;
    }
}
