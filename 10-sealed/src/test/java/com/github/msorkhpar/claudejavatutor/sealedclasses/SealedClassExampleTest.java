package com.github.msorkhpar.claudejavatutor.trycatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SealedClassExampleTest {

    private SealedClassExample example;

    @BeforeEach
    void setUp() {
        example = new SealedClassExample();
    }

    @Test
    void testCircleArea() {
        SealedClassExample.Circle circle = example.new Circle(5);
        double area = example.calculateArea(circle);
        assertThat(area).isCloseTo(78.54, within(0.01));
    }

    @Test
    void testSquareArea() {
        SealedClassExample.Square square = example.new Square(4);
        double area = example.calculateArea(square);
        assertThat(area).isEqualTo(16.0);
    }

    @Test
    void testTriangleArea() {
        SealedClassExample.Triangle triangle = example.new Triangle(3, 4);
        double area = example.calculateArea(triangle);
        assertThat(area).isEqualTo(6.0);
    }

    @Test
    void testExhaustivePatternMatching() {
        SealedClassExample.Shape[] shapes = {
                example.new Circle(2),
                example.new Square(3),
                example.new Triangle(4, 5)
        };

        for (SealedClassExample.Shape shape : shapes) {
            double area = example.calculateArea(shape);
            assertThat(area).isPositive();
        }
    }
}