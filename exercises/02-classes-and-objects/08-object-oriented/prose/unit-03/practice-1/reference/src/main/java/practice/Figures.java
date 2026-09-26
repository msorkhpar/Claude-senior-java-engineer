package practice;

public class Figures {

    public abstract static class Shape {
        public abstract double area();

        public abstract double perimeter();
    }

    public static class Circle extends Shape {
        private final double radius;

        public Circle(double radius) {
            if (radius <= 0) {
                throw new IllegalArgumentException("a radius must be positive");
            }
            this.radius = radius;
        }

        @Override
        public double area() {
            return Math.PI * radius * radius;
        }

        @Override
        public double perimeter() {
            return 2 * Math.PI * radius;
        }
    }

    public static class Square extends Shape {
        private final double side;

        public Square(double side) {
            if (side <= 0) {
                throw new IllegalArgumentException("a side must be positive");
            }
            this.side = side;
        }

        @Override
        public double area() {
            return side * side;
        }

        @Override
        public double perimeter() {
            return 4 * side;
        }
    }

    public static class Triangle extends Shape {
        private final double a;
        private final double b;
        private final double c;

        public Triangle(double a, double b, double c) {
            if (a + b <= c || a + c <= b || b + c <= a) {
                throw new IllegalArgumentException("these sides make no triangle");
            }
            this.a = a;
            this.b = b;
            this.c = c;
        }

        @Override
        public double area() {
            double s = perimeter() / 2;
            return Math.sqrt(s * (s - a) * (s - b) * (s - c));
        }

        @Override
        public double perimeter() {
            return a + b + c;
        }
    }
}
