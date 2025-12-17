package com.github.msorkhpar.claudejavatutor.oo;

// Shape interface
interface Shape2 {
    double calculateArea();

    default void display() {
        System.out.println("This is a shape with area: " + calculateArea());
    }
}


