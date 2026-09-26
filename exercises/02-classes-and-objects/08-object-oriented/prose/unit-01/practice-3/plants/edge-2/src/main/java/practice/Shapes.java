package practice;

import java.util.List;

public class Shapes {

    public abstract static class Shape {
        protected final String color;

        public Shape(String color) {
            this.color = color;
        }

        public abstract double calculateArea();

        public String displayColor() {
            return "The shape color is " + color;
        }

        @Override
        public String toString() {
            return "Shape{color='" + color + "'}";
        }
    }

    public static class Circle extends Shape {
        private final double radius;

        public Circle(String color, double radius) {
            super(color);
            if (radius < 0) {
                throw new IllegalArgumentException("a radius cannot be negative");
            }
            this.radius = radius;
        }

        @Override
        public double calculateArea() {
            return Math.PI * radius * radius;
        }

        @Override
        public String toString() {
            return "Circle{color='" + color + "', radius=" + radius + "}";
        }
    }

    public static class Rectangle extends Shape {
        private final double width;
        private final double height;

        public Rectangle(String color, double width, double height) {
            super(color);
            if (width < 0 || height < 0) {
                throw new IllegalArgumentException("a side cannot be negative");
            }
            this.width = width;
            this.height = height;
        }

        @Override
        public double calculateArea() {
            return width * height;
        }

        @Override
        public String toString() {
            return "Rectangle{color='" + color + "', width=" + width + ", height=" + height + "}";
        }
    }

    /** The sum of every shape's area; 0.0 for no shapes. */
    public static double totalArea(List<? extends Shape> shapes) {
        return shapes.stream().map(Shape::calculateArea).reduce(Double::sum).orElseThrow();
    }
}
