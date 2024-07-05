package com.github.msorkhpar.claudejavatutor.oo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;

class PolymorphismDemoTest {

    private Calculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Calculator();
    }

    @Test
    void testCircleArea() {
        Shape2 circle = new Circle2(5);
        assertThat(circle.calculateArea()).isCloseTo(78.54, within(0.01));
    }

    @Test
    void testRectangleArea() {
        Shape2 rectangle = new Rectangle2(4, 5);
        assertThat(rectangle.calculateArea()).isEqualTo(20.0);
    }

    @Test
    void testPolymorphicBehavior() {
        Shape2 circle = new Circle2(3);
        Shape2 rectangle = new Rectangle2(3, 4);

        assertThat(circle.calculateArea()).isNotEqualTo(rectangle.calculateArea());
        assertThat(circle).isInstanceOf(Shape2.class);
        assertThat(rectangle).isInstanceOf(Shape2.class);
    }

    @Test
    void testCalculatorOverloading() {
        assertThat(calculator.add(5, 10)).isEqualTo(15);
        assertThat(calculator.add(5.5, 10.5)).isCloseTo(16.0, within(0.01));
        assertThat(calculator.add(5, 10, 15)).isEqualTo(30);
    }

}