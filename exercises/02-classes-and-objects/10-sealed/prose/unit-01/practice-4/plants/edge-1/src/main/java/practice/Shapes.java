package practice;

public class Shapes {

    public abstract static sealed class Shape permits Circle, Square, Triangle {
        private final String name;

        protected Shape(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public abstract double area();
    }

    public static final class Circle extends Shape {
        private final double radius;

        public Circle(double radius) {
            super("Circle");
            this.radius = radius;
        }

        public double getRadius() {
            return radius;
        }

        @Override
        public double area() {
            return Math.PI * radius * radius;
        }
    }

    public static final class Square extends Shape {
        private final double side;

        public Square(double side) {
            super("Square");
            this.side = side;
        }

        public double getSide() {
            return side;
        }

        @Override
        public double area() {
            return side * side;
        }
    }

    public static non-sealed class Triangle extends Shape {
        private final double base;
        private final double height;

        public Triangle(double base, double height) {
            this("Triangle", base, height);
        }

        protected Triangle(String name, double base, double height) {
            super(name);
            this.base = base;
            this.height = height;
        }

        public double getBase() {
            return base;
        }

        public double getHeight() {
            return height;
        }

        @Override
        public double area() {
            return 0.5 * base * height;
        }
    }

    /** Triangle is non-sealed, so any class may extend it. */
    public static class RightTriangle extends Triangle {
        public RightTriangle(double base, double height) {
            super("Right triangle", base, height);
        }
    }

    public static String describe(Shape shape) {
        return switch (shape.getName()) {
            case "Circle" -> "A circle with radius " + ((Circle) shape).getRadius();
            case "Square" -> "A square with side length " + ((Square) shape).getSide();
            case "Triangle" -> "A triangle with base " + ((Triangle) shape).getBase()
                    + " and height " + ((Triangle) shape).getHeight();
            default -> throw new IllegalArgumentException("unknown shape " + shape.getName());
        };
    }
}
