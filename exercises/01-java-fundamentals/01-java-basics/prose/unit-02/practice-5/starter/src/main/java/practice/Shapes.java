package practice;

public final class Shapes {

    private Shapes() {
    }

    public abstract static class Shape {
        public abstract double area();
    }

    public static class Circle extends Shape {
        private final double radius;

        public Circle(double radius) {
            this.radius = radius;
        }

        public double radius() {
            return radius;
        }

        @Override
        public double area() {
            throw new UnsupportedOperationException("write Circle.area");
        }
    }

    public static class Square extends Shape {
        private final double side;

        public Square(double side) {
            this.side = side;
        }

        public double side() {
            return side;
        }

        @Override
        public double area() {
            throw new UnsupportedOperationException("write Square.area");
        }
    }

    public static class Triangle extends Shape {
        private final double base;
        private final double height;

        public Triangle(double base, double height) {
            this.base = base;
            this.height = height;
        }

        @Override
        public double area() {
            throw new UnsupportedOperationException("write Triangle.area");
        }
    }

    public static String describe(Shape shape) {
        throw new UnsupportedOperationException("write describe");
    }
}
