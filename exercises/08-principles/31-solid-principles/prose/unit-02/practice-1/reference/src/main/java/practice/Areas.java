package practice;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class Areas {

    private Areas() {
    }

    /** The extension point: any shape computes its own area. */
    public interface Shape {
        double area();

        String name();
    }

    public record Circle(double radius) implements Shape {
        public Circle {
            if (radius < 0) {
                throw new IllegalArgumentException("radius must not be negative");
            }
        }

        public double area() {
            return Math.PI * radius * radius;
        }

        public String name() {
            return "Circle";
        }
    }

    public record Rectangle(double width, double height) implements Shape {
        public Rectangle {
            if (width < 0 || height < 0) {
                throw new IllegalArgumentException("dimensions must not be negative");
            }
        }

        public double area() {
            return width * height;
        }

        public String name() {
            return "Rectangle";
        }
    }

    public record Triangle(double base, double height) implements Shape {
        public Triangle {
            if (base < 0 || height < 0) {
                throw new IllegalArgumentException("dimensions must not be negative");
            }
        }

        public double area() {
            return 0.5 * base * height;
        }

        public String name() {
            return "Triangle";
        }
    }

    /** Closed for modification: works with any Shape, including ones written later. */
    public static final class AreaCalculator {
        public double totalArea(List<? extends Shape> shapes) {
            double total = 0.0;
            for (Shape shape : shapes) {
                total += shape.area();
            }
            return total;
        }

        public Map<String, Double> areaByType(List<? extends Shape> shapes) {
            Map<String, Double> byType = new TreeMap<>();
            for (Shape shape : shapes) {
                byType.merge(shape.name(), shape.area(), Double::sum);
            }
            return byType;
        }
    }
}
