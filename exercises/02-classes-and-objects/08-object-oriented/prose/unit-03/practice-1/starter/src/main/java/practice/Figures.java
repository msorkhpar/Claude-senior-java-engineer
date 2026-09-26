package practice;

public class Figures {

    public abstract static class Shape {
        public abstract double area();

        public abstract double perimeter();
    }

    public static class Circle extends Shape {
        public Circle(double radius) {
            throw new UnsupportedOperationException("write the Circle constructor");
        }

        @Override
        public double area() {
            throw new UnsupportedOperationException("write Circle.area");
        }

        @Override
        public double perimeter() {
            throw new UnsupportedOperationException("write Circle.perimeter");
        }
    }

    public static class Square extends Shape {
        public Square(double side) {
            throw new UnsupportedOperationException("write the Square constructor");
        }

        @Override
        public double area() {
            throw new UnsupportedOperationException("write Square.area");
        }

        @Override
        public double perimeter() {
            throw new UnsupportedOperationException("write Square.perimeter");
        }
    }

    public static class Triangle extends Shape {
        public Triangle(double a, double b, double c) {
            throw new UnsupportedOperationException("write the Triangle constructor");
        }

        @Override
        public double area() {
            throw new UnsupportedOperationException("write Triangle.area");
        }

        @Override
        public double perimeter() {
            throw new UnsupportedOperationException("write Triangle.perimeter");
        }
    }
}
