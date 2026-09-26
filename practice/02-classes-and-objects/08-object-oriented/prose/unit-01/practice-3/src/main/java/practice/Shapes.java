package practice;

import java.util.List;

public class Shapes {

    public abstract static class Shape {
        protected final String color;

        public Shape(String color) {
            throw new UnsupportedOperationException("write the Shape constructor");
        }

        public abstract double calculateArea();

        public String displayColor() {
            throw new UnsupportedOperationException("write displayColor");
        }
    }

    public static class Circle extends Shape {

        public Circle(String color, double radius) {
            super(color);
            throw new UnsupportedOperationException("write the Circle constructor");
        }

        @Override
        public double calculateArea() {
            throw new UnsupportedOperationException("write Circle.calculateArea");
        }
    }

    public static class Rectangle extends Shape {

        public Rectangle(String color, double width, double height) {
            super(color);
            throw new UnsupportedOperationException("write the Rectangle constructor");
        }

        @Override
        public double calculateArea() {
            throw new UnsupportedOperationException("write Rectangle.calculateArea");
        }
    }

    /** The sum of every shape's area; 0.0 for no shapes. */
    public static double totalArea(List<? extends Shape> shapes) {
        throw new UnsupportedOperationException("write totalArea");
    }
}
