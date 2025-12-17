package com.github.msorkhpar.claudejavatutor.oo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ShapeTest {

    @Test
    void testCircleInheritance() {
        Circle circle = new Circle("Red", 5.0);
        assertThat(circle).isInstanceOf(Shape.class);
        assertThat(circle.calculateArea()).isCloseTo(78.54, within(0.01));
        assertThat(circle.toString()).contains("Circle", "Red", "5.0");
    }

    @Test
    void testRectangleInheritance() {
        Rectangle rectangle = new Rectangle("Blue", 4.0, 5.0);
        assertThat(rectangle).isInstanceOf(Shape.class);
        assertThat(rectangle.calculateArea()).isEqualTo(20.0);
        assertThat(rectangle.toString()).contains("Rectangle", "Blue", "4.0", "5.0");
    }

    @Test
    void testPolymorphism() {
        Shape[] shapes = {
                new Circle("Green", 3.0),
                new Rectangle("Yellow", 2.0, 3.0)
        };

        assertThat(shapes[0].calculateArea()).isCloseTo(28.27, within(0.01));
        assertThat(shapes[1].calculateArea()).isEqualTo(6.0);
    }

}