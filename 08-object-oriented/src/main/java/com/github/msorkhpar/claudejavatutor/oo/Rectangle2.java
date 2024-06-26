package com.github.msorkhpar.claudejavatutor.oo;

// Rectangle class implementing Shape
class Rectangle2 implements Shape2 {
    private double length;
    private double width;

    public Rectangle2(double length, double width) {
        this.length = length;
        this.width = width;
    }

    @Override
    public double calculateArea() {
        return length * width;
    }
}
