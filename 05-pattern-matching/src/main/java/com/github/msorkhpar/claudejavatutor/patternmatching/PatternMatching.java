package com.github.msorkhpar.claudejavatutor.patternmatching;

public class PatternMatching {

    public static String processShape(Object obj) {
        if (obj instanceof Square square) {
            return "Square with side length: " + square.getSideLength();
        } else if (obj instanceof Circle circle) {
            return "Circle with radius: " + circle.getRadius();
        } else if (obj instanceof Rectangle rectangle) {
            return "Rectangle with width: " + rectangle.getWidth() + " and height: " + rectangle.getHeight();
        } else {
            return "Unknown shape";
        }
    }

    public static double calculateArea(Object obj) {
        if (obj instanceof Square square) {
            return square.getSideLength() * square.getSideLength();
        } else if (obj instanceof Circle circle) {
            return Math.PI * circle.getRadius() * circle.getRadius();
        } else if (obj instanceof Rectangle rectangle) {
            return rectangle.getWidth() * rectangle.getHeight();
        } else {
            throw new IllegalArgumentException("Unknown shape");
        }
    }

    // Inner classes for demonstration
    static class Square {
        private final double sideLength;

        public Square(double sideLength) {
            this.sideLength = sideLength;
        }

        public double getSideLength() {
            return sideLength;
        }
    }

    static class Circle {
        private final double radius;

        public Circle(double radius) {
            this.radius = radius;
        }

        public double getRadius() {
            return radius;
        }
    }

    static class Rectangle {
        private final double width;
        private final double height;

        public Rectangle(double width, double height) {
            this.width = width;
            this.height = height;
        }

        public double getWidth() {
            return width;
        }

        public double getHeight() {
            return height;
        }
    }
}