package practice;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Areas {

    public interface Shape {
        double calculateArea();

        default String display() {
            return "This is a shape with area: " + String.format("%.2f", calculateArea());
        }
    }

    public static class Circle implements Shape {
        private final double radius;

        public Circle(double radius) {
            this.radius = radius;
        }

        @Override
        public double calculateArea() {
            return Math.PI * radius * radius;
        }
    }

    public static class Rectangle implements Shape {
        private final double length;
        private final double width;

        public Rectangle(double length, double width) {
            this.length = length;
            this.width = width;
        }

        @Override
        public double calculateArea() {
            return length * width;
        }
    }

    public record Square(double side) implements Shape {
        @Override
        public double calculateArea() {
            return side * side;
        }

        @Override
        public String display() {
            return "A square with side " + String.format("%.2f", side);
        }
    }

    public static Optional<Shape> largest(List<? extends Shape> shapes) {
        return shapes.stream().map(Shape.class::cast).max(Comparator.comparingDouble(Shape::calculateArea));
    }
}
