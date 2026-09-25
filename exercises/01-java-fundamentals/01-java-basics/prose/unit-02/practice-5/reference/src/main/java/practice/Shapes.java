package practice;

public final class Shapes {

    private Shapes() {
    }

    public abstract static sealed class Shape permits Circle, Square, Triangle {
        public abstract double area();
    }

    public static final class Circle extends Shape {
        private final double radius;

        public Circle(double radius) {
            this.radius = radius;
        }

        public double radius() {
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
            this.side = side;
        }

        public double side() {
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
            this.base = base;
            this.height = height;
        }

        @Override
        public double area() {
            return 0.5 * base * height;
        }
    }

    public static String describe(Shape shape) {
        return switch (shape) {
            case Circle c -> "circle of radius " + c.radius();
            case Square s -> "square of side " + s.side();
            case Triangle t -> "triangle of area " + t.area();
        };
    }
}
