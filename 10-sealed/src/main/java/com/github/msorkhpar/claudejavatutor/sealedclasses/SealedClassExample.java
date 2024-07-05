package com.github.msorkhpar.claudejavatutor.trycatch;

public class SealedClassExample {

    public sealed interface Shape permits Circle, Square, Triangle {
        double area();
    }

    public final class Circle implements Shape {
        private final double radius;

        public Circle(double radius) {
            this.radius = radius;
        }

        @Override
        public double area() {
            return Math.PI * radius * radius;
        }
    }

    public final class Square implements Shape {
        private final double side;

        public Square(double side) {
            this.side = side;
        }

        @Override
        public double area() {
            return side * side;
        }
    }

    public non-sealed class Triangle implements Shape {
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

    public double calculateArea(Shape shape) {
        return switch (shape) {
            case Circle c -> c.area();
            case Square s -> s.area();
            case Triangle t -> t.area();
        };
    }
}