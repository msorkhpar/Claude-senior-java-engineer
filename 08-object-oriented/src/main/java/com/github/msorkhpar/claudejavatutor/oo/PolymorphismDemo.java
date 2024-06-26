package com.github.msorkhpar.claudejavatutor.oo;

// Main class to demonstrate polymorphism
public class PolymorphismDemo {
    public static void printShapeArea(Shape2 shape) {
        System.out.println("The area of the shape is: " + shape.calculateArea());
        shape.display();
    }

    public static void main(String[] args) {
        Shape2 circle = new Circle2( 5);
        Shape2 rectangle = new Rectangle2( 4, 5);

        printShapeArea(circle);
        printShapeArea(rectangle);

        Calculator calc = new Calculator();
        System.out.println("Sum of 5 and 10: " + calc.add(5, 10));
        System.out.println("Sum of 5.5 and 10.5: " + calc.add(5.5, 10.5));
        System.out.println("Sum of 5, 10, and 15: " + calc.add(5, 10, 15));
    }
}