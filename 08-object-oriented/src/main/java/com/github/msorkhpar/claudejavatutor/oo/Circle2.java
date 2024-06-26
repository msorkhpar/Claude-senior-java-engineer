package com.github.msorkhpar.claudejavatutor.oo;

// Circle class implementing Shape
class Circle2 implements Shape2 {
    private double radius;

    public Circle2(double radius) {
        this.radius = radius;
    }

    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }
}