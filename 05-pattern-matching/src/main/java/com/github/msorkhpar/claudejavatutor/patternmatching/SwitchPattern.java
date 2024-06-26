package com.github.msorkhpar.claudejavatutor.patternmatching;

public class SwitchPattern {

    public static String processShape(Shape shape) {
        return switch (shape) {
            case Circle c -> "Circle with radius " + c.radius();
            case Rectangle r -> "Rectangle with width " + r.width() + " and height " + r.height();
            case Triangle t -> "Triangle with base " + t.base() + " and height " + t.height();
            case null -> "No shape provided";
        };
    }

    public static double calculateArea(Shape shape) {
        return switch (shape) {
            case Circle c -> Math.PI * c.radius() * c.radius();
            case Rectangle r -> r.width() * r.height();
            case Triangle t -> 0.5 * t.base() * t.height();
            case null -> 0.0;
        };
    }

    public static String classifyShape(Shape shape) {
        return switch (shape) {
            case Circle c when c.radius() < 5 -> "Small circle";
            case Circle c -> "Large circle";
            case Rectangle r when r.width() == r.height() -> "Square";
            case Rectangle r -> "Rectangle";
            case Triangle t when t.base() == t.height() -> "Equilateral triangle";
            case Triangle t -> "Triangle";
            case null -> "No shape";
        };
    }
}

sealed interface Shape permits Circle, Rectangle, Triangle {}
record Circle(double radius) implements Shape {}
record Rectangle(double width, double height) implements Shape {}
record Triangle(double base, double height) implements Shape {}