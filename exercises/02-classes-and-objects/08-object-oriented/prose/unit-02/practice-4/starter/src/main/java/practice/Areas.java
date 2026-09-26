package practice;

import java.util.List;
import java.util.Optional;

public class Areas {

    public interface Shape {
        double calculateArea();

        default String display() {
            throw new UnsupportedOperationException("write Shape.display");
        }
    }

    public static class Circle implements Shape {
        private final double radius;

        public Circle(double radius) {
            this.radius = radius;
        }

        @Override
        public double calculateArea() {
            throw new UnsupportedOperationException("write Circle.calculateArea");
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
            throw new UnsupportedOperationException("write Rectangle.calculateArea");
        }
    }

    public record Square(double side) implements Shape {
        @Override
        public double calculateArea() {
            throw new UnsupportedOperationException("write Square.calculateArea");
        }

        @Override
        public String display() {
            throw new UnsupportedOperationException("write Square.display");
        }
    }

    public static Optional<Shape> largest(List<? extends Shape> shapes) {
        throw new UnsupportedOperationException("write largest");
    }
}
