package com.github.msorkhpar.claudejavatutor.trycatch;

// Sealed class example
public abstract sealed class Shape permits Circle, Square, Triangle {
    private final String name;

    protected Shape(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract double area();
}

// Permitted subclasses
final class Circle extends Shape {
    private final double radius;

    public Circle(double radius) {
        super("Circle");
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}

final class Square extends Shape {
    private final double side;

    public Square(double side) {
        super("Square");
        this.side = side;
    }

    @Override
    public double area() {
        return side * side;
    }
}

non-sealed class Triangle extends Shape {
    private final double base;
    private final double height;

    public Triangle(double base, double height) {
        super("Triangle");
        this.base = base;
        this.height = height;
    }

    @Override
    public double area() {
        return 0.5 * base * height;
    }
}

// Sealed interface example
sealed interface Vehicle permits Car, Motorcycle, Truck {
    String getType();

    int getWheels();
}

record Car(String model) implements Vehicle {
    @Override
    public String getType() {
        return "Car";
    }

    @Override
    public int getWheels() {
        return 4;
    }
}

record Motorcycle(String brand) implements Vehicle {
    @Override
    public String getType() {
        return "Motorcycle";
    }

    @Override
    public int getWheels() {
        return 2;
    }
}

record Truck(int capacity) implements Vehicle {
    @Override
    public String getType() {
        return "Truck";
    }

    @Override
    public int getWheels() {
        return 6;
    }
}